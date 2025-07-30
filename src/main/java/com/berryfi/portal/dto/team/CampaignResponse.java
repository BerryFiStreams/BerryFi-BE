package com.berryfi.portal.dto.team;

import com.berryfi.portal.enums.AccessType;
import com.berryfi.portal.enums.CampaignStatus;

import java.time.LocalDateTime;

/**
 * Response DTO for campaign data.
 */
public class CampaignResponse {
    
    private String id;
    private String name;
    private String customName;
    private String projectId;
    private String projectName;
    private AccessType accessType;
    private CampaignStatus status;
    private String description;
    private String url;
    
    // Lead capture settings
    private Boolean requireFirstName;
    private Boolean requireLastName;
    private Boolean requireEmail;
    private Boolean requirePhone;
    private Boolean enableOTP;
    
    // Analytics
    private Integer visits;
    private Integer leads;
    private Integer conversions;
    private Double conversionRate;
    
    private String organizationId;
    private String workspaceId;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Default constructor
    public CampaignResponse() {}
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getCustomName() { return customName; }
    public void setCustomName(String customName) { this.customName = customName; }
    
    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }
    
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    
    public AccessType getAccessType() { return accessType; }
    public void setAccessType(AccessType accessType) { this.accessType = accessType; }
    
    public CampaignStatus getStatus() { return status; }
    public void setStatus(CampaignStatus status) { this.status = status; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    
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
    
    public Integer getVisits() { return visits; }
    public void setVisits(Integer visits) { this.visits = visits; }
    
    public Integer getLeads() { return leads; }
    public void setLeads(Integer leads) { this.leads = leads; }
    
    public Integer getConversions() { return conversions; }
    public void setConversions(Integer conversions) { this.conversions = conversions; }
    
    public Double getConversionRate() { return conversionRate; }
    public void setConversionRate(Double conversionRate) { this.conversionRate = conversionRate; }
    
    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
    
    public String getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(String workspaceId) { this.workspaceId = workspaceId; }
    
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
