package com.berryfi.portal.repository;

import com.berryfi.portal.entity.CreditConversionConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository interface for CreditConversionConfig entity.
 */
@Repository
public interface CreditConversionConfigRepository extends JpaRepository<CreditConversionConfig, String> {

    /**
     * Find the active conversion rate for the current date
     */
    @Query("SELECT c FROM CreditConversionConfig c WHERE c.isActive = true " +
           "AND c.effectiveFrom <= :currentTime " +
           "AND (c.effectiveUntil IS NULL OR c.effectiveUntil >= :currentTime) " +
           "ORDER BY c.effectiveFrom DESC")
    Optional<CreditConversionConfig> findActiveConversionRate(LocalDateTime currentTime);

    /**
     * Find all active conversion rates
     */
    @Query("SELECT c FROM CreditConversionConfig c WHERE c.isActive = true " +
           "ORDER BY c.effectiveFrom DESC")
    Iterable<CreditConversionConfig> findAllActive();

    /**
     * Find the current active conversion rate (shorthand)
     */
    default Optional<CreditConversionConfig> findCurrentActiveRate() {
        return findActiveConversionRate(LocalDateTime.now());
    }
}
