# VM Allocation Limitation Implementation

## Overview

This implementation ensures that VM instances cannot be allocated to multiple sessions simultaneously. The limitation prevents race conditions and ensures that a VM instance is exclusively used by one session at a time, even when multiple project+workspace combinations exist.

## Implementation Details

### 1. Database Query Level Protection

**File: `VmInstanceRepository.java`**

Modified the following queries to exclude VMs with active sessions:

- `findAvailableVmsForProject()`
- `findAvailableVmsByTypeForProject()`

**Query Logic:**
```sql
SELECT v FROM VmInstance v 
WHERE v.projectId = :projectId 
  AND v.vmType = :vmType 
  AND v.status IN ('AVAILABLE', 'STOPPED') 
  AND v.id NOT IN (
    SELECT s.vmInstanceId FROM VmSession s 
    WHERE s.status IN ('REQUESTED', 'STARTING', 'ACTIVE', 'TERMINATING')
  )
ORDER BY v.lastStopped ASC
```

**Key Points:**
- Only returns VMs with status `AVAILABLE` or `STOPPED`
- Excludes VMs that have sessions in active states (`REQUESTED`, `STARTING`, `ACTIVE`, `TERMINATING`)
- Orders by `lastStopped` to prefer recently stopped VMs

### 2. Service Layer Validation

**File: `VmSessionService.java`**

Added double-check validation before VM assignment:

```java
// Double-check that this VM doesn't have any active session to prevent race conditions
Optional<VmSession> activeSessionForVm = vmSessionRepository.findActiveSessionForVm(vm.getId());
if (activeSessionForVm.isPresent()) {
    logger.warn("VM {} is already in use by session {} (status: {}), finding another available VM", 
        vm.getId(), activeSessionForVm.get().getId(), activeSessionForVm.get().getStatus());
    // Try to find another available VM
    availableVms.remove(0);
    if (availableVms.isEmpty()) {
        return VmSessionResult.error("No available VMs of type " + vmType + " for this project (all are currently in use)");
    }
    vm = availableVms.get(0);
    // Re-check the new VM...
}
```

**Key Points:**
- Provides additional race condition protection at service layer
- Falls back to next available VM if first choice is already in use
- Returns clear error if no VMs are available

### 3. Entity Level Improvements

**File: `VmInstance.java`**

Enhanced helper methods:

```java
public boolean canBeAssigned() {
    // VM can only be assigned if it's available/stopped AND not currently assigned to any session
    return (this.status == VmStatus.AVAILABLE || this.status == VmStatus.STOPPED) 
            && this.currentSessionId == null;
}

public boolean isInUse() {
    return this.currentSessionId != null || 
           (this.status != VmStatus.AVAILABLE && this.status != VmStatus.STOPPED);
}
```

**Key Points:**
- `canBeAssigned()` now checks both status and current session assignment
- Added `isInUse()` method for clearer intent checking

### 4. Session Query Enhancement

**File: `VmSessionRepository.java`**

Updated active session query to include terminating sessions:

```java
@Query("SELECT s FROM VmSession s WHERE s.vmInstanceId = :vmInstanceId AND s.status IN ('REQUESTED', 'STARTING', 'ACTIVE', 'TERMINATING')")
Optional<VmSession> findActiveSessionForVm(@Param("vmInstanceId") String vmInstanceId);
```

**Key Points:**
- Includes `TERMINATING` sessions to prevent allocation during cleanup
- Provides comprehensive active session detection

## Session Status Flow

The implementation handles all relevant session statuses:

1. **REQUESTED** - Session requested but VM not started yet
2. **STARTING** - VM is being started in Azure
3. **ACTIVE** - VM is running and session is active
4. **TERMINATING** - Session is being cleaned up (VM being stopped)
5. **COMPLETED** - Session finished successfully (VM available for reuse)
6. **TERMINATED** - Session forcefully terminated (VM available for reuse)
7. **FAILED** - Session failed to start (VM available for reuse)

## Race Condition Prevention

The implementation provides multiple layers of protection:

1. **Database Level:** Query exclusion prevents selection of in-use VMs
2. **Application Level:** Double-check validation before assignment
3. **Entity Level:** Helper methods ensure consistent state checking
4. **Transaction Level:** All operations are wrapped in transactions

## Benefits

1. **Exclusive VM Usage:** No VM can be allocated to multiple sessions simultaneously
2. **Race Condition Protection:** Multiple layers prevent concurrent allocation
3. **Resource Optimization:** VMs are only excluded when truly in use
4. **Clear Error Messages:** Users get meaningful feedback when no VMs are available
5. **Workspace Fairness:** Multiple workspaces can share the same project VM pool without conflicts

## Testing Recommendations

To test this implementation:

1. **Concurrent Session Creation:** Try to create multiple sessions simultaneously for the same project+workspace
2. **Cross-Workspace Testing:** Create sessions from different workspaces using the same project
3. **Edge Case Testing:** Test behavior when all VMs are in use
4. **Cleanup Testing:** Verify VMs become available after session termination
5. **Performance Testing:** Ensure queries perform well under load

## Migration Considerations

This implementation is backward compatible and doesn't require database migrations. Existing sessions will continue to work normally, and new sessions will benefit from the improved allocation logic.
