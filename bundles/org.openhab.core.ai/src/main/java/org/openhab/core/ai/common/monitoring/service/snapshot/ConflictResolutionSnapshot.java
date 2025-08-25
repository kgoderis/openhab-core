package org.openhab.core.ai.common.monitoring.service.snapshot;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.ConflictMetrics;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.service.statistics.StatisticsSnapshot;

@NonNullByDefault
public record ConflictResolutionSnapshot(long total, long success, long failure, long totalDurationNanos,
        double conflictDetectionRate, double conflictResolutionSuccessRate, double conflictComplexity,
        double conflictResolutionEfficiency, double conflictResolutionLatency, double conflictFrequency,
        double conflictEscalationRate, double conflictResolutionConfidence, double conflictPreventionRate,
        double conflictImpact,
        long timestampMs) implements StatisticsSnapshot, CountsMetrics, LatencyMetrics, ConflictMetrics {

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

    // ConflictMetrics implementation
    @Override
    public double conflictDetectionRate() {
        return conflictDetectionRate;
    }

    @Override
    public double conflictResolutionSuccessRate() {
        return conflictResolutionSuccessRate;
    }

    @Override
    public double conflictComplexity() {
        return conflictComplexity;
    }

    @Override
    public double conflictResolutionEfficiency() {
        return conflictResolutionEfficiency;
    }

    @Override
    public double conflictResolutionLatency() {
        return conflictResolutionLatency;
    }

    @Override
    public double conflictFrequency() {
        return conflictFrequency;
    }

    @Override
    public double conflictEscalationRate() {
        return conflictEscalationRate;
    }

    @Override
    public double conflictResolutionConfidence() {
        return conflictResolutionConfidence;
    }

    @Override
    public double conflictPreventionRate() {
        return conflictPreventionRate;
    }

    @Override
    public double conflictImpact() {
        return conflictImpact;
    }
}
