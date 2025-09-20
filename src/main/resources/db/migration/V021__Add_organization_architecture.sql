-- Migration V021: Add Organization Architecture Support
-- This migration adds the organization tables alongside existing workspace tables
-- for backward compatibility during transition

-- Step 1: Create the organizations table
CREATE TABLE IF NOT EXISTS organizations (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    owner_id VARCHAR(50) NOT NULL,
    owner_email VARCHAR(255) NOT NULL,
    owner_name VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    
    -- Credit management
    total_credits DOUBLE PRECISION DEFAULT 0.0,
    used_credits DOUBLE PRECISION DEFAULT 0.0,
    remaining_credits DOUBLE PRECISION DEFAULT 0.0,
    purchased_credits DOUBLE PRECISION DEFAULT 0.0,
    gifted_credits DOUBLE PRECISION DEFAULT 0.0,
    
    -- Budget and limits
    monthly_budget DOUBLE PRECISION DEFAULT 0.0,
    monthly_credits_used DOUBLE PRECISION DEFAULT 0.0,
    
    -- Organization settings
    can_share_projects BOOLEAN DEFAULT TRUE,
    can_receive_shared_projects BOOLEAN DEFAULT TRUE,
    max_projects INTEGER DEFAULT 10,
    max_members INTEGER DEFAULT 50,
    
    -- Statistics
    active_projects INTEGER DEFAULT 0,
    total_members INTEGER DEFAULT 1,
    total_sessions INTEGER DEFAULT 0,
    
    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL,
    
    -- Indexes
    INDEX idx_organization_status (status),
    INDEX idx_organization_owner (owner_id),
    INDEX idx_organization_name (name)
);

-- Step 2: Create project_shares table
CREATE TABLE IF NOT EXISTS project_shares (
    id VARCHAR(50) PRIMARY KEY,
    project_id VARCHAR(50) NOT NULL,
    owner_organization_id VARCHAR(50) NOT NULL,
    shared_with_organization_id VARCHAR(50) NOT NULL,
    share_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    
    -- Credit allocation
    allocated_credits DOUBLE PRECISION DEFAULT 0.0,
    used_credits DOUBLE PRECISION DEFAULT 0.0,
    remaining_credits DOUBLE PRECISION DEFAULT 0.0,
    
    -- Recurring credit settings
    recurring_credits DOUBLE PRECISION DEFAULT 0.0,
    recurring_interval_days INTEGER DEFAULT 0,
    next_credit_gift_date TIMESTAMP,
    last_credit_gift_date TIMESTAMP,
    
    -- One-time credit settings
    one_time_credits DOUBLE PRECISION DEFAULT 0.0,
    one_time_credit_used BOOLEAN DEFAULT FALSE,
    
    -- Permission settings
    can_modify_project BOOLEAN DEFAULT FALSE,
    can_view_analytics BOOLEAN DEFAULT TRUE,
    can_manage_sessions BOOLEAN DEFAULT TRUE,
    can_share_further BOOLEAN DEFAULT FALSE,
    
    -- Expiry settings
    expires_at TIMESTAMP,
    is_permanent BOOLEAN DEFAULT FALSE,
    
    -- Notes
    share_message TEXT,
    terms_conditions TEXT,
    
    -- Audit fields
    shared_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    accepted_at TIMESTAMP,
    rejected_at TIMESTAMP,
    revoked_at TIMESTAMP,
    created_by VARCHAR(50) NOT NULL,
    accepted_by VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Indexes
    INDEX idx_project_share_project (project_id),
    INDEX idx_project_share_owner (owner_organization_id),
    INDEX idx_project_share_shared (shared_with_organization_id),
    INDEX idx_project_share_status (status)
);

-- Step 3: Add organization support to existing projects table (if column doesn't exist)
SELECT COUNT(*) FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
AND TABLE_NAME = 'projects' 
AND COLUMN_NAME = 'is_shareable' INTO @col_exists;

SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE projects ADD COLUMN is_shareable BOOLEAN DEFAULT TRUE', 
    'SELECT "Column is_shareable already exists"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT COUNT(*) FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
AND TABLE_NAME = 'projects' 
AND COLUMN_NAME = 'is_shared_project' INTO @col_exists;

SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE projects ADD COLUMN is_shared_project BOOLEAN DEFAULT FALSE', 
    'SELECT "Column is_shared_project already exists"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT COUNT(*) FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
AND TABLE_NAME = 'projects' 
AND COLUMN_NAME = 'original_organization_id' INTO @col_exists;

SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE projects ADD COLUMN original_organization_id VARCHAR(50)', 
    'SELECT "Column original_organization_id already exists"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT COUNT(*) FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
AND TABLE_NAME = 'projects' 
AND COLUMN_NAME = 'shared_count' INTO @col_exists;

SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE projects ADD COLUMN shared_count INTEGER DEFAULT 0', 
    'SELECT "Column shared_count already exists"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Step 4: Insert sample organization data for testing (only if table is empty)
INSERT IGNORE INTO organizations (
    id, name, description, owner_id, owner_email, owner_name,
    status, total_credits, remaining_credits, purchased_credits,
    created_at, created_by
) VALUES (
    'org_sample', 'Sample Organization', 'Default organization for testing',
    'admin@berryfi.com', 'admin@berryfi.com', 'System Admin',
    'ACTIVE', 1000.0, 1000.0, 1000.0,
    CURRENT_TIMESTAMP, 'system'
);