package com.berryfi.portal.service;

import com.berryfi.portal.entity.CreditConversionConfig;
import com.berryfi.portal.entity.VmPricingConfig;
import com.berryfi.portal.repository.CreditConversionConfigRepository;
import com.berryfi.portal.repository.VmPricingConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service for handling pricing calculations based on database configuration.
 */
@Service
public class PricingService {

    private final CreditConversionConfigRepository conversionConfigRepository;
    private final VmPricingConfigRepository vmPricingConfigRepository;

    @Autowired
    public PricingService(CreditConversionConfigRepository conversionConfigRepository,
                         VmPricingConfigRepository vmPricingConfigRepository) {
        this.conversionConfigRepository = conversionConfigRepository;
        this.vmPricingConfigRepository = vmPricingConfigRepository;
    }

    /**
     * Convert INR amount to credits using current active conversion rate
     */
    public Double convertINRToCredits(Double inrAmount) {
        if (inrAmount == null || inrAmount <= 0) {
            return 0.0;
        }

        Optional<CreditConversionConfig> activeConfig = conversionConfigRepository.findCurrentActiveRate();
        if (activeConfig.isEmpty()) {
            throw new RuntimeException("No active credit conversion configuration found");
        }

        return activeConfig.get().calculateCreditsFromINR(inrAmount);
    }

    /**
     * Convert credits to INR amount using current active conversion rate
     */
    public Double convertCreditsToINR(Double credits) {
        if (credits == null || credits <= 0) {
            return 0.0;
        }

        Optional<CreditConversionConfig> activeConfig = conversionConfigRepository.findCurrentActiveRate();
        if (activeConfig.isEmpty()) {
            throw new RuntimeException("No active credit conversion configuration found");
        }

        return activeConfig.get().calculateINRFromCredits(credits);
    }

    /**
     * Calculate credits required for VM usage based on duration in seconds
     */
    public Double calculateVmUsageCredits(String vmType, Double durationInSeconds) {
        if (vmType == null || durationInSeconds == null || durationInSeconds <= 0) {
            return 0.0;
        }

        Optional<VmPricingConfig> activeConfig = vmPricingConfigRepository.findCurrentActiveConfigForVmType(vmType);
        if (activeConfig.isEmpty()) {
            throw new RuntimeException("No active pricing configuration found for VM type: " + vmType);
        }

        return activeConfig.get().calculateCreditsForDuration(durationInSeconds);
    }

    /**
     * Get current active credit conversion configuration
     */
    public Optional<CreditConversionConfig> getCurrentConversionConfig() {
        return conversionConfigRepository.findCurrentActiveRate();
    }

    /**
     * Get current active VM pricing configuration for a specific VM type
     */
    public Optional<VmPricingConfig> getCurrentVmPricingConfig(String vmType) {
        return vmPricingConfigRepository.findCurrentActiveConfigForVmType(vmType);
    }

    /**
     * Get all current active VM pricing configurations
     */
    public List<VmPricingConfig> getAllCurrentVmPricingConfigs() {
        return vmPricingConfigRepository.findAllCurrentActiveConfigs();
    }

    /**
     * Get current INR per credit rate
     */
    public Double getCurrentINRPerCreditRate() {
        Optional<CreditConversionConfig> config = getCurrentConversionConfig();
        return config.map(CreditConversionConfig::getInrPerCredit).orElse(null);
    }

    /**
     * Get current credits per minute rate for a VM type
     */
    public Double getCurrentCreditsPerMinuteForVm(String vmType) {
        Optional<VmPricingConfig> config = getCurrentVmPricingConfig(vmType);
        return config.map(VmPricingConfig::getCreditsPerMinute).orElse(null);
    }
}
