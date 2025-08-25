package org.openhab.core.ai.common.monitoring.service.snapshot;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.Counts;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;
import org.openhab.core.ai.common.monitoring.api.Timing;

/**
 * Immutable snapshot of action execution performance metrics.
 *
 * <p>
 * This record provides comprehensive performance metrics for action operations including
 * execution times, retry attempts, cache performance, and action-specific analytics.
 * It implements capability interfaces for clean, type-safe metrics access.
 * </p>
 *
 * @param counts basic count metrics (total, success, failure)
 * @param timing latency and duration metrics
 * @param timestampMs timestamp when snapshot was created
 * @param actionId unique identifier for the action
 * @param minExecutionTimeMs minimum execution time in milliseconds
 * @param maxExecutionTimeMs maximum execution time in milliseconds
 * @param totalRetryAttempts total number of retry attempts
 * @param cacheSize current cache size
 * @param firstExecution timestamp of first execution
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record ActionExecutionSnapshot(Counts counts, Timing timing, long timestampMs, String actionId,
        long minExecutionTimeMs, long maxExecutionTimeMs, long totalRetryAttempts, int cacheSize,
        @Nullable Instant firstExecution) implements MetricsSnapshot, CountsMetrics, LatencyMetrics {

    // CountsMetrics implementation
    @Override
    public long total() {
        return counts.total();
    }

    @Override
    public long success() {
        return counts.success();
    }

    @Override
    public long failure() {
        return counts.failure();
    }

    @Override
    public double successRate() {
        return CountsMetrics.super.successRate();
    }

    // LatencyMetrics implementation
    @Override
    public long totalDurationNanos() {
        return timing.totalDurationNanos();
    }

    public double averageMs() {
        return LatencyMetrics.super.averageMs(total());
    }

    public double operationsPerSecond() {
        if (totalDurationNanos() == 0)
            return 0.0;
        double durationSeconds = totalDurationNanos() / 1_000_000_000.0;
        return total() / durationSeconds;
    }

    // Domain-specific methods
    public double getFailureRate() {
        return total() > 0 ? (double) failure() / total() : 0.0;
    }

    public double getRetryRate() {
        return total() > 0 ? (double) totalRetryAttempts / total() : 0.0;
    }

    public double getCacheHitRate() {
        // Simple approximation: assume cache improves performance
        double avgMs = averageMs();
        if (avgMs == 0)
            return 0.0;
        return Math.min(1.0, 100.0 / avgMs); // Higher hit rate for faster operations
    }

    public double getExecutionEfficiency() {
        double successRateDecimal = successRate() / 100.0;
        double retryPenalty = 1.0 - (getRetryRate() * 0.1); // Penalty for retries
        double performanceScore = maxExecutionTimeMs > 0
                ? Math.min(1.0, minExecutionTimeMs / (double) maxExecutionTimeMs)
                : 1.0;

        return successRateDecimal * retryPenalty * performanceScore;
    }

    /**
     * Create an empty ActionExecutionSnapshot for the given action.
     */
    public static ActionExecutionSnapshot empty(String actionId) {
        return new ActionExecutionSnapshot(new Counts(0, 0, 0), new Timing(0), System.currentTimeMillis(), actionId, 0,
                0, 0, 0, null);
    }
}
