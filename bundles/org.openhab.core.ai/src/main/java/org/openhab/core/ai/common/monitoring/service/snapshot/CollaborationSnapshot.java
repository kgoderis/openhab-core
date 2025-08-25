package org.openhab.core.ai.common.monitoring.service.snapshot;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CollaborationMetrics;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.service.statistics.StatisticsSnapshot;

@NonNullByDefault
public record CollaborationSnapshot(long total, long success, long failure, long totalDurationNanos,
        double collaborationEfficiency, double teamCoordinationRate, double sharedResourceUtilization,
        double collaborationSuccessRate, double collaborationLatency, double collaborationThroughput,
        double collaborationErrorRate, double collaborationConfidence, double collaborationCoverage,
        double collaborationImpact,
        long timestampMs) implements StatisticsSnapshot, CountsMetrics, LatencyMetrics, CollaborationMetrics {

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

    // CollaborationMetrics implementation
    @Override
    public double collaborationEfficiency() {
        return collaborationEfficiency;
    }

    @Override
    public double teamCoordinationRate() {
        return teamCoordinationRate;
    }

    @Override
    public double sharedResourceUtilization() {
        return sharedResourceUtilization;
    }

    @Override
    public double collaborationSuccessRate() {
        return collaborationSuccessRate;
    }

    @Override
    public double collaborationLatency() {
        return collaborationLatency;
    }

    @Override
    public double collaborationThroughput() {
        return collaborationThroughput;
    }

    @Override
    public double collaborationErrorRate() {
        return collaborationErrorRate;
    }

    @Override
    public double collaborationConfidence() {
        return collaborationConfidence;
    }

    @Override
    public double collaborationCoverage() {
        return collaborationCoverage;
    }

    @Override
    public double collaborationImpact() {
        return collaborationImpact;
    }
}
