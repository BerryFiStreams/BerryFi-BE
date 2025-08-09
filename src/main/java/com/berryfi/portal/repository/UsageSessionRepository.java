package com.berryfi.portal.repository;

import com.berryfi.portal.entity.UsageSession;
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
 * Repository interface for UsageSession entity.
 */
@Repository
public interface UsageSessionRepository extends JpaRepository<UsageSession, String> {

    /**
     * Find sessions by organization ID
     */
    Page<UsageSession> findByOrganizationIdOrderByStartedAtDesc(
            String organizationId, Pageable pageable);

    /**
     * Find sessions by project ID
     */
    Page<UsageSession> findByProjectIdOrderByStartedAtDesc(
            String projectId, Pageable pageable);

    /**
     * Find sessions by user ID
     */
    Page<UsageSession> findByUserIdOrderByStartedAtDesc(
            String userId, Pageable pageable);

    /**
     * Find active sessions (not ended)
     */
    @Query("SELECT us FROM UsageSession us WHERE us.organizationId = :organizationId " +
           "AND us.endedAt IS NULL " +
           "ORDER BY us.startedAt DESC")
    List<UsageSession> findActiveSessionsByOrganization(@Param("organizationId") String organizationId);

    /**
     * Find sessions within date range
     */
    @Query("SELECT us FROM UsageSession us WHERE us.organizationId = :organizationId " +
           "AND us.startedAt BETWEEN :startDate AND :endDate " +
           "ORDER BY us.startedAt DESC")
    Page<UsageSession> findByOrganizationIdAndDateRange(
            @Param("organizationId") String organizationId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Find session by session ID
     */
    Optional<UsageSession> findBySessionId(String sessionId);

    /**
     * Calculate total duration for organization within date range
     */
    @Query("SELECT COALESCE(SUM(us.durationSeconds), 0) FROM UsageSession us " +
           "WHERE us.organizationId = :organizationId " +
           "AND us.startedAt BETWEEN :startDate AND :endDate " +
           "AND us.endedAt IS NOT NULL")
    Long sumDurationByOrganizationAndDateRange(
            @Param("organizationId") String organizationId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Calculate total credits used for organization within date range
     */
    @Query("SELECT COALESCE(SUM(us.creditsUsed), 0.0) FROM UsageSession us " +
           "WHERE us.organizationId = :organizationId " +
           "AND us.startedAt BETWEEN :startDate AND :endDate")
    Double sumCreditsUsedByOrganizationAndDateRange(
            @Param("organizationId") String organizationId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Count unique users for organization within date range
     */
    @Query("SELECT COUNT(DISTINCT us.userId) FROM UsageSession us " +
           "WHERE us.organizationId = :organizationId " +
           "AND us.startedAt BETWEEN :startDate AND :endDate " +
           "AND us.userId IS NOT NULL")
    Long countUniqueUsersByOrganizationAndDateRange(
            @Param("organizationId") String organizationId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Count sessions for organization within date range
     */
    @Query("SELECT COUNT(us) FROM UsageSession us " +
           "WHERE us.organizationId = :organizationId " +
           "AND us.startedAt BETWEEN :startDate AND :endDate")
    Long countSessionsByOrganizationAndDateRange(
            @Param("organizationId") String organizationId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Get average session duration for organization within date range
     */
    @Query("SELECT AVG(us.durationSeconds) FROM UsageSession us " +
           "WHERE us.organizationId = :organizationId " +
           "AND us.startedAt BETWEEN :startDate AND :endDate " +
           "AND us.endedAt IS NOT NULL")
    Double getAverageSessionDurationByOrganizationAndDateRange(
            @Param("organizationId") String organizationId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find sessions by country
     */
    Page<UsageSession> findByOrganizationIdAndCountryOrderByStartedAtDesc(
            String organizationId, String country, Pageable pageable);

    /**
     * Find sessions by device type
     */
    Page<UsageSession> findByOrganizationIdAndDeviceTypeOrderByStartedAtDesc(
            String organizationId, String deviceType, Pageable pageable);

    /**
     * Get top countries for organization within date range
     */
    @Query("SELECT us.country, COUNT(us) as sessionCount FROM UsageSession us " +
           "WHERE us.organizationId = :organizationId " +
           "AND us.startedAt BETWEEN :startDate AND :endDate " +
           "AND us.country IS NOT NULL " +
           "GROUP BY us.country " +
           "ORDER BY sessionCount DESC")
    List<Object[]> getTopCountriesByOrganizationAndDateRange(
            @Param("organizationId") String organizationId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Get top device types for organization within date range
     */
    @Query("SELECT us.deviceType, COUNT(us) as sessionCount FROM UsageSession us " +
           "WHERE us.organizationId = :organizationId " +
           "AND us.startedAt BETWEEN :startDate AND :endDate " +
           "AND us.deviceType IS NOT NULL " +
           "GROUP BY us.deviceType " +
           "ORDER BY sessionCount DESC")
    List<Object[]> getTopDeviceTypesByOrganizationAndDateRange(
            @Param("organizationId") String organizationId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Get top browsers for organization within date range
     */
    @Query("SELECT us.browser, COUNT(us) as sessionCount FROM UsageSession us " +
           "WHERE us.organizationId = :organizationId " +
           "AND us.startedAt BETWEEN :startDate AND :endDate " +
           "AND us.browser IS NOT NULL " +
           "GROUP BY us.browser " +
           "ORDER BY sessionCount DESC")
    List<Object[]> getTopBrowsersByOrganizationAndDateRange(
            @Param("organizationId") String organizationId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Count total errors for organization within date range
     */
    @Query("SELECT COALESCE(SUM(us.errorCount), 0) FROM UsageSession us " +
           "WHERE us.organizationId = :organizationId " +
           "AND us.startedAt BETWEEN :startDate AND :endDate")
    Long sumErrorsByOrganizationAndDateRange(
            @Param("organizationId") String organizationId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Get average FPS for organization within date range
     */
    @Query("SELECT AVG(us.avgFps) FROM UsageSession us " +
           "WHERE us.organizationId = :organizationId " +
           "AND us.startedAt BETWEEN :startDate AND :endDate " +
           "AND us.avgFps IS NOT NULL")
    Double getAverageFpsByOrganizationAndDateRange(
            @Param("organizationId") String organizationId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Get average bitrate for organization within date range
     */
    @Query("SELECT AVG(us.avgBitrate) FROM UsageSession us " +
           "WHERE us.organizationId = :organizationId " +
           "AND us.startedAt BETWEEN :startDate AND :endDate " +
           "AND us.avgBitrate IS NOT NULL")
    Double getAverageBitrateByOrganizationAndDateRange(
            @Param("organizationId") String organizationId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
