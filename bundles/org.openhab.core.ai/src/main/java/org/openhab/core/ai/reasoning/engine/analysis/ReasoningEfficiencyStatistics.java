package org.openhab.core.ai.reasoning.engine.analysis;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;

import java.time.Duration;
import java.util.List;

/**
 * Statistics for reasoning efficiency over a time range.
 * 
 * <p>
 * This class provides aggregated statistics for reasoning efficiency metrics including
 * analysis counts, success/failure rates, timing information, trends, and percentiles.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record ReasoningEfficiencyStatistics(
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
    long totalReasoningSteps,
    long totalAnalysisOperations,
    double efficiencyScore,
    Duration timeRange
) implements CountsMetrics, LatencyMetrics, TrendMetrics, PercentileMetrics {

    /**
     * Create reasoning efficiency statistics from snapshots.
     * 
     * @param snapshots list of reasoning efficiency snapshots
     * @param timeRange the time range for the statistics
     * @return the reasoning efficiency statistics
     */
    public static ReasoningEfficiencyStatistics of(List<ReasoningEfficiencySnapshot> snapshots, Duration timeRange) {
        if (snapshots.isEmpty()) {
            return empty(timeRange);
        }

        // Aggregate metrics
        long total = snapshots.stream().mapToLong(ReasoningEfficiencySnapshot::total).sum();
        long success = snapshots.stream().mapToLong(ReasoningEfficiencySnapshot::success).sum();
        long failure = snapshots.stream().mapToLong(ReasoningEfficiencySnapshot::failure).sum();
        long totalDurationNanos = snapshots.stream().mapToLong(ReasoningEfficiencySnapshot::totalDurationNanos).sum();
        long totalReasoningSteps = snapshots.stream().mapToLong(ReasoningEfficiencySnapshot::totalReasoningSteps).sum();
        long totalAnalysisOperations = snapshots.stream().mapToLong(ReasoningEfficiencySnapshot::totalAnalysisOperations).sum();

        // Calculate derived metrics
        double averageMs = total > 0 ? (totalDurationNanos / 1_000_000.0) / total : 0.0;
        double successRate = total > 0 ? (double) success / total : 0.0;
        double failureRate = total > 0 ? (double) failure / total : 0.0;
        double throughput = timeRange.toSeconds() > 0 ? (double) total / timeRange.toSeconds() : 0.0;
        double efficiencyScore = totalReasoningSteps > 0 ? (double) totalAnalysisOperations / totalReasoningSteps : 0.0;

        // Calculate trend (simplified - compare first half vs second half)
        double trend = calculateTrend(snapshots);
        
        // Calculate percentiles (simplified - using average for now)
        double p50Ms = averageMs;
        double p95Ms = averageMs * 1.5; // Simplified calculation
        double p99Ms = averageMs * 2.0; // Simplified calculation

        return new ReasoningEfficiencyStatistics(
            total, success, failure, totalDurationNanos, averageMs, successRate, failureRate,
            throughput, trend, p50Ms, p95Ms, p99Ms, totalReasoningSteps, totalAnalysisOperations, efficiencyScore, timeRange
        );
    }

    /**
     * Create empty reasoning efficiency statistics.
     * 
     * @param timeRange the time range
     * @return empty reasoning efficiency statistics
     */
    public static ReasoningEfficiencyStatistics empty(Duration timeRange) {
        return new ReasoningEfficiencyStatistics(
            0, 0, 0, 0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 0, 0.0, timeRange
        );
    }

    /**
     * Calculate trend from snapshots (simplified implementation).
     * 
     * @param snapshots list of snapshots
     * @return trend value
     */
    private static double calculateTrend(List<ReasoningEfficiencySnapshot> snapshots) {
        if (snapshots.size() < 2) {
            return 0.0;
        }
        
        int mid = snapshots.size() / 2;
        double firstHalf = snapshots.subList(0, mid).stream()
            .mapToDouble(ReasoningEfficiencySnapshot::averageMs)
            .average().orElse(0.0);
        double secondHalf = snapshots.subList(mid, snapshots.size()).stream()
            .mapToDouble(ReasoningEfficiencySnapshot::averageMs)
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
     * Get the total number of reasoning steps.
     * 
     * @return total number of reasoning steps
     */
    public long totalReasoningSteps() {
        return totalReasoningSteps;
    }

    /**
     * Get the total number of analysis operations.
     * 
     * @return total number of analysis operations
     */
    public long totalAnalysisOperations() {
        return totalAnalysisOperations;
    }

    /**
     * Get the efficiency score (0.0 to 1.0).
     * 
     * @return efficiency score
     */
    public double efficiencyScore() {
        return efficiencyScore;
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