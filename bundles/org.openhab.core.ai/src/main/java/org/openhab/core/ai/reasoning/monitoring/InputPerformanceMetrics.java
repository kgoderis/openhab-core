package org.openhab.core.ai.reasoning.monitoring;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.ThroughputMetrics;
import org.openhab.core.ai.common.monitoring.base.AbstractMetrics;

/**
 * Consolidated input performance metrics that extends the unified monitoring framework.
 * 
 * <p>
 * This class provides comprehensive performance metrics for input processing operations including
 * input routing, batch creation, processing time, and throughput metrics. It implements
 * CountsMetrics, LatencyMetrics, and ThroughputMetrics capability interfaces.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class InputPerformanceMetrics extends AbstractMetrics
        implements CountsMetrics, LatencyMetrics, ThroughputMetrics {

    private final long totalBatchesCreated;
    private final long totalInputsRouted;
    private final int queueSize;
    private final int activeInputsCount;
    private final int batchCount;
    private final int routerCount;
    private final double operationsPerSecond;
    private final double itemsPerSecond;
    private final long peakThroughput;

    /**
     * Create a new InputPerformanceMetrics instance.
     * 
     * @param id unique identifier for this metrics instance
     * @param timestamp timestamp when metrics were collected
     * @param totalOperations total number of inputs processed
     * @param successfulOperations number of successful operations
     * @param failedOperations number of failed operations
     * @param totalProcessingTime total processing time in nanoseconds
     * @param averageResponseTime average response time in milliseconds
     * @param lastOperationTime timestamp of last operation
     * @param totalBatchesCreated total number of batches created
     * @param totalInputsRouted total number of inputs routed
     * @param queueSize current queue size
     * @param activeInputsCount number of active inputs
     * @param batchCount number of batches
     * @param routerCount number of routers
     * @param operationsPerSecond current operations per second
     * @param itemsPerSecond current items per second
     * @param peakThroughput peak throughput achieved
     * @param data additional monitoring data
     */
    public InputPerformanceMetrics(String id, Instant timestamp, long totalOperations, long successfulOperations,
            long failedOperations, long totalProcessingTime, double averageResponseTime,
            @Nullable Instant lastOperationTime, long totalBatchesCreated, long totalInputsRouted, int queueSize,
            int activeInputsCount, int batchCount, int routerCount, double operationsPerSecond, double itemsPerSecond,
            long peakThroughput, @Nullable Map<String, Object> data) {
        super(id, timestamp, "reasoning", "input-performance", "Input performance metrics", data, totalOperations,
                successfulOperations, failedOperations, totalProcessingTime, averageResponseTime, lastOperationTime);
        this.totalBatchesCreated = totalBatchesCreated;
        this.totalInputsRouted = totalInputsRouted;
        this.queueSize = queueSize;
        this.activeInputsCount = activeInputsCount;
        this.batchCount = batchCount;
        this.routerCount = routerCount;
        this.operationsPerSecond = operationsPerSecond;
        this.itemsPerSecond = itemsPerSecond;
        this.peakThroughput = peakThroughput;
    }

    /**
     * Create a new InputPerformanceMetrics instance with current timestamp.
     * 
     * @param id unique identifier for this metrics instance
     * @param totalOperations total number of inputs processed
     * @param successfulOperations number of successful operations
     * @param failedOperations number of failed operations
     * @param totalProcessingTime total processing time in nanoseconds
     * @param averageResponseTime average response time in milliseconds
     * @param totalBatchesCreated total number of batches created
     * @param totalInputsRouted total number of inputs routed
     * @param queueSize current queue size
     * @param activeInputsCount number of active inputs
     * @param batchCount number of batches
     * @param routerCount number of routers
     * @param operationsPerSecond current operations per second
     * @param itemsPerSecond current items per second
     * @param peakThroughput peak throughput achieved
     */
    public InputPerformanceMetrics(String id, long totalOperations, long successfulOperations, long failedOperations,
            long totalProcessingTime, double averageResponseTime, long totalBatchesCreated, long totalInputsRouted,
            int queueSize, int activeInputsCount, int batchCount, int routerCount, double operationsPerSecond,
            double itemsPerSecond, long peakThroughput) {
        this(id, Instant.now(), totalOperations, successfulOperations, failedOperations, totalProcessingTime,
                averageResponseTime, null, totalBatchesCreated, totalInputsRouted, queueSize, activeInputsCount,
                batchCount, routerCount, operationsPerSecond, itemsPerSecond, peakThroughput, null);
    }

    /**
     * Get the total number of batches created.
     * 
     * @return total batches created
     */
    public long getTotalBatchesCreated() {
        return totalBatchesCreated;
    }

    /**
     * Get the total number of inputs routed.
     * 
     * @return total inputs routed
     */
    public long getTotalInputsRouted() {
        return totalInputsRouted;
    }

    /**
     * Get the current queue size.
     * 
     * @return queue size
     */
    public int getQueueSize() {
        return queueSize;
    }

    /**
     * Get the number of active inputs.
     * 
     * @return active inputs count
     */
    public int getActiveInputsCount() {
        return activeInputsCount;
    }

    /**
     * Get the number of batches.
     * 
     * @return batch count
     */
    public int getBatchCount() {
        return batchCount;
    }

    /**
     * Get the number of routers.
     * 
     * @return router count
     */
    public int getRouterCount() {
        return routerCount;
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

    // ThroughputMetrics interface implementation
    @Override
    public double operationsPerSecond() {
        return operationsPerSecond;
    }

    @Override
    public double itemsPerSecond() {
        return itemsPerSecond;
    }

    @Override
    public long peakThroughput() {
        return peakThroughput;
    }

    /**
     * Get the batch creation rate.
     * 
     * @return batch creation rate as a percentage
     */
    public double getBatchCreationRate() {
        return total() > 0 ? (double) totalBatchesCreated / total() * 100.0 : 0.0;
    }

    /**
     * Get the input routing rate.
     * 
     * @return input routing rate as a percentage
     */
    public double getInputRoutingRate() {
        return total() > 0 ? (double) totalInputsRouted / total() * 100.0 : 0.0;
    }

    /**
     * Get the queue utilization rate.
     * 
     * @return queue utilization rate as a percentage
     */
    public double getQueueUtilizationRate() {
        return queueSize > 0 ? (double) activeInputsCount / queueSize * 100.0 : 0.0;
    }

    /**
     * Get the input processing efficiency score.
     * 
     * @return input processing efficiency score between 0.0 and 1.0
     */
    public double getInputProcessingEfficiency() {
        double successRate = successRate();
        double routingRate = total() > 0 ? (double) totalInputsRouted / total() : 0.0;
        double batchRate = total() > 0 ? (double) totalBatchesCreated / total() : 0.0;
        double throughputScore = operationsPerSecond > 0 ? Math.min(1.0, operationsPerSecond / 100.0) : 0.0;
        double latencyScore = getAverageResponseTime() < 500 ? 1.0
                : getAverageResponseTime() < 1000 ? 0.8 : getAverageResponseTime() < 2000 ? 0.6 : 0.4;

        return (successRate * 0.3) + (routingRate * 0.2) + (batchRate * 0.2) + (throughputScore * 0.2)
                + (latencyScore * 0.1);
    }

    /**
     * Check if input processing is performing well (high success rate, good throughput).
     * 
     * @return true if input processing is performing well
     */
    public boolean isPerformingWell() {
        return successRate() > 0.8 && operationsPerSecond > 10.0 && getAverageResponseTime() < 1000;
    }

    /**
     * Check if the queue is congested (high utilization rate).
     * 
     * @return true if the queue is congested
     */
    public boolean isQueueCongested() {
        return getQueueUtilizationRate() > 80.0;
    }

    /**
     * Check if the system is at peak throughput.
     * 
     * @return true if the system is at peak throughput
     */
    public boolean isAtPeakThroughput() {
        return Math.abs(operationsPerSecond - peakThroughput) < 0.001;
    }
}
