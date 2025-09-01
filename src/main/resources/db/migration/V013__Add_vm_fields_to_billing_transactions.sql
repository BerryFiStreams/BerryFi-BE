-- Migration to add VM-specific fields to billing_transactions table
-- This enhances the existing billing_transactions to support VM usage tracking

-- Add new columns for VM usage tracking (only if they don't exist)
ALTER TABLE billing_transactions 
ADD COLUMN IF NOT EXISTS vm_type VARCHAR(20) NULL,
ADD COLUMN IF NOT EXISTS duration_seconds DECIMAL(10,2) NULL,
ADD COLUMN IF NOT EXISTS credits_per_minute DECIMAL(10,4) NULL;

-- Add indexes for better query performance (only if they don't exist)
CREATE INDEX IF NOT EXISTS idx_billing_transactions_vm_type ON billing_transactions (vm_type);
CREATE INDEX IF NOT EXISTS idx_billing_transactions_vm_usage ON billing_transactions (vm_type, duration_seconds);
