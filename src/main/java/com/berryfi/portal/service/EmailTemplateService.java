package com.berryfi.portal.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Service for processing email templates with BerryFi Studio design system.
 * Handles template loading, variable substitution, and email content generation.
 */
@Service
public class EmailTemplateService {

    private static final Logger logger = LoggerFactory.getLogger(EmailTemplateService.class);

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${app.company.name:BerryFi Studio}")
    private String companyName;

    /**
     * Process an email template with variable substitution.
     * 
     * @param templateName Template file name (without .html extension)
     * @param variables Map of variables to substitute in the template
     * @return Processed HTML content
     */
    public String processTemplate(String templateName, Map<String, String> variables) {
        try {
            // Load template content
            String templateContent = loadTemplate(templateName);
            
            // Substitute variables
            String processedContent = substituteVariables(templateContent, variables);
            
            logger.info("Successfully processed email template: {}", templateName);
            return processedContent;
            
        } catch (Exception e) {
            logger.error("Failed to process email template: {}", templateName, e);
            throw new RuntimeException("Failed to process email template: " + templateName, e);
        }
    }

    /**
     * Load template file from classpath.
     */
    private String loadTemplate(String templateName) throws IOException {
        String templatePath = "templates/email/" + templateName + ".html";
        ClassPathResource resource = new ClassPathResource(templatePath);
        
        if (!resource.exists()) {
            throw new IllegalArgumentException("Email template not found: " + templatePath);
        }
        
        return resource.getContentAsString(StandardCharsets.UTF_8);
    }

    /**
     * Substitute variables in template content.
     */
    private String substituteVariables(String template, Map<String, String> variables) {
        String result = template;
        
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            String value = entry.getValue() != null ? entry.getValue() : "";
            result = result.replace(placeholder, value);
        }
        
        return result;
    }

    /**
     * Generate invitation email content using the BerryFi Studio template.
     */
    public String generateInvitationEmail(String inviterName, String projectName, 
                                        String organizationName, String invitationLink,
                                        String initialCredits, String monthlyCredits) {
        Map<String, String> variables = Map.of(
            "inviter_name", inviterName,
            "project_name", projectName,
            "organization_name", organizationName,
            "invitation_link", invitationLink,
            "initial_credits", initialCredits,
            "monthly_credits", monthlyCredits
        );
        
        return processTemplate("invitation", variables);
    }

    /**
     * Generate a welcome email for new users.
     */
    public String generateWelcomeEmail(String userName, String organizationName) {
        Map<String, String> variables = Map.of(
            "user_name", userName,
            "organization_name", organizationName,
            "dashboard_link", frontendUrl + "/dashboard",
            "company_name", companyName
        );
        
        return processTemplate("welcome", variables);
    }

    /**
     * Generate a password reset email.
     */
    public String generatePasswordResetEmail(String userName, String resetLink) {
        Map<String, String> variables = Map.of(
            "user_name", userName,
            "reset_link", resetLink,
            "company_name", companyName
        );
        
        return processTemplate("password-reset", variables);
    }

    /**
     * Generate a notification email for various system events.
     */
    public String generateNotificationEmail(String userName, String subject, 
                                          String message, String actionLink, String actionText) {
        Map<String, String> variables = Map.of(
            "user_name", userName,
            "notification_subject", subject,
            "notification_message", message,
            "action_link", actionLink != null ? actionLink : "",
            "action_text", actionText != null ? actionText : "View Details",
            "company_name", companyName
        );
        
        return processTemplate("notification", variables);
    }
}