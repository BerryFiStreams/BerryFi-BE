package com.berryfi.portal.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing an audit log entry.
 */
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_user", columnList = "userId"),
    @Index(name = "idx_audit_organization", columnList = "organizationId"),
    @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
    @Index(name = "idx_audit_action", columnList = "action")
})
public class AuditLog {

    @Id
    @Column(name = "id", length = 50)
    private String id;

    @NotBlank(message = "User ID is required")
    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "user_name")
    private String userName;

    @NotBlank(message = "Organization ID is required")
    @Column(name = "organization_id", nullable = false)
    private String organizationId;

    @Column(name = "workspace_id")
    private String workspaceId;

    @NotBlank(message = "Action is required")
    @Column(name = "action", nullable = false)
    private String action;

    @NotBlank(message = "Resource is required")
    @Column(name = "resource", nullable = false)
    private String resource;

    @Column(name = "resource_id")
    private String resourceId;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details; // JSON string

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @NotNull(message = "Timestamp is required")
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "status")
    private String status = "SUCCESS";

    @Column(name = "session_id")
    private String sessionId;

    // URL tracking specific fields
    @Column(name = "project_id")
    private String projectId;

    @Column(name = "tracking_url")
    private String trackingUrl;

    @Column(name = "referrer_url")
    private String referrerUrl;

    @Column(name = "original_url")
    private String originalUrl;

    public AuditLog() {
        this.id = generateAuditId();
        this.timestamp = LocalDateTime.now();
    }

    public AuditLog(String userId, String userName, String organizationId, String action, 
                   String resource, String resourceId) {
        this();
        this.userId = userId;
        this.userName = userName;
        this.organizationId = organizationId;
        this.action = action;
        this.resource = resource;
        this.resourceId = resourceId;
    }

    public AuditLog(String userId, String userName, String organizationId, String workspaceId, 
                   String action, String resource, String resourceId) {
        this();
        this.userId = userId;
        this.userName = userName;
        this.organizationId = organizationId;
        this.workspaceId = workspaceId;
        this.action = action;
        this.resource = resource;
        this.resourceId = resourceId;
    }

    private String generateAuditId() {
        return "audit_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
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

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getTrackingUrl() {
        return trackingUrl;
    }

    public void setTrackingUrl(String trackingUrl) {
        this.trackingUrl = trackingUrl;
    }

    public String getReferrerUrl() {
        return referrerUrl;
    }

    public void setReferrerUrl(String referrerUrl) {
        this.referrerUrl = referrerUrl;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }
}
