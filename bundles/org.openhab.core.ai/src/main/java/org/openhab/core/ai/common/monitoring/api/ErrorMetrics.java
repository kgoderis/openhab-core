package org.openhab.core.ai.common.monitoring.api;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Capability interface for error tracking metrics.
 * 
 * <p>
 * This interface provides functionality for tracking and analyzing errors,
 * including error counts by type and timing information.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface ErrorMetrics {

    /**
     * Get the last error message.
     * 
     * @return last error message, or null if no errors
     */
    String lastError();

    /**
     * Get the timestamp of the last failure.
     * 
     * @return last failure time in milliseconds since epoch, or 0L if no failures
     */
    long lastFailureTime();

    /**
     * Get error counts by error type.
     * 
     * @return map of error type to count
     */
    Map<String, Long> errorCountsByType();

    /**
     * Get the total number of errors.
     * 
     * @return total error count
     */
    default long totalErrors() {
        return errorCountsByType().values().stream().mapToLong(Long::longValue).sum();
    }

    /**
     * Get the most common error type.
     * 
     * @return most common error type, or null if no errors
     */
    default String mostCommonErrorType() {
        return errorCountsByType().entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey)
                .orElse(null);
    }

    /**
     * Get the count for a specific error type.
     * 
     * @param errorType the error type
     * @return count for the error type, or 0L if not found
     */
    default long getErrorCount(String errorType) {
        return errorCountsByType().getOrDefault(errorType, 0L);
    }

    /**
     * Check if there are any errors.
     * 
     * @return true if there are errors, false otherwise
     */
    default boolean hasErrors() {
        return totalErrors() > 0;
    }

    /**
     * Get the error rate as a percentage.
     * 
     * @param total the total number of operations
     * @return error rate percentage (0.0-100.0), or 0.0 if no operations
     */
    default double errorRatePercentage(long total) {
        return total > 0 ? (totalErrors() * 100.0) / total : 0.0;
    }

    /**
     * Get the time since the last failure in milliseconds.
     * 
     * @return time since last failure in milliseconds, or Long.MAX_VALUE if no failures
     */
    default long timeSinceLastFailure() {
        if (lastFailureTime() == 0L) {
            return Long.MAX_VALUE;
        }
        return System.currentTimeMillis() - lastFailureTime();
    }

    /**
     * Check if the system has had recent failures.
     * 
     * @param thresholdMs threshold in milliseconds
     * @return true if there was a failure within the threshold, false otherwise
     */
    default boolean hasRecentFailures(long thresholdMs) {
        return timeSinceLastFailure() < thresholdMs;
    }
}
