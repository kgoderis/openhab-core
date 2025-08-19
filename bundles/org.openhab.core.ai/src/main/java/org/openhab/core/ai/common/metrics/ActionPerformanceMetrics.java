package org.openhab.core.ai.common.metrics;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Unified action performance metrics implementation.
 * 
 * <p>
 * This class provides performance metrics specific to action operations,
 * using composition with the unified performance metrics hierarchy.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class ActionPerformanceMetrics implements PerformanceMetrics {

    private final UnifiedPerformanceMetrics baseMetrics;
    private final String actionId;
    private final long minExecutionTimeMs;
    private final long maxExecutionTimeMs;
    private final @Nullable Instant firstExecution;

    private ActionPerformanceMetrics(Builder builder) {
        this.baseMetrics = builder.baseMetricsBuilder.build();
        this.actionId = builder.actionId;
        this.minExecutionTimeMs = builder.minExecutionTimeMs;
        this.maxExecutionTimeMs = builder.maxExecutionTimeMs;
        this.firstExecution = builder.firstExecution;
    }

    @Override
    public long getTotalOperations() {
        return baseMetrics.getTotalOperations();
    }

    @Override
    public long getSuccessfulOperations() {
        return baseMetrics.getSuccessfulOperations();
    }

    @Override
    public long getFailedOperations() {
        return baseMetrics.getFailedOperations();
    }

    @Override
    public long getTotalProcessingTime() {
        return baseMetrics.getTotalProcessingTime();
    }

    @Override
    public double getAverageResponseTime() {
        return baseMetrics.getAverageResponseTime();
    }

    @Override
    public @Nullable Instant getLastOperationTime() {
        return baseMetrics.getLastOperationTime();
    }

    public String getActionId() {
        return actionId;
    }

    public long getMinExecutionTimeMs() {
        return minExecutionTimeMs;
    }

    public long getMaxExecutionTimeMs() {
        return maxExecutionTimeMs;
    }

    public @Nullable Instant getFirstExecution() {
        return firstExecution;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    /**
     * Builder for ActionPerformanceMetrics.
     */
    public static final class Builder {

        private final PerformanceMetricsBuilder baseMetricsBuilder = new PerformanceMetricsBuilder();
        private String actionId = "";
        private long minExecutionTimeMs = Long.MAX_VALUE;
        private long maxExecutionTimeMs = 0;
        private @Nullable Instant firstExecution;

        public Builder() {
            // Default constructor
        }

        public Builder(ActionPerformanceMetrics source) {
            // Copy base metrics
            baseMetricsBuilder.withTotalCount(source.getTotalOperations())
                    .withSuccessCount(source.getSuccessfulOperations()).withFailureCount(source.getFailedOperations())
                    .withTotalDurationMs(source.getTotalProcessingTime())
                    .withLastExecution(source.getLastOperationTime());

            // Copy action-specific metrics
            this.actionId = source.actionId;
            this.minExecutionTimeMs = source.minExecutionTimeMs;
            this.maxExecutionTimeMs = source.maxExecutionTimeMs;
            this.firstExecution = source.firstExecution;
        }

        public Builder withTotalCount(long totalCount) {
            baseMetricsBuilder.withTotalCount(totalCount);
            return this;
        }

        public Builder withSuccessCount(long successCount) {
            baseMetricsBuilder.withSuccessCount(successCount);
            return this;
        }

        public Builder withFailureCount(long failureCount) {
            baseMetricsBuilder.withFailureCount(failureCount);
            return this;
        }

        public Builder withTotalDurationMs(long totalDurationMs) {
            baseMetricsBuilder.withTotalDurationMs(totalDurationMs);
            return this;
        }

        public Builder withLastExecution(@Nullable Instant lastExecution) {
            baseMetricsBuilder.withLastExecution(lastExecution);
            return this;
        }

        public Builder withActionId(String actionId) {
            this.actionId = actionId;
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

        public Builder withFirstExecution(@Nullable Instant firstExecution) {
            this.firstExecution = firstExecution;
            return this;
        }

        public ActionPerformanceMetrics build() {
            validate();
            return new ActionPerformanceMetrics(this);
        }

        private void validate() {
            if (actionId == null) {
                throw new IllegalArgumentException("actionId must not be null");
            }
            if (minExecutionTimeMs < 0) {
                throw new IllegalArgumentException("minExecutionTimeMs must be non-negative");
            }
            if (maxExecutionTimeMs < 0) {
                throw new IllegalArgumentException("maxExecutionTimeMs must be non-negative");
            }
            if (minExecutionTimeMs > maxExecutionTimeMs && maxExecutionTimeMs > 0) {
                throw new IllegalArgumentException("minExecutionTimeMs cannot be greater than maxExecutionTimeMs");
            }
        }
    }
}
