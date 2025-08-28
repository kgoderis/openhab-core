package org.openhab.core.ai.agent.lifecycle;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.Counts;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;
import org.openhab.core.ai.common.monitoring.api.Timing;

/**
 * Immutable snapshot of default agent metrics data.
 * 
 * <p>
 * This class provides a point-in-time view of agent performance metrics
 * including execution counts, success/failure rates, and timing information.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record DefaultAgentSnapshot(Counts counts, Timing timing, long timestampMs, String agentId,
        long totalExecutions) implements MetricsSnapshot, CountsMetrics, LatencyMetrics {

    /**
     * Create a new DefaultAgentSnapshot instance.
     * 
     * @param counts the execution counts
     * @param timing the timing information
     * @param timestampMs the timestamp in milliseconds
     * @param agentId the agent ID
     * @param totalExecutions the total number of executions
     * @return new DefaultAgentSnapshot instance
     */
    public static DefaultAgentSnapshot of(Counts counts, Timing timing, long timestampMs, String agentId,
            long totalExecutions) {
        return new DefaultAgentSnapshot(counts, timing, timestampMs, agentId, totalExecutions);
    }

    /**
     * Create an empty DefaultAgentSnapshot instance.
     * 
     * @param agentId the agent ID
     * @return empty DefaultAgentSnapshot instance
     */
    public static DefaultAgentSnapshot empty(String agentId) {
        return new DefaultAgentSnapshot(new Counts(0, 0, 0), new Timing(0), System.currentTimeMillis(), agentId, 0);
    }

    // CountsMetrics implementation
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
    public double successRate() {
        return CountsMetrics.super.successRate();
    }

    // LatencyMetrics implementation
    @Override
    public long totalDurationNanos() {
        return timing.totalDurationNanos();
    }

    public double averageMs() {
        return total() > 0 ? (double) totalDurationNanos() / total() / 1_000_000.0 : 0.0;
    }

    /**
     * Get the agent ID.
     * 
     * @return the agent ID
     */
    public String agentId() {
        return agentId;
    }

    /**
     * Get the total number of executions.
     * 
     * @return the total executions
     */
    public long totalExecutions() {
        return totalExecutions;
    }

    /**
     * Get the execution efficiency as a percentage.
     * 
     * @return efficiency between 0.0 and 100.0
     */
    public double executionEfficiency() {
        return total() > 0 ? (double) success() / total() * 100.0 : 0.0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DefaultAgentSnapshot that = (DefaultAgentSnapshot) obj;
        return timestampMs == that.timestampMs && totalExecutions == that.totalExecutions
                && Objects.equals(counts, that.counts) && Objects.equals(timing, that.timing)
                && Objects.equals(agentId, that.agentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(counts, timing, timestampMs, agentId, totalExecutions);
    }

    @Override
    public String toString() {
        return String.format(
                "DefaultAgentSnapshot{agentId=%s, total=%d, success=%d, failure=%d, avgMs=%.2f, efficiency=%.1f%%, totalExecutions=%d}",
                agentId, total(), success(), failure(), averageMs(), executionEfficiency(), totalExecutions);
    }
}
