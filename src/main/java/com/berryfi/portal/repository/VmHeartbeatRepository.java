package com.berryfi.portal.repository;

import com.berryfi.portal.entity.VmHeartbeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for VmHeartbeat entity.
 */
@Repository
public interface VmHeartbeatRepository extends JpaRepository<VmHeartbeat, String> {

    /**
     * Find heartbeats by session
     */
    List<VmHeartbeat> findBySessionIdOrderByTimestampDesc(String sessionId);

    /**
     * Find latest heartbeat for a session
     */
    Optional<VmHeartbeat> findFirstBySessionIdOrderByTimestampDesc(String sessionId);

    /**
     * Find heartbeats by session in date range
     */
    @Query("SELECT h FROM VmHeartbeat h WHERE h.sessionId = :sessionId AND h.timestamp BETWEEN :startTime AND :endTime ORDER BY h.timestamp DESC")
    List<VmHeartbeat> findBySessionIdAndTimeRange(@Param("sessionId") String sessionId,
                                                 @Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime);

    /**
     * Find heartbeats by status
     */
    List<VmHeartbeat> findByStatusOrderByTimestampDesc(String status);

    /**
     * Find recent heartbeats (last N minutes)
     */
    @Query("SELECT h FROM VmHeartbeat h WHERE h.timestamp > :cutoffTime ORDER BY h.timestamp DESC")
    List<VmHeartbeat> findRecentHeartbeats(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Find heartbeats with high CPU usage
     */
    @Query("SELECT h FROM VmHeartbeat h WHERE h.cpuUsage > :threshold ORDER BY h.timestamp DESC")
    List<VmHeartbeat> findHighCpuUsageHeartbeats(@Param("threshold") Double threshold);

    /**
     * Find heartbeats with high memory usage
     */
    @Query("SELECT h FROM VmHeartbeat h WHERE h.memoryUsage > :threshold ORDER BY h.timestamp DESC")
    List<VmHeartbeat> findHighMemoryUsageHeartbeats(@Param("threshold") Double threshold);

    /**
     * Count heartbeats for a session
     */
    long countBySessionId(String sessionId);

    /**
     * Count heartbeats for a session in date range
     */
    @Query("SELECT COUNT(h) FROM VmHeartbeat h WHERE h.sessionId = :sessionId AND h.timestamp BETWEEN :startTime AND :endTime")
    long countBySessionIdAndTimeRange(@Param("sessionId") String sessionId,
                                    @Param("startTime") LocalDateTime startTime,
                                    @Param("endTime") LocalDateTime endTime);

    /**
     * Get average system metrics for a session
     */
    @Query("SELECT " +
           "AVG(h.cpuUsage) as avgCpu, " +
           "AVG(h.memoryUsage) as avgMemory, " +
           "AVG(h.diskUsage) as avgDisk, " +
           "MAX(h.cpuUsage) as maxCpu, " +
           "MAX(h.memoryUsage) as maxMemory " +
           "FROM VmHeartbeat h WHERE h.sessionId = :sessionId")
    Object[] getSessionMetricsStats(@Param("sessionId") String sessionId);

    /**
     * Delete old heartbeats older than specified date
     */
    @Query("DELETE FROM VmHeartbeat h WHERE h.timestamp < :cutoffDate")
    void deleteOldHeartbeats(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find sessions with no recent heartbeats
     */
    @Query("SELECT DISTINCT h.sessionId FROM VmHeartbeat h WHERE h.sessionId NOT IN " +
           "(SELECT h2.sessionId FROM VmHeartbeat h2 WHERE h2.timestamp > :cutoffTime)")
    List<String> findSessionsWithoutRecentHeartbeats(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Get heartbeat frequency for a session (heartbeats per hour)
     */
    @Query("SELECT COUNT(h) FROM VmHeartbeat h WHERE h.sessionId = :sessionId AND h.timestamp > :oneHourAgo")
    long getHeartbeatFrequency(@Param("sessionId") String sessionId, @Param("oneHourAgo") LocalDateTime oneHourAgo);
}
