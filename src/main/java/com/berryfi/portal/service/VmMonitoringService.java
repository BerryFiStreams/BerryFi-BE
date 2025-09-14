package com.berryfi.portal.service;

import com.berryfi.portal.entity.VmSession;
import com.berryfi.portal.service.VmSessionService.VmSessionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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

    /**
     * Monitor for sessions that have timed out (no heartbeat)
     * Runs every 10 seconds to ensure quick detection of inactive sessions
     */
    @Scheduled(fixedRate = 10000) // 10 seconds
    public void monitorTimedOutSessions() {
        try {
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
            // TODO: Add statistics gathering and logging
            logger.info("VM monitoring service is running");
            
        } catch (Exception e) {
            logger.error("Error logging VM session statistics: " + e.getMessage(), e);
        }
    }
}
