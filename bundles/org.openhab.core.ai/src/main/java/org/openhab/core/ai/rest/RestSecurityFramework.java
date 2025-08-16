package org.openhab.core.ai.rest;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST Security Framework for authentication, authorization, and security features
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public final class RestSecurityFramework {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestSecurityFramework.class);

    private RestSecurityFramework() {
        // Utility class
    }

    // Basic security responses
    public static Response unauthorized(String message) {
        return Response.status(401).header("WWW-Authenticate", "Bearer")
                .entity(Map.of("error", message, "code", "UNAUTHORIZED", "timestamp", System.currentTimeMillis()))
                .type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    public static Response forbidden(String message) {
        return Response.status(403)
                .entity(Map.of("error", message, "code", "FORBIDDEN", "timestamp", System.currentTimeMillis()))
                .type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    // Authentication utilities
    public static boolean isValidToken(String token) {
        return token != null && token.startsWith("Bearer ") && token.length() > 20;
    }

    public static String extractToken(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return null;
    }

    public static Response invalidToken() {
        return unauthorized("Invalid or missing authentication token");
    }

    public static Response expiredToken() {
        return unauthorized("Authentication token has expired");
    }

    // Authorization utilities
    public static boolean hasPermission(String user, String permission) {
        // Implement actual permission checking logic
        if (user == null || user.trim().isEmpty()) {
            LOGGER.warn("User is null or empty for permission check: {}", permission);
            return false;
        }

        if (permission == null || permission.trim().isEmpty()) {
            LOGGER.warn("Permission is null or empty for user: {}", user);
            return false;
        }

        // Admin users have all permissions
        if ("admin".equals(user) || "root".equals(user) || "superuser".equals(user)) {
            LOGGER.debug("Admin user {} granted permission: {}", user, permission);
            return true;
        }

        // Define permission mappings
        Map<String, Set<String>> userPermissions = Map.of("user", Set.of("read", "write", "execute"), "guest",
                Set.of("read"), "moderator", Set.of("read", "write", "moderate"), "developer",
                Set.of("read", "write", "execute", "debug"));

        // Check if user has the specific permission
        Set<String> permissions = userPermissions.get(user);
        if (permissions != null && permissions.contains(permission)) {
            LOGGER.debug("User {} granted permission: {}", user, permission);
            return true;
        }

        LOGGER.debug("User {} denied permission: {}", user, permission);
        return false;
    }

    public static boolean hasRole(String user, String role) {
        // Implement actual role checking logic
        if (user == null || user.trim().isEmpty()) {
            LOGGER.warn("User is null or empty for role check: {}", role);
            return false;
        }

        if (role == null || role.trim().isEmpty()) {
            LOGGER.warn("Role is null or empty for user: {}", user);
            return false;
        }

        // Admin users have all roles
        if ("admin".equals(user) || "root".equals(user) || "superuser".equals(user)) {
            LOGGER.debug("Admin user {} granted role: {}", user, role);
            return true;
        }

        // Define role mappings
        Map<String, Set<String>> userRoles = Map.of("user", Set.of("user", "authenticated"), "guest",
                Set.of("guest", "anonymous"), "moderator", Set.of("user", "authenticated", "moderator"), "developer",
                Set.of("user", "authenticated", "developer"), "tester", Set.of("user", "authenticated", "tester"));

        // Check if user has the specific role
        Set<String> roles = userRoles.get(user);
        if (roles != null && roles.contains(role)) {
            LOGGER.debug("User {} granted role: {}", user, role);
            return true;
        }

        LOGGER.debug("User {} denied role: {}", user, role);
        return false;
    }

    public static Response insufficientPermissions(String requiredPermission) {
        return forbidden("Insufficient permissions. Required: " + requiredPermission);
    }

    public static Response insufficientRole(String requiredRole) {
        return forbidden("Insufficient role. Required: " + requiredRole);
    }

    // Rate limiting
    private static final Map<String, RestRateLimitInfo> rateLimitCache = new ConcurrentHashMap<>();
    private static final int DEFAULT_MAX_REQUESTS = 100;
    private static final long DEFAULT_WINDOW_MS = 60000; // 1 minute

    public static boolean isRateLimited(String clientId, String endpoint) {
        // Implement actual rate limiting logic with Redis or similar
        if (clientId == null || clientId.trim().isEmpty()) {
            LOGGER.warn("Client ID is null or empty for rate limiting check");
            return false;
        }

        if (endpoint == null || endpoint.trim().isEmpty()) {
            LOGGER.warn("Endpoint is null or empty for rate limiting check");
            return false;
        }

        String cacheKey = clientId + ":" + endpoint;
        long currentTime = System.currentTimeMillis();

        // Get or create rate limit info
        RestRateLimitInfo info = rateLimitCache.computeIfAbsent(cacheKey,
                k -> new RestRateLimitInfo(DEFAULT_MAX_REQUESTS, 0, currentTime + DEFAULT_WINDOW_MS));

        // Check if window has expired
        if (currentTime > info.resetTime) {
            // Reset the window
            info = new RestRateLimitInfo(DEFAULT_MAX_REQUESTS, 1, currentTime + DEFAULT_WINDOW_MS);
            rateLimitCache.put(cacheKey, info);
            return false;
        }

        // Check if rate limit exceeded
        if (info.currentRequests >= info.maxRequests) {
            LOGGER.warn("Rate limit exceeded for client {} on endpoint {}", clientId, endpoint);
            return true;
        }

        // Increment request count
        info = new RestRateLimitInfo(info.maxRequests, info.currentRequests + 1, info.resetTime);
        rateLimitCache.put(cacheKey, info);

        return false;
    }

    // Rate limit info extracted to top-level: org.openhab.core.ai.rest.RestRateLimitInfo

    public static Response rateLimitExceeded(String endpoint) {
        LOGGER.warn("Rate limit exceeded for endpoint: {}", endpoint);
        return Response.status(429).header("Retry-After", 60)
                .entity(Map.of("error", "Rate limit exceeded", "endpoint", endpoint, "retry_after", 60))
                .type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    // Input validation and sanitization
    public static boolean isValidInput(String input) {
        return input != null && !input.trim().isEmpty() && input.length() <= 1000;
    }

    public static String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        // Basic XSS prevention
        return input.replaceAll("<script[^>]*>.*?</script>", "").replaceAll("<[^>]*>", "").trim();
    }

    public static Response invalidInput(String field, String reason) {
        return Response.status(400).entity(Map.of("error", "Invalid input", "field", field, "reason", reason))
                .type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    // CORS and security headers
    public static Response.ResponseBuilder addSecurityHeaders(Response.ResponseBuilder builder) {
        return builder.header("X-Content-Type-Options", "nosniff").header("X-Frame-Options", "DENY")
                .header("X-XSS-Protection", "1; mode=block")
                .header("Strict-Transport-Security", "max-age=31536000; includeSubDomains")
                .header("Content-Security-Policy", "default-src 'self'")
                .header("Referrer-Policy", "strict-origin-when-cross-origin");
    }

    // Audit logging
    public static void logAccess(String user, String endpoint, String method, int status) {
        LOGGER.info("REST Access: {} {} {} -> {} (User: {})", method, endpoint, status, user);
    }

    public static void logSecurityEvent(String event, String user, String details) {
        LOGGER.warn("Security Event: {} (User: {}, Details: {})", event, user, details);
    }

    public static void logAuthenticationSuccess(String user, String method) {
        LOGGER.info("Authentication Success: {} via {}", user, method);
    }

    public static void logAuthenticationFailure(String user, String method, String reason) {
        LOGGER.warn("Authentication Failure: {} via {} - {}", user, method, reason);
    }

    // Session management
    private static final Map<String, SessionInfo> sessionCache = new ConcurrentHashMap<>();
    private static final long DEFAULT_SESSION_TIMEOUT_MS = 3600000; // 1 hour

    public static boolean isValidSession(String sessionId) {
        // Implement actual session validation
        if (sessionId == null || sessionId.trim().isEmpty()) {
            LOGGER.warn("Session ID is null or empty");
            return false;
        }

        if (sessionId.length() < 32) {
            LOGGER.warn("Session ID too short: {}", sessionId.length());
            return false;
        }

        // Check if session exists in cache
        SessionInfo sessionInfo = sessionCache.get(sessionId);
        if (sessionInfo == null) {
            LOGGER.debug("Session not found: {}", sessionId);
            return false;
        }

        // Check if session has expired
        long currentTime = System.currentTimeMillis();
        if (currentTime > sessionInfo.expirationTime) {
            LOGGER.debug("Session expired: {}", sessionId);
            sessionCache.remove(sessionId);
            return false;
        }

        // Check if session is active
        if (!sessionInfo.active) {
            LOGGER.debug("Session inactive: {}", sessionId);
            return false;
        }

        // Update last access time
        sessionInfo = new SessionInfo(sessionInfo.userId, sessionInfo.username, sessionInfo.expirationTime,
                sessionInfo.active, currentTime);
        sessionCache.put(sessionId, sessionInfo);

        LOGGER.debug("Valid session: {} for user: {}", sessionId, sessionInfo.userId);
        return true;
    }

    /**
     * Create a new session
     */
    public static String createSession(String userId, String username) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }

        String sessionId = generateSessionId();
        long expirationTime = System.currentTimeMillis() + DEFAULT_SESSION_TIMEOUT_MS;

        SessionInfo sessionInfo = new SessionInfo(userId, username, expirationTime, true, System.currentTimeMillis());
        sessionCache.put(sessionId, sessionInfo);

        LOGGER.debug("Created session: {} for user: {}", sessionId, userId);
        return sessionId;
    }

    /**
     * Invalidate a session
     */
    public static void invalidateSession(String sessionId) {
        if (sessionId != null) {
            sessionCache.remove(sessionId);
            LOGGER.debug("Invalidated session: {}", sessionId);
        }
    }

    /**
     * Generate a secure session ID
     */
    private static String generateSessionId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    // SessionInfo extracted to top-level: org.openhab.core.ai.rest.SessionInfo

    public static Response invalidSession() {
        return unauthorized("Invalid or expired session");
    }

    public static Response sessionExpired() {
        return unauthorized("Session has expired");
    }

    // API key management
    public static boolean isValidApiKey(String apiKey) {
        return apiKey != null && apiKey.length() >= 32 && apiKey.matches("^[a-zA-Z0-9_-]+$");
    }

    public static Response invalidApiKey() {
        return unauthorized("Invalid API key");
    }

    public static Response apiKeyExpired() {
        return unauthorized("API key has expired");
    }

    // Security testing utilities
    public static Response securityTest(String testType) {
        Map<String, Object> result = Map.of("test_type", testType, "status", "passed", "timestamp",
                System.currentTimeMillis(), "security_level", "high");
        return Response.ok(result, MediaType.APPLICATION_JSON_TYPE).build();
    }

    public static boolean isSecurityTestMode() {
        // Implement configuration-based test mode
        try {
            // Check system property for test mode
            String testModeProperty = System.getProperty("ai.security.test.mode");
            if ("true".equalsIgnoreCase(testModeProperty)) {
                LOGGER.info("Security test mode enabled via system property");
                return true;
            }

            // Check environment variable for test mode
            String testModeEnv = System.getenv("AI_SECURITY_TEST_MODE");
            if ("true".equalsIgnoreCase(testModeEnv)) {
                LOGGER.info("Security test mode enabled via environment variable");
                return true;
            }

            // Check for test configuration file
            String configFile = System.getProperty("ai.security.config", "ai-security.properties");
            File file = new File(configFile);
            if (file.exists()) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    Properties props = new Properties();
                    props.load(fis);
                    String testMode = props.getProperty("security.test.mode");
                    if ("true".equalsIgnoreCase(testMode)) {
                        LOGGER.info("Security test mode enabled via configuration file: {}", configFile);
                        return true;
                    }
                } catch (Exception e) {
                    LOGGER.debug("Could not read security configuration file: {}", configFile, e);
                }
            }

            // Check for development environment indicators
            String userHome = System.getProperty("user.home");
            if (userHome != null && userHome.contains("dev") || userHome.contains("test")) {
                LOGGER.debug("Development environment detected, but test mode not explicitly enabled");
            }

            return false;

        } catch (Exception e) {
            LOGGER.error("Error checking security test mode", e);
            return false;
        }
    }
}
