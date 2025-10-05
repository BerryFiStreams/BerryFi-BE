package com.berryfi.portal.repository;

import com.berryfi.portal.entity.TeamMemberInvitation;
import com.berryfi.portal.enums.InvitationStatus;
import com.berryfi.portal.enums.Role;
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
 * Repository interface for TeamMemberInvitation entity operations.
 */
@Repository
public interface TeamMemberInvitationRepository extends JpaRepository<TeamMemberInvitation, String> {

    /**
     * Find invitation by invite token.
     */
    Optional<TeamMemberInvitation> findByInviteToken(String inviteToken);

    /**
     * Find invitations by email.
     */
    List<TeamMemberInvitation> findByInviteEmail(String inviteEmail);

    /**
     * Find invitations by organization and status.
     */
    Page<TeamMemberInvitation> findByOrganizationIdAndStatusOrderByCreatedAtDesc(String organizationId, InvitationStatus status, Pageable pageable);

    /**
     * Find invitations by organization.
     */
    Page<TeamMemberInvitation> findByOrganizationIdOrderByCreatedAtDesc(String organizationId, Pageable pageable);

    /**
     * Find invitations by invited by user.
     */
    Page<TeamMemberInvitation> findByInvitedByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    /**
     * Find invitations by invited by user and status.
     */
    Page<TeamMemberInvitation> findByInvitedByUserIdAndStatusOrderByCreatedAtDesc(String userId, InvitationStatus status, Pageable pageable);

    /**
     * Find invitations by status.
     */
    Page<TeamMemberInvitation> findByStatusOrderByCreatedAtDesc(InvitationStatus status, Pageable pageable);

    /**
     * Find pending invitation for email and organization.
     */
    Optional<TeamMemberInvitation> findByInviteEmailAndOrganizationIdAndStatus(String inviteEmail, String organizationId, InvitationStatus status);

    /**
     * Check if there's already a pending invitation for this email and organization.
     */
    boolean existsByInviteEmailAndOrganizationIdAndStatus(String inviteEmail, String organizationId, InvitationStatus status);

    /**
     * Find expired invitations that need to be marked as expired.
     */
    @Query("SELECT tmi FROM TeamMemberInvitation tmi WHERE tmi.status = 'PENDING' AND tmi.expiresAt < :currentTime")
    List<TeamMemberInvitation> findExpiredInvitations(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Count invitations by organization and status.
     */
    Long countByOrganizationIdAndStatus(String organizationId, InvitationStatus status);

    /**
     * Count invitations by organization.
     */
    Long countByOrganizationId(String organizationId);

    /**
     * Find invitations by organization and role.
     */
    List<TeamMemberInvitation> findByOrganizationIdAndRole(String organizationId, Role role);

    /**
     * Find invitations by organization, role and status.
     */
    List<TeamMemberInvitation> findByOrganizationIdAndRoleAndStatus(String organizationId, Role role, InvitationStatus status);

    /**
     * Delete invitations older than specified date.
     */
    @Query("DELETE FROM TeamMemberInvitation tmi WHERE tmi.createdAt < :cutoffDate AND tmi.status IN ('EXPIRED', 'DECLINED')")
    void deleteOldInvitations(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find recent invitations for analytics.
     */
    @Query("SELECT tmi FROM TeamMemberInvitation tmi WHERE tmi.organizationId = :organizationId AND tmi.createdAt >= :since ORDER BY tmi.createdAt DESC")
    List<TeamMemberInvitation> findRecentInvitations(@Param("organizationId") String organizationId, @Param("since") LocalDateTime since);
}