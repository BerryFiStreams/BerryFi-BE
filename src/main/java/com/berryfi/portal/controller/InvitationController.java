package com.berryfi.portal.controller;

import com.berryfi.portal.dto.invitation.InvitationDetailsResponse;
import com.berryfi.portal.dto.invitation.InvitationRegistrationRequest;
import com.berryfi.portal.dto.auth.AuthResponse;
import com.berryfi.portal.service.InvitationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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