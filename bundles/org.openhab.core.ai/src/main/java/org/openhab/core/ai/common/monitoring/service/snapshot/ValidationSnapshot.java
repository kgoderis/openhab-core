package org.openhab.core.ai.common.monitoring.service.snapshot;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.ValidationMetrics;
import org.openhab.core.ai.common.monitoring.service.statistics.StatisticsSnapshot;

@NonNullByDefault
public record ValidationSnapshot(long total, long success, long failure, long totalDurationNanos,
        double validationSuccessRate, double validationAccuracy, double validationThroughput,
        double validationEfficiency, double validationErrorRate, double validationLatency, double validationCoverage,
        double validationConfidence, double validationRejectionRate, double validationComplianceRate,
        long timestampMs) implements StatisticsSnapshot, CountsMetrics, LatencyMetrics, ValidationMetrics {

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

    // ValidationMetrics implementation
    @Override
    public double validationSuccessRate() {
        return validationSuccessRate;
    }

    @Override
    public double validationAccuracy() {
        return validationAccuracy;
    }

    @Override
    public double validationThroughput() {
        return validationThroughput;
    }

    @Override
    public double validationEfficiency() {
        return validationEfficiency;
    }

    @Override
    public double validationErrorRate() {
        return validationErrorRate;
    }

    @Override
    public double validationLatency() {
        return validationLatency;
    }

    @Override
    public double validationCoverage() {
        return validationCoverage;
    }

    @Override
    public double validationConfidence() {
        return validationConfidence;
    }

    @Override
    public double validationRejectionRate() {
        return validationRejectionRate;
    }

    @Override
    public double validationComplianceRate() {
        return validationComplianceRate;
    }
}
