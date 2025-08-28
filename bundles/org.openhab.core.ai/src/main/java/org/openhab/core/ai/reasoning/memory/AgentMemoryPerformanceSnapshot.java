package org.openhab.core.ai.reasoning.memory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;
import org.openhab.core.ai.common.monitoring.base.Counts;
import org.openhab.core.ai.common.monitoring.base.Timing;

/**
 * Snapshot of agent memory performance metrics at a point in time.
 * 
 * <p>
 * This class provides an immutable view of agent-specific memory metrics including
 * agent memory operations, context management, and timing information.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record AgentMemoryPerformanceSnapshot(Counts counts, Timing timing, long timestampMs, String agentId, long totalAgentMemoryOperations, long totalContextSwitches, double contextRetentionRate)
        implements MetricsSnapshot, CountsMetrics, LatencyMetrics {

    /**
     * Create an agent memory performance snapshot from individual metrics.
     * 
     * @param total total number of operations
     * @param success number of successful operations
     * @param failure number of failed operations
     * @param totalDurationNanos total duration in nanoseconds
     * @param agentId the agent identifier
     * @param totalAgentMemoryOperations total number of agent memory operations
     * @param totalContextSwitches total number of context switches
     * @param contextRetentionRate context retention rate percentage
     * @return the agent memory performance snapshot
     */
    public static AgentMemoryPerformanceSnapshot of(long total, long success, long failure, long totalDurationNanos, String agentId, long totalAgentMemoryOperations, long totalContextSwitches, double contextRetentionRate) {
        return new AgentMemoryPerformanceSnapshot(
            new Counts(total, success, failure),
            new Timing(totalDurationNanos),
            System.currentTimeMillis(),
            agentId,
            totalAgentMemoryOperations,
            totalContextSwitches,
            contextRetentionRate
        );
    }

    /**
     * Create an empty agent memory performance snapshot.
     * 
     * @param agentId the agent identifier
     * @return an empty agent memory performance snapshot
     */
    public static AgentMemoryPerformanceSnapshot empty(String agentId) {
        return new AgentMemoryPerformanceSnapshot(
            new Counts(0, 0, 0),
            new Timing(0),
            System.currentTimeMillis(),
            agentId,
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
     * Get the average operation time in milliseconds.
     * 
     * @return average operation time in milliseconds
     */
    public double averageMs() {
        return LatencyMetrics.super.averageMs(total());
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
}
