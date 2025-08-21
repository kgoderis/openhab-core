package org.openhab.core.ai.common.monitoring.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Immutable record for timing-based metrics.
 * 
 * <p>
 * This record provides a thread-safe way to share timing metrics between
 * collectors and snapshots.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record Timing(long totalDurationNanos) {

    /**
     * Create a new Timing record.
     * 
     * @param totalDurationNanos total duration in nanoseconds
     */
    public Timing {
        if (totalDurationNanos < 0) {
            throw new IllegalArgumentException("totalDurationNanos must be non-negative");
        }
    }

    /**
     * Get the total duration in milliseconds.
     * 
     * @return total duration in milliseconds
     */
    public double totalDurationMs() {
        return (double) totalDurationNanos / 1_000_000.0;
    }

    /**
     * Get the total duration in seconds.
     * 
     * @return total duration in seconds
     */
    public double totalDurationSeconds() {
        return (double) totalDurationNanos / 1_000_000_000.0;
    }

    /**
     * Get the total duration in microseconds.
     * 
     * @return total duration in microseconds
     */
    public double totalDurationMicros() {
        return (double) totalDurationNanos / 1_000.0;
    }
}
