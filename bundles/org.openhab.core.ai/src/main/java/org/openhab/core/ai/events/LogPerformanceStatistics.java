package org.openhab.core.ai.events;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;

import java.time.Duration;
import java.util.List;

/**
 * Statistics for log performance over a time range.
 * 
 * <p>
 * This class provides aggregated statistics for log processing metrics including
 * ingestion counts, processing rates, timing information, trends, and percentiles.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record LogPerformanceStatistics(
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
    long totalLogsProcessed,
    long totalAnomaliesDetected,
    double processingRate,
    Duration timeRange
) implements CountsMetrics, LatencyMetrics, TrendMetrics, PercentileMetrics {

    /**
     * Create log performance statistics from snapshots.
     * 
     * @param snapshots list of log performance snapshots
     * @param timeRange the time range for the statistics
     * @return the log performance statistics
     */
    public static LogPerformanceStatistics of(List<LogPerformanceSnapshot> snapshots, Duration timeRange) {
        if (snapshots.isEmpty()) {
            return empty(timeRange);
        }

        // Aggregate metrics
        long total = snapshots.stream().mapToLong(LogPerformanceSnapshot::total).sum();
        long success = snapshots.stream().mapToLong(LogPerformanceSnapshot::success).sum();
        long failure = snapshots.stream().mapToLong(LogPerformanceSnapshot::failure).sum();
        long totalDurationNanos = snapshots.stream().mapToLong(LogPerformanceSnapshot::totalDurationNanos).sum();
        long totalLogsProcessed = snapshots.stream().mapToLong(LogPerformanceSnapshot::totalLogsProcessed).sum();
        long totalAnomaliesDetected = snapshots.stream().mapToLong(LogPerformanceSnapshot::totalAnomaliesDetected).sum();

        // Calculate derived metrics
        double averageMs = total > 0 ? (totalDurationNanos / 1_000_000.0) / total : 0.0;
        double successRate = total > 0 ? (double) success / total : 0.0;
        double failureRate = total > 0 ? (double) failure / total : 0.0;
        double throughput = timeRange.toSeconds() > 0 ? (double) total / timeRange.toSeconds() : 0.0;
        double processingRate = timeRange.toSeconds() > 0 ? (double) totalLogsProcessed / timeRange.toSeconds() : 0.0;

        // Calculate trend (simplified - compare first half vs second half)
        double trend = calculateTrend(snapshots);
        
        // Calculate percentiles (simplified - using average for now)
        double p50Ms = averageMs;
        double p95Ms = averageMs * 1.5; // Simplified calculation
        double p99Ms = averageMs * 2.0; // Simplified calculation

        return new LogPerformanceStatistics(
            total, success, failure, totalDurationNanos, averageMs, successRate, failureRate,
            throughput, trend, p50Ms, p95Ms, p99Ms, totalLogsProcessed, totalAnomaliesDetected, processingRate, timeRange
        );
    }

    /**
     * Create empty log performance statistics.
     * 
     * @param timeRange the time range
     * @return empty log performance statistics
     */
    public static LogPerformanceStatistics empty(Duration timeRange) {
        return new LogPerformanceStatistics(
            0, 0, 0, 0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 0, 0.0, timeRange
        );
    }

    /**
     * Calculate trend from snapshots (simplified implementation).
     * 
     * @param snapshots list of snapshots
     * @return trend value
     */
    private static double calculateTrend(List<LogPerformanceSnapshot> snapshots) {
        if (snapshots.size() < 2) {
            return 0.0;
        }
        
        int mid = snapshots.size() / 2;
        double firstHalf = snapshots.subList(0, mid).stream()
            .mapToDouble(LogPerformanceSnapshot::averageMs)
            .average().orElse(0.0);
        double secondHalf = snapshots.subList(mid, snapshots.size()).stream()
            .mapToDouble(LogPerformanceSnapshot::averageMs)
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

    /**
     * Get the time range.
     * 
     * @return the time range
     */
    public Duration timeRange() {
        return timeRange;
    }
}
