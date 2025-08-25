package org.openhab.core.ai.common.monitoring.service.snapshot;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CommunicationMetrics;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.service.statistics.StatisticsSnapshot;

@NonNullByDefault
public record CommunicationSnapshot(long total, long success, long failure, long totalDurationNanos,
        double communicationSuccessRate, double messageThroughput, double communicationLatency,
        double communicationReliability, double messageDeliveryRate, double communicationErrorRate,
        long messageQueueDepth, double bandwidthUtilization, double communicationRetryRate,
        double communicationTimeoutRate,
        long timestampMs) implements StatisticsSnapshot, CountsMetrics, LatencyMetrics, CommunicationMetrics {

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

    // CommunicationMetrics implementation
    @Override
    public double communicationSuccessRate() {
        return communicationSuccessRate;
    }

    @Override
    public double messageThroughput() {
        return messageThroughput;
    }

    @Override
    public double communicationLatency() {
        return communicationLatency;
    }

    @Override
    public double communicationReliability() {
        return communicationReliability;
    }

    @Override
    public double messageDeliveryRate() {
        return messageDeliveryRate;
    }

    @Override
    public double communicationErrorRate() {
        return communicationErrorRate;
    }

    @Override
    public long messageQueueDepth() {
        return messageQueueDepth;
    }

    @Override
    public double bandwidthUtilization() {
        return bandwidthUtilization;
    }

    @Override
    public double communicationRetryRate() {
        return communicationRetryRate;
    }

    @Override
    public double communicationTimeoutRate() {
        return communicationTimeoutRate;
    }

    /**
     * Create an empty communication snapshot.
     * 
     * @param communicationId the communication identifier
     * @return empty communication snapshot
     */
    public static CommunicationSnapshot empty(String communicationId) {
        return new CommunicationSnapshot(0L, 0L, 0L, 0L, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0L, 0.0, 0.0, 0.0,
                System.currentTimeMillis());
    }
}
