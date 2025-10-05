# Invitation API Documentation

## Overview
The Invitation API handles project invitation management, user registration through invitations, and invitation lifecycle operations.

**Base URL:** `/api/invitations`

---

## 1. Get Invitation Details

### `GET /api/invitations/{token}`

Retrieve detailed information about a project invitation using the invitation token.

#### Parameters

| Parameter | Type   | Location | Required | Description |
|-----------|--------|----------|----------|-------------|
| `token`   | string | path     | Yes      | The unique invitation token |

#### Request Example
```http
GET /api/invitations/inv_a1b2c3d4e5f6g7h8i9j0 HTTP/1.1
Host: api.berryfi.com
```

#### Success Response (200 OK)
```json
{
  "inviteToken": "inv_a1b2c3d4e5f6g7h8i9j0",
  "projectName": "Customer Analytics Dashboard",
  "projectDescription": "Advanced analytics platform for customer behavior tracking and insights",
  "inviterName": "John Smith",
  "inviterEmail": "john.smith@acmecorp.com",
  "inviterOrganization": "ACME Corporation",
  "inviteeEmail": "sarah.johnson@example.com",
  "initialCredits": 1500,
  "monthlyRecurringCredits": 500,
  "shareMessage": "Welcome to our analytics team! This project will help you understand our customer base better.",
  "expiresAt": "2025-01-15T10:30:00",
  "status": "PENDING",
  "createdAt": "2025-01-08T09:15:00"
}
```

#### Error Responses

**404 Not Found**
```json
{
  "timestamp": "2025-01-08T14:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Invitation not found or invalid",
  "path": "/api/invitations/invalid_token"
}
```

**400 Bad Request - Expired Token**
```json
{
  "timestamp": "2025-01-08T14:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "This invitation has expired",
  "path": "/api/invitations/expired_token"
}
```

---

## 2. Register Through Invitation

### `POST /api/invitations/register`

Register a new user and organization using an invitation token. This endpoint creates both a user account and an organization, then automatically shares the project.

#### Request Body
```json
{
  "inviteToken": "inv_a1b2c3d4e5f6g7h8i9j0",
  "userName": "Sarah Johnson",
  "email": "sarah.johnson@newcompany.com",
  "password": "SecurePassword123!",
  "organizationName": "Johnson Analytics Inc",
  "organizationDescription": "Data analytics and business intelligence consulting firm specializing in customer insights and market research"
}
```

#### Request Body Schema

| Field | Type | Required | Constraints | Description |
|-------|------|----------|-------------|-------------|
| `inviteToken` | string | Yes | Valid active token | The invitation token from the email |
| `userName` | string | Yes | 2-100 characters | Full name of the user |
| `email` | string | Yes | Valid email format, unique | User's email address |
| `password` | string | Yes | Min 8 chars, 1 upper, 1 lower, 1 number, 1 special | Strong password |
| `organizationName` | string | Yes | 2-100 characters, unique | Organization name |
| `organizationDescription` | string | No | Max 500 characters | Optional organization description |

#### Request Example
```http
POST /api/invitations/register HTTP/1.1
Host: api.berryfi.com
Content-Type: application/json

{
  "inviteToken": "inv_a1b2c3d4e5f6g7h8i9j0",
  "userName": "Sarah Johnson",
  "email": "sarah.johnson@newcompany.com",
  "password": "SecurePassword123!",
  "organizationName": "Johnson Analytics Inc",
  "organizationDescription": "Data analytics consulting firm"
}
```

#### Success Response (200 OK)
```json
{
  "user": {
    "id": "usr_xyz789abc123",
    "name": "Sarah Johnson",
    "email": "sarah.johnson@newcompany.com",
    "role": "ORG_OWNER",
    "accountType": "ORGANIZATION",
    "organizationId": "org_def456ghi789",
    "status": "ACTIVE",
    "lastLogin": null
  },
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJzYXJhaC5qb2huc29uQG5ld2NvbXBhbnkuY29tIiwidXNlcklkIjoidXNyX3h5ejc4OWFiYzEyMyIsInJvbGUiOiJPUkdfT1dORVIiLCJhY2NvdW50VHlwZSI6Ik9SR0FOSVpBVElPTiIsIm9yZ2FuaXphdGlvbklkIjoib3JnX2RlZjQ1NmdoaTc4OSIsImlhdCI6MTczNTkwNjIwMCwiZXhwIjoxNzM1OTkyNjAwfQ.example_signature",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJzYXJhaC5qb2huc29uQG5ld2NvbXBhbnkuY29tIiwidXNlcklkIjoidXNyX3h5ejc4OWFiYzEyMyIsInRva2VuVHlwZSI6InJlZnJlc2giLCJpYXQiOjE3MzU5MDYyMDAsImV4cCI6MTczNjUxMTAwMH0.example_refresh_signature"
}
```

#### Error Responses

**400 Bad Request - Invalid Token**
```json
{
  "timestamp": "2025-01-08T14:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid or expired invitation token",
  "path": "/api/invitations/register"
}
```

**400 Bad Request - Validation Error**
```json
{
  "timestamp": "2025-01-08T14:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/invitations/register",
  "validationErrors": [
    {
      "field": "password",
      "message": "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character"
    },
    {
      "field": "email",
      "message": "Invalid email format"
    }
  ]
}
```

**409 Conflict - User Already Exists**
```json
{
  "timestamp": "2025-01-08T14:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "A user with this email already exists",
  "path": "/api/invitations/register"
}
```

**409 Conflict - Organization Name Taken**
```json
{
  "timestamp": "2025-01-08T14:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "An organization with this name already exists",
  "path": "/api/invitations/register"
}
```

**409 Conflict - Invitation Already Accepted**
```json
{
  "timestamp": "2025-01-08T14:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "This invitation has already been accepted",
  "path": "/api/invitations/register"
}
```

---

## 3. Accept Invitation (Existing User)

### `POST /api/invitations/{token}/accept`

Accept a project invitation for an existing user. This endpoint creates team membership with ORG_OWNER role in the inviting organization and automatically shares the project.

#### Parameters

| Parameter | Type   | Location | Required | Description |
|-----------|--------|----------|----------|-------------|
| `token`   | string | path     | Yes      | The unique invitation token |

#### Headers
```
Authorization: Bearer <access_token>
```

#### Request Example
```http
POST /api/invitations/inv_a1b2c3d4e5f6g7h8i9j0/accept HTTP/1.1
Host: api.berryfi.com
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### Success Response (200 OK)
```json
{
  "message": "Invitation accepted successfully",
  "status": "ACCEPTED",
  "acceptedAt": "2025-01-08T14:30:00",
  "organizationJoined": "ACME Corporation",
  "roleAssigned": "ORG_OWNER"
}
```

#### Response Details
When an existing user accepts an invitation, the following actions occur:
1. **Team Membership Created**: User becomes an ORG_OWNER in the inviting organization
2. **User Organization Updated**: User's primary organization is changed to the inviting organization
3. **Project Access Granted**: Automatic project sharing with specified credits and permissions
4. **Invitation Status Updated**: Invitation marked as ACCEPTED

#### Error Responses

**401 Unauthorized**
```json
{
  "timestamp": "2025-01-08T14:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Authentication required",
  "path": "/api/invitations/some_token/accept"
}
```

**403 Forbidden - Wrong Email**
```json
{
  "timestamp": "2025-01-08T14:30:00",
  "status": 403,
  "error": "Forbidden",
  "message": "This invitation is not for your email address",
  "path": "/api/invitations/some_token/accept"
}
```

**404 Not Found**
```json
{
  "timestamp": "2025-01-08T14:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Invitation not found or invalid",
  "path": "/api/invitations/invalid_token/accept"
}
```

**409 Conflict - Already Processed**
```json
{
  "timestamp": "2025-01-08T14:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "This invitation has already been processed",
  "path": "/api/invitations/processed_token/accept"
}
```

---

## 4. Decline Invitation

### `POST /api/invitations/{token}/decline`

Decline a project invitation. This action is irreversible and marks the invitation as DECLINED.

#### Parameters

| Parameter | Type   | Location | Required | Description |
|-----------|--------|----------|----------|-------------|
| `token`   | string | path     | Yes      | The unique invitation token |

#### Request Example
```http
POST /api/invitations/inv_a1b2c3d4e5f6g7h8i9j0/decline HTTP/1.1
Host: api.berryfi.com
```

#### Success Response (200 OK)
```json
{
  "message": "Invitation declined successfully",
  "status": "DECLINED",
  "declinedAt": "2025-01-08T14:30:00"
}
```

#### Error Responses

**404 Not Found**
```json
{
  "timestamp": "2025-01-08T14:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Invitation not found",
  "path": "/api/invitations/invalid_token/decline"
}
```

**409 Conflict - Already Processed**
```json
{
  "timestamp": "2025-01-08T14:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "This invitation has already been processed",
  "path": "/api/invitations/processed_token/decline"
}
```

---

## 5. Get Sent Invitations

### `GET /api/invitations/sent`

Retrieve all invitations sent by the authenticated user with optional status filtering and pagination.

#### Parameters

| Parameter | Type             | Location | Required | Default | Description |
|-----------|------------------|----------|----------|---------|-------------|
| `status`  | InvitationStatus | query    | No       | null    | Filter by invitation status |
| `page`    | integer          | query    | No       | 0       | Page number (0-based) |
| `size`    | integer          | query    | No       | 20      | Number of results per page |

#### Headers
```
Authorization: Bearer <access_token>
```

#### Request Example
```http
GET /api/invitations/sent?status=PENDING&page=0&size=10 HTTP/1.1
Host: api.berryfi.com
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### Success Response (200 OK)
```json
{
  "content": [
    {
      "inviteToken": "inv_a1b2c3d4e5f6g7h8i9j0",
      "projectName": "Customer Analytics Dashboard",
      "projectId": "proj_abc123",
      "inviteeEmail": "sarah.johnson@example.com",
      "status": "PENDING",
      "initialCredits": 1500.0,
      "monthlyRecurringCredits": 500.0,
      "shareMessage": "Welcome to our analytics team!",
      "sentAt": "2025-01-08T09:15:00",
      "expiresAt": "2025-01-15T09:15:00",
      "lastActivityAt": "2025-01-08T09:15:00",
      "canResend": true
    },
    {
      "inviteToken": "inv_b2c3d4e5f6g7h8i9j0k1",
      "projectName": "Marketing Dashboard",
      "projectId": "proj_def456",
      "inviteeEmail": "john.doe@company.com",
      "status": "ACCEPTED",
      "initialCredits": 1000.0,
      "monthlyRecurringCredits": 300.0,
      "shareMessage": "Join our marketing insights project!",
      "sentAt": "2025-01-07T14:30:00",
      "expiresAt": "2025-01-14T14:30:00",
      "lastActivityAt": "2025-01-08T10:45:00",
      "canResend": false
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {
      "sorted": true,
      "unsorted": false
    }
  },
  "totalElements": 15,
  "totalPages": 2,
  "first": true,
  "last": false,
  "numberOfElements": 10
}
```

#### Status Filter Options
- `PENDING` - Invitations awaiting response
- `ACCEPTED` - Invitations that were accepted
- `DECLINED` - Invitations that were declined
- `EXPIRED` - Invitations that expired
- `CANCELLED` - Invitations that were cancelled

---

## 6. Get Pending Invitations

### `GET /api/invitations/sent/pending`

Retrieve all pending invitations sent by the authenticated user (useful for bulk resending).

#### Headers
```
Authorization: Bearer <access_token>
```

#### Request Example
```http
GET /api/invitations/sent/pending HTTP/1.1
Host: api.berryfi.com
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### Success Response (200 OK)
```json
[
  {
    "inviteToken": "inv_a1b2c3d4e5f6g7h8i9j0",
    "projectName": "Customer Analytics Dashboard",
    "projectId": "proj_abc123",
    "inviteeEmail": "sarah.johnson@example.com",
    "status": "PENDING",
    "initialCredits": 1500.0,
    "monthlyRecurringCredits": 500.0,
    "shareMessage": "Welcome to our analytics team!",
    "sentAt": "2025-01-08T09:15:00",
    "expiresAt": "2025-01-15T09:15:00",
    "lastActivityAt": "2025-01-08T09:15:00",
    "canResend": true
  },
  {
    "inviteToken": "inv_c3d4e5f6g7h8i9j0k1l2",
    "projectName": "Sales Dashboard",
    "projectId": "proj_ghi789",
    "inviteeEmail": "mike.wilson@startup.com",
    "status": "PENDING",
    "initialCredits": 800.0,
    "monthlyRecurringCredits": 200.0,
    "shareMessage": "Check out our sales analytics!",
    "sentAt": "2025-01-07T16:20:00",
    "expiresAt": "2025-01-14T16:20:00",
    "lastActivityAt": "2025-01-07T16:20:00",
    "canResend": true
  }
]
```

#### Response Details
- Returns up to 100 pending invitations
- Sorted by creation date (newest first)
- Only includes invitations that can be resent (`canResend: true`)
- Useful for displaying a "Resend All" or "Manage Pending" interface

---

## 7. Resend Invitation

### `POST /api/invitations/{token}/resend`

Resend an invitation email for a pending invitation. Only the original inviter can resend invitations.

#### Parameters

| Parameter | Type   | Location | Required | Description |
|-----------|--------|----------|----------|-------------|
| `token`   | string | path     | Yes      | The unique invitation token |

#### Headers
```
Authorization: Bearer <access_token>
```

#### Request Example
```http
POST /api/invitations/inv_a1b2c3d4e5f6g7h8i9j0/resend HTTP/1.1
Host: api.berryfi.com
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### Success Response (200 OK)
```json
{
  "message": "Invitation email resent successfully",
  "resentAt": "2025-01-08T14:30:00",
  "recipientEmail": "sarah.johnson@example.com"
}
```

#### Error Responses

**404 Not Found**
```json
{
  "timestamp": "2025-01-08T14:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Invitation not found",
  "path": "/api/invitations/invalid_token/resend"
}
```

**403 Forbidden**
```json
{
  "timestamp": "2025-01-08T14:30:00",
  "status": 403,
  "error": "Forbidden",
  "message": "You are not authorized to resend this invitation",
  "path": "/api/invitations/some_token/resend"
}
```

**400 Bad Request - Cannot Resend**
```json
{
  "timestamp": "2025-01-08T14:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Cannot resend invitation. Invitation status: ACCEPTED",
  "path": "/api/invitations/accepted_token/resend"
}
```

---

## Data Models

### SentInvitationResponse
```typescript
interface SentInvitationResponse {
  inviteToken: string;              // Unique invitation token
  projectName: string;              // Name of the project being shared
  projectId: string;                // ID of the project
  inviteeEmail: string;             // Email address of the invitee
  status: InvitationStatus;         // Current status of invitation
  initialCredits?: number;          // Initial credits allocated
  monthlyRecurringCredits?: number; // Monthly recurring credits
  shareMessage?: string;            // Optional message from inviter
  sentAt: string;                   // ISO datetime when invitation was sent
  expiresAt: string;                // ISO datetime when invitation expires
  lastActivityAt: string;           // ISO datetime of last activity
  canResend: boolean;               // Whether invitation can be resent
}
```

### InvitationDetailsResponse
```typescript
interface InvitationDetailsResponse {
  inviteToken: string;           // Unique invitation token
  projectName: string;           // Name of the project being shared
  projectDescription?: string;   // Optional project description
  inviterName: string;          // Name of the person who sent invitation
  inviterEmail: string;         // Email of the inviter
  inviterOrganization: string;  // Organization name of the inviter
  inviteeEmail: string;         // Email address of the invitee
  initialCredits?: number;      // Initial credits allocated
  monthlyRecurringCredits?: number; // Monthly recurring credits
  shareMessage?: string;        // Optional message from inviter
  expiresAt: string;           // ISO datetime when invitation expires
  status: InvitationStatus;    // Current status of invitation
  createdAt: string;           // ISO datetime when invitation was created
}
```

### InvitationRegistrationRequest
```typescript
interface InvitationRegistrationRequest {
  inviteToken: string;              // Required: Invitation token
  userName: string;                 // Required: User's full name (2-100 chars)
  email: string;                    // Required: Valid unique email
  password: string;                 // Required: Strong password
  organizationName: string;         // Required: Unique org name (2-100 chars)
  organizationDescription?: string; // Optional: Org description (max 500 chars)
}
```

### AuthResponse
```typescript
interface AuthResponse {
  user: UserDto;        // User information
  accessToken: string;  // JWT access token (24h expiry)
  refreshToken: string; // JWT refresh token (7d expiry)
}
```

### UserDto
```typescript
interface UserDto {
  id: string;                    // Unique user ID
  name: string;                  // User's full name
  email: string;                 // User's email address
  role: Role;                    // User role (ORG_OWNER for invited users)
  accountType: AccountType;      // Account type (ORGANIZATION)
  organizationId: string;        // Associated organization ID
  status: UserStatus;            // User status (ACTIVE)
  lastLogin?: string;            // Last login timestamp (null for new users)
}
```

### InvitationStatus Enum
```typescript
enum InvitationStatus {
  PENDING = "PENDING",       // Invitation sent, awaiting response
  ACCEPTED = "ACCEPTED",     // Invitation accepted, project shared
  DECLINED = "DECLINED",     // Invitation declined by recipient
  EXPIRED = "EXPIRED",       // Invitation expired automatically
  CANCELLED = "CANCELLED"    // Invitation cancelled by sender
}
```

---

## Authentication & Authorization

### Public Endpoints
- `GET /api/invitations/{token}` - No authentication required
- `POST /api/invitations/register` - No authentication required
- `POST /api/invitations/{token}/decline` - No authentication required

### Protected Endpoints
- `POST /api/invitations/{token}/accept` - Requires authentication, user must match invitation email
- `POST /api/invitations/{token}/resend` - Requires authentication, only original inviter
- `GET /api/invitations/sent` - Requires authentication, shows user's sent invitations
- `GET /api/invitations/sent/pending` - Requires authentication, shows user's pending invitations

---

## Frontend Integration Guide

### 1. Invitation Landing Page
```javascript
// Route: /invite/:token
const InvitationPage = ({ token }) => {
  const [invitation, setInvitation] = useState(null);
  
  useEffect(() => {
    fetch(`/api/invitations/${token}`)
      .then(res => res.json())
      .then(data => setInvitation(data))
      .catch(err => handleError(err));
  }, [token]);
  
  // Show invitation details and registration form
};
```

### 2. Registration Flow
```javascript
const handleRegistration = async (formData) => {
  try {
    const response = await fetch('/api/invitations/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        inviteToken: token,
        ...formData
      })
    });
    
    const authData = await response.json();
    
    // Store tokens
    localStorage.setItem('accessToken', authData.accessToken);
    localStorage.setItem('refreshToken', authData.refreshToken);
    
    // Redirect to dashboard
    navigate('/dashboard');
  } catch (error) {
    handleRegistrationError(error);
  }
};
```

---

## Error Handling

### Common HTTP Status Codes
- **200**: Success
- **400**: Bad Request (validation errors, invalid tokens)
- **403**: Forbidden (authorization errors)
- **404**: Not Found (invitation doesn't exist)
- **409**: Conflict (duplicate email/org name, already processed)
- **500**: Internal Server Error

### Error Response Format
All error responses follow this format:
```json
{
  "timestamp": "2025-01-08T14:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Detailed error message",
  "path": "/api/invitations/endpoint",
  "validationErrors": [
    // Optional array for validation errors
    {
      "field": "fieldName",
      "message": "Field-specific error message"
    }
  ]
}
```

---

## Rate Limiting

- **Registration**: 5 attempts per hour per IP
- **Resend Email**: 3 attempts per hour per token
- **Get Details**: 60 requests per hour per IP

---

## Testing Examples

### cURL Examples

**Get Invitation Details:**
```bash
curl -X GET "https://api.berryfi.com/api/invitations/inv_a1b2c3d4e5f6g7h8i9j0"
```

**Register Through Invitation:**
```bash
curl -X POST "https://api.berryfi.com/api/invitations/register" \
  -H "Content-Type: application/json" \
  -d '{
    "inviteToken": "inv_a1b2c3d4e5f6g7h8i9j0",
    "userName": "Sarah Johnson",
    "email": "sarah@newcompany.com",
    "password": "SecurePass123!",
    "organizationName": "Johnson Analytics",
    "organizationDescription": "Analytics consulting"
  }'
```

**Accept Invitation (Existing User):**
```bash
curl -X POST "https://api.berryfi.com/api/invitations/inv_a1b2c3d4e5f6g7h8i9j0/accept" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

**Decline Invitation:**
```bash
curl -X POST "https://api.berryfi.com/api/invitations/inv_a1b2c3d4e5f6g7h8i9j0/decline"
```

**Get Sent Invitations:**
```bash
curl -X GET "https://api.berryfi.com/api/invitations/sent?status=PENDING&page=0&size=10" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

**Get Pending Invitations:**
```bash
curl -X GET "https://api.berryfi.com/api/invitations/sent/pending" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

**Resend Invitation:**
```bash
curl -X POST "https://api.berryfi.com/api/invitations/inv_a1b2c3d4e5f6g7h8i9j0/resend" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```