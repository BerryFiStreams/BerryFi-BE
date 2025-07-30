package com.berryfi.portal.security;

import com.berryfi.portal.entity.User;
import com.berryfi.portal.enums.Permission;
import com.berryfi.portal.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * Custom permission evaluator for checking user permissions.
 */
@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {

    @Autowired
    private PermissionService permissionService;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || permission == null) {
            return false;
        }

        User user = (User) authentication.getPrincipal();
        String permissionName = permission.toString();

        // Map string permissions to Permission enum
        Permission perm = getPermissionFromString(targetDomainObject, permissionName);
        if (perm == null) {
            return false;
        }

        return permissionService.hasPermission(user, perm);
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        return hasPermission(authentication, targetType, permission);
    }

    private Permission getPermissionFromString(Object targetDomainObject, String permission) {
        String target = targetDomainObject != null ? targetDomainObject.toString() : "";
        
        return switch (target.toLowerCase() + ":" + permission.toLowerCase()) {
            // Project permissions
            case "project:create" -> Permission.PROJECT_CREATE;
            case "project:read", "project:view" -> Permission.PROJECT_VIEW;
            case "project:update" -> Permission.PROJECT_UPDATE;
            case "project:delete" -> Permission.PROJECT_DELETE;
            case "project:deploy" -> Permission.PROJECT_DEPLOY;
            case "project:stop" -> Permission.PROJECT_STOP;
            
            // Workspace permissions
            case "workspace:create" -> Permission.WORKSPACE_CREATE;
            case "workspace:read", "workspace:view" -> Permission.WORKSPACE_VIEW;
            case "workspace:update" -> Permission.WORKSPACE_UPDATE;
            case "workspace:delete" -> Permission.WORKSPACE_DELETE;
            case "workspace:manage_credits" -> Permission.WORKSPACE_MANAGE_CREDITS;
            case "workspace:use_credits" -> Permission.WORKSPACE_MANAGE_CREDITS; // Using same permission
            
            // User permissions
            case "user:create" -> Permission.USER_CREATE;
            case "user:read", "user:view" -> Permission.USER_VIEW;
            case "user:update" -> Permission.USER_UPDATE;
            case "user:delete" -> Permission.USER_DELETE;
            
            // Auth permissions
            case "auth:login" -> Permission.AUTH_LOGIN;
            case "auth:logout" -> Permission.AUTH_LOGOUT;
            case "auth:refresh" -> Permission.AUTH_REFRESH;
            case "auth:view_profile" -> Permission.AUTH_VIEW_PROFILE;
            
            default -> null;
        };
    }
}
