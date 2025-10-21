# Frontend Quick Start - Multi-Tenant Implementation

## 🚀 Quick Implementation Steps

### Step 1: Install Dependencies (if needed)
No additional dependencies required! Uses standard `fetch` API.

---

### Step 2: Create Tenant Utility File

Create `src/utils/tenant.js`:

```javascript
/**
 * Get current tenant subdomain from URL
 * Returns null if on main portal
 */
export const getTenantSubdomain = () => {
  const hostname = window.location.hostname;
  
  // Development mode
  if (hostname === 'localhost' || hostname.startsWith('127.0')) {
    // Use ?tenant=xxx for local testing
    const params = new URLSearchParams(window.location.search);
    return params.get('tenant');
  }
  
  const parts = hostname.split('.');
  
  // Main portal (portal.berryfi.in or berryfi.in)
  if (parts.length === 2 || parts[0] === 'portal' || parts[0] === 'www') {
    return null;
  }
  
  // Tenant subdomain (myproject.berryfi.in)
  return parts[0];
};

export const isTenantContext = () => getTenantSubdomain() !== null;
```

---

### Step 3: Create Tenant Hook

Create `src/hooks/useTenantConfig.js`:

```javascript
import { useState, useEffect } from 'react';
import { getTenantSubdomain } from '../utils/tenant';

const DEFAULT_CONFIG = {
  projectName: 'BerryFi Portal',
  brandAppName: 'BerryFi Studio',
  brandPrimaryColor: '#ff1136',
  brandSecondaryColor: '#2d2d2d'
};

export const useTenantConfig = () => {
  const [config, setConfig] = useState(null);
  const [loading, setLoading] = useState(true);
  
  useEffect(() => {
    const fetchConfig = async () => {
      try {
        const subdomain = getTenantSubdomain();
        
        if (!subdomain) {
          setConfig(DEFAULT_CONFIG);
          setLoading(false);
          return;
        }
        
        const response = await fetch(
          `${import.meta.env.VITE_API_URL}/api/tenant/config/${subdomain}`
        );
        
        if (!response.ok) {
          throw new Error('Failed to fetch tenant config');
        }
        
        const data = await response.json();
        setConfig(data);
      } catch (error) {
        console.error('Error fetching tenant config:', error);
        setConfig(DEFAULT_CONFIG);
      } finally {
        setLoading(false);
      }
    };
    
    fetchConfig();
  }, []);
  
  return { config, loading };
};
```

---

### Step 4: Apply Branding in App Component

Update your `src/App.jsx`:

```javascript
import { useEffect } from 'react';
import { useTenantConfig } from './hooks/useTenantConfig';

function App() {
  const { config, loading } = useTenantConfig();
  
  useEffect(() => {
    if (!config) return;
    
    // Update page title
    document.title = config.brandAppName || 'BerryFi Studio';
    
    // Update favicon
    if (config.brandFaviconUrl) {
      let link = document.querySelector("link[rel~='icon']");
      if (!link) {
        link = document.createElement('link');
        link.rel = 'icon';
        document.head.appendChild(link);
      }
      link.href = config.brandFaviconUrl;
    }
    
    // Apply theme colors
    const root = document.documentElement;
    if (config.brandPrimaryColor) {
      root.style.setProperty('--color-primary', config.brandPrimaryColor);
    }
    if (config.brandSecondaryColor) {
      root.style.setProperty('--color-secondary', config.brandSecondaryColor);
    }
  }, [config]);
  
  if (loading) {
    return (
      <div className="loading-screen">
        <div className="spinner" />
        <p>Loading...</p>
      </div>
    );
  }
  
  return (
    <div className="app">
      {/* Your app components */}
      <Header logo={config?.brandLogoUrl} />
      {/* ... rest of your app */}
    </div>
  );
}

export default App;
```

---

### Step 5: Update Your CSS for Dynamic Theme

Add to your `src/index.css` or `src/App.css`:

```css
:root {
  /* Default colors (will be overridden by tenant config) */
  --color-primary: #ff1136;
  --color-secondary: #2d2d2d;
}

/* Use these variables in your styles */
.btn-primary {
  background-color: var(--color-primary);
  color: white;
}

.btn-primary:hover {
  filter: brightness(0.9);
}

a {
  color: var(--color-primary);
}

.header {
  background-color: var(--color-secondary);
}
```

---

### Step 6: Update Header Component

```javascript
// src/components/Header.jsx

function Header({ logo }) {
  const { config } = useTenantConfig();
  
  return (
    <header className="header">
      <div className="logo-container">
        {logo ? (
          <img src={logo} alt={config?.brandAppName} className="logo" />
        ) : (
          <h1>{config?.brandAppName || 'BerryFi Studio'}</h1>
        )}
      </div>
      {/* ... rest of header */}
    </header>
  );
}
```

---

## 🧪 Testing Locally

### Method 1: Query Parameter

Visit: `http://localhost:5173?tenant=myproject`

The `getTenantSubdomain()` function will read the `tenant` parameter in dev mode.

---

### Method 2: Update /etc/hosts (Advanced)

Add to `/etc/hosts`:
```
127.0.0.1 myproject.localhost
127.0.0.1 test.localhost
```

Then visit: `http://myproject.localhost:5173`

Update your utility function to handle `.localhost`:

```javascript
export const getTenantSubdomain = () => {
  const hostname = window.location.hostname;
  
  if (hostname.endsWith('.localhost')) {
    return hostname.split('.')[0];
  }
  
  // ... rest of code
};
```

---

## 📡 API Calls with Tenant Context

Good news! You don't need to do anything special. The backend automatically knows which tenant based on the hostname:

```javascript
// This automatically works for tenant-specific data
const response = await fetch('/api/projects', {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});

// Backend uses TenantContext to filter results
```

---

## 🎨 Advanced: Context Provider Pattern

For better state management:

```javascript
// src/contexts/TenantContext.jsx

import { createContext, useContext } from 'react';
import { useTenantConfig } from '../hooks/useTenantConfig';

const TenantContext = createContext(null);

export function TenantProvider({ children }) {
  const tenantData = useTenantConfig();
  return (
    <TenantContext.Provider value={tenantData}>
      {children}
    </TenantContext.Provider>
  );
}

export const useTenant = () => {
  const context = useContext(TenantContext);
  if (!context) {
    throw new Error('useTenant must be used within TenantProvider');
  }
  return context;
};
```

Wrap your app:

```javascript
// src/main.jsx

import { TenantProvider } from './contexts/TenantContext';

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <TenantProvider>
      <App />
    </TenantProvider>
  </React.StrictMode>
);
```

---

## 🔐 Environment Variables

Add to your `.env`:

```env
VITE_API_URL=https://portal.berryfi.in
```

For production with subdomains:

```env
VITE_API_URL=https://portal.berryfi.in
```

Note: API URL stays the same even for tenant subdomains. The backend handles tenant routing.

---

## ✅ Checklist

- [ ] Created `utils/tenant.js`
- [ ] Created `hooks/useTenantConfig.js`
- [ ] Updated `App.jsx` to apply branding
- [ ] Updated CSS with CSS variables
- [ ] Updated Header component
- [ ] Tested locally with query parameter
- [ ] Verified theme colors apply correctly
- [ ] Verified logo and favicon load

---

## 🐛 Common Issues

### Issue: "Failed to fetch tenant config"

**Solution:** Check API URL in `.env` file and ensure backend is running.

---

### Issue: Theme colors not applying

**Solution:** Make sure CSS variables are defined in root:

```css
:root {
  --color-primary: #ff1136;
}
```

And used in styles:

```css
.btn {
  background: var(--color-primary);
}
```

---

### Issue: Logo not showing

**Solution:** Check:
1. `config.brandLogoUrl` is not null/undefined
2. URL is accessible (CORS enabled)
3. Image element has proper error handling

---

## 📞 Need Help?

Check the full documentation: `MULTI_TENANT_GUIDE.md`

Backend API Endpoints:
- `GET /api/tenant/config` - Get config for current subdomain
- `GET /api/tenant/config/{subdomain}` - Get config by subdomain
- `GET /api/tenant/check-subdomain?subdomain=xxx` - Check availability

---

**Ready to go!** 🚀

All backend changes are complete and tested. Follow these steps to implement on the frontend and you'll have full multi-tenant support!
