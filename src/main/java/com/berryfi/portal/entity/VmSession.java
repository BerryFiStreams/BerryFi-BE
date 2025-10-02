package com.berryfi.portal.entity;

import com.berryfi.portal.enums.SessionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

/**    public String getUserId() {y representing a VM session.
 * Tracks the usage of a VM by a user for a specific project.
 */
@Entity
@Table(name = "vm_sessions", indexes = {
    @Index(name = "idx_session_vm", columnList = "vmInstanceId"),
    @Index(name = "idx_session_project", columnList = "projectId"),
    @Index(name = "idx_session_user", columnList = "userId"),
    @Index(name = "idx_session_status", columnList = "status"),
    @Index(name = "idx_session_start", columnList = "startTime")
})
public class VmSession {
    
    @Id
    @Column(name = "id", length = 50)
    private String id;
    
    @NotBlank(message = "VM instance ID is required")
    @Column(name = "vm_instance_id", nullable = false)
    private String vmInstanceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vm_instance_id", referencedColumnName = "id", insertable = false, updatable = false)
    private VmInstance vmInstance;
    
    @NotBlank(message = "Project ID is required")
    @Column(name = "project_id", nullable = false)
    private String projectId;
    
    @NotBlank(message = "Workspace ID is required")
    @Column(name = "workspace_id", nullable = false)
    private String workspaceId;
    
    @NotBlank(message = "Organization ID is required")
    @Column(name = "organization_id", nullable = false)
    private String organizationId;
    
    @NotBlank(message = "User ID is required")
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "user_email")
    private String userEmail;
    
    @Column(name = "username")
    private String username;
    
    @Column(name = "client_ip_address")
    private String clientIpAddress;
    
    @Column(name = "client_country")
    private String clientCountry;
    
    @Column(name = "client_city") 
    private String clientCity;
    
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;
    
    @NotNull(message = "Session status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SessionStatus status = SessionStatus.REQUESTED;
    
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    @Column(name = "last_heartbeat")
    private LocalDateTime lastHeartbeat;
    
    @Column(name = "heartbeat_count")
    private Integer heartbeatCount = 0;
    
    @Column(name = "duration_seconds")
    private Long durationSeconds;
    
    @Column(name = "credits_used")
    private Double creditsUsed = 0.0;
    
    @Column(name = "credits_per_minute")
    private Double creditsPerMinute;
    
    @Column(name = "billing_transaction_id")
    private String billingTransactionId;
    
    @Column(name = "vm_ip_address")
    private String vmIpAddress;
    
    @Column(name = "vm_port")
    private Integer vmPort;
    
    @Column(name = "connection_url")
    private String connectionUrl;
    
    @Column(name = "termination_reason")
    private String terminationReason;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Constructors
    public VmSession() {
        this.id = generateSessionId();
        this.startTime = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public VmSession(String vmInstanceId, String projectId, String userId) {
        this.id = generateSessionId();
        this.vmInstanceId = vmInstanceId;
        this.projectId = projectId;
        this.userId = userId;
        this.status = SessionStatus.STARTING;
        this.startTime = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    private String generateSessionId() {
        return "sess_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    // Helper methods
    public boolean isActive() {
        return this.status == SessionStatus.ACTIVE;
    }

    public boolean isCompleted() {
        return this.status == SessionStatus.COMPLETED || this.status == SessionStatus.TERMINATED;
    }

    public boolean needsHeartbeat() {
        if (lastHeartbeat == null) return true;
        return LocalDateTime.now().minusMinutes(2).isAfter(lastHeartbeat);
    }

    public boolean hasTimedOut(int timeoutMinutes) {
        if (lastHeartbeat == null) {
            return LocalDateTime.now().minusMinutes(timeoutMinutes).isAfter(startTime);
        }
        return LocalDateTime.now().minusMinutes(timeoutMinutes).isAfter(lastHeartbeat);
    }

    public void recordHeartbeat() {
        this.lastHeartbeat = LocalDateTime.now();
        this.heartbeatCount = (this.heartbeatCount != null ? this.heartbeatCount : 0) + 1;
        if (this.status == SessionStatus.STARTING || this.status == SessionStatus.REQUESTED) {
            this.status = SessionStatus.ACTIVE;
        }
    }

    public void markAsStarting() {
        this.status = SessionStatus.STARTING;
    }

    public void markAsActive(String ipAddress, Integer port) {
        this.status = SessionStatus.ACTIVE;
        this.vmIpAddress = ipAddress;
        this.vmPort = port;
        if (ipAddress != null && port != null) {
            this.connectionUrl = String.format("http://%s:%d", ipAddress, port);
        }
    }

    public void markAsTerminating(String reason) {
        this.status = SessionStatus.TERMINATING;
        this.terminationReason = reason;
    }

    public void markAsCompleted(Double totalCreditsUsed, String billingTransactionId) {
        this.status = SessionStatus.COMPLETED;
        this.endTime = LocalDateTime.now();
        this.creditsUsed = totalCreditsUsed;
        this.billingTransactionId = billingTransactionId;
        
        // Calculate duration
        if (this.startTime != null && this.endTime != null) {
            this.durationSeconds = java.time.Duration.between(this.startTime, this.endTime).toSeconds();
        }
    }

    public void markAsTerminated(String reason, Double totalCreditsUsed, String billingTransactionId) {
        this.status = SessionStatus.TERMINATED;
        this.endTime = LocalDateTime.now();
        this.terminationReason = reason;
        this.creditsUsed = totalCreditsUsed;
        this.billingTransactionId = billingTransactionId;
        
        // Calculate duration
        if (this.startTime != null && this.endTime != null) {
            this.durationSeconds = java.time.Duration.between(this.startTime, this.endTime).toSeconds();
        }
    }

    public void markAsFailed(String reason) {
        this.status = SessionStatus.FAILED;
        this.endTime = LocalDateTime.now();
        this.terminationReason = reason;
    }

    public Long getDurationInSeconds() {
        if (durationSeconds != null) return durationSeconds;
        if (startTime == null) return 0L;
        
        LocalDateTime endPoint = endTime != null ? endTime : LocalDateTime.now();
        return java.time.Duration.between(startTime, endPoint).toSeconds();
    }

    public Double getDurationInMinutes() {
        return getDurationInSeconds() / 60.0;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVmInstanceId() {
        return vmInstanceId;
    }

    public VmInstance getVmInstance() {
        return vmInstance;
    }

    public void setVmInstanceId(String vmInstanceId) {
        this.vmInstanceId = vmInstanceId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getClientIpAddress() {
        return clientIpAddress;
    }

    public void setClientIpAddress(String clientIpAddress) {
        this.clientIpAddress = clientIpAddress;
    }

    public String getClientCountry() {
        return clientCountry;
    }

    public void setClientCountry(String clientCountry) {
        this.clientCountry = clientCountry;
    }

    public String getClientCity() {
        return clientCity;
    }

    public void setClientCity(String clientCity) {
        this.clientCity = clientCity;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public LocalDateTime getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(LocalDateTime lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    public Integer getHeartbeatCount() {
        return heartbeatCount;
    }

    public void setHeartbeatCount(Integer heartbeatCount) {
        this.heartbeatCount = heartbeatCount;
    }

    public Long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public Double getCreditsUsed() {
        return creditsUsed;
    }

    public void setCreditsUsed(Double creditsUsed) {
        this.creditsUsed = creditsUsed;
    }

    public Double getCreditsPerMinute() {
        return creditsPerMinute;
    }

    public void setCreditsPerMinute(Double creditsPerMinute) {
        this.creditsPerMinute = creditsPerMinute;
    }

    public String getBillingTransactionId() {
        return billingTransactionId;
    }

    public void setBillingTransactionId(String billingTransactionId) {
        this.billingTransactionId = billingTransactionId;
    }

    public String getVmIpAddress() {
        return vmIpAddress;
    }

    public void setVmIpAddress(String vmIpAddress) {
        this.vmIpAddress = vmIpAddress;
    }

    public Integer getVmPort() {
        return vmPort;
    }

    public void setVmPort(Integer vmPort) {
        this.vmPort = vmPort;
    }

    public String getConnectionUrl() {
        return connectionUrl;
    }

    public void setConnectionUrl(String connectionUrl) {
        this.connectionUrl = connectionUrl;
    }

    public String getTerminationReason() {
        return terminationReason;
    }

    public void setTerminationReason(String terminationReason) {
        this.terminationReason = terminationReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
