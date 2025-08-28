package org.openhab.core.ai.common.monitoring.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Capability interface for cache hit rate metrics.
 * 
 * <p>
 * This interface provides cache-specific functionality including hit counts,
 * miss counts, hit rate calculation, and cache efficiency metrics.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface HitRateMetrics {

    /**
     * Get the total number of cache hits.
     * 
     * @return hit count
     */
    long hits();

    /**
     * Get the total number of cache misses.
     * 
     * @return miss count
     */
    long misses();

    /**
     * Get the total number of cache operations (hits + misses).
     * 
     * @return total cache operations
     */
    default long totalOperations() {
        return hits() + misses();
    }

    /**
     * Calculate the cache hit rate as a percentage.
     * 
     * @return hit rate between 0.0 and 1.0, or 0.0 if no operations
     */
    default double hitRate() {
        long total = totalOperations();
        return total > 0 ? (double) hits() / total : 0.0;
    }

    /**
     * Calculate the cache hit rate as a percentage (0-100).
     * 
     * @return hit rate percentage between 0.0 and 100.0, or 0.0 if no operations
     */
    default double hitRatePercentage() {
        return hitRate() * 100.0;
    }

    /**
     * Calculate the cache miss rate as a percentage.
     * 
     * @return miss rate between 0.0 and 1.0, or 0.0 if no operations
     */
    default double missRate() {
        long total = totalOperations();
        return total > 0 ? (double) misses() / total : 0.0;
    }

    /**
     * Calculate the cache miss rate as a percentage (0-100).
     * 
     * @return miss rate percentage between 0.0 and 100.0, or 0.0 if no operations
     */
    default double missRatePercentage() {
        return missRate() * 100.0;
    }

    /**
     * Check if there are any cache operations recorded.
     * 
     * @return true if there are cache operations, false otherwise
     */
    default boolean hasOperations() {
        return totalOperations() > 0;
    }

    /**
     * Determine cache efficiency level based on hit rate.
     * 
     * @return cache efficiency level
     */
    default CacheEfficiency efficiency() {
        double hitRate = hitRatePercentage();
        if (hitRate >= 90.0) {
            return CacheEfficiency.EXCELLENT;
        } else if (hitRate >= 75.0) {
            return CacheEfficiency.GOOD;
        } else if (hitRate >= 50.0) {
            return CacheEfficiency.FAIR;
        } else {
            return CacheEfficiency.POOR;
        }
    }

    /**
     * Cache efficiency levels.
     */
    enum CacheEfficiency {
        EXCELLENT,
        GOOD,
        FAIR,
        POOR
    }
}
