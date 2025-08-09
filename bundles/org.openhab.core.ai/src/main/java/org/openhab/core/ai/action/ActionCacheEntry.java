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
package org.openhab.core.ai.action;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Cache entry for action results.
 * 
 * This class represents a cached action result with metadata including
 * creation time, expiration time, and access statistics.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class ActionCacheEntry {

    private final String actionId;
    private final Map<String, Object> parameters;
    private final @Nullable Object result;
    private final Instant createdAt;
    private final Instant expiresAt;
    private final int accessCount;
    private final Instant lastAccessed;

    public ActionCacheEntry(String actionId, Map<String, Object> parameters, @Nullable Object result, Instant createdAt,
            Instant expiresAt) {
        this.actionId = actionId;
        this.parameters = parameters;
        this.result = result;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.accessCount = 0;
        this.lastAccessed = createdAt;
    }

    public ActionCacheEntry(String actionId, Map<String, Object> parameters, @Nullable Object result, Instant createdAt,
            Instant expiresAt, int accessCount, Instant lastAccessed) {
        this.actionId = actionId;
        this.parameters = parameters;
        this.result = result;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.accessCount = accessCount;
        this.lastAccessed = lastAccessed;
    }

    // Getters
    public String getActionId() {
        return actionId;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public @Nullable Object getResult() {
        return result;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public int getAccessCount() {
        return accessCount;
    }

    public Instant getLastAccessed() {
        return lastAccessed;
    }

    /**
     * Check if this cache entry has expired.
     * 
     * @return true if the entry has expired, false otherwise
     */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    /**
     * Create a new cache entry with incremented access count.
     * 
     * @return a new cache entry with updated access statistics
     */
    public ActionCacheEntry withAccess() {
        return new ActionCacheEntry(actionId, parameters, result, createdAt, expiresAt, accessCount + 1, Instant.now());
    }

    @Override
    public String toString() {
        return String.format("ActionCacheEntry{actionId='%s', accessCount=%d, expired=%s}", actionId, accessCount,
                isExpired());
    }
}
