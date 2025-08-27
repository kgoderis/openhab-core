/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.core.ai.agent.api;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.context.AgentModelContext;
import java.util.Map;
import java.util.Set;
import org.openhab.core.ai.common.monitoring.api.MetricKey;
import org.openhab.core.ai.common.monitoring.api.MetricKeys;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.reasoning.memory.CacheEntry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Agent Model Context Cache for context caching and reuse.
 * 
 * This class provides efficient caching capabilities for agent contexts,
 * including automatic expiration, size management, and performance optimization.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = AgentModelContextCache.class)
@NonNullByDefault
public class AgentModelContextCache {

    private static final Logger logger = LoggerFactory.getLogger(AgentModelContextCache.class);

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();

    @Reference
    private @Nullable MetricsService metricsService;

    // Configuration
    private final int maxCacheSize;
    private final Duration defaultExpiration;
    private final Duration cleanupInterval;

    /**
     * Create a new context cache with default configuration.
     */
    @Activate
    public AgentModelContextCache() {
        this.maxCacheSize = 1000;
        this.defaultExpiration = Duration.ofMinutes(30);
        this.cleanupInterval = Duration.ofMinutes(5);

        startCleanupTask();
        logger.info("Agent Model Context Cache initialized with max size: {}, default expiration: {}", maxCacheSize,
                defaultExpiration);
    }

    /**
     * Create a new context cache with custom configuration.
     * 
     * @param maxCacheSize Maximum number of cached contexts
     * @param defaultExpiration Default expiration time for cached contexts
     * @param cleanupInterval Interval for cleanup task execution
     */
    public AgentModelContextCache(int maxCacheSize, Duration defaultExpiration, Duration cleanupInterval) {
        this.maxCacheSize = maxCacheSize;
        this.defaultExpiration = defaultExpiration;
        this.cleanupInterval = cleanupInterval;

        startCleanupTask();
        logger.info("Agent Model Context Cache initialized with max size: {}, default expiration: {}", maxCacheSize,
                defaultExpiration);
    }

    /**
     * Get a context from the cache.
     * 
     * @param contextId The context ID to retrieve
     * @return The cached context, or null if not found or expired
     */
    public AgentModelContext get(String contextId) {
        CacheEntry entry = cache.get(contextId);

        if (entry == null) {
            recordMetrics("agent-model-context-cache", "cache-miss", true, Duration.ZERO);
            return null;
        }

        // Check if entry has expired
        if (entry.isExpired()) {
            cache.remove(contextId);
            recordMetrics("agent-model-context-cache", "cache-eviction", true, Duration.ZERO);
            recordMetrics("agent-model-context-cache", "cache-miss", true, Duration.ZERO);
            return null;
        }

        recordMetrics("agent-model-context-cache", "cache-hit", true, Duration.ZERO);
        return entry.getContext();
    }

    /**
     * Put a context into the cache.
     * 
     * @param contextId The context ID
     * @param context The context to cache
     */
    public void put(String contextId, AgentModelContext context) {
        put(contextId, context, defaultExpiration);
    }

    /**
     * Put a context into the cache with custom expiration.
     * 
     * @param contextId The context ID
     * @param context The context to cache
     * @param expiration The expiration time
     */
    public void put(String contextId, AgentModelContext context, Duration expiration) {
        // Check if cache is full and evict oldest entry if necessary
        if (cache.size() >= maxCacheSize) {
            evictOldestEntry();
        }

        CacheEntry entry = new CacheEntry(context, expiration);
        cache.put(contextId, entry);
        recordMetrics("agent-model-context-cache", "cache-put", true, Duration.ZERO);
    }

    /**
     * Remove a context from the cache.
     * 
     * @param contextId The context ID to remove
     * @return The removed context, or null if not found
     */
    public AgentModelContext remove(String contextId) {
        CacheEntry entry = cache.remove(contextId);
        if (entry != null) {
            recordMetrics("agent-model-context-cache", "cache-remove", true, Duration.ZERO);
            return entry.getContext();
        }
        return null;
    }

    /**
     * Clear all cached contexts.
     */
    public void clear() {
        int size = cache.size();
        cache.clear();
        recordMetrics("agent-model-context-cache", "cache-clear", true, Duration.ZERO);
        logger.info("Cleared {} cached contexts", size);
    }

    /**
     * Get the current cache size.
     * 
     * @return Number of cached contexts
     */
    public int size() {
        return cache.size();
    }

    /**
     * Check if the cache contains a context.
     * 
     * @param contextId The context ID to check
     * @return true if the context is cached and not expired
     */
    public boolean contains(String contextId) {
        CacheEntry entry = cache.get(contextId);
        if (entry == null || entry.isExpired()) {
            return false;
        }
        return true;
    }

    /**
     * Get cache statistics.
     * 
     * @return Cache statistics
     */
    public AgentModelContextCacheStatistics getStatistics() {
        MetricsService metrics = metricsService;
        if (metrics != null) {
            try {
                try {
                    MetricKey agentModelContextCacheKey = MetricKeys.custom("agent-model-context-cache", Map.of(), Set.of("counts", "latency"));
                    var snapshot = metrics.getSnapshot(agentModelContextCacheKey, org.openhab.core.ai.common.monitoring.service.snapshot.GenericMetricsSnapshot.class);
                    if (snapshot != null) {
                        long totalOperations = snapshot.getLong("total");
                        long failedOperations = snapshot.getLong("failure");
                        long hits = totalOperations - failedOperations; // Approximate hits
                        long misses = failedOperations; // Approximate misses
                        long evictions = 0L; // Placeholder - would need domain-specific data
                        return new AgentModelContextCacheStatistics(cache.size(), hits, misses, evictions, maxCacheSize,
                            defaultExpiration);
                    }
                } catch (Exception e) {
                    logger.warn("Error retrieving metrics for agent-model-context-cache: {}", e.getMessage());
                    // Fallback to default values
                    return new AgentModelContextCacheStatistics(cache.size(), 0L, 0L, 0L, maxCacheSize, defaultExpiration);
                }
            } catch (Exception e) {
                logger.warn("Error retrieving metrics for agent-model-context-cache: {}", e.getMessage());
                // Fallback to default values
                return new AgentModelContextCacheStatistics(cache.size(), 0L, 0L, 0L, maxCacheSize, defaultExpiration);
            }
        } else {
            // Fallback to default values if MetricsService is not available
            return new AgentModelContextCacheStatistics(cache.size(), 0L, 0L, 0L, maxCacheSize, defaultExpiration);
        }
        
        // Fallback to default values if no snapshot was found
        return new AgentModelContextCacheStatistics(cache.size(), 0L, 0L, 0L, maxCacheSize, defaultExpiration);
    }

    /**
     * Evict the oldest entry from the cache.
     */
    private void evictOldestEntry() {
        String oldestKey = null;
        Instant oldestTime = Instant.now();

        for (Map.Entry<String, CacheEntry> entry : cache.entrySet()) {
            if (entry.getValue().getExpirationTime().isBefore(oldestTime)) {
                oldestTime = entry.getValue().getExpirationTime();
                oldestKey = entry.getKey();
            }
        }

        if (oldestKey != null) {
            cache.remove(oldestKey);
            recordMetrics("agent-model-context-cache", "cache-eviction", true, Duration.ZERO);
            logger.debug("Evicted oldest cache entry: {}", oldestKey);
        }
    }

    /**
     * Start the cleanup task.
     */
    private void startCleanupTask() {
        cleanupExecutor.scheduleAtFixedRate(this::cleanupExpiredEntries, cleanupInterval.toMinutes(),
                cleanupInterval.toMinutes(), TimeUnit.MINUTES);
    }

    /**
     * Clean up expired entries from the cache.
     */
    private void cleanupExpiredEntries() {
        int removedCount = 0;
        for (Map.Entry<String, CacheEntry> entry : cache.entrySet()) {
            if (entry.getValue().isExpired()) {
                cache.remove(entry.getKey());
                removedCount++;
            }
        }

        if (removedCount > 0) {
            recordMetrics("agent-model-context-cache", "cache-eviction", true, Duration.ZERO);
            logger.debug("Cleaned up {} expired cache entries", removedCount);
        }
    }

    /**
     * Deactivate the cache.
     */
    @Deactivate
    public void deactivate() {
        cleanupExecutor.shutdown();
        try {
            if (!cleanupExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            cleanupExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        clear();
        logger.info("Agent Model Context Cache deactivated");
    }

    private void recordMetrics(String domain, String operation, boolean success, Duration duration) {
        MetricsService metrics = metricsService;
        if (metrics != null) {
            try {
                metrics.recordOperation(domain, operation, success, duration);
            } catch (Exception e) {
                logger.warn("Failed to record agent model context cache metrics for operation {} - {}: {}", domain, operation, e.getMessage());
                // Graceful degradation: continue with cache operations even if metrics recording fails
            }
        } else {
            logger.warn("MetricsService not available, cannot record metrics for operation: {} - {}", domain,
                    operation);
        }
    }

    // CacheStatistics extracted to org.openhab.core.ai.reasoning.AgentModelContextCacheStatistics
}
