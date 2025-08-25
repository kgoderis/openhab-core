package org.openhab.core.ai.common.monitoring.service.snapshot;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;

/**
 * Snapshot class for client performance metrics.
 * 
 * <p>
 * This class provides a point-in-time view of client performance metrics
 * including request counts, success/failure rates, and response times.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record ClientPerformanceSnapshot(long totalRequests, long successfulRequests, long failedRequests,
        long totalDurationNanos, long timestampMs) implements MetricsSnapshot, CountsMetrics, LatencyMetrics {

    // CountsMetrics implementation
    @Override
    public long total() {
        return totalRequests;
    }

    @Override
    public long success() {
        return successfulRequests;
    }

    @Override
    public long failure() {
        return failedRequests;
    }

    @Override
    public double successRate() {
        if (totalRequests == 0)
            return 0.0;
        return (successfulRequests * 100.0) / totalRequests;
    }

    // LatencyMetrics implementation
    @Override
    public long totalDurationNanos() {
        return totalDurationNanos;
    }

    /**
     * Get the average response time in milliseconds.
     * 
     * @return average response time in milliseconds
     */
    public double averageResponseTime() {
        if (totalRequests == 0)
            return 0.0;
        return totalDurationNanos / (totalRequests * 1_000_000.0);
    }

    /**
     * Get the timestamp when this snapshot was created.
     * 
     * @return timestamp in milliseconds
     */
    public long timestamp() {
        return timestampMs;
    }

    /**
     * Create a snapshot from client performance data.
     * 
     * @param totalRequests the total number of requests
     * @param successfulRequests the number of successful requests
     * @param failedRequests the number of failed requests
     * @param totalDurationNanos the total duration in nanoseconds
     * @return client performance snapshot
     */
    public static ClientPerformanceSnapshot fromData(long totalRequests, long successfulRequests, long failedRequests,
            long totalDurationNanos) {
        return new ClientPerformanceSnapshot(totalRequests, successfulRequests, failedRequests, totalDurationNanos,
                System.currentTimeMillis());
    }

    /**
     * Create an empty snapshot.
     * 
     * @return empty client performance snapshot
     */
    public static ClientPerformanceSnapshot empty() {
        return new ClientPerformanceSnapshot(0, 0, 0, 0, System.currentTimeMillis());
    }
}
