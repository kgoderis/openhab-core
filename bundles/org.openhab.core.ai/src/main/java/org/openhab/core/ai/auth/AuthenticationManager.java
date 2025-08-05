package org.openhab.core.ai.auth;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Central authentication manager for AI services.
 * 
 * This manager coordinates authentication across different AI protocols
 * and provides a unified interface for authentication operations.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class AuthenticationManager {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationManager.class);

    private final Map<String, AuthenticationProvider> providers = new ConcurrentHashMap<>();
    private final Map<String, AuthenticationContext> activeContexts = new ConcurrentHashMap<>();
    private final List<AuthenticationListener> listeners = new CopyOnWriteArrayList<>();

    // TODO : Should JWT autentication just be a provider like all the other providers?
    private JWTManager jwtManager;
    private RoleBasedAccessControl rbac;
    private AuditLogger auditLogger;

    /**
     * Create a new authentication manager.
     * 
     * @param jwtManager JWT token manager
     * @param rbac Role-based access control system
     * @param auditLogger Audit logging service
     */
    public AuthenticationManager(JWTManager jwtManager, RoleBasedAccessControl rbac, AuditLogger auditLogger) {
        this.jwtManager = jwtManager;
        this.rbac = rbac;
        this.auditLogger = auditLogger;
        logger.info("AI Authentication Manager initialized");
    }

    /**
     * Register an authentication provider.
     * 
     * @param provider Authentication provider to register
     */
    public void registerProvider(AuthenticationProvider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("Provider cannot be null");
        }

        String providerName = provider.getProviderName();
        providers.put(providerName, provider);
        logger.info("Registered authentication provider: {}", providerName);
    }

    /**
     * Unregister an authentication provider.
     * 
     * @param providerName Name of the provider to remove
     * @return true if provider was removed, false if not found
     */
    public boolean unregisterProvider(String providerName) {
        AuthenticationProvider removed = providers.remove(providerName);
        if (removed != null) {
            logger.info("Unregistered authentication provider: {}", providerName);
            return true;
        }
        return false;
    }

    /**
     * Authenticate using credentials and return authentication context.
     * 
     * @param credentials Authentication credentials
     * @param protocol Protocol name (mcp, a2a)
     * @param clientId Client identifier
     * @return Authentication context if successful
     */
    public Optional<AuthenticationContext> authenticate(Map<String, String> credentials, String protocol,
            String clientId) {
        logger.debug("Attempting authentication for protocol: {}, client: {}", protocol, clientId);

        auditLogger.logAuthenticationAttempt(clientId, protocol, Instant.now());

        // Try each provider until one succeeds
        for (AuthenticationProvider provider : providers.values()) {
            if (!provider.isEnabled()) {
                continue;
            }

            try {
                Optional<AuthenticationContext> context = provider.authenticate(credentials);
                if (context.isPresent()) {
                    AuthenticationContext authContext = context.get();

                    // Enhance context with RBAC permissions
                    Set<String> permissions = rbac.getPermissions(authContext.getPrincipalId(), protocol);
                    AuthenticationContext enhancedContext = createEnhancedContext(authContext, permissions);

                    // Store active context
                    activeContexts.put(enhancedContext.getSessionId(), enhancedContext);

                    // Generate JWT token
                    jwtManager.generateToken(enhancedContext);

                    // Notify listeners
                    notifyAuthenticationSuccess(enhancedContext, protocol, clientId);

                    auditLogger.logAuthenticationSuccess(clientId, authContext.getPrincipalId(), protocol,
                            Instant.now());
                    logger.info("Authentication successful for principal: {} using provider: {}",
                            authContext.getPrincipalId(), provider.getProviderName());

                    return Optional.of(enhancedContext);
                }
            } catch (Exception e) {
                String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error";
                logger.warn("Authentication failed with provider {}: {}", provider.getProviderName(), errorMessage);
                auditLogger.logAuthenticationFailure(clientId, provider.getProviderName(), errorMessage, Instant.now());
            }
        }

        auditLogger.logAuthenticationFailure(clientId, "ALL_PROVIDERS", "No provider succeeded", Instant.now());
        notifyAuthenticationFailure(clientId, protocol, "Authentication failed");
        return Optional.empty();
    }

    /**
     * Authenticate using JWT token.
     * 
     * @param jwtToken JWT token
     * @param protocol Protocol name
     * @return Authentication context if token is valid
     */
    public Optional<AuthenticationContext> authenticateWithJWT(String jwtToken, String protocol) {
        logger.debug("Attempting JWT authentication for protocol: {}", protocol);

        try {
            Optional<AuthenticationContext> context = jwtManager.validateToken(jwtToken);
            if (context.isPresent()) {
                AuthenticationContext authContext = context.get();

                // Check if context is still active and valid
                if (activeContexts.containsKey(authContext.getSessionId()) && authContext.isValid()) {
                    auditLogger.logJWTAuthenticationSuccess(authContext.getPrincipalId(), protocol, Instant.now());
                    return context;
                }
            }
        } catch (Exception e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error";
            logger.warn("JWT authentication failed: {}", errorMessage);
            auditLogger.logJWTAuthenticationFailure(jwtToken.substring(0, Math.min(10, jwtToken.length())),
                    errorMessage, Instant.now());
        }

        return Optional.empty();
    }

    /**
     * Validate an existing authentication context.
     * 
     * @param context Authentication context to validate
     * @return true if context is valid
     */
    public boolean validateContext(AuthenticationContext context) {
        if (context == null || !context.isValid()) {
            return false;
        }

        // Check if context is still active
        if (!activeContexts.containsKey(context.getSessionId())) {
            return false;
        }

        // Try to validate with original provider
        for (AuthenticationProvider provider : providers.values()) {
            if (provider.getProviderName().equals(context.getAuthenticationScheme())) {
                return provider.validateContext(context);
            }
        }

        return false;
    }

    /**
     * Refresh authentication tokens for a context.
     * 
     * @param context Current authentication context
     * @return New context with refreshed tokens
     */
    public Optional<AuthenticationContext> refreshAuthentication(AuthenticationContext context) {
        if (context == null) {
            return Optional.empty();
        }

        // Find the original provider
        for (AuthenticationProvider provider : providers.values()) {
            if (provider.getProviderName().equals(context.getAuthenticationScheme())) {
                Optional<AuthenticationContext> refreshedContext = provider.refreshAuthentication(context);
                if (refreshedContext.isPresent()) {
                    // Update active contexts
                    activeContexts.remove(context.getSessionId());
                    activeContexts.put(refreshedContext.get().getSessionId(), refreshedContext.get());

                    auditLogger.logTokenRefresh(context.getPrincipalId(), Instant.now());
                    return refreshedContext;
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Logout and invalidate authentication context.
     * 
     * @param sessionId Session ID to logout
     * @return true if logout was successful
     */
    public boolean logout(String sessionId) {
        AuthenticationContext context = activeContexts.remove(sessionId);
        if (context != null) {
            auditLogger.logLogout(context.getPrincipalId(), Instant.now());
            notifyLogout(context);
            logger.info("Logged out session: {} for principal: {}", sessionId, context.getPrincipalId());
            return true;
        }
        return false;
    }

    /**
     * Get active authentication context by session ID.
     * 
     * @param sessionId Session identifier
     * @return Authentication context if found and valid
     */
    public Optional<AuthenticationContext> getActiveContext(String sessionId) {
        AuthenticationContext context = activeContexts.get(sessionId);
        if (context != null && context.isValid()) {
            return Optional.of(context);
        } else if (context != null) {
            // Remove expired context
            activeContexts.remove(sessionId);
        }
        return Optional.empty();
    }

    /**
     * Check if a principal has a specific permission for a protocol.
     * 
     * @param principalId Principal identifier
     * @param permission Permission to check
     * @param protocol Protocol name
     * @return true if permission is granted
     */
    public boolean hasPermission(String principalId, String permission, String protocol) {
        return rbac.hasPermission(principalId, permission, protocol);
    }

    /**
     * Add authentication listener.
     * 
     * @param listener Listener to add
     */
    public void addAuthenticationListener(AuthenticationListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    /**
     * Remove authentication listener.
     * 
     * @param listener Listener to remove
     */
    public void removeAuthenticationListener(AuthenticationListener listener) {
        listeners.remove(listener);
    }

    /**
     * Get all active session IDs.
     * 
     * @return Set of active session IDs
     */
    public Set<String> getActiveSessions() {
        return Set.copyOf(activeContexts.keySet());
    }

    /**
     * Cleanup expired sessions.
     * 
     * @return Number of sessions cleaned up
     */
    public int cleanupExpiredSessions() {
        int cleaned = 0;
        for (Map.Entry<String, AuthenticationContext> entry : activeContexts.entrySet()) {
            if (!entry.getValue().isValid()) {
                activeContexts.remove(entry.getKey());
                cleaned++;
            }
        }

        if (cleaned > 0) {
            logger.info("Cleaned up {} expired sessions", cleaned);
        }

        return cleaned;
    }

    private AuthenticationContext createEnhancedContext(AuthenticationContext original, Set<String> permissions) {
        // Create new context with enhanced permissions
        return new AuthenticationContext(original.getPrincipalId(), original.getAuthenticationScheme(),
                original.getCredentials(), permissions, original.getIssuedAt(), original.getExpiresAt().orElse(null),
                original.getSessionId());
    }

    private void notifyAuthenticationSuccess(AuthenticationContext context, String protocol, String clientId) {
        for (AuthenticationListener listener : listeners) {
            try {
                listener.onAuthenticationSuccess(context, protocol, clientId);
            } catch (Exception e) {
                logger.warn("Authentication listener failed: {}", e.getMessage());
            }
        }
    }

    private void notifyAuthenticationFailure(String clientId, String protocol, String reason) {
        for (AuthenticationListener listener : listeners) {
            try {
                listener.onAuthenticationFailure(clientId, protocol, reason);
            } catch (Exception e) {
                logger.warn("Authentication listener failed: {}", e.getMessage());
            }
        }
    }

    private void notifyLogout(AuthenticationContext context) {
        for (AuthenticationListener listener : listeners) {
            try {
                listener.onLogout(context);
            } catch (Exception e) {
                logger.warn("Authentication listener failed: {}", e.getMessage());
            }
        }
    }
}
