-- Flyway Migration V1__add_multitenant_fields_to_projects.sql
-- Add multi-tenant subdomain and branding fields to projects table
-- Date: 2025-10-19

-- Add subdomain field (unique, for tenant identification)
ALTER TABLE projects 
ADD COLUMN subdomain VARCHAR(63) UNIQUE COMMENT 'Unique subdomain for tenant access (e.g., "myproject")';

-- Add custom domain fields for white-label domains
ALTER TABLE projects 
ADD COLUMN custom_domain VARCHAR(255) COMMENT 'Custom domain for white-label access (e.g., "app.client.com")',
ADD COLUMN custom_domain_verified BOOLEAN DEFAULT FALSE COMMENT 'Whether custom domain ownership is verified';

-- Add branding fields for tenant customization
ALTER TABLE projects 
ADD COLUMN brand_logo_url VARCHAR(500) COMMENT 'URL to custom logo image',
ADD COLUMN brand_primary_color VARCHAR(7) COMMENT 'Primary brand color in hex format (e.g., "#ff1136")',
ADD COLUMN brand_secondary_color VARCHAR(7) COMMENT 'Secondary brand color in hex format',
ADD COLUMN brand_favicon_url VARCHAR(500) COMMENT 'URL to custom favicon',
ADD COLUMN brand_app_name VARCHAR(100) COMMENT 'Custom application name for branding';

-- Create index for subdomain lookups (critical for performance)
CREATE INDEX idx_project_subdomain ON projects(subdomain);

-- Create index for custom domain lookups
CREATE INDEX idx_project_custom_domain ON projects(custom_domain);

-- Optional: Backfill subdomains for existing projects (commented out by default)
-- Uncomment and run after verifying migration works
-- UPDATE projects 
-- SET subdomain = CONCAT('project-', LOWER(SUBSTRING(id, 1, 8)))
-- WHERE subdomain IS NULL AND id IS NOT NULL;
