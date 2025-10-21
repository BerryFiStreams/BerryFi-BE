package com.berryfi.portal.interceptor;

import com.berryfi.portal.context.TenantContext;
import com.berryfi.portal.entity.Project;
import com.berryfi.portal.service.TenantService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

/**
 * Interceptor that extracts tenant information from the Host header
 * and sets up the tenant context for the request.
 */
@Component
public class TenantInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(TenantInterceptor.class);

    @Autowired
    private TenantService tenantService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String host = request.getHeader("Host");
        String requestUri = request.getRequestURI();
        
        logger.debug("Processing request - Host: {}, URI: {}", host, requestUri);

        try {
            // Resolve tenant from hostname
            Optional<Project> projectOpt = tenantService.resolveTenantFromHostname(host);

            if (projectOpt.isPresent()) {
                Project project = projectOpt.get();
                String subdomain = project.getSubdomain() != null ? 
                                 project.getSubdomain() : 
                                 project.getCustomDomain();
                
                // Set tenant context
                tenantService.setTenantContext(project, subdomain);
                
                logger.info("Request on tenant subdomain '{}' mapped to project: {} (ID: {})", 
                           subdomain, project.getName(), project.getId());
            } else {
                // Main portal - no tenant context needed
                logger.debug("Request on main portal domain: {}", host);
                TenantContext.clear();
            }

            return true;
        } catch (Exception e) {
            logger.error("Error setting up tenant context for host {}: {}", host, e.getMessage(), e);
            // Don't block the request, just log the error
            TenantContext.clear();
            return true;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                               Object handler, Exception ex) {
        // Clean up tenant context after request completion
        TenantContext.clear();
        logger.debug("Tenant context cleared for request");
    }
}
