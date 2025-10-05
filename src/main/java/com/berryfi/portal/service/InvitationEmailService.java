package com.berryfi.portal.service;

import com.berryfi.portal.entity.ProjectInvitation;
import com.berryfi.portal.entity.Project;
import com.berryfi.portal.entity.User;
import com.berryfi.portal.entity.Organization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Service for sending invitation emails using Spring Boot's JavaMailSender.
 * Supports HTML and text email formats with SMTP configuration.
 */
@Service
public class InvitationEmailService {

    private static final Logger logger = LoggerFactory.getLogger(InvitationEmailService.class);

    private final JavaMailSender mailSender;
    private final EmailTemplateService emailTemplateService;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${app.company.name:BerryFi Studio}")
    private String companyName;

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.email.from-name}")
    private String fromName;

    @Value("${app.email.enabled:true}")
    private boolean emailEnabled;
    
    @Value("${app.email.debug:false}")
    private boolean emailDebugMode;

    @Autowired
    public InvitationEmailService(JavaMailSender mailSender, EmailTemplateService emailTemplateService) {
        this.mailSender = mailSender;
        this.emailTemplateService = emailTemplateService;
    }

    /**
     * Send project sharing invitation email.
     */
    public void sendProjectInvitationEmail(ProjectInvitation invitation, Project project, 
                                         User invitedByUser, Organization invitedByOrganization) {
        logger.info("Sending project invitation email to: {} for project: {}", 
                   invitation.getInviteEmail(), project.getName());

        try {
            // Build invitation URL
            String invitationUrl = buildInvitationUrl(invitation.getInviteToken());
            
            // Build email content using BerryFi Studio template system
            String subject = buildEmailSubject(invitedByUser.getName(), invitedByOrganization.getName(), project.getName());
            
            String initialCredits = invitation.getInitialCredits() != null ? invitation.getInitialCredits().toString() : "0";
            String monthlyCredits = invitation.getMonthlyRecurringCredits() != null ? invitation.getMonthlyRecurringCredits().toString() : "0";
            
            String htmlContent = emailTemplateService.generateInvitationEmail(
                invitedByUser.getName(),
                project.getName(),
                invitedByOrganization.getName(),
                invitationUrl,
                initialCredits,
                monthlyCredits
            );
            String textContent = buildEmailTextContent(invitation, project, invitedByUser, invitedByOrganization, invitationUrl);

            // Send actual email
            sendActualEmail(invitation.getInviteEmail(), subject, htmlContent, textContent);

            logger.info("Successfully sent invitation email to: {}", invitation.getInviteEmail());

        } catch (Exception e) {
            logger.error("Failed to send invitation email to: {} for project: {}", 
                        invitation.getInviteEmail(), project.getName(), e);
            throw new RuntimeException("Failed to send invitation email", e);
        }
    }

    /**
     * Build invitation URL.
     */
    private String buildInvitationUrl(String inviteToken) {
        return frontendUrl + "/invite/" + inviteToken;
    }

    /**
     * Build email subject.
     */
    private String buildEmailSubject(String inviterName, String organizationName, String projectName) {
        return String.format("%s from %s invited you to join project '%s'", 
                           inviterName, organizationName, projectName);
    }



    /**
     * Build plain text email content following BerryFi Studio design principles.
     */
    private String buildEmailTextContent(ProjectInvitation invitation, Project project, 
                                       User invitedByUser, Organization invitedByOrganization, 
                                       String invitationUrl) {
        StringBuilder text = new StringBuilder();
        
        text.append("🎉 BerryFi Studio - Project Invitation\n");
        text.append("=====================================\n\n");
        
        text.append("Hello!\n\n");
        text.append(invitedByUser.getName()).append(" has invited you to collaborate on \"")
            .append(project.getName()).append("\" in ").append(invitedByOrganization.getName()).append(".\n\n");
        
        text.append("📋 PROJECT DETAILS\n");
        text.append("------------------\n");
        text.append("Project: ").append(project.getName()).append("\n");
        if (project.getDescription() != null && !project.getDescription().trim().isEmpty()) {
            text.append("Description: ").append(project.getDescription()).append("\n");
        }
        text.append("Invited by: ").append(invitedByUser.getName()).append("\n");
        text.append("Organization: ").append(invitedByOrganization.getName()).append("\n");
        
        if (invitation.getInitialCredits() != null && invitation.getInitialCredits() > 0) {
            text.append("Initial Credits: ").append(invitation.getInitialCredits()).append("\n");
        }
        
        if (invitation.getMonthlyRecurringCredits() != null && invitation.getMonthlyRecurringCredits() > 0) {
            text.append("Monthly Credits: ").append(invitation.getMonthlyRecurringCredits()).append("\n");
        }
        
        text.append("Expires: ").append(invitation.getExpiresAt()).append("\n\n");
        
        if (invitation.getShareMessage() != null && !invitation.getShareMessage().trim().isEmpty()) {
            text.append("💬 PERSONAL MESSAGE FROM ").append(invitedByUser.getName().toUpperCase()).append("\n");
            text.append("------------------------------------------\n");
            text.append("\"").append(invitation.getShareMessage()).append("\"\n\n");
        }
        
        text.append("🚀 ACCEPT INVITATION\n");
        text.append("--------------------\n");
        text.append("Click or copy this link to accept:\n");
        text.append(invitationUrl).append("\n\n");
        
        text.append("❓ NEED HELP?\n");
        text.append("-------------\n");
        text.append("Contact ").append(invitedByUser.getName()).append(" at ").append(invitedByUser.getEmail()).append("\n\n");
        
        text.append("════════════════════════════════════════\n");
        text.append("This invitation was sent by BerryFi Studio\n");
        text.append("on behalf of ").append(invitedByOrganization.getName()).append(".\n\n");
        text.append("If you weren't expecting this invitation,\n");
        text.append("you can safely ignore this email.\n\n");
        text.append("© 2025 BerryFi Studio. All rights reserved.\n");
        
        return text.toString();
    }

    /**
     * Send actual email using JavaMailSender.
     */
    private void sendActualEmail(String to, String subject, String htmlContent, String textContent) {
        logger.debug("=== sendActualEmail called ===");
        logger.debug("Email enabled: {}", emailEnabled);
        logger.debug("Email debug mode: {}", emailDebugMode);
        
        if (!emailEnabled) {
            logger.info("Email sending is disabled. Would have sent email to: {} with subject: {}", to, subject);
            return;
        }
        
        if (emailDebugMode) {
            logger.info("=== EMAIL DEBUG MODE - Not sending actual email ===");
            logger.info("To: {}", to);
            logger.info("Subject: {}", subject);
            logger.info("HTML Content Length: {}", htmlContent.length());
            logger.info("Text Content Length: {}", textContent.length());
            logger.info("From: {} <{}>", fromName, fromEmail);
            logger.info("=== EMAIL DEBUG MODE END ===");
            return;
        }

        try {
            logger.debug("Creating MimeMessage");
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Set sender
            logger.debug("Setting sender: {} <{}>", fromName, fromEmail);
            helper.setFrom(fromEmail, fromName);
            
            // Set recipient
            logger.debug("Setting recipient: {}", to);
            helper.setTo(to);
            
            // Set subject
            logger.debug("Setting subject: {}", subject);
            helper.setSubject(subject);
            
            // Set content (HTML with text fallback)
            logger.debug("Setting email content (HTML length: {}, Text length: {})", htmlContent.length(), textContent.length());
            helper.setText(textContent, htmlContent);
            
            // Send the email
            logger.debug("Sending email via JavaMailSender");
            mailSender.send(message);
            
            logger.info("Successfully sent email to: {} with subject: {}", to, subject);
            
        } catch (MessagingException e) {
            logger.error("Failed to send email to: {} with subject: {}", to, subject, e);
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error sending email to: {} with subject: {}", to, subject, e);
            throw new RuntimeException("Unexpected error sending email: " + e.getMessage(), e);
        }
    }

    /**
     * Send team member invitation email.
     */
    public void sendTeamInvitationEmail(String to, String subject, String htmlContent, String textContent) {
        logger.info("=== InvitationEmailService.sendTeamInvitationEmail called ===");
        logger.info("Sending team invitation email to: {} with subject: {}", to, subject);
        logger.debug("Email enabled: {}", emailEnabled);
        logger.debug("From email: {}", fromEmail);
        logger.debug("From name: {}", fromName);
        
        try {
            logger.debug("Calling sendActualEmail method");
            sendActualEmail(to, subject, htmlContent, textContent);
            logger.info("Successfully sent team invitation email to: {}", to);
        } catch (Exception e) {
            logger.error("Failed to send team invitation email to: {} with subject: {}", to, subject, e);
            logger.error("Exception type: {}, message: {}", e.getClass().getSimpleName(), e.getMessage());
            if (e.getCause() != null) {
                logger.error("Root cause: {}, message: {}", e.getCause().getClass().getSimpleName(), e.getCause().getMessage());
            }
            throw new RuntimeException("Failed to send team invitation email", e);
        }
    }

    /**
     * Resend invitation email.
     */
    public void resendInvitationEmail(ProjectInvitation invitation, Project project, 
                                    User invitedByUser, Organization invitedByOrganization) {
        logger.info("Resending project invitation email to: {} for project: {}", 
                   invitation.getInviteEmail(), project.getName());
        
        sendProjectInvitationEmail(invitation, project, invitedByUser, invitedByOrganization);
    }
}