package com.berryfi.portal.dto.analytics;

import java.util.List;
import java.util.Map;

/**
 * Response DTO for network analytics data.
 */
public class NetworkAnalyticsResponse {
    private Map<String, Object> summary;
    private List<Map<String, Object>> chartData;
    private Map<String, Object> filters;
    private Map<String, Double> networkPerformance;
    private Map<String, Long> connectionTypes;
    private List<Map<String, Object>> latencyData;

    public NetworkAnalyticsResponse() {}

    public NetworkAnalyticsResponse(Map<String, Object> summary, List<Map<String, Object>> chartData,
                                   Map<String, Object> filters, Map<String, Double> networkPerformance,
                                   Map<String, Long> connectionTypes, List<Map<String, Object>> latencyData) {
        this.summary = summary;
        this.chartData = chartData;
        this.filters = filters;
        this.networkPerformance = networkPerformance;
        this.connectionTypes = connectionTypes;
        this.latencyData = latencyData;
    }

    // Getters and setters
    public Map<String, Object> getSummary() { return summary; }
    public void setSummary(Map<String, Object> summary) { this.summary = summary; }

    public List<Map<String, Object>> getChartData() { return chartData; }
    public void setChartData(List<Map<String, Object>> chartData) { this.chartData = chartData; }

    public Map<String, Object> getFilters() { return filters; }
    public void setFilters(Map<String, Object> filters) { this.filters = filters; }

    public Map<String, Double> getNetworkPerformance() { return networkPerformance; }
    public void setNetworkPerformance(Map<String, Double> networkPerformance) { this.networkPerformance = networkPerformance; }

    public Map<String, Long> getConnectionTypes() { return connectionTypes; }
    public void setConnectionTypes(Map<String, Long> connectionTypes) { this.connectionTypes = connectionTypes; }

    public List<Map<String, Object>> getLatencyData() { return latencyData; }
    public void setLatencyData(List<Map<String, Object>> latencyData) { this.latencyData = latencyData; }
}
