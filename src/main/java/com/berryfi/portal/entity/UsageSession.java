package com.berryfi.portal.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing usage session logs.
 */
@Entity
@Table(name = "usage_sessions",
       indexes = {
           @Index(name = "idx_usage_organization", columnList = "organizationId"),
           @Index(name = "idx_usage_workspace", columnList = "workspaceId"),
           @Index(name = "idx_usage_project", columnList = "projectId"),
           @Index(name = "idx_usage_started", columnList = "startedAt"),
           @Index(name = "idx_usage_user", columnList = "userId")
       })
public class UsageSession {
    
    @Id
    @Column(length = 50)
    private String id;
    
    @Column(name = "organization_id", nullable = false)
    private String organizationId;
    
    @Column(name = "workspace_id")
    private String workspaceId;
    
    @Column(name = "project_id", nullable = false)
    private String projectId;
    
    @Column(name = "project_name", nullable = false)
    private String projectName;
    
    @Column(name = "user_id")
    private String userId;
    
    @Column(name = "session_id", nullable = false)
    private String sessionId;
    
    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;
    
    @Column(name = "ended_at")
    private LocalDateTime endedAt;
    
    @Column(name = "duration_seconds")
    private Integer durationSeconds;
    
    @Column(name = "credits_used")
    private Double creditsUsed;
    
    @Column(name = "device_type")
    private String deviceType;
    
    @Column(name = "browser")
    private String browser;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "country")
    private String country;
    
    @Column(name = "city")
    private String city;
    
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;
    
    @Column(name = "referrer")
    private String referrer;
    
    @Column(name = "network_quality")
    private String networkQuality;
    
    @Column(name = "avg_fps")
    private Double avgFps;
    
    @Column(name = "avg_bitrate")
    private Double avgBitrate;
    
    @Column(name = "error_count")
    private Integer errorCount;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Constructors
    public UsageSession() {
        this.createdAt = LocalDateTime.now();
        this.startedAt = LocalDateTime.now();
        this.errorCount = 0;
    }

    public UsageSession(String id, String organizationId, String projectId, 
                       String projectName, String sessionId) {
        this();
        this.id = id;
        this.organizationId = organizationId;
        this.projectId = projectId;
        this.projectName = projectName;
        this.sessionId = sessionId;
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

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
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
        return creditsUsed;
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

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
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

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getReferrer() {
        return referrer;
    }

    public void setReferrer(String referrer) {
        this.referrer = referrer;
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

    /**
     * Calculate session duration when ending session
     */
    public void endSession() {
        if (this.endedAt == null) {
            this.endedAt = LocalDateTime.now();
            if (this.startedAt != null) {
                this.durationSeconds = (int) java.time.Duration.between(this.startedAt, this.endedAt).getSeconds();
            }
        }
    }
}
