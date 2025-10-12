package com.berryfi.portal.controller;

import com.berryfi.portal.dto.analytics.*;
import com.berryfi.portal.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for analytics endpoints.
 */
@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    /**
     * Get usage analytics data with optional filters.
     * GET /analytics/usage
     */
    @GetMapping("/usage")
    public ResponseEntity<UsageAnalyticsResponse> getUsageAnalytics(
            @RequestParam String dateRange,
            @RequestParam(required = false) String filterType,
            @RequestParam(required = false) String selectedFilter,
            @RequestParam(required = false) String projectId) {
        try {
            UsageAnalyticsResponse analytics = analyticsService.getUsageAnalytics(
                    dateRange, filterType, selectedFilter, projectId);
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get lead analytics data with optional filters.
     * GET /analytics/leads
     */
    @GetMapping("/leads")
    public ResponseEntity<LeadAnalyticsResponse> getLeadAnalytics(
            @RequestParam String dateRange,
            @RequestParam(required = false) String filterType,
            @RequestParam(required = false) String selectedFilter,
            @RequestParam(required = false) String projectId) {
        try {
            LeadAnalyticsResponse analytics = analyticsService.getLeadAnalytics(
                    dateRange, filterType, selectedFilter, projectId);
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get geographic analytics data with optional filters.
     * GET /analytics/geographic
     */
    @GetMapping("/geographic")
    public ResponseEntity<GeographicAnalyticsResponse> getGeographicAnalytics(
            @RequestParam String dateRange,
            @RequestParam(required = false) String filterType,
            @RequestParam(required = false) String selectedFilter,
            @RequestParam(required = false) String projectId) {
        try {
            GeographicAnalyticsResponse analytics = analyticsService.getGeographicAnalytics(
                    dateRange, filterType, selectedFilter, projectId);
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get device analytics data with optional filters.
     * GET /analytics/devices
     */
    @GetMapping("/devices")
    public ResponseEntity<DeviceAnalyticsResponse> getDeviceAnalytics(
            @RequestParam String dateRange,
            @RequestParam(required = false) String filterType,
            @RequestParam(required = false) String selectedFilter,
            @RequestParam(required = false) String projectId) {
        try {
            DeviceAnalyticsResponse analytics = analyticsService.getDeviceAnalytics(
                    dateRange, filterType, selectedFilter, projectId);
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get network analytics data with optional filters.
     * GET /analytics/network
     */
    @GetMapping("/network")
    public ResponseEntity<NetworkAnalyticsResponse> getNetworkAnalytics(
            @RequestParam String dateRange,
            @RequestParam(required = false) String filterType,
            @RequestParam(required = false) String selectedFilter,
            @RequestParam(required = false) String projectId) {
        try {
            NetworkAnalyticsResponse analytics = analyticsService.getNetworkAnalytics(
                    dateRange, filterType, selectedFilter, projectId);
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get project usage analytics data with optional filters.
     * GET /analytics/project-usage
     */
    @GetMapping("/project-usage")
    public ResponseEntity<ProjectUsageAnalyticsResponse> getProjectUsageAnalytics(
            @RequestParam String dateRange,
            @RequestParam(required = false) String filterType,
            @RequestParam(required = false) String selectedFilter,
            @RequestParam(required = false) String projectId) {
        try {
            ProjectUsageAnalyticsResponse analytics = analyticsService.getProjectUsageAnalytics(
                    dateRange, filterType, selectedFilter, projectId);
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Export leads data.
     * POST /analytics/export-leads
     */
    @PostMapping("/export-leads")
    public ResponseEntity<ExportResponse> exportLeads(
            @RequestBody ExportLeadsRequest request) {
        try {
            ExportResponse response = analyticsService.exportLeads(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get lead journey analytics.
     * GET /leads/{leadId}/journey
     */
    @GetMapping("/leads/{leadId}/journey")
    public ResponseEntity<LeadJourneyResponse> getLeadJourney(
            @PathVariable String leadId) {
        try {
            LeadJourneyResponse journey = analyticsService.getLeadJourney(leadId);
            return ResponseEntity.ok(journey);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
