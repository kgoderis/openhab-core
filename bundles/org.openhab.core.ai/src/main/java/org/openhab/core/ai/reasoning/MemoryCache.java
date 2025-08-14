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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Simple in-memory cache for agent memory entries with time-based eviction.
 *
 * <p>Keys are composed of agentId and memoryId. Values are arbitrary objects
 * representing cached memory payloads. Entries are evicted when their TTL
 * expires or when explicitly removed.</p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 5.0.0
 */
@NonNullByDefault
public final class MemoryCache {

    private static final class CacheValue {
        final Object value;
        final Instant createdAt;

        CacheValue(Object value) {
            this.value = value;
            this.createdAt = Instant.now();
        }
    }

    private final Map<String, CacheValue> cache = new ConcurrentHashMap<>();
    private volatile Duration timeToLive = Duration.ofMinutes(30);

    public void setTimeToLive(Duration ttl) {
        this.timeToLive = ttl;
    }

    public void put(String agentId, String memoryId, Object value) {
        cache.put(key(agentId, memoryId), new CacheValue(value));
    }

    public @Nullable Object get(String agentId, String memoryId) {
        String key = key(agentId, memoryId);
        CacheValue cv = cache.get(key);
        if (cv == null) {
            return null;
        }
        if (isExpired(cv)) {
            cache.remove(key);
            return null;
        }
        return cv.value;
    }

    public void remove(String agentId, String memoryId) {
        cache.remove(key(agentId, memoryId));
    }

    public void clear() {
        cache.clear();
    }

    public int size() {
        return cache.size();
    }

    public void evictExpired() {
        for (Map.Entry<String, CacheValue> e : cache.entrySet()) {
            if (isExpired(e.getValue())) {
                cache.remove(e.getKey());
            }
        }
    }

    private boolean isExpired(CacheValue cv) {
        return cv.createdAt.plus(timeToLive).isBefore(Instant.now());
    }

    private String key(String agentId, String memoryId) {
        return agentId + "::" + memoryId;
    }
}


