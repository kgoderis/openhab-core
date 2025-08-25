package org.openhab.core.ai.common.monitoring.service.snapshot;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.PersistenceMetrics;
import org.openhab.core.ai.common.monitoring.service.statistics.StatisticsSnapshot;

@NonNullByDefault
public record PersistenceSnapshot(long total, long success, long failure, long totalDurationNanos,
        double dataIntegrityRate, double storageEfficiency, double recoverySuccessRate, double backupFrequency,
        double dataRetentionEfficiency,
        long timestampMs) implements StatisticsSnapshot, CountsMetrics, LatencyMetrics, PersistenceMetrics {

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

    // PersistenceMetrics implementation
    @Override
    public double dataIntegrityRate() {
        return dataIntegrityRate;
    }

    @Override
    public double storageEfficiency() {
        return storageEfficiency;
    }

    @Override
    public double recoverySuccessRate() {
        return recoverySuccessRate;
    }

    @Override
    public double backupFrequency() {
        return backupFrequency;
    }

    @Override
    public double dataRetentionEfficiency() {
        return dataRetentionEfficiency;
    }
}
