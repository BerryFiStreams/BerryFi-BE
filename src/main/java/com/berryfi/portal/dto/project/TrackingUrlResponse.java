package com.berryfi.portal.dto.project;

/**
 * DTO for tracking URL response.
 */
public class TrackingUrlResponse {
    
    private String projectId;
    private String userId;
    private String trackingUrl;
    private String originalUrl;
    private Long expiresAt;

    public TrackingUrlResponse() {}

    public TrackingUrlResponse(String projectId, String userId, String trackingUrl, String originalUrl, Long expiresAt) {
        this.projectId = projectId;
        this.userId = userId;
        this.trackingUrl = trackingUrl;
        this.originalUrl = originalUrl;
        this.expiresAt = expiresAt;
    }

    // Getters and Setters
    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTrackingUrl() {
        return trackingUrl;
    }

    public void setTrackingUrl(String trackingUrl) {
        this.trackingUrl = trackingUrl;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public Long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Long expiresAt) {
        this.expiresAt = expiresAt;
    }
}
