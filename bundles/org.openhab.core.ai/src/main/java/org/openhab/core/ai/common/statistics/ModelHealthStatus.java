package org.openhab.core.ai.common.statistics;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.builder.AbstractBuilder;

/**
 * Unified Model Health Status implementation.
 * 
 * <p>
 * This class provides comprehensive health status information for model integration,
 * including overall health state, model availability, error rates, and performance metrics.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class ModelHealthStatus extends BaseStatistics {

    public enum HealthState {
        HEALTHY,
        DEGRADED,
        UNHEALTHY,
        UNKNOWN
    }

    private final HealthState overallHealth;
    private final boolean primaryModelAvailable;
    private final boolean fallbackModelAvailable;
    private final double errorRate;
    private final double responseTimeMs;
    private final long totalRequests;
    private final long failedRequests;
    private final @Nullable String lastError;
    private final @Nullable Instant lastHealthCheck;
    private final @Nullable Instant lastSuccessfulRequest;
    private final @Nullable Instant lastFailedRequest;

    private ModelHealthStatus(Builder builder) {
        super(builder.id, builder.timestamp, StatisticsType.MONITORING, builder.metrics);
        this.overallHealth = builder.overallHealth;
        this.primaryModelAvailable = builder.primaryModelAvailable;
        this.fallbackModelAvailable = builder.fallbackModelAvailable;
        this.errorRate = builder.errorRate;
        this.responseTimeMs = builder.responseTimeMs;
        this.totalRequests = builder.totalRequests;
        this.failedRequests = builder.failedRequests;
        this.lastError = builder.lastError;
        this.lastHealthCheck = builder.lastHealthCheck;
        this.lastSuccessfulRequest = builder.lastSuccessfulRequest;
        this.lastFailedRequest = builder.lastFailedRequest;
    }

    public HealthState getOverallHealth() {
        return overallHealth;
    }

    public boolean isPrimaryModelAvailable() {
        return primaryModelAvailable;
    }

    public boolean isFallbackModelAvailable() {
        return fallbackModelAvailable;
    }

    public double getErrorRate() {
        return errorRate;
    }

    public double getResponseTimeMs() {
        return responseTimeMs;
    }

    public long getTotalRequests() {
        return totalRequests;
    }

    public long getFailedRequests() {
        return failedRequests;
    }

    public @Nullable String getLastError() {
        return lastError;
    }

    public @Nullable Instant getLastHealthCheck() {
        return lastHealthCheck;
    }

    public @Nullable Instant getLastSuccessfulRequest() {
        return lastSuccessfulRequest;
    }

    public @Nullable Instant getLastFailedRequest() {
        return lastFailedRequest;
    }

    public boolean isHealthy() {
        return overallHealth == HealthState.HEALTHY;
    }

    public boolean isDegraded() {
        return overallHealth == HealthState.DEGRADED;
    }

    public boolean isUnhealthy() {
        return overallHealth == HealthState.UNHEALTHY;
    }

    public boolean isUnknown() {
        return overallHealth == HealthState.UNKNOWN;
    }

    public double getSuccessRate() {
        return totalRequests > 0 ? (double) (totalRequests - failedRequests) / totalRequests * 100.0 : 0.0;
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
        ModelHealthStatus other = (ModelHealthStatus) obj;
        return overallHealth == other.overallHealth && primaryModelAvailable == other.primaryModelAvailable
                && fallbackModelAvailable == other.fallbackModelAvailable
                && Double.compare(errorRate, other.errorRate) == 0
                && Double.compare(responseTimeMs, other.responseTimeMs) == 0 && totalRequests == other.totalRequests
                && failedRequests == other.failedRequests && Objects.equals(lastError, other.lastError)
                && Objects.equals(lastHealthCheck, other.lastHealthCheck)
                && Objects.equals(lastSuccessfulRequest, other.lastSuccessfulRequest)
                && Objects.equals(lastFailedRequest, other.lastFailedRequest);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), overallHealth, primaryModelAvailable, fallbackModelAvailable, errorRate,
                responseTimeMs, totalRequests, failedRequests, lastError, lastHealthCheck, lastSuccessfulRequest,
                lastFailedRequest);
    }

    @Override
    public String toString() {
        return String.format(
                "ModelHealthStatus{id='%s', overallHealth=%s, primaryModelAvailable=%s, fallbackModelAvailable=%s, errorRate=%.2f%%, successRate=%.2f%%, responseTimeMs=%.2f}",
                getId(), overallHealth, primaryModelAvailable, fallbackModelAvailable, errorRate, getSuccessRate(),
                responseTimeMs);
    }

    public static final class Builder extends AbstractBuilder<ModelHealthStatus> {
        private String id = "";
        private @Nullable Instant timestamp;
        private final Map<String, Object> metrics = new HashMap<>();
        private HealthState overallHealth = HealthState.UNKNOWN;
        private boolean primaryModelAvailable = false;
        private boolean fallbackModelAvailable = false;
        private double errorRate = 0.0;
        private double responseTimeMs = 0.0;
        private long totalRequests = 0;
        private long failedRequests = 0;
        private @Nullable String lastError;
        private @Nullable Instant lastHealthCheck;
        private @Nullable Instant lastSuccessfulRequest;
        private @Nullable Instant lastFailedRequest;

        public Builder() {
        }

        public Builder(ModelHealthStatus source) {
            this.id = source.getId();
            this.timestamp = source.getTimestamp();
            this.metrics.putAll(source.getMetrics());
            this.overallHealth = source.overallHealth;
            this.primaryModelAvailable = source.primaryModelAvailable;
            this.fallbackModelAvailable = source.fallbackModelAvailable;
            this.errorRate = source.errorRate;
            this.responseTimeMs = source.responseTimeMs;
            this.totalRequests = source.totalRequests;
            this.failedRequests = source.failedRequests;
            this.lastError = source.lastError;
            this.lastHealthCheck = source.lastHealthCheck;
            this.lastSuccessfulRequest = source.lastSuccessfulRequest;
            this.lastFailedRequest = source.lastFailedRequest;
        }

        public Builder withId(String id) {
            this.id = Objects.requireNonNull(id, "id");
            return this;
        }

        public Builder withTimestamp(@Nullable Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder withOverallHealth(HealthState overallHealth) {
            this.overallHealth = Objects.requireNonNull(overallHealth, "overallHealth");
            return this;
        }

        public Builder withPrimaryModelAvailable(boolean primaryModelAvailable) {
            this.primaryModelAvailable = primaryModelAvailable;
            return this;
        }

        public Builder withFallbackModelAvailable(boolean fallbackModelAvailable) {
            this.fallbackModelAvailable = fallbackModelAvailable;
            return this;
        }

        public Builder withErrorRate(double errorRate) {
            this.errorRate = errorRate;
            return this;
        }

        public Builder withResponseTimeMs(double responseTimeMs) {
            this.responseTimeMs = responseTimeMs;
            return this;
        }

        public Builder withTotalRequests(long totalRequests) {
            this.totalRequests = totalRequests;
            return this;
        }

        public Builder withFailedRequests(long failedRequests) {
            this.failedRequests = failedRequests;
            return this;
        }

        public Builder withLastError(@Nullable String lastError) {
            this.lastError = lastError;
            return this;
        }

        public Builder withLastHealthCheck(@Nullable Instant lastHealthCheck) {
            this.lastHealthCheck = lastHealthCheck;
            return this;
        }

        public Builder withLastSuccessfulRequest(@Nullable Instant lastSuccessfulRequest) {
            this.lastSuccessfulRequest = lastSuccessfulRequest;
            return this;
        }

        public Builder withLastFailedRequest(@Nullable Instant lastFailedRequest) {
            this.lastFailedRequest = lastFailedRequest;
            return this;
        }

        @Override
        public ModelHealthStatus build() {
            validate();
            populateMetrics();
            return new ModelHealthStatus(this);
        }

        @Override
        protected void validate() {
            if (id.trim().isEmpty()) {
                addValidationError("id cannot be blank");
            }
            if (errorRate < 0.0 || errorRate > 100.0) {
                addValidationError("errorRate must be between 0.0 and 100.0");
            }
            if (responseTimeMs < 0.0) {
                addValidationError("responseTimeMs must be non-negative");
            }
            if (totalRequests < 0) {
                addValidationError("totalRequests must be non-negative");
            }
            if (failedRequests < 0) {
                addValidationError("failedRequests must be non-negative");
            }
            if (failedRequests > totalRequests) {
                addValidationError("failedRequests cannot exceed totalRequests");
            }
        }

        @Override
        protected void doReset() {
            id = "";
            timestamp = null;
            metrics.clear();
            overallHealth = HealthState.UNKNOWN;
            primaryModelAvailable = false;
            fallbackModelAvailable = false;
            errorRate = 0.0;
            responseTimeMs = 0.0;
            totalRequests = 0;
            failedRequests = 0;
            lastError = null;
            lastHealthCheck = null;
            lastSuccessfulRequest = null;
            lastFailedRequest = null;
        }

        private void populateMetrics() {
            metrics.put("overallHealth", overallHealth.name());
            metrics.put("primaryModelAvailable", primaryModelAvailable);
            metrics.put("fallbackModelAvailable", fallbackModelAvailable);
            metrics.put("errorRate", errorRate);
            metrics.put("responseTimeMs", responseTimeMs);
            metrics.put("totalRequests", totalRequests);
            metrics.put("failedRequests", failedRequests);
            metrics.put("lastError", lastError);
            metrics.put("lastHealthCheck", lastHealthCheck);
            metrics.put("lastSuccessfulRequest", lastSuccessfulRequest);
            metrics.put("lastFailedRequest", lastFailedRequest);
            metrics.put("successRate",
                    totalRequests > 0 ? (double) (totalRequests - failedRequests) / totalRequests * 100.0 : 0.0);
            metrics.put("isHealthy", isHealthy());
            metrics.put("isDegraded", isDegraded());
            metrics.put("isUnhealthy", isUnhealthy());
            metrics.put("isUnknown", isUnknown());
        }

        private boolean isHealthy() {
            return overallHealth == HealthState.HEALTHY;
        }

        private boolean isDegraded() {
            return overallHealth == HealthState.DEGRADED;
        }

        private boolean isUnhealthy() {
            return overallHealth == HealthState.UNHEALTHY;
        }

        private boolean isUnknown() {
            return overallHealth == HealthState.UNKNOWN;
        }
    }
}
