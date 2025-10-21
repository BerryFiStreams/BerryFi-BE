# Tenant Configuration Implementation

## Overview
Successfully implemented tenant-specific configuration storage through the existing Project update API. This allows storing subdomain, branding, and custom domain information in a structured JSON format.

## API Endpoint
**PUT** `/api/projects/{projectId}`

## Request Payload Structure
```json
{
  "name": "RAV Project",
  "description": "Project created RAV Rajpath Alpha",
  "config": "{\"vmSize\":\"Standard_B2s\",\"region\":\"eastus\",\"autoShutdown\":false,\"shutdownTime\":\"22:00\"}",
  "tenantConfig": "{\"subdomain\":\"rajpath-alpha\",\"branding\":{\"appName\":\"Rajpath Alpha\",\"primaryColor\":\"#2196F3\",\"secondaryColor\":\"#FFC107\",\"showPoweredBy\":true},\"urls\":{}}"
}
```

## TenantConfig JSON Structure
```json
{
  "subdomain": "rajpath-alpha",
  "branding": {
    "appName": "Rajpath Alpha",
    "primaryColor": "#2196F3",
    "secondaryColor": "#FFC107",
    "logoUrl": "https://example.com/logo.png",
    "faviconUrl": "https://example.com/favicon.ico",
    "showPoweredBy": true
  },
  "customDomain": {
    "domain": "rajpath.example.com",
    "verified": false,
    "sslCertificate": "...",
    "dnsRecords": []
  },
  "urls": {
    "tenantUrl": "https://rajpath-alpha.berryfi.com",
    "customDomainUrl": "https://rajpath.example.com",
    "apiUrl": "https://api.berryfi.com",
    "websocketUrl": "wss://ws.berryfi.com"
  }
}
```

## Implementation Details

### 1. UpdateProjectRequest DTO
**File:** `src/main/java/com/berryfi/portal/dto/project/UpdateProjectRequest.java`

Added new field:
```java
private String tenantConfig;
```

### 2. ProjectTenantConfigDTO
**File:** `src/main/java/com/berryfi/portal/dto/project/ProjectTenantConfigDTO.java`

Structured DTO with nested classes for:
- `CustomDomainConfig`: domain, verified, sslCertificate, dnsRecords
- `TenantBranding`: appName, primaryColor, secondaryColor, logoUrl, faviconUrl, showPoweredBy
- `TenantUrls`: tenantUrl, customDomainUrl, apiUrl, websocketUrl

### 3. ProjectService Update Logic
**File:** `src/main/java/com/berryfi/portal/service/ProjectService.java`

#### Key Features:
1. **JSON Parsing**: Uses Jackson ObjectMapper to parse tenantConfig string into ProjectTenantConfigDTO
2. **Subdomain Validation**: 
   - Converts subdomain to lowercase
   - Checks if subdomain is already taken by another project
   - Throws `IllegalArgumentException` if duplicate found
3. **Branding Updates**: 
   - Updates brandAppName, brandPrimaryColor, brandSecondaryColor
   - Updates brandLogoUrl, brandFaviconUrl
4. **Custom Domain Updates**:
   - Updates customDomain and customDomainVerified fields
5. **Error Handling**: Catches JSON parsing errors and throws meaningful exceptions

## Database Fields Updated
When tenantConfig is provided, the following Project entity fields are updated:

| Field | Source | Description |
|-------|--------|-------------|
| `subdomain` | `tenantConfig.subdomain` | Unique subdomain identifier |
| `brandAppName` | `tenantConfig.branding.appName` | Application name for branding |
| `brandPrimaryColor` | `tenantConfig.branding.primaryColor` | Primary brand color (hex) |
| `brandSecondaryColor` | `tenantConfig.branding.secondaryColor` | Secondary brand color (hex) |
| `brandLogoUrl` | `tenantConfig.branding.logoUrl` | URL to brand logo |
| `brandFaviconUrl` | `tenantConfig.branding.faviconUrl` | URL to favicon |
| `customDomain` | `tenantConfig.customDomain.domain` | Custom domain URL |
| `customDomainVerified` | `tenantConfig.customDomain.verified` | Domain verification status |

## Validation Rules

### Subdomain Validation
- Converted to lowercase automatically
- Must be unique across all projects
- Throws error if duplicate: `"Subdomain 'rajpath-alpha' is already taken by another project"`

### JSON Validation
- Invalid JSON throws error: `"Invalid tenant configuration JSON: {error message}"`
- All fields are optional within tenantConfig

## Example Usage

### Updating Tenant Configuration
```bash
curl -X PUT http://localhost:8080/api/projects/proj_034d6bc8c68c \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "RAV Project",
    "tenantConfig": "{\"subdomain\":\"rajpath-alpha\",\"branding\":{\"appName\":\"Rajpath Alpha\",\"primaryColor\":\"#2196F3\",\"secondaryColor\":\"#FFC107\"}}"
  }'
```

### Response
```json
{
  "id": "proj_034d6bc8c68c",
  "name": "RAV Project",
  "subdomain": "rajpath-alpha",
  "brandAppName": "Rajpath Alpha",
  "brandPrimaryColor": "#2196F3",
  "brandSecondaryColor": "##FFC107",
  ...
}
```

## Testing

### Test Scenario 1: Create Tenant Config
1. Send PUT request with tenantConfig containing subdomain and branding
2. Verify subdomain is stored in lowercase
3. Verify branding fields are updated correctly
4. Check database to confirm all fields are persisted

### Test Scenario 2: Subdomain Conflict
1. Create project with subdomain "rajpath-alpha"
2. Try to update another project with same subdomain
3. Verify error response: `"Subdomain 'rajpath-alpha' is already taken"`

### Test Scenario 3: Invalid JSON
1. Send PUT request with malformed tenantConfig JSON
2. Verify error response: `"Invalid tenant configuration JSON: ..."`

### Test Scenario 4: Partial Updates
1. Update only branding without subdomain
2. Verify existing subdomain is not cleared
3. Update only subdomain without branding
4. Verify existing branding is not cleared

## Security Considerations

1. **Authorization**: Requires `hasPermission('project', 'update')` permission
2. **Subdomain Uniqueness**: Enforced at service layer to prevent duplicates
3. **Input Validation**: JSON parsing errors are caught and returned as user-friendly messages
4. **XSS Prevention**: Color codes and URLs should be sanitized on frontend

## Future Enhancements

1. **Subdomain Format Validation**: Add regex validation for subdomain format (alphanumeric, hyphens only)
2. **Custom Domain Verification**: Implement DNS verification flow
3. **SSL Certificate Management**: Add logic to provision and renew SSL certificates
4. **Bulk Updates**: Support updating multiple projects at once
5. **Audit Logging**: Track tenant config changes for compliance

## Files Modified

1. ✅ `src/main/java/com/berryfi/portal/dto/project/UpdateProjectRequest.java`
2. ✅ `src/main/java/com/berryfi/portal/dto/project/ProjectTenantConfigDTO.java` (created)
3. ✅ `src/main/java/com/berryfi/portal/service/ProjectService.java`
4. ✅ `src/main/java/com/berryfi/portal/config/SecurityConfig.java` (tenant endpoints made public)

## Status
✅ **Implementation Complete**
✅ **Compilation Successful**
✅ **Application Running**

---
**Date:** October 19, 2025
**Version:** 1.0
