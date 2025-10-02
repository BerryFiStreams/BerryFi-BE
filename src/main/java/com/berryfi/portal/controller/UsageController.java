package com.berryfi.portal.controller;

import com.berryfi.portal.dto.usage.*;
import com.berryfi.portal.entity.User;
import com.berryfi.portal.service.UsageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for usage tracking and analytics - workspace-based approach.
 */
@RestController
@RequestMapping("/api/usage")
@CrossOrigin(origins = "*")
public class UsageController {

    @Autowired
    private UsageService usageService;

    /**
     * Get usage sessions for user's entitled workspaces - workspace-based approach
     * GET /api/usage/sessions?projectId=xxx&userId=xxx&startDate=xxx&endDate=xxx&page=0&size=20
     */
    @GetMapping("/sessions")
    public ResponseEntity<Page<UsageSessionDto>> getUsageSessions(
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal User currentUser) {
        try {
            Page<UsageSessionDto> sessions = usageService.getUsageSessionsForUser(
                    currentUser, projectId, userId, startDate, endDate, page, size);
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get active sessions for user's organization
     * GET /api/usage/sessions/active
     */
    @GetMapping("/sessions/active")
    public ResponseEntity<List<UsageSessionDto>> getActiveSessions(
            @AuthenticationPrincipal User currentUser) {
        try {
            List<UsageSessionDto> sessions = usageService.getActiveSessionsForUser(currentUser);
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get usage statistics for user's organization within date range
     * GET /api/usage/statistics?startDate=xxx&endDate=xxx
     */
    @GetMapping("/statistics")
    public ResponseEntity<UsageAnalyticsDto> getUsageStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal User currentUser) {
        try {
            UsageAnalyticsDto statistics = usageService.getUsageStatisticsForUser(
                    currentUser, startDate, endDate);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get usage analytics for user's entitled workspaces
     * GET /api/usage/analytics?projectId=xxx&startDate=xxx&endDate=xxx&page=0&size=20
     */
    @GetMapping("/analytics")
    public ResponseEntity<Page<UsageAnalyticsDto>> getUsageAnalytics(
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal User currentUser) {
        try {
            Page<UsageAnalyticsDto> analytics = usageService.getUsageAnalyticsForUser(
                    currentUser, projectId, startDate, endDate, page, size);
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Generate daily analytics for user's organization
     * POST /api/usage/analytics/generate
     */
    @PostMapping("/analytics/generate")
    public ResponseEntity<List<UsageAnalyticsDto>> generateDailyAnalytics(
            @RequestBody GenerateAnalyticsRequest request,
            @AuthenticationPrincipal User currentUser) {
        try {
            List<UsageAnalyticsDto> analytics = usageService.generateDailyAnalyticsForUser(
                    currentUser, request.getDate());
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Request DTOs for POST/PUT endpoints
    public static class GenerateAnalyticsRequest {
        private LocalDate date;

        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
    }
}
