package com.berryfi.portal.repository;

import com.berryfi.portal.entity.VmPricingConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VmPricingConfigRepository extends JpaRepository<VmPricingConfig, String> {
    
    /**
     * Find the active pricing config for a specific VM type at the current time
     */
    @Query("SELECT v FROM VmPricingConfig v WHERE v.vmType = :vmType " +
           "AND v.isActive = true " +
           "AND v.effectiveFrom <= :currentTime " +
           "AND (v.effectiveUntil IS NULL OR v.effectiveUntil > :currentTime) " +
           "ORDER BY v.effectiveFrom DESC")
    Optional<VmPricingConfig> findActiveConfigForVmType(@Param("vmType") String vmType, 
                                                        @Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Find the currently active config for a VM type (convenience method)
     */
    default Optional<VmPricingConfig> findCurrentActiveConfigForVmType(String vmType) {
        return findActiveConfigForVmType(vmType, LocalDateTime.now());
    }
    
    /**
     * Find all currently active VM pricing configs
     */
    @Query("SELECT v FROM VmPricingConfig v WHERE v.isActive = true " +
           "AND v.effectiveFrom <= :currentTime " +
           "AND (v.effectiveUntil IS NULL OR v.effectiveUntil > :currentTime) " +
           "ORDER BY v.vmType, v.effectiveFrom DESC")
    List<VmPricingConfig> findAllActiveConfigs(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Find all currently active configs (convenience method)
     */
    default List<VmPricingConfig> findAllCurrentActiveConfigs() {
        return findAllActiveConfigs(LocalDateTime.now());
    }
    
    /**
     * Find all configurations for a specific VM type (active and inactive)
     */
    @Query("SELECT v FROM VmPricingConfig v WHERE v.vmType = :vmType " +
           "ORDER BY v.effectiveFrom DESC")
    List<VmPricingConfig> findAllConfigsForVmType(@Param("vmType") String vmType);
}
