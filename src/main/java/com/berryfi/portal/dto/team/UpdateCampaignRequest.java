package com.berryfi.portal.dto.team;

import com.berryfi.portal.enums.AccessType;
import com.berryfi.portal.enums.CampaignStatus;

import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating campaign data.
 */
public class UpdateCampaignRequest {
    
    @Size(max = 100, message = "Campaign name cannot exceed 100 characters")
    private String name;
    
    private String customName;
    
    private AccessType accessType;
    
    private CampaignStatus status;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    // Lead capture settings
    private Boolean requireFirstName;
    private Boolean requireLastName;
    private Boolean requireEmail;
    private Boolean requirePhone;
    private Boolean enableOTP;
    
    // Default constructor
    public UpdateCampaignRequest() {}
    
    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getCustomName() { return customName; }
    public void setCustomName(String customName) { this.customName = customName; }
    
    public AccessType getAccessType() { return accessType; }
    public void setAccessType(AccessType accessType) { this.accessType = accessType; }
    
    public CampaignStatus getStatus() { return status; }
    public void setStatus(CampaignStatus status) { this.status = status; }
    
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
