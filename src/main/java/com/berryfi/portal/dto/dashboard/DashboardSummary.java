package com.berryfi.portal.dto.dashboard;

/**
 * DTO for dashboard summary containing user statistics.
 */
public class DashboardSummary {
    
    private Integer totalSessionsCount;
    private Double creditsAsOfToday;
    private Integer totalProjects;
    
    public DashboardSummary() {}
    
    public DashboardSummary(Integer totalSessionsCount, Double creditsAsOfToday, Integer totalProjects) {
        this.totalSessionsCount = totalSessionsCount;
        this.creditsAsOfToday = creditsAsOfToday;
        this.totalProjects = totalProjects;
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
    
    public Integer getTotalProjects() {
        return totalProjects;
    }

    public void setTotalProjects(Integer totalProjects) {
        this.totalProjects = totalProjects;
    }
}
