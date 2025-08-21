package org.openhab.core.ai.tool.monitoring;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.base.AbstractMetrics;

/**
 * Consolidated security performance metrics that extends the unified monitoring framework.
 * 
 * <p>
 * This class provides comprehensive performance metrics for security operations including
 * authentication counts, success rates, and latency metrics. It implements both
 * CountsMetrics and LatencyMetrics capability interfaces.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class SecurityPerformanceMetrics extends AbstractMetrics implements CountsMetrics, LatencyMetrics {

    /**
     * Create a new SecurityPerformanceMetrics instance.
     * 
     * @param id unique identifier for this metrics instance
     * @param timestamp timestamp when metrics were collected
     * @param totalOperations total number of security operations
     * @param successfulOperations number of successful security checks
     * @param failedOperations number of failed security checks
     * @param totalProcessingTime total processing time in nanoseconds
     * @param averageResponseTime average response time in milliseconds
     * @param lastOperationTime timestamp of last operation
     * @param data additional monitoring data
     */
    public SecurityPerformanceMetrics(String id, Instant timestamp, long totalOperations, long successfulOperations,
            long failedOperations, long totalProcessingTime, double averageResponseTime,
            @Nullable Instant lastOperationTime, @Nullable Map<String, Object> data) {
        super(id, timestamp, "tool", "security-performance", "Security performance metrics", data, totalOperations,
                successfulOperations, failedOperations, totalProcessingTime, averageResponseTime, lastOperationTime);
    }

    /**
     * Create a new SecurityPerformanceMetrics instance with current timestamp.
     * 
     * @param id unique identifier for this metrics instance
     * @param totalOperations total number of security operations
     * @param successfulOperations number of successful security checks
     * @param failedOperations number of failed security checks
     * @param totalProcessingTime total processing time in nanoseconds
     * @param averageResponseTime average response time in milliseconds
     */
    public SecurityPerformanceMetrics(String id, long totalOperations, long successfulOperations, long failedOperations,
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
     * Get the security success rate.
     * 
     * @return security success rate as a percentage
     */
    public double getSecuritySuccessRate() {
        return successRate() * 100.0;
    }

    /**
     * Get the security throughput (checks per second).
     * 
     * @return security checks per second
     */
    public double getSecurityThroughput() {
        return getOperationsPerSecond();
    }

    /**
     * Get the security risk score.
     * 
     * @return security risk score between 0.0 and 1.0
     */
    public double getSecurityRiskScore() {
        return 1.0 - successRate();
    }
}
