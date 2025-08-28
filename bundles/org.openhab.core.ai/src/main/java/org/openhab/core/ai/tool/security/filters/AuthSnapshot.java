package org.openhab.core.ai.tool.security.filters;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.HitRateMetrics;
import org.openhab.core.ai.common.monitoring.api.SecurityMetrics;
import org.openhab.core.ai.common.monitoring.service.statistics.StatisticsSnapshot;

/**
 * Immutable snapshot of authentication metrics data.
 * 
 * <p>
 * This class provides a point-in-time view of authentication performance
 * including basic counts, cache hit rates, and security-specific metrics.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record AuthSnapshot(long total, long success, long failure, long cacheHits, long cacheMisses, int cacheSize,
        long timestampMs) implements StatisticsSnapshot, CountsMetrics, HitRateMetrics, SecurityMetrics {

    /**
     * Factory method to create a snapshot from metrics data.
     * 
     * @param total total authentication requests
     * @param success successful authentications
     * @param failure failed authentications
     * @param cacheHits number of cache hits
     * @param cacheMisses number of cache misses
     * @param cacheSize current cache size
     * @return new AuthSnapshot instance
     */
    public static AuthSnapshot of(long total, long success, long failure, long cacheHits, long cacheMisses,
            int cacheSize) {
        return new AuthSnapshot(total, success, failure, cacheHits, cacheMisses, cacheSize, System.currentTimeMillis());
    }

    /**
     * Factory method to create an empty snapshot.
     * 
     * @return empty AuthSnapshot instance
     */
    public static AuthSnapshot empty() {
        return new AuthSnapshot(0, 0, 0, 0, 0, 0, System.currentTimeMillis());
    }

    // HitRateMetrics implementation
    @Override
    public long hits() {
        return cacheHits;
    }

    @Override
    public long misses() {
        return cacheMisses;
    }

    @Override
    public double hitRate() {
        long totalCacheAccess = cacheHits + cacheMisses;
        return totalCacheAccess > 0 ? (double) cacheHits / totalCacheAccess : 0.0;
    }

    @Override
    public boolean hasOperations() {
        return total > 0;
    }

    // SecurityMetrics implementation
    @Override
    public int activeSessions() {
        // For auth metrics, active sessions could be approximated by cache size
        return cacheSize;
    }

    @Override
    public int maxConnections() {
        // Default max connections - could be configurable
        return 1000;
    }

    @Override
    public boolean authenticationEnabled() {
        return true; // Auth is always enabled if we're tracking auth metrics
    }

    @Override
    public boolean requestValidationEnabled() {
        return true; // Request validation is enabled if auth is enabled
    }

    @Override
    public int rateLimitPerMinute() {
        // Default rate limit - could be configurable
        return 100;
    }

    @Override
    public int blockedClients() {
        // Blocked clients could be approximated by failed authentications
        return (int) Math.min(failure, Integer.MAX_VALUE);
    }

    /**
     * Create a summary string with key metrics.
     * 
     * @return summary string
     */
    public String toSummary() {
        return String.format("AuthSnapshot{total=%d, success=%d, failure=%d, cacheHitRate=%.1f%%, cacheSize=%d}", total,
                success, failure, hitRate() * 100.0, cacheSize);
    }
}

