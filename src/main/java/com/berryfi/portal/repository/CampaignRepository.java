package com.berryfi.portal.repository;

import com.berryfi.portal.entity.Campaign;
import com.berryfi.portal.enums.AccessType;
import com.berryfi.portal.enums.CampaignStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for Campaign entity.
 */
@Repository
public interface CampaignRepository extends JpaRepository<Campaign, String> {
    
    // Find campaigns by organization
    Page<Campaign> findByOrganizationIdOrderByCreatedAtDesc(String organizationId, Pageable pageable);
    
    // Find campaigns by project
    Page<Campaign> findByProjectIdOrderByCreatedAtDesc(String projectId, Pageable pageable);
    
    // Find campaigns by status
    List<Campaign> findByOrganizationIdAndStatus(String organizationId, CampaignStatus status);
    
    // Find campaigns by access type
    List<Campaign> findByOrganizationIdAndAccessType(String organizationId, AccessType accessType);
    
    // Find campaigns by created user
    Page<Campaign> findByCreatedByOrderByCreatedAtDesc(String createdBy, Pageable pageable);
    
    // Search campaigns by name
    @Query("SELECT c FROM Campaign c WHERE c.organizationId = :organizationId AND " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.customName) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Campaign> searchByName(@Param("organizationId") String organizationId, 
                               @Param("searchTerm") String searchTerm, 
                               Pageable pageable);
    
    // Find active campaigns
    @Query("SELECT c FROM Campaign c WHERE c.organizationId = :organizationId AND c.status = 'ACTIVE'")
    List<Campaign> findActiveCampaigns(@Param("organizationId") String organizationId);
    
    // Find campaigns with high conversion rates
    @Query("SELECT c FROM Campaign c WHERE c.organizationId = :organizationId AND c.conversionRate >= :minRate")
    List<Campaign> findHighPerformingCampaigns(@Param("organizationId") String organizationId, 
                                              @Param("minRate") Double minRate);
    
    // Count campaigns by status
    @Query("SELECT COUNT(c) FROM Campaign c WHERE c.organizationId = :organizationId AND c.status = :status")
    Long countByOrganizationIdAndStatus(@Param("organizationId") String organizationId, 
                                       @Param("status") CampaignStatus status);
    
    // Find campaigns created in date range
    @Query("SELECT c FROM Campaign c WHERE c.organizationId = :organizationId AND " +
           "c.createdAt BETWEEN :startDate AND :endDate ORDER BY c.createdAt DESC")
    List<Campaign> findByDateRange(@Param("organizationId") String organizationId,
                                  @Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate);
    
    // Campaign analytics aggregation
    @Query("SELECT SUM(c.visits) FROM Campaign c WHERE c.organizationId = :organizationId")
    Long getTotalVisits(@Param("organizationId") String organizationId);
    
    @Query("SELECT SUM(c.leads) FROM Campaign c WHERE c.organizationId = :organizationId")
    Long getTotalLeads(@Param("organizationId") String organizationId);
    
    @Query("SELECT SUM(c.conversions) FROM Campaign c WHERE c.organizationId = :organizationId")
    Long getTotalConversions(@Param("organizationId") String organizationId);
    
    @Query("SELECT AVG(c.conversionRate) FROM Campaign c WHERE c.organizationId = :organizationId AND c.conversionRate > 0")
    Double getAverageConversionRate(@Param("organizationId") String organizationId);
    
    // Check if campaign name exists
    boolean existsByOrganizationIdAndName(String organizationId, String name);
    
    // Count campaigns by organization
    Long countByOrganizationId(String organizationId);
    
    // Find by project and status
    List<Campaign> findByProjectIdAndStatus(String projectId, CampaignStatus status);
    
    // Find top performing campaigns
    @Query("SELECT c FROM Campaign c WHERE c.organizationId = :organizationId " +
           "ORDER BY c.conversionRate DESC, c.leads DESC")
    Page<Campaign> findTopPerforming(@Param("organizationId") String organizationId, Pageable pageable);
}
