package com.berryfi.portal.service;

import com.berryfi.portal.entity.AuditLog;
import com.berryfi.portal.entity.Project;
import com.berryfi.portal.entity.TrackingLink;
import com.berryfi.portal.entity.User;
import com.berryfi.portal.repository.AuditLogRepository;
import com.berryfi.portal.repository.ProjectRepository;
import com.berryfi.portal.repository.TrackingLinkRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.time.LocalDateTime;
import java.security.SecureRandom;

/**
 * Service for URL tracking and audit logging.
 */
@Service
@Transactional
public class UrlTrackingService {

    private static final Logger logger = LoggerFactory.getLogger(UrlTrackingService.class);

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.domain}")
    private String appDomain;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TrackingLinkRepository trackingLinkRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Build the base URL for a project based on custom domain or subdomain.
     * Priority: customDomain (if verified) > subdomain > default base URL
     */
    private String buildProjectBaseUrl(Project project) {
        // Use custom domain if verified
        if (project.getCustomDomain() != null && 
            Boolean.TRUE.equals(project.getCustomDomainVerified())) {
            return "https://" + project.getCustomDomain();
        }
        
        // Use subdomain if available
        if (project.getSubdomain() != null && !project.getSubdomain().trim().isEmpty()) {
            return "https://" + project.getSubdomain() + "." + appDomain;
        }
        
        // Fallback to default base URL
        return this.baseUrl;
    }

    /**
     * Generate a user-specific tracking URL for a project using project's subdomain/custom domain.
     */
    public String generateTrackingUrl(String projectId, String userId) {
        // Fetch project to determine base URL
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null) {
            logger.warn("Project not found for tracking URL generation: {}", projectId);
            return this.baseUrl;
        }
        
        String projectBaseUrl = buildProjectBaseUrl(project);
        return generateShortTrackingUrl(projectId, userId, projectBaseUrl);
    }

    /**
     * Generate a user-specific tracking URL for a project using project's subdomain/custom domain.
     * This overload accepts the Project object directly to avoid redundant DB queries.
     */
    public String generateTrackingUrl(Project project, String userId) {
        if (project == null) {
            logger.warn("Project is null for tracking URL generation");
            return this.baseUrl;
        }
        
        String projectBaseUrl = buildProjectBaseUrl(project);
        return generateShortTrackingUrl(project.getId(), userId, projectBaseUrl);
    }

    /**
     * Generate a short tracking URL for a project.
     */
    public String generateShortTrackingUrl(String projectId, String userId, String baseUrl) {
        try {
            // Check if we already have an active tracking link for this project and user
            TrackingLink existingLink = trackingLinkRepository
                .findByProjectIdAndUserIdAndActive(projectId, userId, true)
                .orElse(null);
            
            if (existingLink != null) {
                // Return existing short URL
                return baseUrl + "/track/" + existingLink.getShortCode();
            }

            // Generate new short code
            String shortCode = generateUniqueShortCode();
            String token = generateTrackingToken(projectId, userId);

            // Create tracking link entity
            TrackingLink trackingLink = new TrackingLink(shortCode, projectId, userId, token);
            trackingLink.setExpiresAt(LocalDateTime.now().plusDays(365)); // 1 year expiry
            trackingLinkRepository.save(trackingLink);

            // Generate short tracking URL
            String trackingUrl = baseUrl + "/track/" + shortCode;

            logger.debug("Generated short tracking URL for project {} and user {}: {}", projectId, userId, trackingUrl);
            return trackingUrl;

        } catch (Exception e) {
            logger.error("Error generating short tracking URL for project {} and user {}: {}", projectId, userId, e.getMessage());
            // Fallback to long URL method
            return generateTrackingUrl(projectId, userId, baseUrl);
        }
    }

    /**
     * Generate a unique short code (8 characters).
     */
    private String generateUniqueShortCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder shortCode;
        
        do {
            shortCode = new StringBuilder(8);
            for (int i = 0; i < 8; i++) {
                shortCode.append(characters.charAt(random.nextInt(characters.length())));
            }
        } while (trackingLinkRepository.existsByShortCode(shortCode.toString()));
        
        return shortCode.toString();
    }

    /**
     * Generate a user-specific tracking URL for a project.
     */
    public String generateTrackingUrl(String projectId, String userId, String baseUrl) {
        try {
            // Create tracking parameters
            Map<String, String> trackingParams = new HashMap<>();
            trackingParams.put("projectId", projectId);
            trackingParams.put("userId", userId);
            trackingParams.put("timestamp", String.valueOf(System.currentTimeMillis()));
            trackingParams.put("token", generateTrackingToken(projectId, userId));

            // Encode tracking parameters
            String trackingData = objectMapper.writeValueAsString(trackingParams);
            String encodedTracking = Base64.getUrlEncoder().encodeToString(trackingData.getBytes());

            // Generate tracking URL
            String trackingUrl = baseUrl + "/track/" + encodedTracking;

            logger.debug("Generated tracking URL for project {} and user {}: {}", projectId, userId, trackingUrl);
            return trackingUrl;

        } catch (Exception e) {
            logger.error("Error generating tracking URL for project {} and user {}: {}", projectId, userId, e.getMessage());
            return baseUrl; // Fallback to original URL
        }
    }

    /**
     * Track URL access using short code and log audit entry.
     */
    public void trackShortUrlAccess(String shortCode, String referrerUrl, HttpServletRequest request) {
        try {
            // Find tracking link by short code
            TrackingLink trackingLink = trackingLinkRepository
                .findActiveByShortCode(shortCode, LocalDateTime.now())
                .orElse(null);

            if (trackingLink == null) {
                logger.warn("Invalid or expired short code: {}", shortCode);
                return;
            }

            String projectId = trackingLink.getProjectId();
            String userId = trackingLink.getUserId();

            // Get project and validate
            Project project = projectRepository.findById(projectId).orElse(null);
            if (project == null) {
                logger.warn("Project not found for tracking: {}", projectId);
                return;
            }

            // Create audit log entry
            AuditLog auditLog = new AuditLog();
            auditLog.setUserId(userId);
            auditLog.setOrganizationId(project.getOrganizationId());
            auditLog.setAction("URL_ACCESS");
            auditLog.setResource("PROJECT_URL");
            auditLog.setResourceId(projectId);
            auditLog.setProjectId(projectId);
            auditLog.setTrackingUrl(request.getRequestURL().toString());
            auditLog.setReferrerUrl(referrerUrl != null && !referrerUrl.trim().isEmpty() ? referrerUrl : "direct");
            auditLog.setOriginalUrl(project.getLinks());
            auditLog.setIpAddress(getClientIpAddress(request));
            auditLog.setUserAgent(request.getHeader("User-Agent"));
            auditLog.setSessionId(request.getSession(false) != null ? request.getSession().getId() : null);

            // Add additional details
            Map<String, Object> details = new HashMap<>();
            details.put("projectName", project.getName());
            details.put("shortCode", shortCode);
            details.put("trackingToken", trackingLink.getToken());
            details.put("method", request.getMethod());
            details.put("queryString", request.getQueryString());

            auditLog.setDetails(objectMapper.writeValueAsString(details));

            // Save audit log
            auditLogRepository.save(auditLog);

            logger.info("Tracked short URL access for project {} by user {} from referrer: {}", 
                       projectId, userId, referrerUrl);

        } catch (Exception e) {
            logger.error("Error tracking short URL access: {}", e.getMessage(), e);
        }
    }

    /**
     * Get project from short code.
     */
    public Project getProjectByShortCode(String shortCode) {
        try {
            TrackingLink trackingLink = trackingLinkRepository
                .findActiveByShortCode(shortCode, LocalDateTime.now())
                .orElse(null);

            if (trackingLink != null) {
                return projectRepository.findById(trackingLink.getProjectId()).orElse(null);
            }
            return null;
        } catch (Exception e) {
            logger.error("Error getting project by short code: {}", e.getMessage(), e);
            return null;
        }
    }
    public void trackUrlAccess(String trackingData, String referrerUrl, HttpServletRequest request) {
        try {
            // Decode tracking data
            byte[] decodedBytes = Base64.getUrlDecoder().decode(trackingData);
            String decodedString = new String(decodedBytes);
            
            @SuppressWarnings("unchecked")
            Map<String, String> trackingParams = objectMapper.readValue(decodedString, Map.class);

            String projectId = trackingParams.get("projectId");
            String userId = trackingParams.get("userId");
            String token = trackingParams.get("token");

            // Validate tracking token
            if (!validateTrackingToken(projectId, userId, token)) {
                logger.warn("Invalid tracking token for project {} and user {}", projectId, userId);
                return;
            }

            // Get project and validate
            Project project = projectRepository.findById(projectId).orElse(null);
            if (project == null) {
                logger.warn("Project not found for tracking: {}", projectId);
                return;
            }

            // Create audit log entry
            AuditLog auditLog = new AuditLog();
            auditLog.setUserId(userId);
            auditLog.setOrganizationId(project.getOrganizationId());
            auditLog.setAction("URL_ACCESS");
            auditLog.setResource("PROJECT_URL");
            auditLog.setResourceId(projectId);
            auditLog.setProjectId(projectId);
            auditLog.setTrackingUrl(request.getRequestURL().toString());
            auditLog.setReferrerUrl(referrerUrl != null && !referrerUrl.trim().isEmpty() ? referrerUrl : "direct");
            auditLog.setOriginalUrl(project.getLinks());
            auditLog.setIpAddress(getClientIpAddress(request));
            auditLog.setUserAgent(request.getHeader("User-Agent"));
            auditLog.setSessionId(request.getSession(false) != null ? request.getSession().getId() : null);

            // Add additional details
            Map<String, Object> details = new HashMap<>();
            details.put("projectName", project.getName());
            details.put("trackingToken", token);
            details.put("timestamp", trackingParams.get("timestamp"));
            details.put("method", request.getMethod());
            details.put("queryString", request.getQueryString());

            auditLog.setDetails(objectMapper.writeValueAsString(details));

            // Save audit log
            auditLogRepository.save(auditLog);

            logger.info("Tracked URL access for project {} by user {} from referrer: {}", 
                       projectId, userId, referrerUrl);

        } catch (Exception e) {
            logger.error("Error tracking URL access: {}", e.getMessage(), e);
        }
    }

    /**
     * Generate a tracking token for validation.
     */
    private String generateTrackingToken(String projectId, String userId) {
        String data = projectId + ":" + userId + ":" + System.currentTimeMillis();
        return UUID.nameUUIDFromBytes(data.getBytes()).toString();
    }

    /**
     * Validate tracking token.
     */
    private boolean validateTrackingToken(String projectId, String userId, String token) {
        // For now, just check if token is not empty
        // In production, implement proper token validation with expiration
        return token != null && !token.trim().isEmpty();
    }

    /**
     * Get client IP address from request.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * Log general audit event.
     */
    public void logAuditEvent(User user, String action, String resource, String resourceId, 
                             Map<String, Object> details, HttpServletRequest request) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setUserId(user.getId());
            auditLog.setUserName(user.getName());
            auditLog.setOrganizationId(user.getOrganizationId());
            auditLog.setAction(action);
            auditLog.setResource(resource);
            auditLog.setResourceId(resourceId);
            
            if (request != null) {
                auditLog.setIpAddress(getClientIpAddress(request));
                auditLog.setUserAgent(request.getHeader("User-Agent"));
                auditLog.setSessionId(request.getSession(false) != null ? request.getSession().getId() : null);
            }

            if (details != null) {
                auditLog.setDetails(objectMapper.writeValueAsString(details));
            }

            auditLogRepository.save(auditLog);

            logger.debug("Logged audit event: {} {} by user {}", action, resource, user.getId());

        } catch (Exception e) {
            logger.error("Error logging audit event: {}", e.getMessage(), e);
        }
    }
}
