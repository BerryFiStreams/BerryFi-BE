-- First, deactivate duplicate tracking links, keeping only the most recent one for each project-user-campaign combination
UPDATE tracking_links t1
SET active = false
WHERE t1.short_code NOT IN (
    SELECT t2.short_code 
    FROM (
        SELECT short_code, 
               ROW_NUMBER() OVER (
                   PARTITION BY project_id, user_id, COALESCE(campaign_id, 'NULL')
                   ORDER BY created_at DESC
               ) as rn
        FROM tracking_links
        WHERE active = true
    ) t2
    WHERE t2.rn = 1
)
AND t1.active = true;

-- Add index for better query performance on project-user-campaign-active lookups
CREATE INDEX idx_tracking_link_lookup ON tracking_links(project_id, user_id, campaign_id, active);
