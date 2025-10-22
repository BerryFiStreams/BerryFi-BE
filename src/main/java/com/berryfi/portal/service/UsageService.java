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
 * Service for managing usage sessions and analytics based on VM sessions.
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
    private ProjectRepository projectRepository;



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
     * Get usage sessions for user's entitled organizations - based on VM sessions
     */
    public Page<UsageSessionDto> getUsageSessionsForUser(User currentUser, 
                                                       String projectId, String userId, 
                                                       LocalDate startDate, LocalDate endDate,
                                                       int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        
        // Convert LocalDate to LocalDateTime for database queries
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59, 999_999_999) : null;
        
        // Check organization access
        if (currentUser.getOrganizationId() == null) {
            return Page.empty(pageable);
        }

        Page<VmSession> vmSessions = Page.empty(pageable);
        
        if (projectId != null) {
            // Validate access to specified project
            if (!isUserEntitledToProject(currentUser, projectId)) {
                return Page.empty(pageable);
            }
            vmSessions = vmSessionRepository.findByProjectIdOrderByStartTimeDesc(projectId, pageable);
        } else if (userId != null && startDateTime != null && endDateTime != null) {
            // Get sessions for specific user in date range
            vmSessions = vmSessionRepository.findByUserIdOrderByStartTimeDesc(userId, pageable);
        } else if (startDateTime != null && endDateTime != null) {
            // Get sessions in date range for organization
            vmSessions = vmSessionRepository.findSessionsByDateRange(startDateTime, endDateTime, pageable);
        } else {
            // Get all sessions for user (organization-based filtering will be added later)\n            vmSessions = vmSessionRepository.findByUserIdOrderByStartTimeDesc(currentUser.getId(), pageable);
        }

        return vmSessions.map(this::convertVmSessionToDto);
    }

    /**
     * Get active sessions for user's organization - based on VM sessions
     */
    public List<UsageSessionDto> getActiveSessionsForUser(User currentUser) {
        // Check organization access
        if (currentUser.getOrganizationId() == null) {
            return List.of();
        }

        // Get active VM sessions (simplified - use all active sessions for now)
        List<VmSession> activeSessions = vmSessionRepository.findByStatusOrderByStartTimeDesc(com.berryfi.portal.enums.SessionStatus.ACTIVE);

        return activeSessions.stream()
                .map(this::convertVmSessionToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get usage statistics for user's organization within date range - based on VM sessions
     */
    public UsageAnalyticsDto getUsageStatisticsForUser(User currentUser, LocalDate startDate, LocalDate endDate) {
        // Check organization access
        if (currentUser.getOrganizationId() == null) {
            // Return empty analytics
            UsageAnalyticsDto analytics = new UsageAnalyticsDto();
            analytics.setDate(startDate);
            analytics.setTotalSessions(0);
            analytics.setTotalDurationSeconds(0L);
            analytics.setTotalCreditsUsed(0.0);
            analytics.setUniqueUsers(0);
            analytics.setAvgSessionDuration(0.0);
            return analytics;
        }

        // Calculate basic statistics (organization-specific methods will be added later)
        // For now, return minimal stats
        long totalSessions = 0;
        Double totalCredits = 0.0;
        long totalDuration = 0;

        // Calculate average duration
        Double avgDuration = totalSessions > 0 ? (double) totalDuration / totalSessions : 0.0;

        UsageAnalyticsDto analytics = new UsageAnalyticsDto();
        analytics.setOrganizationId(currentUser.getOrganizationId());
        analytics.setDate(startDate);
        analytics.setTotalSessions((int) totalSessions);
        analytics.setTotalDurationSeconds(totalDuration);
        analytics.setTotalCreditsUsed(totalCredits != null ? totalCredits : 0.0);
        analytics.setUniqueUsers(0); // TODO: Add method to count unique users in organization
        analytics.setAvgSessionDuration(avgDuration);

        return analytics;
    }

    /**
     * Get usage analytics for user's entitled organizations
     */
    public Page<UsageAnalyticsDto> getUsageAnalyticsForUser(User currentUser, 
                                                          String projectId, LocalDate startDate, 
                                                          LocalDate endDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        
        // Check organization access
        if (currentUser.getOrganizationId() == null) {
            return Page.empty(pageable);
        }

        Page<UsageAnalytics> analytics;

        if (projectId != null) {
            // Validate access to specified project
            if (!isUserEntitledToProject(currentUser, projectId)) {
                return Page.empty(pageable);
            }
            analytics = usageAnalyticsRepository.findByProjectIdOrderByDateDesc(projectId, pageable);
        } else {
            // Get analytics for organization
            analytics = usageAnalyticsRepository.findByOrganizationIdOrderByDateDesc(
                    currentUser.getOrganizationId(), pageable);
        }

        return analytics.map(this::convertToAnalyticsDto);
    }

    /**
     * Generate daily analytics for user's entitled organizations
     */
    public List<UsageAnalyticsDto> generateDailyAnalyticsForUser(User currentUser, LocalDate date) {
        // Get user's organization ID directly
        if (currentUser.getOrganizationId() == null) {
            return List.of();
        }

        // Generate analytics for the user's organization
        UsageAnalyticsDto analytics = generateDailyAnalytics(currentUser.getOrganizationId(), date);
        return List.of(analytics);
    }

    /**
     * Convert VmSession entity to UsageSessionDto
     */
    private UsageSessionDto convertVmSessionToDto(VmSession vmSession) {
        UsageSessionDto dto = new UsageSessionDto();
        dto.setId(vmSession.getId());
        dto.setOrganizationId(vmSession.getOrganizationId());
        dto.setProjectId(vmSession.getProjectId());
        
        // Fetch project name from Project entity
        String projectName = null;
        if (vmSession.getProjectId() != null) {
            projectName = projectRepository.findById(vmSession.getProjectId())
                    .map(Project::getName)
                    .orElse(null);
        }
        dto.setProjectName(projectName);
        
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
        
        // Extract device type and browser from user agent
        String userAgent = vmSession.getUserAgent();
        dto.setDeviceType(extractDeviceType(userAgent));
        dto.setBrowser(extractBrowser(userAgent));
        
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
     * Extract browser name from user agent string
     */
    private String extractBrowser(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return null;
        }
        
        userAgent = userAgent.toLowerCase();
        
        // Check for browsers in order of specificity
        if (userAgent.contains("edg/") || userAgent.contains("edge")) {
            return "Edge";
        } else if (userAgent.contains("opr/") || userAgent.contains("opera")) {
            return "Opera";
        } else if (userAgent.contains("chrome") && !userAgent.contains("edg")) {
            return "Chrome";
        } else if (userAgent.contains("safari") && !userAgent.contains("chrome")) {
            return "Safari";
        } else if (userAgent.contains("firefox")) {
            return "Firefox";
        } else if (userAgent.contains("msie") || userAgent.contains("trident")) {
            return "Internet Explorer";
        }
        
        return "Other";
    }
    
    /**
     * Extract device type from user agent string
     */
    private String extractDeviceType(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return null;
        }
        
        userAgent = userAgent.toLowerCase();
        
        // Check for mobile devices
        if (userAgent.contains("mobile") || userAgent.contains("android") || 
            userAgent.contains("iphone") || userAgent.contains("ipod")) {
            return "Mobile";
        } else if (userAgent.contains("tablet") || userAgent.contains("ipad")) {
            return "Tablet";
        }
        
        return "Desktop";
    }

    /**
     * Convert UsageAnalytics entity to DTO
     */
    private UsageAnalyticsDto convertToAnalyticsDto(UsageAnalytics analytics) {
        UsageAnalyticsDto dto = new UsageAnalyticsDto();
        dto.setId(analytics.getId());
        dto.setOrganizationId(analytics.getOrganizationId());
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
     * Check if user has access to project (organization-based)
     */
    private boolean isUserEntitledToProject(User user, String projectId) {
        // In organization-based architecture, users have access to projects in their organization
        return user.getOrganizationId() != null && projectId != null;
    }
}
