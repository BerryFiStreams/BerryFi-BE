-- Add visit tracking fields to the leads table
ALTER TABLE leads 
ADD COLUMN visit_count INT DEFAULT 0,
ADD COLUMN total_session_time BIGINT DEFAULT 0,
ADD COLUMN last_visit_date DATETIME NULL;

-- Update existing records to have default values for visit tracking
UPDATE leads SET 
    visit_count = 0,
    total_session_time = 0
WHERE visit_count IS NULL OR total_session_time IS NULL;
