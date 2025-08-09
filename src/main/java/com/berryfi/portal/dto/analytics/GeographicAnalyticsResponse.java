package com.berryfi.portal.dto.analytics;

import java.util.List;
import java.util.Map;

/**
 * Response DTO for geographic analytics data.
 */
public class GeographicAnalyticsResponse {
    private Map<String, Object> summary;
    private List<Map<String, Object>> chartData;
    private Map<String, Object> filters;
    private Map<String, Long> usersByCountry;
    private Map<String, Long> usersByCity;
    private List<Map<String, Object>> topRegions;

    public GeographicAnalyticsResponse() {}

    public GeographicAnalyticsResponse(Map<String, Object> summary, List<Map<String, Object>> chartData,
                                     Map<String, Object> filters, Map<String, Long> usersByCountry,
                                     Map<String, Long> usersByCity, List<Map<String, Object>> topRegions) {
        this.summary = summary;
        this.chartData = chartData;
        this.filters = filters;
        this.usersByCountry = usersByCountry;
        this.usersByCity = usersByCity;
        this.topRegions = topRegions;
    }

    // Getters and setters
    public Map<String, Object> getSummary() { return summary; }
    public void setSummary(Map<String, Object> summary) { this.summary = summary; }

    public List<Map<String, Object>> getChartData() { return chartData; }
    public void setChartData(List<Map<String, Object>> chartData) { this.chartData = chartData; }

    public Map<String, Object> getFilters() { return filters; }
    public void setFilters(Map<String, Object> filters) { this.filters = filters; }

    public Map<String, Long> getUsersByCountry() { return usersByCountry; }
    public void setUsersByCountry(Map<String, Long> usersByCountry) { this.usersByCountry = usersByCountry; }

    public Map<String, Long> getUsersByCity() { return usersByCity; }
    public void setUsersByCity(Map<String, Long> usersByCity) { this.usersByCity = usersByCity; }

    public List<Map<String, Object>> getTopRegions() { return topRegions; }
    public void setTopRegions(List<Map<String, Object>> topRegions) { this.topRegions = topRegions; }
}
