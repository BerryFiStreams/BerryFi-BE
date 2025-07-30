package com.berryfi.portal.enums;

/**
 * Enum representing user roles in the system.
 * Roles determine the level of access and permissions for users.
 */
public enum Role {
    // Super admin - has access to everything
    SUPER_ADMIN("super_admin", "Super Admin"),
    
    // Organization level roles
    ORG_OWNER("org_owner", "Organization Owner"),
    ORG_ADMIN("org_admin", "Organization Admin"),
    ORG_AUDITOR("org_auditor", "Organization Auditor"),
    ORG_REPORTER("org_reporter", "Organization Reporter"),
    ORG_BILLING("org_billing", "Organization Billing"),
    ORG_MEMBER("org_member", "Organization Member"),
    
    // Workspace level roles
    WORKSPACE_ADMIN("workspace_admin", "Workspace Admin"),
    WORKSPACE_AUDITOR("workspace_auditor", "Workspace Auditor"),
    WORKSPACE_REPORTER("workspace_reporter", "Workspace Reporter"),
    WORKSPACE_BILLING("workspace_billing", "Workspace Billing"),
    WORKSPACE_MEMBER("workspace_member", "Workspace Member");

    private final String value;
    private final String displayName;

    Role(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    public String getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Role fromValue(String value) {
        for (Role role : Role.values()) {
            if (role.value.equals(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid role value: " + value);
    }

    public boolean isOrganizationLevel() {
        return this == SUPER_ADMIN || 
               this == ORG_OWNER || 
               this == ORG_ADMIN || 
               this == ORG_AUDITOR || 
               this == ORG_REPORTER || 
               this == ORG_BILLING || 
               this == ORG_MEMBER;
    }

    public boolean isWorkspaceLevel() {
        return this == WORKSPACE_ADMIN || 
               this == WORKSPACE_AUDITOR || 
               this == WORKSPACE_REPORTER || 
               this == WORKSPACE_BILLING || 
               this == WORKSPACE_MEMBER;
    }

    public boolean hasAdminPrivileges() {
        return this == SUPER_ADMIN || 
               this == ORG_OWNER || 
               this == ORG_ADMIN || 
               this == WORKSPACE_ADMIN;
    }

    public boolean canAccessBilling() {
        return this == SUPER_ADMIN || 
               this == ORG_OWNER || 
               this == ORG_ADMIN || 
               this == ORG_BILLING || 
               this == WORKSPACE_BILLING;
    }

    public boolean canAccessAudit() {
        return this == SUPER_ADMIN || 
               this == ORG_OWNER || 
               this == ORG_ADMIN || 
               this == ORG_AUDITOR || 
               this == WORKSPACE_AUDITOR;
    }

    public boolean canAccessReports() {
        return this == SUPER_ADMIN || 
               this == ORG_OWNER || 
               this == ORG_ADMIN || 
               this == ORG_REPORTER || 
               this == WORKSPACE_REPORTER;
    }
}
