package com.berryfi.portal.dto.dashboard;

/**
 * DTO for dashboard summary containing user statistics.
 */
public class DashboardSummary {
    
    private Integer totalSessionsCount;
    private Double creditsAsOfToday;
    private Integer totalOrganizations;
    
    public DashboardSummary() {}
    
    public DashboardSummary(Integer totalSessionsCount, Double creditsAsOfToday, Integer totalOrganizations) {
        this.totalSessionsCount = totalSessionsCount;
        this.creditsAsOfToday = creditsAsOfToday;
        this.totalOrganizations = totalOrganizations;
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
    
    public Integer getTotalOrganizations() {
        return totalOrganizations;
    }

    public void setTotalOrganizations(Integer totalOrganizations) {
        this.totalOrganizations = totalOrganizations;
    }
}
