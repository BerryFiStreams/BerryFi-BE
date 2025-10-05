package com.berryfi.portal.dto.invitation;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * DTO for invitation details response.
 */
public class InvitationDetailsResponse {

    @JsonProperty("inviteToken")
    private String inviteToken;

    @JsonProperty("inviteEmail")
    private String inviteEmail;

    @JsonProperty("projectId")
    private String projectId;

    @JsonProperty("projectName")
    private String projectName;

    @JsonProperty("invitedByUserName")
    private String invitedByUserName;

    @JsonProperty("invitedByOrganizationName")
    private String invitedByOrganizationName;

    @JsonProperty("initialCredits")
    private Double initialCredits;

    @JsonProperty("monthlyRecurringCredits")
    private Double monthlyRecurringCredits;

    @JsonProperty("shareMessage")
    private String shareMessage;

    @JsonProperty("expiresAt")
    private LocalDateTime expiresAt;

    @JsonProperty("isExpired")
    private Boolean isExpired;

    @JsonProperty("status")
    private String status;

    // Constructors
    public InvitationDetailsResponse() {}

    public InvitationDetailsResponse(String inviteToken, String inviteEmail, String projectName,
                                   String invitedByUserName, String invitedByOrganizationName) {
        this.inviteToken = inviteToken;
        this.inviteEmail = inviteEmail;
        this.projectName = projectName;
        this.invitedByUserName = invitedByUserName;
        this.invitedByOrganizationName = invitedByOrganizationName;
    }

    // Getters and Setters
    public String getInviteToken() {
        return inviteToken;
    }

    public void setInviteToken(String inviteToken) {
        this.inviteToken = inviteToken;
    }

    public String getInviteEmail() {
        return inviteEmail;
    }

    public void setInviteEmail(String inviteEmail) {
        this.inviteEmail = inviteEmail;
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

    public String getInvitedByUserName() {
        return invitedByUserName;
    }

    public void setInvitedByUserName(String invitedByUserName) {
        this.invitedByUserName = invitedByUserName;
    }

    public String getInvitedByOrganizationName() {
        return invitedByOrganizationName;
    }

    public void setInvitedByOrganizationName(String invitedByOrganizationName) {
        this.invitedByOrganizationName = invitedByOrganizationName;
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

    public String getShareMessage() {
        return shareMessage;
    }

    public void setShareMessage(String shareMessage) {
        this.shareMessage = shareMessage;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Boolean getIsExpired() {
        return isExpired;
    }

    public void setIsExpired(Boolean isExpired) {
        this.isExpired = isExpired;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "InvitationDetailsResponse{" +
                "inviteToken='" + inviteToken + '\'' +
                ", inviteEmail='" + inviteEmail + '\'' +
                ", projectName='" + projectName + '\'' +
                ", invitedByOrganizationName='" + invitedByOrganizationName + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}