package org.openhab.core.ai.action.monitoring;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.base.AbstractMetrics;

/**
 * Consolidated action performance metrics that extends the unified monitoring framework.
 *
 * <p>
 * This class provides comprehensive performance metrics for action operations including
 * execution times, retry attempts, cache performance, and action-specific analytics.
 * It implements CountsMetrics and LatencyMetrics capability interfaces.
 * </p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ActionPerformanceMetrics extends AbstractMetrics implements CountsMetrics, LatencyMetrics {

    private final String actionId;
    private final long minExecutionTimeMs;
    private final long maxExecutionTimeMs;
    private final long totalRetryAttempts;
    private final int cacheSize;
    private final @Nullable Instant firstExecution;

    /**
     * Create a new ActionPerformanceMetrics instance.
     *
     * @param id unique identifier for this metrics instance
     * @param timestamp timestamp when metrics were collected
     * @param totalOperations total number of action executions
     * @param successfulOperations number of successful executions
     * @param failedOperations number of failed executions
     * @param totalProcessingTime total processing time in nanoseconds
     * @param averageResponseTime average response time in milliseconds
     * @param lastOperationTime timestamp of last operation
     * @param actionId unique identifier for the action
     * @param minExecutionTimeMs minimum execution time in milliseconds
     * @param maxExecutionTimeMs maximum execution time in milliseconds
     * @param totalRetryAttempts total number of retry attempts
     * @param cacheSize current cache size
     * @param firstExecution timestamp of first execution
     * @param data additional monitoring data
     */
    public ActionPerformanceMetrics(String id, Instant timestamp, long totalOperations, long successfulOperations,
            long failedOperations, long totalProcessingTime, double averageResponseTime,
            @Nullable Instant lastOperationTime, String actionId, long minExecutionTimeMs, long maxExecutionTimeMs,
            long totalRetryAttempts, int cacheSize, @Nullable Instant firstExecution,
            @Nullable Map<String, Object> data) {
        super(id, timestamp, "action", "action-performance", "Action performance metrics", data, totalOperations,
                successfulOperations, failedOperations, totalProcessingTime, averageResponseTime, lastOperationTime);
        this.actionId = actionId;
        this.minExecutionTimeMs = minExecutionTimeMs;
        this.maxExecutionTimeMs = maxExecutionTimeMs;
        this.totalRetryAttempts = totalRetryAttempts;
        this.cacheSize = cacheSize;
        this.firstExecution = firstExecution;
    }

    /**
     * Create a new ActionPerformanceMetrics instance with current timestamp.
     *
     * @param id unique identifier for this metrics instance
     * @param totalOperations total number of action executions
     * @param successfulOperations number of successful executions
     * @param failedOperations number of failed executions
     * @param totalProcessingTime total processing time in nanoseconds
     * @param averageResponseTime average response time in milliseconds
     * @param actionId unique identifier for the action
     * @param minExecutionTimeMs minimum execution time in milliseconds
     * @param maxExecutionTimeMs maximum execution time in milliseconds
     * @param totalRetryAttempts total number of retry attempts
     * @param cacheSize current cache size
     */
    public ActionPerformanceMetrics(String id, long totalOperations, long successfulOperations, long failedOperations,
            long totalProcessingTime, double averageResponseTime, String actionId, long minExecutionTimeMs,
            long maxExecutionTimeMs, long totalRetryAttempts, int cacheSize) {
        this(id, Instant.now(), totalOperations, successfulOperations, failedOperations, totalProcessingTime,
                averageResponseTime, null, actionId, minExecutionTimeMs, maxExecutionTimeMs, totalRetryAttempts,
                cacheSize, null, null);
    }

    /**
     * Get the action identifier.
     *
     * @return action identifier
     */
    public String getActionId() {
        return actionId;
    }

    /**
     * Get the minimum execution time in milliseconds.
     *
     * @return minimum execution time
     */
    public long getMinExecutionTimeMs() {
        return minExecutionTimeMs;
    }

    /**
     * Get the maximum execution time in milliseconds.
     *
     * @return maximum execution time
     */
    public long getMaxExecutionTimeMs() {
        return maxExecutionTimeMs;
    }

    /**
     * Get the total number of retry attempts.
     *
     * @return total retry attempts
     */
    public long getTotalRetryAttempts() {
        return totalRetryAttempts;
    }

    /**
     * Get the current cache size.
     *
     * @return cache size
     */
    public int getCacheSize() {
        return cacheSize;
    }

    /**
     * Get the timestamp of first execution.
     *
     * @return first execution timestamp, or null if not available
     */
    public @Nullable Instant getFirstExecution() {
        return firstExecution;
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
     * Get the execution time range (max - min) in milliseconds.
     *
     * @return execution time range
     */
    public long getExecutionTimeRange() {
        return maxExecutionTimeMs - minExecutionTimeMs;
    }

    /**
     * Get the retry rate as a percentage.
     *
     * @return retry rate percentage
     */
    public double getRetryRate() {
        return total() > 0 ? (double) totalRetryAttempts / total() : 0.0;
    }

    /**
     * Get the action performance efficiency score.
     *
     * @return action performance efficiency score between 0.0 and 1.0
     */
    public double getActionPerformanceEfficiency() {
        double successRate = successRate();
        double latencyScore = getAverageResponseTime() < 1000 ? 1.0
                : getAverageResponseTime() < 3000 ? 0.8 : getAverageResponseTime() < 5000 ? 0.6 : 0.4;
        double retryPenalty = totalRetryAttempts > 0 ? Math.max(0.0, 1.0 - (getRetryRate() * 0.5)) : 1.0;
        double consistencyScore = getExecutionTimeRange() < 1000 ? 1.0
                : getExecutionTimeRange() < 3000 ? 0.8 : getExecutionTimeRange() < 5000 ? 0.6 : 0.4;

        return (successRate * 0.4) + (latencyScore * 0.3) + (retryPenalty * 0.2) + (consistencyScore * 0.1);
    }

    /**
     * Check if action performance is performing well (high success rate, low retry rate).
     *
     * @return true if action performance is performing well
     */
    public boolean isPerformingWell() {
        return successRate() > 0.9 && getRetryRate() < 0.1 && getAverageResponseTime() < 3000;
    }

    /**
     * Check if there are critical action issues (high failure rate or excessive retries).
     *
     * @return true if there are critical action issues
     */
    public boolean hasCriticalIssues() {
        return successRate() < 0.8 || getRetryRate() > 0.3 || getAverageResponseTime() > 10000;
    }

    /**
     * Builder for ActionPerformanceMetrics.
     */
    public static final class Builder {
        private String id = "";
        private long totalOperations = 0;
        private long successfulOperations = 0;
        private long failedOperations = 0;
        private long totalProcessingTime = 0;
        private double averageResponseTime = 0.0;
        private String actionId = "";
        private long minExecutionTimeMs = Long.MAX_VALUE;
        private long maxExecutionTimeMs = 0;
        private long totalRetryAttempts = 0;
        private int cacheSize = 0;
        private @Nullable Instant firstExecution;
        private @Nullable Map<String, Object> data;

        public Builder() {
            // Default constructor
        }

        public Builder(ActionPerformanceMetrics source) {
            this.id = source.getId();
            this.totalOperations = source.getTotalOperations();
            this.successfulOperations = source.getSuccessfulOperations();
            this.failedOperations = source.getFailedOperations();
            this.totalProcessingTime = source.getTotalProcessingTime();
            this.averageResponseTime = source.getAverageResponseTime();
            this.actionId = source.getActionId();
            this.minExecutionTimeMs = source.getMinExecutionTimeMs();
            this.maxExecutionTimeMs = source.getMaxExecutionTimeMs();
            this.totalRetryAttempts = source.getTotalRetryAttempts();
            this.cacheSize = source.getCacheSize();
            this.firstExecution = source.getFirstExecution();
            this.data = source.getData();
        }

        public Builder withId(String id) {
            this.id = Objects.requireNonNull(id, "id");
            return this;
        }

        public Builder withTotalOperations(long totalOperations) {
            this.totalOperations = totalOperations;
            return this;
        }

        public Builder withSuccessfulOperations(long successfulOperations) {
            this.successfulOperations = successfulOperations;
            return this;
        }

        public Builder withFailedOperations(long failedOperations) {
            this.failedOperations = failedOperations;
            return this;
        }

        public Builder withTotalProcessingTime(long totalProcessingTime) {
            this.totalProcessingTime = totalProcessingTime;
            return this;
        }

        public Builder withAverageResponseTime(double averageResponseTime) {
            this.averageResponseTime = averageResponseTime;
            return this;
        }

        public Builder withActionId(String actionId) {
            this.actionId = Objects.requireNonNull(actionId, "actionId");
            return this;
        }

        public Builder withMinExecutionTimeMs(long minExecutionTimeMs) {
            this.minExecutionTimeMs = minExecutionTimeMs;
            return this;
        }

        public Builder withMaxExecutionTimeMs(long maxExecutionTimeMs) {
            this.maxExecutionTimeMs = maxExecutionTimeMs;
            return this;
        }

        public Builder withTotalRetryAttempts(long totalRetryAttempts) {
            this.totalRetryAttempts = totalRetryAttempts;
            return this;
        }

        public Builder withCacheSize(int cacheSize) {
            this.cacheSize = cacheSize;
            return this;
        }

        public Builder withFirstExecution(@Nullable Instant firstExecution) {
            this.firstExecution = firstExecution;
            return this;
        }

        public Builder withData(@Nullable Map<String, Object> data) {
            this.data = data;
            return this;
        }

        public ActionPerformanceMetrics build() {
            validate();
            return new ActionPerformanceMetrics(id, Instant.now(), totalOperations, successfulOperations,
                    failedOperations, totalProcessingTime, averageResponseTime, null, actionId, minExecutionTimeMs,
                    maxExecutionTimeMs, totalRetryAttempts, cacheSize, firstExecution, data);
        }

        private void validate() {
            if (id.isBlank()) {
                throw new IllegalArgumentException("id must not be blank");
            }
            if (totalOperations < 0) {
                throw new IllegalArgumentException("totalOperations must be non-negative");
            }
            if (successfulOperations < 0) {
                throw new IllegalArgumentException("successfulOperations must be non-negative");
            }
            if (failedOperations < 0) {
                throw new IllegalArgumentException("failedOperations must be non-negative");
            }
            if (totalProcessingTime < 0) {
                throw new IllegalArgumentException("totalProcessingTime must be non-negative");
            }
            if (averageResponseTime < 0) {
                throw new IllegalArgumentException("averageResponseTime must be non-negative");
            }
            if (actionId.isBlank()) {
                throw new IllegalArgumentException("actionId must not be blank");
            }
            if (minExecutionTimeMs < 0) {
                throw new IllegalArgumentException("minExecutionTimeMs must be non-negative");
            }
            if (maxExecutionTimeMs < 0) {
                throw new IllegalArgumentException("maxExecutionTimeMs must be non-negative");
            }
            if (totalRetryAttempts < 0) {
                throw new IllegalArgumentException("totalRetryAttempts must be non-negative");
            }
            if (cacheSize < 0) {
                throw new IllegalArgumentException("cacheSize must be non-negative");
            }
            if (minExecutionTimeMs > maxExecutionTimeMs && maxExecutionTimeMs > 0) {
                throw new IllegalArgumentException("minExecutionTimeMs cannot be greater than maxExecutionTimeMs");
            }
            if (totalOperations != successfulOperations + failedOperations) {
                throw new IllegalArgumentException(
                        "totalOperations must equal successfulOperations + failedOperations");
            }
        }
    }

    /**
     * Create a new builder for ActionPerformanceMetrics.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create a new builder for ActionPerformanceMetrics from an existing instance.
     *
     * @return a new builder instance initialized with this instance's values
     */
    public Builder toBuilder() {
        return new Builder(this);
    }
}
