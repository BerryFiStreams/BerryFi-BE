package com.berryfi.portal.annotation;

import java.lang.annotation.*;

/**
 * Annotation to mark VM session methods that should be audited.
 * This annotation will trigger automatic audit logging using VMSessionAuditLog
 * for workspace-level VM session activities.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface VMSessionAudit {
    
    /**
     * The action being performed on VM session (e.g., "VM_SESSION_START", "VM_SESSION_STOP", "VM_SESSION_HEARTBEAT").
     */
    String action();
    
    /**
     * Description of the VM session action for logging purposes.
     */
    String description() default "";
    
    /**
     * Parameter name that contains the session ID (default is "sessionId").
     */
    String sessionIdParam() default "sessionId";
    
    /**
     * Whether to include detailed VM session information in audit details.
     */
    boolean includeSessionDetails() default true;
    
    /**
     * Whether to include performance metrics (duration, credits used, etc.) in audit.
     */
    boolean includeMetrics() default true;
    
    /**
     * Whether to audit even if the operation fails.
     */
    boolean auditOnFailure() default true;
}
