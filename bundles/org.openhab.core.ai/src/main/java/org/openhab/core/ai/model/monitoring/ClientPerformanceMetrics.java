package org.openhab.core.ai.model.monitoring;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.base.AbstractMetrics;

/**
 * Consolidated client performance metrics that extends the unified monitoring framework.
 * 
 * <p>
 * This class provides comprehensive performance metrics for client operations including
 * response times, error rates, and request counts. It implements CountsMetrics and
 * LatencyMetrics capability interfaces.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ClientPerformanceMetrics extends AbstractMetrics implements CountsMetrics, LatencyMetrics {

    private final long minResponseTime;
    private final long maxResponseTime;
    private final double errorRate;

    /**
     * Create a new ClientPerformanceMetrics instance.
     * 
     * @param id unique identifier for this metrics instance
     * @param timestamp timestamp when metrics were collected
     * @param totalOperations total number of client requests
     * @param successfulOperations number of successful requests
     * @param failedOperations number of failed requests
     * @param totalProcessingTime total processing time in nanoseconds
     * @param averageResponseTime average response time in milliseconds
     * @param lastOperationTime timestamp of last operation
     * @param minResponseTime minimum response time in milliseconds
     * @param maxResponseTime maximum response time in milliseconds
     * @param errorRate error rate as a percentage
     * @param data additional monitoring data
     */
    public ClientPerformanceMetrics(String id, Instant timestamp, long totalOperations, long successfulOperations,
            long failedOperations, long totalProcessingTime, double averageResponseTime,
            @Nullable Instant lastOperationTime, long minResponseTime, long maxResponseTime, double errorRate,
            @Nullable Map<String, Object> data) {
        super(id, timestamp, "model", "client-performance", "Client performance metrics", data, totalOperations,
                successfulOperations, failedOperations, totalProcessingTime, averageResponseTime, lastOperationTime);
        this.minResponseTime = minResponseTime;
        this.maxResponseTime = maxResponseTime;
        this.errorRate = errorRate;
    }

    /**
     * Create a new ClientPerformanceMetrics instance with current timestamp.
     * 
     * @param id unique identifier for this metrics instance
     * @param totalOperations total number of client requests
     * @param successfulOperations number of successful requests
     * @param failedOperations number of failed requests
     * @param totalProcessingTime total processing time in nanoseconds
     * @param averageResponseTime average response time in milliseconds
     * @param minResponseTime minimum response time in milliseconds
     * @param maxResponseTime maximum response time in milliseconds
     * @param errorRate error rate as a percentage
     */
    public ClientPerformanceMetrics(String id, long totalOperations, long successfulOperations, long failedOperations,
            long totalProcessingTime, double averageResponseTime, long minResponseTime, long maxResponseTime,
            double errorRate) {
        this(id, Instant.now(), totalOperations, successfulOperations, failedOperations, totalProcessingTime,
                averageResponseTime, null, minResponseTime, maxResponseTime, errorRate, null);
    }

    /**
     * Get the minimum response time in milliseconds.
     * 
     * @return minimum response time
     */
    public long getMinResponseTime() {
        return minResponseTime;
    }

    /**
     * Get the maximum response time in milliseconds.
     * 
     * @return maximum response time
     */
    public long getMaxResponseTime() {
        return maxResponseTime;
    }

    /**
     * Get the error rate as a percentage.
     * 
     * @return error rate percentage
     */
    public double getErrorRate() {
        return errorRate;
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
     * Get the response time range (max - min) in milliseconds.
     * 
     * @return response time range
     */
    public long getResponseTimeRange() {
        return maxResponseTime - minResponseTime;
    }

    /**
     * Get the client performance efficiency score.
     * 
     * @return client performance efficiency score between 0.0 and 1.0
     */
    public double getClientPerformanceEfficiency() {
        double successRate = successRate();
        double latencyScore = getAverageResponseTime() < 1000 ? 1.0
                : getAverageResponseTime() < 3000 ? 0.8 : getAverageResponseTime() < 5000 ? 0.6 : 0.4;
        double errorPenalty = errorRate > 0 ? Math.max(0.0, 1.0 - (errorRate / 100.0)) : 1.0;
        double consistencyScore = getResponseTimeRange() < 1000 ? 1.0
                : getResponseTimeRange() < 3000 ? 0.8 : getResponseTimeRange() < 5000 ? 0.6 : 0.4;

        return (successRate * 0.4) + (latencyScore * 0.3) + (errorPenalty * 0.2) + (consistencyScore * 0.1);
    }

    /**
     * Check if client performance is performing well (high success rate, low error rate).
     * 
     * @return true if client performance is performing well
     */
    public boolean isPerformingWell() {
        return successRate() > 0.9 && errorRate < 5.0 && getAverageResponseTime() < 3000;
    }

    /**
     * Check if there are critical client issues (high error rate or slow response times).
     * 
     * @return true if there are critical client issues
     */
    public boolean hasCriticalIssues() {
        return successRate() < 0.8 || errorRate > 10.0 || getAverageResponseTime() > 10000;
    }
}
