package org.openhab.core.ai.mcp.internal;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.auth.AIAuditLogger;
import org.openhab.core.ai.common.auth.AIAuthenticationContext;
import org.openhab.core.ai.common.auth.AIAuthenticationManager;
import org.openhab.core.ai.common.auth.AIRoleBasedAccessControl;
import org.openhab.core.ai.common.auth.APIKeyAuthenticationProvider;
import org.openhab.core.ai.common.auth.OAuth21AuthenticationProvider;
import org.openhab.core.ai.common.auth.OpenHABUsersAuthenticationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Security manager for MCP server integration with ai.common bundle.
 * 
 * This class provides authentication, authorization, rate limiting, and
 * request validation for MCP server operations. It orchestrates the
 * ai.common authentication providers and adds MCP-specific security features.
 * 
 * 
 */
@NonNullByDefault
public class MCPSecurityManager {

    private static final Logger logger = LoggerFactory.getLogger(MCPSecurityManager.class);

    // Core ai.common components
    private final AIAuthenticationManager authManager;
    private final AIRoleBasedAccessControl rbac;
    private final AIAuditLogger auditLogger;
    private final MCPServerConfiguration config;

    // MCP-specific authentication providers (registered with authManager)
    private final OpenHABUsersAuthenticationProvider openhabUsersProvider;
    private final APIKeyAuthenticationProvider apiKeyProvider;
    private final OAuth21AuthenticationProvider oauth21Provider;

    // Rate limiting and security tracking
    private final Map<String, AtomicLong> requestCounters = new ConcurrentHashMap<>();
    private final Map<String, Long> lastRequestTimes = new ConcurrentHashMap<>();
    private final Map<String, Integer> failedAttempts = new ConcurrentHashMap<>();
    private final Map<String, Long> blockedUntil = new ConcurrentHashMap<>();

    // MCP-specific permissions
    // TODO : Check if these permissions are effectively used in the code
    private static final String PERMISSION_MCP_CONNECT = "mcp:connect";
    private static final String PERMISSION_MCP_TOOLS = "mcp:tools";
    private static final String PERMISSION_MCP_READ = "mcp:read";
    private static final String PERMISSION_MCP_WRITE = "mcp:write";
    private static final String PERMISSION_MCP_ADMIN = "mcp:admin";

    /**
     * Create a new MCP security manager.
     * 
     * @param authManager AI authentication manager from ai.common
     * @param rbac Role-based access control from ai.common
     * @param auditLogger Audit logger from ai.common
     * @param config MCP server configuration
     */
    public MCPSecurityManager(AIAuthenticationManager authManager, AIRoleBasedAccessControl rbac,
            AIAuditLogger auditLogger, MCPServerConfiguration config) {
        this.authManager = authManager;
        this.rbac = rbac;
        this.auditLogger = auditLogger;
        this.config = config;

        // Initialize authentication providers
        this.openhabUsersProvider = new OpenHABUsersAuthenticationProvider(config.getOpenhabUsersFile(),
                config.isOpenhabUsersEnabled());

        this.apiKeyProvider = new APIKeyAuthenticationProvider(config.getApiKeyValue(), config.getApiKeyHeader(),
                config.isApiKeyEnabled());

        this.oauth21Provider = new OAuth21AuthenticationProvider(config.getOauthIssuerUrl(), config.getOauthClientId(),
                config.getOauthClientSecret(), config.getOauthRedirectUri(), config.isOauthPkceEnabled(),
                "oauth2.1".equals(config.getPrimaryAuthMethod()) || "oauth2.1".equals(config.getFallbackAuthMethod()));

        // Register providers with the authentication manager
        registerProvidersWithAuthManager();

        logger.info("MCP Security Manager initialized with authentication providers: primary={}, fallback={}",
                config.getPrimaryAuthMethod(), config.getFallbackAuthMethod());
    }

    /**
     * Register authentication providers with the ai.common authentication manager.
     */
    private void registerProvidersWithAuthManager() {
        // Register providers based on configuration
        if (config.isOpenhabUsersEnabled()) {
            authManager.registerProvider(openhabUsersProvider);
            logger.debug("Registered OpenHAB users authentication provider");
        }

        if (config.isApiKeyEnabled()) {
            authManager.registerProvider(apiKeyProvider);
            logger.debug("Registered API key authentication provider");
        }

        if ("oauth2.1".equals(config.getPrimaryAuthMethod()) || "oauth2.1".equals(config.getFallbackAuthMethod())) {
            authManager.registerProvider(oauth21Provider);
            logger.debug("Registered OAuth 2.1 authentication provider");
        }
    }

    /**
     * Authenticate a client with credentials.
     * 
     * @param credentials Authentication credentials
     * @param clientId Client identifier
     * @return Authentication context if successful
     */
    public Optional<AIAuthenticationContext> authenticateClient(Map<String, String> credentials, String clientId) {
        // Validate input parameters
        if (clientId == null) {
            logger.warn("Authentication attempt with null client ID");
            return Optional.empty();
        }

        if (!config.isEnableAuthentication()) {
            logger.debug("Authentication disabled, allowing client: {}", clientId);
            return Optional.empty();
        }

        // Check rate limiting before authentication
        if (!checkRateLimit(clientId)) {
            logger.warn("Rate limit exceeded for authentication attempt by client: {}", clientId);
            auditLogger.logSecurityViolation(clientId, "RATE_LIMIT", "Rate limit exceeded during authentication", "mcp",
                    Instant.now());
            return Optional.empty();
        }

        // Check if client is blocked
        if (isClientBlocked(clientId)) {
            logger.warn("Blocked client attempted authentication: {}", clientId);
            auditLogger.logSecurityViolation(clientId, "BLOCKED_CLIENT", "Client is blocked", "mcp", Instant.now());
            return Optional.empty();
        }

        try {
            // Try primary authentication method
            Optional<AIAuthenticationContext> context = authenticateWithPrimaryMethod(credentials, clientId);
            if (context.isPresent()) {
                logger.info("Client authenticated successfully with primary method: {}", clientId);
                resetFailedAttempts(clientId);
                return context;
            }

            // Try fallback authentication method
            context = authenticateWithFallbackMethod(credentials, clientId);
            if (context.isPresent()) {
                logger.info("Client authenticated successfully with fallback method: {}", clientId);
                resetFailedAttempts(clientId);
                return context;
            }

            // Authentication failed
            handleFailedAuthentication(clientId);
            return Optional.empty();

        } catch (Exception e) {
            logger.error("Authentication error for client: {}", clientId, e);
            handleFailedAuthentication(clientId);
            return Optional.empty();
        }
    }

    /**
     * Authenticate using primary authentication method.
     *
     * @param credentials Authentication credentials
     * @param clientId Client identifier
     * @return Authentication context if successful
     */
    private Optional<AIAuthenticationContext> authenticateWithPrimaryMethod(Map<String, String> credentials,
            String clientId) {
        String primaryMethod = config.getPrimaryAuthMethod();

        switch (primaryMethod) {
            case "oauth2.1":
                return oauth21Provider.authenticate(credentials);
            case "openhab_users":
                return openhabUsersProvider.authenticate(credentials);
            case "api_key":
                return apiKeyProvider.authenticate(credentials);
            case "jwt":
                // Use existing JWT authentication from ai.common
                String jwtToken = credentials.get("jwt_token");
                if (jwtToken != null) {
                    return authManager.authenticateWithJWT(jwtToken, "mcp");
                }
                break;
            default:
                logger.warn("Unknown primary authentication method: {}", primaryMethod);
        }

        return Optional.empty();
    }

    /**
     * Authenticate using fallback authentication method.
     *
     * @param credentials Authentication credentials
     * @param clientId Client identifier
     * @return Authentication context if successful
     */
    private Optional<AIAuthenticationContext> authenticateWithFallbackMethod(Map<String, String> credentials,
            String clientId) {
        String fallbackMethod = config.getFallbackAuthMethod();

        switch (fallbackMethod) {
            case "oauth2.1":
                return oauth21Provider.authenticate(credentials);
            case "openhab_users":
                return openhabUsersProvider.authenticate(credentials);
            case "api_key":
                return apiKeyProvider.authenticate(credentials);
            case "jwt":
                // Use existing JWT authentication from ai.common
                String jwtToken = credentials.get("jwt_token");
                if (jwtToken != null) {
                    return authManager.authenticateWithJWT(jwtToken, "mcp");
                }
                break;
            default:
                logger.warn("Unknown fallback authentication method: {}", fallbackMethod);
        }

        return Optional.empty();
    }

    /**
     * Authenticate using JWT token.
     * 
     * @param jwtToken JWT token
     * @param clientId Client identifier
     * @return Authentication context if successful
     */
    public Optional<AIAuthenticationContext> authenticateWithJWT(String jwtToken, String clientId) {
        // Validate input parameters
        if (clientId == null) {
            logger.warn("JWT authentication attempt with null client ID");
            return Optional.empty();
        }

        if (!config.isEnableAuthentication()) {
            logger.debug("Authentication disabled, allowing client: {}", clientId);
            return Optional.empty();
        }

        // Check rate limiting before JWT authentication
        if (!checkRateLimit(clientId)) {
            logger.warn("Rate limit exceeded for JWT authentication attempt by client: {}", clientId);
            auditLogger.logSecurityViolation(clientId, "RATE_LIMIT", "Rate limit exceeded during JWT authentication",
                    "mcp", Instant.now());
            return Optional.empty();
        }

        // Check if client is blocked
        if (isClientBlocked(clientId)) {
            logger.warn("Blocked client attempted JWT authentication: {}", clientId);
            auditLogger.logSecurityViolation(clientId, "BLOCKED_CLIENT", "Client is blocked", "mcp", Instant.now());
            return Optional.empty();
        }

        try {
            Optional<AIAuthenticationContext> context = authManager.authenticateWithJWT(jwtToken, "mcp");
            if (context.isPresent()) {
                logger.info("Client authenticated with JWT successfully: {}", clientId);
                resetFailedAttempts(clientId);

                // Log successful JWT authentication
                auditLogger.logAuthenticationSuccess(clientId, "mcp", context.get().getPrincipalId(), Instant.now());

                return context;
            } else {
                handleFailedAuthentication(clientId);
                return Optional.empty();
            }
        } catch (Exception e) {
            logger.error("JWT authentication error for client: {}", clientId, e);
            handleFailedAuthentication(clientId);
            return Optional.empty();
        }
    }

    /**
     * Validate and rate limit a request.
     * 
     * @param clientId Client identifier
     * @param requestType Type of request
     * @return true if request is allowed
     */
    public boolean validateRequest(String clientId, String requestType) {
        // Validate input parameters
        if (clientId == null) {
            logger.warn("Request validation attempt with null client ID");
            return false;
        }

        // Check rate limiting
        if (!checkRateLimit(clientId)) {
            logger.warn("Rate limit exceeded for client: {}", clientId);
            auditLogger.logSecurityViolation(clientId, "RATE_LIMIT", "Rate limit exceeded", "mcp", Instant.now());
            return false;
        }

        // Check request validation if enabled
        if (config.isEnableRequestValidation()) {
            if (!validateRequestFormat(requestType)) {
                logger.warn("Invalid request format from client: {}", clientId);
                auditLogger.logSecurityViolation(clientId, "INVALID_REQUEST", "Invalid request format", "mcp",
                        Instant.now());
                return false;
            }
        }

        return true;
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
        if (counter != null && counter.get() >= config.getRateLimitPerMinute()) {
            return false;
        }

        // Increment counter
        if (counter != null) {
            counter.incrementAndGet();
        }
        lastRequestTimes.put(clientId, currentTime);

        return true;
    }

    /**
     * Check if client has permission for a specific operation.
     * 
     * @param context Authentication context
     * @param permission Permission to check
     * @return true if permission is granted
     */
    public boolean hasPermission(AIAuthenticationContext context, String permission) {
        if (context == null) {
            return !config.isEnableAuthentication();
        }

        String principalId = context.getPrincipalId();

        // Use ai.common authentication manager for permission checking
        boolean hasPermission = authManager.hasPermission(principalId, permission, "mcp");

        // Log permission check
        auditLogger.logPermissionCheck(principalId, permission, "mcp", hasPermission, Instant.now());

        if (!hasPermission) {
            logger.warn("Permission denied: {} for principal: {}", permission, principalId);
        }

        return hasPermission;
    }

    /**
     * Check if client has MCP-specific permission.
     * 
     * @param context Authentication context
     * @param mcpPermission MCP permission to check (connect, tools, read, write, admin)
     * @return true if permission is granted
     */
    public boolean hasMCPPermission(AIAuthenticationContext context, String mcpPermission) {
        String fullPermission = "mcp:" + mcpPermission;
        return hasPermission(context, fullPermission);
    }

    /**
     * Validate request format.
     * 
     * @param requestType Request type
     * @return true if request format is valid
     */
    private boolean validateRequestFormat(String requestType) {
        // Basic validation - can be extended based on specific requirements
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
     * Check if a client is blocked due to failed attempts.
     * 
     * @param clientId Client identifier
     * @return true if client is blocked
     */
    public boolean isClientBlocked(String clientId) {
        // Validate input parameter
        if (clientId == null) {
            return false; // Cannot determine if null client is blocked
        }

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
     * Reset failed attempts for a client.
     * 
     * @param clientId Client identifier
     */
    private void resetFailedAttempts(String clientId) {
        failedAttempts.remove(clientId);
        blockedUntil.remove(clientId);
    }

    /**
     * Handle failed authentication attempt.
     * 
     * @param clientId Client identifier
     */
    private void handleFailedAuthentication(String clientId) {
        int attempts = failedAttempts.compute(clientId, (k, v) -> v == null ? 1 : v + 1);

        // Log failed authentication attempt
        auditLogger.logAuthenticationFailure(clientId, "mcp", "Invalid credentials", Instant.now());

        if (attempts >= 5) { // Block after 5 failed attempts
            long blockUntil = System.currentTimeMillis() + (15 * 60 * 1000); // 15 minutes
            blockedUntil.put(clientId, blockUntil);
            logger.warn("Client blocked due to failed attempts: {} (blocked until: {})", clientId,
                    Instant.ofEpochMilli(blockUntil));

            // Log security violation
            auditLogger.logSecurityViolation(clientId, "ACCOUNT_LOCKOUT", "Account locked due to failed attempts",
                    "mcp", Instant.now());
        }
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

        @Override
        public String toString() {
            return String.format("SecurityStatistics{activeClients=%d, failedAttempts=%d, blockedClients=%d, "
                    + "authEnabled=%s, validationEnabled=%s, maxConnections=%d, rateLimit=%d, activeSessions=%d}",
                    activeClients, failedAttempts, blockedClients, authenticationEnabled, requestValidationEnabled,
                    maxConnections, rateLimitPerMinute, activeSessions);
        }
    }
}
