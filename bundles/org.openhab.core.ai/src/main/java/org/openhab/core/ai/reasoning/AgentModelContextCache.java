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
package org.openhab.core.ai.reasoning;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
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
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);
    private final AtomicLong cacheEvictions = new AtomicLong(0);

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
    public AgentModelContextBuilder.AgentModelContext get(String contextId) {
        CacheEntry entry = cache.get(contextId);

        if (entry == null) {
            cacheMisses.incrementAndGet();
            logger.debug("Cache miss for context: {}", contextId);
            return null;
        }

        if (entry.isExpired()) {
            cache.remove(contextId);
            cacheEvictions.incrementAndGet();
            cacheMisses.incrementAndGet();
            logger.debug("Cache hit for expired context: {}", contextId);
            return null;
        }

        cacheHits.incrementAndGet();
        entry.updateLastAccess();
        logger.debug("Cache hit for context: {}", contextId);
        return entry.getContext();
    }

    /**
     * Put a context into the cache.
     * 
     * @param context The context to cache
     * @return True if successfully cached, false otherwise
     */
    public boolean put(AgentModelContextBuilder.AgentModelContext context) {
        return put(context, defaultExpiration);
    }

    /**
     * Put a context into the cache with custom expiration.
     * 
     * @param context The context to cache
     * @param expiration The expiration time for this context
     * @return True if successfully cached, false otherwise
     */
    public boolean put(AgentModelContextBuilder.AgentModelContext context, Duration expiration) {
        if (context == null) {
            logger.warn("Attempted to cache null context");
            return false;
        }

        String contextId = context.getContextId();
        if (contextId == null || contextId.trim().isEmpty()) {
            logger.warn("Attempted to cache context with null or empty ID");
            return false;
        }

        // Check cache size limit
        if (cache.size() >= maxCacheSize) {
            evictLeastRecentlyUsed();
        }

        CacheEntry entry = new CacheEntry(context, expiration);
        cache.put(contextId, entry);

        logger.debug("Cached context: {} with expiration: {}", contextId, expiration);
        return true;
    }

    /**
     * Remove a context from the cache.
     * 
     * @param contextId The context ID to remove
     * @return True if the context was removed, false if not found
     */
    public boolean remove(String contextId) {
        CacheEntry removed = cache.remove(contextId);
        if (removed != null) {
            logger.debug("Removed context from cache: {}", contextId);
            return true;
        }
        return false;
    }

    /**
     * Clear all contexts from the cache.
     */
    public void clear() {
        int size = cache.size();
        cache.clear();
        logger.info("Cleared {} contexts from cache", size);
    }

    /**
     * Check if a context exists in the cache.
     * 
     * @param contextId The context ID to check
     * @return True if the context exists and is not expired, false otherwise
     */
    public boolean contains(String contextId) {
        CacheEntry entry = cache.get(contextId);
        return entry != null && !entry.isExpired();
    }

    /**
     * Get cache statistics.
     * 
     * @return Cache statistics
     */
    public AgentModelContextCacheStatistics getStatistics() {
        return new AgentModelContextCacheStatistics(cache.size(), cacheHits.get(), cacheMisses.get(),
                cacheEvictions.get(), maxCacheSize, defaultExpiration);
    }

    /**
     * Get the current cache size.
     * 
     * @return Number of contexts currently in the cache
     */
    public int size() {
        return cache.size();
    }

    /**
     * Check if the cache is empty.
     * 
     * @return True if the cache is empty, false otherwise
     */
    public boolean isEmpty() {
        return cache.isEmpty();
    }

    /**
     * Get the maximum cache size.
     * 
     * @return Maximum number of contexts allowed in the cache
     */
    public int getMaxCacheSize() {
        return maxCacheSize;
    }

    /**
     * Get the default expiration time.
     * 
     * @return Default expiration time for cached contexts
     */
    public Duration getDefaultExpiration() {
        return defaultExpiration;
    }

    /**
     * Start the cleanup task.
     */
    private void startCleanupTask() {
        cleanupExecutor.scheduleAtFixedRate(this::cleanupExpiredEntries, cleanupInterval.toMillis(),
                cleanupInterval.toMillis(), TimeUnit.MILLISECONDS);
        logger.debug("Started cache cleanup task with interval: {}", cleanupInterval);
    }

    /**
     * Clean up expired entries from the cache.
     */
    private void cleanupExpiredEntries() {
        try {
            int initialSize = cache.size();
            cache.entrySet().removeIf(entry -> {
                boolean expired = entry.getValue().isExpired();
                if (expired) {
                    cacheEvictions.incrementAndGet();
                    logger.debug("Cleaned up expired context: {}", entry.getKey());
                }
                return expired;
            });

            int removedCount = initialSize - cache.size();
            if (removedCount > 0) {
                logger.debug("Cache cleanup removed {} expired entries", removedCount);
            }
        } catch (Exception e) {
            logger.error("Error during cache cleanup", e);
        }
    }

    /**
     * Evict the least recently used entry from the cache.
     */
    private void evictLeastRecentlyUsed() {
        String lruKey = null;
        Instant lruTime = Instant.now();

        for (Map.Entry<String, CacheEntry> entry : cache.entrySet()) {
            if (entry.getValue().getLastAccess().isBefore(lruTime)) {
                lruTime = entry.getValue().getLastAccess();
                lruKey = entry.getKey();
            }
        }

        if (lruKey != null) {
            cache.remove(lruKey);
            cacheEvictions.incrementAndGet();
            logger.debug("Evicted least recently used context: {}", lruKey);
        }
    }

    /**
     * Cleanup on deactivation.
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

    /**
     * Cache Entry class.
     */
    // Extracted: org.openhab.core.ai.reasoning.CacheEntry

    /**
     * Cache Statistics class.
     */
    // CacheStatistics extracted to org.openhab.core.ai.reasoning.AgentModelContextCacheStatistics
}
