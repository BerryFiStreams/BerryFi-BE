package com.berryfi.portal.entity;

import com.berryfi.portal.enums.ProjectShareStatus;
import com.berryfi.portal.enums.ShareType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a project share between organizations.
 */
@Entity
@Table(name = "project_shares", indexes = {
    @Index(name = "idx_project_share_project", columnList = "projectId"),
    @Index(name = "idx_project_share_shared_by", columnList = "sharedByOrganizationId"),
    @Index(name = "idx_project_share_shared_with", columnList = "sharedWithOrganizationId"),
    @Index(name = "idx_project_share_status", columnList = "status")
})
public class ProjectShare {

    @Id
    @Column(name = "id", length = 50)
    private String id;

    @NotBlank(message = "Project ID is required")
    @Column(name = "project_id", nullable = false)
    private String projectId;

    @NotBlank(message = "Shared by organization ID is required")
    @Column(name = "shared_by_organization_id", nullable = false)
    private String sharedByOrganizationId;

    @NotBlank(message = "Shared with organization ID is required")
    @Column(name = "shared_with_organization_id", nullable = false)
    private String sharedWithOrganizationId;

    @NotBlank(message = "Owner organization ID is required")
    @Column(name = "owner_organization_id", nullable = false)
    private String ownerOrganizationId;

    @NotNull(message = "Share type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "share_type", nullable = false, length = 20)
    private ShareType shareType;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProjectShareStatus status = ProjectShareStatus.PENDING;

    // Credit allocation for this share
    @Column(name = "allocated_credits")
    private Double allocatedCredits = 0.0;

    @Column(name = "used_credits")
    private Double usedCredits = 0.0;

    @Column(name = "remaining_credits")
    private Double remainingCredits = 0.0;

    // Recurring credit gift settings
    @Column(name = "recurring_credits")
    private Double recurringCredits = 0.0;

    @Column(name = "recurring_interval_days")
    private Integer recurringIntervalDays = 0;

    @Column(name = "next_credit_gift_date")
    private LocalDateTime nextCreditGiftDate;

    @Column(name = "last_credit_gift_date")
    private LocalDateTime lastCreditGiftDate;

    // One-time credit gift
    @Column(name = "one_time_credits")
    private Double oneTimeCredits = 0.0;

    @Column(name = "one_time_credit_used")
    private Boolean oneTimeCreditUsed = false;

    // Permission settings
    @Column(name = "can_modify_project")
    private Boolean canModifyProject = false;

    @Column(name = "can_view_analytics")
    private Boolean canViewAnalytics = true;

    @Column(name = "can_manage_sessions")
    private Boolean canManageSessions = true;

    @Column(name = "can_share_further")
    private Boolean canShareFurther = false;

    // Expiry settings
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "is_permanent")
    private Boolean isPermanent = false;

    // Notes and context
    @Column(name = "share_message", columnDefinition = "TEXT")
    private String shareMessage;

    @Column(name = "terms_conditions", columnDefinition = "TEXT")
    private String termsConditions;

    // Audit fields
    @Column(name = "shared_at", nullable = false, updatable = false)
    private LocalDateTime sharedAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "accepted_by")
    private String acceptedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public ProjectShare() {
        this.id = generateProjectShareId();
        this.sharedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public ProjectShare(String projectId, String sharedByOrganizationId, String sharedWithOrganizationId, 
                       ShareType shareType, String createdBy) {
        this();
        this.projectId = projectId;
        this.sharedByOrganizationId = sharedByOrganizationId;
        this.sharedWithOrganizationId = sharedWithOrganizationId;
        this.ownerOrganizationId = sharedByOrganizationId; // Owner is the organization that shares the project
        this.shareType = shareType;
        this.createdBy = createdBy;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    private String generateProjectShareId() {
        return "share_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    // Helper methods
    public boolean isPending() {
        return this.status == ProjectShareStatus.PENDING;
    }

    public boolean isAccepted() {
        return this.status == ProjectShareStatus.ACCEPTED;
    }

    public boolean isRejected() {
        return this.status == ProjectShareStatus.REJECTED;
    }

    public boolean isRevoked() {
        return this.status == ProjectShareStatus.REVOKED;
    }

    public boolean isExpired() {
        return this.status == ProjectShareStatus.EXPIRED || 
               (this.expiresAt != null && LocalDateTime.now().isAfter(this.expiresAt));
    }

    public void acceptShare(String acceptedBy) {
        this.status = ProjectShareStatus.ACCEPTED;
        this.acceptedAt = LocalDateTime.now();
        this.acceptedBy = acceptedBy;
        
        // Initialize remaining credits
        this.remainingCredits = this.allocatedCredits;
        
        // Set next recurring credit gift date if applicable
        if (this.recurringCredits != null && this.recurringCredits > 0 && this.recurringIntervalDays != null && this.recurringIntervalDays > 0) {
            this.nextCreditGiftDate = LocalDateTime.now().plusDays(this.recurringIntervalDays);
        }
    }

    public void rejectShare() {
        this.status = ProjectShareStatus.REJECTED;
        this.rejectedAt = LocalDateTime.now();
    }

    public void revokeShare() {
        this.status = ProjectShareStatus.REVOKED;
        this.revokedAt = LocalDateTime.now();
    }

    public void expireShare() {
        this.status = ProjectShareStatus.EXPIRED;
    }

    public void useCredits(double creditsUsed) {
        if (this.remainingCredits == null) {
            this.remainingCredits = 0.0;
        }
        if (this.usedCredits == null) {
            this.usedCredits = 0.0;
        }
        
        this.usedCredits += creditsUsed;
        this.remainingCredits = Math.max(0, this.remainingCredits - creditsUsed);
    }

    public void addRecurringCredits() {
        if (this.recurringCredits != null && this.recurringCredits > 0) {
            if (this.remainingCredits == null) {
                this.remainingCredits = 0.0;
            }
            this.remainingCredits += this.recurringCredits;
            this.lastCreditGiftDate = LocalDateTime.now();
            
            if (this.recurringIntervalDays != null && this.recurringIntervalDays > 0) {
                this.nextCreditGiftDate = LocalDateTime.now().plusDays(this.recurringIntervalDays);
            }
        }
    }

    public void useOneTimeCredits() {
        if (this.oneTimeCredits != null && this.oneTimeCredits > 0 && !this.oneTimeCreditUsed) {
            if (this.remainingCredits == null) {
                this.remainingCredits = 0.0;
            }
            this.remainingCredits += this.oneTimeCredits;
            this.oneTimeCreditUsed = true;
        }
    }

    public boolean hasEnoughCredits(double requiredCredits) {
        return this.remainingCredits != null && this.remainingCredits >= requiredCredits;
    }

    public boolean needsRecurringCreditGift() {
        return this.recurringCredits != null && this.recurringCredits > 0 &&
               this.nextCreditGiftDate != null && LocalDateTime.now().isAfter(this.nextCreditGiftDate);
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getSharedByOrganizationId() {
        return sharedByOrganizationId;
    }

    public void setSharedByOrganizationId(String sharedByOrganizationId) {
        this.sharedByOrganizationId = sharedByOrganizationId;
    }

    public String getSharedWithOrganizationId() {
        return sharedWithOrganizationId;
    }

    public void setSharedWithOrganizationId(String sharedWithOrganizationId) {
        this.sharedWithOrganizationId = sharedWithOrganizationId;
    }

    public ShareType getShareType() {
        return shareType;
    }

    public void setShareType(ShareType shareType) {
        this.shareType = shareType;
    }

    public ProjectShareStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectShareStatus status) {
        this.status = status;
    }

    public Double getAllocatedCredits() {
        return allocatedCredits;
    }

    public void setAllocatedCredits(Double allocatedCredits) {
        this.allocatedCredits = allocatedCredits;
        this.remainingCredits = allocatedCredits;
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

    public Double getRecurringCredits() {
        return recurringCredits;
    }

    public void setRecurringCredits(Double recurringCredits) {
        this.recurringCredits = recurringCredits;
    }

    public Integer getRecurringIntervalDays() {
        return recurringIntervalDays;
    }

    public void setRecurringIntervalDays(Integer recurringIntervalDays) {
        this.recurringIntervalDays = recurringIntervalDays;
    }

    public LocalDateTime getNextCreditGiftDate() {
        return nextCreditGiftDate;
    }

    public void setNextCreditGiftDate(LocalDateTime nextCreditGiftDate) {
        this.nextCreditGiftDate = nextCreditGiftDate;
    }

    public LocalDateTime getLastCreditGiftDate() {
        return lastCreditGiftDate;
    }

    public void setLastCreditGiftDate(LocalDateTime lastCreditGiftDate) {
        this.lastCreditGiftDate = lastCreditGiftDate;
    }

    public Double getOneTimeCredits() {
        return oneTimeCredits;
    }

    public void setOneTimeCredits(Double oneTimeCredits) {
        this.oneTimeCredits = oneTimeCredits;
    }

    public Boolean getOneTimeCreditUsed() {
        return oneTimeCreditUsed;
    }

    public void setOneTimeCreditUsed(Boolean oneTimeCreditUsed) {
        this.oneTimeCreditUsed = oneTimeCreditUsed;
    }

    public Boolean getCanModifyProject() {
        return canModifyProject;
    }

    public void setCanModifyProject(Boolean canModifyProject) {
        this.canModifyProject = canModifyProject;
    }

    public Boolean getCanViewAnalytics() {
        return canViewAnalytics;
    }

    public void setCanViewAnalytics(Boolean canViewAnalytics) {
        this.canViewAnalytics = canViewAnalytics;
    }

    public Boolean getCanManageSessions() {
        return canManageSessions;
    }

    public void setCanManageSessions(Boolean canManageSessions) {
        this.canManageSessions = canManageSessions;
    }

    public Boolean getCanShareFurther() {
        return canShareFurther;
    }

    public void setCanShareFurther(Boolean canShareFurther) {
        this.canShareFurther = canShareFurther;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Boolean getIsPermanent() {
        return isPermanent;
    }

    public void setIsPermanent(Boolean isPermanent) {
        this.isPermanent = isPermanent;
    }

    public String getShareMessage() {
        return shareMessage;
    }

    public void setShareMessage(String shareMessage) {
        this.shareMessage = shareMessage;
    }

    public String getTermsConditions() {
        return termsConditions;
    }

    public void setTermsConditions(String termsConditions) {
        this.termsConditions = termsConditions;
    }

    public LocalDateTime getSharedAt() {
        return sharedAt;
    }

    public void setSharedAt(LocalDateTime sharedAt) {
        this.sharedAt = sharedAt;
    }

    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(LocalDateTime acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public LocalDateTime getRejectedAt() {
        return rejectedAt;
    }

    public void setRejectedAt(LocalDateTime rejectedAt) {
        this.rejectedAt = rejectedAt;
    }

    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(LocalDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getOwnerOrganizationId() {
        return ownerOrganizationId;
    }

    public void setOwnerOrganizationId(String ownerOrganizationId) {
        this.ownerOrganizationId = ownerOrganizationId;
    }

    public String getAcceptedBy() {
        return acceptedBy;
    }

    public void setAcceptedBy(String acceptedBy) {
        this.acceptedBy = acceptedBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}