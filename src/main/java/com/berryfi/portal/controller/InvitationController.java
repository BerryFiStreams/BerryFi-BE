package com.berryfi.portal.controller;

import com.berryfi.portal.dto.invitation.InvitationDetailsResponse;
import com.berryfi.portal.dto.invitation.InvitationRegistrationRequest;
import com.berryfi.portal.dto.invitation.SentInvitationResponse;
import com.berryfi.portal.dto.auth.AuthResponse;
import com.berryfi.portal.enums.InvitationStatus;
import com.berryfi.portal.service.InvitationService;
import com.berryfi.portal.service.JwtService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for handling project invitations and invitation-based registration.
 */
@RestController
@RequestMapping("/api/invitations")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Invitations", description = "Project invitation and registration operations")
public class InvitationController {

    private static final Logger logger = LoggerFactory.getLogger(InvitationController.class);

    @Autowired
    private InvitationService invitationService;

    @Autowired
    private JwtService jwtService;

    /**
     * Extract user ID from JWT token.
     */
    private String extractUserIdFromToken(String token) {
        return jwtService.getUserIdFromToken(token);
    }

    /**
     * Get all invitations sent by the authenticated user.
     * GET /api/invitations/sent
     */
    @GetMapping("/sent")
    public ResponseEntity<Page<SentInvitationResponse>> getSentInvitations(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) InvitationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        logger.info("Getting sent invitations with status: {}, page: {}, size: {}", status, page, size);
        
        // Extract user ID from JWT token
        String accessToken = authHeader.replace("Bearer ", "");
        String userId = extractUserIdFromToken(accessToken);
        
        Page<SentInvitationResponse> sentInvitations = invitationService.getSentInvitations(userId, status, page, size);
        
        return ResponseEntity.ok(sentInvitations);
    }

    /**
     * Get pending invitations sent by the authenticated user (for easy resending).
     * GET /api/invitations/sent/pending
     */
    @GetMapping("/sent/pending")
    public ResponseEntity<List<SentInvitationResponse>> getPendingInvitations(
            @RequestHeader("Authorization") String authHeader) {
        
        logger.info("Getting pending invitations for user");
        
        // Extract user ID from JWT token
        String accessToken = authHeader.replace("Bearer ", "");
        String userId = extractUserIdFromToken(accessToken);
        
        List<SentInvitationResponse> pendingInvitations = invitationService.getPendingInvitations(userId);
        
        return ResponseEntity.ok(pendingInvitations);
    }

    /**
     * Get invitation details by token.
     * GET /api/invitations/{token}
     */
    @GetMapping("/{token}")
    public ResponseEntity<InvitationDetailsResponse> getInvitationDetails(@PathVariable String token) {
        logger.info("Getting invitation details for token: {}", token);

        InvitationDetailsResponse invitation = invitationService.getInvitationDetails(token);

        return ResponseEntity.ok(invitation);
    }

    /**
     * Register user and organization through invitation.
     * POST /api/invitations/register
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerThroughInvitation(
            @Valid @RequestBody InvitationRegistrationRequest request) {

        logger.info("Processing invitation registration for token: {}", request.getInviteToken());

        AuthResponse authResponse = invitationService.registerThroughInvitation(request);

        return ResponseEntity.ok(authResponse);
    }

    /**
     * Accept invitation for existing user.
     * POST /api/invitations/{token}/accept
     */
    @PostMapping("/{token}/accept")
    public ResponseEntity<Void> acceptInvitation(
            @PathVariable String token,
            @RequestHeader("Authorization") String authHeader) {
        
        logger.info("Accepting invitation for token: {}", token);
        
        // Extract user ID from JWT token (assuming JWT authentication)
        String accessToken = authHeader.replace("Bearer ", "");
        String userId = extractUserIdFromToken(accessToken);
        
        invitationService.acceptInvitation(token, userId);
        
        return ResponseEntity.ok().build();
    }

    /**
     * Decline invitation.
     * POST /api/invitations/{token}/decline
     */
    @PostMapping("/{token}/decline")
    public ResponseEntity<Void> declineInvitation(@PathVariable String token) {
        logger.info("Declining invitation with token: {}", token);

        invitationService.declineInvitation(token);

        return ResponseEntity.ok().build();
    }

    /**
     * Resend invitation email.
     * POST /api/invitations/{token}/resend
     */
    @PostMapping("/{token}/resend")
    public ResponseEntity<Void> resendInvitation(@PathVariable String token) {
        logger.info("Resending invitation with token: {}", token);

        invitationService.resendInvitation(token);

        return ResponseEntity.ok().build();
    }
}