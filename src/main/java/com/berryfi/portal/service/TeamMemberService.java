package com.berryfi.portal.service;

import com.berryfi.portal.dto.team.TeamMemberResponse;
import com.berryfi.portal.entity.TeamMember;
import com.berryfi.portal.entity.User;
import com.berryfi.portal.entity.Workspace;
import com.berryfi.portal.enums.Role;
import com.berryfi.portal.enums.UserStatus;
import com.berryfi.portal.repository.TeamMemberRepository;
import com.berryfi.portal.repository.UserRepository;
import com.berryfi.portal.repository.WorkspaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service class for managing team members.
 */
@Service
public class TeamMemberService {
    
    @Autowired
    private TeamMemberRepository teamMemberRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private WorkspaceRepository workspaceRepository;
    
    /**
     * Invite a user to join the team.
     */
    public TeamMemberResponse inviteTeamMember(String userEmail, String organizationId, String workspaceId, 
                                              Role role, String invitedBy) {
        // Check if user exists
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if already a team member
        Optional<TeamMember> existingMember = teamMemberRepository.findByUserIdAndOrganizationId(user.getId(), organizationId);
        if (existingMember.isPresent()) {
            throw new RuntimeException("User is already a team member");
        }
        
        // Validate workspace if provided
        Workspace workspace = null;
        if (workspaceId != null) {
            workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new RuntimeException("Workspace not found"));
            
            if (!workspace.getOrganizationId().equals(organizationId)) {
                throw new RuntimeException("Workspace not found in organization");
            }
        }
        
        TeamMember teamMember = new TeamMember();
        teamMember.setId(UUID.randomUUID().toString());
        teamMember.setUserId(user.getId());
        teamMember.setUserName(user.getUsername());
        teamMember.setOrganizationId(organizationId);
        teamMember.setWorkspaceId(workspaceId);
        teamMember.setRole(role);
        teamMember.setStatus(UserStatus.INVITED);
        teamMember.setInvitedBy(invitedBy);
        teamMember.setInvitedAt(LocalDateTime.now());
        
        TeamMember savedTeamMember = teamMemberRepository.save(teamMember);
        return mapToResponse(savedTeamMember);
    }
    
    /**
     * Accept team invitation.
     */
    public TeamMemberResponse acceptInvitation(String teamMemberId, String userId) {
        TeamMember teamMember = teamMemberRepository.findById(teamMemberId)
            .orElseThrow(() -> new RuntimeException("Team member invitation not found"));
        
        if (!teamMember.getUserId().equals(userId)) {
            throw new RuntimeException("Not authorized to accept this invitation");
        }
        
        if (teamMember.getStatus() != UserStatus.INVITED) {
            throw new RuntimeException("Invitation is not pending");
        }
        
        teamMember.activate();
        TeamMember updatedTeamMember = teamMemberRepository.save(teamMember);
        return mapToResponse(updatedTeamMember);
    }
    
    /**
     * Update team member role.
     */
    public TeamMemberResponse updateTeamMemberRole(String teamMemberId, Role newRole, String organizationId) {
        TeamMember teamMember = teamMemberRepository.findById(teamMemberId)
            .orElseThrow(() -> new RuntimeException("Team member not found"));
        
        if (!teamMember.getOrganizationId().equals(organizationId)) {
            throw new RuntimeException("Team member not found in organization");
        }
        
        teamMember.setRole(newRole);
        TeamMember updatedTeamMember = teamMemberRepository.save(teamMember);
        return mapToResponse(updatedTeamMember);
    }
    
    /**
     * Get team member by ID.
     */
    public TeamMemberResponse getTeamMember(String teamMemberId, String organizationId) {
        TeamMember teamMember = teamMemberRepository.findById(teamMemberId)
            .orElseThrow(() -> new RuntimeException("Team member not found"));
        
        if (!teamMember.getOrganizationId().equals(organizationId)) {
            throw new RuntimeException("Team member not found in organization");
        }
        
        return mapToResponse(teamMember);
    }
    
    /**
     * Get team members by organization with pagination.
     */
    public Page<TeamMemberResponse> getTeamMembers(String organizationId, Pageable pageable) {
        Page<TeamMember> teamMembers = teamMemberRepository.findByOrganizationIdOrderByJoinedAtDesc(organizationId, pageable);
        return teamMembers.map(this::mapToResponse);
    }
    
    /**
     * Get team members by workspace.
     */
    public Page<TeamMemberResponse> getTeamMembersByWorkspace(String workspaceId, Pageable pageable) {
        Page<TeamMember> teamMembers = teamMemberRepository.findByWorkspaceIdOrderByJoinedAtDesc(workspaceId, pageable);
        return teamMembers.map(this::mapToResponse);
    }
    
    /**
     * Search team members.
     */
    public Page<TeamMemberResponse> searchTeamMembers(String organizationId, String searchTerm, Pageable pageable) {
        Page<TeamMember> teamMembers = teamMemberRepository.searchTeamMembers(organizationId, searchTerm, pageable);
        return teamMembers.map(this::mapToResponse);
    }
    
    /**
     * Get active team members.
     */
    public Page<TeamMemberResponse> getActiveTeamMembers(String organizationId, Pageable pageable) {
        Page<TeamMember> teamMembers = teamMemberRepository.findByOrganizationIdAndStatusOrderByJoinedAtDesc(organizationId, UserStatus.ACTIVE, pageable);
        return teamMembers.map(this::mapToResponse);
    }
    
    /**
     * Get team members by role.
     */
    public List<TeamMemberResponse> getTeamMembersByRole(String organizationId, Role role) {
        List<TeamMember> teamMembers = teamMemberRepository.findByOrganizationIdAndRole(organizationId, role);
        return teamMembers.stream().map(this::mapToResponse).toList();
    }
    
    /**
     * Remove team member.
     */
    public void removeTeamMember(String teamMemberId, String organizationId) {
        TeamMember teamMember = teamMemberRepository.findById(teamMemberId)
            .orElseThrow(() -> new RuntimeException("Team member not found"));
        
        if (!teamMember.getOrganizationId().equals(organizationId)) {
            throw new RuntimeException("Team member not found in organization");
        }
        
        teamMemberRepository.delete(teamMember);
    }
    
    /**
     * Deactivate team member.
     */
    public TeamMemberResponse deactivateTeamMember(String teamMemberId, String organizationId) {
        TeamMember teamMember = teamMemberRepository.findById(teamMemberId)
            .orElseThrow(() -> new RuntimeException("Team member not found"));
        
        if (!teamMember.getOrganizationId().equals(organizationId)) {
            throw new RuntimeException("Team member not found in organization");
        }
        
        teamMember.setStatus(UserStatus.DISABLED);
        TeamMember updatedTeamMember = teamMemberRepository.save(teamMember);
        return mapToResponse(updatedTeamMember);
    }
    
    /**
     * Reactivate team member.
     */
    public TeamMemberResponse reactivateTeamMember(String teamMemberId, String organizationId) {
        TeamMember teamMember = teamMemberRepository.findById(teamMemberId)
            .orElseThrow(() -> new RuntimeException("Team member not found"));
        
        if (!teamMember.getOrganizationId().equals(organizationId)) {
            throw new RuntimeException("Team member not found in organization");
        }
        
        teamMember.activate();
        TeamMember updatedTeamMember = teamMemberRepository.save(teamMember);
        return mapToResponse(updatedTeamMember);
    }
    
    /**
     * Get team analytics.
     */
    public TeamAnalytics getTeamAnalytics(String organizationId) {
        TeamAnalytics analytics = new TeamAnalytics();
        analytics.setTotalMembers(teamMemberRepository.countByOrganizationId(organizationId));
        analytics.setActiveMembers(teamMemberRepository.countByOrganizationIdAndStatus(organizationId, UserStatus.ACTIVE));
        analytics.setAdminMembers(teamMemberRepository.countByOrganizationIdAndRole(organizationId, Role.ORG_ADMIN));
        analytics.setManagerMembers(teamMemberRepository.countByOrganizationIdAndRole(organizationId, Role.WORKSPACE_ADMIN));
        analytics.setMemberMembers(teamMemberRepository.countByOrganizationIdAndRole(organizationId, Role.ORG_MEMBER));
        
        // Recently active members (last 7 days)
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        analytics.setRecentlyActiveMembers(teamMemberRepository.countActiveMembers(organizationId, weekAgo));
        
        return analytics;
    }
    
    /**
     * Check if user is member of organization.
     */
    public boolean isUserMemberOfOrganization(String userId, String organizationId) {
        return teamMemberRepository.existsByUserIdAndOrganizationId(userId, organizationId);
    }
    
    /**
     * Check if user is member of workspace.
     */
    public boolean isUserMemberOfWorkspace(String userId, String workspaceId) {
        return teamMemberRepository.existsByUserIdAndWorkspaceId(userId, workspaceId);
    }
    
    /**
     * Get user's workspaces.
     */
    public List<TeamMemberResponse> getUserWorkspaces(String userId) {
        List<TeamMember> teamMembers = teamMemberRepository.findActiveWorkspacesForUser(userId);
        return teamMembers.stream().map(this::mapToResponse).toList();
    }
    
    /**
     * Map TeamMember entity to TeamMemberResponse DTO.
     */
    private TeamMemberResponse mapToResponse(TeamMember teamMember) {
        TeamMemberResponse response = new TeamMemberResponse();
        response.setId(teamMember.getId());
        response.setUserId(teamMember.getUserId());
        
        // Get user information from repository
        Optional<User> user = userRepository.findById(teamMember.getUserId());
        if (user.isPresent()) {
            response.setUserName(user.get().getUsername());
            response.setUserEmail(user.get().getEmail());
            // Use name field for both first and last name display
            response.setFirstName(user.get().getName());
            response.setLastName(""); // Empty since User entity only has name field
        } else {
            // Fallback to stored user name if user not found
            response.setUserName(teamMember.getUserName());
        }
        
        response.setOrganizationId(teamMember.getOrganizationId());
        response.setWorkspaceId(teamMember.getWorkspaceId());
        response.setRole(teamMember.getRole());
        response.setStatus(teamMember.getStatus());
        response.setInvitedBy(teamMember.getInvitedBy());
        response.setInvitedAt(teamMember.getInvitedAt());
        response.setJoinedAt(teamMember.getJoinedAt());
        response.setLastActiveAt(teamMember.getLastActiveAt());
        response.setIsActive(teamMember.isActive()); // Use isActive() method instead of getIsActive()
        response.setCreatedAt(teamMember.getCreatedAt());
        response.setUpdatedAt(teamMember.getUpdatedAt());
        
        // Set permissions based on role
        response.setCanManageTeam(teamMember.getRole() == Role.ORG_ADMIN || teamMember.getRole() == Role.ORG_OWNER);
        response.setCanManageProjects(teamMember.getRole() != Role.ORG_MEMBER && teamMember.getRole() != Role.WORKSPACE_MEMBER);
        response.setCanManageCampaigns(teamMember.getRole() != Role.ORG_MEMBER && teamMember.getRole() != Role.WORKSPACE_MEMBER);
        response.setCanViewAnalytics(true); // All members can view analytics
        
        return response;
    }
    
    /**
     * Inner class for team analytics data.
     */
    public static class TeamAnalytics {
        private Long totalMembers;
        private Long activeMembers;
        private Long adminMembers;
        private Long managerMembers;
        private Long memberMembers;
        private Long recentlyActiveMembers;
        
        // Getters and setters
        public Long getTotalMembers() { return totalMembers; }
        public void setTotalMembers(Long totalMembers) { this.totalMembers = totalMembers; }
        
        public Long getActiveMembers() { return activeMembers; }
        public void setActiveMembers(Long activeMembers) { this.activeMembers = activeMembers; }
        
        public Long getAdminMembers() { return adminMembers; }
        public void setAdminMembers(Long adminMembers) { this.adminMembers = adminMembers; }
        
        public Long getManagerMembers() { return managerMembers; }
        public void setManagerMembers(Long managerMembers) { this.managerMembers = managerMembers; }
        
        public Long getMemberMembers() { return memberMembers; }
        public void setMemberMembers(Long memberMembers) { this.memberMembers = memberMembers; }
        
        public Long getRecentlyActiveMembers() { return recentlyActiveMembers; }
        public void setRecentlyActiveMembers(Long recentlyActiveMembers) { this.recentlyActiveMembers = recentlyActiveMembers; }
    }
}
