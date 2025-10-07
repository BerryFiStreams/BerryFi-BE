# Team Invitation API Documentation

## Overview

The Team Invitation API provides comprehensive functionality for managing team member invitations within BerryFi organizations. This includes sending invitations, tracking their status, and managing the invitation lifecycle with resend and cancel capabilities.

## Base URL
```
https://api.berryfi.com/api/team
```

## Authentication

All endpoints require JWT Bearer token authentication:
```
Authorization: Bearer <jwt_token>
```

The JWT token must contain organization context and user permissions.

---

## API Endpoints

### 1. Send Team Invitation

Send an invitation to a new team member via email.

**Endpoint:** `POST /api/team/invite`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <jwt_token>
```

**Request Body:**
```json
{
  "userEmail": "string",    // Required: Valid email address
  "role": "string",         // Required: Role enum value
  "message": "string"       // Optional: Personal message
}
```

**Available Roles:**
- `SUPER_ADMIN` - Super Admin (system-wide access)
- `ORG_OWNER` - Organization Owner
- `ORG_ADMIN` - Organization Admin
- `ORG_AUDITOR` - Organization Auditor
- `ORG_REPORTER` - Organization Reporter
- `ORG_BILLING` - Organization Billing
- `ORG_MEMBER` - Organization Member
- `PROJECT_ADMIN` - Project Admin
- `PROJECT_COLLABORATOR` - Project Collaborator
- `PROJECT_VIEWER` - Project Viewer

**Request Example:**
```json
{
  "userEmail": "newmember@example.com",
  "role": "ORG_MEMBER",
  "message": "Welcome to our BerryFi team! We're excited to have you join us."
}
```

**Success Response (201 Created):**
```json
{
  "id": "tm_12345abcde",
  "userEmail": "newmember@example.com",
  "organizationId": "berryfi",
  "organizationName": "BerryFi Studios",
  "role": "ORG_MEMBER",
  "roleName": "Organization Member",
  "status": "PENDING",
  "inviteToken": "inv_token_xyz789",
  "invitedByUserId": "user_78bdd7f7",
  "inviterName": "John Doe",
  "inviterEmail": "admin@berryfi.com",
  "message": "Welcome to our BerryFi team! We're excited to have you join us.",
  "createdAt": "2025-10-05T19:30:00.123456",
  "expiresAt": "2025-10-12T19:30:00.123456",
  "acceptedAt": null,
  "invitationLink": "https://portal.berryfi.com/invite/inv_token_xyz789"
}
```

**Error Response (400 Bad Request):**
```json
{
  "message": "Failed to invite team member: User already exists in organization",
  "timestamp": 1696527000123
}
```

**Possible Error Messages:**
- "User already exists in organization"
- "Invalid email address format"
- "Role is required"
- "Organization not found"
- "Insufficient permissions to invite members"

---

### 2. Get Team Invitations

Retrieve paginated list of team invitations with optional status filtering.

**Endpoint:** `GET /api/team/invitations`

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Query Parameters:**
- `status` (optional): Filter by invitation status
  - `PENDING` - Active pending invitations
  - `ACCEPTED` - Accepted invitations
  - `DECLINED` - Declined invitations
  - `EXPIRED` - Expired invitations
  - `CANCELLED` - Cancelled invitations
- `page` (optional, default: 0): Page number (0-based)
- `size` (optional, default: 20): Page size (max: 100)

**Request Example:**
```
GET /api/team/invitations?status=PENDING&page=0&size=10
```

**Success Response (200 OK):**
```json
{
  "content": [
    {
      "id": "tm_12345abcde",
      "userEmail": "newmember@example.com",
      "organizationId": "berryfi",
      "organizationName": "BerryFi Studios",
      "role": "ORG_MEMBER",
      "roleName": "Organization Member",
      "status": "PENDING",
      "inviteToken": "inv_token_xyz789",
      "invitedByUserId": "user_78bdd7f7",
      "inviterName": "John Doe",
      "inviterEmail": "admin@berryfi.com",
      "message": "Welcome to our team!",
      "createdAt": "2025-10-05T19:30:00.123456",
      "expiresAt": "2025-10-12T19:30:00.123456",
      "acceptedAt": null,
      "invitationLink": "https://portal.berryfi.com/invite/inv_token_xyz789"
    }
  ],
  "pageable": {
    "sort": {
      "empty": true,
      "sorted": false,
      "unsorted": true
    },
    "offset": 0,
    "pageSize": 10,
    "pageNumber": 0,
    "paged": true,
    "unpaged": false
  },
  "last": true,
  "totalPages": 1,
  "totalElements": 1,
  "size": 10,
  "number": 0,
  "sort": {
    "empty": true,
    "sorted": false,
    "unsorted": true
  },
  "first": true,
  "numberOfElements": 1,
  "empty": false
}
```

**Error Response (400 Bad Request):**
```json
{
  "message": "Failed to get team invitations: Invalid status parameter",
  "timestamp": 1696527000123
}
```

---

### 3. Resend Team Invitation

Resend an existing pending invitation with extended expiration.

**Endpoint:** `POST /api/team/invitations/{invitationId}/resend`

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Path Parameters:**
- `invitationId`: The ID of the invitation to resend

**Request Example:**
```
POST /api/team/invitations/tm_12345abcde/resend
```

**Success Response (200 OK):**
```json
{
  "id": "tm_12345abcde",
  "userEmail": "newmember@example.com",
  "organizationId": "berryfi",
  "organizationName": "BerryFi Studios",
  "role": "ORG_MEMBER",
  "roleName": "Organization Member",
  "status": "PENDING",
  "inviteToken": "inv_token_xyz789",
  "invitedByUserId": "user_78bdd7f7",
  "inviterName": "John Doe",
  "inviterEmail": "admin@berryfi.com",
  "message": "Welcome to our team!",
  "createdAt": "2025-10-05T19:30:00.123456",
  "expiresAt": "2025-10-19T20:45:00.654321",
  "acceptedAt": null,
  "invitationLink": "https://portal.berryfi.com/invite/inv_token_xyz789"
}
```

**Error Response (400 Bad Request):**
```json
{
  "message": "Failed to resend team invitation: Invitation cannot be resent - already accepted",
  "timestamp": 1696527000123
}
```

**Possible Error Messages:**
- "Invitation not found"
- "Invitation cannot be resent - already accepted"
- "Invitation cannot be resent - already declined" 
- "Invitation cannot be resent - already cancelled"
- "Only organization owners/admins can resend invitations"

---

### 4. Cancel Team Invitation

Cancel a pending team invitation.

**Endpoint:** `POST /api/team/invitations/{invitationId}/cancel`

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Path Parameters:**
- `invitationId`: The ID of the invitation to cancel

**Request Example:**
```
POST /api/team/invitations/tm_12345abcde/cancel
```

**Success Response (200 OK):**
```json
{
  "id": "tm_12345abcde",
  "userEmail": "newmember@example.com",
  "organizationId": "berryfi",
  "organizationName": "BerryFi Studios",
  "role": "ORG_MEMBER",
  "roleName": "Organization Member",
  "status": "CANCELLED",
  "inviteToken": "inv_token_xyz789",
  "invitedByUserId": "user_78bdd7f7",
  "inviterName": "John Doe",
  "inviterEmail": "admin@berryfi.com",
  "message": "Welcome to our team!",
  "createdAt": "2025-10-05T19:30:00.123456",
  "expiresAt": "2025-10-12T19:30:00.123456",
  "acceptedAt": null,
  "invitationLink": "https://portal.berryfi.com/invite/inv_token_xyz789"
}
```

**Error Response (400 Bad Request):**
```json
{
  "message": "Failed to cancel team invitation: Invitation is already accepted and cannot be cancelled",
  "timestamp": 1696527000123
}
```

**Possible Error Messages:**
- "Invitation not found"
- "Invitation is already accepted and cannot be cancelled"
- "Invitation is already declined and cannot be cancelled"
- "Invitation is already cancelled"
- "Only organization owners/admins can cancel invitations"

---

## Data Models

### TeamInvitationRequest

```json
{
  "userEmail": "string",     // Required: Valid email address
  "role": "Role",           // Required: Role enum value
  "message": "string"       // Optional: Personal invitation message
}
```

**Validation Rules:**
- `userEmail`: Must be a valid email format, not blank
- `role`: Must be a valid Role enum value, not null
- `message`: Optional, can be null or empty

### TeamInvitationResponse

```json
{
  "id": "string",                    // Unique invitation ID
  "userEmail": "string",             // Invited user's email
  "organizationId": "string",        // Organization ID
  "organizationName": "string",      // Organization display name
  "role": "Role",                    // Assigned role enum
  "roleName": "string",              // Role display name
  "status": "InvitationStatus",      // Current invitation status
  "inviteToken": "string",           // Unique invitation token
  "invitedByUserId": "string",       // ID of user who sent invitation
  "inviterName": "string",           // Name of inviting user
  "inviterEmail": "string",          // Email of inviting user
  "message": "string",               // Personal invitation message
  "createdAt": "datetime",           // When invitation was created
  "expiresAt": "datetime",           // When invitation expires
  "acceptedAt": "datetime",          // When invitation was accepted (null if not)
  "invitationLink": "string"         // Full invitation URL
}
```

### Helper Methods in Response

The `TeamInvitationResponse` includes several helper methods for UI integration:

- `isExpired()`: Returns true if invitation has passed expiration date
- `isPending()`: Returns true if status is PENDING and not expired
- `isAccepted()`: Returns true if status is ACCEPTED
- `isDeclined()`: Returns true if status is DECLINED
- `isCancelled()`: Returns true if status is CANCELLED
- `canBeResent()`: Returns true if invitation can be resent (PENDING and not expired)
- `canBeCancelled()`: Returns true if invitation can be cancelled (PENDING status)
- `getStatusDisplay()`: Returns "EXPIRED" for expired pending invitations, otherwise status name

### Role Enum Values

```
SUPER_ADMIN          - Super Admin (system-wide access)
ORG_OWNER           - Organization Owner
ORG_ADMIN           - Organization Admin
ORG_AUDITOR         - Organization Auditor
ORG_REPORTER        - Organization Reporter
ORG_BILLING         - Organization Billing
ORG_MEMBER          - Organization Member
PROJECT_ADMIN       - Project Admin
PROJECT_COLLABORATOR - Project Collaborator
PROJECT_VIEWER      - Project Viewer
```

### InvitationStatus Enum Values

```
PENDING    - Invitation sent, awaiting response
ACCEPTED   - Invitation accepted, user registered
DECLINED   - Invitation declined by recipient
EXPIRED    - Invitation expired without response
CANCELLED  - Invitation cancelled by sender
```

---

## Error Handling

### HTTP Status Codes

- `200 OK` - Successful GET/PUT operations
- `201 Created` - Successful invitation creation
- `400 Bad Request` - Invalid request data or business logic error
- `401 Unauthorized` - Missing or invalid authentication
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Unexpected server error

### Error Response Format

```json
{
  "message": "string",      // Human-readable error message
  "timestamp": 1696527000123 // Unix timestamp
}
```

---

## Business Rules

### Invitation Creation
- User must have organization admin privileges to send invitations
- Cannot invite existing organization members
- Each email can only have one pending invitation per organization
- Invitations expire after 7 days by default

### Invitation Management
- Only pending invitations can be resent or cancelled
- Resending an invitation extends expiration by 7 days
- Only the invitation sender or organization admins can resend/cancel
- Expired invitations cannot be resent (must create new invitation)

### Role Permissions
- `SUPER_ADMIN`: Can invite to any role, manage all invitations
- `ORG_OWNER`: Can invite to any organization role, manage org invitations
- `ORG_ADMIN`: Can invite to member/collaborator roles, manage lower-level invitations
- Other roles: Cannot send invitations

---

## Rate Limiting

- Invitation sending: 10 invitations per minute per organization
- API calls: 100 requests per minute per user
- Resend operations: 3 resends per invitation per day

---

## Security Considerations

- Invitation tokens are cryptographically secure UUIDs
- Tokens expire automatically after 7 days
- Email addresses are validated before sending
- Organization context is validated from JWT token
- All operations are logged for audit purposes

---

## Example Usage Flows

### 1. Complete Invitation Workflow

```bash
# 1. Send invitation
curl -X POST https://api.berryfi.com/api/team/invite \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "userEmail": "newmember@example.com",
    "role": "ORG_MEMBER",
    "message": "Welcome to BerryFi!"
  }'

# 2. Check invitation status
curl -X GET "https://api.berryfi.com/api/team/invitations?status=PENDING" \
  -H "Authorization: Bearer <token>"

# 3. Resend if needed
curl -X POST https://api.berryfi.com/api/team/invitations/tm_12345abcde/resend \
  -H "Authorization: Bearer <token>"

# 4. Cancel if needed
curl -X POST https://api.berryfi.com/api/team/invitations/tm_12345abcde/cancel \
  -H "Authorization: Bearer <token>"
```

### 2. Admin Dashboard Integration

```javascript
// Fetch pending invitations for dashboard
const response = await fetch('/api/team/invitations?status=PENDING&size=50', {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});

const invitations = await response.json();

// Display invitations with action buttons
invitations.content.forEach(invitation => {
  console.log(`${invitation.userEmail} - ${invitation.getStatusDisplay()}`);
  
  if (invitation.canBeResent()) {
    // Show resend button
  }
  
  if (invitation.canBeCancelled()) {
    // Show cancel button
  }
});
```

---

## Changelog

### v1.0.0 (2025-10-05)
- Initial API release
- Basic invitation sending functionality
- Status tracking and filtering
- Resend and cancel operations
- Comprehensive error handling
- JWT authentication integration
