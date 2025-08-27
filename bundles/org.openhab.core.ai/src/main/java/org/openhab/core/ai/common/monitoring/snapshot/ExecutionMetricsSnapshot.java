package org.openhab.core.ai.common.monitoring.snapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.Counts;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.HealthStatus;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;
import org.openhab.core.ai.common.monitoring.api.Timing;

/**
 * Immutable snapshot of execution metrics.
 * 
 * <p>
 * This record provides a thread-safe snapshot of execution metrics at a specific
 * point in time. It implements multiple capability interfaces for different
 * types of metrics access.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record ExecutionMetricsSnapshot(Counts counts, Timing timing, long timestampMs,
        // Health-related fields
        HealthStatus healthStatus, String statusMessage, long lastFailureTime, long lastSuccessTime, String lastError,
        long consecutiveFailures) implements MetricsSnapshot, CountsMetrics, LatencyMetrics {

    public ExecutionMetricsSnapshot {
        Objects.requireNonNull(healthStatus, "healthStatus");
        Objects.requireNonNull(statusMessage, "statusMessage");
        Objects.requireNonNull(lastError, "lastError");
    }

    public long total() {
        return counts.total();
    }

    public long success() {
        return counts.success();
    }

    public long failure() {
        return counts.failure();
    }

    /**
     * Calculate success rate as a percentage.
     * 
     * @return success rate as percentage (0.0-100.0)
     */
    public double successRate() {
        return successRatePercent();
    }

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

    // ===== Computed Values with Edge Case Handling =====

    /**
     * Calculate operations per second based on total operations and total duration.
     * 
     * @return operations per second, or 0.0 if no operations or duration
     */
    public double operationsPerSecond() {
        if (total() == 0 || totalDurationNanos() == 0) {
            return 0.0;
        }
        double durationSeconds = totalDurationNanos() / 1_000_000_000.0;
        return total() / durationSeconds;
    }

    /**
     * Calculate failure rate percentage.
     * 
     * @return failure rate as percentage (0.0-100.0)
     */
    public double failureRatePercent() {
        if (total() == 0) {
            return 0.0;
        }
        return (failure() * 100.0) / total();
    }

    /**
     * Calculate success rate percentage.
     * 
     * @return success rate as percentage (0.0-100.0)
     */
    public double successRatePercent() {
        if (total() == 0) {
            return 0.0;
        }
        return (success() * 100.0) / total();
    }

    /**
     * Calculate average latency in milliseconds with edge case protection.
     * 
     * @return average latency in milliseconds, or 0.0 if no operations
     */
    public double averageLatencyMs() {
        if (total() == 0) {
            return 0.0;
        }
        return totalDurationNanos() / (total() * 1_000_000.0);
    }

    /**
     * Get efficiency score (0.0-1.0) based on success rate and performance.
     * 
     * @return efficiency score combining success rate and performance
     */
    public double efficiencyScore() {
        if (total() == 0) {
            return 0.0;
        }
        double successRatio = (double) success() / total();
        double performanceScore = Math.min(1.0, 1000.0 / Math.max(1.0, averageLatencyMs()));
        return (successRatio + performanceScore) / 2.0;
    }

    /**
     * Check if this snapshot represents a healthy state.
     * 
     * @return true if success rate is above 95% and average latency is reasonable
     */
    public boolean isHealthy() {
        return successRatePercent() >= 95.0 && averageLatencyMs() < 5000.0 && healthStatus == HealthStatus.HEALTHY;
    }



    // ===== Health-Specific Methods =====

    /**
     * Get time since last failure in milliseconds.
     * 
     * @return milliseconds since last failure, or -1 if no failures recorded
     */
    public long timeSinceLastFailureMs() {
        if (lastFailureTime == 0) {
            return -1;
        }
        return timestampMs - lastFailureTime;
    }

    /**
     * Get time since last success in milliseconds.
     * 
     * @return milliseconds since last success, or -1 if no successes recorded
     */
    public long timeSinceLastSuccessMs() {
        if (lastSuccessTime == 0) {
            return -1;
        }
        return timestampMs - lastSuccessTime;
    }

    /**
     * Calculate overall health score (0.0-1.0) combining execution and health metrics.
     * 
     * @return health score combining various health indicators
     */
    public double healthScore() {
        double executionScore = efficiencyScore();
        double uptimeScore = successRatePercent() / 100.0;

        // Reduce score based on consecutive failures
        double failurePenalty = Math.min(0.5, consecutiveFailures * 0.1);

        // Reduce score based on time since last success
        double recentActivityScore = 1.0;
        long timeSinceSuccess = timeSinceLastSuccessMs();
        if (timeSinceSuccess > 300_000) { // 5 minutes
            recentActivityScore = Math.max(0.0, 1.0 - (timeSinceSuccess - 300_000) / 600_000.0);
        }

        return Math.max(0.0, (executionScore + uptimeScore + recentActivityScore) / 3.0 - failurePenalty);
    }

    /**
     * Check if immediate attention is required.
     * 
     * @return true if health score is critically low or consecutive failures are high
     */
    public boolean requiresImmediateAttention() {
        return healthScore() < 0.3 || consecutiveFailures >= 5;
    }

    /**
     * Get the last error message.
     * 
     * @return last error message, or empty string if none
     */
    public String getLastError() {
        return lastError;
    }

    /**
     * Get the number of consecutive failures.
     * 
     * @return consecutive failure count
     */
    public long getConsecutiveFailures() {
        return consecutiveFailures;
    }



    /**
     * Builder for ExecutionMetricsSnapshot.
     */
    public static final class Builder {
        private Counts counts = new Counts(0, 0, 0);
        private Timing timing = new Timing(0);
        private long timestampMs = System.currentTimeMillis();
        // Health-related fields
        private HealthStatus healthStatus = HealthStatus.UNKNOWN;
        private String statusMessage = "";
        private long lastFailureTime = 0L;
        private long lastSuccessTime = 0L;
        private String lastError = "";
        private long consecutiveFailures = 0L;

        public Builder() {
            // Default constructor
        }

        public Builder(ExecutionMetricsSnapshot source) {
            this.counts = source.counts;
            this.timing = source.timing;
            this.timestampMs = source.timestampMs;
            this.healthStatus = source.healthStatus;
            this.statusMessage = source.statusMessage;
            this.lastFailureTime = source.lastFailureTime;
            this.lastSuccessTime = source.lastSuccessTime;
            this.lastError = source.lastError;
            this.consecutiveFailures = source.consecutiveFailures;
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

        // Health-related builder methods
        public Builder withHealthStatus(HealthStatus healthStatus) {
            this.healthStatus = Objects.requireNonNull(healthStatus, "healthStatus");
            return this;
        }

        public Builder withStatusMessage(String statusMessage) {
            this.statusMessage = Objects.requireNonNull(statusMessage, "statusMessage");
            return this;
        }

        public Builder withLastFailureTime(long lastFailureTime) {
            this.lastFailureTime = lastFailureTime;
            return this;
        }

        public Builder withLastSuccessTime(long lastSuccessTime) {
            this.lastSuccessTime = lastSuccessTime;
            return this;
        }

        public Builder withLastError(String lastError) {
            this.lastError = Objects.requireNonNull(lastError, "lastError");
            return this;
        }

        public Builder withConsecutiveFailures(long consecutiveFailures) {
            this.consecutiveFailures = consecutiveFailures;
            return this;
        }

        public ExecutionMetricsSnapshot build() {
            validate();
            return new ExecutionMetricsSnapshot(counts, timing, timestampMs, healthStatus, statusMessage,
                    lastFailureTime, lastSuccessTime, lastError, consecutiveFailures);
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
            if (counts.total() != counts.success() + counts.failure()) {
                throw new IllegalArgumentException("total count must equal success + failure");
            }
        }
    }

    /**
     * Create a new builder for ExecutionMetricsSnapshot.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create a new builder for ExecutionMetricsSnapshot from an existing instance.
     *
     * @return a new builder instance initialized with this instance's values
     */
    public Builder toBuilder() {
        return new Builder(this);
    }

    // ===== Object Contract Methods =====

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ExecutionMetricsSnapshot other = (ExecutionMetricsSnapshot) obj;
        return Objects.equals(counts, other.counts) && Objects.equals(timing, other.timing)
                && timestampMs == other.timestampMs;
    }

    @Override
    public int hashCode() {
        return Objects.hash(counts, timing, timestampMs);
    }

    @Override
    public String toString() {
        return new StringBuilder("ExecutionMetricsSnapshot{").append("total=").append(total()).append(", success=")
                .append(success()).append(", failure=").append(failure()).append(", successRate=")
                .append(String.format("%.2f%%", successRatePercent())).append(", avgLatency=")
                .append(String.format("%.2fms", averageLatencyMs())).append(", opsPerSec=")
                .append(String.format("%.2f", operationsPerSecond())).append(", efficiency=")
                .append(String.format("%.3f", efficiencyScore())).append(", healthStatus=").append(healthStatus)
                .append(", healthScore=").append(String.format("%.3f", healthScore())).append(", consecutiveFailures=")
                .append(consecutiveFailures).append(", requiresAttention=").append(requiresImmediateAttention())
                .append(", timestamp=").append(timestampMs).append("}").toString();
    }

    /**
     * Create a concise summary string for logging.
     * 
     * @return concise summary string
     */
    public String toSummary() {
        return String.format("Exec[%d ops, %.1f%% success, %.1fms avg, %.1f ops/s]", total(), successRatePercent(),
                averageLatencyMs(), operationsPerSecond());
    }
}
