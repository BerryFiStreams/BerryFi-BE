package com.berryfi.portal.annotation;

import java.lang.annotation.*;

/**
 * Annotation to mark methods that should be audited at the organization level.
 * This annotation will trigger automatic audit logging for organization-wide actions.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OrganizationAudit {
    
    /**
     * The action being performed (e.g., "CREATE", "UPDATE", "DELETE", "VIEW").
     */
    String action();
    
    /**
     * The resource type being acted upon (e.g., "USER", "PROJECT", "BILLING").
     */
    String resource();
    
    /**
     * Description of the action for logging purposes.
     */
    String description() default "";
    
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
