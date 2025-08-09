package com.berryfi.portal.dto.reports;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for scheduled reports list.
 */
public class ScheduledReportsResponse {
    private List<ScheduledReportSummary> reports;
    private long totalCount;

    public ScheduledReportsResponse() {}

    public ScheduledReportsResponse(List<ScheduledReportSummary> reports, long totalCount) {
        this.reports = reports;
        this.totalCount = totalCount;
    }

    // Getters and setters
    public List<ScheduledReportSummary> getReports() { return reports; }
    public void setReports(List<ScheduledReportSummary> reports) { this.reports = reports; }

    public long getTotalCount() { return totalCount; }
    public void setTotalCount(long totalCount) { this.totalCount = totalCount; }

    /**
     * Summary DTO for scheduled report.
     */
    public static class ScheduledReportSummary {
        private String id;
        private String name;
        private String type;
        private String schedule;
        private String status;
        private LocalDateTime lastRun;
        private LocalDateTime nextRun;

        public ScheduledReportSummary() {}

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getSchedule() { return schedule; }
        public void setSchedule(String schedule) { this.schedule = schedule; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public LocalDateTime getLastRun() { return lastRun; }
        public void setLastRun(LocalDateTime lastRun) { this.lastRun = lastRun; }

        public LocalDateTime getNextRun() { return nextRun; }
        public void setNextRun(LocalDateTime nextRun) { this.nextRun = nextRun; }
    }
}
