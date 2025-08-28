package org.openhab.core.ai.agent.api;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.Counts;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;
import org.openhab.core.ai.common.monitoring.api.Timing;

/**
 * Performance metrics snapshot for model-based action plans
 * 
 * <p>
 * This record provides:
 * - Immutable snapshot of action plan performance metrics
 * - Plan execution counts (total, successful, failed)
 * - Timing information for plan execution
 * - Capability interfaces for different metrics access patterns
 * - Thread-safe and suitable for concurrent use
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record AgentModelActionPlanPerformanceMetrics(Counts counts, Timing timing, long timestampMs, long planCount,
        long stepCount, double modelAccuracy) implements MetricsSnapshot, CountsMetrics, LatencyMetrics {

    /**
     * Validation constructor for the record.
     */
    public AgentModelActionPlanPerformanceMetrics {
        Objects.requireNonNull(counts, "counts must not be null");
        Objects.requireNonNull(timing, "timing must not be null");
        if (timestampMs < 0) {
            throw new IllegalArgumentException("timestampMs must be non-negative");
        }
        if (planCount < 0) {
            throw new IllegalArgumentException("planCount must be non-negative");
        }
        if (stepCount < 0) {
            throw new IllegalArgumentException("stepCount must be non-negative");
        }
        if (modelAccuracy < 0.0 || modelAccuracy > 1.0) {
            throw new IllegalArgumentException("modelAccuracy must be between 0.0 and 1.0");
        }
    }

    // ===== CountsMetrics Implementation =====

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

    /**
     * Calculate success rate as a percentage.
     * 
     * @return success rate as percentage (0.0-100.0)
     */
    public double successRate() {
        return CountsMetrics.super.successRate();
    }

    // ===== LatencyMetrics Implementation =====

    @Override
    public long totalDurationNanos() {
        return timing.totalDurationNanos();
    }

    /**
     * Get the average response time in milliseconds.
     *
     * @return average response time in milliseconds
     */
    public double averageMs() {
        return LatencyMetrics.super.averageMs(total());
    }

    // ===== Action Plan Specific Metrics =====

    /**
     * Calculate operations per second based on total operations and total duration.
     * 
     * @return operations per second, or 0.0 if no operations or duration
     */
    public double getOperationsPerSecond() {
        if (total() == 0 || totalDurationNanos() == 0) {
            return 0.0;
        }
        double durationSeconds = totalDurationNanos() / 1_000_000_000.0;
        return total() / durationSeconds;
    }

    /**
     * Calculate average steps per plan.
     * 
     * @return average steps per plan, or 0.0 if no plans
     */
    public double getAverageStepsPerPlan() {
        return planCount > 0 ? (double) stepCount / planCount : 0.0;
    }

    /**
     * Calculate plan success rate percentage.
     * 
     * @return plan success rate as percentage (0.0-100.0)
     */
    public double getPlanSuccessRatePercent() {
        if (planCount == 0) {
            return 0.0;
        }
        return (success() * 100.0) / planCount;
    }

    /**
     * Calculate plan failure rate percentage.
     * 
     * @return plan failure rate as percentage (0.0-100.0)
     */
    public double getPlanFailureRatePercent() {
        if (planCount == 0) {
            return 0.0;
        }
        return (failure() * 100.0) / planCount;
    }

    /**
     * Calculate efficiency score (0.0-1.0) based on success rate, performance, and model accuracy.
     * 
     * @return efficiency score combining success rate, performance, and model accuracy
     */
    public double getEfficiencyScore() {
        if (total() == 0) {
            return 0.0;
        }
        double successRatio = (double) success() / total();
        double performanceScore = Math.min(1.0, 1000.0 / Math.max(1.0, averageMs()));
        return (successRatio + performanceScore + modelAccuracy) / 3.0;
    }

    /**
     * Check if this snapshot represents a healthy state.
     * 
     * @return true if success rate is above 95%, latency is reasonable, and model accuracy is good
     */
    public boolean isHealthy() {
        return successRate() >= 0.95 && averageMs() < 5000.0 && modelAccuracy >= 0.8;
    }

    // ===== Builder Pattern =====

    /**
     * Builder for AgentModelActionPlanPerformanceMetrics.
     */
    public static final class Builder {
        private Counts counts = new Counts(0, 0, 0);
        private Timing timing = new Timing(0);
        private long timestampMs = System.currentTimeMillis();
        private long planCount = 0;
        private long stepCount = 0;
        private double modelAccuracy = 0.0;

        public Builder() {
            // Default constructor
        }

        public Builder(AgentModelActionPlanPerformanceMetrics source) {
            this.counts = source.counts;
            this.timing = source.timing;
            this.timestampMs = source.timestampMs;
            this.planCount = source.planCount;
            this.stepCount = source.stepCount;
            this.modelAccuracy = source.modelAccuracy;
        }

        public Builder withCounts(Counts counts) {
            this.counts = Objects.requireNonNull(counts, "counts");
            return this;
        }

        public Builder withTiming(Timing timing) {
            this.timing = Objects.requireNonNull(timing, "timing");
            return this;
        }

        public Builder withTimestampMs(long timestampMs) {
            this.timestampMs = timestampMs;
            return this;
        }

        public Builder withPlanCount(long planCount) {
            this.planCount = planCount;
            return this;
        }

        public Builder withStepCount(long stepCount) {
            this.stepCount = stepCount;
            return this;
        }

        public Builder withModelAccuracy(double modelAccuracy) {
            this.modelAccuracy = modelAccuracy;
            return this;
        }

        public Builder withTotal(long total) {
            this.counts = new Counts(total, counts.success(), counts.failure());
            return this;
        }

        public Builder withSuccess(long success) {
            this.counts = new Counts(counts.total(), success, counts.failure());
            return this;
        }

        public Builder withFailure(long failure) {
            this.counts = new Counts(counts.total(), counts.success(), failure);
            return this;
        }

        public Builder withTotalDurationNanos(long totalDurationNanos) {
            this.timing = new Timing(totalDurationNanos);
            return this;
        }

        public AgentModelActionPlanPerformanceMetrics build() {
            validate();
            return new AgentModelActionPlanPerformanceMetrics(counts, timing, timestampMs, planCount, stepCount,
                    modelAccuracy);
        }

        private void validate() {
            if (counts.total() < 0) {
                throw new IllegalArgumentException("total count must be non-negative");
            }
            if (counts.success() < 0) {
                throw new IllegalArgumentException("success count must be non-negative");
            }
            if (counts.failure() < 0) {
                throw new IllegalArgumentException("failure count must be non-negative");
            }
            if (timing.totalDurationNanos() < 0) {
                throw new IllegalArgumentException("total duration must be non-negative");
            }
            if (planCount < 0) {
                throw new IllegalArgumentException("plan count must be non-negative");
            }
            if (stepCount < 0) {
                throw new IllegalArgumentException("step count must be non-negative");
            }
            if (modelAccuracy < 0.0 || modelAccuracy > 1.0) {
                throw new IllegalArgumentException("model accuracy must be between 0.0 and 1.0");
            }
        }
    }

    /**
     * Create a new builder for AgentModelActionPlanPerformanceMetrics.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create a new builder for AgentModelActionPlanPerformanceMetrics from an existing instance.
     *
     * @return a new builder instance initialized with this instance's values
     */
    public Builder toBuilder() {
        return new Builder(this);
    }

    // ===== Utility Methods =====

    /**
     * Create a concise summary string for logging.
     * 
     * @return concise summary string
     */
    public String toSummary() {
        return String.format("ActionPlan[%d plans, %d steps, %.1f%% success, %.1fms avg, %.3f accuracy]", planCount,
                stepCount, successRate() * 100, averageMs(), modelAccuracy);
    }
}
