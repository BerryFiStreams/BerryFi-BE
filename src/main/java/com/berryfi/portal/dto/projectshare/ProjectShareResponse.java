package com.berryfi.portal.dto.projectshare;

import com.berryfi.portal.enums.ProjectShareStatus;
import com.berryfi.portal.enums.ShareType;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * Response DTO for project VM access sharing details.
 * Contains information about trackable VM access links and credit usage.
 */
public class ProjectShareResponse {
    
    private String id;
    private String projectId;
    private String projectName;
    private String sharingOrganizationId;
    private String sharingOrganizationName;
    private String targetOrganizationId;
    private String targetOrganizationName;
    private String sharedById;
    private String sharedByName;
    private ProjectShareStatus status;
    private ShareType shareType;
    private Double creditsAllocated;
    private Double creditsUsed;
    private Double remainingCredits;
    private String message;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiresAt;
    
    private Integer recurringDays;
    private String acceptedById;
    private String acceptedByName;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime acceptedAt;
    
    private String rejectedById;
    private String rejectedByName;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rejectedAt;
    
    private String rejectionReason;
    private String revokedById;
    private String revokedByName;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime revokedAt;
    
    private String revocationReason;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastCreditRefillAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // Constructors
    public ProjectShareResponse() {}

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

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getSharingOrganizationId() {
        return sharingOrganizationId;
    }

    public void setSharingOrganizationId(String sharingOrganizationId) {
        this.sharingOrganizationId = sharingOrganizationId;
    }

    public String getSharingOrganizationName() {
        return sharingOrganizationName;
    }

    public void setSharingOrganizationName(String sharingOrganizationName) {
        this.sharingOrganizationName = sharingOrganizationName;
    }

    public String getTargetOrganizationId() {
        return targetOrganizationId;
    }

    public void setTargetOrganizationId(String targetOrganizationId) {
        this.targetOrganizationId = targetOrganizationId;
    }

    public String getTargetOrganizationName() {
        return targetOrganizationName;
    }

    public void setTargetOrganizationName(String targetOrganizationName) {
        this.targetOrganizationName = targetOrganizationName;
    }

    public String getSharedById() {
        return sharedById;
    }

    public void setSharedById(String sharedById) {
        this.sharedById = sharedById;
    }

    public String getSharedByName() {
        return sharedByName;
    }

    public void setSharedByName(String sharedByName) {
        this.sharedByName = sharedByName;
    }

    public ProjectShareStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectShareStatus status) {
        this.status = status;
    }

    public ShareType getShareType() {
        return shareType;
    }

    public void setShareType(ShareType shareType) {
        this.shareType = shareType;
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

    public Double getRemainingCredits() {
        return remainingCredits;
    }

    public void setRemainingCredits(Double remainingCredits) {
        this.remainingCredits = remainingCredits;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Integer getRecurringDays() {
        return recurringDays;
    }

    public void setRecurringDays(Integer recurringDays) {
        this.recurringDays = recurringDays;
    }

    public String getAcceptedById() {
        return acceptedById;
    }

    public void setAcceptedById(String acceptedById) {
        this.acceptedById = acceptedById;
    }

    public String getAcceptedByName() {
        return acceptedByName;
    }

    public void setAcceptedByName(String acceptedByName) {
        this.acceptedByName = acceptedByName;
    }

    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(LocalDateTime acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public String getRejectedById() {
        return rejectedById;
    }

    public void setRejectedById(String rejectedById) {
        this.rejectedById = rejectedById;
    }

    public String getRejectedByName() {
        return rejectedByName;
    }

    public void setRejectedByName(String rejectedByName) {
        this.rejectedByName = rejectedByName;
    }

    public LocalDateTime getRejectedAt() {
        return rejectedAt;
    }

    public void setRejectedAt(LocalDateTime rejectedAt) {
        this.rejectedAt = rejectedAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getRevokedById() {
        return revokedById;
    }

    public void setRevokedById(String revokedById) {
        this.revokedById = revokedById;
    }

    public String getRevokedByName() {
        return revokedByName;
    }

    public void setRevokedByName(String revokedByName) {
        this.revokedByName = revokedByName;
    }

    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(LocalDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }

    public String getRevocationReason() {
        return revocationReason;
    }

    public void setRevocationReason(String revocationReason) {
        this.revocationReason = revocationReason;
    }

    public LocalDateTime getLastCreditRefillAt() {
        return lastCreditRefillAt;
    }

    public void setLastCreditRefillAt(LocalDateTime lastCreditRefillAt) {
        this.lastCreditRefillAt = lastCreditRefillAt;
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