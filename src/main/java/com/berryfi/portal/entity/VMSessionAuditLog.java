package com.berryfi.portal.entity;

import com.berryfi.portal.enums.SessionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing VM session-specific audit log entries.
 * This tracks workspace-level VM session actions and provides access control
 * based on workspace membership rather than organization-level permissions.
 */
@Entity
@Table(name = "vm_session_audit_logs", indexes = {
    @Index(name = "idx_vm_audit_workspace", columnList = "workspaceId"),
    @Index(name = "idx_vm_audit_user", columnList = "userId"),
    @Index(name = "idx_vm_audit_session", columnList = "sessionId"),
    @Index(name = "idx_vm_audit_timestamp", columnList = "timestamp"),
    @Index(name = "idx_vm_audit_action", columnList = "action"),
    @Index(name = "idx_vm_audit_organization", columnList = "organizationId")
})
public class VMSessionAuditLog {

    @Id
    @Column(name = "id", length = 50)
    private String id;

    @NotBlank(message = "User ID is required")
    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "user_email")
    private String userEmail;

    @NotBlank(message = "Organization ID is required")
    @Column(name = "organization_id", nullable = false)
    private String organizationId;

    @NotBlank(message = "Workspace ID is required")
    @Column(name = "workspace_id", nullable = false)
    private String workspaceId;

    @Column(name = "workspace_name")
    private String workspaceName;

    @Column(name = "project_id")
    private String projectId;

    @Column(name = "project_name")
    private String projectName;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "vm_instance_id")
    private String vmInstanceId;

    @Column(name = "vm_instance_type")
    private String vmInstanceType;

    @NotBlank(message = "Action is required")
    @Column(name = "action", nullable = false)
    private String action; // VM_SESSION_START, VM_SESSION_STOP, VM_SESSION_PAUSE, VM_SESSION_RESUME, VM_SESSION_TERMINATE, etc.

    @Column(name = "resource", nullable = false)
    private String resource = "VM_SESSION";

    @Column(name = "resource_id")
    private String resourceId;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details; // JSON string with session-specific details

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @NotNull(message = "Timestamp is required")
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "status")
    private String status = "SUCCESS"; // SUCCESS, FAILED, PENDING

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    // VM Session specific fields
    @Column(name = "session_duration_seconds")
    private Long sessionDurationSeconds;

    @Column(name = "credits_used")
    private Double creditsUsed;

    @Column(name = "session_status")
    @Enumerated(EnumType.STRING)
    private SessionStatus sessionStatus;

    @Column(name = "vm_ip_address")
    private String vmIpAddress;

    @Column(name = "vm_port")
    private Integer vmPort;

    @Column(name = "connection_url")
    private String connectionUrl;

    @Column(name = "termination_reason")
    private String terminationReason;

    @Column(name = "heartbeat_count")
    private Integer heartbeatCount;

    @Column(name = "client_country")
    private String clientCountry;

    @Column(name = "client_city")
    private String clientCity;

    // Metadata
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public VMSessionAuditLog() {
        this.id = generateAuditId();
        this.timestamp = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public VMSessionAuditLog(String userId, String userName, String userEmail, String organizationId, 
                           String workspaceId, String workspaceName, String action) {
        this();
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.organizationId = organizationId;
        this.workspaceId = workspaceId;
        this.workspaceName = workspaceName;
        this.action = action;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    private String generateAuditId() {
        return "vmaudit_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    // Helper methods for creating VM session audit logs
    public static VMSessionAuditLog createSessionStartLog(VmSession session, String userName, String userEmail, 
                                                         String workspaceName, String projectName) {
        VMSessionAuditLog log = new VMSessionAuditLog(
            session.getUserId(), userName, userEmail, session.getOrganizationId(),
            session.getWorkspaceId(), workspaceName, "VM_SESSION_START"
        );
        log.setSessionId(session.getId());
        log.setVmInstanceId(session.getVmInstanceId());
        log.setProjectId(session.getProjectId());
        log.setProjectName(projectName);
        log.setResourceId(session.getId());
        log.setClientIpAddress(session.getClientIpAddress());
        log.setUserAgent(session.getUserAgent());
        log.setClientCountry(session.getClientCountry());
        log.setClientCity(session.getClientCity());
        log.setSessionStatus(session.getStatus());
        return log;
    }

    public static VMSessionAuditLog createSessionStopLog(VmSession session, String userName, String userEmail,
                                                        String workspaceName, String projectName, String reason) {
        VMSessionAuditLog log = new VMSessionAuditLog(
            session.getUserId(), userName, userEmail, session.getOrganizationId(),
            session.getWorkspaceId(), workspaceName, "VM_SESSION_STOP"
        );
        log.setSessionId(session.getId());
        log.setVmInstanceId(session.getVmInstanceId());
        log.setProjectId(session.getProjectId());
        log.setProjectName(projectName);
        log.setResourceId(session.getId());
        log.setSessionDurationSeconds(session.getDurationInSeconds());
        log.setCreditsUsed(session.getCreditsUsed());
        log.setTerminationReason(reason);
        log.setHeartbeatCount(session.getHeartbeatCount());
        log.setSessionStatus(session.getStatus());
        return log;
    }

    public static VMSessionAuditLog createSessionHeartbeatLog(VmSession session, String userName, String userEmail,
                                                            String workspaceName, String projectName) {
        VMSessionAuditLog log = new VMSessionAuditLog(
            session.getUserId(), userName, userEmail, session.getOrganizationId(),
            session.getWorkspaceId(), workspaceName, "VM_SESSION_HEARTBEAT"
        );
        log.setSessionId(session.getId());
        log.setVmInstanceId(session.getVmInstanceId());
        log.setProjectId(session.getProjectId());
        log.setProjectName(projectName);
        log.setResourceId(session.getId());
        log.setHeartbeatCount(session.getHeartbeatCount());
        log.setSessionStatus(session.getStatus());
        return log;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getWorkspaceName() {
        return workspaceName;
    }

    public void setWorkspaceName(String workspaceName) {
        this.workspaceName = workspaceName;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getVmInstanceId() {
        return vmInstanceId;
    }

    public void setVmInstanceId(String vmInstanceId) {
        this.vmInstanceId = vmInstanceId;
    }

    public String getVmInstanceType() {
        return vmInstanceType;
    }

    public void setVmInstanceType(String vmInstanceType) {
        this.vmInstanceType = vmInstanceType;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Long getSessionDurationSeconds() {
        return sessionDurationSeconds;
    }

    public void setSessionDurationSeconds(Long sessionDurationSeconds) {
        this.sessionDurationSeconds = sessionDurationSeconds;
    }

    public Double getCreditsUsed() {
        return creditsUsed;
    }

    public void setCreditsUsed(Double creditsUsed) {
        this.creditsUsed = creditsUsed;
    }

    public SessionStatus getSessionStatus() {
        return sessionStatus;
    }

    public void setSessionStatus(SessionStatus sessionStatus) {
        this.sessionStatus = sessionStatus;
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

    public Integer getHeartbeatCount() {
        return heartbeatCount;
    }

    public void setHeartbeatCount(Integer heartbeatCount) {
        this.heartbeatCount = heartbeatCount;
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

    public String getClientIpAddress() {
        return ipAddress;
    }

    public void setClientIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
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
}
