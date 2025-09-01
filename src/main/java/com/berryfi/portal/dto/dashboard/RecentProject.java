package com.berryfi.portal.dto.dashboard;

import com.berryfi.portal.enums.ProjectStatus;
import java.time.LocalDateTime;

/**
 * DTO for recent project details in dashboard.
 */
public class RecentProject {
    
    private String id;
    private String name;
    private String description;
    private String workspaceId;
    private String workspaceName;
    private Double totalCreditsUsed;
    private Integer sessionsCount;
    private ProjectStatus status;
    private LocalDateTime lastDeployed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public RecentProject() {}
    
    public RecentProject(String id, String name, String description, String workspaceId, 
                        String workspaceName, Double totalCreditsUsed, Integer sessionsCount, 
                        ProjectStatus status, LocalDateTime lastDeployed, LocalDateTime createdAt, 
                        LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.workspaceId = workspaceId;
        this.workspaceName = workspaceName;
        this.totalCreditsUsed = totalCreditsUsed;
        this.sessionsCount = sessionsCount;
        this.status = status;
        this.lastDeployed = lastDeployed;
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
    
    public String getWorkspaceId() {
        return workspaceId;
    }
    
    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }
    
    public String getWorkspaceName() {
        return workspaceName;
    }
    
    public void setWorkspaceName(String workspaceName) {
        this.workspaceName = workspaceName;
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
    
    public ProjectStatus getStatus() {
        return status;
    }
    
    public void setStatus(ProjectStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getLastDeployed() {
        return lastDeployed;
    }
    
    public void setLastDeployed(LocalDateTime lastDeployed) {
        this.lastDeployed = lastDeployed;
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
