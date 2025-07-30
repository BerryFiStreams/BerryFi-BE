package com.berryfi.portal.repository;

import com.berryfi.portal.entity.TeamMember;
import com.berryfi.portal.enums.Role;
import com.berryfi.portal.enums.UserStatus;
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
 * Repository interface for TeamMember entity.
 */
@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, String> {
    
    // Find team members by organization
    Page<TeamMember> findByOrganizationIdOrderByJoinedAtDesc(String organizationId, Pageable pageable);
    
    // Find team members by workspace
    Page<TeamMember> findByWorkspaceIdOrderByJoinedAtDesc(String workspaceId, Pageable pageable);
    
    // Find team member by user and organization
    Optional<TeamMember> findByUserIdAndOrganizationId(String userId, String organizationId);
    
    // Find team member by user and workspace
    Optional<TeamMember> findByUserIdAndWorkspaceId(String userId, String workspaceId);
    
    // Find team members by role
    List<TeamMember> findByOrganizationIdAndRole(String organizationId, Role role);
    
    // Find team members by status
    List<TeamMember> findByOrganizationIdAndStatus(String organizationId, UserStatus status);
    
    // Find active team members
    Page<TeamMember> findByOrganizationIdAndStatusOrderByJoinedAtDesc(String organizationId, UserStatus status, Pageable pageable);
    
    // Find team members invited by user
    List<TeamMember> findByInvitedByOrderByInvitedAtDesc(String invitedBy);
    
    // Search team members by name or email
    @Query("SELECT tm FROM TeamMember tm WHERE tm.organizationId = :organizationId AND " +
           "(LOWER(tm.userName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(tm.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<TeamMember> searchTeamMembers(@Param("organizationId") String organizationId, 
                                      @Param("searchTerm") String searchTerm, 
                                      Pageable pageable);
    
    // Count team members by organization
    Long countByOrganizationId(String organizationId);
    
    // Count active team members
    Long countByOrganizationIdAndStatus(String organizationId, UserStatus status);
    
    // Count team members by role
    @Query("SELECT COUNT(tm) FROM TeamMember tm WHERE tm.organizationId = :organizationId AND tm.role = :role")
    Long countByOrganizationIdAndRole(@Param("organizationId") String organizationId, 
                                     @Param("role") Role role);
    
    // Find team members who joined in date range
    @Query("SELECT tm FROM TeamMember tm WHERE tm.organizationId = :organizationId AND " +
           "tm.joinedAt BETWEEN :startDate AND :endDate ORDER BY tm.joinedAt DESC")
    List<TeamMember> findByJoinedDateRange(@Param("organizationId") String organizationId,
                                          @Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);
    
    // Find team members with pending invitations
    List<TeamMember> findByOrganizationIdAndStatusAndJoinedAtIsNull(String organizationId, UserStatus status);
    
    // Find team members with admin privileges
    @Query("SELECT tm FROM TeamMember tm WHERE tm.organizationId = :organizationId AND " +
           "tm.role IN ('ORG_ADMIN', 'ORG_OWNER')")
    List<TeamMember> findAdminMembers(@Param("organizationId") String organizationId);
    
    // Find recently active team members
    @Query("SELECT tm FROM TeamMember tm WHERE tm.organizationId = :organizationId AND " +
           "tm.lastActiveAt >= :sinceDate ORDER BY tm.lastActiveAt DESC")
    List<TeamMember> findRecentlyActive(@Param("organizationId") String organizationId, 
                                       @Param("sinceDate") LocalDateTime sinceDate);
    
    // Check if user is member of organization
    boolean existsByUserIdAndOrganizationId(String userId, String organizationId);
    
    // Check if user is member of workspace
    boolean existsByUserIdAndWorkspaceId(String userId, String workspaceId);
    
    // Find team members by multiple criteria
    @Query("SELECT tm FROM TeamMember tm WHERE tm.organizationId = :organizationId " +
           "AND (:role IS NULL OR tm.role = :role) " +
           "AND (:status IS NULL OR tm.status = :status) " +
           "ORDER BY tm.joinedAt DESC")
    Page<TeamMember> findByCriteria(@Param("organizationId") String organizationId,
                                   @Param("role") Role role,
                                   @Param("status") UserStatus status,
                                   Pageable pageable);
    
    // Team member activity analytics
    @Query("SELECT COUNT(tm) FROM TeamMember tm WHERE tm.organizationId = :organizationId AND tm.lastActiveAt >= :sinceDate")
    Long countActiveMembers(@Param("organizationId") String organizationId, 
                           @Param("sinceDate") LocalDateTime sinceDate);
    
    // Find team members by workspace and role
    List<TeamMember> findByWorkspaceIdAndRole(String workspaceId, Role role);
    
    // Find all workspaces for a user
    @Query("SELECT tm FROM TeamMember tm WHERE tm.userId = :userId AND tm.status = 'ACTIVE'")
    List<TeamMember> findActiveWorkspacesForUser(@Param("userId") String userId);
}
