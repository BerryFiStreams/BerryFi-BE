package com.berryfi.portal.controller;

import com.berryfi.portal.dto.usage.*;
import com.berryfi.portal.service.UsageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * REST Controller for usage tracking and analytics.
 */
@RestController
@RequestMapping("/usage")
@CrossOrigin(origins = "*")
public class UsageController {

    @Autowired
    private UsageService usageService;

    /**
     * Start a new usage session
     * POST /usage/sessions/start
     */
    @PostMapping("/sessions/start")
    public ResponseEntity<UsageSessionDto> startSession(
            @RequestBody StartSessionRequest request) {
        try {
            UsageSessionDto session = usageService.startSession(
                    request.getOrganizationId(),
                    request.getProjectId(),
                    request.getProjectName(),
                    request.getUserId(),
                    request.getSessionId());
            return ResponseEntity.status(HttpStatus.CREATED).body(session);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * End a usage session
     * PUT /usage/sessions/{sessionId}/end
     */
    @PutMapping("/sessions/{sessionId}/end")
    public ResponseEntity<UsageSessionDto> endSession(
            @PathVariable String sessionId,
            @RequestBody EndSessionRequest request) {
        try {
            UsageSessionDto session = usageService.endSession(sessionId, request.getCreditsUsed());
            return ResponseEntity.ok(session);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update session metrics
     * PUT /usage/sessions/{sessionId}/metrics
     */
    @PutMapping("/sessions/{sessionId}/metrics")
    public ResponseEntity<UsageSessionDto> updateSession(
            @PathVariable String sessionId,
            @RequestBody UpdateSessionRequest request) {
        try {
            UsageSessionDto session = usageService.updateSession(
                    sessionId,
                    request.getDeviceType(),
                    request.getBrowser(),
                    request.getCountry(),
                    request.getCity(),
                    request.getNetworkQuality(),
                    request.getAvgFps(),
                    request.getAvgBitrate(),
                    request.getErrorCount());
            return ResponseEntity.ok(session);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get usage sessions for organization
     * GET /usage/sessions?organizationId=xxx&projectId=xxx&userId=xxx&startDate=xxx&endDate=xxx&page=0&size=20
     */
    @GetMapping("/sessions")
    public ResponseEntity<Page<UsageSessionDto>> getUsageSessions(
            @RequestParam String organizationId,
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Page<UsageSessionDto> sessions = usageService.getUsageSessions(
                    organizationId, projectId, userId, startDate, endDate, page, size);
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get active sessions for organization
     * GET /usage/sessions/active?organizationId=xxx
     */
    @GetMapping("/sessions/active")
    public ResponseEntity<List<UsageSessionDto>> getActiveSessions(
            @RequestParam String organizationId) {
        try {
            List<UsageSessionDto> sessions = usageService.getActiveSessions(organizationId);
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get usage statistics for organization within date range
     * GET /usage/statistics?organizationId=xxx&startDate=xxx&endDate=xxx
     */
    @GetMapping("/statistics")
    public ResponseEntity<UsageAnalyticsDto> getUsageStatistics(
            @RequestParam String organizationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            UsageAnalyticsDto statistics = usageService.getUsageStatistics(
                    organizationId, startDate, endDate);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get usage analytics for organization
     * GET /usage/analytics?organizationId=xxx&projectId=xxx&startDate=xxx&endDate=xxx&page=0&size=20
     */
    @GetMapping("/analytics")
    public ResponseEntity<Page<UsageAnalyticsDto>> getUsageAnalytics(
            @RequestParam String organizationId,
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Page<UsageAnalyticsDto> analytics = usageService.getUsageAnalytics(
                    organizationId, projectId, startDate, endDate, page, size);
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Generate daily analytics for organization
     * POST /usage/analytics/generate
     */
    @PostMapping("/analytics/generate")
    public ResponseEntity<UsageAnalyticsDto> generateDailyAnalytics(
            @RequestBody GenerateAnalyticsRequest request) {
        try {
            UsageAnalyticsDto analytics = usageService.generateDailyAnalytics(
                    request.getOrganizationId(), request.getDate());
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Request DTOs for POST/PUT endpoints
    public static class StartSessionRequest {
        private String organizationId;
        private String projectId;
        private String projectName;
        private String userId;
        private String sessionId;

        public String getOrganizationId() { return organizationId; }
        public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
        public String getProjectId() { return projectId; }
        public void setProjectId(String projectId) { this.projectId = projectId; }
        public String getProjectName() { return projectName; }
        public void setProjectName(String projectName) { this.projectName = projectName; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    }

    public static class EndSessionRequest {
        private Double creditsUsed;

        public Double getCreditsUsed() { return creditsUsed; }
        public void setCreditsUsed(Double creditsUsed) { this.creditsUsed = creditsUsed; }
    }

    public static class UpdateSessionRequest {
        private String deviceType;
        private String browser;
        private String country;
        private String city;
        private String networkQuality;
        private Double avgFps;
        private Double avgBitrate;
        private Integer errorCount;

        public String getDeviceType() { return deviceType; }
        public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
        public String getBrowser() { return browser; }
        public void setBrowser(String browser) { this.browser = browser; }
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getNetworkQuality() { return networkQuality; }
        public void setNetworkQuality(String networkQuality) { this.networkQuality = networkQuality; }
        public Double getAvgFps() { return avgFps; }
        public void setAvgFps(Double avgFps) { this.avgFps = avgFps; }
        public Double getAvgBitrate() { return avgBitrate; }
        public void setAvgBitrate(Double avgBitrate) { this.avgBitrate = avgBitrate; }
        public Integer getErrorCount() { return errorCount; }
        public void setErrorCount(Integer errorCount) { this.errorCount = errorCount; }
    }

    public static class GenerateAnalyticsRequest {
        private String organizationId;
        private LocalDate date;

        public String getOrganizationId() { return organizationId; }
        public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
    }
}
