# VM Session Management System

This document describes the complete VM session management system implemented for BerryFi, allowing users to track usage of actual Azure VMs with real-time monitoring and workspace-based credit billing.

## Overview

The VM session management system provides:
- **Azure VM Integration**: Start, stop, and monitor actual Azure VMs
- **Session Lifecycle Management**: Track VM usage sessions with billing
- **Real-time Monitoring**: Heartbeat-based VM activity monitoring
- **Credit-based Billing**: Workspace-level credit allocation and usage tracking
- **Automatic Cleanup**: Background monitoring for timeout and long-running sessions

## Architecture

### Credit Hierarchy
```
Organization (purchases credits with INR)
    ↓
Workspace (allocated credits from organization)
    ↓
Projects (share credits from workspace pool)
    ↓
VM Sessions (deduct credits from workspace)
```

### Core Components

#### 1. Entities

**VmInstance** (`com.berryfi.portal.entity.VmInstance`)
- Represents Azure VMs available in workspace pools
- Links to Azure resources (subscription, resource group, VM name)
- Tracks assignment to projects and sessions
- Manages VM status lifecycle

**VmSession** (`com.berryfi.portal.entity.VmSession`)
- Tracks individual VM usage sessions
- Records start/end times, user, project, workspace
- Calculates duration and credit usage
- Manages heartbeat timestamps

**VmHeartbeat** (`com.berryfi.portal.entity.VmHeartbeat`)
- Records VM activity with system metrics
- Tracks CPU, memory usage per heartbeat
- Links to active sessions

#### 2. Enums

**VmStatus** (`com.berryfi.portal.enums.VmStatus`)
```java
AVAILABLE, STARTING, RUNNING, STOPPING, STOPPED, 
DEALLOCATED, MAINTENANCE, ERROR
```

**SessionStatus** (`com.berryfi.portal.enums.SessionStatus`)
```java
STARTING, RUNNING, TERMINATING, COMPLETED, FAILED, TIMEOUT
```

#### 3. Services

**VmSessionService** (`com.berryfi.portal.service.VmSessionService`)
- Core session lifecycle management
- VM assignment and release
- Credit validation and billing integration
- Heartbeat processing

**AzureVmService** (`com.berryfi.portal.service.AzureVmService`)
- Azure SDK integration for VM operations
- Start/stop/restart VM commands
- Status monitoring and details retrieval
- IP address and resource information

**VmMonitoringService** (`com.berryfi.portal.service.VmMonitoringService`)
- Background scheduled monitoring
- Timeout detection (5 minutes default)
- Long-running session cleanup (8 hours default)
- Automatic session termination

**PricingService** (`com.berryfi.portal.service.PricingService`)
- Database-driven pricing configuration
- Credits per minute calculation
- INR to credits conversion

**BillingService** (Enhanced)
- Workspace credit allocation from organizations
- VM usage billing integration
- Credit balance validation

#### 4. Controllers

**VmController** (`com.berryfi.portal.controller.VmController`)
- REST API for VM session management
- Endpoints: start, stop, heartbeat, status
- Authentication and authorization

#### 5. Repositories

All repositories extend JpaRepository with custom query methods:
- `VmInstanceRepository`: VM pool management
- `VmSessionRepository`: Session queries
- `VmHeartbeatRepository`: Heartbeat storage
- Enhanced workspace/billing repositories

## API Endpoints

### VM Session Management

#### Start VM Session
```http
POST /api/vm/sessions/start
Content-Type: application/json
Authorization: Bearer <jwt-token>

{
  "projectId": "project-123",
  "vmType": "medium"
}
```

#### Stop VM Session
```http
POST /api/vm/sessions/{sessionId}/stop
Authorization: Bearer <jwt-token>
```

#### Submit Heartbeat
```http
POST /api/vm/sessions/{sessionId}/heartbeat
Content-Type: application/json
Authorization: Bearer <jwt-token>

{
  "status": "running",
  "cpuUsage": 45.2,
  "memoryUsage": 67.8
}
```

#### Get Session Status
```http
GET /api/vm/sessions/{sessionId}
Authorization: Bearer <jwt-token>
```

#### Get Active Session
```http
GET /api/vm/sessions/active
Authorization: Bearer <jwt-token>
```

### VM Instance Management (Admin)

#### Create VM Instance
```http
POST /api/admin/vm-instances
Content-Type: application/json
Authorization: Bearer <admin-jwt-token>

{
  "vmName": "ml-gpu-vm-01",
  "vmType": "large", 
  "azureResourceId": "ml-gpu-vm-01",
  "azureResourceGroup": "berryfi-ml-resources",
  "azureSubscriptionId": "12345678-1234-1234-1234-123456789012",
  "azureTenantId": "87654321-4321-4321-4321-210987654321",
  "azureClientId": "abcd1234-5678-9012-3456-789012345678",
  "azureClientSecret": "your-client-secret-here",
  "workspaceId": "workspace-123",
  "organizationId": "org-456"
}
```

#### Get Workspace VMs
```http
GET /api/admin/vm-instances/workspace/{workspaceId}?page=0&size=10
Authorization: Bearer <admin-jwt-token>
```

#### Get Available VMs
```http
GET /api/admin/vm-instances/workspace/{workspaceId}/available?vmType=large
Authorization: Bearer <admin-jwt-token>
```

#### Update VM Credentials
```http
PUT /api/admin/vm-instances/{vmInstanceId}/credentials
Content-Type: application/json
Authorization: Bearer <admin-jwt-token>

{
  "azureSubscriptionId": "new-subscription-id",
  "azureTenantId": "new-tenant-id",
  "azureClientId": "new-client-id", 
  "azureClientSecret": "new-client-secret"
}
```

#### Delete VM Instance
```http
DELETE /api/admin/vm-instances/{vmInstanceId}
Authorization: Bearer <admin-jwt-token>
```

## Configuration

### Per-VM Azure Credentials

**Important**: Each VM instance has its own Azure configuration stored in the database. This allows you to manage VMs across different Azure subscriptions, tenants, and service principals.

#### VM Instance Database Schema
```sql
CREATE TABLE vm_instances (
    -- ... existing fields ...
    azure_subscription_id VARCHAR(255) NOT NULL,
    azure_tenant_id VARCHAR(255) NOT NULL,
    azure_client_id VARCHAR(255) NOT NULL,
    azure_client_secret VARCHAR(500) NOT NULL,
    azure_resource_group VARCHAR(255) NOT NULL,
    -- ... rest of schema ...
);
```

#### Creating VM Instances
Use the VM Instance Management API to create VMs with their specific Azure credentials:

```http
POST /api/admin/vm-instances
Content-Type: application/json
Authorization: Bearer <admin-jwt-token>

{
  "vmName": "ml-gpu-vm-01",
  "vmType": "large",
  "azureResourceId": "ml-gpu-vm-01",
  "azureResourceGroup": "berryfi-ml-resources",
  "azureSubscriptionId": "12345678-1234-1234-1234-123456789012",
  "azureTenantId": "87654321-4321-4321-4321-210987654321",
  "azureClientId": "abcd1234-5678-9012-3456-789012345678",
  "azureClientSecret": "your-client-secret-here",
  "workspaceId": "workspace-123",
  "organizationId": "org-456",
  "azureRegion": "eastus",
  "description": "High-performance ML training VM"
}
```

### Application Properties
```properties
# VM Configuration
vm.heartbeat.timeout.minutes=5
vm.session.max.duration.hours=8
vm.pricing.credits-per-minute.small=0.5
vm.pricing.credits-per-minute.medium=1.0
vm.pricing.credits-per-minute.large=2.0

# Note: Azure credentials are now stored per-VM in the database
# Each VM instance contains its own:
# - azure_subscription_id 
# - azure_tenant_id
# - azure_client_id  
# - azure_client_secret
# - azure_resource_group
```

### Database Schema

#### VM Instances Table
```sql
CREATE TABLE vm_instances (
    id VARCHAR(50) PRIMARY KEY,
    vm_name VARCHAR(255) NOT NULL,
    vm_type VARCHAR(50) NOT NULL,
    azure_resource_id VARCHAR(255) NOT NULL,
    azure_resource_group VARCHAR(255) NOT NULL,
    azure_subscription_id VARCHAR(255) NOT NULL,
    workspace_id VARCHAR(50) NOT NULL,
    organization_id VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    current_project_id VARCHAR(50),
    current_session_id VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### VM Sessions Table
```sql
CREATE TABLE vm_sessions (
    id VARCHAR(50) PRIMARY KEY,
    vm_instance_id VARCHAR(50) NOT NULL,
    project_id VARCHAR(50) NOT NULL,
    workspace_id VARCHAR(50) NOT NULL,
    organization_id VARCHAR(50) NOT NULL,
    user_id VARCHAR(50) NOT NULL,
    user_email VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    last_heartbeat TIMESTAMP,
    credits_per_minute DECIMAL(10,4),
    credits_used DECIMAL(10,4),
    billing_transaction_id VARCHAR(50),
    termination_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### VM Heartbeats Table
```sql
CREATE TABLE vm_heartbeats (
    id VARCHAR(50) PRIMARY KEY,
    session_id VARCHAR(50) NOT NULL,
    status VARCHAR(255) NOT NULL,
    cpu_usage DECIMAL(5,2),
    memory_usage DECIMAL(5,2),
    disk_usage DECIMAL(5,2),
    network_in DECIMAL(10,2),
    network_out DECIMAL(10,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Usage Flow

### 1. VM Session Startup
1. User requests VM session via API
2. System validates project access and workspace credits
3. Available VM assigned from workspace pool
4. Azure VM started via Azure SDK
5. Session created with STARTING status
6. VM status updated to RUNNING
7. Session marked as RUNNING

### 2. Active Session Management
1. User/system sends heartbeats every 30-60 seconds
2. Heartbeats update last_heartbeat timestamp
3. System metrics (CPU, memory) recorded
4. Session duration continuously calculated

### 3. Session Termination
1. User requests stop OR timeout detected
2. Session marked as TERMINATING
3. Azure VM stopped/deallocated
4. Usage duration calculated
5. Credits billed to workspace
6. VM released back to available pool
7. Session marked as COMPLETED

### 4. Background Monitoring
1. **Timeout Monitor** (every 2 minutes)
   - Finds sessions without heartbeat > 5 minutes
   - Force terminates timed-out sessions
   
2. **Long-running Monitor** (every 10 minutes)
   - Finds sessions > 8 hours duration
   - Force terminates long-running sessions

## Credit Flow

### Organization Level
- Organizations purchase credits with INR payments
- Credits stored in organization balance
- Can allocate credits to workspaces

### Workspace Level
- Receives credit allocation from organization
- Maintains workspace credit balance
- All VM sessions deduct from workspace credits
- Multiple projects share workspace credit pool

### Billing Integration
- VM usage calculated as: `duration_minutes * credits_per_minute`
- Credits deducted from workspace balance
- Billing transaction created for audit trail
- Failed billing prevents session completion

## Monitoring & Alerts

### Session Health
- Heartbeat timeout detection
- Automatic session cleanup
- Resource leak prevention

### Cost Management
- Real-time credit usage tracking
- Workspace budget monitoring
- Usage analytics and reporting

### System Health
- Azure connectivity monitoring
- VM pool availability
- Background service health

## Security Considerations

### Authentication & Authorization
- JWT-based API authentication
- User session ownership validation
- Admin-only VM instance management endpoints
- Admin-only force termination endpoints

### Per-VM Azure Credentials
- **Isolation**: Each VM uses its own Azure service principal
- **Least Privilege**: Service principals can be scoped to specific resource groups
- **Multi-Tenant Support**: Support VMs across different Azure tenants/subscriptions
- **Credential Rotation**: Update credentials per-VM without affecting others
- **Audit Trail**: Track which credentials are used for each VM operation

### Azure Integration Security
- Service principal authentication per VM
- Resource group scoped permissions
- Secure credential storage in database (encrypted at rest recommended)
- Credential masking in API responses

### Data Protection
- Azure client secrets masked in API responses
- Session data encryption recommended
- Audit trail maintenance
- PII handling compliance

### Best Practices
1. **Rotate Credentials Regularly**: Use the credential update API
2. **Scope Permissions**: Limit service principals to specific resource groups
3. **Monitor Access**: Log all Azure operations per VM
4. **Encrypt Secrets**: Consider encrypting client secrets in database
5. **Network Security**: Use Azure private endpoints when possible

## Deployment Notes

### Prerequisites
1. Multiple Azure subscriptions with VM resources (as needed)
2. Service principals for each subscription with VM management permissions
3. MySQL database with schema migrations
4. Spring Boot 3.x application environment

### Azure Service Principal Setup
For each Azure subscription that will host VMs:

1. Create a service principal:
```bash
az ad sp create-for-rbac --name "BerryFi-VM-SP-{subscription-name}" \
  --role "Virtual Machine Contributor" \
  --scopes "/subscriptions/{subscription-id}"
```

2. Note down the output:
   - `appId` → `azure_client_id`
   - `password` → `azure_client_secret`  
   - `tenant` → `azure_tenant_id`
   - Subscription ID → `azure_subscription_id`

### Database Migration
Run the migration to add per-VM Azure credentials:
```sql
-- V014__Add_per_vm_azure_credentials.sql
ALTER TABLE vm_instances 
ADD COLUMN azure_tenant_id VARCHAR(255) NOT NULL DEFAULT 'temp-tenant-id',
ADD COLUMN azure_client_id VARCHAR(255) NOT NULL DEFAULT 'temp-client-id',  
ADD COLUMN azure_client_secret VARCHAR(500) NOT NULL DEFAULT 'temp-client-secret';
```

### VM Instance Setup
1. Use the admin API to create VM instances with their specific Azure credentials
2. Each VM can belong to different subscriptions/tenants
3. Verify Azure connectivity for each VM before making them available

### Configuration Steps
1. Set up service principals for each Azure subscription
2. Configure application.properties (without global Azure config)
3. Run database migrations
4. Create VM instances via API with their specific credentials
5. Test VM session management

## Future Enhancements

### Planned Features
- Multi-region VM support
- VM template management
- Advanced resource scheduling
- Cost optimization algorithms
- Real-time usage dashboards

### Scalability Considerations
- VM pool auto-scaling
- Session load balancing
- Database sharding for large deployments
- Caching layer for frequent queries

## Troubleshooting

### Common Issues
1. **Azure authentication failures**: Verify service principal credentials
2. **VM start failures**: Check Azure resource availability and permissions
3. **Heartbeat timeouts**: Validate network connectivity and client implementation
4. **Credit calculation errors**: Verify pricing configuration and billing service integration

### Debugging
- Enable DEBUG logging for VM services
- Monitor Azure activity logs
- Check database query performance
- Validate heartbeat submission patterns

---

**Implementation Status**: Complete VM session management system with Azure integration, real-time monitoring, and workspace-based credit billing.

**Last Updated**: December 2024
**Version**: 1.0.0
