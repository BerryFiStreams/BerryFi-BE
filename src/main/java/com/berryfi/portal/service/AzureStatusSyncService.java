package com.berryfi.portal.service;

import com.berryfi.portal.entity.VmInstance;
import com.berryfi.portal.entity.VmSession;
import com.berryfi.portal.enums.SessionStatus;
import com.berryfi.portal.enums.VmStatus;
import com.berryfi.portal.repository.VmInstanceRepository;
import com.berryfi.portal.repository.VmSessionRepository;
import com.berryfi.portal.service.VmSessionService.VmSessionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service for synchronizing VM and session status with Azure.
 * Handles periodic status checks and reconciliation between database and Azure state.
 */
@Service
public class AzureStatusSyncService {

    private static final Logger logger = LoggerFactory.getLogger(AzureStatusSyncService.class);

    @Autowired
    private VmSessionService vmSessionService;
    
    @Autowired
    private VmInstanceRepository vmInstanceRepository;
    
    @Autowired
    private VmSessionRepository vmSessionRepository;
    
    @Autowired
    private AzureVmService azureVmService;

    @Value("${azure.status.sync.enabled:true}")
    private boolean statusSyncEnabled;
    
    @Value("${azure.status.sync.active-sessions.interval.seconds:30}")
    private int activeSessionSyncIntervalSeconds;
    
    @Value("${azure.status.sync.full.interval.seconds:300}")
    private int fullSyncIntervalSeconds;
    
    @Value("${azure.status.sync.batch.size:10}")
    private int batchSize;

    private final AtomicInteger activeSyncCount = new AtomicInteger(0);
    private final AtomicInteger fullSyncCount = new AtomicInteger(0);
    private LocalDateTime lastActiveSyncTime;
    private LocalDateTime lastFullSyncTime;

    @PostConstruct
    public void init() {
        logger.info("Azure Status Sync Service initialized");
        logger.info("  - Azure sync enabled: {}", statusSyncEnabled);
        if (statusSyncEnabled) {
            logger.info("  - Active sessions sync: every {} seconds", activeSessionSyncIntervalSeconds);
            logger.info("  - Full VM status sync: every {} seconds", fullSyncIntervalSeconds);
            logger.info("  - Batch size for full sync: {}", batchSize);
        } else {
            logger.warn("  - Azure status synchronization is DISABLED");
        }
    }

    /**
     * Quick sync for active sessions only
     * Runs every 30 seconds (configurable)
     */
    @Scheduled(fixedRateString = "#{${azure.status.sync.active-sessions.interval.seconds:30} * 1000}")
    public void syncActiveSessions() {
        if (!statusSyncEnabled) {
            logger.debug("Azure status sync is disabled, skipping active sessions sync");
            return;
        }

        int syncRun = activeSyncCount.incrementAndGet();
        LocalDateTime syncStartTime = LocalDateTime.now();
        lastActiveSyncTime = syncStartTime;
        
        try {
            logger.debug("Starting active sessions sync #{} at {}", syncRun, syncStartTime);
            
            // Get all active sessions
            List<VmSession> activeSessions = vmSessionRepository.findActiveSessions();
            
            if (activeSessions.isEmpty()) {
                logger.debug("Active sessions sync #{}: No active sessions found", syncRun);
                return;
            }
            
            logger.debug("Active sessions sync #{}: Found {} active sessions to check", 
                syncRun, activeSessions.size());
            
            int successCount = 0;
            int errorCount = 0;
            int mismatchCount = 0;
            
            for (VmSession session : activeSessions) {
                try {
                    logger.debug("Active sync #{}: Checking session {} (VM: {}, User: {})", 
                        syncRun, session.getId(), session.getVmInstanceId(), session.getUserId());
                    
                    boolean hasMismatch = reconcileSessionWithAzure(session, syncRun);
                    if (hasMismatch) {
                        mismatchCount++;
                    }
                    successCount++;
                    
                } catch (Exception e) {
                    errorCount++;
                    logger.error("Active sync #{}: Failed to sync session {}: {}", 
                        syncRun, session.getId(), e.getMessage(), e);
                }
            }
            
            LocalDateTime syncEndTime = LocalDateTime.now();
            long durationMs = java.time.Duration.between(syncStartTime, syncEndTime).toMillis();
            
            if (mismatchCount > 0 || errorCount > 0) {
                logger.info("Active sessions sync #{} completed in {}ms: {} successful, {} errors, {} mismatches found", 
                    syncRun, durationMs, successCount, errorCount, mismatchCount);
            } else {
                logger.debug("Active sessions sync #{} completed in {}ms: {} successful, {} errors, {} mismatches found", 
                    syncRun, durationMs, successCount, errorCount, mismatchCount);
            }
                
        } catch (Exception e) {
            logger.error("Active sessions sync #{} failed: {}", syncRun, e.getMessage(), e);
        }
    }

    /**
     * Comprehensive sync of all managed VMs
     * Runs every 5 minutes (configurable)
     */
    @Scheduled(fixedRateString = "#{${azure.status.sync.full.interval.seconds:300} * 1000}")
    public void fullVmStatusSync() {
        if (!statusSyncEnabled) {
            logger.debug("Azure status sync is disabled, skipping full VM sync");
            return;
        }

        int syncRun = fullSyncCount.incrementAndGet();
        LocalDateTime syncStartTime = LocalDateTime.now();
        lastFullSyncTime = syncStartTime;
        
        try {
            logger.debug("Starting full VM status sync #{} at {}", syncRun, syncStartTime);
            
            // Get all managed VMs (those that need Azure monitoring)
            List<VmInstance> managedVms = vmInstanceRepository.findManagedVmsForSync();
            
            if (managedVms.isEmpty()) {
                logger.debug("Full VM sync #{}: No managed VMs found to sync", syncRun);
                return;
            }
            
            logger.debug("Full VM sync #{}: Found {} managed VMs to check", syncRun, managedVms.size());
            
            int successCount = 0;
            int errorCount = 0;
            int statusUpdatesCount = 0;
            int batchNum = 0;
            
            // Process VMs in batches to avoid overwhelming Azure API
            for (int i = 0; i < managedVms.size(); i += batchSize) {
                batchNum++;
                int endIndex = Math.min(i + batchSize, managedVms.size());
                List<VmInstance> batch = managedVms.subList(i, endIndex);
                
                logger.debug("Full sync #{}: Processing batch {} ({}/{} VMs)", 
                    syncRun, batchNum, batch.size(), managedVms.size());
                
                for (VmInstance vm : batch) {
                    try {
                        logger.debug("Full sync #{}: Checking VM {} (Status: {}, Azure Resource: {})", 
                            syncRun, vm.getId(), vm.getStatus(), vm.getAzureResourceId());
                        
                        boolean wasUpdated = syncVmStatusWithAzure(vm, syncRun);
                        if (wasUpdated) {
                            statusUpdatesCount++;
                        }
                        successCount++;
                        
                    } catch (Exception e) {
                        errorCount++;
                        logger.error("Full sync #{}: Failed to sync VM {}: {}", 
                            syncRun, vm.getId(), e.getMessage(), e);
                    }
                }
                
                // Small delay between batches to be gentle on Azure API
                if (i + batchSize < managedVms.size()) {
                    try {
                        Thread.sleep(100); // 100ms delay between batches
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
            
            LocalDateTime syncEndTime = LocalDateTime.now();
            long durationMs = java.time.Duration.between(syncStartTime, syncEndTime).toMillis();
            
            if (statusUpdatesCount > 0 || errorCount > 0) {
                logger.info("Full VM sync #{} completed in {}ms: {} successful, {} errors, {} status updates", 
                    syncRun, durationMs, successCount, errorCount, statusUpdatesCount);
            } else {
                logger.debug("Full VM sync #{} completed in {}ms: {} successful, {} errors, {} status updates", 
                    syncRun, durationMs, successCount, errorCount, statusUpdatesCount);
            }
                
        } catch (Exception e) {
            logger.error("Full VM sync #{} failed: {}", syncRun, e.getMessage(), e);
        }
    }

    /**
     * Reconcile a session's status with Azure
     */
    private boolean reconcileSessionWithAzure(VmSession session, int syncRun) {
        try {
            VmInstance vm = session.getVmInstance();
            if (vm == null) {
                logger.warn("Active sync #{}: Session {} has no associated VM instance", 
                    syncRun, session.getId());
                return false;
            }
            
            // Get current status from Azure
            VmStatus azureStatus = azureVmService.getVmStatus(vm);
            VmStatus dbStatus = vm.getStatus();
            SessionStatus sessionStatus = session.getStatus();
            
            logger.debug("Active sync #{}: Session {} status check - DB VM: {}, Azure VM: {}, Session: {}", 
                syncRun, session.getId(), dbStatus, azureStatus, sessionStatus);
            
            if (azureStatus == VmStatus.ERROR) {
                logger.warn("Active sync #{}: Could not get Azure status for VM {} in session {}", 
                    syncRun, vm.getId(), session.getId());
                return false;
            }
            
            // Check for status mismatches that require action
            boolean hasMismatch = false;
            
            if (shouldReconcileStatus(sessionStatus, dbStatus, azureStatus)) {
                hasMismatch = true;
                logger.warn("Active sync #{}: Status mismatch detected for session {} - Session: {}, DB VM: {}, Azure VM: {}", 
                    syncRun, session.getId(), sessionStatus, dbStatus, azureStatus);
                
                handleStatusMismatch(session, vm, dbStatus, azureStatus, syncRun);
            }
            
            return hasMismatch;
            
        } catch (Exception e) {
            logger.error("Active sync #{}: Error reconciling session {}: {}", 
                syncRun, session.getId(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * Sync a VM's status with Azure and update database if needed
     */
    private boolean syncVmStatusWithAzure(VmInstance vm, int syncRun) {
        try {
            VmStatus currentStatus = vm.getStatus();
            VmStatus azureStatus = azureVmService.getVmStatus(vm);
            
            logger.debug("Full sync #{}: VM {} status check - DB: {}, Azure: {}", 
                syncRun, vm.getId(), currentStatus, azureStatus);
            
            if (azureStatus == VmStatus.ERROR) {
                logger.warn("Full sync #{}: Could not get Azure status for VM {}", syncRun, vm.getId());
                return false;
            }
            
            boolean statusUpdated = false;
            
            // Update VM status in database if it differs from Azure
            if (currentStatus != azureStatus && shouldUpdateVmStatus(currentStatus, azureStatus)) {
                logger.info("Full sync #{}: Updating VM {} status from {} to {} (Azure sync)", 
                    syncRun, vm.getId(), currentStatus, azureStatus);
                
                vm.setStatus(azureStatus);
                vm.setLastSyncTime(LocalDateTime.now());
                vmInstanceRepository.save(vm);
                
                statusUpdated = true;
            }
            
            // If VM is stopped (not deallocated) and not in use, deallocate it to save costs
            if (azureStatus == VmStatus.STOPPED && !vm.isInUse()) {
                logger.info("Full sync #{}: VM {} is STOPPED but not deallocated, sending deallocate command to save costs", 
                    syncRun, vm.getId());
                
                try {
                    boolean deallocated = azureVmService.stopVm(vm);
                    if (deallocated) {
                        logger.info("Full sync #{}: Successfully deallocated VM {}", syncRun, vm.getId());
                        vm.setStatus(VmStatus.DEALLOCATED);
                        vm.setLastSyncTime(LocalDateTime.now());
                        vmInstanceRepository.save(vm);
                        statusUpdated = true;
                    } else {
                        logger.warn("Full sync #{}: Failed to deallocate VM {}", syncRun, vm.getId());
                    }
                } catch (Exception e) {
                    logger.error("Full sync #{}: Error deallocating VM {}: {}", syncRun, vm.getId(), e.getMessage(), e);
                }
            }
            
            // Update last sync time even if no status change
            if (!statusUpdated) {
                vm.setLastSyncTime(LocalDateTime.now());
                vmInstanceRepository.save(vm);
            }
            
            return statusUpdated;
            
        } catch (Exception e) {
            logger.error("Full sync #{}: Error syncing VM {}: {}", syncRun, vm.getId(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * Handle status mismatches between session, database VM, and Azure VM
     */
    private void handleStatusMismatch(VmSession session, VmInstance vm, 
                                    VmStatus dbStatus, VmStatus azureStatus, int syncRun) {
        try {
            switch (azureStatus) {
                case RUNNING:
                    handleAzureVmRunning(session, vm, dbStatus, syncRun);
                    break;
                    
                case STOPPED:
                case DEALLOCATED:
                    handleAzureVmStopped(session, vm, azureStatus, syncRun);
                    break;
                    
                case STARTING:
                    handleAzureVmStarting(session, vm, dbStatus, syncRun);
                    break;
                    
                case STOPPING:
                    handleAzureVmStopping(session, vm, dbStatus, syncRun);
                    break;
                    
                default:
                    logger.warn("Active sync #{}: Unknown Azure status {} for VM {} in session {}", 
                        syncRun, azureStatus, vm.getId(), session.getId());
                    break;
            }
            
        } catch (Exception e) {
            logger.error("Active sync #{}: Error handling status mismatch for session {}: {}", 
                syncRun, session.getId(), e.getMessage(), e);
        }
    }

    private void handleAzureVmRunning(VmSession session, VmInstance vm, VmStatus dbStatus, int syncRun) {
        if (dbStatus == VmStatus.STOPPED || dbStatus == VmStatus.AVAILABLE || dbStatus == VmStatus.DEALLOCATED) {
            logger.warn("Active sync #{}: Azure VM {} is RUNNING but DB shows {}, updating VM status", 
                syncRun, vm.getId(), dbStatus);
            
            vm.setStatus(VmStatus.RUNNING);
            vm.setLastSyncTime(LocalDateTime.now());
            vmInstanceRepository.save(vm);
            
            // If session is not active, consider resuming it
            if (session.getStatus() != SessionStatus.ACTIVE) {
                logger.info("Active sync #{}: Session {} VM is running in Azure, but session is {}, marking as active", 
                    syncRun, session.getId(), session.getStatus());
                // Note: We don't automatically resume sessions as it could cause billing issues
                // This should be handled manually or with specific business logic
            }
        }
    }

    private void handleAzureVmStopped(VmSession session, VmInstance vm, VmStatus azureStatus, int syncRun) {
        if (session.getStatus() == SessionStatus.ACTIVE || vm.getStatus() == VmStatus.RUNNING) {
            logger.warn("Active sync #{}: Azure VM {} is {} but session is ACTIVE or VM is RUNNING, terminating session", 
                syncRun, vm.getId(), azureStatus);
            
            // Update VM status first
            vm.setStatus(azureStatus);
            vm.setLastSyncTime(LocalDateTime.now());
            vmInstanceRepository.save(vm);
            
            // Force terminate the session
            VmSessionResult result = vmSessionService.forceTerminateSession(
                session.getId(), 
                "Auto-terminated: VM found stopped/deallocated in Azure during sync #" + syncRun
            );
            
            if (result.isSuccess()) {
                logger.info("Active sync #{}: Successfully terminated session {} due to Azure VM stopped", 
                    syncRun, session.getId());
            } else {
                logger.error("Active sync #{}: Failed to terminate session {} despite Azure VM being stopped: {}", 
                    syncRun, session.getId(), result.getMessage());
            }
        } else if (azureStatus == VmStatus.STOPPED && session.getStatus() != SessionStatus.ACTIVE) {
            // VM is only powered off but not deallocated, and session is not active
            // Deallocate it to save costs
            logger.info("Active sync #{}: Azure VM {} is STOPPED (not deallocated) with inactive session, sending deallocate command", 
                syncRun, vm.getId());
            
            try {
                boolean deallocated = azureVmService.stopVm(vm);
                if (deallocated) {
                    logger.info("Active sync #{}: Successfully deallocated VM {} to save costs", syncRun, vm.getId());
                    vm.setStatus(VmStatus.DEALLOCATED);
                    vm.setLastSyncTime(LocalDateTime.now());
                    vmInstanceRepository.save(vm);
                } else {
                    logger.warn("Active sync #{}: Failed to deallocate VM {}", syncRun, vm.getId());
                }
            } catch (Exception e) {
                logger.error("Active sync #{}: Error deallocating VM {}: {}", syncRun, vm.getId(), e.getMessage(), e);
            }
        }
    }

    private void handleAzureVmStarting(VmSession session, VmInstance vm, VmStatus dbStatus, int syncRun) {
        if (dbStatus != VmStatus.STARTING && dbStatus != VmStatus.ASSIGNED) {
            logger.info("Active sync #{}: Azure VM {} is STARTING, updating DB status from {} to STARTING", 
                syncRun, vm.getId(), dbStatus);
            
            vm.setStatus(VmStatus.STARTING);
            vm.setLastSyncTime(LocalDateTime.now());
            vmInstanceRepository.save(vm);
        }
    }

    private void handleAzureVmStopping(VmSession session, VmInstance vm, VmStatus dbStatus, int syncRun) {
        if (dbStatus != VmStatus.STOPPING) {
            logger.info("Active sync #{}: Azure VM {} is STOPPING, updating DB status from {} to STOPPING", 
                syncRun, vm.getId(), dbStatus);
            
            vm.setStatus(VmStatus.STOPPING);
            vm.setLastSyncTime(LocalDateTime.now());
            vmInstanceRepository.save(vm);
        }
    }

    /**
     * Determine if session/VM status should be reconciled with Azure
     */
    private boolean shouldReconcileStatus(SessionStatus sessionStatus, VmStatus dbStatus, VmStatus azureStatus) {
        // Always reconcile if there's a significant mismatch
        if (sessionStatus == SessionStatus.ACTIVE && (azureStatus == VmStatus.STOPPED || azureStatus == VmStatus.DEALLOCATED)) {
            return true; // Active session but Azure VM is stopped
        }
        
        if ((dbStatus == VmStatus.RUNNING || dbStatus == VmStatus.ASSIGNED) && 
            (azureStatus == VmStatus.STOPPED || azureStatus == VmStatus.DEALLOCATED)) {
            return true; // DB thinks VM is running but Azure shows stopped
        }
        
        if ((dbStatus == VmStatus.STOPPED || dbStatus == VmStatus.AVAILABLE) && azureStatus == VmStatus.RUNNING) {
            return true; // DB thinks VM is stopped but Azure shows running
        }
        
        return false;
    }

    /**
     * Determine if VM status in database should be updated based on Azure status
     */
    private boolean shouldUpdateVmStatus(VmStatus currentStatus, VmStatus azureStatus) {
        // Don't update if Azure status is unknown/error
        if (azureStatus == VmStatus.ERROR) {
            return false;
        }
        
        // Always update if statuses are different and Azure status is definitive
        return currentStatus != azureStatus;
    }

    /**
     * Get sync statistics
     */
    public SyncStats getSyncStats() {
        return new SyncStats(
            statusSyncEnabled,
            activeSyncCount.get(),
            fullSyncCount.get(),
            lastActiveSyncTime,
            lastFullSyncTime
        );
    }

    /**
     * Statistics data class
     */
    public static class SyncStats {
        private final boolean enabled;
        private final int activeSyncRuns;
        private final int fullSyncRuns;
        private final LocalDateTime lastActiveSync;
        private final LocalDateTime lastFullSync;

        public SyncStats(boolean enabled, int activeSyncRuns, int fullSyncRuns, 
                        LocalDateTime lastActiveSync, LocalDateTime lastFullSync) {
            this.enabled = enabled;
            this.activeSyncRuns = activeSyncRuns;
            this.fullSyncRuns = fullSyncRuns;
            this.lastActiveSync = lastActiveSync;
            this.lastFullSync = lastFullSync;
        }

        // Getters
        public boolean isEnabled() { return enabled; }
        public int getActiveSyncRuns() { return activeSyncRuns; }
        public int getFullSyncRuns() { return fullSyncRuns; }
        public LocalDateTime getLastActiveSync() { return lastActiveSync; }
        public LocalDateTime getLastFullSync() { return lastFullSync; }
    }
}