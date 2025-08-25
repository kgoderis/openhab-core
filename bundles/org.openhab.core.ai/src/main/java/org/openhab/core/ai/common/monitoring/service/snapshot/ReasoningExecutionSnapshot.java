package org.openhab.core.ai.common.monitoring.service.snapshot;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.ReasoningMetrics;
import org.openhab.core.ai.common.monitoring.service.statistics.StatisticsSnapshot;

@NonNullByDefault
public record ReasoningExecutionSnapshot(long total, long success, long failure, long totalDurationNanos,
        double reasoningAccuracy, double reasoningLatency, double reasoningComplexity, double reasoningEfficiency,
        double reasoningThroughput, double reasoningErrorRate, double reasoningConfidence, long reasoningStepCount,
        double reasoningBacktrackingRate, double reasoningOptimizationLevel,
        long timestampMs) implements StatisticsSnapshot, CountsMetrics, LatencyMetrics, ReasoningMetrics {

    // CountsMetrics implementation
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
    public double successRate() {
        if (total == 0) {
            return 0.0;
        }
        return (success * 100.0) / total;
    }

    // LatencyMetrics implementation
    @Override
    public long totalDurationNanos() {
        return totalDurationNanos;
    }

    @Override
    public double averageMs(long total) {
        if (total == 0) {
            return 0.0;
        }
        return totalDurationNanos / (total * 1_000_000.0);
    }

    // ReasoningMetrics implementation
    @Override
    public double reasoningAccuracy() {
        return reasoningAccuracy;
    }

    @Override
    public double reasoningLatency() {
        return reasoningLatency;
    }

    @Override
    public double reasoningComplexity() {
        return reasoningComplexity;
    }

    @Override
    public double reasoningEfficiency() {
        return reasoningEfficiency;
    }

    @Override
    public double reasoningThroughput() {
        return reasoningThroughput;
    }

    @Override
    public double reasoningErrorRate() {
        return reasoningErrorRate;
    }

    @Override
    public double reasoningConfidence() {
        return reasoningConfidence;
    }

    @Override
    public long reasoningStepCount() {
        return reasoningStepCount;
    }

    @Override
    public double reasoningBacktrackingRate() {
        return reasoningBacktrackingRate;
    }

    @Override
    public double reasoningOptimizationLevel() {
        return reasoningOptimizationLevel;
    }
}
