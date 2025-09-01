-- Migration to create credit_conversion_config table
-- This table stores INR to credit conversion rates with time-based validity

CREATE TABLE IF NOT EXISTS credit_conversion_config (
    id VARCHAR(50) PRIMARY KEY,
    inr_per_credit DECIMAL(10,4) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'INR',
    effective_from TIMESTAMP NOT NULL,
    effective_until TIMESTAMP NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    description TEXT,
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,
    
    INDEX idx_credit_conversion_active (is_active, effective_from, effective_until),
    INDEX idx_credit_conversion_created_by (created_by),
    INDEX idx_credit_conversion_effective (effective_from, effective_until)
);

-- Insert default conversion rate (2.75 INR = 1 credit)
INSERT IGNORE INTO credit_conversion_config (
    id, 
    inr_per_credit, 
    currency, 
    effective_from, 
    is_active, 
    description, 
    created_by, 
    created_at, 
    updated_at
) VALUES (
    'default-inr-conversion',
    2.75,
    'INR',
    CURRENT_TIMESTAMP,
    TRUE,
    'Default INR to credit conversion rate - 2.75 INR per credit',
    'SYSTEM',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);
