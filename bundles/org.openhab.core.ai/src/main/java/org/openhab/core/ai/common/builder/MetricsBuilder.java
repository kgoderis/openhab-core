package org.openhab.core.ai.common.builder;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Base builder class for metrics-related objects.
 * 
 * <p>
 * This class provides common functionality for building metrics objects including:
 * - Performance metrics (counters, timings, rates)
 * - Statistics tracking (success/failure counts, averages)
 * - Time-based metrics (first/last execution times)
 * - Custom metric collections
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public abstract class MetricsBuilder<T> extends AbstractBuilder<T> {

    // Common metrics fields
    protected long totalCount = 0;
    protected long successCount = 0;
    protected long failureCount = 0;
    protected long totalDurationMs = 0;
    protected long minDurationMs = Long.MAX_VALUE;
    protected long maxDurationMs = 0;
    protected @Nullable Instant firstExecution;
    protected @Nullable Instant lastExecution;
    protected final Map<String, Long> customCounters = new HashMap<>();
    protected final Map<String, Duration> customDurations = new HashMap<>();
    protected final Map<String, Object> customMetrics = new HashMap<>();

    /**
     * Set the total count of operations.
     */
    public MetricsBuilder<T> withTotalCount(long totalCount) {
        this.totalCount = totalCount;
        return this;
    }

    /**
     * Set the success count.
     */
    public MetricsBuilder<T> withSuccessCount(long successCount) {
        this.successCount = successCount;
        return this;
    }

    /**
     * Set the failure count.
     */
    public MetricsBuilder<T> withFailureCount(long failureCount) {
        this.failureCount = failureCount;
        return this;
    }

    /**
     * Set the total duration in milliseconds.
     */
    public MetricsBuilder<T> withTotalDurationMs(long totalDurationMs) {
        this.totalDurationMs = totalDurationMs;
        return this;
    }

    /**
     * Set the minimum duration in milliseconds.
     */
    public MetricsBuilder<T> withMinDurationMs(long minDurationMs) {
        this.minDurationMs = minDurationMs;
        return this;
    }

    /**
     * Set the maximum duration in milliseconds.
     */
    public MetricsBuilder<T> withMaxDurationMs(long maxDurationMs) {
        this.maxDurationMs = maxDurationMs;
        return this;
    }

    /**
     * Set the first execution time.
     */
    public MetricsBuilder<T> withFirstExecution(@Nullable Instant firstExecution) {
        this.firstExecution = firstExecution;
        return this;
    }

    /**
     * Set the last execution time.
     */
    public MetricsBuilder<T> withLastExecution(@Nullable Instant lastExecution) {
        this.lastExecution = lastExecution;
        return this;
    }

    /**
     * Add a custom counter metric.
     */
    public MetricsBuilder<T> withCustomCounter(String name, long value) {
        this.customCounters.put(Objects.requireNonNull(name, "name"), value);
        return this;
    }

    /**
     * Add a custom duration metric.
     */
    public MetricsBuilder<T> withCustomDuration(String name, Duration duration) {
        this.customDurations.put(Objects.requireNonNull(name, "name"), Objects.requireNonNull(duration, "duration"));
        return this;
    }

    /**
     * Add a custom metric of any type.
     */
    public MetricsBuilder<T> withCustomMetric(String name, Object value) {
        this.customMetrics.put(Objects.requireNonNull(name, "name"), value);
        return this;
    }

    /**
     * Set all custom counters at once.
     */
    public MetricsBuilder<T> withCustomCounters(Map<String, Long> counters) {
        this.customCounters.clear();
        if (counters != null) {
            this.customCounters.putAll(counters);
        }
        return this;
    }

    /**
     * Set all custom durations at once.
     */
    public MetricsBuilder<T> withCustomDurations(Map<String, Duration> durations) {
        this.customDurations.clear();
        if (durations != null) {
            this.customDurations.putAll(durations);
        }
        return this;
    }

    /**
     * Set all custom metrics at once.
     */
    public MetricsBuilder<T> withCustomMetrics(Map<String, Object> metrics) {
        this.customMetrics.clear();
        if (metrics != null) {
            this.customMetrics.putAll(metrics);
        }
        return this;
    }

    /**
     * Calculate and set the success rate as a percentage.
     */
    public MetricsBuilder<T> withCalculatedSuccessRate() {
        if (totalCount > 0) {
            double successRate = (double) successCount / totalCount * 100.0;
            this.customMetrics.put("successRate", successRate);
        }
        return this;
    }

    /**
     * Calculate and set the average duration.
     */
    public MetricsBuilder<T> withCalculatedAverageDuration() {
        if (totalCount > 0) {
            double averageDuration = (double) totalDurationMs / totalCount;
            this.customMetrics.put("averageDurationMs", averageDuration);
        }
        return this;
    }

    @Override
    protected void validate() {
        // Validate metrics-specific constraints
        if (totalCount < 0) {
            addValidationError("totalCount must be non-negative");
        }
        if (successCount < 0) {
            addValidationError("successCount must be non-negative");
        }
        if (failureCount < 0) {
            addValidationError("failureCount must be non-negative");
        }
        if (totalDurationMs < 0) {
            addValidationError("totalDurationMs must be non-negative");
        }
        if (minDurationMs < 0) {
            addValidationError("minDurationMs must be non-negative");
        }
        if (maxDurationMs < 0) {
            addValidationError("maxDurationMs must be non-negative");
        }

        // Validate that success + failure doesn't exceed total
        if (successCount + failureCount > totalCount) {
            addValidationError("successCount + failureCount cannot exceed totalCount");
        }

        // Validate time constraints
        if (firstExecution != null && lastExecution != null && firstExecution.isAfter(lastExecution)) {
            addValidationError("firstExecution cannot be after lastExecution");
        }
    }

    @Override
    protected void doReset() {
        // Reset metrics-specific fields
        totalCount = 0;
        successCount = 0;
        failureCount = 0;
        totalDurationMs = 0;
        minDurationMs = Long.MAX_VALUE;
        maxDurationMs = 0;
        firstExecution = null;
        lastExecution = null;
        customCounters.clear();
        customDurations.clear();
        customMetrics.clear();
    }

    /**
     * Get the total count.
     */
    public long getTotalCount() {
        return totalCount;
    }

    /**
     * Get the success count.
     */
    public long getSuccessCount() {
        return successCount;
    }

    /**
     * Get the failure count.
     */
    public long getFailureCount() {
        return failureCount;
    }

    /**
     * Get the total duration in milliseconds.
     */
    public long getTotalDurationMs() {
        return totalDurationMs;
    }

    /**
     * Get the minimum duration in milliseconds.
     */
    public long getMinDurationMs() {
        return minDurationMs;
    }

    /**
     * Get the maximum duration in milliseconds.
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
     * Get the last execution time.
     */
    public @Nullable Instant getLastExecution() {
        return lastExecution;
    }

    /**
     * Get all custom counters.
     */
    public Map<String, Long> getCustomCounters() {
        return new HashMap<>(customCounters);
    }

    /**
     * Get all custom durations.
     */
    public Map<String, Duration> getCustomDurations() {
        return new HashMap<>(customDurations);
    }

    /**
     * Get all custom metrics.
     */
    public Map<String, Object> getCustomMetrics() {
        return new HashMap<>(customMetrics);
    }
}
