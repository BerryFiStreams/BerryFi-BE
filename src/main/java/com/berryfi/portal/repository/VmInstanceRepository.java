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
     * Find all VMs for a project
     */
    List<VmInstance> findByProjectIdOrderByCreatedAtDesc(String projectId);

    /**
     * Find all VMs for a project with pagination
     */
    Page<VmInstance> findByProjectId(String projectId, Pageable pageable);

    /**
     * Find all available VMs for a project (excludes VMs with active sessions)
     * This ensures no VM is allocated to multiple sessions simultaneously
     */
    @Query("SELECT v FROM VmInstance v WHERE v.projectId = :projectId " +
           "AND v.status IN ('AVAILABLE', 'STOPPED') " +
           "AND v.id NOT IN (SELECT s.vmInstanceId FROM VmSession s WHERE s.status IN ('REQUESTED', 'STARTING', 'ACTIVE', 'TERMINATING')) " +
           "ORDER BY v.lastStopped ASC")
    List<VmInstance> findAvailableVmsForProject(@Param("projectId") String projectId);

    /**
     * Find available VMs of a specific type for a project (excludes VMs with active sessions)
     * This ensures no VM is allocated to multiple sessions simultaneously
     */
    @Query("SELECT v FROM VmInstance v WHERE v.projectId = :projectId " +
           "AND v.vmType = :vmType " +
           "AND v.status IN ('AVAILABLE', 'STOPPED') " +
           "AND v.id NOT IN (SELECT s.vmInstanceId FROM VmSession s WHERE s.status IN ('REQUESTED', 'STARTING', 'ACTIVE', 'TERMINATING')) " +
           "ORDER BY v.lastStopped ASC")
    List<VmInstance> findAvailableVmsByTypeForProject(@Param("projectId") String projectId, @Param("vmType") String vmType);

    /**
     * Find VMs by status
     */
    List<VmInstance> findByStatusOrderByUpdatedAtDesc(VmStatus status);

    /**
     * Find VMs by status for a project
     */
    List<VmInstance> findByProjectIdAndStatusOrderByUpdatedAtDesc(String projectId, VmStatus status);

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
     * Count VMs by status for a project
     */
    @Query("SELECT COUNT(v) FROM VmInstance v WHERE v.projectId = :projectId AND v.status = :status")
    long countByProjectIdAndStatus(@Param("projectId") String projectId, @Param("status") VmStatus status);

    /**
     * Count total VMs for a project
     */
    long countByProjectId(String projectId);

    /**
     * Find VMs that have been running longer than specified duration
     */
    @Query("SELECT v FROM VmInstance v WHERE v.status = 'RUNNING' AND v.lastStarted < :cutoffTime")
    List<VmInstance> findRunningVmsOlderThan(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Find VMs by type for a specific project
     */
    List<VmInstance> findByProjectIdAndVmTypeOrderByCreatedAtAsc(String projectId, String vmType);

    /**
     * Get VM utilization statistics for a project
     */
    @Query("SELECT " +
           "COUNT(v) as total, " +
           "SUM(CASE WHEN v.status = 'RUNNING' THEN 1 ELSE 0 END) as running, " +
           "SUM(CASE WHEN v.status = 'AVAILABLE' THEN 1 ELSE 0 END) as available, " +
           "SUM(CASE WHEN v.status IN ('ASSIGNED', 'STARTING') THEN 1 ELSE 0 END) as starting " +
           "FROM VmInstance v WHERE v.projectId = :projectId")
    Object[] getProjectVmStats(@Param("projectId") String projectId);

    /**
     * Find VMs that need attention (error, maintenance, etc.)
     */
    @Query("SELECT v FROM VmInstance v WHERE v.status IN ('ERROR', 'MAINTENANCE') ORDER BY v.updatedAt DESC")
    List<VmInstance> findVmsNeedingAttention();

    /**
     * Check if project has any VM of specific type
     */
    boolean existsByProjectIdAndVmType(String projectId, String vmType);

    /**
     * Find managed VMs that need Azure status synchronization
     */
    @Query("SELECT v FROM VmInstance v WHERE v.status IN ('ASSIGNED', 'STARTING', 'RUNNING', 'STOPPING') ORDER BY v.updatedAt ASC")
    List<VmInstance> findManagedVmsForSync();
}
