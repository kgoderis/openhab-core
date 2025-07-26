package org.openhab.core.ai.common.util;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Validation utility class for AI protocol data.
 * 
 * This class provides validation functions for data structures
 * used in both MCP and A2A protocol implementations.
 * 
 * 
 */
public final class ValidationUtils {

    // Private constructor to prevent instantiation
    private ValidationUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Validate that a parameter is not null.
     * 
     * @param <T> Parameter type
     * @param parameter The parameter to validate
     * @param parameterName The name of the parameter (for error messages)
     * @return The parameter if valid
     * @throws IllegalArgumentException if parameter is null
     */
    public static <T> T requireNonNull(T parameter, String parameterName) {
        if (parameter == null) {
            throw new IllegalArgumentException(parameterName + " cannot be null");
        }
        return parameter;
    }

    /**
     * Validate that a string parameter is not null or empty.
     * 
     * @param parameter The string to validate
     * @param parameterName The name of the parameter (for error messages)
     * @return The parameter if valid
     * @throws IllegalArgumentException if parameter is null or empty
     */
    public static String requireNonEmpty(String parameter, String parameterName) {
        requireNonNull(parameter, parameterName);
        if (parameter.trim().isEmpty()) {
            throw new IllegalArgumentException(parameterName + " cannot be empty");
        }
        return parameter;
    }

    /**
     * Validate that a collection is not null or empty.
     * 
     * @param <T> Collection type
     * @param collection The collection to validate
     * @param parameterName The name of the parameter (for error messages)
     * @return The collection if valid
     * @throws IllegalArgumentException if collection is null or empty
     */
    public static <T> T requireNonEmpty(T collection, String parameterName) {
        requireNonNull(collection, parameterName);

        boolean isEmpty = false;
        if (collection instanceof Map) {
            isEmpty = ((Map<?, ?>) collection).isEmpty();
        } else if (collection instanceof List) {
            isEmpty = ((List<?>) collection).isEmpty();
        } else if (collection instanceof Set) {
            isEmpty = ((Set<?>) collection).isEmpty();
        }

        if (isEmpty) {
            throw new IllegalArgumentException(parameterName + " cannot be empty");
        }
        return collection;
    }

    /**
     * Validate that a number is within a specific range.
     * 
     * @param value The value to validate
     * @param min Minimum allowed value (inclusive)
     * @param max Maximum allowed value (inclusive)
     * @param parameterName The name of the parameter (for error messages)
     * @return The value if valid
     * @throws IllegalArgumentException if value is outside the range
     */
    public static int requireInRange(int value, int min, int max, String parameterName) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(
                    parameterName + " must be between " + min + " and " + max + " (inclusive), got: " + value);
        }
        return value;
    }

    /**
     * Validate that a long value is within a specific range.
     * 
     * @param value The value to validate
     * @param min Minimum allowed value (inclusive)
     * @param max Maximum allowed value (inclusive)
     * @param parameterName The name of the parameter (for error messages)
     * @return The value if valid
     * @throws IllegalArgumentException if value is outside the range
     */
    public static long requireInRange(long value, long min, long max, String parameterName) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(
                    parameterName + " must be between " + min + " and " + max + " (inclusive), got: " + value);
        }
        return value;
    }

    /**
     * Validate that a value is positive.
     * 
     * @param value The value to validate
     * @param parameterName The name of the parameter (for error messages)
     * @return The value if valid
     * @throws IllegalArgumentException if value is not positive
     */
    public static int requirePositive(int value, String parameterName) {
        if (value <= 0) {
            throw new IllegalArgumentException(parameterName + " must be positive, got: " + value);
        }
        return value;
    }

    /**
     * Validate that a value is non-negative.
     * 
     * @param value The value to validate
     * @param parameterName The name of the parameter (for error messages)
     * @return The value if valid
     * @throws IllegalArgumentException if value is negative
     */
    public static int requireNonNegative(int value, String parameterName) {
        if (value < 0) {
            throw new IllegalArgumentException(parameterName + " must be non-negative, got: " + value);
        }
        return value;
    }

    /**
     * Validate a parameter using a custom predicate.
     * 
     * @param <T> Parameter type
     * @param parameter The parameter to validate
     * @param predicate The validation predicate
     * @param parameterName The name of the parameter (for error messages)
     * @param errorMessage Custom error message if validation fails
     * @return The parameter if valid
     * @throws IllegalArgumentException if validation fails
     */
    public static <T> T require(T parameter, Predicate<T> predicate, String parameterName, String errorMessage) {
        requireNonNull(parameter, parameterName);
        if (!predicate.test(parameter)) {
            throw new IllegalArgumentException(parameterName + ": " + errorMessage);
        }
        return parameter;
    }

    /**
     * Validate that a string matches a specific pattern.
     * 
     * @param value The string to validate
     * @param pattern The regex pattern to match
     * @param parameterName The name of the parameter (for error messages)
     * @return The value if valid
     * @throws IllegalArgumentException if value doesn't match the pattern
     */
    public static String requirePattern(String value, String pattern, String parameterName) {
        requireNonEmpty(value, parameterName);
        if (!value.matches(pattern)) {
            throw new IllegalArgumentException(parameterName + " must match pattern '" + pattern + "', got: " + value);
        }
        return value;
    }

    /**
     * Validate a URL format.
     * 
     * @param url The URL to validate
     * @param parameterName The name of the parameter (for error messages)
     * @return The URL if valid
     * @throws IllegalArgumentException if URL is invalid
     */
    public static String requireValidUrl(String url, String parameterName) {
        requireNonEmpty(url, parameterName);
        if (!AIUtils.isValidUrl(url)) {
            throw new IllegalArgumentException(parameterName + " must be a valid URL, got: " + url);
        }
        return url;
    }

    /**
     * Validate a protocol name.
     * 
     * @param protocolName The protocol name to validate
     * @param parameterName The name of the parameter (for error messages)
     * @return The protocol name if valid
     * @throws IllegalArgumentException if protocol name is invalid
     */
    public static String requireValidProtocolName(String protocolName, String parameterName) {
        requireNonEmpty(protocolName, parameterName);
        if (!AIUtils.isValidProtocolName(protocolName)) {
            throw new IllegalArgumentException(parameterName
                    + " must be a valid protocol name (alphanumeric, underscore, dash), got: " + protocolName);
        }
        return protocolName;
    }

    /**
     * Validate an agent ID.
     * 
     * @param agentId The agent ID to validate
     * @param parameterName The name of the parameter (for error messages)
     * @return The agent ID if valid
     * @throws IllegalArgumentException if agent ID is invalid
     */
    public static String requireValidAgentId(String agentId, String parameterName) {
        requireNonEmpty(agentId, parameterName);
        if (!AIUtils.isValidAgentId(agentId)) {
            throw new IllegalArgumentException(
                    parameterName + " must be a valid agent ID (2-64 chars, alphanumeric with ._-), got: " + agentId);
        }
        return agentId;
    }

    /**
     * Validate that all elements in a collection are non-null.
     * 
     * @param <T> Collection type
     * @param collection The collection to validate
     * @param parameterName The name of the parameter (for error messages)
     * @return The collection if valid
     * @throws IllegalArgumentException if any element is null
     */
    public static <T extends Iterable<?>> T requireNoNullElements(T collection, String parameterName) {
        requireNonNull(collection, parameterName);
        int index = 0;
        for (@Nullable
        Object element : collection) {
            if (element == null) {
                throw new IllegalArgumentException(parameterName + " contains null element at index " + index);
            }
            index++;
        }
        return collection;
    }
}
