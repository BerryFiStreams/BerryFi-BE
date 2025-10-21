# Tracking URL Generation Flow

## Decision Tree

```
┌─────────────────────────────────────────┐
│  Generate Tracking URL for Project     │
│  (projectId, userId)                    │
└─────────────┬───────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────┐
│  Fetch Project from Database            │
└─────────────┬───────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────┐
│  buildProjectBaseUrl(project)           │
└─────────────┬───────────────────────────┘
              │
              ▼
       ┌──────┴──────┐
       │   Priority  │
       │   Check     │
       └──────┬──────┘
              │
              ▼
    ┌─────────────────┐
    │ Custom Domain?  │
    │   (verified)    │
    └────┬───────┬────┘
         │ YES   │ NO
         │       │
         ▼       ▼
    ┌─────┐  ┌─────────────┐
    │ USE │  │ Subdomain?  │
    │  ↓  │  └────┬───┬────┘
    │     │   YES │   │ NO
    │     │       │   │
    │     │       ▼   ▼
    │     │   ┌─────┐ ┌──────────┐
    │     │   │ USE │ │ Use Base │
    │     │   │  ↓  │ │   URL    │
    │     │   └─────┘ └─────┬────┘
    │     │       │          │
    └──┬──┘       │          │
       │          │          │
       └────┬─────┴──────────┘
            │
            ▼
┌─────────────────────────────────────────┐
│  Base URL Determined                    │
│  • https://app.myclient.com   OR        │
│  • https://myproject.berryfi.in   OR    │
│  • https://portal.berryfi.in            │
└─────────────┬───────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────┐
│  Generate Short Code                    │
│  (8 character alphanumeric)             │
└─────────────┬───────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────┐
│  Create TrackingLink Entity             │
│  • shortCode: "aB3dE7fG"                │
│  • projectId: "proj-123"                │
│  • userId: "user-456"                   │
│  • token: UUID                          │
│  • expiresAt: now + 365 days            │
└─────────────┬───────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────┐
│  Save to Database                       │
└─────────────┬───────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────┐
│  Return Tracking URL                    │
│  {baseUrl}/track/{shortCode}            │
│                                         │
│  Example:                               │
│  https://app.myclient.com/track/aB3dE7fG│
└─────────────────────────────────────────┘
```

## URL Resolution Examples

### Example 1: Verified Custom Domain (Highest Priority)
```
Input:
  Project {
    subdomain: "myproject"
    customDomain: "app.myclient.com"
    customDomainVerified: true
  }

Output:
  https://app.myclient.com/track/aB3dE7fG
```

### Example 2: Subdomain Only
```
Input:
  Project {
    subdomain: "myproject"
    customDomain: null
    customDomainVerified: false
  }

Output:
  https://myproject.berryfi.in/track/aB3dE7fG
```

### Example 3: Unverified Custom Domain (Falls back to Subdomain)
```
Input:
  Project {
    subdomain: "myproject"
    customDomain: "app.myclient.com"
    customDomainVerified: false  ← NOT VERIFIED
  }

Output:
  https://myproject.berryfi.in/track/aB3dE7fG
```

### Example 4: No Tenant Config (Default)
```
Input:
  Project {
    subdomain: null
    customDomain: null
    customDomainVerified: false
  }

Output:
  https://portal.berryfi.in/track/aB3dE7fG
```

## Access Flow (When User Clicks Tracking Link)

```
┌─────────────────────────────────────────┐
│  User Clicks Tracking Link             │
│  https://app.myclient.com/track/aB3dE7fG│
└─────────────┬───────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────┐
│  TrackingController receives request    │
│  GET /track/{trackingData}              │
└─────────────┬───────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────┐
│  Detect format: Short Code or Base64    │
└─────────────┬───────────────────────────┘
              │
              ▼
       ┌──────┴──────┐
       │   Is Short  │
       │    Code?    │
       └──┬────────┬─┘
    YES   │        │  NO
          │        │
          ▼        ▼
    ┌─────────┐  ┌──────────────┐
    │ Lookup  │  │ Decode Base64│
    │TrackLnk │  │ (Legacy)     │
    └────┬────┘  └──────┬───────┘
         │              │
         └──────┬───────┘
                │
                ▼
┌─────────────────────────────────────────┐
│  Get Project & User from TrackingLink   │
└─────────────┬───────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────┐
│  Create Audit Log Entry                 │
│  • userId, projectId                    │
│  • IP address, User-Agent               │
│  • Referrer URL                         │
│  • Timestamp                            │
└─────────────┬───────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────┐
│  Redirect to Project's Original URL     │
│  (project.getLinks())                   │
└─────────────────────────────────────────┘
```

## Integration Points

### 1. ProjectService
```java
// When listing/searching projects
Page<ProjectSummary> projects = projectRepository.findAll();
projects.map(project -> {
    // Generate tracking URL with project-specific base
    String trackingUrl = urlTrackingService.generateTrackingUrl(
        project,  // ← Pass entire project object
        currentUser.getId()
    );
    summary.setTrackingUrl(trackingUrl);
    return summary;
});
```

### 2. UrlTrackingService
```java
public String generateTrackingUrl(Project project, String userId) {
    // 1. Determine base URL
    String baseUrl = buildProjectBaseUrl(project);
    
    // 2. Generate short code
    String shortCode = generateUniqueShortCode();
    
    // 3. Create tracking link
    TrackingLink link = new TrackingLink(
        shortCode, 
        project.getId(), 
        userId, 
        generateToken()
    );
    trackingLinkRepository.save(link);
    
    // 4. Return URL
    return baseUrl + "/track/" + shortCode;
}
```

### 3. TrackingController
```java
@GetMapping("/{trackingData}")
public void trackAndRedirect(
    @PathVariable String trackingData,
    HttpServletRequest request,
    HttpServletResponse response
) {
    // Log the access
    urlTrackingService.trackShortUrlAccess(trackingData, ...);
    
    // Get project and redirect
    Project project = urlTrackingService.getProjectByShortCode(trackingData);
    response.sendRedirect(project.getLinks());
}
```

## Architecture Benefits

### 1. Decoupling
- Base URL determination is centralized in one method
- Easy to modify URL generation logic
- Single source of truth for priority rules

### 2. Performance
- Overloaded method accepts Project object
- Reduces redundant database queries
- Better efficiency for bulk operations

### 3. Flexibility
- Easy to add new URL resolution strategies
- Can support different URL schemes per project
- Extensible for future requirements

### 4. Maintainability
- Clear separation of concerns
- Well-documented priority logic
- Easy to test and debug
