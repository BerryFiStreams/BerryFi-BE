package com.berryfi.portal.dto.project;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * DTO for shared project usage statistics.
 */
public class SharedProjectUsageResponse {

    @JsonProperty("projectId")
    private String projectId;

    @JsonProperty("projectName")
    private String projectName;

    @JsonProperty("sharedWithOrganizationId")
    private String sharedWithOrganizationId;

    @JsonProperty("sharedWithOrganizationName")
    private String sharedWithOrganizationName;

    @JsonProperty("adminEmail")
    private String adminEmail;

    @JsonProperty("shareType")
    private String shareType; // "DIRECT" or "INDIRECT"

    @JsonProperty("sharedAt")
    private LocalDateTime sharedAt;

    @JsonProperty("creditsAllocated")
    private Double creditsAllocated = 0.0;

    @JsonProperty("creditsUsed")
    private Double creditsUsed = 0.0;

    @JsonProperty("creditsRemaining")
    private Double creditsRemaining = 0.0;

    @JsonProperty("monthlyRecurringCredits")
    private Double monthlyRecurringCredits = 0.0;

    @JsonProperty("totalSessions")
    private Long totalSessions = 0L;

    @JsonProperty("lastUsed")
    private LocalDateTime lastUsed;

    @JsonProperty("status")
    private String status; // ProjectShareStatus

    // Constructors
    public SharedProjectUsageResponse() {}

    public SharedProjectUsageResponse(String projectId, String projectName, String sharedWithOrganizationId, 
                                     String sharedWithOrganizationName, String adminEmail, String shareType) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.sharedWithOrganizationId = sharedWithOrganizationId;
        this.sharedWithOrganizationName = sharedWithOrganizationName;
        this.adminEmail = adminEmail;
        this.shareType = shareType;
    }

    // Getters and Setters
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

    public String getSharedWithOrganizationId() {
        return sharedWithOrganizationId;
    }

    public void setSharedWithOrganizationId(String sharedWithOrganizationId) {
        this.sharedWithOrganizationId = sharedWithOrganizationId;
    }

    public String getSharedWithOrganizationName() {
        return sharedWithOrganizationName;
    }

    public void setSharedWithOrganizationName(String sharedWithOrganizationName) {
        this.sharedWithOrganizationName = sharedWithOrganizationName;
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }

    public String getShareType() {
        return shareType;
    }

    public void setShareType(String shareType) {
        this.shareType = shareType;
    }

    public LocalDateTime getSharedAt() {
        return sharedAt;
    }

    public void setSharedAt(LocalDateTime sharedAt) {
        this.sharedAt = sharedAt;
    }

    public Double getCreditsAllocated() {
        return creditsAllocated;
    }

    public void setCreditsAllocated(Double creditsAllocated) {
        this.creditsAllocated = creditsAllocated;
    }

    public Double getCreditsUsed() {
        return creditsUsed;
    }

    public void setCreditsUsed(Double creditsUsed) {
        this.creditsUsed = creditsUsed;
    }

    public Double getCreditsRemaining() {
        return creditsRemaining;
    }

    public void setCreditsRemaining(Double creditsRemaining) {
        this.creditsRemaining = creditsRemaining;
    }

    public Double getMonthlyRecurringCredits() {
        return monthlyRecurringCredits;
    }

    public void setMonthlyRecurringCredits(Double monthlyRecurringCredits) {
        this.monthlyRecurringCredits = monthlyRecurringCredits;
    }

    public Long getTotalSessions() {
        return totalSessions;
    }

    public void setTotalSessions(Long totalSessions) {
        this.totalSessions = totalSessions;
    }

    public LocalDateTime getLastUsed() {
        return lastUsed;
    }

    public void setLastUsed(LocalDateTime lastUsed) {
        this.lastUsed = lastUsed;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "SharedProjectUsageResponse{" +
                "projectId='" + projectId + '\'' +
                ", projectName='" + projectName + '\'' +
                ", sharedWithOrganizationName='" + sharedWithOrganizationName + '\'' +
                ", shareType='" + shareType + '\'' +
                ", creditsUsed=" + creditsUsed +
                ", totalSessions=" + totalSessions +
                '}';
    }
}