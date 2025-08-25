package org.openhab.core.ai.common.monitoring.service.snapshot;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CoordinationMetrics;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.service.statistics.StatisticsSnapshot;

@NonNullByDefault
public record CoordinationSnapshot(long total, long success, long failure, long totalDurationNanos,
        double coordinationEfficiency, double taskSynchronizationRate, double resourceCoordinationRate,
        double coordinationSuccessRate, double coordinationLatency, double coordinationThroughput,
        double coordinationErrorRate, double coordinationConfidence, double coordinationCoverage,
        double coordinationImpact,
        long timestampMs) implements StatisticsSnapshot, CountsMetrics, LatencyMetrics, CoordinationMetrics {

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

    // CoordinationMetrics implementation
    @Override
    public double coordinationEfficiency() {
        return coordinationEfficiency;
    }

    @Override
    public double taskSynchronizationRate() {
        return taskSynchronizationRate;
    }

    @Override
    public double resourceCoordinationRate() {
        return resourceCoordinationRate;
    }

    @Override
    public double coordinationSuccessRate() {
        return coordinationSuccessRate;
    }

    @Override
    public double coordinationLatency() {
        return coordinationLatency;
    }

    @Override
    public double coordinationThroughput() {
        return coordinationThroughput;
    }

    @Override
    public double coordinationErrorRate() {
        return coordinationErrorRate;
    }

    @Override
    public double coordinationConfidence() {
        return coordinationConfidence;
    }

    @Override
    public double coordinationCoverage() {
        return coordinationCoverage;
    }

    @Override
    public double coordinationImpact() {
        return coordinationImpact;
    }
}
