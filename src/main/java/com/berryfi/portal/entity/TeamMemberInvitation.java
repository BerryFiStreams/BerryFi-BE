package com.berryfi.portal.entity;

import com.berryfi.portal.enums.InvitationStatus;
import com.berryfi.portal.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a team member invitation sent to a user.
 */
@Entity
@Table(name = "team_member_invitations", indexes = {
    @Index(name = "idx_team_invitation_email", columnList = "inviteEmail"),
    @Index(name = "idx_team_invitation_token", columnList = "inviteToken", unique = true),
    @Index(name = "idx_team_invitation_status", columnList = "status"),
    @Index(name = "idx_team_invitation_organization", columnList = "organizationId"),
    @Index(name = "idx_team_invitation_expires", columnList = "expiresAt")
})
public class TeamMemberInvitation {

    @Id
    @Column(name = "id", length = 50)
    private String id;

    @NotBlank(message = "Organization ID is required")
    @Column(name = "organization_id", nullable = false)
    private String organizationId;

    @NotBlank(message = "Invited by user ID is required")
    @Column(name = "invited_by_user_id", nullable = false)
    private String invitedByUserId;

    @Email(message = "Valid email is required")
    @NotBlank(message = "Invite email is required")
    @Column(name = "invite_email", nullable = false)
    private String inviteEmail;

    @NotBlank(message = "Invite token is required")
    @Column(name = "invite_token", nullable = false, unique = true)
    private String inviteToken;

    @NotNull(message = "Role is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InvitationStatus status = InvitationStatus.PENDING;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    // Tracking timestamps
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

    // Registration details (filled when user accepts invitation)
    @Column(name = "registered_user_id")
    private String registeredUserId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public TeamMemberInvitation() {
        this.id = generateInvitationId();
        this.inviteToken = generateInviteToken();
        this.sentAt = LocalDateTime.now();
        this.lastEmailSentAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusDays(7); // 7 days to accept
    }

    public TeamMemberInvitation(String organizationId, String invitedByUserId, String inviteEmail, Role role) {
        this();
        this.organizationId = organizationId;
        this.invitedByUserId = invitedByUserId;
        this.inviteEmail = inviteEmail;
        this.role = role;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    private String generateInvitationId() {
        return "tmi_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
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
        // If already marked as expired, return true
        if (this.status == InvitationStatus.EXPIRED) {
            return true;
        }
        
        // Only check time-based expiration for pending invitations
        // Accepted, declined, or cancelled invitations should not be considered "expired"
        if (this.status == InvitationStatus.PENDING) {
            return LocalDateTime.now().isAfter(this.expiresAt);
        }
        
        // For any other status (ACCEPTED, DECLINED), they are not expired
        return false;
    }

    /**
     * Check if this PENDING invitation should be expired based on time.
     * This method is specifically for checking if a pending invitation needs to be marked as expired.
     */
    public boolean shouldBeExpired() {
        return this.status == InvitationStatus.PENDING && LocalDateTime.now().isAfter(this.expiresAt);
    }

    // Status change methods
    public void acceptInvitation(String userId) {
        this.status = InvitationStatus.ACCEPTED;
        this.acceptedAt = LocalDateTime.now();
        this.registeredUserId = userId;
    }

    public void declineInvitation() {
        this.status = InvitationStatus.DECLINED;
        this.declinedAt = LocalDateTime.now();
    }

    public void expireInvitation() {
        this.status = InvitationStatus.EXPIRED;
    }

    public void resendInvitation() {
        this.emailSentCount = (this.emailSentCount != null ? this.emailSentCount : 0) + 1;
        this.lastEmailSentAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }

    public String getInvitedByUserId() { return invitedByUserId; }
    public void setInvitedByUserId(String invitedByUserId) { this.invitedByUserId = invitedByUserId; }

    public String getInviteEmail() { return inviteEmail; }
    public void setInviteEmail(String inviteEmail) { this.inviteEmail = inviteEmail; }

    public String getInviteToken() { return inviteToken; }
    public void setInviteToken(String inviteToken) { this.inviteToken = inviteToken; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public InvitationStatus getStatus() { return status; }
    public void setStatus(InvitationStatus status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public LocalDateTime getAcceptedAt() { return acceptedAt; }
    public void setAcceptedAt(LocalDateTime acceptedAt) { this.acceptedAt = acceptedAt; }

    public LocalDateTime getDeclinedAt() { return declinedAt; }
    public void setDeclinedAt(LocalDateTime declinedAt) { this.declinedAt = declinedAt; }

    public Integer getEmailSentCount() { return emailSentCount; }
    public void setEmailSentCount(Integer emailSentCount) { this.emailSentCount = emailSentCount; }

    public LocalDateTime getLastEmailSentAt() { return lastEmailSentAt; }
    public void setLastEmailSentAt(LocalDateTime lastEmailSentAt) { this.lastEmailSentAt = lastEmailSentAt; }

    public String getRegisteredUserId() { return registeredUserId; }
    public void setRegisteredUserId(String registeredUserId) { this.registeredUserId = registeredUserId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}