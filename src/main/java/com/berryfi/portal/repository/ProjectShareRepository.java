package com.berryfi.portal.repository;

import com.berryfi.portal.entity.ProjectShare;
import com.berryfi.portal.enums.ProjectShareStatus;
import com.berryfi.portal.enums.ShareType;
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
 * Repository interface for ProjectShare entity operations.
 */
@Repository
public interface ProjectShareRepository extends JpaRepository<ProjectShare, String> {

    /**
     * Find project shares by sharing organization.
     */
    Page<ProjectShare> findBySharedByOrganizationId(String sharedByOrganizationId, Pageable pageable);

    /**
     * Find project shares received by organization.
     */
    Page<ProjectShare> findBySharedWithOrganizationId(String sharedWithOrganizationId, Pageable pageable);

    /**
     * Find project shares by project ID.
     */
    Page<ProjectShare> findByProjectId(String projectId, Pageable pageable);

    /**
     * Find project shares by status.
     */
    Page<ProjectShare> findByStatus(ProjectShareStatus status, Pageable pageable);

    /**
     * Find project shares by owner organization and status.
     */
    Page<ProjectShare> findBySharedByOrganizationIdAndStatus(String sharedByOrganizationId, 
                                                        ProjectShareStatus status, 
                                                        Pageable pageable);    /**
     * Find project shares received by organization and status.
     */
    Page<ProjectShare> findBySharedWithOrganizationIdAndStatus(String sharedWithOrganizationId, 
                                                              ProjectShareStatus status, 
                                                              Pageable pageable);

    /**
     * Check if project is already shared with organization.
     */
    boolean existsByProjectIdAndSharedWithOrganizationIdAndStatusIn(
        String projectId, 
        String sharedWithOrganizationId, 
        List<ProjectShareStatus> statuses
    );

    /**
     * Find specific project share between organizations.
     */
    Optional<ProjectShare> findByProjectIdAndSharedByOrganizationIdAndSharedWithOrganizationId(
        String projectId,
        String sharedByOrganizationId,
        String sharedWithOrganizationId
    );

    /**
     * Find active project shares by project.
     */
    @Query("SELECT ps FROM ProjectShare ps WHERE ps.projectId = :projectId AND ps.status = 'ACCEPTED'")
    List<ProjectShare> findActiveSharesByProject(@Param("projectId") String projectId);

    /**
     * Find expired project shares.
     */
    @Query("SELECT ps FROM ProjectShare ps WHERE ps.expiresAt < :currentTime AND ps.status = 'ACCEPTED'")
    List<ProjectShare> findExpiredShares(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Find project shares that need recurring credit gifts.
     */
    @Query("SELECT ps FROM ProjectShare ps WHERE ps.status = 'ACCEPTED' AND " +
           "ps.recurringCredits > 0 AND ps.nextCreditGiftDate < :currentTime")
    List<ProjectShare> findSharesNeedingRecurringCredits(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Count shares by project.
     */
    @Query("SELECT COUNT(ps) FROM ProjectShare ps WHERE ps.projectId = :projectId AND ps.status = 'ACCEPTED'")
    long countActiveSharesByProject(@Param("projectId") String projectId);

    /**
     * Count shares by owner organization.
     */
    long countBySharedByOrganizationIdAndStatus(String sharedByOrganizationId, ProjectShareStatus status);

    /**
     * Find who directly shared a project with an organization (for sharedBy field)
     */
    Optional<ProjectShare> findByProjectIdAndSharedWithOrganizationIdAndStatus(String projectId, String sharedWithOrganizationId, ProjectShareStatus status);

    /**
     * Check if a project is already shared with a specific organization
     */
    boolean existsByProjectIdAndSharedWithOrganizationIdAndStatus(String projectId, String sharedWithOrganizationId, ProjectShareStatus status);

    /**
     * Check if an organization can reshare a project (has canShareFurther permission)
     */
    @Query("SELECT ps FROM ProjectShare ps WHERE ps.projectId = :projectId AND ps.sharedWithOrganizationId = :organizationId AND ps.status = :status AND ps.canShareFurther = true")
    Optional<ProjectShare> findReshareableAccess(@Param("projectId") String projectId, 
                                                @Param("organizationId") String organizationId,
                                                @Param("status") ProjectShareStatus status);

    /**
     * Count shares received by organization.
     */
    long countBySharedWithOrganizationIdAndStatus(String sharedWithOrganizationId, ProjectShareStatus status);

    /**
     * Use credits for a project share.
     */
    @Modifying
    @Query("UPDATE ProjectShare ps SET ps.usedCredits = ps.usedCredits + :creditsUsed, " +
           "ps.remainingCredits = ps.remainingCredits - :creditsUsed, " +
           "ps.updatedAt = :updatedAt " +
           "WHERE ps.id = :shareId")
    int useCredits(@Param("shareId") String shareId, 
                   @Param("creditsUsed") Double creditsUsed,
                   @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Add recurring credits to a project share.
     */
    @Modifying
    @Query("UPDATE ProjectShare ps SET ps.remainingCredits = ps.remainingCredits + :credits, " +
           "ps.lastCreditGiftDate = :giftDate, " +
           "ps.nextCreditGiftDate = :nextGiftDate, " +
           "ps.updatedAt = :updatedAt " +
           "WHERE ps.id = :shareId")
    int addRecurringCredits(@Param("shareId") String shareId, 
                           @Param("credits") Double credits,
                           @Param("giftDate") LocalDateTime giftDate,
                           @Param("nextGiftDate") LocalDateTime nextGiftDate,
                           @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Update share status.
     */
    @Modifying
    @Query("UPDATE ProjectShare ps SET ps.status = :status, ps.updatedAt = :updatedAt WHERE ps.id = :shareId")
    int updateStatus(@Param("shareId") String shareId, 
                     @Param("status") ProjectShareStatus status,
                     @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Accept project share.
     */
    @Modifying
    @Query("UPDATE ProjectShare ps SET ps.status = 'ACCEPTED', " +
           "ps.acceptedAt = :acceptedAt, " +
           "ps.acceptedBy = :acceptedBy, " +
           "ps.remainingCredits = ps.allocatedCredits, " +
           "ps.updatedAt = :updatedAt " +
           "WHERE ps.id = :shareId")
    int acceptShare(@Param("shareId") String shareId, 
                    @Param("acceptedAt") LocalDateTime acceptedAt,
                    @Param("acceptedBy") String acceptedBy,
                    @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Reject project share.
     */
    @Modifying
    @Query("UPDATE ProjectShare ps SET ps.status = 'REJECTED', " +
           "ps.rejectedAt = :rejectedAt, " +
           "ps.updatedAt = :updatedAt " +
           "WHERE ps.id = :shareId")
    int rejectShare(@Param("shareId") String shareId, 
                    @Param("rejectedAt") LocalDateTime rejectedAt,
                    @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Revoke project share.
     */
    @Modifying
    @Query("UPDATE ProjectShare ps SET ps.status = 'REVOKED', " +
           "ps.revokedAt = :revokedAt, " +
           "ps.updatedAt = :updatedAt " +
           "WHERE ps.id = :shareId")
    int revokeShare(@Param("shareId") String shareId, 
                    @Param("revokedAt") LocalDateTime revokedAt,
                    @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Find shares by share type.
     */
    Page<ProjectShare> findByShareType(ShareType shareType, Pageable pageable);

    /**
     * Get analytics for organization sharing.
     */
    @Query("SELECT " +
           "COUNT(CASE WHEN ps.status = 'ACCEPTED' THEN 1 END) as acceptedShares, " +
           "COUNT(CASE WHEN ps.status = 'PENDING' THEN 1 END) as pendingShares, " +
           "COUNT(CASE WHEN ps.status = 'REJECTED' THEN 1 END) as rejectedShares, " +
           "COALESCE(SUM(ps.allocatedCredits), 0) as totalAllocatedCredits, " +
           "COALESCE(SUM(ps.usedCredits), 0) as totalUsedCredits " +
           "FROM ProjectShare ps WHERE ps.sharedByOrganizationId = :organizationId")
    Object[] getOrganizationSharingAnalytics(@Param("organizationId") String organizationId);

    /**
     * Find all projects shared by an organization (both direct and indirect).
     * This includes projects originally owned by the organization and projects they reshared.
     */
    @Query("SELECT ps FROM ProjectShare ps WHERE ps.sharedByOrganizationId = :organizationId AND ps.status = 'ACCEPTED'")
    List<ProjectShare> findAllProjectsSharedByOrganization(@Param("organizationId") String organizationId);

    /**
     * Find all direct shares by organization (projects originally owned by the organization).
     */
    @Query("SELECT ps FROM ProjectShare ps " +
           "JOIN Project p ON ps.projectId = p.id " +
           "WHERE ps.sharedByOrganizationId = :organizationId " +
           "AND p.organizationId = :organizationId " +
           "AND ps.status = 'ACCEPTED'")
    List<ProjectShare> findDirectSharesByOrganization(@Param("organizationId") String organizationId);

    /**
     * Find all indirect shares by organization (projects the organization reshared).
     */
    @Query("SELECT ps FROM ProjectShare ps " +
           "JOIN Project p ON ps.projectId = p.id " +
           "WHERE ps.sharedByOrganizationId = :organizationId " +
           "AND p.organizationId != :organizationId " +
           "AND ps.status = 'ACCEPTED'")
    List<ProjectShare> findIndirectSharesByOrganization(@Param("organizationId") String organizationId);
}