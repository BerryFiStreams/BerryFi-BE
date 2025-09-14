# IP Tracking Setup Guide

This guide explains how to set up IP address tracking and geolocation for VM sessions in BerryFi.

## Features Added

1. **Client IP Address Tracking**: Captures the real client IP address, handling proxy headers and load balancers
2. **Geographic Location Resolution**: Resolves IP addresses to country and city using ip-api.com API
3. **User Agent Tracking**: Records the browser/client user agent string
4. **Username Tracking**: Stores the user's display name/username
5. **Smart Caching**: Caches API responses for 24 hours to reduce API calls and improve performance

## Database Changes

The following fields have been added to the `vm_sessions` table:
- `username` - Display name of the user
- `client_ip_address` - Client's IP address (IPv4/IPv6)
- `client_country` - Country resolved from IP
- `client_city` - City resolved from IP  
- `user_agent` - Browser user agent string

## IP Geolocation Service (API-based)

This implementation uses **ip-api.com** for geolocation:

### ✅ **Advantages**
- **No Database Files**: No need to download/manage MaxMind database files
- **Always Up-to-Date**: API data is constantly updated
- **Zero Setup**: Works out of the box
- **Free Tier**: 1,000 requests/month, 45 requests/minute
- **Automatic Caching**: 24-hour cache reduces API calls

### 📊 **Usage Limits**
- **Free Plan**: 1,000 requests/month, 45 requests/minute
- **Paid Plans**: 
  - Pro: $13/month for 150,000 requests
  - Business: $50/month for 500,000 requests
  - Enterprise: Custom pricing

### 💰 **Cost Estimation**
- Small app (100 sessions/month): **FREE**
- Medium app (1,000 sessions/month): **FREE** (with caching)
- Large app (10,000 sessions/month): **$13/month** Pro plan

## Configuration

Update `application.properties`:

```properties
# IP Geolocation Configuration (API-based)
app.geoip.enabled=true
app.geoip.api.timeout=5000
```

### Optional: Custom API Provider

To switch to a different API provider (like IPStack, Abstract API), modify `IpGeolocationService.java`:

```java
private static final String IP_API_URL = "https://api.ipstack.com/";
// Add your API key parameter
```

## Alternative API Providers

### 1. **ip-api.com** (Current Implementation)
- **Free**: 1,000 requests/month, 45/minute
- **URL**: `http://ip-api.com/json/{ip}`
- **No API Key Required**

### 2. **IPStack** (Alternative)
- **Free**: 1,000 requests/month  
- **Paid**: From $10/month
- **URL**: `https://api.ipstack.com/{ip}?access_key=YOUR_KEY`
- **Requires API Key**

### 3. **Abstract API** (Alternative)
- **Free**: 1,000 requests/month
- **Paid**: From $9/month  
- **URL**: `https://ipgeolocation.abstractapi.com/v1/?api_key=YOUR_KEY&ip_address={ip}`
- **Requires API Key**

## API Response Changes

The VM session API responses now include client tracking information:

```json
{
  "sessionId": "sess_123abc456def",
  "vmInstanceId": "vm_789xyz",
  "status": "RUNNING",
  "username": "john.doe",
  "clientIpAddress": "203.0.113.42", 
  "clientCountry": "United States",
  "clientCity": "New York",
  "userAgent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36...",
  "startedAt": "2025-09-14T12:30:00",
  "durationSeconds": 1800,
  "creditsUsed": 30.0
}
```

## Privacy Considerations

- IP addresses are stored for session tracking and security purposes
- Geographic data is resolved from IP addresses but the database doesn't leave the server
- User agents help identify client types for analytics and troubleshooting
- Consider implementing data retention policies for IP address data
- Ensure compliance with privacy regulations (GDPR, CCPA, etc.)

## Testing IP Tracking

### Development Testing

For local development, IP tracking will capture localhost addresses. To test with real IP addresses:

1. **Using ngrok** (recommended for testing):
   ```bash
   # Install ngrok and expose your local server
   ngrok http 8080
   
   # Access your API through the ngrok URL to simulate external requests
   curl -X POST https://your-ngrok-url.ngrok.io/api/vm/sessions/start \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer your-jwt-token" \
     -d '{"projectId": "proj123", "vmType": "medium"}'
   ```

2. **Mock IP Headers** for proxy testing:
   ```bash
   curl -X POST http://localhost:8080/api/vm/sessions/start \
     -H "X-Forwarded-For: 8.8.8.8" \
     -H "X-Real-IP: 8.8.8.8" \
     -H "User-Agent: Mozilla/5.0 Test Browser" \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer your-jwt-token" \
     -d '{"projectId": "proj123", "vmType": "medium"}'
   ```

3. **Test with Known IP Addresses**:
   ```bash
   # Test with Google DNS (should resolve to United States)  
   curl -X POST http://localhost:8080/api/vm/sessions/start \
     -H "X-Forwarded-For: 8.8.8.8" \
     ...
   
   # Test with Cloudflare DNS (should resolve to Australia)
   curl -X POST http://localhost:8080/api/vm/sessions/start \
     -H "X-Forwarded-For: 1.1.1.1" \
     ...
   ```

### Production Validation

In production, verify that:
- Real client IPs are being captured (not load balancer IPs)
- Geographic resolution is working for international users
- Proxy headers are being handled correctly
- User agents are being captured properly
- API rate limits are being respected

## Troubleshooting

### API Geolocation Not Working

1. **Check service status**:
   ```bash
   # Check application logs for initialization messages
   tail -f application.log | grep -i geoip
   ```

2. **Test API directly**:
   ```bash
   # Test ip-api.com directly
   curl "http://ip-api.com/json/8.8.8.8?fields=status,message,country,city"
   
   # Expected response:
   # {"status":"success","country":"United States","city":"Mountain View"}
   ```

3. **Check API rate limits**:
   ```bash
   # Monitor for 429 (Too Many Requests) errors
   # ip-api.com free tier: 45 requests per minute
   tail -f application.log | grep -i "429\|rate limit"
   ```

4. **Verify network connectivity**:
   ```bash
   # Test outbound HTTP connectivity
   curl -I http://ip-api.com
   ```

### Common Issues

1. **Empty Location Data**: 
   - Some IP ranges don't have city/country data - this is normal
   - Private/localhost IPs are automatically skipped

2. **Rate Limit Exceeded**: 
   - API returns HTTP 429
   - Check cache configuration
   - Consider upgrading to paid plan

3. **API Timeouts**: 
   - Increase `app.geoip.api.timeout` value
   - Check network connectivity to ip-api.com

4. **Localhost IPs**: 
   - Local development will show localhost addresses
   - Use ngrok or deployed environment for testing

### Performance Optimization

1. **Cache Statistics**:
   ```java
   // Add to IpGeolocationService for monitoring
   public int getCacheSize() {
       return locationCache.size();
   }
   ```

2. **Rate Limiting**:
   - Current implementation: 45 requests/minute (free tier)
   - Cache prevents excessive API calls
   - Consider implementing request queuing for high-volume apps

3. **Failover Strategy**:
   ```java
   // Consider implementing multiple API providers as backup
   if (primaryApiDown) {
       return backupGeolocationService.resolveLocation(ip);
   }
   ```

### API Upgrade Path

**When to Upgrade from Free Tier:**
- More than 1,000 unique IPs per month
- Need higher rate limits (>45 requests/minute) 
- Require HTTPS API endpoints
- Need commercial license terms

**Paid Plan Features:**
- **Pro ($13/month)**: 150,000 requests, HTTPS, commercial use
- **Business ($50/month)**: 500,000 requests, priority support
- **Enterprise**: Custom limits, SLA, dedicated support

## Security & Privacy Notes

- **No External Database Files**: All geolocation happens via API calls
- **Automatic Caching**: Reduces API calls and improves privacy
- **Private IP Filtering**: Localhost and private IPs are automatically skipped
- **Rate Limiting**: Built-in protection against API abuse
- **Data Retention**: Consider implementing policies for IP address data
- **Compliance**: Ensure compliance with privacy regulations (GDPR, CCPA, etc.)
- **API Security**: ip-api.com doesn't require API keys, reducing credential exposure

## Database Migration

The migration `V015__Add_client_tracking_to_vm_sessions.sql` will automatically:
- Add the new columns to existing `vm_sessions` table
- Create appropriate indexes for performance
- Handle existing data gracefully (new columns will be NULL for existing sessions)

Run the migration with:
```bash
./mvnw flyway:migrate
```

## Quick Setup Summary

1. ✅ **Database Migration**: Run `./mvnw flyway:migrate`
2. ✅ **Configuration**: Already included in `application.properties`
3. ✅ **No Additional Setup**: API-based solution works out of the box
4. ✅ **Test**: Use ngrok or proxy headers to test with real IPs
5. ✅ **Monitor**: Check logs for API rate limits and errors

That's it! No database files to download or manage. The system is ready to track IP addresses and resolve locations automatically.
