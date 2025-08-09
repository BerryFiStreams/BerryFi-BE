package com.berryfi.portal.dto.reports;

import java.util.List;
import java.util.Map;

/**
 * Response DTO for analytics reports.
 */
public class AnalyticsReportResponse {
    private String reportId;
    private String reportType;
    private String dateRange;
    private Map<String, Object> summary;
    private List<Map<String, Object>> chartData;
    private Map<String, Object> metadata;

    public AnalyticsReportResponse() {}

    public AnalyticsReportResponse(String reportId, String reportType, String dateRange,
                                  Map<String, Object> summary, List<Map<String, Object>> chartData,
                                  Map<String, Object> metadata) {
        this.reportId = reportId;
        this.reportType = reportType;
        this.dateRange = dateRange;
        this.summary = summary;
        this.chartData = chartData;
        this.metadata = metadata;
    }

    // Getters and setters
    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }

    public String getDateRange() { return dateRange; }
    public void setDateRange(String dateRange) { this.dateRange = dateRange; }

    public Map<String, Object> getSummary() { return summary; }
    public void setSummary(Map<String, Object> summary) { this.summary = summary; }

    public List<Map<String, Object>> getChartData() { return chartData; }
    public void setChartData(List<Map<String, Object>> chartData) { this.chartData = chartData; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
}
