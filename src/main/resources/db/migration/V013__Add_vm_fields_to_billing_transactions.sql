-- Migration to add VM-specific fields to billing_transactions table
-- This enhances the existing billing_transactions to support VM usage tracking

-- Add new columns for VM usage tracking (only if they don't exist)
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE table_name = 'billing_transactions' 
    AND table_schema = DATABASE() 
    AND column_name = 'vm_type') = 0,
    'ALTER TABLE billing_transactions ADD COLUMN vm_type VARCHAR(20) NULL',
    'SELECT "Column vm_type already exists"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE table_name = 'billing_transactions' 
    AND table_schema = DATABASE() 
    AND column_name = 'duration_seconds') = 0,
    'ALTER TABLE billing_transactions ADD COLUMN duration_seconds DECIMAL(10,2) NULL',
    'SELECT "Column duration_seconds already exists"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE table_name = 'billing_transactions' 
    AND table_schema = DATABASE() 
    AND column_name = 'credits_per_minute') = 0,
    'ALTER TABLE billing_transactions ADD COLUMN credits_per_minute DECIMAL(10,4) NULL',
    'SELECT "Column credits_per_minute already exists"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add indexes for better query performance (only if they don't exist)
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
    WHERE table_name = 'billing_transactions' 
    AND table_schema = DATABASE() 
    AND index_name = 'idx_billing_transactions_vm_type') = 0,
    'CREATE INDEX idx_billing_transactions_vm_type ON billing_transactions (vm_type)',
    'SELECT "Index idx_billing_transactions_vm_type already exists"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
    WHERE table_name = 'billing_transactions' 
    AND table_schema = DATABASE() 
    AND index_name = 'idx_billing_transactions_vm_usage') = 0,
    'CREATE INDEX idx_billing_transactions_vm_usage ON billing_transactions (vm_type, duration_seconds)',
    'SELECT "Index idx_billing_transactions_vm_usage already exists"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
