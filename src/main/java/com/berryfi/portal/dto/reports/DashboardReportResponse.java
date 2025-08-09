package com.berryfi.portal.dto.reports;

import java.util.List;
import java.util.Map;

/**
 * Response DTO for dashboard reports.
 */
public class DashboardReportResponse {
    private Map<String, Object> overview;
    private List<Map<String, Object>> widgets;
    private Map<String, Object> kpis;

    public DashboardReportResponse() {}

    public DashboardReportResponse(Map<String, Object> overview, List<Map<String, Object>> widgets,
                                  Map<String, Object> kpis) {
        this.overview = overview;
        this.widgets = widgets;
        this.kpis = kpis;
    }

    // Getters and setters
    public Map<String, Object> getOverview() { return overview; }
    public void setOverview(Map<String, Object> overview) { this.overview = overview; }

    public List<Map<String, Object>> getWidgets() { return widgets; }
    public void setWidgets(List<Map<String, Object>> widgets) { this.widgets = widgets; }

    public Map<String, Object> getKpis() { return kpis; }
    public void setKpis(Map<String, Object> kpis) { this.kpis = kpis; }
}
