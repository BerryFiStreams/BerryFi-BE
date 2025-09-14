-- Migration script to make VM instances project-based instead of workspace-based
-- This migration updates the vm_instances table structure

-- Step 1: Add project_id column (nullable initially for existing data)
ALTER TABLE vm_instances ADD COLUMN project_id VARCHAR(50);

-- Step 2: Update existing VM instances to use project_id
-- Set project_id based on current_project_id if it exists
UPDATE vm_instances 
SET project_id = current_project_id 
WHERE current_project_id IS NOT NULL;

-- For VMs without current_project_id, we'll need to assign them to a default project
-- This is a business decision - in practice, you might want to:
-- 1. Create a "default" project for each workspace
-- 2. Or remove orphaned VMs
-- 3. Or manually assign them
-- For now, we'll leave them with NULL project_id and they'll need manual assignment

-- Step 3: Drop old indexes
DROP INDEX IF EXISTS idx_vm_workspace;
DROP INDEX IF EXISTS idx_vm_organization;

-- Step 4: Create new indexes for project-based structure
CREATE INDEX idx_vm_project ON vm_instances(project_id);
CREATE INDEX idx_vm_current_project ON vm_instances(current_project_id);

-- Step 5: Remove workspace_id and organization_id columns (commented out for safety)
-- In production, you might want to do this in a separate migration after ensuring everything works
-- ALTER TABLE vm_instances DROP COLUMN workspace_id;
-- ALTER TABLE vm_instances DROP COLUMN organization_id;

-- Step 6: Make project_id NOT NULL for future records (after data cleanup)
-- This should be done after ensuring all VMs have been assigned to projects
-- ALTER TABLE vm_instances ALTER COLUMN project_id SET NOT NULL;

-- Note: The actual column drops and NOT NULL constraint should be done in a separate migration
-- after ensuring all data has been properly migrated and the application is working correctly.
