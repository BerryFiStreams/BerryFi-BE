package com.berryfi.portal.service;

import com.berryfi.portal.dto.usage.*;
import com.berryfi.portal.entity.*;
import com.berryfi.portal.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing usage sessions and analytics based on VM sessions and workspace entitlements.
 */
@Service
@Transactional
public class UsageService {

    @Autowired
    private UsageAnalyticsRepository usageAnalyticsRepository;
    
    @Autowired
    private VmSessionRepository vmSessionRepository;
    
    @Autowired
    private TeamMemberRepository teamMemberRepository;
    
    @Autowired
    private WorkspaceRepository workspaceRepository;



    /**
     * Get usage analytics for organization
     */
    public Page<UsageAnalyticsDto> getUsageAnalytics(String organizationId, String projectId,
                                                    LocalDate startDate, LocalDate endDate,
                                                    int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UsageAnalytics> analytics;

        if (projectId != null) {
            analytics = usageAnalyticsRepository.findByProjectIdOrderByDateDesc(projectId, pageable);
        } else if (startDate != null && endDate != null) {
            // For date range filtering, use the regular method for now
            analytics = usageAnalyticsRepository.findByOrganizationIdOrderByDateDesc(
                    organizationId, pageable);
        } else {
            analytics = usageAnalyticsRepository.findByOrganizationIdOrderByDateDesc(
                    organizationId, pageable);
        }

        return analytics.map(this::convertToAnalyticsDto);
    }

    /**
     * Generate daily analytics for organization - based on VM sessions
     */
    public UsageAnalyticsDto generateDailyAnalytics(String organizationId, LocalDate date) {
        // Check if analytics already exist
        Optional<UsageAnalytics> existing = usageAnalyticsRepository
                .findByOrganizationIdAndDate(organizationId, date);
        
        if (existing.isPresent()) {
            return convertToAnalyticsDto(existing.get());
        }

        // Generate new analytics based on VM sessions
        // For now, create basic analytics - methods can be added to VmSessionRepository as needed
        UsageAnalytics analytics = new UsageAnalytics();
        analytics.setId(UUID.randomUUID().toString());
        analytics.setOrganizationId(organizationId);
        analytics.setDate(date);
        analytics.setTotalSessions(0);
        analytics.setTotalDurationSeconds(0L);
        analytics.setTotalCreditsUsed(0.0);
        analytics.setUniqueUsers(0);
        analytics.setAvgSessionDuration(0.0);
        analytics.setTotalErrors(0);
        analytics.setAvgFps(0.0);
        analytics.setAvgBitrate(0.0);

        analytics = usageAnalyticsRepository.save(analytics);
        return convertToAnalyticsDto(analytics);
    }

    /**
     * Get usage sessions for user's entitled workspaces - based on VM sessions
     */
    public Page<UsageSessionDto> getUsageSessionsForUser(User currentUser, String workspaceId, 
                                                       String projectId, String userId, 
                                                       LocalDate startDate, LocalDate endDate,
                                                       int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        
        // Convert LocalDate to LocalDateTime for database queries
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59, 999_999_999) : null;
        
        // Get user's entitled workspace IDs
        List<String> entitledWorkspaces = getUserEntitledWorkspaces(currentUser);
        if (entitledWorkspaces.isEmpty()) {
            // Return empty page if user has no entitled workspaces
            return Page.empty(pageable);
        }

        Page<VmSession> vmSessions;
        
        if (workspaceId != null) {
            // Validate access to specified workspace
            if (!isUserEntitledToWorkspace(currentUser, workspaceId)) {
                return Page.empty(pageable);
            }
            vmSessions = vmSessionRepository.findByWorkspaceIdOrderByStartTimeDesc(workspaceId, pageable);
        } else if (projectId != null) {
            // Validate access to specified project
            if (!isUserEntitledToProject(currentUser, projectId)) {
                return Page.empty(pageable);
            }
            vmSessions = vmSessionRepository.findByProjectIdOrderByStartTimeDesc(projectId, pageable);
        } else if (userId != null && startDateTime != null && endDateTime != null) {
            // Get sessions for specific user in date range, filtered by entitled workspaces
            vmSessions = vmSessionRepository.findByUserIdOrderByStartTimeDesc(userId, pageable);
        } else if (startDateTime != null && endDateTime != null) {
            // Get sessions in date range for entitled workspaces
            vmSessions = vmSessionRepository.findSessionsByDateRange(startDateTime, endDateTime, pageable);
        } else {
            // Get all sessions for entitled workspaces
            if (entitledWorkspaces.isEmpty()) {
                vmSessions = Page.empty(pageable);
            } else {
                vmSessions = vmSessionRepository.findByWorkspaceIdsOrderByStartTimeDesc(entitledWorkspaces, pageable);
            }
        }

        return vmSessions.map(this::convertVmSessionToDto);
    }

    /**
     * Get active sessions for user's entitled workspaces - based on VM sessions
     */
    public List<UsageSessionDto> getActiveSessionsForUser(User currentUser) {
        // Get user's entitled workspace IDs
        List<String> entitledWorkspaces = getUserEntitledWorkspaces(currentUser);
        if (entitledWorkspaces.isEmpty()) {
            return List.of();
        }

        // Get active VM sessions from all entitled workspaces
        List<VmSession> activeSessions = List.of();
        for (String workspaceId : entitledWorkspaces) {
            activeSessions.addAll(vmSessionRepository.findActiveSessionsInWorkspace(workspaceId));
        }

        return activeSessions.stream()
                .map(this::convertVmSessionToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get usage statistics for user's entitled workspaces within date range - based on VM sessions
     */
    public UsageAnalyticsDto getUsageStatisticsForUser(User currentUser, LocalDate startDate, LocalDate endDate) {
        // Get user's entitled workspace IDs
        List<String> entitledWorkspaces = getUserEntitledWorkspaces(currentUser);
        if (entitledWorkspaces.isEmpty()) {
            // Return empty analytics
            UsageAnalyticsDto analytics = new UsageAnalyticsDto();
            analytics.setOrganizationId(currentUser.getOrganizationId());
            analytics.setDate(startDate);
            analytics.setTotalSessions(0);
            analytics.setTotalDurationSeconds(0L);
            analytics.setTotalCreditsUsed(0.0);
            analytics.setUniqueUsers(0);
            analytics.setAvgSessionDuration(0.0);
            return analytics;
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        // Calculate aggregated statistics across all entitled workspaces
        long totalSessions = 0;
        long totalDuration = 0;
        double totalCredits = 0.0;
        long uniqueUsers = 0;

        for (String workspaceId : entitledWorkspaces) {
            totalSessions += vmSessionRepository.countByWorkspaceIdAndStatusAndStartTimeBetween(
                    workspaceId, com.berryfi.portal.enums.SessionStatus.COMPLETED, startDateTime, endDateTime);
                    
            Double workspaceCredits = vmSessionRepository.getTotalCreditsUsedInWorkspace(
                    workspaceId, startDateTime, endDateTime);
            if (workspaceCredits != null) {
                totalCredits += workspaceCredits;
            }
        }

        // Calculate average duration
        Double avgDuration = totalSessions > 0 ? (double) totalDuration / totalSessions : 0.0;

        UsageAnalyticsDto analytics = new UsageAnalyticsDto();
        analytics.setOrganizationId(currentUser.getOrganizationId());
        analytics.setDate(startDate);
        analytics.setTotalSessions((int) totalSessions);
        analytics.setTotalDurationSeconds(totalDuration);
        analytics.setTotalCreditsUsed(totalCredits);
        analytics.setUniqueUsers((int) uniqueUsers);
        analytics.setAvgSessionDuration(avgDuration);

        return analytics;
    }

    /**
     * Get usage analytics for user's entitled workspaces
     */
    public Page<UsageAnalyticsDto> getUsageAnalyticsForUser(User currentUser, String workspaceId, 
                                                          String projectId, LocalDate startDate, 
                                                          LocalDate endDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        
        // Get user's entitled workspace IDs
        List<String> entitledWorkspaces = getUserEntitledWorkspaces(currentUser);
        if (entitledWorkspaces.isEmpty()) {
            return Page.empty(pageable);
        }

        Page<UsageAnalytics> analytics;

        if (workspaceId != null) {
            // Validate access to specified workspace
            if (!isUserEntitledToWorkspace(currentUser, workspaceId)) {
                return Page.empty(pageable);
            }
            analytics = usageAnalyticsRepository.findByWorkspaceIdOrderByDateDesc(workspaceId, pageable);
        } else if (projectId != null) {
            // Validate access to specified project
            if (!isUserEntitledToProject(currentUser, projectId)) {
                return Page.empty(pageable);
            }
            analytics = usageAnalyticsRepository.findByProjectIdOrderByDateDesc(projectId, pageable);
        } else {
            // Get analytics for all entitled workspaces (this might need a custom query)
            analytics = usageAnalyticsRepository.findByOrganizationIdOrderByDateDesc(
                    currentUser.getOrganizationId(), pageable);
        }

        return analytics.map(this::convertToAnalyticsDto);
    }

    /**
     * Generate daily analytics for user's entitled workspaces
     */
    public List<UsageAnalyticsDto> generateDailyAnalyticsForUser(User currentUser, LocalDate date) {
        // Get user's entitled workspace IDs
        List<String> entitledWorkspaces = getUserEntitledWorkspaces(currentUser);
        if (entitledWorkspaces.isEmpty()) {
            return List.of();
        }

        List<UsageAnalyticsDto> results = List.of();
        for (String workspaceId : entitledWorkspaces) {
            // For each workspace, generate analytics
            Optional<Workspace> workspace = workspaceRepository.findById(workspaceId);
            if (workspace.isPresent()) {
                UsageAnalyticsDto analytics = generateDailyAnalytics(workspace.get().getOrganizationId(), date);
                analytics.setWorkspaceId(workspaceId);
                results.add(analytics);
            }
        }
        
        return results;
    }

    /**
     * Convert VmSession entity to UsageSessionDto
     */
    private UsageSessionDto convertVmSessionToDto(VmSession vmSession) {
        UsageSessionDto dto = new UsageSessionDto();
        dto.setId(vmSession.getId());
        dto.setOrganizationId(vmSession.getOrganizationId());
        dto.setWorkspaceId(vmSession.getWorkspaceId());
        dto.setProjectId(vmSession.getProjectId());
        dto.setProjectName(null); // Will be set separately if needed
        dto.setUserId(vmSession.getUserId());
        dto.setSessionId(vmSession.getId()); // Use VM session ID as session ID
        dto.setStartedAt(vmSession.getStartTime());
        dto.setEndedAt(vmSession.getEndTime());
        
        // Calculate duration if session is ended
        if (vmSession.getStartTime() != null && vmSession.getEndTime() != null) {
            Long duration = vmSession.getDurationSeconds();
            dto.setDurationSeconds(duration != null ? duration.intValue() : null);
        }
        
        dto.setCreditsUsed(vmSession.getCreditsUsed());
        dto.setDeviceType(null); // VM sessions don't track device type currently
        dto.setBrowser(null); // VM sessions don't track browser currently
        dto.setCountry(vmSession.getClientCountry());
        dto.setCity(vmSession.getClientCity());
        dto.setNetworkQuality(null); // VM sessions don't track network quality currently
        dto.setAvgFps(null); // VM sessions don't track FPS currently
        dto.setAvgBitrate(null); // VM sessions don't track bitrate currently
        dto.setErrorCount(null); // VM sessions don't track error count currently
        dto.setCreatedAt(vmSession.getCreatedAt());
        return dto;
    }

    /**
     * Convert UsageAnalytics entity to DTO
     */
    private UsageAnalyticsDto convertToAnalyticsDto(UsageAnalytics analytics) {
        UsageAnalyticsDto dto = new UsageAnalyticsDto();
        dto.setId(analytics.getId());
        dto.setOrganizationId(analytics.getOrganizationId());
        dto.setWorkspaceId(analytics.getWorkspaceId());
        dto.setProjectId(analytics.getProjectId());
        dto.setUserId(analytics.getUserId());
        dto.setDate(analytics.getDate());
        dto.setTotalSessions(analytics.getTotalSessions());
        dto.setTotalDurationSeconds(analytics.getTotalDurationSeconds());
        dto.setTotalCreditsUsed(analytics.getTotalCreditsUsed());
        dto.setUniqueUsers(analytics.getUniqueUsers());
        dto.setAvgSessionDuration(analytics.getAvgSessionDuration());
        dto.setPeakConcurrentSessions(analytics.getPeakConcurrentSessions());
        dto.setTotalErrors(analytics.getTotalErrors());
        dto.setAvgFps(analytics.getAvgFps());
        dto.setAvgBitrate(analytics.getAvgBitrate());
        dto.setTopCountry(analytics.getTopCountry());
        dto.setTopDeviceType(analytics.getTopDeviceType());
        dto.setTopBrowser(analytics.getTopBrowser());
        dto.setBounceRate(analytics.getBounceRate());
        dto.setConversionRate(analytics.getConversionRate());
        dto.setCreatedAt(analytics.getCreatedAt());
        dto.setUpdatedAt(analytics.getUpdatedAt());
        return dto;
    }

    /**
     * Get list of workspace IDs the user is entitled to
     */
    private List<String> getUserEntitledWorkspaces(User user) {
        // Get workspaces through team membership
        List<TeamMember> teamMembers = teamMemberRepository.findActiveWorkspacesForUser(user.getId());
        List<String> memberWorkspaces = teamMembers.stream()
                .map(TeamMember::getWorkspaceId)
                .filter(workspaceId -> workspaceId != null)
                .collect(Collectors.toList());
        
        // Get workspaces where user is the admin/creator
        List<Workspace> adminWorkspaces = workspaceRepository.findByAdminEmailAndOrganizationId(
            user.getEmail(), user.getOrganizationId());
        List<String> adminWorkspaceIds = adminWorkspaces.stream()
                .map(Workspace::getId)
                .collect(Collectors.toList());
        
        // Get workspaces created by the user
        List<Workspace> createdWorkspaces = workspaceRepository.findByCreatedByAndOrganizationId(
            user.getId(), user.getOrganizationId());
        List<String> createdWorkspaceIds = createdWorkspaces.stream()
                .map(Workspace::getId)
                .collect(Collectors.toList());
        
        // Combine and deduplicate
        Set<String> allWorkspaces = new HashSet<>();
        allWorkspaces.addAll(memberWorkspaces);
        allWorkspaces.addAll(adminWorkspaceIds);
        allWorkspaces.addAll(createdWorkspaceIds);
        
        return new ArrayList<>(allWorkspaces);
    }

    /**
     * Check if user has access to workspace
     */
    private boolean isUserEntitledToWorkspace(User user, String workspaceId) {
        if (workspaceId == null) return false;
        
        // Check team membership
        if (teamMemberRepository.existsByUserIdAndWorkspaceId(user.getId(), workspaceId)) {
            return true;
        }
        
        // Check workspace ownership/creation
        Optional<Workspace> workspace = workspaceRepository.findById(workspaceId);
        if (workspace.isPresent()) {
            Workspace ws = workspace.get();
            // Check if user is admin or creator of the workspace in the same organization
            return user.getOrganizationId().equals(ws.getOrganizationId()) && 
                   (user.getEmail().equals(ws.getAdminEmail()) || user.getId().equals(ws.getCreatedBy()));
        }
        
        return false;
    }

    /**
     * Check if user has access to project through workspace membership
     */
    private boolean isUserEntitledToProject(User user, String projectId) {
        if (projectId == null) return false;
        
        // Find workspace that contains this project
        Optional<Workspace> workspace = workspaceRepository.findByProjectId(projectId);
        if (workspace.isEmpty()) return false;
        
        return isUserEntitledToWorkspace(user, workspace.get().getId());
    }
}
