package com.berryfi.portal.service;

import com.berryfi.portal.entity.User;
import com.berryfi.portal.enums.Permission;
import com.berryfi.portal.enums.Role;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Set;

/**
 * Service for managing user permissions based on roles.
 */
@Service
public class PermissionService {

    /**
     * Get all permissions for a specific role.
     */
    public Set<Permission> getPermissionsForRole(Role role) {
        return switch (role) {
            case SUPER_ADMIN -> getAllPermissions();
            case ORG_OWNER -> getOrganizationOwnerPermissions();
            case ORG_ADMIN -> getOrganizationAdminPermissions();
            case ORG_AUDITOR -> getOrganizationAuditorPermissions();
            case ORG_REPORTER -> getOrganizationReporterPermissions();
            case ORG_BILLING -> getOrganizationBillingPermissions();
            case ORG_MEMBER -> getOrganizationMemberPermissions();
            case WORKSPACE_ADMIN -> getWorkspaceAdminPermissions();
            case WORKSPACE_AUDITOR -> getWorkspaceAuditorPermissions();
            case WORKSPACE_REPORTER -> getWorkspaceReporterPermissions();
            case WORKSPACE_BILLING -> getWorkspaceBillingPermissions();
            case WORKSPACE_MEMBER -> getWorkspaceMemberPermissions();
        };
    }

    /**
     * Check if a user has a specific permission.
     */
    public boolean hasPermission(User user, Permission permission) {
        Set<Permission> userPermissions = getPermissionsForRole(user.getRole());
        return userPermissions.contains(permission);
    }

    /**
     * Check if a user has any of the specified permissions.
     */
    public boolean hasAnyPermission(User user, Permission... permissions) {
        Set<Permission> userPermissions = getPermissionsForRole(user.getRole());
        for (Permission permission : permissions) {
            if (userPermissions.contains(permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if a user has all of the specified permissions.
     */
    public boolean hasAllPermissions(User user, Permission... permissions) {
        Set<Permission> userPermissions = getPermissionsForRole(user.getRole());
        for (Permission permission : permissions) {
            if (!userPermissions.contains(permission)) {
                return false;
            }
        }
        return true;
    }

    // Permission sets for different roles
    private Set<Permission> getAllPermissions() {
        return EnumSet.allOf(Permission.class);
    }

    private Set<Permission> getOrganizationOwnerPermissions() {
        return EnumSet.of(
            // Authentication
            Permission.AUTH_LOGIN, Permission.AUTH_LOGOUT, Permission.AUTH_REFRESH, Permission.AUTH_VIEW_PROFILE,
            
            // User management (full)
            Permission.USER_VIEW, Permission.USER_CREATE, Permission.USER_UPDATE, Permission.USER_DELETE, Permission.USER_MANAGE_ROLES,
            
            // Projects (full)
            Permission.PROJECT_VIEW, Permission.PROJECT_CREATE, Permission.PROJECT_UPDATE, Permission.PROJECT_DELETE,
            Permission.PROJECT_DEPLOY, Permission.PROJECT_STOP, Permission.PROJECT_MANAGE_CONFIG,
            Permission.PROJECT_MANAGE_BRANDING, Permission.PROJECT_MANAGE_LINKS, Permission.PROJECT_VIEW_STATUS,
            
            // Billing (full)
            Permission.BILLING_VIEW_BALANCE, Permission.BILLING_VIEW_USAGE, Permission.BILLING_VIEW_TRANSACTIONS,
            Permission.BILLING_VIEW_PLANS, Permission.BILLING_MANAGE_PAYMENT, Permission.BILLING_VIEW_INVOICES,
            Permission.BILLING_MANAGE_SUPPORT,
            
            // Team (full)
            Permission.TEAM_VIEW_MEMBERS, Permission.TEAM_MANAGE_MEMBERS, Permission.TEAM_VIEW_CAMPAIGNS,
            Permission.TEAM_CREATE_CAMPAIGNS, Permission.TEAM_UPDATE_CAMPAIGNS, Permission.TEAM_DELETE_CAMPAIGNS,
            Permission.TEAM_MANAGE_CAMPAIGNS, Permission.TEAM_VIEW_LEADS, Permission.TEAM_CREATE_LEADS,
            Permission.TEAM_UPDATE_LEADS, Permission.TEAM_MANAGE_LEADS, Permission.TEAM_VIEW_STATS,
            
            // Workspaces (full)
            Permission.WORKSPACE_VIEW, Permission.WORKSPACE_CREATE, Permission.WORKSPACE_UPDATE, Permission.WORKSPACE_DELETE,
            Permission.WORKSPACE_MANAGE_CREDITS, Permission.WORKSPACE_VIEW_STATS, Permission.WORKSPACE_MANAGE_MEMBERS,
            
            // Analytics (full)
            Permission.ANALYTICS_VIEW_USAGE, Permission.ANALYTICS_VIEW_LEADS, Permission.ANALYTICS_VIEW_GEOGRAPHIC,
            Permission.ANALYTICS_VIEW_DEVICES, Permission.ANALYTICS_VIEW_NETWORK, Permission.ANALYTICS_VIEW_PROJECT_USAGE,
            Permission.ANALYTICS_EXPORT_LEADS, Permission.ANALYTICS_VIEW_JOURNEY,
            
            // Audit (full)
            Permission.AUDIT_VIEW_LOGS, Permission.AUDIT_VIEW_STATS, Permission.AUDIT_VIEW_USERS,
            Permission.AUDIT_VIEW_ACTIONS, Permission.AUDIT_EXPORT,
            
            // Usage (full)
            Permission.USAGE_VIEW_SESSIONS, Permission.USAGE_VIEW_STATS, Permission.USAGE_VIEW_WORKSPACES,
            Permission.USAGE_VIEW_MEMBERS, Permission.USAGE_EXPORT,
            
            // Reports (full)
            Permission.REPORTS_VIEW_DASHBOARD, Permission.REPORTS_VIEW_ANALYTICS, Permission.REPORTS_EXPORT
        );
    }

    private Set<Permission> getOrganizationAdminPermissions() {
        return EnumSet.of(
            // Authentication
            Permission.AUTH_LOGIN, Permission.AUTH_LOGOUT, Permission.AUTH_REFRESH, Permission.AUTH_VIEW_PROFILE,
            
            // User management (limited)
            Permission.USER_VIEW, Permission.USER_CREATE, Permission.USER_UPDATE, Permission.USER_MANAGE_ROLES,
            
            // Projects (full)
            Permission.PROJECT_VIEW, Permission.PROJECT_CREATE, Permission.PROJECT_UPDATE, Permission.PROJECT_DELETE,
            Permission.PROJECT_DEPLOY, Permission.PROJECT_STOP, Permission.PROJECT_MANAGE_CONFIG,
            Permission.PROJECT_MANAGE_BRANDING, Permission.PROJECT_MANAGE_LINKS, Permission.PROJECT_VIEW_STATUS,
            
            // Billing (view only)
            Permission.BILLING_VIEW_BALANCE, Permission.BILLING_VIEW_USAGE, Permission.BILLING_VIEW_TRANSACTIONS,
            Permission.BILLING_VIEW_PLANS, Permission.BILLING_VIEW_INVOICES,
            
            // Team (full)
            Permission.TEAM_VIEW_MEMBERS, Permission.TEAM_MANAGE_MEMBERS, Permission.TEAM_VIEW_CAMPAIGNS,
            Permission.TEAM_CREATE_CAMPAIGNS, Permission.TEAM_UPDATE_CAMPAIGNS, Permission.TEAM_DELETE_CAMPAIGNS,
            Permission.TEAM_MANAGE_CAMPAIGNS, Permission.TEAM_VIEW_LEADS, Permission.TEAM_CREATE_LEADS,
            Permission.TEAM_UPDATE_LEADS, Permission.TEAM_MANAGE_LEADS, Permission.TEAM_VIEW_STATS,
            
            // Workspaces (limited)
            Permission.WORKSPACE_VIEW, Permission.WORKSPACE_CREATE, Permission.WORKSPACE_UPDATE,
            Permission.WORKSPACE_VIEW_STATS, Permission.WORKSPACE_MANAGE_MEMBERS,
            
            // Analytics (full)
            Permission.ANALYTICS_VIEW_USAGE, Permission.ANALYTICS_VIEW_LEADS, Permission.ANALYTICS_VIEW_GEOGRAPHIC,
            Permission.ANALYTICS_VIEW_DEVICES, Permission.ANALYTICS_VIEW_NETWORK, Permission.ANALYTICS_VIEW_PROJECT_USAGE,
            Permission.ANALYTICS_EXPORT_LEADS, Permission.ANALYTICS_VIEW_JOURNEY,
            
            // Audit (view only)
            Permission.AUDIT_VIEW_LOGS, Permission.AUDIT_VIEW_STATS, Permission.AUDIT_VIEW_USERS, Permission.AUDIT_VIEW_ACTIONS,
            
            // Usage (view only)
            Permission.USAGE_VIEW_SESSIONS, Permission.USAGE_VIEW_STATS, Permission.USAGE_VIEW_WORKSPACES, Permission.USAGE_VIEW_MEMBERS,
            
            // Reports (full)
            Permission.REPORTS_VIEW_DASHBOARD, Permission.REPORTS_VIEW_ANALYTICS, Permission.REPORTS_EXPORT
        );
    }

    private Set<Permission> getOrganizationAuditorPermissions() {
        return EnumSet.of(
            // Authentication
            Permission.AUTH_LOGIN, Permission.AUTH_LOGOUT, Permission.AUTH_REFRESH, Permission.AUTH_VIEW_PROFILE,
            
            // Projects (view only)
            Permission.PROJECT_VIEW, Permission.PROJECT_VIEW_STATUS,
            
            // Audit (full)
            Permission.AUDIT_VIEW_LOGS, Permission.AUDIT_VIEW_STATS, Permission.AUDIT_VIEW_USERS,
            Permission.AUDIT_VIEW_ACTIONS, Permission.AUDIT_EXPORT,
            
            // Usage (full)
            Permission.USAGE_VIEW_SESSIONS, Permission.USAGE_VIEW_STATS, Permission.USAGE_VIEW_WORKSPACES,
            Permission.USAGE_VIEW_MEMBERS, Permission.USAGE_EXPORT
        );
    }

    private Set<Permission> getOrganizationReporterPermissions() {
        return EnumSet.of(
            // Authentication
            Permission.AUTH_LOGIN, Permission.AUTH_LOGOUT, Permission.AUTH_REFRESH, Permission.AUTH_VIEW_PROFILE,
            
            // Projects (view only)
            Permission.PROJECT_VIEW, Permission.PROJECT_VIEW_STATUS,
            
            // Analytics (full)
            Permission.ANALYTICS_VIEW_USAGE, Permission.ANALYTICS_VIEW_LEADS, Permission.ANALYTICS_VIEW_GEOGRAPHIC,
            Permission.ANALYTICS_VIEW_DEVICES, Permission.ANALYTICS_VIEW_NETWORK, Permission.ANALYTICS_VIEW_PROJECT_USAGE,
            Permission.ANALYTICS_EXPORT_LEADS, Permission.ANALYTICS_VIEW_JOURNEY,
            
            // Reports (full)
            Permission.REPORTS_VIEW_DASHBOARD, Permission.REPORTS_VIEW_ANALYTICS, Permission.REPORTS_EXPORT
        );
    }

    private Set<Permission> getOrganizationBillingPermissions() {
        return EnumSet.of(
            // Authentication
            Permission.AUTH_LOGIN, Permission.AUTH_LOGOUT, Permission.AUTH_REFRESH, Permission.AUTH_VIEW_PROFILE,
            
            // Billing (full)
            Permission.BILLING_VIEW_BALANCE, Permission.BILLING_VIEW_USAGE, Permission.BILLING_VIEW_TRANSACTIONS,
            Permission.BILLING_VIEW_PLANS, Permission.BILLING_MANAGE_PAYMENT, Permission.BILLING_VIEW_INVOICES,
            Permission.BILLING_MANAGE_SUPPORT
        );
    }

    private Set<Permission> getOrganizationMemberPermissions() {
        return EnumSet.of(
            // Authentication
            Permission.AUTH_LOGIN, Permission.AUTH_LOGOUT, Permission.AUTH_REFRESH, Permission.AUTH_VIEW_PROFILE,
            
            // Projects (view only)
            Permission.PROJECT_VIEW, Permission.PROJECT_VIEW_STATUS,
            
            // Team (limited)
            Permission.TEAM_VIEW_CAMPAIGNS, Permission.TEAM_VIEW_LEADS, Permission.TEAM_VIEW_STATS
        );
    }

    private Set<Permission> getWorkspaceAdminPermissions() {
        return EnumSet.of(
            // Authentication
            Permission.AUTH_LOGIN, Permission.AUTH_LOGOUT, Permission.AUTH_REFRESH, Permission.AUTH_VIEW_PROFILE,
            
            // User management (workspace only)
            Permission.USER_VIEW,
            
            // Projects (full within workspace)
            Permission.PROJECT_VIEW, Permission.PROJECT_CREATE, Permission.PROJECT_UPDATE,
            Permission.PROJECT_DEPLOY, Permission.PROJECT_STOP, Permission.PROJECT_MANAGE_CONFIG,
            Permission.PROJECT_MANAGE_BRANDING, Permission.PROJECT_MANAGE_LINKS, Permission.PROJECT_VIEW_STATUS,
            
            // Team (full within workspace)
            Permission.TEAM_VIEW_MEMBERS, Permission.TEAM_MANAGE_MEMBERS, Permission.TEAM_VIEW_CAMPAIGNS,
            Permission.TEAM_CREATE_CAMPAIGNS, Permission.TEAM_UPDATE_CAMPAIGNS, Permission.TEAM_DELETE_CAMPAIGNS,
            Permission.TEAM_MANAGE_CAMPAIGNS, Permission.TEAM_VIEW_LEADS, Permission.TEAM_CREATE_LEADS,
            Permission.TEAM_UPDATE_LEADS, Permission.TEAM_MANAGE_LEADS, Permission.TEAM_VIEW_STATS,
            
            // Workspace (limited)
            Permission.WORKSPACE_VIEW, Permission.WORKSPACE_VIEW_STATS, Permission.WORKSPACE_MANAGE_MEMBERS,
            
            // Analytics (workspace only)
            Permission.ANALYTICS_VIEW_USAGE, Permission.ANALYTICS_VIEW_LEADS, Permission.ANALYTICS_VIEW_GEOGRAPHIC,
            Permission.ANALYTICS_VIEW_DEVICES, Permission.ANALYTICS_VIEW_NETWORK, Permission.ANALYTICS_VIEW_PROJECT_USAGE,
            Permission.ANALYTICS_EXPORT_LEADS, Permission.ANALYTICS_VIEW_JOURNEY
        );
    }

    private Set<Permission> getWorkspaceAuditorPermissions() {
        return EnumSet.of(
            // Authentication
            Permission.AUTH_LOGIN, Permission.AUTH_LOGOUT, Permission.AUTH_REFRESH, Permission.AUTH_VIEW_PROFILE,
            
            // Projects (view only)
            Permission.PROJECT_VIEW, Permission.PROJECT_VIEW_STATUS,
            
            // Audit (workspace only)
            Permission.AUDIT_VIEW_LOGS, Permission.AUDIT_VIEW_STATS, Permission.AUDIT_VIEW_USERS,
            Permission.AUDIT_VIEW_ACTIONS, Permission.AUDIT_EXPORT,
            
            // Usage (workspace only)
            Permission.USAGE_VIEW_SESSIONS, Permission.USAGE_VIEW_STATS, Permission.USAGE_VIEW_MEMBERS, Permission.USAGE_EXPORT
        );
    }

    private Set<Permission> getWorkspaceReporterPermissions() {
        return EnumSet.of(
            // Authentication
            Permission.AUTH_LOGIN, Permission.AUTH_LOGOUT, Permission.AUTH_REFRESH, Permission.AUTH_VIEW_PROFILE,
            
            // Projects (view only)
            Permission.PROJECT_VIEW, Permission.PROJECT_VIEW_STATUS,
            
            // Analytics (workspace only)
            Permission.ANALYTICS_VIEW_USAGE, Permission.ANALYTICS_VIEW_LEADS, Permission.ANALYTICS_VIEW_GEOGRAPHIC,
            Permission.ANALYTICS_VIEW_DEVICES, Permission.ANALYTICS_VIEW_NETWORK, Permission.ANALYTICS_VIEW_PROJECT_USAGE,
            Permission.ANALYTICS_EXPORT_LEADS, Permission.ANALYTICS_VIEW_JOURNEY,
            
            // Reports (workspace only)
            Permission.REPORTS_VIEW_DASHBOARD, Permission.REPORTS_VIEW_ANALYTICS, Permission.REPORTS_EXPORT
        );
    }

    private Set<Permission> getWorkspaceBillingPermissions() {
        return EnumSet.of(
            // Authentication
            Permission.AUTH_LOGIN, Permission.AUTH_LOGOUT, Permission.AUTH_REFRESH, Permission.AUTH_VIEW_PROFILE,
            
            // Billing (workspace only)
            Permission.BILLING_VIEW_BALANCE, Permission.BILLING_VIEW_USAGE, Permission.BILLING_VIEW_TRANSACTIONS,
            Permission.BILLING_VIEW_PLANS, Permission.BILLING_VIEW_INVOICES, Permission.BILLING_MANAGE_SUPPORT
        );
    }

    private Set<Permission> getWorkspaceMemberPermissions() {
        return EnumSet.of(
            // Authentication
            Permission.AUTH_LOGIN, Permission.AUTH_LOGOUT, Permission.AUTH_REFRESH, Permission.AUTH_VIEW_PROFILE,
            
            // Projects (view only)
            Permission.PROJECT_VIEW, Permission.PROJECT_VIEW_STATUS,
            
            // Team (limited)
            Permission.TEAM_VIEW_CAMPAIGNS, Permission.TEAM_VIEW_LEADS, Permission.TEAM_VIEW_STATS
        );
    }
}
