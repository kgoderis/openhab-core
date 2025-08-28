package org.openhab.core.ai.events;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;
import org.openhab.core.ai.common.monitoring.base.Counts;
import org.openhab.core.ai.common.monitoring.base.Timing;

/**
 * Snapshot of correlation performance metrics at a point in time.
 * 
 * <p>
 * This class provides an immutable view of event correlation metrics including
 * correlation counts, success/failure rates, and timing information.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record CorrelationPerformanceSnapshot(Counts counts, Timing timing, long timestampMs, long totalCorrelations, long totalPatternsDetected, double correlationAccuracy)
        implements MetricsSnapshot, CountsMetrics, LatencyMetrics {

    /**
     * Create a correlation performance snapshot from individual metrics.
     * 
     * @param total total number of operations
     * @param success number of successful operations
     * @param failure number of failed operations
     * @param totalDurationNanos total duration in nanoseconds
     * @param totalCorrelations total number of correlations performed
     * @param totalPatternsDetected total number of patterns detected
     * @param correlationAccuracy correlation accuracy percentage
     * @return the correlation performance snapshot
     */
    public static CorrelationPerformanceSnapshot of(long total, long success, long failure, long totalDurationNanos, long totalCorrelations, long totalPatternsDetected, double correlationAccuracy) {
        return new CorrelationPerformanceSnapshot(
            new Counts(total, success, failure),
            new Timing(totalDurationNanos),
            System.currentTimeMillis(),
            totalCorrelations,
            totalPatternsDetected,
            correlationAccuracy
        );
    }

    /**
     * Create an empty correlation performance snapshot.
     * 
     * @return an empty correlation performance snapshot
     */
    public static CorrelationPerformanceSnapshot empty() {
        return new CorrelationPerformanceSnapshot(
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
     * Get the total number of correlations performed.
     * 
     * @return total number of correlations performed
     */
    public long totalCorrelations() {
        return totalCorrelations;
    }

    /**
     * Get the total number of patterns detected.
     * 
     * @return total number of patterns detected
     */
    public long totalPatternsDetected() {
        return totalPatternsDetected;
    }

    /**
     * Get the correlation accuracy percentage.
     * 
     * @return correlation accuracy percentage
     */
    public double correlationAccuracy() {
        return correlationAccuracy;
    }
}
