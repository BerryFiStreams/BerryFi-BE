package com.berryfi.portal.service;

import com.berryfi.portal.dto.project.*;
import com.berryfi.portal.entity.Project;
import com.berryfi.portal.entity.User;
import com.berryfi.portal.enums.ProjectStatus;
import com.berryfi.portal.exception.ResourceNotFoundException;
import com.berryfi.portal.exception.UnauthorizedException;
import com.berryfi.portal.repository.ProjectRepository;
import com.berryfi.portal.service.WorkspaceService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service class for managing projects.
 */
@Service
@Transactional
public class ProjectService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private WorkspaceService workspaceService;

    /**
     * Create a new project.
     */
    @PreAuthorize("hasPermission('project', 'create')")
    public ProjectResponse createProject(CreateProjectRequest request, User currentUser) {
        logger.info("Creating new project: {} for organization: {}", request.getName(), request.getOrganizationId());

        // Check if project name already exists in organization
        if (projectRepository.existsByNameAndOrganizationId(request.getName(), request.getOrganizationId())) {
            throw new IllegalArgumentException("Project with name '" + request.getName() + "' already exists in this organization");
        }

        Project project = new Project(
            request.getName(),
            request.getDescription(),
            request.getOrganizationId(),
            request.getAccountType(),
            currentUser.getId()
        );

        project.setWorkspaceId(request.getWorkspaceId());
        project.setConfig(request.getConfig());
        project.setBranding(request.getBranding());
        project.setLinks(request.getLinks());

        Project savedProject = projectRepository.save(project);
        logger.info("Created project: {} with ID: {}", savedProject.getName(), savedProject.getId());

        // If project is associated with a workspace, update the workspace
        if (savedProject.getWorkspaceId() != null && !savedProject.getWorkspaceId().trim().isEmpty()) {
            try {
                workspaceService.updateWorkspaceProject(
                    savedProject.getWorkspaceId(), 
                    savedProject.getId(), 
                    savedProject.getName(), 
                    currentUser
                );
                logger.info("Updated workspace {} with new project {}", 
                           savedProject.getWorkspaceId(), savedProject.getId());
            } catch (Exception e) {
                logger.warn("Failed to update workspace with project: {}", e.getMessage());
            }
        }

        return ProjectResponse.from(savedProject);
    }

    /**
     * Get all projects for an organization with pagination.
     */
    @PreAuthorize("hasPermission('project', 'read')")
    public Page<ProjectSummary> getProjects(String organizationId, Pageable pageable, User currentUser) {
        logger.debug("Fetching projects for organization: {}", organizationId);

        // Ensure user has access to this organization
        validateOrganizationAccess(organizationId, currentUser);

        return projectRepository.findByOrganizationId(organizationId, pageable)
                .map(ProjectSummary::from);
    }

    /**
     * Get projects by workspace.
     */
    @PreAuthorize("hasPermission('project', 'read')")
    public Page<ProjectSummary> getProjectsByWorkspace(String workspaceId, Pageable pageable, User currentUser) {
        logger.debug("Fetching projects for workspace: {}", workspaceId);

        return projectRepository.findByWorkspaceId(workspaceId, pageable)
                .map(ProjectSummary::from);
    }

    /**
     * Get a specific project by ID.
     */
    @PreAuthorize("hasPermission('project', 'read')")
    public ProjectResponse getProject(String projectId, User currentUser) {
        logger.debug("Fetching project: {}", projectId);

        Project project = findProjectWithAccess(projectId, currentUser);
        return ProjectResponse.from(project);
    }

    /**
     * Update a project.
     */
    @PreAuthorize("hasPermission('project', 'update')")
    public ProjectResponse updateProject(String projectId, UpdateProjectRequest request, User currentUser) {
        logger.info("Updating project: {}", projectId);

        Project project = findProjectWithAccess(projectId, currentUser);

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            // Check if new name already exists (excluding current project)
            if (!request.getName().equals(project.getName()) && 
                projectRepository.existsByNameAndOrganizationId(request.getName(), project.getOrganizationId())) {
                throw new IllegalArgumentException("Project with name '" + request.getName() + "' already exists in this organization");
            }
            project.setName(request.getName().trim());
        }

        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }

        if (request.getConfig() != null) {
            project.setConfig(request.getConfig());
        }

        if (request.getBranding() != null) {
            project.setBranding(request.getBranding());
        }

        if (request.getLinks() != null) {
            project.setLinks(request.getLinks());
        }

        Project updatedProject = projectRepository.save(project);
        logger.info("Updated project: {}", updatedProject.getId());

        return ProjectResponse.from(updatedProject);
    }

    /**
     * Delete a project.
     */
    @PreAuthorize("hasPermission('project', 'delete')")
    public void deleteProject(String projectId, User currentUser) {
        logger.info("Deleting project: {}", projectId);

        Project project = findProjectWithAccess(projectId, currentUser);

        // Stop project if running before deletion
        if (project.isRunning() || project.isDeploying()) {
            logger.info("Stopping project before deletion: {}", projectId);
            project.updateStatus(ProjectStatus.STOPPED);
            projectRepository.save(project);
        }

        projectRepository.delete(project);
        logger.info("Deleted project: {}", projectId);
    }

    /**
     * Deploy a project.
     */
    @PreAuthorize("hasPermission('project', 'deploy')")
    public ProjectResponse deployProject(String projectId, DeployProjectRequest request, User currentUser) {
        logger.info("Deploying project: {}", projectId);

        Project project = findProjectWithAccess(projectId, currentUser);

        // Check if project is already deploying or running
        if (project.isDeploying() && !request.isForceDeploy()) {
            throw new IllegalStateException("Project is already being deployed. Use force deploy to override.");
        }

        // Update project configuration if provided
        if (request.getConfig() != null) {
            project.setConfig(request.getConfig());
        }
        if (request.getBranding() != null) {
            project.setBranding(request.getBranding());
        }
        if (request.getLinks() != null) {
            project.setLinks(request.getLinks());
        }

        // Set status to deploying
        project.updateStatus(ProjectStatus.DEPLOYING);
        project.setErrors(null); // Clear previous errors

        Project savedProject = projectRepository.save(project);

        // Start async deployment process
        deployProjectAsync(savedProject.getId());

        logger.info("Started deployment for project: {}", projectId);
        return ProjectResponse.from(savedProject);
    }

    /**
     * Stop a project.
     */
    @PreAuthorize("hasPermission('project', 'deploy')")
    public ProjectResponse stopProject(String projectId, User currentUser) {
        logger.info("Stopping project: {}", projectId);

        Project project = findProjectWithAccess(projectId, currentUser);

        if (project.isStopped()) {
            throw new IllegalStateException("Project is already stopped");
        }

        project.updateStatus(ProjectStatus.STOPPED);
        project.setCurrentCCU(0);
        project.setProductionUrl(null);

        Project savedProject = projectRepository.save(project);
        logger.info("Stopped project: {}", projectId);

        return ProjectResponse.from(savedProject);
    }

    /**
     * Get project status.
     */
    @PreAuthorize("hasPermission('project', 'read')")
    public ProjectStatus getProjectStatus(String projectId, User currentUser) {
        logger.debug("Getting status for project: {}", projectId);

        Project project = findProjectWithAccess(projectId, currentUser);
        return project.getStatus();
    }

    /**
     * Search projects by keyword.
     */
    @PreAuthorize("hasPermission('project', 'read')")
    public Page<ProjectSummary> searchProjects(String organizationId, String keyword, Pageable pageable, User currentUser) {
        logger.debug("Searching projects in organization: {} with keyword: {}", organizationId, keyword);

        validateOrganizationAccess(organizationId, currentUser);

        return projectRepository.searchProjects(organizationId, keyword, pageable)
                .map(ProjectSummary::from);
    }

    /**
     * Get project statistics for an organization.
     */
    @PreAuthorize("hasPermission('project', 'read')")
    public ProjectStatistics getProjectStatistics(String organizationId, User currentUser) {
        logger.debug("Getting project statistics for organization: {}", organizationId);

        validateOrganizationAccess(organizationId, currentUser);

        long totalProjects = projectRepository.countByOrganizationId(organizationId);
        long runningProjects = projectRepository.countByOrganizationIdAndStatus(organizationId, ProjectStatus.RUNNING);
        long stoppedProjects = projectRepository.countByOrganizationIdAndStatus(organizationId, ProjectStatus.STOPPED);
        long deployingProjects = projectRepository.countByOrganizationIdAndStatus(organizationId, ProjectStatus.DEPLOYING);
        long errorProjects = projectRepository.countByOrganizationIdAndStatus(organizationId, ProjectStatus.ERROR);

        Double totalCreditsUsed = projectRepository.getTotalCreditsUsedByOrganization(organizationId);
        Long totalSessions = projectRepository.getTotalSessionsCountByOrganization(organizationId);

        return new ProjectStatistics(
            totalProjects,
            runningProjects,
            stoppedProjects,
            deployingProjects,
            errorProjects,
            totalCreditsUsed != null ? totalCreditsUsed : 0.0,
            totalSessions != null ? totalSessions : 0L
        );
    }

    /**
     * Async deployment simulation (in real implementation, this would interact with deployment service).
     */
    @Transactional
    public void deployProjectAsync(String projectId) {
        CompletableFuture.runAsync(() -> {
            try {
                // Simulate deployment time
                Thread.sleep(5000);

                Project project = projectRepository.findById(projectId).orElse(null);
                if (project != null && project.getStatus() == ProjectStatus.DEPLOYING) {
                    // Simulate successful deployment
                    project.updateStatus(ProjectStatus.RUNNING);
                    project.setProductionUrl("https://" + project.getId() + ".berryfi.app");
                    project.setLastDeployed(LocalDateTime.now());
                    projectRepository.save(project);

                    logger.info("Project deployed successfully: {}", projectId);
                }
            } catch (Exception e) {
                logger.error("Deployment failed for project: {}", projectId, e);
                
                Project project = projectRepository.findById(projectId).orElse(null);
                if (project != null) {
                    project.updateStatus(ProjectStatus.ERROR);
                    project.setErrors("Deployment failed: " + e.getMessage());
                    projectRepository.save(project);
                }
            }
        });
    }

    /**
     * Find project with access validation.
     */
    private Project findProjectWithAccess(String projectId, User currentUser) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));

        validateOrganizationAccess(project.getOrganizationId(), currentUser);
        return project;
    }

    /**
     * Track project usage in workspace.
     */
    private void trackProjectUsageInWorkspace(Project project, double credits) {
        if (project.getWorkspaceId() != null && !project.getWorkspaceId().trim().isEmpty()) {
            try {
                // Create a system user for workspace operations
                User systemUser = new User();
                systemUser.setId("system");
                systemUser.setOrganizationId(project.getOrganizationId());
                
                workspaceService.useCreditsFromWorkspace(project.getWorkspaceId(), credits, systemUser);
                logger.debug("Tracked {} credits usage for project {} in workspace {}", 
                           credits, project.getId(), project.getWorkspaceId());
            } catch (Exception e) {
                logger.warn("Failed to track usage in workspace: {}", e.getMessage());
            }
        }
    }

    /**
     * Increment session count in workspace.
     */
    private void incrementWorkspaceSession(Project project) {
        if (project.getWorkspaceId() != null && !project.getWorkspaceId().trim().isEmpty()) {
            try {
                // Create a system user for workspace operations
                User systemUser = new User();
                systemUser.setId("system");
                systemUser.setOrganizationId(project.getOrganizationId());
                
                workspaceService.incrementWorkspaceSession(project.getWorkspaceId(), systemUser);
                logger.debug("Incremented session count for workspace {}", project.getWorkspaceId());
            } catch (Exception e) {
                logger.warn("Failed to increment workspace session: {}", e.getMessage());
            }
        }
    }
    private void validateOrganizationAccess(String organizationId, User currentUser) {
        // In a real implementation, you would check if the user belongs to the organization
        // For now, we'll just log it
        logger.debug("Validating organization access for user: {} to organization: {}", 
                    currentUser.getId(), organizationId);
        
        // TODO: Implement proper organization access validation
        // This would typically check user's organization membership or workspace access
    }

    /**
     * DTO for project statistics.
     */
    public static class ProjectStatistics {
        private final long totalProjects;
        private final long runningProjects;
        private final long stoppedProjects;
        private final long deployingProjects;
        private final long errorProjects;
        private final double totalCreditsUsed;
        private final long totalSessions;

        public ProjectStatistics(long totalProjects, long runningProjects, long stoppedProjects, 
                               long deployingProjects, long errorProjects, double totalCreditsUsed, long totalSessions) {
            this.totalProjects = totalProjects;
            this.runningProjects = runningProjects;
            this.stoppedProjects = stoppedProjects;
            this.deployingProjects = deployingProjects;
            this.errorProjects = errorProjects;
            this.totalCreditsUsed = totalCreditsUsed;
            this.totalSessions = totalSessions;
        }

        // Getters
        public long getTotalProjects() { return totalProjects; }
        public long getRunningProjects() { return runningProjects; }
        public long getStoppedProjects() { return stoppedProjects; }
        public long getDeployingProjects() { return deployingProjects; }
        public long getErrorProjects() { return errorProjects; }
        public double getTotalCreditsUsed() { return totalCreditsUsed; }
        public long getTotalSessions() { return totalSessions; }
    }
}
