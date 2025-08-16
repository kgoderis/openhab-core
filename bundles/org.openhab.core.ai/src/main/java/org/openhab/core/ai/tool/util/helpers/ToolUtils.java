package org.openhab.core.ai.tool.util.helpers;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Helper utilities for tool operations.
 * 
 * This class provides utility methods for common tool operations, including
 * validation, formatting, and data transformation.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ToolUtils {

    // Cache for tool availability status
    private static final Map<String, Boolean> toolAvailabilityCache = new ConcurrentHashMap<>();

    // Cache for tool performance metrics
    private static final Map<String, Long> toolPerformanceCache = new ConcurrentHashMap<>();

    // Pattern for sanitizing input
    private static final Pattern SANITIZE_PATTERN = Pattern.compile("[<>\"'&]");

    /**
     * Validate tool configuration.
     * 
     * @param config the tool configuration to validate
     * @return true if the configuration is valid
     */
    public static boolean validateToolConfiguration(Map<String, Object> config) {
        if (config == null || config.isEmpty()) {
            return false;
        }

        // Check for required configuration fields
        if (!config.containsKey("id") || !config.containsKey("name")) {
            return false;
        }

        // Validate ID format
        Object id = config.get("id");
        if (!(id instanceof String) || ((String) id).trim().isEmpty()) {
            return false;
        }

        // Validate name format
        Object name = config.get("name");
        if (!(name instanceof String) || ((String) name).trim().isEmpty()) {
            return false;
        }

        // Validate version if present
        if (config.containsKey("version")) {
            Object version = config.get("version");
            if (!(version instanceof String) || !isValidVersion((String) version)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Format tool parameters for display.
     * 
     * @param parameters the tool parameters to format
     * @return formatted parameters string
     */
    public static String formatToolParameters(Map<String, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return "{}";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{");

        boolean first = true;
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(entry.getKey()).append("=");

            Object value = entry.getValue();
            if (value instanceof String) {
                sb.append("\"").append(value).append("\"");
            } else {
                sb.append(value);
            }

            first = false;
        }

        sb.append("}");
        return sb.toString();
    }

    /**
     * Transform tool data between formats.
     * 
     * @param data the data to transform
     * @param sourceFormat the source format
     * @param targetFormat the target format
     * @return transformed data
     */
    public static Object transformToolData(Object data, String sourceFormat, String targetFormat) {
        if (data == null) {
            return null;
        }

        // Handle common format transformations
        if ("string".equals(sourceFormat) && "json".equals(targetFormat)) {
            // String to JSON transformation
            return Map.of("value", data.toString());
        } else if ("json".equals(sourceFormat) && "string".equals(targetFormat)) {
            // JSON to string transformation
            if (data instanceof Map) {
                return data.toString();
            }
        } else if ("number".equals(sourceFormat) && "string".equals(targetFormat)) {
            // Number to string transformation
            return data.toString();
        } else if ("string".equals(sourceFormat) && "number".equals(targetFormat)) {
            // String to number transformation
            try {
                if (data.toString().contains(".")) {
                    return Double.parseDouble(data.toString());
                } else {
                    return Long.parseLong(data.toString());
                }
            } catch (NumberFormatException e) {
                return data; // Return original if conversion fails
            }
        }

        // Default: return original data
        return data;
    }

    /**
     * Generate a unique tool ID.
     * 
     * @param prefix the ID prefix
     * @return unique tool ID
     */
    public static String generateToolId(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            prefix = "tool";
        }

        // Clean the prefix to ensure it's valid
        String cleanPrefix = prefix.replaceAll("[^a-zA-Z0-9_-]", "_");

        // Generate a unique suffix using UUID
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        return cleanPrefix + "_" + suffix;
    }

    /**
     * Sanitize tool input.
     * 
     * @param input the input to sanitize
     * @return sanitized input
     */
    public static String sanitizeToolInput(String input) {
        if (input == null) {
            return "";
        }

        // Remove leading/trailing whitespace
        String sanitized = input.trim();

        // Replace potentially dangerous characters
        sanitized = SANITIZE_PATTERN.matcher(sanitized).replaceAll("");

        // Limit length to prevent abuse
        if (sanitized.length() > 10000) {
            sanitized = sanitized.substring(0, 10000);
        }

        return sanitized;
    }

    /**
     * Check if a tool is available.
     * 
     * @param toolId the tool ID to check
     * @return true if the tool is available
     */
    public static boolean isToolAvailable(String toolId) {
        if (toolId == null || toolId.trim().isEmpty()) {
            return false;
        }

        // Check cache first
        Boolean cached = toolAvailabilityCache.get(toolId);
        if (cached != null) {
            return cached;
        }

        // Basic availability check
        boolean available = toolId.matches("^[a-zA-Z0-9_-]+$") && toolId.length() <= 100;

        // Cache the result
        toolAvailabilityCache.put(toolId, available);

        return available;
    }

    /**
     * Validate tool data.
     * 
     * @param data the data to validate
     * @param schema the validation schema
     * @return true if the data is valid according to the schema
     */
    public static boolean validateToolData(Object data, Map<String, Object> schema) {
        if (schema == null || schema.isEmpty()) {
            return true; // No schema means everything is valid
        }

        if (data == null) {
            // Check if null is allowed
            return !Boolean.TRUE.equals(schema.get("required"));
        }

        // Check type validation
        String expectedType = (String) schema.get("type");
        if (expectedType != null) {
            switch (expectedType) {
                case "string":
                    if (!(data instanceof String)) {
                        return false;
                    }
                    break;
                case "number":
                    if (!(data instanceof Number)) {
                        return false;
                    }
                    break;
                case "boolean":
                    if (!(data instanceof Boolean)) {
                        return false;
                    }
                    break;
                case "object":
                    if (!(data instanceof Map)) {
                        return false;
                    }
                    break;
                case "array":
                    if (!(data instanceof List)) {
                        return false;
                    }
                    break;
            }
        }

        // Check string length constraints
        if (data instanceof String && schema.containsKey("maxLength")) {
            int maxLength = ((Number) schema.get("maxLength")).intValue();
            if (((String) data).length() > maxLength) {
                return false;
            }
        }

        return true;
    }

    /**
     * Optimize tool performance.
     * 
     * @param toolId the tool ID
     * @param executionTimeMs the execution time in milliseconds
     */
    public static void optimizeToolPerformance(String toolId, long executionTimeMs) {
        if (toolId == null || toolId.trim().isEmpty()) {
            return;
        }

        // Store performance metrics
        toolPerformanceCache.put(toolId, executionTimeMs);

        // If execution time is too high, log a warning
        if (executionTimeMs > 5000) { // 5 seconds threshold
            System.err.println("Warning: Tool " + toolId + " took " + executionTimeMs + "ms to execute");
        }
    }

    /**
     * Get tool performance metrics.
     * 
     * @param toolId the tool ID
     * @return the execution time in milliseconds, or -1 if not available
     */
    public static long getToolPerformance(String toolId) {
        if (toolId == null || toolId.trim().isEmpty()) {
            return -1;
        }

        return toolPerformanceCache.getOrDefault(toolId, -1L);
    }

    /**
     * Clear tool caches.
     * 
     * @param toolId the tool ID to clear, or null to clear all
     */
    public static void clearToolCaches(String toolId) {
        if (toolId == null) {
            toolAvailabilityCache.clear();
            toolPerformanceCache.clear();
        } else {
            toolAvailabilityCache.remove(toolId);
            toolPerformanceCache.remove(toolId);
        }
    }

    /**
     * Check if a version string is valid.
     * 
     * @param version the version string to validate
     * @return true if the version is valid
     */
    private static boolean isValidVersion(String version) {
        if (version == null || version.trim().isEmpty()) {
            return false;
        }

        // Basic semantic versioning validation
        return version.matches("^\\d+\\.\\d+\\.\\d+(-[a-zA-Z0-9.-]+)?(\\+[a-zA-Z0-9.-]+)?$");
    }
}
