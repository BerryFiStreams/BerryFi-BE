package com.berryfi.portal.dto.billing;

import com.berryfi.portal.enums.TransactionType;
import com.berryfi.portal.util.NumberFormatUtil;
import java.time.LocalDateTime;

/**
 * DTO for billing transaction information.
 */
public class BillingTransactionDto {
    
    private String id;
    private String organizationId;
    private String invoiceId;
    private TransactionType type;
    private Double amount;
    private Double creditsAmount;
    private String description;
    private String reference;
    private String paymentMethod;
    private String paymentReference;
    private LocalDateTime transactionDate;
    private LocalDateTime createdAt;

    // Constructors
    public BillingTransactionDto() {}

    public BillingTransactionDto(String id, String organizationId, 
                               TransactionType type, Double amount, 
                               String description, LocalDateTime transactionDate) {
        this.id = id;
        this.organizationId = organizationId;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.transactionDate = transactionDate;
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

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public Double getAmount() {
        return NumberFormatUtil.formatCurrency(amount);
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getCreditsAmount() {
        return NumberFormatUtil.formatCredits(creditsAmount);
    }

    public void setCreditsAmount(Double creditsAmount) {
        this.creditsAmount = creditsAmount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
