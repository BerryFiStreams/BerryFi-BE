package com.berryfi.portal.dto.project;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.AssertTrue;

/**
 * DTO for sharing a project with another organization or user.
 * Either organizationId or userEmail must be provided.
 */
public class ShareProjectRequest {

    private String organizationId;

    @Email(message = "Please provide a valid email address")
    private String userEmail;

    @Min(value = 0, message = "Initial credits must be non-negative")
    private Double initialCredits = 0.0;

    @Min(value = 0, message = "Monthly recurring credits must be non-negative")
    private Double monthlyRecurringCredits = 0.0;

    // Optional sharing configuration
    private Boolean canViewAnalytics = true;
    private Boolean canManageSessions = true;
    private Boolean canShareFurther = false;
    private Boolean isPermanent = false;
    private String shareMessage;

    public ShareProjectRequest() {}

    public ShareProjectRequest(String organizationId, String userEmail, Double initialCredits, Double monthlyRecurringCredits) {
        this.organizationId = organizationId;
        this.userEmail = userEmail;
        this.initialCredits = initialCredits;
        this.monthlyRecurringCredits = monthlyRecurringCredits;
    }

    /**
     * Validation: Either organizationId or userEmail must be provided, but not both
     */
    @AssertTrue(message = "Either organizationId or userEmail must be provided, but not both")
    public boolean isValidTarget() {
        boolean hasOrgId = organizationId != null && !organizationId.trim().isEmpty();
        boolean hasEmail = userEmail != null && !userEmail.trim().isEmpty();
        return (hasOrgId && !hasEmail) || (!hasOrgId && hasEmail);
    }

    // Getters and Setters
    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public Double getInitialCredits() {
        return initialCredits;
    }

    public void setInitialCredits(Double initialCredits) {
        this.initialCredits = initialCredits;
    }

    public Double getMonthlyRecurringCredits() {
        return monthlyRecurringCredits;
    }

    public void setMonthlyRecurringCredits(Double monthlyRecurringCredits) {
        this.monthlyRecurringCredits = monthlyRecurringCredits;
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
}