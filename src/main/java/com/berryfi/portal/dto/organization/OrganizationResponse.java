package com.berryfi.portal.dto.organization;

import com.berryfi.portal.enums.OrganizationStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * Response DTO for organization details.
 */
public class OrganizationResponse {
    
    private String id;
    private String name;
    private String description;
    private String ownerId;
    private String ownerEmail;
    private String ownerName;
    private OrganizationStatus status;
    private Double totalCredits;
    private Double usedCredits;
    private Double remainingCredits;
    private Double purchasedCredits;
    private Double giftedCredits;
    private Double monthlyBudget;
    private Double monthlyCreditsUsed;
    private Boolean canShareProjects;
    private Boolean canReceiveSharedProjects;
    private Integer maxProjects;
    private Integer maxMembers;
    private Integer activeProjects;
    private Integer totalMembers;
    private Integer totalSessions;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // Constructors
    public OrganizationResponse() {}

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

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public OrganizationStatus getStatus() {
        return status;
    }

    public void setStatus(OrganizationStatus status) {
        this.status = status;
    }

    public Double getTotalCredits() {
        return totalCredits;
    }

    public void setTotalCredits(Double totalCredits) {
        this.totalCredits = totalCredits;
    }

    public Double getUsedCredits() {
        return usedCredits;
    }

    public void setUsedCredits(Double usedCredits) {
        this.usedCredits = usedCredits;
    }

    public Double getRemainingCredits() {
        return remainingCredits;
    }

    public void setRemainingCredits(Double remainingCredits) {
        this.remainingCredits = remainingCredits;
    }

    public Double getPurchasedCredits() {
        return purchasedCredits;
    }

    public void setPurchasedCredits(Double purchasedCredits) {
        this.purchasedCredits = purchasedCredits;
    }

    public Double getGiftedCredits() {
        return giftedCredits;
    }

    public void setGiftedCredits(Double giftedCredits) {
        this.giftedCredits = giftedCredits;
    }

    public Double getMonthlyBudget() {
        return monthlyBudget;
    }

    public void setMonthlyBudget(Double monthlyBudget) {
        this.monthlyBudget = monthlyBudget;
    }

    public Double getMonthlyCreditsUsed() {
        return monthlyCreditsUsed;
    }

    public void setMonthlyCreditsUsed(Double monthlyCreditsUsed) {
        this.monthlyCreditsUsed = monthlyCreditsUsed;
    }

    public Boolean getCanShareProjects() {
        return canShareProjects;
    }

    public void setCanShareProjects(Boolean canShareProjects) {
        this.canShareProjects = canShareProjects;
    }

    public Boolean getCanReceiveSharedProjects() {
        return canReceiveSharedProjects;
    }

    public void setCanReceiveSharedProjects(Boolean canReceiveSharedProjects) {
        this.canReceiveSharedProjects = canReceiveSharedProjects;
    }

    public Integer getMaxProjects() {
        return maxProjects;
    }

    public void setMaxProjects(Integer maxProjects) {
        this.maxProjects = maxProjects;
    }

    public Integer getMaxMembers() {
        return maxMembers;
    }

    public void setMaxMembers(Integer maxMembers) {
        this.maxMembers = maxMembers;
    }

    public Integer getActiveProjects() {
        return activeProjects;
    }

    public void setActiveProjects(Integer activeProjects) {
        this.activeProjects = activeProjects;
    }

    public Integer getTotalMembers() {
        return totalMembers;
    }

    public void setTotalMembers(Integer totalMembers) {
        this.totalMembers = totalMembers;
    }

    public Integer getTotalSessions() {
        return totalSessions;
    }

    public void setTotalSessions(Integer totalSessions) {
        this.totalSessions = totalSessions;
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
}