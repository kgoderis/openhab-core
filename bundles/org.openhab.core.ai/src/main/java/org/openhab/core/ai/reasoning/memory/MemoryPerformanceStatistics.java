package org.openhab.core.ai.reasoning.memory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;

import java.time.Duration;
import java.util.List;

/**
 * Statistics for memory performance over a time range.
 * 
 * <p>
 * This class provides aggregated statistics for memory management metrics including
 * store/retrieve counts, consolidation rates, timing information, trends, and percentiles.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record MemoryPerformanceStatistics(
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
    long totalMemoryOperations,
    long totalMemoryConsolidations,
    double memoryUtilization,
    Duration timeRange
) implements CountsMetrics, LatencyMetrics, TrendMetrics, PercentileMetrics {

    /**
     * Create memory performance statistics from snapshots.
     * 
     * @param snapshots list of memory performance snapshots
     * @param timeRange the time range for the statistics
     * @return the memory performance statistics
     */
    public static MemoryPerformanceStatistics of(List<MemoryPerformanceSnapshot> snapshots, Duration timeRange) {
        if (snapshots.isEmpty()) {
            return empty(timeRange);
        }

        // Aggregate metrics
        long total = snapshots.stream().mapToLong(MemoryPerformanceSnapshot::total).sum();
        long success = snapshots.stream().mapToLong(MemoryPerformanceSnapshot::success).sum();
        long failure = snapshots.stream().mapToLong(MemoryPerformanceSnapshot::failure).sum();
        long totalDurationNanos = snapshots.stream().mapToLong(MemoryPerformanceSnapshot::totalDurationNanos).sum();
        long totalMemoryOperations = snapshots.stream().mapToLong(MemoryPerformanceSnapshot::totalMemoryOperations).sum();
        long totalMemoryConsolidations = snapshots.stream().mapToLong(MemoryPerformanceSnapshot::totalMemoryConsolidations).sum();

        // Calculate derived metrics
        double averageMs = total > 0 ? (totalDurationNanos / 1_000_000.0) / total : 0.0;
        double successRate = total > 0 ? (double) success / total : 0.0;
        double failureRate = total > 0 ? (double) failure / total : 0.0;
        double throughput = timeRange.toSeconds() > 0 ? (double) total / timeRange.toSeconds() : 0.0;
        double memoryUtilization = totalMemoryOperations > 0 ? (double) totalMemoryConsolidations / totalMemoryOperations : 0.0;

        // Calculate trend (simplified - compare first half vs second half)
        double trend = calculateTrend(snapshots);
        
        // Calculate percentiles (simplified - using average for now)
        double p50Ms = averageMs;
        double p95Ms = averageMs * 1.5; // Simplified calculation
        double p99Ms = averageMs * 2.0; // Simplified calculation

        return new MemoryPerformanceStatistics(
            total, success, failure, totalDurationNanos, averageMs, successRate, failureRate,
            throughput, trend, p50Ms, p95Ms, p99Ms, totalMemoryOperations, totalMemoryConsolidations, memoryUtilization, timeRange
        );
    }

    /**
     * Create empty memory performance statistics.
     * 
     * @param timeRange the time range
     * @return empty memory performance statistics
     */
    public static MemoryPerformanceStatistics empty(Duration timeRange) {
        return new MemoryPerformanceStatistics(
            0, 0, 0, 0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 0, 0.0, timeRange
        );
    }

    /**
     * Calculate trend from snapshots (simplified implementation).
     * 
     * @param snapshots list of snapshots
     * @return trend value
     */
    private static double calculateTrend(List<MemoryPerformanceSnapshot> snapshots) {
        if (snapshots.size() < 2) {
            return 0.0;
        }
        
        int mid = snapshots.size() / 2;
        double firstHalf = snapshots.subList(0, mid).stream()
            .mapToDouble(MemoryPerformanceSnapshot::averageMs)
            .average().orElse(0.0);
        double secondHalf = snapshots.subList(mid, snapshots.size()).stream()
            .mapToDouble(MemoryPerformanceSnapshot::averageMs)
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
     * Get the total number of memory operations.
     * 
     * @return total number of memory operations
     */
    public long totalMemoryOperations() {
        return totalMemoryOperations;
    }

    /**
     * Get the total number of memory consolidations.
     * 
     * @return total number of memory consolidations
     */
    public long totalMemoryConsolidations() {
        return totalMemoryConsolidations;
    }

    /**
     * Get the memory utilization percentage.
     * 
     * @return memory utilization percentage
     */
    public double memoryUtilization() {
        return memoryUtilization;
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