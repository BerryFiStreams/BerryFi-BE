package com.berryfi.portal.repository;

import com.berryfi.portal.entity.Workspace;
import com.berryfi.portal.enums.WorkspaceStatus;
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
 * Repository interface for Workspace entity operations.
 */
@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, String> {

    /**
     * Find all workspaces belonging to a specific organization.
     */
    Page<Workspace> findByOrganizationId(String organizationId, Pageable pageable);

    /**
     * Find workspaces by organization and status.
     */
    Page<Workspace> findByOrganizationIdAndStatus(String organizationId, WorkspaceStatus status, Pageable pageable);

    /**
     * Find a workspace by ID and organization (for security).
     */
    Optional<Workspace> findByIdAndOrganizationId(String id, String organizationId);

    /**
     * Check if workspace exists in organization.
     */
    boolean existsByIdAndOrganizationId(String id, String organizationId);

    /**
     * Find workspace by project ID.
     */
    Optional<Workspace> findByProjectId(String projectId);

    /**
     * Check if workspace name exists in organization.
     */
    boolean existsByNameAndOrganizationId(String name, String organizationId);

    /**
     * Find workspace by admin email.
     */
    Optional<Workspace> findByAdminEmail(String adminEmail);

    /**
     * Find workspaces by admin email.
     */
    List<Workspace> findByAdminEmailAndOrganizationId(String adminEmail, String organizationId);

    /**
     * Count workspaces by organization.
     */
    long countByOrganizationId(String organizationId);

    /**
     * Count workspaces by organization and status.
     */
    long countByOrganizationIdAndStatus(String organizationId, WorkspaceStatus status);

    /**
     * Find workspaces by status.
     */
    List<Workspace> findByStatus(WorkspaceStatus status);

    /**
     * Find workspaces created by a specific user.
     */
    Page<Workspace> findByCreatedBy(String createdBy, Pageable pageable);
    
    /**
     * Find workspaces by created by user ID and organization ID.
     */
    List<Workspace> findByCreatedByAndOrganizationId(String createdByUserId, String organizationId);

    /**
     * Find workspaces created after a specific date.
     */
    Page<Workspace> findByCreatedAtAfter(LocalDateTime date, Pageable pageable);

    /**
     * Find workspaces with budget usage over a threshold.
     */
    @Query("SELECT w FROM Workspace w WHERE w.organizationId = :organizationId AND w.budgetUsagePercentage > :threshold")
    List<Workspace> findHighBudgetUsageWorkspaces(@Param("organizationId") String organizationId, 
                                                  @Param("threshold") Double threshold);

    /**
     * Find workspaces that are over budget.
     */
    @Query("SELECT w FROM Workspace w WHERE w.organizationId = :organizationId AND w.isOverBudget = true")
    List<Workspace> findOverBudgetWorkspaces(@Param("organizationId") String organizationId);

    /**
     * Get total credits allocated by organization.
     */
    @Query("SELECT COALESCE(SUM(w.giftedCredits + w.purchasedCredits), 0) FROM Workspace w WHERE w.organizationId = :organizationId")
    Double getTotalCreditsAllocatedByOrganization(@Param("organizationId") String organizationId);

    /**
     * Get total credits used by organization.
     */
    @Query("SELECT COALESCE(SUM(w.totalCreditsUsed), 0) FROM Workspace w WHERE w.organizationId = :organizationId")
    Double getTotalCreditsUsedByOrganization(@Param("organizationId") String organizationId);

    /**
     * Get total sessions this month by organization.
     */
    @Query("SELECT COALESCE(SUM(w.sessionsThisMonth), 0) FROM Workspace w WHERE w.organizationId = :organizationId")
    Long getTotalSessionsThisMonthByOrganization(@Param("organizationId") String organizationId);

    /**
     * Get average usage per workspace.
     */
    @Query("SELECT COALESCE(AVG(w.totalCreditsUsed), 0) FROM Workspace w WHERE w.organizationId = :organizationId")
    Double getAverageUsagePerWorkspace(@Param("organizationId") String organizationId);

    /**
     * Get top workspaces by usage.
     */
    @Query("SELECT w FROM Workspace w WHERE w.organizationId = :organizationId ORDER BY w.totalCreditsUsed DESC")
    List<Workspace> findTopWorkspacesByUsage(@Param("organizationId") String organizationId, Pageable pageable);

    /**
     * Update workspace budget usage.
     */
    @Modifying
    @Query("UPDATE Workspace w SET w.budgetUsagePercentage = :percentage, w.isOverBudget = :isOverBudget, w.updatedAt = :updatedAt WHERE w.id = :workspaceId")
    int updateBudgetUsage(@Param("workspaceId") String workspaceId, 
                         @Param("percentage") Double percentage, 
                         @Param("isOverBudget") Boolean isOverBudget,
                         @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Update workspace credits.
     */
    @Modifying
    @Query("UPDATE Workspace w SET w.currentBalance = :balance, w.remainingGiftedCredits = :giftedCredits, w.remainingPurchasedCredits = :purchasedCredits, w.updatedAt = :updatedAt WHERE w.id = :workspaceId")
    int updateWorkspaceCredits(@Param("workspaceId") String workspaceId, 
                              @Param("balance") Double balance,
                              @Param("giftedCredits") Double giftedCredits,
                              @Param("purchasedCredits") Double purchasedCredits,
                              @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Increment sessions count.
     */
    @Modifying
    @Query("UPDATE Workspace w SET w.sessionsThisMonth = w.sessionsThisMonth + 1, w.updatedAt = :updatedAt WHERE w.id = :workspaceId")
    int incrementSessionsCount(@Param("workspaceId") String workspaceId, @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Add credits used.
     */
    @Modifying
    @Query("UPDATE Workspace w SET w.totalCreditsUsed = w.totalCreditsUsed + :credits, w.creditsUsedThisMonth = w.creditsUsedThisMonth + :credits, w.updatedAt = :updatedAt WHERE w.id = :workspaceId")
    int addCreditsUsed(@Param("workspaceId") String workspaceId, 
                      @Param("credits") Double credits, 
                      @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Update team member count.
     */
    @Modifying
    @Query("UPDATE Workspace w SET w.teamMemberCount = :count, w.updatedAt = :updatedAt WHERE w.id = :workspaceId")
    int updateTeamMemberCount(@Param("workspaceId") String workspaceId, 
                             @Param("count") Integer count,
                             @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Update workspace project association.
     */
    @Modifying
    @Query("UPDATE Workspace w SET w.projectId = :projectId, w.updatedAt = :updatedAt WHERE w.id = :workspaceId")
    int updateWorkspaceProject(@Param("workspaceId") String workspaceId, 
                              @Param("projectId") String projectId,
                              @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Search workspaces by name containing keyword (case-insensitive).
     */
    @Query("SELECT w FROM Workspace w WHERE w.organizationId = :organizationId AND LOWER(w.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Workspace> searchWorkspacesByName(@Param("organizationId") String organizationId, 
                                          @Param("keyword") String keyword, 
                                          Pageable pageable);

    /**
     * Search workspaces by name or description containing keyword.
     */
    @Query("SELECT w FROM Workspace w WHERE w.organizationId = :organizationId AND " +
           "(LOWER(w.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(w.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Workspace> searchWorkspacesByNameOrDescription(@Param("organizationId") String organizationId, 
                                                       @Param("keyword") String keyword, 
                                                       Pageable pageable);

    /**
     * Find workspaces by organization ordered by updated date (recent first)
     */
    @Query("SELECT w FROM Workspace w WHERE w.organizationId = :organizationId ORDER BY w.updatedAt DESC")
    List<Workspace> findByOrganizationIdOrderByUpdatedAtDesc(@Param("organizationId") String organizationId, Pageable pageable);
}
