package com.berryfi.portal.controller;

import com.berryfi.portal.dto.project.*;
import com.berryfi.portal.entity.User;
import com.berryfi.portal.enums.ProjectStatus;
import com.berryfi.portal.service.ProjectService;
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

/**
 * REST Controller for project management operations.
 */
@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProjectController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);

    @Autowired
    private ProjectService projectService;

    /**
     * Get all projects for an organization.
     * GET /api/projects?organizationId=org123&page=0&size=10&sort=createdAt,desc
     */
    @GetMapping
    public ResponseEntity<Page<ProjectSummary>> getProjects(
            @RequestParam String organizationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal User currentUser) {

        logger.info("Getting projects for organization: {} (page: {}, size: {})", organizationId, page, size);

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ProjectSummary> projects = projectService.getProjects(organizationId, pageable, currentUser);

        return ResponseEntity.ok(projects);
    }

    /**
     * Get projects by workspace.
     * GET /api/projects/workspace/{workspaceId}
     */
    @GetMapping("/workspace/{workspaceId}")
    public ResponseEntity<Page<ProjectSummary>> getProjectsByWorkspace(
            @PathVariable String workspaceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal User currentUser) {

        logger.info("Getting projects for workspace: {} (page: {}, size: {})", workspaceId, page, size);

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ProjectSummary> projects = projectService.getProjectsByWorkspace(workspaceId, pageable, currentUser);

        return ResponseEntity.ok(projects);
    }

    /**
     * Create a new project.
     * POST /api/projects
     */
    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            @AuthenticationPrincipal User currentUser) {

        logger.info("Creating project: {} for organization: {}", request.getName(), request.getOrganizationId());

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
     * Search projects.
     * GET /api/projects/search?organizationId=org123&q=keyword
     */
    @GetMapping("/search")
    public ResponseEntity<Page<ProjectSummary>> searchProjects(
            @RequestParam String organizationId,
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal User currentUser) {

        logger.info("Searching projects in organization: {} with keyword: {}", organizationId, q);

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ProjectSummary> projects = projectService.searchProjects(organizationId, q, pageable, currentUser);

        return ResponseEntity.ok(projects);
    }

    /**
     * Get project statistics for an organization.
     * GET /api/projects/stats?organizationId=org123
     */
    @GetMapping("/stats")
    public ResponseEntity<ProjectService.ProjectStatistics> getProjectStatistics(
            @RequestParam String organizationId,
            @AuthenticationPrincipal User currentUser) {

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
}
