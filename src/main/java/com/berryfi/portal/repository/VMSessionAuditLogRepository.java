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
 * Provides organization-level audit queries for VM session activities.
 */
@Repository
public interface VMSessionAuditLogRepository extends JpaRepository<VMSessionAuditLog, String> {

    /**
     * Find VM session audit logs by organization ID (primary query).
     */
    Page<VMSessionAuditLog> findByOrganizationIdOrderByTimestampDesc(String organizationId, Pageable pageable);

    /**
     * Find VM session audit logs by user ID within an organization.
     */
    Page<VMSessionAuditLog> findByUserIdAndOrganizationIdOrderByTimestampDesc(String userId, String organizationId, Pageable pageable);

    /**
     * Find VM session audit logs by session ID.
     */
    List<VMSessionAuditLog> findBySessionIdOrderByTimestampDesc(String sessionId);

    /**
     * Find VM session audit logs by action within an organization.
     */
    Page<VMSessionAuditLog> findByOrganizationIdAndActionOrderByTimestampDesc(String organizationId, String action, Pageable pageable);

    /**
     * Find VM session audit logs by VM instance ID within an organization.
     */
    Page<VMSessionAuditLog> findByOrganizationIdAndVmInstanceIdOrderByTimestampDesc(String organizationId, String vmInstanceId, Pageable pageable);

    /**
     * Find VM session audit logs with comprehensive filters for organization level.
     */
    @Query("SELECT v FROM VMSessionAuditLog v WHERE " +
           "v.organizationId = :organizationId AND " +
           "(:userId IS NULL OR v.userId = :userId) AND " +
           "(:sessionId IS NULL OR v.sessionId = :sessionId) AND " +
           "(:action IS NULL OR v.action = :action) AND " +
           "(:vmInstanceId IS NULL OR v.vmInstanceId = :vmInstanceId) AND " +
           "(:startDate IS NULL OR v.timestamp >= :startDate) AND " +
           "(:endDate IS NULL OR v.timestamp <= :endDate) AND " +
           "(:status IS NULL OR v.status = :status) " +
           "ORDER BY v.timestamp DESC")
    Page<VMSessionAuditLog> findByOrganizationWithFilters(@Param("organizationId") String organizationId,
                                                          @Param("userId") String userId,
                                                          @Param("sessionId") String sessionId,
                                                          @Param("action") String action,
                                                          @Param("vmInstanceId") String vmInstanceId,
                                                          @Param("startDate") LocalDateTime startDate,
                                                          @Param("endDate") LocalDateTime endDate,
                                                          @Param("status") String status,
                                                          Pageable pageable);

    /**
     * Count VM session audit logs by organization.
     */
    long countByOrganizationId(String organizationId);

    /**
     * Get VM session audit statistics by organization.
     */
    @Query("SELECT v.action, COUNT(v) FROM VMSessionAuditLog v WHERE v.organizationId = :organizationId GROUP BY v.action")
    List<Object[]> getActionCountsByOrganization(@Param("organizationId") String organizationId);
}
