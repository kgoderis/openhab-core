package org.openhab.core.ai.agent.api;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Performance metrics for individual action steps
 * 
 * <p>
 * This class provides:
 * - Step execution performance metrics
 * - Resource utilization tracking
 * - Quality and accuracy metrics
 * - Optimization recommendations
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class AgentModelActionStepPerformanceMetrics {

    // Step execution metrics
    private final Duration executionTime;
    private final Duration preparationTime;
    private final Duration validationTime;
    private final Duration cleanupTime;

    // Quality metrics
    private final boolean success;
    private final double accuracy;
    private final double confidenceScore;
    private final int validationErrors;
    private final int executionErrors;

    // Resource utilization
    private final double cpuUtilization;
    private final double memoryUtilization;
    private final long memoryUsed;
    private final int networkRequests;
    private final Duration networkTime;

    // Optimization data
    private final @Nullable Map<String, Object> optimizationData;
    private final @Nullable Duration estimatedOptimizationTime;

    // Timestamps
    private final Instant metricsTimestamp;
    private final @Nullable Instant stepStartTime;
    private final @Nullable Instant stepEndTime;

    private AgentModelActionStepPerformanceMetrics(Builder builder) {
        this.executionTime = builder.executionTime;
        this.preparationTime = builder.preparationTime;
        this.validationTime = builder.validationTime;
        this.cleanupTime = builder.cleanupTime;
        this.success = builder.success;
        this.accuracy = builder.accuracy;
        this.confidenceScore = builder.confidenceScore;
        this.validationErrors = builder.validationErrors;
        this.executionErrors = builder.executionErrors;
        this.cpuUtilization = builder.cpuUtilization;
        this.memoryUtilization = builder.memoryUtilization;
        this.memoryUsed = builder.memoryUsed;
        this.networkRequests = builder.networkRequests;
        this.networkTime = builder.networkTime;
        this.optimizationData = builder.optimizationData != null ? Map.copyOf(builder.optimizationData) : null;
        this.estimatedOptimizationTime = builder.estimatedOptimizationTime;
        this.metricsTimestamp = builder.metricsTimestamp;
        this.stepStartTime = builder.stepStartTime;
        this.stepEndTime = builder.stepEndTime;
    }

    /**
     * Create a new step performance metrics builder
     * 
     * @return the builder
     */
    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public Duration getExecutionTime() {
        return executionTime;
    }

    public Duration getPreparationTime() {
        return preparationTime;
    }

    public Duration getValidationTime() {
        return validationTime;
    }

    public Duration getCleanupTime() {
        return cleanupTime;
    }

    public boolean isSuccess() {
        return success;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public double getConfidenceScore() {
        return confidenceScore;
    }

    public int getValidationErrors() {
        return validationErrors;
    }

    public int getExecutionErrors() {
        return executionErrors;
    }

    public double getCpuUtilization() {
        return cpuUtilization;
    }

    public double getMemoryUtilization() {
        return memoryUtilization;
    }

    public long getMemoryUsed() {
        return memoryUsed;
    }

    public int getNetworkRequests() {
        return networkRequests;
    }

    public Duration getNetworkTime() {
        return networkTime;
    }

    public @Nullable Map<String, Object> getOptimizationData() {
        return optimizationData;
    }

    public @Nullable Duration getEstimatedOptimizationTime() {
        return estimatedOptimizationTime;
    }

    public Instant getMetricsTimestamp() {
        return metricsTimestamp;
    }

    public @Nullable Instant getStepStartTime() {
        return stepStartTime;
    }

    public @Nullable Instant getStepEndTime() {
        return stepEndTime;
    }

    /**
     * Get the total time for this step
     * 
     * @return the total time
     */
    public Duration getTotalTime() {
        return executionTime.plus(preparationTime).plus(validationTime).plus(cleanupTime);
    }

    /**
     * Check if the step execution was successful
     * 
     * @return true if successful
     */
    public boolean isSuccessful() {
        return success && executionErrors == 0 && validationErrors == 0;
    }

    /**
     * Check if the step needs optimization
     * 
     * @return true if optimization is recommended
     */
    public boolean needsOptimization() {
        return !success || executionTime.toSeconds() > 60 || cpuUtilization > 0.8 || memoryUtilization > 0.8
                || networkRequests > 10;
    }

    /**
     * Get the efficiency score (0.0 to 1.0)
     * 
     * @return the efficiency score
     */
    public double getEfficiencyScore() {
        double timeEfficiency = 1.0 - Math.min(executionTime.toSeconds() / 60.0, 1.0);
        double resourceEfficiency = 1.0 - Math.max(cpuUtilization, memoryUtilization);
        double successEfficiency = success ? 1.0 : 0.0;
        double accuracyEfficiency = accuracy;

        return (timeEfficiency + resourceEfficiency + successEfficiency + accuracyEfficiency) / 4.0;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        AgentModelActionStepPerformanceMetrics that = (AgentModelActionStepPerformanceMetrics) obj;
        return Objects.equals(metricsTimestamp, that.metricsTimestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(metricsTimestamp);
    }

    @Override
    public String toString() {
        return String.format(
                "AgentModelActionStepPerformanceMetrics{executionTime=%s, success=%s, accuracy=%.2f, efficiency=%.2f}",
                executionTime, success, accuracy, getEfficiencyScore());
    }

    /**
     * Builder for AgentModelActionStepPerformanceMetrics
     */
    public static final class Builder {
        private Duration executionTime = Duration.ZERO;
        private Duration preparationTime = Duration.ZERO;
        private Duration validationTime = Duration.ZERO;
        private Duration cleanupTime = Duration.ZERO;
        private boolean success = false;
        private double accuracy = 0.0;
        private double confidenceScore = 0.0;
        private int validationErrors = 0;
        private int executionErrors = 0;
        private double cpuUtilization = 0.0;
        private double memoryUtilization = 0.0;
        private long memoryUsed = 0L;
        private int networkRequests = 0;
        private Duration networkTime = Duration.ZERO;
        private @Nullable Map<String, Object> optimizationData;
        private @Nullable Duration estimatedOptimizationTime;
        private Instant metricsTimestamp = Instant.now();
        private @Nullable Instant stepStartTime;
        private @Nullable Instant stepEndTime;

        public Builder executionTime(Duration executionTime) {
            this.executionTime = Objects.requireNonNull(executionTime, "executionTime");
            return this;
        }

        public Builder preparationTime(Duration preparationTime) {
            this.preparationTime = Objects.requireNonNull(preparationTime, "preparationTime");
            return this;
        }

        public Builder validationTime(Duration validationTime) {
            this.validationTime = Objects.requireNonNull(validationTime, "validationTime");
            return this;
        }

        public Builder cleanupTime(Duration cleanupTime) {
            this.cleanupTime = Objects.requireNonNull(cleanupTime, "cleanupTime");
            return this;
        }

        public Builder success(boolean success) {
            this.success = success;
            return this;
        }

        public Builder accuracy(double accuracy) {
            this.accuracy = accuracy;
            return this;
        }

        public Builder confidenceScore(double confidenceScore) {
            this.confidenceScore = confidenceScore;
            return this;
        }

        public Builder validationErrors(int validationErrors) {
            this.validationErrors = validationErrors;
            return this;
        }

        public Builder executionErrors(int executionErrors) {
            this.executionErrors = executionErrors;
            return this;
        }

        public Builder cpuUtilization(double cpuUtilization) {
            this.cpuUtilization = cpuUtilization;
            return this;
        }

        public Builder memoryUtilization(double memoryUtilization) {
            this.memoryUtilization = memoryUtilization;
            return this;
        }

        public Builder memoryUsed(long memoryUsed) {
            this.memoryUsed = memoryUsed;
            return this;
        }

        public Builder networkRequests(int networkRequests) {
            this.networkRequests = networkRequests;
            return this;
        }

        public Builder networkTime(Duration networkTime) {
            this.networkTime = Objects.requireNonNull(networkTime, "networkTime");
            return this;
        }

        public Builder optimizationData(@Nullable Map<String, Object> optimizationData) {
            this.optimizationData = optimizationData;
            return this;
        }

        public Builder estimatedOptimizationTime(@Nullable Duration estimatedOptimizationTime) {
            this.estimatedOptimizationTime = estimatedOptimizationTime;
            return this;
        }

        public Builder metricsTimestamp(Instant metricsTimestamp) {
            this.metricsTimestamp = Objects.requireNonNull(metricsTimestamp, "metricsTimestamp");
            return this;
        }

        public Builder stepStartTime(@Nullable Instant stepStartTime) {
            this.stepStartTime = stepStartTime;
            return this;
        }

        public Builder stepEndTime(@Nullable Instant stepEndTime) {
            this.stepEndTime = stepEndTime;
            return this;
        }

        public AgentModelActionStepPerformanceMetrics build() {
            return new AgentModelActionStepPerformanceMetrics(this);
        }
    }
}
