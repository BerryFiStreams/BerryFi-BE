package com.berryfi.portal.repository;

import com.berryfi.portal.entity.SubscriptionPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for SubscriptionPlan entity.
 */
@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, String> {

    /**
     * Find all active subscription plans
     */
    List<SubscriptionPlan> findByActiveTrueOrderByPriceAsc();

    /**
     * Find subscription plan by name
     */
    Optional<SubscriptionPlan> findByNameAndActiveTrue(String name);

    /**
     * Find subscription plans by max CCU
     */
    List<SubscriptionPlan> findByMaxCCUGreaterThanEqualAndActiveTrueOrderByPriceAsc(Integer maxCCU);

    /**
     * Find subscription plans by price range
     */
    @Query("SELECT sp FROM SubscriptionPlan sp WHERE sp.active = true " +
           "AND sp.price BETWEEN :minPrice AND :maxPrice " +
           "ORDER BY sp.price ASC")
    List<SubscriptionPlan> findByPriceRange(
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice);

    /**
     * Find popular subscription plans
     */
    List<SubscriptionPlan> findByIsPopularTrueAndActiveTrueOrderByPriceAsc();

    /**
     * Find cheapest active plan
     */
    Optional<SubscriptionPlan> findFirstByActiveTrueOrderByPriceAsc();

    /**
     * Find most expensive active plan
     */
    Optional<SubscriptionPlan> findFirstByActiveTrueOrderByPriceDesc();

    /**
     * Count active plans
     */
    Long countByActiveTrue();

    /**
     * Find plans by minimum credits
     */
    @Query("SELECT sp FROM SubscriptionPlan sp WHERE sp.active = true " +
           "AND sp.credits >= :minCredits " +
           "ORDER BY sp.price ASC")
    List<SubscriptionPlan> findByMinimumCredits(@Param("minCredits") Double minCredits);

    /**
     * Get all plans paginated
     */
    Page<SubscriptionPlan> findByActiveTrueOrderByPriceAsc(Pageable pageable);

    /**
     * Search plans by name
     */
    @Query("SELECT sp FROM SubscriptionPlan sp WHERE sp.active = true " +
           "AND LOWER(sp.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY sp.price ASC")
    Page<SubscriptionPlan> searchActivePlans(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Get plan statistics
     */
    @Query("SELECT AVG(sp.price) FROM SubscriptionPlan sp WHERE sp.active = true")
    Double getAveragePriceOfActivePlans();

    /**
     * Get max CCU offered by any active plan
     */
    @Query("SELECT MAX(sp.maxCCU) FROM SubscriptionPlan sp WHERE sp.active = true")
    Integer getMaxCCUOffered();

    /**
     * Get total credits offered by all active plans
     */
    @Query("SELECT SUM(sp.credits) FROM SubscriptionPlan sp WHERE sp.active = true")
    Double getTotalCreditsOffered();
}