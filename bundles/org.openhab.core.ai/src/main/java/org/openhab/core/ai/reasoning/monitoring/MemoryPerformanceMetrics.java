package org.openhab.core.ai.reasoning.monitoring;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.MemoryMetrics;
import org.openhab.core.ai.common.monitoring.base.AbstractMetrics;

/**
 * Consolidated memory performance metrics that extends the unified monitoring framework.
 * 
 * <p>
 * This class provides comprehensive performance metrics for memory operations including
 * memory storage, retrieval, search performance, and memory utilization. It implements
 * CountsMetrics, LatencyMetrics, and MemoryMetrics capability interfaces.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class MemoryPerformanceMetrics extends AbstractMetrics implements CountsMetrics, LatencyMetrics, MemoryMetrics {

    private final long shortTermMemories;
    private final long longTermMemories;
    private final double averageSearchTime;
    private final double averageStorageTime;
    private final long memoryUsageBytes;
    private final long peakMemoryUsageBytes;
    private final double memoryUtilizationPercentage;

    /**
     * Create a new MemoryPerformanceMetrics instance.
     * 
     * @param id unique identifier for this metrics instance
     * @param timestamp timestamp when metrics were collected
     * @param totalOperations total number of memory operations
     * @param successfulOperations number of successful operations
     * @param failedOperations number of failed operations
     * @param totalProcessingTime total processing time in nanoseconds
     * @param averageResponseTime average response time in milliseconds
     * @param lastOperationTime timestamp of last operation
     * @param shortTermMemories number of short-term memories
     * @param longTermMemories number of long-term memories
     * @param averageSearchTime average search time in milliseconds
     * @param averageStorageTime average storage time in milliseconds
     * @param memoryUsageBytes current memory usage in bytes
     * @param peakMemoryUsageBytes peak memory usage in bytes
     * @param memoryUtilizationPercentage memory utilization percentage
     * @param data additional monitoring data
     */
    public MemoryPerformanceMetrics(String id, Instant timestamp, long totalOperations, long successfulOperations,
            long failedOperations, long totalProcessingTime, double averageResponseTime,
            @Nullable Instant lastOperationTime, long shortTermMemories, long longTermMemories,
            double averageSearchTime, double averageStorageTime, long memoryUsageBytes, long peakMemoryUsageBytes,
            double memoryUtilizationPercentage, @Nullable Map<String, Object> data) {
        super(id, timestamp, "reasoning", "memory-performance", "Memory performance metrics", data, totalOperations,
                successfulOperations, failedOperations, totalProcessingTime, averageResponseTime, lastOperationTime);
        this.shortTermMemories = shortTermMemories;
        this.longTermMemories = longTermMemories;
        this.averageSearchTime = averageSearchTime;
        this.averageStorageTime = averageStorageTime;
        this.memoryUsageBytes = memoryUsageBytes;
        this.peakMemoryUsageBytes = peakMemoryUsageBytes;
        this.memoryUtilizationPercentage = memoryUtilizationPercentage;
    }

    /**
     * Create a new MemoryPerformanceMetrics instance with current timestamp.
     * 
     * @param id unique identifier for this metrics instance
     * @param totalOperations total number of memory operations
     * @param successfulOperations number of successful operations
     * @param failedOperations number of failed operations
     * @param totalProcessingTime total processing time in nanoseconds
     * @param averageResponseTime average response time in milliseconds
     * @param shortTermMemories number of short-term memories
     * @param longTermMemories number of long-term memories
     * @param averageSearchTime average search time in milliseconds
     * @param averageStorageTime average storage time in milliseconds
     * @param memoryUsageBytes current memory usage in bytes
     * @param peakMemoryUsageBytes peak memory usage in bytes
     * @param memoryUtilizationPercentage memory utilization percentage
     */
    public MemoryPerformanceMetrics(String id, long totalOperations, long successfulOperations, long failedOperations,
            long totalProcessingTime, double averageResponseTime, long shortTermMemories, long longTermMemories,
            double averageSearchTime, double averageStorageTime, long memoryUsageBytes, long peakMemoryUsageBytes,
            double memoryUtilizationPercentage) {
        this(id, Instant.now(), totalOperations, successfulOperations, failedOperations, totalProcessingTime,
                averageResponseTime, null, shortTermMemories, longTermMemories, averageSearchTime, averageStorageTime,
                memoryUsageBytes, peakMemoryUsageBytes, memoryUtilizationPercentage, null);
    }

    /**
     * Get the number of short-term memories.
     * 
     * @return short-term memories count
     */
    public long getShortTermMemories() {
        return shortTermMemories;
    }

    /**
     * Get the number of long-term memories.
     * 
     * @return long-term memories count
     */
    public long getLongTermMemories() {
        return longTermMemories;
    }

    /**
     * Get the average search time in milliseconds.
     * 
     * @return average search time
     */
    public double getAverageSearchTime() {
        return averageSearchTime;
    }

    /**
     * Get the average storage time in milliseconds.
     * 
     * @return average storage time
     */
    public double getAverageStorageTime() {
        return averageStorageTime;
    }

    // CountsMetrics interface implementation
    @Override
    public long total() {
        return getTotalOperations();
    }

    @Override
    public long success() {
        return getSuccessfulOperations();
    }

    @Override
    public long failure() {
        return getFailedOperations();
    }

    // LatencyMetrics interface implementation
    @Override
    public long totalDurationNanos() {
        return getTotalProcessingTime();
    }

    // MemoryMetrics interface implementation
    @Override
    public long memoryUsageBytes() {
        return memoryUsageBytes;
    }

    @Override
    public long peakMemoryUsageBytes() {
        return peakMemoryUsageBytes;
    }

    @Override
    public double memoryUtilizationPercentage() {
        return memoryUtilizationPercentage;
    }

    /**
     * Get the total number of memories (short-term + long-term).
     * 
     * @return total memories count
     */
    public long getTotalMemories() {
        return shortTermMemories + longTermMemories;
    }

    /**
     * Get the short-term memory ratio.
     * 
     * @return short-term memory ratio as a percentage
     */
    public double getShortTermMemoryRatio() {
        long total = getTotalMemories();
        return total > 0 ? (double) shortTermMemories / total * 100.0 : 0.0;
    }

    /**
     * Get the long-term memory ratio.
     * 
     * @return long-term memory ratio as a percentage
     */
    public double getLongTermMemoryRatio() {
        long total = getTotalMemories();
        return total > 0 ? (double) longTermMemories / total * 100.0 : 0.0;
    }

    /**
     * Get the memory efficiency score.
     * 
     * @return memory efficiency score between 0.0 and 1.0
     */
    public double getMemoryEfficiency() {
        double successRate = successRate();
        double searchEfficiency = averageSearchTime < 100 ? 1.0
                : averageSearchTime < 500 ? 0.8 : averageSearchTime < 1000 ? 0.6 : 0.4;
        double storageEfficiency = averageStorageTime < 50 ? 1.0
                : averageStorageTime < 200 ? 0.8 : averageStorageTime < 500 ? 0.6 : 0.4;
        double memoryUtilizationScore = memoryUtilizationPercentage < 70 ? 1.0
                : memoryUtilizationPercentage < 85 ? 0.8 : memoryUtilizationPercentage < 95 ? 0.6 : 0.4;
        double latencyScore = getAverageResponseTime() < 200 ? 1.0
                : getAverageResponseTime() < 500 ? 0.8 : getAverageResponseTime() < 1000 ? 0.6 : 0.4;

        return (successRate * 0.3) + (searchEfficiency * 0.2) + (storageEfficiency * 0.2)
                + (memoryUtilizationScore * 0.2) + (latencyScore * 0.1);
    }

    /**
     * Check if memory operations are performing well (high success rate, good search/storage times).
     * 
     * @return true if memory operations are performing well
     */
    public boolean isPerformingWell() {
        return successRate() > 0.9 && averageSearchTime < 500 && averageStorageTime < 200 && !isHighMemoryUsage();
    }

    /**
     * Check if memory usage is high (above 80% utilization).
     * 
     * @return true if memory usage is high
     */
    @Override
    public boolean isHighMemoryUsage() {
        return memoryUtilizationPercentage > 80.0;
    }

    /**
     * Check if memory usage is critical (above 95% utilization).
     * 
     * @return true if memory usage is critical
     */
    @Override
    public boolean isCriticalMemoryUsage() {
        return memoryUtilizationPercentage > 95.0;
    }

    /**
     * Check if the memory system is well-balanced (good distribution between short and long-term).
     * 
     * @return true if the memory system is well-balanced
     */
    public boolean isMemoryWellBalanced() {
        double shortTermRatio = getShortTermMemoryRatio();
        double longTermRatio = getLongTermMemoryRatio();
        return shortTermRatio > 20.0 && shortTermRatio < 80.0 && longTermRatio > 20.0 && longTermRatio < 80.0;
    }

    /**
     * Get the available memory in bytes.
     * 
     * @return available memory in bytes
     */
    @Override
    public long availableMemoryBytes() {
        return Runtime.getRuntime().totalMemory() - memoryUsageBytes;
    }
}
