package com.berryfi.portal.entity;

import com.berryfi.portal.enums.Role;
import com.berryfi.portal.enums.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing a team member relationship between users and organizations.
 */
@Entity
@Table(name = "team_members", indexes = {
    @Index(name = "idx_team_organization", columnList = "organizationId"),
    @Index(name = "idx_team_user", columnList = "userId"),
    @Index(name = "idx_team_role", columnList = "role")
})
public class TeamMember {
    
    @Id
    @Column(length = 50)
    private String id;
    
    @NotBlank(message = "User ID is required")
    @Column(nullable = false)
    private String userId;
    
    @NotBlank(message = "User name is required")
    @Column(nullable = false)
    private String userName;
    
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @Column(nullable = false)
    private String email;
    
    @NotNull(message = "Role is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    
    @NotBlank(message = "Organization ID is required")
    @Column(nullable = false)
    private String organizationId;
    
    // Optional project-specific access (for shared project members)
    private String projectId;
    
    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;
    
    private LocalDateTime invitedAt;
    private LocalDateTime joinedAt;
    private LocalDateTime lastActiveAt;
    
    @NotBlank(message = "Invited by is required")
    @Column(nullable = false)
    private String invitedBy;
    
    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    // Default constructor
    public TeamMember() {}
    
    // Constructor
    public TeamMember(String id, String userId, String userName, String email, 
                     Role role, String organizationId, UserStatus status, String invitedBy) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.email = email;
        this.role = role;
        this.organizationId = organizationId;
        this.status = status;
        this.invitedBy = invitedBy;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    
    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
    
    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }
    
    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }
    
    public LocalDateTime getInvitedAt() { return invitedAt; }
    public void setInvitedAt(LocalDateTime invitedAt) { this.invitedAt = invitedAt; }
    
    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
    
    public LocalDateTime getLastActiveAt() { return lastActiveAt; }
    public void setLastActiveAt(LocalDateTime lastActiveAt) { this.lastActiveAt = lastActiveAt; }
    
    public String getInvitedBy() { return invitedBy; }
    public void setInvitedBy(String invitedBy) { this.invitedBy = invitedBy; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Helper methods
    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }
    
    public boolean isInvited() {
        return status == UserStatus.INVITED;
    }
    
    public void activate() {
        this.status = UserStatus.ACTIVE;
        this.joinedAt = LocalDateTime.now();
    }
    
    public void updateLastActive() {
        this.lastActiveAt = LocalDateTime.now();
    }

    // Helper methods for role and project access
    public boolean isOrganizationMember() {
        return this.projectId == null;
    }

    public boolean isProjectMember() {
        return this.projectId != null;
    }

    public boolean hasOrganizationAccess() {
        return isOrganizationMember() && role.isOrganizationLevel();
    }

    public boolean hasProjectAccess() {
        return isProjectMember() && role.isProjectLevel();
    }

    public boolean canManageProjects() {
        return role.canManageProjects() && isOrganizationMember();
    }

    public boolean canShareProjects() {
        return role.canShareProjects() && isOrganizationMember();
    }
}
