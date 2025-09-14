package com.berryfi.portal.dto.workspace;

import com.berryfi.portal.enums.BudgetAction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * DTO for creating a new workspace.
 */
public class CreateWorkspaceRequest {

    @NotBlank(message = "Workspace name is required")
    private String name;

    private String description;
    
    @NotBlank(message = "Admin name is required")
    private String adminName;
    
    @NotBlank(message = "Admin email is required")
    private String adminEmail;
    
    @PositiveOrZero(message = "Monthly budget must be positive or zero")
    private Double monthlyBudget;
    
    private BudgetAction budgetAction = BudgetAction.ALERT;
    
    @PositiveOrZero(message = "Gifted credits must be positive or zero")
    private Double giftedCredits;
    
    @PositiveOrZero(message = "Purchased credits must be positive or zero")
    private Double purchasedCredits;

    @NotBlank(message = "Project ID is required")
    private String projectId;

    public CreateWorkspaceRequest() {}

    public CreateWorkspaceRequest(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Getters and Setters
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

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
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

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
}
