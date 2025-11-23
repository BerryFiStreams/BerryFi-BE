-- Drop the custom_name index and column if they exist
-- MySQL doesn't support DROP COLUMN/INDEX IF EXISTS, so we use a stored procedure
DROP PROCEDURE IF EXISTS drop_custom_name_migration;

DELIMITER $$
CREATE PROCEDURE drop_custom_name_migration()
BEGIN
    DECLARE idx_exists INT DEFAULT 0;
    DECLARE col_exists INT DEFAULT 0;
    
    -- Check if index exists
    SELECT COUNT(*) INTO idx_exists
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'campaigns'
      AND INDEX_NAME = 'idx_campaign_custom_name';
    
    -- Drop index if it exists
    IF idx_exists > 0 THEN
        ALTER TABLE campaigns DROP INDEX idx_campaign_custom_name;
    END IF;
    
    -- Check if column exists
    SELECT COUNT(*) INTO col_exists
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'campaigns'
      AND COLUMN_NAME = 'custom_name';
    
    -- Drop column if it exists
    IF col_exists > 0 THEN
        ALTER TABLE campaigns DROP COLUMN custom_name;
    END IF;
END$$

DELIMITER ;

CALL drop_custom_name_migration();

DROP PROCEDURE IF EXISTS drop_custom_name_migration;
