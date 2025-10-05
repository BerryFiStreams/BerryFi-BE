package com.berryfi.portal.service;

import com.berryfi.portal.dto.invitation.InvitationDetailsResponse;
import com.berryfi.portal.dto.invitation.InvitationRegistrationRequest;
import com.berryfi.portal.dto.invitation.SentInvitationResponse;
import com.berryfi.portal.dto.auth.AuthResponse;
import com.berryfi.portal.dto.user.UserDto;
import com.berryfi.portal.entity.*;
import com.berryfi.portal.enums.*;
import com.berryfi.portal.exception.ResourceNotFoundException;
import com.berryfi.portal.repository.*;
import com.berryfi.portal.service.JwtService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for handling project invitations and invitation-based registration.
 */
@Service
@Transactional
public class InvitationService {

    private static final Logger logger = LoggerFactory.getLogger(InvitationService.class);

    @Autowired
    private ProjectInvitationRepository projectInvitationRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private ProjectShareRepository projectShareRepository;

    @Autowired
    private InvitationEmailService invitationEmailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private JwtService jwtService;

    /**
     * Get all invitations sent by a user.
     */
    public Page<SentInvitationResponse> getSentInvitations(String userId, InvitationStatus status, int page, int size) {
        logger.info("Getting sent invitations for user: {}, status: {}, page: {}, size: {}", userId, status, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ProjectInvitation> invitations;
        
        if (status != null) {
            invitations = projectInvitationRepository.findByInvitedByUserIdAndStatusOrderByCreatedAtDesc(userId, status, pageable);
        } else {
            invitations = projectInvitationRepository.findByInvitedByUserIdOrderByCreatedAtDesc(userId, pageable);
        }
        
        return invitations.map(this::convertToSentInvitationResponse);
    }

    /**
     * Get pending invitations sent by a user (for easy resending).
     */
    public List<SentInvitationResponse> getPendingInvitations(String userId) {
        logger.info("Getting pending invitations for user: {}", userId);
        
        Pageable pageable = PageRequest.of(0, 100); // Get up to 100 pending invitations
        Page<ProjectInvitation> pendingInvitations = projectInvitationRepository
                .findByInvitedByUserIdAndStatusOrderByCreatedAtDesc(userId, InvitationStatus.PENDING, pageable);
        
        return pendingInvitations.getContent().stream()
                .map(this::convertToSentInvitationResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert ProjectInvitation to SentInvitationResponse.
     */
    private SentInvitationResponse convertToSentInvitationResponse(ProjectInvitation invitation) {
        // Get project details
        Project project = projectRepository.findById(invitation.getProjectId())
                .orElse(null);
        
        String projectName = project != null ? project.getName() : "Unknown Project";
        
        // Determine last activity time
        LocalDateTime lastActivityAt = invitation.getUpdatedAt();
        if (invitation.getAcceptedAt() != null) {
            lastActivityAt = invitation.getAcceptedAt();
        } else if (invitation.getDeclinedAt() != null) {
            lastActivityAt = invitation.getDeclinedAt();
        }
        
        return new SentInvitationResponse(
                invitation.getInviteToken(),
                projectName,
                invitation.getProjectId(),
                invitation.getInviteEmail(),
                invitation.getStatus(),
                invitation.getInitialCredits(),
                invitation.getMonthlyRecurringCredits(),
                invitation.getShareMessage(),
                invitation.getCreatedAt(),
                invitation.getExpiresAt(),
                lastActivityAt
        );
    }

    /**
     * Get invitation details by token.
     */
    public InvitationDetailsResponse getInvitationDetails(String token) {
        logger.info("Getting invitation details for token: {}", token);

        ProjectInvitation invitation = projectInvitationRepository.findByInviteToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found or invalid"));

        if (invitation.isExpired()) {
            invitation.expireInvitation();
            projectInvitationRepository.save(invitation);
            throw new IllegalArgumentException("This invitation has expired");
        }

        if (!invitation.isPending()) {
            throw new IllegalArgumentException("This invitation is no longer valid");
        }

        // Get related entities
        Project project = projectRepository.findById(invitation.getProjectId()).orElse(null);
        User invitedByUser = userRepository.findById(invitation.getInvitedByUserId()).orElse(null);
        Organization invitedByOrg = organizationRepository.findById(invitation.getInvitedByOrganizationId()).orElse(null);

        if (project == null || invitedByUser == null || invitedByOrg == null) {
            throw new ResourceNotFoundException("Related invitation data not found");
        }

        // Build response
        InvitationDetailsResponse response = new InvitationDetailsResponse(
                invitation.getInviteToken(),
                invitation.getInviteEmail(),
                project.getName(),
                invitedByUser.getName(),
                invitedByOrg.getName()
        );

        response.setProjectId(project.getId());
        response.setInitialCredits(invitation.getInitialCredits());
        response.setMonthlyRecurringCredits(invitation.getMonthlyRecurringCredits());
        response.setShareMessage(invitation.getShareMessage());
        response.setExpiresAt(invitation.getExpiresAt());
        response.setIsExpired(invitation.isExpired());
        response.setStatus(invitation.getStatus().toString());

        return response;
    }

    /**
     * Register user and organization through invitation.
     */
    public AuthResponse registerThroughInvitation(InvitationRegistrationRequest request) {
        logger.info("Processing invitation registration for token: {}", request.getInviteToken());

        // Get and validate invitation
        ProjectInvitation invitation = projectInvitationRepository.findByInviteToken(request.getInviteToken())
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found or invalid"));

        if (invitation.isExpired()) {
            invitation.expireInvitation();
            projectInvitationRepository.save(invitation);
            throw new IllegalArgumentException("This invitation has expired");
        }

        if (!invitation.isPending()) {
            throw new IllegalArgumentException("This invitation is no longer valid");
        }

        // Validate email matches invitation
        if (!invitation.getInviteEmail().equalsIgnoreCase(request.getEmail())) {
            throw new IllegalArgumentException("Email address does not match the invitation");
        }

        // Check if email is already registered
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("An account with this email address already exists");
        }

        // Create organization
        Organization organization = new Organization(
                request.getOrganizationName(),
                request.getOrganizationDescription(),
                null, // Will be set after user creation
                request.getEmail(),
                request.getFullName(),
                null // Will be set after user creation
        );
        organization = organizationRepository.save(organization);

        // Create user
        User user = new User(
                request.getFullName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                Role.ORG_OWNER, // First user in organization is owner
                AccountType.ORGANIZATION // Default account type
        );
        user.setOrganizationId(organization.getId());
        user = userRepository.save(user);

        // Update organization with user details
        organization.setOwnerId(user.getId());
        organization.setCreatedBy(user.getId());
        organizationRepository.save(organization);

        // Mark invitation as accepted
        invitation.acceptInvitation(user.getId(), organization.getId());
        projectInvitationRepository.save(invitation);

        // Create project share
        createProjectShareFromInvitation(invitation);

        // Generate JWT token
        String token = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Update user with refresh token
        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        logger.info("Successfully registered user {} and organization {} through invitation", 
                   user.getEmail(), organization.getName());

        // Convert user to UserDto
        UserDto userDto = UserDto.fromUser(user);
        
        return new AuthResponse(userDto, token, refreshToken);
    }

    /**
     * Create project share from accepted invitation by existing user.
     */
    private void createProjectShareFromAcceptedInvitation(ProjectInvitation invitation, String userId, String organizationId) {
        logger.info("Creating project share from accepted invitation: {} for user: {}", invitation.getId(), userId);

        // Create new share relationship
        ProjectShare projectShare = new ProjectShare(
                invitation.getProjectId(),
                invitation.getInvitedByOrganizationId(), // The organization that sent the invitation
                organizationId, // User's organization (now the inviting organization)
                ShareType.DIRECT,
                invitation.getInvitedByUserId()
        );

        // Set credit allocation and permissions from invitation
        projectShare.setAllocatedCredits(invitation.getInitialCredits());
        projectShare.setRemainingCredits(invitation.getInitialCredits());
        projectShare.setRecurringCredits(invitation.getMonthlyRecurringCredits());
        projectShare.setRecurringIntervalDays(30); // Monthly = 30 days
        projectShare.setCanViewAnalytics(invitation.getCanViewAnalytics());
        projectShare.setCanManageSessions(invitation.getCanManageSessions());
        projectShare.setCanShareFurther(invitation.getCanShareFurther());
        projectShare.setIsPermanent(invitation.getIsPermanent());
        projectShare.setShareMessage(invitation.getShareMessage());

        // Auto-accept the share since user accepted the invitation
        projectShare.acceptShare(userId);
        projectShare.setAcceptedAt(LocalDateTime.now());

        projectShareRepository.save(projectShare);

        logger.info("Successfully created project share for user: {} with allocated credits: {}", 
                   userId, invitation.getInitialCredits());
    }

    /**
     * Create project share from accepted invitation.
     */
    private void createProjectShareFromInvitation(ProjectInvitation invitation) {
        logger.info("Creating project share from accepted invitation: {}", invitation.getId());

        // Create new share relationship
        ProjectShare projectShare = new ProjectShare(
                invitation.getProjectId(),
                invitation.getInvitedByOrganizationId(), // The organization that sent the invitation
                invitation.getRegisteredOrganizationId(), // The newly created organization
                ShareType.DIRECT,
                invitation.getInvitedByUserId()
        );

        // Set credit allocation and permissions from invitation
        projectShare.setAllocatedCredits(invitation.getInitialCredits());
        projectShare.setRemainingCredits(invitation.getInitialCredits());
        projectShare.setRecurringCredits(invitation.getMonthlyRecurringCredits());
        projectShare.setRecurringIntervalDays(30); // Monthly = 30 days
        projectShare.setCanViewAnalytics(invitation.getCanViewAnalytics());
        projectShare.setCanManageSessions(invitation.getCanManageSessions());
        projectShare.setCanShareFurther(invitation.getCanShareFurther());
        projectShare.setIsPermanent(invitation.getIsPermanent());
        projectShare.setShareMessage(invitation.getShareMessage());

        // Auto-accept the share since user registered through invitation
        projectShare.acceptShare(invitation.getRegisteredUserId());

        projectShareRepository.save(projectShare);

        logger.info("Successfully created project share for invitation: {}", invitation.getId());
    }

    /**
     * Accept invitation for existing user.
     * Creates organization membership and shares project.
     */
    public void acceptInvitation(String token, String userId) {
        logger.info("Accepting invitation for token: {} by user: {}", token, userId);

        // Get and validate invitation
        ProjectInvitation invitation = projectInvitationRepository.findByInviteToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found or invalid"));

        if (invitation.isExpired()) {
            invitation.expireInvitation();
            projectInvitationRepository.save(invitation);
            throw new IllegalArgumentException("This invitation has expired");
        }

        if (!invitation.getStatus().equals(InvitationStatus.PENDING)) {
            throw new IllegalArgumentException("This invitation has already been processed");
        }

        // Get user and validate
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.getEmail().equals(invitation.getInviteEmail())) {
            throw new IllegalArgumentException("This invitation is not for your email address");
        }

        // Get inviting organization
        Organization invitingOrganization = organizationRepository.findById(invitation.getInvitedByOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Inviting organization not found"));

        // Create team membership with ORG_OWNER role in the inviting organization
        TeamMember teamMember = new TeamMember(
                "tm_" + java.util.UUID.randomUUID().toString(),
                user.getId(),
                user.getName(),
                user.getEmail(),
                Role.ORG_OWNER, // Make user ORG_OWNER in inviting organization
                invitation.getInvitedByOrganizationId(),
                UserStatus.ACTIVE,
                invitation.getInvitedByUserId()
        );
        teamMember.setJoinedAt(LocalDateTime.now());
        teamMemberRepository.save(teamMember);

        // Update user's organization to the inviting organization
        user.setOrganizationId(invitation.getInvitedByOrganizationId());
        user.setRole(Role.ORG_OWNER);
        userRepository.save(user);

        // Create project share
        createProjectShareFromAcceptedInvitation(invitation, user.getId(), user.getOrganizationId());

        // Mark invitation as accepted
        invitation.acceptInvitation(user.getId(), user.getOrganizationId());
        projectInvitationRepository.save(invitation);

        logger.info("Successfully accepted invitation for user: {} in organization: {}", 
                   user.getEmail(), invitingOrganization.getName());
    }

    /**
     * Decline invitation.
     */
    public void declineInvitation(String token) {
        logger.info("Declining invitation with token: {}", token);

        ProjectInvitation invitation = projectInvitationRepository.findByInviteToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found or invalid"));

        if (!invitation.isPending()) {
            throw new IllegalArgumentException("This invitation cannot be declined");
        }

        invitation.declineInvitation();
        projectInvitationRepository.save(invitation);

        logger.info("Successfully declined invitation: {}", invitation.getId());
    }

    /**
     * Resend invitation email.
     */
    public void resendInvitation(String token) {
        logger.info("Resending invitation with token: {}", token);

        ProjectInvitation invitation = projectInvitationRepository.findByInviteToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found or invalid"));

        if (!invitation.isPending()) {
            throw new IllegalArgumentException("This invitation cannot be resent");
        }

        // Update resend information
        invitation.resendInvitation();
        projectInvitationRepository.save(invitation);

        // Get related entities and resend email
        Project project = projectRepository.findById(invitation.getProjectId()).orElse(null);
        User invitedByUser = userRepository.findById(invitation.getInvitedByUserId()).orElse(null);
        Organization invitedByOrg = organizationRepository.findById(invitation.getInvitedByOrganizationId()).orElse(null);

        if (project != null && invitedByUser != null && invitedByOrg != null) {
            invitationEmailService.resendInvitationEmail(invitation, project, invitedByUser, invitedByOrg);
        }

        logger.info("Successfully resent invitation: {}", invitation.getId());
    }

    /**
     * Clean up expired invitations (can be called by scheduled task).
     */
    public void cleanupExpiredInvitations() {
        logger.info("Cleaning up expired invitations");

        projectInvitationRepository.findExpiredInvitations(LocalDateTime.now())
                .forEach(invitation -> {
                    invitation.expireInvitation();
                    projectInvitationRepository.save(invitation);
                });

        logger.info("Completed cleaning up expired invitations");
    }
}