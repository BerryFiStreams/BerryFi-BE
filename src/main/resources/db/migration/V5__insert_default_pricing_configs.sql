-- Insert default VM pricing configurations
-- These define how many credits per minute each VM type costs

-- T4 VM Pricing: 1.0 credits per minute
INSERT INTO vm_pricing_config (
    id,
    vm_type,
    credits_per_minute,
    effective_from,
    effective_until,
    is_active,
    description,
    created_by,
    created_at,
    updated_at
) VALUES (
    'vpc_t4_default',
    'T4',
    1.0,
    NOW(),
    NULL,
    1,
    'Default pricing for NVIDIA T4 GPU VMs - 1 credit per minute',
    'system',
    NOW(),
    NOW()
);

-- A10 VM Pricing: 2.0 credits per minute (more expensive, more powerful)
INSERT INTO vm_pricing_config (
    id,
    vm_type,
    credits_per_minute,
    effective_from,
    effective_until,
    is_active,
    description,
    created_by,
    created_at,
    updated_at
) VALUES (
    'vpc_a10_default',
    'A10',
    2.0,
    NOW(),
    NULL,
    1,
    'Default pricing for NVIDIA A10 GPU VMs - 2 credits per minute',
    'system',
    NOW(),
    NOW()
);

-- Insert default credit conversion configuration
-- This defines how much INR = 1 credit
INSERT INTO credit_conversion_config (
    id,
    inr_per_credit,
    currency,
    effective_from,
    effective_until,
    is_active,
    description,
    created_by,
    created_at,
    updated_at
) VALUES (
    'ccc_default',
    10.0,
    'INR',
    NOW(),
    NULL,
    1,
    'Default conversion: 1 credit = 10 INR',
    'system',
    NOW(),
    NOW()
) ON DUPLICATE KEY UPDATE
    updated_at = NOW();
