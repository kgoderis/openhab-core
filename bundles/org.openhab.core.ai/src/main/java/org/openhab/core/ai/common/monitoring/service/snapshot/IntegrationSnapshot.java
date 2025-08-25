package org.openhab.core.ai.common.monitoring.service.snapshot;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.IntegrationMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.service.statistics.StatisticsSnapshot;

@NonNullByDefault
public record IntegrationSnapshot(long total, long success, long failure, long totalDurationNanos,
        double integrationSuccessRate, double integrationEfficiency, double integrationReliability,
        double integrationLatency, double integrationThroughput, double integrationAccuracy,
        double integrationConsistency, double integrationCompleteness, double integrationValidity,
        double integrationOptimization,
        long timestampMs) implements StatisticsSnapshot, CountsMetrics, LatencyMetrics, IntegrationMetrics {

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

    // IntegrationMetrics implementation
    @Override
    public double integrationSuccessRate() {
        return integrationSuccessRate;
    }

    @Override
    public double integrationEfficiency() {
        return integrationEfficiency;
    }

    @Override
    public double integrationReliability() {
        return integrationReliability;
    }

    @Override
    public double integrationLatency() {
        return integrationLatency;
    }

    @Override
    public double integrationThroughput() {
        return integrationThroughput;
    }

    @Override
    public double integrationAccuracy() {
        return integrationAccuracy;
    }

    @Override
    public double integrationConsistency() {
        return integrationConsistency;
    }

    @Override
    public double integrationCompleteness() {
        return integrationCompleteness;
    }

    @Override
    public double integrationValidity() {
        return integrationValidity;
    }

    @Override
    public double integrationOptimization() {
        return integrationOptimization;
    }
}
