package org.openhab.core.ai.common.metrics;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Unified delegation performance metrics implementation.
 * 
 * <p>
 * This class provides performance metrics specific to delegation operations,
 * using composition with the unified performance metrics hierarchy.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class DelegationPerformanceMetrics implements PerformanceMetrics {

    private final UnifiedPerformanceMetrics baseMetrics;
    private final int registeredAgents;

    private DelegationPerformanceMetrics(Builder builder) {
        this.baseMetrics = builder.baseMetricsBuilder.build();
        this.registeredAgents = builder.registeredAgents;
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

    public long getTotalDelegations() {
        return getTotalOperations();
    }

    public long getSuccessfulDelegations() {
        return getSuccessfulOperations();
    }

    public long getFailedDelegations() {
        return getFailedOperations();
    }

    public long getTotalDelegationTime() {
        return getTotalProcessingTime();
    }

    public int getRegisteredAgents() {
        return registeredAgents;
    }

    public double getSuccessRate() {
        return baseMetrics.getSuccessRate();
    }

    public double getAverageDelegationTime() {
        return getAverageResponseTime();
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    /**
     * Builder for DelegationPerformanceMetrics.
     */
    public static final class Builder {

        private final PerformanceMetricsBuilder baseMetricsBuilder = new PerformanceMetricsBuilder();
        private int registeredAgents = 0;

        public Builder() {
            // Default constructor
        }

        public Builder(DelegationPerformanceMetrics source) {
            // Copy base metrics
            baseMetricsBuilder.withTotalCount(source.getTotalOperations())
                    .withSuccessCount(source.getSuccessfulOperations()).withFailureCount(source.getFailedOperations())
                    .withTotalDurationMs(source.getTotalProcessingTime())
                    .withLastExecution(source.getLastOperationTime());

            // Copy delegation-specific metrics
            this.registeredAgents = source.registeredAgents;
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

        public Builder withTotalDelegations(long totalDelegations) {
            baseMetricsBuilder.withTotalCount(totalDelegations);
            return this;
        }

        public Builder withSuccessfulDelegations(long successfulDelegations) {
            baseMetricsBuilder.withSuccessCount(successfulDelegations);
            return this;
        }

        public Builder withFailedDelegations(long failedDelegations) {
            baseMetricsBuilder.withFailureCount(failedDelegations);
            return this;
        }

        public Builder withTotalDelegationTime(long totalDelegationTime) {
            baseMetricsBuilder.withTotalDurationMs(totalDelegationTime);
            return this;
        }

        public Builder withRegisteredAgents(int registeredAgents) {
            this.registeredAgents = registeredAgents;
            return this;
        }

        public DelegationPerformanceMetrics build() {
            validate();
            return new DelegationPerformanceMetrics(this);
        }

        private void validate() {
            if (registeredAgents < 0) {
                throw new IllegalArgumentException("registeredAgents must be non-negative");
            }
        }
    }
}
