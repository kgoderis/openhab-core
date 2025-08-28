package org.openhab.core.ai.reasoning.input;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;

import java.time.Duration;
import java.util.List;

/**
 * Statistics for input performance over a time range.
 * 
 * <p>
 * This class provides aggregated statistics for input processing metrics including
 * routing counts, validation rates, timing information, trends, and percentiles.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record InputPerformanceStatistics(
    long total,
    long success,
    long failure,
    long totalDurationNanos,
    double averageMs,
    double successRate,
    double failureRate,
    double throughput,
    double trend,
    double p50Ms,
    double p95Ms,
    double p99Ms,
    long totalInputsProcessed,
    long totalInputsRouted,
    double routingAccuracy,
    Duration timeRange
) implements CountsMetrics, LatencyMetrics, TrendMetrics, PercentileMetrics {

    /**
     * Create input performance statistics from snapshots.
     * 
     * @param snapshots list of input performance snapshots
     * @param timeRange the time range for the statistics
     * @return the input performance statistics
     */
    public static InputPerformanceStatistics of(List<InputPerformanceSnapshot> snapshots, Duration timeRange) {
        if (snapshots.isEmpty()) {
            return empty(timeRange);
        }

        // Aggregate metrics
        long total = snapshots.stream().mapToLong(InputPerformanceSnapshot::total).sum();
        long success = snapshots.stream().mapToLong(InputPerformanceSnapshot::success).sum();
        long failure = snapshots.stream().mapToLong(InputPerformanceSnapshot::failure).sum();
        long totalDurationNanos = snapshots.stream().mapToLong(InputPerformanceSnapshot::totalDurationNanos).sum();
        long totalInputsProcessed = snapshots.stream().mapToLong(InputPerformanceSnapshot::totalInputsProcessed).sum();
        long totalInputsRouted = snapshots.stream().mapToLong(InputPerformanceSnapshot::totalInputsRouted).sum();

        // Calculate derived metrics
        double averageMs = total > 0 ? (totalDurationNanos / 1_000_000.0) / total : 0.0;
        double successRate = total > 0 ? (double) success / total : 0.0;
        double failureRate = total > 0 ? (double) failure / total : 0.0;
        double throughput = timeRange.toSeconds() > 0 ? (double) total / timeRange.toSeconds() : 0.0;
        double routingAccuracy = totalInputsProcessed > 0 ? (double) totalInputsRouted / totalInputsProcessed : 0.0;

        // Calculate trend (simplified - compare first half vs second half)
        double trend = calculateTrend(snapshots);
        
        // Calculate percentiles (simplified - using average for now)
        double p50Ms = averageMs;
        double p95Ms = averageMs * 1.5; // Simplified calculation
        double p99Ms = averageMs * 2.0; // Simplified calculation

        return new InputPerformanceStatistics(
            total, success, failure, totalDurationNanos, averageMs, successRate, failureRate,
            throughput, trend, p50Ms, p95Ms, p99Ms, totalInputsProcessed, totalInputsRouted, routingAccuracy, timeRange
        );
    }

    /**
     * Create empty input performance statistics.
     * 
     * @param timeRange the time range
     * @return empty input performance statistics
     */
    public static InputPerformanceStatistics empty(Duration timeRange) {
        return new InputPerformanceStatistics(
            0, 0, 0, 0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 0, 0.0, timeRange
        );
    }

    /**
     * Calculate trend from snapshots (simplified implementation).
     * 
     * @param snapshots list of snapshots
     * @return trend value
     */
    private static double calculateTrend(List<InputPerformanceSnapshot> snapshots) {
        if (snapshots.size() < 2) {
            return 0.0;
        }
        
        int mid = snapshots.size() / 2;
        double firstHalf = snapshots.subList(0, mid).stream()
            .mapToDouble(InputPerformanceSnapshot::averageMs)
            .average().orElse(0.0);
        double secondHalf = snapshots.subList(mid, snapshots.size()).stream()
            .mapToDouble(InputPerformanceSnapshot::averageMs)
            .average().orElse(0.0);
        
        return firstHalf > 0 ? (secondHalf - firstHalf) / firstHalf : 0.0;
    }

    @Override
    public long total() {
        return total;
    }

    @Override
    public long success() {
        return success;
    }

    @Override
    public long failure() {
        return failure;
    }

    @Override
    public long totalDurationNanos() {
        return totalDurationNanos;
    }

    @Override
    public double trend() {
        return trend;
    }

    @Override
    public double p50() {
        return p50Ms;
    }

    @Override
    public double p95() {
        return p95Ms;
    }

    @Override
    public double p99() {
        return p99Ms;
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

    /**
     * Get the time range.
     * 
     * @return the time range
     */
    public Duration timeRange() {
        return timeRange;
    }
}
