package com.berryfi.portal.dto.audit;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response DTO for audit log entries.
 */
public class AuditLogResponse {
    private String id;
    private String userId;
    private String userName;
    private String organizationId;
    private String action;
    private String resource;
    private String resourceId;
    private Map<String, Object> details;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime timestamp;
    private String status;

    public AuditLogResponse() {}

    public AuditLogResponse(String id, String userId, String userName, String organizationId,
                           String action, String resource, String resourceId, Map<String, Object> details,
                           String ipAddress, String userAgent, LocalDateTime timestamp, String status) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.organizationId = organizationId;
        this.action = action;
        this.resource = resource;
        this.resourceId = resourceId;
        this.details = details;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.timestamp = timestamp;
        this.status = status;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getResource() { return resource; }
    public void setResource(String resource) { this.resource = resource; }

    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }

    public Map<String, Object> getDetails() { return details; }
    public void setDetails(Map<String, Object> details) { this.details = details; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
