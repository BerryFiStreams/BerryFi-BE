package com.berryfi.portal.dto.team;

import com.berryfi.portal.enums.InvitationStatus;
import com.berryfi.portal.enums.Role;

import java.time.LocalDateTime;

/**
 * Response DTO for team invitation details with status information.
 */
public class TeamInvitationResponse {
    
    private String id;
    private String userEmail;
    private String organizationId;
    private String organizationName;
    private Role role;
    private String roleName;
    private InvitationStatus status;
    private String inviteToken;
    private String invitedByUserId;
    private String inviterName;
    private String inviterEmail;
    private String message;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private LocalDateTime acceptedAt;
    private String invitationLink;
    
    // Constructors
    public TeamInvitationResponse() {}
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUserEmail() {
        return userEmail;
    }
    
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
    
    public String getOrganizationId() {
        return organizationId;
    }
    
    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }
    
    public String getOrganizationName() {
        return organizationName;
    }
    
    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }
    
    public Role getRole() {
        return role;
    }
    
    public void setRole(Role role) {
        this.role = role;
    }
    
    public String getRoleName() {
        return roleName;
    }
    
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
    
    public InvitationStatus getStatus() {
        return status;
    }
    
    public void setStatus(InvitationStatus status) {
        this.status = status;
    }
    
    public String getInviteToken() {
        return inviteToken;
    }
    
    public void setInviteToken(String inviteToken) {
        this.inviteToken = inviteToken;
    }
    
    public String getInvitedByUserId() {
        return invitedByUserId;
    }
    
    public void setInvitedByUserId(String invitedByUserId) {
        this.invitedByUserId = invitedByUserId;
    }
    
    public String getInviterName() {
        return inviterName;
    }
    
    public void setInviterName(String inviterName) {
        this.inviterName = inviterName;
    }
    
    public String getInviterEmail() {
        return inviterEmail;
    }
    
    public void setInviterEmail(String inviterEmail) {
        this.inviterEmail = inviterEmail;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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
    
    public String getInvitationLink() {
        return invitationLink;
    }
    
    public void setInvitationLink(String invitationLink) {
        this.invitationLink = invitationLink;
    }
    
    // Helper methods
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    public boolean isPending() {
        return status == InvitationStatus.PENDING && !isExpired();
    }
    
    public boolean isAccepted() {
        return status == InvitationStatus.ACCEPTED;
    }
    
    public boolean isDeclined() {
        return status == InvitationStatus.DECLINED;
    }
    
    public boolean isCancelled() {
        return status == InvitationStatus.CANCELLED;
    }
    
    public boolean canBeResent() {
        return status == InvitationStatus.PENDING && !isExpired();
    }
    
    public boolean canBeCancelled() {
        return status == InvitationStatus.PENDING;
    }
    
    public String getStatusDisplay() {
        if (status == InvitationStatus.PENDING && isExpired()) {
            return "EXPIRED";
        }
        return status.name();
    }
    
    @Override
    public String toString() {
        return "TeamInvitationResponse{" +
                "id='" + id + '\'' +
                ", userEmail='" + userEmail + '\'' +
                ", organizationName='" + organizationName + '\'' +
                ", role=" + role +
                ", status=" + status +
                ", inviterName='" + inviterName + '\'' +
                ", createdAt=" + createdAt +
                ", expiresAt=" + expiresAt +
                '}';
    }
}