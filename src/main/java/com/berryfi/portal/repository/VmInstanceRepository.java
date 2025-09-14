package com.berryfi.portal.repository;

import com.berryfi.portal.entity.VmInstance;
import com.berryfi.portal.enums.VmStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for VmInstance entity.
 */
@Repository
public interface VmInstanceRepository extends JpaRepository<VmInstance, String> {

    /**
     * Find all VMs in a workspace
     */
    List<VmInstance> findByWorkspaceIdOrderByCreatedAtDesc(String workspaceId);

    /**
     * Find all VMs in a workspace with pagination
     */
    Page<VmInstance> findByWorkspaceId(String workspaceId, Pageable pageable);

    /**
     * Find all available VMs in a workspace
     */
    @Query("SELECT v FROM VmInstance v WHERE v.workspaceId = :workspaceId AND v.status IN ('AVAILABLE', 'STOPPED') ORDER BY v.lastStopped ASC")
    List<VmInstance> findAvailableVmsInWorkspace(@Param("workspaceId") String workspaceId);

    /**
     * Find all VMs in an organization
     */
    List<VmInstance> findByOrganizationIdOrderByCreatedAtDesc(String organizationId);

    /**
     * Find available VMs of a specific type in a workspace
     */
    @Query("SELECT v FROM VmInstance v WHERE v.workspaceId = :workspaceId AND v.vmType = :vmType AND v.status IN ('AVAILABLE', 'STOPPED') ORDER BY v.lastStopped ASC")
    List<VmInstance> findAvailableVmsByTypeInWorkspace(@Param("workspaceId") String workspaceId, @Param("vmType") String vmType);

    /**
     * Find VMs by status
     */
    List<VmInstance> findByStatusOrderByUpdatedAtDesc(VmStatus status);

    /**
     * Find VMs by status in a workspace
     */
    List<VmInstance> findByWorkspaceIdAndStatusOrderByUpdatedAtDesc(String workspaceId, VmStatus status);

    /**
     * Find VM by current session
     */
    Optional<VmInstance> findByCurrentSessionId(String sessionId);

    /**
     * Find VMs currently assigned to a project
     */
    List<VmInstance> findByCurrentProjectIdOrderByLastStartedDesc(String projectId);

    /**
     * Find VMs by Azure resource ID
     */
    Optional<VmInstance> findByAzureResourceId(String azureResourceId);

    /**
     * Count VMs by status in a workspace
     */
    @Query("SELECT COUNT(v) FROM VmInstance v WHERE v.workspaceId = :workspaceId AND v.status = :status")
    long countByWorkspaceIdAndStatus(@Param("workspaceId") String workspaceId, @Param("status") VmStatus status);

    /**
     * Count total VMs in a workspace
     */
    long countByWorkspaceId(String workspaceId);

    /**
     * Find VMs that have been running longer than specified duration
     */
    @Query("SELECT v FROM VmInstance v WHERE v.status = 'RUNNING' AND v.lastStarted < :cutoffTime")
    List<VmInstance> findRunningVmsOlderThan(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Find VMs by type across all workspaces
     */
    List<VmInstance> findByVmTypeOrderByWorkspaceIdAsc(String vmType);

    /**
     * Get VM utilization statistics for a workspace
     */
    @Query("SELECT " +
           "COUNT(v) as total, " +
           "SUM(CASE WHEN v.status = 'RUNNING' THEN 1 ELSE 0 END) as running, " +
           "SUM(CASE WHEN v.status = 'AVAILABLE' THEN 1 ELSE 0 END) as available, " +
           "SUM(CASE WHEN v.status IN ('ASSIGNED', 'STARTING') THEN 1 ELSE 0 END) as starting " +
           "FROM VmInstance v WHERE v.workspaceId = :workspaceId")
    Object[] getWorkspaceVmStats(@Param("workspaceId") String workspaceId);

    /**
     * Find VMs that need attention (error, maintenance, etc.)
     */
    @Query("SELECT v FROM VmInstance v WHERE v.status IN ('ERROR', 'MAINTENANCE') ORDER BY v.updatedAt DESC")
    List<VmInstance> findVmsNeedingAttention();

    /**
     * Check if workspace has any VM of specific type
     */
    boolean existsByWorkspaceIdAndVmType(String workspaceId, String vmType);

    /**
     * Find managed VMs that need Azure status synchronization
     */
    @Query("SELECT v FROM VmInstance v WHERE v.status IN ('ASSIGNED', 'STARTING', 'RUNNING', 'STOPPING') ORDER BY v.updatedAt ASC")
    List<VmInstance> findManagedVmsForSync();
}
