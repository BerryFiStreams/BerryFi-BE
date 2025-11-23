-- Add campaign_id column to tracking_links table
ALTER TABLE tracking_links ADD COLUMN campaign_id VARCHAR(255);

-- Add index for campaign lookups
CREATE INDEX idx_tracking_link_campaign ON tracking_links(campaign_id);
