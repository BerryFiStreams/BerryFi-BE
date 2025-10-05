package com.berryfi.portal.entity;

import com.berryfi.portal.enums.VmStatus;
import com.berryfi.portal.enums.VmType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a VM instance in the system.
 * VMs are assigned to projects.
 */
@Entity
@Table(name = "vm_instances", indexes = {
    @Index(name = "idx_vm_project", columnList = "projectId"),
    @Index(name = "idx_vm_status", columnList = "status"),
    @Index(name = "idx_vm_type", columnList = "vmType"),
    @Index(name = "idx_vm_azure_resource", columnList = "azureResourceId"),
    @Index(name = "idx_vm_current_project", columnList = "currentProjectId")
})
public class VmInstance {
    
    @Id
    @Column(name = "id", length = 50)
    private String id;
    
    @NotBlank(message = "VM name is required")
    @Column(name = "vm_name", nullable = false)
    private String vmName;
    
    @NotNull(message = "VM type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "vm_type", nullable = false)
    private VmType vmType; // T4 or A10 only
    
    @NotBlank(message = "Azure resource ID is required")
    @Column(name = "azure_resource_id", nullable = false)
    private String azureResourceId;
    
    @NotBlank(message = "Azure resource group is required")
    @Column(name = "azure_resource_group", nullable = false)
    private String azureResourceGroup;
    
    @NotBlank(message = "Azure subscription ID is required")
    @Column(name = "azure_subscription_id", nullable = false)
    private String azureSubscriptionId;
    
    @Column(name = "azure_region")
    private String azureRegion;
    
    @NotBlank(message = "Azure tenant ID is required")
    @Column(name = "azure_tenant_id", nullable = false)
    private String azureTenantId;
    
    @NotBlank(message = "Azure client ID is required")
    @Column(name = "azure_client_id", nullable = false)
    private String azureClientId;
    
    @NotBlank(message = "Azure client secret is required")
    @Column(name = "azure_client_secret", nullable = false)
    private String azureClientSecret;
    
    @NotNull(message = "VM status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private VmStatus status = VmStatus.AVAILABLE;
    
    @NotBlank(message = "Project ID is required")
    @Column(name = "project_id", nullable = false)
    private String projectId;
    
    @Column(name = "current_project_id")
    private String currentProjectId; // Which project is currently using this VM
    
    @Column(name = "current_session_id")
    private String currentSessionId; // Which session is currently using this VM
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "port")
    private Integer port;
    
    @Column(name = "connection_url")
    private String connectionUrl;
    
    @Column(name = "last_started")
    private LocalDateTime lastStarted;
    
    @Column(name = "last_stopped")
    private LocalDateTime lastStopped;
    
    @Column(name = "total_runtime_minutes")
    private Double totalRuntimeMinutes = 0.0;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "last_sync_time")
    private LocalDateTime lastSyncTime;
    
    @Column(name = "created_by", nullable = false)
    private String createdBy;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Constructors
    public VmInstance() {
        this.id = generateVmId();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public VmInstance(String vmName, VmType vmType, String azureResourceId, 
                     String azureResourceGroup, String azureSubscriptionId,
                     String azureTenantId, String azureClientId, String azureClientSecret,
                     String projectId, String createdBy) {
        this();
        this.vmName = vmName;
        this.vmType = vmType;
        this.azureResourceId = azureResourceId;
        this.azureResourceGroup = azureResourceGroup;
        this.azureSubscriptionId = azureSubscriptionId;
        this.azureTenantId = azureTenantId;
        this.azureClientId = azureClientId;
        this.azureClientSecret = azureClientSecret;
        this.projectId = projectId;
        this.createdBy = createdBy;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    private String generateVmId() {
        return "vm_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    // Helper methods
    public boolean isAvailable() {
        return this.status == VmStatus.AVAILABLE;
    }

    public boolean isRunning() {
        return this.status == VmStatus.RUNNING;
    }

    public boolean canBeAssigned() {
        // VM can only be assigned if it's available/stopped AND not currently assigned to any session
        return (this.status == VmStatus.AVAILABLE || this.status == VmStatus.STOPPED) 
                && this.currentSessionId == null;
    }

    public boolean isInUse() {
        return this.currentSessionId != null || 
               (this.status != VmStatus.AVAILABLE && this.status != VmStatus.STOPPED);
    }

    public void assignToSession(String projectId, String sessionId) {
        this.currentProjectId = projectId;
        this.currentSessionId = sessionId;
        this.status = VmStatus.ASSIGNED;
    }

    public void releaseFromSession() {
        this.currentProjectId = null;
        this.currentSessionId = null;
        this.status = VmStatus.AVAILABLE;
    }

    public void markAsStarting() {
        this.status = VmStatus.STARTING;
        this.lastStarted = LocalDateTime.now();
    }

    public void markAsRunning(String ipAddress, Integer port) {
        this.status = VmStatus.RUNNING;
        this.ipAddress = ipAddress;
        this.port = port;
        if (ipAddress != null && port != null) {
            this.connectionUrl = String.format("http://%s:%d", ipAddress, port);
        }
    }

    public void markAsStopping() {
        this.status = VmStatus.STOPPING;
    }

    public void markAsStopped() {
        this.status = VmStatus.STOPPED;
        this.lastStopped = LocalDateTime.now();
        this.ipAddress = null;
        this.port = null;
        this.connectionUrl = null;
        
        // Calculate runtime if we have start and stop times
        if (this.lastStarted != null && this.lastStopped != null) {
            long runtimeMinutes = java.time.Duration.between(this.lastStarted, this.lastStopped).toMinutes();
            this.totalRuntimeMinutes = (this.totalRuntimeMinutes != null ? this.totalRuntimeMinutes : 0.0) + runtimeMinutes;
        }
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVmName() {
        return vmName;
    }

    public void setVmName(String vmName) {
        this.vmName = vmName;
    }

    public VmType getVmType() {
        return vmType;
    }

    public void setVmType(VmType vmType) {
        this.vmType = vmType;
    }

    public String getAzureResourceId() {
        return azureResourceId;
    }

    public void setAzureResourceId(String azureResourceId) {
        this.azureResourceId = azureResourceId;
    }

    public String getAzureResourceGroup() {
        return azureResourceGroup;
    }

    public void setAzureResourceGroup(String azureResourceGroup) {
        this.azureResourceGroup = azureResourceGroup;
    }

    public String getAzureSubscriptionId() {
        return azureSubscriptionId;
    }

    public void setAzureSubscriptionId(String azureSubscriptionId) {
        this.azureSubscriptionId = azureSubscriptionId;
    }

    public String getAzureRegion() {
        return azureRegion;
    }

    public void setAzureRegion(String azureRegion) {
        this.azureRegion = azureRegion;
    }

    public String getAzureTenantId() {
        return azureTenantId;
    }

    public void setAzureTenantId(String azureTenantId) {
        this.azureTenantId = azureTenantId;
    }

    public String getAzureClientId() {
        return azureClientId;
    }

    public void setAzureClientId(String azureClientId) {
        this.azureClientId = azureClientId;
    }

    public String getAzureClientSecret() {
        return azureClientSecret;
    }

    public void setAzureClientSecret(String azureClientSecret) {
        this.azureClientSecret = azureClientSecret;
    }

    public VmStatus getStatus() {
        return status;
    }

    public void setStatus(VmStatus status) {
        this.status = status;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getCurrentProjectId() {
        return currentProjectId;
    }

    public void setCurrentProjectId(String currentProjectId) {
        this.currentProjectId = currentProjectId;
    }

    public String getCurrentSessionId() {
        return currentSessionId;
    }

    public void setCurrentSessionId(String currentSessionId) {
        this.currentSessionId = currentSessionId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getConnectionUrl() {
        return connectionUrl;
    }

    public void setConnectionUrl(String connectionUrl) {
        this.connectionUrl = connectionUrl;
    }

    public LocalDateTime getLastStarted() {
        return lastStarted;
    }

    public void setLastStarted(LocalDateTime lastStarted) {
        this.lastStarted = lastStarted;
    }

    public LocalDateTime getLastStopped() {
        return lastStopped;
    }

    public void setLastStopped(LocalDateTime lastStopped) {
        this.lastStopped = lastStopped;
    }

    public Double getTotalRuntimeMinutes() {
        return totalRuntimeMinutes;
    }

    public void setTotalRuntimeMinutes(Double totalRuntimeMinutes) {
        this.totalRuntimeMinutes = totalRuntimeMinutes;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getLastSyncTime() {
        return lastSyncTime;
    }

    public void setLastSyncTime(LocalDateTime lastSyncTime) {
        this.lastSyncTime = lastSyncTime;
    }
}
