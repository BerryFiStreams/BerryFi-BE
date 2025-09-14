package com.berryfi.portal.service;

import com.berryfi.portal.dto.dashboard.*;
import com.berryfi.portal.entity.User;
import com.berryfi.portal.entity.Workspace;
import com.berryfi.portal.entity.Project;
import com.berryfi.portal.repository.WorkspaceRepository;
import com.berryfi.portal.repository.ProjectRepository;
import com.berryfi.portal.repository.UsageSessionRepository;
import com.berryfi.portal.repository.BillingTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing dashboard operations.
 */
@Service
@Transactional(readOnly = true)
public class DashboardService {

    private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UsageSessionRepository usageSessionRepository;

    @Autowired
    private BillingTransactionRepository billingTransactionRepository;

    /**
     * Get dashboard data for the current user's organization.
     */
    @PreAuthorize("hasPermission('reports', 'view_dashboard')")
    public DashboardResponse getDashboardData(User currentUser) {
        logger.info("Getting dashboard data for user: {} in organization: {}", 
                   currentUser.getId(), currentUser.getOrganizationId());

        String organizationId = currentUser.getOrganizationId();

        // Get dashboard summary
        DashboardSummary summary = getDashboardSummary(organizationId);

        // Get recent 3 workspaces
        List<RecentWorkspace> recentWorkspaces = getRecentWorkspaces(organizationId);

        // Get recent 3 projects
        List<RecentProject> recentProjects = getRecentProjects(organizationId);

        return new DashboardResponse(summary, recentWorkspaces, recentProjects);
    }

    /**
     * Get dashboard summary statistics.
     */
    private DashboardSummary getDashboardSummary(String organizationId) {
        // Get total sessions count for the organization
        Long totalSessions = usageSessionRepository.countByOrganizationId(organizationId);
        
        // Get credits as of today (current balance from latest billing transaction)
        Double currentCredits = getCurrentCreditsBalance(organizationId);
        
        // Get total workspaces count for the organization
        Long totalWorkspaces = workspaceRepository.countByOrganizationId(organizationId);

        return new DashboardSummary(
            totalSessions != null ? totalSessions.intValue() : 0,
            currentCredits != null ? currentCredits : 0.0,
            totalWorkspaces != null ? totalWorkspaces.intValue() : 0
        );
    }

    /**
     * Get current credits balance from the latest transaction.
     */
    private Double getCurrentCreditsBalance(String organizationId) {
        return billingTransactionRepository.findFirstByOrganizationIdOrderByDateDesc(organizationId)
                .map(transaction -> transaction.getResultingBalance())
                .orElse(0.0);
    }

    /**
     * Get recent 3 workspaces with details.
     */
    private List<RecentWorkspace> getRecentWorkspaces(String organizationId) {
        Pageable pageable = PageRequest.of(0, 3);
        List<Workspace> workspaces = workspaceRepository.findByOrganizationIdOrderByUpdatedAtDesc(organizationId, pageable);

        return workspaces.stream()
                .map(this::convertToRecentWorkspace)
                .collect(Collectors.toList());
    }

    /**
     * Get recent 3 projects with details.
     */
    private List<RecentProject> getRecentProjects(String organizationId) {
        Pageable pageable = PageRequest.of(0, 3);
        List<Project> projects = projectRepository.findByOrganizationIdOrderByUpdatedAtDesc(organizationId, pageable);

        return projects.stream()
                .map(this::convertToRecentProject)
                .collect(Collectors.toList());
    }

    /**
     * Convert Workspace entity to RecentWorkspace DTO.
     */
    private RecentWorkspace convertToRecentWorkspace(Workspace workspace) {
        // Calculate available credits (gifted + purchased - used)
        Double creditsAvailable = calculateAvailableCredits(workspace);

        // Get project name if projectId exists
        String projectName = null;
        if (workspace.getProjectId() != null) {
            projectName = projectRepository.findById(workspace.getProjectId())
                    .map(Project::getName)
                    .orElse("Unknown Project");
        }

        return new RecentWorkspace(
            workspace.getId(),
            workspace.getName(),
            workspace.getDescription(),
            creditsAvailable,
            projectName,
            workspace.getTeamMemberCount(),
            workspace.getStatus(),
            workspace.getCreatedAt(),
            workspace.getUpdatedAt()
        );
    }

    /**
     * Calculate available credits for a workspace.
     */
    private Double calculateAvailableCredits(Workspace workspace) {
        Double remainingGifted = workspace.getRemainingGiftedCredits() != null ? workspace.getRemainingGiftedCredits() : 0.0;
        Double remainingPurchased = workspace.getRemainingPurchasedCredits() != null ? workspace.getRemainingPurchasedCredits() : 0.0;
        return remainingGifted + remainingPurchased;
    }

    /**
     * Convert Project entity to RecentProject DTO.
     */
    private RecentProject convertToRecentProject(Project project) {
        // Get workspace that belongs to this project
        String workspaceId = null;
        String workspaceName = null;
        Workspace workspace = workspaceRepository.findByProjectId(project.getId()).orElse(null);
        if (workspace != null) {
            workspaceId = workspace.getId();
            workspaceName = workspace.getName();
        }

        return new RecentProject(
            project.getId(),
            project.getName(),
            project.getDescription(),
            workspaceId,
            workspaceName,
            project.getTotalCreditsUsed(),
            project.getSessionsCount(),
            project.getStatus(),
            project.getLastDeployed(),
            project.getCreatedAt(),
            project.getUpdatedAt()
        );
    }
}
