# Tenant Configuration API - Structured DTO Pattern

## ✅ Updated Implementation

The tenant configuration now uses **structured DTOs** instead of JSON strings for better type safety and easier frontend integration.

---

## API Request Format

### Endpoint
```
PUT /api/projects/{projectId}
```

### Request Body (NEW Structure)
```json
{
  "name": "RAV Project",
  "description": "Project created RAV Rajpath Alpha",
  "config": "{\"vmSize\":\"Standard_B2s\",\"region\":\"eastus\",\"autoShutdown\":false,\"shutdownTime\":\"22:00\"}",
  "tenantConfig": {
    "subdomain": "rajpath",
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
      "sslCertificate": "",
      "dnsRecords": []
    },
    "urls": {
      "tenantUrl": "https://rajpath.berryfi.com",
      "customDomainUrl": "https://rajpath.example.com",
      "apiUrl": "https://api.berryfi.com",
      "websocketUrl": "wss://ws.berryfi.com"
    }
  }
}
```

### Key Changes from Previous Version:
- ❌ **OLD**: `"tenantConfig": "{\"subdomain\":\"rajpath\",\"branding\":{...}}"` (JSON string)
- ✅ **NEW**: `"tenantConfig": { "subdomain": "rajpath", "branding": {...} }` (Structured object)

---

## API Response Format

### Response Structure
```json
{
  "id": "proj_034d6bc8c68c",
  "name": "RAV Project",
  "description": "Project created RAV Rajpath Alpha",
  "status": "RUNNING",
  "config": "{...}",
  "branding": "{...}",
  "tenantConfig": {
    "subdomain": "rajpath",
    "branding": {
      "appName": "Rajpath Alpha",
      "primaryColor": "#2196F3",
      "secondaryColor": "#FFC107",
      "showPoweredBy": true
    },
    "customDomain": null,
    "urls": null
  },
  "createdAt": "2025-10-19T10:00:00",
  "updatedAt": "2025-10-19T12:42:00"
}
```

---

## TypeScript/Frontend Integration

### Request Type
```typescript
interface UpdateProjectRequest {
  name?: string;
  description?: string;
  config?: string;
  branding?: string;
  links?: string;
  tenantConfig?: TenantConfig;
}

interface TenantConfig {
  subdomain?: string;
  branding?: TenantBranding;
  customDomain?: CustomDomainConfig;
  urls?: TenantUrls;
}

interface TenantBranding {
  appName?: string;
  primaryColor?: string;
  secondaryColor?: string;
  logoUrl?: string;
  faviconUrl?: string;
  showPoweredBy?: boolean;
}

interface CustomDomainConfig {
  domain?: string;
  verified?: boolean;
  sslCertificate?: string;
  dnsRecords?: string[];
}

interface TenantUrls {
  tenantUrl?: string;
  customDomainUrl?: string;
  apiUrl?: string;
  websocketUrl?: string;
}
```

### Example Frontend Code
```typescript
// Update project with tenant configuration
const updateProject = async (projectId: string) => {
  const response = await fetch(`/api/projects/${projectId}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({
      name: "RAV Project",
      description: "Project created RAV Rajpath Alpha",
      tenantConfig: {
        subdomain: "rajpath",
        branding: {
          appName: "Rajpath Alpha",
          primaryColor: "#2196F3",
          secondaryColor: "#FFC107",
          showPoweredBy: true
        }
      }
    })
  });
  
  const project = await response.json();
  
  // Access tenant config directly
  console.log(project.tenantConfig.subdomain); // "rajpath"
  console.log(project.tenantConfig.branding.appName); // "Rajpath Alpha"
};
```

---

## Benefits of Structured DTO Pattern

### 1. **Type Safety** ✅
- Frontend gets full TypeScript type checking
- No need to manually parse JSON strings
- IDE autocomplete works perfectly

### 2. **Easier to Work With** ✅
```javascript
// OLD way (JSON string)
const config = JSON.parse(request.tenantConfig);
const subdomain = config.subdomain;

// NEW way (Structured object)
const subdomain = request.tenantConfig.subdomain;
```

### 3. **Better Validation** ✅
- Backend validates structure automatically
- Jackson handles deserialization errors
- Clear error messages for invalid data

### 4. **Cleaner API Documentation** ✅
- API docs show exact structure
- Examples are more readable
- Easier for frontend developers to understand

---

## Backend Implementation Details

### DTOs Used
1. **UpdateProjectRequest** - Accepts structured tenantConfig object
2. **ProjectTenantConfigDTO** - Structured DTO with nested classes:
   - TenantBranding
   - CustomDomainConfig
   - TenantUrls
3. **ProjectResponse** - Returns structured tenantConfig object

### Service Layer
```java
// ProjectService.updateProject()
if (request.getTenantConfig() != null) {
    ProjectTenantConfigDTO tenantConfig = request.getTenantConfig();
    
    // Direct access to nested properties
    if (tenantConfig.getSubdomain() != null) {
        project.setSubdomain(tenantConfig.getSubdomain());
    }
    
    if (tenantConfig.getBranding() != null) {
        project.setBrandAppName(tenantConfig.getBranding().getAppName());
        project.setBrandPrimaryColor(tenantConfig.getBranding().getPrimaryColor());
        // ...
    }
}
```

---

## Migration from Old API

If you were using the old JSON string format, update your requests:

### Before (JSON String)
```json
{
  "tenantConfig": "{\"subdomain\":\"rajpath\",\"branding\":{\"appName\":\"Rajpath Alpha\"}}"
}
```

### After (Structured Object)
```json
{
  "tenantConfig": {
    "subdomain": "rajpath",
    "branding": {
      "appName": "Rajpath Alpha"
    }
  }
}
```

---

## Testing

### cURL Example
```bash
curl -X PUT http://localhost:8080/api/projects/proj_034d6bc8c68c \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "RAV Project",
    "tenantConfig": {
      "subdomain": "rajpath",
      "branding": {
        "appName": "Rajpath Alpha",
        "primaryColor": "#2196F3",
        "secondaryColor": "#FFC107"
      }
    }
  }'
```

### Response
```json
{
  "id": "proj_034d6bc8c68c",
  "name": "RAV Project",
  "tenantConfig": {
    "subdomain": "rajpath",
    "branding": {
      "appName": "Rajpath Alpha",
      "primaryColor": "#2196F3",
      "secondaryColor": "#FFC107"
    }
  }
}
```

---

## Status
✅ **Implementation Complete**
- UpdateProjectRequest uses structured DTO
- ProjectResponse returns structured DTO
- ProjectService handles object directly (no JSON parsing)
- Application compiles and runs successfully

**Date:** October 19, 2025
