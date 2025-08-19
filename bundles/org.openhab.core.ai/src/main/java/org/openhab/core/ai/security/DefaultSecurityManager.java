package org.openhab.core.ai.security;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.auth.AuthenticationContext;
import org.openhab.core.ai.common.security.BaseSecurityStatistics;
import org.openhab.core.ai.common.security.SecurityStatistics;
import org.openhab.core.ai.tool.security.filters.SecurityResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of the unified SecurityManager.
 * 
 * This class provides comprehensive security validation for AI services
 * including authentication, authorization, and access control.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class DefaultSecurityManager implements SecurityManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSecurityManager.class);

    // Security statistics
    private final AtomicLong totalChecks = new AtomicLong(0);
    private final AtomicLong allowedOperations = new AtomicLong(0);
    private final AtomicLong deniedOperations = new AtomicLong(0);
    private final AtomicLong securityViolations = new AtomicLong(0);
    private final AtomicLong totalResponseTime = new AtomicLong(0);

    // Component-specific security configurations
    private final Map<String, Map<String, Object>> componentSecurityRules = new ConcurrentHashMap<>();

    // Security violation tracking
    private final Map<String, Long> violationCounts = new ConcurrentHashMap<>();

    // Blocked users and components
    private final Map<String, Boolean> blockedUsers = new ConcurrentHashMap<>();
    private final Map<String, Boolean> blockedComponents = new ConcurrentHashMap<>();

    /**
     * Create a new DefaultSecurityManager.
     */
    public DefaultSecurityManager() {
        LOGGER.info("Default Security Manager initialized");
        initializeDefaultSecurityRules();
    }

    @Override
    public SecurityResult validateOperation(AuthenticationContext context) {
        long startTime = System.currentTimeMillis();
        totalChecks.incrementAndGet();

        try {
            String principalId = context.getPrincipalId();
            String sessionId = context.getSessionId();

            // Basic validation
            if (principalId == null || principalId.trim().isEmpty()) {
                deniedOperations.incrementAndGet();
                return SecurityResult.failure("Invalid principal ID");
            }

            // Check if user is blocked
            if (isUserBlocked(principalId)) {
                deniedOperations.incrementAndGet();
                logSecurityViolation("system", "Blocked user attempted operation",
                        Map.of("principalId", principalId, "sessionId", sessionId));
                return SecurityResult.failure("User is blocked from performing operations");
            }

            // Validate session
            if (!isValidSession(sessionId, principalId)) {
                deniedOperations.incrementAndGet();
                return SecurityResult.failure("Invalid or expired session");
            }

            // Component-specific validation
            boolean isAllowed = validateComponentOperation(principalId, sessionId, context);

            if (isAllowed) {
                allowedOperations.incrementAndGet();
                return SecurityResult.success(principalId, sessionId);
            } else {
                deniedOperations.incrementAndGet();
                return SecurityResult.failure("Operation not allowed for this context");
            }

        } finally {
            long responseTime = System.currentTimeMillis() - startTime;
            totalResponseTime.addAndGet(responseTime);
        }
    }

    @Override
    public boolean isExecutionAllowed(String componentId, String context) {
        // Validate component ID
        if (componentId == null || componentId.trim().isEmpty()) {
            logSecurityViolation(componentId, "Invalid component ID", Map.of("context", context));
            return false;
        }

        // Check if component is blocked
        if (isComponentBlocked(componentId)) {
            logSecurityViolation(componentId, "Blocked component execution attempted", Map.of("context", context));
            return false;
        }

        // Check if component has specific security rules
        Map<String, Object> rules = componentSecurityRules.get(componentId);
        if (rules != null && rules.containsKey("executionAllowed")) {
            return Boolean.TRUE.equals(rules.get("executionAllowed"));
        }

        // Default: allow execution for known components, deny for unknown/blocked
        boolean allowed = !componentId.startsWith("unknown_") && !componentId.startsWith("blocked_");

        if (!allowed) {
            logSecurityViolation(componentId, "Execution not allowed for component", Map.of("context", context));
        }

        return allowed;
    }

    @Override
    public boolean validateAccess(String componentId, @Nullable String userId) {
        // Validate component ID
        if (componentId == null || componentId.trim().isEmpty()) {
            logSecurityViolation(componentId, "Invalid component ID for access validation", Map.of("userId", userId));
            return false;
        }

        // If no user ID provided, deny access
        if (userId == null || userId.trim().isEmpty()) {
            logSecurityViolation(componentId, "No user ID provided for access validation", Map.of());
            return false;
        }

        // Check if user is blocked
        if (isUserBlocked(userId)) {
            logSecurityViolation(componentId, "Blocked user attempted access", Map.of("userId", userId));
            return false;
        }

        // Check if component has access rules
        Map<String, Object> rules = componentSecurityRules.get(componentId);
        if (rules != null && rules.containsKey("allowedUsers")) {
            @SuppressWarnings("unchecked")
            var allowedUsers = (java.util.List<String>) rules.get("allowedUsers");
            return allowedUsers != null && allowedUsers.contains(userId);
        }

        // Default: allow access for non-blocked users
        boolean allowed = !userId.startsWith("blocked_") && !userId.equals("anonymous");

        if (!allowed) {
            logSecurityViolation(componentId, "Access denied for user", Map.of("userId", userId));
        }

        return allowed;
    }

    @Override
    public void logSecurityViolation(String componentId, String violation, @Nullable Map<String, Object> context) {
        securityViolations.incrementAndGet();
        violationCounts.merge(componentId, 1L, Long::sum);

        LOGGER.warn("Security violation for component: {}, violation: {}, context: {}", componentId, violation,
                context);

        // Additional violation handling can be added here
        // e.g., alerting, blocking, etc.
    }

    @Override
    public SecurityStatistics getSecurityStatistics() {
        long total = totalChecks.get();
        return new BaseSecurityStatistics(total, allowedOperations.get(), deniedOperations.get(),
                securityViolations.get(), Instant.now()) {
            // Anonymous implementation using unified BaseSecurityStatistics
        };
    }

    // Private helper methods

    private void initializeDefaultSecurityRules() {
        // Initialize default security rules for common components
        Map<String, Object> toolRules = Map.of("executionAllowed", true, "allowedUsers",
                java.util.List.of("admin", "user", "system"));
        componentSecurityRules.put("tool", toolRules);

        Map<String, Object> agentRules = Map.of("executionAllowed", true, "allowedUsers",
                java.util.List.of("admin", "agent"));
        componentSecurityRules.put("agent", agentRules);

        Map<String, Object> messageRules = Map.of("executionAllowed", true, "allowedUsers",
                java.util.List.of("admin", "user", "system"));
        componentSecurityRules.put("message", messageRules);
    }

    private boolean isUserBlocked(String userId) {
        return blockedUsers.getOrDefault(userId, false);
    }

    private boolean isComponentBlocked(String componentId) {
        return blockedComponents.getOrDefault(componentId, false);
    }

    private boolean isValidSession(String sessionId, String principalId) {
        // Basic session validation - in a real implementation, this would check
        // session expiration, validity, etc.
        return sessionId != null && !sessionId.trim().isEmpty() && sessionId.length() >= 8; // Minimum session ID length
    }

    private boolean validateComponentOperation(String principalId, String sessionId, AuthenticationContext context) {
        // Basic validation logic
        return principalId != null && !principalId.trim().isEmpty() && sessionId != null && !sessionId.trim().isEmpty()
                && context.isValid();
    }

    // Public methods for security management

    /**
     * Block a user from performing operations.
     * 
     * @param userId the user ID to block
     */
    public void blockUser(String userId) {
        blockedUsers.put(userId, true);
        LOGGER.info("User blocked: {}", userId);
    }

    /**
     * Unblock a user.
     * 
     * @param userId the user ID to unblock
     */
    public void unblockUser(String userId) {
        blockedUsers.remove(userId);
        LOGGER.info("User unblocked: {}", userId);
    }

    /**
     * Block a component from execution.
     * 
     * @param componentId the component ID to block
     */
    public void blockComponent(String componentId) {
        blockedComponents.put(componentId, true);
        LOGGER.info("Component blocked: {}", componentId);
    }

    /**
     * Unblock a component.
     * 
     * @param componentId the component ID to unblock
     */
    public void unblockComponent(String componentId) {
        blockedComponents.remove(componentId);
        LOGGER.info("Component unblocked: {}", componentId);
    }

    /**
     * Set security rules for a component.
     * 
     * @param componentId the component ID
     * @param rules the security rules
     */
    public void setComponentSecurityRules(String componentId, Map<String, Object> rules) {
        componentSecurityRules.put(componentId, rules);
        LOGGER.info("Security rules set for component: {}", componentId);
    }
}
