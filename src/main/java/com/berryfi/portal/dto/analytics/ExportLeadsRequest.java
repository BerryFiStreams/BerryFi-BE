package com.berryfi.portal.dto.analytics;

import java.util.List;

/**
 * Request DTO for exporting leads data.
 */
public class ExportLeadsRequest {
    private String dateRange;
    private String filterType;
    private String selectedFilter;
    private String projectId;
    private String exportFormat; // CSV, XLSX, PDF
    private List<String> fields;

    public ExportLeadsRequest() {}

    public ExportLeadsRequest(String dateRange, String filterType, String selectedFilter, String projectId,
                             String exportFormat, List<String> fields) {
        this.dateRange = dateRange;
        this.filterType = filterType;
        this.selectedFilter = selectedFilter;
        this.projectId = projectId;
        this.exportFormat = exportFormat;
        this.fields = fields;
    }

    // Getters and setters
    public String getDateRange() { return dateRange; }
    public void setDateRange(String dateRange) { this.dateRange = dateRange; }

    public String getFilterType() { return filterType; }
    public void setFilterType(String filterType) { this.filterType = filterType; }

    public String getSelectedFilter() { return selectedFilter; }
    public void setSelectedFilter(String selectedFilter) { this.selectedFilter = selectedFilter; }

    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }

    public String getExportFormat() { return exportFormat; }
    public void setExportFormat(String exportFormat) { this.exportFormat = exportFormat; }

    public List<String> getFields() { return fields; }
    public void setFields(List<String> fields) { this.fields = fields; }
}
