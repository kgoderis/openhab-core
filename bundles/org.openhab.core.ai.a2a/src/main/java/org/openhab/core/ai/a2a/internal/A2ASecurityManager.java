package org.openhab.core.ai.a2a.internal;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.openhab.core.ai.common.auth.AIAuditLogger;
import org.openhab.core.ai.common.auth.AIAuthenticationContext;
import org.openhab.core.ai.common.auth.AIAuthenticationManager;
import org.openhab.core.ai.common.auth.AIRoleBasedAccessControl;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.a2a.spec.Message;

/**
 * Security manager for A2A server integration with ai.common bundle.
 * 
 * This class provides authentication, authorization, rate limiting, and
 * request validation for A2A server operations. It orchestrates the
 * ai.common authentication providers and adds A2A-specific security features.
 * 
 * 
 */
@Component(service = A2ASecurityManager.class, immediate = true)
public class A2ASecurityManager {

    private static final Logger logger = LoggerFactory.getLogger(A2ASecurityManager.class);

    // Core ai.common components
    @Reference
    private AIAuthenticationManager authManager;

    @Reference
    private AIRoleBasedAccessControl rbac;

    @Reference
    private AIAuditLogger auditLogger;

    // A2A-specific configuration
    private final A2AServerConfiguration config;

    // Rate limiting and security tracking
    private final Map<String, AtomicLong> requestCounters = new ConcurrentHashMap<>();
    private final Map<String, Long> lastRequestTimes = new ConcurrentHashMap<>();
    private final Map<String, Integer> failedAttempts = new ConcurrentHashMap<>();
    private final Map<String, Long> blockedUntil = new ConcurrentHashMap<>();

    // A2A-specific permissions
    private static final String PERMISSION_A2A_CONNECT = "a2a:connect";
    private static final String PERMISSION_A2A_EXECUTE = "a2a:execute";
    private static final String PERMISSION_A2A_READ = "a2a:read";
    private static final String PERMISSION_A2A_WRITE = "a2a:write";
    private static final String PERMISSION_A2A_ADMIN = "a2a:admin";

    /**
     * Create a new A2A security manager.
     * 
     * @param config A2A server configuration
     */
    @Activate
    public A2ASecurityManager() {
        this.config = new A2AServerConfiguration(); // Default configuration for now
        logger.info("A2A Security Manager initialized");
    }

    /**
     * Cleanup on deactivation.
     */
    @Deactivate
    public void deactivate() {
        logger.info("A2A Security Manager deactivated");
    }

    /**
     * Authenticate an A2A message using the ai.common authentication manager.
     *
     * @param message A2A message to authenticate
     * @return Authentication context if successful
     */
    public Optional<AIAuthenticationContext> authenticateA2AMessage(Message message) {
        if (message == null) {
            logger.warn("Cannot authenticate null A2A message");
            return Optional.empty();
        }

        String clientId = extractClientIdFromMessage(message);
        Map<String, String> credentials = extractCredentialsFromMessage(message);

        if (!config.isEnableAuthentication()) {
            logger.debug("Authentication disabled, allowing client: {}", clientId);
            return Optional.empty();
        }

        // Check if client is blocked
        if (isClientBlocked(clientId)) {
            logger.warn("Blocked client attempted authentication: {}", clientId);
            auditLogger.logSecurityViolation(clientId, "BLOCKED_CLIENT", "Client is blocked", "a2a", Instant.now());
            return Optional.empty();
        }

        try {
            // Use ai.common authentication manager for authentication
            Optional<AIAuthenticationContext> context = authManager.authenticate(credentials, "a2a", clientId);

            if (context.isPresent()) {
                logger.info("A2A client authenticated successfully for client: {}", clientId);
                resetFailedAttempts(clientId);

                // Log successful authentication
                auditLogger.logAuthenticationSuccess(clientId, "a2a", context.get().getPrincipalId(), Instant.now());

                return context;
            }

            handleFailedAuthentication(clientId);
            return Optional.empty();

        } catch (Exception e) {
            logger.error("A2A authentication error for client: {}", clientId, e);
            handleFailedAuthentication(clientId);
            return Optional.empty();
        }
    }

    /**
     * Check if client has permission for a specific A2A operation.
     * 
     * @param context Authentication context
     * @param permission Permission to check
     * @return true if permission is granted
     */
    public boolean hasA2APermission(AIAuthenticationContext context, String permission) {
        if (context == null) {
            return !config.isEnableAuthentication();
        }

        String principalId = context.getPrincipalId();

        // Use ai.common authentication manager for permission checking
        boolean hasPermission = authManager.hasPermission(principalId, permission, "a2a");

        // Log permission check
        auditLogger.logPermissionCheck(principalId, permission, "a2a", hasPermission, Instant.now());

        if (!hasPermission) {
            logger.warn("A2A permission denied: {} for principal: {}", permission, principalId);
        }

        return hasPermission;
    }

    /**
     * Check if client has A2A-specific permission.
     * 
     * @param context Authentication context
     * @param a2aPermission A2A permission to check (connect, execute, read, write, admin)
     * @return true if permission is granted
     */
    public boolean hasA2APermission(AIAuthenticationContext context, A2APermission a2aPermission) {
        String fullPermission = "a2a:" + a2aPermission.getPermission();
        return hasA2APermission(context, fullPermission);
    }

    /**
     * Validate and rate limit an A2A request.
     * 
     * @param clientId Client identifier
     * @param requestType Type of request
     * @return true if request is allowed
     */
    public boolean validateA2ARequest(String clientId, String requestType) {
        // Check rate limiting
        if (!checkRateLimit(clientId)) {
            logger.warn("A2A rate limit exceeded for client: {}", clientId);
            auditLogger.logSecurityViolation(clientId, "RATE_LIMIT", "Rate limit exceeded", "a2a", Instant.now());
            return false;
        }

        // Check request validation if enabled
        if (config.isEnableRequestValidation()) {
            if (!validateRequestFormat(requestType)) {
                logger.warn("Invalid A2A request format from client: {}", clientId);
                auditLogger.logSecurityViolation(clientId, "INVALID_REQUEST", "Invalid request format", "a2a",
                        Instant.now());
                return false;
            }
        }

        return true;
    }

    /**
     * Check if a client is blocked due to failed attempts.
     * 
     * @param clientId Client identifier
     * @return true if client is blocked
     */
    public boolean isClientBlocked(String clientId) {
        Long blockedUntilTime = blockedUntil.get(clientId);
        if (blockedUntilTime != null && System.currentTimeMillis() < blockedUntilTime) {
            return true;
        }

        // Clear expired block
        if (blockedUntilTime != null && System.currentTimeMillis() >= blockedUntilTime) {
            blockedUntil.remove(clientId);
            failedAttempts.remove(clientId);
        }

        return false;
    }

    /**
     * Get security statistics.
     * 
     * @return Security statistics
     */
    public SecurityStatistics getSecurityStatistics() {
        return new SecurityStatistics(requestCounters.size(), failedAttempts.size(), blockedUntil.size(),
                config.isEnableAuthentication(), config.isEnableRequestValidation(), config.getMaxConnections(),
                config.getRateLimitPerMinute(), authManager.getActiveSessions().size());
    }

    /**
     * Extract client ID from A2A message.
     * 
     * @param message A2A message
     * @return Client ID
     */
    private String extractClientIdFromMessage(Message message) {
        Map<String, Object> metadata = message.getMetadata();
        if (metadata != null && metadata.containsKey("senderId")) {
            return metadata.get("senderId").toString();
        }
        return "a2a-client-" + System.currentTimeMillis();
    }

    /**
     * Extract credentials from A2A message.
     * 
     * @param message A2A message
     * @return Extracted credentials
     */
    private Map<String, String> extractCredentialsFromMessage(Message message) {
        Map<String, Object> metadata = message.getMetadata();
        if (metadata == null) {
            return Map.of();
        }

        Map<String, String> credentials = new java.util.HashMap<>();

        // Extract authentication-related information from metadata
        if (metadata.containsKey("apiKey")) {
            credentials.put("apiKey", metadata.get("apiKey").toString());
        }

        if (metadata.containsKey("token")) {
            credentials.put("token", metadata.get("token").toString());
        }

        if (metadata.containsKey("authToken")) {
            credentials.put("authToken", metadata.get("authToken").toString());
        }

        if (metadata.containsKey("jwt")) {
            credentials.put("jwt_token", metadata.get("jwt").toString());
        }

        if (metadata.containsKey("username") && metadata.containsKey("password")) {
            credentials.put("username", metadata.get("username").toString());
            credentials.put("password", metadata.get("password").toString());
        }

        return credentials;
    }

    /**
     * Handle failed authentication attempt.
     * 
     * @param clientId Client identifier
     */
    private void handleFailedAuthentication(String clientId) {
        int attempts = failedAttempts.compute(clientId, (k, v) -> v == null ? 1 : v + 1);

        // Log failed authentication attempt
        auditLogger.logAuthenticationFailure(clientId, "a2a", "Invalid credentials", Instant.now());

        if (attempts >= 5) { // Block after 5 failed attempts
            long blockUntil = System.currentTimeMillis() + (15 * 60 * 1000); // 15 minutes
            blockedUntil.put(clientId, blockUntil);
            logger.warn("A2A client blocked due to failed attempts: {} (blocked until: {})", clientId,
                    Instant.ofEpochMilli(blockUntil));

            // Log security violation
            auditLogger.logSecurityViolation(clientId, "ACCOUNT_LOCKOUT", "Account locked due to failed attempts",
                    "a2a", Instant.now());
        }
    }

    /**
     * Reset failed attempts for a client.
     * 
     * @param clientId Client identifier
     */
    private void resetFailedAttempts(String clientId) {
        failedAttempts.remove(clientId);
        blockedUntil.remove(clientId);
    }

    /**
     * Check rate limiting for a client.
     * 
     * @param clientId Client identifier
     * @return true if within rate limit
     */
    private boolean checkRateLimit(String clientId) {
        long currentTime = System.currentTimeMillis();
        long windowStart = currentTime - (60 * 1000); // 1 minute window

        AtomicLong counter = requestCounters.computeIfAbsent(clientId, k -> new AtomicLong(0));
        Long lastRequest = lastRequestTimes.get(clientId);

        // Reset counter if window has passed
        if (lastRequest == null || lastRequest < windowStart) {
            counter.set(0);
        }

        // Check if within rate limit
        if (counter.get() >= config.getRateLimitPerMinute()) {
            return false;
        }

        // Increment counter
        counter.incrementAndGet();
        lastRequestTimes.put(clientId, currentTime);

        return true;
    }

    /**
     * Validate request format.
     * 
     * @param requestType Request type
     * @return true if request format is valid
     */
    private boolean validateRequestFormat(String requestType) {
        // Basic validation - can be extended based on specific A2A requirements
        return requestType != null && !requestType.trim().isEmpty();
    }

    /**
     * Get the underlying ai.common authentication manager.
     * 
     * @return Authentication manager
     */
    public AIAuthenticationManager getAuthenticationManager() {
        return authManager;
    }

    /**
     * Get the underlying ai.common role-based access control.
     * 
     * @return RBAC system
     */
    public AIRoleBasedAccessControl getRoleBasedAccessControl() {
        return rbac;
    }

    /**
     * Get the underlying ai.common audit logger.
     * 
     * @return Audit logger
     */
    public AIAuditLogger getAuditLogger() {
        return auditLogger;
    }

    /**
     * A2A-specific permissions.
     */
    public enum A2APermission {
        CONNECT("connect"),
        EXECUTE("execute"),
        READ("read"),
        WRITE("write"),
        ADMIN("admin");

        private final String permission;

        A2APermission(String permission) {
            this.permission = permission;
        }

        public String getPermission() {
            return permission;
        }
    }

    /**
     * Security statistics.
     */
    public static class SecurityStatistics {
        private final int activeClients;
        private final int failedAttempts;
        private final int blockedClients;
        private final boolean authenticationEnabled;
        private final boolean requestValidationEnabled;
        private final int maxConnections;
        private final int rateLimitPerMinute;
        private final int activeSessions;

        public SecurityStatistics(int activeClients, int failedAttempts, int blockedClients,
                boolean authenticationEnabled, boolean requestValidationEnabled, int maxConnections,
                int rateLimitPerMinute, int activeSessions) {
            this.activeClients = activeClients;
            this.failedAttempts = failedAttempts;
            this.blockedClients = blockedClients;
            this.authenticationEnabled = authenticationEnabled;
            this.requestValidationEnabled = requestValidationEnabled;
            this.maxConnections = maxConnections;
            this.rateLimitPerMinute = rateLimitPerMinute;
            this.activeSessions = activeSessions;
        }

        public int getActiveClients() {
            return activeClients;
        }

        public int getFailedAttempts() {
            return failedAttempts;
        }

        public int getBlockedClients() {
            return blockedClients;
        }

        public boolean isAuthenticationEnabled() {
            return authenticationEnabled;
        }

        public boolean isRequestValidationEnabled() {
            return requestValidationEnabled;
        }

        public int getMaxConnections() {
            return maxConnections;
        }

        public int getRateLimitPerMinute() {
            return rateLimitPerMinute;
        }

        public int getActiveSessions() {
            return activeSessions;
        }
    }

    /**
     * Simple A2A server configuration for security settings.
     */
    private static class A2AServerConfiguration {
        public boolean isEnableAuthentication() {
            return true;
        }

        public boolean isEnableRequestValidation() {
            return true;
        }

        public int getMaxConnections() {
            return 100;
        }

        public int getRateLimitPerMinute() {
            return 1000;
        }
    }
}
