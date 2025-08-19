package org.openhab.core.ai.common.metrics.api;

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
}
