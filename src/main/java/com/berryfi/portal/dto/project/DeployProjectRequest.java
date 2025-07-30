package com.berryfi.portal.dto.project;

/**
 * DTO for deploying a project.
 */
public class DeployProjectRequest {

    private String config;
    private String branding;
    private String links;
    private boolean forceDeploy = false;

    public DeployProjectRequest() {}

    public DeployProjectRequest(String config, String branding, String links) {
        this.config = config;
        this.branding = branding;
        this.links = links;
    }

    // Getters and Setters
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

    public boolean isForceDeploy() {
        return forceDeploy;
    }

    public void setForceDeploy(boolean forceDeploy) {
        this.forceDeploy = forceDeploy;
    }
}
