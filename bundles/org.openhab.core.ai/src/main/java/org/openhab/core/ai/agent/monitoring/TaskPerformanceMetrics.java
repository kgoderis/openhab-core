package org.openhab.core.ai.agent.monitoring;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.ThroughputMetrics;
import org.openhab.core.ai.common.monitoring.base.AbstractMetrics;

/**
 * Consolidated task performance metrics that extends the unified monitoring framework.
 * 
 * <p>
 * This class provides comprehensive performance metrics for task operations including
 * execution counts, success rates, latency metrics, and throughput. It implements
 * CountsMetrics, LatencyMetrics, and ThroughputMetrics capability interfaces.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class TaskPerformanceMetrics extends AbstractMetrics
        implements CountsMetrics, LatencyMetrics, ThroughputMetrics {

    private final String taskId;
    private final double operationsPerSecond;
    private final double itemsPerSecond;
    private final long peakThroughput;

    /**
     * Create a new TaskPerformanceMetrics instance.
     * 
     * @param id unique identifier for this metrics instance
     * @param timestamp timestamp when metrics were collected
     * @param taskId the task identifier
     * @param totalOperations total number of task operations
     * @param successfulOperations number of successful operations
     * @param failedOperations number of failed operations
     * @param totalProcessingTime total processing time in nanoseconds
     * @param averageResponseTime average response time in milliseconds
     * @param lastOperationTime timestamp of last operation
     * @param operationsPerSecond current operations per second
     * @param itemsPerSecond current items per second
     * @param peakThroughput peak throughput achieved
     * @param data additional monitoring data
     */
    public TaskPerformanceMetrics(String id, Instant timestamp, String taskId, long totalOperations,
            long successfulOperations, long failedOperations, long totalProcessingTime, double averageResponseTime,
            @Nullable Instant lastOperationTime, double operationsPerSecond, double itemsPerSecond, long peakThroughput,
            @Nullable Map<String, Object> data) {
        super(id, timestamp, "agent", "task-performance", "Task performance metrics", data, totalOperations,
                successfulOperations, failedOperations, totalProcessingTime, averageResponseTime, lastOperationTime);
        this.taskId = taskId;
        this.operationsPerSecond = operationsPerSecond;
        this.itemsPerSecond = itemsPerSecond;
        this.peakThroughput = peakThroughput;
    }

    /**
     * Create a new TaskPerformanceMetrics instance with current timestamp.
     * 
     * @param id unique identifier for this metrics instance
     * @param taskId the task identifier
     * @param totalOperations total number of task operations
     * @param successfulOperations number of successful operations
     * @param failedOperations number of failed operations
     * @param totalProcessingTime total processing time in nanoseconds
     * @param averageResponseTime average response time in milliseconds
     * @param operationsPerSecond current operations per second
     * @param itemsPerSecond current items per second
     * @param peakThroughput peak throughput achieved
     */
    public TaskPerformanceMetrics(String id, String taskId, long totalOperations, long successfulOperations,
            long failedOperations, long totalProcessingTime, double averageResponseTime, double operationsPerSecond,
            double itemsPerSecond, long peakThroughput) {
        this(id, Instant.now(), taskId, totalOperations, successfulOperations, failedOperations, totalProcessingTime,
                averageResponseTime, null, operationsPerSecond, itemsPerSecond, peakThroughput, null);
    }

    /**
     * Get the task identifier.
     * 
     * @return the task identifier
     */
    public String getTaskId() {
        return taskId;
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
     * Get the task efficiency score.
     * 
     * @return efficiency score between 0.0 and 1.0
     */
    public double getTaskEfficiency() {
        double successRate = successRate();
        double throughputScore = operationsPerSecond > 0 ? Math.min(1.0, operationsPerSecond / 50.0) : 0.0;
        double latencyScore = getAverageResponseTime() < 1000 ? 1.0
                : getAverageResponseTime() < 5000 ? 0.8 : getAverageResponseTime() < 10000 ? 0.6 : 0.4;

        return (successRate * 0.4) + (throughputScore * 0.3) + (latencyScore * 0.3);
    }

    /**
     * Get the task completion rate.
     * 
     * @return task completion rate as a percentage
     */
    public double getTaskCompletionRate() {
        return successRate() * 100.0;
    }
}
