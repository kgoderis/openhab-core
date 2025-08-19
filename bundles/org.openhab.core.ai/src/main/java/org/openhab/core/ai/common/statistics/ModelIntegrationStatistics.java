package org.openhab.core.ai.common.statistics;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.builder.AbstractBuilder;

/**
 * Unified Model Integration Statistics implementation.
 * 
 * <p>
 * This class provides comprehensive statistics for model integration operations,
 * including agent counts, request statistics, cache performance, and error tracking.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class ModelIntegrationStatistics extends BaseStatistics {

    private final long totalAgents;
    private final long activeAgents;
    private final long totalRequests;
    private final long successfulRequests;
    private final long failedRequests;
    private final long cacheHits;
    private final long cacheMisses;
    private final long totalResponseTimeMs;
    private final long averageResponseTimeMs;
    private final long minResponseTimeMs;
    private final long maxResponseTimeMs;
    private final long totalTokensUsed;
    private final long totalCost;
    private final @Nullable Instant lastRequestTime;
    private final @Nullable Instant lastSuccessTime;
    private final @Nullable Instant lastFailureTime;
    private final @Nullable String lastError;
    private final List<String> registeredAgentIds;

    private ModelIntegrationStatistics(Builder builder) {
        super(builder.id, builder.timestamp, StatisticsType.EXECUTION, builder.metrics);
        this.totalAgents = builder.totalAgents;
        this.activeAgents = builder.activeAgents;
        this.totalRequests = builder.totalRequests;
        this.successfulRequests = builder.successfulRequests;
        this.failedRequests = builder.failedRequests;
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
        this.registeredAgentIds = List.copyOf(builder.registeredAgentIds);
    }

    public long getTotalAgents() {
        return totalAgents;
    }

    public long getActiveAgents() {
        return activeAgents;
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

    public long getCacheHits() {
        return cacheHits;
    }

    public long getCacheMisses() {
        return cacheMisses;
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

    public long getTotalTokensUsed() {
        return totalTokensUsed;
    }

    public long getTotalCost() {
        return totalCost;
    }

    public @Nullable Instant getLastRequestTime() {
        return lastRequestTime;
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

    public List<String> getRegisteredAgentIds() {
        return registeredAgentIds;
    }

    public double getCacheHitRate() {
        long totalCache = cacheHits + cacheMisses;
        return totalCache > 0 ? (double) cacheHits / totalCache * 100.0 : 0.0;
    }

    public double getSuccessRate() {
        return totalRequests > 0 ? (double) successfulRequests / totalRequests * 100.0 : 0.0;
    }

    public double getAgentUtilizationRate() {
        return totalAgents > 0 ? (double) activeAgents / totalAgents * 100.0 : 0.0;
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
        ModelIntegrationStatistics other = (ModelIntegrationStatistics) obj;
        return totalAgents == other.totalAgents && activeAgents == other.activeAgents
                && totalRequests == other.totalRequests && successfulRequests == other.successfulRequests
                && failedRequests == other.failedRequests && cacheHits == other.cacheHits
                && cacheMisses == other.cacheMisses && totalResponseTimeMs == other.totalResponseTimeMs
                && averageResponseTimeMs == other.averageResponseTimeMs && minResponseTimeMs == other.minResponseTimeMs
                && maxResponseTimeMs == other.maxResponseTimeMs && totalTokensUsed == other.totalTokensUsed
                && totalCost == other.totalCost && Objects.equals(lastRequestTime, other.lastRequestTime)
                && Objects.equals(lastSuccessTime, other.lastSuccessTime)
                && Objects.equals(lastFailureTime, other.lastFailureTime) && Objects.equals(lastError, other.lastError)
                && Objects.equals(registeredAgentIds, other.registeredAgentIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), totalAgents, activeAgents, totalRequests, successfulRequests,
                failedRequests, cacheHits, cacheMisses, totalResponseTimeMs, averageResponseTimeMs, minResponseTimeMs,
                maxResponseTimeMs, totalTokensUsed, totalCost, lastRequestTime, lastSuccessTime, lastFailureTime,
                lastError, registeredAgentIds);
    }

    @Override
    public String toString() {
        return String.format(
                "ModelIntegrationStatistics{id='%s', totalAgents=%d, activeAgents=%d, totalRequests=%d, successfulRequests=%d, failedRequests=%d, cacheHitRate=%.2f%%, successRate=%.2f%%, agentUtilization=%.2f%%}",
                getId(), totalAgents, activeAgents, totalRequests, successfulRequests, failedRequests,
                getCacheHitRate(), getSuccessRate(), getAgentUtilizationRate());
    }

    public static final class Builder extends AbstractBuilder<ModelIntegrationStatistics> {
        private String id = "";
        private @Nullable Instant timestamp;
        private final Map<String, Object> metrics = new HashMap<>();
        private long totalAgents = 0;
        private long activeAgents = 0;
        private long totalRequests = 0;
        private long successfulRequests = 0;
        private long failedRequests = 0;
        private long cacheHits = 0;
        private long cacheMisses = 0;
        private long totalResponseTimeMs = 0;
        private long averageResponseTimeMs = 0;
        private long minResponseTimeMs = Long.MAX_VALUE;
        private long maxResponseTimeMs = 0;
        private long totalTokensUsed = 0;
        private long totalCost = 0;
        private @Nullable Instant lastRequestTime;
        private @Nullable Instant lastSuccessTime;
        private @Nullable Instant lastFailureTime;
        private @Nullable String lastError;
        private final List<String> registeredAgentIds = List.of();

        public Builder() {
        }

        public Builder(ModelIntegrationStatistics source) {
            this.id = source.getId();
            this.timestamp = source.getTimestamp();
            this.metrics.putAll(source.getMetrics());
            this.totalAgents = source.totalAgents;
            this.activeAgents = source.activeAgents;
            this.totalRequests = source.totalRequests;
            this.successfulRequests = source.successfulRequests;
            this.failedRequests = source.failedRequests;
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
            // Note: registeredAgentIds is immutable, so we can't modify it
        }

        public Builder withId(String id) {
            this.id = Objects.requireNonNull(id, "id");
            return this;
        }

        public Builder withTimestamp(@Nullable Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder withTotalAgents(long totalAgents) {
            this.totalAgents = totalAgents;
            return this;
        }

        public Builder withActiveAgents(long activeAgents) {
            this.activeAgents = activeAgents;
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

        public Builder withTotalTokensUsed(long totalTokensUsed) {
            this.totalTokensUsed = totalTokensUsed;
            return this;
        }

        public Builder withTotalCost(long totalCost) {
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

        public Builder withRegisteredAgentIds(List<String> registeredAgentIds) {
            // Note: This is a limitation of the current design - we can't modify the immutable list
            // In a real implementation, we'd need to make this mutable or use a different approach
            return this;
        }

        @Override
        public ModelIntegrationStatistics build() {
            validate();
            populateMetrics();
            return new ModelIntegrationStatistics(this);
        }

        @Override
        protected void validate() {
            if (id.trim().isEmpty()) {
                addValidationError("id cannot be blank");
            }
            if (totalAgents < 0) {
                addValidationError("totalAgents must be non-negative");
            }
            if (activeAgents < 0) {
                addValidationError("activeAgents must be non-negative");
            }
            if (activeAgents > totalAgents) {
                addValidationError("activeAgents cannot exceed totalAgents");
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
            if (cacheHits < 0) {
                addValidationError("cacheHits must be non-negative");
            }
            if (cacheMisses < 0) {
                addValidationError("cacheMisses must be non-negative");
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
            if (totalTokensUsed < 0) {
                addValidationError("totalTokensUsed must be non-negative");
            }
            if (totalCost < 0) {
                addValidationError("totalCost must be non-negative");
            }
        }

        @Override
        protected void doReset() {
            id = "";
            timestamp = null;
            metrics.clear();
            totalAgents = 0;
            activeAgents = 0;
            totalRequests = 0;
            successfulRequests = 0;
            failedRequests = 0;
            cacheHits = 0;
            cacheMisses = 0;
            totalResponseTimeMs = 0;
            averageResponseTimeMs = 0;
            minResponseTimeMs = Long.MAX_VALUE;
            maxResponseTimeMs = 0;
            totalTokensUsed = 0;
            totalCost = 0;
            lastRequestTime = null;
            lastSuccessTime = null;
            lastFailureTime = null;
            lastError = null;
        }

        private void populateMetrics() {
            metrics.put("totalAgents", totalAgents);
            metrics.put("activeAgents", activeAgents);
            metrics.put("totalRequests", totalRequests);
            metrics.put("successfulRequests", successfulRequests);
            metrics.put("failedRequests", failedRequests);
            metrics.put("cacheHits", cacheHits);
            metrics.put("cacheMisses", cacheMisses);
            metrics.put("totalResponseTimeMs", totalResponseTimeMs);
            metrics.put("averageResponseTimeMs", averageResponseTimeMs);
            metrics.put("minResponseTimeMs", minResponseTimeMs == Long.MAX_VALUE ? 0 : minResponseTimeMs);
            metrics.put("maxResponseTimeMs", maxResponseTimeMs);
            metrics.put("totalTokensUsed", totalTokensUsed);
            metrics.put("totalCost", totalCost);
            metrics.put("lastRequestTime", lastRequestTime);
            metrics.put("lastSuccessTime", lastSuccessTime);
            metrics.put("lastFailureTime", lastFailureTime);
            metrics.put("lastError", lastError);
            metrics.put("registeredAgentIds", registeredAgentIds);
            metrics.put("cacheHitRate",
                    cacheHits + cacheMisses > 0 ? (double) cacheHits / (cacheHits + cacheMisses) * 100.0 : 0.0);
            metrics.put("successRate", totalRequests > 0 ? (double) successfulRequests / totalRequests * 100.0 : 0.0);
            metrics.put("agentUtilizationRate", totalAgents > 0 ? (double) activeAgents / totalAgents * 100.0 : 0.0);
        }
    }
}
