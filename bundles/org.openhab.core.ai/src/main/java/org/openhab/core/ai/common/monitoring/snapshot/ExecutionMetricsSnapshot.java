package org.openhab.core.ai.common.monitoring.snapshot;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.Counts;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.Metrics;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;
import org.openhab.core.ai.common.monitoring.api.MonitoringType;
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
public record ExecutionMetricsSnapshot(Counts counts, Timing timing,
        long timestampMs) implements MetricsSnapshot, CountsMetrics, LatencyMetrics, Metrics {

    public long total() {
        return counts.total();
    }

    public long success() {
        return counts.success();
    }

    public long failure() {
        return counts.failure();
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
        return successRatePercent() >= 95.0 && averageLatencyMs() < 5000.0;
    }

    // ===== Metrics Interface Implementation =====

    @Override
    public String getId() {
        return "execution-metrics";
    }

    @Override
    public java.time.Instant getTimestamp() {
        return java.time.Instant.ofEpochMilli(timestampMs);
    }

    @Override
    public MonitoringType getType() {
        return MonitoringType.PERFORMANCE;
    }

    @Override
    public java.util.Map<String, Object> getData() {
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("totalOperations", getTotalOperations());
        data.put("successfulOperations", getSuccessfulOperations());
        data.put("failedOperations", getFailedOperations());
        data.put("totalProcessingTime", getTotalProcessingTime());
        data.put("averageResponseTime", getAverageResponseTime());
        data.put("successRate", getSuccessRate());
        data.put("operationsPerSecond", getOperationsPerSecond());
        return data;
    }

    @Override
    public String getDomain() {
        return "execution";
    }

    @Override
    public String getSource() {
        return "execution-collector";
    }

    @Override
    public long getTotalOperations() {
        return total();
    }

    @Override
    public long getSuccessfulOperations() {
        return success();
    }

    @Override
    public long getFailedOperations() {
        return failure();
    }

    @Override
    public long getTotalProcessingTime() {
        return totalDurationNanos() / 1_000_000L; // Convert nanoseconds to milliseconds
    }

    @Override
    public double getAverageResponseTime() {
        return averageMs();
    }

    @Override
    public java.time.Instant getLastOperationTime() {
        // This would need to be tracked separately in the collector
        // For now, return the snapshot timestamp
        return getTimestamp();
    }

    /**
     * Builder for ExecutionMetricsSnapshot.
     */
    public static final class Builder {
        private Counts counts = new Counts(0, 0, 0);
        private Timing timing = new Timing(0);
        private long timestampMs = System.currentTimeMillis();

        public Builder() {
            // Default constructor
        }

        public Builder(ExecutionMetricsSnapshot source) {
            this.counts = source.counts;
            this.timing = source.timing;
            this.timestampMs = source.timestampMs;
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

        public ExecutionMetricsSnapshot build() {
            validate();
            return new ExecutionMetricsSnapshot(counts, timing, timestampMs);
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
                .append(String.format("%.3f", efficiencyScore())).append(", healthy=").append(isHealthy())
                .append(", timestamp=").append(getTimestamp()).append("}").toString();
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
