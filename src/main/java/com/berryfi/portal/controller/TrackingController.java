package com.berryfi.portal.controller;

import com.berryfi.portal.entity.Project;
import com.berryfi.portal.repository.ProjectRepository;
import com.berryfi.portal.service.UrlTrackingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

/**
 * Controller for URL tracking and redirection.
 */
@RestController
@RequestMapping("/track")
public class TrackingController {

    private static final Logger logger = LoggerFactory.getLogger(TrackingController.class);

    @Value("${app.base-url}")
    private String baseUrl;

    @Autowired
    private UrlTrackingService urlTrackingService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Track URL access and redirect to original URL.
     * GET /track/{trackingData}
     */
    @GetMapping("/{trackingData}")
    public void trackAndRedirect(@PathVariable String trackingData,
                               @RequestHeader(value = "Referer", required = false) String referrerUrl,
                               HttpServletRequest request,
                               HttpServletResponse response) throws IOException {
        
        try {
            // Check if it's a short code (8 characters, alphanumeric) or Base64 encoded data
            if (trackingData.length() == 8 && trackingData.matches("[A-Za-z0-9]{8}")) {
                // Handle short code
                handleShortCodeTracking(trackingData, referrerUrl, request, response);
            } else {
                // Handle Base64 encoded tracking data (legacy format)
                handleLegacyTracking(trackingData, referrerUrl, request, response);
            }
            
        } catch (Exception e) {
            logger.error("Error processing tracking request: {}", e.getMessage(), e);
            response.sendRedirect(baseUrl);
        }
    }

    /**
     * Handle short code tracking.
     */
    private void handleShortCodeTracking(String shortCode, String referrerUrl, 
                                       HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Track the URL access
        urlTrackingService.trackShortUrlAccess(shortCode, referrerUrl, request);
        
        // Get project and redirect
        Project project = urlTrackingService.getProjectByShortCode(shortCode);
        if (project != null && project.getSubdomain() != null && !project.getSubdomain().trim().isEmpty()) {
            logger.info("Redirecting to project URL: {} for short code: {}", project.getLinks(), shortCode);
            response.sendRedirect(project.getSubdomain());
        } else {
            logger.warn("Project not found or no URL for short code: {}", shortCode);
            response.sendRedirect(baseUrl);
        }
    }

    /**
     * Handle legacy Base64 tracking.
     */
    private void handleLegacyTracking(String trackingData, String referrerUrl,
                                    HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Track the URL access
        urlTrackingService.trackUrlAccess(trackingData, referrerUrl, request);
        
        // Decode tracking data to get project information
        byte[] decodedBytes = Base64.getUrlDecoder().decode(trackingData);
        String decodedString = new String(decodedBytes);
        
        @SuppressWarnings("unchecked")
        Map<String, String> trackingParams = objectMapper.readValue(decodedString, Map.class);
        
        String projectId = trackingParams.get("projectId");
        
        if (projectId != null) {
            // Get project and redirect to its URL
            Project project = projectRepository.findById(projectId).orElse(null);
            if (project != null && project.getLinks() != null && !project.getLinks().trim().isEmpty()) {
                logger.info("Redirecting to project URL: {} for project: {}", project.getLinks(), projectId);
                response.sendRedirect(project.getLinks());
                return;
            }
        }
        
        // Fallback redirect if project not found or no URL
        logger.warn("Project not found or no URL for tracking data: {}", trackingData);
        response.sendRedirect(baseUrl);
    }

    /**
     * Generate tracking URL for a project using the configured base URL.
     * POST /track/generate
     */
    @PostMapping("/generate")
    public String generateTrackingUrl(@RequestParam String projectId,
                                    @RequestParam String userId) {
        return urlTrackingService.generateTrackingUrl(projectId, userId);
    }

    /**
     * Generate tracking URL for a project with custom base URL.
     * POST /track/generate-custom
     */
    @PostMapping("/generate-custom")
    public String generateTrackingUrlCustom(@RequestParam String projectId,
                                           @RequestParam String userId,
                                           @RequestParam String baseUrl) {
        return urlTrackingService.generateTrackingUrl(projectId, userId, baseUrl);
    }
}
