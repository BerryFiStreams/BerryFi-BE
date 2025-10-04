package com.berryfi.portal.dto.audit;

import java.util.Map;

/**
 * Response DTO for VM session audit statistics.
 * Provides organization-level statistics for VM session activities.
 */
public class VMSessionAuditStatsResponse {
    private long totalLogs;
    private Map<String, Long> actionCounts;
    private Map<String, Long> userActivityCounts;
    private Map<String, Long> vmInstanceCounts;
    private double totalCreditsUsed;

    public VMSessionAuditStatsResponse() {}

    public VMSessionAuditStatsResponse(long totalLogs, Map<String, Long> actionCounts, 
                                     Map<String, Long> userActivityCounts,
                                     Map<String, Long> vmInstanceCounts, double totalCreditsUsed) {
        this.totalLogs = totalLogs;
        this.actionCounts = actionCounts;
        this.userActivityCounts = userActivityCounts;
        this.vmInstanceCounts = vmInstanceCounts;
        this.totalCreditsUsed = totalCreditsUsed;
    }

    // Getters and Setters
    public long getTotalLogs() {
        return totalLogs;
    }

    public void setTotalLogs(long totalLogs) {
        this.totalLogs = totalLogs;
    }

    public Map<String, Long> getActionCounts() {
        return actionCounts;
    }

    public void setActionCounts(Map<String, Long> actionCounts) {
        this.actionCounts = actionCounts;
    }

    public Map<String, Long> getUserActivityCounts() {
        return userActivityCounts;
    }

    public void setUserActivityCounts(Map<String, Long> userActivityCounts) {
        this.userActivityCounts = userActivityCounts;
    }

    public Map<String, Long> getVmInstanceCounts() {
        return vmInstanceCounts;
    }

    public void setVmInstanceCounts(Map<String, Long> vmInstanceCounts) {
        this.vmInstanceCounts = vmInstanceCounts;
    }

    public double getTotalCreditsUsed() {
        return totalCreditsUsed;
    }

    public void setTotalCreditsUsed(double totalCreditsUsed) {
        this.totalCreditsUsed = totalCreditsUsed;
    }
}
