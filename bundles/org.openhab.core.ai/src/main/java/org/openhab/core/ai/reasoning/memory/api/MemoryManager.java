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
package org.openhab.core.ai.reasoning.memory.api;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.reasoning.memory.MemoryConsolidationResult;
import org.openhab.core.ai.reasoning.memory.MemoryStoreResult;

/**
 * Interface for memory management in the AI reasoning system.
 * 
 * This interface provides a common abstraction for memory operations,
 * ensuring consistent behavior across all reasoning components.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public interface MemoryManager {

    /**
     * Store short-term memory for an agent.
     * 
     * @param agentId The agent identifier
     * @param memory The memory content
     * @param metadata Optional metadata
     * @return A CompletableFuture containing the storage result
     */
    CompletableFuture<MemoryStoreResult> storeShortTermMemory(String agentId, String memory,
            @Nullable Map<String, Object> metadata);

    /**
     * Store long-term memory for an agent.
     * 
     * @param agentId The agent identifier
     * @param memory The memory content
     * @param metadata Optional metadata
     * @return A CompletableFuture containing the storage result
     */
    CompletableFuture<MemoryStoreResult> storeLongTermMemory(String agentId, String memory,
            @Nullable Map<String, Object> metadata);

    /**
     * Search memories for an agent.
     * 
     * @param agentId The agent identifier
     * @param query The search query
     * @param limit Maximum number of results
     * @return A CompletableFuture containing the search results
     */
    CompletableFuture<List<MemorySearchResult>> searchMemories(String agentId, String query, int limit);

    /**
     * Consolidate memories for an agent.
     * 
     * @param agentId The agent identifier
     * @return A CompletableFuture containing the consolidation result
     */
    CompletableFuture<MemoryConsolidationResult> consolidateMemories(String agentId);

    /**
     * Get memory performance metrics.
     * 
     * @param agentId The agent identifier
     * @return A CompletableFuture containing the performance metrics
     */
    CompletableFuture<MemoryPerformanceMetrics> getPerformanceMetrics(String agentId);

    /**
     * Clear all memories for an agent.
     * 
     * @param agentId The agent identifier
     * @return A CompletableFuture containing the operation result
     */
    CompletableFuture<Boolean> clearMemories(String agentId);

    // Extracted: MemoryStoreResult, MemorySearchResult, MemoryConsolidationResult, MemoryPerformanceMetrics
}
