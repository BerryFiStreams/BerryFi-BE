package com.berryfi.portal.service;

import com.berryfi.portal.dto.workspace.*;
import com.berryfi.portal.entity.User;
import com.berryfi.portal.entity.Workspace;
import com.berryfi.portal.enums.WorkspaceStatus;
import com.berryfi.portal.exception.ResourceNotFoundException;
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

        Workspace workspace = new Workspace(
            request.getName(),
            request.getDescription(),
            currentUser.getOrganizationId(),
            currentUser.getEmail(), // Use current user as admin initially
            currentUser.getName(),
            currentUser.getId()
        );

        Workspace savedWorkspace = workspaceRepository.save(workspace);
        logger.info("Created workspace: {} with ID: {}", savedWorkspace.getName(), savedWorkspace.getId());

        return WorkspaceResponse.from(savedWorkspace);
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
                .map(WorkspaceResponse::from)
                .toList();
    }

    /**
     * Get a specific workspace by ID.
     */
    @PreAuthorize("hasPermission('workspace', 'read')")
    public WorkspaceResponse getWorkspace(String workspaceId, User currentUser) {
        logger.debug("Fetching workspace: {}", workspaceId);

        Workspace workspace = findWorkspaceWithAccess(workspaceId, currentUser);
        return WorkspaceResponse.from(workspace);
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
    public void updateWorkspaceProject(String workspaceId, String projectId, String projectName, User currentUser) {
        logger.info("Updating workspace {} with project: {} ({})", workspaceId, projectId, projectName);

        Workspace workspace = findWorkspaceWithAccess(workspaceId, currentUser);
        workspace.setProjectId(projectId);
        workspace.setProjectName(projectName);

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

        return WorkspaceResponse.from(savedWorkspace);
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
