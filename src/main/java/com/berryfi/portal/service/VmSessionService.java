package com.berryfi.portal.service;

import com.berryfi.portal.entity.*;
import com.berryfi.portal.enums.VmType;
import com.berryfi.portal.enums.SessionStatus;
import com.berryfi.portal.enums.VmStatus;
import com.berryfi.portal.repository.*;
import com.berryfi.portal.dto.billing.BillingTransactionDto;
import com.berryfi.portal.dto.billing.BillingBalanceDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing VM sessions.
 * Handles session lifecycle, VM assignment, and billing integration.
 */
@Service
@Transactional
public class VmSessionService {

    private static final Logger logger = LoggerFactory.getLogger(VmSessionService.class);

    @Autowired
    private VmInstanceRepository vmInstanceRepository;

    @Autowired
    private VmSessionRepository vmSessionRepository;

    @Autowired
    private VmHeartbeatRepository vmHeartbeatRepository;

    @Autowired
    private ProjectRepository projectRepository;



    @Autowired
    private BillingService billingService;

    @Autowired
    private PricingService pricingService;

    @Autowired
    private AzureVmService azureVmService;

    @Value("${vm.heartbeat.timeout.seconds:30}")
    private int heartbeatTimeoutSeconds;

    @Value("${vm.session.max.duration.hours:8}")
    private int maxSessionDurationHours;

    /**
     * Start a VM session for a project with client tracking information
     */
    public VmSessionResult startVmSession(String projectId, String userId, String userEmail, VmType vmType,
                                         String username, String clientIpAddress, String clientCountry, 
                                         String clientCity, String userAgent) {
        try {
            logger.info("Starting VM session for project: {}, user: {}, vmType: {}, clientIP: {}", 
                       projectId, userId, vmType, clientIpAddress);
            
            // Validate input parameters
            if (projectId == null || projectId.isEmpty()) {
                return VmSessionResult.error("Project ID cannot be null or empty");
            }

            if (userId == null || userId.isEmpty()) {
                return VmSessionResult.error("User ID cannot be null or empty");
            }

            // Get project and validate
            Optional<Project> projectOpt = projectRepository.findById(projectId);
            if (projectOpt.isEmpty()) {
                return VmSessionResult.error("Project not found: " + projectId);
            }

            Project project = projectOpt.get();
            String organizationId = project.getOrganizationId();
            
            // Validate project data
            if (organizationId == null || organizationId.isEmpty()) {
                logger.error("Project {} has null or empty organization ID", projectId);
                return VmSessionResult.error("Project has invalid organization ID");
            }
            


            // Check if user already has an active session
            Optional<VmSession> existingSession = vmSessionRepository.findUserActiveSession(userId);
            if (existingSession.isPresent()) {
                return VmSessionResult.error("User already has an active session: " + existingSession.get().getId());
            }

            // Find available VM - either of specified type or any available type
            List<VmInstance> availableVms;
            String vmTypeStr;
            if (vmType != null) {
                // User specified a VM type
                availableVms = vmInstanceRepository.findAvailableVmsByTypeForProject(projectId, vmType.getValue());
                vmTypeStr = vmType.getValue();
                if (availableVms.isEmpty()) {
                    return VmSessionResult.error("No available VMs of type " + vmTypeStr + " for this project");
                }
            } else {
                // Auto-select any available VM
                availableVms = vmInstanceRepository.findAvailableVmsForProject(projectId);
                if (availableVms.isEmpty()) {
                    return VmSessionResult.error("No available VMs for this project");
                }
                vmTypeStr = availableVms.get(0).getVmType().getValue();
                logger.info("Auto-selected VM type: {} for project: {}", vmTypeStr, projectId);
            }

            VmInstance vm = availableVms.get(0); // Take the first available VM

            // Validate VM has proper ID
            if (vm.getId() == null || vm.getId().isEmpty()) {
                logger.error("VM instance has null or empty ID: {}", vm);
                return VmSessionResult.error("VM instance has invalid ID");
            }
            
            // Double-check that this VM doesn't have any active session to prevent race conditions
            Optional<VmSession> activeSessionForVm = vmSessionRepository.findActiveSessionForVm(vm.getId());
            if (activeSessionForVm.isPresent()) {
                logger.warn("VM {} is already in use by session {} (status: {}), finding another available VM", 
                    vm.getId(), activeSessionForVm.get().getId(), activeSessionForVm.get().getStatus());
                // Try to find another available VM (remove the first one from the list and try again)
                availableVms.remove(0);
                if (availableVms.isEmpty()) {
                    return VmSessionResult.error("No available VMs" + (vmType != null ? " of type " + vmTypeStr : "") + " for this project (all are currently in use)");
                }
                vm = availableVms.get(0);
                vmTypeStr = vm.getVmType().getValue(); // Update type in case it changed
                
                // Re-check the new VM
                activeSessionForVm = vmSessionRepository.findActiveSessionForVm(vm.getId());
                if (activeSessionForVm.isPresent()) {
                    return VmSessionResult.error("No available VMs" + (vmType != null ? " of type " + vmTypeStr : "") + " for this project (all are currently in use)");
                }
            }

            // Check organization has sufficient credits (estimate for 1 minute initially)
            Double estimatedCredits = pricingService.calculateVmUsageCredits(vmTypeStr, 60.0);
            BillingBalanceDto balance = billingService.getBillingBalance(organizationId);
            if (balance.getCurrentBalance() < estimatedCredits) {
                return VmSessionResult.error("Insufficient credits in organization for VM session");
            }

            // Create session with client information
            VmSession session = new VmSession(vm.getId(), projectId, userId);
            session.setOrganizationId(organizationId);
            session.setUserEmail(userEmail);
            session.setCreditsPerMinute(pricingService.getCurrentCreditsPerMinuteForVm(vmTypeStr));
            
            // Set client tracking information
            session.setUsername(username);
            session.setClientIpAddress(clientIpAddress);
            session.setClientCountry(clientCountry);
            session.setClientCity(clientCity);
            session.setUserAgent(userAgent);

            // Assign VM to session
            vm.assignToSession(projectId, session.getId());
            vm.markAsStarting();

            // Save entities
            vmInstanceRepository.save(vm);
            session = vmSessionRepository.save(session);

            // Start VM in Azure
            boolean azureStarted = azureVmService.startVm(vm);
            if (!azureStarted) {
                // Rollback
                vm.releaseFromSession();
                session.markAsFailed("Failed to start VM in Azure");
                vmInstanceRepository.save(vm);
                vmSessionRepository.save(session);
                return VmSessionResult.error("Failed to start VM in Azure");
            }

            // Update session status and set start time now that VM has actually started
            session.markAsStarting();
            session.setStartTime(LocalDateTime.now());
            session = vmSessionRepository.save(session);

            logger.info("VM session started successfully: {}", session.getId());
            return VmSessionResult.success(session, vm);

        } catch (Exception e) {
            logger.error("Failed to start VM session: " + e.getMessage(), e);
            return VmSessionResult.error("Internal error: " + e.getMessage());
        }
    }

    /**
     * Start a VM session for a project (backward compatibility)
     */
    public VmSessionResult startVmSession(String projectId, String userId, String userEmail, VmType vmType) {
        return startVmSession(projectId, userId, userEmail, vmType, null, null, null, null, null);
    }

    /**
     * Stop a VM session
     */
    public VmSessionResult stopVmSession(String sessionId, String userId) {
        try {
            logger.info("Stopping VM session: {} by user: {}", sessionId, userId);

            Optional<VmSession> sessionOpt = vmSessionRepository.findById(sessionId);
            if (sessionOpt.isEmpty()) {
                logger.error("Session not found: {}", sessionId);
                return VmSessionResult.error("Session not found: " + sessionId);
            }

            VmSession session = sessionOpt.get();
            logger.debug("Found session: {} with status: {}", sessionId, session.getStatus());

            // Check session is active
            if (!session.isActive()) {
                logger.warn("Session {} is not active (status: {}), cannot stop", sessionId, session.getStatus());
                return VmSessionResult.error("Session is not active: " + session.getStatus());
            }

            logger.info("Session {} is active, proceeding to stop", sessionId);
            return stopSessionInternal(session, "User requested stop");

        } catch (Exception e) {
            logger.error("Failed to stop VM session: " + e.getMessage(), e);
            return VmSessionResult.error("Internal error: " + e.getMessage());
        }
    }

    /**
     * Internal method to stop a session
     */
    private VmSessionResult stopSessionInternal(VmSession session, String reason) {
        try {
            logger.info("stopSessionInternal called for session: {} (reason: {})", session.getId(), reason);
            
            // Get VM
            Optional<VmInstance> vmOpt = vmInstanceRepository.findById(session.getVmInstanceId());
            if (vmOpt.isEmpty()) {
                logger.error("VM not found for session: {}", session.getId());
                return VmSessionResult.error("VM not found");
            }

            VmInstance vm = vmOpt.get();
            logger.info("Found VM: {} (type: {}, status: {}) for session: {}", 
                vm.getVmName(), vm.getVmType(), vm.getStatus(), session.getId());

            // Mark session as terminating
            session.markAsTerminating(reason);
            vmSessionRepository.save(session);
            logger.debug("Session {} marked as terminating", session.getId());

            // Stop VM in Azure
            vm.markAsStopping();
            vmInstanceRepository.save(vm);
            logger.debug("VM {} marked as stopping in database", vm.getVmName());

            logger.info("Calling azureVmService.stopVm() for VM: {}", vm.getVmName());
            boolean azureStopped = azureVmService.stopVm(vm);
            if (!azureStopped) {
                logger.warn("Failed to stop VM in Azure, but continuing with session cleanup");
            } else {
                logger.info("Azure VM stop completed successfully for: {}", vm.getVmName());
            }

            // Calculate usage and bill
            Long durationSeconds = session.getDurationInSeconds();
            Double creditsUsed = pricingService.calculateVmUsageCredits(vm.getVmType().getValue(), durationSeconds.doubleValue());
            
            // Round credits to 2 decimal places
            creditsUsed = Math.round(creditsUsed * 100.0) / 100.0;

            // Create billing transaction
            BillingTransactionDto billing = billingService.recordVmUsage(
                session.getOrganizationId(), 
                vm.getVmType().getValue(), 
                durationSeconds.doubleValue(), 
                session.getId()
            );

            // Mark session as completed
            session.markAsCompleted(creditsUsed, billing.getId());
            session = vmSessionRepository.save(session);

            // Release VM
            vm.markAsStopped();
            vm.releaseFromSession();
            vmInstanceRepository.save(vm);

            logger.info("VM session stopped successfully: {} (Duration: {} seconds, Credits used: {})", 
                session.getId(), durationSeconds, creditsUsed);

            return VmSessionResult.success(session, vm);

        } catch (Exception e) {
            logger.error("Failed to stop session internally: " + e.getMessage(), e);
            return VmSessionResult.error("Internal error: " + e.getMessage());
        }
    }

    /**
     * Record heartbeat for a session
     */
    public boolean recordHeartbeat(String sessionId, String status, Double cpuUsage, Double memoryUsage) {
        try {
            Optional<VmSession> sessionOpt = vmSessionRepository.findById(sessionId);
            if (sessionOpt.isEmpty()) {
                logger.warn("Heartbeat received for non-existent session: {}", sessionId);
                return false;
            }

            VmSession session = sessionOpt.get();

            // Update session heartbeat
            session.recordHeartbeat();
            vmSessionRepository.save(session);

            // Create heartbeat record
            VmHeartbeat heartbeat = new VmHeartbeat(sessionId, status, cpuUsage, memoryUsage);
            vmHeartbeatRepository.save(heartbeat);

            logger.debug("Heartbeat recorded for session: {}", sessionId);
            return true;

        } catch (Exception e) {
            logger.error("Failed to record heartbeat for session {}: {}", sessionId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Get active sessions that have timed out
     */
    public List<VmSession> getTimedOutSessions() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusSeconds(heartbeatTimeoutSeconds);
        // Ensure the VM has been active for at least 30 seconds before considering it for termination
        LocalDateTime minActiveTime = LocalDateTime.now().minusSeconds(heartbeatTimeoutSeconds);
        return vmSessionRepository.findTimedOutSessions(cutoffTime, minActiveTime);
    }

    /**
     * Get long-running sessions that exceed max duration
     */
    public List<VmSession> getLongRunningSessions() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(maxSessionDurationHours);
        return vmSessionRepository.findLongRunningSessions(cutoffTime);
    }

    /**
     * Force terminate a session (admin function)
     */
    public VmSessionResult forceTerminateSession(String sessionId, String reason) {
        try {
            Optional<VmSession> sessionOpt = vmSessionRepository.findById(sessionId);
            if (sessionOpt.isEmpty()) {
                return VmSessionResult.error("Session not found: " + sessionId);
            }

            VmSession session = sessionOpt.get();
            return stopSessionInternal(session, "Force terminated: " + reason);

        } catch (Exception e) {
            logger.error("Failed to force terminate session: " + e.getMessage(), e);
            return VmSessionResult.error("Internal error: " + e.getMessage());
        }
    }

    /**
     * Get session status
     */
    public Optional<VmSession> getSession(String sessionId) {
        return vmSessionRepository.findById(sessionId);
    }

    /**
     * Get session with real-time Azure VM status
     */
    public Optional<VmSession> getSessionWithRealTimeStatus(String sessionId) {
        Optional<VmSession> sessionOpt = vmSessionRepository.findById(sessionId);
        
        if (sessionOpt.isEmpty()) {
            return sessionOpt;
        }
        
        VmSession session = sessionOpt.get();
        
        // Fetch VM instance to populate session details from database
        try {
            Optional<VmInstance> vmOpt = vmInstanceRepository.findById(session.getVmInstanceId());
            if (vmOpt.isPresent()) {
                VmInstance vm = vmOpt.get();
                logger.debug("Found VM instance {} with IP: {}, Port: {}, ConnectionURL: {}", 
                    vm.getId(), vm.getIpAddress(), vm.getPort(), vm.getConnectionUrl());
                logger.debug("Session {} has IP: {}, Port: {}, ConnectionURL: {}", 
                    session.getId(), session.getVmIpAddress(), session.getVmPort(), session.getConnectionUrl());
                
                boolean vmUpdated = false;
                boolean sessionUpdated = false;
                
                // Only fetch real-time status for active/starting sessions
                if (session.getStatus() == SessionStatus.ACTIVE || 
                    session.getStatus() == SessionStatus.STARTING) {
                    
                    // Get real-time Azure VM status
                    VmStatus azureStatus = azureVmService.getVmStatus(vm);
                    
                    // Update VM status if different
                    if (vm.getStatus() != azureStatus) {
                        logger.info("Updating VM {} status from {} to {} (real-time check)", 
                            vm.getVmName(), vm.getStatus(), azureStatus);
                        vm.setStatus(azureStatus);
                        vmUpdated = true;
                    }
                    
                    if (vmUpdated) {
                        vm = vmInstanceRepository.save(vm);
                    }
                }
                
                // Copy VM connection details to session if not already set
                if (session.getVmIpAddress() == null && vm.getIpAddress() != null) {
                    logger.info("Copying IP address from VM {} to session {}: {}", 
                        vm.getId(), session.getId(), vm.getIpAddress());
                    session.setVmIpAddress(vm.getIpAddress());
                    sessionUpdated = true;
                }
                if (session.getVmPort() == null && vm.getPort() != null) {
                    logger.info("Copying port from VM {} to session {}: {}", 
                        vm.getId(), session.getId(), vm.getPort());
                    session.setVmPort(vm.getPort());
                    sessionUpdated = true;
                }
                if (session.getConnectionUrl() == null && vm.getConnectionUrl() != null) {
                    logger.info("Copying connection URL from VM {} to session {}: {}", 
                        vm.getId(), session.getId(), vm.getConnectionUrl());
                    session.setConnectionUrl(vm.getConnectionUrl());
                    sessionUpdated = true;
                }
                
                if (sessionUpdated) {
                    logger.info("Saving session {} with updated connection details", session.getId());
                    session = vmSessionRepository.save(session);
                } else {
                    logger.debug("No session updates needed - session already has connection details or VM has none");
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to fetch real-time Azure status for session {}: {}", 
                sessionId, e.getMessage());
        }
        
        return Optional.of(session);
    }

    /**
     * Get user's current active session
     */
    public Optional<VmSession> getUserActiveSession(String userId) {
        return vmSessionRepository.findUserActiveSession(userId);
    }

    /**
     * Update session user details and location
     */
    public VmSessionResult updateSessionDetails(String sessionId, String firstName, String lastName, 
                                                String email, String phone, Double latitude, Double longitude) {
        try {
            Optional<VmSession> sessionOpt = vmSessionRepository.findById(sessionId);
            if (sessionOpt.isEmpty()) {
                return VmSessionResult.error("Session not found: " + sessionId);
            }

            VmSession session = sessionOpt.get();
            
            // Check if session is still active
            if (session.isCompleted()) {
                return VmSessionResult.error("Cannot update completed session");
            }

            // Update user details if provided
            if (firstName != null && !firstName.isEmpty()) {
                session.setUserFirstName(firstName);
            }
            
            if (lastName != null && !lastName.isEmpty()) {
                session.setUserLastName(lastName);
            }
            
            // Update username if both names are provided
            if ((firstName != null && !firstName.isEmpty()) && (lastName != null && !lastName.isEmpty())) {
                session.setUsername(firstName + " " + lastName);
            }
            
            if (email != null && !email.isEmpty()) {
                session.setUserEmail(email);
                // Update userId if email is provided (since userId is email in this system)
                session.setUserId(email);
            }
            
            if (phone != null && !phone.isEmpty()) {
                session.setUserPhone(phone);
            }
            
            // Update location if provided
            if (latitude != null) {
                session.setClientLatitude(latitude);
            }
            
            if (longitude != null) {
                session.setClientLongitude(longitude);
            }

            // Save updated session
            session = vmSessionRepository.save(session);
            
            logger.info("Updated session details for: {}", sessionId);
            return VmSessionResult.success(session, null);

        } catch (Exception e) {
            logger.error("Failed to update session details: " + e.getMessage(), e);
            return VmSessionResult.error("Internal error: " + e.getMessage());
        }
    }

    /**
     * Result class for VM session operations
     */
    public static class VmSessionResult {
        private boolean success;
        private String message;
        private VmSession session;
        private VmInstance vmInstance;

        public static VmSessionResult success(VmSession session, VmInstance vm) {
            VmSessionResult result = new VmSessionResult();
            result.success = true;
            result.session = session;
            result.vmInstance = vm;
            result.message = "Success";
            return result;
        }

        public static VmSessionResult error(String message) {
            VmSessionResult result = new VmSessionResult();
            result.success = false;
            result.message = message;
            return result;
        }

        // Getters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public VmSession getSession() { return session; }
        public VmInstance getVmInstance() { return vmInstance; }
    }
}
