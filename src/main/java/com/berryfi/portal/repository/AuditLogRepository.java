package com.berryfi.portal.repository;

import com.berryfi.portal.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for AuditLog entity.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, String> {

    /**
     * Find audit logs by user ID.
     */
    Page<AuditLog> findByUserIdOrderByTimestampDesc(String userId, Pageable pageable);

    /**
     * Find audit logs by organization ID.
     */
    Page<AuditLog> findByOrganizationIdOrderByTimestampDesc(String organizationId, Pageable pageable);

    /**
     * Find audit logs by user ID and organization ID.
     */
    Page<AuditLog> findByUserIdAndOrganizationIdOrderByTimestampDesc(String userId, String organizationId, Pageable pageable);

    /**
     * Find audit logs by action and organization ID.
     */
    Page<AuditLog> findByActionAndOrganizationIdOrderByTimestampDesc(String action, String organizationId, Pageable pageable);

    /**
     * Find audit logs by resource and organization ID.
     */
    Page<AuditLog> findByResourceAndOrganizationIdOrderByTimestampDesc(String resource, String organizationId, Pageable pageable);

    /**
     * Find audit logs by project ID (for URL tracking).
     */
    Page<AuditLog> findByProjectIdOrderByTimestampDesc(String projectId, Pageable pageable);

    /**
     * Find URL access logs by user and project.
     */
    @Query("SELECT a FROM AuditLog a WHERE a.userId = :userId AND a.projectId = :projectId AND a.action = 'URL_ACCESS' ORDER BY a.timestamp DESC")
    Page<AuditLog> findUrlAccessLogsByUserAndProject(@Param("userId") String userId, @Param("projectId") String projectId, Pageable pageable);

    /**
     * Find audit logs with filters.
     */
    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:organizationId IS NULL OR a.organizationId = :organizationId) AND " +
           "(:userId IS NULL OR a.userId = :userId) AND " +
           "(:action IS NULL OR a.action = :action) AND " +
           "(:resource IS NULL OR a.resource = :resource) AND " +
           "(:startDate IS NULL OR a.timestamp >= :startDate) AND " +
           "(:endDate IS NULL OR a.timestamp <= :endDate) " +
           "ORDER BY a.timestamp DESC")
    Page<AuditLog> findWithFilters(@Param("organizationId") String organizationId,
                                   @Param("userId") String userId,
                                   @Param("action") String action,
                                   @Param("resource") String resource,
                                   @Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate,
                                   Pageable pageable);

    /**
     * Count total logs by organization.
     */
    long countByOrganizationId(String organizationId);

    /**
     * Count logs by action and organization.
     */
    long countByActionAndOrganizationId(String action, String organizationId);

    /**
     * Get URL access count for a project and user.
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.userId = :userId AND a.projectId = :projectId AND a.action = 'URL_ACCESS'")
    long countUrlAccessByUserAndProject(@Param("userId") String userId, @Param("projectId") String projectId);

    /**
     * Get unique referrer URLs for a project.
     */
    @Query("SELECT DISTINCT a.referrerUrl FROM AuditLog a WHERE a.projectId = :projectId AND a.action = 'URL_ACCESS' AND a.referrerUrl IS NOT NULL")
    List<String> findUniqueReferrersByProject(@Param("projectId") String projectId);
}
