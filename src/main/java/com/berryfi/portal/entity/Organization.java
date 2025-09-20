package com.berryfi.portal.entity;

import com.berryfi.portal.enums.OrganizationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing an organization in the system.
 * Organizations replace workspaces and can have multiple projects.
 */
@Entity
@Table(name = "organizations", indexes = {
    @Index(name = "idx_organization_status", columnList = "status"),
    @Index(name = "idx_organization_owner", columnList = "ownerId"),
    @Index(name = "idx_organization_name", columnList = "name")
})
public class Organization {

    @Id
    @Column(name = "id", length = 50)
    private String id;

    @NotBlank(message = "Organization name is required")
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotBlank(message = "Owner ID is required")
    @Column(name = "owner_id", nullable = false)
    private String ownerId;

    @NotBlank(message = "Owner email is required")
    @Column(name = "owner_email", nullable = false)
    private String ownerEmail;

    @NotBlank(message = "Owner name is required")
    @Column(name = "owner_name", nullable = false)
    private String ownerName;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrganizationStatus status = OrganizationStatus.ACTIVE;

    // Credit management
    @Column(name = "total_credits")
    private Double totalCredits = 0.0;

    @Column(name = "used_credits")
    private Double usedCredits = 0.0;

    @Column(name = "remaining_credits")
    private Double remainingCredits = 0.0;

    @Column(name = "purchased_credits")
    private Double purchasedCredits = 0.0;

    @Column(name = "gifted_credits")
    private Double giftedCredits = 0.0;

    // Budget and limits
    @Column(name = "monthly_budget")
    private Double monthlyBudget = 0.0;

    @Column(name = "monthly_credits_used")
    private Double monthlyCreditsUsed = 0.0;

    // Organization settings
    @Column(name = "can_share_projects")
    private Boolean canShareProjects = true;

    @Column(name = "can_receive_shared_projects")
    private Boolean canReceiveSharedProjects = true;

    @Column(name = "max_projects")
    private Integer maxProjects = 10;

    @Column(name = "max_members")
    private Integer maxMembers = 50;

    // Statistics
    @Column(name = "active_projects")
    private Integer activeProjects = 0;

    @Column(name = "total_members")
    private Integer totalMembers = 1;

    @Column(name = "total_sessions")
    private Integer totalSessions = 0;

    // Audit fields
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    public Organization() {
        this.id = generateOrganizationId();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Organization(String name, String description, String ownerId, String ownerEmail, String ownerName, String createdBy) {
        this();
        this.name = name;
        this.description = description;
        this.ownerId = ownerId;
        this.ownerEmail = ownerEmail;
        this.ownerName = ownerName;
        this.createdBy = createdBy;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    private String generateOrganizationId() {
        return "org_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    // Helper methods
    public boolean isActive() {
        return this.status == OrganizationStatus.ACTIVE;
    }

    public boolean isSuspended() {
        return this.status == OrganizationStatus.SUSPENDED;
    }

    public boolean isDeleted() {
        return this.status == OrganizationStatus.DELETED;
    }

    public void updateCredits(double creditsUsed) {
        this.usedCredits = (this.usedCredits == null ? 0.0 : this.usedCredits) + creditsUsed;
        this.remainingCredits = (this.totalCredits == null ? 0.0 : this.totalCredits) - 
                              (this.usedCredits == null ? 0.0 : this.usedCredits);
        this.monthlyCreditsUsed = (this.monthlyCreditsUsed == null ? 0.0 : this.monthlyCreditsUsed) + creditsUsed;
    }

    public void addCredits(double credits, boolean isPurchased) {
        if (isPurchased) {
            this.purchasedCredits = (this.purchasedCredits == null ? 0.0 : this.purchasedCredits) + credits;
        } else {
            this.giftedCredits = (this.giftedCredits == null ? 0.0 : this.giftedCredits) + credits;
        }
        this.totalCredits = (this.totalCredits == null ? 0.0 : this.totalCredits) + credits;
        this.remainingCredits = (this.remainingCredits == null ? 0.0 : this.remainingCredits) + credits;
    }

    public boolean hasEnoughCredits(double requiredCredits) {
        return this.remainingCredits != null && this.remainingCredits >= requiredCredits;
    }

    public boolean isOverBudget() {
        if (monthlyBudget == null || monthlyBudget == 0) {
            return false;
        }
        return monthlyCreditsUsed != null && monthlyCreditsUsed > monthlyBudget;
    }

    public void incrementProjectCount() {
        this.activeProjects = (this.activeProjects == null ? 0 : this.activeProjects) + 1;
    }

    public void decrementProjectCount() {
        this.activeProjects = Math.max(0, (this.activeProjects == null ? 0 : this.activeProjects) - 1);
    }

    public void incrementMemberCount() {
        this.totalMembers = (this.totalMembers == null ? 1 : this.totalMembers) + 1;
    }

    public void decrementMemberCount() {
        this.totalMembers = Math.max(1, (this.totalMembers == null ? 1 : this.totalMembers) - 1);
    }

    public void incrementSessionCount() {
        this.totalSessions = (this.totalSessions == null ? 0 : this.totalSessions) + 1;
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

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public OrganizationStatus getStatus() {
        return status;
    }

    public void setStatus(OrganizationStatus status) {
        this.status = status;
    }

    public Double getTotalCredits() {
        return totalCredits;
    }

    public void setTotalCredits(Double totalCredits) {
        this.totalCredits = totalCredits;
    }

    public Double getUsedCredits() {
        return usedCredits;
    }

    public void setUsedCredits(Double usedCredits) {
        this.usedCredits = usedCredits;
    }

    public Double getRemainingCredits() {
        return remainingCredits;
    }

    public void setRemainingCredits(Double remainingCredits) {
        this.remainingCredits = remainingCredits;
    }

    public Double getPurchasedCredits() {
        return purchasedCredits;
    }

    public void setPurchasedCredits(Double purchasedCredits) {
        this.purchasedCredits = purchasedCredits;
    }

    public Double getGiftedCredits() {
        return giftedCredits;
    }

    public void setGiftedCredits(Double giftedCredits) {
        this.giftedCredits = giftedCredits;
    }

    public Double getMonthlyBudget() {
        return monthlyBudget;
    }

    public void setMonthlyBudget(Double monthlyBudget) {
        this.monthlyBudget = monthlyBudget;
    }

    public Double getMonthlyCreditsUsed() {
        return monthlyCreditsUsed;
    }

    public void setMonthlyCreditsUsed(Double monthlyCreditsUsed) {
        this.monthlyCreditsUsed = monthlyCreditsUsed;
    }

    public Boolean getCanShareProjects() {
        return canShareProjects;
    }

    public void setCanShareProjects(Boolean canShareProjects) {
        this.canShareProjects = canShareProjects;
    }

    public Boolean getCanReceiveSharedProjects() {
        return canReceiveSharedProjects;
    }

    public void setCanReceiveSharedProjects(Boolean canReceiveSharedProjects) {
        this.canReceiveSharedProjects = canReceiveSharedProjects;
    }

    public Integer getMaxProjects() {
        return maxProjects;
    }

    public void setMaxProjects(Integer maxProjects) {
        this.maxProjects = maxProjects;
    }

    public Integer getMaxMembers() {
        return maxMembers;
    }

    public void setMaxMembers(Integer maxMembers) {
        this.maxMembers = maxMembers;
    }

    public Integer getActiveProjects() {
        return activeProjects;
    }

    public void setActiveProjects(Integer activeProjects) {
        this.activeProjects = activeProjects;
    }

    public Integer getTotalMembers() {
        return totalMembers;
    }

    public void setTotalMembers(Integer totalMembers) {
        this.totalMembers = totalMembers;
    }

    public Integer getTotalSessions() {
        return totalSessions;
    }

    public void setTotalSessions(Integer totalSessions) {
        this.totalSessions = totalSessions;
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
}