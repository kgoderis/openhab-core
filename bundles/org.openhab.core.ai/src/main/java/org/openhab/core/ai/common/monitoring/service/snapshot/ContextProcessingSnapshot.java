package org.openhab.core.ai.common.monitoring.service.snapshot;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.ContextMetrics;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.service.statistics.StatisticsSnapshot;

@NonNullByDefault
public record ContextProcessingSnapshot(long total, long success, long failure, long totalDurationNanos,
        double contextRelevanceScore, double contextProcessingEfficiency, double contextAccuracy,
        double contextAdaptationRate, double contextProcessingLatency, double contextThroughput,
        double contextErrorRate, double contextConsistencyRate, double contextCompleteness, double contextConfidence,
        long timestampMs) implements StatisticsSnapshot, CountsMetrics, LatencyMetrics, ContextMetrics {

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

    // ContextMetrics implementation
    @Override
    public double contextRelevanceScore() {
        return contextRelevanceScore;
    }

    @Override
    public double contextProcessingEfficiency() {
        return contextProcessingEfficiency;
    }

    @Override
    public double contextAccuracy() {
        return contextAccuracy;
    }

    @Override
    public double contextAdaptationRate() {
        return contextAdaptationRate;
    }

    @Override
    public double contextProcessingLatency() {
        return contextProcessingLatency;
    }

    @Override
    public double contextThroughput() {
        return contextThroughput;
    }

    @Override
    public double contextErrorRate() {
        return contextErrorRate;
    }

    @Override
    public double contextConsistencyRate() {
        return contextConsistencyRate;
    }

    @Override
    public double contextCompleteness() {
        return contextCompleteness;
    }

    @Override
    public double contextConfidence() {
        return contextConfidence;
    }
}
