package com.berryfi.portal.dto.analytics;

/**
 * Response DTO for export operations.
 */
public class ExportResponse {
    private String downloadUrl;
    private String fileName;
    private String format;
    private long fileSize;
    private String status;

    public ExportResponse() {}

    public ExportResponse(String downloadUrl, String fileName, String format, long fileSize, String status) {
        this.downloadUrl = downloadUrl;
        this.fileName = fileName;
        this.format = format;
        this.fileSize = fileSize;
        this.status = status;
    }

    // Getters and setters
    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
