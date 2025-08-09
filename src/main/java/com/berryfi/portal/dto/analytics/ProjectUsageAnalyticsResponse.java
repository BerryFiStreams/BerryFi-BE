package com.berryfi.portal.dto.analytics;

import java.util.List;
import java.util.Map;

/**
 * Response DTO for project usage analytics data.
 */
public class ProjectUsageAnalyticsResponse {
    private Map<String, Object> summary;
    private List<Map<String, Object>> chartData;
    private Map<String, Object> filters;
    private Map<String, Double> projectUsage;
    private Map<String, Long> sessionsByProject;
    private List<Map<String, Object>> topProjects;

    public ProjectUsageAnalyticsResponse() {}

    public ProjectUsageAnalyticsResponse(Map<String, Object> summary, List<Map<String, Object>> chartData,
                                        Map<String, Object> filters, Map<String, Double> projectUsage,
                                        Map<String, Long> sessionsByProject, List<Map<String, Object>> topProjects) {
        this.summary = summary;
        this.chartData = chartData;
        this.filters = filters;
        this.projectUsage = projectUsage;
        this.sessionsByProject = sessionsByProject;
        this.topProjects = topProjects;
    }

    // Getters and setters
    public Map<String, Object> getSummary() { return summary; }
    public void setSummary(Map<String, Object> summary) { this.summary = summary; }

    public List<Map<String, Object>> getChartData() { return chartData; }
    public void setChartData(List<Map<String, Object>> chartData) { this.chartData = chartData; }

    public Map<String, Object> getFilters() { return filters; }
    public void setFilters(Map<String, Object> filters) { this.filters = filters; }

    public Map<String, Double> getProjectUsage() { return projectUsage; }
    public void setProjectUsage(Map<String, Double> projectUsage) { this.projectUsage = projectUsage; }

    public Map<String, Long> getSessionsByProject() { return sessionsByProject; }
    public void setSessionsByProject(Map<String, Long> sessionsByProject) { this.sessionsByProject = sessionsByProject; }

    public List<Map<String, Object>> getTopProjects() { return topProjects; }
    public void setTopProjects(List<Map<String, Object>> topProjects) { this.topProjects = topProjects; }
}
