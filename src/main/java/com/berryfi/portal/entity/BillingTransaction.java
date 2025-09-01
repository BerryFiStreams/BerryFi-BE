package com.berryfi.portal.entity;

import com.berryfi.portal.enums.TransactionType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a billing transaction.
 */
@Entity
@Table(name = "billing_transactions",
       indexes = {
           @Index(name = "idx_transaction_organization", columnList = "organizationId"),
           @Index(name = "idx_transaction_workspace", columnList = "workspaceId"),
           @Index(name = "idx_transaction_date", columnList = "date"),
           @Index(name = "idx_transaction_type", columnList = "type")
       })
public class BillingTransaction {
    
    @Id
    @Column(length = 50)
    private String id;
    
    @Column(name = "organization_id", nullable = false)
    private String organizationId;
    
    @Column(name = "workspace_id")
    private String workspaceId;
    
    @Column(name = "workspace_name")
    private String workspaceName;
    
    @Column(nullable = false)
    private LocalDateTime date;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private Double amount;
    
    @Column(name = "resulting_balance", nullable = false)
    private Double resultingBalance;
    
    @Column(nullable = false)
    private String status;
    
    @Column
    private String reference;
    
    @Column(name = "processed_by")
    private String processedBy; // Admin who processed the transaction
    
    @Column(name = "inr_amount")
    private Double inrAmount; // Original INR amount (for recharge transactions)
    
    @Column(name = "conversion_rate")
    private Double conversionRate; // INR per credit rate used at time of transaction
    
    @Column(name = "vm_type")
    private String vmType; // VM type for usage transactions (e.g., "T4", "A10")
    
    @Column(name = "duration_seconds")
    private Double durationSeconds; // Duration in seconds for VM usage
    
    @Column(name = "credits_per_minute")
    private Double creditsPerMinute; // Credits per minute rate used for VM billing
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public BillingTransaction() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public BillingTransaction(String id, String organizationId, TransactionType type, 
                            String description, Double amount, Double resultingBalance) {
        this();
        this.id = id;
        this.organizationId = organizationId;
        this.type = type;
        this.description = description;
        this.amount = amount;
        this.resultingBalance = resultingBalance;
        this.date = LocalDateTime.now();
        this.status = "completed";
    }

    public BillingTransaction(String id, String organizationId, TransactionType type, 
                            String description, Double amount, Double resultingBalance,
                            String processedBy, Double inrAmount, Double conversionRate) {
        this(id, organizationId, type, description, amount, resultingBalance);
        this.processedBy = processedBy;
        this.inrAmount = inrAmount;
        this.conversionRate = conversionRate;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getWorkspaceName() {
        return workspaceName;
    }

    public void setWorkspaceName(String workspaceName) {
        this.workspaceName = workspaceName;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getResultingBalance() {
        return resultingBalance;
    }

    public void setResultingBalance(Double resultingBalance) {
        this.resultingBalance = resultingBalance;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getProcessedBy() {
        return processedBy;
    }

    public void setProcessedBy(String processedBy) {
        this.processedBy = processedBy;
    }

    public Double getInrAmount() {
        return inrAmount;
    }

    public void setInrAmount(Double inrAmount) {
        this.inrAmount = inrAmount;
    }

    public Double getConversionRate() {
        return conversionRate;
    }

    public void setConversionRate(Double conversionRate) {
        this.conversionRate = conversionRate;
    }

    public String getVmType() {
        return vmType;
    }

    public void setVmType(String vmType) {
        this.vmType = vmType;
    }

    public Double getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Double durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public Double getCreditsPerMinute() {
        return creditsPerMinute;
    }

    public void setCreditsPerMinute(Double creditsPerMinute) {
        this.creditsPerMinute = creditsPerMinute;
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
