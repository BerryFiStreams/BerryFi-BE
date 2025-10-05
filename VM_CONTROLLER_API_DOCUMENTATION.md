# VM Controller API Documentation

This document provides comprehensive documentation for the VM management system in BerryFi Portal, covering both VM session management (`VmController`) and VM instance administration (`VmInstanceController`).

## Table of Contents

1. [Overview](#overview)
2. [Authentication](#authentication)
3. [VM Session Management (VmController)](#vm-session-management-vmcontroller)
4. [VM Instance Administration (VmInstanceController)](#vm-instance-administration-vminstancecontroller)
5. [Data Models](#data-models)
6. [Error Handling](#error-handling)
7. [Usage Examples](#usage-examples)

## Overview

The VM management system consists of two main controllers:

- **VmController** (`/api/vm`): Handles VM session lifecycle management, available to all authenticated users
- **VmInstanceController** (`/api/admin/vm-instances`): Handles VM instance administration, restricted to SUPER_ADMIN users only

### VM Session Lifecycle

1. **Start Session**: User starts a VM session for a project
2. **Heartbeat**: Regular heartbeat signals to maintain session
3. **Monitor**: Track session status, usage, and metrics
4. **Stop**: User stops the session or admin terminates it
5. **Cleanup**: Session cleanup and resource deallocation

## Authentication

### VM Session Endpoints (VmController)
- **Authentication**: Optional for some endpoints (open access for demos)
- **Authorization**: Basic user authentication where required

### VM Instance Administration (VmInstanceController)
- **Authentication**: Required (JWT token)
- **Authorization**: SUPER_ADMIN role only
- **Access Control**: All endpoints check for SUPER_ADMIN role before processing

## VM Session Management (VmController)

Base URL: `/api/vm`

### 1. Start VM Session

**Endpoint**: `POST /api/vm/sessions/start`

**Description**: Initiates a new VM session for a project

**Authentication**: Optional (supports anonymous users for demos)

**Request Body**:
```json
{
  "projectId": "string (required)",
  "vmType": "string (required)",
  "firstName": "string (optional)",
  "lastName": "string (optional)", 
  "email": "string (optional)",
  "phone": "string (optional)"
}
```

**Request Example**:
```json
{
  "projectId": "proj_12345",
  "vmType": "standard-4gb",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "phone": "+1234567890"
}
```

**Response**: `200 OK`
```json
{
  "success": true,
  "message": "VM session started successfully",
  "data": {
    "sessionId": "session_67890",
    "vmInstanceId": "vm_12345",
    "status": "STARTING",
    "startedAt": "2024-01-15T10:30:00Z",
    "lastHeartbeat": null,
    "durationSeconds": 0,
    "creditsUsed": 0.0,
    "vmType": "standard-4gb",
    "vmStatus": "STARTING",
    "azureResourceId": "/subscriptions/.../vm-instance",
    "vmIpAddress": "10.0.0.100",
    "vmPort": 3389,
    "connectionUrl": "rdp://10.0.0.100:3389",
    "username": "John Doe",
    "clientIpAddress": "192.168.1.100",
    "clientCountry": "United States",
    "clientCity": "New York",
    "userAgent": "Mozilla/5.0..."
  }
}
```

**Error Responses**:
- `400 Bad Request`: Invalid request parameters
- `500 Internal Server Error`: Server error

### 2. Stop VM Session

**Endpoint**: `POST /api/vm/sessions/{sessionId}/stop`

**Description**: Stops an active VM session

**Authentication**: Optional

**Path Parameters**:
- `sessionId`: String - The session ID to stop

**Request Body** (Optional):
```json
{
  "email": "string (optional)"
}
```

**Response**: `200 OK`
```json
{
  "success": true,
  "message": "VM session stopped successfully",
  "data": {
    "sessionId": "session_67890",
    "status": "STOPPED",
    "durationSeconds": 3600,
    "creditsUsed": 1.5
  }
}
```

### 3. Submit Heartbeat

**Endpoint**: `POST /api/vm/sessions/{sessionId}/heartbeat`

**Description**: Submits a heartbeat signal to maintain session

**Authentication**: Optional

**Path Parameters**:
- `sessionId`: String - The session ID

**Request Body**:
```json
{
  "status": "string (required)",
  "cpuUsage": "number (optional)",
  "memoryUsage": "number (optional)"
}
```

**Request Example**:
```json
{
  "status": "ACTIVE",
  "cpuUsage": 45.5,
  "memoryUsage": 62.3
}
```

**Response**: `200 OK`
```json
{
  "success": true,
  "message": "Heartbeat recorded successfully",
  "data": null
}
```

### 4. Get Session Status

**Endpoint**: `GET /api/vm/sessions/{sessionId}`

**Description**: Retrieves current session status and details

**Authentication**: Optional

**Path Parameters**:
- `sessionId`: String - The session ID

**Response**: `200 OK`
```json
{
  "success": true,
  "message": "Session retrieved successfully",
  "data": {
    "sessionId": "session_67890",
    "vmInstanceId": "vm_12345",
    "status": "ACTIVE",
    "startedAt": "2024-01-15T10:30:00Z",
    "lastHeartbeat": "2024-01-15T11:25:00Z",
    "durationSeconds": 3300,
    "creditsUsed": 1.375,
    "vmType": "standard-4gb",
    "vmIpAddress": "10.0.0.100",
    "vmPort": 3389,
    "connectionUrl": "rdp://10.0.0.100:3389"
  }
}
```

### 5. Get Active Session

**Endpoint**: `GET /api/vm/sessions/active`

**Description**: Retrieves user's current active session

**Authentication**: Optional

**Query Parameters**:
- `email`: String - User email (required)

**Response**: `200 OK`
```json
{
  "success": true,
  "message": "Active session retrieved",
  "data": {
    "sessionId": "session_67890",
    "status": "ACTIVE",
    "startedAt": "2024-01-15T10:30:00Z",
    "durationSeconds": 3300
  }
}
```

### 6. Force Terminate Session (Admin)

**Endpoint**: `POST /api/vm/sessions/{sessionId}/terminate`

**Description**: Force terminates a session (admin operation)

**Authentication**: Optional (but intended for admin use)

**Path Parameters**:
- `sessionId`: String - The session ID

**Request Body**:
```json
{
  "reason": "string (required)"
}
```

**Request Example**:
```json
{
  "reason": "Maintenance required"
}
```

**Response**: `200 OK`
```json
{
  "success": true,
  "message": "Session terminated successfully",
  "data": {
    "sessionId": "session_67890",
    "status": "TERMINATED",
    "durationSeconds": 3600
  }
}
```

## VM Instance Administration (VmInstanceController)

Base URL: `/api/admin/vm-instances`

**⚠️ IMPORTANT**: All endpoints require SUPER_ADMIN role. Non-admin users will receive `403 Forbidden` responses.

### 1. Get All VM Instances

**Endpoint**: `GET /api/admin/vm-instances`

**Description**: Retrieves paginated list of all VM instances across all projects

**Authentication**: Required (JWT token)

**Authorization**: SUPER_ADMIN role only

**Query Parameters**:
- `page`: Integer - Page number (default: 0)
- `size`: Integer - Page size (default: 10)

**Response**: `200 OK`
```json
{
  "success": true,
  "message": "VM instances retrieved successfully",
  "data": {
    "content": [
      {
        "id": "vm_12345",
        "vmName": "BerryFi-Dev-VM-001",
        "vmType": "standard-4gb",
        "status": "AVAILABLE",
        "projectId": "proj_12345",
        "createdAt": "2024-01-15T10:30:00Z"
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10
    },
    "totalElements": 1,
    "totalPages": 1
  }
}
```

**Error Responses**:
- `403 Forbidden`: Non-admin user attempting access
- `500 Internal Server Error`: Server error

### 2. Create VM Instance

**Endpoint**: `POST /api/admin/vm-instances`

**Description**: Creates a new VM instance with Azure credentials

**Authentication**: Required (JWT token)

**Authorization**: SUPER_ADMIN role only

**Request Body**:
```json
{
  "vmName": "string (required)",
  "vmType": "string (required)",
  "azureResourceId": "string (required)",
  "azureResourceGroup": "string (required)",
  "azureSubscriptionId": "string (required)",
  "azureTenantId": "string (required)",
  "azureClientId": "string (required)",
  "azureClientSecret": "string (required)",
  "projectId": "string (required)",
  "azureRegion": "string (optional)",
  "description": "string (optional)"
}
```

**Request Example**:
```json
{
  "vmName": "BerryFi-Dev-VM-001",
  "vmType": "standard-4gb",
  "azureResourceId": "/subscriptions/12345678-1234-1234-1234-123456789012/resourceGroups/berryfi-vms/providers/Microsoft.Compute/virtualMachines/vm-001",
  "azureResourceGroup": "berryfi-vms",
  "azureSubscriptionId": "12345678-1234-1234-1234-123456789012",
  "azureTenantId": "87654321-4321-4321-4321-210987654321",
  "azureClientId": "11111111-1111-1111-1111-111111111111",
  "azureClientSecret": "your-azure-client-secret",
  "projectId": "proj_12345",
  "azureRegion": "East US",
  "description": "Development VM for BerryFi project"
}
```

**Response**: `200 OK`
```json
{
  "success": true,
  "message": "VM instance created successfully",
  "data": {
    "id": "vm_12345",
    "vmName": "BerryFi-Dev-VM-001",
    "vmType": "standard-4gb",
    "status": "AVAILABLE",
    "projectId": "proj_12345",
    "azureResourceId": "/subscriptions/.../vm-001",
    "azureResourceGroup": "berryfi-vms",
    "azureRegion": "East US",
    "description": "Development VM for BerryFi project",
    "createdAt": "2024-01-15T10:30:00Z"
  }
}
```

**Error Responses**:
- `403 Forbidden`: Non-admin user attempting access
- `400 Bad Request`: Invalid request parameters
- `500 Internal Server Error`: Server error

### 3. Update VM Credentials

**Endpoint**: `PUT /api/admin/vm-instances/{vmInstanceId}/credentials`

**Description**: Updates Azure credentials for a VM instance

**Authentication**: Required (JWT token)

**Authorization**: SUPER_ADMIN role only

**Path Parameters**:
- `vmInstanceId`: String - The VM instance ID

**Request Body**:
```json
{
  "azureSubscriptionId": "string (optional)",
  "azureTenantId": "string (optional)",
  "azureClientId": "string (optional)",
  "azureClientSecret": "string (optional)",
  "azureResourceGroup": "string (optional)"
}
```

**Response**: `200 OK`
```json
{
  "success": true,
  "message": "VM credentials updated successfully", 
  "data": {
    "id": "vm_12345",
    "vmName": "BerryFi-Dev-VM-001",
    "updatedAt": "2024-01-15T11:30:00Z"
  }
}
```

### 4. Get VM Instance Details

**Endpoint**: `GET /api/admin/vm-instances/{vmInstanceId}`

**Description**: Retrieves detailed VM instance information

**Authentication**: Required (JWT token)

**Authorization**: SUPER_ADMIN role only

**Path Parameters**:
- `vmInstanceId`: String - The VM instance ID

**Response**: `200 OK`
```json
{
  "success": true,
  "message": "VM instance retrieved successfully",
  "data": {
    "id": "vm_12345",
    "vmName": "BerryFi-Dev-VM-001",
    "vmType": "standard-4gb",
    "status": "AVAILABLE",
    "projectId": "proj_12345",
    "azureResourceId": "/subscriptions/.../vm-001",
    "azureResourceGroup": "berryfi-vms",
    "azureSubscriptionId": "12345678-1234-1234-1234-123456789012",
    "azureTenantId": "87654321-4321-4321-4321-210987654321",
    "azureClientId": "11111111-1111-1111-1111-111111111111",
    "azureClientSecret": "***",
    "azureRegion": "East US",
    "description": "Development VM for BerryFi project",
    "createdAt": "2024-01-15T10:30:00Z"
  }
}
```

### 5. Get VM Instances by Project

**Endpoint**: `GET /api/admin/vm-instances/project/{projectId}`

**Description**: Retrieves paginated list of VM instances for a project

**Authentication**: Required (JWT token)

**Authorization**: SUPER_ADMIN role only

**Path Parameters**:
- `projectId`: String - The project ID

**Query Parameters**:
- `page`: Integer - Page number (default: 0)
- `size`: Integer - Page size (default: 10)

**Response**: `200 OK`
```json
{
  "success": true,
  "message": "VM instances retrieved successfully",
  "data": {
    "content": [
      {
        "id": "vm_12345",
        "vmName": "BerryFi-Dev-VM-001", 
        "vmType": "standard-4gb",
        "status": "AVAILABLE",
        "createdAt": "2024-01-15T10:30:00Z"
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10
    },
    "totalElements": 1,
    "totalPages": 1
  }
}
```

### 6. Get Available VM Instances

**Endpoint**: `GET /api/admin/vm-instances/project/{projectId}/available`

**Description**: Retrieves available VM instances for a project

**Authentication**: Required (JWT token)

**Authorization**: SUPER_ADMIN role only

**Path Parameters**:
- `projectId`: String - The project ID

**Query Parameters**:
- `vmType`: String - Filter by VM type (optional)

**Response**: `200 OK`
```json
{
  "success": true,
  "message": "Available VM instances retrieved",
  "data": [
    {
      "id": "vm_12345",
      "vmName": "BerryFi-Dev-VM-001",
      "vmType": "standard-4gb",
      "status": "AVAILABLE",
      "azureResourceId": "/subscriptions/.../vm-001"
    }
  ]
}
```

### 7. Delete VM Instance

**Endpoint**: `DELETE /api/admin/vm-instances/{vmInstanceId}`

**Description**: Deletes a VM instance (only if not in use)

**Authentication**: Required (JWT token)

**Authorization**: SUPER_ADMIN role only

**Path Parameters**:
- `vmInstanceId`: String - The VM instance ID

**Response**: `200 OK`
```json
{
  "success": true,
  "message": "VM instance deleted successfully",
  "data": null
}
```

**Error Responses**:
- `400 Bad Request`: VM instance is currently in use
- `404 Not Found`: VM instance not found

### 8. Test Azure VM Configuration

**Endpoint**: `POST /api/admin/vm-instances/test-configuration`

**Description**: Tests Azure credentials and VM accessibility to validate configuration before creating VM instances

**Authentication**: Required (JWT token)

**Authorization**: SUPER_ADMIN role only

**Request Body**:
```json
{
  "azureResourceId": "string (required)",
  "azureResourceGroup": "string (required)",
  "azureSubscriptionId": "string (required)",
  "azureTenantId": "string (required)",
  "azureClientId": "string (required)",
  "azureClientSecret": "string (required)"
}
```

**Request Example**:
```json
{
  "azureResourceId": "/subscriptions/12345678-1234-1234-1234-123456789012/resourceGroups/berryfi-vms/providers/Microsoft.Compute/virtualMachines/vm-001",
  "azureResourceGroup": "berryfi-vms",
  "azureSubscriptionId": "12345678-1234-1234-1234-123456789012",
  "azureTenantId": "87654321-4321-4321-4321-210987654321",
  "azureClientId": "11111111-1111-1111-1111-111111111111",
  "azureClientSecret": "your-azure-client-secret"
}
```

**Response**: `200 OK` (Success)
```json
{
  "success": true,
  "message": "Azure configuration test completed successfully",
  "data": {
    "configurationValid": true,
    "credentialsValid": true,
    "vmAccessible": true,
    "vmStatus": "PowerState/running",
    "vmSize": "Standard_B2s",
    "ipAddress": "10.0.0.100",
    "testTimestamp": "2024-01-15T10:30:00Z"
  }
}
```

**Response**: `200 OK` (Failed)
```json
{
  "success": true,
  "message": "Azure configuration test completed with issues",
  "data": {
    "configurationValid": false,
    "credentialsValid": false,
    "vmAccessible": false,
    "errorMessage": "Authentication failed: Invalid client credentials",
    "testTimestamp": "2024-01-15T10:30:00Z"
  }
}
```

**Error Responses**:
- `403 Forbidden`: Non-admin user attempting access
- `400 Bad Request`: Invalid request parameters
- `500 Internal Server Error`: Server error

**Test Results Explanation**:
- `configurationValid`: Overall test result (true if both credentials and VM are valid)
- `credentialsValid`: Whether the Azure service principal credentials are valid
- `vmAccessible`: Whether the specified VM can be found and accessed
- `vmStatus`: Current power state of the VM in Azure
- `vmSize`: VM size/type (e.g., Standard_B2s, Standard_D2s_v3)
- `ipAddress`: Current IP address of the VM (if available)
- `errorMessage`: Detailed error message if any test fails

## Data Models

### VmSession

```typescript
interface VmSession {
  id: string;
  vmInstanceId: string;
  userId: string;
  userEmail: string;
  username: string;
  projectId: string;
  status: SessionStatus;
  startTime: string;
  endTime?: string;
  lastHeartbeat?: string;
  durationInSeconds: number;
  creditsUsed: number;
  vmIpAddress?: string;
  vmPort?: number;
  connectionUrl?: string;
  clientIpAddress?: string;
  clientCountry?: string;
  clientCity?: string;
  userAgent?: string;
}
```

### VmInstance

```typescript
interface VmInstance {
  id: string;
  vmName: string;
  vmType: string;
  azureResourceId: string;
  azureResourceGroup: string;
  azureSubscriptionId: string;
  azureTenantId: string;
  azureClientId: string;
  azureClientSecret: string;
  azureRegion?: string;
  projectId: string;
  status: VmStatus;
  description?: string;
  ipAddress?: string;
  port?: number;
  connectionUrl?: string;
  currentSessionId?: string;
  createdAt: string;
  updatedAt: string;
}
```

### Enums

```typescript
enum SessionStatus {
  STARTING = "STARTING",
  ACTIVE = "ACTIVE", 
  STOPPING = "STOPPING",
  STOPPED = "STOPPED",
  TERMINATED = "TERMINATED",
  ERROR = "ERROR"
}

enum VmStatus {
  AVAILABLE = "AVAILABLE",
  IN_USE = "IN_USE",
  MAINTENANCE = "MAINTENANCE",
  ERROR = "ERROR"
}
```

### ApiResponse

```typescript
interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T | null;
  timestamp?: string;
}
```

## Error Handling

### Common Error Responses

**403 Forbidden** (Admin endpoints only):
```json
{
  "success": false,
  "message": "Access denied. Only system administrators can create VM instances.",
  "data": null
}
```

**400 Bad Request**:
```json
{
  "success": false,
  "message": "VM name is required",
  "data": null
}
```

**404 Not Found**:
```json
{
  "success": false,
  "message": "VM instance not found",
  "data": null
}
```

**500 Internal Server Error**:
```json
{
  "success": false,
  "message": "Failed to create VM instance: Database connection error",
  "data": null
}
```

## Usage Examples

### Starting a VM Session (JavaScript/Frontend)

```javascript
// Start a VM session
const startSession = async (projectId, vmType, userInfo) => {
  try {
    const response = await fetch('/api/vm/sessions/start', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        projectId,
        vmType,
        firstName: userInfo.firstName,
        lastName: userInfo.lastName,
        email: userInfo.email
      })
    });
    
    const result = await response.json();
    
    if (result.success) {
      console.log('Session started:', result.data.sessionId);
      return result.data;
    } else {
      throw new Error(result.message);
    }
  } catch (error) {
    console.error('Failed to start session:', error);
    throw error;
  }
};

// Usage
const sessionData = await startSession('proj_12345', 'standard-4gb', {
  firstName: 'John',
  lastName: 'Doe', 
  email: 'john.doe@example.com'
});
```

### Heartbeat Monitoring

```javascript
// Send heartbeat every 30 seconds
const sendHeartbeat = async (sessionId, status, metrics) => {
  try {
    const response = await fetch(`/api/vm/sessions/${sessionId}/heartbeat`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        status,
        cpuUsage: metrics.cpu,
        memoryUsage: metrics.memory
      })
    });
    
    return await response.json();
  } catch (error) {
    console.error('Heartbeat failed:', error);
    return null;
  }
};

// Heartbeat loop
setInterval(async () => {
  const metrics = await getSystemMetrics(); // Your metrics function
  await sendHeartbeat(sessionId, 'ACTIVE', metrics);
}, 30000);
```

### Admin VM Instance Creation (with JWT)

```javascript
// Create VM instance (admin only)
const createVmInstance = async (vmData, authToken) => {
  try {
    const response = await fetch('/api/admin/vm-instances', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${authToken}`
      },
      body: JSON.stringify(vmData)
    });
    
    const result = await response.json();
    
    if (response.status === 403) {
      throw new Error('Access denied: Admin privileges required');
    }
    
    if (result.success) {
      return result.data;
    } else {
      throw new Error(result.message);
    }
  } catch (error) {
    console.error('Failed to create VM instance:', error);
    throw error;
  }
};

// Usage
const vmData = {
  vmName: 'New-Dev-VM',
  vmType: 'standard-4gb',
  azureResourceId: '/subscriptions/.../vm-new',
  azureResourceGroup: 'dev-vms',
  azureSubscriptionId: 'subscription-id',
  azureTenantId: 'tenant-id',
  azureClientId: 'client-id',
  azureClientSecret: 'client-secret',
  projectId: 'proj_12345',
  description: 'New development VM'
};

const vmInstance = await createVmInstance(vmData, userToken);
```

### Testing Azure VM Configuration (Admin)

```javascript
// Test Azure VM configuration before creating VM instance
const testVmConfiguration = async (configData, authToken) => {
  try {
    const response = await fetch('/api/admin/vm-instances/test-configuration', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${authToken}`
      },
      body: JSON.stringify(configData)
    });
    
    const result = await response.json();
    
    if (response.status === 403) {
      throw new Error('Access denied: Admin privileges required');
    }
    
    if (result.success) {
      const testData = result.data;
      
      if (testData.configurationValid) {
        console.log('✅ Configuration test passed');
        console.log(`VM Status: ${testData.vmStatus}`);
        console.log(`VM Size: ${testData.vmSize}`);
        console.log(`IP Address: ${testData.ipAddress}`);
        return testData;
      } else {
        console.warn('⚠️ Configuration test failed');
        console.warn(`Error: ${testData.errorMessage}`);
        console.warn(`Credentials Valid: ${testData.credentialsValid}`);
        console.warn(`VM Accessible: ${testData.vmAccessible}`);
        return testData;
      }
    } else {
      throw new Error(result.message);
    }
  } catch (error) {
    console.error('Failed to test VM configuration:', error);
    throw error;
  }
};

// Usage - Test configuration before creating VM instance
const configData = {
  azureResourceId: '/subscriptions/12345/resourceGroups/vms/providers/Microsoft.Compute/virtualMachines/vm-001',
  azureResourceGroup: 'berryfi-vms',
  azureSubscriptionId: '12345678-1234-1234-1234-123456789012',
  azureTenantId: '87654321-4321-4321-4321-210987654321',
  azureClientId: '11111111-1111-1111-1111-111111111111',
  azureClientSecret: 'your-azure-client-secret'
};

try {
  const testResult = await testVmConfiguration(configData, userToken);
  
  if (testResult.configurationValid) {
    // Configuration is valid, proceed with VM instance creation
    const vmData = {
      vmName: 'Validated-VM',
      vmType: 'standard-4gb',
      projectId: 'proj_12345',
      description: 'VM created after successful configuration test',
      ...configData
    };
    
    const vmInstance = await createVmInstance(vmData, userToken);
    console.log('VM instance created successfully:', vmInstance.id);
  } else {
    console.error('Cannot create VM instance - configuration test failed');
    // Handle configuration errors (show user-friendly message, etc.)
  }
} catch (error) {
  console.error('Configuration test error:', error);
}
```

### Error Handling Best Practices

```javascript
const handleApiCall = async (apiCall) => {
  try {
    const result = await apiCall();
    return result;
  } catch (error) {
    // Handle different error types
    if (error.status === 403) {
      // Redirect to login or show access denied
      console.error('Access denied');
    } else if (error.status === 404) {
      // Resource not found
      console.error('Resource not found');
    } else if (error.status >= 500) {
      // Server error - retry logic
      console.error('Server error - retrying...');
    }
    
    throw error;
  }
};
```

---

## Security Notes

1. **Admin Endpoints**: All VM instance administration endpoints require SUPER_ADMIN role
2. **Sensitive Data**: Azure client secrets are masked in GET responses
3. **Session Management**: Sessions are tracked by IP and user agent for security
4. **Authorization Logging**: All unauthorized access attempts are logged with user details

## Rate Limiting

- Heartbeat endpoints: 2 requests per second per session
- Session creation: 10 requests per minute per IP
- Admin endpoints: 60 requests per minute per user

## Support

For questions or issues with the VM Controller API, please contact the BerryFi development team or refer to the system logs for detailed error information.