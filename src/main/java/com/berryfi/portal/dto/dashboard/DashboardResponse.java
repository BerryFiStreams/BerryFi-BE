package com.berryfi.portal.dto.dashboard;

import java.util.List;

/**
 * DTO for dashboard response containing all dashboard data.
 */
public class DashboardResponse {
    
    private DashboardSummary summary;
    private List<RecentWorkspace> recentWorkspaces;
    private List<RecentProject> recentProjects;
    
    public DashboardResponse() {}
    
    public DashboardResponse(DashboardSummary summary, List<RecentWorkspace> recentWorkspaces, 
                           List<RecentProject> recentProjects) {
        this.summary = summary;
        this.recentWorkspaces = recentWorkspaces;
        this.recentProjects = recentProjects;
    }
    
    // Getters and Setters
    public DashboardSummary getSummary() {
        return summary;
    }
    
    public void setSummary(DashboardSummary summary) {
        this.summary = summary;
    }
    
    public List<RecentWorkspace> getRecentWorkspaces() {
        return recentWorkspaces;
    }
    
    public void setRecentWorkspaces(List<RecentWorkspace> recentWorkspaces) {
        this.recentWorkspaces = recentWorkspaces;
    }
    
    public List<RecentProject> getRecentProjects() {
        return recentProjects;
    }
    
    public void setRecentProjects(List<RecentProject> recentProjects) {
        this.recentProjects = recentProjects;
    }
}
