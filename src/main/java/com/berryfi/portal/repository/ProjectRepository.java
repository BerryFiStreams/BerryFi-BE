package com.berryfi.portal.repository;

import com.berryfi.portal.entity.Project;
import com.berryfi.portal.enums.AccountType;
import com.berryfi.portal.enums.ProjectStatus;
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
 * Repository interface for Project entity operations.
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, String> {

    /**
     * Find all projects belonging to a specific organization.
     */
    Page<Project> findByOrganizationId(String organizationId, Pageable pageable);

    /**
     * Find projects by organization and status.
     */
    Page<Project> findByOrganizationIdAndStatus(String organizationId, ProjectStatus status, Pageable pageable);

    /**
     * Find projects by organization and account type.
     */
    Page<Project> findByOrganizationIdAndAccountType(String organizationId, AccountType accountType, Pageable pageable);

    /**
     * Find a project by ID and organization (for security).
     */
    Optional<Project> findByIdAndOrganizationId(String id, String organizationId);

    /**
     * Check if project exists in organization.
     */
    boolean existsByIdAndOrganizationId(String id, String organizationId);

    /**
     * Check if project name exists in organization.
     */
    boolean existsByNameAndOrganizationId(String name, String organizationId);

    /**
     * Count projects by organization.
     */
    long countByOrganizationId(String organizationId);

    /**
     * Count projects by organization and status.
     */
    long countByOrganizationIdAndStatus(String organizationId, ProjectStatus status);

    /**
     * Find all running projects.
     */
    List<Project> findByStatus(ProjectStatus status);

    /**
     * Find projects created by a specific user.
     */
    Page<Project> findByCreatedBy(String createdBy, Pageable pageable);

    /**
     * Find projects created after a specific date.
     */
    Page<Project> findByCreatedAtAfter(LocalDateTime date, Pageable pageable);

    /**
     * Find projects with total credits used greater than specified amount.
     */
    @Query("SELECT p FROM Project p WHERE p.organizationId = :organizationId AND p.totalCreditsUsed > :creditsThreshold")
    List<Project> findHighCreditUsageProjects(@Param("organizationId") String organizationId, 
                                             @Param("creditsThreshold") Double creditsThreshold);

    /**
     * Get total credits used by organization.
     */
    @Query("SELECT COALESCE(SUM(p.totalCreditsUsed), 0) FROM Project p WHERE p.organizationId = :organizationId")
    Double getTotalCreditsUsedByOrganization(@Param("organizationId") String organizationId);

    /**
     * Get total sessions count by organization.
     */
    @Query("SELECT COALESCE(SUM(p.sessionsCount), 0) FROM Project p WHERE p.organizationId = :organizationId")
    Long getTotalSessionsCountByOrganization(@Param("organizationId") String organizationId);

    /**
     * Find projects with sessions count greater than specified amount.
     */
    @Query("SELECT p FROM Project p WHERE p.organizationId = :organizationId AND p.sessionsCount > :sessionsThreshold")
    List<Project> findHighSessionProjects(@Param("organizationId") String organizationId, 
                                         @Param("sessionsThreshold") Integer sessionsThreshold);

    /**
     * Update project status.
     */
    @Modifying
    @Query("UPDATE Project p SET p.status = :status, p.updatedAt = :updatedAt WHERE p.id = :projectId")
    int updateProjectStatus(@Param("projectId") String projectId, 
                           @Param("status") ProjectStatus status, 
                           @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Update project CCU (Concurrent Connected Users).
     */
    @Modifying
    @Query("UPDATE Project p SET p.currentCCU = :ccu, p.updatedAt = :updatedAt WHERE p.id = :projectId")
    int updateProjectCCU(@Param("projectId") String projectId, 
                        @Param("ccu") Integer ccu, 
                        @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Update project uptime.
     */
    @Modifying
    @Query("UPDATE Project p SET p.uptime = :uptime, p.updatedAt = :updatedAt WHERE p.id = :projectId")
    int updateProjectUptime(@Param("projectId") String projectId, 
                           @Param("uptime") Double uptime, 
                           @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Increment sessions count.
     */
    @Modifying
    @Query("UPDATE Project p SET p.sessionsCount = p.sessionsCount + 1, p.updatedAt = :updatedAt WHERE p.id = :projectId")
    int incrementSessionsCount(@Param("projectId") String projectId, @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Add credits used.
     */
    @Modifying
    @Query("UPDATE Project p SET p.totalCreditsUsed = p.totalCreditsUsed + :credits, p.updatedAt = :updatedAt WHERE p.id = :projectId")
    int addCreditsUsed(@Param("projectId") String projectId, 
                      @Param("credits") Double credits, 
                      @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Find projects that haven't been updated for a specific duration (for cleanup/monitoring).
     */
    @Query("SELECT p FROM Project p WHERE p.updatedAt < :cutoffDate")
    List<Project> findStaleProjects(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Search projects by name containing keyword (case-insensitive).
     */
    @Query("SELECT p FROM Project p WHERE p.organizationId = :organizationId AND LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Project> searchProjectsByName(@Param("organizationId") String organizationId, 
                                      @Param("keyword") String keyword, 
                                      Pageable pageable);

    /**
     * Search projects by name or description containing keyword.
     */
    @Query("SELECT p FROM Project p WHERE p.organizationId = :organizationId AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Project> searchProjects(@Param("organizationId") String organizationId, 
                                 @Param("keyword") String keyword, 
                                 Pageable pageable);

    /**
     * Find projects by organization ordered by updated date (recent first)
     */
    @Query("SELECT p FROM Project p WHERE p.organizationId = :organizationId ORDER BY p.updatedAt DESC")
    List<Project> findByOrganizationIdOrderByUpdatedAtDesc(@Param("organizationId") String organizationId, Pageable pageable);

    /**
     * Find projects owned by or shared with an organization
     */
    @Query("SELECT p FROM Project p WHERE p.organizationId = :organizationId OR " +
           "(p.sharedWithOrganizations IS NOT NULL AND p.sharedWithOrganizations LIKE CONCAT('%\"', :organizationId, '\"%'))")
    Page<Project> findOwnedAndSharedProjects(@Param("organizationId") String organizationId, Pageable pageable);

    /**
     * Find projects shared with a specific organization
     */
    @Query("SELECT p FROM Project p WHERE p.organizationId != :organizationId AND " +
           "p.sharedWithOrganizations IS NOT NULL AND p.sharedWithOrganizations LIKE CONCAT('%\"', :organizationId, '\"%')")
    Page<Project> findSharedWithOrganization(@Param("organizationId") String organizationId, Pageable pageable);

    /**
     * Find projects owned by an organization
     */
    Page<Project> findByOrganizationIdAndSharedWithOrganizationsIsNull(String organizationId, Pageable pageable);
}
