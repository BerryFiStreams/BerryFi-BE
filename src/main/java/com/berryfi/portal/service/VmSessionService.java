package com.berryfi.portal.service;

import com.berryfi.portal.entity.*;
import com.berryfi.portal.enums.SessionStatus;
import com.berryfi.portal.repository.*;
import com.berryfi.portal.dto.billing.BillingTransactionDto;
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
    private WorkspaceRepository workspaceRepository;

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
    public VmSessionResult startVmSession(String projectId, String userId, String userEmail, String vmType,
                                         String username, String clientIpAddress, String clientCountry, 
                                         String clientCity, String userAgent) {
        try {
            logger.info("Starting VM session for project: {}, user: {}, vmType: {}, clientIP: {}", 
                       projectId, userId, vmType, clientIpAddress);

            // Get project and validate
            Optional<Project> projectOpt = projectRepository.findById(projectId);
            if (projectOpt.isEmpty()) {
                return VmSessionResult.error("Project not found: " + projectId);
            }

            Project project = projectOpt.get();
            String workspaceId = project.getWorkspaceId();
            String organizationId = project.getOrganizationId();

            // Check if user already has an active session
            Optional<VmSession> existingSession = vmSessionRepository.findUserActiveSession(userId);
            if (existingSession.isPresent()) {
                return VmSessionResult.error("User already has an active session: " + existingSession.get().getId());
            }

            // Find available VM of requested type in workspace
            List<VmInstance> availableVms = vmInstanceRepository.findAvailableVmsByTypeInWorkspace(workspaceId, vmType);
            if (availableVms.isEmpty()) {
                return VmSessionResult.error("No available VMs of type " + vmType + " in workspace");
            }

            VmInstance vm = availableVms.get(0); // Take the first available VM

            // Check workspace has sufficient credits (estimate for 1 minute initially)
            if (!billingService.hasWorkspaceSufficientCredits(workspaceId, vmType, 60.0)) {
                return VmSessionResult.error("Insufficient credits in workspace for VM session");
            }

            // Create session with client information
            VmSession session = new VmSession(vm.getId(), projectId, workspaceId, organizationId, userId, userEmail);
            session.setCreditsPerMinute(pricingService.getCurrentCreditsPerMinuteForVm(vmType));
            
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

            // Update session status
            session.markAsStarting();
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
    public VmSessionResult startVmSession(String projectId, String userId, String userEmail, String vmType) {
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
                return VmSessionResult.error("Session not found: " + sessionId);
            }

            VmSession session = sessionOpt.get();

            // Validate user owns this session
            if (!session.getUserId().equals(userId)) {
                return VmSessionResult.error("User does not own this session");
            }

            // Check session is active
            if (!session.isActive()) {
                return VmSessionResult.error("Session is not active: " + session.getStatus());
            }

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
            // Get VM
            Optional<VmInstance> vmOpt = vmInstanceRepository.findById(session.getVmInstanceId());
            if (vmOpt.isEmpty()) {
                logger.error("VM not found for session: {}", session.getId());
                return VmSessionResult.error("VM not found");
            }

            VmInstance vm = vmOpt.get();

            // Mark session as terminating
            session.markAsTerminating(reason);
            vmSessionRepository.save(session);

            // Stop VM in Azure
            vm.markAsStopping();
            vmInstanceRepository.save(vm);

            boolean azureStopped = azureVmService.stopVm(vm);
            if (!azureStopped) {
                logger.warn("Failed to stop VM in Azure, but continuing with session cleanup");
            }

            // Calculate usage and bill
            Long durationSeconds = session.getDurationInSeconds();
            Double creditsUsed = pricingService.calculateVmUsageCredits(vm.getVmType(), durationSeconds.doubleValue());

            // Create billing transaction
            BillingTransactionDto billing = billingService.recordVmUsageForWorkspace(
                session.getWorkspaceId(), 
                session.getProjectId(), 
                vm.getVmType(), 
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
     * Get user's current active session
     */
    public Optional<VmSession> getUserActiveSession(String userId) {
        return vmSessionRepository.findUserActiveSession(userId);
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
