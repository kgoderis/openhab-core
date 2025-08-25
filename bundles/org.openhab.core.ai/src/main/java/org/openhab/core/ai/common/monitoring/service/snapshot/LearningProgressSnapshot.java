package org.openhab.core.ai.common.monitoring.service.snapshot;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.LearningMetrics;
import org.openhab.core.ai.common.monitoring.service.statistics.StatisticsSnapshot;

@NonNullByDefault
public record LearningProgressSnapshot(long total, long success, long failure, long totalDurationNanos,
        double learningRate, double knowledgeAcquisitionRate, double skillImprovementRate, double learningEfficiency,
        double learningProgress, double learningAccuracy, double learningRetentionRate, double learningAdaptationRate,
        double learningConfidence, double learningThroughput,
        long timestampMs) implements StatisticsSnapshot, CountsMetrics, LatencyMetrics, LearningMetrics {

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

    // LearningMetrics implementation
    @Override
    public double learningRate() {
        return learningRate;
    }

    @Override
    public double knowledgeAcquisitionRate() {
        return knowledgeAcquisitionRate;
    }

    @Override
    public double skillImprovementRate() {
        return skillImprovementRate;
    }

    @Override
    public double learningEfficiency() {
        return learningEfficiency;
    }

    @Override
    public double learningProgress() {
        return learningProgress;
    }

    @Override
    public double learningAccuracy() {
        return learningAccuracy;
    }

    @Override
    public double learningRetentionRate() {
        return learningRetentionRate;
    }

    @Override
    public double learningAdaptationRate() {
        return learningAdaptationRate;
    }

    @Override
    public double learningConfidence() {
        return learningConfidence;
    }

    @Override
    public double learningThroughput() {
        return learningThroughput;
    }
}
