package com.berryfi.portal.dto.billing;

/**
 * DTO for billing usage data.
 */
public class BillingUsageDto {
    private double currentMonth;
    private double previousMonth;
    private double projected;
    private double spendingChange;

    public BillingUsageDto() {}

    public BillingUsageDto(double currentMonth, double previousMonth, double projected, double spendingChange) {
        this.currentMonth = currentMonth;
        this.previousMonth = previousMonth;
        this.projected = projected;
        this.spendingChange = spendingChange;
    }

    // Getters and setters
    public double getCurrentMonth() { return currentMonth; }
    public void setCurrentMonth(double currentMonth) { this.currentMonth = currentMonth; }

    public double getPreviousMonth() { return previousMonth; }
    public void setPreviousMonth(double previousMonth) { this.previousMonth = previousMonth; }

    public double getProjected() { return projected; }
    public void setProjected(double projected) { this.projected = projected; }

    public double getSpendingChange() { return spendingChange; }
    public void setSpendingChange(double spendingChange) { this.spendingChange = spendingChange; }
}
