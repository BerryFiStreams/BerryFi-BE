package com.berryfi.portal.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing credit conversion configuration.
 * This stores the INR to credit conversion rate and other related settings.
 */
@Entity
@Table(name = "credit_conversion_config")
public class CreditConversionConfig {
    
    @Id
    @Column(length = 50)
    private String id;
    
    @Column(name = "inr_per_credit", nullable = false)
    private Double inrPerCredit; // e.g., 2.75 INR = 1 credit
    
    @Column(name = "currency", nullable = false)
    private String currency = "INR";
    
    @Column(name = "effective_from", nullable = false)
    private LocalDateTime effectiveFrom;
    
    @Column(name = "effective_until")
    private LocalDateTime effectiveUntil;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "created_by", nullable = false)
    private String createdBy;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public CreditConversionConfig() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.effectiveFrom = LocalDateTime.now();
    }

    public CreditConversionConfig(String id, Double inrPerCredit, String createdBy, String description) {
        this();
        this.id = id;
        this.inrPerCredit = inrPerCredit;
        this.createdBy = createdBy;
        this.description = description;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getInrPerCredit() {
        return inrPerCredit;
    }

    public void setInrPerCredit(Double inrPerCredit) {
        this.inrPerCredit = inrPerCredit;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public LocalDateTime getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDateTime effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public LocalDateTime getEffectiveUntil() {
        return effectiveUntil;
    }

    public void setEffectiveUntil(LocalDateTime effectiveUntil) {
        this.effectiveUntil = effectiveUntil;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Calculate credits from INR amount using this conversion rate
     */
    public Double calculateCreditsFromINR(Double inrAmount) {
        if (inrAmount == null || inrPerCredit == null || inrPerCredit <= 0) {
            return 0.0;
        }
        return inrAmount / inrPerCredit;
    }

    /**
     * Calculate INR amount from credits using this conversion rate
     */
    public Double calculateINRFromCredits(Double credits) {
        if (credits == null || inrPerCredit == null) {
            return 0.0;
        }
        return credits * inrPerCredit;
    }
}
