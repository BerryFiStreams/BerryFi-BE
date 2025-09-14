package com.berryfi.portal.dto.billing;

import com.berryfi.portal.enums.InvoiceStatus;
import com.berryfi.portal.util.NumberFormatUtil;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for billing invoice information.
 */
public class BillingInvoiceDto {
    
    private String id;
    private String organizationId;
    private String subscriptionPlanId;
    private String invoiceNumber;
    private InvoiceStatus status;
    private Double amount;
    private Double taxAmount;
    private Double totalAmount;
    private String currency;
    private LocalDate billingPeriodStart;
    private LocalDate billingPeriodEnd;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDate paidDate;
    private String paymentMethod;
    private String paymentReference;
    private List<BillingInvoiceLineItemDto> lineItems;
    private String notes;
    private LocalDateTime createdAt;

    // Constructors
    public BillingInvoiceDto() {}

    public BillingInvoiceDto(String id, String organizationId, String invoiceNumber, 
                           InvoiceStatus status, Double amount, LocalDate issueDate, 
                           LocalDate dueDate) {
        this.id = id;
        this.organizationId = organizationId;
        this.invoiceNumber = invoiceNumber;
        this.status = status;
        this.amount = amount;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.currency = "USD";
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

    public String getSubscriptionPlanId() {
        return subscriptionPlanId;
    }

    public void setSubscriptionPlanId(String subscriptionPlanId) {
        this.subscriptionPlanId = subscriptionPlanId;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public void setStatus(InvoiceStatus status) {
        this.status = status;
    }

    public Double getAmount() {
        return NumberFormatUtil.formatCurrency(amount);
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getTaxAmount() {
        return NumberFormatUtil.formatCurrency(taxAmount);
    }

    public void setTaxAmount(Double taxAmount) {
        this.taxAmount = taxAmount;
    }

    public Double getTotalAmount() {
        return NumberFormatUtil.formatCurrency(totalAmount);
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public LocalDate getBillingPeriodStart() {
        return billingPeriodStart;
    }

    public void setBillingPeriodStart(LocalDate billingPeriodStart) {
        this.billingPeriodStart = billingPeriodStart;
    }

    public LocalDate getBillingPeriodEnd() {
        return billingPeriodEnd;
    }

    public void setBillingPeriodEnd(LocalDate billingPeriodEnd) {
        this.billingPeriodEnd = billingPeriodEnd;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getPaidDate() {
        return paidDate;
    }

    public void setPaidDate(LocalDate paidDate) {
        this.paidDate = paidDate;
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

    public List<BillingInvoiceLineItemDto> getLineItems() {
        return lineItems;
    }

    public void setLineItems(List<BillingInvoiceLineItemDto> lineItems) {
        this.lineItems = lineItems;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Inner class for invoice line items
     */
    public static class BillingInvoiceLineItemDto {
        private String description;
        private Integer quantity;
        private Double unitPrice;
        private Double amount;

        public BillingInvoiceLineItemDto() {}

        public BillingInvoiceLineItemDto(String description, Integer quantity, 
                                       Double unitPrice, Double amount) {
            this.description = description;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.amount = amount;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public Double getUnitPrice() {
            return NumberFormatUtil.formatCurrency(unitPrice);
        }

        public void setUnitPrice(Double unitPrice) {
            this.unitPrice = unitPrice;
        }

        public Double getAmount() {
            return NumberFormatUtil.formatCurrency(amount);
        }

        public void setAmount(Double amount) {
            this.amount = amount;
        }
    }
}
