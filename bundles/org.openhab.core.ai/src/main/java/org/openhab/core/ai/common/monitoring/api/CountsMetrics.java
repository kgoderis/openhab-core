package org.openhab.core.ai.common.monitoring.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Capability interface for basic counting metrics.
 * 
 * <p>
 * This interface provides basic counting functionality including total counts,
 * success counts, failure counts, and success rate calculation.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface CountsMetrics {

    /**
     * Get the total count of operations.
     * 
     * @return total count
     */
    long total();

    /**
     * Get the count of successful operations.
     * 
     * @return success count
     */
    long success();

    /**
     * Get the count of failed operations.
     * 
     * @return failure count
     */
    long failure();

    /**
     * Calculate the success rate as a percentage.
     * 
     * @return success rate between 0.0 and 1.0, or 0.0 if no operations
     */
    default double successRate() {
        long total = total();
        return total > 0 ? (double) success() / total : 0.0;
    }

    /**
     * Calculate the success rate as a percentage (0-100).
     * 
     * @return success rate percentage between 0.0 and 100.0, or 0.0 if no operations
     */
    default double successRatePercentage() {
        return successRate() * 100.0;
    }

    /**
     * Check if there are any operations recorded.
     * 
     * @return true if there are operations, false otherwise
     */
    default boolean hasOperations() {
        return total() > 0;
    }

    /**
     * Check if all operations were successful.
     * 
     * @return true if all operations were successful, false otherwise
     */
    default boolean allSuccessful() {
        return hasOperations() && failure() == 0;
    }

    /**
     * Check if all operations failed.
     * 
     * @return true if all operations failed, false otherwise
     */
    default boolean allFailed() {
        return hasOperations() && success() == 0;
    }

    /**
     * Get the failure rate as a percentage.
     * 
     * @return failure rate between 0.0 and 1.0, or 0.0 if no operations
     */
    default double failureRate() {
        long total = total();
        return total > 0 ? (double) failure() / total : 0.0;
    }

    /**
     * Get the failure rate as a percentage (0-100).
     * 
     * @return failure rate percentage between 0.0 and 100.0, or 0.0 if no operations
     */
    default double failureRatePercentage() {
        return failureRate() * 100.0;
    }
}
