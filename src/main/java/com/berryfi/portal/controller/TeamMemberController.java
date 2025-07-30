package com.berryfi.portal.controller;

import com.berryfi.portal.dto.team.TeamMemberResponse;
import com.berryfi.portal.enums.Role;
import com.berryfi.portal.service.TeamMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for team member management operations.
 */
@RestController
@RequestMapping("/api/team/members")
@CrossOrigin(origins = "*")
public class TeamMemberController {
    
    @Autowired
    private TeamMemberService teamMemberService;
    
    /**
     * Invite a user to join the team.
     */
    @PostMapping("/invite")
    public ResponseEntity<TeamMemberResponse> inviteTeamMember(
            @RequestParam String userEmail,
            @RequestHeader("X-Organization-ID") String organizationId,
            @RequestParam(required = false) String workspaceId,
            @RequestParam Role role,
            @RequestHeader("X-User-ID") String invitedBy) {
        try {
            TeamMemberResponse response = teamMemberService.inviteTeamMember(
                userEmail, organizationId, workspaceId, role, invitedBy);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Accept team invitation.
     */
    @PostMapping("/{teamMemberId}/accept")
    public ResponseEntity<TeamMemberResponse> acceptInvitation(
            @PathVariable String teamMemberId,
            @RequestHeader("X-User-ID") String userId) {
        try {
            TeamMemberResponse response = teamMemberService.acceptInvitation(teamMemberId, userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get team member by ID.
     */
    @GetMapping("/{teamMemberId}")
    public ResponseEntity<TeamMemberResponse> getTeamMember(
            @PathVariable String teamMemberId,
            @RequestHeader("X-Organization-ID") String organizationId) {
        try {
            TeamMemberResponse response = teamMemberService.getTeamMember(teamMemberId, organizationId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get team members by organization with pagination.
     */
    @GetMapping
    public ResponseEntity<Page<TeamMemberResponse>> getTeamMembers(
            @RequestHeader("X-Organization-ID") String organizationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TeamMemberResponse> teamMembers = teamMemberService.getTeamMembers(organizationId, pageable);
        return ResponseEntity.ok(teamMembers);
    }
    
    /**
     * Get team members by workspace.
     */
    @GetMapping("/workspace/{workspaceId}")
    public ResponseEntity<Page<TeamMemberResponse>> getTeamMembersByWorkspace(
            @PathVariable String workspaceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TeamMemberResponse> teamMembers = teamMemberService.getTeamMembersByWorkspace(workspaceId, pageable);
        return ResponseEntity.ok(teamMembers);
    }
    
    /**
     * Search team members.
     */
    @GetMapping("/search")
    public ResponseEntity<Page<TeamMemberResponse>> searchTeamMembers(
            @RequestHeader("X-Organization-ID") String organizationId,
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TeamMemberResponse> teamMembers = teamMemberService.searchTeamMembers(organizationId, q, pageable);
        return ResponseEntity.ok(teamMembers);
    }
    
    /**
     * Get active team members.
     */
    @GetMapping("/active")
    public ResponseEntity<Page<TeamMemberResponse>> getActiveTeamMembers(
            @RequestHeader("X-Organization-ID") String organizationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TeamMemberResponse> teamMembers = teamMemberService.getActiveTeamMembers(organizationId, pageable);
        return ResponseEntity.ok(teamMembers);
    }
    
    /**
     * Get team members by role.
     */
    @GetMapping("/role/{role}")
    public ResponseEntity<List<TeamMemberResponse>> getTeamMembersByRole(
            @RequestHeader("X-Organization-ID") String organizationId,
            @PathVariable Role role) {
        List<TeamMemberResponse> teamMembers = teamMemberService.getTeamMembersByRole(organizationId, role);
        return ResponseEntity.ok(teamMembers);
    }
    
    /**
     * Update team member role.
     */
    @PutMapping("/{teamMemberId}/role")
    public ResponseEntity<TeamMemberResponse> updateTeamMemberRole(
            @PathVariable String teamMemberId,
            @RequestParam Role newRole,
            @RequestHeader("X-Organization-ID") String organizationId) {
        try {
            TeamMemberResponse response = teamMemberService.updateTeamMemberRole(teamMemberId, newRole, organizationId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Deactivate team member.
     */
    @PutMapping("/{teamMemberId}/deactivate")
    public ResponseEntity<TeamMemberResponse> deactivateTeamMember(
            @PathVariable String teamMemberId,
            @RequestHeader("X-Organization-ID") String organizationId) {
        try {
            TeamMemberResponse response = teamMemberService.deactivateTeamMember(teamMemberId, organizationId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Reactivate team member.
     */
    @PutMapping("/{teamMemberId}/reactivate")
    public ResponseEntity<TeamMemberResponse> reactivateTeamMember(
            @PathVariable String teamMemberId,
            @RequestHeader("X-Organization-ID") String organizationId) {
        try {
            TeamMemberResponse response = teamMemberService.reactivateTeamMember(teamMemberId, organizationId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Remove team member.
     */
    @DeleteMapping("/{teamMemberId}")
    public ResponseEntity<Void> removeTeamMember(
            @PathVariable String teamMemberId,
            @RequestHeader("X-Organization-ID") String organizationId) {
        try {
            teamMemberService.removeTeamMember(teamMemberId, organizationId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get user's workspaces.
     */
    @GetMapping("/user/{userId}/workspaces")
    public ResponseEntity<List<TeamMemberResponse>> getUserWorkspaces(@PathVariable String userId) {
        List<TeamMemberResponse> workspaces = teamMemberService.getUserWorkspaces(userId);
        return ResponseEntity.ok(workspaces);
    }
    
    /**
     * Check if user is member of organization.
     */
    @GetMapping("/check/organization/{userId}")
    public ResponseEntity<Boolean> isUserMemberOfOrganization(
            @PathVariable String userId,
            @RequestHeader("X-Organization-ID") String organizationId) {
        boolean isMember = teamMemberService.isUserMemberOfOrganization(userId, organizationId);
        return ResponseEntity.ok(isMember);
    }
    
    /**
     * Check if user is member of workspace.
     */
    @GetMapping("/check/workspace/{userId}")
    public ResponseEntity<Boolean> isUserMemberOfWorkspace(
            @PathVariable String userId,
            @RequestParam String workspaceId) {
        boolean isMember = teamMemberService.isUserMemberOfWorkspace(userId, workspaceId);
        return ResponseEntity.ok(isMember);
    }
    
    /**
     * Get team analytics.
     */
    @GetMapping("/analytics")
    public ResponseEntity<TeamMemberService.TeamAnalytics> getTeamAnalytics(
            @RequestHeader("X-Organization-ID") String organizationId) {
        TeamMemberService.TeamAnalytics analytics = teamMemberService.getTeamAnalytics(organizationId);
        return ResponseEntity.ok(analytics);
    }
}
