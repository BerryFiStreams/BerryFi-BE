package com.berryfi.portal.controller;

import com.berryfi.portal.annotation.OrganizationAudit;
import com.berryfi.portal.annotation.WorkspaceAudit;
import com.berryfi.portal.dto.workspace.*;
import com.berryfi.portal.entity.User;
import com.berryfi.portal.service.WorkspaceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for workspace management operations.
 */
@RestController
@RequestMapping("/api/workspaces")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Workspaces", description = "Workspace management operations")
public class WorkspaceController {

    private static final Logger logger = LoggerFactory.getLogger(WorkspaceController.class);

    @Autowired
    private WorkspaceService workspaceService;

    /**
     * Get all workspaces for the current user's organization.
     * GET /api/workspaces
     */
    @GetMapping
    @OrganizationAudit(action = "VIEW", resource = "WORKSPACE", description = "List workspaces")
    public ResponseEntity<ApiResponse<List<WorkspaceResponse>>> getWorkspaces(
            @AuthenticationPrincipal User currentUser) {

        logger.info("Getting workspaces for organization: {}", currentUser.getOrganizationId());

        List<WorkspaceResponse> workspaces = workspaceService.getWorkspaces(currentUser);

        return ResponseEntity.ok(new ApiResponse<>(true, workspaces));
    }

    /**
     * Create a new workspace.
     * POST /api/workspaces
     */
    @PostMapping
    @OrganizationAudit(action = "CREATE", resource = "WORKSPACE", description = "Create new workspace", includeRequestParams = true, includeResponse = true)
    public ResponseEntity<ApiResponse<WorkspaceResponse>> createWorkspace(
            @Valid @RequestBody CreateWorkspaceRequest request,
            @AuthenticationPrincipal User currentUser) {

        logger.info("Creating workspace: {} for organization: {}", request.getName(), currentUser.getOrganizationId());

        WorkspaceResponse workspace = workspaceService.createWorkspace(request, currentUser);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, workspace));
    }

    /**
     * Get workspace statistics.
     * GET /api/workspaces/stats
     */
    @GetMapping("/stats")
    @OrganizationAudit(action = "VIEW", resource = "WORKSPACE_STATS", description = "View workspace statistics")
    public ResponseEntity<ApiResponse<WorkspaceService.WorkspaceStatistics>> getWorkspaceStatistics(
            @AuthenticationPrincipal User currentUser) {

        logger.info("Getting workspace statistics for organization: {}", currentUser.getOrganizationId());

        WorkspaceService.WorkspaceStatistics stats = workspaceService.getWorkspaceStatistics(currentUser);

        return ResponseEntity.ok(new ApiResponse<>(true, stats));
    }

    /**
     * Get a specific workspace by ID.
     * GET /api/workspaces/{workspaceId}
     */
    @GetMapping("/{workspaceId}")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> getWorkspace(
            @PathVariable String workspaceId,
            @AuthenticationPrincipal User currentUser) {

        logger.info("Getting workspace: {}", workspaceId);

        WorkspaceResponse workspace = workspaceService.getWorkspace(workspaceId, currentUser);

        return ResponseEntity.ok(new ApiResponse<>(true, workspace));
    }

    /**
     * Add credits to a workspace.
     * POST /api/workspaces/{workspaceId}/credits
     */
    @PostMapping("/{workspaceId}/credits")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> addCredits(
            @PathVariable String workspaceId,
            @RequestBody Map<String, Double> creditsRequest,
            @AuthenticationPrincipal User currentUser) {

        logger.info("Adding credits to workspace: {}", workspaceId);

        double giftedCredits = creditsRequest.getOrDefault("giftedCredits", 0.0);
        double purchasedCredits = creditsRequest.getOrDefault("purchasedCredits", 0.0);

        WorkspaceResponse workspace = workspaceService.addCreditsToWorkspace(
            workspaceId, giftedCredits, purchasedCredits, currentUser);

        return ResponseEntity.ok(new ApiResponse<>(true, workspace));
    }

    /**
     * Associate a project with a workspace.
     * PUT /api/workspaces/{workspaceId}/project
     */
    @PutMapping("/{workspaceId}/project")
    public ResponseEntity<ApiResponse<String>> updateWorkspaceProject(
            @PathVariable String workspaceId,
            @RequestBody Map<String, String> projectRequest,
            @AuthenticationPrincipal User currentUser) {

        logger.info("Updating workspace {} with project", workspaceId);

        String projectId = projectRequest.get("projectId");

        workspaceService.updateWorkspaceProject(workspaceId, projectId, currentUser);

        return ResponseEntity.ok(new ApiResponse<>(true, "Project association updated successfully"));
    }

    /**
     * Generic API response wrapper.
     */
    public static class ApiResponse<T> {
        private final boolean success;
        private final T data;

        public ApiResponse(boolean success, T data) {
            this.success = success;
            this.data = data;
        }

        public boolean isSuccess() {
            return success;
        }

        public T getData() {
            return data;
        }
    }
}
