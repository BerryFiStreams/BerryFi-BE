package com.berryfi.portal.dto.audit;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response DTO for detailed audit log entry.
 */
public class AuditLogDetailResponse {
    private String id;
    private String userId;
    private String userName;
    private String userEmail;
    private String organizationId;
    private String organizationName;
    private String action;
    private String resource;
    private String resourceId;
    private String resourceName;
    private Map<String, Object> details;
    private Map<String, Object> beforeState;
    private Map<String, Object> afterState;
    private String ipAddress;
    private String userAgent;
    private String sessionId;
    private LocalDateTime timestamp;
    private String status;
    private String outcome;
    private String failureReason;

    public AuditLogDetailResponse() {}

    // Constructor with all fields
    public AuditLogDetailResponse(String id, String userId, String userName, String userEmail,
                                 String organizationId, String organizationName, String action, String resource,
                                 String resourceId, String resourceName, Map<String, Object> details,
                                 Map<String, Object> beforeState, Map<String, Object> afterState,
                                 String ipAddress, String userAgent, String sessionId, LocalDateTime timestamp,
                                 String status, String outcome, String failureReason) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.organizationId = organizationId;
        this.organizationName = organizationName;
        this.action = action;
        this.resource = resource;
        this.resourceId = resourceId;
        this.resourceName = resourceName;
        this.details = details;
        this.beforeState = beforeState;
        this.afterState = afterState;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.sessionId = sessionId;
        this.timestamp = timestamp;
        this.status = status;
        this.outcome = outcome;
        this.failureReason = failureReason;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }

    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getResource() { return resource; }
    public void setResource(String resource) { this.resource = resource; }

    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }

    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }

    public Map<String, Object> getDetails() { return details; }
    public void setDetails(Map<String, Object> details) { this.details = details; }

    public Map<String, Object> getBeforeState() { return beforeState; }
    public void setBeforeState(Map<String, Object> beforeState) { this.beforeState = beforeState; }

    public Map<String, Object> getAfterState() { return afterState; }
    public void setAfterState(Map<String, Object> afterState) { this.afterState = afterState; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
}
