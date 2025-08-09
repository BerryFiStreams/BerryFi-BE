package com.berryfi.portal.repository;

import com.berryfi.portal.entity.BillingInvoice;
import com.berryfi.portal.enums.InvoiceStatus;
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
 * Repository interface for BillingInvoice entity.
 */
@Repository
public interface BillingInvoiceRepository extends JpaRepository<BillingInvoice, String> {

    /**
     * Find invoices by organization ID
     */
    Page<BillingInvoice> findByOrganizationIdOrderByDateDesc(
            String organizationId, Pageable pageable);

    /**
     * Find invoices by organization ID and status
     */
    Page<BillingInvoice> findByOrganizationIdAndStatusOrderByDateDesc(
            String organizationId, InvoiceStatus status, Pageable pageable);

    /**
     * Find invoice by invoice number
     */
    Optional<BillingInvoice> findByInvoiceNumber(String invoiceNumber);

    /**
     * Find overdue invoices
     */
    @Query("SELECT bi FROM BillingInvoice bi WHERE bi.status = 'PENDING' " +
           "AND bi.dueDate < :currentDate")
    List<BillingInvoice> findOverdueInvoices(@Param("currentDate") LocalDate currentDate);

    /**
     * Find overdue invoices for organization
     */
    @Query("SELECT bi FROM BillingInvoice bi WHERE bi.organizationId = :organizationId " +
           "AND bi.status = 'PENDING' AND bi.dueDate < :currentDate")
    List<BillingInvoice> findOverdueInvoicesByOrganization(
            @Param("organizationId") String organizationId,
            @Param("currentDate") LocalDate currentDate);

    /**
     * Find invoices by date range
     */
    @Query("SELECT bi FROM BillingInvoice bi WHERE bi.organizationId = :organizationId " +
           "AND bi.date BETWEEN :startDate AND :endDate " +
           "ORDER BY bi.date DESC")
    Page<BillingInvoice> findByOrganizationIdAndDateRange(
            @Param("organizationId") String organizationId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    /**
     * Calculate total amount for organization within date range
     */
    @Query("SELECT COALESCE(SUM(bi.amount), 0.0) FROM BillingInvoice bi " +
           "WHERE bi.organizationId = :organizationId " +
           "AND bi.date BETWEEN :startDate AND :endDate " +
           "AND bi.status = :status")
    Double sumTotalAmountByOrganizationAndDateRangeAndStatus(
            @Param("organizationId") String organizationId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") InvoiceStatus status);

    /**
     * Get outstanding balance for organization
     */
    @Query("SELECT COALESCE(SUM(bi.amount), 0.0) FROM BillingInvoice bi " +
           "WHERE bi.organizationId = :organizationId " +
           "AND bi.status IN ('PENDING', 'OVERDUE')")
    Double getOutstandingBalanceByOrganization(@Param("organizationId") String organizationId);

    /**
     * Count invoices by status for organization
     */
    Long countByOrganizationIdAndStatus(String organizationId, InvoiceStatus status);

    /**
     * Find latest invoice for organization
     */
    Optional<BillingInvoice> findFirstByOrganizationIdOrderByDateDesc(String organizationId);

    /**
     * Find invoices due within days
     */
    @Query("SELECT bi FROM BillingInvoice bi WHERE bi.organizationId = :organizationId " +
           "AND bi.status = 'PENDING' " +
           "AND bi.dueDate BETWEEN :currentDate AND :futureDate")
    List<BillingInvoice> findInvoicesDueWithinDays(
            @Param("organizationId") String organizationId,
            @Param("currentDate") LocalDate currentDate,
            @Param("futureDate") LocalDate futureDate);

    /**
     * Find invoices by billing period
     */
    @Query("SELECT bi FROM BillingInvoice bi WHERE bi.organizationId = :organizationId " +
           "AND bi.date BETWEEN :periodStart AND :periodEnd")
    List<BillingInvoice> findByOrganizationIdAndBillingPeriod(
            @Param("organizationId") String organizationId,
            @Param("periodStart") LocalDate periodStart,
            @Param("periodEnd") LocalDate periodEnd);

    /**
     * Get monthly invoice count for organization
     */
    @Query("SELECT COUNT(bi) FROM BillingInvoice bi " +
           "WHERE bi.organizationId = :organizationId " +
           "AND EXTRACT(YEAR FROM bi.date) = :year " +
           "AND EXTRACT(MONTH FROM bi.date) = :month")
    Long getMonthlyInvoiceCount(
            @Param("organizationId") String organizationId,
            @Param("year") int year,
            @Param("month") int month);
}
