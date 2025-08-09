package com.berryfi.portal.dto.reports;

import java.util.List;
import java.util.Map;

/**
 * Request DTO for generating reports.
 */
public class GenerateReportRequest {
    private String reportType;
    private String organizationId;
    private String workspaceId;
    private String dateRange;
    private String startDate;
    private String endDate;
    private String format; // PDF, CSV, XLSX
    private List<String> metrics;
    private Map<String, Object> filters;

    public GenerateReportRequest() {}

    // Getters and setters
    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }

    public String getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(String workspaceId) { this.workspaceId = workspaceId; }

    public String getDateRange() { return dateRange; }
    public void setDateRange(String dateRange) { this.dateRange = dateRange; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }

    public List<String> getMetrics() { return metrics; }
    public void setMetrics(List<String> metrics) { this.metrics = metrics; }

    public Map<String, Object> getFilters() { return filters; }
    public void setFilters(Map<String, Object> filters) { this.filters = filters; }
}
