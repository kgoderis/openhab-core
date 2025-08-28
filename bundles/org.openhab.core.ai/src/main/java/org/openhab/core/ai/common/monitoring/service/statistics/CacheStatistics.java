package org.openhab.core.ai.common.monitoring.service.statistics;

import java.time.Duration;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.HitRateMetrics;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;
import org.openhab.core.ai.common.monitoring.service.snapshot.CacheSnapshot;

/**
 * Statistics class for cache metrics.
 * 
 * <p>
 * This class provides comprehensive cache statistics including hit rate analysis,
 * eviction patterns, cache utilization trends, and performance metrics.
 * It aggregates multiple CacheSnapshot instances to provide historical
 * and statistical analysis of cache behavior.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record CacheStatistics(List<CacheSnapshot> snapshots, Duration timeRange, long timestampMs)
        implements
            StatisticsSnapshot,
            CountsMetrics,
            HitRateMetrics,
            TrendMetrics,
            PercentileMetrics {

    // CountsMetrics implementation (aggregated across all snapshots)
    @Override
    public long total() {
        return snapshots.stream().mapToLong(CacheSnapshot::total).sum();
    }

    @Override
    public long success() {
        return hits(); // Use hits as success metric
    }

    @Override
    public long failure() {
        return misses(); // Use misses as failure metric
    }

    // HitRateMetrics implementation (aggregated across all snapshots)
    @Override
    public long hits() {
        return snapshots.stream().mapToLong(CacheSnapshot::hits).sum();
    }

    @Override
    public long misses() {
        return snapshots.stream().mapToLong(CacheSnapshot::misses).sum();
    }

    // TrendMetrics implementation
    @Override
    public double trendPercentage() {
        if (snapshots.size() < 2) {
            return 0.0;
        }
        // Calculate hit rate trend from first to last snapshot
        double firstHitRate = snapshots.get(0).hitRatePercentage();
        double lastHitRate = snapshots.get(snapshots.size() - 1).hitRatePercentage();
        return firstHitRate > 0 ? ((lastHitRate - firstHitRate) / firstHitRate) * 100.0 : 0.0;
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
        long totalOps = totalOperations();
        long timeSpanMs = snapshots.get(snapshots.size() - 1).timestampMs() - snapshots.get(0).timestampMs();
        return timeSpanMs > 0 ? (double) totalOps / (timeSpanMs / 1000.0) : 0.0;
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

        List<Double> hitRates = snapshots.stream().mapToDouble(CacheSnapshot::hitRatePercentage).sorted().boxed()
                .toList();

        if (hitRates.isEmpty()) {
            return 0.0;
        }

        int index = (int) Math.ceil(percentile * hitRates.size()) - 1;
        index = Math.max(0, Math.min(index, hitRates.size() - 1));
        return hitRates.get(index);
    }

    /**
     * Get total evictions across all snapshots.
     * 
     * @return total eviction count
     */
    public long totalEvictions() {
        return snapshots.stream().mapToLong(CacheSnapshot::evictions).sum();
    }

    /**
     * Get average cache utilization across all snapshots.
     * 
     * @return average utilization percentage
     */
    public double averageUtilization() {
        return snapshots.stream().mapToDouble(CacheSnapshot::utilizationPercentage).average().orElse(0.0);
    }

    /**
     * Get maximum cache utilization across all snapshots.
     * 
     * @return maximum utilization percentage
     */
    public double maxUtilization() {
        return snapshots.stream().mapToDouble(CacheSnapshot::utilizationPercentage).max().orElse(0.0);
    }

    /**
     * Get minimum cache utilization across all snapshots.
     * 
     * @return minimum utilization percentage
     */
    public double minUtilization() {
        return snapshots.stream().mapToDouble(CacheSnapshot::utilizationPercentage).min().orElse(0.0);
    }

    /**
     * Get average cache size across all snapshots.
     * 
     * @return average cache size
     */
    public double averageCacheSize() {
        return snapshots.stream().mapToLong(CacheSnapshot::cacheSize).average().orElse(0.0);
    }

    /**
     * Get overall eviction rate across all snapshots.
     * 
     * @return eviction rate percentage
     */
    public double overallEvictionRate() {
        long totalOps = totalOperations();
        return totalOps > 0 ? (double) totalEvictions() / totalOps * 100.0 : 0.0;
    }

    /**
     * Check if cache performance is considered healthy.
     * 
     * @return true if hit rate is above 70% and eviction rate is below 10%
     */
    public boolean isHealthy() {
        return hitRatePercentage() > 70.0 && overallEvictionRate() < 10.0;
    }

    /**
     * Get the number of times cache was near capacity.
     * 
     * @return count of near-capacity instances
     */
    public long nearCapacityCount() {
        return snapshots.stream().mapToLong(s -> s.isNearCapacity() ? 1L : 0L).sum();
    }

    /**
     * Get the number of times cache was at capacity.
     * 
     * @return count of at-capacity instances
     */
    public long atCapacityCount() {
        return snapshots.stream().mapToLong(s -> s.isAtCapacity() ? 1L : 0L).sum();
    }

    // Override to resolve conflict between CountsMetrics and HitRateMetrics
    @Override
    public boolean hasOperations() {
        return totalOperations() > 0;
    }
}
