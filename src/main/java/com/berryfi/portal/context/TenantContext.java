package com.berryfi.portal.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread-local context for storing tenant information for the current request.
 * This allows tenant-aware operations throughout the request lifecycle.
 */
public class TenantContext {

    private static final Logger logger = LoggerFactory.getLogger(TenantContext.class);

    private static final ThreadLocal<String> currentProjectId = new ThreadLocal<>();
    private static final ThreadLocal<String> currentProjectName = new ThreadLocal<>();
    private static final ThreadLocal<String> currentSubdomain = new ThreadLocal<>();
    private static final ThreadLocal<String> currentOrganizationId = new ThreadLocal<>();

    /**
     * Set the current tenant subdomain (e.g., "myproject" from "myproject.berryfi.in").
     */
    public static void setTenantSubdomain(String subdomain) {
        logger.debug("Setting tenant subdomain: {}", subdomain);
        currentSubdomain.set(subdomain);
    }

    /**
     * Get the current tenant subdomain.
     */
    public static String getTenantSubdomain() {
        return currentSubdomain.get();
    }

    /**
     * Set the current project ID for this tenant.
     */
    public static void setProjectId(String projectId) {
        logger.debug("Setting project ID: {}", projectId);
        currentProjectId.set(projectId);
    }

    /**
     * Get the current project ID for this tenant.
     */
    public static String getProjectId() {
        return currentProjectId.get();
    }

    /**
     * Set the current project name.
     */
    public static void setProjectName(String projectName) {
        logger.debug("Setting project name: {}", projectName);
        currentProjectName.set(projectName);
    }

    /**
     * Get the current project name.
     */
    public static String getProjectName() {
        return currentProjectName.get();
    }

    /**
     * Set the current organization ID for this tenant.
     */
    public static void setOrganizationId(String organizationId) {
        logger.debug("Setting organization ID: {}", organizationId);
        currentOrganizationId.set(organizationId);
    }

    /**
     * Get the current organization ID for this tenant.
     */
    public static String getOrganizationId() {
        return currentOrganizationId.get();
    }

    /**
     * Check if a tenant context is currently set.
     */
    public static boolean hasTenantContext() {
        return currentProjectId.get() != null;
    }

    /**
     * Clear all tenant context for the current thread.
     * Should be called after request processing is complete.
     */
    public static void clear() {
        logger.debug("Clearing tenant context");
        currentProjectId.remove();
        currentProjectName.remove();
        currentOrganizationId.remove();
        currentSubdomain.remove();
    }

    /**
     * Get a summary of the current tenant context (for logging/debugging).
     */
    public static String getContextSummary() {
        return String.format("Tenant[subdomain=%s, projectId=%s, projectName=%s, orgId=%s]",
                currentSubdomain.get(),
                currentProjectId.get(),
                currentProjectName.get(),
                currentOrganizationId.get());
    }
}
