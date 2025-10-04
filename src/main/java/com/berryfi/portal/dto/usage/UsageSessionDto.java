package com.berryfi.portal.dto.usage;

import com.berryfi.portal.util.NumberFormatUtil;
import java.time.LocalDateTime;

/**
 * DTO for usage session information.
 */
public class UsageSessionDto {
    
    private String id;
    private String organizationId;
    private String projectId;
    private String projectName;
    private String userId;
    private String sessionId;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private Integer durationSeconds;
    private Double creditsUsed;
    private String deviceType;
    private String browser;
    private String country;
    private String city;
    private String networkQuality;
    private Double avgFps;
    private Double avgBitrate;
    private Integer errorCount;
    private LocalDateTime createdAt;

    // Constructors
    public UsageSessionDto() {}

    public UsageSessionDto(String id, String organizationId, String projectId, 
                          String projectName, String sessionId, 
                          LocalDateTime startedAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.projectId = projectId;
        this.projectName = projectName;
        this.sessionId = sessionId;
        this.startedAt = startedAt;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public Double getCreditsUsed() {
        return NumberFormatUtil.formatCredits(creditsUsed);
    }

    public void setCreditsUsed(Double creditsUsed) {
        this.creditsUsed = creditsUsed;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getNetworkQuality() {
        return networkQuality;
    }

    public void setNetworkQuality(String networkQuality) {
        this.networkQuality = networkQuality;
    }

    public Double getAvgFps() {
        return avgFps;
    }

    public void setAvgFps(Double avgFps) {
        this.avgFps = avgFps;
    }

    public Double getAvgBitrate() {
        return avgBitrate;
    }

    public void setAvgBitrate(Double avgBitrate) {
        this.avgBitrate = avgBitrate;
    }

    public Integer getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(Integer errorCount) {
        this.errorCount = errorCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
