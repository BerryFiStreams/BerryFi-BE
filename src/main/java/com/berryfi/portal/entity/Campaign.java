package com.berryfi.portal.entity;

import com.berryfi.portal.enums.AccessType;
import com.berryfi.portal.enums.CampaignStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing a marketing campaign in the system.
 */
@Entity
@Table(name = "campaigns", indexes = {
    @Index(name = "idx_campaign_organization", columnList = "organizationId"),
    @Index(name = "idx_campaign_workspace", columnList = "workspaceId"),
    @Index(name = "idx_campaign_project", columnList = "projectId"),
    @Index(name = "idx_campaign_status", columnList = "status"),
    @Index(name = "idx_campaign_custom_name", columnList = "customName")
})
public class Campaign {
    
    @Id
    @Column(length = 50)
    private String id;
    
    @NotBlank(message = "Name is required")
    @Column(nullable = false)
    private String name;
    
    @NotBlank(message = "Custom name is required")
    @Column(unique = true, nullable = false)
    private String customName; // URL-friendly name
    
    @NotBlank(message = "Project ID is required")
    @Column(nullable = false)
    private String projectId;
    
    @NotBlank(message = "Project name is required")
    @Column(nullable = false)
    private String projectName;
    
    @NotBlank(message = "Organization ID is required")
    @Column(nullable = false)
    private String organizationId;
    
    private String workspaceId;
    
    @NotNull(message = "Access type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccessType accessType;
    
    @NotNull(message = "Campaign status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CampaignStatus status;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private String url;
    
    // Lead capture configuration
    private Boolean requireFirstName = true;
    
    private Boolean requireLastName = true;
    
    private Boolean requireEmail = true;
    
    private Boolean requirePhone = false;
    
    private Boolean enableOTP = false;
    
    // Analytics fields
    private Integer visits = 0;
    
    private Integer leads = 0;
    
    private Integer conversions = 0;
    
    private Double conversionRate = 0.0;
    
    @NotBlank(message = "Created by is required")
    @Column(nullable = false)
    private String createdBy;
    
    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    
    // Default constructor
    public Campaign() {
        this.requireFirstName = true;
        this.requireLastName = true;
        this.requireEmail = true;
        this.requirePhone = false;
        this.enableOTP = false;
        this.visits = 0;
        this.leads = 0;
        this.conversions = 0;
        this.conversionRate = 0.0;
    }
    
    // Constructor with required fields
    public Campaign(String id, String name, String customName, String projectId, 
                   String projectName, String organizationId, AccessType accessType, 
                   CampaignStatus status, String createdBy) {
        this();
        this.id = id;
        this.name = name;
        this.customName = customName;
        this.projectId = projectId;
        this.projectName = projectName;
        this.organizationId = organizationId;
        this.accessType = accessType;
        this.status = status;
        this.createdBy = createdBy;
    }
    
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
    
    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
    
    public String getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(String workspaceId) { this.workspaceId = workspaceId; }
    
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
    
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Helper methods for analytics
    public void incrementVisits() {
        this.visits++;
        updateConversionRate();
    }
    
    public void incrementLeads() {
        this.leads++;
        updateConversionRate();
    }
    
    public void incrementConversions() {
        this.conversions++;
        updateConversionRate();
    }
    
    private void updateConversionRate() {
        if (this.visits > 0) {
            this.conversionRate = (double) this.leads / this.visits * 100;
        }
    }
    
    // Generate campaign URL
    public void generateUrl(String baseUrl) {
        this.url = String.format("%s/campaign/%s", baseUrl, this.customName);
    }
}
