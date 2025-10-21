# Multi-Tenant Architecture Implementation Guide

## Overview

BerryFi now supports **project-level multi-tenancy** with subdomain-based routing. Each project gets its own subdomain (e.g., `myproject.berryfi.in`) with custom branding and isolated data access.

---

## 🏗️ Architecture Components

### Backend Components

1. **TenantContext** - Thread-local storage for tenant information per request
2. **TenantInterceptor** - Extracts tenant from `Host` header and sets context
3. **TenantService** - Resolves tenant by subdomain/custom domain
4. **TenantController** - API endpoints for frontend to fetch tenant configuration
5. **Project Entity** - Extended with subdomain and branding fields
6. **ProjectService** - Auto-generates unique subdomains on project creation

### How It Works

```
User visits: myproject.berryfi.in
     ↓
TenantInterceptor reads Host header
     ↓
TenantService resolves project by subdomain
     ↓
TenantContext stores project info for request
     ↓
All API calls automatically scoped to that project
```

---

## 🎯 Backend API Endpoints

### 1. Get Tenant Configuration (Context-based)

**Endpoint:** `GET /api/tenant/config`

**Description:** Returns tenant configuration based on current request's subdomain

**Response:**
```json
{
  "projectId": "proj_abc123",
  "projectName": "My Awesome Project",
  "subdomain": "myproject",
  "customDomain": null,
  "organizationId": "org_xyz789",
  "brandLogoUrl": "https://cdn.example.com/logo.png",
  "brandPrimaryColor": "#ff1136",
  "brandSecondaryColor": "#2d2d2d",
  "brandFaviconUrl": "https://cdn.example.com/favicon.ico",
  "brandAppName": "My Custom App"
}
```

**When to use:** After user authentication, or for any authenticated API call

---

### 2. Get Tenant Configuration by Subdomain

**Endpoint:** `GET /api/tenant/config/{subdomain}`

**Description:** Public endpoint to fetch tenant config before authentication

**Example:**
```bash
GET /api/tenant/config/myproject
```

**Response:** Same as above

**When to use:** On initial page load to fetch branding before user logs in

---

### 3. Check Subdomain Availability

**Endpoint:** `GET /api/tenant/check-subdomain?subdomain=myproject`

**Description:** Check if a subdomain is available for a new project

**Response:**
```json
{
  "subdomain": "myproject",
  "available": true
}
```

**When to use:** When creating a new project, to validate subdomain

---

## 💻 Frontend Implementation Guide

### Step 1: Detect Current Subdomain

Add this utility function to your React app:

```javascript
// utils/tenant.js

/**
 * Extract tenant subdomain from current hostname
 * @returns {string|null} subdomain or null if on main portal
 */
export function getTenantSubdomain() {
  const hostname = window.location.hostname;
  const parts = hostname.split('.');
  
  // localhost:5173 → development mode
  if (hostname === 'localhost' || hostname.startsWith('127.0')) {
    // For local development, you can use a query parameter
    const params = new URLSearchParams(window.location.search);
    return params.get('tenant') || null;
  }
  
  // berryfi.in or portal.berryfi.in → main portal
  if (parts.length === 2 || parts[0] === 'portal' || parts[0] === 'www') {
    return null;
  }
  
  // myproject.berryfi.in → tenant subdomain
  if (parts.length >= 3) {
    return parts[0];
  }
  
  return null;
}

/**
 * Check if current context is a tenant (not main portal)
 */
export function isTenantContext() {
  return getTenantSubdomain() !== null;
}
```

---

### Step 2: Fetch Tenant Configuration on App Load

```javascript
// hooks/useTenantConfig.js

import { useState, useEffect } from 'react';
import { getTenantSubdomain } from '../utils/tenant';
import api from '../services/api';

export function useTenantConfig() {
  const [config, setConfig] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  useEffect(() => {
    const fetchConfig = async () => {
      try {
        const subdomain = getTenantSubdomain();
        
        if (!subdomain) {
          // Main portal - use default config
          setConfig({
            projectName: 'BerryFi Portal',
            brandAppName: 'BerryFi Studio',
            brandPrimaryColor: '#ff1136',
            brandSecondaryColor: '#2d2d2d'
          });
          setLoading(false);
          return;
        }
        
        // Fetch tenant config by subdomain (public endpoint)
        const response = await api.get(`/api/tenant/config/${subdomain}`);
        setConfig(response.data);
        setLoading(false);
      } catch (err) {
        console.error('Error fetching tenant config:', err);
        setError(err);
        setLoading(false);
      }
    };
    
    fetchConfig();
  }, []);
  
  return { config, loading, error };
}
```

---

### Step 3: Apply Tenant Branding in Your App

```javascript
// App.jsx

import { useTenantConfig } from './hooks/useTenantConfig';
import { useEffect } from 'react';

function App() {
  const { config, loading } = useTenantConfig();
  
  useEffect(() => {
    if (!config) return;
    
    // Update document title
    document.title = config.brandAppName || 'BerryFi Studio';
    
    // Update favicon
    if (config.brandFaviconUrl) {
      const link = document.querySelector("link[rel~='icon']");
      if (link) link.href = config.brandFaviconUrl;
    }
    
    // Apply CSS custom properties for theme colors
    if (config.brandPrimaryColor) {
      document.documentElement.style.setProperty(
        '--primary-color', 
        config.brandPrimaryColor
      );
    }
    
    if (config.brandSecondaryColor) {
      document.documentElement.style.setProperty(
        '--secondary-color', 
        config.brandSecondaryColor
      );
    }
  }, [config]);
  
  if (loading) {
    return <div>Loading...</div>;
  }
  
  return (
    <div className="app">
      {/* Your app content */}
      <Header logo={config?.brandLogoUrl} appName={config?.brandAppName} />
      {/* ... */}
    </div>
  );
}
```

---

### Step 4: Theme Integration Example

```css
/* globals.css */

:root {
  --primary-color: #ff1136;
  --secondary-color: #2d2d2d;
}

/* These will be overridden by tenant config */
button.primary {
  background-color: var(--primary-color);
}

a {
  color: var(--primary-color);
}
```

---

### Step 5: Context Provider Pattern (Recommended)

```javascript
// contexts/TenantContext.jsx

import React, { createContext, useContext } from 'react';
import { useTenantConfig } from '../hooks/useTenantConfig';

const TenantContext = createContext();

export function TenantProvider({ children }) {
  const { config, loading, error } = useTenantConfig();
  
  return (
    <TenantContext.Provider value={{ config, loading, error }}>
      {children}
    </TenantContext.Provider>
  );
}

export function useTenant() {
  const context = useContext(TenantContext);
  if (!context) {
    throw new Error('useTenant must be used within TenantProvider');
  }
  return context;
}
```

```javascript
// main.jsx or index.jsx

import { TenantProvider } from './contexts/TenantContext';

ReactDOM.createRoot(document.getElementById('root')).render(
  <TenantProvider>
    <App />
  </TenantProvider>
);
```

---

## 🚀 Deployment Setup

### 1. DNS Configuration (Hostinger)

Add these DNS records in Hostinger:

| Type  | Name     | Value                              | TTL  |
|-------|----------|------------------------------------|------|
| CNAME | `portal` | `your-app.up.railway.app`          | 3600 |
| CNAME | `*`      | `your-app.up.railway.app`          | 3600 |

✅ This routes ALL subdomains to your Railway app

---

### 2. Railway Configuration

1. Go to your Railway project settings
2. Under "Domains", add:
   - `portal.berryfi.in`
   - `*.berryfi.in`

Railway will automatically issue wildcard SSL certificates.

---

### 3. Application Properties

Add to `application.properties`:

```properties
# Main domain for tenant resolution
app.domain=berryfi.in

# Main subdomain (portal/dashboard)
app.main-subdomain=portal

# Frontend URL (for CORS if needed)
app.frontend.url=https://portal.berryfi.in
```

---

## 🧪 Testing

### Local Development

Since you can't test subdomains locally, use query parameters:

```
http://localhost:5173?tenant=myproject
```

Your frontend code should check for this parameter in development mode.

---

### Testing on Staging

1. Create a test project in the admin panel
2. Note its auto-generated subdomain (e.g., `test-proj`)
3. Visit `https://test-proj.berryfi.in`
4. Verify tenant-specific branding loads

---

## 📊 Example User Flow

### Main Portal Flow

```
1. User visits: portal.berryfi.in
2. No tenant context set
3. Shows global project list
4. Can create new projects
```

### Tenant Project Flow

```
1. User visits: myproject.berryfi.in
2. TenantInterceptor sets project context
3. Tenant branding loads automatically
4. All API calls scoped to that project
5. User sees only that project's data
```

---

## 🔐 Security Considerations

1. **Tenant Isolation**: All data access is automatically filtered by project ID
2. **Authentication**: Users still need valid JWT tokens
3. **Authorization**: Existing role-based access control still applies
4. **CORS**: Configure CORS to allow `*.berryfi.in`

---

## 🎨 Branding Customization

Tenants can customize:

- **Logo URL**: Main app logo
- **Primary Color**: Buttons, links, accents
- **Secondary Color**: Text, backgrounds
- **Favicon URL**: Browser tab icon
- **App Name**: Title shown in UI

Update via Project settings in admin panel.

---

## 🔄 Custom Domain Support (Future)

To allow `myclient.com` → `myproject.berryfi.in`:

1. Customer adds CNAME: `myclient.com → berryfi.in`
2. Verify domain ownership via TXT record
3. Set `customDomain` and `customDomainVerified` in Project entity
4. Add domain to Railway
5. Backend automatically routes requests

---

## ⚙️ Backend Configuration Summary

### New Database Fields (Project table)

```sql
ALTER TABLE projects ADD COLUMN subdomain VARCHAR(63) UNIQUE;
ALTER TABLE projects ADD COLUMN custom_domain VARCHAR(255);
ALTER TABLE projects ADD COLUMN custom_domain_verified BOOLEAN DEFAULT FALSE;
ALTER TABLE projects ADD COLUMN brand_logo_url VARCHAR(500);
ALTER TABLE projects ADD COLUMN brand_primary_color VARCHAR(7);
ALTER TABLE projects ADD COLUMN brand_secondary_color VARCHAR(7);
ALTER TABLE projects ADD COLUMN brand_favicon_url VARCHAR(500);
ALTER TABLE projects ADD COLUMN brand_app_name VARCHAR(100);

CREATE INDEX idx_project_subdomain ON projects(subdomain);
```

---

## 📝 API Request Examples

### Authenticated Request (After Login)

```javascript
// The backend automatically knows which tenant based on Host header
const response = await fetch('https://myproject.berryfi.in/api/project/sessions', {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});

// Backend knows: projectId from TenantContext
```

### Initial Page Load (Before Login)

```javascript
// Fetch tenant config to customize login page
const subdomain = getTenantSubdomain();
const config = await fetch(`https://portal.berryfi.in/api/tenant/config/${subdomain}`);

// Apply branding to login page
applyTenantBranding(config);
```

---

## 🐛 Troubleshooting

### Issue: Subdomain not resolving

**Check:**
1. DNS propagation (wait 5-10 minutes)
2. Railway wildcard domain is configured
3. `app.domain` property matches your domain

---

### Issue: No tenant branding loading

**Check:**
1. Project has a subdomain set in database
2. Branding fields populated in Project entity
3. Frontend is calling correct API endpoint

---

### Issue: CORS errors on subdomain

**Update CORS configuration:**

```java
@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                    .allowedOrigins("https://*.berryfi.in")
                    .allowedMethods("*");
            }
        };
    }
}
```

---

## ✅ Checklist

### Backend Deployment
- [ ] Deploy to Railway with updated code
- [ ] Configure `app.domain` in properties
- [ ] Verify database migrations ran
- [ ] Add wildcard domain in Railway

### DNS Setup
- [ ] Add CNAME for `portal`
- [ ] Add wildcard CNAME for `*`
- [ ] Wait for DNS propagation
- [ ] Test DNS resolution

### Frontend Implementation
- [ ] Add subdomain detection utility
- [ ] Create tenant config hook
- [ ] Implement branding application
- [ ] Test on multiple subdomains
- [ ] Update CORS if needed

### Testing
- [ ] Create test project
- [ ] Visit tenant subdomain
- [ ] Verify branding loads
- [ ] Test API calls work correctly
- [ ] Test main portal still works

---

## 📞 Support

For questions or issues:
- Backend changes: Review `TenantService.java` and `TenantController.java`
- Frontend integration: Check `useTenantConfig` hook implementation
- DNS/Deployment: Verify Railway and Hostinger configurations

---

**Last Updated:** October 19, 2025
