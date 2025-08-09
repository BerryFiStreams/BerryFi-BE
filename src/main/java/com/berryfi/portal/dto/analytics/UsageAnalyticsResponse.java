package com.berryfi.portal.dto.analytics;

import java.util.List;
import java.util.Map;

/**
 * Response DTO for usage analytics data.
 */
public class UsageAnalyticsResponse {
    private Map<String, Object> summary;
    private List<Map<String, Object>> chartData;
    private Map<String, Object> filters;
    private long totalSessions;
    private double totalCreditsUsed;
    private double averageSessionDuration;

    public UsageAnalyticsResponse() {}

    public UsageAnalyticsResponse(Map<String, Object> summary, List<Map<String, Object>> chartData, 
                                 Map<String, Object> filters, long totalSessions, double totalCreditsUsed, 
                                 double averageSessionDuration) {
        this.summary = summary;
        this.chartData = chartData;
        this.filters = filters;
        this.totalSessions = totalSessions;
        this.totalCreditsUsed = totalCreditsUsed;
        this.averageSessionDuration = averageSessionDuration;
    }

    // Getters and setters
    public Map<String, Object> getSummary() { return summary; }
    public void setSummary(Map<String, Object> summary) { this.summary = summary; }

    public List<Map<String, Object>> getChartData() { return chartData; }
    public void setChartData(List<Map<String, Object>> chartData) { this.chartData = chartData; }

    public Map<String, Object> getFilters() { return filters; }
    public void setFilters(Map<String, Object> filters) { this.filters = filters; }

    public long getTotalSessions() { return totalSessions; }
    public void setTotalSessions(long totalSessions) { this.totalSessions = totalSessions; }

    public double getTotalCreditsUsed() { return totalCreditsUsed; }
    public void setTotalCreditsUsed(double totalCreditsUsed) { this.totalCreditsUsed = totalCreditsUsed; }

    public double getAverageSessionDuration() { return averageSessionDuration; }
    public void setAverageSessionDuration(double averageSessionDuration) { this.averageSessionDuration = averageSessionDuration; }
}
