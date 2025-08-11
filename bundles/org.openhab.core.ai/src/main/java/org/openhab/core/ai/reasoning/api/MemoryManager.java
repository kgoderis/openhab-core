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
package org.openhab.core.ai.reasoning.api;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

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

    /**
     * Memory store result.
     */
    class MemoryStoreResult {
        private final boolean success;
        private final String memoryId;
        private final String error;

        public MemoryStoreResult(boolean success, String memoryId, String error) {
            this.success = success;
            this.memoryId = memoryId;
            this.error = error;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMemoryId() {
            return memoryId;
        }

        public String getError() {
            return error;
        }
    }

    /**
     * Memory search result.
     */
    class MemorySearchResult {
        private final String memoryId;
        private final String content;
        private final double relevance;
        private final long timestamp;

        public MemorySearchResult(String memoryId, String content, double relevance, long timestamp) {
            this.memoryId = memoryId;
            this.content = content;
            this.relevance = relevance;
            this.timestamp = timestamp;
        }

        public String getMemoryId() {
            return memoryId;
        }

        public String getContent() {
            return content;
        }

        public double getRelevance() {
            return relevance;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    /**
     * Memory consolidation result.
     */
    class MemoryConsolidationResult {
        private final boolean success;
        private final int consolidatedCount;
        private final String error;

        public MemoryConsolidationResult(boolean success, int consolidatedCount, String error) {
            this.success = success;
            this.consolidatedCount = consolidatedCount;
            this.error = error;
        }

        public boolean isSuccess() {
            return success;
        }

        public int getConsolidatedCount() {
            return consolidatedCount;
        }

        public String getError() {
            return error;
        }
    }

    /**
     * Memory performance metrics.
     */
    class MemoryPerformanceMetrics {
        private final long totalMemories;
        private final long shortTermMemories;
        private final long longTermMemories;
        private final double averageSearchTime;
        private final double averageStorageTime;

        public MemoryPerformanceMetrics(long totalMemories, long shortTermMemories, long longTermMemories,
                double averageSearchTime, double averageStorageTime) {
            this.totalMemories = totalMemories;
            this.shortTermMemories = shortTermMemories;
            this.longTermMemories = longTermMemories;
            this.averageSearchTime = averageSearchTime;
            this.averageStorageTime = averageStorageTime;
        }

        public long getTotalMemories() {
            return totalMemories;
        }

        public long getShortTermMemories() {
            return shortTermMemories;
        }

        public long getLongTermMemories() {
            return longTermMemories;
        }

        public double getAverageSearchTime() {
            return averageSearchTime;
        }

        public double getAverageStorageTime() {
            return averageStorageTime;
        }
    }
}
