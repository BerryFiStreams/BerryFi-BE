package com.berryfi.portal.dto.reports;

import com.berryfi.portal.dto.dashboard.DashboardSummary;
import com.berryfi.portal.dto.dashboard.RecentProject;
import com.berryfi.portal.dto.dashboard.RecentWorkspace;

import java.util.List;

/**
 * Response DTO for dashboard reports.
 */
public class DashboardReportResponse {
    private DashboardSummary summary;
    private List<RecentWorkspace> recentWorkspaces;
    private List<RecentProject> recentProjects;

    public DashboardReportResponse() {}

    public DashboardReportResponse(DashboardSummary summary, List<RecentWorkspace> recentWorkspaces,
                                  List<RecentProject> recentProjects) {
        this.summary = summary;
        this.recentWorkspaces = recentWorkspaces;
        this.recentProjects = recentProjects;
    }

    // Getters and setters
    public DashboardSummary getSummary() { return summary; }
    public void setSummary(DashboardSummary summary) { this.summary = summary; }

    public List<RecentWorkspace> getRecentWorkspaces() { return recentWorkspaces; }
    public void setRecentWorkspaces(List<RecentWorkspace> recentWorkspaces) { this.recentWorkspaces = recentWorkspaces; }

    public List<RecentProject> getRecentProjects() { return recentProjects; }
    public void setRecentProjects(List<RecentProject> recentProjects) { this.recentProjects = recentProjects; }
}
