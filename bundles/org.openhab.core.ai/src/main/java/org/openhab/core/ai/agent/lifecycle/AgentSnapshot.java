package org.openhab.core.ai.agent.lifecycle;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;
import org.openhab.core.ai.common.monitoring.base.Counts;
import org.openhab.core.ai.common.monitoring.base.Timing;

/**
 * Snapshot of agent metrics at a point in time.
 * 
 * <p>
 * This class provides an immutable view of agent metrics including execution counts,
 * success/failure rates, and timing information.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record AgentSnapshot(Counts counts, Timing timing, long timestampMs, String agentId, long totalExecutions)
        implements MetricsSnapshot, CountsMetrics, LatencyMetrics {

    /**
     * Create an agent snapshot from individual metrics.
     * 
     * @param total total number of operations
     * @param success number of successful operations
     * @param failure number of failed operations
     * @param totalDurationNanos total duration in nanoseconds
     * @param agentId the agent identifier
     * @param totalExecutions total number of executions
     * @return the agent snapshot
     */
    public static AgentSnapshot of(long total, long success, long failure, long totalDurationNanos, String agentId, long totalExecutions) {
        return new AgentSnapshot(
            new Counts(total, success, failure),
            new Timing(totalDurationNanos),
            System.currentTimeMillis(),
            agentId,
            totalExecutions
        );
    }

    /**
     * Create an empty agent snapshot.
     * 
     * @param agentId the agent identifier
     * @return an empty agent snapshot
     */
    public static AgentSnapshot empty(String agentId) {
        return new AgentSnapshot(
            new Counts(0, 0, 0),
            new Timing(0),
            System.currentTimeMillis(),
            agentId,
            0
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
     * Get the average execution time in milliseconds.
     * 
     * @return average execution time in milliseconds
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
     * Get the total number of executions.
     * 
     * @return total number of executions
     */
    public long totalExecutions() {
        return totalExecutions;
    }
}
