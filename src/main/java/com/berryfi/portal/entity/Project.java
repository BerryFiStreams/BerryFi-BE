package com.berryfi.portal.entity;

import com.berryfi.portal.enums.AccountType;
import com.berryfi.portal.enums.ProjectStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a project in the system.
 */
@Entity
@Table(name = "projects", indexes = {
    @Index(name = "idx_project_organization", columnList = "organizationId"),
    @Index(name = "idx_project_status", columnList = "status")
})
public class Project {

    @Id
    @Column(name = "id", length = 50)
    private String id;

    @NotBlank(message = "Project name is required")
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProjectStatus status = ProjectStatus.STOPPED;

    @Column(name = "production_url")
    private String productionUrl;

    @NotBlank(message = "Organization ID is required")
    @Column(name = "organization_id", nullable = false)
    private String organizationId;

    @NotNull(message = "Account type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    @Column(name = "total_credits_used")
    private Double totalCreditsUsed = 0.0;

    // Project-specific credit allocation
    @Column(name = "allocated_credits")
    private Double allocatedCredits = 0.0;

    @Column(name = "remaining_credits")
    private Double remainingCredits = 0.0;

    @Column(name = "sessions_count")
    private Integer sessionsCount = 0;

    // Sharing functionality - JSON array of organization IDs this project is shared with
    @Column(name = "shared_with_organizations", columnDefinition = "TEXT")
    private String sharedWithOrganizations; // JSON array of org IDs

    @Column(name = "last_deployed")
    private LocalDateTime lastDeployed;

    @Column(name = "current_ccu")
    private Integer currentCCU = 0;

    @Column(name = "uptime")
    private Double uptime = 0.0;

    // Sharing settings
    @Column(name = "is_shareable")
    private Boolean isShareable = true;

    @Column(name = "is_shared_project")
    private Boolean isSharedProject = false;

    @Column(name = "original_organization_id")
    private String originalOrganizationId;

    @Column(name = "shared_count")
    private Integer sharedCount = 0;

    @Column(name = "max_shares_allowed")
    private Integer maxSharesAllowed = 10;

    // Resource limits
    @Column(name = "max_concurrent_sessions")
    private Integer maxConcurrentSessions = 5;

    @Column(name = "max_session_duration_hours")
    private Integer maxSessionDurationHours = 8;

    // Project metadata
    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags;

    @Column(name = "category")
    private String category;

    @Column(name = "visibility")
    private String visibility = "PRIVATE"; // PRIVATE, ORGANIZATION, PUBLIC

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    // JSON fields stored as TEXT for simplicity (in production, consider using JSONB for PostgreSQL)
    @Column(name = "config", columnDefinition = "TEXT")
    private String config;

    @Column(name = "branding", columnDefinition = "TEXT")
    private String branding;

    @Column(name = "links", columnDefinition = "TEXT")
    private String links;

    @Column(name = "errors", columnDefinition = "TEXT")
    private String errors;

    public Project() {
        this.id = generateProjectId();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Project(String name, String description, String organizationId, AccountType accountType, String createdBy) {
        this();
        this.name = name;
        this.description = description;
        this.organizationId = organizationId;
        this.accountType = accountType;
        this.createdBy = createdBy;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    private String generateProjectId() {
        return "proj_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    // Helper methods
    public boolean isRunning() {
        return this.status == ProjectStatus.RUNNING;
    }

    public boolean isStopped() {
        return this.status == ProjectStatus.STOPPED;
    }

    public boolean isDeploying() {
        return this.status == ProjectStatus.DEPLOYING;
    }

    public boolean hasError() {
        return this.status == ProjectStatus.ERROR;
    }

    public void updateStatus(ProjectStatus newStatus) {
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
        
        if (newStatus == ProjectStatus.RUNNING && this.lastDeployed == null) {
            this.lastDeployed = LocalDateTime.now();
        }
    }

    public void incrementSessionsCount() {
        this.sessionsCount = (this.sessionsCount == null ? 0 : this.sessionsCount) + 1;
    }

    public void addCreditsUsed(double credits) {
        this.totalCreditsUsed = (this.totalCreditsUsed == null ? 0.0 : this.totalCreditsUsed) + credits;
        // Also deduct from remaining credits if allocated
        if (this.remainingCredits != null && this.remainingCredits > 0) {
            this.remainingCredits = Math.max(0.0, this.remainingCredits - credits);
        }
    }

    /**
     * Allocate credits to this project
     */
    public void allocateCredits(double credits) {
        this.allocatedCredits = (this.allocatedCredits == null ? 0.0 : this.allocatedCredits) + credits;
        this.remainingCredits = (this.remainingCredits == null ? 0.0 : this.remainingCredits) + credits;
    }

    /**
     * Check if project has enough credits for a session
     */
    public boolean hasEnoughCredits(double requiredCredits) {
        return this.remainingCredits != null && this.remainingCredits >= requiredCredits;
    }

    /**
     * Get sharing status relative to an organization
     */
    public String getProjectStatus(String organizationId) {
        if (this.organizationId.equals(organizationId)) {
            return "OWNED";
        } else if (isSharedWithOrganization(organizationId)) {
            return "SHARED";
        }
        return "NOT_ACCESSIBLE";
    }

    /**
     * Check if project is shared with a specific organization
     */
    public boolean isSharedWithOrganization(String organizationId) {
        if (this.sharedWithOrganizations == null || this.sharedWithOrganizations.isEmpty()) {
            return false;
        }
        // Simple contains check - in production, you'd want proper JSON parsing
        return this.sharedWithOrganizations.contains("\"" + organizationId + "\"");
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

    public String getProductionUrl() {
        return productionUrl;
    }

    public void setProductionUrl(String productionUrl) {
        this.productionUrl = productionUrl;
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

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public String getBranding() {
        return branding;
    }

    public void setBranding(String branding) {
        this.branding = branding;
    }

    public String getLinks() {
        return links;
    }

    public void setLinks(String links) {
        this.links = links;
    }

    public String getErrors() {
        return errors;
    }

    public void setErrors(String errors) {
        this.errors = errors;
    }

    public Boolean getIsShareable() {
        return isShareable;
    }

    public void setIsShareable(Boolean isShareable) {
        this.isShareable = isShareable;
    }

    public Boolean getIsSharedProject() {
        return isSharedProject;
    }

    public void setIsSharedProject(Boolean isSharedProject) {
        this.isSharedProject = isSharedProject;
    }

    public String getOriginalOrganizationId() {
        return originalOrganizationId;
    }

    public void setOriginalOrganizationId(String originalOrganizationId) {
        this.originalOrganizationId = originalOrganizationId;
    }

    public Integer getSharedCount() {
        return sharedCount;
    }

    public void setSharedCount(Integer sharedCount) {
        this.sharedCount = sharedCount;
    }

    public Integer getMaxSharesAllowed() {
        return maxSharesAllowed;
    }

    public void setMaxSharesAllowed(Integer maxSharesAllowed) {
        this.maxSharesAllowed = maxSharesAllowed;
    }

    public Integer getMaxConcurrentSessions() {
        return maxConcurrentSessions;
    }

    public void setMaxConcurrentSessions(Integer maxConcurrentSessions) {
        this.maxConcurrentSessions = maxConcurrentSessions;
    }

    public Integer getMaxSessionDurationHours() {
        return maxSessionDurationHours;
    }

    public void setMaxSessionDurationHours(Integer maxSessionDurationHours) {
        this.maxSessionDurationHours = maxSessionDurationHours;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    // Helper methods for sharing
    public void incrementSharedCount() {
        this.sharedCount = (this.sharedCount == null ? 0 : this.sharedCount) + 1;
    }

    public void decrementSharedCount() {
        this.sharedCount = Math.max(0, (this.sharedCount == null ? 0 : this.sharedCount) - 1);
    }

    public boolean canBeSharedMore() {
        return this.isShareable && 
               (this.maxSharesAllowed == null || this.maxSharesAllowed <= 0 || 
                (this.sharedCount != null && this.sharedCount < this.maxSharesAllowed));
    }

    public boolean isOwnedBy(String organizationId) {
        return this.organizationId.equals(organizationId) && !this.isSharedProject;
    }

    public boolean isSharedWith(String organizationId) {
        return this.organizationId.equals(organizationId) && this.isSharedProject;
    }

    // New getters and setters for credit and sharing functionality
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

    public String getSharedWithOrganizations() {
        return sharedWithOrganizations;
    }

    public void setSharedWithOrganizations(String sharedWithOrganizations) {
        this.sharedWithOrganizations = sharedWithOrganizations;
    }
}
