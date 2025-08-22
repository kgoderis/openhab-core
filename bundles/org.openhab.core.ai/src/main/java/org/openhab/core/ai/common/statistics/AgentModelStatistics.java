package org.openhab.core.ai.common.statistics;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MonitoringType;
import org.openhab.core.ai.common.monitoring.base.AbstractStatistics;

/**
 * Statistics for agent-model interactions and performance.
 * 
 * <p>
 * This class provides comprehensive statistics about agent-model interactions,
 * including request counts, response times, cache performance, token usage,
 * and cost tracking.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class AgentModelStatistics extends AbstractStatistics {

    private final String agentId;
    private final long cacheHits;
    private final long cacheMisses;
    private final long totalResponseTimeMs;
    private final double averageResponseTimeMs;
    private final long minResponseTimeMs;
    private final long maxResponseTimeMs;
    private final long totalTokensUsed;
    private final double totalCost;
    private final @Nullable Instant lastRequestTime;
    private final @Nullable Instant lastSuccessTime;
    private final @Nullable Instant lastFailureTime;
    private final @Nullable String lastError;

    private AgentModelStatistics(Builder builder) {
        super(builder.id, builder.timestamp, "agent-model", builder.source, builder.description, builder.data,
                builder.totalRequests, builder.successfulRequests, builder.failedRequests, builder.collectionStartTime,
                builder.collectionEndTime, builder.additionalMeasures);
        this.agentId = builder.agentId;
        this.cacheHits = builder.cacheHits;
        this.cacheMisses = builder.cacheMisses;
        this.totalResponseTimeMs = builder.totalResponseTimeMs;
        this.averageResponseTimeMs = builder.averageResponseTimeMs;
        this.minResponseTimeMs = builder.minResponseTimeMs;
        this.maxResponseTimeMs = builder.maxResponseTimeMs;
        this.totalTokensUsed = builder.totalTokensUsed;
        this.totalCost = builder.totalCost;
        this.lastRequestTime = builder.lastRequestTime;
        this.lastSuccessTime = builder.lastSuccessTime;
        this.lastFailureTime = builder.lastFailureTime;
        this.lastError = builder.lastError;
    }

    /**
     * Create a new builder for AgentModelStatistics.
     * 
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create a new builder for AgentModelStatistics with required fields.
     * 
     * @param id the unique identifier
     * @param agentId the agent identifier
     * @return a new builder instance
     */
    public static Builder builder(String id, String agentId) {
        return new Builder(id, agentId);
    }

    /**
     * Get the agent identifier.
     * 
     * @return the agent identifier
     */
    public String getAgentId() {
        return agentId;
    }

    /**
     * Get the number of cache hits.
     * 
     * @return the cache hits count
     */
    public long getCacheHits() {
        return cacheHits;
    }

    /**
     * Get the number of cache misses.
     * 
     * @return the cache misses count
     */
    public long getCacheMisses() {
        return cacheMisses;
    }

    /**
     * Get the total response time in milliseconds.
     * 
     * @return the total response time
     */
    public long getTotalResponseTimeMs() {
        return totalResponseTimeMs;
    }

    /**
     * Get the average response time in milliseconds.
     * 
     * @return the average response time
     */
    public double getAverageResponseTimeMs() {
        return averageResponseTimeMs;
    }

    /**
     * Get the minimum response time in milliseconds.
     * 
     * @return the minimum response time
     */
    public long getMinResponseTimeMs() {
        return minResponseTimeMs;
    }

    /**
     * Get the maximum response time in milliseconds.
     * 
     * @return the maximum response time
     */
    public long getMaxResponseTimeMs() {
        return maxResponseTimeMs;
    }

    /**
     * Get the total tokens used.
     * 
     * @return the total tokens used
     */
    public long getTotalTokensUsed() {
        return totalTokensUsed;
    }

    /**
     * Get the total cost.
     * 
     * @return the total cost
     */
    public double getTotalCost() {
        return totalCost;
    }

    /**
     * Get the timestamp of the last request.
     * 
     * @return the last request time, or null if no requests
     */
    public @Nullable Instant getLastRequestTime() {
        return lastRequestTime;
    }

    /**
     * Get the timestamp of the last successful request.
     * 
     * @return the last success time, or null if no successful requests
     */
    public @Nullable Instant getLastSuccessTime() {
        return lastSuccessTime;
    }

    /**
     * Get the timestamp of the last failed request.
     * 
     * @return the last failure time, or null if no failed requests
     */
    public @Nullable Instant getLastFailureTime() {
        return lastFailureTime;
    }

    /**
     * Get the last error message.
     * 
     * @return the last error message, or null if no errors
     */
    public @Nullable String getLastError() {
        return lastError;
    }

    /**
     * Calculate the cache hit rate as a percentage.
     * 
     * @return the cache hit rate (0.0 to 100.0)
     */
    public double getCacheHitRate() {
        long totalCache = cacheHits + cacheMisses;
        return totalCache > 0 ? (double) cacheHits / totalCache * 100.0 : 0.0;
    }

    /**
     * Calculate the success rate as a percentage.
     * 
     * @return the success rate (0.0 to 100.0)
     */
    public double getSuccessRate() {
        long total = getTotalCount();
        return total > 0 ? (double) getSuccessCount() / total * 100.0 : 0.0;
    }

    @Override
    public MonitoringType getType() {
        return MonitoringType.STATISTICS;
    }

    /**
     * Create a new builder with the current values.
     * 
     * @return a new builder instance
     */
    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AgentModelStatistics other = (AgentModelStatistics) obj;
        return Objects.equals(agentId, other.agentId) && cacheHits == other.cacheHits
                && cacheMisses == other.cacheMisses && totalResponseTimeMs == other.totalResponseTimeMs
                && Double.compare(averageResponseTimeMs, other.averageResponseTimeMs) == 0
                && minResponseTimeMs == other.minResponseTimeMs && maxResponseTimeMs == other.maxResponseTimeMs
                && totalTokensUsed == other.totalTokensUsed && Double.compare(totalCost, other.totalCost) == 0
                && Objects.equals(lastRequestTime, other.lastRequestTime)
                && Objects.equals(lastSuccessTime, other.lastSuccessTime)
                && Objects.equals(lastFailureTime, other.lastFailureTime) && Objects.equals(lastError, other.lastError);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hashCode(agentId);
        result = prime * result + (int) (cacheHits ^ (cacheHits >>> 32));
        result = prime * result + (int) (cacheMisses ^ (cacheMisses >>> 32));
        result = prime * result + (int) (totalResponseTimeMs ^ (totalResponseTimeMs >>> 32));
        result = prime * result + Double.hashCode(averageResponseTimeMs);
        result = prime * result + (int) (minResponseTimeMs ^ (minResponseTimeMs >>> 32));
        result = prime * result + (int) (maxResponseTimeMs ^ (maxResponseTimeMs >>> 32));
        result = prime * result + (int) (totalTokensUsed ^ (totalTokensUsed >>> 32));
        result = prime * result + Double.hashCode(totalCost);
        result = prime * result + Objects.hashCode(lastRequestTime);
        result = prime * result + Objects.hashCode(lastSuccessTime);
        result = prime * result + Objects.hashCode(lastFailureTime);
        result = prime * result + Objects.hashCode(lastError);
        return result;
    }

    @Override
    public String toString() {
        return "AgentModelStatistics{" + "id='" + getId() + '\'' + ", agentId='" + agentId + '\'' + ", totalRequests="
                + getTotalCount() + ", successfulRequests=" + getSuccessCount() + ", failedRequests="
                + getFailureCount() + ", cacheHits=" + cacheHits + ", cacheMisses=" + cacheMisses
                + ", totalResponseTimeMs=" + totalResponseTimeMs + ", averageResponseTimeMs=" + averageResponseTimeMs
                + ", minResponseTimeMs=" + minResponseTimeMs + ", maxResponseTimeMs=" + maxResponseTimeMs
                + ", totalTokensUsed=" + totalTokensUsed + ", totalCost=" + totalCost + ", lastRequestTime="
                + lastRequestTime + ", lastSuccessTime=" + lastSuccessTime + ", lastFailureTime=" + lastFailureTime
                + ", lastError='" + lastError + '\'' + '}';
    }

    /**
     * Builder for AgentModelStatistics.
     */
    public static final class Builder {
        private String id;
        private String agentId;
        private Instant timestamp = Instant.now();
        private @Nullable String source;
        private @Nullable String description;
        private @Nullable Map<String, Object> data;
        private long totalRequests;
        private long successfulRequests;
        private long failedRequests;
        private long cacheHits;
        private long cacheMisses;
        private long totalResponseTimeMs;
        private double averageResponseTimeMs;
        private long minResponseTimeMs = Long.MAX_VALUE;
        private long maxResponseTimeMs;
        private long totalTokensUsed;
        private double totalCost;
        private @Nullable Instant lastRequestTime;
        private @Nullable Instant lastSuccessTime;
        private @Nullable Instant lastFailureTime;
        private @Nullable String lastError;
        private @Nullable Instant collectionStartTime;
        private @Nullable Instant collectionEndTime;
        private Map<String, Double> additionalMeasures = new java.util.HashMap<>();

        private Builder() {
        }

        private Builder(String id, String agentId) {
            this.id = Objects.requireNonNull(id, "id");
            this.agentId = Objects.requireNonNull(agentId, "agentId");
        }

        private Builder(AgentModelStatistics source) {
            this.id = source.getId();
            this.agentId = source.agentId;
            this.timestamp = source.getTimestamp();
            this.source = source.getSource();
            this.description = source.getDescription();
            this.data = source.getData();
            this.totalRequests = source.getTotalCount();
            this.successfulRequests = source.getSuccessCount();
            this.failedRequests = source.getFailureCount();
            this.cacheHits = source.cacheHits;
            this.cacheMisses = source.cacheMisses;
            this.totalResponseTimeMs = source.totalResponseTimeMs;
            this.averageResponseTimeMs = source.averageResponseTimeMs;
            this.minResponseTimeMs = source.minResponseTimeMs;
            this.maxResponseTimeMs = source.maxResponseTimeMs;
            this.totalTokensUsed = source.totalTokensUsed;
            this.totalCost = source.totalCost;
            this.lastRequestTime = source.lastRequestTime;
            this.lastSuccessTime = source.lastSuccessTime;
            this.lastFailureTime = source.lastFailureTime;
            this.lastError = source.lastError;
            this.collectionStartTime = source.getCollectionStartTime();
            this.collectionEndTime = source.getCollectionEndTime();
            this.additionalMeasures = new java.util.HashMap<>(
                    source.getAdditionalMeasures() != null ? source.getAdditionalMeasures() : java.util.Map.of());
        }

        public Builder withId(String id) {
            this.id = Objects.requireNonNull(id, "id");
            return this;
        }

        public Builder withAgentId(String agentId) {
            this.agentId = Objects.requireNonNull(agentId, "agentId");
            return this;
        }

        public Builder withTimestamp(Instant timestamp) {
            this.timestamp = Objects.requireNonNull(timestamp, "timestamp");
            return this;
        }

        public Builder withSource(@Nullable String source) {
            this.source = source;
            return this;
        }

        public Builder withDescription(@Nullable String description) {
            this.description = description;
            return this;
        }

        public Builder withData(@Nullable Map<String, Object> data) {
            this.data = data;
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

        public Builder withCacheHits(long cacheHits) {
            this.cacheHits = cacheHits;
            return this;
        }

        public Builder withCacheMisses(long cacheMisses) {
            this.cacheMisses = cacheMisses;
            return this;
        }

        public Builder withTotalResponseTimeMs(long totalResponseTimeMs) {
            this.totalResponseTimeMs = totalResponseTimeMs;
            return this;
        }

        public Builder withAverageResponseTimeMs(double averageResponseTimeMs) {
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

        public Builder withTotalTokensUsed(long totalTokensUsed) {
            this.totalTokensUsed = totalTokensUsed;
            return this;
        }

        public Builder withTotalCost(double totalCost) {
            this.totalCost = totalCost;
            return this;
        }

        public Builder withLastRequestTime(@Nullable Instant lastRequestTime) {
            this.lastRequestTime = lastRequestTime;
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

        public Builder withCollectionStartTime(@Nullable Instant collectionStartTime) {
            this.collectionStartTime = collectionStartTime;
            return this;
        }

        public Builder withCollectionEndTime(@Nullable Instant collectionEndTime) {
            this.collectionEndTime = collectionEndTime;
            return this;
        }

        public Builder withAdditionalMeasure(String name, double value) {
            this.additionalMeasures.put(Objects.requireNonNull(name, "name"), value);
            return this;
        }

        public Builder withAdditionalMeasures(Map<String, Double> additionalMeasures) {
            this.additionalMeasures.putAll(Objects.requireNonNull(additionalMeasures, "additionalMeasures"));
            return this;
        }

        /**
         * Check if the builder configuration is valid.
         * 
         * @return true if valid, false otherwise
         */
        public boolean isValid() {
            return getValidationErrors().isEmpty();
        }

        /**
         * Get validation errors for the current configuration.
         * 
         * @return list of validation error messages
         */
        public List<String> getValidationErrors() {
            List<String> errors = new ArrayList<>();
            if (id == null || id.isBlank()) {
                errors.add("id cannot be blank");
            }
            if (agentId == null || agentId.isBlank()) {
                errors.add("agentId cannot be blank");
            }
            if (totalRequests < 0) {
                errors.add("totalRequests cannot be negative");
            }
            if (successfulRequests < 0) {
                errors.add("successfulRequests cannot be negative");
            }
            if (failedRequests < 0) {
                errors.add("failedRequests cannot be negative");
            }
            if (cacheHits < 0) {
                errors.add("cacheHits cannot be negative");
            }
            if (cacheMisses < 0) {
                errors.add("cacheMisses cannot be negative");
            }
            if (totalResponseTimeMs < 0) {
                errors.add("totalResponseTimeMs cannot be negative");
            }
            if (averageResponseTimeMs < 0) {
                errors.add("averageResponseTimeMs cannot be negative");
            }
            if (minResponseTimeMs < 0) {
                errors.add("minResponseTimeMs cannot be negative");
            }
            if (maxResponseTimeMs < 0) {
                errors.add("maxResponseTimeMs cannot be negative");
            }
            if (totalTokensUsed < 0) {
                errors.add("totalTokensUsed cannot be negative");
            }
            if (totalCost < 0) {
                errors.add("totalCost cannot be negative");
            }
            if (totalRequests != successfulRequests + failedRequests) {
                errors.add("totalRequests must equal successfulRequests + failedRequests");
            }
            return errors;
        }

        /**
         * Build the AgentModelStatistics instance.
         * 
         * @return the built instance
         * @throws IllegalArgumentException if the configuration is invalid
         */
        public AgentModelStatistics build() {
            List<String> errors = getValidationErrors();
            if (!errors.isEmpty()) {
                throw new IllegalArgumentException(
                        "Invalid AgentModelStatistics configuration: " + String.join(", ", errors));
            }
            return new AgentModelStatistics(this);
        }
    }
}
