package com.berryfi.portal.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing aggregated usage analytics.
 */
@Entity
@Table(name = "usage_analytics",
       indexes = {
           @Index(name = "idx_analytics_organization", columnList = "organizationId"),
           @Index(name = "idx_analytics_date", columnList = "date"),
           @Index(name = "idx_analytics_org_date", columnList = "organizationId, date"),
           @Index(name = "idx_analytics_project", columnList = "projectId")
       })
public class UsageAnalytics {
    
    @Id
    @Column(length = 50)
    private String id;
    
    @Column(name = "organization_id", nullable = false)
    private String organizationId;
    
    @Column(name = "project_id")
    private String projectId;
    
    @Column(name = "user_id")
    private String userId;
    
    @Column(name = "date", nullable = false)
    private LocalDate date;
    
    @Column(name = "total_sessions")
    private Integer totalSessions;
    
    @Column(name = "total_duration_seconds")
    private Long totalDurationSeconds;
    
    @Column(name = "total_credits_used")
    private Double totalCreditsUsed;
    
    @Column(name = "unique_users")
    private Integer uniqueUsers;
    
    @Column(name = "avg_session_duration")
    private Double avgSessionDuration;
    
    @Column(name = "peak_concurrent_sessions")
    private Integer peakConcurrentSessions;
    
    @Column(name = "total_errors")
    private Integer totalErrors;
    
    @Column(name = "avg_fps")
    private Double avgFps;
    
    @Column(name = "avg_bitrate")
    private Double avgBitrate;
    
    @Column(name = "top_country")
    private String topCountry;
    
    @Column(name = "top_device_type")
    private String topDeviceType;
    
    @Column(name = "top_browser")
    private String topBrowser;
    
    @Column(name = "bounce_rate")
    private Double bounceRate;
    
    @Column(name = "conversion_rate")
    private Double conversionRate;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public UsageAnalytics() {
        this.createdAt = LocalDateTime.now();
        this.totalSessions = 0;
        this.totalDurationSeconds = 0L;
        this.totalCreditsUsed = 0.0;
        this.uniqueUsers = 0;
        this.totalErrors = 0;
    }

    public UsageAnalytics(String id, String organizationId, LocalDate date) {
        this();
        this.id = id;
        this.organizationId = organizationId;
        this.date = date;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Integer getTotalSessions() {
        return totalSessions;
    }

    public void setTotalSessions(Integer totalSessions) {
        this.totalSessions = totalSessions;
    }

    public Long getTotalDurationSeconds() {
        return totalDurationSeconds;
    }

    public void setTotalDurationSeconds(Long totalDurationSeconds) {
        this.totalDurationSeconds = totalDurationSeconds;
    }

    public Double getTotalCreditsUsed() {
        return totalCreditsUsed;
    }

    public void setTotalCreditsUsed(Double totalCreditsUsed) {
        this.totalCreditsUsed = totalCreditsUsed;
    }

    public Integer getUniqueUsers() {
        return uniqueUsers;
    }

    public void setUniqueUsers(Integer uniqueUsers) {
        this.uniqueUsers = uniqueUsers;
    }

    public Double getAvgSessionDuration() {
        return avgSessionDuration;
    }

    public void setAvgSessionDuration(Double avgSessionDuration) {
        this.avgSessionDuration = avgSessionDuration;
    }

    public Integer getPeakConcurrentSessions() {
        return peakConcurrentSessions;
    }

    public void setPeakConcurrentSessions(Integer peakConcurrentSessions) {
        this.peakConcurrentSessions = peakConcurrentSessions;
    }

    public Integer getTotalErrors() {
        return totalErrors;
    }

    public void setTotalErrors(Integer totalErrors) {
        this.totalErrors = totalErrors;
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

    public String getTopCountry() {
        return topCountry;
    }

    public void setTopCountry(String topCountry) {
        this.topCountry = topCountry;
    }

    public String getTopDeviceType() {
        return topDeviceType;
    }

    public void setTopDeviceType(String topDeviceType) {
        this.topDeviceType = topDeviceType;
    }

    public String getTopBrowser() {
        return topBrowser;
    }

    public void setTopBrowser(String topBrowser) {
        this.topBrowser = topBrowser;
    }

    public Double getBounceRate() {
        return bounceRate;
    }

    public void setBounceRate(Double bounceRate) {
        this.bounceRate = bounceRate;
    }

    public Double getConversionRate() {
        return conversionRate;
    }

    public void setConversionRate(Double conversionRate) {
        this.conversionRate = conversionRate;
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

    /**
     * Update the updated_at timestamp
     */
    @PreUpdate
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
}
