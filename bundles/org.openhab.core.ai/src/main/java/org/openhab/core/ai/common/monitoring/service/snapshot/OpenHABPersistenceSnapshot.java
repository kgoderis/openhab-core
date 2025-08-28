package org.openhab.core.ai.common.monitoring.service.snapshot;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;

/**
 * Snapshot record for openHAB-specific persistence metrics.
 * 
 * <p>
 * This record provides openHAB-specific persistence metrics including
 * item state persistence, persistence service operations, and openHAB-specific
 * data patterns. It implements CountsMetrics and LatencyMetrics capability interfaces.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record OpenHABPersistenceSnapshot(String persistenceServiceId, String persistenceServiceName,
        long totalItemStates, long successfulItemStates, long failedItemStates, long totalQueries,
        long successfulQueries, long failedQueries, long totalBackups, long successfulBackups, long failedBackups,
        long totalRestores, long successfulRestores, long failedRestores, long totalDurationNanos,
        long averageQueryTimeNanos, long averageBackupTimeNanos, long averageRestoreTimeNanos, long dataSizeBytes,
        long itemCount, long serviceUptimeMs, boolean serviceAvailable,
        long timestampMs) implements MetricsSnapshot, CountsMetrics, LatencyMetrics {

    // CountsMetrics implementation
    @Override
    public long total() {
        return totalItemStates + totalQueries + totalBackups + totalRestores;
    }

    @Override
    public long success() {
        return successfulItemStates + successfulQueries + successfulBackups + successfulRestores;
    }

    @Override
    public long failure() {
        return failedItemStates + failedQueries + failedBackups + failedRestores;
    }

    // LatencyMetrics implementation
    @Override
    public long totalDurationNanos() {
        return totalDurationNanos;
    }

    /**
     * Calculate item state persistence success rate.
     * 
     * @return success rate between 0.0 and 1.0
     */
    public double itemStateSuccessRate() {
        return totalItemStates > 0 ? (double) successfulItemStates / totalItemStates : 0.0;
    }

    /**
     * Calculate query success rate.
     * 
     * @return success rate between 0.0 and 1.0
     */
    public double querySuccessRate() {
        return totalQueries > 0 ? (double) successfulQueries / totalQueries : 0.0;
    }

    /**
     * Calculate backup success rate.
     * 
     * @return success rate between 0.0 and 1.0
     */
    public double backupSuccessRate() {
        return totalBackups > 0 ? (double) successfulBackups / totalBackups : 0.0;
    }

    /**
     * Calculate restore success rate.
     * 
     * @return success rate between 0.0 and 1.0
     */
    public double restoreSuccessRate() {
        return totalRestores > 0 ? (double) successfulRestores / totalRestores : 0.0;
    }

    /**
     * Get average query time in milliseconds.
     * 
     * @return average query time in ms
     */
    public double averageQueryTimeMs() {
        return averageQueryTimeNanos / 1_000_000.0;
    }

    /**
     * Get average backup time in milliseconds.
     * 
     * @return average backup time in ms
     */
    public double averageBackupTimeMs() {
        return averageBackupTimeNanos / 1_000_000.0;
    }

    /**
     * Get average restore time in milliseconds.
     * 
     * @return average restore time in ms
     */
    public double averageRestoreTimeMs() {
        return averageRestoreTimeNanos / 1_000_000.0;
    }

    /**
     * Get data size in megabytes.
     * 
     * @return data size in MB
     */
    public double dataSizeMB() {
        return dataSizeBytes / (1024.0 * 1024.0);
    }

    /**
     * Get service uptime in hours.
     * 
     * @return uptime in hours
     */
    public double uptimeHours() {
        return serviceUptimeMs / (1000.0 * 60.0 * 60.0);
    }

    /**
     * Calculate data density (bytes per item).
     * 
     * @return data density in bytes per item
     */
    public double dataDensity() {
        return itemCount > 0 ? (double) dataSizeBytes / itemCount : 0.0;
    }

    /**
     * Calculate throughput (operations per second).
     * 
     * @return throughput in operations per second
     */
    public double throughput() {
        return serviceUptimeMs > 0 ? (double) total() / (serviceUptimeMs / 1000.0) : 0.0;
    }

    /**
     * Check if persistence service is performing well.
     * 
     * @return true if overall success rate > 95% and service is available
     */
    public boolean isHealthy() {
        double overallSuccessRate = total() > 0 ? (double) success() / total() : 0.0;
        return overallSuccessRate > 0.95 && serviceAvailable;
    }

    /**
     * Check if persistence service is under stress.
     * 
     * @return true if failure rate > 5% or average query time > 1 second
     */
    public boolean isStressed() {
        double failureRate = total() > 0 ? (double) failure() / total() : 0.0;
        return failureRate > 0.05 || averageQueryTimeMs() > 1000.0;
    }

    /**
     * Get service availability status.
     * 
     * @return availability status string
     */
    public String availabilityStatus() {
        if (!serviceAvailable) {
            return "unavailable";
        } else if (isStressed()) {
            return "stressed";
        } else if (isHealthy()) {
            return "healthy";
        } else {
            return "degraded";
        }
    }

    /**
     * Calculate service reliability score.
     * 
     * @return reliability score between 0.0 and 1.0
     */
    public double reliabilityScore() {
        if (!serviceAvailable) {
            return 0.0;
        }

        double successWeight = total() > 0 ? (double) success() / total() : 1.0;
        double timeWeight = Math.max(0.0, 1.0 - (averageQueryTimeMs() / 5000.0)); // 5 second baseline
        double uptimeWeight = Math.min(1.0, uptimeHours() / 24.0); // 24 hour baseline

        return (successWeight * 0.5) + (timeWeight * 0.3) + (uptimeWeight * 0.2);
    }
}
