package com.berryfi.portal.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service for handling file operations.
 */
@Service
public class FileService {

    private static final String EXPORT_DIRECTORY = "exports/audit";

    /**
     * Save export content to file.
     */
    public String saveExportFile(String content, String format, String organizationId) throws IOException {
        // Create exports directory if it doesn't exist
        Path exportDir = Paths.get(EXPORT_DIRECTORY);
        if (!Files.exists(exportDir)) {
            Files.createDirectories(exportDir);
        }

        // Generate unique filename
        String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
        String fileName = String.format("audit_export_%s_%s.%s", organizationId, timestamp, format.toLowerCase());
        
        Path filePath = exportDir.resolve(fileName);
        
        // Write content to file
        Files.write(filePath, content.getBytes());
        
        return fileName;
    }

    /**
     * Read export file content.
     */
    public String readExportFile(String fileName) throws IOException {
        Path filePath = Paths.get(EXPORT_DIRECTORY).resolve(fileName);
        
        if (!Files.exists(filePath)) {
            throw new IOException("File not found: " + fileName);
        }
        
        return new String(Files.readAllBytes(filePath));
    }

    /**
     * Check if export file exists.
     */
    public boolean exportFileExists(String fileName) {
        Path filePath = Paths.get(EXPORT_DIRECTORY).resolve(fileName);
        return Files.exists(filePath);
    }

    /**
     * Get file size.
     */
    public long getFileSize(String fileName) throws IOException {
        Path filePath = Paths.get(EXPORT_DIRECTORY).resolve(fileName);
        return Files.size(filePath);
    }

    /**
     * Delete export file.
     */
    public boolean deleteExportFile(String fileName) throws IOException {
        Path filePath = Paths.get(EXPORT_DIRECTORY).resolve(fileName);
        return Files.deleteIfExists(filePath);
    }

    /**
     * Get content type based on file extension.
     */
    public String getContentType(String fileName) {
        if (fileName.endsWith(".csv")) {
            return "text/csv";
        } else if (fileName.endsWith(".json")) {
            return "application/json";
        } else if (fileName.endsWith(".xlsx")) {
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        } else if (fileName.endsWith(".pdf")) {
            return "application/pdf";
        }
        return "application/octet-stream";
    }
}
