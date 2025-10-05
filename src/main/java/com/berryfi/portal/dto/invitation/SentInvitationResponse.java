package com.berryfi.portal.dto.invitation;

import com.berryfi.portal.enums.InvitationStatus;

import java.time.LocalDateTime;

/**
 * DTO for displaying sent invitation details to the invitation sender.
 */
public class SentInvitationResponse {

    private String inviteToken;
    private String projectName;
    private String projectId;
    private String inviteeEmail;
    private InvitationStatus status;
    private Double initialCredits;
    private Double monthlyRecurringCredits;
    private String shareMessage;
    private LocalDateTime sentAt;
    private LocalDateTime expiresAt;
    private LocalDateTime lastActivityAt; // When invitation was last interacted with

    public SentInvitationResponse() {}

    public SentInvitationResponse(String inviteToken, String projectName, String projectId, 
                                String inviteeEmail, InvitationStatus status, 
                                Double initialCredits, Double monthlyRecurringCredits,
                                String shareMessage, LocalDateTime sentAt, 
                                LocalDateTime expiresAt, LocalDateTime lastActivityAt) {
        this.inviteToken = inviteToken;
        this.projectName = projectName;
        this.projectId = projectId;
        this.inviteeEmail = inviteeEmail;
        this.status = status;
        this.initialCredits = initialCredits;
        this.monthlyRecurringCredits = monthlyRecurringCredits;
        this.shareMessage = shareMessage;
        this.sentAt = sentAt;
        this.expiresAt = expiresAt;
        this.lastActivityAt = lastActivityAt;
    }

    // Getters and Setters
    public String getInviteToken() {
        return inviteToken;
    }

    public void setInviteToken(String inviteToken) {
        this.inviteToken = inviteToken;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getInviteeEmail() {
        return inviteeEmail;
    }

    public void setInviteeEmail(String inviteeEmail) {
        this.inviteeEmail = inviteeEmail;
    }

    public InvitationStatus getStatus() {
        return status;
    }

    public void setStatus(InvitationStatus status) {
        this.status = status;
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

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getLastActivityAt() {
        return lastActivityAt;
    }

    public void setLastActivityAt(LocalDateTime lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

    /**
     * Check if this invitation is expired.
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Check if this invitation can be resent.
     */
    public boolean canResend() {
        return status == InvitationStatus.PENDING && !isExpired();
    }
}