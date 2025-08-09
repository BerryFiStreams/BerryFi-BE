package com.berryfi.portal.dto.reports;

import java.util.List;
import java.util.Map;

/**
 * Request DTO for updating scheduled reports.
 */
public class UpdateScheduledReportRequest {
    private String name;
    private String description;
    private String schedule;
    private List<String> recipients;
    private Map<String, Object> parameters;
    private String status;

    public UpdateScheduledReportRequest() {}

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSchedule() { return schedule; }
    public void setSchedule(String schedule) { this.schedule = schedule; }

    public List<String> getRecipients() { return recipients; }
    public void setRecipients(List<String> recipients) { this.recipients = recipients; }

    public Map<String, Object> getParameters() { return parameters; }
    public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
