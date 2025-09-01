package com.berryfi.portal.dto.dashboard;

import com.berryfi.portal.enums.WorkspaceStatus;
import java.time.LocalDateTime;

/**
 * DTO for recent workspace details in dashboard.
 */
public class RecentWorkspace {
    
    private String id;
    private String name;
    private String description;
    private Double creditsAvailable;
    private String projectName;
    private Integer membersCount;
    private WorkspaceStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public RecentWorkspace() {}
    
    public RecentWorkspace(String id, String name, String description, Double creditsAvailable, 
                          String projectName, Integer membersCount, WorkspaceStatus status, 
                          LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.creditsAvailable = creditsAvailable;
        this.projectName = projectName;
        this.membersCount = membersCount;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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
    
    public Double getCreditsAvailable() {
        return creditsAvailable;
    }
    
    public void setCreditsAvailable(Double creditsAvailable) {
        this.creditsAvailable = creditsAvailable;
    }
    
    public String getProjectName() {
        return projectName;
    }
    
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
    
    public Integer getMembersCount() {
        return membersCount;
    }
    
    public void setMembersCount(Integer membersCount) {
        this.membersCount = membersCount;
    }
    
    public WorkspaceStatus getStatus() {
        return status;
    }
    
    public void setStatus(WorkspaceStatus status) {
        this.status = status;
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
}
