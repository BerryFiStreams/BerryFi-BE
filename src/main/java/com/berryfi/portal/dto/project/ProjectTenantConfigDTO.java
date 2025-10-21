package com.berryfi.portal.dto.project;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Project Tenant Configuration
 * Contains subdomain, custom domain, and multi-tenant settings
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectTenantConfigDTO {
    
    /**
     * Project identifier
     */
    private String projectId;
    
    /**
     * Project name
     */
    private String projectName;
    
    /**
     * Unique subdomain for tenant access
     * Example: "myproject" → https://myproject.berryfi.in
     */
    private String subdomain;
    
    /**
     * Whether subdomain is available for registration
     */
    private Boolean subdomainAvailable;
    
    /**
     * Custom domain configuration
     */
    private CustomDomainConfig customDomain;
    
    /**
     * Tenant branding configuration
     */
    private TenantBranding branding;
    
    /**
     * Tenant access URLs
     */
    private TenantUrls urls;
    
    /**
     * Custom Domain Configuration
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CustomDomainConfig {
        /**
         * Custom domain name
         * Example: "app.myclient.com"
         */
        private String domain;
        
        /**
         * Whether domain ownership is verified
         */
        private Boolean verified;
        
        /**
         * Domain verification method
         * Options: "DNS_TXT", "DNS_CNAME", "FILE_UPLOAD"
         */
        private String verificationMethod;
        
        /**
         * Verification token/record value
         */
        private String verificationToken;
        
        /**
         * When domain was verified
         */
        private LocalDateTime verifiedAt;
        
        /**
         * SSL certificate status
         * Options: "PENDING", "ACTIVE", "EXPIRED", "FAILED"
         */
        private String sslStatus;
        
        /**
         * SSL certificate expiry date
         */
        private LocalDateTime sslExpiresAt;
    }
    
    /**
     * Tenant Branding Configuration
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TenantBranding {
        /**
         * Custom application name
         * Example: "My Gaming Studio"
         */
        private String appName;
        
        /**
         * Logo URL for header/navbar
         * Example: "https://cdn.example.com/logo.png"
         */
        private String logoUrl;
        
        /**
         * Favicon URL for browser tab
         * Example: "https://cdn.example.com/favicon.ico"
         */
        private String faviconUrl;
        
        /**
         * Primary brand color (hex format)
         * Example: "#ff1136"
         */
        private String primaryColor;
        
        /**
         * Secondary brand color (hex format)
         * Example: "#2d2d2d"
         */
        private String secondaryColor;
        
        /**
         * Whether to show "Powered by BerryFi" badge
         */
        private Boolean showPoweredBy;
    }
    
    /**
     * Tenant Access URLs
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TenantUrls {
        /**
         * Main tenant URL (subdomain-based)
         * Example: "https://myproject.berryfi.in"
         */
        private String tenantUrl;
        
        /**
         * Custom domain URL (if verified)
         * Example: "https://app.myclient.com"
         */
        private String customDomainUrl;
        
        /**
         * API base URL for this tenant
         * Example: "https://myproject.berryfi.in/api"
         */
        private String apiUrl;
        
        /**
         * WebSocket URL for this tenant
         * Example: "wss://myproject.berryfi.in/ws"
         */
        private String websocketUrl;
    }
}
