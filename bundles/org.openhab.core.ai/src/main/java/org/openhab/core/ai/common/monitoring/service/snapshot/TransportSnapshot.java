package org.openhab.core.ai.common.monitoring.service.snapshot;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.TransportMetrics;
import org.openhab.core.ai.common.monitoring.service.statistics.StatisticsSnapshot;

@NonNullByDefault
public record TransportSnapshot(long total, long success, long failure, long totalDurationNanos,
        double transportReliability, double transportThroughput, double transportLatency, double transportEfficiency,
        double transportErrorRate, double connectionSuccessRate, double packetLossRate,
        double transportBandwidthUtilization, double transportRetryRate, double transportTimeoutRate,
        long timestampMs) implements StatisticsSnapshot, CountsMetrics, LatencyMetrics, TransportMetrics {

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

    // TransportMetrics implementation
    @Override
    public double transportReliability() {
        return transportReliability;
    }

    @Override
    public double transportThroughput() {
        return transportThroughput;
    }

    @Override
    public double transportLatency() {
        return transportLatency;
    }

    @Override
    public double transportEfficiency() {
        return transportEfficiency;
    }

    @Override
    public double transportErrorRate() {
        return transportErrorRate;
    }

    @Override
    public double connectionSuccessRate() {
        return connectionSuccessRate;
    }

    @Override
    public double packetLossRate() {
        return packetLossRate;
    }

    @Override
    public double transportBandwidthUtilization() {
        return transportBandwidthUtilization;
    }

    @Override
    public double transportRetryRate() {
        return transportRetryRate;
    }

    @Override
    public double transportTimeoutRate() {
        return transportTimeoutRate;
    }
}
