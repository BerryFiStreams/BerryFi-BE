package com.berryfi.portal.repository;

import com.berryfi.portal.entity.BillingTransaction;
import com.berryfi.portal.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository interface for BillingTransaction entity.
 */
@Repository
public interface BillingTransactionRepository extends JpaRepository<BillingTransaction, String> {

    /**
     * Find transactions by organization ID
     */
    Page<BillingTransaction> findByOrganizationIdOrderByDateDesc(
            String organizationId, Pageable pageable);

    /**
     * Find transactions by organization ID and type
     */
    Page<BillingTransaction> findByOrganizationIdAndTypeOrderByDateDesc(
            String organizationId, TransactionType type, Pageable pageable);

    /**
     * Find transactions by organization ID within date range
     */
    @Query("SELECT bt FROM BillingTransaction bt WHERE bt.organizationId = :organizationId " +
           "AND bt.date BETWEEN :startDate AND :endDate " +
           "ORDER BY bt.date DESC")
    Page<BillingTransaction> findByOrganizationIdAndDateRange(
            @Param("organizationId") String organizationId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Calculate total credits used by organization within date range
     */
    @Query("SELECT COALESCE(SUM(bt.amount), 0.0) FROM BillingTransaction bt " +
           "WHERE bt.organizationId = :organizationId " +
           "AND bt.type = :type " +
           "AND bt.date BETWEEN :startDate AND :endDate")
    Double sumCreditsUsedByOrganizationAndDateRange(
            @Param("organizationId") String organizationId,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Calculate total amount spent by organization within date range
     */
    @Query("SELECT COALESCE(SUM(bt.amount), 0.0) FROM BillingTransaction bt " +
           "WHERE bt.organizationId = :organizationId " +
           "AND bt.date BETWEEN :startDate AND :endDate")
    Double sumAmountByOrganizationAndDateRange(
            @Param("organizationId") String organizationId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Get last transaction for organization
     */
    Optional<BillingTransaction> findFirstByOrganizationIdOrderByDateDesc(
            String organizationId);

    /**
     * Get monthly spending for organization
     */
    @Query("SELECT COALESCE(SUM(bt.amount), 0.0) FROM BillingTransaction bt " +
           "WHERE bt.organizationId = :organizationId " +
           "AND EXTRACT(YEAR FROM bt.date) = :year " +
           "AND EXTRACT(MONTH FROM bt.date) = :month")
    Double getMonthlySpending(
            @Param("organizationId") String organizationId,
            @Param("year") int year,
            @Param("month") int month);

    /**
     * Get total credits balance for organization
     */
    @Query("SELECT COALESCE(SUM(CASE WHEN bt.type = 'CREDIT_ADDED' THEN bt.amount " +
           "WHEN bt.type = 'ALLOCATION' THEN bt.amount " +
           "WHEN bt.type = 'USAGE' THEN -bt.amount " +
           "WHEN bt.type = 'REFUND' THEN bt.amount " +
           "ELSE 0 END), 0.0) FROM BillingTransaction bt " +
           "WHERE bt.organizationId = :organizationId")
    Double getCurrentCreditsBalance(@Param("organizationId") String organizationId);

    /**
     * Count transactions by organization and type within date range
     */
    @Query("SELECT COUNT(bt) FROM BillingTransaction bt " +
           "WHERE bt.organizationId = :organizationId " +
           "AND bt.type = :type " +
           "AND bt.date BETWEEN :startDate AND :endDate")
    Long countByOrganizationAndTypeAndDateRange(
            @Param("organizationId") String organizationId,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find recent transactions for organization
     */
    @Query("SELECT bt FROM BillingTransaction bt WHERE bt.organizationId = :organizationId " +
           "ORDER BY bt.date DESC")
    Page<BillingTransaction> findRecentTransactions(
            @Param("organizationId") String organizationId, Pageable pageable);
}
