package com.berryfi.portal.controller;

import com.berryfi.portal.dto.ApiResponse;
import com.berryfi.portal.entity.User;
import com.berryfi.portal.entity.VmInstance;
import com.berryfi.portal.enums.Role;
import com.berryfi.portal.enums.VmType;
import com.berryfi.portal.repository.UserRepository;
import com.berryfi.portal.repository.VmInstanceRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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
@Tag(name = "VM Instance Administration", description = "VM instance management operations (SUPER_ADMIN only)")
public class VmInstanceController {

    private static final Logger logger = LoggerFactory.getLogger(VmInstanceController.class);

    @Autowired
    private VmInstanceRepository vmInstanceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.berryfi.portal.service.AzureVmService azureVmService;

    /**
     * Check if the current user has SUPER_ADMIN role
     */
    private boolean isSuperAdmin(UserDetails userDetails) {
        if (userDetails == null) {
            return false;
        }
        
        Optional<User> userOpt = userRepository.findByEmail(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            return false;
        }
        
        return userOpt.get().getRole() == Role.SUPER_ADMIN;
    }

    /**
     * Create a new VM instance with Azure credentials
     * Only SUPER_ADMIN users can create VM instances
     */
    @Operation(
        summary = "Create VM instance",
        description = "Creates a new VM instance with Azure credentials. Only SUPER_ADMIN users can perform this operation.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "VM instance created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "Success Response",
                    value = """
                    {
                      "success": true,
                      "message": "VM instance created successfully",
                      "data": {
                        "id": "vm_12345",
                        "vmName": "BerryFi-Dev-VM-001",
                        "vmType": "T4",
                        "status": "AVAILABLE",
                        "projectId": "proj_12345",
                        "createdAt": "2024-01-15T10:30:00Z"
                      }
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", 
            description = "Access denied - SUPER_ADMIN role required",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Access Denied",
                    value = """
                    {
                      "success": false,
                      "message": "Access denied. Only system administrators can create VM instances.",
                      "data": null
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Invalid request data"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500", 
            description = "Internal server error"
        )
    })
    @PostMapping
    public ResponseEntity<ApiResponse<VmInstance>> createVmInstance(
            @RequestBody @Valid CreateVmInstanceRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            // Check if user has SUPER_ADMIN role
            if (!isSuperAdmin(userDetails)) {
                logger.warn("Unauthorized VM instance creation attempt by user: {}", 
                    userDetails != null ? userDetails.getUsername() : "anonymous");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied. Only system administrators can create VM instances."));
            }
            
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
     * Get all VM instances with pagination
     * Only SUPER_ADMIN users can view VM instances
     */
    @Operation(
        summary = "Get all VM instances",
        description = "Retrieves paginated list of all VM instances across all projects. Only SUPER_ADMIN users can perform this operation.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "VM instances retrieved successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", 
            description = "Access denied - SUPER_ADMIN role required"
        )
    })
    @GetMapping
    public ResponseEntity<ApiResponse<Page<VmInstance>>> getAllVmInstances(
            @Parameter(description = "Page number", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            // Check if user has SUPER_ADMIN role
            if (!isSuperAdmin(userDetails)) {
                logger.warn("Unauthorized VM instances access attempt by user: {}", 
                    userDetails != null ? userDetails.getUsername() : "anonymous");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied. Only system administrators can view VM instances."));
            }
            
            Pageable pageable = PageRequest.of(page, size);
            Page<VmInstance> vmInstances = vmInstanceRepository.findAll(pageable);
            
            return ResponseEntity.ok(ApiResponse.success("VM instances retrieved successfully", vmInstances));

        } catch (Exception e) {
            logger.error("Failed to get VM instances: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to get VM instances: " + e.getMessage()));
        }
    }

    /**
     * Get VM instances by project
     * Only SUPER_ADMIN users can view VM instances
     */
    @Operation(
        summary = "Get VM instances by project",
        description = "Retrieves paginated list of VM instances for a specific project. Only SUPER_ADMIN users can perform this operation.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "VM instances retrieved successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", 
            description = "Access denied - SUPER_ADMIN role required"
        )
    })
    @GetMapping("/project/{projectId}")
    public ResponseEntity<ApiResponse<Page<VmInstance>>> getVmInstancesByProject(
            @Parameter(description = "Project ID", required = true, example = "proj_12345")
            @PathVariable String projectId,
            @Parameter(description = "Page number", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            // Check if user has SUPER_ADMIN role
            if (!isSuperAdmin(userDetails)) {
                logger.warn("Unauthorized VM instances access attempt by user: {}", 
                    userDetails != null ? userDetails.getUsername() : "anonymous");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied. Only system administrators can view VM instances."));
            }
            
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
     * Only SUPER_ADMIN users can view available VM instances
     */
    @GetMapping("/project/{projectId}/available")
    public ResponseEntity<ApiResponse<List<VmInstance>>> getAvailableVmInstances(
            @PathVariable String projectId,
            @RequestParam(required = false) VmType vmType,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            // Check if user has SUPER_ADMIN role
            if (!isSuperAdmin(userDetails)) {
                logger.warn("Unauthorized available VM instances access attempt by user: {}", 
                    userDetails != null ? userDetails.getUsername() : "anonymous");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied. Only system administrators can view available VM instances."));
            }
            
            List<VmInstance> vmInstances;
            if (vmType != null) {
                vmInstances = vmInstanceRepository.findAvailableVmsByTypeForProject(projectId, vmType.getValue());
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
     * Only SUPER_ADMIN users can update VM credentials
     */
    @Operation(
        summary = "Update VM credentials",
        description = "Updates Azure credentials for a VM instance. Only SUPER_ADMIN users can perform this operation.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "VM credentials updated successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", 
            description = "Access denied - SUPER_ADMIN role required"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "VM instance not found"
        )
    })
    @PutMapping("/{vmInstanceId}/credentials")
    public ResponseEntity<ApiResponse<VmInstance>> updateVmCredentials(
            @PathVariable String vmInstanceId,
            @RequestBody @Valid UpdateVmCredentialsRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            // Check if user has SUPER_ADMIN role
            if (!isSuperAdmin(userDetails)) {
                logger.warn("Unauthorized VM credentials update attempt by user: {}", 
                    userDetails != null ? userDetails.getUsername() : "anonymous");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied. Only system administrators can update VM credentials."));
            }
            
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
     * Only SUPER_ADMIN users can view detailed VM instance information
     */
    @GetMapping("/{vmInstanceId}")
    public ResponseEntity<ApiResponse<VmInstance>> getVmInstance(
            @PathVariable String vmInstanceId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            // Check if user has SUPER_ADMIN role
            if (!isSuperAdmin(userDetails)) {
                logger.warn("Unauthorized VM instance access attempt by user: {}", 
                    userDetails != null ? userDetails.getUsername() : "anonymous");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied. Only system administrators can view VM instance details."));
            }
            
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
     * Only SUPER_ADMIN users can delete VM instances
     */
    @Operation(
        summary = "Delete VM instance",
        description = "Deletes a VM instance. The VM instance must not be in use. Only SUPER_ADMIN users can perform this operation.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "VM instance deleted successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "VM instance is currently in use and cannot be deleted"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", 
            description = "Access denied - SUPER_ADMIN role required"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "VM instance not found"
        )
    })
    @DeleteMapping("/{vmInstanceId}")
    public ResponseEntity<ApiResponse<String>> deleteVmInstance(
            @PathVariable String vmInstanceId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            // Check if user has SUPER_ADMIN role
            if (!isSuperAdmin(userDetails)) {
                logger.warn("Unauthorized VM instance deletion attempt by user: {}", 
                    userDetails != null ? userDetails.getUsername() : "anonymous");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied. Only system administrators can delete VM instances."));
            }
            
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

    /**
     * Test Azure VM configuration
     * Only SUPER_ADMIN users can test VM configurations
     */
    @Operation(
        summary = "Test Azure VM configuration",
        description = "Tests Azure credentials and VM accessibility to validate configuration before creating VM instances. Only SUPER_ADMIN users can perform this operation.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Configuration test completed",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = {
                    @ExampleObject(
                        name = "Success Response",
                        value = """
                        {
                          "success": true,
                          "message": "Azure configuration test completed successfully",
                          "data": {
                            "configurationValid": true,
                            "credentialsValid": true,
                            "vmAccessible": true,
                            "vmStatus": "PowerState/running",
                            "vmSize": "Standard_B2s",
                            "ipAddress": "10.0.0.100",
                            "testTimestamp": "2024-01-15T10:30:00Z"
                          }
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "Failed Response",
                        value = """
                        {
                          "success": true,
                          "message": "Azure configuration test completed with issues",
                          "data": {
                            "configurationValid": false,
                            "credentialsValid": false,
                            "vmAccessible": false,
                            "errorMessage": "Authentication failed: Invalid client credentials",
                            "testTimestamp": "2024-01-15T10:30:00Z"
                          }
                        }
                        """
                    )
                }
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", 
            description = "Access denied - SUPER_ADMIN role required"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Invalid request data"
        )
    })
    @PostMapping("/test-configuration")
    public ResponseEntity<ApiResponse<VmConfigurationTestResult>> testVmConfiguration(
            @RequestBody @Valid TestVmConfigurationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            // Check if user has SUPER_ADMIN role
            if (!isSuperAdmin(userDetails)) {
                logger.warn("Unauthorized VM configuration test attempt by user: {}", 
                    userDetails != null ? userDetails.getUsername() : "anonymous");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied. Only system administrators can test VM configurations."));
            }
            
            logger.info("Testing Azure VM configuration for resource: {} by user: {}", 
                request.getAzureResourceId(), userDetails.getUsername());
            
            VmConfigurationTestResult result = performConfigurationTest(request);
            
            if (result.isConfigurationValid()) {
                return ResponseEntity.ok(ApiResponse.success("Azure configuration test completed successfully", result));
            } else {
                return ResponseEntity.ok(ApiResponse.success("Azure configuration test completed with issues", result));
            }

        } catch (Exception e) {
            logger.error("Failed to test VM configuration: " + e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to test VM configuration: " + e.getMessage()));
        }
    }

    /**
     * Perform the actual configuration test
     */
    private VmConfigurationTestResult performConfigurationTest(TestVmConfigurationRequest request) {
        VmConfigurationTestResult result = new VmConfigurationTestResult();
        result.setTestTimestamp(java.time.LocalDateTime.now());
        
        try {
            // Create a temporary VmInstance object for testing
            VmInstance tempVm = new VmInstance(
                "test-vm",
                VmType.T4, // Use T4 as test type
                request.getAzureResourceId(),
                request.getAzureResourceGroup(),
                request.getAzureSubscriptionId(),
                request.getAzureTenantId(),
                request.getAzureClientId(),
                request.getAzureClientSecret(),
                "test-project",
                "test-user"
            );
            
            // Test 1: Basic credential validation by trying to create Azure client
            logger.debug("Testing Azure credentials...");
            try {
                com.berryfi.portal.service.AzureVmService.VmDetails vmDetails = azureVmService.getVmDetails(tempVm);
                result.setCredentialsValid(true);
                logger.debug("Azure credentials validation successful");
                
                // Test 2: VM accessibility
                logger.debug("Testing VM accessibility...");
                if (vmDetails != null && vmDetails.getName() != null) {
                    result.setVmAccessible(true);
                    result.setVmStatus(vmDetails.getStatus());
                    result.setVmSize(vmDetails.getVmSize());
                    result.setIpAddress(vmDetails.getIpAddress());
                    logger.debug("VM accessibility test successful");
                } else {
                    result.setVmAccessible(false);
                    result.setErrorMessage("VM not found or not accessible in Azure");
                    logger.warn("VM not accessible: {}", request.getAzureResourceId());
                }
                
            } catch (Exception credentialError) {
                result.setCredentialsValid(false);
                result.setVmAccessible(false);
                result.setErrorMessage("Credential validation failed: " + credentialError.getMessage());
                logger.warn("Azure credential validation failed: {}", credentialError.getMessage());
            }
            
            // Overall configuration validity
            result.setConfigurationValid(result.isCredentialsValid() && result.isVmAccessible());
            
        } catch (Exception e) {
            result.setConfigurationValid(false);
            result.setCredentialsValid(false);
            result.setVmAccessible(false);
            result.setErrorMessage("Configuration test failed: " + e.getMessage());
            logger.error("Configuration test failed: {}", e.getMessage(), e);
        }
        
        return result;
    }

    // Request DTOs
    @Schema(description = "Request payload for testing Azure VM configuration")
    public static class TestVmConfigurationRequest {
        @Schema(description = "Azure resource ID", example = "/subscriptions/12345/resourceGroups/vms/providers/Microsoft.Compute/virtualMachines/vm-001", required = true)
        @NotBlank(message = "Azure resource ID is required")
        private String azureResourceId;
        
        @Schema(description = "Azure resource group", example = "berryfi-vms", required = true)
        @NotBlank(message = "Azure resource group is required")
        private String azureResourceGroup;
        
        @Schema(description = "Azure subscription ID", example = "12345678-1234-1234-1234-123456789012", required = true)
        @NotBlank(message = "Azure subscription ID is required")
        private String azureSubscriptionId;
        
        @Schema(description = "Azure tenant ID", example = "87654321-4321-4321-4321-210987654321", required = true)
        @NotBlank(message = "Azure tenant ID is required")
        private String azureTenantId;
        
        @Schema(description = "Azure client ID", example = "11111111-1111-1111-1111-111111111111", required = true)
        @NotBlank(message = "Azure client ID is required")
        private String azureClientId;
        
        @Schema(description = "Azure client secret", example = "your-azure-client-secret", required = true)
        @NotBlank(message = "Azure client secret is required")
        private String azureClientSecret;

        // Getters and setters
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
    }

    // Response DTO
    @Schema(description = "Result of Azure VM configuration test")
    public static class VmConfigurationTestResult {
        @Schema(description = "Overall configuration validity", example = "true")
        private boolean configurationValid;
        
        @Schema(description = "Whether Azure credentials are valid", example = "true")
        private boolean credentialsValid;
        
        @Schema(description = "Whether VM is accessible", example = "true")
        private boolean vmAccessible;
        
        @Schema(description = "Current VM status from Azure", example = "PowerState/running")
        private String vmStatus;
        
        @Schema(description = "VM size/type from Azure", example = "Standard_B2s")
        private String vmSize;
        
        @Schema(description = "VM IP address", example = "10.0.0.100")
        private String ipAddress;
        
        @Schema(description = "Error message if test failed", example = "Authentication failed: Invalid client credentials")
        private String errorMessage;
        
        @Schema(description = "Test execution timestamp", example = "2024-01-15T10:30:00Z")
        private java.time.LocalDateTime testTimestamp;

        // Getters and setters
        public boolean isConfigurationValid() { return configurationValid; }
        public void setConfigurationValid(boolean configurationValid) { this.configurationValid = configurationValid; }
        
        public boolean isCredentialsValid() { return credentialsValid; }
        public void setCredentialsValid(boolean credentialsValid) { this.credentialsValid = credentialsValid; }
        
        public boolean isVmAccessible() { return vmAccessible; }
        public void setVmAccessible(boolean vmAccessible) { this.vmAccessible = vmAccessible; }
        
        public String getVmStatus() { return vmStatus; }
        public void setVmStatus(String vmStatus) { this.vmStatus = vmStatus; }
        
        public String getVmSize() { return vmSize; }
        public void setVmSize(String vmSize) { this.vmSize = vmSize; }
        
        public String getIpAddress() { return ipAddress; }
        public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public java.time.LocalDateTime getTestTimestamp() { return testTimestamp; }
        public void setTestTimestamp(java.time.LocalDateTime testTimestamp) { this.testTimestamp = testTimestamp; }
    }

    @Schema(description = "Request payload for creating a new VM instance")
    public static class CreateVmInstanceRequest {
        @Schema(description = "VM name", example = "BerryFi-Dev-VM-001", required = true)
        @NotBlank(message = "VM name is required")
        private String vmName;
        
        @Schema(description = "VM type (T4 or A10 only)", example = "T4", required = true, allowableValues = {"T4", "A10"})
        private VmType vmType;
        
        @Schema(description = "Azure resource ID", example = "/subscriptions/12345/resourceGroups/vms/providers/Microsoft.Compute/virtualMachines/vm-001", required = true)
        @NotBlank(message = "Azure resource ID is required")
        private String azureResourceId;
        
        @Schema(description = "Azure resource group", example = "berryfi-vms", required = true)
        @NotBlank(message = "Azure resource group is required")
        private String azureResourceGroup;
        
        @Schema(description = "Azure subscription ID", example = "12345678-1234-1234-1234-123456789012", required = true)
        @NotBlank(message = "Azure subscription ID is required")
        private String azureSubscriptionId;
        
        @Schema(description = "Azure tenant ID", example = "87654321-4321-4321-4321-210987654321", required = true)
        @NotBlank(message = "Azure tenant ID is required")
        private String azureTenantId;
        
        @Schema(description = "Azure client ID", example = "11111111-1111-1111-1111-111111111111", required = true)
        @NotBlank(message = "Azure client ID is required")
        private String azureClientId;
        
        @Schema(description = "Azure client secret", example = "your-azure-client-secret", required = true)
        @NotBlank(message = "Azure client secret is required")
        private String azureClientSecret;
        
        @Schema(description = "Project ID", example = "proj_12345", required = true)
        @NotBlank(message = "Project ID is required")
        private String projectId;
        
        @Schema(description = "Azure region", example = "East US")
        private String azureRegion;
        
        @Schema(description = "VM description", example = "Development VM for BerryFi project")
        private String description;

        // Getters and setters
        public String getVmName() { return vmName; }
        public void setVmName(String vmName) { this.vmName = vmName; }
        
        public VmType getVmType() { return vmType; }
        public void setVmType(VmType vmType) { this.vmType = vmType; }
        
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
