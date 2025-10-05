package com.berryfi.portal.security;

import com.berryfi.portal.entity.User;
import com.berryfi.portal.enums.Permission;
import com.berryfi.portal.service.PermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(CustomPermissionEvaluator.class);

    private final PermissionService permissionService;

    @Autowired
    public CustomPermissionEvaluator(PermissionService permissionService) {
        this.permissionService = permissionService;
        logger.info("CustomPermissionEvaluator initialized");
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || permission == null) {
            return false;
        }

        try {
            logger.debug("Evaluating permission: target={}, permission={}, authentication={}", 
                targetDomainObject, permission, authentication != null ? authentication.getName() : "null");
            
            Object principal = authentication.getPrincipal();
            if (!(principal instanceof User)) {
                logger.warn("Principal is not a User instance. Type: {}", 
                    principal != null ? principal.getClass().getSimpleName() : "null");
                return false;
            }

            User user = (User) principal;
            String permissionName = permission.toString();

            // Map string permissions to Permission enum
            Permission perm = getPermissionFromString(targetDomainObject, permissionName);
            if (perm == null) {
                logger.warn("Unknown permission mapping: target={}, permission={}", targetDomainObject, permissionName);
                return false;
            }

            boolean hasPermission = permissionService.hasPermission(user, perm);
            logger.debug("Permission check result: user={}, permission={}, result={}", 
                user.getEmail(), perm, hasPermission);
            
            return hasPermission;
        } catch (Exception e) {
            logger.error("Error evaluating permission: target={}, permission={}, error={}", 
                targetDomainObject, permission, e.getMessage(), e);
            return false;
        }
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
            case "project:share" -> Permission.PROJECT_SHARE;
            
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
            
            // Dashboard permissions
            case "dashboard:read", "dashboard:view" -> Permission.REPORTS_VIEW_DASHBOARD;
            
            // Reports permissions
            case "reports:view_dashboard" -> Permission.REPORTS_VIEW_DASHBOARD;
            
            // Organization permissions (mapping to available permissions)
            case "organization:create" -> Permission.USER_CREATE; // Approximation
            case "organization:read", "organization:view" -> Permission.USER_VIEW; 
            case "organization:update" -> Permission.USER_UPDATE;
            case "organization:manage_credits" -> Permission.BILLING_MANAGE_PAYMENT;
            case "organization:search" -> Permission.USER_VIEW;
            case "organization:suspend" -> Permission.USER_DELETE;
            case "organization:activate" -> Permission.USER_UPDATE;
            
            default -> null;
        };
    }
}
