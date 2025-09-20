package com.berryfi.portal.repository;

import com.berryfi.portal.entity.Organization;
import com.berryfi.portal.enums.OrganizationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Organization entity operations.
 */
@Repository
public interface OrganizationRepository extends JpaRepository<Organization, String> {

    /**
     * Find organization by status.
     */
    Page<Organization> findByStatus(OrganizationStatus status, Pageable pageable);

    /**
     * Find organizations by owner ID.
     */
    List<Organization> findByOwnerId(String ownerId);

    /**
     * Find organization by owner email.
     */
    Optional<Organization> findByOwnerEmail(String ownerEmail);

    /**
     * Check if organization name exists.
     */
    boolean existsByName(String name);

    /**
     * Check if owner already has an organization.
     */
    boolean existsByOwnerEmail(String ownerEmail);

    /**
     * Find organizations that can share projects.
     */
    @Query("SELECT o FROM Organization o WHERE o.canShareProjects = true AND o.status = 'ACTIVE'")
    Page<Organization> findOrganizationsThatCanShare(Pageable pageable);

    /**
     * Find organizations that can receive shared projects.
     */
    @Query("SELECT o FROM Organization o WHERE o.canReceiveSharedProjects = true AND o.status = 'ACTIVE'")
    Page<Organization> findOrganizationsThatCanReceiveShares(Pageable pageable);

    /**
     * Search organizations by name containing keyword (case-insensitive).
     */
    @Query("SELECT o FROM Organization o WHERE LOWER(o.name) LIKE LOWER(CONCAT('%', :keyword, '%')) AND o.status = 'ACTIVE'")
    Page<Organization> searchByName(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Find organizations with low credits.
     */
    @Query("SELECT o FROM Organization o WHERE o.remainingCredits < :threshold AND o.status = 'ACTIVE'")
    List<Organization> findOrganizationsWithLowCredits(@Param("threshold") Double threshold);

    /**
     * Find organizations over monthly budget.
     */
    @Query("SELECT o FROM Organization o WHERE o.monthlyBudget > 0 AND o.monthlyCreditsUsed > o.monthlyBudget AND o.status = 'ACTIVE'")
    List<Organization> findOrganizationsOverBudget();

    /**
     * Update organization credits.
     */
    @Modifying
    @Query("UPDATE Organization o SET o.usedCredits = o.usedCredits + :creditsUsed, " +
           "o.remainingCredits = o.remainingCredits - :creditsUsed, " +
           "o.monthlyCreditsUsed = o.monthlyCreditsUsed + :creditsUsed, " +
           "o.updatedAt = :updatedAt " +
           "WHERE o.id = :organizationId")
    int updateCreditsUsed(@Param("organizationId") String organizationId, 
                         @Param("creditsUsed") Double creditsUsed,
                         @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Add credits to organization.
     */
    @Modifying
    @Query("UPDATE Organization o SET o.totalCredits = o.totalCredits + :credits, " +
           "o.remainingCredits = o.remainingCredits + :credits, " +
           "o.purchasedCredits = o.purchasedCredits + :purchasedCredits, " +
           "o.giftedCredits = o.giftedCredits + :giftedCredits, " +
           "o.updatedAt = :updatedAt " +
           "WHERE o.id = :organizationId")
    int addCredits(@Param("organizationId") String organizationId, 
                   @Param("credits") Double credits,
                   @Param("purchasedCredits") Double purchasedCredits,
                   @Param("giftedCredits") Double giftedCredits,
                   @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Update organization project count.
     */
    @Modifying
    @Query("UPDATE Organization o SET o.activeProjects = :count, o.updatedAt = :updatedAt WHERE o.id = :organizationId")
    int updateProjectCount(@Param("organizationId") String organizationId, 
                          @Param("count") Integer count,
                          @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Update organization member count.
     */
    @Modifying
    @Query("UPDATE Organization o SET o.totalMembers = :count, o.updatedAt = :updatedAt WHERE o.id = :organizationId")
    int updateMemberCount(@Param("organizationId") String organizationId, 
                         @Param("count") Integer count,
                         @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Increment session count.
     */
    @Modifying
    @Query("UPDATE Organization o SET o.totalSessions = o.totalSessions + 1, o.updatedAt = :updatedAt WHERE o.id = :organizationId")
    int incrementSessionCount(@Param("organizationId") String organizationId, 
                             @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Reset monthly usage for all organizations.
     */
    @Modifying
    @Query("UPDATE Organization o SET o.monthlyCreditsUsed = 0.0, o.updatedAt = :updatedAt")
    int resetMonthlyUsage(@Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Count organizations by status.
     */
    long countByStatus(OrganizationStatus status);

    /**
     * Get total organizations count.
     */
    @Query("SELECT COUNT(o) FROM Organization o WHERE o.status != 'DELETED'")
    long countActiveOrganizations();

    /**
     * Get total credits across all organizations.
     */
    @Query("SELECT COALESCE(SUM(o.totalCredits), 0) FROM Organization o WHERE o.status = 'ACTIVE'")
    Double getTotalCreditsAcrossOrganizations();

    /**
     * Get total used credits across all organizations.
     */
    @Query("SELECT COALESCE(SUM(o.usedCredits), 0) FROM Organization o WHERE o.status = 'ACTIVE'")
    Double getTotalUsedCreditsAcrossOrganizations();
}