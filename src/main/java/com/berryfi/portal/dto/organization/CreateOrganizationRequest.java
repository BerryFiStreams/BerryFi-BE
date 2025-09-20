package com.berryfi.portal.dto.organization;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new organization.
 */
public class CreateOrganizationRequest {
    
    @NotBlank(message = "Organization name is required")
    @Size(min = 2, max = 100, message = "Organization name must be between 2 and 100 characters")
    private String name;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    private Integer maxProjects;
    private Integer maxMembers;
    private Double monthlyBudget;
    private Boolean canShareProjects;
    private Boolean canReceiveSharedProjects;

    // Constructors
    public CreateOrganizationRequest() {}

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

    public Integer getMaxProjects() {
        return maxProjects;
    }

    public void setMaxProjects(Integer maxProjects) {
        this.maxProjects = maxProjects;
    }

    public Integer getMaxMembers() {
        return maxMembers;
    }

    public void setMaxMembers(Integer maxMembers) {
        this.maxMembers = maxMembers;
    }

    public Double getMonthlyBudget() {
        return monthlyBudget;
    }

    public void setMonthlyBudget(Double monthlyBudget) {
        this.monthlyBudget = monthlyBudget;
    }

    public Boolean getCanShareProjects() {
        return canShareProjects;
    }

    public void setCanShareProjects(Boolean canShareProjects) {
        this.canShareProjects = canShareProjects;
    }

    public Boolean getCanReceiveSharedProjects() {
        return canReceiveSharedProjects;
    }

    public void setCanReceiveSharedProjects(Boolean canReceiveSharedProjects) {
        this.canReceiveSharedProjects = canReceiveSharedProjects;
    }
}