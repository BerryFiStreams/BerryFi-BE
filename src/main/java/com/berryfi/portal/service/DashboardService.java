package com.berryfi.portal.service;

import com.berryfi.portal.dto.dashboard.*;
import java.util.Collections;
import com.berryfi.portal.entity.User;

import com.berryfi.portal.entity.Project;

import com.berryfi.portal.repository.ProjectRepository;
import com.berryfi.portal.repository.VmSessionRepository;
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
    private ProjectRepository projectRepository;

    @Autowired
    private VmSessionRepository vmSessionRepository;

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

                // Get recent 5 projects
        List<RecentProject> recentProjects = getRecentProjects(organizationId);
        
        return new DashboardResponse(summary, Collections.emptyList(), recentProjects);
    }

    /**
     * Get dashboard summary statistics.
     */
    private DashboardSummary getDashboardSummary(String organizationId) {
        // For now, use a simple count - could be enhanced to filter by organization
        Long totalSessions = vmSessionRepository.count();
        
        // Get credits as of today (current balance from latest billing transaction)
        Double currentCredits = getCurrentCreditsBalance(organizationId);
        
        return new DashboardSummary(
            totalSessions != null ? totalSessions.intValue() : 0,
            currentCredits != null ? currentCredits : 0.0,
            0 // No separate workspace count needed
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
     * Convert Project entity to RecentProject DTO.
     */
    private RecentProject convertToRecentProject(Project project) {
        return new RecentProject(
            project.getId(),
            project.getName(),
            project.getDescription(),
            null, // organizationId placeholder
            null, // organizationName placeholder
            project.getTotalCreditsUsed(),
            project.getSessionsCount(),
            project.getStatus(),
            project.getLastDeployed(),
            project.getCreatedAt(),
            project.getUpdatedAt()
        );
    }
}
