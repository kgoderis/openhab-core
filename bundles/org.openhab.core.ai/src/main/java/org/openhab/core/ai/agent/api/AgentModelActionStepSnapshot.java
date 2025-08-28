package org.openhab.core.ai.agent.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;
import org.openhab.core.ai.common.monitoring.base.Counts;
import org.openhab.core.ai.common.monitoring.base.Timing;

/**
 * Snapshot of agent model action step metrics at a point in time.
 * 
 * <p>
 * This class provides an immutable view of action step metrics including execution counts,
 * success/failure rates, and timing information.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record AgentModelActionStepSnapshot(Counts counts, Timing timing, long timestampMs, String stepId, String actionId, int stepOrder)
        implements MetricsSnapshot, CountsMetrics, LatencyMetrics {

    /**
     * Create an agent model action step snapshot from individual metrics.
     * 
     * @param total total number of operations
     * @param success number of successful operations
     * @param failure number of failed operations
     * @param totalDurationNanos total duration in nanoseconds
     * @param stepId the step identifier
     * @param actionId the action identifier
     * @param stepOrder the step order
     * @return the agent model action step snapshot
     */
    public static AgentModelActionStepSnapshot of(long total, long success, long failure, long totalDurationNanos, String stepId, String actionId, int stepOrder) {
        return new AgentModelActionStepSnapshot(
            new Counts(total, success, failure),
            new Timing(totalDurationNanos),
            System.currentTimeMillis(),
            stepId,
            actionId,
            stepOrder
        );
    }

    /**
     * Create an empty agent model action step snapshot.
     * 
     * @param stepId the step identifier
     * @param actionId the action identifier
     * @param stepOrder the step order
     * @return an empty agent model action step snapshot
     */
    public static AgentModelActionStepSnapshot empty(String stepId, String actionId, int stepOrder) {
        return new AgentModelActionStepSnapshot(
            new Counts(0, 0, 0),
            new Timing(0),
            System.currentTimeMillis(),
            stepId,
            actionId,
            stepOrder
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
}
