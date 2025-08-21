package org.openhab.core.ai.common.monitoring.snapshot;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.Counts;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;
import org.openhab.core.ai.common.monitoring.api.Timing;

/**
 * Immutable snapshot of delegation metrics.
 * 
 * <p>
 * This record provides a thread-safe snapshot of delegation metrics at a specific
 * point in time. It extends ExecutionMetricsSnapshot with delegation-specific
 * fields.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record DelegationMetricsSnapshot(Counts counts, Timing timing, long timestampMs,
        int registeredAgents) implements MetricsSnapshot, CountsMetrics, LatencyMetrics {

    public long total() {
        return counts.total();
    }

    public long success() {
        return counts.success();
    }

    public long failure() {
        return counts.failure();
    }

    public long totalDurationNanos() {
        return timing.totalDurationNanos();
    }

    public double averageMs() {
        return LatencyMetrics.super.averageMs(total());
    }

    public long getTotalDelegations() {
        return total();
    }

    public long getSuccessfulDelegations() {
        return success();
    }

    public long getFailedDelegations() {
        return failure();
    }

    public long getTotalDelegationTime() {
        return totalDurationNanos();
    }

    public int getRegisteredAgents() {
        return registeredAgents;
    }

    public double getSuccessRate() {
        return successRate();
    }

    public double getAverageDelegationTime() {
        return averageMs();
    }
}
