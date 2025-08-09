package com.berryfi.portal.dto.audit;

/**
 * Response DTO for audit export operations.
 */
public class AuditExportResponse {
    private String downloadUrl;
    private String fileName;
    private String format;
    private long fileSize;
    private String status;
    private long recordCount;

    public AuditExportResponse() {}

    public AuditExportResponse(String downloadUrl, String fileName, String format, long fileSize, 
                              String status, long recordCount) {
        this.downloadUrl = downloadUrl;
        this.fileName = fileName;
        this.format = format;
        this.fileSize = fileSize;
        this.status = status;
        this.recordCount = recordCount;
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

    public long getRecordCount() { return recordCount; }
    public void setRecordCount(long recordCount) { this.recordCount = recordCount; }
}
