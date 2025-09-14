package com.berryfi.portal.dto.billing;

import com.berryfi.portal.util.NumberFormatUtil;
/**
 * DTO for organization billing balance information.
 */
public class BillingBalanceDto {
    
    private String organizationId;
    private Double currentBalance;
    private Double totalCredits;
    private Double usedCredits;
    private Double remainingCredits;
    private Double monthlySpend;
    private Double lastTransactionAmount;
    private String subscriptionPlanId;
    private String subscriptionPlanName;
    private String billingCycle;
    private Boolean isOverage;
    private Double overageAmount;
    private String currency;

    // Constructors
    public BillingBalanceDto() {}

    public BillingBalanceDto(String organizationId, Double currentBalance, 
                           Double totalCredits, Double usedCredits, 
                           Double remainingCredits) {
        this.organizationId = organizationId;
        this.currentBalance = currentBalance;
        this.totalCredits = totalCredits;
        this.usedCredits = usedCredits;
        this.remainingCredits = remainingCredits;
        this.currency = "USD";
        this.isOverage = false;
        this.overageAmount = 0.0;
        this.monthlySpend = 0.0;
    }

    // Getters and Setters
    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public Double getCurrentBalance() {
        return NumberFormatUtil.formatCredits(currentBalance);
    }

    public void setCurrentBalance(Double currentBalance) {
        this.currentBalance = currentBalance;
    }

    public Double getTotalCredits() {
        return NumberFormatUtil.formatCredits(totalCredits);
    }

    public void setTotalCredits(Double totalCredits) {
        this.totalCredits = totalCredits;
    }

    public Double getUsedCredits() {
        return NumberFormatUtil.formatCredits(usedCredits);
    }

    public void setUsedCredits(Double usedCredits) {
        this.usedCredits = usedCredits;
    }

    public Double getRemainingCredits() {
        return NumberFormatUtil.formatCredits(remainingCredits);
    }

    public void setRemainingCredits(Double remainingCredits) {
        this.remainingCredits = remainingCredits;
    }

    public Double getMonthlySpend() {
        return NumberFormatUtil.formatCredits(monthlySpend);
    }

    public void setMonthlySpend(Double monthlySpend) {
        this.monthlySpend = monthlySpend;
    }

    public Double getLastTransactionAmount() {
        return NumberFormatUtil.formatCredits(lastTransactionAmount);
    }

    public void setLastTransactionAmount(Double lastTransactionAmount) {
        this.lastTransactionAmount = lastTransactionAmount;
    }

    public String getSubscriptionPlanId() {
        return subscriptionPlanId;
    }

    public void setSubscriptionPlanId(String subscriptionPlanId) {
        this.subscriptionPlanId = subscriptionPlanId;
    }

    public String getSubscriptionPlanName() {
        return subscriptionPlanName;
    }

    public void setSubscriptionPlanName(String subscriptionPlanName) {
        this.subscriptionPlanName = subscriptionPlanName;
    }

    public String getBillingCycle() {
        return billingCycle;
    }

    public void setBillingCycle(String billingCycle) {
        this.billingCycle = billingCycle;
    }

    public Boolean getIsOverage() {
        return isOverage;
    }

    public void setIsOverage(Boolean isOverage) {
        this.isOverage = isOverage;
    }

    public Double getOverageAmount() {
        return NumberFormatUtil.formatCredits(overageAmount);
    }

    public void setOverageAmount(Double overageAmount) {
        this.overageAmount = overageAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
