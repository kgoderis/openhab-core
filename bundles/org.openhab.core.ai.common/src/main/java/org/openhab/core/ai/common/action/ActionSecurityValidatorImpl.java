package org.openhab.core.ai.common.action;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action Security Validator Implementation
 * 
 * <p>
 * This implementation provides:
 * - Action execution security validation
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
public class ActionSecurityValidatorImpl implements ActionSecurityValidator {

    private static final Logger logger = LoggerFactory.getLogger(ActionSecurityValidatorImpl.class);

    // Security policies
    private final ConcurrentHashMap<String, SecurityPolicy> actionPolicies = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Set<String>> clientPermissions = new ConcurrentHashMap<>();

    // Configuration
    private final AtomicReference<String> policyVersion = new AtomicReference<>("1.0.0");
    private final AtomicReference<Boolean> enableStrictValidation = new AtomicReference<>(true);
    private final AtomicReference<Boolean> enableAuditLogging = new AtomicReference<>(true);

    public ActionSecurityValidatorImpl() {
        // Initialize default security policies
        initializeDefaultPolicies();
    }

    @Override
    public boolean validateAction(AIActionContext actionContext) {
        try {
            // Basic validation
            if (!validateBasicRequirements(actionContext)) {
                logSecurityEvent("BASIC_VALIDATION_FAILED", actionContext, "Basic validation requirements not met");
                return false;
            }

            // Get action name
            Map<String, Object> protocolContext = actionContext.getProtocolContext();
            String actionName = (String) protocolContext.get("action");

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
    private boolean validateBasicRequirements(AIActionContext actionContext) {
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

        // Check protocol context
        Map<String, Object> protocolContext = actionContext.getProtocolContext();
        if (protocolContext == null || protocolContext.isEmpty()) {
            return false;
        }

        return true;
    }

    /**
     * Validate action policy
     */
    private boolean validateActionPolicy(String actionName, AIActionContext actionContext) {
        SecurityPolicy policy = actionPolicies.get(actionName);

        if (policy == null) {
            // Default policy for unknown actions
            return !enableStrictValidation.get();
        }

        // Check if action is enabled
        if (!policy.isEnabled()) {
            return false;
        }

        // Check rate limiting
        if (policy.hasRateLimit() && !checkRateLimit(actionName, actionContext)) {
            return false;
        }

        // Check time restrictions
        if (policy.hasTimeRestrictions() && !checkTimeRestrictions(policy)) {
            return false;
        }

        return true;
    }

    /**
     * Validate client permissions
     */
    private boolean validateClientPermissions(AIActionContext actionContext) {
        String clientId = actionContext.getClientId();
        Set<String> permissions = clientPermissions.get(clientId);

        if (permissions == null) {
            // Default permissions for unknown clients
            return !enableStrictValidation.get();
        }

        Map<String, Object> protocolContext = actionContext.getProtocolContext();
        String actionName = (String) protocolContext.get("action");

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
    private boolean validateDangerousAction(AIActionContext actionContext) {
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
    private boolean checkRateLimit(String actionName, AIActionContext actionContext) {
        // Simple rate limiting implementation
        // In a real implementation, this would use a proper rate limiting service
        return true;
    }

    /**
     * Check time restrictions
     */
    private boolean checkTimeRestrictions(SecurityPolicy policy) {
        // Simple time restriction check
        // In a real implementation, this would check against allowed time windows
        return true;
    }

    /**
     * Log security event
     */
    private void logSecurityEvent(String eventType, AIActionContext actionContext, String message) {
        if (enableAuditLogging.get()) {
            logger.info("SECURITY_EVENT [{}] - Action: {}, Client: {}, Session: {}, Message: {}", eventType,
                    actionContext.getCorrelationId(), actionContext.getClientId(), actionContext.getSessionId(),
                    message);
        }
    }

    /**
     * Initialize default security policies
     */
    private void initializeDefaultPolicies() {
        // Add default policies for common actions
        actionPolicies.put("openhab.items.get", new SecurityPolicy(true, false, false));
        actionPolicies.put("openhab.items.set", new SecurityPolicy(true, true, false));
        actionPolicies.put("openhab.things.get", new SecurityPolicy(true, false, false));
        actionPolicies.put("openhab.rules.get", new SecurityPolicy(true, false, false));
        actionPolicies.put("openhab.rules.create", new SecurityPolicy(true, true, false));
        actionPolicies.put("openhab.rules.delete", new SecurityPolicy(false, true, true));

        // Add default client permissions
        clientPermissions.put("reasoning-engine", Set.of("*"));
        clientPermissions.put("test-client", Set.of("openhab.items.get", "openhab.things.get"));
    }

    /**
     * Add security policy for an action
     */
    public void addSecurityPolicy(String actionName, SecurityPolicy policy) {
        actionPolicies.put(actionName, policy);
        logger.info("Security policy added for action: {}", actionName);
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

    /**
     * Security Policy class
     */
    public static class SecurityPolicy {
        private final boolean enabled;
        private final boolean hasRateLimit;
        private final boolean hasTimeRestrictions;

        public SecurityPolicy(boolean enabled, boolean hasRateLimit, boolean hasTimeRestrictions) {
            this.enabled = enabled;
            this.hasRateLimit = hasRateLimit;
            this.hasTimeRestrictions = hasTimeRestrictions;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public boolean hasRateLimit() {
            return hasRateLimit;
        }

        public boolean hasTimeRestrictions() {
            return hasTimeRestrictions;
        }
    }
}
