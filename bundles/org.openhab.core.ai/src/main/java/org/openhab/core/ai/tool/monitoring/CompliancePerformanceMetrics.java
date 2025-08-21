package org.openhab.core.ai.tool.monitoring;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.base.AbstractMetrics;

/**
 * Consolidated compliance performance metrics that extends the unified monitoring framework.
 * 
 * <p>
 * This class provides comprehensive performance metrics for compliance operations including
 * execution counts, success rates, and latency metrics. It implements both
 * CountsMetrics and LatencyMetrics capability interfaces.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class CompliancePerformanceMetrics extends AbstractMetrics implements CountsMetrics, LatencyMetrics {

    /**
     * Create a new CompliancePerformanceMetrics instance.
     * 
     * @param id unique identifier for this metrics instance
     * @param timestamp timestamp when metrics were collected
     * @param totalOperations total number of compliance operations
     * @param successfulOperations number of successful compliance checks
     * @param failedOperations number of failed compliance checks
     * @param totalProcessingTime total processing time in nanoseconds
     * @param averageResponseTime average response time in milliseconds
     * @param lastOperationTime timestamp of last operation
     * @param data additional monitoring data
     */
    public CompliancePerformanceMetrics(String id, Instant timestamp, long totalOperations, long successfulOperations,
            long failedOperations, long totalProcessingTime, double averageResponseTime,
            @Nullable Instant lastOperationTime, @Nullable Map<String, Object> data) {
        super(id, timestamp, "tool", "compliance-performance", "Compliance performance metrics", data, totalOperations,
                successfulOperations, failedOperations, totalProcessingTime, averageResponseTime, lastOperationTime);
    }

    /**
     * Create a new CompliancePerformanceMetrics instance with current timestamp.
     * 
     * @param id unique identifier for this metrics instance
     * @param totalOperations total number of compliance operations
     * @param successfulOperations number of successful compliance checks
     * @param failedOperations number of failed compliance checks
     * @param totalProcessingTime total processing time in nanoseconds
     * @param averageResponseTime average response time in milliseconds
     */
    public CompliancePerformanceMetrics(String id, long totalOperations, long successfulOperations,
            long failedOperations, long totalProcessingTime, double averageResponseTime) {
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
     * Get the compliance rate.
     * 
     * @return compliance rate as a percentage
     */
    public double getComplianceRate() {
        return successRate() * 100.0;
    }

    /**
     * Get the compliance throughput (checks per second).
     * 
     * @return compliance checks per second
     */
    public double getComplianceThroughput() {
        return getOperationsPerSecond();
    }
}
