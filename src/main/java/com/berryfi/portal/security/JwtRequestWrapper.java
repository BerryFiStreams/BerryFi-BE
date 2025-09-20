package com.berryfi.portal.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.*;

/**
 * Custom HTTP request wrapper to inject JWT claims as headers.
 */
public class JwtRequestWrapper extends HttpServletRequestWrapper {

    private final Map<String, String> customHeaders;

    public JwtRequestWrapper(HttpServletRequest request) {
        super(request);
        this.customHeaders = new HashMap<>();
    }

    public void putHeader(String name, String value) {
        this.customHeaders.put(name, value);
    }

    @Override
    public String getHeader(String name) {
        // Check if it's a custom header first
        String headerValue = customHeaders.get(name);
        if (headerValue != null) {
            return headerValue;
        }
        
        // Check if the value is in request attributes (set by JWT filter)
        Object attributeValue = getAttribute(name);
        if (attributeValue instanceof String) {
            return (String) attributeValue;
        }
        
        // Fall back to original request header
        return super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        // Get original header names
        Set<String> headerNames = new HashSet<>(Collections.list(super.getHeaderNames()));
        
        // Add custom header names
        headerNames.addAll(customHeaders.keySet());
        
        // Add attribute names that are used as headers
        Enumeration<String> attributeNames = getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String name = attributeNames.nextElement();
            if (name.startsWith("X-")) {
                headerNames.add(name);
            }
        }
        
        return Collections.enumeration(headerNames);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        List<String> values = new ArrayList<>();
        
        // Add custom header value
        String customValue = customHeaders.get(name);
        if (customValue != null) {
            values.add(customValue);
        }
        
        // Check if the value is in request attributes
        Object attributeValue = getAttribute(name);
        if (attributeValue instanceof String && customValue == null) {
            values.add((String) attributeValue);
        }
        
        // Add original header values
        Enumeration<String> originalHeaders = super.getHeaders(name);
        while (originalHeaders.hasMoreElements()) {
            String value = originalHeaders.nextElement();
            if (!values.contains(value)) {
                values.add(value);
            }
        }
        
        return Collections.enumeration(values);
    }
}
