package com.berryfi.portal.controller;

import com.berryfi.portal.dto.team.TeamMemberResponse;
import com.berryfi.portal.entity.User;
import com.berryfi.portal.enums.Role;
import com.berryfi.portal.service.TeamMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for team member management operations.
 * Enhanced to use JWT token for user authentication and organization extraction.
 */
@RestController
@RequestMapping("/api/team/members")
@CrossOrigin(origins = "*")
public class TeamMemberController {
    
    @Autowired
    private TeamMemberService teamMemberService;
    
    /**
     * Invite a user to join the team.
     * Uses authenticated user information for organization and inviter details.
     */
    @PostMapping("/invite")
    public ResponseEntity<?> inviteTeamMember(
            @RequestParam String userEmail,
            @RequestParam Role role,
            @AuthenticationPrincipal User currentUser) {
        try {
            String organizationId = currentUser.getOrganizationId();
            String invitedBy = currentUser.getId();
            
            TeamMemberResponse response = teamMemberService.inviteTeamMember(
                userEmail, organizationId, role, invitedBy);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                new ErrorResponse("Failed to invite team member: " + e.getMessage())
            );
        }
    }
    
    /**
     * Accept team invitation.
     * Uses authenticated user information.
     */
    @PostMapping("/{teamMemberId}/accept")
    public ResponseEntity<?> acceptInvitation(
            @PathVariable String teamMemberId,
            @AuthenticationPrincipal User currentUser) {
        try {
            String userId = currentUser.getId();
            
            TeamMemberResponse response = teamMemberService.acceptInvitation(teamMemberId, userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                new ErrorResponse("Failed to accept invitation: " + e.getMessage())
            );
        }
    }
    
    /**
     * Get team member by ID.
     * Uses authenticated user's organization information.
     */
    @GetMapping("/{teamMemberId}")
    public ResponseEntity<?> getTeamMember(
            @PathVariable String teamMemberId,
            @AuthenticationPrincipal User currentUser) {
        try {
            String organizationId = currentUser.getOrganizationId();
            
            TeamMemberResponse response = teamMemberService.getTeamMember(teamMemberId, organizationId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                new ErrorResponse("Failed to get team member: " + e.getMessage())
            );
        }
    }
    
    /**
     * Get team members by organization with pagination.
     * Uses authenticated user's organization information.
     */
    @GetMapping
    public ResponseEntity<?> getTeamMembers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal User currentUser) {
        try {
            String organizationId = currentUser.getOrganizationId();
            
            Pageable pageable = PageRequest.of(page, size);
            Page<TeamMemberResponse> teamMembers = teamMemberService.getTeamMembers(organizationId, pageable);
            return ResponseEntity.ok(teamMembers);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                new ErrorResponse("Failed to get team members: " + e.getMessage())
            );
        }
    }
    

    
    /**
     * Search team members.
     * Uses authenticated user's organization information.
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchTeamMembers(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal User currentUser) {
        try {
            String organizationId = currentUser.getOrganizationId();
            
            Pageable pageable = PageRequest.of(page, size);
            Page<TeamMemberResponse> teamMembers = teamMemberService.searchTeamMembers(organizationId, q, pageable);
            return ResponseEntity.ok(teamMembers);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                new ErrorResponse("Failed to search team members: " + e.getMessage())
            );
        }
    }
    
    /**
     * Get active team members.
     * Uses authenticated user's organization information.
     */
    @GetMapping("/active")
    public ResponseEntity<?> getActiveTeamMembers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal User currentUser) {
        try {
            String organizationId = currentUser.getOrganizationId();
            
            Pageable pageable = PageRequest.of(page, size);
            Page<TeamMemberResponse> teamMembers = teamMemberService.getActiveTeamMembers(organizationId, pageable);
            return ResponseEntity.ok(teamMembers);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                new ErrorResponse("Failed to get active team members: " + e.getMessage())
            );
        }
    }
    
    /**
     * Get team members by role.
     * Uses authenticated user's organization information.
     */
    @GetMapping("/role/{role}")
    public ResponseEntity<?> getTeamMembersByRole(
            @PathVariable Role role,
            @AuthenticationPrincipal User currentUser) {
        try {
            String organizationId = currentUser.getOrganizationId();
            
            List<TeamMemberResponse> teamMembers = teamMemberService.getTeamMembersByRole(organizationId, role);
            return ResponseEntity.ok(teamMembers);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                new ErrorResponse("Failed to get team members by role: " + e.getMessage())
            );
        }
    }
    
    /**
     * Update team member role.
     * Uses authenticated user's organization information.
     */
    @PutMapping("/{teamMemberId}/role")
    public ResponseEntity<?> updateTeamMemberRole(
            @PathVariable String teamMemberId,
            @RequestParam Role newRole,
            @AuthenticationPrincipal User currentUser) {
        try {
            String organizationId = currentUser.getOrganizationId();
            
            TeamMemberResponse response = teamMemberService.updateTeamMemberRole(teamMemberId, newRole, organizationId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                new ErrorResponse("Failed to update team member role: " + e.getMessage())
            );
        }
    }
    
    /**
     * Deactivate team member.
     * Uses authenticated user's organization information.
     */
    @PutMapping("/{teamMemberId}/deactivate")
    public ResponseEntity<?> deactivateTeamMember(
            @PathVariable String teamMemberId,
            @AuthenticationPrincipal User currentUser) {
        try {
            String organizationId = currentUser.getOrganizationId();
            
            TeamMemberResponse response = teamMemberService.deactivateTeamMember(teamMemberId, organizationId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                new ErrorResponse("Failed to deactivate team member: " + e.getMessage())
            );
        }
    }
    
    /**
     * Reactivate team member.
     * Uses authenticated user's organization information.
     */
    @PutMapping("/{teamMemberId}/reactivate")
    public ResponseEntity<?> reactivateTeamMember(
            @PathVariable String teamMemberId,
            @AuthenticationPrincipal User currentUser) {
        try {
            String organizationId = currentUser.getOrganizationId();
            
            TeamMemberResponse response = teamMemberService.reactivateTeamMember(teamMemberId, organizationId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                new ErrorResponse("Failed to reactivate team member: " + e.getMessage())
            );
        }
    }
    
    /**
     * Remove team member.
     * Uses authenticated user's organization information.
     */
    @DeleteMapping("/{teamMemberId}")
    public ResponseEntity<?> removeTeamMember(
            @PathVariable String teamMemberId,
            @AuthenticationPrincipal User currentUser) {
        try {
            String organizationId = currentUser.getOrganizationId();
            
            teamMemberService.removeTeamMember(teamMemberId, organizationId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                new ErrorResponse("Failed to remove team member: " + e.getMessage())
            );
        }
    }
    

    
    /**
     * Check if user is member of organization.
     * Uses authenticated user's organization information.
     */
    @GetMapping("/check/organization/{userId}")
    public ResponseEntity<?> isUserMemberOfOrganization(
            @PathVariable String userId,
            @AuthenticationPrincipal User currentUser) {
        try {
            String organizationId = currentUser.getOrganizationId();
            
            boolean isMember = teamMemberService.isUserMemberOfOrganization(userId, organizationId);
            return ResponseEntity.ok(isMember);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                new ErrorResponse("Failed to check user membership: " + e.getMessage())
            );
        }
    }
    

    
    /**
     * Get team analytics.
     * Uses authenticated user's organization information.
     */
    @GetMapping("/analytics")
    public ResponseEntity<?> getTeamAnalytics(
            @AuthenticationPrincipal User currentUser) {
        try {
            String organizationId = currentUser.getOrganizationId();
            
            TeamMemberService.TeamAnalytics analytics = teamMemberService.getTeamAnalytics(organizationId);
            return ResponseEntity.ok(analytics);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                new ErrorResponse("Failed to get team analytics: " + e.getMessage())
            );
        }
    }
    
    /**
     * Get current user's team member information.
     * Uses authenticated user information.
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUserTeamInfo(
            @AuthenticationPrincipal User currentUser) {
        try {
            String userId = currentUser.getId();
            String organizationId = currentUser.getOrganizationId();
            
            // Find the current user's team member record
            Page<TeamMemberResponse> teamMembersPage = teamMemberService.getTeamMembers(organizationId, PageRequest.of(0, 100));
            TeamMemberResponse currentUserTeamInfo = teamMembersPage.getContent().stream()
                .filter(tm -> tm.getUserId().equals(userId))
                .findFirst()
                .orElse(null);
                
            if (currentUserTeamInfo == null) {
                return ResponseEntity.badRequest().body(
                    new ErrorResponse("Current user is not a team member of this organization")
                );
            }
            
            return ResponseEntity.ok(currentUserTeamInfo);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                new ErrorResponse("Failed to get current user team info: " + e.getMessage())
            );
        }
    }
    
    /**
     * Get team invitation by token.
     * This endpoint allows users to view invitation details before accepting.
     */
    @GetMapping("/invitations/{token}")
    public ResponseEntity<?> getInvitationByToken(@PathVariable String token) {
        try {
            TeamMemberResponse response = teamMemberService.getInvitationByToken(token);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                new ErrorResponse("Failed to get invitation: " + e.getMessage())
            );
        }
    }
    
    /**
     * Accept team invitation by token.
     * This endpoint allows users to accept invitations via email links.
     */
    @PostMapping("/invitations/{token}/accept")
    public ResponseEntity<?> acceptInvitationByToken(
            @PathVariable String token,
            @AuthenticationPrincipal User currentUser) {
        try {
            String userId = currentUser.getId();
            
            TeamMemberResponse response = teamMemberService.acceptInvitationByToken(token, userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                new ErrorResponse("Failed to accept invitation: " + e.getMessage())
            );
        }
    }
    
    /**
     * Error response DTO for better error handling.
     */
    public static class ErrorResponse {
        private String message;
        private long timestamp;
        
        public ErrorResponse(String message) {
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
}
