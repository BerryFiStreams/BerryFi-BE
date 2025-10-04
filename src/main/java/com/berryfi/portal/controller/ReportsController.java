package com.berryfi.portal.controller;

import com.berryfi.portal.dto.reports.*;
import com.berryfi.portal.entity.User;
import com.berryfi.portal.service.ReportsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for reports endpoints.
 */
@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportsController {

    @Autowired
    private ReportsService reportsService;

    /**
     * Get analytics reports data for authenticated user's organization.
     * GET /reports/analytics
     */
    @GetMapping("/analytics")
    public ResponseEntity<AnalyticsReportResponse> getAnalyticsReport(
            @RequestParam(required = false) String workspaceId,
            @RequestParam(required = false) String dateRange,
            @RequestParam(required = false) String reportType,
            @AuthenticationPrincipal User currentUser) {
        try {
            String organizationId = currentUser.getOrganizationId();
            AnalyticsReportResponse report = reportsService.getAnalyticsReport(
                    organizationId, workspaceId, dateRange, reportType);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get dashboard reports.
     * GET /reports/dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardReportResponse> getDashboardReport(
            @AuthenticationPrincipal User currentUser) {
        try {
            DashboardReportResponse report = reportsService.getDashboardReport(currentUser);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Generate custom report.
     * POST /reports/generate
     */
    @PostMapping("/generate")
    public ResponseEntity<ReportGenerationResponse> generateReport(
            @RequestBody GenerateReportRequest request) {
        try {
            ReportGenerationResponse response = reportsService.generateReport(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get scheduled reports.
     * GET /reports/scheduled
     */
    @GetMapping("/scheduled")
    public ResponseEntity<ScheduledReportsResponse> getScheduledReports(
            @AuthenticationPrincipal User currentUser) {
        try {
            String organizationId = currentUser.getOrganizationId();
            ScheduledReportsResponse reports = reportsService.getScheduledReports(organizationId);
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create scheduled report.
     * POST /reports/scheduled
     */
    @PostMapping("/scheduled")
    public ResponseEntity<ScheduledReportResponse> createScheduledReport(
            @RequestBody CreateScheduledReportRequest request) {
        try {
            ScheduledReportResponse response = reportsService.createScheduledReport(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update scheduled report.
     * PUT /reports/scheduled/{reportId}
     */
    @PutMapping("/scheduled/{reportId}")
    public ResponseEntity<ScheduledReportResponse> updateScheduledReport(
            @PathVariable String reportId,
            @RequestBody UpdateScheduledReportRequest request) {
        try {
            ScheduledReportResponse response = reportsService.updateScheduledReport(reportId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete scheduled report.
     * DELETE /reports/scheduled/{reportId}
     */
    @DeleteMapping("/scheduled/{reportId}")
    public ResponseEntity<Void> deleteScheduledReport(@PathVariable String reportId) {
        try {
            reportsService.deleteScheduledReport(reportId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get report templates.
     * GET /reports/templates
     */
    @GetMapping("/templates")
    public ResponseEntity<ReportTemplatesResponse> getReportTemplates() {
        try {
            ReportTemplatesResponse templates = reportsService.getReportTemplates();
            return ResponseEntity.ok(templates);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Export report.
     * POST /reports/export
     */
    @PostMapping("/export")
    public ResponseEntity<ReportExportResponse> exportReport(
            @RequestBody ExportReportRequest request) {
        try {
            ReportExportResponse response = reportsService.exportReport(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
