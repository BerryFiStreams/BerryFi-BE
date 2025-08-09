package com.berryfi.portal.dto.reports;

import java.util.List;
import java.util.Map;

/**
 * Response DTO for report templates.
 */
public class ReportTemplatesResponse {
    private List<ReportTemplate> templates;

    public ReportTemplatesResponse() {}

    public ReportTemplatesResponse(List<ReportTemplate> templates) {
        this.templates = templates;
    }

    // Getters and setters
    public List<ReportTemplate> getTemplates() { return templates; }
    public void setTemplates(List<ReportTemplate> templates) { this.templates = templates; }

    /**
     * Template DTO for reports.
     */
    public static class ReportTemplate {
        private String id;
        private String name;
        private String description;
        private String category;
        private List<String> availableMetrics;
        private Map<String, Object> defaultParameters;

        public ReportTemplate() {}

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public List<String> getAvailableMetrics() { return availableMetrics; }
        public void setAvailableMetrics(List<String> availableMetrics) { this.availableMetrics = availableMetrics; }

        public Map<String, Object> getDefaultParameters() { return defaultParameters; }
        public void setDefaultParameters(Map<String, Object> defaultParameters) { this.defaultParameters = defaultParameters; }
    }
}
