package org.openhab.core.ai.agent.api;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class AgentModelStatisticsBuilder {
    String agentId = "";
    long totalRequests = 0;
    long successfulRequests = 0;
    long failedRequests = 0;
    long cacheHits = 0;
    long cacheMisses = 0;
    long totalResponseTimeMs = 0;
    long averageResponseTimeMs = 0;
    long minResponseTimeMs = Long.MAX_VALUE;
    long maxResponseTimeMs = 0;
    long totalTokensUsed = 0;
    long totalCost = 0;
    Instant lastRequestTime = Instant.now();
    Instant lastSuccessTime = Instant.now();
    Instant lastFailureTime = Instant.now();
    String lastError = "";

    public AgentModelStatisticsBuilder agentId(String agentId) { this.agentId = agentId; return this; }
    public AgentModelStatisticsBuilder totalRequests(long totalRequests) { this.totalRequests = totalRequests; return this; }
    public AgentModelStatisticsBuilder successfulRequests(long successfulRequests) { this.successfulRequests = successfulRequests; return this; }
    public AgentModelStatisticsBuilder failedRequests(long failedRequests) { this.failedRequests = failedRequests; return this; }
    public AgentModelStatisticsBuilder cacheHits(long cacheHits) { this.cacheHits = cacheHits; return this; }
    public AgentModelStatisticsBuilder cacheMisses(long cacheMisses) { this.cacheMisses = cacheMisses; return this; }
    public AgentModelStatisticsBuilder totalResponseTimeMs(long totalResponseTimeMs) { this.totalResponseTimeMs = totalResponseTimeMs; return this; }
    public AgentModelStatisticsBuilder averageResponseTimeMs(long averageResponseTimeMs) { this.averageResponseTimeMs = averageResponseTimeMs; return this; }
    public AgentModelStatisticsBuilder minResponseTimeMs(long minResponseTimeMs) { this.minResponseTimeMs = minResponseTimeMs; return this; }
    public AgentModelStatisticsBuilder maxResponseTimeMs(long maxResponseTimeMs) { this.maxResponseTimeMs = maxResponseTimeMs; return this; }
    public AgentModelStatisticsBuilder totalTokensUsed(long totalTokensUsed) { this.totalTokensUsed = totalTokensUsed; return this; }
    public AgentModelStatisticsBuilder totalCost(long totalCost) { this.totalCost = totalCost; return this; }
    public AgentModelStatisticsBuilder lastRequestTime(Instant lastRequestTime) { this.lastRequestTime = lastRequestTime; return this; }
    public AgentModelStatisticsBuilder lastSuccessTime(Instant lastSuccessTime) { this.lastSuccessTime = lastSuccessTime; return this; }
    public AgentModelStatisticsBuilder lastFailureTime(Instant lastFailureTime) { this.lastFailureTime = lastFailureTime; return this; }
    public AgentModelStatisticsBuilder lastError(String lastError) { this.lastError = lastError; return this; }

    public AgentModelStatistics build() { return new AgentModelStatistics(this); }
}


