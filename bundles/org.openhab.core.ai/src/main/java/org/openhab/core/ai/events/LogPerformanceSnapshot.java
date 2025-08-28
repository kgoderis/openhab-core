package org.openhab.core.ai.events;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;
import org.openhab.core.ai.common.monitoring.base.Counts;
import org.openhab.core.ai.common.monitoring.base.Timing;

/**
 * Snapshot of log performance metrics at a point in time.
 * 
 * <p>
 * This class provides an immutable view of log processing metrics including ingestion counts,
 * processing rates, and timing information.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record LogPerformanceSnapshot(Counts counts, Timing timing, long timestampMs, long totalLogsProcessed, long totalAnomaliesDetected, double processingRate)
        implements MetricsSnapshot, CountsMetrics, LatencyMetrics {

    /**
     * Create a log performance snapshot from individual metrics.
     * 
     * @param total total number of operations
     * @param success number of successful operations
     * @param failure number of failed operations
     * @param totalDurationNanos total duration in nanoseconds
     * @param totalLogsProcessed total number of logs processed
     * @param totalAnomaliesDetected total number of anomalies detected
     * @param processingRate logs processed per second
     * @return the log performance snapshot
     */
    public static LogPerformanceSnapshot of(long total, long success, long failure, long totalDurationNanos, long totalLogsProcessed, long totalAnomaliesDetected, double processingRate) {
        return new LogPerformanceSnapshot(
            new Counts(total, success, failure),
            new Timing(totalDurationNanos),
            System.currentTimeMillis(),
            totalLogsProcessed,
            totalAnomaliesDetected,
            processingRate
        );
    }

    /**
     * Create an empty log performance snapshot.
     * 
     * @return an empty log performance snapshot
     */
    public static LogPerformanceSnapshot empty() {
        return new LogPerformanceSnapshot(
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
     * Get the total number of logs processed.
     * 
     * @return total number of logs processed
     */
    public long totalLogsProcessed() {
        return totalLogsProcessed;
    }

    /**
     * Get the total number of anomalies detected.
     * 
     * @return total number of anomalies detected
     */
    public long totalAnomaliesDetected() {
        return totalAnomaliesDetected;
    }

    /**
     * Get the processing rate (logs per second).
     * 
     * @return processing rate
     */
    public double processingRate() {
        return processingRate;
    }
}
