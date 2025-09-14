package com.berryfi.portal.repository;

import com.berryfi.portal.entity.VMSessionAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for VMSessionAuditLog entity.
 * Provides workspace-level audit queries for VM session activities.
 */
@Repository
public interface VMSessionAuditLogRepository extends JpaRepository<VMSessionAuditLog, String> {

    /**
     * Find VM session audit logs by workspace ID.
     * This is the primary query for workspace-level audit access.
     */
    Page<VMSessionAuditLog> findByWorkspaceIdOrderByTimestampDesc(String workspaceId, Pageable pageable);

    /**
     * Find VM session audit logs by user ID within a workspace.
     */
    Page<VMSessionAuditLog> findByUserIdAndWorkspaceIdOrderByTimestampDesc(String userId, String workspaceId, Pageable pageable);

    /**
     * Find VM session audit logs by session ID.
     */
    List<VMSessionAuditLog> findBySessionIdOrderByTimestampDesc(String sessionId);

    /**
     * Find VM session audit logs by organization ID (for org-level access).
     */
    Page<VMSessionAuditLog> findByOrganizationIdOrderByTimestampDesc(String organizationId, Pageable pageable);

    /**
     * Find VM session audit logs by action within a workspace.
     */
    Page<VMSessionAuditLog> findByWorkspaceIdAndActionOrderByTimestampDesc(String workspaceId, String action, Pageable pageable);

    /**
     * Find VM session audit logs by VM instance ID within a workspace.
     */
    Page<VMSessionAuditLog> findByWorkspaceIdAndVmInstanceIdOrderByTimestampDesc(String workspaceId, String vmInstanceId, Pageable pageable);

    /**
     * Find VM session audit logs with comprehensive filters for workspace level.
     */
    @Query("SELECT v FROM VMSessionAuditLog v WHERE " +
           "v.workspaceId = :workspaceId AND " +
           "(:userId IS NULL OR v.userId = :userId) AND " +
           "(:sessionId IS NULL OR v.sessionId = :sessionId) AND " +
           "(:action IS NULL OR v.action = :action) AND " +
           "(:vmInstanceId IS NULL OR v.vmInstanceId = :vmInstanceId) AND " +
           "(:startDate IS NULL OR v.timestamp >= :startDate) AND " +
           "(:endDate IS NULL OR v.timestamp <= :endDate) AND " +
           "(:status IS NULL OR v.status = :status) " +
           "ORDER BY v.timestamp DESC")
    Page<VMSessionAuditLog> findByWorkspaceWithFilters(@Param("workspaceId") String workspaceId,
                                                       @Param("userId") String userId,
                                                       @Param("sessionId") String sessionId,
                                                       @Param("action") String action,
                                                       @Param("vmInstanceId") String vmInstanceId,
                                                       @Param("startDate") LocalDateTime startDate,
                                                       @Param("endDate") LocalDateTime endDate,
                                                       @Param("status") String status,
                                                       Pageable pageable);

    /**
     * Find VM session audit logs with comprehensive filters for organization level.
     */
    @Query("SELECT v FROM VMSessionAuditLog v WHERE " +
           "v.organizationId = :organizationId AND " +
           "(:workspaceId IS NULL OR v.workspaceId = :workspaceId) AND " +
           "(:userId IS NULL OR v.userId = :userId) AND " +
           "(:sessionId IS NULL OR v.sessionId = :sessionId) AND " +
           "(:action IS NULL OR v.action = :action) AND " +
           "(:vmInstanceId IS NULL OR v.vmInstanceId = :vmInstanceId) AND " +
           "(:startDate IS NULL OR v.timestamp >= :startDate) AND " +
           "(:endDate IS NULL OR v.timestamp <= :endDate) AND " +
           "(:status IS NULL OR v.status = :status) " +
           "ORDER BY v.timestamp DESC")
    Page<VMSessionAuditLog> findByOrganizationWithFilters(@Param("organizationId") String organizationId,
                                                          @Param("workspaceId") String workspaceId,
                                                          @Param("userId") String userId,
                                                          @Param("sessionId") String sessionId,
                                                          @Param("action") String action,
                                                          @Param("vmInstanceId") String vmInstanceId,
                                                          @Param("startDate") LocalDateTime startDate,
                                                          @Param("endDate") LocalDateTime endDate,
                                                          @Param("status") String status,
                                                          Pageable pageable);

    /**
     * Count VM session audit logs by workspace.
     */
    long countByWorkspaceId(String workspaceId);

    /**
     * Count VM session audit logs by action and workspace.
     */
    long countByWorkspaceIdAndAction(String workspaceId, String action);

    /**
     * Count VM session audit logs by user within workspace.
     */
    long countByWorkspaceIdAndUserId(String workspaceId, String userId);

    /**
     * Count VM session audit logs by organization.
     */
    long countByOrganizationId(String organizationId);

    /**
     * Get VM session audit statistics by workspace.
     */
    @Query("SELECT v.action, COUNT(v) FROM VMSessionAuditLog v WHERE v.workspaceId = :workspaceId GROUP BY v.action")
    List<Object[]> getActionCountsByWorkspace(@Param("workspaceId") String workspaceId);

    /**
     * Get VM session audit statistics by organization.
     */
    @Query("SELECT v.action, COUNT(v) FROM VMSessionAuditLog v WHERE v.organizationId = :organizationId GROUP BY v.action")
    List<Object[]> getActionCountsByOrganization(@Param("organizationId") String organizationId);

    /**
     * Get user activity statistics for a workspace.
     */
    @Query("SELECT v.userId, v.userName, COUNT(v) FROM VMSessionAuditLog v WHERE v.workspaceId = :workspaceId GROUP BY v.userId, v.userName ORDER BY COUNT(v) DESC")
    List<Object[]> getUserActivityByWorkspace(@Param("workspaceId") String workspaceId);

    /**
     * Get most active VM instances in a workspace.
     */
    @Query("SELECT v.vmInstanceId, v.vmInstanceType, COUNT(v) FROM VMSessionAuditLog v " +
           "WHERE v.workspaceId = :workspaceId AND v.vmInstanceId IS NOT NULL " +
           "GROUP BY v.vmInstanceId, v.vmInstanceType ORDER BY COUNT(v) DESC")
    List<Object[]> getVmInstanceActivityByWorkspace(@Param("workspaceId") String workspaceId);

    /**
     * Get recent VM session audit logs for a specific user in workspace.
     */
    @Query("SELECT v FROM VMSessionAuditLog v WHERE v.userId = :userId AND v.workspaceId = :workspaceId " +
           "ORDER BY v.timestamp DESC")
    Page<VMSessionAuditLog> findRecentActivityByUserInWorkspace(@Param("userId") String userId, 
                                                               @Param("workspaceId") String workspaceId, 
                                                               Pageable pageable);

    /**
     * Find VM session audit logs by date range within workspace.
     */
    @Query("SELECT v FROM VMSessionAuditLog v WHERE v.workspaceId = :workspaceId AND " +
           "v.timestamp >= :startDate AND v.timestamp <= :endDate " +
           "ORDER BY v.timestamp DESC")
    Page<VMSessionAuditLog> findByWorkspaceAndDateRange(@Param("workspaceId") String workspaceId,
                                                       @Param("startDate") LocalDateTime startDate,
                                                       @Param("endDate") LocalDateTime endDate,
                                                       Pageable pageable);

    /**
     * Get total credits used from audit logs (for verification/reconciliation).
     */
    @Query("SELECT SUM(v.creditsUsed) FROM VMSessionAuditLog v WHERE v.workspaceId = :workspaceId " +
           "AND v.action = 'VM_SESSION_STOP' AND v.creditsUsed IS NOT NULL")
    Double getTotalCreditsUsedByWorkspace(@Param("workspaceId") String workspaceId);

    /**
     * Get VM session audit logs that failed.
     */
    Page<VMSessionAuditLog> findByWorkspaceIdAndStatusOrderByTimestampDesc(String workspaceId, String status, Pageable pageable);

    /**
     * Get daily activity aggregation for charts/reports.
     */
    @Query("SELECT DATE(v.timestamp), COUNT(v) FROM VMSessionAuditLog v " +
           "WHERE v.workspaceId = :workspaceId AND v.timestamp >= :startDate " +
           "GROUP BY DATE(v.timestamp) ORDER BY DATE(v.timestamp) DESC")
    List<Object[]> getDailyActivityByWorkspace(@Param("workspaceId") String workspaceId,
                                              @Param("startDate") LocalDateTime startDate);
}
