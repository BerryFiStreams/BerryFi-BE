package com.berryfi.portal.repository;

import com.berryfi.portal.entity.TrackingLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TrackingLinkRepository extends JpaRepository<TrackingLink, String> {

    /**
     * Find an active tracking link by short code.
     */
    @Query("SELECT tl FROM TrackingLink tl WHERE tl.shortCode = :shortCode AND tl.active = true AND (tl.expiresAt IS NULL OR tl.expiresAt > :now)")
    Optional<TrackingLink> findActiveByShortCode(String shortCode, LocalDateTime now);

    /**
     * Find tracking links by project ID.
     */
    Optional<TrackingLink> findByProjectIdAndUserIdAndActive(String projectId, String userId, boolean active);

    /**
     * Check if short code exists.
     */
    boolean existsByShortCode(String shortCode);
}
