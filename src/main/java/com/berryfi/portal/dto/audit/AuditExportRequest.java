package com.berryfi.portal.dto.audit;

import java.util.List;

/**
 * Request DTO for exporting audit logs.
 */
public class AuditExportRequest {
    private String organizationId;
    private String userId;
    private String action;
    private String resource;
    private String startDate;
    private String endDate;
    private String format; // CSV, XLSX, PDF
    private List<String> fields;

    public AuditExportRequest() {}

    public AuditExportRequest(String organizationId, String userId, String action, String resource,
                             String startDate, String endDate, String format, List<String> fields) {
        this.organizationId = organizationId;
        this.userId = userId;
        this.action = action;
        this.resource = resource;
        this.startDate = startDate;
        this.endDate = endDate;
        this.format = format;
        this.fields = fields;
    }

    // Getters and setters
    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getResource() { return resource; }
    public void setResource(String resource) { this.resource = resource; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }

    public List<String> getFields() { return fields; }
    public void setFields(List<String> fields) { this.fields = fields; }
}
