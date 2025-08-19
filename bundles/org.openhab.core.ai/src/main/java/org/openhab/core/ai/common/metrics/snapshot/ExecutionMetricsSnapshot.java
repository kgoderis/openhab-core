package org.openhab.core.ai.common.metrics.snapshot;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.metrics.api.Counts;
import org.openhab.core.ai.common.metrics.api.CountsMetrics;
import org.openhab.core.ai.common.metrics.api.LatencyMetrics;
import org.openhab.core.ai.common.metrics.api.MetricsSnapshot;
import org.openhab.core.ai.common.metrics.api.Timing;

/**
 * Immutable snapshot of execution metrics.
 * 
 * <p>
 * This record provides a thread-safe snapshot of execution metrics at a specific
 * point in time. It implements multiple capability interfaces for different
 * types of metrics access.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record ExecutionMetricsSnapshot(Counts counts, Timing timing,
        long timestampMs) implements MetricsSnapshot, CountsMetrics, LatencyMetrics {

    public long total() {
        return counts.total();
    }

    public long success() {
        return counts.success();
    }

    public long failure() {
        return counts.failure();
    }

    public long totalDurationNanos() {
        return timing.totalDurationNanos();
    }

    public double averageMs() {
        return LatencyMetrics.super.averageMs(total());
    }
}
