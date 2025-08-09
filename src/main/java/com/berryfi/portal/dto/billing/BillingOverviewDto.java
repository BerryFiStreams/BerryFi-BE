package com.berryfi.portal.dto.billing;

/**
 * DTO for billing overview data.
 */
public class BillingOverviewDto {
    private String accountId;
    private String accountName;
    private String accountType;
    private String status;
    private double credits;
    private double monthlyBudget;
    private String billingCycle;
    private String nextBillingDate;
    private boolean isMainAccount;
    private String workspaceId;
    private String workspaceName;

    public BillingOverviewDto() {}

    public BillingOverviewDto(String accountId, String accountName, String accountType, String status,
                             double credits, double monthlyBudget, String billingCycle, String nextBillingDate,
                             boolean isMainAccount, String workspaceId, String workspaceName) {
        this.accountId = accountId;
        this.accountName = accountName;
        this.accountType = accountType;
        this.status = status;
        this.credits = credits;
        this.monthlyBudget = monthlyBudget;
        this.billingCycle = billingCycle;
        this.nextBillingDate = nextBillingDate;
        this.isMainAccount = isMainAccount;
        this.workspaceId = workspaceId;
        this.workspaceName = workspaceName;
    }

    // Getters and setters
    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getCredits() { return credits; }
    public void setCredits(double credits) { this.credits = credits; }

    public double getMonthlyBudget() { return monthlyBudget; }
    public void setMonthlyBudget(double monthlyBudget) { this.monthlyBudget = monthlyBudget; }

    public String getBillingCycle() { return billingCycle; }
    public void setBillingCycle(String billingCycle) { this.billingCycle = billingCycle; }

    public String getNextBillingDate() { return nextBillingDate; }
    public void setNextBillingDate(String nextBillingDate) { this.nextBillingDate = nextBillingDate; }

    public boolean isMainAccount() { return isMainAccount; }
    public void setMainAccount(boolean mainAccount) { isMainAccount = mainAccount; }

    public String getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(String workspaceId) { this.workspaceId = workspaceId; }

    public String getWorkspaceName() { return workspaceName; }
    public void setWorkspaceName(String workspaceName) { this.workspaceName = workspaceName; }
}
