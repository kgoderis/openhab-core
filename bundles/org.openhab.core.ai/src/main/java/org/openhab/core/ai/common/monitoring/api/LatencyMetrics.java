package org.openhab.core.ai.common.monitoring.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Capability interface for latency and timing metrics.
 * 
 * <p>
 * This interface provides functionality for measuring and reporting latency
 * and timing information including total duration and average calculations.
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
     * Calculate the average duration in milliseconds.
     * 
     * @param total the total number of operations to calculate average against
     * @return average duration in milliseconds, or 0.0 if no operations
     */
    default double averageMs(long total) {
        return total > 0 ? (double) totalDurationNanos() / total / 1_000_000.0 : 0.0;
    }

    /**
     * Get the total duration in milliseconds.
     * 
     * @return total duration in milliseconds
     */
    default double totalDurationMs() {
        return (double) totalDurationNanos() / 1_000_000.0;
    }

    /**
     * Get the total duration in seconds.
     * 
     * @return total duration in seconds
     */
    default double totalDurationSeconds() {
        return (double) totalDurationNanos() / 1_000_000_000.0;
    }

    /**
     * Calculate the average duration in microseconds.
     * 
     * @param total the total number of operations to calculate average against
     * @return average duration in microseconds, or 0.0 if no operations
     */
    default double averageMicros(long total) {
        return total > 0 ? (double) totalDurationNanos() / total / 1_000.0 : 0.0;
    }

    /**
     * Calculate the average duration in nanoseconds.
     * 
     * @param total the total number of operations to calculate average against
     * @return average duration in nanoseconds, or 0.0 if no operations
     */
    default double averageNanos(long total) {
        return total > 0 ? (double) totalDurationNanos() / total : 0.0;
    }

    /**
     * Check if there is any duration data.
     * 
     * @return true if there is duration data, false otherwise
     */
    default boolean hasDuration() {
        return totalDurationNanos() > 0;
    }

    /**
     * Get the throughput (operations per second) based on duration.
     * 
     * @param total the total number of operations
     * @return throughput in operations per second, or 0.0 if no duration
     */
    default double throughput(long total) {
        double durationSeconds = totalDurationSeconds();
        return durationSeconds > 0 ? total / durationSeconds : 0.0;
    }
}
