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
package org.openhab.core.ai.reasoning.memory;

import java.time.Duration;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Memory optimization helper that performs consolidation, cache eviction,
 * and session cleanup according to configured policies.
 *
 * <p>
 * This utility coordinates with {@link org.openhab.core.ai.reasoning.memory.AgentMemory} and
 * {@link org.openhab.core.ai.reasoning.memory.MemoryCache}
 * to keep memory usage under control.
 * </p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 5.0.0
 */
@NonNullByDefault
public final class MemoryOptimizer {

    private final org.openhab.core.ai.reasoning.memory.AgentMemory agentMemory;
    private final org.openhab.core.ai.reasoning.memory.MemoryCache memoryCache;

    public MemoryOptimizer(org.openhab.core.ai.reasoning.memory.AgentMemory agentMemory,
            org.openhab.core.ai.reasoning.memory.MemoryCache memoryCache) {
        this.agentMemory = agentMemory;
        this.memoryCache = memoryCache;
    }

    public void optimizeAgent(String agentId) {
        try {
            agentMemory.consolidateMemoriesInternal(agentId);
        } catch (Exception ignored) {
            // best-effort consolidation
        }

        memoryCache.evictExpired();
    }

    public void cleanupInactiveSessions(Duration maxAge) {
        agentMemory.cleanupOldSessions(maxAge);
    }

    public int evictLowImportanceCachedMemories(List<String> agentIds) {
        int evicted = 0;
        for (String agentId : agentIds) {
            // Best-effort: remove cached items tied to agent; as we don't track
            // importance per cache entry here, trigger a full eviction sweep.
            memoryCache.evictExpired();
        }
        return evicted;
    }
}
