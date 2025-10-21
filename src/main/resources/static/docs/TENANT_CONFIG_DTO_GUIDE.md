# Tenant Configuration DTO - Usage Guide

## Overview

`ProjectTenantConfigDTO` is the **correct DTO** for managing multi-tenant configuration including:
- ✅ Subdomain management
- ✅ Custom domain configuration & verification
- ✅ Tenant branding (colors, logo, favicon)
- ✅ Tenant-specific URLs

This DTO maps to the fields added by the Flyway migration.

---

## 📦 DTO Structure

```java
ProjectTenantConfigDTO
├── projectId (String)
├── projectName (String)
├── subdomain (String) ← Main tenant identifier
├── subdomainAvailable (Boolean)
├── customDomain (CustomDomainConfig)
│   ├── domain
│   ├── verified
│   ├── verificationMethod
│   ├── verificationToken
│   ├── verifiedAt
│   ├── sslStatus
│   └── sslExpiresAt
├── branding (TenantBranding)
│   ├── appName
│   ├── logoUrl
│   ├── faviconUrl
│   ├── primaryColor
│   ├── secondaryColor
│   └── showPoweredBy
└── urls (TenantUrls)
    ├── tenantUrl
    ├── customDomainUrl
    ├── apiUrl
    └── websocketUrl
```

---

## 🎯 Use Cases

### 1. Get Tenant Configuration (Frontend)

When a user visits `https://myproject.berryfi.in`, the frontend needs to fetch tenant config:

```java
@GetMapping("/api/tenant/config")
public ProjectTenantConfigDTO getTenantConfig() {
    // Get project from TenantContext (set by TenantInterceptor)
    String projectId = TenantContext.getProjectId();
    String subdomain = TenantContext.getTenantSubdomain();
    
    // Fetch project from database
    Project project = projectRepository.findById(projectId)
        .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    
    // Build tenant config DTO
    return ProjectTenantConfigDTO.builder()
        .projectId(project.getId())
        .projectName(project.getName())
        .subdomain(project.getSubdomain())
        .customDomain(buildCustomDomainConfig(project))
        .branding(buildTenantBranding(project))
        .urls(buildTenantUrls(project))
        .build();
}

private ProjectTenantConfigDTO.CustomDomainConfig buildCustomDomainConfig(Project project) {
    if (project.getCustomDomain() == null) {
        return null;
    }
    
    return ProjectTenantConfigDTO.CustomDomainConfig.builder()
        .domain(project.getCustomDomain())
        .verified(project.getCustomDomainVerified())
        .sslStatus("ACTIVE") // Check actual SSL status
        .build();
}

private ProjectTenantConfigDTO.TenantBranding buildTenantBranding(Project project) {
    return ProjectTenantConfigDTO.TenantBranding.builder()
        .appName(project.getBrandAppName() != null ? project.getBrandAppName() : "BerryFi Studio")
        .logoUrl(project.getBrandLogoUrl())
        .faviconUrl(project.getBrandFaviconUrl())
        .primaryColor(project.getBrandPrimaryColor() != null ? project.getBrandPrimaryColor() : "#ff1136")
        .secondaryColor(project.getBrandSecondaryColor() != null ? project.getBrandSecondaryColor() : "#2d2d2d")
        .showPoweredBy(true) // Based on subscription plan
        .build();
}

private ProjectTenantConfigDTO.TenantUrls buildTenantUrls(Project project) {
    String baseUrl = "https://" + project.getSubdomain() + ".berryfi.in";
    
    return ProjectTenantConfigDTO.TenantUrls.builder()
        .tenantUrl(baseUrl)
        .customDomainUrl(project.getCustomDomainVerified() ? 
            "https://" + project.getCustomDomain() : null)
        .apiUrl(baseUrl + "/api")
        .websocketUrl("wss://" + project.getSubdomain() + ".berryfi.in/ws")
        .build();
}
```

**Response Example:**
```json
{
  "projectId": "abc123",
  "projectName": "My Gaming Studio",
  "subdomain": "my-gaming-studio",
  "customDomain": {
    "domain": "app.mygamingstudio.com",
    "verified": true,
    "sslStatus": "ACTIVE"
  },
  "branding": {
    "appName": "Epic Gaming Portal",
    "logoUrl": "https://cdn.example.com/logo.png",
    "faviconUrl": "https://cdn.example.com/favicon.ico",
    "primaryColor": "#ff1136",
    "secondaryColor": "#2d2d2d",
    "showPoweredBy": false
  },
  "urls": {
    "tenantUrl": "https://my-gaming-studio.berryfi.in",
    "customDomainUrl": "https://app.mygamingstudio.com",
    "apiUrl": "https://my-gaming-studio.berryfi.in/api",
    "websocketUrl": "wss://my-gaming-studio.berryfi.in/ws"
  }
}
```

---

### 2. Check Subdomain Availability

When creating a project, check if subdomain is available:

```java
@GetMapping("/api/tenant/check-subdomain")
public ProjectTenantConfigDTO checkSubdomainAvailability(@RequestParam String subdomain) {
    boolean available = !projectRepository.existsBySubdomain(subdomain);
    
    return ProjectTenantConfigDTO.builder()
        .subdomain(subdomain)
        .subdomainAvailable(available)
        .build();
}
```

**Request:**
```http
GET /api/tenant/check-subdomain?subdomain=my-project
```

**Response:**
```json
{
  "subdomain": "my-project",
  "subdomainAvailable": true
}
```

---

### 3. Update Tenant Branding

Admin can update tenant branding:

```java
@PutMapping("/api/projects/{projectId}/tenant/branding")
public ProjectTenantConfigDTO updateTenantBranding(
    @PathVariable String projectId,
    @RequestBody ProjectTenantConfigDTO.TenantBranding branding,
    @AuthenticationPrincipal User currentUser
) {
    Project project = projectRepository.findById(projectId)
        .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    
    // Update branding fields
    project.setBrandAppName(branding.getAppName());
    project.setBrandLogoUrl(branding.getLogoUrl());
    project.setBrandFaviconUrl(branding.getFaviconUrl());
    project.setBrandPrimaryColor(branding.getPrimaryColor());
    project.setBrandSecondaryColor(branding.getSecondaryColor());
    
    projectRepository.save(project);
    
    return getTenantConfig(); // Return updated config
}
```

**Request:**
```http
PUT /api/projects/abc123/tenant/branding
Content-Type: application/json

{
  "appName": "Epic Gaming Portal",
  "logoUrl": "https://cdn.example.com/logo.png",
  "primaryColor": "#ff1136",
  "secondaryColor": "#2d2d2d"
}
```

---

### 4. Add Custom Domain

Admin can configure custom domain:

```java
@PostMapping("/api/projects/{projectId}/tenant/custom-domain")
public ProjectTenantConfigDTO addCustomDomain(
    @PathVariable String projectId,
    @RequestBody ProjectTenantConfigDTO.CustomDomainConfig domainConfig,
    @AuthenticationPrincipal User currentUser
) {
    Project project = projectRepository.findById(projectId)
        .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    
    // Set custom domain (not verified yet)
    project.setCustomDomain(domainConfig.getDomain());
    project.setCustomDomainVerified(false);
    
    projectRepository.save(project);
    
    // Generate verification token
    String verificationToken = generateVerificationToken();
    
    // Return config with verification instructions
    ProjectTenantConfigDTO.CustomDomainConfig responseConfig = 
        ProjectTenantConfigDTO.CustomDomainConfig.builder()
            .domain(domainConfig.getDomain())
            .verified(false)
            .verificationMethod("DNS_TXT")
            .verificationToken(verificationToken)
            .build();
    
    return ProjectTenantConfigDTO.builder()
        .projectId(project.getId())
        .customDomain(responseConfig)
        .build();
}

private String generateVerificationToken() {
    return "berryfi-verify-" + UUID.randomUUID().toString();
}
```

**Request:**
```http
POST /api/projects/abc123/tenant/custom-domain
Content-Type: application/json

{
  "domain": "app.myclient.com"
}
```

**Response:**
```json
{
  "projectId": "abc123",
  "customDomain": {
    "domain": "app.myclient.com",
    "verified": false,
    "verificationMethod": "DNS_TXT",
    "verificationToken": "berryfi-verify-a1b2c3d4-e5f6-7890"
  }
}
```

**User Action:**
Add DNS TXT record:
```
_berryfi-challenge.app.myclient.com  TXT  "berryfi-verify-a1b2c3d4-e5f6-7890"
```

---

### 5. Verify Custom Domain

After user adds DNS record:

```java
@PostMapping("/api/projects/{projectId}/tenant/custom-domain/verify")
public ProjectTenantConfigDTO verifyCustomDomain(
    @PathVariable String projectId,
    @AuthenticationPrincipal User currentUser
) {
    Project project = projectRepository.findById(projectId)
        .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    
    // Check DNS TXT record
    boolean verified = verifyDnsTxtRecord(project.getCustomDomain());
    
    if (verified) {
        project.setCustomDomainVerified(true);
        projectRepository.save(project);
        
        // TODO: Issue SSL certificate via Railway/Let's Encrypt
    }
    
    return getTenantConfig();
}

private boolean verifyDnsTxtRecord(String domain) {
    // Use DNS lookup library to check TXT record
    // Return true if verification token found
    return true; // Simplified
}
```

---

## 🔄 Mapping to Database Fields

| DTO Field | Database Column | Notes |
|-----------|----------------|-------|
| `subdomain` | `subdomain` | Auto-generated on project creation |
| `customDomain.domain` | `custom_domain` | User-provided |
| `customDomain.verified` | `custom_domain_verified` | Boolean flag |
| `branding.appName` | `brand_app_name` | Custom app name |
| `branding.logoUrl` | `brand_logo_url` | Logo URL |
| `branding.faviconUrl` | `brand_favicon_url` | Favicon URL |
| `branding.primaryColor` | `brand_primary_color` | Hex color |
| `branding.secondaryColor` | `brand_secondary_color` | Hex color |

---

## 🎨 Frontend Integration

### React Example

```typescript
interface TenantConfig {
  projectId: string;
  projectName: string;
  subdomain: string;
  branding: {
    appName: string;
    logoUrl?: string;
    faviconUrl?: string;
    primaryColor: string;
    secondaryColor: string;
  };
  urls: {
    tenantUrl: string;
    customDomainUrl?: string;
    apiUrl: string;
    websocketUrl: string;
  };
}

// Fetch tenant config on app load
async function loadTenantConfig() {
  const response = await fetch('/api/tenant/config');
  const config: TenantConfig = await response.json();
  
  // Apply branding
  document.title = config.branding.appName;
  document.documentElement.style.setProperty('--primary-color', config.branding.primaryColor);
  document.documentElement.style.setProperty('--secondary-color', config.branding.secondaryColor);
  
  if (config.branding.faviconUrl) {
    const favicon = document.querySelector('link[rel="icon"]');
    favicon?.setAttribute('href', config.branding.faviconUrl);
  }
  
  return config;
}
```

---

## ✅ Summary

**ProjectTenantConfigDTO is for:**
- ✅ Subdomain configuration
- ✅ Custom domain setup & verification
- ✅ Tenant branding (colors, logos)
- ✅ Tenant-specific URLs

**Maps directly to:**
- Database migration fields (`subdomain`, `custom_domain`, `brand_*`)
- TenantService logic
- TenantController endpoints

**Use this DTO when:**
- Fetching tenant configuration for frontend
- Checking subdomain availability
- Updating tenant branding
- Configuring custom domains
- Returning tenant-specific URLs

This is the **correct DTO** for your multi-tenant architecture! 🎯
