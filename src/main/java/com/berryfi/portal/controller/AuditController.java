package com.berryfi.portal.controller;

import com.berryfi.portal.dto.audit.*;
import com.berryfi.portal.entity.User;
import com.berryfi.portal.service.AuditService;
import com.berryfi.portal.service.AuthService;
import com.berryfi.portal.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @Autowired
    private AuthService authService;

    @Autowired
    private FileService fileService;

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

    /**
     * Get current user's audit logs.
     * GET /audit/my-logs
     */
    @GetMapping("/my-logs")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<AuditLogResponse>> getMyAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            User currentUser = authService.getUserByEmail(
                org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication().getName());
            
            Pageable pageable = PageRequest.of(page, size);
            Page<AuditLogResponse> auditLogs = auditService.getAuditLogsByUser(
                currentUser.getId(), currentUser.getOrganizationId(), pageable);
            return ResponseEntity.ok(auditLogs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get URL access logs for a specific project and current user.
     * GET /audit/projects/{projectId}/url-access
     */
    @GetMapping("/projects/{projectId}/url-access")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<AuditLogResponse>> getUrlAccessLogs(
            @PathVariable String projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            User currentUser = authService.getUserByEmail(
                org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication().getName());
            
            Pageable pageable = PageRequest.of(page, size);
            Page<AuditLogResponse> auditLogs = auditService.getUrlAccessLogs(
                currentUser.getId(), projectId, pageable);
            return ResponseEntity.ok(auditLogs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Download exported audit log file.
     * GET /audit/download/{fileName}
     */
    @GetMapping("/download/{fileName}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_ORG_ADMIN') or hasAuthority('ROLE_ORG_OWNER')")
    public ResponseEntity<String> downloadExportedFile(@PathVariable String fileName) {
        try {
            // Check if file exists
            if (!fileService.exportFileExists(fileName)) {
                return ResponseEntity.notFound().build();
            }

            // Read file content
            String content = fileService.readExportFile(fileName);
            String contentType = fileService.getContentType(fileName);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
            headers.add(HttpHeaders.CONTENT_TYPE, contentType);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(content);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ====================
    // WORKSPACE-LEVEL AUDIT ENDPOINTS
    // ====================

    /**
     * Get audit logs for a specific workspace.
     * GET /audit/workspaces/{workspaceId}/logs
     */
    @GetMapping("/workspaces/{workspaceId}/logs")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_ORG_ADMIN') or hasAuthority('ROLE_WORKSPACE_ADMIN')")
    public ResponseEntity<Page<AuditLogResponse>> getWorkspaceAuditLogs(
            @PathVariable String workspaceId,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String resource,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            
            // Get organization ID from workspace ID (you might need to add this logic)
            // For now, using null which means the service will need to handle workspace-specific queries
            Page<AuditLogResponse> auditLogs = auditService.getAuditLogs(
                    null, workspaceId, userId, action, resource, startDate, endDate, pageable);
            
            return ResponseEntity.ok(auditLogs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ====================
    // VM SESSION AUDIT ENDPOINTS
    // ====================

    /**
     * Get VM session audit logs for a workspace.
     * GET /audit/workspaces/{workspaceId}/vm-sessions
     */
    @GetMapping("/workspaces/{workspaceId}/vm-sessions")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_ORG_ADMIN') or hasAuthority('ROLE_WORKSPACE_ADMIN')")
    public ResponseEntity<Page<VMSessionAuditLogResponse>> getVMSessionAuditLogs(
            @PathVariable String workspaceId,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String sessionId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String vmInstanceId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<VMSessionAuditLogResponse> auditLogs = auditService.getVMSessionAuditLogs(
                    workspaceId, userId, sessionId, action, vmInstanceId, 
                    startDate, endDate, status, pageable);
            
            return ResponseEntity.ok(auditLogs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get VM session audit logs for an organization (org-level access).
     * GET /audit/organizations/{organizationId}/vm-sessions
     */
    @GetMapping("/organizations/{organizationId}/vm-sessions")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_ORG_ADMIN') or hasAuthority('ROLE_ORG_OWNER')")
    public ResponseEntity<Page<VMSessionAuditLogResponse>> getOrganizationVMSessionAuditLogs(
            @PathVariable String organizationId,
            @RequestParam(required = false) String workspaceId,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String sessionId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String vmInstanceId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<VMSessionAuditLogResponse> auditLogs = auditService.getVMSessionAuditLogsByOrganization(
                    organizationId, workspaceId, userId, sessionId, action, vmInstanceId,
                    startDate, endDate, status, pageable);
            
            return ResponseEntity.ok(auditLogs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get VM session audit statistics for a workspace.
     * GET /audit/workspaces/{workspaceId}/vm-sessions/stats
     */
    @GetMapping("/workspaces/{workspaceId}/vm-sessions/stats")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_ORG_ADMIN') or hasAuthority('ROLE_WORKSPACE_ADMIN')")
    public ResponseEntity<VMSessionAuditStatsResponse> getVMSessionAuditStats(
            @PathVariable String workspaceId) {
        try {
            VMSessionAuditStatsResponse stats = auditService.getVMSessionAuditStats(workspaceId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get current user's VM session audit logs in their workspaces.
     * GET /audit/my-vm-sessions
     */
    @GetMapping("/my-vm-sessions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<VMSessionAuditLogResponse>> getMyVMSessionAuditLogs(
            @RequestParam(required = false) String workspaceId,
            @RequestParam(required = false) String sessionId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            User currentUser = authService.getUserByEmail(
                org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication().getName());
            
            if (workspaceId == null) {
                // If no workspace specified, get from user's organization
                Pageable pageable = PageRequest.of(page, size);
                Page<VMSessionAuditLogResponse> auditLogs = auditService.getVMSessionAuditLogsByOrganization(
                    currentUser.getOrganizationId(), null, currentUser.getId(), sessionId, action, null,
                    startDate, endDate, null, pageable);
                return ResponseEntity.ok(auditLogs);
            } else {
                // Specific workspace
                Pageable pageable = PageRequest.of(page, size);
                Page<VMSessionAuditLogResponse> auditLogs = auditService.getVMSessionAuditLogs(
                    workspaceId, currentUser.getId(), sessionId, action, null, 
                    startDate, endDate, null, pageable);
                return ResponseEntity.ok(auditLogs);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
