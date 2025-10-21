package com.berryfi.portal.controller;

import com.berryfi.portal.context.TenantContext;
import com.berryfi.portal.dto.tenant.TenantConfigResponse;
import com.berryfi.portal.entity.Project;
import com.berryfi.portal.repository.ProjectRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * REST Controller for tenant configuration and branding.
 * Provides tenant-specific information to the frontend based on subdomain.
 */
@RestController
@RequestMapping("/api/tenant")
@Tag(name = "Tenant", description = "Tenant configuration and branding operations")
public class TenantController {

    private static final Logger logger = LoggerFactory.getLogger(TenantController.class);

    @Autowired
    private ProjectRepository projectRepository;

    /**
     * Get tenant configuration for the current request's subdomain.
     * This endpoint is called by the frontend to load tenant-specific branding.
     * 
     * GET /api/tenant/config
     */
    @GetMapping("/config")
    public ResponseEntity<TenantConfigResponse> getTenantConfig(HttpServletRequest request) {
        String host = request.getHeader("Host");
        String subdomain = TenantContext.getTenantSubdomain();
        String projectId = TenantContext.getProjectId();
        
        logger.info("GET /api/tenant/config - Host: {}, Thread: {}, Context: {}", 
                   host, Thread.currentThread().getName(), TenantContext.getContextSummary());
        logger.debug("TenantContext details - subdomain: {}, projectId: {}, hasContext: {}", 
                    subdomain, projectId, TenantContext.hasTenantContext());

        // Check if we're in a tenant context
        if (!TenantContext.hasTenantContext()) {
            logger.warn("No tenant context found for Host: {}. Returning default config.", host);
            // Return default/portal configuration
            return ResponseEntity.ok(createDefaultConfig());
        }

        Optional<Project> projectOpt = projectRepository.findById(projectId);

        if (projectOpt.isEmpty()) {
            logger.warn("Project not found in tenant context: {}", projectId);
            return ResponseEntity.ok(createDefaultConfig());
        }

        Project project = projectOpt.get();
        TenantConfigResponse config = mapProjectToConfig(project);

        logger.info("Returning tenant config for project: {} ({})", project.getName(), project.getId());
        return ResponseEntity.ok(config);
    }

    /**
     * Get tenant configuration by subdomain (public endpoint for initial load).
     * This allows the frontend to fetch config before making authenticated requests.
     * 
     * GET /api/tenant/config/{subdomain}
     */
    @GetMapping("/config/{subdomain}")
    public ResponseEntity<TenantConfigResponse> getTenantConfigBySubdomain(@PathVariable String subdomain) {
        logger.info("Getting tenant config for subdomain: {}", subdomain);

        Optional<Project> projectOpt = projectRepository.findBySubdomain(subdomain);

        if (projectOpt.isEmpty()) {
            logger.warn("No project found for subdomain: {}", subdomain);
            return ResponseEntity.ok(createDefaultConfig());
        }

        Project project = projectOpt.get();
        TenantConfigResponse config = mapProjectToConfig(project);

        logger.info("Returning tenant config for subdomain '{}': project {} ({})", 
                   subdomain, project.getName(), project.getId());
        return ResponseEntity.ok(config);
    }

    /**
     * Check if a subdomain is available.
     * 
     * GET /api/tenant/check-subdomain?subdomain=myproject
     */
    @GetMapping("/check-subdomain")
    public ResponseEntity<?> checkSubdomainAvailability(@RequestParam String subdomain) {
        logger.info("Checking subdomain availability: {}", subdomain);

        boolean exists = projectRepository.existsBySubdomain(subdomain);

        return ResponseEntity.ok(new SubdomainCheckResponse(subdomain, !exists));
    }

    /**
     * Map Project entity to TenantConfigResponse DTO.
     */
    private TenantConfigResponse mapProjectToConfig(Project project) {
        TenantConfigResponse config = new TenantConfigResponse();
        config.setProjectId(project.getId());
        config.setProjectName(project.getName());
        config.setSubdomain(project.getSubdomain());
        config.setCustomDomain(project.getCustomDomain());
        config.setOrganizationId(project.getOrganizationId());
        
        // Branding
        config.setBrandLogoUrl(project.getBrandLogoUrl());
        config.setBrandPrimaryColor(project.getBrandPrimaryColor());
        config.setBrandSecondaryColor(project.getBrandSecondaryColor());
        config.setBrandFaviconUrl(project.getBrandFaviconUrl());
        config.setBrandAppName(project.getBrandAppName() != null ? 
                              project.getBrandAppName() : project.getName());
        config.setBrandYoutubeUrl(project.getBrandYoutubeUrl());
        
        return config;
    }

    /**
     * Create default configuration for main portal.
     */
    private TenantConfigResponse createDefaultConfig() {
        TenantConfigResponse config = new TenantConfigResponse();
        config.setProjectName("BerryFi Portal");
        config.setBrandAppName("BerryFi Studio");
        config.setBrandPrimaryColor("#ff1136");
        config.setBrandSecondaryColor("#2d2d2d");
        return config;
    }

    /**
     * Response class for subdomain availability check.
     */
    static class SubdomainCheckResponse {
        private String subdomain;
        private boolean available;

        public SubdomainCheckResponse(String subdomain, boolean available) {
            this.subdomain = subdomain;
            this.available = available;
        }

        public String getSubdomain() {
            return subdomain;
        }

        public void setSubdomain(String subdomain) {
            this.subdomain = subdomain;
        }

        public boolean isAvailable() {
            return available;
        }

        public void setAvailable(boolean available) {
            this.available = available;
        }
    }
}
