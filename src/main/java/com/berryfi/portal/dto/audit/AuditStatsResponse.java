package com.berryfi.portal.dto.audit;

import java.util.Map;

/**
 * Response DTO for audit statistics.
 */
public class AuditStatsResponse {
    private long totalLogs;
    private long todayLogs;
    private long weekLogs;
    private long monthLogs;
    private Map<String, Long> actionCounts;
    private Map<String, Long> userActivityCounts;
    private Map<String, Long> resourceCounts;
    private Map<String, Long> dailyActivity;

    public AuditStatsResponse() {}

    public AuditStatsResponse(long totalLogs, long todayLogs, long weekLogs, long monthLogs,
                             Map<String, Long> actionCounts, Map<String, Long> userActivityCounts,
                             Map<String, Long> resourceCounts, Map<String, Long> dailyActivity) {
        this.totalLogs = totalLogs;
        this.todayLogs = todayLogs;
        this.weekLogs = weekLogs;
        this.monthLogs = monthLogs;
        this.actionCounts = actionCounts;
        this.userActivityCounts = userActivityCounts;
        this.resourceCounts = resourceCounts;
        this.dailyActivity = dailyActivity;
    }

    // Getters and setters
    public long getTotalLogs() { return totalLogs; }
    public void setTotalLogs(long totalLogs) { this.totalLogs = totalLogs; }

    public long getTodayLogs() { return todayLogs; }
    public void setTodayLogs(long todayLogs) { this.todayLogs = todayLogs; }

    public long getWeekLogs() { return weekLogs; }
    public void setWeekLogs(long weekLogs) { this.weekLogs = weekLogs; }

    public long getMonthLogs() { return monthLogs; }
    public void setMonthLogs(long monthLogs) { this.monthLogs = monthLogs; }

    public Map<String, Long> getActionCounts() { return actionCounts; }
    public void setActionCounts(Map<String, Long> actionCounts) { this.actionCounts = actionCounts; }

    public Map<String, Long> getUserActivityCounts() { return userActivityCounts; }
    public void setUserActivityCounts(Map<String, Long> userActivityCounts) { this.userActivityCounts = userActivityCounts; }

    public Map<String, Long> getResourceCounts() { return resourceCounts; }
    public void setResourceCounts(Map<String, Long> resourceCounts) { this.resourceCounts = resourceCounts; }

    public Map<String, Long> getDailyActivity() { return dailyActivity; }
    public void setDailyActivity(Map<String, Long> dailyActivity) { this.dailyActivity = dailyActivity; }
}
