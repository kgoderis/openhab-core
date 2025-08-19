package org.openhab.core.ai.common.metrics.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Capability interface for latency-based metrics.
 * 
 * <p>
 * This interface provides methods for accessing latency metrics including
 * total duration and average response time.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface LatencyMetrics {

    /**
     * Get the total duration in nanoseconds.
     * 
     * @return total duration in nanoseconds
     */
    long totalDurationNanos();

    /**
     * Get the average response time in milliseconds.
     * 
     * @param totalOperations total number of operations for calculation
     * @return average response time in milliseconds
     */
    default double averageMs(long totalOperations) {
        if (totalOperations == 0) {
            return 0.0;
        }
        return (double) totalDurationNanos() / totalOperations / 1_000_000.0;
    }
}
