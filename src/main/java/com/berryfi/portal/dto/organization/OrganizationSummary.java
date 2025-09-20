package com.berryfi.portal.dto.organization;

import com.berryfi.portal.enums.OrganizationStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * Summary DTO for organization details used in lists and searches.
 */
public class OrganizationSummary {
    
    private String id;
    private String name;
    private String description;
    private String ownerName;
    private OrganizationStatus status;
    private Integer activeProjects;
    private Integer totalMembers;
    private Boolean canShareProjects;
    private Boolean canReceiveSharedProjects;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    // Constructors
    public OrganizationSummary() {}

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

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public OrganizationStatus getStatus() {
        return status;
    }

    public void setStatus(OrganizationStatus status) {
        this.status = status;
    }

    public Integer getActiveProjects() {
        return activeProjects;
    }

    public void setActiveProjects(Integer activeProjects) {
        this.activeProjects = activeProjects;
    }

    public Integer getTotalMembers() {
        return totalMembers;
    }

    public void setTotalMembers(Integer totalMembers) {
        this.totalMembers = totalMembers;
    }

    public Boolean getCanShareProjects() {
        return canShareProjects;
    }

    public void setCanShareProjects(Boolean canShareProjects) {
        this.canShareProjects = canShareProjects;
    }

    public Boolean getCanReceiveSharedProjects() {
        return canReceiveSharedProjects;
    }

    public void setCanReceiveSharedProjects(Boolean canReceiveSharedProjects) {
        this.canReceiveSharedProjects = canReceiveSharedProjects;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}