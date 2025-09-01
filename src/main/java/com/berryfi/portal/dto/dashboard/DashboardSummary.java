package com.berryfi.portal.dto.dashboard;

/**
 * DTO for dashboard summary containing user statistics.
 */
public class DashboardSummary {
    
    private Integer totalSessionsCount;
    private Double creditsAsOfToday;
    private Integer totalWorkspaces;
    
    public DashboardSummary() {}
    
    public DashboardSummary(Integer totalSessionsCount, Double creditsAsOfToday, Integer totalWorkspaces) {
        this.totalSessionsCount = totalSessionsCount;
        this.creditsAsOfToday = creditsAsOfToday;
        this.totalWorkspaces = totalWorkspaces;
    }
    
    // Getters and Setters
    public Integer getTotalSessionsCount() {
        return totalSessionsCount;
    }
    
    public void setTotalSessionsCount(Integer totalSessionsCount) {
        this.totalSessionsCount = totalSessionsCount;
    }
    
    public Double getCreditsAsOfToday() {
        return creditsAsOfToday;
    }
    
    public void setCreditsAsOfToday(Double creditsAsOfToday) {
        this.creditsAsOfToday = creditsAsOfToday;
    }
    
    public Integer getTotalWorkspaces() {
        return totalWorkspaces;
    }
    
    public void setTotalWorkspaces(Integer totalWorkspaces) {
        this.totalWorkspaces = totalWorkspaces;
    }
}
