package com.berryfi.portal.service;

import com.berryfi.portal.entity.AuditLog;
import com.berryfi.portal.entity.Project;
import com.berryfi.portal.entity.User;
import com.berryfi.portal.repository.AuditLogRepository;
import com.berryfi.portal.repository.ProjectRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service for URL tracking and audit logging.
 */
@Service
@Transactional
public class UrlTrackingService {

    private static final Logger logger = LoggerFactory.getLogger(UrlTrackingService.class);

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ObjectMapper objectMapper;

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
     * Track URL access and log audit entry.
     */
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
            auditLog.setReferrerUrl(referrerUrl);
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
