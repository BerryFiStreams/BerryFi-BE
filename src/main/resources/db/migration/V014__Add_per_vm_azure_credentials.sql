-- Add per-VM Azure credentials to vm_instances table
-- This allows each VM to have its own Azure subscription, tenant, and client credentials

ALTER TABLE vm_instances 
ADD COLUMN azure_tenant_id VARCHAR(255) NOT NULL DEFAULT 'temp-tenant-id',
ADD COLUMN azure_client_id VARCHAR(255) NOT NULL DEFAULT 'temp-client-id',  
ADD COLUMN azure_client_secret VARCHAR(500) NOT NULL DEFAULT 'temp-client-secret';

-- Remove default values after adding columns
-- (You should update existing records with actual values before running this)
-- ALTER TABLE vm_instances 
-- ALTER COLUMN azure_tenant_id DROP DEFAULT,
-- ALTER COLUMN azure_client_id DROP DEFAULT,
-- ALTER COLUMN azure_client_secret DROP DEFAULT;

-- Add indexes for performance
CREATE INDEX idx_vm_azure_tenant ON vm_instances(azure_tenant_id);
CREATE INDEX idx_vm_azure_client ON vm_instances(azure_client_id);
