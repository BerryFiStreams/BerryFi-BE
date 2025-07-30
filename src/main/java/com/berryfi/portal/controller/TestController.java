package com.berryfi.portal.controller;

import com.berryfi.portal.dto.user.UserDto;
import com.berryfi.portal.entity.User;
import com.berryfi.portal.enums.Permission;
import com.berryfi.portal.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Test controller to verify authentication and permissions.
 */
@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private PermissionService permissionService;

    /**
     * Public endpoint for testing.
     */
    @GetMapping("/public")
    public ResponseEntity<Map<String, Object>> publicEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "This is a public endpoint");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    /**
     * Protected endpoint for authenticated users.
     */
    @GetMapping("/protected")
    public ResponseEntity<Map<String, Object>> protectedEndpoint(@AuthenticationPrincipal User user) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "This is a protected endpoint");
        response.put("user", UserDto.fromUser(user));
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    /**
     * Admin only endpoint.
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ORG_OWNER') or hasRole('ORG_ADMIN') or hasRole('WORKSPACE_ADMIN')")
    public ResponseEntity<Map<String, Object>> adminEndpoint(@AuthenticationPrincipal User user) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "This is an admin-only endpoint");
        response.put("user", UserDto.fromUser(user));
        response.put("hasAdminPrivileges", user.hasAdminPrivileges());
        return ResponseEntity.ok(response);
    }

    /**
     * Get user permissions.
     */
    @GetMapping("/permissions")
    public ResponseEntity<Map<String, Object>> getUserPermissions(@AuthenticationPrincipal User user) {
        Set<Permission> permissions = permissionService.getPermissionsForRole(user.getRole());
        
        Map<String, Object> response = new HashMap<>();
        response.put("user", UserDto.fromUser(user));
        response.put("role", user.getRole());
        response.put("permissions", permissions);
        response.put("permissionCount", permissions.size());
        
        // Sample permission checks
        Map<String, Boolean> permissionChecks = new HashMap<>();
        permissionChecks.put("canViewProjects", permissionService.hasPermission(user, Permission.PROJECT_VIEW));
        permissionChecks.put("canCreateProjects", permissionService.hasPermission(user, Permission.PROJECT_CREATE));
        permissionChecks.put("canViewBilling", permissionService.hasPermission(user, Permission.BILLING_VIEW_BALANCE));
        permissionChecks.put("canManageUsers", permissionService.hasPermission(user, Permission.USER_MANAGE_ROLES));
        permissionChecks.put("canAccessAudit", permissionService.hasPermission(user, Permission.AUDIT_VIEW_LOGS));
        
        response.put("permissionChecks", permissionChecks);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Billing endpoint - only for users with billing permissions.
     */
    @GetMapping("/billing")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ORG_OWNER') or hasRole('ORG_ADMIN') or hasRole('ORG_BILLING') or hasRole('WORKSPACE_BILLING')")
    public ResponseEntity<Map<String, Object>> billingEndpoint(@AuthenticationPrincipal User user) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "This endpoint requires billing permissions");
        response.put("user", UserDto.fromUser(user));
        response.put("canAccessBilling", user.canAccessBilling());
        return ResponseEntity.ok(response);
    }

    /**
     * Reports endpoint - only for users with reporting permissions.
     */
    @GetMapping("/reports")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ORG_OWNER') or hasRole('ORG_ADMIN') or hasRole('ORG_REPORTER') or hasRole('WORKSPACE_REPORTER')")
    public ResponseEntity<Map<String, Object>> reportsEndpoint(@AuthenticationPrincipal User user) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "This endpoint requires reporting permissions");
        response.put("user", UserDto.fromUser(user));
        response.put("canAccessReports", user.canAccessReports());
        return ResponseEntity.ok(response);
    }
}
