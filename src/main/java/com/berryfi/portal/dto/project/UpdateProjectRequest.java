package com.berryfi.portal.dto.project;

/**
 * DTO for updating project details.
 */
public class UpdateProjectRequest {

    private String name;
    private String description;
    private String config;
    private String branding;
    private String links;

    public UpdateProjectRequest() {}

    public UpdateProjectRequest(String name, String description) {
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
