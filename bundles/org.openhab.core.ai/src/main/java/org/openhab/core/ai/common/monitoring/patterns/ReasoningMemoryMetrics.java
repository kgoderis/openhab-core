/*
 * Copyright (c) 2010-2024 openHAB e.V. and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 */
package org.openhab.core.ai.common.monitoring.patterns;

import java.time.Duration;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricsService;

/**
 * Static utility class for recording reasoning memory metrics.
 * 
 * <p>This class provides static methods for recording various reasoning memory operations
 * including memory operations, analysis, retrieval, and storage.
 * All methods follow the static utility pattern with MetricsService as the first parameter.
 * 
 * <p>Example usage:
 * <pre>{@code
 * // Record memory operation
 * ReasoningMemoryMetrics.recordMemoryOperation(metricsService, "store", "episodic", 
 *     true, Duration.ofMillis(10), 1024, "memory-123");
 * 
 * // Record memory analysis
 * ReasoningMemoryMetrics.recordMemoryAnalysis(metricsService, "pattern-analysis", 
 *     true, Duration.ofMillis(50), 0.85, 100);
 * }</pre>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class ReasoningMemoryMetrics {

    /**
     * Private constructor to prevent instantiation.
     */
    private ReasoningMemoryMetrics() {
        // Utility class
    }

    /**
     * Record memory operation metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param operation the type of memory operation (store, retrieve, update, delete, etc.)
     * @param memoryType the type of memory (episodic, semantic, working, long-term, etc.)
     * @param success whether the memory operation was successful
     * @param duration the duration of the memory operation
     * @param memorySize the size of the memory data in bytes
     * @param memoryId the unique identifier of the memory item
     */
    public static void recordMemoryOperation(MetricsService metricsService, String operation, String memoryType,
            boolean success, Duration duration, long memorySize, @Nullable String memoryId) {
        try {
            var operationBuilder = metricsService.recordOperation("reasoning-memory", "operation")
                    .withSuccess(success)
                    .withDuration(duration.toNanos())
                    .withData("operation", operation)
                    .withData("memoryType", memoryType)
                    .withData("memorySize", memorySize);

            if (memoryId != null) {
                operationBuilder.withData("memoryId", memoryId);
            }

            operationBuilder.record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting memory operations
        }
    }

    /**
     * Record memory analysis metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param analysisType the type of memory analysis (pattern-analysis, similarity-analysis, etc.)
     * @param success whether the memory analysis was successful
     * @param duration the duration of the memory analysis
     * @param analysisAccuracy the accuracy of the analysis (0.0 to 1.0)
     * @param memoryItemsAnalyzed the number of memory items analyzed
     */
    public static void recordMemoryAnalysis(MetricsService metricsService, String analysisType, boolean success,
            Duration duration, double analysisAccuracy, int memoryItemsAnalyzed) {
        try {
            metricsService.recordOperation("reasoning-memory", "analysis")
                    .withSuccess(success)
                    .withDuration(duration.toNanos())
                    .withData("analysisType", analysisType)
                    .withData("analysisAccuracy", analysisAccuracy)
                    .withData("memoryItemsAnalyzed", memoryItemsAnalyzed)
                    .withData("analysisRate", memoryItemsAnalyzed / (duration.toSeconds() + 1.0))
                    .record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting memory operations
        }
    }

    /**
     * Record memory retrieval metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param retrievalType the type of memory retrieval (exact-match, similarity-search, pattern-match, etc.)
     * @param success whether the memory retrieval was successful
     * @param duration the duration of the memory retrieval
     * @param retrievedItems the number of memory items retrieved
     * @param retrievalAccuracy the accuracy of the retrieval (0.0 to 1.0)
     * @param queryComplexity the complexity of the retrieval query (1-10)
     */
    public static void recordMemoryRetrieval(MetricsService metricsService, String retrievalType, boolean success,
            Duration duration, int retrievedItems, double retrievalAccuracy, int queryComplexity) {
        try {
            metricsService.recordOperation("reasoning-memory", "retrieval")
                    .withSuccess(success)
                    .withDuration(duration.toNanos())
                    .withData("retrievalType", retrievalType)
                    .withData("retrievedItems", retrievedItems)
                    .withData("retrievalAccuracy", retrievalAccuracy)
                    .withData("queryComplexity", queryComplexity)
                    .withData("retrievalRate", retrievedItems / (duration.toSeconds() + 1.0))
                    .record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting memory operations
        }
    }

    /**
     * Record memory storage metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param storageType the type of memory storage (persistent, temporary, cached, etc.)
     * @param success whether the memory storage was successful
     * @param duration the duration of the memory storage operation
     * @param storageSize the size of the stored memory data in bytes
     * @param compressionRatio the compression ratio achieved (0.0 to 1.0)
     * @param context additional context data for the storage operation
     */
    public static void recordMemoryStorage(MetricsService metricsService, String storageType, boolean success,
            Duration duration, long storageSize, double compressionRatio, Map<String, Object> context) {
        try {
            var operation = metricsService.recordOperation("reasoning-memory", "storage")
                    .withSuccess(success)
                    .withDuration(duration.toNanos())
                    .withData("storageType", storageType)
                    .withData("storageSize", storageSize)
                    .withData("compressionRatio", compressionRatio)
                    .withData("compressedSize", (long) (storageSize * compressionRatio));

            // Add context data
            context.forEach(operation::withData);

            operation.record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting memory operations
        }
    }

    /**
     * Record memory consolidation metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param consolidationType the type of memory consolidation (short-to-long-term, episodic-to-semantic, etc.)
     * @param success whether the memory consolidation was successful
     * @param duration the duration of the consolidation process
     * @param sourceMemories the number of source memories consolidated
     * @param consolidatedMemories the number of consolidated memories created
     * @param consolidationEfficiency the efficiency of the consolidation process (0.0 to 1.0)
     */
    public static void recordMemoryConsolidation(MetricsService metricsService, String consolidationType, boolean success,
            Duration duration, int sourceMemories, int consolidatedMemories, double consolidationEfficiency) {
        try {
            metricsService.recordOperation("reasoning-memory", "consolidation")
                    .withSuccess(success)
                    .withDuration(duration.toNanos())
                    .withData("consolidationType", consolidationType)
                    .withData("sourceMemories", sourceMemories)
                    .withData("consolidatedMemories", consolidatedMemories)
                    .withData("consolidationEfficiency", consolidationEfficiency)
                    .withData("consolidationRatio", consolidatedMemories / (double) sourceMemories)
                    .record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting memory operations
        }
    }

    /**
     * Record memory performance metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param performanceMetric the type of memory performance metric being measured
     * @param value the performance value
     * @param duration the measurement duration
     * @param memoryContext the context of the memory performance measurement
     * @param context additional context data for the performance metric
     */
    public static void recordMemoryPerformance(MetricsService metricsService, String performanceMetric, double value,
            Duration duration, @Nullable String memoryContext, Map<String, Object> context) {
        try {
            var operation = metricsService.recordOperation("reasoning-memory", "performance")
                    .withSuccess(true)
                    .withDuration(duration.toNanos())
                    .withData("performanceMetric", performanceMetric)
                    .withData("value", value)
                    .withData("durationMs", duration.toMillis());

            if (memoryContext != null) {
                operation.withData("memoryContext", memoryContext);
            }

            // Add context data
            context.forEach(operation::withData);

            operation.record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting memory operations
        }
    }
}
