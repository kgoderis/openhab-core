package org.openhab.core.ai.common.monitoring.service.snapshot;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.AgentMetrics;
import org.openhab.core.ai.common.monitoring.api.Counts;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;
import org.openhab.core.ai.common.monitoring.api.Timing;

/**
 * Snapshot for agent task operations with capability interfaces.
 * 
 * <p>
 * This snapshot provides comprehensive metrics for agent task operations:
 * - Basic counting metrics (total, success, failure)
 * - Latency metrics (duration, averages)
 * - Agent-specific metrics (decision accuracy, learning rate)
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record AgentTaskSnapshot(Counts counts, Timing timing, long timestampMs, double decisionAccuracy,
        double learningRate) implements MetricsSnapshot, CountsMetrics, LatencyMetrics, AgentMetrics {

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
        if (total() == 0)
            return 0.0;
        return (success() * 100.0) / total();
    }

    // LatencyMetrics implementation
    @Override
    public long totalDurationNanos() {
        return timing.totalDurationNanos();
    }

    public double averageMs() {
        return LatencyMetrics.super.averageMs(total());
    }

    public double operationsPerSecond() {
        if (timing.totalDurationNanos() == 0)
            return 0.0;
        double durationSeconds = timing.totalDurationNanos() / 1_000_000_000.0;
        return total() / durationSeconds;
    }

    // AgentMetrics implementation
    public double decisionAccuracy() {
        return decisionAccuracy;
    }

    public double learningRate() {
        return learningRate;
    }

    // Note: successRate() is already implemented above for CountsMetrics
}
