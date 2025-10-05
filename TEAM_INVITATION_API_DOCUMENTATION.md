# Team Invitation API Documentation

## Overview

The Team Invitation API provides comprehensive functionality for managing team member invitations within BerryFi organizations. This includes sending invitations, user reg### 6. Get Invitation Details by Tokenstration through invitations, tracking invitation### 7. Register Through Team Invitationstatus, and managing the invitation lifecycle with resend and cancel capabilitie### 8. Accept Team Invitation (Existing User).

The API follows a two-endpoint pattern for invita### 4. Resend Team Invitationion acceptance:
- **Registration endpoint** (`/r### 5. Cancel Team Invitationgister`) for new users to join through invitations
- **Acceptance endpoint** (`/accept`) for existing users to accept invitations

## Base URL
```
https://api.berryfi.com/api/team
```

## Authentication

Most endpoints require JWT Bearer token authentication:
```
Authorization: Bearer <jwt_token>
```

**Public Endpoints** (no authentication required):
- `GET /api/team/invitations/token/{token}` - View invitation details
- `POST /api/team/invitations/register` - Register through invitation

**Authenticated Endpoints**:
- All management endpoints (send, list, resend, cancel)
- `POST /api/team/invitations/token/{token}/accept` - Accept invitation as existing user

---

## API Endpoints

### 1. Check User Existence (Helper Endpoint)

**Endpoint:** `GET /api/team/invitations/check-user`

**Description:** Check if a user exists by email address. This helps determine whether to use the registration flow (for new users) or the accept flow (for existing users).

**Authentication:** Public (no authentication required)

**Query Parameters:**
- `email` (string, required): Email address to check

**Response:**
```json
{
  "exists": false,
  "email": "pratikkumbhare9990@gmail.com"
}
```

**Usage Flow:**
1. When a user clicks an invitation link, first call this endpoint to check if they have an account
2. If `exists: false`, direct them to use the registration endpoint
3. If `exists: true`, direct them to log in and use the accept endpoint

---

### 3. List Team Invitations

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
  "userId": null,
  "userName": null,
  "userEmail": "newmember@example.com",
  "firstName": null,
  "lastName": null,
  "organizationId": "berryfi",
  "organizationName": "BerryFi Studios",
  "role": "ORG_MEMBER",
  "status": "INVITED",
  "invitedBy": "user_78bdd7f7",
  "invitedByName": "John Doe",
  "invitedAt": "2025-10-05T19:30:00.123456",
  "joinedAt": null,
  "lastActiveAt": null,
  "isActive": true,
  "profileImageUrl": null,
  "canManageTeam": false,
  "canManageProjects": false,
  "canManageCampaigns": false,
  "canViewAnalytics": true,
  "projectsCreated": 0,
  "campaignsCreated": 0,
  "leadsManaged": 0,
  "createdAt": "2025-10-05T19:30:00.123456",
  "updatedAt": "2025-10-05T19:30:00.123456"
}
```

**Error Response (400 Bad Request):**
```json
{
  "message": "Failed to invite team member: User already exists in organization",
  "timestamp": 1696527000123
}
```

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
      "id": "tmi_12345abcde",
      "userEmail": "newmember@example.com",
      "organizationId": "berryfi",
      "organizationName": "BerryFi Studios",
      "role": "ORG_MEMBER",
      "roleName": "Organization Member",
      "status": "PENDING",
      "inviteToken": "3f263bee-f333-413d-916b-85fc8ae31dbc",
      "invitedByUserId": "user_78bdd7f7",
      "inviterName": "John Doe",
      "inviterEmail": "admin@berryfi.com",
      "message": "Welcome to our team!",
      "createdAt": "2025-10-05T19:30:00.123456",
      "expiresAt": "2025-10-12T19:30:00.123456",
      "acceptedAt": null,
      "invitationLink": "https://portal.berryfi.com/team-invitation/3f263bee-f333-413d-916b-85fc8ae31dbc",
      "expired": false,
      "pending": true,
      "declined": false,
      "accepted": false,
      "cancelled": false,
      "statusDisplay": "PENDING"
    }
  ],
  "pageable": {
    "sort": {"empty": true, "sorted": false, "unsorted": true},
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
  "sort": {"empty": true, "sorted": false, "unsorted": true},
  "first": true,
  "numberOfElements": 1,
  "empty": false
}
```

---

### 3. Get Invitation Details by Token

Retrieve invitation details using the invitation token (public endpoint for email links).

**Endpoint:** `GET /api/team/invitations/token/{token}`

**Headers:** None required (public endpoint)

**Path Parameters:**
- `token`: The invitation token from the email link

**Request Example:**
```
GET /api/team/invitations/token/3f263bee-f333-413d-916b-85fc8ae31dbc
```

**Success Response (200 OK):**
```json
{
  "id": "tmi_c9a82331d874454b",
  "userId": null,
  "userName": null,
  "userEmail": "pratikkumbhare9990@gmail.com",
  "firstName": null,
  "lastName": null,
  "organizationId": null,
  "organizationName": "BerryFi Systems",
  "role": "ORG_MEMBER",
  "status": "INVITED",
  "invitedBy": "user_78bdd7f7",
  "invitedByName": null,
  "invitedAt": "2025-10-05T19:19:08.220318",
  "joinedAt": null,
  "lastActiveAt": null,
  "isActive": null,
  "profileImageUrl": null,
  "canManageTeam": null,
  "canManageProjects": null,
  "canManageCampaigns": null,
  "canViewAnalytics": null,
  "projectsCreated": null,
  "campaignsCreated": null,
  "leadsManaged": null,
  "createdAt": null,
  "updatedAt": null
}
```

---

### 4. Register Through Team Invitation

Register a new user account and accept team invitation in one step (public endpoint).

**Endpoint:** `POST /api/team/invitations/register`

**Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "inviteToken": "string",    // Required: Invitation token from email
  "fullName": "string",       // Required: User's full name
  "password": "string",       // Required: User's password (min 8 chars)
  "phoneNumber": "string",    // Optional: Phone number
  "jobTitle": "string"        // Optional: Job title
}
```

**Request Example:**
```json
{
  "inviteToken": "3f263bee-f333-413d-916b-85fc8ae31dbc",
  "fullName": "Pratik Kumbhare",
  "password": "SecurePassword123!",
  "phoneNumber": "+1-555-0123",
  "jobTitle": "Software Developer"
}
```

**Success Response (200 OK):**
```json
{
  "id": "tm_newuser123",
  "userId": "user_newuser123",
  "userName": "Pratik Kumbhare",
  "userEmail": "pratikkumbhare9990@gmail.com",
  "firstName": null,
  "lastName": null,
  "organizationId": "berryfi",
  "organizationName": "BerryFi Systems",
  "role": "ORG_MEMBER",
  "status": "ACTIVE",
  "invitedBy": "user_78bdd7f7",
  "invitedByName": "Super Admin",
  "invitedAt": "2025-10-05T19:19:08.220318",
  "joinedAt": "2025-10-05T20:30:45.123456",
  "lastActiveAt": null,
  "isActive": true,
  "profileImageUrl": null,
  "canManageTeam": false,
  "canManageProjects": false,
  "canManageCampaigns": false,
  "canViewAnalytics": true,
  "projectsCreated": 0,
  "campaignsCreated": 0,
  "leadsManaged": 0,
  "createdAt": "2025-10-05T20:30:45.123456",
  "updatedAt": "2025-10-05T20:30:45.123456"
}
```

**Error Response (400 Bad Request):**
```json
{
  "message": "An account with this email address already exists. Please log in to accept the invitation.",
  "timestamp": 1696527000123
}
```

**Possible Error Messages:**
- "Team invitation not found or invalid"
- "This team invitation is no longer valid. Status: ACCEPTED"
- "This team invitation has expired"
- "An account with this email address already exists. Please log in to accept the invitation."

---

### 5. Accept Team Invitation (Existing User)

Accept a team invitation for an existing authenticated user.

**Endpoint:** `POST /api/team/invitations/token/{token}/accept`

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Path Parameters:**
- `token`: The invitation token from the email link

**Request Example:**
```
POST /api/team/invitations/token/3f263bee-f333-413d-916b-85fc8ae31dbc/accept
```

**Success Response (200 OK):**
```json
{
  "id": "tm_existinguser456",
  "userId": "user_78bdd7f7",
  "userName": "John Doe",
  "userEmail": "john.doe@example.com",
  "firstName": null,
  "lastName": null,
  "organizationId": "berryfi",
  "organizationName": "BerryFi Systems",
  "role": "ORG_MEMBER",
  "status": "ACTIVE",
  "invitedBy": "user_78bdd7f7",
  "invitedByName": "Super Admin",
  "invitedAt": "2025-10-05T19:19:08.220318",
  "joinedAt": "2025-10-05T20:45:12.654321",
  "lastActiveAt": "2025-10-05T20:45:12.654321",
  "isActive": true,
  "profileImageUrl": null,
  "canManageTeam": false,
  "canManageProjects": false,
  "canManageCampaigns": false,
  "canViewAnalytics": true,
  "projectsCreated": 0,
  "campaignsCreated": 0,
  "leadsManaged": 0,
  "createdAt": "2025-10-05T20:45:12.654321",
  "updatedAt": "2025-10-05T20:45:12.654321"
}
```

---

### 6. Resend Team Invitation

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
POST /api/team/invitations/tmi_12345abcde/resend
```

**Success Response (200 OK):**
```json
{
  "id": "tmi_12345abcde",
  "userEmail": "newmember@example.com",
  "organizationId": "berryfi",
  "organizationName": "BerryFi Studios",
  "role": "ORG_MEMBER",
  "roleName": "Organization Member",
  "status": "PENDING",
  "inviteToken": "3f263bee-f333-413d-916b-85fc8ae31dbc",
  "invitedByUserId": "user_78bdd7f7",
  "inviterName": "John Doe",
  "inviterEmail": "admin@berryfi.com",
  "message": "Welcome to our team!",
  "createdAt": "2025-10-05T19:30:00.123456",
  "expiresAt": "2025-10-19T20:45:00.654321",
  "acceptedAt": null,
  "invitationLink": "https://portal.berryfi.com/team-invitation/3f263bee-f333-413d-916b-85fc8ae31dbc",
  "expired": false,
  "pending": true,
  "declined": false,
  "accepted": false,
  "cancelled": false,
  "statusDisplay": "PENDING"
}
```

---

### 7. Cancel Team Invitation

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
POST /api/team/invitations/tmi_12345abcde/cancel
```

**Success Response (200 OK):**
```json
{
  "id": "tmi_12345abcde",
  "userEmail": "newmember@example.com",
  "organizationId": "berryfi",
  "organizationName": "BerryFi Studios",
  "role": "ORG_MEMBER",
  "roleName": "Organization Member",
  "status": "CANCELLED",
  "inviteToken": "3f263bee-f333-413d-916b-85fc8ae31dbc",
  "invitedByUserId": "user_78bdd7f7",
  "inviterName": "John Doe",
  "inviterEmail": "admin@berryfi.com",
  "message": "Welcome to our team!",
  "createdAt": "2025-10-05T19:30:00.123456",
  "expiresAt": "2025-10-12T19:30:00.123456",
  "acceptedAt": null,
  "invitationLink": "https://portal.berryfi.com/team-invitation/3f263bee-f333-413d-916b-85fc8ae31dbc",
  "expired": false,
  "pending": false,
  "declined": false,
  "accepted": false,
  "cancelled": true,
  "statusDisplay": "CANCELLED"
}
```

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

### TeamInvitationRegistrationRequest

```json
{
  "inviteToken": "string",   // Required: Invitation token from email
  "fullName": "string",      // Required: User's full name
  "password": "string",      // Required: Password (min 8 characters)
  "phoneNumber": "string",   // Optional: Phone number
  "jobTitle": "string"       // Optional: Job title
}
```

**Validation Rules:**
- `inviteToken`: Must be a valid invitation token, not blank
- `fullName`: Required, not blank
- `password`: Required, minimum 8 characters
- `phoneNumber`: Optional
- `jobTitle`: Optional

### TeamMemberResponse

```json
{
  "id": "string",                    // Team member ID
  "userId": "string",                // User ID (null for pending invitations)
  "userName": "string",              // User's full name
  "userEmail": "string",             // User's email address
  "firstName": "string",             // User's first name (may be null)
  "lastName": "string",              // User's last name (may be null)
  "organizationId": "string",        // Organization ID
  "organizationName": "string",      // Organization display name
  "role": "Role",                    // User's role in the organization
  "status": "UserStatus",            // Current user status (INVITED, ACTIVE, etc.)
  "invitedBy": "string",             // ID of user who sent invitation
  "invitedByName": "string",         // Name of user who sent invitation
  "invitedAt": "datetime",           // When invitation was sent
  "joinedAt": "datetime",            // When user joined (accepted invitation)
  "lastActiveAt": "datetime",        // Last activity timestamp
  "isActive": "boolean",             // Whether user is currently active
  "profileImageUrl": "string",       // Profile image URL
  "canManageTeam": "boolean",        // Permission to manage team
  "canManageProjects": "boolean",    // Permission to manage projects
  "canManageCampaigns": "boolean",   // Permission to manage campaigns
  "canViewAnalytics": "boolean",     // Permission to view analytics
  "projectsCreated": "number",       // Number of projects created
  "campaignsCreated": "number",      // Number of campaigns created
  "leadsManaged": "number",          // Number of leads managed
  "createdAt": "datetime",           // Record creation timestamp
  "updatedAt": "datetime"            // Record last update timestamp
}
```

### TeamInvitationResponse

```json
{
  "id": "string",                    // Invitation ID
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
  "invitationLink": "string",        // Full invitation URL
  "expired": "boolean",              // Whether invitation has expired
  "pending": "boolean",              // Whether invitation is pending and not expired
  "declined": "boolean",             // Whether invitation was declined
  "accepted": "boolean",             // Whether invitation was accepted
  "cancelled": "boolean",            // Whether invitation was cancelled
  "statusDisplay": "string"          // Human-readable status (includes EXPIRED for expired pending)
}
```

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

### UserStatus Enum Values

```
INVITED    - User has been invited but not yet registered
ACTIVE     - User is active and can access the system
DISABLED   - User account is disabled
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

### Invitation Registration
- New users can register through invitation without prior authentication
- Email in registration must match the invitation email
- Creates user account with role specified in invitation
- Automatically adds user to the organization as team member
- Marks invitation as accepted

### Invitation Acceptance (Existing Users)
- Only authenticated users can accept invitations
- User must exist in the system
- Adds existing user to the organization with invitation role
- Marks invitation as accepted

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
- Registration: 5 registrations per IP per hour

---

## Security Considerations

- Invitation tokens are cryptographically secure UUIDs
- Tokens expire automatically after 7 days
- Email addresses are validated before sending
- Passwords are encrypted using BCrypt
- Organization context is validated from JWT token (authenticated endpoints)
- All operations are logged for audit purposes
- Rate limiting prevents abuse

---

## Example Usage Flows

### 1. New User Registration Flow

```bash
# 1. Admin sends invitation
curl -X POST https://api.berryfi.com/api/team/invite \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin_token>" \
  -d '{
    "userEmail": "newuser@example.com",
    "role": "ORG_MEMBER",
    "message": "Welcome to BerryFi!"
  }'

# 2. User receives email with invitation link
# Link contains token: https://portal.berryfi.com/team-invitation/3f263bee-f333-413d-916b-85fc8ae31dbc

# 3. User views invitation details (public endpoint)
curl -X GET https://api.berryfi.com/api/team/invitations/token/3f263bee-f333-413d-916b-85fc8ae31dbc

# 4. User registers through invitation
curl -X POST https://api.berryfi.com/api/team/invitations/register \
  -H "Content-Type: application/json" \
  -d '{
    "inviteToken": "3f263bee-f333-413d-916b-85fc8ae31dbc",
    "fullName": "John Doe",
    "password": "SecurePassword123!"
  }'
```

### 2. Existing User Acceptance Flow

```bash
# 1. Admin sends invitation (same as above)

# 2. Existing user logs in and accepts invitation
curl -X POST https://api.berryfi.com/api/team/invitations/token/3f263bee-f333-413d-916b-85fc8ae31dbc/accept \
  -H "Authorization: Bearer <user_token>"
```

### 3. Invitation Management Flow

```bash
# 1. Check pending invitations
curl -X GET "https://api.berryfi.com/api/team/invitations?status=PENDING" \
  -H "Authorization: Bearer <admin_token>"

# 2. Resend if needed
curl -X POST https://api.berryfi.com/api/team/invitations/tmi_12345abcde/resend \
  -H "Authorization: Bearer <admin_token>"

# 3. Cancel if needed
curl -X POST https://api.berryfi.com/api/team/invitations/tmi_12345abcde/cancel \
  -H "Authorization: Bearer <admin_token>"
```

### 4. Frontend Integration Example

```javascript
// New user registration flow
class TeamInvitationService {
  
  // View invitation details
  async getInvitationDetails(token) {
    const response = await fetch(`/api/team/invitations/token/${token}`);
    return response.json();
  }
  
  // Register new user
  async registerThroughInvitation(data) {
    const response = await fetch('/api/team/invitations/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    });
    return response.json();
  }
  
  // Accept invitation for existing user
  async acceptInvitation(token, authToken) {
    const response = await fetch(`/api/team/invitations/token/${token}/accept`, {
      method: 'POST',
      headers: { 'Authorization': `Bearer ${authToken}` }
    });
    return response.json();
  }
  
  // Admin: Get pending invitations
  async getPendingInvitations(authToken) {
    const response = await fetch('/api/team/invitations?status=PENDING', {
      headers: { 'Authorization': `Bearer ${authToken}` }
    });
    return response.json();
  }
}

// Usage example
const invitationService = new TeamInvitationService();

// For new user registration page
const invitation = await invitationService.getInvitationDetails(tokenFromUrl);
console.log(`Join ${invitation.organizationName} as ${invitation.role}`);

await invitationService.registerThroughInvitation({
  inviteToken: tokenFromUrl,
  fullName: 'John Doe',
  password: 'SecurePassword123!'
});
```

---

## Changelog

### v2.0.0 (2025-10-05)
- **BREAKING CHANGE**: Restructured invitation acceptance endpoints
- Added separate `/register` endpoint for new user registration
- Updated `/accept` endpoint to require authentication for existing users
- Improved security with proper password encryption
- Enhanced error handling and validation
- Added comprehensive user creation flow
- Performance optimization for invitation listing (fixed N+1 query issue)
- Added detailed business rule documentation

### v1.0.0 (2025-10-05)
- Initial API release
- Basic invitation sending functionality
- Status tracking and filtering
- Resend and cancel operations
- JWT authentication integration

---

## Support

For API support and questions:
- **Documentation**: This document
- **Support Email**: api-support@berryfi.com
- **Status Page**: https://status.berryfi.com