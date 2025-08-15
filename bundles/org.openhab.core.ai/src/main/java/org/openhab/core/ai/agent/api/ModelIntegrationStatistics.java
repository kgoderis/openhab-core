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

    ModelIntegrationStatistics(ModelIntegrationStatisticsBuilder builder) {
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

    public static ModelIntegrationStatisticsBuilder builder() {
        return new ModelIntegrationStatisticsBuilder();
    }
}
