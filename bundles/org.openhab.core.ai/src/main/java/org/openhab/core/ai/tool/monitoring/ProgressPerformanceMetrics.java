package org.openhab.core.ai.tool.monitoring;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.ThroughputMetrics;
import org.openhab.core.ai.common.monitoring.base.AbstractMetrics;

/**
 * Consolidated progress performance metrics that extends the unified monitoring framework.
 * 
 * <p>
 * This class provides comprehensive performance metrics for progress tracking operations including
 * execution counts, success rates, and throughput metrics. It implements both
 * CountsMetrics and ThroughputMetrics capability interfaces.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ProgressPerformanceMetrics extends AbstractMetrics implements CountsMetrics, ThroughputMetrics {

    private final double operationsPerSecond;
    private final double itemsPerSecond;
    private final long peakThroughput;

    /**
     * Create a new ProgressPerformanceMetrics instance.
     * 
     * @param id unique identifier for this metrics instance
     * @param timestamp timestamp when metrics were collected
     * @param totalOperations total number of progress operations
     * @param successfulOperations number of successful progress updates
     * @param failedOperations number of failed progress updates
     * @param totalProcessingTime total processing time in nanoseconds
     * @param averageResponseTime average response time in milliseconds
     * @param lastOperationTime timestamp of last operation
     * @param operationsPerSecond current operations per second
     * @param itemsPerSecond current items per second
     * @param peakThroughput peak throughput achieved
     * @param data additional monitoring data
     */
    public ProgressPerformanceMetrics(String id, Instant timestamp, long totalOperations, long successfulOperations,
            long failedOperations, long totalProcessingTime, double averageResponseTime,
            @Nullable Instant lastOperationTime, double operationsPerSecond, double itemsPerSecond, long peakThroughput,
            @Nullable Map<String, Object> data) {
        super(id, timestamp, "tool", "progress-performance", "Progress performance metrics", data, totalOperations,
                successfulOperations, failedOperations, totalProcessingTime, averageResponseTime, lastOperationTime);
        this.operationsPerSecond = operationsPerSecond;
        this.itemsPerSecond = itemsPerSecond;
        this.peakThroughput = peakThroughput;
    }

    /**
     * Create a new ProgressPerformanceMetrics instance with current timestamp.
     * 
     * @param id unique identifier for this metrics instance
     * @param totalOperations total number of progress operations
     * @param successfulOperations number of successful progress updates
     * @param failedOperations number of failed progress updates
     * @param totalProcessingTime total processing time in nanoseconds
     * @param averageResponseTime average response time in milliseconds
     * @param operationsPerSecond current operations per second
     * @param itemsPerSecond current items per second
     * @param peakThroughput peak throughput achieved
     */
    public ProgressPerformanceMetrics(String id, long totalOperations, long successfulOperations, long failedOperations,
            long totalProcessingTime, double averageResponseTime, double operationsPerSecond, double itemsPerSecond,
            long peakThroughput) {
        this(id, Instant.now(), totalOperations, successfulOperations, failedOperations, totalProcessingTime,
                averageResponseTime, null, operationsPerSecond, itemsPerSecond, peakThroughput, null);
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
     * Get the progress tracking accuracy.
     * 
     * @return progress tracking accuracy as a percentage
     */
    public double getProgressAccuracy() {
        return successRate() * 100.0;
    }

    /**
     * Get the progress tracking efficiency.
     * 
     * @return progress tracking efficiency score between 0.0 and 1.0
     */
    public double getProgressEfficiency() {
        double accuracy = successRate();
        double throughputScore = operationsPerSecond > 0 ? Math.min(1.0, operationsPerSecond / 50.0) : 0.0;

        return (accuracy * 0.6) + (throughputScore * 0.4);
    }
}
