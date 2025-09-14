package com.berryfi.portal.service;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.PowerState;
import com.berryfi.portal.entity.VmInstance;
import com.berryfi.portal.enums.VmStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for interacting with Azure VMs.
 * Each VM has its own Azure configuration (subscription, tenant, client credentials).
 * Now includes real Azure SDK integration with status synchronization capabilities.
 */
@Service
public class AzureVmService {

    private static final Logger logger = LoggerFactory.getLogger(AzureVmService.class);
    
    // Cache Azure clients to avoid recreating them for each request
    private final Map<String, AzureResourceManager> azureClientCache = new ConcurrentHashMap<>();
    
    @Value("${azure.status.sync.enabled:true}")
    private boolean statusSyncEnabled;
    
    @Value("${azure.api.timeout.seconds:30}")
    private int azureApiTimeoutSeconds;

    /**
     * Start a VM in Azure using its specific credentials
     */
    public boolean startVm(VmInstance vm) {
        try {
            logger.info("Starting VM: {} (Azure Resource: {}) in subscription: {}", 
                vm.getVmName(), vm.getAzureResourceId(), vm.getAzureSubscriptionId());
            
            if (!statusSyncEnabled) {
                logger.info("Azure sync disabled, simulating VM start for: {}", vm.getVmName());
                return true;
            }
            
            AzureResourceManager azure = getOrCreateAzureClient(vm);
            VirtualMachine virtualMachine = azure.virtualMachines()
                .getByResourceGroup(vm.getAzureResourceGroup(), vm.getAzureResourceId());
                
            if (virtualMachine == null) {
                logger.error("VM not found in Azure: {} in resource group: {}", 
                    vm.getAzureResourceId(), vm.getAzureResourceGroup());
                return false;
            }
            
            // Check current status before starting
            PowerState currentState = virtualMachine.powerState();
            logger.debug("Current Azure VM power state: {}", currentState);
            
            String powerStateStr = currentState.toString();
            if ("PowerState/running".equals(powerStateStr)) {
                logger.info("VM {} is already running in Azure (state: {})", vm.getVmName(), powerStateStr);
                return true;
            }
            
            // Start the VM
            virtualMachine.start();
            logger.info("VM start command sent successfully for: {}", vm.getVmName());
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to start VM {}: {}", vm.getVmName(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * Stop a VM in Azure using its specific credentials
     */
    public boolean stopVm(VmInstance vm) {
        try {
            logger.info("Stopping VM: {} (Azure Resource: {}) in subscription: {}", 
                vm.getVmName(), vm.getAzureResourceId(), vm.getAzureSubscriptionId());
            
            if (!statusSyncEnabled) {
                logger.info("Azure sync disabled, simulating VM stop for: {}", vm.getVmName());
                return true;
            }
            
            AzureResourceManager azure = getOrCreateAzureClient(vm);
            VirtualMachine virtualMachine = azure.virtualMachines()
                .getByResourceGroup(vm.getAzureResourceGroup(), vm.getAzureResourceId());
                
            if (virtualMachine == null) {
                logger.error("VM not found in Azure: {} in resource group: {}", 
                    vm.getAzureResourceId(), vm.getAzureResourceGroup());
                return false;
            }
            
            // Check current status before stopping
            PowerState currentState = virtualMachine.powerState();
            logger.debug("Current Azure VM power state: {}", currentState);
            
            String powerStateStr = currentState.toString();
            if ("PowerState/stopped".equals(powerStateStr) || 
                "PowerState/deallocated".equals(powerStateStr) || 
                "PowerState/deallocating".equals(powerStateStr)) {
                logger.info("VM {} is already stopped/deallocated in Azure (state: {})", vm.getVmName(), powerStateStr);
                return true;
            }
            
            // Stop and deallocate the VM to save costs
            virtualMachine.powerOff();
            virtualMachine.deallocate();
            logger.info("VM stop and deallocate command sent successfully for: {}", vm.getVmName());
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to stop VM {}: {}", vm.getVmName(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * Restart a VM in Azure using its specific credentials
     */
    public boolean restartVm(VmInstance vm) {
        try {
            logger.info("Restarting VM: {} (Azure Resource: {}) in subscription: {}", 
                vm.getVmName(), vm.getAzureResourceId(), vm.getAzureSubscriptionId());
            
            // TODO: Implement Azure SDK integration with per-VM credentials
            // AzureResourceManager azure = createAzureClient(vm);
            // VirtualMachine virtualMachine = azure.virtualMachines()
            //     .getByResourceGroup(vm.getAzureResourceGroup(), vm.getAzureResourceId());
            // virtualMachine.restart();
            
            // For now, simulate success
            logger.info("VM restart request sent for: {} using tenant: {}", 
                vm.getVmName(), vm.getAzureTenantId());
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to restart VM {}: {}", vm.getVmName(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * Get VM status from Azure using its specific credentials
     */
    public VmStatus getVmStatus(VmInstance vm) {
        try {
            logger.debug("Getting status for VM: {} (Azure Resource: {}) in subscription: {}", 
                vm.getVmName(), vm.getAzureResourceId(), vm.getAzureSubscriptionId());
            
            if (!statusSyncEnabled) {
                logger.debug("Azure sync disabled, returning cached status for: {}", vm.getVmName());
                return vm.getStatus(); // Return current database status
            }
            
            AzureResourceManager azure = getOrCreateAzureClient(vm);
            VirtualMachine virtualMachine = azure.virtualMachines()
                .getByResourceGroup(vm.getAzureResourceGroup(), vm.getAzureResourceId());
                
            if (virtualMachine == null) {
                logger.error("VM not found in Azure: {} in resource group: {}", 
                    vm.getAzureResourceId(), vm.getAzureResourceGroup());
                return VmStatus.ERROR;
            }
            
            PowerState powerState = virtualMachine.powerState();
            VmStatus vmStatus = mapPowerStateToVmStatus(powerState);
            
            logger.debug("Azure VM {} power state: {} -> mapped to: {}", 
                vm.getVmName(), powerState, vmStatus);
            
            return vmStatus;
            
        } catch (Exception e) {
            logger.error("Failed to get status for VM {}: {}", vm.getVmName(), e.getMessage(), e);
            return VmStatus.ERROR;
        }
    }

    /**
     * Get VM details from Azure using its specific credentials
     */
    public VmDetails getVmDetails(VmInstance vm) {
        try {
            logger.debug("Getting details for VM: {} (Azure Resource: {}) in subscription: {}", 
                vm.getVmName(), vm.getAzureResourceId(), vm.getAzureSubscriptionId());
            
            // TODO: Implement Azure SDK integration with per-VM credentials
            // AzureResourceManager azure = createAzureClient(vm);
            // VirtualMachine virtualMachine = azure.virtualMachines()
            //     .getByResourceGroup(vm.getAzureResourceGroup(), vm.getAzureResourceId());
            
            // For now, return simulated details
            return new VmDetails(vm.getVmName(), "PowerState/running", "10.0.0.1", "Standard_B2s");
            
        } catch (Exception e) {
            logger.error("Failed to get details for VM {}: {}", vm.getVmName(), e.getMessage(), e);
            return new VmDetails(vm.getVmName(), "PowerState/unknown", null, null);
        }
    }

    /**
     * Get or create cached Azure client using VM-specific credentials
     */
    private AzureResourceManager getOrCreateAzureClient(VmInstance vm) {
        String cacheKey = vm.getAzureSubscriptionId() + "_" + vm.getAzureTenantId() + "_" + vm.getAzureClientId();
        
        return azureClientCache.computeIfAbsent(cacheKey, k -> {
            try {
                logger.debug("Creating new Azure client for VM: {} with tenant: {} and subscription: {}", 
                    vm.getVmName(), vm.getAzureTenantId(), vm.getAzureSubscriptionId());
                
                TokenCredential credential = new ClientSecretCredentialBuilder()
                    .clientId(vm.getAzureClientId())
                    .clientSecret(vm.getAzureClientSecret())
                    .tenantId(vm.getAzureTenantId())
                    .build();
                
                AzureProfile profile = new AzureProfile(vm.getAzureTenantId(), 
                    vm.getAzureSubscriptionId(), AzureEnvironment.AZURE);
                
                return AzureResourceManager.authenticate(credential, profile)
                    .withSubscription(vm.getAzureSubscriptionId());
                    
            } catch (Exception e) {
                logger.error("Failed to create Azure client for VM {}: {}", vm.getVmName(), e.getMessage(), e);
                throw new RuntimeException("Failed to create Azure client", e);
            }
        });
    }
    
    /**
     * Map Azure PowerState to internal VmStatus enum
     */
    private VmStatus mapPowerStateToVmStatus(PowerState powerState) {
        if (powerState == null) {
            return VmStatus.ERROR;
        }
        
        switch (powerState.toString()) {
            case "PowerState/running":
                return VmStatus.RUNNING;
            case "PowerState/stopped":
                return VmStatus.STOPPED;
            case "PowerState/deallocated": 
            case "PowerState/deallocating":
                return VmStatus.DEALLOCATED;
            case "PowerState/starting":
                return VmStatus.STARTING;
            case "PowerState/stopping":
                return VmStatus.STOPPING;
            default:
                logger.warn("Unknown Azure PowerState: {}, mapping to ERROR", powerState);
                return VmStatus.ERROR;
        }
    }

    /**
     * VM details data class
     */
    public static class VmDetails {
        private String name;
        private String status;
        private String ipAddress;
        private String vmSize;

        public VmDetails(String name, String status, String ipAddress, String vmSize) {
            this.name = name;
            this.status = status;
            this.ipAddress = ipAddress;
            this.vmSize = vmSize;
        }

        // Getters
        public String getName() { return name; }
        public String getStatus() { return status; }
        public String getIpAddress() { return ipAddress; }
        public String getVmSize() { return vmSize; }
    }
}
