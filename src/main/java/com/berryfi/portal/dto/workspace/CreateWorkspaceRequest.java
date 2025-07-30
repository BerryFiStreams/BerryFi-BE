package com.berryfi.portal.dto.workspace;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for creating a new workspace.
 */
public class CreateWorkspaceRequest {

    @NotBlank(message = "Workspace name is required")
    private String name;

    private String description;

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
}
