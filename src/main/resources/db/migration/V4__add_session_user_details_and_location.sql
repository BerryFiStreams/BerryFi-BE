-- Add user details and location fields to vm_sessions table
ALTER TABLE vm_sessions 
ADD COLUMN user_phone VARCHAR(50),
ADD COLUMN user_first_name VARCHAR(100),
ADD COLUMN user_last_name VARCHAR(100),
ADD COLUMN client_latitude DOUBLE,
ADD COLUMN client_longitude DOUBLE;
