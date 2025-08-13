package org.openhab.core.ai.agent.api;

import java.time.Instant;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class ModelIntegrationStatisticsBuilder {
    long totalAgents = 0;
    long activeAgents = 0;
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
    List<String> registeredAgentIds = List.of();

    public ModelIntegrationStatisticsBuilder totalAgents(long totalAgents) { this.totalAgents = totalAgents; return this; }
    public ModelIntegrationStatisticsBuilder activeAgents(long activeAgents) { this.activeAgents = activeAgents; return this; }
    public ModelIntegrationStatisticsBuilder totalRequests(long totalRequests) { this.totalRequests = totalRequests; return this; }
    public ModelIntegrationStatisticsBuilder successfulRequests(long successfulRequests) { this.successfulRequests = successfulRequests; return this; }
    public ModelIntegrationStatisticsBuilder failedRequests(long failedRequests) { this.failedRequests = failedRequests; return this; }
    public ModelIntegrationStatisticsBuilder cacheHits(long cacheHits) { this.cacheHits = cacheHits; return this; }
    public ModelIntegrationStatisticsBuilder cacheMisses(long cacheMisses) { this.cacheMisses = cacheMisses; return this; }
    public ModelIntegrationStatisticsBuilder totalResponseTimeMs(long totalResponseTimeMs) { this.totalResponseTimeMs = totalResponseTimeMs; return this; }
    public ModelIntegrationStatisticsBuilder averageResponseTimeMs(long averageResponseTimeMs) { this.averageResponseTimeMs = averageResponseTimeMs; return this; }
    public ModelIntegrationStatisticsBuilder minResponseTimeMs(long minResponseTimeMs) { this.minResponseTimeMs = minResponseTimeMs; return this; }
    public ModelIntegrationStatisticsBuilder maxResponseTimeMs(long maxResponseTimeMs) { this.maxResponseTimeMs = maxResponseTimeMs; return this; }
    public ModelIntegrationStatisticsBuilder totalTokensUsed(long totalTokensUsed) { this.totalTokensUsed = totalTokensUsed; return this; }
    public ModelIntegrationStatisticsBuilder totalCost(long totalCost) { this.totalCost = totalCost; return this; }
    public ModelIntegrationStatisticsBuilder lastRequestTime(Instant lastRequestTime) { this.lastRequestTime = lastRequestTime; return this; }
    public ModelIntegrationStatisticsBuilder lastSuccessTime(Instant lastSuccessTime) { this.lastSuccessTime = lastSuccessTime; return this; }
    public ModelIntegrationStatisticsBuilder lastFailureTime(Instant lastFailureTime) { this.lastFailureTime = lastFailureTime; return this; }
    public ModelIntegrationStatisticsBuilder lastError(String lastError) { this.lastError = lastError; return this; }
    public ModelIntegrationStatisticsBuilder registeredAgentIds(List<String> registeredAgentIds) { this.registeredAgentIds = registeredAgentIds; return this; }

    public ModelIntegrationStatistics build() { return new ModelIntegrationStatistics(this); }
}


