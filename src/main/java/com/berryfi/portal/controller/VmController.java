package com.berryfi.portal.controller;

import com.berryfi.portal.dto.ApiResponse;
import com.berryfi.portal.entity.VmSession;
import com.berryfi.portal.entity.VmInstance;
import com.berryfi.portal.service.VmSessionService;
import com.berryfi.portal.service.VmSessionService.VmSessionResult;
import com.berryfi.portal.service.IpGeolocationService;
import com.berryfi.portal.util.ClientInfoExtractor;
import com.berryfi.portal.util.NumberFormatUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * REST controller for VM session management.
 * Handles VM session lifecycle, heartbeats, and monitoring.
 */
@RestController
@RequestMapping("/api/vm")
@CrossOrigin(origins = "*")
public class VmController {

    private static final Logger logger = LoggerFactory.getLogger(VmController.class);

    @Autowired
    private VmSessionService vmSessionService;

    @Autowired
    private IpGeolocationService ipGeolocationService;

    @Autowired
    private ClientInfoExtractor clientInfoExtractor;

    /**
     * Start a VM session
     */
    @PostMapping("/sessions/start")
    public ResponseEntity<ApiResponse<VmSessionResponseDto>> startSession(
            @RequestBody @Valid StartVmSessionRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            // For open endpoints, we'll use request parameters or default values for user identification
            String userId = request.getEmail() != null ? request.getEmail() : "anonymous-user";
            String userEmail = request.getEmail() != null ? request.getEmail() : "anonymous@berryfi.com";
            String username = (request.getFirstName() != null && request.getLastName() != null) 
                ? request.getFirstName() + " " + request.getLastName() 
                : "Anonymous User";

            // Extract client information
            String clientIp = clientInfoExtractor.getClientIpAddress(httpRequest);
            String userAgent = clientInfoExtractor.getUserAgent(httpRequest);
            
            // Resolve location from IP
            String country = null;
            String city = null;
            if (clientIp != null) {
                IpGeolocationService.LocationInfo location = ipGeolocationService.resolveLocation(clientIp);
                if (location != null && location.hasLocation()) {
                    country = location.getCountry();
                    city = location.getCity();
                }
            }

            VmSessionResult result = vmSessionService.startVmSession(
                request.getProjectId(),
                request.getWorkspaceId(), 
                userId, 
                userEmail, 
                request.getVmType(),
                username,
                clientIp,
                country,
                city,
                userAgent
            );

            if (result.isSuccess()) {
                VmSessionResponseDto response = new VmSessionResponseDto(
                    result.getSession(), 
                    result.getVmInstance()
                );
                return ResponseEntity.ok(ApiResponse.success("VM session started successfully", response));
            } else {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error(result.getMessage()));
            }

        } catch (Exception e) {
            logger.error("Failed to start VM session: " + e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to start VM session: " + e.getMessage()));
        }
    }

    /**
     * Stop a VM session
     */
    @PostMapping("/sessions/{sessionId}/stop")
    public ResponseEntity<ApiResponse<VmSessionResponseDto>> stopSession(
            @PathVariable String sessionId,
            @RequestBody(required = false) StopVmSessionRequest request) {
        
        try {
            String userId = null;
            if (request != null && request.getEmail() != null) {
                userId = request.getEmail();
            }

            VmSessionResult result = vmSessionService.stopVmSession(sessionId, userId);

            if (result.isSuccess()) {
                VmSessionResponseDto response = new VmSessionResponseDto(
                    result.getSession(), 
                    result.getVmInstance()
                );
                return ResponseEntity.ok(ApiResponse.success("VM session stopped successfully", response));
            } else {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error(result.getMessage()));
            }

        } catch (Exception e) {
            logger.error("Failed to stop VM session: " + e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to stop VM session: " + e.getMessage()));
        }
    }

    /**
     * Submit heartbeat for a session
     */
    @PostMapping("/sessions/{sessionId}/heartbeat")
    public ResponseEntity<ApiResponse<String>> submitHeartbeat(
            @PathVariable String sessionId,
            @RequestBody @Valid HeartbeatRequest request) {
        
        try {
            boolean success = vmSessionService.recordHeartbeat(
                sessionId, 
                request.getStatus(), 
                request.getCpuUsage(), 
                request.getMemoryUsage()
            );

            if (success) {
                return ResponseEntity.ok(ApiResponse.success("Heartbeat recorded successfully", null));
            } else {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to record heartbeat"));
            }

        } catch (Exception e) {
            logger.error("Failed to record heartbeat: " + e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to record heartbeat: " + e.getMessage()));
        }
    }

    /**
     * Get session status
     */
    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<ApiResponse<VmSessionResponseDto>> getSession(
            @PathVariable String sessionId) {
        
        try {
            Optional<VmSession> sessionOpt = vmSessionService.getSession(sessionId);
            
            if (sessionOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            VmSession session = sessionOpt.get();
            
            VmSessionResponseDto response = new VmSessionResponseDto(session, null);
            return ResponseEntity.ok(ApiResponse.success("Session retrieved successfully", response));

        } catch (Exception e) {
            logger.error("Failed to get session: " + e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to get session: " + e.getMessage()));
        }
    }

    /**
     * Get user's current active session
     */
    @GetMapping("/sessions/active")
    public ResponseEntity<ApiResponse<VmSessionResponseDto>> getActiveSession(
            @RequestParam(required = false) String email) {
        
        try {
            if (email == null || email.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Email is required"));
            }
            
            Optional<VmSession> sessionOpt = vmSessionService.getUserActiveSession(email);
            
            if (sessionOpt.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success("No active session", null));
            }

            VmSession session = sessionOpt.get();
            VmSessionResponseDto response = new VmSessionResponseDto(session, null);
            return ResponseEntity.ok(ApiResponse.success("Active session retrieved", response));

        } catch (Exception e) {
            logger.error("Failed to get active session: " + e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to get active session: " + e.getMessage()));
        }
    }

    /**
     * Force terminate session (admin endpoint)
     */
    @PostMapping("/sessions/{sessionId}/terminate")
    public ResponseEntity<ApiResponse<VmSessionResponseDto>> terminateSession(
            @PathVariable String sessionId,
            @RequestBody @Valid TerminateSessionRequest request) {
        
        try {
            VmSessionResult result = vmSessionService.forceTerminateSession(sessionId, request.getReason());

            if (result.isSuccess()) {
                VmSessionResponseDto response = new VmSessionResponseDto(
                    result.getSession(), 
                    result.getVmInstance()
                );
                return ResponseEntity.ok(ApiResponse.success("Session terminated successfully", response));
            } else {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error(result.getMessage()));
            }

        } catch (Exception e) {
            logger.error("Failed to terminate session: " + e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to terminate session: " + e.getMessage()));
        }
    }

    // Response DTO
    public static class StartVmSessionRequest {
        @NotBlank(message = "Project ID is required")
        private String projectId;
        
        @NotBlank(message = "Workspace ID is required")
        private String workspaceId;
        
        @NotBlank(message = "VM type is required")
        private String vmType;

        // Optional user fields for non-authenticated access
        private String firstName;
        private String lastName;
        private String email;
        private String phone;

        public String getProjectId() { return projectId; }
        public void setProjectId(String projectId) { this.projectId = projectId; }
        
        public String getWorkspaceId() { return workspaceId; }
        public void setWorkspaceId(String workspaceId) { this.workspaceId = workspaceId; }
        
        public String getVmType() { return vmType; }
        public void setVmType(String vmType) { this.vmType = vmType; }

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }

    public static class StopVmSessionRequest {
        private String email;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public static class HeartbeatRequest {
        @NotBlank(message = "Status is required")
        private String status;
        
        private Double cpuUsage;
        private Double memoryUsage;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public Double getCpuUsage() { return cpuUsage; }
        public void setCpuUsage(Double cpuUsage) { this.cpuUsage = cpuUsage; }
        
        public Double getMemoryUsage() { return memoryUsage; }
        public void setMemoryUsage(Double memoryUsage) { this.memoryUsage = memoryUsage; }
    }

    public static class TerminateSessionRequest {
        @NotBlank(message = "Reason is required")
        private String reason;

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    // Response DTO
    public static class VmSessionResponseDto {
        private String sessionId;
        private String vmInstanceId;
        private String status;
        private LocalDateTime startedAt;
        private LocalDateTime lastHeartbeat;
        private Long durationSeconds;
        private Double creditsUsed;
        private String vmType;
        private String vmStatus;
        private String azureResourceId;
        
        // VM Connection details
        private String vmIpAddress;
        private Integer vmPort;
        private String connectionUrl;
        
        // Client tracking information
        private String username;
        private String clientIpAddress;
        private String clientCountry;
        private String clientCity;
        private String userAgent;

        public VmSessionResponseDto(VmSession session, VmInstance vmInstance) {
            this.sessionId = session.getId();
            this.vmInstanceId = session.getVmInstanceId();
            this.status = session.getStatus().name();
            this.startedAt = session.getStartTime();
            this.lastHeartbeat = session.getLastHeartbeat();
            this.durationSeconds = session.getDurationInSeconds();
            this.creditsUsed = NumberFormatUtil.formatCredits(session.getCreditsUsed());
            
            // Client tracking information
            this.username = session.getUsername();
            this.clientIpAddress = session.getClientIpAddress();
            this.clientCountry = session.getClientCountry();
            this.clientCity = session.getClientCity();
            this.userAgent = session.getUserAgent();
            
            // VM Connection details from session (if available) or VM instance
            this.vmIpAddress = session.getVmIpAddress();
            this.vmPort = session.getVmPort();
            this.connectionUrl = session.getConnectionUrl();
            
            if (vmInstance != null) {
                this.vmType = vmInstance.getVmType();
                this.vmStatus = vmInstance.getStatus().name();
                this.azureResourceId = vmInstance.getAzureResourceId();
                
                // If session doesn't have connection details, use VM instance details
                if (this.vmIpAddress == null) {
                    this.vmIpAddress = vmInstance.getIpAddress();
                }
                if (this.vmPort == null) {
                    this.vmPort = vmInstance.getPort();
                }
                if (this.connectionUrl == null) {
                    this.connectionUrl = vmInstance.getConnectionUrl();
                }
            }
        }

        // Getters for existing fields
        public String getSessionId() { return sessionId; }
        public String getVmInstanceId() { return vmInstanceId; }
        public String getStatus() { return status; }
        public LocalDateTime getStartedAt() { return startedAt; }
        public LocalDateTime getLastHeartbeat() { return lastHeartbeat; }
        public Long getDurationSeconds() { return durationSeconds; }
        public Double getCreditsUsed() { return creditsUsed; }
        public String getVmType() { return vmType; }
        public String getVmStatus() { return vmStatus; }
        public String getAzureResourceId() { return azureResourceId; }
        
        // Getters for VM connection details
        public String getVmIpAddress() { return vmIpAddress; }
        public Integer getVmPort() { return vmPort; }
        public String getConnectionUrl() { return connectionUrl; }
        
        // Getters for client tracking fields
        public String getUsername() { return username; }
        public String getClientIpAddress() { return clientIpAddress; }
        public String getClientCountry() { return clientCountry; }
        public String getClientCity() { return clientCity; }
        public String getUserAgent() { return userAgent; }
    }
}
