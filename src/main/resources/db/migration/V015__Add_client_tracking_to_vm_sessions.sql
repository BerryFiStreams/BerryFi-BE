-- Add client tracking fields to vm_sessions table
-- This migration adds IP address, location, and user agent tracking for VM sessions

ALTER TABLE vm_sessions 
ADD COLUMN username VARCHAR(255) COMMENT 'Username of the session user',
ADD COLUMN client_ip_address VARCHAR(45) COMMENT 'Client IP address (IPv4/IPv6)',
ADD COLUMN client_country VARCHAR(100) COMMENT 'Country resolved from IP address',
ADD COLUMN client_city VARCHAR(100) COMMENT 'City resolved from IP address',
ADD COLUMN user_agent TEXT COMMENT 'Browser user agent string';

-- Add indexes for commonly queried fields
CREATE INDEX idx_vm_session_ip ON vm_sessions (client_ip_address);
CREATE INDEX idx_vm_session_country ON vm_sessions (client_country);
CREATE INDEX idx_vm_session_username ON vm_sessions (username);
