package com.berryfi.portal.dto.team;

import com.berryfi.portal.enums.AccessType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for creating a new campaign.
 */
public class CreateCampaignRequest {
    
    @NotBlank(message = "Name is required")
    private String name;
    
    private String customName;
    
    @NotBlank(message = "Project ID is required")
    private String projectId;
    
    @NotBlank(message = "Workspace ID is required")
    private String workspaceId;
    
    @NotNull(message = "Access type is required")
    private AccessType accessType;
    
    private String description;
    
    // Lead capture configuration
    private Boolean requireFirstName;
    private Boolean requireLastName;
    private Boolean requireEmail;
    private Boolean requirePhone;
    private Boolean enableOTP;
    
    // Default constructor
    public CreateCampaignRequest() {}
    
    // Constructor
    public CreateCampaignRequest(String name, String projectId, AccessType accessType) {
        this.name = name;
        this.projectId = projectId;
        this.accessType = accessType;
    }
    
    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getCustomName() { return customName; }
    public void setCustomName(String customName) { this.customName = customName; }
    
    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }
    
    public String getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(String workspaceId) { this.workspaceId = workspaceId; }
    
    public AccessType getAccessType() { return accessType; }
    public void setAccessType(AccessType accessType) { this.accessType = accessType; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Boolean getRequireFirstName() { return requireFirstName; }
    public void setRequireFirstName(Boolean requireFirstName) { this.requireFirstName = requireFirstName; }
    
    public Boolean getRequireLastName() { return requireLastName; }
    public void setRequireLastName(Boolean requireLastName) { this.requireLastName = requireLastName; }
    
    public Boolean getRequireEmail() { return requireEmail; }
    public void setRequireEmail(Boolean requireEmail) { this.requireEmail = requireEmail; }
    
    public Boolean getRequirePhone() { return requirePhone; }
    public void setRequirePhone(Boolean requirePhone) { this.requirePhone = requirePhone; }
    
    public Boolean getEnableOTP() { return enableOTP; }
    public void setEnableOTP(Boolean enableOTP) { this.enableOTP = enableOTP; }
}
