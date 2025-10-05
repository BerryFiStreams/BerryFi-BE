# Project Invitation System API Documentation

## Overview
The Project Invitation System allows organization administrators to invite users via email to share projects. When sharing with non-existent users, the system sends invitation emails that allow recipients to register and create organizations.

## Authentication
All endpoints require appropriate authentication and authorization. The invitation endpoints use token-based access.

---

## API Endpoints

### 1. Get Invitation Details
**GET** `/api/invitations/{token}`

Retrieve details of a project invitation using the invitation token.

#### Parameters
- `token` (path, required): The invitation token from the email

#### Response (200 OK)
```json
{
  "inviteToken": "inv_abc123...",
  "projectName": "Analytics Dashboard",
  "projectDescription": "Customer analytics and reporting dashboard",
  "inviterName": "John Smith",
  "inviterEmail": "john@acme.com",
  "inviterOrganization": "ACME Corp",
  "inviteeEmail": "newuser@example.com",
  "creditsAllocated": 1000,
  "expiresAt": "2024-01-15T10:30:00",
  "status": "PENDING"
}
```

#### Error Responses
- **404 Not Found**: Invitation not found
- **400 Bad Request**: Invalid or expired invitation token

---

### 2. Register Through Invitation
**POST** `/api/invitations/register`

Register a new user and organization using an invitation token.

#### Request Body
```json
{
  "inviteToken": "inv_abc123...",
  "userName": "Jane Doe",
  "email": "jane@newcompany.com",
  "password": "SecurePass123!",
  "organizationName": "New Company Inc",
  "organizationDescription": "Innovative tech solutions"
}
```

#### Response (200 OK)
```json
{
  "user": {
    "id": "usr_xyz789",
    "name": "Jane Doe",
    "email": "jane@newcompany.com",
    "role": "ORG_OWNER",
    "accountType": "ORGANIZATION",
    "organizationId": "org_abc123",
    "status": "ACTIVE",
    "lastLogin": null
  },
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### Error Responses
- **400 Bad Request**: Invalid request data or invitation token
- **404 Not Found**: Invitation not found
- **409 Conflict**: User already exists or invitation already accepted

---

### 3. Decline Invitation
**POST** `/api/invitations/{token}/decline`

Decline a project invitation.

#### Parameters
- `token` (path, required): The invitation token

#### Response (200 OK)
Empty response body

#### Error Responses
- **404 Not Found**: Invitation not found
- **400 Bad Request**: Invalid invitation token
- **409 Conflict**: Invitation already processed

---

### 4. Resend Invitation
**POST** `/api/invitations/{token}/resend`

Resend an invitation email (for invitation sender).

#### Parameters
- `token` (path, required): The invitation token

#### Response (200 OK)
Empty response body

#### Error Responses
- **404 Not Found**: Invitation not found
- **400 Bad Request**: Invalid invitation token
- **403 Forbidden**: Not authorized to resend this invitation

---

## Integration with Project Sharing

### Updated Share Project Endpoint
**POST** `/api/projects/{projectId}/share`

When sharing with a non-existent user email, the system now:
1. Creates a ProjectInvitation record
2. Sends an invitation email with registration link
3. Returns success with invitation details

#### Request Body (Enhanced)
```json
{
  "targetIdentifier": "newuser@example.com",  // Email for non-existent users
  "creditsToAllocate": 1000,
  "expiresAt": "2024-01-15T10:30:00"          // Optional expiration date
}
```

#### Response for Email Invitation (200 OK)
```json
{
  "success": true,
  "message": "Project shared successfully",
  "shareType": "INVITATION",
  "invitationToken": "inv_abc123...",
  "inviteeEmail": "newuser@example.com",
  "expiresAt": "2024-01-15T10:30:00"
}
```

---

## Email Templates

### Invitation Email
**Subject**: "You're invited to collaborate on {projectName}"

**HTML Content**:
- Project details and invitation context
- Registration link with embedded token
- Expiration information
- Inviter details

**Registration URL Format**:
```
https://app.berryfi.com/invitation/register?token={inviteToken}
```

---

## Entity Relationships

### ProjectInvitation Entity
```java
@Entity
public class ProjectInvitation {
    private String id;
    private String inviteToken;        // Unique invitation token
    private String projectId;          // Project being shared
    private String inviterUserId;      // User who sent invitation
    private String inviteeEmail;       // Recipient email
    private Integer creditsAllocated;  // Credits to allocate
    private InvitationStatus status;   // PENDING, ACCEPTED, DECLINED, etc.
    private LocalDateTime expiresAt;   // Invitation expiration
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String referredBy;         // Tracking referral chain
}
```

### InvitationStatus Enum
- `PENDING`: Invitation sent, awaiting response
- `ACCEPTED`: Invitation accepted, project shared
- `DECLINED`: Invitation declined by recipient
- `EXPIRED`: Invitation expired (auto-set by system)
- `CANCELLED`: Invitation cancelled by sender

---

## Validation Rules

### Registration Request
- **userName**: Required, 2-100 characters
- **email**: Required, valid email format, unique in system
- **password**: Required, minimum 8 characters, at least one uppercase, lowercase, number, and special character
- **organizationName**: Required, 2-100 characters, unique in system
- **organizationDescription**: Optional, max 500 characters
- **inviteToken**: Required, valid active invitation token

### Business Rules
1. Only ORG_ADMIN users can share projects
2. Invitations expire after 7 days by default
3. Each invitation can only be accepted once
4. Users created through invitations become ORG_OWNER of their new organization
5. Project sharing happens automatically when invitation is accepted
6. Referral tracking maintains invitation chain

---

## Error Handling

### Common Error Format
```json
{
  "timestamp": "2024-01-01T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid invitation token",
  "path": "/api/invitations/register"
}
```

### Specific Error Scenarios

#### Expired Invitation
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "This invitation has expired"
}
```

#### User Already Exists
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "A user with this email already exists"
}
```

#### Organization Name Taken
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "An organization with this name already exists"
}
```

---

## Security Considerations

1. **Token Security**: Invitation tokens are UUIDs, making them difficult to guess
2. **Email Verification**: The email address is verified through the invitation flow
3. **Expiration**: All invitations have configurable expiration times
4. **Rate Limiting**: Consider implementing rate limiting on invitation sending
5. **Authorization**: Only organization admins can send invitations
6. **Audit Trail**: All invitation activities are logged for security monitoring

---

## Testing Scenarios

### Happy Path
1. Admin shares project with non-existent email
2. System creates invitation and sends email
3. Recipient clicks link and registers
4. New user and organization created
5. Project automatically shared with allocated credits

### Edge Cases
1. Expired invitation handling
2. Duplicate registration attempts
3. Invalid token handling
4. Email delivery failures
5. Partial registration failures (rollback scenarios)

---

## Future Enhancements

1. **Bulk Invitations**: Support for inviting multiple users at once
2. **Invitation Templates**: Customizable email templates per organization
3. **Advanced Permissions**: Granular permissions for shared projects
4. **Analytics**: Invitation success rates and user conversion metrics
5. **Integration**: Support for other communication channels (SMS, Slack, etc.)