# Campaign API Documentation

## Base URL
```
/api/team/campaigns
```

## Authentication
All endpoints require authentication via JWT token:
- `Authorization`: Bearer token (required for all operations)

The JWT token contains user information including `userId` and `organizationId`, which are automatically extracted by the authentication filter and added as headers:
- `X-User-ID`: User identifier (automatically extracted from JWT)
- `X-Organization-ID`: Organization identifier (automatically extracted from JWT)

**Note:** You don't need to manually set these headers - they are automatically populated from your JWT token.

---

## Endpoints

### 1. Create Campaign
Creates a new campaign for an organization.

**Endpoint:** `POST /api/team/campaigns`

**Headers:**
- `Authorization`: Bearer <jwt-token> (required)
- `Content-Type`: application/json

**Request Body:**
```json
{
  "name": "Summer Sale Campaign",
  "customName": "summer-2025",
  "projectId": "proj-123",
  "accessType": "LEAD_CAPTURE",
  "description": "Campaign for summer promotional activities",
  "requireFirstName": true,
  "requireLastName": true,
  "requireEmail": true,
  "requirePhone": false,
  "enableOTP": false
}
```

**Request Parameters:**
- `name` (String, required): Campaign name
- `customName` (String, optional): Custom URL-friendly name
- `projectId` (String, required): Associated project ID
- `accessType` (String, required): Access type - `DIRECT` or `LEAD_CAPTURE`
- `description` (String, optional): Campaign description
- `requireFirstName` (Boolean, optional): Require first name in lead capture
- `requireLastName` (Boolean, optional): Require last name in lead capture
- `requireEmail` (Boolean, optional): Require email in lead capture
- `requirePhone` (Boolean, optional): Require phone in lead capture
- `enableOTP` (Boolean, optional): Enable OTP verification

**Success Response (201 Created):**
```json
{
  "id": "camp-789",
  "name": "Summer Sale Campaign",
  "customName": "summer-2025",
  "projectId": "proj-123",
  "projectName": "Main Website",
  "accessType": "LEAD_CAPTURE",
  "status": "ACTIVE",
  "description": "Campaign for summer promotional activities",
  "url": "https://berryfi.app/c/summer-2025",
  "requireFirstName": true,
  "requireLastName": true,
  "requireEmail": true,
  "requirePhone": false,
  "enableOTP": false,
  "visits": 0,
  "leads": 0,
  "conversions": 0,
  "conversionRate": 0.0,
  "organizationId": "org-456",
  "createdBy": "user-123",
  "createdAt": "2025-11-23T10:30:00",
  "updatedAt": "2025-11-23T10:30:00"
}
```

**Error Response (400 Bad Request):**
```json
{
  "error": "Bad Request",
  "message": "Invalid request parameters"
}
```

---

### 2. Get Campaign by ID
Retrieves a specific campaign by its ID.

**Endpoint:** `GET /api/team/campaigns/{campaignId}`

**Headers:**
- `Authorization`: Bearer <jwt-token> (required)

**Path Parameters:**
- `campaignId` (String, required): Campaign identifier

**Success Response (200 OK):**
```json
{
  "id": "camp-789",
  "name": "Summer Sale Campaign",
  "customName": "summer-2025",
  "projectId": "proj-123",
  "projectName": "Main Website",
  "accessType": "LEAD_CAPTURE",
  "status": "ACTIVE",
  "description": "Campaign for summer promotional activities",
  "url": "https://berryfi.app/c/summer-2025",
  "requireFirstName": true,
  "requireLastName": true,
  "requireEmail": true,
  "requirePhone": false,
  "enableOTP": false,
  "visits": 1250,
  "leads": 340,
  "conversions": 85,
  "conversionRate": 25.0,
  "organizationId": "org-456",
  "createdBy": "user-123",
  "createdAt": "2025-11-23T10:30:00",
  "updatedAt": "2025-11-23T15:45:00"
}
```

**Error Response (404 Not Found):**
```json
{
  "error": "Not Found",
  "message": "Campaign not found"
}
```

---

### 3. Get Campaigns (Paginated)
Retrieves all campaigns for an organization with pagination.

**Endpoint:** `GET /api/team/campaigns`

**Headers:**
- `Authorization`: Bearer <jwt-token> (required)

**Query Parameters:**
- `page` (Integer, optional, default: 0): Page number (0-indexed)
- `size` (Integer, optional, default: 20): Page size

**Example Request:**
```
GET /api/team/campaigns?page=0&size=10
```

**Success Response (200 OK):**
```json
{
  "content": [
    {
      "id": "camp-789",
      "name": "Summer Sale Campaign",
      "customName": "summer-2025",
      "projectId": "proj-123",
      "projectName": "Main Website",
      "accessType": "LEAD_CAPTURE",
      "status": "ACTIVE",
      "description": "Campaign for summer promotional activities",
      "url": "https://berryfi.app/c/summer-2025",
      "visits": 1250,
      "leads": 340,
      "conversions": 85,
      "conversionRate": 25.0,
      "organizationId": "org-456",
      "createdBy": "user-123",
      "createdAt": "2025-11-23T10:30:00",
      "updatedAt": "2025-11-23T15:45:00"
    }
  ],
  "pageable": {
    "sort": {
      "sorted": false,
      "unsorted": true,
      "empty": true
    },
    "pageNumber": 0,
    "pageSize": 10,
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalPages": 1,
  "totalElements": 1,
  "last": true,
  "first": true,
  "size": 10,
  "number": 0,
  "sort": {
    "sorted": false,
    "unsorted": true,
    "empty": true
  },
  "numberOfElements": 1,
  "empty": false
}
```

---

### 4. Search Campaigns
Searches campaigns by query string with pagination.

**Endpoint:** `GET /api/team/campaigns/search`

**Headers:**
- `Authorization`: Bearer <jwt-token> (required)

**Query Parameters:**
- `q` (String, required): Search query
- `page` (Integer, optional, default: 0): Page number (0-indexed)
- `size` (Integer, optional, default: 20): Page size

**Example Request:**
```
GET /api/team/campaigns/search?q=summer&page=0&size=10
```

**Success Response (200 OK):**
```json
{
  "content": [
    {
      "id": "camp-789",
      "name": "Summer Sale Campaign",
      "customName": "summer-2025",
      "projectId": "proj-123",
      "projectName": "Main Website",
      "accessType": "LEAD_CAPTURE",
      "status": "ACTIVE",
      "url": "https://berryfi.app/c/summer-2025",
      "visits": 1250,
      "leads": 340,
      "conversions": 85,
      "conversionRate": 25.0,
      "organizationId": "org-456",
      "createdAt": "2025-11-23T10:30:00",
      "updatedAt": "2025-11-23T15:45:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "offset": 0
  },
  "totalPages": 1,
  "totalElements": 1,
  "last": true,
  "first": true,
  "numberOfElements": 1,
  "empty": false
}
```

---

### 5. Get Campaigns by Project
Retrieves all campaigns associated with a specific project.

**Endpoint:** `GET /api/team/campaigns/project/{projectId}`

**Path Parameters:**
- `projectId` (String, required): Project identifier

**Query Parameters:**
- `page` (Integer, optional, default: 0): Page number (0-indexed)
- `size` (Integer, optional, default: 20): Page size

**Example Request:**
```
GET /api/team/campaigns/project/proj-123?page=0&size=10
```

**Success Response (200 OK):**
```json
{
  "content": [
    {
      "id": "camp-789",
      "name": "Summer Sale Campaign",
      "projectId": "proj-123",
      "projectName": "Main Website",
      "status": "ACTIVE",
      "visits": 1250,
      "leads": 340,
      "conversions": 85,
      "conversionRate": 25.0,
      "createdAt": "2025-11-23T10:30:00"
    }
  ],
  "totalPages": 1,
  "totalElements": 1
}
```

---

### 6. Get Active Campaigns
Retrieves all active campaigns for an organization (no pagination).

**Endpoint:** `GET /api/team/campaigns/active`

**Headers:**
- `Authorization`: Bearer <jwt-token> (required)

**Success Response (200 OK):**
```json
[
  {
    "id": "camp-789",
    "name": "Summer Sale Campaign",
    "customName": "summer-2025",
    "projectId": "proj-123",
    "projectName": "Main Website",
    "accessType": "LEAD_CAPTURE",
    "status": "ACTIVE",
    "url": "https://berryfi.app/c/summer-2025",
    "visits": 1250,
    "leads": 340,
    "conversions": 85,
    "conversionRate": 25.0,
    "organizationId": "org-456",
    "createdAt": "2025-11-23T10:30:00",
    "updatedAt": "2025-11-23T15:45:00"
  },
  {
    "id": "camp-790",
    "name": "Winter Campaign",
    "status": "ACTIVE",
    "visits": 890,
    "leads": 210,
    "conversions": 63,
    "conversionRate": 30.0
  }
]
```

---

### 7. Update Campaign
Updates an existing campaign.

**Endpoint:** `PUT /api/team/campaigns/{campaignId}`

**Headers:**
- `Authorization`: Bearer <jwt-token> (required)
- `Content-Type`: application/json

**Path Parameters:**
- `campaignId` (String, required): Campaign identifier

**Request Body:**
```json
{
  "name": "Summer Mega Sale Campaign",
  "customName": "summer-mega-2025",
  "accessType": "DIRECT",
  "status": "ACTIVE",
  "description": "Updated campaign description",
  "requireFirstName": false,
  "requireLastName": false,
  "requireEmail": true,
  "requirePhone": true,
  "enableOTP": true
}
```

**Request Parameters (all optional):**
- `name` (String, max 100 chars): Updated campaign name
- `customName` (String): Updated custom name
- `accessType` (String): Access type - `DIRECT` or `LEAD_CAPTURE`
- `status` (String): Campaign status - `ACTIVE`, `PAUSED`, or `INACTIVE`
- `description` (String, max 500 chars): Updated description
- `requireFirstName` (Boolean): Require first name in lead capture
- `requireLastName` (Boolean): Require last name in lead capture
- `requireEmail` (Boolean): Require email in lead capture
- `requirePhone` (Boolean): Require phone in lead capture
- `enableOTP` (Boolean): Enable OTP verification

**Success Response (200 OK):**
```json
{
  "id": "camp-789",
  "name": "Summer Mega Sale Campaign",
  "customName": "summer-mega-2025",
  "projectId": "proj-123",
  "projectName": "Main Website",
  "accessType": "DIRECT",
  "status": "ACTIVE",
  "description": "Updated campaign description",
  "url": "https://berryfi.app/c/summer-mega-2025",
  "requireEmail": true,
  "requirePhone": true,
  "enableOTP": true,
  "visits": 1250,
  "leads": 340,
  "conversions": 85,
  "conversionRate": 25.0,
  "organizationId": "org-456",
  "createdBy": "user-123",
  "createdAt": "2025-11-23T10:30:00",
  "updatedAt": "2025-11-23T16:20:00"
}
```

**Error Response (400 Bad Request):**
```json
{
  "error": "Bad Request",
  "message": "Invalid update parameters"
}
```

---

### 8. Delete Campaign
Deletes a campaign.

**Endpoint:** `DELETE /api/team/campaigns/{campaignId}`

**Headers:**
- `Authorization`: Bearer <jwt-token> (required)

**Path Parameters:**
- `campaignId` (String, required): Campaign identifier

**Success Response (204 No Content):**
No response body.

**Error Response (404 Not Found):**
```json
{
  "error": "Not Found",
  "message": "Campaign not found"
}
```

---

### 9. Record Visit
Records a visit to the campaign for analytics tracking.

**Endpoint:** `POST /api/team/campaigns/{campaignId}/visit`

**Path Parameters:**
- `campaignId` (String, required): Campaign identifier

**Success Response (200 OK):**
No response body.

**Error Response (404 Not Found):**
```json
{
  "error": "Not Found",
  "message": "Campaign not found"
}
```

---

### 10. Get Campaign Analytics
Retrieves aggregated analytics for all campaigns in an organization.

**Endpoint:** `GET /api/team/campaigns/analytics`

**Headers:**
- `Authorization`: Bearer <jwt-token> (required)

**Success Response (200 OK):**
```json
{
  "totalCampaigns": 15,
  "activeCampaigns": 8,
  "totalVisits": 12500,
  "totalLeads": 3200,
  "totalConversions": 950,
  "averageConversionRate": 29.68
}
```

**Response Fields:**
- `totalCampaigns` (Long): Total number of campaigns
- `activeCampaigns` (Long): Number of active campaigns
- `totalVisits` (Long): Total visits across all campaigns
- `totalLeads` (Long): Total leads captured
- `totalConversions` (Long): Total conversions
- `averageConversionRate` (Double): Average conversion rate (percentage)

---

### 11. Pause Campaign
Pauses an active campaign.

**Endpoint:** `POST /api/team/campaigns/{campaignId}/pause`

**Headers:**
- `Authorization`: Bearer <jwt-token> (required)

**Path Parameters:**
- `campaignId` (String, required): Campaign identifier

**Success Response (200 OK):**
```json
{
  "id": "camp-789",
  "name": "Summer Sale Campaign",
  "customName": "summer-2025",
  "projectId": "proj-123",
  "projectName": "Main Website",
  "accessType": "LEAD_CAPTURE",
  "status": "PAUSED",
  "description": "Campaign for summer promotional activities",
  "url": "https://berryfi.app/c/summer-2025",
  "visits": 1250,
  "leads": 340,
  "conversions": 85,
  "conversionRate": 25.0,
  "organizationId": "org-456",
  "createdBy": "user-123",
  "createdAt": "2025-11-23T10:30:00",
  "updatedAt": "2025-11-23T17:00:00"
}
```

**Error Response (404 Not Found):**
```json
{
  "error": "Not Found",
  "message": "Campaign not found"
}
```

---

### 12. Resume Campaign
Resumes a paused campaign.

**Endpoint:** `POST /api/team/campaigns/{campaignId}/resume`

**Headers:**
- `Authorization`: Bearer <jwt-token> (required)

**Path Parameters:**
- `campaignId` (String, required): Campaign identifier

**Success Response (200 OK):**
```json
{
  "id": "camp-789",
  "name": "Summer Sale Campaign",
  "customName": "summer-2025",
  "projectId": "proj-123",
  "projectName": "Main Website",
  "accessType": "LEAD_CAPTURE",
  "status": "ACTIVE",
  "description": "Campaign for summer promotional activities",
  "url": "https://berryfi.app/c/summer-2025",
  "visits": 1250,
  "leads": 340,
  "conversions": 85,
  "conversionRate": 25.0,
  "organizationId": "org-456",
  "createdBy": "user-123",
  "createdAt": "2025-11-23T10:30:00",
  "updatedAt": "2025-11-23T17:30:00"
}
```

**Error Response (404 Not Found):**
```json
{
  "error": "Not Found",
  "message": "Campaign not found"
}
```

---

### 13. Copy Campaign Link
Retrieves the campaign URL for sharing/copying.

**Endpoint:** `POST /api/team/campaigns/{campaignId}/copy-link`

**Headers:**
- `Authorization`: Bearer <jwt-token> (required)

**Path Parameters:**
- `campaignId` (String, required): Campaign identifier

**Success Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "url": "https://berryfi.app/c/summer-2025"
  }
}
```

**Error Response (404 Not Found):**
```json
{
  "error": "Not Found",
  "message": "Campaign not found"
}
```

---

## Data Models

### AccessType Enum
```
DIRECT          - Direct access without lead capture
LEAD_CAPTURE    - Requires lead information capture before access
```

### CampaignStatus Enum
```
ACTIVE    - Campaign is currently active
PAUSED    - Campaign is temporarily paused
INACTIVE  - Campaign is inactive
```

---

## Error Codes

| Status Code | Description |
|-------------|-------------|
| 200 | Success |
| 201 | Created successfully |
| 204 | No content (successful deletion) |
| 400 | Bad request - Invalid parameters |
| 404 | Not found - Campaign or resource doesn't exist |
| 500 | Internal server error |

---

## Notes

1. **Authentication**: All endpoints require a valid JWT token in the Authorization header. The token contains your user ID and organization ID, which are automatically extracted and used for authorization.

2. **Pagination**: Most list endpoints support pagination using `page` and `size` query parameters. Page numbers are 0-indexed.

3. **Lead Capture Configuration**: When `accessType` is set to `LEAD_CAPTURE`, the lead capture fields (`requireFirstName`, `requireLastName`, `requireEmail`, `requirePhone`, `enableOTP`) determine what information must be collected before granting access.

4. **Analytics**: Visit tracking is done through the `/visit` endpoint. Conversion rate is automatically calculated based on conversions/visits ratio.

5. **URL Generation**: Campaign URLs are automatically generated based on the `customName` field if provided, otherwise based on the campaign ID.

6. **Organization Isolation**: All campaigns are scoped to your organization (extracted from JWT token), ensuring data isolation between organizations.

7. **Timestamps**: All timestamps are in ISO 8601 format (e.g., `2025-11-23T10:30:00`).
