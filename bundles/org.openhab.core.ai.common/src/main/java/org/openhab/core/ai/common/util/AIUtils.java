package org.openhab.core.ai.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Utility class for AI protocol implementations.
 * 
 * This class provides common helper functions used across both
 * MCP and A2A protocol implementations.
 * 
 * 
 */
public final class AIUtils {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Pattern VALID_PROTOCOL_NAME = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_-]*$");
    private static final Pattern VALID_AGENT_ID = Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9._-]*[a-zA-Z0-9]$");

    // Private constructor to prevent instantiation
    private AIUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Generate a secure random session ID.
     * 
     * @return A cryptographically secure random session ID
     */
    public static String generateSessionId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Generate a secure random token.
     * 
     * @param length The length of the token in bytes (will be base64 encoded)
     * @return A base64-encoded random token
     */
    public static String generateSecureToken(int length) {
        byte[] tokenBytes = new byte[length];
        SECURE_RANDOM.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    /**
     * Generate a SHA-256 hash of the input string.
     * 
     * @param input The input string to hash
     * @return The SHA-256 hash as a hexadecimal string
     */
    public static String sha256Hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Validate a protocol name.
     * 
     * @param protocolName The protocol name to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidProtocolName(String protocolName) {
        return protocolName != null && !protocolName.trim().isEmpty()
                && VALID_PROTOCOL_NAME.matcher(protocolName).matches();
    }

    /**
     * Validate an agent ID.
     * 
     * @param agentId The agent ID to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidAgentId(String agentId) {
        return agentId != null && agentId.length() >= 2 && agentId.length() <= 64
                && VALID_AGENT_ID.matcher(agentId).matches();
    }

    /**
     * Validate a URL format.
     * 
     * @param url The URL to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        try {
            java.net.URI.create(url).toURL();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Sanitize a string for logging (remove sensitive information).
     * 
     * @param input The input string
     * @return Sanitized string safe for logging
     */
    public static String sanitizeForLogging(String input) {
        if (input == null) {
            return "null";
        }

        // Replace common sensitive patterns
        String sanitized = input.replaceAll("(?i)(password|token|key|secret|auth)=[^\\s&]+", "$1=***")
                .replaceAll("(?i)(bearer|basic)\\s+[^\\s]+", "$1 ***")
                .replaceAll("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b", "***@***.***");

        return sanitized;
    }

    /**
     * Format an Instant as ISO-8601 string.
     * 
     * @param instant The instant to format
     * @return ISO-8601 formatted string
     */
    public static String formatInstant(Instant instant) {
        return instant != null ? DateTimeFormatter.ISO_INSTANT.format(instant) : null;
    }

    /**
     * Check if a string is null or empty.
     * 
     * @param str The string to check
     * @return true if null or empty, false otherwise
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Check if a string is not null and not empty.
     * 
     * @param str The string to check
     * @return true if not null and not empty, false otherwise
     */
    public static boolean isNotNullOrEmpty(String str) {
        return !isNullOrEmpty(str);
    }

    /**
     * Get a safe string representation (null-safe).
     * 
     * @param obj The object to convert to string
     * @return String representation or "null" if the object is null
     */
    public static String safeToString(Object obj) {
        return obj != null ? obj.toString() : "null";
    }

    /**
     * Create a request correlation ID for tracing.
     * 
     * @param protocolName The protocol name
     * @param requestType The type of request
     * @return A correlation ID for request tracing
     */
    public static String createCorrelationId(String protocolName, String requestType) {
        return String.format("%s-%s-%s", protocolName != null ? protocolName : "unknown",
                requestType != null ? requestType : "request", generateSessionId().substring(0, 8));
    }

    /**
     * Merge two maps, with the second map taking precedence for duplicate keys.
     * 
     * @param <K> Key type
     * @param <V> Value type
     * @param map1 First map
     * @param map2 Second map (takes precedence)
     * @return Merged map
     */
    public static <K, V> Map<K, V> mergeMaps(Map<K, V> map1, Map<K, V> map2) {
        if (map1 == null && map2 == null) {
            return Map.of();
        }
        if (map1 == null) {
            return Map.copyOf(map2);
        }
        if (map2 == null) {
            return Map.copyOf(map1);
        }

        var merged = new java.util.HashMap<>(map1);
        merged.putAll(map2);
        return Map.copyOf(merged);
    }

    /**
     * Calculate timeout with jitter to avoid thundering herd.
     * 
     * @param baseTimeoutMs Base timeout in milliseconds
     * @param jitterPercent Jitter percentage (0-100)
     * @return Timeout with jitter applied
     */
    public static long calculateTimeoutWithJitter(long baseTimeoutMs, int jitterPercent) {
        if (jitterPercent <= 0) {
            return baseTimeoutMs;
        }

        int clampedJitter = Math.min(100, Math.max(0, jitterPercent));
        double jitterFactor = 1.0 + (SECURE_RANDOM.nextDouble() * 2 - 1) * (clampedJitter / 100.0);
        return Math.round(baseTimeoutMs * jitterFactor);
    }
}
