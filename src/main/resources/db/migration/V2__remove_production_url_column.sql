-- Migration to remove production_url column from projects table
-- This column is deprecated as we now use tracking URLs with subdomain/custom domain support

-- Drop the production_url column
ALTER TABLE projects DROP COLUMN IF EXISTS production_url;
