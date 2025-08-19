package org.openhab.core.ai.common.metrics;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Unified memory performance metrics implementation.
 * 
 * <p>
 * This class provides performance metrics specific to memory operations,
 * using composition with the unified performance metrics hierarchy.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class MemoryPerformanceMetrics implements PerformanceMetrics {

    private final UnifiedPerformanceMetrics baseMetrics;
    private final long totalStores;
    private final long totalRetrievals;
    private final long totalConsolidations;
    private final long totalPatternRecognitions;
    private final int shortTermMemoryCount;
    private final int longTermMemoryCount;
    private final int patternCount;

    private MemoryPerformanceMetrics(Builder builder) {
        this.baseMetrics = builder.baseMetricsBuilder.build();
        this.totalStores = builder.totalStores;
        this.totalRetrievals = builder.totalRetrievals;
        this.totalConsolidations = builder.totalConsolidations;
        this.totalPatternRecognitions = builder.totalPatternRecognitions;
        this.shortTermMemoryCount = builder.shortTermMemoryCount;
        this.longTermMemoryCount = builder.longTermMemoryCount;
        this.patternCount = builder.patternCount;
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

    public long getTotalStores() {
        return totalStores;
    }

    public long getTotalRetrievals() {
        return totalRetrievals;
    }

    public long getTotalConsolidations() {
        return totalConsolidations;
    }

    public long getTotalPatternRecognitions() {
        return totalPatternRecognitions;
    }

    public int getShortTermMemoryCount() {
        return shortTermMemoryCount;
    }

    public int getLongTermMemoryCount() {
        return longTermMemoryCount;
    }

    public int getPatternCount() {
        return patternCount;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    /**
     * Builder for MemoryPerformanceMetrics.
     */
    public static final class Builder {

        private final PerformanceMetricsBuilder baseMetricsBuilder = new PerformanceMetricsBuilder();
        private long totalStores;
        private long totalRetrievals;
        private long totalConsolidations;
        private long totalPatternRecognitions;
        private int shortTermMemoryCount;
        private int longTermMemoryCount;
        private int patternCount;

        public Builder() {
            // Default constructor
        }

        public Builder(MemoryPerformanceMetrics source) {
            // Copy base metrics
            baseMetricsBuilder.withTotalCount(source.getTotalOperations())
                    .withSuccessCount(source.getSuccessfulOperations()).withFailureCount(source.getFailedOperations())
                    .withTotalDurationMs(source.getTotalProcessingTime())
                    .withLastExecution(source.getLastOperationTime());

            // Copy memory-specific metrics
            this.totalStores = source.totalStores;
            this.totalRetrievals = source.totalRetrievals;
            this.totalConsolidations = source.totalConsolidations;
            this.totalPatternRecognitions = source.totalPatternRecognitions;
            this.shortTermMemoryCount = source.shortTermMemoryCount;
            this.longTermMemoryCount = source.longTermMemoryCount;
            this.patternCount = source.patternCount;
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

        public Builder withTotalStores(long totalStores) {
            this.totalStores = totalStores;
            return this;
        }

        public Builder withTotalRetrievals(long totalRetrievals) {
            this.totalRetrievals = totalRetrievals;
            return this;
        }

        public Builder withTotalConsolidations(long totalConsolidations) {
            this.totalConsolidations = totalConsolidations;
            return this;
        }

        public Builder withTotalPatternRecognitions(long totalPatternRecognitions) {
            this.totalPatternRecognitions = totalPatternRecognitions;
            return this;
        }

        public Builder withShortTermMemoryCount(int shortTermMemoryCount) {
            this.shortTermMemoryCount = shortTermMemoryCount;
            return this;
        }

        public Builder withLongTermMemoryCount(int longTermMemoryCount) {
            this.longTermMemoryCount = longTermMemoryCount;
            return this;
        }

        public Builder withPatternCount(int patternCount) {
            this.patternCount = patternCount;
            return this;
        }

        public MemoryPerformanceMetrics build() {
            validate();
            return new MemoryPerformanceMetrics(this);
        }

        private void validate() {
            if (totalStores < 0) {
                throw new IllegalArgumentException("totalStores must be non-negative");
            }
            if (totalRetrievals < 0) {
                throw new IllegalArgumentException("totalRetrievals must be non-negative");
            }
            if (totalConsolidations < 0) {
                throw new IllegalArgumentException("totalConsolidations must be non-negative");
            }
            if (totalPatternRecognitions < 0) {
                throw new IllegalArgumentException("totalPatternRecognitions must be non-negative");
            }
            if (shortTermMemoryCount < 0) {
                throw new IllegalArgumentException("shortTermMemoryCount must be non-negative");
            }
            if (longTermMemoryCount < 0) {
                throw new IllegalArgumentException("longTermMemoryCount must be non-negative");
            }
            if (patternCount < 0) {
                throw new IllegalArgumentException("patternCount must be non-negative");
            }
        }
    }
}
