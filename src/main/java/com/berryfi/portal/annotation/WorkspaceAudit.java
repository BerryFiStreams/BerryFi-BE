package com.berryfi.portal.annotation;

import java.lang.annotation.*;

/**
 * Annotation to mark methods that should be audited at the workspace level.
 * This annotation will trigger automatic audit logging for workspace-specific actions.
 * Users need access to the specific workspace to view these audit logs.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WorkspaceAudit {
    
    /**
     * The action being performed (e.g., "CREATE", "UPDATE", "DELETE", "VIEW").
     */
    String action();
    
    /**
     * The resource type being acted upon (e.g., "WORKSPACE", "TEAM_MEMBER", "BUDGET").
     */
    String resource();
    
    /**
     * Description of the action for logging purposes.
     */
    String description() default "";
    
    /**
     * Parameter name that contains the workspace ID (default is "workspaceId").
     */
    String workspaceIdParam() default "workspaceId";
    
    /**
     * Whether to include request parameters in audit details.
     */
    boolean includeRequestParams() default false;
    
    /**
     * Whether to include response data in audit details.
     */
    boolean includeResponse() default false;
    
    /**
     * Whether to audit even if the operation fails.
     */
    boolean auditOnFailure() default true;
}
