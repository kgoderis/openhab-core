package org.openhab.core.ai.common.monitoring.service.snapshot;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.MessagingMetrics;
import org.openhab.core.ai.common.monitoring.service.statistics.StatisticsSnapshot;

@NonNullByDefault
public record MessagingSnapshot(long total, long success, long failure, long totalDurationNanos,
        double messageDeliverySuccessRate, double messagingThroughput, double messagingLatency,
        double messagingReliability, long messageQueueDepth, double messagingErrorRate,
        double messageAcknowledgmentRate, double messagingRetryRate, double messagingTimeoutRate,
        double messagingConfidence,
        long timestampMs) implements StatisticsSnapshot, CountsMetrics, LatencyMetrics, MessagingMetrics {

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

    // MessagingMetrics implementation
    @Override
    public double messageDeliverySuccessRate() {
        return messageDeliverySuccessRate;
    }

    @Override
    public double messagingThroughput() {
        return messagingThroughput;
    }

    @Override
    public double messagingLatency() {
        return messagingLatency;
    }

    @Override
    public double messagingReliability() {
        return messagingReliability;
    }

    @Override
    public long messageQueueDepth() {
        return messageQueueDepth;
    }

    @Override
    public double messagingErrorRate() {
        return messagingErrorRate;
    }

    @Override
    public double messageAcknowledgmentRate() {
        return messageAcknowledgmentRate;
    }

    @Override
    public double messagingRetryRate() {
        return messagingRetryRate;
    }

    @Override
    public double messagingTimeoutRate() {
        return messagingTimeoutRate;
    }

    @Override
    public double messagingConfidence() {
        return messagingConfidence;
    }
}
