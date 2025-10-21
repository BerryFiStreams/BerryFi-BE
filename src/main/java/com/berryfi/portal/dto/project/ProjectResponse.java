package com.berryfi.portal.dto.project;

import com.berryfi.portal.entity.Project;
import com.berryfi.portal.enums.AccountType;
import com.berryfi.portal.enums.ProjectStatus;

import java.time.LocalDateTime;

/**
 * DTO for project response.
 */
public class ProjectResponse {

    private String id;
    private String name;
    private String description;
    private ProjectStatus status;
    private String organizationId;
    private AccountType accountType;
    private Double totalCreditsUsed;
    private Integer sessionsCount;
    private LocalDateTime lastDeployed;
    private Integer currentCCU;
    private Double uptime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String config;
    private String branding;
    private String links;
    private String errors;
    
    // Tenant configuration
    private ProjectTenantConfigDTO tenantConfig;
    
    // Sharing information
    private String accessType; // "OWNED" or "SHARED"
    private String sharedBy; // Organization name that shared this project (if shared)
    
    // Credit information
    private Double allocatedCredits;
    private Double remainingCredits;

    public ProjectResponse() {}

    public ProjectResponse(Project project) {
        this.id = project.getId();
        this.name = project.getName();
        this.description = project.getDescription();
        this.status = project.getStatus();
        this.organizationId = project.getOrganizationId();
        this.accountType = project.getAccountType();
        this.totalCreditsUsed = project.getTotalCreditsUsed();
        this.sessionsCount = project.getSessionsCount();
        this.lastDeployed = project.getLastDeployed();
        this.currentCCU = project.getCurrentCCU();
        this.uptime = project.getUptime();
        this.createdAt = project.getCreatedAt();
        this.updatedAt = project.getUpdatedAt();
        this.createdBy = project.getCreatedBy();
        this.config = project.getConfig();
        this.branding = project.getBranding();
        this.links = project.getLinks();
        this.errors = project.getErrors();
        this.allocatedCredits = project.getAllocatedCredits();
        this.remainingCredits = project.getRemainingCredits();
        
        // Build tenant configuration from project fields
        if (project.getSubdomain() != null || project.getBrandAppName() != null || project.getCustomDomain() != null) {
            ProjectTenantConfigDTO tenantDto = new ProjectTenantConfigDTO();
            tenantDto.setSubdomain(project.getSubdomain());
            
            // Set branding if any branding fields exist
            if (project.getBrandAppName() != null || project.getBrandPrimaryColor() != null) {
                ProjectTenantConfigDTO.TenantBranding branding = new ProjectTenantConfigDTO.TenantBranding();
                branding.setAppName(project.getBrandAppName());
                branding.setPrimaryColor(project.getBrandPrimaryColor());
                branding.setSecondaryColor(project.getBrandSecondaryColor());
                branding.setLogoUrl(project.getBrandLogoUrl());
                branding.setFaviconUrl(project.getBrandFaviconUrl());
                tenantDto.setBranding(branding);
            }
            
            // Set custom domain if it exists
            if (project.getCustomDomain() != null) {
                ProjectTenantConfigDTO.CustomDomainConfig customDomain = new ProjectTenantConfigDTO.CustomDomainConfig();
                customDomain.setDomain(project.getCustomDomain());
                customDomain.setVerified(project.getCustomDomainVerified());
                tenantDto.setCustomDomain(customDomain);
            }
            
            this.tenantConfig = tenantDto;
        }
        
        // accessType and sharedBy will be set by service layer based on context
    }

    // Static factory method
    public static ProjectResponse from(Project project) {
        return new ProjectResponse(project);
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

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
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

    public Double getTotalCreditsUsed() {
        return totalCreditsUsed;
    }

    public void setTotalCreditsUsed(Double totalCreditsUsed) {
        this.totalCreditsUsed = totalCreditsUsed;
    }

    public Integer getSessionsCount() {
        return sessionsCount;
    }

    public void setSessionsCount(Integer sessionsCount) {
        this.sessionsCount = sessionsCount;
    }

    public LocalDateTime getLastDeployed() {
        return lastDeployed;
    }

    public void setLastDeployed(LocalDateTime lastDeployed) {
        this.lastDeployed = lastDeployed;
    }

    public Integer getCurrentCCU() {
        return currentCCU;
    }

    public void setCurrentCCU(Integer currentCCU) {
        this.currentCCU = currentCCU;
    }

    public Double getUptime() {
        return uptime;
    }

    public void setUptime(Double uptime) {
        this.uptime = uptime;
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

    public String getErrors() {
        return errors;
    }

    public void setErrors(String errors) {
        this.errors = errors;
    }

    public String getAccessType() {
        return accessType;
    }

    public void setAccessType(String accessType) {
        this.accessType = accessType;
    }

    public String getSharedBy() {
        return sharedBy;
    }

    public void setSharedBy(String sharedBy) {
        this.sharedBy = sharedBy;
    }

    public Double getAllocatedCredits() {
        return allocatedCredits;
    }

    public void setAllocatedCredits(Double allocatedCredits) {
        this.allocatedCredits = allocatedCredits;
    }

    public Double getRemainingCredits() {
        return remainingCredits;
    }

    public void setRemainingCredits(Double remainingCredits) {
        this.remainingCredits = remainingCredits;
    }

    public ProjectTenantConfigDTO getTenantConfig() {
        return tenantConfig;
    }

    public void setTenantConfig(ProjectTenantConfigDTO tenantConfig) {
        this.tenantConfig = tenantConfig;
    }
}
