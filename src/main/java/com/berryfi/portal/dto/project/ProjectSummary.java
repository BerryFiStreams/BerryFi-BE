package com.berryfi.portal.dto.project;

import com.berryfi.portal.entity.Project;
import com.berryfi.portal.enums.AccountType;
import com.berryfi.portal.enums.ProjectStatus;

import java.time.LocalDateTime;

/**
 * DTO for project summary in list views.
 */
public class ProjectSummary {

    private String id;
    private String name;
    private String description;
    private ProjectStatus status;
    private String trackingUrl; // User-specific tracking URL
    private String organizationId;
    private AccountType accountType;
    private Double totalCreditsUsed;
    private Integer sessionsCount;
    private LocalDateTime lastDeployed;
    private Integer currentCCU;
    private Double uptime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    
    // Sharing information
    private String accessType; // "OWNED" or "SHARED"
    private String sharedBy; // Organization name that shared this project (if shared)
    
    // Credit information
    private Double allocatedCredits;
    private Double remainingCredits;

    public ProjectSummary() {}

    public ProjectSummary(Project project) {
        this.id = project.getId();
        this.name = project.getName();
        this.description = project.getDescription();
        this.status = project.getStatus();
        this.trackingUrl = null; // Will be set by service layer for user-specific URLs
        this.organizationId = project.getOrganizationId();
        this.accountType = project.getAccountType();
        this.totalCreditsUsed = project.getTotalCreditsUsed();
        this.sessionsCount = project.getSessionsCount();
        this.lastDeployed = project.getLastDeployed();
        this.currentCCU = project.getCurrentCCU();
        this.uptime = project.getUptime();
        this.createdAt = project.getCreatedAt();
        this.updatedAt = project.getUpdatedAt();
        this.createdBy = project.getCreatedBy();
    }

    // Static factory method
    public static ProjectSummary from(Project project) {
        return new ProjectSummary(project);
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    public String getTrackingUrl() {
        return trackingUrl;
    }

    public void setTrackingUrl(String trackingUrl) {
        this.trackingUrl = trackingUrl;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public Double getTotalCreditsUsed() {
        return totalCreditsUsed;
    }

    public void setTotalCreditsUsed(Double totalCreditsUsed) {
        this.totalCreditsUsed = totalCreditsUsed;
    }

    public Integer getSessionsCount() {
        return sessionsCount;
    }

    public void setSessionsCount(Integer sessionsCount) {
        this.sessionsCount = sessionsCount;
    }

    public LocalDateTime getLastDeployed() {
        return lastDeployed;
    }

    public void setLastDeployed(LocalDateTime lastDeployed) {
        this.lastDeployed = lastDeployed;
    }

    public Integer getCurrentCCU() {
        return currentCCU;
    }

    public void setCurrentCCU(Integer currentCCU) {
        this.currentCCU = currentCCU;
    }

    public Double getUptime() {
        return uptime;
    }

    public void setUptime(Double uptime) {
        this.uptime = uptime;
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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getAccessType() {
        return accessType;
    }

    public void setAccessType(String accessType) {
        this.accessType = accessType;
    }

    public String getSharedBy() {
        return sharedBy;
    }

    public void setSharedBy(String sharedBy) {
        this.sharedBy = sharedBy;
    }

    public Double getAllocatedCredits() {
        return allocatedCredits;
    }

    public void setAllocatedCredits(Double allocatedCredits) {
        this.allocatedCredits = allocatedCredits;
    }

    public Double getRemainingCredits() {
        return remainingCredits;
    }

    public void setRemainingCredits(Double remainingCredits) {
        this.remainingCredits = remainingCredits;
    }
}
