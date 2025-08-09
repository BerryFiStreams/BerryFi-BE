package com.berryfi.portal.dto.reports;

/**
 * Response DTO for report generation.
 */
public class ReportGenerationResponse {
    private String reportId;
    private String status;
    private String downloadUrl;
    private String fileName;
    private long estimatedCompletionTime;

    public ReportGenerationResponse() {}

    public ReportGenerationResponse(String reportId, String status, String downloadUrl, 
                                   String fileName, long estimatedCompletionTime) {
        this.reportId = reportId;
        this.status = status;
        this.downloadUrl = downloadUrl;
        this.fileName = fileName;
        this.estimatedCompletionTime = estimatedCompletionTime;
    }

    // Getters and setters
    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public long getEstimatedCompletionTime() { return estimatedCompletionTime; }
    public void setEstimatedCompletionTime(long estimatedCompletionTime) { this.estimatedCompletionTime = estimatedCompletionTime; }
}
