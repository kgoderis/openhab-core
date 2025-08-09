package org.openhab.core.ai.agent.lifecycle;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.auth.AuditLogger;
import org.openhab.core.ai.auth.AuthenticationContext;
import org.openhab.core.ai.auth.AuthenticationManager;
import org.openhab.core.ai.auth.RoleBasedAccessControl;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.a2a.spec.Message;

/**
 * Security manager for A2A operations.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
@Component(service = AgentSecurityManager.class)
public class AgentSecurityManager {

    private static final Logger logger = LoggerFactory.getLogger(AgentSecurityManager.class);

    // Core ai.common components
    @Reference
    private @Nullable AuthenticationManager authManager;

    @Reference
    private @Nullable RoleBasedAccessControl rbac;

    @Reference
    private @Nullable AuditLogger auditLogger;

    // A2A-specific configuration
    private final ServerConfiguration config;

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
    public AgentSecurityManager() {
        this.config = new ServerConfiguration(); // Default configuration for now
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
    public Optional<AuthenticationContext> authenticateA2AMessage(Message message) {
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
            return Optional.empty();
        }

        // Check rate limiting
        if (!checkRateLimit(clientId)) {
            logger.warn("Rate limit exceeded for client: {}", clientId);
            return Optional.empty();
        }

        // Authenticate using ai.common authentication manager
        if (authManager == null) {
            logger.error("Authentication manager not available");
            return Optional.empty();
        }

        try {
            Optional<AuthenticationContext> authContext = authManager.authenticate(credentials, "a2a", clientId);
            if (authContext.isPresent()) {
                resetFailedAttempts(clientId);
                logger.debug("Successfully authenticated client: {}", clientId);
                return authContext;
            } else {
                handleFailedAuthentication(clientId);
                logger.warn("Authentication failed for client: {}", clientId);
                return Optional.empty();
            }
        } catch (Exception e) {
            handleFailedAuthentication(clientId);
            logger.error("Authentication error for client: {}", clientId, e);
            return Optional.empty();
        }
    }

    /**
     * Check if the authenticated context has the specified A2A permission.
     *
     * @param context Authentication context
     * @param permission Permission to check
     * @return true if permission is granted
     */
    public boolean hasA2APermission(AuthenticationContext context, String permission) {
        if (context == null) {
            logger.warn("Cannot check permission for null authentication context");
            return false;
        }

        if (rbac == null) {
            logger.error("Role-based access control not available");
            return false;
        }

        try {
            return rbac.hasPermission(context.getPrincipalId(), permission, "a2a");
        } catch (Exception e) {
            logger.error("Error checking permission: {}", permission, e);
            return false;
        }
    }

    /**
     * Check if the authenticated context has the specified A2A permission.
     *
     * @param context Authentication context
     * @param a2aPermission A2A permission to check
     * @return true if permission is granted
     */
    public boolean hasA2APermission(AuthenticationContext context, A2APermission a2aPermission) {
        return hasA2APermission(context, "a2a:" + a2aPermission.getPermission());
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
            return false;
        }

        // Check request validation if enabled
        if (config.isEnableRequestValidation()) {
            if (!validateRequestFormat(requestType)) {
                logger.warn("Invalid A2A request format from client: {}", clientId);
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
                config.getRateLimitPerMinute(), authManager == null ? 0 : authManager.getActiveSessions().size());
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
        if (auditLogger != null) {
            auditLogger.logAuthenticationFailure(clientId, "a2a", "Invalid credentials", Instant.now());
        }

        if (attempts >= 5) { // Block after 5 failed attempts
            long blockUntil = System.currentTimeMillis() + (15 * 60 * 1000); // 15 minutes
            blockedUntil.put(clientId, blockUntil);
            logger.warn("A2A client blocked due to failed attempts: {} (blocked until: {})", clientId,
                    Instant.ofEpochMilli(blockUntil));

            // Log security violation
            if (auditLogger != null) {
                auditLogger.logSecurityViolation(clientId, "ACCOUNT_LOCKOUT", "Account locked due to failed attempts",
                        "a2a", Instant.now());
            }
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
    public @Nullable AuthenticationManager getAuthenticationManager() {
        return authManager;
    }

    /**
     * Get the underlying ai.common role-based access control.
     * 
     * @return RBAC system
     */
    public @Nullable RoleBasedAccessControl getRoleBasedAccessControl() {
        return rbac;
    }

    /**
     * Get the underlying ai.common audit logger.
     * 
     * @return Audit logger
     */
    public @Nullable AuditLogger getAuditLogger() {
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
    private static class ServerConfiguration {
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
