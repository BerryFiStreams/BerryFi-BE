package com.berryfi.portal.controller;

import com.berryfi.portal.dto.audit.*;
import com.berryfi.portal.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for audit endpoints.
 */
@RestController
@RequestMapping("/audit")
@CrossOrigin(origins = "*")
public class AuditController {

    @Autowired
    private AuditService auditService;

    /**
     * Get audit logs with pagination and filtering.
     * GET /audit/logs
     */
    @GetMapping("/logs")
    public ResponseEntity<Page<AuditLogResponse>> getAuditLogs(
            @RequestParam(required = false) String organizationId,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String resource,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<AuditLogResponse> auditLogs = auditService.getAuditLogs(
                    organizationId, userId, action, resource, startDate, endDate, pageable);
            return ResponseEntity.ok(auditLogs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get audit statistics.
     * GET /audit/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<AuditStatsResponse> getAuditStats(
            @RequestParam(required = false) String organizationId,
            @RequestParam(required = false) String dateRange) {
        try {
            AuditStatsResponse stats = auditService.getAuditStats(organizationId, dateRange);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get audit logs by user.
     * GET /audit/users/{userId}/logs
     */
    @GetMapping("/users/{userId}/logs")
    public ResponseEntity<Page<AuditLogResponse>> getAuditLogsByUser(
            @PathVariable String userId,
            @RequestParam(required = false) String organizationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<AuditLogResponse> auditLogs = auditService.getAuditLogsByUser(userId, organizationId, pageable);
            return ResponseEntity.ok(auditLogs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get available action types for filtering.
     * GET /audit/action-types
     */
    @GetMapping("/action-types")
    public ResponseEntity<List<String>> getActionTypes() {
        try {
            List<String> actionTypes = auditService.getActionTypes();
            return ResponseEntity.ok(actionTypes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Export audit logs.
     * POST /audit/export
     */
    @PostMapping("/export")
    public ResponseEntity<AuditExportResponse> exportAuditLogs(
            @RequestBody AuditExportRequest request) {
        try {
            AuditExportResponse response = auditService.exportAuditLogs(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get user activity summary.
     * GET /audit/users/{userId}/activity
     */
    @GetMapping("/users/{userId}/activity")
    public ResponseEntity<UserActivityResponse> getUserActivity(
            @PathVariable String userId,
            @RequestParam(required = false) String dateRange) {
        try {
            UserActivityResponse activity = auditService.getUserActivity(userId, dateRange);
            return ResponseEntity.ok(activity);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get audit log details by ID.
     * GET /audit/logs/{logId}
     */
    @GetMapping("/logs/{logId}")
    public ResponseEntity<AuditLogDetailResponse> getAuditLogDetails(
            @PathVariable String logId) {
        try {
            AuditLogDetailResponse details = auditService.getAuditLogDetails(logId);
            return ResponseEntity.ok(details);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
