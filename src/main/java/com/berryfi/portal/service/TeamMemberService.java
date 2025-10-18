package com.berryfi.portal.service;

import com.berryfi.portal.dto.team.TeamMemberResponse;
import com.berryfi.portal.entity.TeamMember;
import com.berryfi.portal.entity.TeamMemberInvitation;
import com.berryfi.portal.entity.User;
import com.berryfi.portal.entity.Organization;
import com.berryfi.portal.enums.AccountType;
import com.berryfi.portal.enums.InvitationStatus;
import com.berryfi.portal.enums.Role;
import com.berryfi.portal.enums.UserStatus;

import com.berryfi.portal.repository.TeamMemberRepository;
import com.berryfi.portal.repository.TeamMemberInvitationRepository;
import com.berryfi.portal.repository.UserRepository;
import com.berryfi.portal.repository.OrganizationRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class TeamMemberService {

    private static final Logger logger = LoggerFactory.getLogger(TeamMemberService.class);

    @Autowired
    private TeamMemberRepository teamMemberRepository;
    
    @Autowired
    private TeamMemberInvitationRepository teamMemberInvitationRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private OrganizationRepository organizationRepository;
    
    @Autowired
    private EmailTemplateService emailTemplateService;
    
    @Autowired
    private InvitationEmailService invitationEmailService;
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private EntityManager entityManager;
    
    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    /**
     * Invite a team member to join the organization.
     * This creates an invitation with a unique token and sends an email.
     */
    @Transactional
    public TeamMemberResponse inviteTeamMember(String userEmail, String organizationId, 
                                              Role role, String invitedBy) {
        return inviteTeamMember(userEmail, organizationId, role, invitedBy, null);
    }

    /**
     * Invite a team member to join the organization with custom message.
     * This creates an invitation with a unique token and sends an email.
     */
    @Transactional
    public TeamMemberResponse inviteTeamMember(String userEmail, String organizationId, 
                                              Role role, String invitedBy, String message) {
        logger.info("=== Starting team invitation process ===");
        logger.info("Inviting user: {} to organization: {} with role: {} by: {}", userEmail, organizationId, role, invitedBy);
        logger.info("Message: {}", message);
        
        // Check if user already exists and is a team member in any organization
        logger.debug("Checking if user {} already exists in the system", userEmail);
        Optional<User> existingUserOpt = userRepository.findByEmail(userEmail);
        
        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();
            logger.debug("Found existing user: {} with ID: {}", userEmail, existingUser.getId());
            
            // Check if user is already a team member in ANY organization
            List<TeamMember> userTeamMemberships = teamMemberRepository.findActiveOrganizationsForUser(existingUser.getId());
            if (!userTeamMemberships.isEmpty()) {
                TeamMember existingMembership = userTeamMemberships.get(0); // Get first membership
                logger.warn("User {} is already a team member in organization: {}. Cannot invite to multiple organizations.", 
                           userEmail, existingMembership.getOrganizationId());
                throw new RuntimeException("User is already a team member in another organization");
            }
            
            // If user exists but has no team memberships, check if they're inviting to the user's own organization
            if (existingUser.getOrganizationId() != null && !existingUser.getOrganizationId().equals(organizationId)) {
                logger.warn("User {} belongs to organization {} but invitation is for organization {}. Cannot cross-invite.", 
                           userEmail, existingUser.getOrganizationId(), organizationId);
                throw new RuntimeException("User already belongs to a different organization");
            }
        }
        logger.debug("User {} is not an existing team member, proceeding with invitation", userEmail);

                // Check if there's already a pending invitation
        logger.debug("Checking for existing pending invitations for {} in organization {}", userEmail, organizationId);
        
        // First, check if there's any pending invitation for this email and organization
        Optional<TeamMemberInvitation> existingInvitationOpt = teamMemberInvitationRepository
            .findByInviteEmailAndOrganizationIdAndStatus(userEmail, organizationId, InvitationStatus.PENDING);
            
        if (existingInvitationOpt.isPresent()) {
            TeamMemberInvitation existingInvitation = existingInvitationOpt.get();
            
            // Check if the existing invitation is expired
            if (existingInvitation.isExpired()) {
                logger.info("Found expired invitation for {} in organization {}. Marking it as expired.", 
                           userEmail, organizationId);
                existingInvitation.expireInvitation();
                teamMemberInvitationRepository.save(existingInvitation);
                logger.debug("Expired invitation has been marked as EXPIRED, proceeding with new invitation");
            } else {
                // Invitation exists and is still valid
                logger.warn("User {} already has a valid pending invitation in organization {}", userEmail, organizationId);
                throw new RuntimeException("User already has a pending invitation");
            }
        }
        logger.debug("No existing pending invitations found for {}", userEmail);

        // Create the invitation
        logger.info("Creating team invitation for {} in organization {}", userEmail, organizationId);
        TeamMemberInvitation invitation = new TeamMemberInvitation();
        String inviteToken = UUID.randomUUID().toString();
        invitation.setInviteToken(inviteToken);
        invitation.setInviteEmail(userEmail);
        invitation.setOrganizationId(organizationId);
        invitation.setRole(role);
        invitation.setInvitedByUserId(invitedBy);
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setCreatedAt(LocalDateTime.now());
        invitation.setExpiresAt(LocalDateTime.now().plusDays(7)); // 7 days expiration
        if (message != null && !message.trim().isEmpty()) {
            invitation.setMessage(message.trim());
        }

        logger.debug("Generated invitation token: {}", inviteToken);
        logger.debug("Invitation expires at: {}", invitation.getExpiresAt());

        // Save the invitation
        logger.debug("Saving invitation to database");
        invitation = teamMemberInvitationRepository.save(invitation);
        logger.info("Invitation saved with ID: {}", invitation.getId());

                // Send the invitation email
        logger.info("=== Starting email sending process ===");
        try {
            logger.debug("Calling sendTeamInvitationEmail for {}", invitation.getInviteEmail());
            sendTeamInvitationEmail(invitation);
            logger.info("Email sending completed successfully for {}", invitation.getInviteEmail());
        } catch (Exception e) {
            logger.error("Failed to send invitation email to {}: {}", invitation.getInviteEmail(), e.getMessage(), e);
            logger.error("Exception type: {}", e.getClass().getSimpleName());
            if (e.getCause() != null) {
                logger.error("Root cause: {} - {}", e.getCause().getClass().getSimpleName(), e.getCause().getMessage());
            }
            throw new RuntimeException("Failed to send team invitation email", e);
        }

        // Return response
        TeamMemberResponse response = new TeamMemberResponse();
        response.setId(invitation.getId());
        response.setUserEmail(invitation.getInviteEmail());
        response.setRole(invitation.getRole());
        response.setStatus(UserStatus.INVITED);
        response.setInvitedAt(invitation.getCreatedAt());
        response.setInvitedBy(invitation.getInvitedByUserId());
        
        return response;
    }

    /**
     * Send team invitation email.
     */
    private void sendTeamInvitationEmail(TeamMemberInvitation invitation) {
        logger.debug("=== sendTeamInvitationEmail called ===");
        logger.debug("Invitation details: email={}, org={}, role={}, token={}", 
                    invitation.getInviteEmail(), invitation.getOrganizationId(), 
                    invitation.getRole(), invitation.getInviteToken());
        
        try {
            // Get organization details
            logger.debug("Fetching organization details for ID: {}", invitation.getOrganizationId());
            Optional<Organization> orgOpt = organizationRepository.findById(invitation.getOrganizationId());
            String organizationName = orgOpt.map(Organization::getName).orElse("Organization");
            logger.debug("Organization name: {}", organizationName);
            
            // Get inviter details
            logger.debug("Fetching inviter details for ID: {}", invitation.getInvitedByUserId());
            Optional<User> inviterOpt = userRepository.findById(invitation.getInvitedByUserId());
            String inviterName = inviterOpt.map(User::getName).orElse("Team Admin");
            logger.debug("Inviter name: {}", inviterName);
            
            // Prepare template variables
            String subject = "Team Invitation from " + organizationName;
            String roleName = getRoleDisplayName(invitation.getRole());
            String roleDescription = getRoleDescription(invitation.getRole());
            
            // Get inviter email for template
            String inviterEmail = inviterOpt.map(User::getEmail).orElse("admin@berryfi.in");
            logger.debug("Inviter email: {}", inviterEmail);
            
            // Build invitation link
            String invitationLink = buildTeamInvitationUrl(invitation.getInviteToken());
            logger.debug("Invitation link: {}", invitationLink);
            
            // Generate email content using template service
            logger.debug("Generating email template with parameters:");
            logger.debug("  - inviterName: {}", inviterName);
            logger.debug("  - inviterEmail: {}", inviterEmail);
            logger.debug("  - organizationName: {}", organizationName);
            logger.debug("  - roleName: {}", roleName);
            logger.debug("  - roleDescription: {}", roleDescription);
            logger.debug("  - invitationLink: {}", invitationLink);
            logger.debug("  - expiresAt: {}", invitation.getExpiresAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm")));
            logger.debug("  - message: {}", invitation.getMessage());
            
            String htmlContent = emailTemplateService.generateTeamInvitationEmail(
                inviterName,
                inviterEmail,
                organizationName,
                roleName,
                roleDescription,
                invitationLink,
                invitation.getExpiresAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm")),
                invitation.getMessage()
            );
            logger.debug("HTML email content generated successfully (length: {})", htmlContent.length());
            
            String textContent = String.format(
                "You have been invited to join %s as %s by %s. " +
                "Click the link in the email to accept the invitation. " +
                "This invitation expires on %s.",
                organizationName, roleName, inviterName,
                invitation.getExpiresAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm"))
            );
            
            // Send the email using InvitationEmailService
            logger.info("=== Calling actual email sending ===");
            logger.debug("Email details:");
            logger.debug("  - to: {}", invitation.getInviteEmail());
            logger.debug("  - subject: {}", subject);
            logger.debug("  - htmlContent length: {}", htmlContent.length());
            logger.debug("  - textContent length: {}", textContent.length());
            
            sendTeamInvitationEmailActual(invitation.getInviteEmail(), subject, htmlContent, textContent);
            logger.info("Email sent successfully via InvitationEmailService");
            
        } catch (Exception e) {
            logger.error("Failed to send invitation email to {}: {}", invitation.getInviteEmail(), e.getMessage(), e);
            throw new RuntimeException("Failed to send team invitation email", e);
        }
    }

    /**
     * Get role display name for UI.
     */
    private String getRoleDisplayName(Role role) {
        return switch (role) {
            case ORG_OWNER -> "Organization Owner";
            case ORG_ADMIN -> "Organization Administrator";
            case ORG_MEMBER -> "Organization Member";
            case ORG_AUDITOR -> "Organization Auditor";
            case ORG_REPORTER -> "Organization Reporter";
            case ORG_BILLING -> "Billing Manager";
            case PROJECT_ADMIN -> "Project Administrator";
            case PROJECT_COLLABORATOR -> "Project Collaborator";
            default -> role.name();
        };
    }

    /**
     * Get role description for invitation email.
     */
    private String getRoleDescription(Role role) {
        return switch (role) {
            case ORG_OWNER -> "Full organization control and management";
            case ORG_ADMIN -> "Organization administration and user management";
            case ORG_MEMBER -> "Standard organization member access";
            case ORG_AUDITOR -> "Read-only access for auditing purposes";
            case ORG_REPORTER -> "Access to reports and analytics";
            case ORG_BILLING -> "Billing and subscription management";
            case PROJECT_ADMIN -> "Project administration and management";
            case PROJECT_COLLABORATOR -> "Project collaboration and contribution";
            default -> "Organization member";
        };
    }

    /**
     * Get team invitation by token.
     * This allows users to view invitation details before accepting.
     */
    public TeamMemberResponse getInvitationByToken(String inviteToken) {
        // Find invitation by token first, then check status
        TeamMemberInvitation invitation = teamMemberInvitationRepository.findByInviteToken(inviteToken)
            .orElseThrow(() -> new RuntimeException("Invalid invitation token"));
        
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new RuntimeException("Invitation is not pending");
        }
            
        // Check if invitation is expired
        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Invitation has expired");
        }
        
        // Return invitation details as a response
        TeamMemberResponse response = new TeamMemberResponse();
        response.setId(invitation.getId());
        response.setUserEmail(invitation.getInviteEmail());
        response.setRole(invitation.getRole());
        // Map InvitationStatus to UserStatus for consistency
        response.setStatus(mapInvitationStatusToUserStatus(invitation.getStatus()));
        response.setInvitedAt(invitation.getCreatedAt());
        response.setInvitedBy(invitation.getInvitedByUserId());
        
        // Set organization details if needed
        Optional<Organization> orgOpt = organizationRepository.findById(invitation.getOrganizationId());
        if (orgOpt.isPresent()) {
            response.setOrganizationName(orgOpt.get().getName());
        }
        
        return response;
    }

    /**
     * Map InvitationStatus to UserStatus for consistent status representation.
     */
    private UserStatus mapInvitationStatusToUserStatus(InvitationStatus invitationStatus) {
        switch (invitationStatus) {
            case PENDING:
                return UserStatus.INVITED;
            case ACCEPTED:
                return UserStatus.ACTIVE;
            case DECLINED:
            case EXPIRED:
            case CANCELLED:
                return UserStatus.DISABLED;
            default:
                return UserStatus.INVITED;
        }
    }

    /**
     * Accept team invitation by token without authentication.
     * This method finds the user by invitation email and accepts the invitation.
     * For existing users only - does not create new users.
     */
    @Transactional
    public TeamMemberResponse acceptInvitationByToken(String inviteToken) {
        logger.info("Accepting team invitation by token without authentication: {}", inviteToken);
        
        try {
            // Find invitation by token first, then check status
            TeamMemberInvitation invitation = teamMemberInvitationRepository.findByInviteToken(inviteToken)
                .orElseThrow(() -> new RuntimeException("Invalid invitation token"));
            
            logger.info("Found invitation for email: {} in organization: {}, status: {}", 
                       invitation.getInviteEmail(), invitation.getOrganizationId(), invitation.getStatus());
            
            if (invitation.getStatus() != InvitationStatus.PENDING) {
                throw new RuntimeException("Invitation is not pending - current status: " + invitation.getStatus());
            }
                
            // Check if invitation is expired
            if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Invitation has expired");
            }

            // Find user by invitation email
            Optional<User> userOpt = userRepository.findByEmail(invitation.getInviteEmail());
            if (!userOpt.isPresent()) {
                logger.warn("No user found with email: {}", invitation.getInviteEmail());
                throw new RuntimeException(
                    String.format("No user found with email '%s'. Please register first using the registration endpoint.", 
                                 invitation.getInviteEmail())
                );
            }
            
            User user = userOpt.get();
            logger.info("Found user: ID={}, Email={}", user.getId(), user.getEmail());

            // Check if user is already a team member
            Optional<TeamMember> existingMemberOpt = teamMemberRepository.findByUserIdAndOrganizationId(
                user.getId(), invitation.getOrganizationId());
            if (existingMemberOpt.isPresent()) {
                logger.warn("User {} is already a team member in organization {}", user.getId(), invitation.getOrganizationId());
                throw new RuntimeException("User is already a team member");
            }

            // Create team member
            TeamMember teamMember = new TeamMember();
            teamMember.setId("tm_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12)); // Generate unique ID
            teamMember.setUserId(user.getId());
            teamMember.setOrganizationId(invitation.getOrganizationId());
            teamMember.setRole(invitation.getRole());
            teamMember.setStatus(UserStatus.ACTIVE);
            teamMember.setJoinedAt(LocalDateTime.now());
            teamMember.setInvitedBy(invitation.getInvitedByUserId());
            
            logger.info("About to save team member: userId={}, orgId={}, role={}", 
                       teamMember.getUserId(), teamMember.getOrganizationId(), teamMember.getRole());
            
            try {
                teamMember = teamMemberRepository.save(teamMember);
                logger.info("Successfully saved team member: {}", teamMember.getId());
            } catch (Exception e) {
                logger.error("Failed to save team member: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to save team member: " + e.getMessage(), e);
            }

            // Update invitation status
            logger.info("About to update invitation status to ACCEPTED");
            try {
                invitation.acceptInvitation(user.getId());
                teamMemberInvitationRepository.save(invitation);
                logger.info("Successfully updated invitation status");
            } catch (Exception e) {
                logger.error("Failed to update invitation status: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to update invitation status: " + e.getMessage(), e);
            }
            
            logger.info("Successfully accepted invitation for user: {}", user.getId());
            return mapToResponse(teamMember);
            
        } catch (Exception e) {
            logger.error("Error in acceptInvitationByToken: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Accept team invitation by token for existing user.
     * For existing users, adds them to the team.
     */
    @Transactional
    public TeamMemberResponse acceptInvitationByTokenForUser(String inviteToken, String userId) {
        logger.info("Accepting team invitation by token: {} for user: {}", inviteToken, userId);
        
        // Find invitation by token first, then check status
        TeamMemberInvitation invitation = teamMemberInvitationRepository.findByInviteToken(inviteToken)
            .orElseThrow(() -> new RuntimeException("Invalid invitation token"));
        
        logger.info("Found invitation for email: {} in organization: {}", 
                   invitation.getInviteEmail(), invitation.getOrganizationId());
        
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new RuntimeException("Invitation is not pending");
        }
            
        // Check if invitation is expired
        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Invitation has expired");
        }

        // Get or create user
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        logger.info("User details: ID={}, Email={}", user.getId(), user.getEmail());

        // Verify user email matches invitation (case-insensitive)
        String userEmail = user.getEmail().toLowerCase().trim();
        String inviteEmail = invitation.getInviteEmail().toLowerCase().trim();
        
        if (!userEmail.equals(inviteEmail)) {
            logger.error("Email mismatch: User email '{}' does not match invitation email '{}'", 
                        user.getEmail(), invitation.getInviteEmail());
            throw new RuntimeException(
                String.format("User email does not match invitation. Invitation was sent to '%s' but you are logged in as '%s'", 
                             invitation.getInviteEmail(), user.getEmail())
            );
        }

        // Check if user is already a team member
        Optional<TeamMember> existingMemberOpt = teamMemberRepository.findByUserIdAndOrganizationId(
            userId, invitation.getOrganizationId());
        if (existingMemberOpt.isPresent()) {
            throw new RuntimeException("User is already a team member");
        }

        // Create team member
        TeamMember teamMember = new TeamMember();
        teamMember.setId("tm_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12)); // Generate unique ID
        teamMember.setUserId(userId);
        teamMember.setOrganizationId(invitation.getOrganizationId());
        teamMember.setRole(invitation.getRole());
        teamMember.setStatus(UserStatus.ACTIVE);
        teamMember.setJoinedAt(LocalDateTime.now());
        teamMember.setInvitedBy(invitation.getInvitedByUserId());
        
        teamMember = teamMemberRepository.save(teamMember);

        // Update invitation status
        invitation.acceptInvitation(user.getId());
        teamMemberInvitationRepository.save(invitation);
        
        return mapToResponse(teamMember);
    }

    /**
     * Accept team invitation by token and create new user if needed.
     * This method handles the complete invitation acceptance flow for new users.
     */
    @Transactional
    public TeamMemberResponse acceptInvitationByTokenAndCreateUser(
            String inviteToken, String password, String firstName, String lastName) {
        logger.info("Accepting invitation by token and creating user: token={}", inviteToken);
        
        // Find invitation by token first, then check status
        TeamMemberInvitation invitation = teamMemberInvitationRepository.findByInviteToken(inviteToken)
            .orElseThrow(() -> new RuntimeException("Invalid invitation token"));
        
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new RuntimeException("Invitation is not pending - current status: " + invitation.getStatus());
        }
            
        // Check if invitation is expired
        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Invitation has expired");
        }

        String inviteEmail = invitation.getInviteEmail();
        logger.info("Processing invitation for email: {}", inviteEmail);

        // Check if user already exists
        Optional<User> existingUserOpt = userRepository.findByEmail(inviteEmail);
        User user;
        
        if (existingUserOpt.isPresent()) {
            // User exists - use existing user
            user = existingUserOpt.get();
            logger.info("Found existing user: {}", user.getId());
        } else {
            // Create new user
            logger.info("Creating new user for email: {}", inviteEmail);
            user = createUserFromInvitation(invitation, password, firstName, lastName);
            logger.info("Created new user: {}", user.getId());
        }

        // Check if user is already a team member
        Optional<TeamMember> existingMemberOpt = teamMemberRepository.findByUserIdAndOrganizationId(
            user.getId(), invitation.getOrganizationId());
        if (existingMemberOpt.isPresent()) {
            throw new RuntimeException("User is already a team member");
        }

        // Create team member
        TeamMember teamMember = new TeamMember();
        teamMember.setId("tm_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12)); // Generate unique ID
        teamMember.setUserId(user.getId());
        teamMember.setOrganizationId(invitation.getOrganizationId());
        teamMember.setRole(invitation.getRole());
        teamMember.setStatus(UserStatus.ACTIVE);
        teamMember.setJoinedAt(LocalDateTime.now());
        teamMember.setInvitedBy(invitation.getInvitedByUserId());
        
        teamMember = teamMemberRepository.save(teamMember);

        // Update invitation status
        invitation.acceptInvitation(user.getId());
        teamMemberInvitationRepository.save(invitation);
        
        logger.info("Successfully accepted invitation and created team member: {}", teamMember.getId());
        return mapToResponse(teamMember);
    }

    /**
     * Create a new user from invitation details.
     */
    private User createUserFromInvitation(TeamMemberInvitation invitation, String password, String firstName, String lastName) {
        User user = new User();
        // ID is automatically generated by the User constructor
        user.setEmail(invitation.getInviteEmail());
        user.setOrganizationId(invitation.getOrganizationId());
        user.setStatus(UserStatus.ACTIVE);
        user.setRole(invitation.getRole());
        user.setAccountType(AccountType.ORGANIZATION); // Set appropriate account type
        
        // Set name - combine first and last name or derive from email
        String fullName;
        if (firstName != null && !firstName.trim().isEmpty()) {
            fullName = firstName.trim();
            if (lastName != null && !lastName.trim().isEmpty()) {
                fullName += " " + lastName.trim();
            }
        } else {
            // Derive name from email
            String emailPrefix = invitation.getInviteEmail().split("@")[0];
            // Capitalize first letter and replace dots/underscores with spaces
            fullName = emailPrefix.substring(0, 1).toUpperCase() + 
                      emailPrefix.substring(1).replace(".", " ").replace("_", " ");
        }
        
        user.setName(fullName);
        
        // Set password - use provided or generate temporary password
        if (password != null && !password.trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(password)); // Hash password with BCrypt
        } else {
            // Generate temporary password - user should be prompted to change it
            user.setPassword(passwordEncoder.encode("TempPass123!")); // Hash temporary password
        }
        
        // createdAt and updatedAt are automatically set by the User constructor
        
        return userRepository.save(user);
    }

    /**
     * Accept team invitation (legacy method for backward compatibility).
     */
    public TeamMemberResponse acceptInvitation(String teamMemberId, String userId) {
        // Try to find as team member first (old system)
        Optional<TeamMember> teamMemberOpt = teamMemberRepository.findById(teamMemberId);
        if (teamMemberOpt.isPresent()) {
            TeamMember teamMember = teamMemberOpt.get();
            
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
        
        // Try to find as invitation (new system) - assume teamMemberId is actually a token
        return acceptInvitationByTokenForUser(teamMemberId, userId);
    }

    /**
     * Build team invitation URL for frontend.
     */
    private String buildTeamInvitationUrl(String inviteToken) {
        return frontendUrl + "/team-invitation/" + inviteToken;
    }

    /**
     * Send actual team invitation email using InvitationEmailService.
     */
    private void sendTeamInvitationEmailActual(String to, String subject, String htmlContent, String textContent) {
        logger.debug("=== sendTeamInvitationEmailActual called ===");
        logger.debug("Calling InvitationEmailService.sendTeamInvitationEmail");
        logger.debug("Parameters: to={}, subject={}", to, subject);
        
        try {
            invitationEmailService.sendTeamInvitationEmail(to, subject, htmlContent, textContent);
            logger.info("Successfully sent team invitation email to: {}", to);
        } catch (Exception e) {
            logger.error("Failed to send team invitation email to: {}", to, e);
            logger.error("Exception details: type={}, message={}", e.getClass().getSimpleName(), e.getMessage());
            if (e.getCause() != null) {
                logger.error("Root cause: type={}, message={}", e.getCause().getClass().getSimpleName(), e.getCause().getMessage());
            }
            throw new RuntimeException("Failed to send team invitation email: " + e.getMessage(), e);
        }
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
     * Search team members.
     */
    public Page<TeamMemberResponse> searchTeamMembers(String organizationId, String query, Pageable pageable) {
        Page<TeamMember> teamMembers = teamMemberRepository.searchTeamMembers(
            organizationId, query, pageable);
        return teamMembers.map(this::mapToResponse);
    }

    /**
     * Get active team members.
     */
    public Page<TeamMemberResponse> getActiveTeamMembers(String organizationId, Pageable pageable) {
        Page<TeamMember> teamMembers = teamMemberRepository.findByOrganizationIdAndStatusOrderByJoinedAtDesc(
            organizationId, UserStatus.ACTIVE, pageable);
        return teamMembers.map(this::mapToResponse);
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
     * Get team member count by organization.
     */
    public long getTeamMemberCount(String organizationId) {
        return teamMemberRepository.countByOrganizationId(organizationId);
    }

    /**
     * Get team members by role.
     */
    public List<TeamMemberResponse> getTeamMembersByRole(String organizationId, Role role) {
        List<TeamMember> teamMembers = teamMemberRepository.findByOrganizationIdAndRole(organizationId, role);
        return teamMembers.stream().map(this::mapToResponse).toList();
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
        
        teamMember.setStatus(UserStatus.ACTIVE);
        TeamMember updatedTeamMember = teamMemberRepository.save(teamMember);
        return mapToResponse(updatedTeamMember);
    }

    /**
     * Check if user is member of organization.
     */
    public boolean isUserMemberOfOrganization(String userId, String organizationId) {
        return teamMemberRepository.existsByUserIdAndOrganizationId(userId, organizationId);
    }

    /**
     * Get team analytics.
     */
    public TeamAnalytics getTeamAnalytics(String organizationId) {
        TeamAnalytics analytics = new TeamAnalytics();
        
        analytics.setTotalMembers(teamMemberRepository.countByOrganizationId(organizationId).intValue());
        analytics.setActiveMembers(teamMemberRepository.countByOrganizationIdAndStatus(organizationId, UserStatus.ACTIVE).intValue());
        analytics.setPendingInvitations(teamMemberInvitationRepository.countByOrganizationIdAndStatus(organizationId, InvitationStatus.PENDING).intValue());
        
        return analytics;
    }

    /**
     * Simple TeamAnalytics class for basic analytics.
     */
    public static class TeamAnalytics {
        private int totalMembers;
        private int activeMembers;
        private int pendingInvitations;
        
        public int getTotalMembers() { return totalMembers; }
        public void setTotalMembers(int totalMembers) { this.totalMembers = totalMembers; }
        
        public int getActiveMembers() { return activeMembers; }
        public void setActiveMembers(int activeMembers) { this.activeMembers = activeMembers; }
        
        public int getPendingInvitations() { return pendingInvitations; }
        public void setPendingInvitations(int pendingInvitations) { this.pendingInvitations = pendingInvitations; }
    }

    /**
     * Map TeamMember entity to response DTO.
     */
    private TeamMemberResponse mapToResponse(TeamMember teamMember) {
        TeamMemberResponse response = new TeamMemberResponse();
        response.setId(teamMember.getId());
        response.setUserId(teamMember.getUserId());
        response.setOrganizationId(teamMember.getOrganizationId());
        response.setRole(teamMember.getRole());
        response.setStatus(teamMember.getStatus());
        response.setJoinedAt(teamMember.getJoinedAt());
        response.setInvitedBy(teamMember.getInvitedBy());
        
        // Load user details if available
        Optional<User> userOpt = userRepository.findById(teamMember.getUserId());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            response.setUserEmail(user.getEmail());
            response.setUserName(user.getName());
        }
        
        // Load organization details if available
        Optional<Organization> orgOpt = organizationRepository.findById(teamMember.getOrganizationId());
        if (orgOpt.isPresent()) {
            response.setOrganizationName(orgOpt.get().getName());
        }
        
        return response;
    }

    /**
     * Get team invitations for an organization with pagination and optional status filter.
     * Optimized to avoid N+1 query problem by batch loading organizations and users.
     */
    public Page<com.berryfi.portal.dto.team.TeamInvitationResponse> getTeamInvitations(
            String organizationId, InvitationStatus status, Pageable pageable) {
        logger.info("Getting team invitations for organization: {}, status: {}", organizationId, status);
        
        Page<TeamMemberInvitation> invitations;
        if (status != null) {
            invitations = teamMemberInvitationRepository.findByOrganizationIdAndStatusOrderByCreatedAtDesc(organizationId, status, pageable);
        } else {
            invitations = teamMemberInvitationRepository.findByOrganizationIdOrderByCreatedAtDesc(organizationId, pageable);
        }
        
        // Extract all unique organization IDs and user IDs
        List<String> orgIds = invitations.getContent().stream()
            .map(TeamMemberInvitation::getOrganizationId)
            .distinct()
            .toList();
        
        List<String> userIds = invitations.getContent().stream()
            .map(TeamMemberInvitation::getInvitedByUserId)
            .distinct()
            .toList();
        
        logger.debug("Optimizing queries: {} invitations, {} unique orgs, {} unique users", 
                    invitations.getContent().size(), orgIds.size(), userIds.size());
        
        // Batch load organizations and users to avoid N+1 queries
        List<Organization> organizations = organizationRepository.findAllById(orgIds);
        List<User> users = userRepository.findAllById(userIds);
        
        // Create lookup maps for O(1) access
        Map<String, Organization> orgMap = organizations.stream()
            .collect(Collectors.toMap(Organization::getId, org -> org));
        Map<String, User> userMap = users.stream()
            .collect(Collectors.toMap(User::getId, user -> user));
        
        return invitations.map(invitation -> mapInvitationToResponseOptimized(invitation, orgMap, userMap));
    }

    /**
     * Map TeamMemberInvitation entity to TeamInvitationResponse DTO.
     * DEPRECATED: Use mapInvitationToResponseOptimized for better performance.
     */
    private com.berryfi.portal.dto.team.TeamInvitationResponse mapInvitationToResponse(TeamMemberInvitation invitation) {
        com.berryfi.portal.dto.team.TeamInvitationResponse response = new com.berryfi.portal.dto.team.TeamInvitationResponse();
        
        response.setId(invitation.getId());
        response.setUserEmail(invitation.getInviteEmail());
        response.setOrganizationId(invitation.getOrganizationId());
        response.setRole(invitation.getRole());
        response.setRoleName(getRoleDisplayName(invitation.getRole()));
        response.setStatus(invitation.getStatus());
        response.setInviteToken(invitation.getInviteToken());
        response.setInvitedByUserId(invitation.getInvitedByUserId());
        response.setMessage(invitation.getMessage());
        response.setCreatedAt(invitation.getCreatedAt());
        response.setExpiresAt(invitation.getExpiresAt());
        response.setAcceptedAt(invitation.getAcceptedAt());
        
        // Build invitation link
        response.setInvitationLink(buildTeamInvitationUrl(invitation.getInviteToken()));
        
        // Load organization details
        Optional<Organization> orgOpt = organizationRepository.findById(invitation.getOrganizationId());
        if (orgOpt.isPresent()) {
            response.setOrganizationName(orgOpt.get().getName());
        }
        
        // Load inviter details
        Optional<User> inviterOpt = userRepository.findById(invitation.getInvitedByUserId());
        if (inviterOpt.isPresent()) {
            User inviter = inviterOpt.get();
            response.setInviterName(inviter.getName());
            response.setInviterEmail(inviter.getEmail());
        }
        
        return response;
    }

    /**
     * Optimized mapping method that uses pre-loaded data to avoid N+1 queries.
     */
    private com.berryfi.portal.dto.team.TeamInvitationResponse mapInvitationToResponseOptimized(
            TeamMemberInvitation invitation, 
            Map<String, Organization> orgMap, 
            Map<String, User> userMap) {
        
        com.berryfi.portal.dto.team.TeamInvitationResponse response = new com.berryfi.portal.dto.team.TeamInvitationResponse();
        
        response.setId(invitation.getId());
        response.setUserEmail(invitation.getInviteEmail());
        response.setOrganizationId(invitation.getOrganizationId());
        response.setRole(invitation.getRole());
        response.setRoleName(getRoleDisplayName(invitation.getRole()));
        response.setStatus(invitation.getStatus());
        response.setInviteToken(invitation.getInviteToken());
        response.setInvitedByUserId(invitation.getInvitedByUserId());
        response.setMessage(invitation.getMessage());
        response.setCreatedAt(invitation.getCreatedAt());
        response.setExpiresAt(invitation.getExpiresAt());
        response.setAcceptedAt(invitation.getAcceptedAt());
        
        // Build invitation link
        response.setInvitationLink(buildTeamInvitationUrl(invitation.getInviteToken()));
        
        // Load organization details from pre-loaded map (O(1) lookup)
        Organization org = orgMap.get(invitation.getOrganizationId());
        if (org != null) {
            response.setOrganizationName(org.getName());
        }
        
        // Load inviter details from pre-loaded map (O(1) lookup)
        User inviter = userMap.get(invitation.getInvitedByUserId());
        if (inviter != null) {
            response.setInviterName(inviter.getName());
            response.setInviterEmail(inviter.getEmail());
        }
        
        return response;
    }

    /**
     * Resend a pending team invitation.
     */
    @Transactional
    public com.berryfi.portal.dto.team.TeamInvitationResponse resendTeamInvitation(
            String invitationId, String organizationId, String requestedByUserId) {
        logger.info("Resending team invitation: {} for organization: {} by user: {}", 
                   invitationId, organizationId, requestedByUserId);

        // Find the invitation
        Optional<TeamMemberInvitation> invitationOpt = teamMemberInvitationRepository.findById(invitationId);
        if (invitationOpt.isEmpty()) {
            throw new RuntimeException("Team invitation not found");
        }

        TeamMemberInvitation invitation = invitationOpt.get();

        // Validate organization
        if (!invitation.getOrganizationId().equals(organizationId)) {
            throw new RuntimeException("Invitation does not belong to your organization");
        }

        // Validate status - only pending invitations can be resent
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new RuntimeException("Only pending invitations can be resent. Current status: " + invitation.getStatus());
        }

        // Check if invitation is expired
        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            // Update status to expired
            invitation.setStatus(InvitationStatus.EXPIRED);
            teamMemberInvitationRepository.save(invitation);
            throw new RuntimeException("Invitation has expired and cannot be resent");
        }

        try {
            // Extend expiration by 7 more days
            invitation.setExpiresAt(LocalDateTime.now().plusDays(7));
            invitation = teamMemberInvitationRepository.save(invitation);
            logger.info("Extended invitation expiration to: {}", invitation.getExpiresAt());

            // Resend the email
            logger.info("Resending invitation email to: {}", invitation.getInviteEmail());
            sendTeamInvitationEmail(invitation);
            
            logger.info("Successfully resent team invitation: {}", invitationId);
            return mapInvitationToResponse(invitation);

        } catch (Exception e) {
            logger.error("Failed to resend team invitation {}: {}", invitationId, e.getMessage(), e);
            throw new RuntimeException("Failed to resend team invitation: " + e.getMessage(), e);
        }
    }

    /**
     * Cancel a pending team invitation.
     */
    @Transactional
    public com.berryfi.portal.dto.team.TeamInvitationResponse cancelTeamInvitation(
            String invitationId, String organizationId, String requestedByUserId) {
        logger.info("Cancelling team invitation: {} for organization: {} by user: {}", 
                   invitationId, organizationId, requestedByUserId);

        // Find the invitation
        Optional<TeamMemberInvitation> invitationOpt = teamMemberInvitationRepository.findById(invitationId);
        if (invitationOpt.isEmpty()) {
            throw new RuntimeException("Team invitation not found");
        }

        TeamMemberInvitation invitation = invitationOpt.get();

        // Validate organization
        if (!invitation.getOrganizationId().equals(organizationId)) {
            throw new RuntimeException("Invitation does not belong to your organization");
        }

        // Validate status - only pending invitations can be cancelled
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new RuntimeException("Only pending invitations can be cancelled. Current status: " + invitation.getStatus());
        }

        // Update status to cancelled
        invitation.setStatus(InvitationStatus.CANCELLED);
        invitation = teamMemberInvitationRepository.save(invitation);

        logger.info("Successfully cancelled team invitation: {} by user: {}", invitationId, requestedByUserId);
        return mapInvitationToResponse(invitation);
    }

    /**
     * Register user through team invitation (follows InvitationController pattern).
     * Creates a new user account and adds them to the team.
     */
    @Transactional(rollbackFor = Exception.class)
    public TeamMemberResponse registerThroughTeamInvitation(com.berryfi.portal.controller.TeamController.TeamInvitationRegistrationRequest request) {
        logger.info("Processing team invitation registration for token: {}", request.getInviteToken());

        // Get and validate invitation
        logger.debug("Searching for invitation with token: {}", request.getInviteToken());
        Optional<TeamMemberInvitation> invitationOpt = teamMemberInvitationRepository.findByInviteToken(request.getInviteToken());
        
        if (invitationOpt.isEmpty()) {
            logger.error("No invitation found for token: {}", request.getInviteToken());
            throw new RuntimeException("Team invitation not found or invalid");
        }
        
        TeamMemberInvitation invitation = invitationOpt.get();
        logger.info("Found invitation: id={}, email={}, status={}, organizationId={}", 
            invitation.getId(), invitation.getInviteEmail(), invitation.getStatus(), invitation.getOrganizationId());

        // Validate invitation status and expiration
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new RuntimeException("This team invitation is no longer valid. Status: " + invitation.getStatus());
        }

        if (invitation.getExpiresAt() != null && LocalDateTime.now().isAfter(invitation.getExpiresAt())) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            teamMemberInvitationRepository.save(invitation);
            throw new RuntimeException("This team invitation has expired");
        }

        // Check if user with this email already exists
        if (userRepository.existsByEmail(invitation.getInviteEmail())) {
            throw new RuntimeException("An account with this email address already exists. Please log in to accept the invitation.");
        }

        // Get organization details
        Organization organization = organizationRepository.findById(invitation.getOrganizationId())
            .orElseThrow(() -> new RuntimeException("Organization not found"));

        // Create new user account
        logger.info("Step 1: Creating new user account for email: {}", invitation.getInviteEmail());
        logger.debug("Request fullName: '{}', password length: {}", request.getFullName(), 
            request.getPassword() != null ? request.getPassword().length() : "null");
        User newUser;
        try {
            String fullName = request.getFullName();
            if (fullName == null || fullName.trim().isEmpty()) {
                logger.warn("FullName is null or empty, using email prefix as name");
                fullName = invitation.getInviteEmail().split("@")[0];
            }
            newUser = new User(
                fullName,
                invitation.getInviteEmail(),
                passwordEncoder.encode(request.getPassword()),
                invitation.getRole(),
                AccountType.ORGANIZATION
            );
            newUser.setOrganizationId(organization.getId());
            logger.debug("User object created, attempting to save: email={}, orgId={}", newUser.getEmail(), newUser.getOrganizationId());
            newUser = userRepository.save(newUser);
            entityManager.flush(); // Force immediate database write
            logger.info("Step 1 SUCCESS: Created new user account with ID: {} for email: {}", newUser.getId(), newUser.getEmail());
        } catch (Exception e) {
            logger.error("Step 1 FAILED: Error creating user account: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create user account: " + e.getMessage());
        }

        // Create team member record
        logger.info("Step 2: Creating team member for user: {} in organization: {}", newUser.getId(), organization.getName());
        TeamMember teamMember;
        try {
            teamMember = new TeamMember();
            String teamMemberId = "tm_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
            logger.debug("Generated team member ID: {}", teamMemberId);
            teamMember.setId(teamMemberId);
            teamMember.setUserId(newUser.getId());
            teamMember.setUserName(newUser.getName());
            teamMember.setEmail(newUser.getEmail());
            teamMember.setRole(invitation.getRole());
            teamMember.setOrganizationId(organization.getId());
            teamMember.setStatus(UserStatus.ACTIVE);
            teamMember.setInvitedBy(invitation.getInvitedByUserId());
            teamMember.setInvitedAt(invitation.getCreatedAt());
            teamMember.setJoinedAt(LocalDateTime.now());
            
            logger.debug("Team member object created, attempting to save: id={}, userId={}, email={}", 
                teamMember.getId(), teamMember.getUserId(), teamMember.getEmail());
            teamMember = teamMemberRepository.save(teamMember);
            entityManager.flush(); // Force immediate database write
            logger.info("Step 2 SUCCESS: Created team member with ID: {}", teamMember.getId());
        } catch (Exception e) {
            logger.error("Step 2 FAILED: Error creating team member: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create team member: " + e.getMessage());
        }

        // Mark invitation as accepted
        logger.info("Step 3: Updating invitation status to ACCEPTED");
        try {
            logger.debug("Before update: invitation status={}, registeredUserId={}", invitation.getStatus(), invitation.getRegisteredUserId());
            invitation.setStatus(InvitationStatus.ACCEPTED);
            invitation.setAcceptedAt(LocalDateTime.now());
            invitation.setRegisteredUserId(newUser.getId());
            logger.debug("After update: invitation status={}, registeredUserId={}, acceptedAt={}", 
                invitation.getStatus(), invitation.getRegisteredUserId(), invitation.getAcceptedAt());
            teamMemberInvitationRepository.save(invitation);
            entityManager.flush(); // Force immediate database write
            logger.info("Step 3 SUCCESS: Updated invitation status");
        } catch (Exception e) {
            logger.error("Step 3 FAILED: Error updating invitation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update invitation status: " + e.getMessage());
        }

        logger.info("Successfully registered user {} through team invitation and added to organization {}", 
                   newUser.getEmail(), organization.getName());

        // Send welcome email
        try {
            invitationEmailService.sendWelcomeEmail(newUser.getEmail(), newUser.getName(), organization.getName());
        } catch (Exception e) {
            // Log error but don't fail registration if welcome email fails
            // This will be handled gracefully in the sendWelcomeEmail method
        }

        logger.info("Step 4: Mapping team member to response");
        try {
            TeamMemberResponse response = mapToResponse(teamMember);
            logger.info("Step 4 SUCCESS: Response created");
            return response;
        } catch (Exception e) {
            logger.error("Step 4 FAILED: Error mapping response: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to map response: " + e.getMessage());
        }
    }

    /**
     * Check if a user exists by email address.
     * This is a helper method for invitation flow to determine whether to use registration or accept flow.
     */
    public boolean checkUserExistsByEmail(String email) {
        logger.info("Checking if user exists by email: {}", email);
        return userRepository.findByEmail(email).isPresent();
    }

    /**
     * Expire all invitations that are past their expiration time.
     * This method can be called periodically to clean up expired invitations.
     */
    @Transactional
    public int expireOldInvitations() {
        logger.info("Starting to expire old invitations");
        List<TeamMemberInvitation> expiredInvitations = teamMemberInvitationRepository
            .findExpiredInvitations(LocalDateTime.now());
            
        int expiredCount = 0;
        for (TeamMemberInvitation invitation : expiredInvitations) {
            // Double-check that we only expire PENDING invitations
            if (invitation.getStatus() == InvitationStatus.PENDING && invitation.shouldBeExpired()) {
                invitation.expireInvitation();
                teamMemberInvitationRepository.save(invitation);
                expiredCount++;
                logger.debug("Expired invitation: {} for email: {}", invitation.getId(), invitation.getInviteEmail());
            } else {
                logger.warn("Skipped expiring invitation {} with status {} - only PENDING invitations should be expired", 
                           invitation.getId(), invitation.getStatus());
            }
        }
        
        logger.info("Expired {} old invitations", expiredCount);
        return expiredCount;
    }
}