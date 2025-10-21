# Tracking URL Subdomain/Custom Domain Implementation

## Overview
Updated the tracking URL generation system to use the project's subdomain or custom domain as the base URL instead of the default application base URL.

## Changes Made

### 1. Updated `UrlTrackingService.java`

#### Added Configuration
- Added `@Value("${app.domain}")` to inject the application domain (e.g., `berryfi.in`)

#### New Helper Method: `buildProjectBaseUrl(Project project)`
This method determines the appropriate base URL for a project with the following priority:

1. **Custom Domain (if verified)**: `https://{customDomain}`
   - Example: `https://app.myclient.com`
   
2. **Subdomain**: `https://{subdomain}.{appDomain}`
   - Example: `https://myproject.berryfi.in`
   
3. **Default Base URL**: Falls back to `app.base-url` configuration
   - Example: `https://portal.berryfi.in`

#### Updated Methods

**`generateTrackingUrl(String projectId, String userId)`**
- Now fetches the project from database
- Calls `buildProjectBaseUrl()` to determine the correct base URL
- Generates tracking URL using project-specific base URL

**New Overload: `generateTrackingUrl(Project project, String userId)`**
- Accepts Project object directly to avoid redundant database queries
- More efficient when the project is already loaded
- Used by `ProjectService` for better performance

### 2. Updated `ProjectService.java`

Updated two methods that generate tracking URLs:

#### `getProjects()` method (Line ~160)
```java
// OLD: Used project ID
String trackingUrl = urlTrackingService.generateTrackingUrl(
    project.getId(), currentUser.getId());

// NEW: Uses project object directly
String trackingUrl = urlTrackingService.generateTrackingUrl(
    project, currentUser.getId());
```

#### `searchProjects()` method (Line ~434)
```java
// OLD: Used project ID
String trackingUrl = urlTrackingService.generateTrackingUrl(
    project.getId(), currentUser.getId());

// NEW: Uses project object directly
String trackingUrl = urlTrackingService.generateTrackingUrl(
    project, currentUser.getId());
```

## How It Works

### Example Scenarios

#### Scenario 1: Project with Verified Custom Domain
```
Project:
  - id: "proj-123"
  - subdomain: "myproject"
  - customDomain: "app.myclient.com"
  - customDomainVerified: true

Generated Tracking URL:
  https://app.myclient.com/track/aB3dE7fG
```

#### Scenario 2: Project with Subdomain (No Custom Domain)
```
Project:
  - id: "proj-456"
  - subdomain: "awesomeapp"
  - customDomain: null
  - customDomainVerified: false

Generated Tracking URL:
  https://awesomeapp.berryfi.in/track/xY9zK2mN
```

#### Scenario 3: Project without Subdomain or Custom Domain
```
Project:
  - id: "proj-789"
  - subdomain: null
  - customDomain: null
  - customDomainVerified: false

Generated Tracking URL:
  https://portal.berryfi.in/track/pQ4rS8tU
```

## Benefits

### 1. **Multi-Tenant URL Support**
- Each project can have its own branded URL
- Tracking URLs match the tenant's domain/subdomain
- Better white-labeling support

### 2. **User Experience**
- Users see tracking links that match their project's domain
- More professional appearance for clients using custom domains
- Consistent branding throughout the tracking flow

### 3. **Performance Optimization**
- New overload method reduces redundant database queries
- Project object is reused when already loaded
- More efficient for bulk operations (list/search)

### 4. **Priority Logic**
- Smart fallback mechanism ensures URLs always work
- Verified custom domains take precedence
- Subdomain fallback for standard tenants
- Default URL fallback for projects without tenant configuration

## Configuration

Required in `application.properties`:
```properties
# Application base URL (fallback)
app.base-url=https://portal.berryfi.in

# Application domain for subdomains
app.domain=berryfi.in
```

## Database Fields Used

From `Project` entity:
- `subdomain` - Unique subdomain identifier (e.g., "myproject")
- `customDomain` - Custom domain URL (e.g., "app.myclient.com")
- `customDomainVerified` - Boolean flag indicating domain verification status

## Testing

### Manual Testing Steps

1. **Test with Custom Domain (Verified)**
   - Create a project with verified custom domain
   - Generate tracking URL
   - Verify URL uses custom domain: `https://app.myclient.com/track/{code}`

2. **Test with Subdomain**
   - Create a project with subdomain
   - Generate tracking URL
   - Verify URL uses subdomain: `https://myproject.berryfi.in/track/{code}`

3. **Test without Tenant Configuration**
   - Create a project without subdomain/custom domain
   - Generate tracking URL
   - Verify URL uses default base: `https://portal.berryfi.in/track/{code}`

4. **Test Custom Domain Priority**
   - Create a project with both subdomain AND verified custom domain
   - Generate tracking URL
   - Verify custom domain takes precedence

### API Endpoints to Test

- `GET /api/projects` - Returns list with tracking URLs
- `GET /api/projects/search?keyword={keyword}` - Returns search results with tracking URLs
- `GET /api/projects/{projectId}` - Returns single project details

### Expected Response
```json
{
  "id": "proj-123",
  "name": "My Project",
  "subdomain": "myproject",
  "customDomain": "app.myclient.com",
  "customDomainVerified": true,
  "trackingUrl": "https://app.myclient.com/track/aB3dE7fG",
  ...
}
```

## Backward Compatibility

✅ **Fully backward compatible**
- Projects without subdomain/custom domain continue to work
- Existing tracking links remain functional
- No breaking changes to API responses
- Default behavior maintained for legacy projects

## Future Enhancements

1. **HTTPS Protocol Detection**
   - Currently hardcoded to `https://`
   - Could detect protocol from configuration or request

2. **URL Scheme Configuration**
   - Allow per-project URL scheme configuration
   - Support for http/https/custom protocols

3. **Subdomain Validation**
   - Add validation for subdomain format
   - Prevent conflicts with reserved subdomains

4. **Custom Domain SSL Verification**
   - Integrate SSL certificate verification
   - Automated HTTPS availability checks

## Notes

- The tracking controller (`/track/{code}`) must be accessible from all domains (subdomain + custom domains)
- CORS configuration may need updates to support custom domains
- DNS records must be properly configured for custom domains to work
- SSL certificates must be configured for custom domains

## Related Files

- `/src/main/java/com/berryfi/portal/service/UrlTrackingService.java`
- `/src/main/java/com/berryfi/portal/service/ProjectService.java`
- `/src/main/java/com/berryfi/portal/entity/Project.java`
- `/src/main/java/com/berryfi/portal/controller/TrackingController.java`
- `/src/main/resources/application.properties`
