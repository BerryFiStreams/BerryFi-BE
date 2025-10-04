package com.berryfi.portal.dto.dashboard;

import java.util.List;

/**
 * DTO for dashboard response containing all dashboard data.
 */
public class DashboardResponse {
    
    private DashboardSummary summary;
    private List<RecentProject> recentProjects;
    
    public DashboardResponse() {}
    
    public DashboardResponse(DashboardSummary summary, List<RecentProject> recentProjects) {
        this.summary = summary;
        this.recentProjects = recentProjects;
    }
    
    // Getters and Setters
    public DashboardSummary getSummary() {
        return summary;
    }
    
    public void setSummary(DashboardSummary summary) {
        this.summary = summary;
    }
    

    
    public List<RecentProject> getRecentProjects() {
        return recentProjects;
    }
    
    public void setRecentProjects(List<RecentProject> recentProjects) {
        this.recentProjects = recentProjects;
    }
}
