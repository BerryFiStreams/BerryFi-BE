# Project Controller API Documentation

## Overview
The Project Controller provides endpoints for managing projects, including creation, retrieval, sharing, and credit allocation. All endpoints require authentication and appropriate role-based permissions.

**Base URL:** `/api/projects`

**Authentication:** Bearer Token (JWT)

**Content-Type:** `application/json`

---

## Endpoints

### 1. Get All Projects

**Endpoint:** `GET /api/projects`

**Description:** Retrieve all projects for the authenticated user's organization with pagination and sorting.

**Parameters:**
- `page` (optional, default: 0) - Page number
- `size` (optional, default: 10) - Page size
- `sortBy` (optional, default: "createdAt") - Sort field
- `sortDir` (optional, default: "desc") - Sort direction (asc/desc)

**Request Headers:**
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

**Request Example:**
```http
GET /api/projects?page=0&size=5&sortBy=name&sortDir=asc
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response Example:**
```json
{
  "content": [
    {
      "id": "proj_abc123def456",
      "name": "ML Training Platform",
      "description": "Machine learning model training environment",
      "status": "RUNNING",
      "accountType": "PREMIUM",
      "organizationId": "org_xyz789",
      "createdBy": "user_123abc",
      "createdAt": "2025-10-01T10:00:00",
      "updatedAt": "2025-10-05T08:30:00",
      "deploymentUrl": "https://proj_abc123def456.berryfi.app",
      "lastDeployed": "2025-10-01T10:15:00",
      "allocatedCredits": 500.0,
      "remainingCredits": 245.5,
      "accessType": "OWNED",
      "sharedBy": null,
      "totalCreditsUsed": 254.5,
      "sessionsCount": 12,
      "uptime": 48.5
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 5,
    "sort": {
      "sorted": true,
      "ascending": true
    }
  },
  "totalElements": 15,
  "totalPages": 3,
  "first": true,
  "last": false,
  "numberOfElements": 5
}
```

---

### 2. Get Project by ID

**Endpoint:** `GET /api/projects/{projectId}`

**Description:** Retrieve a specific project by its ID.

**Path Parameters:**
- `projectId` (required) - The project ID

**Request Example:**
```http
GET /api/projects/proj_abc123def456
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response Example:**
```json
{
  "id": "proj_abc123def456",
  "name": "ML Training Platform",
  "description": "Machine learning model training environment",
  "status": "RUNNING",
  "accountType": "PREMIUM",
  "organizationId": "org_xyz789",
  "createdBy": "user_123abc",
  "createdAt": "2025-10-01T10:00:00",
  "updatedAt": "2025-10-05T08:30:00",
  "deploymentUrl": "https://proj_abc123def456.berryfi.app",
  "lastDeployed": "2025-10-01T10:15:00",
  "allocatedCredits": 500.0,
  "remainingCredits": 245.5,
  "config": {
    "vmSize": "Standard_D2s_v3",
    "region": "eastus",
    "autoShutdown": true
  },
  "branding": {
    "logoUrl": "https://example.com/logo.png",
    "primaryColor": "#FF5722"
  },
  "links": {
    "documentation": "https://docs.example.com",
    "support": "https://support.example.com"
  }
}
```

---

### 3. Create Project

**Endpoint:** `POST /api/projects`

**Description:** Create a new project. Organization ID is automatically extracted from the authenticated user.

**Required Role:** `ORG_OWNER`, `ORG_ADMIN`, or `SUPER_ADMIN`

**Request Body:**
```json
{
  "name": "Web Analytics Dashboard",
  "description": "Real-time web analytics and reporting platform",
  "accountType": "STANDARD",
  "config": {
    "vmSize": "Standard_B2s",
    "region": "westus2",
    "autoShutdown": true,
    "shutdownTime": "22:00"
  },
  "branding": {
    "logoUrl": "https://company.com/logo.png",
    "primaryColor": "#2196F3",
    "secondaryColor": "#FFC107"
  },
  "links": {
    "documentation": "https://docs.company.com/analytics",
    "support": "https://support.company.com",
    "repository": "https://github.com/company/analytics"
  }
}
```

**Response Example:**
```json
{
  "id": "proj_def456ghi789",
  "name": "Web Analytics Dashboard",
  "description": "Real-time web analytics and reporting platform",
  "status": "CREATED",
  "accountType": "STANDARD",
  "organizationId": "org_xyz789",
  "createdBy": "user_123abc",
  "createdAt": "2025-10-05T10:00:00",
  "updatedAt": "2025-10-05T10:00:00",
  "deploymentUrl": null,
  "lastDeployed": null,
  "allocatedCredits": 0.0,
  "remainingCredits": 0.0,
  "config": {
    "vmSize": "Standard_B2s",
    "region": "westus2",
    "autoShutdown": true,
    "shutdownTime": "22:00"
  },
  "branding": {
    "logoUrl": "https://company.com/logo.png",
    "primaryColor": "#2196F3",
    "secondaryColor": "#FFC107"
  },
  "links": {
    "documentation": "https://docs.company.com/analytics",
    "support": "https://support.company.com",
    "repository": "https://github.com/company/analytics"
  }
}
```

---

### 4. Update Project

**Endpoint:** `PUT /api/projects/{projectId}`

**Description:** Update an existing project.

**Path Parameters:**
- `projectId` (required) - The project ID

**Request Body:**
```json
{
  "name": "Updated ML Training Platform",
  "description": "Enhanced machine learning model training environment with GPU support",
  "config": {
    "vmSize": "Standard_NC6s_v3",
    "region": "eastus",
    "autoShutdown": true,
    "shutdownTime": "23:00"
  },
  "branding": {
    "logoUrl": "https://company.com/new-logo.png",
    "primaryColor": "#FF5722"
  }
}
```

**Response:** Same as Get Project by ID response with updated values.

---

### 5. Delete Project

**Endpoint:** `DELETE /api/projects/{projectId}`

**Description:** Delete a project (soft delete - changes status to DELETED).

**Path Parameters:**
- `projectId` (required) - The project ID

**Request Example:**
```http
DELETE /api/projects/proj_abc123def456
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response:**
```
HTTP 204 No Content
```

---

### 6. Deploy Project

**Endpoint:** `POST /api/projects/{projectId}/deploy`

**Description:** Deploy a project to production environment.

**Path Parameters:**
- `projectId` (required) - The project ID

**Request Example:**
```http
POST /api/projects/proj_abc123def456/deploy
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response Example:**
```json
{
  "id": "proj_abc123def456",
  "name": "ML Training Platform",
  "status": "DEPLOYING",
  "deploymentUrl": null,
  "message": "Deployment started successfully"
}
```

---

### 7. Share Project

**Endpoint:** `POST /api/projects/{projectId}/share`

**Description:** Share a project with another organization with credit allocation. Only organization admins can share projects.

**Required Role:** `ORG_OWNER`, `ORG_ADMIN`, or `SUPER_ADMIN`

**Path Parameters:**
- `projectId` (required) - The project ID

**Request Body (Option 1 - Share with Organization ID):**
```json
{
  "organizationId": "org_target123",
  "initialCredits": 100.0,
  "monthlyRecurringCredits": 25.0,
  "canViewAnalytics": true,
  "canManageSessions": true,
  "canShareFurther": false,
  "isPermanent": false,
  "shareMessage": "Shared for collaboration on ML project"
}
```

**Request Body (Option 2 - Share with User Email):**
```json
{
  "userEmail": "admin@targetcompany.com",
  "initialCredits": 50.0,
  "monthlyRecurringCredits": 10.0,
  "canViewAnalytics": true,
  "canManageSessions": false,
  "canShareFurther": true,
  "isPermanent": true,
  "shareMessage": "Welcome to our shared analytics platform!"
}
```

**Response:**
```
HTTP 200 OK
```

**Error Response (Insufficient Credits):**
```json
{
  "error": "Insufficient credits. Available: 75.5, Required: 100.0",
  "timestamp": "2025-10-05T10:15:00",
  "status": 400
}
```

**Error Response (Permission Denied):**
```json
{
  "error": "Only organization admins can share projects. Your role: Organization Member",
  "timestamp": "2025-10-05T10:15:00",
  "status": 403
}
```

**Error Response (User Email Not Found):**
```json
{
  "error": "User not found with email: nonexistent@example.com",
  "timestamp": "2025-10-05T10:15:00",
  "status": 404
}
```

**Error Response (User Has No Organization):**
```json
{
  "error": "User does not belong to any organization: user@example.com",
  "timestamp": "2025-10-05T10:15:00",
  "status": 400
}
```

**Error Response (Organization Not Found):**
```json
{
  "error": "Target organization not found: org_nonexistent123",
  "timestamp": "2025-10-05T10:15:00",
  "status": 404
}
```

**Error Response (Project Already Shared):**
```json
{
  "error": "Project is already shared with this organization",
  "timestamp": "2025-10-05T10:15:00",
  "status": 400
}
```

**Error Response (Missing Share Target):**
```json
{
  "error": "Either organizationId or userEmail must be provided",
  "timestamp": "2025-10-05T10:15:00",
  "status": 400
}
```

---

### 8. Unshare Project

**Endpoint:** `DELETE /api/projects/{projectId}/share/{organizationId}`

**Description:** Remove sharing access for a specific organization. Only organization admins can unshare projects.

**Required Role:** `ORG_OWNER`, `ORG_ADMIN`, or `SUPER_ADMIN`

**Path Parameters:**
- `projectId` (required) - The project ID
- `organizationId` (required) - The organization ID to unshare from

**Request Example:**
```http
DELETE /api/projects/proj_abc123def456/share/org_target123
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response:**
```
HTTP 200 OK
```

---

### 9. Allocate Credits

**Endpoint:** `POST /api/projects/{projectId}/credits`

**Description:** Allocate credits to a specific project.

**Path Parameters:**
- `projectId` (required) - The project ID

**Request Body:**
```json
{
  "credits": 250.0
}
```

**Response Example:**
```json
{
  "id": "proj_abc123def456",
  "name": "ML Training Platform",
  "allocatedCredits": 750.0,
  "remainingCredits": 495.5,
  "message": "Successfully allocated 250.0 credits to project"
}
```

---

### 10. Get Shared Project Usage

**Endpoint:** `GET /api/projects/shared-usage`

**Description:** Get all projects shared by the current organization with detailed usage statistics. Returns both direct shares (projects owned by this org) and indirect shares (projects reshared by this org).

**Required Role:** `ORG_OWNER`, `ORG_ADMIN`, or `SUPER_ADMIN`

**Request Example:**
```http
GET /api/projects/shared-usage
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response Example:**
```json
[
  {
    "projectId": "proj_abc123def456",
    "projectName": "ML Training Platform",
    "sharedWithOrganizationId": "org_datasci789",
    "sharedWithOrganizationName": "DataScience Corp",
    "adminEmail": "admin@datasciencecorp.com",
    "shareType": "DIRECT",
    "sharedAt": "2025-10-01T10:00:00",
    "creditsAllocated": 100.0,
    "creditsUsed": 45.5,
    "creditsRemaining": 54.5,
    "monthlyRecurringCredits": 25.0,
    "totalSessions": 12,
    "lastUsed": "2025-10-05T08:30:00",
    "status": "ACCEPTED"
  },
  {
    "projectId": "proj_def456ghi789",
    "projectName": "Web Analytics Dashboard",
    "sharedWithOrganizationId": "org_marketing456",
    "sharedWithOrganizationName": "Marketing Solutions Inc",
    "adminEmail": "admin@marketingsolutions.com",
    "shareType": "INDIRECT",
    "sharedAt": "2025-09-28T14:00:00",
    "creditsAllocated": 50.0,
    "creditsUsed": 12.3,
    "creditsRemaining": 37.7,
    "monthlyRecurringCredits": 10.0,
    "totalSessions": 5,
    "lastUsed": "2025-10-04T16:45:00",
    "status": "ACCEPTED"
  }
]
```

---

## Share Project Error Scenarios

### What Happens When User Email or Organization ID Doesn't Exist?

The system performs several validation checks when sharing a project:

#### 1. **User Email Validation (when using `userEmail`)**
```json
{
  "userEmail": "nonexistent@example.com",
  "initialCredits": 50.0
}
```

**Process:**
1. System searches for user by email in the database
2. If user is **not found**: Returns `404 Not Found` error
3. If user is **found** but has no organization: Returns `400 Bad Request` error
4. If user is **found** and has organization: Proceeds to organization validation

**Error Responses:**
- **User Not Found:** `404` - "User not found with email: nonexistent@example.com"
- **User Has No Organization:** `400` - "User does not belong to any organization: user@example.com"

#### 2. **Organization ID Validation (when using `organizationId`)**
```json
{
  "organizationId": "org_nonexistent123",
  "initialCredits": 50.0
}
```

**Process:**
1. System checks if organization exists in the database
2. If organization is **not found**: Returns `404 Not Found` error
3. If organization is **found**: Proceeds to sharing validation

**Error Response:**
- **Organization Not Found:** `404` - "Target organization not found: org_nonexistent123"

#### 3. **Additional Validation Checks**

After resolving the target organization, the system performs these checks:

- **Already Shared Check:** Prevents duplicate sharing
  - Error: `400` - "Project is already shared with this organization"

- **Credit Validation:** Ensures sharing organization has enough credits
  - Error: `400` - "Insufficient credits. Available: 75.5, Required: 100.0"

- **Permission Check:** Ensures only admins can share
  - Error: `403` - "Only organization admins can share projects. Your role: Organization Member"

#### 4. **Validation Flow Diagram**

```
Share Request → Validate User Role → Resolve Target Organization → Check Organization Exists → Check Not Already Shared → Validate Credits → Create Share
     ↓                ↓                        ↓                         ↓                      ↓                    ↓             ↓
   403 Error      403 Error              404/400 Error              404 Error            400 Error           400 Error      Success
```

#### 5. **Best Practices for Error Handling**

**Frontend Implementation:**
```javascript
try {
  const response = await fetch('/api/projects/proj_123/share', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      userEmail: 'user@example.com',
      initialCredits: 100.0
    })
  });
  
  if (!response.ok) {
    const error = await response.json();
    
    switch (response.status) {
      case 404:
        if (error.error.includes('User not found')) {
          showError('The email address you entered is not registered in our system.');
        } else if (error.error.includes('organization not found')) {
          showError('The target organization no longer exists.');
        }
        break;
      case 400:
        if (error.error.includes('does not belong to any organization')) {
          showError('This user is not associated with any organization.');
        } else if (error.error.includes('already shared')) {
          showError('This project is already shared with that organization.');
        } else if (error.error.includes('Insufficient credits')) {
          showError('You do not have enough credits to share this project.');
        }
        break;
      case 403:
        showError('You need admin permissions to share projects.');
        break;
      default:
        showError('An unexpected error occurred. Please try again.');
    }
  } else {
    showSuccess('Project shared successfully!');
  }
} catch (error) {
  showError('Network error. Please check your connection.');
}
```

---

## Data Models

### ProjectSummary
```json
{
  "id": "string",
  "name": "string",
  "description": "string",
  "status": "CREATED|DEPLOYING|RUNNING|STOPPED|ERROR|DELETED",
  "accountType": "FREE|STANDARD|PREMIUM|ENTERPRISE",
  "organizationId": "string",
  "createdBy": "string",
  "createdAt": "datetime",
  "updatedAt": "datetime",
  "deploymentUrl": "string",
  "lastDeployed": "datetime",
  "allocatedCredits": "number",
  "remainingCredits": "number",
  "accessType": "OWNED|SHARED",
  "sharedBy": "string",
  "totalCreditsUsed": "number",
  "sessionsCount": "number",
  "uptime": "number"
}
```

### ProjectResponse
```json
{
  "id": "string",
  "name": "string",
  "description": "string",
  "status": "string",
  "accountType": "string",
  "organizationId": "string",
  "createdBy": "string",
  "createdAt": "datetime",
  "updatedAt": "datetime",
  "deploymentUrl": "string",
  "lastDeployed": "datetime",
  "allocatedCredits": "number",
  "remainingCredits": "number",
  "config": "object",
  "branding": "object",
  "links": "object"
}
```

### ShareProjectRequest
```json
{
  "organizationId": "string (optional)",
  "userEmail": "string (optional)",
  "initialCredits": "number (default: 0.0)",
  "monthlyRecurringCredits": "number (default: 0.0)",
  "canViewAnalytics": "boolean (default: true)",
  "canManageSessions": "boolean (default: true)",
  "canShareFurther": "boolean (default: false)",
  "isPermanent": "boolean (default: false)",
  "shareMessage": "string (optional)"
}
```

### SharedProjectUsageResponse
```json
{
  "projectId": "string",
  "projectName": "string",
  "sharedWithOrganizationId": "string",
  "sharedWithOrganizationName": "string",
  "adminEmail": "string",
  "shareType": "DIRECT|INDIRECT",
  "sharedAt": "datetime",
  "creditsAllocated": "number",
  "creditsUsed": "number",
  "creditsRemaining": "number",
  "monthlyRecurringCredits": "number",
  "totalSessions": "number",
  "lastUsed": "datetime",
  "status": "string"
}
```

---

## Error Responses

### Common Error Response Format
```json
{
  "error": "Error message description",
  "timestamp": "2025-10-05T10:15:00",
  "status": 400,
  "path": "/api/projects/endpoint"
}
```

### HTTP Status Codes
- `200 OK` - Request successful
- `201 Created` - Resource created successfully
- `204 No Content` - Request successful, no response body
- `400 Bad Request` - Invalid request data
- `401 Unauthorized` - Authentication required
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `409 Conflict` - Resource already exists
- `500 Internal Server Error` - Server error

---

## Authentication & Authorization

### Required Headers
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

### Role-Based Access Control

**Project Management:**
- `SUPER_ADMIN` - Full access to all operations
- `ORG_OWNER` - Full access within organization
- `ORG_ADMIN` - Full access within organization
- `ORG_MEMBER` - Read-only access to organization projects

**Project Sharing:**
- Only `SUPER_ADMIN`, `ORG_OWNER`, and `ORG_ADMIN` can share/unshare projects
- Regular members cannot share projects or allocate credits

**Credit Operations:**
- Only `SUPER_ADMIN`, `ORG_OWNER`, `ORG_ADMIN`, and `ORG_BILLING` can allocate credits

---

## Rate Limiting
- 100 requests per minute per user
- 1000 requests per hour per organization

## Notes
- All datetime fields are in ISO 8601 format
- Credits are represented as decimal numbers (Double)
- Project IDs follow the pattern: `proj_<12_char_alphanumeric>`
- Organization IDs follow the pattern: `org_<12_char_alphanumeric>`
- User IDs follow the pattern: `user_<8_char_alphanumeric>`