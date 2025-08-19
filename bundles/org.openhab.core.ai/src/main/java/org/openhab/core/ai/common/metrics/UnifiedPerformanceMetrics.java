package org.openhab.core.ai.common.metrics;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Unified implementation of PerformanceMetrics for all AI components.
 * 
 * <p>
 * This class provides a standardized way to track performance metrics across
 * all AI components including:
 * - Execution counts (total, success, failure)
 * - Timing information (total, min, max, average durations)
 * - Time-based tracking (first/last execution times)
 * - Custom metrics for component-specific data
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class UnifiedPerformanceMetrics implements PerformanceMetrics {

    private final long totalCount;
    private final long successCount;
    private final long failureCount;
    private final long totalDurationMs;
    private final long minDurationMs;
    private final long maxDurationMs;
    private final @Nullable Instant firstExecution;
    private final @Nullable Instant lastExecution;
    private final Map<String, Long> customCounters;
    private final Map<String, Duration> customDurations;
    private final Map<String, Object> customMetrics;

    /* package */ UnifiedPerformanceMetrics(PerformanceMetricsBuilder builder) {
        this.totalCount = builder.getTotalCount();
        this.successCount = builder.getSuccessCount();
        this.failureCount = builder.getFailureCount();
        this.totalDurationMs = builder.getTotalDurationMs();
        this.minDurationMs = builder.getMinDurationMs();
        this.maxDurationMs = builder.getMaxDurationMs();
        this.firstExecution = builder.getFirstExecution();
        this.lastExecution = builder.getLastExecution();
        this.customCounters = builder.getCustomCounters();
        this.customDurations = builder.getCustomDurations();
        this.customMetrics = builder.getCustomMetrics();
    }

    /**
     * Create a new builder for UnifiedPerformanceMetrics.
     */
    public static PerformanceMetricsBuilder builder() {
        return new PerformanceMetricsBuilder();
    }

    @Override
    public long getTotalOperations() {
        return totalCount;
    }

    @Override
    public long getSuccessfulOperations() {
        return successCount;
    }

    @Override
    public long getFailedOperations() {
        return failureCount;
    }

    @Override
    public long getTotalProcessingTime() {
        return totalDurationMs;
    }

    @Override
    public double getAverageResponseTime() {
        if (totalCount == 0) {
            return 0.0;
        }
        return (double) totalDurationMs / totalCount;
    }

    @Override
    public @Nullable Instant getLastOperationTime() {
        return lastExecution;
    }

    /**
     * Get the total number of operations (alias for getTotalOperations).
     */
    public long getTotalCount() {
        return totalCount;
    }

    /**
     * Get the number of successful operations (alias for getSuccessfulOperations).
     */
    public long getSuccessCount() {
        return successCount;
    }

    /**
     * Get the number of failed operations (alias for getFailedOperations).
     */
    public long getFailureCount() {
        return failureCount;
    }

    /**
     * Get the total duration of all operations in milliseconds (alias for getTotalProcessingTime).
     */
    public long getTotalDurationMs() {
        return totalDurationMs;
    }

    /**
     * Get the minimum duration of any operation in milliseconds.
     */
    public long getMinDurationMs() {
        return minDurationMs;
    }

    /**
     * Get the maximum duration of any operation in milliseconds.
     */
    public long getMaxDurationMs() {
        return maxDurationMs;
    }

    /**
     * Get the first execution time.
     */
    public @Nullable Instant getFirstExecution() {
        return firstExecution;
    }

    /**
     * Get the last execution time (alias for getLastOperationTime).
     */
    public @Nullable Instant getLastExecution() {
        return lastExecution;
    }

    /**
     * Get all custom counter metrics.
     */
    public Map<String, Long> getCustomCounters() {
        return customCounters;
    }

    /**
     * Get all custom duration metrics.
     */
    public Map<String, Duration> getCustomDurations() {
        return customDurations;
    }

    /**
     * Get all custom metrics.
     */
    public Map<String, Object> getCustomMetrics() {
        return customMetrics;
    }

    /**
     * Get the total duration as a Duration object.
     */
    public Duration getTotalDuration() {
        return Duration.ofMillis(totalDurationMs);
    }

    /**
     * Get the minimum duration as a Duration object.
     */
    public Duration getMinDuration() {
        return Duration.ofMillis(minDurationMs);
    }

    /**
     * Get the maximum duration as a Duration object.
     */
    public Duration getMaxDuration() {
        return Duration.ofMillis(maxDurationMs);
    }

    /**
     * Get the average duration as a Duration object.
     */
    public Duration getAverageDuration() {
        return Duration.ofMillis((long) getAverageResponseTime());
    }

    /**
     * Check if there are any operations recorded.
     */
    public boolean hasOperations() {
        return totalCount > 0;
    }

    /**
     * Check if there are any successful operations.
     */
    public boolean hasSuccessfulOperations() {
        return successCount > 0;
    }

    /**
     * Check if there are any failed operations.
     */
    public boolean hasFailedOperations() {
        return failureCount > 0;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        UnifiedPerformanceMetrics other = (UnifiedPerformanceMetrics) obj;
        return totalCount == other.totalCount && successCount == other.successCount
                && failureCount == other.failureCount && totalDurationMs == other.totalDurationMs
                && minDurationMs == other.minDurationMs && maxDurationMs == other.maxDurationMs
                && Objects.equals(firstExecution, other.firstExecution)
                && Objects.equals(lastExecution, other.lastExecution)
                && Objects.equals(customCounters, other.customCounters)
                && Objects.equals(customDurations, other.customDurations)
                && Objects.equals(customMetrics, other.customMetrics);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalCount, successCount, failureCount, totalDurationMs, minDurationMs, maxDurationMs,
                firstExecution, lastExecution, customCounters, customDurations, customMetrics);
    }

    @Override
    public String toString() {
        return "UnifiedPerformanceMetrics{" + "totalCount=" + totalCount + ", successCount=" + successCount
                + ", failureCount=" + failureCount + ", totalDurationMs=" + totalDurationMs + ", minDurationMs="
                + minDurationMs + ", maxDurationMs=" + maxDurationMs + ", firstExecution=" + firstExecution
                + ", lastExecution=" + lastExecution + ", successRate=" + getSuccessRate() + "%"
                + ", averageResponseTime=" + getAverageResponseTime() + ", customCounters=" + customCounters.size()
                + ", customDurations=" + customDurations.size() + ", customMetrics=" + customMetrics.size() + '}';
    }
}
