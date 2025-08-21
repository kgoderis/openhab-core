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
}
