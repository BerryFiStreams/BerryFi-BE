package com.berryfi.portal.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Utility class for extracting client information from HTTP requests.
 * Handles common proxy headers and load balancer configurations.
 */
@Component
public class ClientInfoExtractor {

    private static final String[] IP_HEADERS = {
        "X-Forwarded-For",
        "X-Real-IP", 
        "X-Client-IP",
        "X-Forwarded",
        "X-Cluster-Client-IP",
        "Forwarded-For",
        "Forwarded"
    };

    /**
     * Extract the real client IP address from HTTP request.
     * Handles various proxy and load balancer configurations.
     * 
     * @param request HTTP servlet request
     * @return Client IP address or null if not found
     */
    public String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        // Check common proxy headers
        for (String header : IP_HEADERS) {
            String ip = extractIpFromHeader(request.getHeader(header));
            if (ip != null) {
                return ip;
            }
        }

        // Fallback to remote address
        String remoteAddr = request.getRemoteAddr();
        if (isValidIpAddress(remoteAddr)) {
            return remoteAddr;
        }

        return null;
    }

    /**
     * Extract User Agent from HTTP request.
     * 
     * @param request HTTP servlet request
     * @return User Agent string or null if not found
     */
    public String getUserAgent(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        
        String userAgent = request.getHeader("User-Agent");
        return StringUtils.hasText(userAgent) ? userAgent : null;
    }

    /**
     * Extract the first valid IP address from a header value.
     * Headers may contain multiple IPs separated by commas.
     */
    private String extractIpFromHeader(String header) {
        if (!StringUtils.hasText(header)) {
            return null;
        }

        // Handle multiple IPs (comma-separated)
        String[] ips = header.split(",");
        for (String ip : ips) {
            ip = ip.trim();
            if (isValidIpAddress(ip)) {
                return ip;
            }
        }

        return null;
    }

    /**
     * Validate if the given string is a valid IP address.
     * Excludes local/private addresses commonly used by proxies.
     */
    private boolean isValidIpAddress(String ip) {
        if (!StringUtils.hasText(ip)) {
            return false;
        }

        // Remove common proxy indicators
        ip = ip.trim();
        if ("unknown".equalsIgnoreCase(ip) || 
            "localhost".equalsIgnoreCase(ip) ||
            "127.0.0.1".equals(ip) ||
            "0:0:0:0:0:0:0:1".equals(ip) ||
            "::1".equals(ip)) {
            return false;
        }

        // Basic IP format validation (IPv4 and IPv6)
        return isValidIPv4(ip) || isValidIPv6(ip);
    }

    /**
     * Validate IPv4 address format.
     */
    private boolean isValidIPv4(String ip) {
        try {
            String[] parts = ip.split("\\.");
            if (parts.length != 4) {
                return false;
            }

            for (String part : parts) {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255) {
                    return false;
                }
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Basic IPv6 address format validation.
     */
    private boolean isValidIPv6(String ip) {
        // Simple check for IPv6 format (contains colons)
        return ip.contains(":") && ip.split(":").length >= 3;
    }
}
