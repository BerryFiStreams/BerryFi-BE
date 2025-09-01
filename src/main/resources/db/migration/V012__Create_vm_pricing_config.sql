-- Migration to create vm_pricing_config table
-- This table stores credits per minute rates for different VM types

CREATE TABLE IF NOT EXISTS vm_pricing_config (
    id VARCHAR(50) PRIMARY KEY,
    vm_type VARCHAR(20) NOT NULL,
    credits_per_minute DECIMAL(10,4) NOT NULL,
    effective_from TIMESTAMP NOT NULL,
    effective_until TIMESTAMP NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    description TEXT,
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,
    
    INDEX idx_vm_pricing_type_active (vm_type, is_active, effective_from, effective_until),
    INDEX idx_vm_pricing_active (is_active, effective_from, effective_until),
    INDEX idx_vm_pricing_created_by (created_by),
    INDEX idx_vm_pricing_effective (effective_from, effective_until)
);

-- Insert default VM pricing configurations
INSERT IGNORE INTO vm_pricing_config (
    id, 
    vm_type, 
    credits_per_minute, 
    effective_from, 
    is_active, 
    description, 
    created_by, 
    created_at, 
    updated_at
) VALUES 
(
    't4-default-pricing',
    'T4',
    1.0,
    CURRENT_TIMESTAMP,
    TRUE,
    'Default T4 VM pricing - 1 credit per minute',
    'SYSTEM',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
(
    'a10-default-pricing',
    'A10',
    2.0,
    CURRENT_TIMESTAMP,
    TRUE,
    'Default A10 VM pricing - 2 credits per minute',
    'SYSTEM',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);
