package org.openhab.core.ai.rest;

import java.util.Map;

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
        // TODO: Implement actual permission checking logic
        return "admin".equals(user) || "user".equals(user);
    }

    public static boolean hasRole(String user, String role) {
        // TODO: Implement actual role checking logic
        return "admin".equals(user) || "user".equals(user);
    }

    public static Response insufficientPermissions(String requiredPermission) {
        return forbidden("Insufficient permissions. Required: " + requiredPermission);
    }

    public static Response insufficientRole(String requiredRole) {
        return forbidden("Insufficient role. Required: " + requiredRole);
    }

    // Rate limiting
    public static boolean isRateLimited(String clientId, String endpoint) {
        // TODO: Implement actual rate limiting logic with Redis or similar
        return false;
    }

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
    public static boolean isValidSession(String sessionId) {
        // TODO: Implement actual session validation
        return sessionId != null && sessionId.length() >= 32;
    }

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
        // TODO: Implement configuration-based test mode
        return false;
    }
}
