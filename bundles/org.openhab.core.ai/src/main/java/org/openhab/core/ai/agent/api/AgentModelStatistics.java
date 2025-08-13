package org.openhab.core.ai.agent.api;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Agent-specific model integration statistics
 * 
 * <p>
 * This class tracks:
 * - Total model requests and responses
 * - Success and failure rates
 * - Average response times
 * - Cache hit rates
 * - Error rates and types
 * - Performance metrics
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class AgentModelStatistics {

    private final String agentId;
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

    private AgentModelStatistics(AgentModelStatisticsBuilder builder) {
        this.agentId = builder.agentId;
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
    }

    public String getAgentId() {
        return agentId;
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

    public static AgentModelStatisticsBuilder builder() { return new AgentModelStatisticsBuilder(); }
}
