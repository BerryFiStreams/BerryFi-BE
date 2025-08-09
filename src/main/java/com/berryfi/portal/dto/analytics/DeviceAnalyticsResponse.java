package com.berryfi.portal.dto.analytics;

import java.util.List;
import java.util.Map;

/**
 * Response DTO for device analytics data.
 */
public class DeviceAnalyticsResponse {
    private Map<String, Object> summary;
    private List<Map<String, Object>> chartData;
    private Map<String, Object> filters;
    private Map<String, Long> deviceTypes;
    private Map<String, Long> browsers;
    private Map<String, Long> operatingSystems;
    private Map<String, Long> screenResolutions;

    public DeviceAnalyticsResponse() {}

    public DeviceAnalyticsResponse(Map<String, Object> summary, List<Map<String, Object>> chartData,
                                  Map<String, Object> filters, Map<String, Long> deviceTypes,
                                  Map<String, Long> browsers, Map<String, Long> operatingSystems,
                                  Map<String, Long> screenResolutions) {
        this.summary = summary;
        this.chartData = chartData;
        this.filters = filters;
        this.deviceTypes = deviceTypes;
        this.browsers = browsers;
        this.operatingSystems = operatingSystems;
        this.screenResolutions = screenResolutions;
    }

    // Getters and setters
    public Map<String, Object> getSummary() { return summary; }
    public void setSummary(Map<String, Object> summary) { this.summary = summary; }

    public List<Map<String, Object>> getChartData() { return chartData; }
    public void setChartData(List<Map<String, Object>> chartData) { this.chartData = chartData; }

    public Map<String, Object> getFilters() { return filters; }
    public void setFilters(Map<String, Object> filters) { this.filters = filters; }

    public Map<String, Long> getDeviceTypes() { return deviceTypes; }
    public void setDeviceTypes(Map<String, Long> deviceTypes) { this.deviceTypes = deviceTypes; }

    public Map<String, Long> getBrowsers() { return browsers; }
    public void setBrowsers(Map<String, Long> browsers) { this.browsers = browsers; }

    public Map<String, Long> getOperatingSystems() { return operatingSystems; }
    public void setOperatingSystems(Map<String, Long> operatingSystems) { this.operatingSystems = operatingSystems; }

    public Map<String, Long> getScreenResolutions() { return screenResolutions; }
    public void setScreenResolutions(Map<String, Long> screenResolutions) { this.screenResolutions = screenResolutions; }
}
