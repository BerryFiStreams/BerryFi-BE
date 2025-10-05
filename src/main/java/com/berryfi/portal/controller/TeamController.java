package com.berryfi.portal.controller;

import com.berryfi.portal.dto.team.TeamStatsResponse;
import com.berryfi.portal.dto.team.TeamMemberResponse;
import com.berryfi.portal.dto.team.TeamInvitationRequest;
import com.berryfi.portal.dto.team.TeamInvitationResponse;
import com.berryfi.portal.entity.User;
import com.berryfi.portal.enums.InvitationStatus;
import com.berryfi.portal.service.TeamService;
import com.berryfi.portal.service.TeamMemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for team-level operations.
 */
@RestController
@RequestMapping("/api/team")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TeamController {

    private static final Logger logger = LoggerFactory.getLogger(TeamController.class);

    @Autowired
    private TeamService teamService;
    
    @Autowired
    private TeamMemberService teamMemberService;

    /**
     * Get team statistics.
     * GET /api/team/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<TeamStatsResponse> getTeamStats(

            @RequestHeader("X-Organization-ID") String organizationId) {
        logger.info("Getting team stats for organization: {}", organizationId);

        try {
            TeamStatsResponse stats = teamService.getTeamStats(organizationId);
            return ResponseEntity.ok(stats);
        } catch (RuntimeException e) {
            logger.error("Error getting team stats: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Invite a team member.
     * POST /api/team/invite
     */
    @PostMapping("/invite")
    public ResponseEntity<?> inviteTeamMember(
            @RequestBody TeamInvitationRequest request,
            @AuthenticationPrincipal User currentUser) {
        logger.info("Inviting team member: {} for organization: {}", request.getUserEmail(), currentUser.getOrganizationId());

        try {
            String organizationId = currentUser.getOrganizationId();
            String invitedBy = currentUser.getId();

            TeamMemberResponse response = teamMemberService.inviteTeamMember(
                request.getUserEmail(), organizationId, request.getRole(), invitedBy, request.getMessage());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            logger.error("Error inviting team member: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                new ErrorResponse("Failed to invite team member: " + e.getMessage())
            );
        }
    }

    /**
     * Get team invitations with optional status filter.
     * GET /api/team/invitations
     * Supports both InvitationStatus values (PENDING, ACCEPTED, etc.) and UserStatus aliases (INVITED = PENDING)
     */
    @GetMapping("/invitations")
    public ResponseEntity<?> getTeamInvitations(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal User currentUser) {
        logger.info("Getting team invitations for organization: {}, status: {}", currentUser.getOrganizationId(), status);

        try {
            String organizationId = currentUser.getOrganizationId();
            Pageable pageable = PageRequest.of(page, size);

            // Map status string to InvitationStatus, supporting aliases
            InvitationStatus invitationStatus = mapStatusParameter(status);
            
            Page<TeamInvitationResponse> invitations = teamMemberService.getTeamInvitations(organizationId, invitationStatus, pageable);
            return ResponseEntity.ok(invitations);
        } catch (RuntimeException e) {
            logger.error("Error getting team invitations: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                new ErrorResponse("Failed to get team invitations: " + e.getMessage())
            );
        }
    }

    /**
     * Map status parameter string to InvitationStatus enum.
     * Supports both InvitationStatus values and UserStatus aliases for backward compatibility.
     */
    private InvitationStatus mapStatusParameter(String status) {
        if (status == null || status.trim().isEmpty()) {
            return null;
        }
        
        String upperStatus = status.trim().toUpperCase();
        
        // Handle UserStatus aliases for backward compatibility
        switch (upperStatus) {
            case "INVITED":
                return InvitationStatus.PENDING;
            case "ACTIVE":
                return InvitationStatus.ACCEPTED;
            case "DISABLED":
                // Could map to DECLINED, EXPIRED, or CANCELLED - using DECLINED as default
                return InvitationStatus.DECLINED;
            default:
                // Try to parse as InvitationStatus
                try {
                    return InvitationStatus.valueOf(upperStatus);
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("Invalid status parameter: " + status + 
                        ". Valid values are: PENDING, ACCEPTED, DECLINED, EXPIRED, CANCELLED, or INVITED (alias for PENDING)");
                }
        }
    }

    /**
     * Resend a pending team invitation.
     * POST /api/team/invitations/{invitationId}/resend
     */
    @PostMapping("/invitations/{invitationId}/resend")
    public ResponseEntity<?> resendTeamInvitation(
            @PathVariable String invitationId,
            @AuthenticationPrincipal User currentUser) {
        logger.info("Resending team invitation: {} by user: {}", invitationId, currentUser.getId());

        try {
            String organizationId = currentUser.getOrganizationId();
            String userId = currentUser.getId();

            TeamInvitationResponse response = teamMemberService.resendTeamInvitation(invitationId, organizationId, userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("Error resending team invitation: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                new ErrorResponse("Failed to resend team invitation: " + e.getMessage())
            );
        }
    }

    /**
     * Cancel a pending team invitation.
     * POST /api/team/invitations/{invitationId}/cancel
     */
    @PostMapping("/invitations/{invitationId}/cancel")
    public ResponseEntity<?> cancelTeamInvitation(
            @PathVariable String invitationId,
            @AuthenticationPrincipal User currentUser) {
        logger.info("Cancelling team invitation: {} by user: {}", invitationId, currentUser.getId());

        try {
            String organizationId = currentUser.getOrganizationId();
            String userId = currentUser.getId();

            TeamInvitationResponse response = teamMemberService.cancelTeamInvitation(invitationId, organizationId, userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("Error cancelling team invitation: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                new ErrorResponse("Failed to cancel team invitation: " + e.getMessage())
            );
        }
    }

    /**
     * Get team invitation by token (public endpoint for email links).
     * GET /api/team/invitations/token/{token}
     * This endpoint allows unauthenticated access so users can view invitation details from email links.
     */
    @GetMapping("/invitations/token/{token}")
    public ResponseEntity<?> getInvitationByToken(@PathVariable String token) {
        logger.info("Getting invitation details by token: {}", token);

        try {
            TeamMemberResponse response = teamMemberService.getInvitationByToken(token);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("Error getting invitation by token: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                new ErrorResponse("Failed to get invitation: " + e.getMessage())
            );
        }
    }

    /**
     * Check if a user exists by email (helper endpoint for invitation flow).
     * GET /api/team/invitations/check-user?email={email}
     * This public endpoint helps determine whether to use registration or accept flow.
     */
    @GetMapping("/invitations/check-user")
    public ResponseEntity<?> checkUserExists(@RequestParam String email) {
        logger.info("Checking if user exists: {}", email);

        try {
            boolean userExists = teamMemberService.checkUserExistsByEmail(email);
            return ResponseEntity.ok(new UserExistsResponse(userExists, email));
        } catch (Exception e) {
            logger.error("Error checking user existence: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                new ErrorResponse("Failed to check user existence: " + e.getMessage())
            );
        }
    }

    /**
     * Register user through team invitation (public endpoint).
     * POST /api/team/invitations/register
     * This endpoint handles new user registration through team invitation.
     */
    @PostMapping("/invitations/register")
    public ResponseEntity<?> registerThroughTeamInvitation(
            @RequestBody TeamInvitationRegistrationRequest request) {
        
        logger.info("Processing team invitation registration for token: {}", request.getInviteToken());
        
        try {
            TeamMemberResponse response = teamMemberService.registerThroughTeamInvitation(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("Error registering through team invitation: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                new ErrorResponse("Failed to register through team invitation: " + e.getMessage())
            );
        }
    }

        /**
     * Accept a team invitation using the invitation token.
     * POST /api/team/invitations/token/{token}/accept
     * This endpoint is for existing users who are already logged in.
     * For new users who need to register, use POST /api/team/invitations/register instead.
     */
    @PostMapping("/invitations/token/{token}/accept")
    public ResponseEntity<?> acceptInvitationByToken(
            @PathVariable String token,
            @AuthenticationPrincipal User currentUser) {
        
        // Check if user is authenticated
        if (currentUser == null) {
            logger.warn("Unauthenticated user attempted to accept team invitation with token: {}", token);
            return ResponseEntity.status(401).body(
                new ErrorResponse("Authentication required. If you don't have an account, please use the registration endpoint: POST /api/team/invitations/register")
            );
        }
        
        logger.info("Accepting team invitation for token: {} by user: {}", token, currentUser.getId());
        
        try {
            TeamMemberResponse response = teamMemberService.acceptInvitationByTokenForUser(token, currentUser.getId());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("Error accepting team invitation by token: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                new ErrorResponse("Failed to accept team invitation: " + e.getMessage())
            );
        }
    }

    /**
     * Request DTO for team invitation registration.
     */
    public static class TeamInvitationRegistrationRequest {
        private String inviteToken;
        private String fullName;
        private String password;
        private String phoneNumber;
        private String jobTitle;
        
        public TeamInvitationRegistrationRequest() {}
        
        public String getInviteToken() { return inviteToken; }
        public void setInviteToken(String inviteToken) { this.inviteToken = inviteToken; }
        
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        
        public String getJobTitle() { return jobTitle; }
        public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    }

    /**
     * User existence check response DTO.
     */
    public static class UserExistsResponse {
        private boolean exists;
        private String email;

        public UserExistsResponse(boolean exists, String email) {
            this.exists = exists;
            this.email = email;
        }

        public boolean isExists() { return exists; }
        public void setExists(boolean exists) { this.exists = exists; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
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
