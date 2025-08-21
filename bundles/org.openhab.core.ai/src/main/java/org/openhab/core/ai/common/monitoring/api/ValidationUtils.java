package org.openhab.core.ai.common.monitoring.api;

import java.time.Instant;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Utility class providing common validation methods for monitoring data.
 * 
 * <p>
 * This class provides static validation methods that can be used across
 * all monitoring data classes to ensure consistent validation patterns
 * and error messages.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class ValidationUtils {

    private ValidationUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Validate that a string is not null or blank.
     * 
     * @param value the string to validate
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if the string is null or blank
     */
    public static void validateNotBlank(@Nullable String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or blank");
        }
    }

    /**
     * Validate that a value is not null.
     * 
     * @param value the value to validate
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if the value is null
     */
    public static void validateNotNull(@Nullable Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }
    }

    /**
     * Validate that a numeric value is non-negative.
     * 
     * @param value the value to validate
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if the value is negative
     */
    public static void validateNonNegative(long value, String fieldName) {
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " cannot be negative");
        }
    }

    /**
     * Validate that a numeric value is non-negative.
     * 
     * @param value the value to validate
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if the value is negative
     */
    public static void validateNonNegative(double value, String fieldName) {
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " cannot be negative");
        }
    }

    /**
     * Validate that a numeric value is within a specified range.
     * 
     * @param value the value to validate
     * @param min the minimum allowed value (inclusive)
     * @param max the maximum allowed value (inclusive)
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if the value is outside the range
     */
    public static void validateRange(long value, long min, long max, String fieldName) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(fieldName + " must be between " + min + " and " + max + " (inclusive)");
        }
    }

    /**
     * Validate that a numeric value is within a specified range.
     * 
     * @param value the value to validate
     * @param min the minimum allowed value (inclusive)
     * @param max the maximum allowed value (inclusive)
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if the value is outside the range
     */
    public static void validateRange(double value, double min, double max, String fieldName) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(fieldName + " must be between " + min + " and " + max + " (inclusive)");
        }
    }

    /**
     * Validate that a timestamp is not in the future (with tolerance for clock skew).
     * 
     * @param timestamp the timestamp to validate
     * @param fieldName the name of the field for error messages
     * @param toleranceSeconds the tolerance in seconds for clock skew
     * @throws IllegalArgumentException if the timestamp is too far in the future
     */
    public static void validateNotInFuture(Instant timestamp, String fieldName, long toleranceSeconds) {
        Objects.requireNonNull(timestamp, fieldName + " cannot be null");
        Instant now = Instant.now();
        if (timestamp.isAfter(now.plusSeconds(toleranceSeconds))) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be more than " + toleranceSeconds + " seconds in the future");
        }
    }

    /**
     * Validate that a timestamp is not in the future (with default 60-second tolerance).
     * 
     * @param timestamp the timestamp to validate
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if the timestamp is too far in the future
     */
    public static void validateNotInFuture(Instant timestamp, String fieldName) {
        validateNotInFuture(timestamp, fieldName, 60);
    }

    /**
     * Validate that a start time is before an end time.
     * 
     * @param startTime the start time
     * @param endTime the end time
     * @param startFieldName the name of the start time field for error messages
     * @param endFieldName the name of the end time field for error messages
     * @throws IllegalArgumentException if start time is after end time
     */
    public static void validateTimeRange(@Nullable Instant startTime, @Nullable Instant endTime, String startFieldName,
            String endFieldName) {
        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            throw new IllegalArgumentException(startFieldName + " must be before " + endFieldName);
        }
    }

    /**
     * Validate that operation counts are consistent.
     * 
     * @param total the total count
     * @param success the success count
     * @param failure the failure count
     * @param totalFieldName the name of the total field for error messages
     * @param successFieldName the name of the success field for error messages
     * @param failureFieldName the name of the failure field for error messages
     * @throws IllegalArgumentException if the counts are inconsistent
     */
    public static void validateOperationCounts(long total, long success, long failure, String totalFieldName,
            String successFieldName, String failureFieldName) {
        validateNonNegative(total, totalFieldName);
        validateNonNegative(success, successFieldName);
        validateNonNegative(failure, failureFieldName);

        if (total != success + failure) {
            throw new IllegalArgumentException(totalFieldName + " (" + total + ") must equal " + successFieldName + " ("
                    + success + ") + " + failureFieldName + " (" + failure + ")");
        }
    }

    /**
     * Validate that a percentage value is within the valid range (0-100).
     * 
     * @param percentage the percentage value to validate
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if the percentage is outside the valid range
     */
    public static void validatePercentage(double percentage, String fieldName) {
        validateRange(percentage, 0.0, 100.0, fieldName);
    }
}
