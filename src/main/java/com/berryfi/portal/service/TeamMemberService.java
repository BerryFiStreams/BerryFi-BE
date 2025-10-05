package com.berryfi.portal.service;

import com.berryfi.portal.dto.team.TeamMemberResponse;
import com.berryfi.portal.entity.TeamMember;
import com.berryfi.portal.entity.TeamMemberInvitation;
import com.berryfi.portal.entity.User;
import com.berryfi.portal.entity.Organization;
import com.berryfi.portal.enums.InvitationStatus;
import com.berryfi.portal.enums.Role;
import com.berryfi.portal.enums.UserStatus;

import com.berryfi.portal.repository.TeamMemberRepository;
import com.berryfi.portal.repository.TeamMemberInvitationRepository;
import com.berryfi.portal.repository.UserRepository;
import com.berryfi.portal.repository.OrganizationRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
        // Check if user is already a team member
        // Check by finding users in organization first
        List<User> usersWithEmail = userRepository.findByEmail(userEmail).map(List::of).orElse(List.of());
        List<TeamMember> existingMembers = usersWithEmail.stream()
            .flatMap(user -> teamMemberRepository.findByUserIdAndOrganizationId(user.getId(), organizationId).stream())
            .toList();
        if (!existingMembers.isEmpty()) {
            throw new RuntimeException("User is already a team member");
        }

        // Check if there's already a pending invitation
        Optional<TeamMemberInvitation> existingInvitation = teamMemberInvitationRepository
            .findByInviteEmailAndOrganizationIdAndStatus(userEmail, organizationId, InvitationStatus.PENDING);
        if (existingInvitation.isPresent()) {
            throw new RuntimeException("Invitation already exists for this user");
        }

        // Create the invitation
        TeamMemberInvitation invitation = new TeamMemberInvitation();
        invitation.setInviteToken(UUID.randomUUID().toString());
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

        // Save the invitation
        invitation = teamMemberInvitationRepository.save(invitation);

        // Send the invitation email
        try {
            sendTeamInvitationEmail(invitation);
        } catch (Exception e) {
            logger.error("Failed to send invitation email to {}: {}", userEmail, e.getMessage());
            // Don't fail the invitation creation if email fails
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
        try {
            // Get organization details
            Optional<Organization> orgOpt = organizationRepository.findById(invitation.getOrganizationId());
            String organizationName = orgOpt.map(Organization::getName).orElse("Organization");
            
            // Get inviter details
            Optional<User> inviterOpt = userRepository.findById(invitation.getInvitedByUserId());
            String inviterName = inviterOpt.map(User::getName).orElse("Team Admin");
            
            // Prepare template variables
            String subject = "Team Invitation from " + organizationName;
            String roleName = getRoleDisplayName(invitation.getRole());
            String roleDescription = getRoleDescription(invitation.getRole());
            
            // Generate email content using template service
            String htmlContent = emailTemplateService.generateTeamInvitationEmail(
                invitation.getInviteEmail(),
                organizationName,
                inviterName,
                roleName,
                roleDescription,
                invitation.getInviteToken(),
                invitation.getMessage(),
                invitation.getExpiresAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm"))
            );
            
            String textContent = String.format(
                "You have been invited to join %s as %s by %s. " +
                "Click the link in the email to accept the invitation. " +
                "This invitation expires on %s.",
                organizationName, roleName, inviterName,
                invitation.getExpiresAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm"))
            );
            
            // Send the email (placeholder implementation)
            sendTeamInvitationEmailActual(invitation.getInviteEmail(), subject, htmlContent, textContent);
            
        } catch (Exception e) {
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
        response.setStatus(UserStatus.INVITED);
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
     * Accept team invitation by token.
     * For existing users, adds them to the team.
     * For new users, creates account and adds to team.
     */
    @Transactional
    public TeamMemberResponse acceptInvitationByToken(String inviteToken, String userId) {
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

        // Get or create user
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify user email matches invitation
        if (!user.getEmail().equals(invitation.getInviteEmail())) {
            throw new RuntimeException("User email does not match invitation");
        }

        // Check if user is already a team member
        Optional<TeamMember> existingMemberOpt = teamMemberRepository.findByUserIdAndOrganizationId(
            userId, invitation.getOrganizationId());
        if (existingMemberOpt.isPresent()) {
            throw new RuntimeException("User is already a team member");
        }

        // Create team member
        TeamMember teamMember = new TeamMember();
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
        return acceptInvitationByToken(teamMemberId, userId);
    }

    /**
     * Send actual email (temporary method until we can refactor InvitationEmailService).
     */
    private void sendTeamInvitationEmailActual(String to, String subject, String htmlContent, String textContent) {
        // For now, we'll create a simple implementation
        // In a real implementation, you'd want to use the existing email infrastructure
        try {
            // This is a placeholder - you should integrate with your email service
            // For demonstration, we'll just log the email
            logger.info("Would send team invitation email to: {} with subject: {}", to, subject);
            // TODO: Implement actual email sending using JavaMailSender or similar
        } catch (Exception e) {
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
}