package com.berryfi.portal.service;

import com.berryfi.portal.dto.workspace.*;
import com.berryfi.portal.entity.Project;
import com.berryfi.portal.entity.User;
import com.berryfi.portal.entity.Workspace;
import com.berryfi.portal.enums.WorkspaceStatus;
import com.berryfi.portal.exception.ResourceNotFoundException;
import com.berryfi.portal.repository.ProjectRepository;
import com.berryfi.portal.repository.WorkspaceRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class for managing workspaces.
 */
@Service
@Transactional
public class WorkspaceService {

    private static final Logger logger = LoggerFactory.getLogger(WorkspaceService.class);

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private ProjectRepository projectRepository;

    /**
     * Create a new workspace.
     */
    @PreAuthorize("hasPermission('workspace', 'create')")
    public WorkspaceResponse createWorkspace(CreateWorkspaceRequest request, User currentUser) {
        logger.info("Creating new workspace: {} for organization: {}", request.getName(), currentUser.getOrganizationId());

        // Check if workspace name already exists in organization
        if (workspaceRepository.existsByNameAndOrganizationId(request.getName(), currentUser.getOrganizationId())) {
            throw new IllegalArgumentException("Workspace with name '" + request.getName() + "' already exists in this organization");
        }

        // Validate and fetch project information
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + request.getProjectId()));
        
        // Validate project belongs to the same organization
        if (!project.getOrganizationId().equals(currentUser.getOrganizationId())) {
            throw new IllegalArgumentException("Project does not belong to your organization");
        }

        Workspace workspace = new Workspace(
            request.getName(),
            request.getDescription(),
            currentUser.getOrganizationId(),
            request.getAdminEmail() != null ? request.getAdminEmail() : currentUser.getEmail(),
            request.getAdminName() != null ? request.getAdminName() : currentUser.getName(),
            request.getProjectId(),
            currentUser.getId()
        );

        // Set budget information if provided
        if (request.getMonthlyBudget() != null) {
            workspace.setMonthlyBudget(request.getMonthlyBudget());
        }
        
        if (request.getBudgetAction() != null) {
            workspace.setBudgetAction(request.getBudgetAction());
        }
        
        // Set initial credits if provided
        if (request.getGiftedCredits() != null && request.getGiftedCredits() > 0) {
            workspace.setGiftedCredits(request.getGiftedCredits());
            workspace.setRemainingGiftedCredits(request.getGiftedCredits());
        }
        
        if (request.getPurchasedCredits() != null && request.getPurchasedCredits() > 0) {
            workspace.setPurchasedCredits(request.getPurchasedCredits());
            workspace.setRemainingPurchasedCredits(request.getPurchasedCredits());
        }
        
        // Update current balance manually
        Double giftedBalance = workspace.getRemainingGiftedCredits() != null ? workspace.getRemainingGiftedCredits() : 0.0;
        Double purchasedBalance = workspace.getRemainingPurchasedCredits() != null ? workspace.getRemainingPurchasedCredits() : 0.0;
        workspace.setCurrentBalance(giftedBalance + purchasedBalance);

        Workspace savedWorkspace = workspaceRepository.save(workspace);
        
        // Set workspace ID on the project to complete the bidirectional relationship
        project.setWorkspaceId(savedWorkspace.getId());
        projectRepository.save(project);
        
        logger.info("Created workspace: {} with ID: {} and linked to project: {}", 
                   savedWorkspace.getName(), savedWorkspace.getId(), project.getName());

        return createWorkspaceResponse(savedWorkspace, project.getName());
    }

    /**
     * Get all workspaces for an organization.
     */
    @PreAuthorize("hasPermission('workspace', 'read')")
    public List<WorkspaceResponse> getWorkspaces(User currentUser) {
        logger.debug("Fetching workspaces for organization: {}", currentUser.getOrganizationId());

        List<Workspace> workspaces = workspaceRepository.findByOrganizationId(
            currentUser.getOrganizationId(), Pageable.unpaged()).getContent();

        return workspaces.stream()
                .map(this::createWorkspaceResponseWithProjectName)
                .toList();
    }

    /**
     * Get a specific workspace by ID.
     */
    @PreAuthorize("hasPermission('workspace', 'read')")
    public WorkspaceResponse getWorkspace(String workspaceId, User currentUser) {
        logger.debug("Fetching workspace: {}", workspaceId);

        Workspace workspace = findWorkspaceWithAccess(workspaceId, currentUser);
        return createWorkspaceResponseWithProjectName(workspace);
    }

    /**
     * Get workspace statistics for an organization.
     */
    @PreAuthorize("hasPermission('workspace', 'read')")
    public WorkspaceStatistics getWorkspaceStatistics(User currentUser) {
        logger.debug("Getting workspace statistics for organization: {}", currentUser.getOrganizationId());

        String organizationId = currentUser.getOrganizationId();

        long totalWorkspaces = workspaceRepository.countByOrganizationId(organizationId);
        long activeWorkspaces = workspaceRepository.countByOrganizationIdAndStatus(organizationId, WorkspaceStatus.ACTIVE);
        long suspendedWorkspaces = workspaceRepository.countByOrganizationIdAndStatus(organizationId, WorkspaceStatus.SUSPENDED);

        Double totalCreditsAllocated = workspaceRepository.getTotalCreditsAllocatedByOrganization(organizationId);
        Double totalCreditsUsed = workspaceRepository.getTotalCreditsUsedByOrganization(organizationId);
        Double averageUsagePerWorkspace = workspaceRepository.getAverageUsagePerWorkspace(organizationId);

        // Get top workspaces by usage
        List<Workspace> topWorkspaces = workspaceRepository.findTopWorkspacesByUsage(
            organizationId, PageRequest.of(0, 5));

        List<WorkspaceStatistics.TopWorkspace> topWorkspaceStats = topWorkspaces.stream()
                .map(w -> new WorkspaceStatistics.TopWorkspace(
                    w.getId(),
                    w.getName(),
                    w.getTotalCreditsUsed() != null ? w.getTotalCreditsUsed() : 0.0,
                    calculateUsagePercentage(w.getTotalCreditsUsed(), totalCreditsUsed)
                ))
                .toList();

        return new WorkspaceStatistics(
            totalWorkspaces,
            activeWorkspaces,
            suspendedWorkspaces,
            totalCreditsAllocated != null ? totalCreditsAllocated : 0.0,
            totalCreditsUsed != null ? totalCreditsUsed : 0.0,
            averageUsagePerWorkspace != null ? averageUsagePerWorkspace : 0.0,
            topWorkspaceStats
        );
    }

    /**
     * Update workspace-project association.
     */
    @PreAuthorize("hasPermission('workspace', 'update')")
    public void updateWorkspaceProject(String workspaceId, String projectId, User currentUser) {
        logger.info("Updating workspace {} with project: {}", workspaceId, projectId);

        Workspace workspace = findWorkspaceWithAccess(workspaceId, currentUser);
        workspace.setProjectId(projectId);

        workspaceRepository.save(workspace);
        logger.info("Updated workspace {} with project association", workspaceId);
    }

    /**
     * Add credits to workspace.
     */
    @PreAuthorize("hasPermission('workspace', 'manage_credits')")
    public WorkspaceResponse addCreditsToWorkspace(String workspaceId, double giftedCredits, double purchasedCredits, User currentUser) {
        logger.info("Adding credits to workspace {}: gifted={}, purchased={}", workspaceId, giftedCredits, purchasedCredits);

        Workspace workspace = findWorkspaceWithAccess(workspaceId, currentUser);

        if (giftedCredits > 0) {
            workspace.addGiftedCredits(giftedCredits);
        }
        if (purchasedCredits > 0) {
            workspace.addPurchasedCredits(purchasedCredits);
        }

        Workspace savedWorkspace = workspaceRepository.save(workspace);
        logger.info("Added credits to workspace {}", workspaceId);

        return createWorkspaceResponseWithProjectName(savedWorkspace);
    }

    /**
     * Use credits from workspace.
     */
    @PreAuthorize("hasPermission('workspace', 'use_credits')")
    public void useCreditsFromWorkspace(String workspaceId, double credits, User currentUser) {
        logger.debug("Using {} credits from workspace {}", credits, workspaceId);

        Workspace workspace = findWorkspaceWithAccess(workspaceId, currentUser);
        workspace.addCreditsUsed(credits);
        workspaceRepository.save(workspace);

        logger.debug("Used {} credits from workspace {}", credits, workspaceId);
    }

    /**
     * Increment session count for workspace.
     */
    @PreAuthorize("hasPermission('workspace', 'update')")
    public void incrementWorkspaceSession(String workspaceId, User currentUser) {
        logger.debug("Incrementing session count for workspace {}", workspaceId);

        Workspace workspace = findWorkspaceWithAccess(workspaceId, currentUser);
        workspace.incrementSessions();
        workspaceRepository.save(workspace);

        logger.debug("Incremented session count for workspace {}", workspaceId);
    }

    /**
     * Find workspace with access validation.
     */
    private Workspace findWorkspaceWithAccess(String workspaceId, User currentUser) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found: " + workspaceId));

        // Validate user has access to this workspace's organization
        if (!workspace.getOrganizationId().equals(currentUser.getOrganizationId())) {
            throw new ResourceNotFoundException("Workspace not found: " + workspaceId);
        }

        return workspace;
    }

    /**
     * Calculate usage percentage.
     */
    private double calculateUsagePercentage(Double workspaceUsage, Double totalUsage) {
        if (workspaceUsage == null || totalUsage == null || totalUsage == 0) {
            return 0.0;
        }
        return (workspaceUsage / totalUsage) * 100.0;
    }

    /**
     * Create WorkspaceResponse with project name lookup.
     */
    private WorkspaceResponse createWorkspaceResponseWithProjectName(Workspace workspace) {
        WorkspaceResponse response = WorkspaceResponse.from(workspace);
        
        // Fetch project name if projectId exists
        if (workspace.getProjectId() != null) {
            String projectName = projectRepository.findById(workspace.getProjectId())
                    .map(Project::getName)
                    .orElse("Unknown Project");
            response.setProjectName(projectName);
        }
        
        return response;
    }

    /**
     * Create WorkspaceResponse with known project name.
     */
    private WorkspaceResponse createWorkspaceResponse(Workspace workspace, String projectName) {
        WorkspaceResponse response = WorkspaceResponse.from(workspace);
        response.setProjectName(projectName);
        return response;
    }

    /**
     * DTO for workspace statistics.
     */
    public static class WorkspaceStatistics {
        private final long totalWorkspaces;
        private final long activeWorkspaces;
        private final long suspendedWorkspaces;
        private final double totalCreditsAllocated;
        private final double totalCreditsUsed;
        private final double averageUsagePerWorkspace;
        private final List<TopWorkspace> topWorkspaces;

        public WorkspaceStatistics(long totalWorkspaces, long activeWorkspaces, long suspendedWorkspaces,
                                 double totalCreditsAllocated, double totalCreditsUsed, 
                                 double averageUsagePerWorkspace, List<TopWorkspace> topWorkspaces) {
            this.totalWorkspaces = totalWorkspaces;
            this.activeWorkspaces = activeWorkspaces;
            this.suspendedWorkspaces = suspendedWorkspaces;
            this.totalCreditsAllocated = totalCreditsAllocated;
            this.totalCreditsUsed = totalCreditsUsed;
            this.averageUsagePerWorkspace = averageUsagePerWorkspace;
            this.topWorkspaces = topWorkspaces;
        }

        // Getters
        public long getTotalWorkspaces() { return totalWorkspaces; }
        public long getActiveWorkspaces() { return activeWorkspaces; }
        public long getSuspendedWorkspaces() { return suspendedWorkspaces; }
        public double getTotalCreditsAllocated() { return totalCreditsAllocated; }
        public double getTotalCreditsUsed() { return totalCreditsUsed; }
        public double getAverageUsagePerWorkspace() { return averageUsagePerWorkspace; }
        public List<TopWorkspace> getTopWorkspaces() { return topWorkspaces; }

        public static class TopWorkspace {
            private final String id;
            private final String name;
            private final double usage;
            private final double percentage;

            public TopWorkspace(String id, String name, double usage, double percentage) {
                this.id = id;
                this.name = name;
                this.usage = usage;
                this.percentage = percentage;
            }

            // Getters
            public String getId() { return id; }
            public String getName() { return name; }
            public double getUsage() { return usage; }
            public double getPercentage() { return percentage; }
        }
    }
}
