package com.berryfi.portal.dto.analytics;

import java.util.List;
import java.util.Map;

/**
 * Response DTO for lead analytics data.
 */
public class LeadAnalyticsResponse {
    private Map<String, Object> summary;
    private List<Map<String, Object>> chartData;
    private Map<String, Object> filters;
    private long totalLeads;
    private long convertedLeads;
    private double conversionRate;
    private Map<String, Long> leadsByStatus;

    public LeadAnalyticsResponse() {}

    public LeadAnalyticsResponse(Map<String, Object> summary, List<Map<String, Object>> chartData,
                                Map<String, Object> filters, long totalLeads, long convertedLeads,
                                double conversionRate, Map<String, Long> leadsByStatus) {
        this.summary = summary;
        this.chartData = chartData;
        this.filters = filters;
        this.totalLeads = totalLeads;
        this.convertedLeads = convertedLeads;
        this.conversionRate = conversionRate;
        this.leadsByStatus = leadsByStatus;
    }

    // Getters and setters
    public Map<String, Object> getSummary() { return summary; }
    public void setSummary(Map<String, Object> summary) { this.summary = summary; }

    public List<Map<String, Object>> getChartData() { return chartData; }
    public void setChartData(List<Map<String, Object>> chartData) { this.chartData = chartData; }

    public Map<String, Object> getFilters() { return filters; }
    public void setFilters(Map<String, Object> filters) { this.filters = filters; }

    public long getTotalLeads() { return totalLeads; }
    public void setTotalLeads(long totalLeads) { this.totalLeads = totalLeads; }

    public long getConvertedLeads() { return convertedLeads; }
    public void setConvertedLeads(long convertedLeads) { this.convertedLeads = convertedLeads; }

    public double getConversionRate() { return conversionRate; }
    public void setConversionRate(double conversionRate) { this.conversionRate = conversionRate; }

    public Map<String, Long> getLeadsByStatus() { return leadsByStatus; }
    public void setLeadsByStatus(Map<String, Long> leadsByStatus) { this.leadsByStatus = leadsByStatus; }
}
