package com.berryfi.portal.dto.usage;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for usage analytics information.
 */
public class UsageAnalyticsDto {
    
    private String id;
    private String organizationId;
    private String projectId;
    private String userId;
    private LocalDate date;
    private Integer totalSessions;
    private Long totalDurationSeconds;
    private Double totalCreditsUsed;
    private Integer uniqueUsers;
    private Double avgSessionDuration;
    private Integer peakConcurrentSessions;
    private Integer totalErrors;
    private Double avgFps;
    private Double avgBitrate;
    private String topCountry;
    private String topDeviceType;
    private String topBrowser;
    private Double bounceRate;
    private Double conversionRate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public UsageAnalyticsDto() {}

    public UsageAnalyticsDto(String organizationId, LocalDate date, 
                           Integer totalSessions, Long totalDurationSeconds, 
                           Double totalCreditsUsed) {
        this.organizationId = organizationId;
        this.date = date;
        this.totalSessions = totalSessions;
        this.totalDurationSeconds = totalDurationSeconds;
        this.totalCreditsUsed = totalCreditsUsed;
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
}
