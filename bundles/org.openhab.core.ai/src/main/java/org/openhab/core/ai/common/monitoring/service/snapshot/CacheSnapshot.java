package org.openhab.core.ai.common.monitoring.service.snapshot;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.HitRateMetrics;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;

/**
 * Snapshot record for cache metrics.
 * 
 * <p>
 * This record provides cache-specific metrics including hit/miss counts,
 * cache size information, and eviction statistics. It implements both
 * CountsMetrics and HitRateMetrics capability interfaces.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record CacheSnapshot(long hits, long misses, long evictions, long cacheSize, long maxCacheSize,
        long timestampMs) implements MetricsSnapshot, CountsMetrics, HitRateMetrics {

    // CountsMetrics implementation (treating hits as success, misses as failure)
    @Override
    public long total() {
        return hits + misses;
    }

    @Override
    public long success() {
        return hits;
    }

    @Override
    public long failure() {
        return misses;
    }

    // HitRateMetrics implementation (direct mapping)
    @Override
    public long hits() {
        return hits;
    }

    @Override
    public long misses() {
        return misses;
    }

    /**
     * Get the number of cache evictions.
     * 
     * @return eviction count
     */
    public long evictions() {
        return evictions;
    }

    /**
     * Get the current cache size.
     * 
     * @return current cache size
     */
    public long cacheSize() {
        return cacheSize;
    }

    /**
     * Get the maximum cache size.
     * 
     * @return maximum cache size
     */
    public long maxCacheSize() {
        return maxCacheSize;
    }

    /**
     * Calculate the cache utilization as a percentage.
     * 
     * @return cache utilization percentage between 0.0 and 100.0
     */
    public double utilizationPercentage() {
        return maxCacheSize > 0 ? (double) cacheSize / maxCacheSize * 100.0 : 0.0;
    }

    /**
     * Check if the cache is near capacity.
     * 
     * @return true if cache utilization is above 80%
     */
    public boolean isNearCapacity() {
        return utilizationPercentage() > 80.0;
    }

    /**
     * Check if the cache is at capacity.
     * 
     * @return true if cache is at maximum capacity
     */
    public boolean isAtCapacity() {
        return maxCacheSize > 0 && cacheSize >= maxCacheSize;
    }

    /**
     * Get the eviction rate relative to total operations.
     * 
     * @return eviction rate between 0.0 and 1.0
     */
    public double evictionRate() {
        long totalOps = totalOperations();
        return totalOps > 0 ? (double) evictions / totalOps : 0.0;
    }

    /**
     * Get the eviction rate as a percentage.
     * 
     * @return eviction rate percentage between 0.0 and 100.0
     */
    public double evictionRatePercentage() {
        return evictionRate() * 100.0;
    }

    // Override to resolve conflict between CountsMetrics and HitRateMetrics
    @Override
    public boolean hasOperations() {
        return totalOperations() > 0;
    }
}
