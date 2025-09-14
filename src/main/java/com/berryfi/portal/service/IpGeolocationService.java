package com.berryfi.portal.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Service for IP geolocation using ip-api.com API.
 * Resolves IP addresses to geographic locations (country, city).
 * Includes caching to reduce API calls and respect rate limits.
 */
@Service
public class IpGeolocationService {

    private static final Logger logger = LoggerFactory.getLogger(IpGeolocationService.class);
    
    private static final String IP_API_URL = "http://ip-api.com/json/";
    private static final long CACHE_EXPIRY_MS = TimeUnit.HOURS.toMillis(24); // Cache for 24 hours
    private static final int MAX_CACHE_SIZE = 1000; // Prevent memory issues
    
    @Value("${app.geoip.enabled:true}")
    private boolean geoipEnabled;
    
    @Value("${app.geoip.api.timeout:5000}")
    private int apiTimeoutMs;
    
    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;
    
    // Simple in-memory cache to reduce API calls
    private final ConcurrentHashMap<String, CachedLocationInfo> locationCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void initialize() {
        if (!geoipEnabled) {
            logger.info("GeoIP service is disabled");
            return;
        }

        try {
            this.restTemplate = new RestTemplate();
            this.objectMapper = new ObjectMapper();
            
            // Configure timeout for the RestTemplate
            this.restTemplate.getMessageConverters().forEach(converter -> {
                if (converter instanceof org.springframework.http.converter.json.MappingJackson2HttpMessageConverter) {
                    ((org.springframework.http.converter.json.MappingJackson2HttpMessageConverter) converter)
                        .setObjectMapper(objectMapper);
                }
            });
            
            logger.info("GeoIP service initialized successfully with ip-api.com");
            
        } catch (Exception e) {
            logger.error("Failed to initialize GeoIP service: {}", e.getMessage());
            geoipEnabled = false;
        }
    }

    /**
     * Resolve IP address to location information using ip-api.com
     * 
     * @param ipAddress IP address to resolve
     * @return LocationInfo object with country and city, or null if resolution fails
     */
    public LocationInfo resolveLocation(String ipAddress) {
        if (!geoipEnabled || !StringUtils.hasText(ipAddress)) {
            return null;
        }

        // Skip localhost and private IP addresses
        if (isLocalOrPrivateIp(ipAddress)) {
            logger.debug("Skipping geolocation for local/private IP: {}", ipAddress);
            return null;
        }

        // Check cache first
        CachedLocationInfo cached = locationCache.get(ipAddress);
        if (cached != null && !cached.isExpired()) {
            return cached.getLocationInfo();
        }

        try {
            // Clean up cache if it's getting too large
            if (locationCache.size() > MAX_CACHE_SIZE) {
                cleanupExpiredCache();
            }

            // Make API call
            String url = IP_API_URL + ipAddress.trim() + "?fields=status,message,country,city";
            IpApiResponse response = restTemplate.getForObject(url, IpApiResponse.class);
            
            if (response != null && "success".equals(response.getStatus())) {
                LocationInfo locationInfo = new LocationInfo(response.getCountry(), response.getCity());
                
                // Cache the result
                locationCache.put(ipAddress, new CachedLocationInfo(locationInfo));
                
                return locationInfo;
            } else {
                logger.debug("Failed to resolve location for IP {}: {}", ipAddress, 
                           response != null ? response.getMessage() : "Unknown error");
            }
            
        } catch (Exception e) {
            logger.debug("Failed to resolve location for IP {}: {}", ipAddress, e.getMessage());
        }
        
        return null;
    }

    /**
     * Check if the GeoIP service is enabled and ready.
     */
    public boolean isEnabled() {
        return geoipEnabled;
    }

    /**
     * Check if IP address is localhost or private range
     */
    private boolean isLocalOrPrivateIp(String ip) {
        if (!StringUtils.hasText(ip)) {
            return true;
        }
        
        ip = ip.trim();
        
        // Localhost addresses
        if ("127.0.0.1".equals(ip) || "::1".equals(ip) || "localhost".equalsIgnoreCase(ip)) {
            return true;
        }
        
        // Private IP ranges (simplified check)
        if (ip.startsWith("192.168.") || 
            ip.startsWith("10.") || 
            ip.startsWith("172.")) {
            return true;
        }
        
        return false;
    }

    /**
     * Clean up expired entries from cache
     */
    private void cleanupExpiredCache() {
        locationCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        logger.debug("Cleaned up expired cache entries. Current cache size: {}", locationCache.size());
    }

    /**
     * Response model for ip-api.com API
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IpApiResponse {
        @JsonProperty("status")
        private String status;
        
        @JsonProperty("message")
        private String message;
        
        @JsonProperty("country")
        private String country;
        
        @JsonProperty("city")
        private String city;

        // Getters and setters
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
    }

    /**
     * Cached location information with expiration
     */
    private static class CachedLocationInfo {
        private final LocationInfo locationInfo;
        private final long timestamp;

        public CachedLocationInfo(LocationInfo locationInfo) {
            this.locationInfo = locationInfo;
            this.timestamp = System.currentTimeMillis();
        }

        public LocationInfo getLocationInfo() {
            return locationInfo;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_EXPIRY_MS;
        }
    }

    /**
     * Data class for location information.
     */
    public static class LocationInfo {
        private final String country;
        private final String city;

        public LocationInfo(String country, String city) {
            this.country = country;
            this.city = city;
        }

        public String getCountry() {
            return country;
        }

        public String getCity() {
            return city;
        }

        public boolean hasLocation() {
            return StringUtils.hasText(country) || StringUtils.hasText(city);
        }

        @Override
        public String toString() {
            if (StringUtils.hasText(city) && StringUtils.hasText(country)) {
                return city + ", " + country;
            } else if (StringUtils.hasText(city)) {
                return city;
            } else if (StringUtils.hasText(country)) {
                return country;
            }
            return "Unknown";
        }
    }
}
