package org.openhab.core.ai.tool.monitoring;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.base.AbstractMetrics;

/**
 * Consolidated filter performance metrics that extends the unified monitoring framework.
 * 
 * <p>
 * This class provides comprehensive performance metrics for filter operations including
 * execution counts, success rates, and latency metrics. It implements both
 * CountsMetrics and LatencyMetrics capability interfaces.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class FilterPerformanceMetrics extends AbstractMetrics implements CountsMetrics, LatencyMetrics {

    /**
     * Create a new FilterPerformanceMetrics instance.
     * 
     * @param id unique identifier for this metrics instance
     * @param timestamp timestamp when metrics were collected
     * @param totalOperations total number of filter operations
     * @param successfulOperations number of successful filter operations
     * @param failedOperations number of failed filter operations
     * @param totalProcessingTime total processing time in nanoseconds
     * @param averageResponseTime average response time in milliseconds
     * @param lastOperationTime timestamp of last operation
     * @param data additional monitoring data
     */
    public FilterPerformanceMetrics(String id, Instant timestamp, long totalOperations, long successfulOperations,
            long failedOperations, long totalProcessingTime, double averageResponseTime,
            @Nullable Instant lastOperationTime, @Nullable Map<String, Object> data) {
        super(id, timestamp, "tool", "filter-performance", "Filter performance metrics", data, totalOperations,
                successfulOperations, failedOperations, totalProcessingTime, averageResponseTime, lastOperationTime);
    }

    /**
     * Create a new FilterPerformanceMetrics instance with current timestamp.
     * 
     * @param id unique identifier for this metrics instance
     * @param totalOperations total number of filter operations
     * @param successfulOperations number of successful filter operations
     * @param failedOperations number of failed filter operations
     * @param totalProcessingTime total processing time in nanoseconds
     * @param averageResponseTime average response time in milliseconds
     */
    public FilterPerformanceMetrics(String id, long totalOperations, long successfulOperations, long failedOperations,
            long totalProcessingTime, double averageResponseTime) {
        this(id, Instant.now(), totalOperations, successfulOperations, failedOperations, totalProcessingTime,
                averageResponseTime, null, null);
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

    /**
     * Get the filter accuracy rate.
     * 
     * @return filter accuracy as a percentage
     */
    public double getFilterAccuracy() {
        return successRate() * 100.0;
    }

    /**
     * Get the filter throughput (operations per second).
     * 
     * @return filter operations per second
     */
    public double getFilterThroughput() {
        return getOperationsPerSecond();
    }

    /**
     * Get the filter efficiency score.
     * 
     * @return filter efficiency score between 0.0 and 1.0
     */
    public double getFilterEfficiency() {
        double accuracy = successRate();
        double speedScore = getAverageResponseTime() < 100 ? 1.0
                : getAverageResponseTime() < 500 ? 0.8 : getAverageResponseTime() < 1000 ? 0.6 : 0.4;

        return (accuracy * 0.7) + (speedScore * 0.3);
    }
}
