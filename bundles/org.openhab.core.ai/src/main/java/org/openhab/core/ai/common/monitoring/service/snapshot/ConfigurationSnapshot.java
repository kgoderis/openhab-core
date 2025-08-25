package org.openhab.core.ai.common.monitoring.service.snapshot;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.ConfigurationMetrics;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.service.statistics.StatisticsSnapshot;

@NonNullByDefault
public record ConfigurationSnapshot(long total, long success, long failure, long totalDurationNanos,
        double configurationValidationRate, double configurationChangeFrequency, double configurationComplianceRate,
        double configurationEfficiency, double configurationErrorRate, double configurationUpdateLatency,
        double configurationConsistencyRate, double configurationBackupRate, double configurationRestoreRate,
        double configurationVersioningAccuracy,
        long timestampMs) implements StatisticsSnapshot, CountsMetrics, LatencyMetrics, ConfigurationMetrics {

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

    // ConfigurationMetrics implementation
    @Override
    public double configurationValidationRate() {
        return configurationValidationRate;
    }

    @Override
    public double configurationChangeFrequency() {
        return configurationChangeFrequency;
    }

    @Override
    public double configurationComplianceRate() {
        return configurationComplianceRate;
    }

    @Override
    public double configurationEfficiency() {
        return configurationEfficiency;
    }

    @Override
    public double configurationErrorRate() {
        return configurationErrorRate;
    }

    @Override
    public double configurationUpdateLatency() {
        return configurationUpdateLatency;
    }

    @Override
    public double configurationConsistencyRate() {
        return configurationConsistencyRate;
    }

    @Override
    public double configurationBackupRate() {
        return configurationBackupRate;
    }

    @Override
    public double configurationRestoreRate() {
        return configurationRestoreRate;
    }

    @Override
    public double configurationVersioningAccuracy() {
        return configurationVersioningAccuracy;
    }
}
