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
 * Unified Client Statistics implementation.
 *
 * <p>
 * This class provides comprehensive statistics for client operations,
 * including usage metrics, request counts, token usage, costs, and performance data.
 * </p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class ClientStatistics extends BaseStatistics {

    private final String clientId;
    private final ModelProviderType providerType;
    private final String modelName;
    private final long totalRequests;
    private final long successfulRequests;
    private final long failedRequests;
    private final long totalTokens;
    private final double totalCost;
    private final long totalResponseTimeMs;
    private final long averageResponseTimeMs;
    private final long minResponseTimeMs;
    private final long maxResponseTimeMs;
    private final @Nullable Instant lastUsed;
    private final @Nullable Instant lastSuccessTime;
    private final @Nullable Instant lastFailureTime;
    private final @Nullable String lastError;

    private ClientStatistics(Builder builder) {
        super(builder.id, builder.timestamp, StatisticsType.PERFORMANCE, builder.metrics);
        this.clientId = builder.clientId;
        this.providerType = builder.providerType;
        this.modelName = builder.modelName;
        this.totalRequests = builder.totalRequests;
        this.successfulRequests = builder.successfulRequests;
        this.failedRequests = builder.failedRequests;
        this.totalTokens = builder.totalTokens;
        this.totalCost = builder.totalCost;
        this.totalResponseTimeMs = builder.totalResponseTimeMs;
        this.averageResponseTimeMs = builder.averageResponseTimeMs;
        this.minResponseTimeMs = builder.minResponseTimeMs;
        this.maxResponseTimeMs = builder.maxResponseTimeMs;
        this.lastUsed = builder.lastUsed;
        this.lastSuccessTime = builder.lastSuccessTime;
        this.lastFailureTime = builder.lastFailureTime;
        this.lastError = builder.lastError;
    }

    public String getClientId() {
        return clientId;
    }

    public ModelProviderType getProviderType() {
        return providerType;
    }

    public String getModelName() {
        return modelName;
    }

    public long getTotalRequests() {
        return totalRequests;
    }

    public long getSuccessfulRequests() {
        return successfulRequests;
    }

    public long getFailedRequests() {
        return failedRequests;
    }

    public long getTotalTokens() {
        return totalTokens;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public long getTotalResponseTimeMs() {
        return totalResponseTimeMs;
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

    public @Nullable Instant getLastUsed() {
        return lastUsed;
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
        return totalRequests > 0 ? (double) successfulRequests / totalRequests * 100.0 : 0.0;
    }

    public double getFailureRate() {
        return totalRequests > 0 ? (double) failedRequests / totalRequests * 100.0 : 0.0;
    }

    public double getAverageTokensPerRequest() {
        return totalRequests > 0 ? (double) totalTokens / totalRequests : 0.0;
    }

    public double getAverageCostPerRequest() {
        return totalRequests > 0 ? totalCost / totalRequests : 0.0;
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
        ClientStatistics other = (ClientStatistics) obj;
        return Objects.equals(clientId, other.clientId) && providerType == other.providerType
                && Objects.equals(modelName, other.modelName) && totalRequests == other.totalRequests
                && successfulRequests == other.successfulRequests && failedRequests == other.failedRequests
                && totalTokens == other.totalTokens && Double.compare(totalCost, other.totalCost) == 0
                && totalResponseTimeMs == other.totalResponseTimeMs
                && averageResponseTimeMs == other.averageResponseTimeMs && minResponseTimeMs == other.minResponseTimeMs
                && maxResponseTimeMs == other.maxResponseTimeMs && Objects.equals(lastUsed, other.lastUsed)
                && Objects.equals(lastSuccessTime, other.lastSuccessTime)
                && Objects.equals(lastFailureTime, other.lastFailureTime) && Objects.equals(lastError, other.lastError);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), clientId, providerType, modelName, totalRequests, successfulRequests,
                failedRequests, totalTokens, totalCost, totalResponseTimeMs, averageResponseTimeMs, minResponseTimeMs,
                maxResponseTimeMs, lastUsed, lastSuccessTime, lastFailureTime, lastError);
    }

    @Override
    public String toString() {
        return String.format(
                "ClientStatistics{id='%s', clientId='%s', providerType=%s, modelName='%s', totalRequests=%d, successfulRequests=%d, failedRequests=%d, successRate=%.2f%%, totalTokens=%d, totalCost=%.2f}",
                getId(), clientId, providerType, modelName, totalRequests, successfulRequests, failedRequests,
                getSuccessRate(), totalTokens, totalCost);
    }

    public static final class Builder extends AbstractBuilder<ClientStatistics> {
        private String id = "";
        private @Nullable Instant timestamp;
        private final Map<String, Object> metrics = new HashMap<>();
        private String clientId = "";
        private ModelProviderType providerType = ModelProviderType.OPENAI;
        private String modelName = "";
        private long totalRequests = 0;
        private long successfulRequests = 0;
        private long failedRequests = 0;
        private long totalTokens = 0;
        private double totalCost = 0.0;
        private long totalResponseTimeMs = 0;
        private long averageResponseTimeMs = 0;
        private long minResponseTimeMs = Long.MAX_VALUE;
        private long maxResponseTimeMs = 0;
        private @Nullable Instant lastUsed;
        private @Nullable Instant lastSuccessTime;
        private @Nullable Instant lastFailureTime;
        private @Nullable String lastError;

        public Builder() {
        }

        public Builder(ClientStatistics source) {
            this.id = source.getId();
            this.timestamp = source.getTimestamp();
            this.metrics.putAll(source.getMetrics());
            this.clientId = source.clientId;
            this.providerType = source.providerType;
            this.modelName = source.modelName;
            this.totalRequests = source.totalRequests;
            this.successfulRequests = source.successfulRequests;
            this.failedRequests = source.failedRequests;
            this.totalTokens = source.totalTokens;
            this.totalCost = source.totalCost;
            this.totalResponseTimeMs = source.totalResponseTimeMs;
            this.averageResponseTimeMs = source.averageResponseTimeMs;
            this.minResponseTimeMs = source.minResponseTimeMs;
            this.maxResponseTimeMs = source.maxResponseTimeMs;
            this.lastUsed = source.lastUsed;
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

        public Builder withClientId(String clientId) {
            this.clientId = Objects.requireNonNull(clientId, "clientId");
            return this;
        }

        public Builder withProviderType(ModelProviderType providerType) {
            this.providerType = Objects.requireNonNull(providerType, "providerType");
            return this;
        }

        public Builder withModelName(String modelName) {
            this.modelName = Objects.requireNonNull(modelName, "modelName");
            return this;
        }

        public Builder withTotalRequests(long totalRequests) {
            this.totalRequests = totalRequests;
            return this;
        }

        public Builder withSuccessfulRequests(long successfulRequests) {
            this.successfulRequests = successfulRequests;
            return this;
        }

        public Builder withFailedRequests(long failedRequests) {
            this.failedRequests = failedRequests;
            return this;
        }

        public Builder withTotalTokens(long totalTokens) {
            this.totalTokens = totalTokens;
            return this;
        }

        public Builder withTotalCost(double totalCost) {
            this.totalCost = totalCost;
            return this;
        }

        public Builder withTotalResponseTimeMs(long totalResponseTimeMs) {
            this.totalResponseTimeMs = totalResponseTimeMs;
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

        public Builder withLastUsed(@Nullable Instant lastUsed) {
            this.lastUsed = lastUsed;
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
        public ClientStatistics build() {
            validate();
            populateMetrics();
            return new ClientStatistics(this);
        }

        @Override
        protected void validate() {
            if (id.trim().isEmpty()) {
                addValidationError("id cannot be blank");
            }
            if (clientId.trim().isEmpty()) {
                addValidationError("clientId cannot be blank");
            }
            if (modelName.trim().isEmpty()) {
                addValidationError("modelName cannot be blank");
            }
            if (totalRequests < 0) {
                addValidationError("totalRequests must be non-negative");
            }
            if (successfulRequests < 0) {
                addValidationError("successfulRequests must be non-negative");
            }
            if (failedRequests < 0) {
                addValidationError("failedRequests must be non-negative");
            }
            if (successfulRequests + failedRequests > totalRequests) {
                addValidationError("successfulRequests + failedRequests cannot exceed totalRequests");
            }
            if (totalTokens < 0) {
                addValidationError("totalTokens must be non-negative");
            }
            if (totalCost < 0.0) {
                addValidationError("totalCost must be non-negative");
            }
            if (totalResponseTimeMs < 0) {
                addValidationError("totalResponseTimeMs must be non-negative");
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
        }

        @Override
        protected void doReset() {
            id = "";
            timestamp = null;
            metrics.clear();
            clientId = "";
            providerType = ModelProviderType.OPENAI;
            modelName = "";
            totalRequests = 0;
            successfulRequests = 0;
            failedRequests = 0;
            totalTokens = 0;
            totalCost = 0.0;
            totalResponseTimeMs = 0;
            averageResponseTimeMs = 0;
            minResponseTimeMs = Long.MAX_VALUE;
            maxResponseTimeMs = 0;
            lastUsed = null;
            lastSuccessTime = null;
            lastFailureTime = null;
            lastError = null;
        }

        private void populateMetrics() {
            metrics.put("clientId", clientId);
            metrics.put("providerType", providerType.name());
            metrics.put("modelName", modelName);
            metrics.put("totalRequests", totalRequests);
            metrics.put("successfulRequests", successfulRequests);
            metrics.put("failedRequests", failedRequests);
            metrics.put("totalTokens", totalTokens);
            metrics.put("totalCost", totalCost);
            metrics.put("totalResponseTimeMs", totalResponseTimeMs);
            metrics.put("averageResponseTimeMs", averageResponseTimeMs);
            metrics.put("minResponseTimeMs", minResponseTimeMs == Long.MAX_VALUE ? 0 : minResponseTimeMs);
            metrics.put("maxResponseTimeMs", maxResponseTimeMs);
            metrics.put("lastUsed", lastUsed);
            metrics.put("lastSuccessTime", lastSuccessTime);
            metrics.put("lastFailureTime", lastFailureTime);
            metrics.put("lastError", lastError);
            metrics.put("successRate", totalRequests > 0 ? (double) successfulRequests / totalRequests * 100.0 : 0.0);
            metrics.put("failureRate", totalRequests > 0 ? (double) failedRequests / totalRequests * 100.0 : 0.0);
            metrics.put("averageTokensPerRequest", totalRequests > 0 ? (double) totalTokens / totalRequests : 0.0);
            metrics.put("averageCostPerRequest", totalRequests > 0 ? totalCost / totalRequests : 0.0);
        }
    }
}
