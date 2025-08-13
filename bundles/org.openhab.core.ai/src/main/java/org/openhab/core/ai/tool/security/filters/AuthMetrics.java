package org.openhab.core.ai.tool.security.filters;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Authentication metrics for security filters.
 *
 * <p>
 * Tracks total requests, successes/failures, cache efficiency and cache size.
 * </p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class AuthMetrics {
    public final long totalRequests;
    public final long successfulAuthentications;
    public final long failedAuthentications;
    public final long cacheHits;
    public final long cacheMisses;
    public final int cacheSize;

    public AuthMetrics(long totalRequests, long successfulAuthentications, long failedAuthentications, long cacheHits,
            long cacheMisses, int cacheSize) {
        this.totalRequests = totalRequests;
        this.successfulAuthentications = successfulAuthentications;
        this.failedAuthentications = failedAuthentications;
        this.cacheHits = cacheHits;
        this.cacheMisses = cacheMisses;
        this.cacheSize = cacheSize;
    }

    public double getSuccessRate() {
        return totalRequests > 0 ? (double) successfulAuthentications / totalRequests : 0.0;
    }

    public double getCacheHitRate() {
        long totalCacheAccess = cacheHits + cacheMisses;
        return totalCacheAccess > 0 ? (double) cacheHits / totalCacheAccess : 0.0;
    }
}
