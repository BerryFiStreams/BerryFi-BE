package com.berryfi.portal.service;

import com.berryfi.portal.context.TenantContext;
import com.berryfi.portal.entity.Project;
import com.berryfi.portal.repository.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service for resolving tenant information from subdomain or custom domain.
 * Handles multi-tenant project identification and context setup.
 */
@Service
public class TenantService {

    private static final Logger logger = LoggerFactory.getLogger(TenantService.class);

    @Autowired
    private ProjectRepository projectRepository;

    @Value("${app.domain:berryfi.in}")
    private String appDomain;

    @Value("${app.main-subdomain:portal}")
    private String mainSubdomain;

    /**
     * Resolve tenant project from hostname.
     * Returns the Project entity if a tenant is found, null if it's the main portal.
     *
     * @param hostname The full hostname from the request (e.g., "myproject.berryfi.in" or "myclient.com")
     * @return Optional<Project> containing the project if found
     */
    public Optional<Project> resolveTenantFromHostname(String hostname) {
        if (hostname == null || hostname.isEmpty()) {
            logger.debug("Empty hostname, treating as main portal");
            return Optional.empty();
        }

        // Remove port if present
        hostname = hostname.split(":")[0].toLowerCase();

        logger.debug("Resolving tenant from hostname: {}", hostname);

        // Check if it's the main portal domain
        if (isMainPortal(hostname)) {
            logger.debug("Main portal domain detected: {}", hostname);
            return Optional.empty();
        }

        // Try to resolve by subdomain first
        Optional<Project> project = resolveBySubdomain(hostname);
        if (project.isPresent()) {
            return project;
        }

        // Try to resolve by custom domain
        return resolveByCustomDomain(hostname);
    }

    /**
     * Check if hostname is the main portal (portal.berryfi.in or berryfi.in).
     */
    private boolean isMainPortal(String hostname) {
        return hostname.equals(appDomain) || 
               hostname.equals(mainSubdomain + "." + appDomain) ||
               hostname.equals("www." + appDomain);
    }

    /**
     * Resolve project by subdomain pattern.
     * E.g., "myproject.berryfi.in" -> subdomain = "myproject"
     */
    private Optional<Project> resolveBySubdomain(String hostname) {
        // Check if hostname matches pattern: {subdomain}.berryfi.in
        if (hostname.endsWith("." + appDomain)) {
            String subdomain = hostname.substring(0, hostname.length() - appDomain.length() - 1);
            
            // Skip if it's the main portal subdomain
            if (subdomain.equals(mainSubdomain) || subdomain.equals("www")) {
                return Optional.empty();
            }

            // Remove any additional subdomains (e.g., "api.myproject.berryfi.in" -> "myproject")
            String[] parts = subdomain.split("\\.");
            if (parts.length > 1) {
                subdomain = parts[parts.length - 1];
            }

            logger.debug("Extracted subdomain: {}", subdomain);

            Optional<Project> project = projectRepository.findBySubdomain(subdomain);
            if (project.isPresent()) {
                logger.info("Found project by subdomain '{}': {}", subdomain, project.get().getId());
                return project;
            } else {
                logger.warn("No project found for subdomain: {}", subdomain);
            }
        }

        return Optional.empty();
    }

    /**
     * Resolve project by custom domain.
     * E.g., "myclient.com" -> find project with customDomain = "myclient.com"
     */
    private Optional<Project> resolveByCustomDomain(String hostname) {
        logger.debug("Looking up custom domain: {}", hostname);
        
        Optional<Project> project = projectRepository.findByCustomDomainAndCustomDomainVerified(hostname, true);
        
        if (project.isPresent()) {
            logger.info("Found project by custom domain '{}': {}", hostname, project.get().getId());
            return project;
        } else {
            logger.warn("No project found for custom domain: {}", hostname);
        }

        return Optional.empty();
    }

    /**
     * Set tenant context from a resolved project.
     */
    public void setTenantContext(Project project, String subdomain) {
        TenantContext.setProjectId(project.getId());
        TenantContext.setProjectName(project.getName());
        TenantContext.setOrganizationId(project.getOrganizationId());
        TenantContext.setTenantSubdomain(subdomain);
        
        logger.info("Tenant context set: {}", TenantContext.getContextSummary());
    }

    /**
     * Generate a unique subdomain for a new project.
     * Sanitizes the project name and ensures uniqueness.
     */
    public String generateUniqueSubdomain(String projectName) {
        // Sanitize project name to create a valid subdomain
        String baseSubdomain = sanitizeSubdomain(projectName);
        
        // Check if subdomain already exists
        String subdomain = baseSubdomain;
        int counter = 1;
        
        while (projectRepository.findBySubdomain(subdomain).isPresent()) {
            subdomain = baseSubdomain + counter;
            counter++;
            
            // Safety check to prevent infinite loops
            if (counter > 1000) {
                // Fallback to UUID-based subdomain
                subdomain = baseSubdomain + "-" + java.util.UUID.randomUUID().toString().substring(0, 8);
                break;
            }
        }
        
        logger.info("Generated unique subdomain: {}", subdomain);
        return subdomain;
    }

    /**
     * Sanitize a string to create a valid subdomain.
     * - Lowercase
     * - Remove special characters (keep only alphanumeric and hyphens)
     * - Max 63 characters
     * - Start and end with alphanumeric
     */
    public String sanitizeSubdomain(String input) {
        if (input == null || input.isEmpty()) {
            return "project";
        }

        // Convert to lowercase and trim
        String sanitized = input.toLowerCase().trim();

        // Replace spaces and underscores with hyphens
        sanitized = sanitized.replaceAll("[\\s_]+", "-");

        // Remove all characters except alphanumeric and hyphens
        sanitized = sanitized.replaceAll("[^a-z0-9-]", "");

        // Remove consecutive hyphens
        sanitized = sanitized.replaceAll("-+", "-");

        // Remove leading/trailing hyphens
        sanitized = sanitized.replaceAll("^-+|-+$", "");

        // Ensure it starts with a letter or number
        if (sanitized.isEmpty() || !Character.isLetterOrDigit(sanitized.charAt(0))) {
            sanitized = "proj-" + sanitized;
        }

        // Limit to 63 characters (DNS subdomain limit)
        if (sanitized.length() > 63) {
            sanitized = sanitized.substring(0, 63);
        }

        // If still empty or invalid, use default
        if (sanitized.isEmpty()) {
            sanitized = "project";
        }

        return sanitized;
    }

    /**
     * Validate if a subdomain is available and valid.
     */
    public boolean isSubdomainAvailable(String subdomain) {
        if (subdomain == null || subdomain.isEmpty()) {
            return false;
        }

        // Check if it matches the sanitized version (is valid)
        if (!subdomain.equals(sanitizeSubdomain(subdomain))) {
            return false;
        }

        // Check if it's reserved
        if (isReservedSubdomain(subdomain)) {
            return false;
        }

        // Check if it's already taken
        return projectRepository.findBySubdomain(subdomain).isEmpty();
    }

    /**
     * Check if subdomain is reserved (system subdomains).
     */
    private boolean isReservedSubdomain(String subdomain) {
        String[] reserved = {
            "portal", "www", "api", "admin", "app", "mail", "ftp", "smtp",
            "pop", "imap", "webmail", "ns1", "ns2", "dns", "blog", "shop",
            "store", "dev", "staging", "test", "demo", "docs", "help",
            "support", "status", "cdn", "static", "assets", "media"
        };

        for (String r : reserved) {
            if (subdomain.equalsIgnoreCase(r)) {
                return true;
            }
        }

        return false;
    }
}
