package org.openhab.core.ai.rest;

import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shared REST infrastructure utilities for consistent API responses
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public final class SharedRestInfrastructure {

    private static final Logger LOGGER = LoggerFactory.getLogger(SharedRestInfrastructure.class);

    private SharedRestInfrastructure() {
        // Utility class
    }

    // Standard response builders
    public static Response okJson(Object data) {
        return applyStandardHeaders(Response.ok(data, MediaType.APPLICATION_JSON_TYPE)).build();
    }

    public static Response createdJson(Object data) {
        return applyStandardHeaders(
                Response.status(Response.Status.CREATED).entity(data).type(MediaType.APPLICATION_JSON_TYPE)).build();
    }

    public static Response error(Response.Status status, String message) {
        Map<String, Object> error = Map.of("error", message, "status", status.getStatusCode(), "timestamp",
                System.currentTimeMillis());
        return Response.status(status).entity(error).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    public static Response error(Response.Status status, String message, String code) {
        Map<String, Object> error = Map.of("error", message, "code", code, "status", status.getStatusCode(),
                "timestamp", System.currentTimeMillis());
        return Response.status(status).entity(error).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    // Keep the int version for backward compatibility
    public static Response error(int status, String message) {
        Map<String, Object> error = Map.of("error", message, "status", status, "timestamp", System.currentTimeMillis());
        return Response.status(status).entity(error).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    public static Response error(int status, String message, String code) {
        Map<String, Object> error = Map.of("error", message, "code", code, "status", status, "timestamp",
                System.currentTimeMillis());
        return Response.status(status).entity(error).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    // Standard headers application
    public static ResponseBuilder applyStandardHeaders(ResponseBuilder builder) {
        return builder.header("X-Content-Type-Options", "nosniff").header("X-Frame-Options", "DENY")
                .header("X-XSS-Protection", "1; mode=block").header("Cache-Control", "public, max-age=300")
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                .header("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    // Validation utilities
    public static boolean isValidId(String id) {
        return id != null && !id.trim().isEmpty() && id.matches("^[a-zA-Z0-9_-]+$");
    }

    public static boolean isValidLimit(int limit) {
        return limit > 0 && limit <= 100;
    }

    public static boolean isValidOffset(int offset) {
        return offset >= 0;
    }

    public static Response validatePaginationParams(int limit, int offset) {
        if (!isValidLimit(limit)) {
            return error(400, "Invalid limit parameter. Must be between 1 and 100.");
        }
        if (!isValidOffset(offset)) {
            return error(400, "Invalid offset parameter. Must be non-negative.");
        }
        return null; // No error
    }

    // Logging utilities
    public static void logRequest(String method, String path, String userAgent) {
        LOGGER.debug("REST Request: {} {} (User-Agent: {})", method, path, userAgent);
    }

    public static void logResponse(String method, String path, int status) {
        LOGGER.debug("REST Response: {} {} -> {}", method, path, status);
    }

    public static void logError(String method, String path, int status, String error) {
        LOGGER.warn("REST Error: {} {} -> {}: {}", method, path, status, error);
    }

    // Performance monitoring
    public static long startTimer() {
        return System.currentTimeMillis();
    }

    public static long endTimer(long startTime) {
        return System.currentTimeMillis() - startTime;
    }

    public static void logPerformance(String endpoint, long duration) {
        if (duration > 1000) {
            LOGGER.warn("Slow REST endpoint: {} took {}ms", endpoint, duration);
        } else {
            LOGGER.debug("REST endpoint: {} took {}ms", endpoint, duration);
        }
    }

    // Caching utilities
    public static String generateETag(String content) {
        return "W/\"" + Integer.toHexString(content.hashCode()) + "\"";
    }

    public static ResponseBuilder addCacheHeaders(ResponseBuilder builder, String etag, int maxAge) {
        return builder.header("ETag", etag).header("Cache-Control", "public, max-age=" + maxAge).header("Last-Modified",
                System.currentTimeMillis());
    }

    // Rate limiting utilities
    public static boolean isRateLimited(String clientId, int maxRequests, int windowSeconds) {
        // TODO: Implement actual rate limiting logic
        return false;
    }

    public static Response rateLimitExceeded() {
        return Response.status(429).header("Retry-After", 60)
                .entity(Map.of("error", "Rate limit exceeded", "retry_after", 60)).type(MediaType.APPLICATION_JSON_TYPE)
                .build();
    }

    // Security utilities
    public static boolean isValidApiKey(String apiKey) {
        return apiKey != null && apiKey.length() >= 32 && apiKey.matches("^[a-zA-Z0-9_-]+$");
    }

    public static Response unauthorized(String message) {
        return Response.status(401).header("WWW-Authenticate", "Bearer")
                .entity(Map.of("error", message, "code", "UNAUTHORIZED")).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    public static Response forbidden(String message) {
        return Response.status(403).entity(Map.of("error", message, "code", "FORBIDDEN"))
                .type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    // Testing utilities
    public static Map<String, Object> createTestResponse(String message) {
        return Map.of("message", message, "timestamp", System.currentTimeMillis(), "test", true);
    }

    public static Response testEndpoint(String endpoint) {
        return okJson(createTestResponse("Test endpoint: " + endpoint));
    }
}
