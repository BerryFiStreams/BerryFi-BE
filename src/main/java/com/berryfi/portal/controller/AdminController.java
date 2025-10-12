package com.berryfi.portal.controller;

import com.berryfi.portal.dto.ApiResponse;
import com.berryfi.portal.service.AzureStatusSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Admin controller for system monitoring and management
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private AzureStatusSyncService azureStatusSyncService;

    /**
     * Get Azure sync status and statistics
     */
    @GetMapping("/azure-sync-status")
    public ResponseEntity<ApiResponse<AzureStatusSyncService.SyncStats>> getAzureSyncStatus() {
        try {
            logger.info("Admin requested Azure sync status");
            
            AzureStatusSyncService.SyncStats syncStats = azureStatusSyncService.getSyncStats();
            
            return ResponseEntity.ok(ApiResponse.success(
                "Azure sync status retrieved successfully", 
                syncStats
            ));
            
        } catch (Exception e) {
            logger.error("Failed to get Azure sync status: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to get Azure sync status: " + e.getMessage()));
        }
    }

    /**
     * Health check endpoint for monitoring services
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> getHealthStatus() {
        try {
            logger.debug("Admin health check requested");
            
            AzureStatusSyncService.SyncStats syncStats = azureStatusSyncService.getSyncStats();
            
            String healthMessage = String.format(
                "System healthy - Azure sync: %s, Active syncs: %d, Full syncs: %d", 
                syncStats.isEnabled() ? "enabled" : "disabled",
                syncStats.getActiveSyncRuns(),
                syncStats.getFullSyncRuns()
            );
            
            return ResponseEntity.ok(ApiResponse.success(healthMessage, "OK"));
            
        } catch (Exception e) {
            logger.error("Health check failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Health check failed: " + e.getMessage()));
        }
    }
}
