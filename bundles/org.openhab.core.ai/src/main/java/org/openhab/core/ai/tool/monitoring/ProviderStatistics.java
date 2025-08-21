package org.openhab.core.ai.tool.monitoring;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.base.AbstractStatistics;

/**
 * Consolidated provider statistics that extends the unified monitoring framework.
 * 
 * <p>
 * This class provides comprehensive statistics for provider operations including
 * execution counts, success rates, and latency metrics. It implements both
 * CountsMetrics and LatencyMetrics capability interfaces.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ProviderStatistics extends AbstractStatistics implements CountsMetrics, LatencyMetrics {

    private final long totalDurationNanos;

    /**
     * Create a new ProviderStatistics instance.
     * 
     * @param id unique identifier for this statistics instance
     * @param timestamp timestamp when statistics were collected
     * @param totalCount total number of provider executions
     * @param successCount number of successful executions
     * @param failureCount number of failed executions
     * @param totalDurationNanos total execution duration in nanoseconds
     * @param collectionStartTime when collection started
     * @param collectionEndTime when collection ended
     * @param additionalMeasures additional statistical measures
     * @param data additional monitoring data
     */
    public ProviderStatistics(String id, Instant timestamp, long totalCount, long successCount, long failureCount,
            long totalDurationNanos, @Nullable Instant collectionStartTime, @Nullable Instant collectionEndTime,
            @Nullable Map<String, Double> additionalMeasures, @Nullable Map<String, Object> data) {
        super(id, timestamp, "tool", "provider-statistics", "Provider execution statistics", data, totalCount,
                successCount, failureCount, collectionStartTime, collectionEndTime, additionalMeasures);
        this.totalDurationNanos = totalDurationNanos;
    }

    /**
     * Create a new ProviderStatistics instance with current timestamp.
     * 
     * @param id unique identifier for this statistics instance
     * @param totalCount total number of provider executions
     * @param successCount number of successful executions
     * @param failureCount number of failed executions
     * @param totalDurationNanos total execution duration in nanoseconds
     */
    public ProviderStatistics(String id, long totalCount, long successCount, long failureCount,
            long totalDurationNanos) {
        this(id, Instant.now(), totalCount, successCount, failureCount, totalDurationNanos, null, null, null, null);
    }

    // CountsMetrics interface implementation
    @Override
    public long total() {
        return getTotalCount();
    }

    @Override
    public long success() {
        return getSuccessCount();
    }

    @Override
    public long failure() {
        return getFailureCount();
    }

    // LatencyMetrics interface implementation
    @Override
    public long totalDurationNanos() {
        return totalDurationNanos;
    }

    /**
     * Get the average response time in milliseconds.
     * 
     * @return average response time in milliseconds
     */
    public double getAverageResponseTime() {
        return averageMs(total());
    }

    /**
     * Get the health score based on success rate and response time.
     * 
     * @return health score between 0.0 and 1.0
     */
    public double getHealthScore() {
        double successRate = successRate();
        double avgResponseTime = getAverageResponseTime();

        // Simple health score calculation: success rate weighted more heavily
        // than response time performance
        double responseTimeScore = avgResponseTime < 1000 ? 1.0
                : avgResponseTime < 5000 ? 0.8 : avgResponseTime < 10000 ? 0.6 : 0.4;

        return (successRate * 0.7) + (responseTimeScore * 0.3);
    }
}
