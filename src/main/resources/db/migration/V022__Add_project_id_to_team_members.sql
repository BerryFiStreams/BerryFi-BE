-- Migration V022: Add project_id column to team_members table
-- This column allows team members to be associated with specific projects (for shared access)

-- Add project_id column to team_members table
ALTER TABLE team_members 
ADD COLUMN project_id VARCHAR(50) DEFAULT NULL;

-- Add index for better query performance
CREATE INDEX idx_team_project ON team_members (project_id);

-- Add foreign key constraint (if projects table exists)
-- ALTER TABLE team_members 
-- ADD CONSTRAINT fk_team_member_project 
-- FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE SET NULL;