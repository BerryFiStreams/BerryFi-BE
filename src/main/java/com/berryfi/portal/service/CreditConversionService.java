package com.berryfi.portal.service;

import com.berryfi.portal.entity.CreditConversionConfig;
import com.berryfi.portal.repository.CreditConversionConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing credit conversion operations.
 */
@Service
@Transactional
public class CreditConversionService {

    @Autowired
    private CreditConversionConfigRepository conversionConfigRepository;

    /**
     * Get the current active conversion rate
     */
    public Optional<CreditConversionConfig> getCurrentConversionRate() {
        return conversionConfigRepository.findCurrentActiveRate();
    }

    /**
     * Calculate credits from INR amount using current rate
     */
    public Double calculateCreditsFromINR(Double inrAmount) {
        Optional<CreditConversionConfig> config = getCurrentConversionRate();
        if (config.isPresent()) {
            return config.get().calculateCreditsFromINR(inrAmount);
        }
        throw new RuntimeException("No active conversion rate found");
    }

    /**
     * Calculate INR amount from credits using current rate
     */
    public Double calculateINRFromCredits(Double credits) {
        Optional<CreditConversionConfig> config = getCurrentConversionRate();
        if (config.isPresent()) {
            return config.get().calculateINRFromCredits(credits);
        }
        throw new RuntimeException("No active conversion rate found");
    }

    /**
     * Create or update conversion rate configuration
     */
    public CreditConversionConfig createConversionRate(Double inrPerCredit, String createdBy, String description) {
        // Deactivate current active rate
        Optional<CreditConversionConfig> currentActive = getCurrentConversionRate();
        if (currentActive.isPresent()) {
            CreditConversionConfig current = currentActive.get();
            current.setIsActive(false);
            current.setEffectiveUntil(LocalDateTime.now());
            conversionConfigRepository.save(current);
        }

        // Create new active rate
        CreditConversionConfig newConfig = new CreditConversionConfig();
        newConfig.setId(UUID.randomUUID().toString());
        newConfig.setInrPerCredit(inrPerCredit);
        newConfig.setCreatedBy(createdBy);
        newConfig.setDescription(description);
        newConfig.setIsActive(true);
        newConfig.setEffectiveFrom(LocalDateTime.now());

        return conversionConfigRepository.save(newConfig);
    }

    /**
     * Get current conversion rate value
     */
    public Double getCurrentRateValue() {
        Optional<CreditConversionConfig> config = getCurrentConversionRate();
        return config.map(CreditConversionConfig::getInrPerCredit).orElse(2.75); // Default fallback
    }
}
