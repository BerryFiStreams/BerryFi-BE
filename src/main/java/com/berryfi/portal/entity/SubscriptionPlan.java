package com.berryfi.portal.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a subscription plan.
 */
@Entity
@Table(name = "subscription_plans")
public class SubscriptionPlan {
    
    @Id
    @Column(length = 50)
    private String id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private Double price;
    
    @Column(nullable = false)
    private String currency;
    
    @Column(nullable = false)
    private Double credits;
    
    @Column(name = "max_ccu", nullable = false)
    private Integer maxCCU;
    
    @Column(name = "is_popular", nullable = false)
    private Boolean isPopular;
    
    @Column(columnDefinition = "TEXT")
    private String features; // JSON string of features array
    
    @Column(nullable = false)
    private Boolean active;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public SubscriptionPlan() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.active = true;
        this.isPopular = false;
    }

    public SubscriptionPlan(String id, String name, Double price, String currency, 
                           Double credits, Integer maxCCU) {
        this();
        this.id = id;
        this.name = name;
        this.price = price;
        this.currency = currency;
        this.credits = credits;
        this.maxCCU = maxCCU;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Double getCredits() {
        return credits;
    }

    public void setCredits(Double credits) {
        this.credits = credits;
    }

    public Integer getMaxCCU() {
        return maxCCU;
    }

    public void setMaxCCU(Integer maxCCU) {
        this.maxCCU = maxCCU;
    }

    public Boolean getIsPopular() {
        return isPopular;
    }

    public void setIsPopular(Boolean isPopular) {
        this.isPopular = isPopular;
    }

    public String getFeatures() {
        return features;
    }

    public void setFeatures(String features) {
        this.features = features;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
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
}
