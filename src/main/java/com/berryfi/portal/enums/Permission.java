package com.berryfi.portal.enums;

/**
 * Enum representing permissions in the system.
 */
public enum Permission {
    // Authentication permissions
    AUTH_LOGIN("auth:login", "Login to the system"),
    AUTH_LOGOUT("auth:logout", "Logout from the system"),
    AUTH_REFRESH("auth:refresh", "Refresh access token"),
    AUTH_VIEW_PROFILE("auth:view_profile", "View own profile"),
    
    // User management permissions
    USER_VIEW("user:view", "View user information"),
    USER_CREATE("user:create", "Create new users"),
    USER_UPDATE("user:update", "Update user information"),
    USER_DELETE("user:delete", "Delete users"),
    USER_MANAGE_ROLES("user:manage_roles", "Manage user roles"),
    
    // Project permissions
    PROJECT_VIEW("project:view", "View projects"),
    PROJECT_CREATE("project:create", "Create new projects"),
    PROJECT_UPDATE("project:update", "Update project settings"),
    PROJECT_DELETE("project:delete", "Delete projects"),
    PROJECT_DEPLOY("project:deploy", "Deploy projects"),
    PROJECT_STOP("project:stop", "Stop projects"),
    PROJECT_MANAGE_CONFIG("project:manage_config", "Manage project configuration"),
    PROJECT_MANAGE_BRANDING("project:manage_branding", "Manage project branding"),
    PROJECT_MANAGE_LINKS("project:manage_links", "Manage project links"),
    PROJECT_VIEW_STATUS("project:view_status", "View project status"),
    
    // Billing permissions
    BILLING_VIEW_BALANCE("billing:view_balance", "View billing balance"),
    BILLING_VIEW_USAGE("billing:view_usage", "View billing usage"),
    BILLING_VIEW_TRANSACTIONS("billing:view_transactions", "View billing transactions"),
    BILLING_VIEW_PLANS("billing:view_plans", "View subscription plans"),
    BILLING_MANAGE_PAYMENT("billing:manage_payment", "Manage payment methods"),
    BILLING_VIEW_INVOICES("billing:view_invoices", "View invoices"),
    BILLING_MANAGE_SUPPORT("billing:manage_support", "Manage billing support"),
    
    // Team permissions
    TEAM_VIEW_MEMBERS("team:view_members", "View team members"),
    TEAM_MANAGE_MEMBERS("team:manage_members", "Manage team members"),
    TEAM_VIEW_CAMPAIGNS("team:view_campaigns", "View campaigns"),
    TEAM_CREATE_CAMPAIGNS("team:create_campaigns", "Create campaigns"),
    TEAM_UPDATE_CAMPAIGNS("team:update_campaigns", "Update campaigns"),
    TEAM_DELETE_CAMPAIGNS("team:delete_campaigns", "Delete campaigns"),
    TEAM_MANAGE_CAMPAIGNS("team:manage_campaigns", "Manage campaign operations"),
    TEAM_VIEW_LEADS("team:view_leads", "View leads"),
    TEAM_CREATE_LEADS("team:create_leads", "Create leads"),
    TEAM_UPDATE_LEADS("team:update_leads", "Update leads"),
    TEAM_MANAGE_LEADS("team:manage_leads", "Manage lead notes and status"),
    TEAM_VIEW_STATS("team:view_stats", "View team statistics"),
    
    // Workspace permissions
    WORKSPACE_VIEW("workspace:view", "View workspaces"),
    WORKSPACE_CREATE("workspace:create", "Create new workspaces"),
    WORKSPACE_UPDATE("workspace:update", "Update workspace settings"),
    WORKSPACE_DELETE("workspace:delete", "Delete workspaces"),
    WORKSPACE_MANAGE_CREDITS("workspace:manage_credits", "Manage workspace credits"),
    WORKSPACE_VIEW_STATS("workspace:view_stats", "View workspace statistics"),
    WORKSPACE_MANAGE_MEMBERS("workspace:manage_members", "Manage workspace members"),
    
    // Analytics permissions
    ANALYTICS_VIEW_USAGE("analytics:view_usage", "View usage analytics"),
    ANALYTICS_VIEW_LEADS("analytics:view_leads", "View lead analytics"),
    ANALYTICS_VIEW_GEOGRAPHIC("analytics:view_geographic", "View geographic analytics"),
    ANALYTICS_VIEW_DEVICES("analytics:view_devices", "View device analytics"),
    ANALYTICS_VIEW_NETWORK("analytics:view_network", "View network analytics"),
    ANALYTICS_VIEW_PROJECT_USAGE("analytics:view_project_usage", "View project usage analytics"),
    ANALYTICS_EXPORT_LEADS("analytics:export_leads", "Export lead data"),
    ANALYTICS_VIEW_JOURNEY("analytics:view_journey", "View lead journey"),
    
    // Audit permissions
    AUDIT_VIEW_LOGS("audit:view_logs", "View audit logs"),
    AUDIT_VIEW_STATS("audit:view_stats", "View audit statistics"),
    AUDIT_VIEW_USERS("audit:view_users", "View audit user data"),
    AUDIT_VIEW_ACTIONS("audit:view_actions", "View audit action types"),
    AUDIT_EXPORT("audit:export", "Export audit data"),
    
    // Usage permissions
    USAGE_VIEW_SESSIONS("usage:view_sessions", "View usage session logs"),
    USAGE_VIEW_STATS("usage:view_stats", "View usage statistics"),
    USAGE_VIEW_WORKSPACES("usage:view_workspaces", "View usage by workspaces"),
    USAGE_VIEW_MEMBERS("usage:view_members", "View usage by members"),
    USAGE_EXPORT("usage:export", "Export usage data"),
    
    // Reports permissions
    REPORTS_VIEW_DASHBOARD("reports:view_dashboard", "View dashboard reports"),
    REPORTS_VIEW_ANALYTICS("reports:view_analytics", "View analytics reports"),
    REPORTS_EXPORT("reports:export", "Export reports");

    private final String value;
    private final String description;

    Permission(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static Permission fromValue(String value) {
        for (Permission permission : Permission.values()) {
            if (permission.value.equals(value)) {
                return permission;
            }
        }
        throw new IllegalArgumentException("Invalid permission value: " + value);
    }
}
