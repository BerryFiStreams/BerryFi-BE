package com.berryfi.portal.dto.reports;

import java.util.List;
import java.util.Map;

/**
 * Request DTO for creating scheduled reports.
 */
public class CreateScheduledReportRequest {
    private String name;
    private String description;
    private String type;
    private String schedule; // cron expression
    private String organizationId;
    private List<String> recipients;
    private Map<String, Object> parameters;
    private String format;

    public CreateScheduledReportRequest() {}

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSchedule() { return schedule; }
    public void setSchedule(String schedule) { this.schedule = schedule; }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }

    public List<String> getRecipients() { return recipients; }
    public void setRecipients(List<String> recipients) { this.recipients = recipients; }

    public Map<String, Object> getParameters() { return parameters; }
    public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
}
