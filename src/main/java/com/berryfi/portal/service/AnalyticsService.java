package com.berryfi.portal.service;

import com.berryfi.portal.dto.analytics.*;
import com.berryfi.portal.entity.VmSession;
import com.berryfi.portal.repository.VmSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for analytics operations.
 */
@Service
public class AnalyticsService {

    @Autowired
    private VmSessionRepository vmSessionRepository;

    /**
     * Get usage analytics data with optional filters.
     */
    public UsageAnalyticsResponse getUsageAnalytics(String dateRange, String filterType, 
                                                   String selectedFilter, String projectId) {
        // Parse date range
        LocalDateTime[] dateRangeParsed = parseDateRange(dateRange);
        LocalDateTime startDate = dateRangeParsed[0];
        LocalDateTime endDate = dateRangeParsed[1];
        
        // Get sessions based on filters
        List<VmSession> sessions;
        if ("project".equals(filterType) && selectedFilter != null) {
            sessions = vmSessionRepository.findSessionsByDateRange(startDate, endDate, org.springframework.data.domain.Pageable.unpaged())
                    .getContent().stream()
                    .filter(s -> selectedFilter.equals(s.getProjectId()))
                    .collect(Collectors.toList());
        } else {
            sessions = vmSessionRepository.findSessionsByDateRange(startDate, endDate, org.springframework.data.domain.Pageable.unpaged())
                    .getContent();
        }
        
        // Calculate metrics
        long totalSessions = sessions.size();
        double totalCreditsUsed = roundToTwoDecimals(sessions.stream()
                .mapToDouble(s -> s.getCreditsUsed() != null ? s.getCreditsUsed() : 0.0)
                .sum());
        
        double averageSessionDuration = roundToTwoDecimals(sessions.stream()
                .filter(s -> s.getDurationSeconds() != null)
                .mapToLong(VmSession::getDurationSeconds)
                .average()
                .orElse(0.0));
        
        long uniqueUsers = sessions.stream()
                .map(VmSession::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        
        // Group by date for chart data
        Map<LocalDate, List<VmSession>> sessionsByDate = sessions.stream()
                .filter(s -> s.getStartTime() != null)
                .collect(Collectors.groupingBy(s -> s.getStartTime().toLocalDate()));
        
        List<Map<String, Object>> chartData = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        // Generate data for all dates in range, not just dates with sessions
        LocalDate currentDate = startDate.toLocalDate();
        LocalDate lastDate = endDate.toLocalDate();
        
        while (!currentDate.isAfter(lastDate)) {
            Map<String, Object> dataPoint = new HashMap<>();
            dataPoint.put("date", currentDate.format(formatter));
            
            // Get sessions for this date, or use empty list if none
            List<VmSession> sessionsForDate = sessionsByDate.getOrDefault(currentDate, Collections.emptyList());
            dataPoint.put("sessions", sessionsForDate.size());
            double creditsForDate = sessionsForDate.stream()
                    .mapToDouble(s -> s.getCreditsUsed() != null ? s.getCreditsUsed() : 0.0)
                    .sum();
            dataPoint.put("credits", roundToTwoDecimals(creditsForDate));
            
            chartData.add(dataPoint);
            currentDate = currentDate.plusDays(1);
        }
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalSessions", totalSessions);
        summary.put("totalCreditsUsed", totalCreditsUsed);
        summary.put("averageSessionDuration", averageSessionDuration);
        summary.put("uniqueUsers", uniqueUsers);

        Map<String, Object> filters = new HashMap<>();
        filters.put("dateRange", dateRange);
        filters.put("filterType", filterType);
        filters.put("selectedFilter", selectedFilter);

        return new UsageAnalyticsResponse(summary, chartData, filters, totalSessions, totalCreditsUsed, averageSessionDuration);
    }
    
    /**
     * Parse date range string (e.g., "7d", "30d", "90d") to LocalDateTime range
     */
    private LocalDateTime[] parseDateRange(String dateRange) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate;
        
        if (dateRange == null || dateRange.isEmpty()) {
            startDate = endDate.minusDays(30); // Default 30 days
        } else if (dateRange.endsWith("d")) {
            int days = Integer.parseInt(dateRange.substring(0, dateRange.length() - 1));
            startDate = endDate.minusDays(days);
        } else if (dateRange.endsWith("m")) {
            int months = Integer.parseInt(dateRange.substring(0, dateRange.length() - 1));
            startDate = endDate.minusMonths(months);
        } else if (dateRange.endsWith("y")) {
            int years = Integer.parseInt(dateRange.substring(0, dateRange.length() - 1));
            startDate = endDate.minusYears(years);
        } else {
            startDate = endDate.minusDays(30); // Default 30 days
        }
        
        return new LocalDateTime[]{startDate, endDate};
    }
    
    /**
     * Round a double value to 2 decimal places
     */
    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    /**
     * Get lead analytics data with optional filters.
     */
    public LeadAnalyticsResponse getLeadAnalytics(String dateRange, String filterType, 
                                                 String selectedFilter, String projectId) {
        // Mock implementation
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalLeads", 456L);
        summary.put("convertedLeads", 123L);
        summary.put("conversionRate", 27.0);

        List<Map<String, Object>> chartData = new ArrayList<>();
        Map<String, Object> dataPoint = new HashMap<>();
        dataPoint.put("date", "2024-01-15");
        dataPoint.put("leads", 45);
        dataPoint.put("conversions", 12);
        chartData.add(dataPoint);

        Map<String, Object> filters = new HashMap<>();
        filters.put("dateRange", dateRange);
        filters.put("filterType", filterType);

        Map<String, Long> leadsByStatus = new HashMap<>();
        leadsByStatus.put("new", 123L);
        leadsByStatus.put("qualified", 89L);
        leadsByStatus.put("contacted", 67L);
        leadsByStatus.put("converted", 123L);

        return new LeadAnalyticsResponse(summary, chartData, filters, 456L, 123L, 27.0, leadsByStatus);
    }

    /**
     * Get geographic analytics data with optional filters.
     */
    public GeographicAnalyticsResponse getGeographicAnalytics(String dateRange, String filterType, 
                                                             String selectedFilter, String projectId) {
        // Mock implementation
        Map<String, Object> summary = new HashMap<>();
        summary.put("topCountry", "United States");
        summary.put("totalCountries", 45);

        List<Map<String, Object>> chartData = new ArrayList<>();
        
        Map<String, Object> filters = new HashMap<>();
        filters.put("dateRange", dateRange);

        Map<String, Long> usersByCountry = new HashMap<>();
        usersByCountry.put("United States", 145L);
        usersByCountry.put("India", 89L);
        usersByCountry.put("United Kingdom", 67L);

        Map<String, Long> usersByCity = new HashMap<>();
        usersByCity.put("New York", 45L);
        usersByCity.put("Mumbai", 34L);
        usersByCity.put("London", 29L);

        List<Map<String, Object>> topRegions = new ArrayList<>();

        return new GeographicAnalyticsResponse(summary, chartData, filters, usersByCountry, usersByCity, topRegions);
    }

    /**
     * Get device analytics data with optional filters.
     */
    public DeviceAnalyticsResponse getDeviceAnalytics(String dateRange, String filterType, 
                                                     String selectedFilter, String projectId) {
        // Mock implementation
        Map<String, Object> summary = new HashMap<>();
        summary.put("topDevice", "Desktop");
        summary.put("mobilePercentage", 35.2);

        List<Map<String, Object>> chartData = new ArrayList<>();
        
        Map<String, Object> filters = new HashMap<>();
        filters.put("dateRange", dateRange);

        Map<String, Long> deviceTypes = new HashMap<>();
        deviceTypes.put("Desktop", 156L);
        deviceTypes.put("Mobile", 89L);
        deviceTypes.put("Tablet", 23L);

        Map<String, Long> browsers = new HashMap<>();
        browsers.put("Chrome", 178L);
        browsers.put("Firefox", 45L);
        browsers.put("Safari", 34L);

        Map<String, Long> operatingSystems = new HashMap<>();
        operatingSystems.put("Windows", 134L);
        operatingSystems.put("macOS", 67L);
        operatingSystems.put("iOS", 45L);

        Map<String, Long> screenResolutions = new HashMap<>();
        screenResolutions.put("1920x1080", 89L);
        screenResolutions.put("1366x768", 56L);

        return new DeviceAnalyticsResponse(summary, chartData, filters, deviceTypes, browsers, operatingSystems, screenResolutions);
    }

    /**
     * Get network analytics data with optional filters.
     */
    public NetworkAnalyticsResponse getNetworkAnalytics(String dateRange, String filterType, 
                                                       String selectedFilter, String projectId) {
        // Mock implementation
        Map<String, Object> summary = new HashMap<>();
        summary.put("averageLatency", 45.2);
        summary.put("averageBandwidth", 25.6);

        List<Map<String, Object>> chartData = new ArrayList<>();
        
        Map<String, Object> filters = new HashMap<>();
        filters.put("dateRange", dateRange);

        Map<String, Double> networkPerformance = new HashMap<>();
        networkPerformance.put("latency", 45.2);
        networkPerformance.put("bandwidth", 25.6);
        networkPerformance.put("packetLoss", 0.2);

        Map<String, Long> connectionTypes = new HashMap<>();
        connectionTypes.put("Broadband", 145L);
        connectionTypes.put("Mobile", 67L);
        connectionTypes.put("WiFi", 89L);

        List<Map<String, Object>> latencyData = new ArrayList<>();

        return new NetworkAnalyticsResponse(summary, chartData, filters, networkPerformance, connectionTypes, latencyData);
    }

    /**
     * Get project usage analytics data with optional filters.
     */
    public ProjectUsageAnalyticsResponse getProjectUsageAnalytics(String dateRange, String filterType, 
                                                                 String selectedFilter, String projectId) {
        // Mock implementation
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalProjects", 12);
        summary.put("activeProjects", 8);

        List<Map<String, Object>> chartData = new ArrayList<>();
        
        Map<String, Object> filters = new HashMap<>();
        filters.put("dateRange", dateRange);

        Map<String, Double> projectUsage = new HashMap<>();
        projectUsage.put("river_castle_project", 2400.0);
        projectUsage.put("mountain_view_project", 1800.0);

        Map<String, Long> sessionsByProject = new HashMap<>();
        sessionsByProject.put("river_castle_project", 245L);
        sessionsByProject.put("mountain_view_project", 178L);

        List<Map<String, Object>> topProjects = new ArrayList<>();

        return new ProjectUsageAnalyticsResponse(summary, chartData, filters, projectUsage, sessionsByProject, topProjects);
    }

    /**
     * Export leads data.
     */
    public ExportResponse exportLeads(ExportLeadsRequest request) {
        // Mock implementation
        String fileName = "leads_export_" + System.currentTimeMillis() + "." + request.getExportFormat().toLowerCase();
        String downloadUrl = "/api/downloads/" + fileName;
        
        return new ExportResponse(downloadUrl, fileName, request.getExportFormat(), 1024L, "completed");
    }

    /**
     * Get lead journey analytics.
     */
    public LeadJourneyResponse getLeadJourney(String leadId) {
        // Mock implementation
        List<Map<String, Object>> touchpoints = new ArrayList<>();
        Map<String, Object> touchpoint = new HashMap<>();
        touchpoint.put("timestamp", "2024-01-15T10:30:00Z");
        touchpoint.put("action", "Page Visit");
        touchpoint.put("details", "Visited homepage");
        touchpoints.add(touchpoint);

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalTouchpoints", touchpoints.size());
        summary.put("firstTouch", "2024-01-15T10:30:00Z");
        summary.put("lastTouch", "2024-01-20T15:45:00Z");

        return new LeadJourneyResponse(leadId, "John", "Doe", "john.doe@example.com", 
                                     touchpoints, summary, "qualified", 65.0);
    }
}
