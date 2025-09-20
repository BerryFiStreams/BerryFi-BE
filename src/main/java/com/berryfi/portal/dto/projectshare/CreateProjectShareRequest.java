package com.berryfi.portal.dto.projectshare;

import com.berryfi.portal.enums.ShareType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;

import java.time.LocalDateTime;

/**
 * Request DTO for sharing project VM access with another organization.
 * This creates a trackable link for VM access with optional credit allocation.
 */
public class CreateProjectShareRequest {
    
    @NotBlank(message = "Project ID is required")
    private String projectId;
    
    @NotBlank(message = "Target organization ID is required")
    private String targetOrganizationId;
    
    private ShareType shareType = ShareType.ONE_TIME;
    
    @DecimalMin(value = "0.0", message = "Credits allocated cannot be negative")
    private Double creditsAllocated; // Gifted credits for VM usage
    
    private String message; // Optional message to shared organization
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiresAt; // When VM access expires
    
    private Integer recurringDays; // For recurring credit gifts

    // Constructors
    public CreateProjectShareRequest() {}

    // Getters and Setters
    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getTargetOrganizationId() {
        return targetOrganizationId;
    }

    public void setTargetOrganizationId(String targetOrganizationId) {
        this.targetOrganizationId = targetOrganizationId;
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
}