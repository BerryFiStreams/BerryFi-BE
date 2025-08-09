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
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing usage sessions and analytics.
 */
@Service
@Transactional
public class UsageService {

    @Autowired
    private UsageSessionRepository usageSessionRepository;

    @Autowired
    private UsageAnalyticsRepository usageAnalyticsRepository;

    /**
     * Start a new usage session
     */
    public UsageSessionDto startSession(String organizationId, String projectId, 
                                      String projectName, String userId, String sessionId) {
        UsageSession session = new UsageSession();
        session.setId(UUID.randomUUID().toString());
        session.setOrganizationId(organizationId);
        session.setProjectId(projectId);
        session.setProjectName(projectName);
        session.setUserId(userId);
        session.setSessionId(sessionId);
        session.setStartedAt(LocalDateTime.now());

        session = usageSessionRepository.save(session);
        return convertToSessionDto(session);
    }

    /**
     * End a usage session
     */
    public UsageSessionDto endSession(String sessionId, Double creditsUsed) {
        Optional<UsageSession> sessionOpt = usageSessionRepository.findBySessionId(sessionId);
        if (sessionOpt.isEmpty()) {
            throw new RuntimeException("Session not found: " + sessionId);
        }

        UsageSession session = sessionOpt.get();
        session.endSession();
        session.setCreditsUsed(creditsUsed);

        session = usageSessionRepository.save(session);
        return convertToSessionDto(session);
    }

    /**
     * Update session metrics
     */
    public UsageSessionDto updateSession(String sessionId, String deviceType, String browser,
                                       String country, String city, String networkQuality,
                                       Double avgFps, Double avgBitrate, Integer errorCount) {
        Optional<UsageSession> sessionOpt = usageSessionRepository.findBySessionId(sessionId);
        if (sessionOpt.isEmpty()) {
            throw new RuntimeException("Session not found: " + sessionId);
        }

        UsageSession session = sessionOpt.get();
        if (deviceType != null) session.setDeviceType(deviceType);
        if (browser != null) session.setBrowser(browser);
        if (country != null) session.setCountry(country);
        if (city != null) session.setCity(city);
        if (networkQuality != null) session.setNetworkQuality(networkQuality);
        if (avgFps != null) session.setAvgFps(avgFps);
        if (avgBitrate != null) session.setAvgBitrate(avgBitrate);
        if (errorCount != null) session.setErrorCount(errorCount);

        session = usageSessionRepository.save(session);
        return convertToSessionDto(session);
    }

    /**
     * Get usage sessions for organization
     */
    public Page<UsageSessionDto> getUsageSessions(String organizationId, String projectId,
                                                String userId, LocalDateTime startDate,
                                                LocalDateTime endDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UsageSession> sessions;

        if (projectId != null) {
            sessions = usageSessionRepository.findByProjectIdOrderByStartedAtDesc(projectId, pageable);
        } else if (userId != null) {
            sessions = usageSessionRepository.findByUserIdOrderByStartedAtDesc(userId, pageable);
        } else if (startDate != null && endDate != null) {
            sessions = usageSessionRepository.findByOrganizationIdAndDateRange(
                    organizationId, startDate, endDate, pageable);
        } else {
            sessions = usageSessionRepository.findByOrganizationIdOrderByStartedAtDesc(
                    organizationId, pageable);
        }

        return sessions.map(this::convertToSessionDto);
    }

    /**
     * Get active sessions for organization
     */
    public List<UsageSessionDto> getActiveSessions(String organizationId) {
        List<UsageSession> sessions = usageSessionRepository.findActiveSessionsByOrganization(organizationId);
        return sessions.stream().map(this::convertToSessionDto).collect(Collectors.toList());
    }

    /**
     * Get usage statistics for organization within date range
     */
    public UsageAnalyticsDto getUsageStatistics(String organizationId, LocalDate startDate, 
                                              LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        // Calculate statistics
        Long totalSessions = usageSessionRepository.countSessionsByOrganizationAndDateRange(
                organizationId, startDateTime, endDateTime);
        
        Long totalDuration = usageSessionRepository.sumDurationByOrganizationAndDateRange(
                organizationId, startDateTime, endDateTime);
        
        Double totalCredits = usageSessionRepository.sumCreditsUsedByOrganizationAndDateRange(
                organizationId, startDateTime, endDateTime);
        
        Long uniqueUsers = usageSessionRepository.countUniqueUsersByOrganizationAndDateRange(
                organizationId, startDateTime, endDateTime);
        
        Double avgDuration = usageSessionRepository.getAverageSessionDurationByOrganizationAndDateRange(
                organizationId, startDateTime, endDateTime);

        UsageAnalyticsDto analytics = new UsageAnalyticsDto();
        analytics.setOrganizationId(organizationId);
        analytics.setDate(startDate);
        analytics.setTotalSessions(totalSessions.intValue());
        analytics.setTotalDurationSeconds(totalDuration);
        analytics.setTotalCreditsUsed(totalCredits != null ? totalCredits : 0.0);
        analytics.setUniqueUsers(uniqueUsers.intValue());
        analytics.setAvgSessionDuration(avgDuration);

        return analytics;
    }

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
     * Generate daily analytics for organization
     */
    public UsageAnalyticsDto generateDailyAnalytics(String organizationId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        // Check if analytics already exist
        Optional<UsageAnalytics> existing = usageAnalyticsRepository
                .findByOrganizationIdAndDate(organizationId, date);
        
        if (existing.isPresent()) {
            return convertToAnalyticsDto(existing.get());
        }

        // Generate new analytics
        Long totalSessions = usageSessionRepository.countSessionsByOrganizationAndDateRange(
                organizationId, startOfDay, endOfDay);
        
        Long totalDuration = usageSessionRepository.sumDurationByOrganizationAndDateRange(
                organizationId, startOfDay, endOfDay);
        
        Double totalCredits = usageSessionRepository.sumCreditsUsedByOrganizationAndDateRange(
                organizationId, startOfDay, endOfDay);
        
        Long uniqueUsers = usageSessionRepository.countUniqueUsersByOrganizationAndDateRange(
                organizationId, startOfDay, endOfDay);
        
        Double avgDuration = usageSessionRepository.getAverageSessionDurationByOrganizationAndDateRange(
                organizationId, startOfDay, endOfDay);
        
        Long totalErrors = usageSessionRepository.sumErrorsByOrganizationAndDateRange(
                organizationId, startOfDay, endOfDay);
        
        Double avgFps = usageSessionRepository.getAverageFpsByOrganizationAndDateRange(
                organizationId, startOfDay, endOfDay);
        
        Double avgBitrate = usageSessionRepository.getAverageBitrateByOrganizationAndDateRange(
                organizationId, startOfDay, endOfDay);

        // Get top metrics
        List<Object[]> topCountries = usageSessionRepository.getTopCountriesByOrganizationAndDateRange(
                organizationId, startOfDay, endOfDay);
        List<Object[]> topDevices = usageSessionRepository.getTopDeviceTypesByOrganizationAndDateRange(
                organizationId, startOfDay, endOfDay);
        List<Object[]> topBrowsers = usageSessionRepository.getTopBrowsersByOrganizationAndDateRange(
                organizationId, startOfDay, endOfDay);

        UsageAnalytics analytics = new UsageAnalytics();
        analytics.setId(UUID.randomUUID().toString());
        analytics.setOrganizationId(organizationId);
        analytics.setDate(date);
        analytics.setTotalSessions(totalSessions.intValue());
        analytics.setTotalDurationSeconds(totalDuration);
        analytics.setTotalCreditsUsed(totalCredits != null ? totalCredits : 0.0);
        analytics.setUniqueUsers(uniqueUsers.intValue());
        analytics.setAvgSessionDuration(avgDuration);
        analytics.setTotalErrors(totalErrors.intValue());
        analytics.setAvgFps(avgFps);
        analytics.setAvgBitrate(avgBitrate);

        // Set top metrics
        if (!topCountries.isEmpty()) {
            analytics.setTopCountry((String) topCountries.get(0)[0]);
        }
        if (!topDevices.isEmpty()) {
            analytics.setTopDeviceType((String) topDevices.get(0)[0]);
        }
        if (!topBrowsers.isEmpty()) {
            analytics.setTopBrowser((String) topBrowsers.get(0)[0]);
        }

        analytics = usageAnalyticsRepository.save(analytics);
        return convertToAnalyticsDto(analytics);
    }

    /**
     * Convert UsageSession entity to DTO
     */
    private UsageSessionDto convertToSessionDto(UsageSession session) {
        UsageSessionDto dto = new UsageSessionDto();
        dto.setId(session.getId());
        dto.setOrganizationId(session.getOrganizationId());
        dto.setWorkspaceId(session.getWorkspaceId());
        dto.setProjectId(session.getProjectId());
        dto.setProjectName(session.getProjectName());
        dto.setUserId(session.getUserId());
        dto.setSessionId(session.getSessionId());
        dto.setStartedAt(session.getStartedAt());
        dto.setEndedAt(session.getEndedAt());
        dto.setDurationSeconds(session.getDurationSeconds());
        dto.setCreditsUsed(session.getCreditsUsed());
        dto.setDeviceType(session.getDeviceType());
        dto.setBrowser(session.getBrowser());
        dto.setCountry(session.getCountry());
        dto.setCity(session.getCity());
        dto.setNetworkQuality(session.getNetworkQuality());
        dto.setAvgFps(session.getAvgFps());
        dto.setAvgBitrate(session.getAvgBitrate());
        dto.setErrorCount(session.getErrorCount());
        dto.setCreatedAt(session.getCreatedAt());
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
}
