package com.berryfi.portal.dto;

import java.time.LocalDateTime;

/**
 * DTO for billing transaction responses.
 */
public class BillingTransactionDto {
    private String id;
    private String userId;
    private String projectId;
    private String organizationId;
    private String transactionType;
    private Double amount;
    private Double credits;
    private String description;
    private LocalDateTime createdAt;
    private String vmSessionId;
    private String vmType;
    private Double usageHours;

    // Constructors
    public BillingTransactionDto() {}

    public BillingTransactionDto(String id, String userId, String projectId, String organizationId, 
                                String transactionType, Double amount, 
                                Double credits, String description, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.projectId = projectId;
        this.organizationId = organizationId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.credits = credits;
        this.description = description;
        this.createdAt = createdAt;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }



    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public Double getCredits() { return credits; }
    public void setCredits(Double credits) { this.credits = credits; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getVmSessionId() { return vmSessionId; }
    public void setVmSessionId(String vmSessionId) { this.vmSessionId = vmSessionId; }

    public String getVmType() { return vmType; }
    public void setVmType(String vmType) { this.vmType = vmType; }

    public Double getUsageHours() { return usageHours; }
    public void setUsageHours(Double usageHours) { this.usageHours = usageHours; }
}
