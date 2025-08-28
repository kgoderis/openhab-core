package org.openhab.core.ai.agent.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;

import java.time.Duration;
import java.util.List;

/**
 * Statistics for agent model action step performance over a time range.
 * 
 * <p>
 * This class provides aggregated statistics for action step metrics including
 * execution counts, success/failure rates, timing information, trends, and percentiles.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record AgentModelActionStepStatistics(
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
    String stepId,
    String actionId,
    int stepOrder,
    Duration timeRange
) implements CountsMetrics, LatencyMetrics, TrendMetrics, PercentileMetrics {

    /**
     * Create agent model action step statistics from snapshots.
     * 
     * @param snapshots list of action step snapshots
     * @param timeRange the time range for the statistics
     * @return the agent model action step statistics
     */
    public static AgentModelActionStepStatistics of(List<AgentModelActionStepSnapshot> snapshots, Duration timeRange) {
        if (snapshots.isEmpty()) {
            return empty("unknown", "unknown", 0, timeRange);
        }

        // Use the first snapshot for step metadata
        AgentModelActionStepSnapshot first = snapshots.get(0);
        String stepId = first.stepId();
        String actionId = first.actionId();
        int stepOrder = first.stepOrder();

        // Aggregate metrics
        long total = snapshots.stream().mapToLong(AgentModelActionStepSnapshot::total).sum();
        long success = snapshots.stream().mapToLong(AgentModelActionStepSnapshot::success).sum();
        long failure = snapshots.stream().mapToLong(AgentModelActionStepSnapshot::failure).sum();
        long totalDurationNanos = snapshots.stream().mapToLong(AgentModelActionStepSnapshot::totalDurationNanos).sum();

        // Calculate derived metrics
        double averageMs = total > 0 ? (totalDurationNanos / 1_000_000.0) / total : 0.0;
        double successRate = total > 0 ? (double) success / total : 0.0;
        double failureRate = total > 0 ? (double) failure / total : 0.0;
        double throughput = timeRange.toSeconds() > 0 ? (double) total / timeRange.toSeconds() : 0.0;

        // Calculate trend (simplified - compare first half vs second half)
        double trend = calculateTrend(snapshots);
        
        // Calculate percentiles (simplified - using average for now)
        double p50Ms = averageMs;
        double p95Ms = averageMs * 1.5; // Simplified calculation
        double p99Ms = averageMs * 2.0; // Simplified calculation

        return new AgentModelActionStepStatistics(
            total, success, failure, totalDurationNanos, averageMs, successRate, failureRate,
            throughput, trend, p50Ms, p95Ms, p99Ms, stepId, actionId, stepOrder, timeRange
        );
    }

    /**
     * Create empty agent model action step statistics.
     * 
     * @param stepId the step identifier
     * @param actionId the action identifier
     * @param stepOrder the step order
     * @param timeRange the time range
     * @return empty agent model action step statistics
     */
    public static AgentModelActionStepStatistics empty(String stepId, String actionId, int stepOrder, Duration timeRange) {
        return new AgentModelActionStepStatistics(
            0, 0, 0, 0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, stepId, actionId, stepOrder, timeRange
        );
    }

    /**
     * Calculate trend from snapshots (simplified implementation).
     * 
     * @param snapshots list of snapshots
     * @return trend value
     */
    private static double calculateTrend(List<AgentModelActionStepSnapshot> snapshots) {
        if (snapshots.size() < 2) {
            return 0.0;
        }
        
        int mid = snapshots.size() / 2;
        double firstHalf = snapshots.subList(0, mid).stream()
            .mapToDouble(AgentModelActionStepSnapshot::averageMs)
            .average().orElse(0.0);
        double secondHalf = snapshots.subList(mid, snapshots.size()).stream()
            .mapToDouble(AgentModelActionStepSnapshot::averageMs)
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
     * Get the step identifier.
     * 
     * @return the step identifier
     */
    public String stepId() {
        return stepId;
    }

    /**
     * Get the action identifier.
     * 
     * @return the action identifier
     */
    public String actionId() {
        return actionId;
    }

    /**
     * Get the step order.
     * 
     * @return the step order
     */
    public int stepOrder() {
        return stepOrder;
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
