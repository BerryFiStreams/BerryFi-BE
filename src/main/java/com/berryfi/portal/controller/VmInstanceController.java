package com.berryfi.portal.controller;

import com.berryfi.portal.dto.ApiResponse;
import com.berryfi.portal.entity.VmInstance;
import com.berryfi.portal.repository.VmInstanceRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for VM instance management.
 * Allows administrators to create, update, and manage VM instances with their Azure credentials.
 */
@RestController
@RequestMapping("/api/admin/vm-instances")
@CrossOrigin(origins = "*")
public class VmInstanceController {

    private static final Logger logger = LoggerFactory.getLogger(VmInstanceController.class);

    @Autowired
    private VmInstanceRepository vmInstanceRepository;

    /**
     * Create a new VM instance with Azure credentials
     */
    @PostMapping
    public ResponseEntity<ApiResponse<VmInstance>> createVmInstance(
            @RequestBody @Valid CreateVmInstanceRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            // TODO: Add admin authorization check
            
            VmInstance vmInstance = new VmInstance(
                request.getVmName(),
                request.getVmType(),
                request.getAzureResourceId(),
                request.getAzureResourceGroup(),
                request.getAzureSubscriptionId(),
                request.getAzureTenantId(),
                request.getAzureClientId(),
                request.getAzureClientSecret(),
                request.getProjectId(),
                userDetails.getUsername()
            );
            
            if (request.getAzureRegion() != null) {
                vmInstance.setAzureRegion(request.getAzureRegion());
            }
            
            if (request.getDescription() != null) {
                vmInstance.setDescription(request.getDescription());
            }

            vmInstance = vmInstanceRepository.save(vmInstance);
            
            logger.info("Created VM instance: {} for project: {}", vmInstance.getId(), vmInstance.getProjectId());
            
            return ResponseEntity.ok(ApiResponse.success("VM instance created successfully", vmInstance));

        } catch (Exception e) {
            logger.error("Failed to create VM instance: " + e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to create VM instance: " + e.getMessage()));
        }
    }

    /**
     * Get VM instances by project
     */
    @GetMapping("/project/{projectId}")
    public ResponseEntity<ApiResponse<Page<VmInstance>>> getVmInstancesByProject(
            @PathVariable String projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            // TODO: Add authorization check - user should have access to project
            
            Pageable pageable = PageRequest.of(page, size);
            Page<VmInstance> vmInstances = vmInstanceRepository.findByProjectId(projectId, pageable);
            
            return ResponseEntity.ok(ApiResponse.success("VM instances retrieved successfully", vmInstances));

        } catch (Exception e) {
            logger.error("Failed to get VM instances for project {}: {}", projectId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to get VM instances: " + e.getMessage()));
        }
    }

    /**
     * Get available VM instances by project and type
     */
    @GetMapping("/project/{projectId}/available")
    public ResponseEntity<ApiResponse<List<VmInstance>>> getAvailableVmInstances(
            @PathVariable String projectId,
            @RequestParam(required = false) String vmType,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            // TODO: Add authorization check
            
            List<VmInstance> vmInstances;
            if (vmType != null) {
                vmInstances = vmInstanceRepository.findAvailableVmsByTypeForProject(projectId, vmType);
            } else {
                vmInstances = vmInstanceRepository.findAvailableVmsForProject(projectId);
            }
            
            return ResponseEntity.ok(ApiResponse.success("Available VM instances retrieved", vmInstances));

        } catch (Exception e) {
            logger.error("Failed to get available VM instances for project {}: {}", projectId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to get available VM instances: " + e.getMessage()));
        }
    }

    /**
     * Update VM instance Azure credentials
     */
    @PutMapping("/{vmInstanceId}/credentials")
    public ResponseEntity<ApiResponse<VmInstance>> updateVmCredentials(
            @PathVariable String vmInstanceId,
            @RequestBody @Valid UpdateVmCredentialsRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            // TODO: Add admin authorization check
            
            Optional<VmInstance> vmInstanceOpt = vmInstanceRepository.findById(vmInstanceId);
            if (vmInstanceOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            VmInstance vmInstance = vmInstanceOpt.get();
            
            // Update Azure credentials
            if (request.getAzureSubscriptionId() != null) {
                vmInstance.setAzureSubscriptionId(request.getAzureSubscriptionId());
            }
            if (request.getAzureTenantId() != null) {
                vmInstance.setAzureTenantId(request.getAzureTenantId());
            }
            if (request.getAzureClientId() != null) {
                vmInstance.setAzureClientId(request.getAzureClientId());
            }
            if (request.getAzureClientSecret() != null) {
                vmInstance.setAzureClientSecret(request.getAzureClientSecret());
            }
            if (request.getAzureResourceGroup() != null) {
                vmInstance.setAzureResourceGroup(request.getAzureResourceGroup());
            }

            vmInstance = vmInstanceRepository.save(vmInstance);
            
            logger.info("Updated credentials for VM instance: {}", vmInstanceId);
            
            return ResponseEntity.ok(ApiResponse.success("VM credentials updated successfully", vmInstance));

        } catch (Exception e) {
            logger.error("Failed to update VM credentials for {}: {}", vmInstanceId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to update VM credentials: " + e.getMessage()));
        }
    }

    /**
     * Get VM instance details
     */
    @GetMapping("/{vmInstanceId}")
    public ResponseEntity<ApiResponse<VmInstance>> getVmInstance(
            @PathVariable String vmInstanceId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            // TODO: Add authorization check
            
            Optional<VmInstance> vmInstanceOpt = vmInstanceRepository.findById(vmInstanceId);
            if (vmInstanceOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            VmInstance vmInstance = vmInstanceOpt.get();
            
            // Mask sensitive information
            VmInstance maskedInstance = new VmInstance();
            maskedInstance.setId(vmInstance.getId());
            maskedInstance.setVmName(vmInstance.getVmName());
            maskedInstance.setVmType(vmInstance.getVmType());
            maskedInstance.setAzureResourceId(vmInstance.getAzureResourceId());
            maskedInstance.setAzureResourceGroup(vmInstance.getAzureResourceGroup());
            maskedInstance.setAzureSubscriptionId(vmInstance.getAzureSubscriptionId());
            maskedInstance.setAzureTenantId(vmInstance.getAzureTenantId());
            maskedInstance.setAzureClientId(vmInstance.getAzureClientId());
            maskedInstance.setAzureClientSecret("***"); // Mask secret
            maskedInstance.setStatus(vmInstance.getStatus());
            maskedInstance.setProjectId(vmInstance.getProjectId());
            maskedInstance.setCreatedAt(vmInstance.getCreatedAt());
            maskedInstance.setDescription(vmInstance.getDescription());
            
            return ResponseEntity.ok(ApiResponse.success("VM instance retrieved successfully", maskedInstance));

        } catch (Exception e) {
            logger.error("Failed to get VM instance {}: {}", vmInstanceId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to get VM instance: " + e.getMessage()));
        }
    }

    /**
     * Delete VM instance
     */
    @DeleteMapping("/{vmInstanceId}")
    public ResponseEntity<ApiResponse<String>> deleteVmInstance(
            @PathVariable String vmInstanceId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            // TODO: Add admin authorization check
            
            Optional<VmInstance> vmInstanceOpt = vmInstanceRepository.findById(vmInstanceId);
            if (vmInstanceOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            VmInstance vmInstance = vmInstanceOpt.get();
            
            // Check if VM is currently in use
            if (vmInstance.getCurrentSessionId() != null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Cannot delete VM instance - currently in use by session: " + vmInstance.getCurrentSessionId()));
            }

            vmInstanceRepository.delete(vmInstance);
            
            logger.info("Deleted VM instance: {} from project: {}", vmInstanceId, vmInstance.getProjectId());
            
            return ResponseEntity.ok(ApiResponse.success("VM instance deleted successfully", null));

        } catch (Exception e) {
            logger.error("Failed to delete VM instance {}: {}", vmInstanceId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to delete VM instance: " + e.getMessage()));
        }
    }

    // Request DTOs
    public static class CreateVmInstanceRequest {
        @NotBlank(message = "VM name is required")
        private String vmName;
        
        @NotBlank(message = "VM type is required")
        private String vmType;
        
        @NotBlank(message = "Azure resource ID is required")
        private String azureResourceId;
        
        @NotBlank(message = "Azure resource group is required")
        private String azureResourceGroup;
        
        @NotBlank(message = "Azure subscription ID is required")
        private String azureSubscriptionId;
        
        @NotBlank(message = "Azure tenant ID is required")
        private String azureTenantId;
        
        @NotBlank(message = "Azure client ID is required")
        private String azureClientId;
        
        @NotBlank(message = "Azure client secret is required")
        private String azureClientSecret;
        
        @NotBlank(message = "Project ID is required")
        private String projectId;
        
        private String azureRegion;
        private String description;

        // Getters and setters
        public String getVmName() { return vmName; }
        public void setVmName(String vmName) { this.vmName = vmName; }
        
        public String getVmType() { return vmType; }
        public void setVmType(String vmType) { this.vmType = vmType; }
        
        public String getAzureResourceId() { return azureResourceId; }
        public void setAzureResourceId(String azureResourceId) { this.azureResourceId = azureResourceId; }
        
        public String getAzureResourceGroup() { return azureResourceGroup; }
        public void setAzureResourceGroup(String azureResourceGroup) { this.azureResourceGroup = azureResourceGroup; }
        
        public String getAzureSubscriptionId() { return azureSubscriptionId; }
        public void setAzureSubscriptionId(String azureSubscriptionId) { this.azureSubscriptionId = azureSubscriptionId; }
        
        public String getAzureTenantId() { return azureTenantId; }
        public void setAzureTenantId(String azureTenantId) { this.azureTenantId = azureTenantId; }
        
        public String getAzureClientId() { return azureClientId; }
        public void setAzureClientId(String azureClientId) { this.azureClientId = azureClientId; }
        
        public String getAzureClientSecret() { return azureClientSecret; }
        public void setAzureClientSecret(String azureClientSecret) { this.azureClientSecret = azureClientSecret; }
        
        public String getProjectId() { return projectId; }
        public void setProjectId(String projectId) { this.projectId = projectId; }
        
        public String getAzureRegion() { return azureRegion; }
        public void setAzureRegion(String azureRegion) { this.azureRegion = azureRegion; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class UpdateVmCredentialsRequest {
        private String azureSubscriptionId;
        private String azureTenantId;
        private String azureClientId;
        private String azureClientSecret;
        private String azureResourceGroup;

        // Getters and setters
        public String getAzureSubscriptionId() { return azureSubscriptionId; }
        public void setAzureSubscriptionId(String azureSubscriptionId) { this.azureSubscriptionId = azureSubscriptionId; }
        
        public String getAzureTenantId() { return azureTenantId; }
        public void setAzureTenantId(String azureTenantId) { this.azureTenantId = azureTenantId; }
        
        public String getAzureClientId() { return azureClientId; }
        public void setAzureClientId(String azureClientId) { this.azureClientId = azureClientId; }
        
        public String getAzureClientSecret() { return azureClientSecret; }
        public void setAzureClientSecret(String azureClientSecret) { this.azureClientSecret = azureClientSecret; }
        
        public String getAzureResourceGroup() { return azureResourceGroup; }
        public void setAzureResourceGroup(String azureResourceGroup) { this.azureResourceGroup = azureResourceGroup; }
    }
}
