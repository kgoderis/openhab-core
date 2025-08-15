package org.openhab.core.ai.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.agent.api.AgentModelStatistics;

/**
 * Comprehensive system-wide statistics combining agent and tracking data.
 *
 * Aggregates metrics from all registered agents and optional tracking data
 * to provide a consolidated view of system utilization and performance.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class SystemAggregatedStatistics {
    private final List<AgentModelStatistics> agentStatistics;
    private final long totalAgentRequests;
    private final long totalAgentSuccessfulRequests;
    private final long totalAgentFailedRequests;
    private final long totalAgentResponseTime;
    private final long totalAgentTokens;
    private final double totalAgentCost;
    private final @Nullable SystemUsageStats trackingStats;
    private final int registeredAgentCount;
    private final Instant timestamp;

    public SystemAggregatedStatistics(List<AgentModelStatistics> agentStatistics, long totalAgentRequests,
            long totalAgentSuccessfulRequests, long totalAgentFailedRequests, long totalAgentResponseTime,
            long totalAgentTokens, double totalAgentCost, @Nullable SystemUsageStats trackingStats,
            int registeredAgentCount, Instant timestamp) {
        this.agentStatistics = agentStatistics;
        this.totalAgentRequests = totalAgentRequests;
        this.totalAgentSuccessfulRequests = totalAgentSuccessfulRequests;
        this.totalAgentFailedRequests = totalAgentFailedRequests;
        this.totalAgentResponseTime = totalAgentResponseTime;
        this.totalAgentTokens = totalAgentTokens;
        this.totalAgentCost = totalAgentCost;
        this.trackingStats = trackingStats;
        this.registeredAgentCount = registeredAgentCount;
        this.timestamp = timestamp;
    }

    public List<AgentModelStatistics> getAgentStatistics() {
        return new ArrayList<>(agentStatistics);
    }

    public long getTotalAgentRequests() {
        return totalAgentRequests;
    }

    public long getTotalAgentSuccessfulRequests() {
        return totalAgentSuccessfulRequests;
    }

    public long getTotalAgentFailedRequests() {
        return totalAgentFailedRequests;
    }

    public long getTotalAgentResponseTime() {
        return totalAgentResponseTime;
    }

    public long getTotalAgentTokens() {
        return totalAgentTokens;
    }

    public double getTotalAgentCost() {
        return totalAgentCost;
    }

    public @Nullable SystemUsageStats getTrackingStats() {
        return trackingStats;
    }

    public int getRegisteredAgentCount() {
        return registeredAgentCount;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public double getAgentSuccessRate() {
        return totalAgentRequests > 0 ? (double) totalAgentSuccessfulRequests / totalAgentRequests : 0.0;
    }

    public double getAgentAverageResponseTime() {
        return totalAgentRequests > 0 ? (double) totalAgentResponseTime / totalAgentRequests : 0.0;
    }

    public double getTotalSystemCost() {
        double trackingCost = trackingStats != null ? trackingStats.getTotalCost() : 0.0;
        return totalAgentCost + trackingCost;
    }

    public long getTotalSystemRequests() {
        long trackingRequests = trackingStats != null ? trackingStats.getTotalRequests() : 0;
        return totalAgentRequests + trackingRequests;
    }
}
