package com.berryfi.portal.repository;

import com.berryfi.portal.entity.Lead;
import com.berryfi.portal.enums.LeadStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for Lead entity.
 */
@Repository
public interface LeadRepository extends JpaRepository<Lead, String> {
    
    // Find leads by campaign
    Page<Lead> findByCampaignIdOrderByCreatedAtDesc(String campaignId, Pageable pageable);
    
    // Find leads by organization
    Page<Lead> findByOrganizationIdOrderByCreatedAtDesc(String organizationId, Pageable pageable);
    
    // Find leads by status
    Page<Lead> findByOrganizationIdAndStatusOrderByCreatedAtDesc(String organizationId, LeadStatus status, Pageable pageable);
    
    // Find leads assigned to user
    Page<Lead> findByAssignedToOrderByCreatedAtDesc(String assignedTo, Pageable pageable);
    
    // Find unassigned leads
    Page<Lead> findByOrganizationIdAndAssignedToIsNullOrderByCreatedAtDesc(String organizationId, Pageable pageable);
    
    // Search leads by name or email
    @Query("SELECT l FROM Lead l WHERE l.organizationId = :organizationId AND " +
           "(LOWER(l.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(l.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(l.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(l.company) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Lead> searchLeads(@Param("organizationId") String organizationId, 
                          @Param("searchTerm") String searchTerm, 
                          Pageable pageable);
    
    // Find leads by company
    List<Lead> findByOrganizationIdAndCompanyIgnoreCase(String organizationId, String company);
    
    // Find high-scoring leads
    @Query("SELECT l FROM Lead l WHERE l.organizationId = :organizationId AND l.leadScore >= :minScore ORDER BY l.leadScore DESC")
    List<Lead> findHighScoringLeads(@Param("organizationId") String organizationId, 
                                   @Param("minScore") Integer minScore);
    
    // Find converted leads
    Page<Lead> findByOrganizationIdAndIsConvertedTrueOrderByConvertedAtDesc(String organizationId, Pageable pageable);
    
    // Find leads by source
    List<Lead> findByOrganizationIdAndSource(String organizationId, String source);
    
    // Count leads by status
    @Query("SELECT COUNT(l) FROM Lead l WHERE l.organizationId = :organizationId AND l.status = :status")
    Long countByOrganizationIdAndStatus(@Param("organizationId") String organizationId, 
                                       @Param("status") LeadStatus status);
    
    // Count leads by campaign
    Long countByCampaignId(String campaignId);
    
    // Find leads created in date range
    @Query("SELECT l FROM Lead l WHERE l.organizationId = :organizationId AND " +
           "l.createdAt BETWEEN :startDate AND :endDate ORDER BY l.createdAt DESC")
    List<Lead> findByDateRange(@Param("organizationId") String organizationId,
                              @Param("startDate") LocalDateTime startDate,
                              @Param("endDate") LocalDateTime endDate);
    
    // Lead analytics aggregations
    @Query("SELECT COUNT(l) FROM Lead l WHERE l.organizationId = :organizationId")
    Long getTotalLeadsCount(@Param("organizationId") String organizationId);
    
    @Query("SELECT COUNT(l) FROM Lead l WHERE l.organizationId = :organizationId AND l.isConverted = true")
    Long getConvertedLeadsCount(@Param("organizationId") String organizationId);
    
    @Query("SELECT AVG(l.leadScore) FROM Lead l WHERE l.organizationId = :organizationId AND l.leadScore > 0")
    Double getAverageLeadScore(@Param("organizationId") String organizationId);
    
    // Find leads needing follow-up (no recent contact)
    @Query("SELECT l FROM Lead l WHERE l.organizationId = :organizationId AND l.status IN :statuses AND " +
           "(l.lastContactedAt IS NULL OR l.lastContactedAt < :cutoffDate)")
    List<Lead> findLeadsNeedingFollowUp(@Param("organizationId") String organizationId,
                                       @Param("statuses") List<LeadStatus> statuses,
                                       @Param("cutoffDate") LocalDateTime cutoffDate);
    
    // Find leads by email (for duplicate checking)
    List<Lead> findByOrganizationIdAndEmail(String organizationId, String email);
    
    // Find recent leads
    @Query("SELECT l FROM Lead l WHERE l.organizationId = :organizationId AND l.createdAt >= :sinceDate ORDER BY l.createdAt DESC")
    List<Lead> findRecentLeads(@Param("organizationId") String organizationId, 
                              @Param("sinceDate") LocalDateTime sinceDate);
    
    // Lead conversion analytics by campaign
    @Query("SELECT l.campaignId, COUNT(l), COUNT(CASE WHEN l.isConverted = true THEN 1 END) " +
           "FROM Lead l WHERE l.organizationId = :organizationId GROUP BY l.campaignId")
    List<Object[]> getLeadConversionByCampaign(@Param("organizationId") String organizationId);
    
    // Find leads by multiple criteria
    @Query("SELECT l FROM Lead l WHERE l.organizationId = :organizationId " +
           "AND (:status IS NULL OR l.status = :status) " +
           "AND (:assignedTo IS NULL OR l.assignedTo = :assignedTo) " +
           "AND (:campaignId IS NULL OR l.campaignId = :campaignId) " +
           "ORDER BY l.createdAt DESC")
    Page<Lead> findByCriteria(@Param("organizationId") String organizationId,
                             @Param("status") LeadStatus status,
                             @Param("assignedTo") String assignedTo,
                             @Param("campaignId") String campaignId,
                             Pageable pageable);
}
