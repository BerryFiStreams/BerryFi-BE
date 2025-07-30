package com.berryfi.portal.dto.project;

import com.berryfi.portal.enums.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for creating a new project.
 */
public class CreateProjectRequest {

    @NotBlank(message = "Project name is required")
    private String name;

    private String description;

    private String workspaceId;

    @NotBlank(message = "Organization ID is required")
    private String organizationId;

    @NotNull(message = "Account type is required")
    private AccountType accountType;

    private String config;
    private String branding;
    private String links;

    public CreateProjectRequest() {}

    public CreateProjectRequest(String name, String description, String organizationId, AccountType accountType) {
        this.name = name;
        this.description = description;
        this.organizationId = organizationId;
        this.accountType = accountType;
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

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public String getBranding() {
        return branding;
    }

    public void setBranding(String branding) {
        this.branding = branding;
    }

    public String getLinks() {
        return links;
    }

    public void setLinks(String links) {
        this.links = links;
    }
}
