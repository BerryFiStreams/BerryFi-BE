package com.berryfi.portal.dto.workspace;

import com.berryfi.portal.entity.Workspace;
import com.berryfi.portal.enums.WorkspaceStatus;
import com.berryfi.portal.enums.BudgetAction;
import com.berryfi.portal.util.NumberFormatUtil;

import java.time.LocalDateTime;

/**
 * DTO for workspace response.
 */
public class WorkspaceResponse {

    private String id;
    private String name;
    private String description;
    private String organizationId;
    private String adminEmail;
    private String adminName;
    private WorkspaceStatus status;
    private Double currentBalance;
    private Double monthlyBudget;
    private BudgetAction budgetAction;
    private String projectId;
    
    // Computed field - not stored in entity
    private String projectName;
    
    private Integer teamMemberCount;
    private Integer sessionsThisMonth;
    private Double creditsUsedThisMonth;
    private Double budgetUsagePercentage;
    private Boolean isOverBudget;
    private Double giftedCredits;
    private Double purchasedCredits;
    private Double totalCreditsUsed;
    private Double remainingGiftedCredits;
    private Double remainingPurchasedCredits;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;

    public WorkspaceResponse() {}

    public WorkspaceResponse(Workspace workspace) {
        this.id = workspace.getId();
        this.name = workspace.getName();
        this.description = workspace.getDescription();
        this.organizationId = workspace.getOrganizationId();
        this.adminEmail = workspace.getAdminEmail();
        this.adminName = workspace.getAdminName();
        this.status = workspace.getStatus();
        this.currentBalance = workspace.getCurrentBalance();
        this.monthlyBudget = workspace.getMonthlyBudget();
        this.budgetAction = workspace.getBudgetAction();
        this.projectId = workspace.getProjectId();
        this.teamMemberCount = workspace.getTeamMemberCount();
        this.sessionsThisMonth = workspace.getSessionsThisMonth();
        this.creditsUsedThisMonth = workspace.getCreditsUsedThisMonth();
        this.budgetUsagePercentage = workspace.getBudgetUsagePercentage();
        this.isOverBudget = workspace.getIsOverBudget();
        this.giftedCredits = workspace.getGiftedCredits();
        this.purchasedCredits = workspace.getPurchasedCredits();
        this.totalCreditsUsed = workspace.getTotalCreditsUsed();
        this.remainingGiftedCredits = workspace.getRemainingGiftedCredits();
        this.remainingPurchasedCredits = workspace.getRemainingPurchasedCredits();
        this.createdAt = workspace.getCreatedAt();
        this.updatedAt = workspace.getUpdatedAt();
        this.createdBy = workspace.getCreatedBy();
    }

    // Static factory method
    public static WorkspaceResponse from(Workspace workspace) {
        return new WorkspaceResponse(workspace);
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

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }

    public WorkspaceStatus getStatus() {
        return status;
    }

    public void setStatus(WorkspaceStatus status) {
        this.status = status;
    }

    public Double getCurrentBalance() {
        return NumberFormatUtil.formatCredits(currentBalance);
    }

    public void setCurrentBalance(Double currentBalance) {
        this.currentBalance = currentBalance;
    }

    public Double getMonthlyBudget() {
        return monthlyBudget;
    }

    public void setMonthlyBudget(Double monthlyBudget) {
        this.monthlyBudget = monthlyBudget;
    }

    public BudgetAction getBudgetAction() {
        return budgetAction;
    }

    public void setBudgetAction(BudgetAction budgetAction) {
        this.budgetAction = budgetAction;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Integer getTeamMemberCount() {
        return teamMemberCount;
    }

    public void setTeamMemberCount(Integer teamMemberCount) {
        this.teamMemberCount = teamMemberCount;
    }

    public Integer getSessionsThisMonth() {
        return sessionsThisMonth;
    }

    public void setSessionsThisMonth(Integer sessionsThisMonth) {
        this.sessionsThisMonth = sessionsThisMonth;
    }

    public Double getCreditsUsedThisMonth() {
        return NumberFormatUtil.formatCredits(creditsUsedThisMonth);
    }

    public void setCreditsUsedThisMonth(Double creditsUsedThisMonth) {
        this.creditsUsedThisMonth = creditsUsedThisMonth;
    }

    public Double getBudgetUsagePercentage() {
        return NumberFormatUtil.formatPercentage(budgetUsagePercentage);
    }

    public void setBudgetUsagePercentage(Double budgetUsagePercentage) {
        this.budgetUsagePercentage = budgetUsagePercentage;
    }

    public Boolean getIsOverBudget() {
        return isOverBudget;
    }

    public void setIsOverBudget(Boolean isOverBudget) {
        this.isOverBudget = isOverBudget;
    }

    public Double getGiftedCredits() {
        return NumberFormatUtil.formatCredits(giftedCredits);
    }

    public void setGiftedCredits(Double giftedCredits) {
        this.giftedCredits = giftedCredits;
    }

    public Double getPurchasedCredits() {
        return NumberFormatUtil.formatCredits(purchasedCredits);
    }

    public void setPurchasedCredits(Double purchasedCredits) {
        this.purchasedCredits = purchasedCredits;
    }

    public Double getTotalCreditsUsed() {
        return NumberFormatUtil.formatCredits(totalCreditsUsed);
    }

    public void setTotalCreditsUsed(Double totalCreditsUsed) {
        this.totalCreditsUsed = totalCreditsUsed;
    }

    public Double getRemainingGiftedCredits() {
        return NumberFormatUtil.formatCredits(remainingGiftedCredits);
    }

    public void setRemainingGiftedCredits(Double remainingGiftedCredits) {
        this.remainingGiftedCredits = remainingGiftedCredits;
    }

    public Double getRemainingPurchasedCredits() {
        return NumberFormatUtil.formatCredits(remainingPurchasedCredits);
    }

    public void setRemainingPurchasedCredits(Double remainingPurchasedCredits) {
        this.remainingPurchasedCredits = remainingPurchasedCredits;
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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
