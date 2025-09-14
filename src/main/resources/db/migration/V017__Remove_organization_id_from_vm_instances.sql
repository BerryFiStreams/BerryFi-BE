-- Remove organization_id column from vm_instances table
-- This completes the migration to project-based VM management

-- First, let's check if the column exists and remove it if it does
-- Note: MySQL syntax for conditional column drop

-- Drop the column if it exists
SET @sql = (
    SELECT CASE 
        WHEN COUNT(*) > 0 THEN 'ALTER TABLE vm_instances DROP COLUMN organization_id;'
        ELSE 'SELECT "Column organization_id does not exist";'
    END
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'vm_instances' 
    AND COLUMN_NAME = 'organization_id'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Also drop workspace_id column if it exists
SET @sql = (
    SELECT CASE 
        WHEN COUNT(*) > 0 THEN 'ALTER TABLE vm_instances DROP COLUMN workspace_id;'
        ELSE 'SELECT "Column workspace_id does not exist";'
    END
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'vm_instances' 
    AND COLUMN_NAME = 'workspace_id'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
