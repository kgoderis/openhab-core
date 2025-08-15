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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Periodic cleanup helper for memory and cache structures.
 *
 * <p>
 * Intended to be scheduled by the runtime to keep the memory system
 * healthy by clearing old sessions and evicting expired cache entries.
 * </p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 5.0.0
 */
@NonNullByDefault
public final class GarbageCollector {

    private final org.openhab.core.ai.reasoning.memory.AgentMemory agentMemory;
    private final org.openhab.core.ai.reasoning.memory.MemoryCache memoryCache;

    public GarbageCollector(org.openhab.core.ai.reasoning.memory.AgentMemory agentMemory,
            org.openhab.core.ai.reasoning.memory.MemoryCache memoryCache) {
        this.agentMemory = agentMemory;
        this.memoryCache = memoryCache;
    }

    public void runOnce() {
        agentMemory.cleanupOldSessions(Duration.ofHours(12));
        memoryCache.evictExpired();
    }
}
