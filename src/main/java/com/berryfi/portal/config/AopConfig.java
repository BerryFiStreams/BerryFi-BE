package com.berryfi.portal.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Configuration class to enable AOP (Aspect-Oriented Programming) support.
 * This enables the audit aspect to intercept method calls and automatically log actions.
 */
@Configuration
@EnableAspectJAutoProxy
public class AopConfig {
    
    // AOP configuration for audit aspects
    // The @EnableAspectJAutoProxy annotation enables Spring's aspect-oriented programming
    // capabilities, allowing the AuditAspect to automatically intercept and log method calls
    // marked with audit annotations (@OrganizationAudit, @WorkspaceAudit, @VMSessionAudit)
}
