package com.berryfi.portal.repository;

import com.berryfi.portal.entity.User;
import com.berryfi.portal.enums.Role;
import com.berryfi.portal.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for User entity.
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * Find user by email.
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by refresh token.
     */
    Optional<User> findByRefreshToken(String refreshToken);

    /**
     * Find users by organization ID.
     */
    List<User> findByOrganizationId(String organizationId);

    /**
     * Find users by workspace ID.
     */
    List<User> findByWorkspaceId(String workspaceId);

    /**
     * Find users by role.
     */
    List<User> findByRole(Role role);

    /**
     * Find users by status.
     */
    List<User> findByStatus(UserStatus status);

    /**
     * Find users by organization and role.
     */
    List<User> findByOrganizationIdAndRole(String organizationId, Role role);

    /**
     * Find users by workspace and role.
     */
    List<User> findByWorkspaceIdAndRole(String workspaceId, Role role);

    /**
     * Find users by organization and status.
     */
    List<User> findByOrganizationIdAndStatus(String organizationId, UserStatus status);

    /**
     * Find users by workspace and status.
     */
    List<User> findByWorkspaceIdAndStatus(String workspaceId, UserStatus status);

    /**
     * Check if email exists.
     */
    boolean existsByEmail(String email);

    /**
     * Count users by organization.
     */
    long countByOrganizationId(String organizationId);

    /**
     * Count users by workspace.
     */
    long countByWorkspaceId(String workspaceId);

    /**
     * Count active users by organization.
     */
    long countByOrganizationIdAndStatus(String organizationId, UserStatus status);

    /**
     * Count active users by workspace.
     */
    long countByWorkspaceIdAndStatus(String workspaceId, UserStatus status);

    /**
     * Update refresh token for user.
     */
    @Modifying
    @Query("UPDATE User u SET u.refreshToken = :refreshToken WHERE u.id = :userId")
    void updateRefreshToken(@Param("userId") String userId, @Param("refreshToken") String refreshToken);

    /**
     * Clear refresh token for user.
     */
    @Modifying
    @Query("UPDATE User u SET u.refreshToken = null WHERE u.id = :userId")
    void clearRefreshToken(@Param("userId") String userId);

    /**
     * Update last login timestamp.
     */
    @Modifying
    @Query("UPDATE User u SET u.lastLogin = CURRENT_TIMESTAMP WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") String userId);
}
