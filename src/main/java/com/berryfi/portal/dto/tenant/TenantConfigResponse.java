package com.berryfi.portal.dto.tenant;

/**
 * Response DTO for tenant configuration and branding information.
 * This is sent to the frontend to customize the UI based on tenant.
 */
public class TenantConfigResponse {

    private String projectId;
    private String projectName;
    private String subdomain;
    private String customDomain;
    private String organizationId;
    
    // Branding
    private String brandLogoUrl;
    private String brandPrimaryColor;
    private String brandSecondaryColor;
    private String brandFaviconUrl;
    private String brandAppName;
    private String brandYoutubeUrl;

    // Constructors
    public TenantConfigResponse() {
    }

    public TenantConfigResponse(String projectId, String projectName, String subdomain) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.subdomain = subdomain;
    }

    // Getters and Setters
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

    public String getSubdomain() {
        return subdomain;
    }

    public void setSubdomain(String subdomain) {
        this.subdomain = subdomain;
    }

    public String getCustomDomain() {
        return customDomain;
    }

    public void setCustomDomain(String customDomain) {
        this.customDomain = customDomain;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getBrandLogoUrl() {
        return brandLogoUrl;
    }

    public void setBrandLogoUrl(String brandLogoUrl) {
        this.brandLogoUrl = brandLogoUrl;
    }

    public String getBrandPrimaryColor() {
        return brandPrimaryColor;
    }

    public void setBrandPrimaryColor(String brandPrimaryColor) {
        this.brandPrimaryColor = brandPrimaryColor;
    }

    public String getBrandSecondaryColor() {
        return brandSecondaryColor;
    }

    public void setBrandSecondaryColor(String brandSecondaryColor) {
        this.brandSecondaryColor = brandSecondaryColor;
    }

    public String getBrandFaviconUrl() {
        return brandFaviconUrl;
    }

    public void setBrandFaviconUrl(String brandFaviconUrl) {
        this.brandFaviconUrl = brandFaviconUrl;
    }

    public String getBrandAppName() {
        return brandAppName;
    }

    public void setBrandAppName(String brandAppName) {
        this.brandAppName = brandAppName;
    }

    public String getBrandYoutubeUrl() {
        return brandYoutubeUrl;
    }

    public void setBrandYoutubeUrl(String brandYoutubeUrl) {
        this.brandYoutubeUrl = brandYoutubeUrl;
    }
}
