package org.openhab.core.ai.agent.api;

import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.osgi.service.component.annotations.Reference;

@NonNullByDefault
public class AgentModelContextCacheStatistics {

    @Reference
    private @Nullable MetricsService metricsService;
    private final int currentSize;
    private final long hits;
    private final long misses;
    private final long evictions;
    private final int maxSize;
    private final Duration defaultExpiration;

    public AgentModelContextCacheStatistics(int currentSize, long hits, long misses, long evictions, int maxSize,
            Duration defaultExpiration) {
        this.currentSize = currentSize;
        this.hits = hits;
        this.misses = misses;
        this.evictions = evictions;
        this.maxSize = maxSize;
        this.defaultExpiration = defaultExpiration;
    }

    public int getCurrentSize() {
        return currentSize;
    }

    public long getHits() {
        return hits;
    }

    public long getMisses() {
        return misses;
    }

    public long getEvictions() {
        return evictions;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public Duration getDefaultExpiration() {
        return defaultExpiration;
    }

    public double getHitRate() {
        long total = hits + misses;
        return total > 0 ? (double) hits / total : 0.0;
    }

    public double getUtilizationRate() {
        return maxSize > 0 ? (double) currentSize / maxSize : 0.0;
    }

    /**
     * Record cache statistics using MetricsService.
     */
    public void recordCacheStatistics() {
        MetricsService metrics = metricsService;
        if (metrics != null) {
            long startTime = System.currentTimeMillis();
            boolean success = getHitRate() > 0.5; // Consider cache healthy if hit rate > 50%
            long duration = System.currentTimeMillis() - startTime;

            metrics.recordOperation("agent-model", "context-cache", success, java.time.Duration.ofMillis(duration));
        }
    }

    @Override
    public String toString() {
        return String.format(
                "CacheStatistics{size=%d/%d, hits=%d, misses=%d, evictions=%d, hitRate=%.2f, utilization=%.2f}",
                currentSize, maxSize, hits, misses, evictions, getHitRate(), getUtilizationRate());
    }
}
