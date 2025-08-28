package org.openhab.core.ai.common.monitoring.service.statistics;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;
import org.openhab.core.ai.common.monitoring.service.snapshot.OpenHABPersistenceSnapshot;

/**
 * Statistics class for openHAB-specific persistence metrics.
 * 
 * <p>
 * This class provides comprehensive openHAB persistence statistics including
 * service performance, data management efficiency, and openHAB-specific
 * persistence patterns. It aggregates multiple OpenHABPersistenceSnapshot
 * instances to provide historical and statistical analysis of openHAB
 * persistence behavior.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record OpenHABPersistenceStatistics(List<OpenHABPersistenceSnapshot> snapshots, Duration timeRange,
        long timestampMs)
        implements
            StatisticsSnapshot,
            CountsMetrics,
            LatencyMetrics,
            TrendMetrics,
            PercentileMetrics {

    // CountsMetrics implementation (aggregated across all snapshots)
    @Override
    public long total() {
        return snapshots.stream().mapToLong(OpenHABPersistenceSnapshot::total).sum();
    }

    @Override
    public long success() {
        return snapshots.stream().mapToLong(OpenHABPersistenceSnapshot::success).sum();
    }

    @Override
    public long failure() {
        return snapshots.stream().mapToLong(OpenHABPersistenceSnapshot::failure).sum();
    }

    // LatencyMetrics implementation
    @Override
    public long totalDurationNanos() {
        return snapshots.stream().mapToLong(OpenHABPersistenceSnapshot::totalDurationNanos).sum();
    }

    // TrendMetrics implementation
    @Override
    public double trendPercentage() {
        if (snapshots.size() < 2) {
            return 0.0;
        }
        // Calculate reliability trend from first to last snapshot
        double firstReliability = snapshots.get(0).reliabilityScore();
        double lastReliability = snapshots.get(snapshots.size() - 1).reliabilityScore();
        return firstReliability > 0 ? ((lastReliability - firstReliability) / firstReliability) * 100.0 : 0.0;
    }

    @Override
    public String trendDirection() {
        double percentage = trendPercentage();
        if (percentage > 5.0) {
            return "improving";
        } else if (percentage < -5.0) {
            return "declining";
        } else {
            return "stable";
        }
    }

    @Override
    public double changeRate() {
        if (snapshots.size() < 2) {
            return 0.0;
        }
        // Calculate change rate in operations per second
        long totalOperations = total();
        long timeSpanMs = snapshots.get(snapshots.size() - 1).timestampMs() - snapshots.get(0).timestampMs();
        return timeSpanMs > 0 ? (double) totalOperations / (timeSpanMs / 1000.0) : 0.0;
    }

    // PercentileMetrics implementation
    @Override
    public double percentile50() {
        return calculatePercentile(0.5);
    }

    @Override
    public double percentile90() {
        return calculatePercentile(0.9);
    }

    @Override
    public double percentile95() {
        return calculatePercentile(0.95);
    }

    @Override
    public double percentile99() {
        return calculatePercentile(0.99);
    }

    private double calculatePercentile(double percentile) {
        if (snapshots.isEmpty()) {
            return 0.0;
        }

        List<Double> queryTimes = snapshots.stream().mapToDouble(s -> s.averageQueryTimeMs()).sorted().boxed().toList();

        if (queryTimes.isEmpty()) {
            return 0.0;
        }

        int index = (int) Math.ceil(percentile * queryTimes.size()) - 1;
        index = Math.max(0, Math.min(index, queryTimes.size() - 1));
        return queryTimes.get(index);
    }

    /**
     * Get total item states across all snapshots.
     * 
     * @return total item states
     */
    public long totalItemStates() {
        return snapshots.stream().mapToLong(OpenHABPersistenceSnapshot::totalItemStates).sum();
    }

    /**
     * Get total queries across all snapshots.
     * 
     * @return total queries
     */
    public long totalQueries() {
        return snapshots.stream().mapToLong(OpenHABPersistenceSnapshot::totalQueries).sum();
    }

    /**
     * Get total backups across all snapshots.
     * 
     * @return total backups
     */
    public long totalBackups() {
        return snapshots.stream().mapToLong(OpenHABPersistenceSnapshot::totalBackups).sum();
    }

    /**
     * Get total restores across all snapshots.
     * 
     * @return total restores
     */
    public long totalRestores() {
        return snapshots.stream().mapToLong(OpenHABPersistenceSnapshot::totalRestores).sum();
    }

    /**
     * Get total data size across all snapshots.
     * 
     * @return total data size in bytes
     */
    public long totalDataSize() {
        return snapshots.stream().mapToLong(OpenHABPersistenceSnapshot::dataSizeBytes).sum();
    }

    /**
     * Get total item count across all snapshots.
     * 
     * @return total item count
     */
    public long totalItemCount() {
        return snapshots.stream().mapToLong(OpenHABPersistenceSnapshot::itemCount).sum();
    }

    /**
     * Get average item state success rate across all snapshots.
     * 
     * @return average item state success rate
     */
    public double averageItemStateSuccessRate() {
        return snapshots.stream().mapToDouble(OpenHABPersistenceSnapshot::itemStateSuccessRate).average().orElse(0.0);
    }

    /**
     * Get average query success rate across all snapshots.
     * 
     * @return average query success rate
     */
    public double averageQuerySuccessRate() {
        return snapshots.stream().mapToDouble(OpenHABPersistenceSnapshot::querySuccessRate).average().orElse(0.0);
    }

    /**
     * Get average backup success rate across all snapshots.
     * 
     * @return average backup success rate
     */
    public double averageBackupSuccessRate() {
        return snapshots.stream().mapToDouble(OpenHABPersistenceSnapshot::backupSuccessRate).average().orElse(0.0);
    }

    /**
     * Get average restore success rate across all snapshots.
     * 
     * @return average restore success rate
     */
    public double averageRestoreSuccessRate() {
        return snapshots.stream().mapToDouble(OpenHABPersistenceSnapshot::restoreSuccessRate).average().orElse(0.0);
    }

    /**
     * Get average query time across all snapshots.
     * 
     * @return average query time in milliseconds
     */
    public double averageQueryTime() {
        return snapshots.stream().mapToDouble(OpenHABPersistenceSnapshot::averageQueryTimeMs).average().orElse(0.0);
    }

    /**
     * Get average backup time across all snapshots.
     * 
     * @return average backup time in milliseconds
     */
    public double averageBackupTime() {
        return snapshots.stream().mapToDouble(OpenHABPersistenceSnapshot::averageBackupTimeMs).average().orElse(0.0);
    }

    /**
     * Get average restore time across all snapshots.
     * 
     * @return average restore time in milliseconds
     */
    public double averageRestoreTime() {
        return snapshots.stream().mapToDouble(OpenHABPersistenceSnapshot::averageRestoreTimeMs).average().orElse(0.0);
    }

    /**
     * Get average throughput across all snapshots.
     * 
     * @return average throughput in operations per second
     */
    public double averageThroughput() {
        return snapshots.stream().mapToDouble(OpenHABPersistenceSnapshot::throughput).average().orElse(0.0);
    }

    /**
     * Get average data density across all snapshots.
     * 
     * @return average data density in bytes per item
     */
    public double averageDataDensity() {
        return snapshots.stream().mapToDouble(OpenHABPersistenceSnapshot::dataDensity).average().orElse(0.0);
    }

    /**
     * Get average reliability score across all snapshots.
     * 
     * @return average reliability score
     */
    public double averageReliabilityScore() {
        return snapshots.stream().mapToDouble(OpenHABPersistenceSnapshot::reliabilityScore).average().orElse(0.0);
    }

    /**
     * Get persistence service distribution.
     * 
     * @return map of service IDs to their operation counts
     */
    public Map<String, Long> serviceDistribution() {
        return snapshots.stream().collect(Collectors.groupingBy(OpenHABPersistenceSnapshot::persistenceServiceId,
                Collectors.summingLong(OpenHABPersistenceSnapshot::total)));
    }

    /**
     * Get service availability distribution.
     * 
     * @return map of availability status to their counts
     */
    public Map<String, Long> availabilityDistribution() {
        return snapshots.stream()
                .collect(Collectors.groupingBy(OpenHABPersistenceSnapshot::availabilityStatus, Collectors.counting()));
    }

    /**
     * Get count of healthy services.
     * 
     * @return number of healthy services
     */
    public long healthyServiceCount() {
        return snapshots.stream().mapToLong(s -> s.isHealthy() ? 1 : 0).sum();
    }

    /**
     * Get count of stressed services.
     * 
     * @return number of stressed services
     */
    public long stressedServiceCount() {
        return snapshots.stream().mapToLong(s -> s.isStressed() ? 1 : 0).sum();
    }

    /**
     * Get count of available services.
     * 
     * @return number of available services
     */
    public long availableServiceCount() {
        return snapshots.stream().mapToLong(s -> s.serviceAvailable() ? 1 : 0).sum();
    }

    /**
     * Get unique service count.
     * 
     * @return number of unique persistence services
     */
    public long uniqueServiceCount() {
        return snapshots.stream().map(OpenHABPersistenceSnapshot::persistenceServiceId).distinct().count();
    }

    /**
     * Check if overall persistence performance is healthy.
     * 
     * @return true if average reliability > 0.8 and most services are healthy
     */
    public boolean isOverallHealthy() {
        double healthyRatio = snapshots.size() > 0 ? (double) healthyServiceCount() / snapshots.size() : 0.0;
        return averageReliabilityScore() > 0.8 && healthyRatio > 0.8;
    }

    /**
     * Get most active persistence service.
     * 
     * @return service ID with highest operation count
     */
    public String mostActiveService() {
        return serviceDistribution().entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey)
                .orElse("unknown");
    }

    /**
     * Get total data size in megabytes.
     * 
     * @return total data size in MB
     */
    public double totalDataSizeMB() {
        return totalDataSize() / (1024.0 * 1024.0);
    }

    /**
     * Get average data size per service.
     * 
     * @return average data size in MB per service
     */
    public double averageDataSizePerService() {
        long serviceCount = uniqueServiceCount();
        return serviceCount > 0 ? totalDataSizeMB() / serviceCount : 0.0;
    }

    /**
     * Get service utilization rate.
     * 
     * @return utilization rate as percentage of services that are active
     */
    public double serviceUtilizationRate() {
        long totalServices = uniqueServiceCount();
        return totalServices > 0 ? (double) availableServiceCount() / totalServices * 100.0 : 0.0;
    }
}
