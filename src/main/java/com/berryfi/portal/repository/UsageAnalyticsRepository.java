package com.berryfi.portal.repository;

import com.berryfi.portal.entity.UsageAnalytics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for UsageAnalytics entity.
 */
@Repository
public interface UsageAnalyticsRepository extends JpaRepository<UsageAnalytics, String> {

    /**
     * Find analytics by organization ID
     */
    Page<UsageAnalytics> findByOrganizationIdOrderByDateDesc(
            String organizationId, Pageable pageable);

    /**
     * Find analytics by organization ID and date range
     */
    @Query("SELECT ua FROM UsageAnalytics ua WHERE ua.organizationId = :organizationId " +
           "AND ua.date BETWEEN :startDate AND :endDate " +
           "ORDER BY ua.date DESC")
    List<UsageAnalytics> findByOrganizationIdAndDateRange(
            @Param("organizationId") String organizationId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find analytics by organization and specific date
     */
    Optional<UsageAnalytics> findByOrganizationIdAndDate(String organizationId, LocalDate date);

    /**
     * Find analytics by project ID
     */
    Page<UsageAnalytics> findByProjectIdOrderByDateDesc(String projectId, Pageable pageable);

    /**
     * Find analytics by workspace ID
     */
    Page<UsageAnalytics> findByWorkspaceIdOrderByDateDesc(String workspaceId, Pageable pageable);

    /**
     * Find analytics by user ID
     */
    Page<UsageAnalytics> findByUserIdOrderByDateDesc(String userId, Pageable pageable);

    /**
     * Sum total sessions for organization within date range
     */
    @Query("SELECT COALESCE(SUM(ua.totalSessions), 0) FROM UsageAnalytics ua " +
           "WHERE ua.organizationId = :organizationId " +
           "AND ua.date BETWEEN :startDate AND :endDate")
    Long sumTotalSessionsByOrganizationAndDateRange(
            @Param("organizationId") String organizationId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Sum total duration for organization within date range
     */
    @Query("SELECT COALESCE(SUM(ua.totalDurationSeconds), 0) FROM UsageAnalytics ua " +
           "WHERE ua.organizationId = :organizationId " +
           "AND ua.date BETWEEN :startDate AND :endDate")
    Long sumTotalDurationByOrganizationAndDateRange(
            @Param("organizationId") String organizationId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Sum total credits used for organization within date range
     */
    @Query("SELECT COALESCE(SUM(ua.totalCreditsUsed), 0.0) FROM UsageAnalytics ua " +
           "WHERE ua.organizationId = :organizationId " +
           "AND ua.date BETWEEN :startDate AND :endDate")
    Double sumTotalCreditsUsedByOrganizationAndDateRange(
            @Param("organizationId") String organizationId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Get average session duration for organization within date range
     */
    @Query("SELECT AVG(ua.avgSessionDuration) FROM UsageAnalytics ua " +
           "WHERE ua.organizationId = :organizationId " +
           "AND ua.date BETWEEN :startDate AND :endDate " +
           "AND ua.avgSessionDuration IS NOT NULL")
    Double getAverageSessionDurationByOrganizationAndDateRange(
            @Param("organizationId") String organizationId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Get maximum peak concurrent sessions for organization within date range
     */
    @Query("SELECT MAX(ua.peakConcurrentSessions) FROM UsageAnalytics ua " +
           "WHERE ua.organizationId = :organizationId " +
           "AND ua.date BETWEEN :startDate AND :endDate")
    Integer getMaxPeakConcurrentSessionsByOrganizationAndDateRange(
            @Param("organizationId") String organizationId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Sum total errors for organization within date range
     */
    @Query("SELECT COALESCE(SUM(ua.totalErrors), 0) FROM UsageAnalytics ua " +
           "WHERE ua.organizationId = :organizationId " +
           "AND ua.date BETWEEN :startDate AND :endDate")
    Long sumTotalErrorsByOrganizationAndDateRange(
            @Param("organizationId") String organizationId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Get monthly analytics for organization
     */
    @Query("SELECT ua FROM UsageAnalytics ua WHERE ua.organizationId = :organizationId " +
           "AND EXTRACT(YEAR FROM ua.date) = :year " +
           "AND EXTRACT(MONTH FROM ua.date) = :month " +
           "ORDER BY ua.date ASC")
    List<UsageAnalytics> findMonthlyAnalytics(
            @Param("organizationId") String organizationId,
            @Param("year") int year,
            @Param("month") int month);

    /**
     * Get yearly analytics for organization
     */
    @Query("SELECT ua FROM UsageAnalytics ua WHERE ua.organizationId = :organizationId " +
           "AND EXTRACT(YEAR FROM ua.date) = :year " +
           "ORDER BY ua.date ASC")
    List<UsageAnalytics> findYearlyAnalytics(
            @Param("organizationId") String organizationId,
            @Param("year") int year);

    /**
     * Find latest analytics for organization
     */
    Optional<UsageAnalytics> findFirstByOrganizationIdOrderByDateDesc(String organizationId);

    /**
     * Get analytics for multiple organizations within date range
     */
    @Query("SELECT ua FROM UsageAnalytics ua WHERE ua.organizationId IN :organizationIds " +
           "AND ua.date BETWEEN :startDate AND :endDate " +
           "ORDER BY ua.organizationId, ua.date DESC")
    List<UsageAnalytics> findByOrganizationIdsAndDateRange(
            @Param("organizationIds") List<String> organizationIds,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Get top organizations by total sessions within date range
     */
    @Query("SELECT ua.organizationId, SUM(ua.totalSessions) as totalSessions " +
           "FROM UsageAnalytics ua WHERE ua.date BETWEEN :startDate AND :endDate " +
           "GROUP BY ua.organizationId " +
           "ORDER BY totalSessions DESC")
    List<Object[]> getTopOrganizationsByTotalSessions(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    /**
     * Get top organizations by total credits used within date range
     */
    @Query("SELECT ua.organizationId, SUM(ua.totalCreditsUsed) as totalCreditsUsed " +
           "FROM UsageAnalytics ua WHERE ua.date BETWEEN :startDate AND :endDate " +
           "GROUP BY ua.organizationId " +
           "ORDER BY totalCreditsUsed DESC")
    List<Object[]> getTopOrganizationsByCreditsUsed(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    /**
     * Count analytics records for organization
     */
    Long countByOrganizationId(String organizationId);

    /**
     * Delete old analytics records before date
     */
    @Query("DELETE FROM UsageAnalytics ua WHERE ua.date < :beforeDate")
    void deleteAnalyticsBeforeDate(@Param("beforeDate") LocalDate beforeDate);
}
