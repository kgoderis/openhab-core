package org.openhab.core.ai.common.statistics;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.builder.AbstractBuilder;
import org.openhab.core.ai.model.api.ModelProviderType;

/**
 * Unified Provider Statistics implementation.
 *
 * <p>
 * This class provides comprehensive statistics for model provider operations,
 * including execution counts, success rates, response times, and health metrics.
 * </p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class ProviderStatistics extends BaseStatistics {

    private final ModelProviderType providerType;
    private final long totalExecutions;
    private final long successfulExecutions;
    private final long failedExecutions;
    private final long totalExecutionTimeMs;
    private final long averageResponseTimeMs;
    private final long minResponseTimeMs;
    private final long maxResponseTimeMs;
    private final double healthScore;
    private final @Nullable Instant lastExecutionTime;
    private final @Nullable Instant lastSuccessTime;
    private final @Nullable Instant lastFailureTime;
    private final @Nullable String lastError;

    private ProviderStatistics(Builder builder) {
        super(builder.id, builder.timestamp, StatisticsType.EXECUTION, builder.metrics);
        this.providerType = builder.providerType;
        this.totalExecutions = builder.totalExecutions;
        this.successfulExecutions = builder.successfulExecutions;
        this.failedExecutions = builder.failedExecutions;
        this.totalExecutionTimeMs = builder.totalExecutionTimeMs;
        this.averageResponseTimeMs = builder.averageResponseTimeMs;
        this.minResponseTimeMs = builder.minResponseTimeMs;
        this.maxResponseTimeMs = builder.maxResponseTimeMs;
        this.healthScore = builder.healthScore;
        this.lastExecutionTime = builder.lastExecutionTime;
        this.lastSuccessTime = builder.lastSuccessTime;
        this.lastFailureTime = builder.lastFailureTime;
        this.lastError = builder.lastError;
    }

    public ModelProviderType getProviderType() {
        return providerType;
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

    public long getAverageResponseTimeMs() {
        return averageResponseTimeMs;
    }

    public long getMinResponseTimeMs() {
        return minResponseTimeMs;
    }

    public long getMaxResponseTimeMs() {
        return maxResponseTimeMs;
    }

    public double getHealthScore() {
        return healthScore;
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
        ProviderStatistics other = (ProviderStatistics) obj;
        return providerType == other.providerType && totalExecutions == other.totalExecutions
                && successfulExecutions == other.successfulExecutions && failedExecutions == other.failedExecutions
                && totalExecutionTimeMs == other.totalExecutionTimeMs
                && averageResponseTimeMs == other.averageResponseTimeMs && minResponseTimeMs == other.minResponseTimeMs
                && maxResponseTimeMs == other.maxResponseTimeMs && Double.compare(healthScore, other.healthScore) == 0
                && Objects.equals(lastExecutionTime, other.lastExecutionTime)
                && Objects.equals(lastSuccessTime, other.lastSuccessTime)
                && Objects.equals(lastFailureTime, other.lastFailureTime) && Objects.equals(lastError, other.lastError);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), providerType, totalExecutions, successfulExecutions, failedExecutions,
                totalExecutionTimeMs, averageResponseTimeMs, minResponseTimeMs, maxResponseTimeMs, healthScore,
                lastExecutionTime, lastSuccessTime, lastFailureTime, lastError);
    }

    @Override
    public String toString() {
        return String.format(
                "ProviderStatistics{id='%s', providerType=%s, totalExecutions=%d, successfulExecutions=%d, failedExecutions=%d, successRate=%.2f%%, healthScore=%.2f}",
                getId(), providerType, totalExecutions, successfulExecutions, failedExecutions, getSuccessRate(),
                healthScore);
    }

    public static final class Builder extends AbstractBuilder<ProviderStatistics> {
        private String id = "";
        private @Nullable Instant timestamp;
        private final Map<String, Object> metrics = new HashMap<>();
        private ModelProviderType providerType = ModelProviderType.OPENAI;
        private long totalExecutions = 0;
        private long successfulExecutions = 0;
        private long failedExecutions = 0;
        private long totalExecutionTimeMs = 0;
        private long averageResponseTimeMs = 0;
        private long minResponseTimeMs = Long.MAX_VALUE;
        private long maxResponseTimeMs = 0;
        private double healthScore = 0.0;
        private @Nullable Instant lastExecutionTime;
        private @Nullable Instant lastSuccessTime;
        private @Nullable Instant lastFailureTime;
        private @Nullable String lastError;

        public Builder() {
        }

        public Builder(ProviderStatistics source) {
            this.id = source.getId();
            this.timestamp = source.getTimestamp();
            this.metrics.putAll(source.getMetrics());
            this.providerType = source.providerType;
            this.totalExecutions = source.totalExecutions;
            this.successfulExecutions = source.successfulExecutions;
            this.failedExecutions = source.failedExecutions;
            this.totalExecutionTimeMs = source.totalExecutionTimeMs;
            this.averageResponseTimeMs = source.averageResponseTimeMs;
            this.minResponseTimeMs = source.minResponseTimeMs;
            this.maxResponseTimeMs = source.maxResponseTimeMs;
            this.healthScore = source.healthScore;
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

        public Builder withProviderType(ModelProviderType providerType) {
            this.providerType = Objects.requireNonNull(providerType, "providerType");
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

        public Builder withAverageResponseTimeMs(long averageResponseTimeMs) {
            this.averageResponseTimeMs = averageResponseTimeMs;
            return this;
        }

        public Builder withMinResponseTimeMs(long minResponseTimeMs) {
            this.minResponseTimeMs = minResponseTimeMs;
            return this;
        }

        public Builder withMaxResponseTimeMs(long maxResponseTimeMs) {
            this.maxResponseTimeMs = maxResponseTimeMs;
            return this;
        }

        public Builder withHealthScore(double healthScore) {
            this.healthScore = healthScore;
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
        public ProviderStatistics build() {
            validate();
            populateMetrics();
            return new ProviderStatistics(this);
        }

        @Override
        protected void validate() {
            if (id.trim().isEmpty()) {
                addValidationError("id cannot be blank");
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
            if (averageResponseTimeMs < 0) {
                addValidationError("averageResponseTimeMs must be non-negative");
            }
            if (minResponseTimeMs < 0 && minResponseTimeMs != Long.MAX_VALUE) {
                addValidationError("minResponseTimeMs must be non-negative");
            }
            if (maxResponseTimeMs < 0) {
                addValidationError("maxResponseTimeMs must be non-negative");
            }
            if (minResponseTimeMs != Long.MAX_VALUE && maxResponseTimeMs < minResponseTimeMs) {
                addValidationError("maxResponseTimeMs cannot be less than minResponseTimeMs");
            }
            if (healthScore < 0.0 || healthScore > 1.0) {
                addValidationError("healthScore must be between 0.0 and 1.0");
            }
        }

        @Override
        protected void doReset() {
            id = "";
            timestamp = null;
            metrics.clear();
            providerType = ModelProviderType.OPENAI;
            totalExecutions = 0;
            successfulExecutions = 0;
            failedExecutions = 0;
            totalExecutionTimeMs = 0;
            averageResponseTimeMs = 0;
            minResponseTimeMs = Long.MAX_VALUE;
            maxResponseTimeMs = 0;
            healthScore = 0.0;
            lastExecutionTime = null;
            lastSuccessTime = null;
            lastFailureTime = null;
            lastError = null;
        }

        private void populateMetrics() {
            metrics.put("providerType", providerType.name());
            metrics.put("totalExecutions", totalExecutions);
            metrics.put("successfulExecutions", successfulExecutions);
            metrics.put("failedExecutions", failedExecutions);
            metrics.put("totalExecutionTimeMs", totalExecutionTimeMs);
            metrics.put("averageResponseTimeMs", averageResponseTimeMs);
            metrics.put("minResponseTimeMs", minResponseTimeMs == Long.MAX_VALUE ? 0 : minResponseTimeMs);
            metrics.put("maxResponseTimeMs", maxResponseTimeMs);
            metrics.put("healthScore", healthScore);
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
