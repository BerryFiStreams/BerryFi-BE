-- Add workspace_id column to existing audit_logs table for workspace-level auditing
ALTER TABLE audit_logs 
ADD COLUMN workspace_id VARCHAR(50) NULL,
ADD INDEX idx_audit_workspace (workspace_id);

-- Create vm_session_audit_logs table for detailed VM session auditing
CREATE TABLE vm_session_audit_logs (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    user_name VARCHAR(255),
    user_email VARCHAR(255),
    organization_id VARCHAR(255) NOT NULL,
    workspace_id VARCHAR(50) NOT NULL,
    workspace_name VARCHAR(255),
    project_id VARCHAR(255),
    project_name VARCHAR(255),
    session_id VARCHAR(50),
    vm_instance_id VARCHAR(50),
    vm_instance_type VARCHAR(50),
    action VARCHAR(100) NOT NULL,
    resource VARCHAR(100) NOT NULL DEFAULT 'VM_SESSION',
    resource_id VARCHAR(255),
    details TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    timestamp TIMESTAMP NOT NULL,
    status VARCHAR(50) DEFAULT 'SUCCESS',
    error_message TEXT,
    session_duration_seconds BIGINT,
    credits_used DOUBLE,
    session_status VARCHAR(50),
    vm_ip_address VARCHAR(45),
    vm_port INT,
    connection_url VARCHAR(500),
    termination_reason VARCHAR(255),
    heartbeat_count INT,
    client_country VARCHAR(100),
    client_city VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create indexes for vm_session_audit_logs
CREATE INDEX idx_vm_audit_workspace ON vm_session_audit_logs (workspace_id);
CREATE INDEX idx_vm_audit_user ON vm_session_audit_logs (user_id);
CREATE INDEX idx_vm_audit_session ON vm_session_audit_logs (session_id);
CREATE INDEX idx_vm_audit_timestamp ON vm_session_audit_logs (timestamp);
CREATE INDEX idx_vm_audit_action ON vm_session_audit_logs (action);
CREATE INDEX idx_vm_audit_organization ON vm_session_audit_logs (organization_id);
CREATE INDEX idx_vm_audit_vm_instance ON vm_session_audit_logs (vm_instance_id);
CREATE INDEX idx_vm_audit_project ON vm_session_audit_logs (project_id);
