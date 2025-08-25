package org.openhab.core.ai.common.monitoring.service.snapshot;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.OptimizationMetrics;
import org.openhab.core.ai.common.monitoring.service.statistics.StatisticsSnapshot;

@NonNullByDefault
public record OptimizationSnapshot(long total, long success, long failure, long totalDurationNanos,
        double optimizationEfficiency, double performanceImprovementRate, double resourceOptimizationRate,
        double optimizationSuccessRate, double optimizationLatency, double optimizationThroughput,
        double optimizationErrorRate, double optimizationConfidence, double optimizationCoverage,
        double optimizationImpact,
        long timestampMs) implements StatisticsSnapshot, CountsMetrics, LatencyMetrics, OptimizationMetrics {

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

    // OptimizationMetrics implementation
    @Override
    public double optimizationEfficiency() {
        return optimizationEfficiency;
    }

    @Override
    public double performanceImprovementRate() {
        return performanceImprovementRate;
    }

    @Override
    public double resourceOptimizationRate() {
        return resourceOptimizationRate;
    }

    @Override
    public double optimizationSuccessRate() {
        return optimizationSuccessRate;
    }

    @Override
    public double optimizationLatency() {
        return optimizationLatency;
    }

    @Override
    public double optimizationThroughput() {
        return optimizationThroughput;
    }

    @Override
    public double optimizationErrorRate() {
        return optimizationErrorRate;
    }

    @Override
    public double optimizationConfidence() {
        return optimizationConfidence;
    }

    @Override
    public double optimizationCoverage() {
        return optimizationCoverage;
    }

    @Override
    public double optimizationImpact() {
        return optimizationImpact;
    }
}
