package com.berryfi.portal.scheduler;

import com.berryfi.portal.service.TeamMemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled tasks for cleaning up expired invitations.
 */
@Component
public class InvitationCleanupScheduler {

    private static final Logger logger = LoggerFactory.getLogger(InvitationCleanupScheduler.class);

    @Autowired
    private TeamMemberService teamMemberService;

    /**
     * Expire old invitations every hour.
     * This task runs at the beginning of every hour to clean up expired invitations.
     */
    @Scheduled(cron = "0 0 * * * *") // Run at the beginning of every hour
    public void expireOldInvitations() {
        logger.info("=== Starting scheduled expiration of old invitations ===");
        
        try {
            int expiredCount = teamMemberService.expireOldInvitations();
            
            if (expiredCount > 0) {
                logger.info("Scheduled task expired {} old invitations", expiredCount);
            } else {
                logger.debug("No expired invitations found during scheduled cleanup");
            }
            
        } catch (Exception e) {
            logger.error("Error during scheduled expiration of old invitations: {}", e.getMessage(), e);
        }
        
        logger.debug("=== Completed scheduled expiration of old invitations ===");
    }

    /**
     * Daily cleanup of very old invitations (older than 30 days).
     * This task runs daily at 2 AM to permanently delete old expired/declined invitations.
     */
    @Scheduled(cron = "0 0 2 * * *") // Run daily at 2 AM
    public void cleanupVeryOldInvitations() {
        logger.info("=== Starting daily cleanup of very old invitations ===");
        
        try {
            // First expire any remaining old invitations
            int expiredCount = teamMemberService.expireOldInvitations();
            
            if (expiredCount > 0) {
                logger.info("Daily cleanup expired {} additional old invitations", expiredCount);
            }
            
            // Note: You can add logic here to delete very old invitations if needed
            // For now, we just expire them to maintain audit trail
            
            logger.info("Daily cleanup completed successfully");
            
        } catch (Exception e) {
            logger.error("Error during daily cleanup of old invitations: {}", e.getMessage(), e);
        }
        
        logger.debug("=== Completed daily cleanup of very old invitations ===");
    }
}