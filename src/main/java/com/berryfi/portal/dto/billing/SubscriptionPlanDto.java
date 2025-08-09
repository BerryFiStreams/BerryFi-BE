package com.berryfi.portal.dto.billing;

import com.berryfi.portal.enums.BillingCycle;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for subscription plan information.
 */
public class SubscriptionPlanDto {
    
    private String id;
    private String name;
    private String description;
    private Double price;
    private BillingCycle billingCycle;
    private Double creditsIncluded;
    private Integer maxUsers;
    private Integer maxProjects;
    private Integer maxStorageGb;
    private List<String> features;
    private Boolean isActive;
    private Boolean isPopular;
    private LocalDateTime createdAt;

    // Constructors
    public SubscriptionPlanDto() {}

    public SubscriptionPlanDto(String id, String name, String description, 
                             Double price, BillingCycle billingCycle, 
                             Double creditsIncluded, Boolean isActive) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.billingCycle = billingCycle;
        this.creditsIncluded = creditsIncluded;
        this.isActive = isActive;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public BillingCycle getBillingCycle() {
        return billingCycle;
    }

    public void setBillingCycle(BillingCycle billingCycle) {
        this.billingCycle = billingCycle;
    }

    public Double getCreditsIncluded() {
        return creditsIncluded;
    }

    public void setCreditsIncluded(Double creditsIncluded) {
        this.creditsIncluded = creditsIncluded;
    }

    public Integer getMaxUsers() {
        return maxUsers;
    }

    public void setMaxUsers(Integer maxUsers) {
        this.maxUsers = maxUsers;
    }

    public Integer getMaxProjects() {
        return maxProjects;
    }

    public void setMaxProjects(Integer maxProjects) {
        this.maxProjects = maxProjects;
    }

    public Integer getMaxStorageGb() {
        return maxStorageGb;
    }

    public void setMaxStorageGb(Integer maxStorageGb) {
        this.maxStorageGb = maxStorageGb;
    }

    public List<String> getFeatures() {
        return features;
    }

    public void setFeatures(List<String> features) {
        this.features = features;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getIsPopular() {
        return isPopular;
    }

    public void setIsPopular(Boolean isPopular) {
        this.isPopular = isPopular;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
