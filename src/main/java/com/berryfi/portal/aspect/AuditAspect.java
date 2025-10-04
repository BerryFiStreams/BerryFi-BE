package com.berryfi.portal.aspect;

import com.berryfi.portal.annotation.OrganizationAudit;
import com.berryfi.portal.annotation.VMSessionAudit;
import com.berryfi.portal.entity.VmSession;
import com.berryfi.portal.service.AuditService;
import com.berryfi.portal.service.AuthService;
import com.berryfi.portal.service.VmSessionService;
import com.berryfi.portal.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

/**
 * AOP Aspect for automatic audit logging.
 * Intercepts methods annotated with audit annotations and automatically logs the actions.
 */
@Aspect
@Component
public class AuditAspect {

    private static final Logger logger = LoggerFactory.getLogger(AuditAspect.class);

    @Autowired
    private AuditService auditService;

    @Autowired
    private AuthService authService;

    @Autowired
    private VmSessionService vmSessionService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Pointcut for organization-level audit methods.
     */
    @Pointcut("@annotation(com.berryfi.portal.annotation.OrganizationAudit)")
    public void organizationAuditPointcut() {}



    /**
     * Pointcut for VM session audit methods.
     */
    @Pointcut("@annotation(com.berryfi.portal.annotation.VMSessionAudit)")
    public void vmSessionAuditPointcut() {}

    /**
     * Around advice for organization audit.
     */
    @Around("organizationAuditPointcut() && @annotation(orgAudit)")
    public Object auditOrganizationAction(ProceedingJoinPoint joinPoint, OrganizationAudit orgAudit) throws Throwable {
        String status = "SUCCESS";
        String errorMessage = null;
        Object result = null;
        long startTime = System.currentTimeMillis();

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            status = "FAILED";
            errorMessage = e.getMessage();
            if (orgAudit.auditOnFailure()) {
                throw e;
            } else {
                throw e;
            }
        } finally {
            try {
                logOrganizationAudit(joinPoint, orgAudit, result, status, errorMessage, startTime);
            } catch (Exception e) {
                logger.error("Failed to log organization audit: {}", e.getMessage(), e);
            }
        }
    }



    /**
     * Around advice for VM session audit.
     */
    @Around("vmSessionAuditPointcut() && @annotation(vmSessionAudit)")
    public Object auditVMSessionAction(ProceedingJoinPoint joinPoint, VMSessionAudit vmSessionAudit) throws Throwable {
        String status = "SUCCESS";
        String errorMessage = null;
        Object result = null;
        long startTime = System.currentTimeMillis();

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            status = "FAILED";
            errorMessage = e.getMessage();
            if (vmSessionAudit.auditOnFailure()) {
                throw e;
            } else {
                throw e;
            }
        } finally {
            try {
                logVMSessionAudit(joinPoint, vmSessionAudit, result, status, errorMessage, startTime);
            } catch (Exception e) {
                logger.error("Failed to log VM session audit: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Log organization-level audit.
     */
    private void logOrganizationAudit(ProceedingJoinPoint joinPoint, OrganizationAudit orgAudit, 
                                    Object result, String status, String errorMessage, long startTime) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                logger.warn("No current user found for organization audit logging");
                return;
            }

            String resourceId = extractResourceId(joinPoint, result);
            String details = buildAuditDetails(joinPoint, orgAudit.includeRequestParams(), 
                                             orgAudit.includeResponse(), result, errorMessage, startTime);

            HttpServletRequest request = getCurrentRequest();
            
            auditService.logOrganizationAction(
                currentUser.getId(),
                currentUser.getName(),
                currentUser.getOrganizationId(),
                orgAudit.action(),
                orgAudit.resource(),
                resourceId,
                details,
                request
            );

        } catch (Exception e) {
            logger.error("Error in organization audit logging: {}", e.getMessage(), e);
        }
    }



    /**
     * Log VM session audit.
     */
    private void logVMSessionAudit(ProceedingJoinPoint joinPoint, VMSessionAudit vmSessionAudit,
                                 Object result, String status, String errorMessage, long startTime) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                logger.warn("No current user found for VM session audit logging");
                return;
            }

            String sessionId = extractParameterValue(joinPoint, vmSessionAudit.sessionIdParam());
            if (sessionId == null) {
                logger.warn("Could not extract session ID for VM session audit");
                return;
            }

            // Get the VM session details
            VmSession session = vmSessionService.getSession(sessionId).orElse(null);
            if (session == null) {
                logger.warn("Could not find VM session {} for audit logging", sessionId);
                return;
            }

            // Get project name
            String organizationName = "Unknown Organization"; // Default organization name
            String projectName = "Unknown Project"; // TODO: Get project name from project service

            String details = buildVMSessionAuditDetails(joinPoint, vmSessionAudit, session, 
                                                      result, errorMessage, startTime);

            auditService.logVMSessionAction(
                session,
                currentUser.getName(),
                currentUser.getEmail(),
                organizationName,
                projectName,
                vmSessionAudit.action(),
                details
            );

        } catch (Exception e) {
            logger.error("Error in VM session audit logging: {}", e.getMessage(), e);
        }
    }

    /**
     * Get current authenticated user.
     */
    private User getCurrentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getName() != null) {
                return authService.getUserByEmail(auth.getName());
            }
        } catch (Exception e) {
            logger.warn("Could not get current user: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Get current HTTP request.
     */
    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            return attrs.getRequest();
        } catch (Exception e) {
            logger.warn("Could not get current request: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extract parameter value by name.
     */
    private String extractParameterValue(JoinPoint joinPoint, String paramName) {
        try {
            Method method = ((org.aspectj.lang.reflect.MethodSignature) joinPoint.getSignature()).getMethod();
            Parameter[] parameters = method.getParameters();
            Object[] args = joinPoint.getArgs();

            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i].getName().equals(paramName)) {
                    return args[i] != null ? args[i].toString() : null;
                }
            }
        } catch (Exception e) {
            logger.warn("Error extracting parameter {}: {}", paramName, e.getMessage());
        }
        return null;
    }

    /**
     * Extract resource ID from method parameters or result.
     */
    private String extractResourceId(JoinPoint joinPoint, Object result) {
        try {
            // Try to extract ID from result first
            if (result != null) {
                // Check if result has an ID field/method
                try {
                    Method getIdMethod = result.getClass().getMethod("getId");
                    Object id = getIdMethod.invoke(result);
                    if (id != null) {
                        return id.toString();
                    }
                } catch (Exception e) {
                    // Ignore, try other approaches
                }
            }

            // Try to extract from parameters
            Object[] args = joinPoint.getArgs();
            if (args.length > 0) {
                for (Object arg : args) {
                    if (arg instanceof String && ((String) arg).contains("_")) {
                        // Likely an ID pattern
                        return (String) arg;
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Error extracting resource ID: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Build audit details JSON.
     */
    private String buildAuditDetails(JoinPoint joinPoint, boolean includeRequestParams,
                                   boolean includeResponse, Object result, String errorMessage, long startTime) {
        try {
            Map<String, Object> details = new HashMap<>();
            details.put("method", joinPoint.getSignature().getName());
            details.put("duration_ms", System.currentTimeMillis() - startTime);

            if (errorMessage != null) {
                details.put("error", errorMessage);
            }

            if (includeRequestParams) {
                details.put("parameters", joinPoint.getArgs());
            }

            if (includeResponse && result != null) {
                details.put("response", result);
            }

            return objectMapper.writeValueAsString(details);
        } catch (Exception e) {
            logger.warn("Error building audit details: {}", e.getMessage());
            return "{}";
        }
    }

    /**
     * Build VM session specific audit details.
     */
    private String buildVMSessionAuditDetails(JoinPoint joinPoint, VMSessionAudit vmSessionAudit,
                                            VmSession session, Object result, String errorMessage, long startTime) {
        try {
            Map<String, Object> details = new HashMap<>();
            details.put("method", joinPoint.getSignature().getName());
            details.put("duration_ms", System.currentTimeMillis() - startTime);

            if (errorMessage != null) {
                details.put("error", errorMessage);
            }

            if (vmSessionAudit.includeSessionDetails()) {
                details.put("session_status", session.getStatus());
                details.put("vm_instance_id", session.getVmInstanceId());
                details.put("project_id", session.getProjectId());
                details.put("connection_url", session.getConnectionUrl());
            }

            if (vmSessionAudit.includeMetrics()) {
                details.put("session_duration_seconds", session.getDurationInSeconds());
                details.put("credits_used", session.getCreditsUsed());
                details.put("heartbeat_count", session.getHeartbeatCount());
            }

            return objectMapper.writeValueAsString(details);
        } catch (Exception e) {
            logger.warn("Error building VM session audit details: {}", e.getMessage());
            return "{}";
        }
    }
}
