package com.berryfi.portal.controller;

import com.berryfi.portal.dto.common.PageResponse;
import com.berryfi.portal.dto.project.*;
import com.berryfi.portal.entity.User;
import com.berryfi.portal.enums.ProjectStatus;
import com.berryfi.portal.service.ProjectService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for project management operations.
 */
@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Projects", description = "Project management operations")
public class ProjectController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);

    @Autowired
    private ProjectService projectService;

    /**
     * Get all projects for the authenticated user's organization.
     * GET /api/projects?page=0&size=10&sort=createdAt,desc
     */
    @GetMapping
    public ResponseEntity<PageResponse<ProjectSummary>> getProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal User currentUser) {

        String organizationId = currentUser.getOrganizationId();
        logger.info("Getting projects for organization: {} (page: {}, size: {})", organizationId, page, size);

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ProjectSummary> projects = projectService.getProjects(organizationId, pageable, currentUser);

        return ResponseEntity.ok(PageResponse.from(projects));
    }



    /**
     * Create a new project.
     * POST /api/projects
     */
    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            @AuthenticationPrincipal User currentUser) {

        String organizationId = currentUser.getOrganizationId();
        logger.info("Creating project: {} for organization: {}", request.getName(), organizationId);

        ProjectResponse project = projectService.createProject(request, currentUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(project);
    }

    /**
     * Get a specific project by ID.
     * GET /api/projects/{projectId}
     */
    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> getProject(
            @PathVariable String projectId,
            @AuthenticationPrincipal User currentUser) {

        logger.info("Getting project: {}", projectId);

        ProjectResponse project = projectService.getProject(projectId, currentUser);

        return ResponseEntity.ok(project);
    }

    /**
     * Update a project.
     * PUT /api/projects/{projectId}
     */
    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable String projectId,
            @Valid @RequestBody UpdateProjectRequest request,
            @AuthenticationPrincipal User currentUser) {

        logger.info("Updating project: {}", projectId);

        ProjectResponse project = projectService.updateProject(projectId, request, currentUser);

        return ResponseEntity.ok(project);
    }

    /**
     * Delete a project.
     * DELETE /api/projects/{projectId}
     */
    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable String projectId,
            @AuthenticationPrincipal User currentUser) {

        logger.info("Deleting project: {}", projectId);

        projectService.deleteProject(projectId, currentUser);

        return ResponseEntity.noContent().build();
    }

    /**
     * Deploy a project.
     * POST /api/projects/{projectId}/deploy
     */
    @PostMapping("/{projectId}/deploy")
    public ResponseEntity<ProjectResponse> deployProject(
            @PathVariable String projectId,
            @Valid @RequestBody DeployProjectRequest request,
            @AuthenticationPrincipal User currentUser) {

        logger.info("Deploying project: {}", projectId);

        ProjectResponse project = projectService.deployProject(projectId, request, currentUser);

        return ResponseEntity.ok(project);
    }

    /**
     * Stop a project.
     * POST /api/projects/{projectId}/stop
     */
    @PostMapping("/{projectId}/stop")
    public ResponseEntity<ProjectResponse> stopProject(
            @PathVariable String projectId,
            @AuthenticationPrincipal User currentUser) {

        logger.info("Stopping project: {}", projectId);

        ProjectResponse project = projectService.stopProject(projectId, currentUser);

        return ResponseEntity.ok(project);
    }

    /**
     * Get project status.
     * GET /api/projects/{projectId}/status
     */
    @GetMapping("/{projectId}/status")
    public ResponseEntity<ProjectStatusResponse> getProjectStatus(
            @PathVariable String projectId,
            @AuthenticationPrincipal User currentUser) {

        logger.info("Getting status for project: {}", projectId);

        ProjectStatus status = projectService.getProjectStatus(projectId, currentUser);

        return ResponseEntity.ok(new ProjectStatusResponse(status.getValue(), status.getDisplayName()));
    }

    /**
     * Search projects in the authenticated user's organization.
     * GET /api/projects/search?q=keyword
     */
    @GetMapping("/search")
    public ResponseEntity<PageResponse<ProjectSummary>> searchProjects(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal User currentUser) {

        String organizationId = currentUser.getOrganizationId();
        logger.info("Searching projects in organization: {} with keyword: {}", organizationId, q);

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ProjectSummary> projects = projectService.searchProjects(organizationId, q, pageable, currentUser);

        return ResponseEntity.ok(PageResponse.from(projects));
    }

    /**
     * Get project statistics for the authenticated user's organization.
     * GET /api/projects/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<ProjectService.ProjectStatistics> getProjectStatistics(
            @AuthenticationPrincipal User currentUser) {

        String organizationId = currentUser.getOrganizationId();
        logger.info("Getting project statistics for organization: {}", organizationId);

        ProjectService.ProjectStatistics stats = projectService.getProjectStatistics(organizationId, currentUser);

        return ResponseEntity.ok(stats);
    }

    /**
     * DTO for project status response.
     */
    public static class ProjectStatusResponse {
        private final String status;
        private final String displayName;

        public ProjectStatusResponse(String status, String displayName) {
            this.status = status;
            this.displayName = displayName;
        }

        public String getStatus() {
            return status;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Get project configuration.
     * GET /api/projects/{projectId}/config
     */
    @GetMapping("/{projectId}/config")
    public ResponseEntity<ProjectConfigResponse> getProjectConfig(
            @PathVariable String projectId,
            @AuthenticationPrincipal User currentUser) {
        logger.info("Getting config for project: {}", projectId);

        try {
            ProjectConfigResponse config = projectService.getProjectConfig(projectId, currentUser);
            return ResponseEntity.ok(config);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get project branding settings.
     * GET /api/projects/{projectId}/branding
     */
    @GetMapping("/{projectId}/branding")
    public ResponseEntity<ProjectBrandingResponse> getBrandingSettings(
            @PathVariable String projectId,
            @AuthenticationPrincipal User currentUser) {
        logger.info("Getting branding settings for project: {}", projectId);

        try {
            ProjectBrandingResponse branding = projectService.getProjectBranding(projectId, currentUser);
            return ResponseEntity.ok(branding);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get project link settings.
     * GET /api/projects/{projectId}/links
     */
    @GetMapping("/{projectId}/links")
    public ResponseEntity<ProjectLinksResponse> getLinkSettings(
            @PathVariable String projectId,
            @AuthenticationPrincipal User currentUser) {
        logger.info("Getting link settings for project: {}", projectId);

        try {
            ProjectLinksResponse links = projectService.getProjectLinks(projectId, currentUser);
            return ResponseEntity.ok(links);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Share a project with another organization with credit allocation.
     * Only organization admins (ORG_OWNER, ORG_ADMIN, SUPER_ADMIN) can share projects.
     * POST /api/projects/{projectId}/share
     */
    @PostMapping("/{projectId}/share")
    public ResponseEntity<Void> shareProject(
            @PathVariable String projectId,
            @Valid @RequestBody ShareProjectRequest request,
            @AuthenticationPrincipal User currentUser) {

        logger.info("Sharing project {} with request: {}", projectId, request);

        projectService.shareProject(projectId, request, currentUser);

        return ResponseEntity.ok().build();
    }

    /**
     * Unshare a project from an organization.
     * Only organization admins (ORG_OWNER, ORG_ADMIN, SUPER_ADMIN) can unshare projects.
     * DELETE /api/projects/{projectId}/share/{organizationId}
     */
    @DeleteMapping("/{projectId}/share/{organizationId}")
    public ResponseEntity<Void> unshareProject(
            @PathVariable String projectId,
            @PathVariable String organizationId,
            @AuthenticationPrincipal User currentUser) {

        logger.info("Unsharing project {} from organization: {}", projectId, organizationId);

        projectService.unshareProject(projectId, organizationId, currentUser);

        return ResponseEntity.ok().build();
    }

    /**
     * Allocate credits to a project.
     * POST /api/projects/{projectId}/credits
     */
    @PostMapping("/{projectId}/credits")
    public ResponseEntity<ProjectResponse> allocateCredits(
            @PathVariable String projectId,
            @Valid @RequestBody AllocateCreditsRequest request,
            @AuthenticationPrincipal User currentUser) {

        logger.info("Allocating {} credits to project: {}", request.getCredits(), projectId);

        ProjectResponse project = projectService.allocateCredits(projectId, request.getCredits(), currentUser);

        return ResponseEntity.ok(project);
    }

    /**
     * Get all projects shared by the current organization with usage statistics.
     * Returns both direct shares (projects owned by this org) and indirect shares (projects reshared by this org).
     * Includes organization names, admin emails, credits used, and session counts for each shared project.
     * Only organization admins can access this endpoint.
     * GET /api/projects/shared-usage
     */
    @GetMapping("/shared-usage")
    public ResponseEntity<List<SharedProjectUsageResponse>> getSharedProjectUsage(
            @AuthenticationPrincipal User currentUser) {

        logger.info("Getting shared project usage for organization: {}", currentUser.getOrganizationId());

        List<SharedProjectUsageResponse> sharedUsage = projectService.getSharedProjectUsage(currentUser);

        return ResponseEntity.ok(sharedUsage);
    }

}
