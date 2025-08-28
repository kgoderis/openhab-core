package org.openhab.core.ai.reasoning.input;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;
import org.openhab.core.ai.common.monitoring.base.Counts;
import org.openhab.core.ai.common.monitoring.base.Timing;

/**
 * Snapshot of input performance metrics at a point in time.
 * 
 * <p>
 * This class provides an immutable view of input processing metrics including routing counts,
 * validation rates, and timing information.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record InputPerformanceSnapshot(Counts counts, Timing timing, long timestampMs, long totalInputsProcessed, long totalInputsRouted, double routingAccuracy)
        implements MetricsSnapshot, CountsMetrics, LatencyMetrics {

    /**
     * Create an input performance snapshot from individual metrics.
     * 
     * @param total total number of operations
     * @param success number of successful operations
     * @param failure number of failed operations
     * @param totalDurationNanos total duration in nanoseconds
     * @param totalInputsProcessed total number of inputs processed
     * @param totalInputsRouted total number of inputs routed
     * @param routingAccuracy routing accuracy percentage
     * @return the input performance snapshot
     */
    public static InputPerformanceSnapshot of(long total, long success, long failure, long totalDurationNanos, long totalInputsProcessed, long totalInputsRouted, double routingAccuracy) {
        return new InputPerformanceSnapshot(
            new Counts(total, success, failure),
            new Timing(totalDurationNanos),
            System.currentTimeMillis(),
            totalInputsProcessed,
            totalInputsRouted,
            routingAccuracy
        );
    }

    /**
     * Create an empty input performance snapshot.
     * 
     * @return an empty input performance snapshot
     */
    public static InputPerformanceSnapshot empty() {
        return new InputPerformanceSnapshot(
            new Counts(0, 0, 0),
            new Timing(0),
            System.currentTimeMillis(),
            0,
            0,
            0.0
        );
    }

    @Override
    public long total() {
        return counts.total();
    }

    @Override
    public long success() {
        return counts.success();
    }

    @Override
    public long failure() {
        return counts.failure();
    }

    @Override
    public long totalDurationNanos() {
        return timing.totalDurationNanos();
    }

    /**
     * Get the average processing time in milliseconds.
     * 
     * @return average processing time in milliseconds
     */
    public double averageMs() {
        return LatencyMetrics.super.averageMs(total());
    }

    /**
     * Get the total number of inputs processed.
     * 
     * @return total number of inputs processed
     */
    public long totalInputsProcessed() {
        return totalInputsProcessed;
    }

    /**
     * Get the total number of inputs routed.
     * 
     * @return total number of inputs routed
     */
    public long totalInputsRouted() {
        return totalInputsRouted;
    }

    /**
     * Get the routing accuracy percentage.
     * 
     * @return routing accuracy percentage
     */
    public double routingAccuracy() {
        return routingAccuracy;
    }
}
