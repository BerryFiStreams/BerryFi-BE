# Azure VM Status Synchronization Implementation Plan

## Problem Statement
Currently, the BerryFi system does not synchronize VM session status with actual Azure VM status, leading to potential state drift, billing inconsistencies, and poor user experience.

## Proposed Solution

### 1. Enhanced AzureVmService
- Implement actual Azure SDK integration (commented TODO sections)
- Add `syncVmStatus()` method to check real Azure status
- Map Azure PowerStates to internal VmStatus enum

### 2. Azure Status Monitoring Service
Create `AzureStatusSyncService` with:
- **Every 30 seconds**: Quick sync for ACTIVE sessions
- **Every 5 minutes**: Full sync of all ASSIGNED/STARTING/RUNNING VMs
- **Smart batching**: Group VMs by Azure subscription for efficient API calls

### 3. Status Reconciliation Logic
When Azure status differs from database:
- **Azure Running + DB Stopped**: Update DB to RUNNING, resume billing
- **Azure Stopped + DB Active**: Mark session as TERMINATED, stop billing
- **Azure Deallocated + DB Active**: Force terminate session, full cleanup

### 4. Implementation Components

#### Enhanced AzureVmService
```java
public class AzureVmService {
    
    public AzureVmStatus getActualVmStatus(VmInstance vm) {
        // Real Azure SDK call
        AzureResourceManager azure = createAzureClient(vm);
        VirtualMachine azureVm = azure.virtualMachines()
            .getByResourceGroup(vm.getAzureResourceGroup(), vm.getAzureResourceId());
        return mapPowerStateToVmStatus(azureVm.powerState());
    }
    
    private VmStatus mapPowerStateToVmStatus(PowerState powerState) {
        // Map Azure PowerState to internal VmStatus
        switch (powerState.toString()) {
            case "PowerState/running": return VmStatus.RUNNING;
            case "PowerState/stopped": return VmStatus.STOPPED;
            case "PowerState/starting": return VmStatus.STARTING;
            case "PowerState/stopping": return VmStatus.STOPPING;
            case "PowerState/deallocated": return VmStatus.STOPPED;
            default: return VmStatus.UNKNOWN;
        }
    }
}
```

#### New Azure Sync Service
```java
@Service
public class AzureStatusSyncService {
    
    @Scheduled(fixedRate = 30000) // 30 seconds
    public void syncActiveSessions() {
        // Quick sync for active sessions only
        List<VmSession> activeSessions = vmSessionService.getActiveSessions();
        for (VmSession session : activeSessions) {
            reconcileSessionWithAzure(session);
        }
    }
    
    @Scheduled(fixedRate = 300000) // 5 minutes  
    public void fullVmStatusSync() {
        // Comprehensive sync of all managed VMs
        List<VmInstance> managedVms = vmInstanceService.getAllManagedVms();
        syncVmStatuses(managedVms);
    }
    
    private void reconcileSessionWithAzure(VmSession session) {
        VmInstance vm = session.getVmInstance();
        AzureVmStatus azureStatus = azureVmService.getActualVmStatus(vm);
        VmStatus currentStatus = vm.getStatus();
        
        if (shouldUpdateStatus(currentStatus, azureStatus)) {
            handleStatusMismatch(session, currentStatus, azureStatus);
        }
    }
}
```

#### Status Reconciliation Rules
```java
private void handleStatusMismatch(VmSession session, VmStatus dbStatus, AzureVmStatus azureStatus) {
    switch (azureStatus) {
        case RUNNING:
            if (dbStatus == VmStatus.STOPPED || dbStatus == VmStatus.AVAILABLE) {
                // Azure VM running but DB thinks it's stopped
                logger.warn("Azure VM {} is running but DB shows {}, correcting status", 
                    session.getVmInstanceId(), dbStatus);
                vmSessionService.resumeSession(session.getId(), "Auto-resumed: VM found running in Azure");
            }
            break;
            
        case STOPPED:
        case DEALLOCATED:
            if (dbStatus == VmStatus.RUNNING || session.getStatus() == SessionStatus.ACTIVE) {
                // Azure VM stopped but session active
                logger.warn("Azure VM {} is stopped but session is active, terminating session", 
                    session.getVmInstanceId());
                vmSessionService.forceTerminateSession(session.getId(), 
                    "Auto-terminated: VM stopped in Azure");
            }
            break;
    }
}
```

### 5. Configuration Options
```properties
# Azure Status Sync Configuration
azure.status.sync.enabled=true
azure.status.sync.active-sessions.interval.seconds=30
azure.status.sync.full.interval.seconds=300
azure.status.sync.batch.size=10
azure.status.sync.retry.attempts=3
azure.status.sync.timeout.seconds=30
```

### 6. Benefits
- **Accurate Billing**: Only bill for actually running VMs
- **Better UX**: Users see real VM status
- **Cost Optimization**: Detect and stop orphaned VMs
- **Reliability**: Handle Azure outages gracefully
- **Compliance**: Audit trail of all status changes

### 7. Risk Mitigation
- **Rate Limiting**: Respect Azure API limits
- **Error Handling**: Graceful degradation on Azure API failures  
- **Batching**: Efficient API usage
- **Logging**: Comprehensive audit trail
- **Configuration**: Enable/disable per environment

### 8. Implementation Priority
1. **Phase 1**: Basic status sync for active sessions (30-second cycle)
2. **Phase 2**: Full VM status sync (5-minute cycle) 
3. **Phase 3**: Advanced reconciliation rules
4. **Phase 4**: Monitoring and alerting

## Recommendation
Implement this Azure status synchronization **immediately** before going to production to ensure:
- Accurate billing
- Reliable VM management
- Better user experience
- Compliance with actual resource usage

The current 30-second inactivity timeout is good, but it should be combined with Azure status verification to ensure VMs are actually stopped in Azure, not just marked as stopped in the database.
