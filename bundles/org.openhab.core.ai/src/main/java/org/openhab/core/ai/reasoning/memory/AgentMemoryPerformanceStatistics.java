package org.openhab.core.ai.reasoning.memory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;

import java.time.Duration;
import java.util.List;

/**
 * Statistics for agent memory performance over a time range.
 * 
 * <p>
 * This class provides aggregated statistics for agent-specific memory metrics including
 * agent memory operations, context management, timing information, trends, and percentiles.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record AgentMemoryPerformanceStatistics(
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
    String agentId,
    long totalAgentMemoryOperations,
    long totalContextSwitches,
    double contextRetentionRate,
    Duration timeRange
) implements CountsMetrics, LatencyMetrics, TrendMetrics, PercentileMetrics {

    /**
     * Create agent memory performance statistics from snapshots.
     * 
     * @param snapshots list of agent memory performance snapshots
     * @param timeRange the time range for the statistics
     * @return the agent memory performance statistics
     */
    public static AgentMemoryPerformanceStatistics of(List<AgentMemoryPerformanceSnapshot> snapshots, Duration timeRange) {
        if (snapshots.isEmpty()) {
            return empty("unknown", timeRange);
        }

        // Use the first snapshot for agent metadata
        AgentMemoryPerformanceSnapshot first = snapshots.get(0);
        String agentId = first.agentId();

        // Aggregate metrics
        long total = snapshots.stream().mapToLong(AgentMemoryPerformanceSnapshot::total).sum();
        long success = snapshots.stream().mapToLong(AgentMemoryPerformanceSnapshot::success).sum();
        long failure = snapshots.stream().mapToLong(AgentMemoryPerformanceSnapshot::failure).sum();
        long totalDurationNanos = snapshots.stream().mapToLong(AgentMemoryPerformanceSnapshot::totalDurationNanos).sum();
        long totalAgentMemoryOperations = snapshots.stream().mapToLong(AgentMemoryPerformanceSnapshot::totalAgentMemoryOperations).sum();
        long totalContextSwitches = snapshots.stream().mapToLong(AgentMemoryPerformanceSnapshot::totalContextSwitches).sum();

        // Calculate derived metrics
        double averageMs = total > 0 ? (totalDurationNanos / 1_000_000.0) / total : 0.0;
        double successRate = total > 0 ? (double) success / total : 0.0;
        double failureRate = total > 0 ? (double) failure / total : 0.0;
        double throughput = timeRange.toSeconds() > 0 ? (double) total / timeRange.toSeconds() : 0.0;
        double contextRetentionRate = totalAgentMemoryOperations > 0 ? (double) totalContextSwitches / totalAgentMemoryOperations : 0.0;

        // Calculate trend (simplified - compare first half vs second half)
        double trend = calculateTrend(snapshots);
        
        // Calculate percentiles (simplified - using average for now)
        double p50Ms = averageMs;
        double p95Ms = averageMs * 1.5; // Simplified calculation
        double p99Ms = averageMs * 2.0; // Simplified calculation

        return new AgentMemoryPerformanceStatistics(
            total, success, failure, totalDurationNanos, averageMs, successRate, failureRate,
            throughput, trend, p50Ms, p95Ms, p99Ms, agentId, totalAgentMemoryOperations, totalContextSwitches, contextRetentionRate, timeRange
        );
    }

    /**
     * Create empty agent memory performance statistics.
     * 
     * @param agentId the agent identifier
     * @param timeRange the time range
     * @return empty agent memory performance statistics
     */
    public static AgentMemoryPerformanceStatistics empty(String agentId, Duration timeRange) {
        return new AgentMemoryPerformanceStatistics(
            0, 0, 0, 0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, agentId, 0, 0, 0.0, timeRange
        );
    }

    /**
     * Calculate trend from snapshots (simplified implementation).
     * 
     * @param snapshots list of snapshots
     * @return trend value
     */
    private static double calculateTrend(List<AgentMemoryPerformanceSnapshot> snapshots) {
        if (snapshots.size() < 2) {
            return 0.0;
        }
        
        int mid = snapshots.size() / 2;
        double firstHalf = snapshots.subList(0, mid).stream()
            .mapToDouble(AgentMemoryPerformanceSnapshot::averageMs)
            .average().orElse(0.0);
        double secondHalf = snapshots.subList(mid, snapshots.size()).stream()
            .mapToDouble(AgentMemoryPerformanceSnapshot::averageMs)
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
     * Get the agent identifier.
     * 
     * @return the agent identifier
     */
    public String agentId() {
        return agentId;
    }

    /**
     * Get the total number of agent memory operations.
     * 
     * @return total number of agent memory operations
     */
    public long totalAgentMemoryOperations() {
        return totalAgentMemoryOperations;
    }

    /**
     * Get the total number of context switches.
     * 
     * @return total number of context switches
     */
    public long totalContextSwitches() {
        return totalContextSwitches;
    }

    /**
     * Get the context retention rate percentage.
     * 
     * @return context retention rate percentage
     */
    public double contextRetentionRate() {
        return contextRetentionRate;
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