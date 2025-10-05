package com.berryfi.portal.repository;

import com.berryfi.portal.entity.ProjectInvitation;
import com.berryfi.portal.enums.InvitationStatus;
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
 * Repository interface for ProjectInvitation entity operations.
 */
@Repository
public interface ProjectInvitationRepository extends JpaRepository<ProjectInvitation, String> {

    /**
     * Find invitation by token.
     */
    Optional<ProjectInvitation> findByInviteToken(String inviteToken);

    /**
     * Find invitations by email.
     */
    Page<ProjectInvitation> findByInviteEmailOrderByCreatedAtDesc(String inviteEmail, Pageable pageable);

    /**
     * Find invitations by project.
     */
    Page<ProjectInvitation> findByProjectIdOrderByCreatedAtDesc(String projectId, Pageable pageable);

    /**
     * Find invitations by inviting organization.
     */
    Page<ProjectInvitation> findByInvitedByOrganizationIdOrderByCreatedAtDesc(String organizationId, Pageable pageable);

    /**
     * Find invitations by sender user.
     */
    Page<ProjectInvitation> findByInvitedByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    /**
     * Find invitations by sender user and status.
     */
    Page<ProjectInvitation> findByInvitedByUserIdAndStatusOrderByCreatedAtDesc(String userId, InvitationStatus status, Pageable pageable);

    /**
     * Find invitations by status.
     */
    Page<ProjectInvitation> findByStatusOrderByCreatedAtDesc(InvitationStatus status, Pageable pageable);

    /**
     * Find pending invitation for email and project.
     */
    Optional<ProjectInvitation> findByInviteEmailAndProjectIdAndStatus(String inviteEmail, String projectId, InvitationStatus status);

    /**
     * Check if there's already a pending invitation for this email and project.
     */
    boolean existsByInviteEmailAndProjectIdAndStatus(String inviteEmail, String projectId, InvitationStatus status);

    /**
     * Find expired invitations that need to be marked as expired.
     */
    @Query("SELECT pi FROM ProjectInvitation pi WHERE pi.status = 'PENDING' AND pi.expiresAt < :currentTime")
    List<ProjectInvitation> findExpiredInvitations(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Count invitations by organization and status.
     */
    long countByInvitedByOrganizationIdAndStatus(String organizationId, InvitationStatus status);

    /**
     * Count invitations by email and status.
     */
    long countByInviteEmailAndStatus(String inviteEmail, InvitationStatus status);

    /**
     * Find recent invitations for email (within last 24 hours).
     */
    @Query("SELECT pi FROM ProjectInvitation pi WHERE pi.inviteEmail = :email AND pi.createdAt > :since ORDER BY pi.createdAt DESC")
    List<ProjectInvitation> findRecentInvitationsByEmail(@Param("email") String email, @Param("since") LocalDateTime since);

    /**
     * Mark invitation as expired.
     */
    @Modifying
    @Query("UPDATE ProjectInvitation pi SET pi.status = 'EXPIRED', pi.updatedAt = :updatedAt WHERE pi.id = :invitationId")
    int markAsExpired(@Param("invitationId") String invitationId, @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Mark invitation as accepted.
     */
    @Modifying
    @Query("UPDATE ProjectInvitation pi SET pi.status = 'ACCEPTED', pi.acceptedAt = :acceptedAt, " +
           "pi.registeredUserId = :userId, pi.registeredOrganizationId = :organizationId, " +
           "pi.updatedAt = :updatedAt WHERE pi.id = :invitationId")
    int markAsAccepted(@Param("invitationId") String invitationId,
                       @Param("acceptedAt") LocalDateTime acceptedAt,
                       @Param("userId") String userId,
                       @Param("organizationId") String organizationId,
                       @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Mark invitation as declined.
     */
    @Modifying
    @Query("UPDATE ProjectInvitation pi SET pi.status = 'DECLINED', pi.declinedAt = :declinedAt, " +
           "pi.updatedAt = :updatedAt WHERE pi.id = :invitationId")
    int markAsDeclined(@Param("invitationId") String invitationId,
                       @Param("declinedAt") LocalDateTime declinedAt,
                       @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Update resend information.
     */
    @Modifying
    @Query("UPDATE ProjectInvitation pi SET pi.emailSentCount = pi.emailSentCount + 1, " +
           "pi.lastEmailSentAt = :sentAt, pi.expiresAt = :newExpiresAt, pi.updatedAt = :updatedAt " +
           "WHERE pi.id = :invitationId")
    int updateResendInfo(@Param("invitationId") String invitationId,
                         @Param("sentAt") LocalDateTime sentAt,
                         @Param("newExpiresAt") LocalDateTime newExpiresAt,
                         @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Get invitation analytics for organization.
     */
    @Query("SELECT " +
           "COUNT(CASE WHEN pi.status = 'PENDING' THEN 1 END) as pendingInvitations, " +
           "COUNT(CASE WHEN pi.status = 'ACCEPTED' THEN 1 END) as acceptedInvitations, " +
           "COUNT(CASE WHEN pi.status = 'DECLINED' THEN 1 END) as declinedInvitations, " +
           "COUNT(CASE WHEN pi.status = 'EXPIRED' THEN 1 END) as expiredInvitations " +
           "FROM ProjectInvitation pi WHERE pi.invitedByOrganizationId = :organizationId")
    Object[] getInvitationAnalytics(@Param("organizationId") String organizationId);
}