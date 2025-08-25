package org.openhab.core.ai.common.monitoring.service.snapshot;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.Counts;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;
import org.openhab.core.ai.common.monitoring.api.ModelMetrics;
import org.openhab.core.ai.common.monitoring.api.Timing;

/**
 * Snapshot for model completion operations with capability interfaces.
 * 
 * <p>
 * This snapshot provides comprehensive metrics for model completion operations:
 * - Basic counting metrics (total, success, failure)
 * - Latency metrics (duration, averages)
 * - Model-specific metrics (tokens, cost, throughput)
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record ModelCompletionSnapshot(Counts counts, Timing timing, long timestampMs, long totalTokens,
        double totalCost) implements MetricsSnapshot, CountsMetrics, LatencyMetrics, ModelMetrics {

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

    // ModelMetrics implementation
    public double tokensPerSecond() {
        if (total() == 0 || timing.totalDurationNanos() == 0)
            return 0.0;
        double durationSeconds = timing.totalDurationNanos() / 1_000_000_000.0;
        return totalTokens / durationSeconds;
    }

    public double costPerRequest() {
        if (total() == 0)
            return 0.0;
        return totalCost / total();
    }

    public double averageTokensPerRequest() {
        if (total() == 0)
            return 0.0;
        return (double) totalTokens / total();
    }
}
