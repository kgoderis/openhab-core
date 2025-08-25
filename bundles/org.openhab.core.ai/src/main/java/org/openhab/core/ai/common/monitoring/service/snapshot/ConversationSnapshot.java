package org.openhab.core.ai.common.monitoring.service.snapshot;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.ConversationMetrics;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.service.statistics.StatisticsSnapshot;

@NonNullByDefault
public record ConversationSnapshot(long total, long success, long failure, long totalDurationNanos,
        double conversationQuality, double responseAccuracy, double conversationFlowRate,
        double conversationSatisfaction, double conversationLatency, double conversationThroughput,
        double conversationErrorRate, double conversationConfidence, double conversationCoverage,
        double conversationImpact,
        long timestampMs) implements StatisticsSnapshot, CountsMetrics, LatencyMetrics, ConversationMetrics {

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

    // ConversationMetrics implementation
    @Override
    public double conversationEfficiency() {
        return conversationEfficiency;
    }

    @Override
    public double conversationReliability() {
        return conversationReliability;
    }

    @Override
    public double conversationLatency() {
        return conversationLatency;
    }

    @Override
    public double conversationThroughput() {
        return conversationThroughput;
    }

    @Override
    public double conversationAccuracy() {
        return conversationAccuracy;
    }

    @Override
    public double conversationConsistency() {
        return conversationConsistency;
    }

    @Override
    public double conversationCompleteness() {
        return conversationCompleteness;
    }

    @Override
    public double conversationValidity() {
        return conversationValidity;
    }

    @Override
    public double conversationOptimization() {
        return conversationOptimization;
    }

    @Override
    public double conversationAdaptation() {
        return conversationAdaptation;
    }
}
