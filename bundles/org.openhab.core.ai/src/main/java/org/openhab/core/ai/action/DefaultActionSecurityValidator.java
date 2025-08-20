package org.openhab.core.ai.action;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.action.api.ActionKeys;
import org.openhab.core.ai.action.api.ActionSecurityValidator;
import org.openhab.core.ai.common.context.ExecutionContext;
import org.openhab.core.ai.common.security.SecurityLevel;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action Security Validator Implementation
 * 
 * <p>
 * This implementation provides:
 * - Action execution security validation using enhanced ActionSecurityPolicy
 * - Permission checking for action execution
 * - Access control validation
 * - Security policy enforcement
 * - Audit logging for security events
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = ActionSecurityValidator.class)
@NonNullByDefault
public class DefaultActionSecurityValidator implements ActionSecurityValidator {

    private static final Logger logger = LoggerFactory.getLogger(DefaultActionSecurityValidator.class);

    // Enhanced security policies using ActionSecurityPolicy
    private final ConcurrentHashMap<String, ActionSecurityPolicy> actionPolicies = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Set<String>> clientPermissions = new ConcurrentHashMap<>();

    // Configuration
    private final AtomicReference<String> policyVersion = new AtomicReference<>("1.0.0");
    private final AtomicReference<Boolean> enableStrictValidation = new AtomicReference<>(true);
    private final AtomicReference<Boolean> enableAuditLogging = new AtomicReference<>(true);

    public DefaultActionSecurityValidator() {
        // Initialize default security policies
        initializeDefaultPolicies();
    }

    @Override
    public boolean validateAction(ExecutionContext actionContext) {
        try {
            // Basic validation
            if (!validateBasicRequirements(actionContext)) {
                logSecurityEvent("BASIC_VALIDATION_FAILED", actionContext, "Basic validation requirements not met");
                return false;
            }

            // Get action name
            String actionName = actionContext.getValue(ActionKeys.ACTION_NAME.getKey(), String.class);

            if (actionName == null) {
                logSecurityEvent("MISSING_ACTION_NAME", actionContext, "Action name not found in context");
                return false;
            }

            // Check action policy
            if (!validateActionPolicy(actionName, actionContext)) {
                logSecurityEvent("ACTION_POLICY_VIOLATION", actionContext, "Action policy validation failed");
                return false;
            }

            // Check client permissions
            if (!validateClientPermissions(actionContext)) {
                logSecurityEvent("CLIENT_PERMISSION_DENIED", actionContext, "Client lacks required permissions");
                return false;
            }

            // Check for dangerous actions
            if (isDangerousAction(actionName) && !validateDangerousAction(actionContext)) {
                logSecurityEvent("DANGEROUS_ACTION_DENIED", actionContext, "Dangerous action execution denied");
                return false;
            }

            logSecurityEvent("VALIDATION_SUCCESS", actionContext, "Action validation successful");
            return true;

        } catch (Exception e) {
            logger.error("Error during action validation: {}", actionContext.getCorrelationId(), e);
            logSecurityEvent("VALIDATION_ERROR", actionContext, "Validation error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPolicyVersion() {
        return policyVersion.get();
    }

    /**
     * Validate basic requirements
     */
    private boolean validateBasicRequirements(ExecutionContext actionContext) {
        // Check correlation ID
        if (actionContext.getCorrelationId() == null || actionContext.getCorrelationId().isEmpty()) {
            return false;
        }

        // Check client ID
        if (actionContext.getClientId() == null || actionContext.getClientId().isEmpty()) {
            return false;
        }

        // Check session ID
        if (actionContext.getSessionId() == null || actionContext.getSessionId().isEmpty()) {
            return false;
        }

        // Check if context has any values
        if (actionContext.getAllValues().isEmpty()) {
            return false;
        }

        return true;
    }

    /**
     * Validate action policy using enhanced ActionSecurityPolicy
     */
    private boolean validateActionPolicy(String actionName, ExecutionContext actionContext) {
        ActionSecurityPolicy policy = actionPolicies.get(actionName);

        if (policy == null) {
            // Default policy for unknown actions
            return !enableStrictValidation.get();
        }

        // Check if action is enabled
        if (policy.getSecurityLevel() == SecurityLevel.MAXIMUM) {
            // Critical security level requires additional validation
            return validateCriticalAction(actionContext);
        }

        // Check authentication requirements
        if (policy.isRequiresAuthentication() && !validateAuthentication(actionContext)) {
            return false;
        }

        // Check authorization requirements
        if (policy.isRequiresAuthorization() && !validateAuthorization(actionContext, policy)) {
            return false;
        }

        // Check rate limiting for high security actions
        if (policy.getSecurityLevel() == SecurityLevel.HIGH && !checkRateLimit(actionName, actionContext)) {
            return false;
        }

        return true;
    }

    /**
     * Validate client permissions
     */
    private boolean validateClientPermissions(ExecutionContext actionContext) {
        String clientId = actionContext.getClientId();
        Set<String> permissions = clientPermissions.get(clientId);

        if (permissions == null) {
            // Default permissions for unknown clients
            return !enableStrictValidation.get();
        }

        String actionName = actionContext.getValue(ActionKeys.ACTION_NAME.getKey(), String.class);

        if (actionName == null) {
            return false;
        }

        // Check if client has permission for this action
        return permissions.contains(actionName) || permissions.contains("*");
    }

    /**
     * Check if action is dangerous
     */
    private boolean isDangerousAction(String actionName) {
        Set<String> dangerousActions = Set.of("system.shutdown", "system.restart", "filesystem.delete",
                "filesystem.format", "network.disconnect", "security.disable");

        return dangerousActions.contains(actionName);
    }

    /**
     * Validate dangerous action execution
     */
    private boolean validateDangerousAction(ExecutionContext actionContext) {
        // For dangerous actions, require additional validation
        // This could include:
        // - User confirmation
        // - Multi-factor authentication
        // - Admin privileges
        // - Audit trail

        // For now, deny all dangerous actions
        return false;
    }

    /**
     * Check rate limiting
     */
    private boolean checkRateLimit(String actionName, ExecutionContext actionContext) {
        // Simple rate limiting implementation
        // In a real implementation, this would use a proper rate limiting service
        return true;
    }

    /**
     * Validate authentication for action execution
     */
    private boolean validateAuthentication(ExecutionContext actionContext) {
        // TODO: Implement proper authentication validation
        // This should check for valid authentication tokens, API keys, etc.
        String clientId = actionContext.getClientId();
        return clientId != null && !clientId.isEmpty();
    }

    /**
     * Validate authorization for action execution
     */
    private boolean validateAuthorization(ExecutionContext actionContext, ActionSecurityPolicy policy) {
        // TODO: Implement proper authorization validation
        // This should check user roles, permissions, etc.
        String clientId = actionContext.getClientId();
        Set<String> permissions = clientPermissions.get(clientId);

        if (permissions == null) {
            return false;
        }

        // Check if client has required permissions
        return permissions.contains("*") || policy.getAllowedAgents().contains(clientId);
    }

    /**
     * Validate critical security level actions
     */
    private boolean validateCriticalAction(ExecutionContext actionContext) {
        // TODO: Implement critical action validation
        // This should include multi-factor authentication, admin approval, etc.
        logger.warn("Critical action validation not yet implemented");
        return false;
    }

    /**
     * Check time restrictions
     */
    private boolean checkTimeRestrictions(ActionSecurityPolicy policy) {
        // TODO: Implement time restriction check using ActionSecurityPolicy
        // This should check against allowed time windows defined in the policy
        return true;
    }

    /**
     * Log security event
     */
    private void logSecurityEvent(String eventType, ExecutionContext actionContext, String message) {
        if (enableAuditLogging.get()) {
            logger.info("SECURITY_EVENT [{}] - Action: {}, Client: {}, Session: {}, Message: {}", eventType,
                    actionContext.getCorrelationId(), actionContext.getClientId(), actionContext.getSessionId(),
                    message);
        }
    }

    /**
     * Initialize default security policies using enhanced ActionSecurityPolicy
     */
    private void initializeDefaultPolicies() {
        // Add default policies for common actions using enhanced ActionSecurityPolicy
        actionPolicies.put("openhab.items.get", ActionSecurityPolicy.builder().actionId("openhab.items.get")
                .securityLevel(SecurityLevel.LOW).requiresAuthentication(false).requiresAuthorization(false).build());

        actionPolicies.put("openhab.items.set", ActionSecurityPolicy.builder().actionId("openhab.items.set")
                .securityLevel(SecurityLevel.MEDIUM).requiresAuthentication(true).requiresAuthorization(true).build());

        actionPolicies.put("openhab.things.get", ActionSecurityPolicy.builder().actionId("openhab.things.get")
                .securityLevel(SecurityLevel.LOW).requiresAuthentication(false).requiresAuthorization(false).build());

        actionPolicies.put("openhab.rules.get", ActionSecurityPolicy.builder().actionId("openhab.rules.get")
                .securityLevel(SecurityLevel.LOW).requiresAuthentication(false).requiresAuthorization(false).build());

        actionPolicies.put("openhab.rules.create", ActionSecurityPolicy.builder().actionId("openhab.rules.create")
                .securityLevel(SecurityLevel.MEDIUM).requiresAuthentication(true).requiresAuthorization(true).build());

        actionPolicies.put("openhab.rules.delete", ActionSecurityPolicy.builder().actionId("openhab.rules.delete")
                .securityLevel(SecurityLevel.HIGH).requiresAuthentication(true).requiresAuthorization(true).build());

        // Add default client permissions
        clientPermissions.put("reasoning-engine", Set.of("*"));
        clientPermissions.put("test-client", Set.of("openhab.items.get", "openhab.things.get"));
    }

    /**
     * Add security policy for an action using enhanced ActionSecurityPolicy
     */
    public void addSecurityPolicy(String actionName, ActionSecurityPolicy policy) {
        actionPolicies.put(actionName, policy);
        logger.info("Enhanced security policy added for action: {}", actionName);
    }

    /**
     * Add client permissions
     */
    public void addClientPermissions(String clientId, Set<String> permissions) {
        clientPermissions.put(clientId, permissions);
        logger.info("Permissions added for client: {}", clientId);
    }

    /**
     * Update policy version
     */
    public void setPolicyVersion(String version) {
        policyVersion.set(version);
        logger.info("Security policy version updated to: {}", version);
    }
}
