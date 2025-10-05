package com.berryfi.portal.service;

import com.berryfi.portal.entity.ProjectInvitation;
import com.berryfi.portal.entity.Project;
import com.berryfi.portal.entity.User;
import com.berryfi.portal.entity.Organization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service for sending invitation emails.
 * This is a mock implementation. In production, integrate with actual email service.
 */
@Service
public class InvitationEmailService {

    private static final Logger logger = LoggerFactory.getLogger(InvitationEmailService.class);

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.company.name:BerryFi}")
    private String companyName;

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
            
            // Build email content
            String subject = buildEmailSubject(invitedByUser.getName(), invitedByOrganization.getName(), project.getName());
            String htmlContent = buildEmailHtmlContent(invitation, project, invitedByUser, invitedByOrganization, invitationUrl);
            String textContent = buildEmailTextContent(invitation, project, invitedByUser, invitedByOrganization, invitationUrl);

            // Mock email sending - In production, replace with actual email service
            mockSendEmail(invitation.getInviteEmail(), subject, htmlContent, textContent);

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
     * Build HTML email content.
     */
    private String buildEmailHtmlContent(ProjectInvitation invitation, Project project, 
                                       User invitedByUser, Organization invitedByOrganization, 
                                       String invitationUrl) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>");
        html.append("<html><head><meta charset='UTF-8'>");
        html.append("<title>Project Invitation</title>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }");
        html.append(".container { max-width: 600px; margin: 0 auto; padding: 20px; }");
        html.append(".header { background: #2196F3; color: white; padding: 20px; text-align: center; }");
        html.append(".content { padding: 30px; background: #f9f9f9; }");
        html.append(".button { display: inline-block; padding: 12px 24px; background: #FF5722; color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }");
        html.append(".details { background: white; padding: 20px; margin: 20px 0; border-radius: 5px; }");
        html.append(".footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }");
        html.append("</style>");
        html.append("</head><body>");
        
        html.append("<div class='container'>");
        html.append("<div class='header'>");
        html.append("<h1>").append(companyName).append(" Project Invitation</h1>");
        html.append("</div>");
        
        html.append("<div class='content'>");
        html.append("<h2>You've been invited to collaborate!</h2>");
        html.append("<p>Hello,</p>");
        html.append("<p><strong>").append(invitedByUser.getName()).append("</strong> from <strong>")
               .append(invitedByOrganization.getName()).append("</strong> has invited you to join the project:</p>");
        
        html.append("<div class='details'>");
        html.append("<h3>Project: ").append(project.getName()).append("</h3>");
        if (project.getDescription() != null) {
            html.append("<p>").append(project.getDescription()).append("</p>");
        }
        
        if (invitation.getInitialCredits() != null && invitation.getInitialCredits() > 0) {
            html.append("<p><strong>Initial Credits:</strong> ").append(invitation.getInitialCredits()).append("</p>");
        }
        
        if (invitation.getMonthlyRecurringCredits() != null && invitation.getMonthlyRecurringCredits() > 0) {
            html.append("<p><strong>Monthly Recurring Credits:</strong> ").append(invitation.getMonthlyRecurringCredits()).append("</p>");
        }
        
        if (invitation.getShareMessage() != null && !invitation.getShareMessage().trim().isEmpty()) {
            html.append("<p><strong>Message from ").append(invitedByUser.getName()).append(":</strong></p>");
            html.append("<p><em>").append(invitation.getShareMessage()).append("</em></p>");
        }
        html.append("</div>");
        
        html.append("<p>To accept this invitation and create your account, click the button below:</p>");
        html.append("<a href='").append(invitationUrl).append("' class='button'>Accept Invitation & Register</a>");
        
        html.append("<p>This invitation will expire on <strong>").append(invitation.getExpiresAt()).append("</strong></p>");
        
        html.append("<p>If you have any questions, you can contact ").append(invitedByUser.getName())
               .append(" at ").append(invitedByUser.getEmail()).append("</p>");
        html.append("</div>");
        
        html.append("<div class='footer'>");
        html.append("<p>This invitation was sent by ").append(companyName).append(" on behalf of ")
               .append(invitedByOrganization.getName()).append("</p>");
        html.append("<p>If you weren't expecting this invitation, you can safely ignore this email.</p>");
        html.append("</div>");
        
        html.append("</div>");
        html.append("</body></html>");
        
        return html.toString();
    }

    /**
     * Build plain text email content.
     */
    private String buildEmailTextContent(ProjectInvitation invitation, Project project, 
                                       User invitedByUser, Organization invitedByOrganization, 
                                       String invitationUrl) {
        StringBuilder text = new StringBuilder();
        
        text.append(companyName).append(" Project Invitation\n");
        text.append("=====================================\n\n");
        
        text.append("Hello,\n\n");
        text.append(invitedByUser.getName()).append(" from ").append(invitedByOrganization.getName())
            .append(" has invited you to join the project:\n\n");
        
        text.append("Project: ").append(project.getName()).append("\n");
        if (project.getDescription() != null) {
            text.append("Description: ").append(project.getDescription()).append("\n");
        }
        
        if (invitation.getInitialCredits() != null && invitation.getInitialCredits() > 0) {
            text.append("Initial Credits: ").append(invitation.getInitialCredits()).append("\n");
        }
        
        if (invitation.getMonthlyRecurringCredits() != null && invitation.getMonthlyRecurringCredits() > 0) {
            text.append("Monthly Recurring Credits: ").append(invitation.getMonthlyRecurringCredits()).append("\n");
        }
        
        if (invitation.getShareMessage() != null && !invitation.getShareMessage().trim().isEmpty()) {
            text.append("\nMessage from ").append(invitedByUser.getName()).append(":\n");
            text.append(invitation.getShareMessage()).append("\n");
        }
        
        text.append("\nTo accept this invitation and create your account, visit:\n");
        text.append(invitationUrl).append("\n\n");
        
        text.append("This invitation will expire on ").append(invitation.getExpiresAt()).append("\n\n");
        
        text.append("If you have any questions, you can contact ").append(invitedByUser.getName())
            .append(" at ").append(invitedByUser.getEmail()).append("\n\n");
        
        text.append("---\n");
        text.append("This invitation was sent by ").append(companyName).append(" on behalf of ")
            .append(invitedByOrganization.getName()).append("\n");
        text.append("If you weren't expecting this invitation, you can safely ignore this email.\n");
        
        return text.toString();
    }

    /**
     * Mock email sending implementation.
     * In production, replace with actual email service (SendGrid, AWS SES, etc.)
     */
    private void mockSendEmail(String to, String subject, String htmlContent, String textContent) {
        logger.info("=== MOCK EMAIL ===");
        logger.info("To: {}", to);
        logger.info("Subject: {}", subject);
        logger.info("HTML Content Length: {} characters", htmlContent.length());
        logger.info("Text Content Length: {} characters", textContent.length());
        logger.info("==================");
        
        // In production, implement actual email sending:
        // emailService.send(to, subject, htmlContent, textContent);
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