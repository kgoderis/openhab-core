package org.openhab.core.ai.agent.api;

import java.time.Instant;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Overall model integration statistics across all agents
 * 
 * <p>
 * This class tracks:
 * - Total model requests and responses across all agents
 * - Overall success and failure rates
 * - Average response times across all agents
 * - Overall cache hit rates
 * - Error rates and types across all agents
 * - Performance metrics across all agents
 * - Resource utilization and optimization metrics
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class ModelIntegrationStatistics {

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
    private final Instant lastRequestTime;
    private final Instant lastSuccessTime;
    private final Instant lastFailureTime;
    private final String lastError;
    private final List<String> registeredAgentIds;

    private ModelIntegrationStatistics(Builder builder) {
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
        this.registeredAgentIds = builder.registeredAgentIds;
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

    public Instant getLastRequestTime() {
        return lastRequestTime;
    }

    public Instant getLastSuccessTime() {
        return lastSuccessTime;
    }

    public Instant getLastFailureTime() {
        return lastFailureTime;
    }

    public String getLastError() {
        return lastError;
    }

    public List<String> getRegisteredAgentIds() {
        return registeredAgentIds;
    }

    public double getSuccessRate() {
        return totalRequests > 0 ? (double) successfulRequests / totalRequests : 0.0;
    }

    public double getFailureRate() {
        return totalRequests > 0 ? (double) failedRequests / totalRequests : 0.0;
    }

    public double getCacheHitRate() {
        long totalCacheRequests = cacheHits + cacheMisses;
        return totalCacheRequests > 0 ? (double) cacheHits / totalCacheRequests : 0.0;
    }

    public double getActiveAgentRate() {
        return totalAgents > 0 ? (double) activeAgents / totalAgents : 0.0;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
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
        private Instant lastRequestTime = Instant.now();
        private Instant lastSuccessTime = Instant.now();
        private Instant lastFailureTime = Instant.now();
        private String lastError = "";
        private List<String> registeredAgentIds = List.of();

        public Builder totalAgents(long totalAgents) {
            this.totalAgents = totalAgents;
            return this;
        }

        public Builder activeAgents(long activeAgents) {
            this.activeAgents = activeAgents;
            return this;
        }

        public Builder totalRequests(long totalRequests) {
            this.totalRequests = totalRequests;
            return this;
        }

        public Builder successfulRequests(long successfulRequests) {
            this.successfulRequests = successfulRequests;
            return this;
        }

        public Builder failedRequests(long failedRequests) {
            this.failedRequests = failedRequests;
            return this;
        }

        public Builder cacheHits(long cacheHits) {
            this.cacheHits = cacheHits;
            return this;
        }

        public Builder cacheMisses(long cacheMisses) {
            this.cacheMisses = cacheMisses;
            return this;
        }

        public Builder totalResponseTimeMs(long totalResponseTimeMs) {
            this.totalResponseTimeMs = totalResponseTimeMs;
            return this;
        }

        public Builder averageResponseTimeMs(long averageResponseTimeMs) {
            this.averageResponseTimeMs = averageResponseTimeMs;
            return this;
        }

        public Builder minResponseTimeMs(long minResponseTimeMs) {
            this.minResponseTimeMs = minResponseTimeMs;
            return this;
        }

        public Builder maxResponseTimeMs(long maxResponseTimeMs) {
            this.maxResponseTimeMs = maxResponseTimeMs;
            return this;
        }

        public Builder totalTokensUsed(long totalTokensUsed) {
            this.totalTokensUsed = totalTokensUsed;
            return this;
        }

        public Builder totalCost(long totalCost) {
            this.totalCost = totalCost;
            return this;
        }

        public Builder lastRequestTime(Instant lastRequestTime) {
            this.lastRequestTime = lastRequestTime;
            return this;
        }

        public Builder lastSuccessTime(Instant lastSuccessTime) {
            this.lastSuccessTime = lastSuccessTime;
            return this;
        }

        public Builder lastFailureTime(Instant lastFailureTime) {
            this.lastFailureTime = lastFailureTime;
            return this;
        }

        public Builder lastError(String lastError) {
            this.lastError = lastError;
            return this;
        }

        public Builder registeredAgentIds(List<String> registeredAgentIds) {
            this.registeredAgentIds = registeredAgentIds;
            return this;
        }

        public ModelIntegrationStatistics build() {
            return new ModelIntegrationStatistics(this);
        }
    }
}
