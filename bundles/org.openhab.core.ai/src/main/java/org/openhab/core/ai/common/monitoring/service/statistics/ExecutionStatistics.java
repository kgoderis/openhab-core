package org.openhab.core.ai.common.monitoring.service.statistics;

import java.time.Duration;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;

/**
 * Statistics class for execution operations.
 * 
 * <p>
 * This class provides computed statistics and insights about execution
 * operations derived from metrics data over time ranges. It implements
 * capability interfaces for clean, type-safe statistics access.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record ExecutionStatistics(long totalExecutions, long successfulExecutions, long failedExecutions,
        long totalDurationNanos, Duration timeRange,
        long timestampMs) implements StatisticsSnapshot, CountsMetrics, TrendMetrics, PercentileMetrics {

    // CountsMetrics implementation
    @Override
    public long total() {
        return totalExecutions;
    }

    @Override
    public long success() {
        return successfulExecutions;
    }

    @Override
    public long failure() {
        return failedExecutions;
    }

    @Override
    public double successRate() {
        if (totalExecutions == 0)
            return 0.0;
        return (successfulExecutions * 100.0) / totalExecutions;
    }

    // TrendMetrics implementation
    @Override
    public double trendPercentage() {
        // For execution statistics, we calculate trend based on success rate
        // This is a simplified implementation - in practice, you'd want historical data
        return 0.0; // Placeholder - would need historical data for proper trend calculation
    }

    @Override
    public String trendDirection() {
        double trend = trendPercentage();
        if (trend > 1.0)
            return "increasing";
        if (trend < -1.0)
            return "decreasing";
        return "stable";
    }

    @Override
    public double changeRate() {
        return trendPercentage() / timeRange.toDays();
    }

    // PercentileMetrics implementation
    @Override
    public double percentile50() {
        // For execution statistics, percentiles would be calculated from execution times
        // This is a simplified implementation
        return totalExecutions > 0 ? (double) totalDurationNanos / totalExecutions / 1_000_000.0 : 0.0;
    }

    @Override
    public double percentile90() {
        // Simplified implementation - would need actual execution time distribution
        return percentile50() * 1.5;
    }

    @Override
    public double percentile95() {
        // Simplified implementation - would need actual execution time distribution
        return percentile50() * 2.0;
    }

    @Override
    public double percentile99() {
        // Simplified implementation - would need actual execution time distribution
        return percentile50() * 3.0;
    }

    // Additional execution specific methods
    /**
     * Get execution success rate as a percentage.
     * 
     * @return execution success rate (0.0 to 100.0)
     */
    public double getExecutionSuccessRate() {
        return successRate();
    }

    /**
     * Get execution failure rate as a percentage.
     * 
     * @return execution failure rate (0.0 to 100.0)
     */
    public double getExecutionFailureRate() {
        if (totalExecutions == 0)
            return 0.0;
        return (failedExecutions * 100.0) / totalExecutions;
    }

    /**
     * Get average execution time in milliseconds.
     * 
     * @return average execution time in milliseconds
     */
    public double getAverageExecutionTimeMs() {
        if (totalExecutions == 0)
            return 0.0;
        return (double) totalDurationNanos / totalExecutions / 1_000_000.0;
    }

    /**
     * Get executions per second.
     * 
     * @return executions per second
     */
    public double getExecutionsPerSecond() {
        if (timeRange.toNanos() == 0)
            return 0.0;
        return totalExecutions / (timeRange.toNanos() / 1_000_000_000.0);
    }

    /**
     * Create statistics from execution data.
     * 
     * @param totalExecutions total number of executions
     * @param successfulExecutions number of successful executions
     * @param failedExecutions number of failed executions
     * @param totalDurationNanos total duration of all executions in nanoseconds
     * @param timeRange time range for statistics
     * @return execution statistics
     */
    public static ExecutionStatistics fromExecutionData(long totalExecutions, long successfulExecutions,
            long failedExecutions, long totalDurationNanos, Duration timeRange) {

        return new ExecutionStatistics(totalExecutions, successfulExecutions, failedExecutions, totalDurationNanos,
                Objects.requireNonNull(timeRange, "timeRange"), System.currentTimeMillis());
    }

    /**
     * Create empty statistics.
     * 
     * @param timeRange time range for statistics
     * @return empty execution statistics
     */
    public static ExecutionStatistics empty(Duration timeRange) {
        return new ExecutionStatistics(0L, 0L, 0L, 0L, timeRange, System.currentTimeMillis());
    }
}
