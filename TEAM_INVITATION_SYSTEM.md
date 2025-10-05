# Team Invitation System Documentation

## Overview
A comprehensive team invitation system that allows organization administrators to invite users via email with token-based acceptance, similar to the existing project invitation system.

## Table of Contents
- [Features](#features)
- [API Endpoints](#api-endpoints)
- [Database Schema](#database-schema)
- [Email System](#email-system)
- [Frontend Integration](#frontend-integration)
- [Security & Authentication](#security--authentication)
- [Error Handling](#error-handling)
- [Deployment Notes](#deployment-notes)

---

## Features

### ✅ Core Functionality
- **Email-based invitations** with unique tokens
- **Role-based access control** with 8 different roles
- **7-day invitation expiry** with automatic cleanup
- **User creation on acceptance** for new users
- **Duplicate prevention** for existing invitations
- **Professional email templates** with organization branding
- **Complete audit trail** with timestamps and status tracking
- **Backward compatibility** with existing team member system

### ✅ Security Features
- **JWT-based authentication** using `@AuthenticationPrincipal`
- **Organization-scoped invitations** (users can only invite to their org)
- **Token-based acceptance** with expiration validation
- **Role validation** preventing unauthorized role assignments
- **Email verification** ensuring invitations match user accounts

---

## API Endpoints

### Team Member Invitation Endpoints

#### 1. Send Team Invitation
**Endpoint:** `POST /api/team/members/invite`
**Authentication:** Required (JWT Bearer token)
**Content-Type:** `application/x-www-form-urlencoded`

**Parameters:**
```
userEmail (required): Email address of the user to invite
role (required): User role (ORG_OWNER, ORG_ADMIN, ORG_MEMBER, etc.)
```

**Request Example:**
```bash
curl -X POST "http://localhost:8080/api/team/members/invite" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "userEmail=john.doe@example.com&role=ORG_MEMBER"
```

**Response:**
```json
{
  "id": "tmi_1234567890123456",
  "userEmail": "john.doe@example.com",
  "role": "ORG_MEMBER",
  "status": "INVITED",
  "invitedAt": "2025-10-05T18:20:00",
  "invitedBy": "admin_user_id",
  "organizationName": "BerryFi Studio"
}
```

#### 2. Get Invitation Details
**Endpoint:** `GET /api/team/members/invitations/{token}`
**Authentication:** Required (JWT Bearer token)

**Purpose:** Preview invitation details before acceptance

**Request Example:**
```bash
curl -X GET "http://localhost:8080/api/team/members/invitations/abc123token456" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```json
{
  "id": "tmi_1234567890123456",
  "userEmail": "john.doe@example.com",
  "role": "ORG_MEMBER",
  "status": "INVITED",
  "invitedAt": "2025-10-05T18:20:00",
  "invitedBy": "admin_user_id",
  "organizationName": "BerryFi Studio"
}
```

#### 3. Accept Invitation
**Endpoint:** `POST /api/team/members/invitations/{token}/accept`
**Authentication:** Required (JWT Bearer token)

**Purpose:** Accept invitation and join the team

**Request Example:**
```bash
curl -X POST "http://localhost:8080/api/team/members/invitations/abc123token456/accept" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```json
{
  "id": "tm_9876543210987654",
  "userId": "user123",
  "userName": "John Doe",
  "userEmail": "john.doe@example.com",
  "organizationId": "org123",
  "organizationName": "BerryFi Studio",
  "role": "ORG_MEMBER",
  "status": "ACTIVE",
  "joinedAt": "2025-10-05T18:25:00",
  "invitedBy": "admin_user_id"
}
```

### Team Management Endpoints

#### 4. Get Team Members (Paginated)
**Endpoint:** `GET /api/team/members`
**Authentication:** Required (JWT Bearer token)

**Query Parameters:**
```
page (optional): Page number (default: 0)
size (optional): Page size (default: 10)
```

**Request Example:**
```bash
curl -X GET "http://localhost:8080/api/team/members?page=0&size=10" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### 5. Get Team Members by Role
**Endpoint:** `GET /api/team/members/role/{role}`
**Authentication:** Required (JWT Bearer token)

**Request Example:**
```bash
curl -X GET "http://localhost:8080/api/team/members/role/ORG_ADMIN" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### 6. Update Team Member Role
**Endpoint:** `PUT /api/team/members/{memberId}/role`
**Authentication:** Required (JWT Bearer token)

**Request Example:**
```bash
curl -X PUT "http://localhost:8080/api/team/members/tm_123456/role" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "role=ORG_ADMIN"
```

#### 7. Remove Team Member
**Endpoint:** `DELETE /api/team/members/{memberId}`
**Authentication:** Required (JWT Bearer token)

#### 8. Get Team Analytics
**Endpoint:** `GET /api/team/analytics`
**Authentication:** Required (JWT Bearer token)

**Response:**
```json
{
  "totalMembers": 15,
  "activeMembers": 12,
  "pendingInvitations": 3
}
```

---

## Database Schema

### team_member_invitations Table

```sql
CREATE TABLE team_member_invitations (
  id VARCHAR(50) PRIMARY KEY,
  organization_id VARCHAR(255) NOT NULL,
  invited_by_user_id VARCHAR(255) NOT NULL,
  invite_email VARCHAR(255) NOT NULL,
  invite_token VARCHAR(255) UNIQUE NOT NULL,
  role ENUM('ORG_OWNER', 'ORG_ADMIN', 'ORG_MEMBER', 'ORG_AUDITOR', 
           'ORG_REPORTER', 'ORG_BILLING', 'PROJECT_ADMIN', 
           'PROJECT_COLLABORATOR') NOT NULL,
  status ENUM('PENDING', 'ACCEPTED', 'DECLINED', 'EXPIRED') NOT NULL,
  message TEXT,
  sent_at DATETIME NOT NULL,
  expires_at DATETIME NOT NULL,
  accepted_at DATETIME,
  declined_at DATETIME,
  email_sent_count INT DEFAULT 1,
  last_email_sent_at DATETIME,
  registered_user_id VARCHAR(255),
  created_at DATETIME NOT NULL,
  updated_at DATETIME,
  
  INDEX idx_team_invitation_email (invite_email),
  INDEX idx_team_invitation_token (invite_token),
  INDEX idx_team_invitation_status (status),
  INDEX idx_team_invitation_organization (organization_id),
  INDEX idx_team_invitation_expires (expires_at)
);
```

### Key Fields Explained

| Field | Purpose | Notes |
|-------|---------|--------|
| `invite_token` | Unique URL-safe token for invitation links | Generated using UUID |
| `expires_at` | Invitation expiration timestamp | Default: 7 days from creation |
| `status` | Current invitation state | PENDING, ACCEPTED, DECLINED, EXPIRED |
| `email_sent_count` | Track email delivery attempts | For monitoring and debugging |
| `registered_user_id` | User ID after acceptance | Links invitation to created user |

---

## Email System

### Email Template Features
- **Professional HTML design** with responsive layout
- **Organization branding** with company name and styling
- **Role descriptions** explaining permissions and responsibilities
- **Expiration warnings** showing deadline for acceptance
- **Direct action buttons** for accepting invitations
- **Fallback text content** for email clients without HTML support

### Email Template Variables
```java
// Template variables passed to email service
String organizationName;     // "BerryFi Studio"
String inviterName;         // "John Admin"
String roleName;            // "Organization Member"
String roleDescription;     // "Standard organization member access"
String inviteToken;         // "abc123token456"
String expirationDate;      // "Oct 12, 2025 at 18:20"
String customMessage;       // Optional personal message
```

### Email Configuration
```properties
# SMTP Configuration (Production)
spring.mail.host=smtp.hostinger.com
spring.mail.port=587
spring.mail.username=noreply@berryfi.in
spring.mail.password=${SMTP_PASSWORD}

# Email Service Settings
app.email.from=noreply@berryfi.in
app.email.from-name=BerryFi Studio
app.email.enabled=true
app.email.use-templates=true
```

---

## Frontend Integration

### React/JavaScript Integration Examples

#### 1. Team Members List Component
```jsx
import React, { useState, useEffect } from 'react';

const TeamMembersList = () => {
  const [members, setMembers] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchTeamMembers();
  }, []);

  const fetchTeamMembers = async () => {
    try {
      const response = await fetch('/api/team/members?page=0&size=20', {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });
      const data = await response.json();
      setMembers(data.content);
    } catch (error) {
      console.error('Failed to fetch team members:', error);
    } finally {
      setLoading(false);
    }
  };

  const getStatusBadge = (status) => {
    const badges = {
      'ACTIVE': { color: 'green', text: 'Active' },
      'INVITED': { color: 'orange', text: 'Pending' },
      'DISABLED': { color: 'red', text: 'Disabled' }
    };
    return badges[status] || { color: 'gray', text: status };
  };

  return (
    <div className="team-members-list">
      {loading ? (
        <div>Loading...</div>
      ) : (
        <table>
          <thead>
            <tr>
              <th>Name</th>
              <th>Email</th>
              <th>Role</th>
              <th>Status</th>
              <th>Joined</th>
            </tr>
          </thead>
          <tbody>
            {members.map(member => (
              <tr key={member.id}>
                <td>{member.userName}</td>
                <td>{member.userEmail}</td>
                <td>{member.role}</td>
                <td>
                  <span className={`badge badge-${getStatusBadge(member.status).color}`}>
                    {getStatusBadge(member.status).text}
                  </span>
                </td>
                <td>{member.joinedAt ? new Date(member.joinedAt).toLocaleDateString() : '-'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
};
```

#### 2. Invite Team Member Modal
```jsx
const InviteTeamMemberModal = ({ isOpen, onClose, onSuccess }) => {
  const [email, setEmail] = useState('');
  const [role, setRole] = useState('ORG_MEMBER');
  const [loading, setLoading] = useState(false);

  const roles = [
    { value: 'ORG_OWNER', label: 'Organization Owner' },
    { value: 'ORG_ADMIN', label: 'Organization Administrator' },
    { value: 'ORG_MEMBER', label: 'Organization Member' },
    { value: 'ORG_AUDITOR', label: 'Organization Auditor' },
    { value: 'ORG_REPORTER', label: 'Organization Reporter' },
    { value: 'ORG_BILLING', label: 'Billing Manager' },
    { value: 'PROJECT_ADMIN', label: 'Project Administrator' },
    { value: 'PROJECT_COLLABORATOR', label: 'Project Collaborator' }
  ];

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      const formData = new FormData();
      formData.append('userEmail', email);
      formData.append('role', role);

      const response = await fetch('/api/team/members/invite', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        body: formData
      });

      if (response.ok) {
        onSuccess('Invitation sent successfully!');
        onClose();
        setEmail('');
        setRole('ORG_MEMBER');
      } else {
        const error = await response.json();
        throw new Error(error.message || 'Failed to send invitation');
      }
    } catch (error) {
      alert('Error: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose}>
      <form onSubmit={handleSubmit}>
        <h2>Invite Team Member</h2>
        
        <div className="form-group">
          <label>Email Address</label>
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            placeholder="john.doe@example.com"
          />
        </div>

        <div className="form-group">
          <label>Role</label>
          <select value={role} onChange={(e) => setRole(e.target.value)}>
            {roles.map(r => (
              <option key={r.value} value={r.value}>{r.label}</option>
            ))}
          </select>
        </div>

        <div className="form-actions">
          <button type="button" onClick={onClose}>Cancel</button>
          <button type="submit" disabled={loading}>
            {loading ? 'Sending...' : 'Send Invitation'}
          </button>
        </div>
      </form>
    </Modal>
  );
};
```

#### 3. Invitation Acceptance Page
```jsx
const InvitationAcceptancePage = () => {
  const [invitation, setInvitation] = useState(null);
  const [loading, setLoading] = useState(true);
  const [accepting, setAccepting] = useState(false);
  const token = new URLSearchParams(window.location.search).get('token');

  useEffect(() => {
    if (token) {
      fetchInvitationDetails();
    }
  }, [token]);

  const fetchInvitationDetails = async () => {
    try {
      const response = await fetch(`/api/team/members/invitations/${token}`, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });

      if (response.ok) {
        const data = await response.json();
        setInvitation(data);
      } else {
        throw new Error('Invalid or expired invitation');
      }
    } catch (error) {
      alert('Error: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  const acceptInvitation = async () => {
    setAccepting(true);
    try {
      const response = await fetch(`/api/team/members/invitations/${token}/accept`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });

      if (response.ok) {
        alert('Welcome to the team! You have successfully joined the organization.');
        window.location.href = '/dashboard';
      } else {
        const error = await response.json();
        throw new Error(error.message || 'Failed to accept invitation');
      }
    } catch (error) {
      alert('Error: ' + error.message);
    } finally {
      setAccepting(false);
    }
  };

  if (loading) return <div>Loading invitation details...</div>;
  if (!invitation) return <div>Invalid invitation</div>;

  return (
    <div className="invitation-page">
      <div className="invitation-card">
        <h1>Team Invitation</h1>
        <p>You've been invited to join <strong>{invitation.organizationName}</strong></p>
        
        <div className="invitation-details">
          <div className="detail-row">
            <label>Role:</label>
            <span>{invitation.role}</span>
          </div>
          <div className="detail-row">
            <label>Email:</label>
            <span>{invitation.userEmail}</span>
          </div>
          <div className="detail-row">
            <label>Invited on:</label>
            <span>{new Date(invitation.invitedAt).toLocaleDateString()}</span>
          </div>
        </div>

        <div className="invitation-actions">
          <button 
            className="btn-primary" 
            onClick={acceptInvitation}
            disabled={accepting}
          >
            {accepting ? 'Accepting...' : 'Accept Invitation'}
          </button>
          <button className="btn-secondary" onClick={() => window.history.back()}>
            Decline
          </button>
        </div>
      </div>
    </div>
  );
};
```

### URL Structure for Email Links
```
https://your-frontend-domain.com/accept-invitation?token=abc123token456
```

---

## Security & Authentication

### JWT Token Requirements
All endpoints require a valid JWT token in the Authorization header:
```
Authorization: Bearer YOUR_JWT_TOKEN
```

### Organization Scope Security
- Users can only invite others to their own organization
- Invitations are automatically scoped to the authenticated user's organization
- Cross-organization invitations are prevented at the service layer

### Role-Based Access Control
```java
// Supported roles with descriptions
public enum Role {
    ORG_OWNER,           // Full organization control
    ORG_ADMIN,           // Organization administration
    ORG_MEMBER,          // Standard member access
    ORG_AUDITOR,         // Read-only auditing access
    ORG_REPORTER,        // Reports and analytics access
    ORG_BILLING,         // Billing management access
    PROJECT_ADMIN,       // Project administration
    PROJECT_COLLABORATOR // Project collaboration
}
```

### Token Security
- **Unique tokens** generated using UUID for each invitation
- **URL-safe format** suitable for email links
- **Single-use tokens** invalidated after acceptance
- **Expiration validation** prevents usage of expired tokens

---

## Error Handling

### Common Error Responses

#### 400 Bad Request
```json
{
  "message": "Failed to invite team member: User is already a team member",
  "timestamp": 1728138000000
}
```

#### 401 Unauthorized
```json
{
  "message": "Unauthorized access",
  "timestamp": 1728138000000
}
```

#### 404 Not Found
```json
{
  "message": "Failed to get invitation: Invalid or expired invitation token",
  "timestamp": 1728138000000
}
```

### Frontend Error Handling
```javascript
const handleApiCall = async (url, options) => {
  try {
    const response = await fetch(url, options);
    
    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || `HTTP ${response.status}`);
    }
    
    return await response.json();
  } catch (error) {
    console.error('API Error:', error);
    
    // Handle specific error types
    if (error.message.includes('already a team member')) {
      alert('This user is already part of your team.');
    } else if (error.message.includes('expired invitation')) {
      alert('This invitation has expired. Please request a new one.');
    } else {
      alert('An error occurred: ' + error.message);
    }
    
    throw error;
  }
};
```

---

## Deployment Notes

### Database Setup Required

⚠️ **Important**: Before deploying, ensure the database schema is updated.

#### Option 1: Auto-create tables (Development)
```properties
# In application-prod.properties
spring.jpa.hibernate.ddl-auto=update
```

#### Option 2: Manual table creation (Production)
Execute the SQL script provided in the [Database Schema](#database-schema) section.

#### Option 3: Database Migration (Recommended)
```sql
-- Run this migration script
CREATE TABLE team_member_invitations (
  -- [Full schema as shown above]
);
```

### Environment Variables
```bash
# Email Configuration
SMTP_HOST=smtp.hostinger.com
SMTP_USERNAME=noreply@berryfi.in
SMTP_PASSWORD=your_email_password

# JWT Configuration  
JWT_SECRET=your-very-secure-jwt-secret-key

# Application URLs
FRONTEND_URL=https://your-frontend-domain.com
```

### Health Check
After deployment, verify the system is working:
```bash
# Check application health
curl https://your-api-domain.com/actuator/health

# Test authentication endpoint
curl -X GET "https://your-api-domain.com/api/team/members" \
  -H "Authorization: Bearer VALID_JWT_TOKEN"
```

---

## Testing

### Manual Testing Checklist

#### ✅ Invitation Creation
- [ ] Send invitation with valid email and role
- [ ] Verify duplicate prevention
- [ ] Check email delivery
- [ ] Validate database record creation

#### ✅ Email System
- [ ] Verify email template rendering
- [ ] Check invitation link format
- [ ] Test email client compatibility
- [ ] Validate expiration date display

#### ✅ Invitation Acceptance
- [ ] Preview invitation details
- [ ] Accept valid invitation
- [ ] Test expired invitation handling
- [ ] Verify user creation for new accounts
- [ ] Check team member status update

#### ✅ Security Testing
- [ ] Test unauthorized access
- [ ] Verify organization scope restrictions
- [ ] Check token expiration handling
- [ ] Test invalid token responses

### Automated Testing Commands
```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=TeamMemberServiceTest

# Run integration tests
./mvnw test -Dtest=TeamMemberControllerIntegrationTest
```

---

## Support & Troubleshooting

### Common Issues

#### 1. "Schema-validation: missing table [team_member_invitations]"
**Solution**: Update database schema or change `hibernate.ddl-auto` to `update`

#### 2. "Communications link failure"
**Solution**: Verify database connection settings and ensure database server is running

#### 3. "Invalid or expired invitation token"
**Solution**: Check token format and expiration date in database

#### 4. "User is already a team member"
**Solution**: Expected behavior - system prevents duplicate memberships

### Logging Configuration
```properties
# Enable debug logging for troubleshooting
logging.level.com.berryfi.portal.service.TeamMemberService=DEBUG
logging.level.com.berryfi.portal.controller.TeamMemberController=DEBUG
```

### Support Contacts
- **Backend Issues**: Contact development team
- **Email Delivery Issues**: Check SMTP configuration and provider settings
- **Database Issues**: Verify connection settings and schema updates

---

## Changelog

### Version 1.0.0 (October 2025)
- ✅ Initial implementation of team invitation system
- ✅ Email-based invitations with token authentication
- ✅ Complete API endpoints for invitation management
- ✅ Professional email templates with organization branding
- ✅ Role-based access control with 8 user roles
- ✅ Integration with existing authentication system
- ✅ Comprehensive error handling and validation
- ✅ Database schema design and implementation
- ✅ Frontend integration examples and documentation

---

*This documentation covers the complete Team Invitation System implementation. For additional support or feature requests, please contact the development team.*