package com.berryfi.portal.repository;

import com.berryfi.portal.entity.VmSession;
import com.berryfi.portal.enums.SessionStatus;
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
 * Repository interface for VmSession entity.
 */
@Repository
public interface VmSessionRepository extends JpaRepository<VmSession, String> {

    /**
     * Find sessions by project
     */
    Page<VmSession> findByProjectIdOrderByStartTimeDesc(String projectId, Pageable pageable);



    /**
     * Find sessions by user
     */
    Page<VmSession> findByUserIdOrderByStartTimeDesc(String userId, Pageable pageable);

    /**
     * Find sessions by VM instance
     */
    List<VmSession> findByVmInstanceIdOrderByStartTimeDesc(String vmInstanceId);

    /**
     * Find active sessions
     */
    @Query("SELECT s FROM VmSession s JOIN FETCH s.vmInstance WHERE s.status IN ('REQUESTED', 'STARTING', 'ACTIVE') ORDER BY s.startTime DESC")
    List<VmSession> findActiveSessions();



    /**
     * Find active session for a VM (including sessions that are terminating to prevent race conditions)
     */
    @Query("SELECT s FROM VmSession s WHERE s.vmInstanceId = :vmInstanceId AND s.status IN ('REQUESTED', 'STARTING', 'ACTIVE', 'TERMINATING')")
    Optional<VmSession> findActiveSessionForVm(@Param("vmInstanceId") String vmInstanceId);

    /**
     * Find sessions by status
     */
    List<VmSession> findByStatusOrderByStartTimeDesc(SessionStatus status);

    /**
     * Find ACTIVE and STARTING sessions that have timed out (no heartbeat for 30+ seconds)
     * Targets sessions that are in ACTIVE or STARTING status. Sessions without any heartbeat
     * are considered timed out if they've been active for at least minActiveTime.
     * Sessions with heartbeats are timed out if the last heartbeat was before cutoffTime.
     * Ensures startTime has been updated after VM actually started (not equal to createdAt).
     */
    @Query("SELECT s FROM VmSession s WHERE s.status IN ('ACTIVE', 'STARTING') " +
           "AND s.startTime <> s.createdAt " +
           "AND (s.startTime < :minActiveTime) " +
           "AND (s.lastHeartbeat IS NULL OR s.lastHeartbeat < :cutoffTime)")
    List<VmSession> findTimedOutSessions(@Param("cutoffTime") LocalDateTime cutoffTime, 
                                        @Param("minActiveTime") LocalDateTime minActiveTime);

    /**
     * Find sessions needing heartbeat (haven't sent one recently)
     */
    @Query("SELECT s FROM VmSession s WHERE s.status = 'ACTIVE' AND (s.lastHeartbeat IS NULL OR s.lastHeartbeat < :cutoffTime)")
    List<VmSession> findSessionsNeedingHeartbeat(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Find sessions by date range
     */
    @Query("SELECT s FROM VmSession s WHERE s.startTime BETWEEN :startDate AND :endDate ORDER BY s.startTime DESC")
    Page<VmSession> findSessionsByDateRange(@Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate, 
                                          Pageable pageable);









    /**
     * Count sessions by user in date range
     */
    @Query("SELECT COUNT(s) FROM VmSession s WHERE s.userId = :userId AND s.startTime BETWEEN :startDate AND :endDate")
    long countUserSessionsInDateRange(@Param("userId") String userId, 
                                    @Param("startDate") LocalDateTime startDate, 
                                    @Param("endDate") LocalDateTime endDate);



    /**
     * Get total credits used by project in date range
     */
    @Query("SELECT SUM(s.creditsUsed) FROM VmSession s WHERE s.projectId = :projectId AND s.startTime BETWEEN :startDate AND :endDate")
    Double getTotalCreditsUsedByProject(@Param("projectId") String projectId,
                                      @Param("startDate") LocalDateTime startDate, 
                                      @Param("endDate") LocalDateTime endDate);

    /**
     * Get total credits used by project (all time) - COMPLETED sessions only
     */
    @Query("SELECT COALESCE(SUM(s.creditsUsed), 0.0) FROM VmSession s WHERE s.projectId = :projectId AND s.status = 'COMPLETED'")
    Double getTotalCreditsUsedByProjectAllTime(@Param("projectId") String projectId);

    /**
     * Count total sessions by project (all time) - COMPLETED sessions only
     */
    @Query("SELECT COUNT(s) FROM VmSession s WHERE s.projectId = :projectId AND s.status = 'COMPLETED'")
    Long countSessionsByProject(@Param("projectId") String projectId);

    /**
     * Get total duration in seconds by project (all time) - COMPLETED sessions only
     */
    @Query("SELECT COALESCE(SUM(s.durationSeconds), 0) FROM VmSession s WHERE s.projectId = :projectId AND s.status = 'COMPLETED' AND s.durationSeconds IS NOT NULL")
    Long getTotalDurationSecondsByProject(@Param("projectId") String projectId);



    /**
     * Find long-running sessions
     */
    @Query("SELECT s FROM VmSession s WHERE s.status IN ('ACTIVE', 'STARTING') AND s.startTime < :cutoffTime ORDER BY s.startTime ASC")
    List<VmSession> findLongRunningSessions(@Param("cutoffTime") LocalDateTime cutoffTime);



    /**
     * Find user's current active session
     */
    @Query("SELECT s FROM VmSession s WHERE s.userId = :userId AND s.status IN ('REQUESTED', 'STARTING', 'ACTIVE') ORDER BY s.startTime DESC")
    Optional<VmSession> findUserActiveSession(@Param("userId") String userId);

    /**
     * Find active sessions that need Azure status synchronization
     */
    @Query("SELECT s FROM VmSession s JOIN FETCH s.vmInstance WHERE s.status IN ('ACTIVE', 'STARTING') ORDER BY s.lastHeartbeat ASC")
    List<VmSession> findActiveSessionsForSync();

    /**
     * Get session statistics for a project by organization
     */
    @Query("SELECT " +
           "COUNT(s) as totalSessions, " +
           "COALESCE(SUM(s.creditsUsed), 0.0) as totalCreditsUsed, " +
           "MAX(s.startTime) as lastUsed " +
           "FROM VmSession s " +
           "WHERE s.projectId = :projectId AND s.organizationId = :organizationId " +
           "AND s.status IN ('COMPLETED', 'TERMINATED')")
    Object[] getProjectUsageByOrganization(@Param("projectId") String projectId, 
                                         @Param("organizationId") String organizationId);

    /**
     * Count sessions for a project by organization
     */
    @Query("SELECT COUNT(s) FROM VmSession s WHERE s.projectId = :projectId AND s.organizationId = :organizationId AND s.status IN ('COMPLETED', 'TERMINATED')")
    Long countSessionsByProjectAndOrganization(@Param("projectId") String projectId, 
                                             @Param("organizationId") String organizationId);

    /**
     * Get total credits used for a project by organization
     */
    @Query("SELECT COALESCE(SUM(s.creditsUsed), 0.0) FROM VmSession s WHERE s.projectId = :projectId AND s.organizationId = :organizationId AND s.status IN ('COMPLETED', 'TERMINATED')")
    Double getTotalCreditsUsedByProjectAndOrganization(@Param("projectId") String projectId, 
                                                      @Param("organizationId") String organizationId);

    /**
     * Get last usage date for a project by organization
     */
    @Query("SELECT MAX(s.startTime) FROM VmSession s WHERE s.projectId = :projectId AND s.organizationId = :organizationId AND s.status IN ('COMPLETED', 'TERMINATED')")
    LocalDateTime getLastUsageByProjectAndOrganization(@Param("projectId") String projectId, 
                                                      @Param("organizationId") String organizationId);
}
