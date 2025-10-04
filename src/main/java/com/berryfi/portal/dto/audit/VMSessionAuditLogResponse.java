package com.berryfi.portal.dto.audit;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response DTO for VMSessionAuditLog.
 * Contains comprehensive VM session audit information for organization-level access.
 */
public class VMSessionAuditLogResponse {
    private String id;
    private String userId;
    private String userName;
    private String userEmail;
    private String organizationId;
    private String projectId;
    private String projectName;
    private String sessionId;
    private String vmInstanceId;
    private String vmInstanceType;
    private String action;
    private String resource;
    private String resourceId;
    private Map<String, Object> details;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime timestamp;
    private String status;
    private String errorMessage;
    private Long sessionDurationSeconds;
    private Double creditsUsed;
    private String sessionStatus;
    private String vmIpAddress;
    private Integer vmPort;
    private String connectionUrl;
    private String terminationReason;
    private Integer heartbeatCount;
    private String clientCountry;
    private String clientCity;

    public VMSessionAuditLogResponse() {}

    public VMSessionAuditLogResponse(String id, String userId, String userName, String userEmail, 
                                   String organizationId,
                                   String projectId, String projectName, String sessionId,
                                   String vmInstanceId, String vmInstanceType, String action,
                                   String resource, String resourceId, Map<String, Object> details,
                                   String ipAddress, String userAgent, LocalDateTime timestamp,
                                   String status, String errorMessage, Long sessionDurationSeconds,
                                   Double creditsUsed, String sessionStatus, String vmIpAddress,
                                   Integer vmPort, String connectionUrl, String terminationReason,
                                   Integer heartbeatCount, String clientCountry, String clientCity) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.organizationId = organizationId;
        this.projectId = projectId;
        this.projectName = projectName;
        this.sessionId = sessionId;
        this.vmInstanceId = vmInstanceId;
        this.vmInstanceType = vmInstanceType;
        this.action = action;
        this.resource = resource;
        this.resourceId = resourceId;
        this.details = details;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.timestamp = timestamp;
        this.status = status;
        this.errorMessage = errorMessage;
        this.sessionDurationSeconds = sessionDurationSeconds;
        this.creditsUsed = creditsUsed;
        this.sessionStatus = sessionStatus;
        this.vmIpAddress = vmIpAddress;
        this.vmPort = vmPort;
        this.connectionUrl = connectionUrl;
        this.terminationReason = terminationReason;
        this.heartbeatCount = heartbeatCount;
        this.clientCountry = clientCountry;
        this.clientCity = clientCity;
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

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
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

    public String getSessionStatus() {
        return sessionStatus;
    }

    public void setSessionStatus(String sessionStatus) {
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
}
