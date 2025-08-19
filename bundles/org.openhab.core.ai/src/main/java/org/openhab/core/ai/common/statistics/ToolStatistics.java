package org.openhab.core.ai.common.statistics;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.builder.AbstractBuilder;

/**
 * Unified Tool Statistics implementation.
 *
 * <p>
 * This class provides comprehensive statistics for tool operations,
 * including execution counts, success rates, response times, and performance metrics.
 * </p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class ToolStatistics extends BaseStatistics {

    private final String toolId;
    private final long totalExecutions;
    private final long successfulExecutions;
    private final long failedExecutions;
    private final long totalExecutionTimeMs;
    private final long averageExecutionTimeMs;
    private final long minExecutionTimeMs;
    private final long maxExecutionTimeMs;
    private final @Nullable Instant lastExecutionTime;
    private final @Nullable Instant lastSuccessTime;
    private final @Nullable Instant lastFailureTime;
    private final @Nullable String lastError;

    private ToolStatistics(Builder builder) {
        super(builder.id, builder.timestamp, StatisticsType.EXECUTION, builder.metrics);
        this.toolId = builder.toolId;
        this.totalExecutions = builder.totalExecutions;
        this.successfulExecutions = builder.successfulExecutions;
        this.failedExecutions = builder.failedExecutions;
        this.totalExecutionTimeMs = builder.totalExecutionTimeMs;
        this.averageExecutionTimeMs = builder.averageExecutionTimeMs;
        this.minExecutionTimeMs = builder.minExecutionTimeMs;
        this.maxExecutionTimeMs = builder.maxExecutionTimeMs;
        this.lastExecutionTime = builder.lastExecutionTime;
        this.lastSuccessTime = builder.lastSuccessTime;
        this.lastFailureTime = builder.lastFailureTime;
        this.lastError = builder.lastError;
    }

    public String getToolId() {
        return toolId;
    }

    public long getTotalExecutions() {
        return totalExecutions;
    }

    public long getSuccessfulExecutions() {
        return successfulExecutions;
    }

    public long getFailedExecutions() {
        return failedExecutions;
    }

    public long getTotalExecutionTimeMs() {
        return totalExecutionTimeMs;
    }

    public long getAverageExecutionTimeMs() {
        return averageExecutionTimeMs;
    }

    public long getMinExecutionTimeMs() {
        return minExecutionTimeMs;
    }

    public long getMaxExecutionTimeMs() {
        return maxExecutionTimeMs;
    }

    public @Nullable Instant getLastExecutionTime() {
        return lastExecutionTime;
    }

    public @Nullable Instant getLastSuccessTime() {
        return lastSuccessTime;
    }

    public @Nullable Instant getLastFailureTime() {
        return lastFailureTime;
    }

    public @Nullable String getLastError() {
        return lastError;
    }

    public double getSuccessRate() {
        return totalExecutions > 0 ? (double) successfulExecutions / totalExecutions * 100.0 : 0.0;
    }

    public double getFailureRate() {
        return totalExecutions > 0 ? (double) failedExecutions / totalExecutions * 100.0 : 0.0;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj) || getClass() != obj.getClass()) {
            return false;
        }
        ToolStatistics other = (ToolStatistics) obj;
        return Objects.equals(toolId, other.toolId) && totalExecutions == other.totalExecutions
                && successfulExecutions == other.successfulExecutions && failedExecutions == other.failedExecutions
                && totalExecutionTimeMs == other.totalExecutionTimeMs
                && averageExecutionTimeMs == other.averageExecutionTimeMs
                && minExecutionTimeMs == other.minExecutionTimeMs && maxExecutionTimeMs == other.maxExecutionTimeMs
                && Objects.equals(lastExecutionTime, other.lastExecutionTime)
                && Objects.equals(lastSuccessTime, other.lastSuccessTime)
                && Objects.equals(lastFailureTime, other.lastFailureTime) && Objects.equals(lastError, other.lastError);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), toolId, totalExecutions, successfulExecutions, failedExecutions,
                totalExecutionTimeMs, averageExecutionTimeMs, minExecutionTimeMs, maxExecutionTimeMs, lastExecutionTime,
                lastSuccessTime, lastFailureTime, lastError);
    }

    @Override
    public String toString() {
        return String.format(
                "ToolStatistics{id='%s', toolId='%s', totalExecutions=%d, successfulExecutions=%d, failedExecutions=%d, successRate=%.2f%%, averageExecutionTimeMs=%d}",
                getId(), toolId, totalExecutions, successfulExecutions, failedExecutions, getSuccessRate(),
                averageExecutionTimeMs);
    }

    public static final class Builder extends AbstractBuilder<ToolStatistics> {
        private String id = "";
        private @Nullable Instant timestamp;
        private final Map<String, Object> metrics = new HashMap<>();
        private String toolId = "";
        private long totalExecutions = 0;
        private long successfulExecutions = 0;
        private long failedExecutions = 0;
        private long totalExecutionTimeMs = 0;
        private long averageExecutionTimeMs = 0;
        private long minExecutionTimeMs = Long.MAX_VALUE;
        private long maxExecutionTimeMs = 0;
        private @Nullable Instant lastExecutionTime;
        private @Nullable Instant lastSuccessTime;
        private @Nullable Instant lastFailureTime;
        private @Nullable String lastError;

        public Builder() {
        }

        public Builder(ToolStatistics source) {
            this.id = source.getId();
            this.timestamp = source.getTimestamp();
            this.metrics.putAll(source.getMetrics());
            this.toolId = source.toolId;
            this.totalExecutions = source.totalExecutions;
            this.successfulExecutions = source.successfulExecutions;
            this.failedExecutions = source.failedExecutions;
            this.totalExecutionTimeMs = source.totalExecutionTimeMs;
            this.averageExecutionTimeMs = source.averageExecutionTimeMs;
            this.minExecutionTimeMs = source.minExecutionTimeMs;
            this.maxExecutionTimeMs = source.maxExecutionTimeMs;
            this.lastExecutionTime = source.lastExecutionTime;
            this.lastSuccessTime = source.lastSuccessTime;
            this.lastFailureTime = source.lastFailureTime;
            this.lastError = source.lastError;
        }

        public Builder withId(String id) {
            this.id = Objects.requireNonNull(id, "id");
            return this;
        }

        public Builder withTimestamp(@Nullable Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder withToolId(String toolId) {
            this.toolId = Objects.requireNonNull(toolId, "toolId");
            return this;
        }

        public Builder withTotalExecutions(long totalExecutions) {
            this.totalExecutions = totalExecutions;
            return this;
        }

        public Builder withSuccessfulExecutions(long successfulExecutions) {
            this.successfulExecutions = successfulExecutions;
            return this;
        }

        public Builder withFailedExecutions(long failedExecutions) {
            this.failedExecutions = failedExecutions;
            return this;
        }

        public Builder withTotalExecutionTimeMs(long totalExecutionTimeMs) {
            this.totalExecutionTimeMs = totalExecutionTimeMs;
            return this;
        }

        public Builder withAverageExecutionTimeMs(long averageExecutionTimeMs) {
            this.averageExecutionTimeMs = averageExecutionTimeMs;
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

        public Builder withLastExecutionTime(@Nullable Instant lastExecutionTime) {
            this.lastExecutionTime = lastExecutionTime;
            return this;
        }

        public Builder withLastSuccessTime(@Nullable Instant lastSuccessTime) {
            this.lastSuccessTime = lastSuccessTime;
            return this;
        }

        public Builder withLastFailureTime(@Nullable Instant lastFailureTime) {
            this.lastFailureTime = lastFailureTime;
            return this;
        }

        public Builder withLastError(@Nullable String lastError) {
            this.lastError = lastError;
            return this;
        }

        @Override
        public ToolStatistics build() {
            validate();
            populateMetrics();
            return new ToolStatistics(this);
        }

        @Override
        protected void validate() {
            if (id.trim().isEmpty()) {
                addValidationError("id cannot be blank");
            }
            if (toolId.trim().isEmpty()) {
                addValidationError("toolId cannot be blank");
            }
            if (totalExecutions < 0) {
                addValidationError("totalExecutions must be non-negative");
            }
            if (successfulExecutions < 0) {
                addValidationError("successfulExecutions must be non-negative");
            }
            if (failedExecutions < 0) {
                addValidationError("failedExecutions must be non-negative");
            }
            if (successfulExecutions + failedExecutions > totalExecutions) {
                addValidationError("successfulExecutions + failedExecutions cannot exceed totalExecutions");
            }
            if (totalExecutionTimeMs < 0) {
                addValidationError("totalExecutionTimeMs must be non-negative");
            }
            if (averageExecutionTimeMs < 0) {
                addValidationError("averageExecutionTimeMs must be non-negative");
            }
            if (minExecutionTimeMs < 0 && minExecutionTimeMs != Long.MAX_VALUE) {
                addValidationError("minExecutionTimeMs must be non-negative");
            }
            if (maxExecutionTimeMs < 0) {
                addValidationError("maxExecutionTimeMs must be non-negative");
            }
            if (minExecutionTimeMs != Long.MAX_VALUE && maxExecutionTimeMs < minExecutionTimeMs) {
                addValidationError("maxExecutionTimeMs cannot be less than minExecutionTimeMs");
            }
        }

        @Override
        protected void doReset() {
            id = "";
            timestamp = null;
            metrics.clear();
            toolId = "";
            totalExecutions = 0;
            successfulExecutions = 0;
            failedExecutions = 0;
            totalExecutionTimeMs = 0;
            averageExecutionTimeMs = 0;
            minExecutionTimeMs = Long.MAX_VALUE;
            maxExecutionTimeMs = 0;
            lastExecutionTime = null;
            lastSuccessTime = null;
            lastFailureTime = null;
            lastError = null;
        }

        private void populateMetrics() {
            metrics.put("toolId", toolId);
            metrics.put("totalExecutions", totalExecutions);
            metrics.put("successfulExecutions", successfulExecutions);
            metrics.put("failedExecutions", failedExecutions);
            metrics.put("totalExecutionTimeMs", totalExecutionTimeMs);
            metrics.put("averageExecutionTimeMs", averageExecutionTimeMs);
            metrics.put("minExecutionTimeMs", minExecutionTimeMs == Long.MAX_VALUE ? 0 : minExecutionTimeMs);
            metrics.put("maxExecutionTimeMs", maxExecutionTimeMs);
            metrics.put("lastExecutionTime", lastExecutionTime);
            metrics.put("lastSuccessTime", lastSuccessTime);
            metrics.put("lastFailureTime", lastFailureTime);
            metrics.put("lastError", lastError);
            metrics.put("successRate",
                    totalExecutions > 0 ? (double) successfulExecutions / totalExecutions * 100.0 : 0.0);
            metrics.put("failureRate", totalExecutions > 0 ? (double) failedExecutions / totalExecutions * 100.0 : 0.0);
        }
    }
}
