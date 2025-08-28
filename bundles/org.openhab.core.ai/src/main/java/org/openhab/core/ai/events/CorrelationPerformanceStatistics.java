package org.openhab.core.ai.events;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;

import java.time.Duration;
import java.util.List;

/**
 * Statistics for correlation performance over a time range.
 * 
 * <p>
 * This class provides aggregated statistics for event correlation metrics including
 * correlation counts, success/failure rates, timing information, trends, and percentiles.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record CorrelationPerformanceStatistics(
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
    long totalCorrelations,
    long totalPatternsDetected,
    double correlationAccuracy,
    Duration timeRange
) implements CountsMetrics, LatencyMetrics, TrendMetrics, PercentileMetrics {

    /**
     * Create correlation performance statistics from snapshots.
     * 
     * @param snapshots list of correlation performance snapshots
     * @param timeRange the time range for the statistics
     * @return the correlation performance statistics
     */
    public static CorrelationPerformanceStatistics of(List<CorrelationPerformanceSnapshot> snapshots, Duration timeRange) {
        if (snapshots.isEmpty()) {
            return empty(timeRange);
        }

        // Aggregate metrics
        long total = snapshots.stream().mapToLong(CorrelationPerformanceSnapshot::total).sum();
        long success = snapshots.stream().mapToLong(CorrelationPerformanceSnapshot::success).sum();
        long failure = snapshots.stream().mapToLong(CorrelationPerformanceSnapshot::failure).sum();
        long totalDurationNanos = snapshots.stream().mapToLong(CorrelationPerformanceSnapshot::totalDurationNanos).sum();
        long totalCorrelations = snapshots.stream().mapToLong(CorrelationPerformanceSnapshot::totalCorrelations).sum();
        long totalPatternsDetected = snapshots.stream().mapToLong(CorrelationPerformanceSnapshot::totalPatternsDetected).sum();

        // Calculate derived metrics
        double averageMs = total > 0 ? (totalDurationNanos / 1_000_000.0) / total : 0.0;
        double successRate = total > 0 ? (double) success / total : 0.0;
        double failureRate = total > 0 ? (double) failure / total : 0.0;
        double throughput = timeRange.toSeconds() > 0 ? (double) total / timeRange.toSeconds() : 0.0;
        double correlationAccuracy = totalCorrelations > 0 ? (double) totalPatternsDetected / totalCorrelations : 0.0;

        // Calculate trend (simplified - compare first half vs second half)
        double trend = calculateTrend(snapshots);
        
        // Calculate percentiles (simplified - using average for now)
        double p50Ms = averageMs;
        double p95Ms = averageMs * 1.5; // Simplified calculation
        double p99Ms = averageMs * 2.0; // Simplified calculation

        return new CorrelationPerformanceStatistics(
            total, success, failure, totalDurationNanos, averageMs, successRate, failureRate,
            throughput, trend, p50Ms, p95Ms, p99Ms, totalCorrelations, totalPatternsDetected, correlationAccuracy, timeRange
        );
    }

    /**
     * Create empty correlation performance statistics.
     * 
     * @param timeRange the time range
     * @return empty correlation performance statistics
     */
    public static CorrelationPerformanceStatistics empty(Duration timeRange) {
        return new CorrelationPerformanceStatistics(
            0, 0, 0, 0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 0, 0.0, timeRange
        );
    }

    /**
     * Calculate trend from snapshots (simplified implementation).
     * 
     * @param snapshots list of snapshots
     * @return trend value
     */
    private static double calculateTrend(List<CorrelationPerformanceSnapshot> snapshots) {
        if (snapshots.size() < 2) {
            return 0.0;
        }
        
        int mid = snapshots.size() / 2;
        double firstHalf = snapshots.subList(0, mid).stream()
            .mapToDouble(CorrelationPerformanceSnapshot::averageMs)
            .average().orElse(0.0);
        double secondHalf = snapshots.subList(mid, snapshots.size()).stream()
            .mapToDouble(CorrelationPerformanceSnapshot::averageMs)
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

    /**
     * Get the time range.
     * 
     * @return the time range
     */
    public Duration timeRange() {
        return timeRange;
    }
}