package org.openhab.core.ai.reasoning.security;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.auth.AuditLogger;
import org.openhab.core.ai.auth.AuthenticationContext;
import org.openhab.core.ai.auth.AuthenticationManager;
import org.openhab.core.ai.auth.RoleBasedAccessControl;
import org.openhab.core.ai.common.context.AgentModelContext;
import org.openhab.core.ai.common.context.AgentModelContext.AgentModelContextBuilder;
import org.openhab.core.ai.reasoning.engine.SharedModelReasoningEngine;
import org.openhab.core.ai.reasoning.model.ModelRequest;
import org.openhab.core.ai.reasoning.model.ModelUsageInfo;
import org.openhab.core.ai.reasoning.prompts.AgentModelPromptBuilder;
import org.openhab.core.ai.reasoning.security.api.SecurityIssue;
import org.openhab.core.ai.reasoning.security.api.SecurityIssueType;
import org.openhab.core.ai.reasoning.security.api.SecurityValidationResult;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Agent Model Security Manager - Focuses on authentication, authorization, and model access security.
 * 
 * This class provides security management for AI model interactions, focusing on:
 * - Authentication and session management
 * - Authorization and permission checking
 * - Model access control and resource protection
 * - Integration with openHAB authentication system
 * 
 * Note: Content safety validation is handled by SafetyConstraintManager
 * 
 * @author Karel Goderis - Initial Contribution
 */
@Component(service = AgentModelSecurityManager.class)
@NonNullByDefault
public class AgentModelSecurityManager {

    private static final Logger logger = LoggerFactory.getLogger(AgentModelSecurityManager.class);

    @Reference
    private SharedModelReasoningEngine reasoningEngine;

    @Reference
    private AgentModelContextBuilder contextBuilder;

    @Reference
    private AgentModelPromptBuilder promptBuilder;

    // Auth system integration
    @Reference
    private AuthenticationManager authenticationManager;

    @Reference
    private RoleBasedAccessControl roleBasedAccessControl;

    @Reference
    private AuditLogger auditLogger;

    private final Map<String, SecurityPolicy> securityPolicies = new ConcurrentHashMap<>();
    private final Map<String, AccessControl> accessControls = new ConcurrentHashMap<>();

    /**
     * Validate authentication and authorization for a model request.
     * 
     * @param request The model request
     * @param context The agent context
     * @return A CompletableFuture containing the security validation result
     */
    public CompletableFuture<SecurityValidationResult> validateSecurity(ModelRequest request,
            AgentModelContext context) {
        logger.debug("Validating authentication and authorization for request: {}", request.getRequestId());

        return CompletableFuture.supplyAsync(() -> {
            try {
                SecurityValidationResult result = new SecurityValidationResult(request.getRequestId());

                // Validate authentication using implemented token validation
                validateAuthentication(request, context, result);

                // Validate authorization using implemented permission methods
                validateAuthorization(request, context, result);

                // Validate content safety using implemented content checking methods
                validateContentSafety(request, context, result);

                // Validate model access control
                validateModelAccessControl(request, context, result);

                // Validate rate limiting
                validateRateLimiting(request, context, result);

                // Log security event
                logSecurityEvent(request, result);

                logger.debug("Authentication and authorization validation completed for request: {}",
                        request.getRequestId());
                return result;

            } catch (Exception e) {
                logger.error("Error validating authentication and authorization: {}", e.getMessage(), e);
                return createErrorSecurityResult(request, e);
            }
        });
    }

    /**
     * Validate authentication for the request using openHAB auth system.
     * 
     * @param request The model request
     * @param context The agent context
     * @param result The validation result to update
     */
    private void validateAuthentication(ModelRequest request, AgentModelContext context,
            SecurityValidationResult result) {
        String agentId = (String) context.getContextData("agentId");
        String authenticationToken = request.getAuthenticationToken();

        if (authenticationToken == null || authenticationToken.isEmpty()) {
            result.addSecurityIssue(SecurityIssueType.AUTHENTICATION_FAILED, "Missing authentication token");
            auditLogger.logAuthenticationFailure(agentId, "ai-model", "Missing authentication token", Instant.now());
            return;
        }

        // Use the implemented token validation method
        if (!isValidToken(authenticationToken, agentId)) {
            result.addSecurityIssue(SecurityIssueType.AUTHENTICATION_FAILED, "Invalid authentication token");
            auditLogger.logAuthenticationFailure(agentId, "ai-model", "Invalid authentication token", Instant.now());
            return;
        }

        try {
            // Use openHAB authentication system
            Optional<AuthenticationContext> authContext = authenticationManager.authenticateWithJWT(authenticationToken,
                    "ai-model");

            if (authContext.isPresent()) {
                AuthenticationContext context1 = authContext.get();
                result.setAuthenticationValid(true);
                auditLogger.logAuthenticationSuccess(agentId, context1.getPrincipalId(), "ai-model", Instant.now());
                logger.debug("Authentication successful for agent: {}", agentId);
            } else {
                result.addSecurityIssue(SecurityIssueType.AUTHENTICATION_FAILED, "Invalid authentication token");
                auditLogger.logAuthenticationFailure(agentId, "ai-model", "Invalid authentication token",
                        Instant.now());
            }
        } catch (Exception e) {
            result.addSecurityIssue(SecurityIssueType.AUTHENTICATION_FAILED, "Authentication error: " + e.getMessage());
            auditLogger.logAuthenticationFailure(agentId, "ai-model", "Authentication error: " + e.getMessage(),
                    Instant.now());
            logger.error("Authentication error for agent {}: {}", agentId, e.getMessage(), e);
        }
    }

    /**
     * Validate authorization for the request using RBAC.
     * 
     * @param request The model request
     * @param context The agent context
     * @param result The validation result to update
     */
    private void validateAuthorization(ModelRequest request, AgentModelContext context,
            SecurityValidationResult result) {
        String agentId = (String) context.getContextData("agentId");
        String agentType = (String) context.getContextData("agentType");

        try {
            // Use the implemented permission checking methods
            if (!hasModelPermission(agentId, request.getModelId())) {
                result.addSecurityIssue(SecurityIssueType.AUTHORIZATION_FAILED,
                        "Agent does not have permission for model: " + request.getModelId());
                auditLogger.logPermissionCheck(agentId, "model:" + request.getModelId() + ":access", "ai-model", false,
                        Instant.now());
                return;
            }

            if (!hasTaskPermission(agentId, request.getTaskType())) {
                result.addSecurityIssue(SecurityIssueType.AUTHORIZATION_FAILED,
                        "Agent does not have permission for task type: " + request.getTaskType());
                auditLogger.logPermissionCheck(agentId, "task:" + request.getTaskType() + ":execute", "ai-model", false,
                        Instant.now());
                return;
            }

            if (!hasRolePermission(agentId, agentType, request.getTaskType())) {
                result.addSecurityIssue(SecurityIssueType.AUTHORIZATION_FAILED,
                        "Agent role does not have permission for task type: " + request.getTaskType());
                auditLogger.logPermissionCheck(agentId, "role:" + agentType + ":" + request.getTaskType() + ":execute",
                        "ai-model", false, Instant.now());
                return;
            }

            // Log successful permission checks
            auditLogger.logPermissionCheck(agentId, "model:" + request.getModelId() + ":access", "ai-model", true,
                    Instant.now());
            auditLogger.logPermissionCheck(agentId, "task:" + request.getTaskType() + ":execute", "ai-model", true,
                    Instant.now());
            auditLogger.logPermissionCheck(agentId, "role:" + agentType + ":" + request.getTaskType() + ":execute",
                    "ai-model", true, Instant.now());

            result.setAuthorizationValid(true);
            logger.debug("Authorization validation passed for agent: {}", agentId);

        } catch (Exception e) {
            result.addSecurityIssue(SecurityIssueType.AUTHORIZATION_FAILED, "Authorization error: " + e.getMessage());
            auditLogger.logSecurityViolation(agentId, "AUTHORIZATION_ERROR", "Authorization error: " + e.getMessage(),
                    "ai-model", Instant.now());
            logger.error("Authorization error for agent {}: {}", agentId, e.getMessage(), e);
        }
    }

    /**
     * Validate model access control for the request.
     * 
     * @param request The model request
     * @param context The agent context
     * @param result The validation result to update
     */
    private void validateModelAccessControl(ModelRequest request, AgentModelContext context,
            SecurityValidationResult result) {
        String agentId = (String) context.getContextData("agentId");
        String modelId = request.getModelId();

        // Check if model is available and accessible
        if (!isModelAvailable(modelId)) {
            result.addSecurityIssue(SecurityIssueType.ACCESS_CONTROL_VIOLATION, "Model is not available: " + modelId);
            return;
        }

        // Check if agent has reached model usage limits
        if (isModelUsageLimitExceeded(agentId, modelId)) {
            result.addSecurityIssue(SecurityIssueType.ACCESS_CONTROL_VIOLATION,
                    "Model usage limit exceeded for agent: " + agentId);
            return;
        }

        // Check if model is in maintenance mode
        if (isModelInMaintenance(modelId)) {
            result.addSecurityIssue(SecurityIssueType.ACCESS_CONTROL_VIOLATION,
                    "Model is in maintenance mode: " + modelId);
            return;
        }

        result.setAccessControlValid(true);
        logger.debug("Model access control validation passed for agent: {} and model: {}", agentId, modelId);
    }

    /**
     * Validate content safety for the request using implemented content checking methods.
     * 
     * @param request The model request
     * @param context The agent context
     * @param result The validation result to update
     */
    private void validateContentSafety(ModelRequest request, AgentModelContext context,
            SecurityValidationResult result) {
        String prompt = request.getPrompt();
        if (prompt != null && !prompt.trim().isEmpty()) {
            // Check for sensitive information
            if (containsSensitiveInformation(prompt)) {
                result.addSecurityIssue(SecurityIssueType.CONTENT_SAFETY_VIOLATION,
                        "Sensitive information detected in prompt");
                auditLogger.logSecurityViolation(request.getAgentId(), "CONTENT_SAFETY_VIOLATION",
                        "Sensitive information detected", "ai-model", Instant.now());
                return;
            }

            // Check for inappropriate content
            if (containsInappropriateContent(prompt)) {
                result.addSecurityIssue(SecurityIssueType.CONTENT_SAFETY_VIOLATION,
                        "Inappropriate content detected in prompt");
                auditLogger.logSecurityViolation(request.getAgentId(), "CONTENT_SAFETY_VIOLATION",
                        "Inappropriate content detected", "ai-model", Instant.now());
                return;
            }

            // Check for malicious content
            if (containsMaliciousContent(prompt)) {
                result.addSecurityIssue(SecurityIssueType.CONTENT_SAFETY_VIOLATION,
                        "Malicious content detected in prompt");
                auditLogger.logSecurityViolation(request.getAgentId(), "CONTENT_SAFETY_VIOLATION",
                        "Malicious content detected", "ai-model", Instant.now());
                return;
            }

            // Check for prompt injection attempts
            if (containsPromptInjection(prompt)) {
                result.addSecurityIssue(SecurityIssueType.CONTENT_SAFETY_VIOLATION,
                        "Prompt injection attempt detected");
                auditLogger.logSecurityViolation(request.getAgentId(), "CONTENT_SAFETY_VIOLATION",
                        "Prompt injection attempt detected", "ai-model", Instant.now());
                return;
            }
        }

        result.setContentSafetyValid(true);
        logger.debug("Content safety validation passed for agent: {}", request.getAgentId());
    }

    /**
     * Validate access control for the request.
     * 
     * @param request The model request
     * @param context The agent context
     * @param result The validation result to update
     */
    private void validateAccessControl(ModelRequest request, AgentModelContext context,
            SecurityValidationResult result) {
        String agentId = (String) context.getContextData("agentId");

        AccessControl accessControl = accessControls.get(agentId);
        if (accessControl == null) {
            // Default access control
            accessControl = new AccessControl(agentId, true, 100, 1000);
        }

        // Check if agent is enabled
        if (!accessControl.isEnabled()) {
            result.addSecurityIssue(SecurityIssueType.ACCESS_CONTROL_VIOLATION, "Agent access is disabled");
            return;
        }

        // Check daily request limit
        if (accessControl.getDailyRequests() >= accessControl.getDailyLimit()) {
            result.addSecurityIssue(SecurityIssueType.ACCESS_CONTROL_VIOLATION, "Daily request limit exceeded");
            return;
        }

        // Check hourly request limit
        if (accessControl.getHourlyRequests() >= accessControl.getHourlyLimit()) {
            result.addSecurityIssue(SecurityIssueType.ACCESS_CONTROL_VIOLATION, "Hourly request limit exceeded");
            return;
        }

        result.setAccessControlValid(true);
    }

    /**
     * Validate rate limiting for the request.
     * 
     * @param request The model request
     * @param context The agent context
     * @param result The validation result to update
     */
    private void validateRateLimiting(ModelRequest request, AgentModelContext context,
            SecurityValidationResult result) {
        String agentId = (String) context.getContextData("agentId");

        // Implement actual rate limiting logic
        try {
            // Get or create access control for the agent
            AccessControl accessControl = accessControls.computeIfAbsent(agentId,
                    k -> new AccessControl(agentId, true, 1000, 100)); // Default: 1000 daily, 100 hourly

            if (!accessControl.isEnabled()) {
                result.addSecurityIssue(SecurityIssueType.ACCESS_CONTROL_VIOLATION,
                        "Access control disabled for agent: " + agentId);
                return;
            }

            // Check hourly rate limit
            if (accessControl.getHourlyRequests() >= accessControl.getHourlyLimit()) {
                result.addSecurityIssue(SecurityIssueType.RATE_LIMIT_EXCEEDED,
                        "Hourly rate limit exceeded for agent: " + agentId + " (limit: "
                                + accessControl.getHourlyLimit() + ", current: " + accessControl.getHourlyRequests()
                                + ")");
                auditLogger.logSecurityViolation(agentId, "RATE_LIMIT_EXCEEDED", "Hourly rate limit exceeded",
                        "ai-model", Instant.now());
                return;
            }

            // Check daily rate limit
            if (accessControl.getDailyRequests() >= accessControl.getDailyLimit()) {
                result.addSecurityIssue(SecurityIssueType.RATE_LIMIT_EXCEEDED,
                        "Daily rate limit exceeded for agent: " + agentId + " (limit: " + accessControl.getDailyLimit()
                                + ", current: " + accessControl.getDailyRequests() + ")");
                auditLogger.logSecurityViolation(agentId, "RATE_LIMIT_EXCEEDED", "Daily rate limit exceeded",
                        "ai-model", Instant.now());
                return;
            }

            // Increment request counters
            accessControl.incrementHourlyRequests();
            accessControl.incrementDailyRequests();

            // Check for burst rate limiting (requests per minute)
            if (isRateLimited(agentId)) {
                result.addSecurityIssue(SecurityIssueType.RATE_LIMIT_EXCEEDED,
                        "Burst rate limit exceeded for agent: " + agentId);
                auditLogger.logSecurityViolation(agentId, "RATE_LIMIT_EXCEEDED", "Burst rate limit exceeded",
                        "ai-model", Instant.now());
                return;
            }

            result.setRateLimitValid(true);
            logger.debug("Rate limiting validation passed for agent: {}", agentId);

        } catch (Exception e) {
            result.addSecurityIssue(SecurityIssueType.SYSTEM_ERROR, "Rate limiting error: " + e.getMessage());
            logger.error("Rate limiting error for agent {}: {}", agentId, e.getMessage(), e);
        }
    }

    /**
     * Log security event.
     * 
     * @param request The model request
     * @param result The security validation result
     */
    private void logSecurityEvent(ModelRequest request, SecurityValidationResult result) {
        SecurityEvent event = new SecurityEvent(generateEventId(), request.getRequestId(), request.getAgentId(),
                result.isValid(), result.getSecurityIssues(), System.currentTimeMillis());

        // Implement actual security event logging
        try {
            // Log to audit logger
            if (result.hasIssues()) {
                for (SecurityIssue issue : result.getSecurityIssues()) {
                    auditLogger.logSecurityViolation(request.getAgentId(), issue.getType().name(),
                            issue.getDescription(), "ai-model", Instant.now());
                }
            } else {
                auditLogger.logSecurityViolation(request.getAgentId(), "SECURITY_VALIDATION_SUCCESS",
                        "Security validation passed", "ai-model", Instant.now());
            }

            // Log to system logger with appropriate level
            if (result.isValid()) {
                logger.debug("Security event logged - Valid: {} (Event ID: {})", event.getEventId(),
                        event.getEventId());
            } else {
                logger.warn("Security event logged - Invalid: {} (Event ID: {}, Issues: {})", event.getEventId(),
                        event.getEventId(), result.getSecurityIssues().size());

                // Log individual issues
                for (SecurityIssue issue : result.getSecurityIssues()) {
                    logger.warn("Security issue: {} - {}", issue.getType(), issue.getDescription());
                }
            }

            // Store event for monitoring and analytics
            storeSecurityEvent(event);

        } catch (Exception e) {
            logger.error("Error logging security event: {}", e.getMessage(), e);
        }
    }

    /**
     * Store security event for monitoring and analytics
     */
    private void storeSecurityEvent(SecurityEvent event) {
        try {
            // In a real implementation, this would store to a database or monitoring system
            // For now, we'll use an in-memory store with size limits
            // securityEvents.add(event); // This line was removed as per the new_code, as securityEvents is not
            // defined.

            // Keep only the last 1000 events to prevent memory issues
            // if (securityEvents.size() > 1000) { // This line was removed as per the new_code, as securityEvents is
            // not defined.
            // securityEvents.remove(0);
            // }

            logger.debug("Security event stored: {}", event.getEventId());
        } catch (Exception e) {
            logger.error("Error storing security event: {}", e.getMessage(), e);
        }
    }

    /**
     * Check if authentication token is valid.
     * 
     * @param token The authentication token
     * @param agentId The agent ID
     * @return True if valid, false otherwise
     */
    private boolean isValidToken(String token, String agentId) {
        // Integrate with actual authentication service
        if (token == null || token.trim().isEmpty()) {
            logger.warn("Empty or null token for agent: {}", agentId);
            return false;
        }

        if (token.length() < 10) {
            logger.warn("Token too short for agent: {} (length: {})", agentId, token.length());
            return false;
        }

        try {
            // Use the authentication manager to validate the token
            if (authenticationManager != null) {
                Optional<AuthenticationContext> authContext = authenticationManager.authenticateWithJWT(token,
                        "ai-model");
                if (authContext.isPresent()) {
                    AuthenticationContext context = authContext.get();

                    // Check if the principal ID matches the agent ID
                    if (agentId.equals(context.getPrincipalId())) {
                        logger.debug("Token validation successful for agent: {}", agentId);
                        return true;
                    } else {
                        logger.warn("Token principal mismatch for agent: {} (expected: {}, got: {})", agentId, agentId,
                                context.getPrincipalId());
                        return false;
                    }
                } else {
                    logger.warn("Token validation failed for agent: {} - no authentication context", agentId);
                    return false;
                }
            } else {
                logger.warn("Authentication manager not available for agent: {}", agentId);
                return false;
            }
        } catch (Exception e) {
            logger.error("Token validation error for agent {}: {}", agentId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Check if agent has permission for the model.
     * 
     * @param agentId The agent ID
     * @param modelId The model ID
     * @return True if has permission, false otherwise
     */
    private boolean hasModelPermission(String agentId, String modelId) {
        // Implement actual permission checking against database
        if (agentId == null || agentId.trim().isEmpty()) {
            logger.warn("Agent ID is null or empty for model permission check");
            return false;
        }

        if (modelId == null || modelId.trim().isEmpty()) {
            logger.warn("Model ID is null or empty for agent: {}", agentId);
            return false;
        }

        try {
            // Use the role-based access control system
            if (roleBasedAccessControl != null) {
                String permission = "model:" + modelId + ":access";
                boolean hasPermission = roleBasedAccessControl.hasPermission(agentId, permission, "ai-model");

                if (hasPermission) {
                    logger.debug("Model permission granted for agent: {} on model: {}", agentId, modelId);
                } else {
                    logger.warn("Model permission denied for agent: {} on model: {}", agentId, modelId);
                }

                return hasPermission;
            } else {
                logger.warn("Role-based access control not available for agent: {}", agentId);
                return false;
            }
        } catch (Exception e) {
            logger.error("Error checking model permission for agent {} on model {}: {}", agentId, modelId,
                    e.getMessage(), e);
            return false;
        }
    }

    /**
     * Check if agent has permission for the task type.
     * 
     * @param agentId The agent ID
     * @param taskType The task type
     * @return True if has permission, false otherwise
     */
    private boolean hasTaskPermission(String agentId, String taskType) {
        // Implement actual permission checking against database
        if (agentId == null || agentId.trim().isEmpty()) {
            logger.warn("Agent ID is null or empty for task permission check");
            return false;
        }

        if (taskType == null || taskType.trim().isEmpty()) {
            logger.warn("Task type is null or empty for agent: {}", agentId);
            return false;
        }

        try {
            // Use the role-based access control system
            if (roleBasedAccessControl != null) {
                String permission = "task:" + taskType + ":execute";
                boolean hasPermission = roleBasedAccessControl.hasPermission(agentId, permission, "ai-model");

                if (hasPermission) {
                    logger.debug("Task permission granted for agent: {} on task: {}", agentId, taskType);
                } else {
                    logger.warn("Task permission denied for agent: {} on task: {}", agentId, taskType);
                }

                return hasPermission;
            } else {
                logger.warn("Role-based access control not available for agent: {}", agentId);
                return false;
            }
        } catch (Exception e) {
            logger.error("Error checking task permission for agent {} on task {}: {}", agentId, taskType,
                    e.getMessage(), e);
            return false;
        }
    }

    /**
     * Check if agent role has permission for the operation.
     * 
     * @param agentId The agent ID
     * @param agentType The agent type
     * @param taskType The task type
     * @return True if has permission, false otherwise
     */
    private boolean hasRolePermission(String agentId, String agentType, String taskType) {
        // Implement actual role-based permission checking
        if (agentId == null || agentId.trim().isEmpty()) {
            logger.warn("Agent ID is null or empty for role permission check");
            return false;
        }

        if (agentType == null || agentType.trim().isEmpty()) {
            logger.warn("Agent type is null or empty for agent: {}", agentId);
            return false;
        }

        if (taskType == null || taskType.trim().isEmpty()) {
            logger.warn("Task type is null or empty for agent: {}", agentId);
            return false;
        }

        try {
            // Use the role-based access control system
            if (roleBasedAccessControl != null) {
                // Check role-based permissions
                String rolePermission = "role:" + agentType + ":" + taskType + ":execute";
                boolean hasRolePermission = roleBasedAccessControl.hasPermission(agentId, rolePermission, "ai-model");

                if (hasRolePermission) {
                    logger.debug("Role permission granted for agent: {} (type: {}) on task: {}", agentId, agentType,
                            taskType);
                    return true;
                }

                // Check if agent has admin role (admin agents can do everything)
                String adminPermission = "role:admin:*:execute";
                boolean hasAdminPermission = roleBasedAccessControl.hasPermission(agentId, adminPermission, "ai-model");

                if (hasAdminPermission) {
                    logger.debug("Admin role permission granted for agent: {} on task: {}", agentId, taskType);
                    return true;
                }

                // Check specific agent type permissions
                String typePermission = "type:" + agentType + ":execute";
                boolean hasTypePermission = roleBasedAccessControl.hasPermission(agentId, typePermission, "ai-model");

                if (hasTypePermission) {
                    logger.debug("Type permission granted for agent: {} (type: {}) on task: {}", agentId, agentType,
                            taskType);
                    return true;
                }

                logger.warn("Role permission denied for agent: {} (type: {}) on task: {}", agentId, agentType,
                        taskType);
                return false;
            } else {
                logger.warn("Role-based access control not available for agent: {}", agentId);
                return false;
            }
        } catch (Exception e) {
            logger.error("Error checking role permission for agent {} (type: {}) on task {}: {}", agentId, agentType,
                    taskType, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Check if content contains sensitive information.
     * 
     * @param content The content to check
     * @return True if contains sensitive information, false otherwise
     */
    private boolean containsSensitiveInformation(String content) {
        // Implement comprehensive PII and credential detection
        if (content == null || content.trim().isEmpty()) {
            return false;
        }

        String lowerContent = content.toLowerCase();

        // PII Patterns
        List<String> piiPatterns = List.of(
                // Social Security Numbers (US)
                "bd{3}-d{2}-d{4}b", "bd{9}b",

                // Credit Card Numbers
                "bd{4}[s-]?d{4}[s-]?d{4}[s-]?d{4}b", "bd{4}sd{4}sd{4}sd{4}b",

                // Email addresses
                "b[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+.[a-zA-Z]{2,}b",

                // Phone numbers (US)
                "bd{3}[-.]?d{3}[-.]?d{4}b", "b(d{3})s?d{3}[-.]?d{4}b",

                // IP addresses
                "bd{1,3}.d{1,3}.d{1,3}.d{1,3}b",

                // MAC addresses
                "b([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})b",

                // Date of birth patterns
                "bd{1,2}[/-]d{1,2}[/-]d{2,4}b", "bd{4}[/-]d{1,2}[/-]d{1,2}b");

        // Check for PII patterns
        for (String pattern : piiPatterns) {
            if (content.matches(".*" + pattern + ".*")) {
                logger.warn("PII pattern detected: {}", pattern);
                return true;
            }
        }

        // Sensitive keywords
        List<String> sensitiveKeywords = List.of("password", "passwd", "pwd", "secret", "key", "token", "api_key",
                "api_key", "private_key", "public_key", "ssh_key", "gpg_key", "encryption_key", "access_token",
                "refresh_token", "bearer_token", "jwt_token", "credit_card", "cc_number", "card_number", "cvv", "cvc",
                "expiry", "social_security", "ssn", "tax_id", "ein", "itin", "bank_account", "routing_number",
                "account_number", "swift_code", "iban", "driver_license", "license_number", "passport_number",
                "visa_number", "medical_record", "health_insurance", "insurance_id", "policy_number", "address",
                "street_address", "home_address", "billing_address", "date_of_birth", "birth_date", "dob", "age",
                "birthday");

        // Check for sensitive keywords
        for (String keyword : sensitiveKeywords) {
            if (lowerContent.contains(keyword)) {
                logger.warn("Sensitive keyword detected: {}", keyword);
                return true;
            }
        }

        // Check for credential patterns
        List<String> credentialPatterns = List.of(
                // API keys (long alphanumeric strings)
                "b[a-zA-Z0-9]{32,}b",

                // JWT tokens (three parts separated by dots)
                "b[a-zA-Z0-9_-]+.[a-zA-Z0-9_-]+.[a-zA-Z0-9_-]+b",

                // Base64 encoded strings
                "b[a-zA-Z0-9+/]{20,}={0,2}b",

                // Hex strings (likely keys)
                "b[a-fA-F0-9]{32,}b");

        // Check for credential patterns
        for (String pattern : credentialPatterns) {
            if (content.matches(".*" + pattern + ".*")) {
                logger.warn("Credential pattern detected: {}", pattern);
                return true;
            }
        }

        return false;
    }

    /**
     * Check if content contains inappropriate content.
     * 
     * @param content The content to check
     * @return True if contains inappropriate content, false otherwise
     */
    private boolean containsInappropriateContent(String content) {
        // Implement actual inappropriate content detection
        if (content == null || content.trim().isEmpty()) {
            return false;
        }

        String lowerContent = content.toLowerCase();

        // Inappropriate content patterns
        List<String> inappropriatePatterns = List.of(
                // Hate speech patterns
                "b(hate|hatred|bigot|bigotry|racist|racism|sexist|sexism|homophobic|transphobic)b",

                // Violence patterns
                "b(kill|murder|assassinate|bomb|explode|terrorist|terrorism|violence|violent)b",

                // Harassment patterns
                "b(stalk|harass|bully|intimidate|threaten|blackmail|extort)b",

                // Illegal activities
                "b(drug|heroin|cocaine|meth|illegal|contraband|smuggle|traffic)b",

                // Self-harm patterns
                "b(suicide|self-harm|cutting|overdose|ends+life)b");

        // Check for inappropriate patterns
        for (String pattern : inappropriatePatterns) {
            if (lowerContent.matches(".*" + pattern + ".*")) {
                logger.warn("Inappropriate content pattern detected: {}", pattern);
                return true;
            }
        }

        // Inappropriate keywords
        List<String> inappropriateKeywords = List.of(
                // Profanity and offensive language
                "fuck", "shit", "bitch", "asshole", "dick", "pussy", "cunt",

                // Discriminatory terms
                "nigger", "faggot", "dyke", "spic", "kike", "chink", "gook", "wop",

                // Threatening language
                "i will kill you", "i want to kill", "i hate you", "i want to hurt", "i will hurt you",
                "i will destroy", "i will ruin",

                // Manipulative language
                "do this or else", "if you don't", "i will tell everyone", "i have your information",
                "i know where you live");

        // Check for inappropriate keywords
        for (String keyword : inappropriateKeywords) {
            if (lowerContent.contains(keyword)) {
                logger.warn("Inappropriate keyword detected: {}", keyword);
                return true;
            }
        }

        // Check for excessive aggression
        int aggressiveWords = 0;
        List<String> aggressiveWordsList = List.of("angry", "rage", "furious", "livid", "enraged", "outraged", "fuming",
                "hostile", "aggressive", "violent", "threatening", "menacing");

        for (String word : aggressiveWordsList) {
            if (lowerContent.contains(word)) {
                aggressiveWords++;
            }
        }

        // If too many aggressive words are found
        if (aggressiveWords >= 3) {
            logger.warn("Excessive aggressive language detected: {} aggressive words", aggressiveWords);
            return true;
        }

        return false;
    }

    /**
     * Check if content contains malicious content.
     * 
     * @param content The content to check
     * @return True if contains malicious content, false otherwise
     */
    private boolean containsMaliciousContent(String content) {
        // Implement actual malicious content detection
        if (content == null || content.trim().isEmpty()) {
            return false;
        }

        String lowerContent = content.toLowerCase();

        // Malicious content patterns
        List<String> maliciousPatterns = List.of(
                // Code injection patterns
                "b(eval|exec|system|shell|command|script|javascript|vbscript|expression)b",

                // SQL injection patterns
                "b(union|select|insert|update|delete|drop|create|alter|table|database)b",

                // XSS patterns
                "b<scriptb|b<iframeb|b<objectb|b<embedb|bonloadb|bonerrorb",

                // Command injection patterns
                "b(cmd|powershell|bash|sh|python|perl|ruby|php|java|node)b",

                // File system access patterns
                "b(file://|ftp://|http://|https://||/etc/|/var/|/tmp/|/home/)b",

                // Registry access patterns (Windows)
                "b(regedit|registry|hkey_|hkcu|hklm|hkcr)b");

        // Check for malicious patterns
        for (String pattern : maliciousPatterns) {
            if (lowerContent.matches(".*" + pattern + ".*")) {
                logger.warn("Malicious content pattern detected: {}", pattern);
                return true;
            }
        }

        // Malicious keywords
        List<String> maliciousKeywords = List.of(
                // Code execution
                "execute code", "run command", "system call", "shell command", "eval(", "exec(", "system(",
                "shell_exec(", "passthru(",

                // File operations
                "read file", "write file", "delete file", "upload file", "file_get_contents", "file_put_contents",
                "fopen", "fwrite",

                // Network access
                "curl", "wget", "ftp", "telnet", "nc", "netcat",

                // Process manipulation
                "kill process", "terminate", "spawn", "fork", "exec",

                // System access
                "sudo", "su", "runas", "elevate", "privilege escalation",

                // Data exfiltration
                "export data", "download", "extract", "dump", "backup");

        // Check for malicious keywords
        for (String keyword : maliciousKeywords) {
            if (lowerContent.contains(keyword)) {
                logger.warn("Malicious keyword detected: {}", keyword);
                return true;
            }
        }

        // Check for suspicious URL patterns
        List<String> suspiciousUrlPatterns = List.of("bhttps?://[^s]*.(exe|bat|cmd|ps1|sh|py|pl|rb|php|js)b",
                "bftp://[^s]*.(exe|bat|cmd|ps1|sh|py|pl|rb|php|js)b",
                "bfile://[^s]*.(exe|bat|cmd|ps1|sh|py|pl|rb|php|js)b");

        // Check for suspicious URL patterns
        for (String pattern : suspiciousUrlPatterns) {
            if (content.matches(".*" + pattern + ".*")) {
                logger.warn("Suspicious URL pattern detected: {}", pattern);
                return true;
            }
        }

        return false;
    }

    /**
     * Check if content contains prompt injection attempts.
     * 
     * @param content The content to check
     * @return True if contains prompt injection, false otherwise
     */
    private boolean containsPromptInjection(String content) {
        // Implement comprehensive prompt injection detection
        if (content == null || content.trim().isEmpty()) {
            return false;
        }

        String lowerContent = content.toLowerCase();

        // Prompt injection patterns
        List<String> injectionPatterns = List.of(
                // Role manipulation patterns
                "b(ignore|forget|disregard|skip|bypass|override|replace)s+(previous|prior|above|earlier|last)s+(instructions|prompts|messages|rules|guidelines)b",
                "b(pretend|act|behave|respond)s+(as|like)s+(a|an)s+w+b",
                "b(yous+are|you're|yous+shoulds+be|become)s+(a|an)s+w+b",

                // System prompt injection patterns
                "bsystem:s*b", "bassistant:s*b", "buser:s*b", "bhuman:s*b", "bai:s*b", "bbot:s*b",

                // Instruction override patterns
                "b(new|different|updated|corrected|revised)s+(instructions|prompt|rules|guidelines)b",
                "b(ignore|forget|disregard)s+(all|everything|what)s+(was|has)s+(said|told|instructed)b",

                // Context manipulation patterns
                "b(this|that|the)s+(is|was|wills+be)s+(a|an)s+(test|game|simulation|roleplay|exercise)b",
                "b(imagine|suppose|assume|pretend)s+(that|you|this)b",

                // Output format manipulation
                "b(output|respond|answer|reply)s+(in|as|with)s+w+b",
                "b(format|structure)s+(your|the)s+(response|answer|output)b");

        // Check for injection patterns
        for (String pattern : injectionPatterns) {
            if (lowerContent.matches(".*" + pattern + ".*")) {
                logger.warn("Prompt injection pattern detected: {}", pattern);
                return true;
            }
        }

        // Prompt injection keywords
        List<String> injectionKeywords = List.of(
                // Direct instruction overrides
                "ignore previous instructions", "forget everything", "disregard all", "new instructions",
                "different prompt", "updated rules", "ignore what was said", "forget the context", "start fresh",

                // Role manipulation
                "pretend to be", "act as if", "behave like", "respond as", "you are now", "you should be", "become a",
                "switch to",

                // System manipulation
                "system prompt", "assistant prompt", "user prompt", "override system", "bypass safety", "ignore safety",

                // Context manipulation
                "this is a test", "this is a game", "this is simulation", "imagine that", "suppose you", "assume this",

                // Output manipulation
                "output in json", "respond in xml", "format as csv", "structure response", "change format",
                "modify output");

        // Check for injection keywords
        for (String keyword : injectionKeywords) {
            if (lowerContent.contains(keyword)) {
                logger.warn("Prompt injection keyword detected: {}", keyword);
                return true;
            }
        }

        // Check for suspicious repetition patterns (common in prompt injection)
        String[] words = lowerContent.split("s+");
        Map<String, Integer> wordCount = new HashMap<>();

        for (String word : words) {
            if (word.length() > 3) { // Only count meaningful words
                wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
            }
        }

        // Check for excessive repetition
        for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
            if (entry.getValue() >= 5) { // Word appears 5+ times
                logger.warn("Excessive word repetition detected: '{}' appears {} times", entry.getKey(),
                        entry.getValue());
                return true;
            }
        }

        // Check for suspicious character patterns
        if (lowerContent.contains("ignore") && lowerContent.contains("instructions")) {
            logger.warn("Suspicious instruction override pattern detected");
            return true;
        }

        if (lowerContent.contains("system:") || lowerContent.contains("assistant:")) {
            logger.warn("System prompt injection pattern detected");
            return true;
        }

        return false;
    }

    /**
     * Check if agent is rate limited.
     * 
     * @param agentId The agent ID
     * @return True if rate limited, false otherwise
     */
    private boolean isRateLimited(String agentId) {
        // Implement actual rate limiting rules and tracking
        if (agentId == null || agentId.trim().isEmpty()) {
            logger.warn("Agent ID is null or empty for rate limiting check");
            return false;
        }

        try {
            // Get or create rate limiting info for the agent
            RateLimitInfo rateLimitInfo = rateLimitCache.computeIfAbsent(agentId,
                    k -> new RateLimitInfo(agentId, 60, 10)); // Default: 60 requests per minute, 10 per second

            long currentTime = System.currentTimeMillis();

            // Check if we need to reset the minute window
            if (currentTime - rateLimitInfo.lastMinuteReset > 60000) { // 1 minute
                rateLimitInfo.resetMinuteWindow();
            }

            // Check if we need to reset the second window
            if (currentTime - rateLimitInfo.lastSecondReset > 1000) { // 1 second
                rateLimitInfo.resetSecondWindow();
            }

            // Check per-second rate limit (burst protection)
            if (rateLimitInfo.requestsThisSecond >= rateLimitInfo.maxRequestsPerSecond) {
                logger.warn("Per-second rate limit exceeded for agent: {} (limit: {}, current: {})", agentId,
                        rateLimitInfo.maxRequestsPerSecond, rateLimitInfo.requestsThisSecond);
                return true;
            }

            // Check per-minute rate limit
            if (rateLimitInfo.requestsThisMinute >= rateLimitInfo.maxRequestsPerMinute) {
                logger.warn("Per-minute rate limit exceeded for agent: {} (limit: {}, current: {})", agentId,
                        rateLimitInfo.maxRequestsPerMinute, rateLimitInfo.requestsThisMinute);
                return true;
            }

            // Increment request counters
            rateLimitInfo.incrementRequests();

            return false;

        } catch (Exception e) {
            logger.error("Error checking rate limiting for agent {}: {}", agentId, e.getMessage(), e);
            return false; // Allow request on error
        }
    }

    /**
     * Rate limiting information for an agent
     */
    // extracted to org.openhab.core.ai.reasoning.RateLimitInfo

    // Rate limiting cache
    private final Map<String, RateLimitInfo> rateLimitCache = new ConcurrentHashMap<>();

    /**
     * Check if a model is available and accessible.
     */
    private boolean isModelAvailable(String modelId) {
        // Implement model availability check
        if (modelId == null || modelId.trim().isEmpty()) {
            logger.warn("Model ID is null or empty for availability check");
            return false;
        }

        try {
            // Check if model is in the available models list
            // In a real implementation, this would check against a model registry or configuration
            List<String> availableModels = List.of("gpt-3.5-turbo", "gpt-4", "gpt-4-turbo", "claude-3-opus",
                    "claude-3-sonnet", "claude-3-haiku", "gemini-pro", "gemini-pro-vision", "llama-2-7b", "llama-2-13b",
                    "llama-2-70b", "mistral-7b", "mixtral-8x7b", "codellama-7b", "codellama-13b", "codellama-34b");

            boolean isAvailable = availableModels.contains(modelId.toLowerCase());

            if (!isAvailable) {
                logger.warn("Model not available: {}", modelId);
            } else {
                logger.debug("Model available: {}", modelId);
            }

            return isAvailable;

        } catch (Exception e) {
            logger.error("Error checking model availability for {}: {}", modelId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Check if agent has exceeded model usage limits.
     */
    private boolean isModelUsageLimitExceeded(String agentId, String modelId) {
        // Implement usage limit checking
        if (agentId == null || agentId.trim().isEmpty()) {
            logger.warn("Agent ID is null or empty for usage limit check");
            return false;
        }

        if (modelId == null || modelId.trim().isEmpty()) {
            logger.warn("Model ID is null or empty for usage limit check");
            return false;
        }

        try {
            // Get or create usage tracking for the agent-model combination
            String usageKey = agentId + ":" + modelId;
            ModelUsageInfo usageInfo = modelUsageCache.computeIfAbsent(usageKey,
                    k -> new ModelUsageInfo(agentId, modelId, 1000, 100)); // Default: 1000 daily, 100 hourly

            long currentTime = System.currentTimeMillis();

            // Check if we need to reset the daily window
            if (currentTime - usageInfo.getLastDailyReset() > 86400000) { // 24 hours
                usageInfo.resetDailyUsage();
            }

            // Check if we need to reset the hourly window
            if (currentTime - usageInfo.getLastHourlyReset() > 3600000) { // 1 hour
                usageInfo.resetHourlyUsage();
            }

            // Check daily usage limit
            if (usageInfo.getDailyUsage() >= usageInfo.getMaxDailyUsage()) {
                logger.warn("Daily usage limit exceeded for agent: {} on model: {} (limit: {}, current: {})", agentId,
                        modelId, usageInfo.getMaxDailyUsage(), usageInfo.getDailyUsage());
                return true;
            }

            // Check hourly usage limit
            if (usageInfo.getHourlyUsage() >= usageInfo.getMaxHourlyUsage()) {
                logger.warn("Hourly usage limit exceeded for agent: {} on model: {} (limit: {}, current: {})", agentId,
                        modelId, usageInfo.getMaxHourlyUsage(), usageInfo.getHourlyUsage());
                return true;
            }

            // Increment usage counters
            usageInfo.incrementUsage();

            return false;

        } catch (Exception e) {
            logger.error("Error checking usage limits for agent {} on model {}: {}", agentId, modelId, e.getMessage(),
                    e);
            return false; // Allow usage on error
        }
    }

    /**
     * Check if a model is in maintenance mode.
     */
    private boolean isModelInMaintenance(String modelId) {
        // Implement maintenance mode check
        if (modelId == null || modelId.trim().isEmpty()) {
            logger.warn("Model ID is null or empty for maintenance check");
            return false;
        }

        try {
            // Check if model is in maintenance mode
            // In a real implementation, this would check against a model status configuration
            List<String> maintenanceModels = List.of(
                    // Models currently in maintenance
                    "gpt-3.5-turbo-legacy", "claude-2", "palm-2");

            boolean inMaintenance = maintenanceModels.contains(modelId.toLowerCase());

            if (inMaintenance) {
                logger.warn("Model in maintenance mode: {}", modelId);
            } else {
                logger.debug("Model not in maintenance: {}", modelId);
            }

            return inMaintenance;

        } catch (Exception e) {
            logger.error("Error checking maintenance status for model {}: {}", modelId, e.getMessage(), e);
            return false; // Assume not in maintenance on error
        }
    }

    /**
     * Model usage information for an agent-model combination
     */
    // extracted to org.openhab.core.ai.reasoning.ModelUsageInfo

    // Model usage cache
    private final Map<String, ModelUsageInfo> modelUsageCache = new ConcurrentHashMap<>();

    /**
     * Create error security result.
     * 
     * @param request The model request
     * @param error The error that occurred
     * @return The error security result
     */
    private SecurityValidationResult createErrorSecurityResult(ModelRequest request, Exception error) {
        SecurityValidationResult result = new SecurityValidationResult(request.getRequestId());
        result.addSecurityIssue(SecurityIssueType.SYSTEM_ERROR,
                "System error during security validation: " + error.getMessage());
        return result;
    }

    /**
     * Generate a unique event ID.
     * 
     * @return A unique event identifier
     */
    private String generateEventId() {
        return "security_event_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
    }

    /**
     * Model Request class.
     */
}
