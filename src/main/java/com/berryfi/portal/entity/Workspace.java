package com.berryfi.portal.entity;

import com.berryfi.portal.enums.WorkspaceStatus;
import com.berryfi.portal.enums.BudgetAction;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a workspace in the system.
 */
@Entity
@Table(name = "workspaces", indexes = {
    @Index(name = "idx_workspace_organization", columnList = "organizationId"),
    @Index(name = "idx_workspace_status", columnList = "status"),
    @Index(name = "idx_workspace_admin", columnList = "adminEmail")
})
public class Workspace {

    @Id
    @Column(name = "id", length = 50)
    private String id;

    @NotBlank(message = "Workspace name is required")
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotBlank(message = "Organization ID is required")
    @Column(name = "organization_id", nullable = false)
    private String organizationId;

    @NotBlank(message = "Admin email is required")
    @Column(name = "admin_email", nullable = false)
    private String adminEmail;

    @NotBlank(message = "Admin name is required")
    @Column(name = "admin_name", nullable = false)
    private String adminName;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private WorkspaceStatus status = WorkspaceStatus.ACTIVE;

    @Column(name = "current_balance")
    private Double currentBalance = 0.0;

    @Column(name = "monthly_budget")
    private Double monthlyBudget = 0.0;

    @NotNull(message = "Budget action is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "budget_action", nullable = false)
    private BudgetAction budgetAction = BudgetAction.ALERT;

    @Column(name = "project_id")
    private String projectId;

    @Column(name = "project_name")
    private String projectName;

    @Column(name = "team_member_count")
    private Integer teamMemberCount = 0;

    @Column(name = "sessions_this_month")
    private Integer sessionsThisMonth = 0;

    @Column(name = "credits_used_this_month")
    private Double creditsUsedThisMonth = 0.0;

    @Column(name = "budget_usage_percentage")
    private Double budgetUsagePercentage = 0.0;

    @Column(name = "is_over_budget")
    private Boolean isOverBudget = false;

    @Column(name = "gifted_credits")
    private Double giftedCredits = 0.0;

    @Column(name = "purchased_credits")
    private Double purchasedCredits = 0.0;

    @Column(name = "total_credits_used")
    private Double totalCreditsUsed = 0.0;

    @Column(name = "remaining_gifted_credits")
    private Double remainingGiftedCredits = 0.0;

    @Column(name = "remaining_purchased_credits")
    private Double remainingPurchasedCredits = 0.0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    public Workspace() {
        this.id = generateWorkspaceId();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Workspace(String name, String description, String organizationId, String adminEmail, String adminName, String createdBy) {
        this();
        this.name = name;
        this.description = description;
        this.organizationId = organizationId;
        this.adminEmail = adminEmail;
        this.adminName = adminName;
        this.createdBy = createdBy;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    private String generateWorkspaceId() {
        return "ws_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    // Helper methods
    public boolean isActive() {
        return this.status == WorkspaceStatus.ACTIVE;
    }

    public boolean isSuspended() {
        return this.status == WorkspaceStatus.SUSPENDED;
    }

    public boolean isDisabled() {
        return this.status == WorkspaceStatus.DISABLED;
    }

    public void updateBudgetUsage() {
        if (monthlyBudget != null && monthlyBudget > 0) {
            this.budgetUsagePercentage = (creditsUsedThisMonth / monthlyBudget) * 100;
            this.isOverBudget = budgetUsagePercentage > 100;
        }
    }

    public void addCreditsUsed(double credits) {
        this.totalCreditsUsed = (this.totalCreditsUsed == null ? 0.0 : this.totalCreditsUsed) + credits;
        this.creditsUsedThisMonth = (this.creditsUsedThisMonth == null ? 0.0 : this.creditsUsedThisMonth) + credits;
        
        // Update remaining credits
        if (remainingGiftedCredits > 0) {
            double giftedUsed = Math.min(credits, remainingGiftedCredits);
            this.remainingGiftedCredits -= giftedUsed;
            credits -= giftedUsed;
        }
        
        if (credits > 0 && remainingPurchasedCredits > 0) {
            this.remainingPurchasedCredits -= Math.min(credits, remainingPurchasedCredits);
        }
        
        updateBudgetUsage();
    }

    public void incrementSessions() {
        this.sessionsThisMonth = (this.sessionsThisMonth == null ? 0 : this.sessionsThisMonth) + 1;
    }

    public void addGiftedCredits(double credits) {
        this.giftedCredits = (this.giftedCredits == null ? 0.0 : this.giftedCredits) + credits;
        this.remainingGiftedCredits = (this.remainingGiftedCredits == null ? 0.0 : this.remainingGiftedCredits) + credits;
        this.currentBalance = calculateCurrentBalance();
    }

    public void addPurchasedCredits(double credits) {
        this.purchasedCredits = (this.purchasedCredits == null ? 0.0 : this.purchasedCredits) + credits;
        this.remainingPurchasedCredits = (this.remainingPurchasedCredits == null ? 0.0 : this.remainingPurchasedCredits) + credits;
        this.currentBalance = calculateCurrentBalance();
    }

    private double calculateCurrentBalance() {
        double gifted = remainingGiftedCredits != null ? remainingGiftedCredits : 0.0;
        double purchased = remainingPurchasedCredits != null ? remainingPurchasedCredits : 0.0;
        return gifted + purchased;
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
        return currentBalance;
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
        return creditsUsedThisMonth;
    }

    public void setCreditsUsedThisMonth(Double creditsUsedThisMonth) {
        this.creditsUsedThisMonth = creditsUsedThisMonth;
    }

    public Double getBudgetUsagePercentage() {
        return budgetUsagePercentage;
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
        return giftedCredits;
    }

    public void setGiftedCredits(Double giftedCredits) {
        this.giftedCredits = giftedCredits;
    }

    public Double getPurchasedCredits() {
        return purchasedCredits;
    }

    public void setPurchasedCredits(Double purchasedCredits) {
        this.purchasedCredits = purchasedCredits;
    }

    public Double getTotalCreditsUsed() {
        return totalCreditsUsed;
    }

    public void setTotalCreditsUsed(Double totalCreditsUsed) {
        this.totalCreditsUsed = totalCreditsUsed;
    }

    public Double getRemainingGiftedCredits() {
        return remainingGiftedCredits;
    }

    public void setRemainingGiftedCredits(Double remainingGiftedCredits) {
        this.remainingGiftedCredits = remainingGiftedCredits;
    }

    public Double getRemainingPurchasedCredits() {
        return remainingPurchasedCredits;
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
