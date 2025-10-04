package com.berryfi.portal.dto.reports;

import java.util.Map;

/**
 * Request DTO for exporting reports.
 */
public class ExportReportRequest {
    private String reportId;
    private String reportType;
    private String format; // PDF, CSV, XLSX
    private String organizationId;
    private String dateRange;
    private Map<String, Object> parameters;

    public ExportReportRequest() {}

    // Getters and setters
    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }

    public String getDateRange() { return dateRange; }
    public void setDateRange(String dateRange) { this.dateRange = dateRange; }

    public Map<String, Object> getParameters() { return parameters; }
    public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }
}
