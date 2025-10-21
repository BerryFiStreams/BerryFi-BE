package com.berryfi.portal.controller;

import com.berryfi.portal.entity.Project;
import com.berryfi.portal.repository.ProjectRepository;
import com.berryfi.portal.service.UrlTrackingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
            // Ensure baseUrl is not null and has a valid value
            String redirectUrl = (baseUrl != null && !baseUrl.trim().isEmpty()) ? baseUrl : "https://berryfi.com";
            response.sendRedirect(redirectUrl);
        }
    }

    /**
     * Handle short code tracking.
     */
    private void handleShortCodeTracking(String shortCode, String referrerUrl, 
                                       HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // Track the URL access
            urlTrackingService.trackShortUrlAccess(shortCode, referrerUrl, request);
            
            // Get project information
            Project project = urlTrackingService.getProjectByShortCode(shortCode);
            if (project != null) {
                // Set tracking cookie for the UI
                setTrackingCookie(response, shortCode, project.getId());
                
                // Determine redirect URL - back to the subdomain they came from
                String redirectUrl = determineRedirectUrl(request, project);
                logger.info("Redirecting to subdomain: {} for short code: {}", redirectUrl, shortCode);
                response.sendRedirect(redirectUrl);
                return;
            }
            
            logger.warn("Project not found for short code: {}", shortCode);
        } catch (Exception e) {
            logger.error("Error processing short code tracking: {}", e.getMessage(), e);
        }
        
        // Fallback redirect
        String redirectUrl = (baseUrl != null && !baseUrl.trim().isEmpty()) ? baseUrl : "https://berryfi.com";
        response.sendRedirect(redirectUrl);
    }

    /**
     * Handle legacy Base64 tracking.
     */
    private void handleLegacyTracking(String trackingData, String referrerUrl,
                                    HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // Track the URL access
            urlTrackingService.trackUrlAccess(trackingData, referrerUrl, request);
            
            // Decode tracking data to get project information
            byte[] decodedBytes = Base64.getUrlDecoder().decode(trackingData);
            String decodedString = new String(decodedBytes);
            
            @SuppressWarnings("unchecked")
            Map<String, String> trackingParams = objectMapper.readValue(decodedString, Map.class);
            
            String projectId = trackingParams.get("projectId");
            
            if (projectId != null) {
                // Get project
                Project project = projectRepository.findById(projectId).orElse(null);
                if (project != null) {
                    // Set tracking cookie for the UI
                    setTrackingCookie(response, trackingData, projectId);
                    
                    // Determine redirect URL - back to the subdomain they came from
                    String redirectUrl = determineRedirectUrl(request, project);
                    logger.info("Redirecting to subdomain: {} for project: {}", redirectUrl, projectId);
                    response.sendRedirect(redirectUrl);
                    return;
                }
            }
            
            // Fallback redirect if project not found or no URL
            logger.warn("Project not found or no URL for tracking data: {}", trackingData);
        } catch (Exception e) {
            logger.error("Error processing legacy tracking data: {}", e.getMessage());
        }
        
        // Final fallback redirect
        String redirectUrl = (baseUrl != null && !baseUrl.trim().isEmpty()) ? baseUrl : "https://berryfi.com";
        response.sendRedirect(redirectUrl);
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
    
    /**
     * Set a tracking cookie for the frontend to use.
     */
    private void setTrackingCookie(HttpServletResponse response, String trackingCode, String projectId) {
        try {
            // Create tracking info JSON
            String trackingInfo = String.format("{\"trackingCode\":\"%s\",\"projectId\":\"%s\",\"timestamp\":%d}", 
                                               trackingCode, projectId, System.currentTimeMillis());
            
            // Encode to Base64 for cookie value
            String encodedValue = Base64.getUrlEncoder().withoutPadding()
                                       .encodeToString(trackingInfo.getBytes(StandardCharsets.UTF_8));
            
            // Create cookie
            Cookie trackingCookie = new Cookie("berryfi_tracking", encodedValue);
            trackingCookie.setPath("/");
            trackingCookie.setMaxAge(60 * 60 * 24 * 30); // 30 days
            trackingCookie.setHttpOnly(false); // Allow JavaScript access
            trackingCookie.setSecure(false); // Set to true in production with HTTPS
            
            response.addCookie(trackingCookie);
            logger.debug("Set tracking cookie for project: {}", projectId);
        } catch (Exception e) {
            logger.error("Error setting tracking cookie: {}", e.getMessage());
        }
    }
    
    /**
     * Determine the redirect URL based on the request origin (subdomain).
     */
    private String determineRedirectUrl(HttpServletRequest request, Project project) {
        // Get the Host header to determine which subdomain the request came from
        String host = request.getHeader("Host");
        String scheme = request.getScheme();
        
        logger.debug("Determining redirect URL - Host: {}, Scheme: {}", host, scheme);
        
        // If request came from a subdomain, redirect back to it
        if (host != null && !host.isEmpty()) {
            // Check if this is already the project's subdomain
            if (project.getSubdomain() != null && host.contains(project.getSubdomain())) {
                // Redirect to root of subdomain
                return scheme + "://" + host + "/";
            }
            
            // Check if project has a custom domain and request came from it
            if (project.getCustomDomain() != null && host.contains(project.getCustomDomain())) {
                return scheme + "://" + host + "/";
            }
            
            // If request came from main portal, redirect to project subdomain
            if (project.getSubdomain() != null && !project.getSubdomain().trim().isEmpty()) {
                return project.getSubdomain();
            }
            
            // If no subdomain, redirect back to origin
            return scheme + "://" + host + "/";
        }
        
        // Fallback: use project's subdomain URL if available
        if (project.getSubdomain() != null && !project.getSubdomain().trim().isEmpty()) {
            return project.getSubdomain();
        }
        
        // Ultimate fallback
        return (baseUrl != null && !baseUrl.trim().isEmpty()) ? baseUrl : "https://berryfi.com";
    }
}
