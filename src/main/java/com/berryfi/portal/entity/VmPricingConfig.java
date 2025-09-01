package com.berryfi.portal.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing VM pricing configuration.
 * This stores the credit cost per minute for different VM types.
 */
@Entity
@Table(name = "vm_pricing_config")
public class VmPricingConfig {
    
    @Id
    @Column(length = 50)
    private String id;
    
    @Column(name = "vm_type", nullable = false)
    private String vmType; // e.g., "T4", "A10"
    
    @Column(name = "credits_per_minute", nullable = false)
    private Double creditsPerMinute; // e.g., 1.0 for T4, 2.0 for A10
    
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
    public VmPricingConfig() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.effectiveFrom = LocalDateTime.now();
    }

    public VmPricingConfig(String id, String vmType, Double creditsPerMinute, String createdBy, String description) {
        this();
        this.id = id;
        this.vmType = vmType;
        this.creditsPerMinute = creditsPerMinute;
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

    public String getVmType() {
        return vmType;
    }

    public void setVmType(String vmType) {
        this.vmType = vmType;
    }

    public Double getCreditsPerMinute() {
        return creditsPerMinute;
    }

    public void setCreditsPerMinute(Double creditsPerMinute) {
        this.creditsPerMinute = creditsPerMinute;
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
     * Calculate total credits for a given duration in seconds
     */
    public Double calculateCreditsForDuration(Double durationInSeconds) {
        if (durationInSeconds == null || creditsPerMinute == null) {
            return 0.0;
        }
        double minutes = durationInSeconds / 60.0;
        return minutes * creditsPerMinute;
    }

    /**
     * Check if this pricing config is currently active
     */
    public boolean isCurrentlyActive() {
        LocalDateTime now = LocalDateTime.now();
        return isActive && 
               effectiveFrom.isBefore(now) && 
               (effectiveUntil == null || effectiveUntil.isAfter(now));
    }
}
