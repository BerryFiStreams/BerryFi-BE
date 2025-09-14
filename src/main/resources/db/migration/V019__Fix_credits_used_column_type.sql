-- Fix credits_used column type to match Hibernate expectation
ALTER TABLE vm_session_audit_logs 
MODIFY COLUMN credits_used DOUBLE;
