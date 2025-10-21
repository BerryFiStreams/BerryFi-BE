package com.berryfi.portal.service;

import com.berryfi.portal.entity.VmSession;
import com.berryfi.portal.service.VmSessionService.VmSessionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Background monitoring service for VM sessions.
 * Handles timeout detection and automatic session termination.
 */
@Service
public class VmMonitoringService {

    private static final Logger logger = LoggerFactory.getLogger(VmMonitoringService.class);

    @Autowired
    private VmSessionService vmSessionService;
    
    @Autowired
    private AzureStatusSyncService azureStatusSyncService;

    @PostConstruct
    public void init() {
        logger.info("VM Monitoring Service initialized - starting session monitoring schedulers");
        logger.info("  - Timed-out sessions check: every 10 seconds");
        logger.info("  - Long-running sessions check: every 10 minutes");
        logger.info("  - Health statistics: every 1 hour");
    }

    /**
     * Monitor for sessions that have timed out (no heartbeat)
     * Runs every 10 seconds to ensure quick detection of inactive sessions
     */
    @Scheduled(fixedRate = 10000) // 10 seconds
    public void monitorTimedOutSessions() {
        try {
            logger.debug("Checking for timed-out sessions...");
            List<VmSession> timedOutSessions = vmSessionService.getTimedOutSessions();
            
            if (!timedOutSessions.isEmpty()) {
                logger.info("Found {} timed-out sessions (inactive >30s)", timedOutSessions.size());
                
                for (VmSession session : timedOutSessions) {
                    logger.info("Terminating inactive session: {} (User: {}, Duration: {} seconds, Last heartbeat: {})", 
                        session.getId(), session.getUserId(), session.getDurationInSeconds(),
                        session.getLastHeartbeat() != null ? session.getLastHeartbeat().toString() : "Never");
                    
                    VmSessionResult result = vmSessionService.forceTerminateSession(
                        session.getId(), 
                        "Session inactive - no heartbeat received for more than 30 seconds"
                    );
                    
                    if (result.isSuccess()) {
                        logger.info("Successfully terminated inactive session: {}", session.getId());
                    } else {
                        logger.error("Failed to terminate inactive session {}: {}", 
                            session.getId(), result.getMessage());
                    }
                }
            } else {
                logger.debug("No inactive sessions found (>30s without heartbeat)");
            }
            
        } catch (Exception e) {
            logger.error("Error monitoring timed-out sessions: " + e.getMessage(), e);
        }
    }

    /**
     * Monitor for sessions that exceed maximum duration
     * Runs every 10 minutes
     */
    @Scheduled(fixedRate = 600000) // 10 minutes
    public void monitorLongRunningSessions() {
        try {
            List<VmSession> longRunningSessions = vmSessionService.getLongRunningSessions();
            
            if (!longRunningSessions.isEmpty()) {
                logger.info("Found {} long-running sessions", longRunningSessions.size());
                
                for (VmSession session : longRunningSessions) {
                    logger.info("Terminating long-running session: {} (User: {}, Duration: {} seconds)", 
                        session.getId(), session.getUserId(), session.getDurationInSeconds());
                    
                    VmSessionResult result = vmSessionService.forceTerminateSession(
                        session.getId(), 
                        "Session exceeded maximum duration limit"
                    );
                    
                    if (result.isSuccess()) {
                        logger.info("Successfully terminated long-running session: {}", session.getId());
                    } else {
                        logger.error("Failed to terminate long-running session {}: {}", 
                            session.getId(), result.getMessage());
                    }
                }
            } else {
                logger.debug("No long-running sessions found");
            }
            
        } catch (Exception e) {
            logger.error("Error monitoring long-running sessions: " + e.getMessage(), e);
        }
    }

    /**
     * Health check and statistics logging
     * Runs every hour
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
    public void logVmSessionStatistics() {
        try {
            logger.info("=== VM Monitoring Service Health Check ===");
            
            // Get timed-out sessions
            List<VmSession> timedOutSessions = vmSessionService.getTimedOutSessions();
            logger.info("Current timed-out sessions: {}", timedOutSessions.size());
            
            // Get long-running sessions
            List<VmSession> longRunningSessions = vmSessionService.getLongRunningSessions();
            logger.info("Current long-running sessions: {}", longRunningSessions.size());
            
            // Get Azure sync stats if available
            try {
                AzureStatusSyncService.SyncStats syncStats = azureStatusSyncService.getSyncStats();
                logger.info("Azure Status Sync: enabled={}, active_sync_runs={}, full_sync_runs={}, last_active_sync={}, last_full_sync={}", 
                    syncStats.isEnabled(), 
                    syncStats.getActiveSyncRuns(), 
                    syncStats.getFullSyncRuns(),
                    syncStats.getLastActiveSync(),
                    syncStats.getLastFullSync());
            } catch (Exception e) {
                logger.warn("Could not get Azure sync statistics: {}", e.getMessage());
            }
            
            logger.info("VM monitoring service is running normally");
            
        } catch (Exception e) {
            logger.error("Error logging VM session statistics: " + e.getMessage(), e);
        }
    }
}
