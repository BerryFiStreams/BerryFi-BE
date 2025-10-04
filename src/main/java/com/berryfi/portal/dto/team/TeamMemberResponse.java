package com.berryfi.portal.dto.team;

import com.berryfi.portal.enums.Role;
import com.berryfi.portal.enums.UserStatus;

import java.time.LocalDateTime;

/**
 * Response DTO for team member data.
 */
public class TeamMemberResponse {
    
    private String id;
    private String userId;
    private String userName;
    private String userEmail;
    private String firstName;
    private String lastName;
    private String organizationId;
    private String organizationName;
    private Role role;
    private UserStatus status;
    private String invitedBy;
    private String invitedByName;
    private LocalDateTime invitedAt;
    private LocalDateTime joinedAt;
    private LocalDateTime lastActiveAt;
    private Boolean isActive;
    private String profileImageUrl;
    
    // Permission/access related
    private Boolean canManageTeam;
    private Boolean canManageProjects;
    private Boolean canManageCampaigns;
    private Boolean canViewAnalytics;
    
    // Activity metrics
    private Integer projectsCreated;
    private Integer campaignsCreated;
    private Integer leadsManaged;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Default constructor
    public TeamMemberResponse() {}
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
    
    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }
    

    
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    
    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }
    
    public String getInvitedBy() { return invitedBy; }
    public void setInvitedBy(String invitedBy) { this.invitedBy = invitedBy; }
    
    public String getInvitedByName() { return invitedByName; }
    public void setInvitedByName(String invitedByName) { this.invitedByName = invitedByName; }
    
    public LocalDateTime getInvitedAt() { return invitedAt; }
    public void setInvitedAt(LocalDateTime invitedAt) { this.invitedAt = invitedAt; }
    
    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
    
    public LocalDateTime getLastActiveAt() { return lastActiveAt; }
    public void setLastActiveAt(LocalDateTime lastActiveAt) { this.lastActiveAt = lastActiveAt; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
    
    public Boolean getCanManageTeam() { return canManageTeam; }
    public void setCanManageTeam(Boolean canManageTeam) { this.canManageTeam = canManageTeam; }
    
    public Boolean getCanManageProjects() { return canManageProjects; }
    public void setCanManageProjects(Boolean canManageProjects) { this.canManageProjects = canManageProjects; }
    
    public Boolean getCanManageCampaigns() { return canManageCampaigns; }
    public void setCanManageCampaigns(Boolean canManageCampaigns) { this.canManageCampaigns = canManageCampaigns; }
    
    public Boolean getCanViewAnalytics() { return canViewAnalytics; }
    public void setCanViewAnalytics(Boolean canViewAnalytics) { this.canViewAnalytics = canViewAnalytics; }
    
    public Integer getProjectsCreated() { return projectsCreated; }
    public void setProjectsCreated(Integer projectsCreated) { this.projectsCreated = projectsCreated; }
    
    public Integer getCampaignsCreated() { return campaignsCreated; }
    public void setCampaignsCreated(Integer campaignsCreated) { this.campaignsCreated = campaignsCreated; }
    
    public Integer getLeadsManaged() { return leadsManaged; }
    public void setLeadsManaged(Integer leadsManaged) { this.leadsManaged = leadsManaged; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
