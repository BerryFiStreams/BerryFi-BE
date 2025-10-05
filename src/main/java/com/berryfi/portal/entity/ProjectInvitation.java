package com.berryfi.portal.entity;

import com.berryfi.portal.enums.InvitationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a project sharing invitation sent to a user.
 */
@Entity
@Table(name = "project_invitations", indexes = {
    @Index(name = "idx_invitation_email", columnList = "inviteEmail"),
    @Index(name = "idx_invitation_token", columnList = "inviteToken", unique = true),
    @Index(name = "idx_invitation_status", columnList = "status"),
    @Index(name = "idx_invitation_project", columnList = "projectId"),
    @Index(name = "idx_invitation_expires", columnList = "expiresAt")
})
public class ProjectInvitation {

    @Id
    @Column(name = "id", length = 50)
    private String id;

    @NotBlank(message = "Project ID is required")
    @Column(name = "project_id", nullable = false)
    private String projectId;

    @NotBlank(message = "Invited by user ID is required")
    @Column(name = "invited_by_user_id", nullable = false)
    private String invitedByUserId;

    @NotBlank(message = "Invited by organization ID is required")
    @Column(name = "invited_by_organization_id", nullable = false)
    private String invitedByOrganizationId;

    @Email(message = "Valid email is required")
    @NotBlank(message = "Invite email is required")
    @Column(name = "invite_email", nullable = false)
    private String inviteEmail;

    @NotBlank(message = "Invite token is required")
    @Column(name = "invite_token", nullable = false, unique = true)
    private String inviteToken;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InvitationStatus status = InvitationStatus.PENDING;

    // Share details from original request
    @Column(name = "initial_credits")
    private Double initialCredits = 0.0;

    @Column(name = "monthly_recurring_credits")
    private Double monthlyRecurringCredits = 0.0;

    @Column(name = "can_view_analytics")
    private Boolean canViewAnalytics = true;

    @Column(name = "can_manage_sessions")
    private Boolean canManageSessions = true;

    @Column(name = "can_share_further")
    private Boolean canShareFurther = false;

    @Column(name = "is_permanent")
    private Boolean isPermanent = false;

    @Column(name = "share_message", columnDefinition = "TEXT")
    private String shareMessage;

    // Registration details (filled when user accepts invitation)
    @Column(name = "registered_user_id")
    private String registeredUserId;

    @Column(name = "registered_organization_id")
    private String registeredOrganizationId;

    // Tracking
    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "declined_at")
    private LocalDateTime declinedAt;

    @Column(name = "email_sent_count")
    private Integer emailSentCount = 1;

    @Column(name = "last_email_sent_at")
    private LocalDateTime lastEmailSentAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public ProjectInvitation() {
        this.id = generateInvitationId();
        this.inviteToken = generateInviteToken();
        this.sentAt = LocalDateTime.now();
        this.lastEmailSentAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusDays(7); // 7 days to accept
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public ProjectInvitation(String projectId, String invitedByUserId, String invitedByOrganizationId, String inviteEmail) {
        this();
        this.projectId = projectId;
        this.invitedByUserId = invitedByUserId;
        this.invitedByOrganizationId = invitedByOrganizationId;
        this.inviteEmail = inviteEmail;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    private String generateInvitationId() {
        return "inv_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    private String generateInviteToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    // Helper methods
    public boolean isPending() {
        return this.status == InvitationStatus.PENDING;
    }

    public boolean isAccepted() {
        return this.status == InvitationStatus.ACCEPTED;
    }

    public boolean isDeclined() {
        return this.status == InvitationStatus.DECLINED;
    }

    public boolean isExpired() {
        return this.status == InvitationStatus.EXPIRED || LocalDateTime.now().isAfter(this.expiresAt);
    }

    public void acceptInvitation(String userId, String organizationId) {
        this.status = InvitationStatus.ACCEPTED;
        this.acceptedAt = LocalDateTime.now();
        this.registeredUserId = userId;
        this.registeredOrganizationId = organizationId;
    }

    public void declineInvitation() {
        this.status = InvitationStatus.DECLINED;
        this.declinedAt = LocalDateTime.now();
    }

    public void expireInvitation() {
        this.status = InvitationStatus.EXPIRED;
    }

    public void resendInvitation() {
        this.emailSentCount++;
        this.lastEmailSentAt = LocalDateTime.now();
        // Extend expiry by 7 days
        this.expiresAt = LocalDateTime.now().plusDays(7);
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

    public String getInvitedByUserId() {
        return invitedByUserId;
    }

    public void setInvitedByUserId(String invitedByUserId) {
        this.invitedByUserId = invitedByUserId;
    }

    public String getInvitedByOrganizationId() {
        return invitedByOrganizationId;
    }

    public void setInvitedByOrganizationId(String invitedByOrganizationId) {
        this.invitedByOrganizationId = invitedByOrganizationId;
    }

    public String getInviteEmail() {
        return inviteEmail;
    }

    public void setInviteEmail(String inviteEmail) {
        this.inviteEmail = inviteEmail;
    }

    public String getInviteToken() {
        return inviteToken;
    }

    public void setInviteToken(String inviteToken) {
        this.inviteToken = inviteToken;
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

    public String getRegisteredUserId() {
        return registeredUserId;
    }

    public void setRegisteredUserId(String registeredUserId) {
        this.registeredUserId = registeredUserId;
    }

    public String getRegisteredOrganizationId() {
        return registeredOrganizationId;
    }

    public void setRegisteredOrganizationId(String registeredOrganizationId) {
        this.registeredOrganizationId = registeredOrganizationId;
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

    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(LocalDateTime acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public LocalDateTime getDeclinedAt() {
        return declinedAt;
    }

    public void setDeclinedAt(LocalDateTime declinedAt) {
        this.declinedAt = declinedAt;
    }

    public Integer getEmailSentCount() {
        return emailSentCount;
    }

    public void setEmailSentCount(Integer emailSentCount) {
        this.emailSentCount = emailSentCount;
    }

    public LocalDateTime getLastEmailSentAt() {
        return lastEmailSentAt;
    }

    public void setLastEmailSentAt(LocalDateTime lastEmailSentAt) {
        this.lastEmailSentAt = lastEmailSentAt;
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