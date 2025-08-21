package org.openhab.core.ai.model.monitoring;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.agent.monitoring.AgentStatistics;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.base.AbstractStatistics;
import org.openhab.core.ai.model.SystemUsageStats;

/**
 * Consolidated system aggregated statistics that extends the unified monitoring framework.
 * 
 * <p>
 * This class provides comprehensive system-wide statistics combining agent and tracking data
 * to provide a consolidated view of system utilization and performance. It implements
 * CountsMetrics capability interface.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class SystemAggregatedStatistics extends AbstractStatistics implements CountsMetrics {

    private final List<AgentStatistics> agentStatistics;
    private final long totalAgentTokens;
    private final double totalAgentCost;
    private final @Nullable SystemUsageStats trackingStats;
    private final int registeredAgentCount;
    private final long totalProcessingTime;
    private final double averageResponseTime;

    /**
     * Create a new SystemAggregatedStatistics instance.
     * 
     * @param id unique identifier for this statistics instance
     * @param timestamp timestamp when statistics were collected
     * @param totalOperations total number of system operations
     * @param successfulOperations number of successful operations
     * @param failedOperations number of failed operations
     * @param totalProcessingTime total processing time in nanoseconds
     * @param averageResponseTime average response time in milliseconds
     * @param lastOperationTime timestamp of last operation
     * @param agentStatistics list of agent statistics
     * @param totalAgentTokens total agent tokens used
     * @param totalAgentCost total agent cost
     * @param trackingStats optional tracking statistics
     * @param registeredAgentCount number of registered agents
     * @param data additional monitoring data
     */
    public SystemAggregatedStatistics(String id, Instant timestamp, long totalOperations, long successfulOperations,
            long failedOperations, long totalProcessingTime, double averageResponseTime,
            @Nullable Instant lastOperationTime, List<AgentStatistics> agentStatistics, long totalAgentTokens,
            double totalAgentCost, @Nullable SystemUsageStats trackingStats, int registeredAgentCount,
            @Nullable Map<String, Object> data) {
        super(id, timestamp, "model", "system-aggregated-statistics", "System aggregated statistics", data,
                totalOperations, successfulOperations, failedOperations, null, null, null);
        this.agentStatistics = new ArrayList<>(agentStatistics);
        this.totalAgentTokens = totalAgentTokens;
        this.totalAgentCost = totalAgentCost;
        this.trackingStats = trackingStats;
        this.registeredAgentCount = registeredAgentCount;
        this.totalProcessingTime = totalProcessingTime;
        this.averageResponseTime = averageResponseTime;
    }

    /**
     * Create a new SystemAggregatedStatistics instance with current timestamp.
     * 
     * @param id unique identifier for this statistics instance
     * @param totalOperations total number of system operations
     * @param successfulOperations number of successful operations
     * @param failedOperations number of failed operations
     * @param totalProcessingTime total processing time in nanoseconds
     * @param averageResponseTime average response time in milliseconds
     * @param agentStatistics list of agent statistics
     * @param totalAgentTokens total agent tokens used
     * @param totalAgentCost total agent cost
     * @param trackingStats optional tracking statistics
     * @param registeredAgentCount number of registered agents
     */
    public SystemAggregatedStatistics(String id, long totalOperations, long successfulOperations, long failedOperations,
            long totalProcessingTime, double averageResponseTime, List<AgentStatistics> agentStatistics,
            long totalAgentTokens, double totalAgentCost, @Nullable SystemUsageStats trackingStats,
            int registeredAgentCount) {
        this(id, Instant.now(), totalOperations, successfulOperations, failedOperations, totalProcessingTime,
                averageResponseTime, null, agentStatistics, totalAgentTokens, totalAgentCost, trackingStats,
                registeredAgentCount, null);
    }

    /**
     * Get the list of agent statistics.
     * 
     * @return list of agent statistics
     */
    public List<AgentStatistics> getAgentStatistics() {
        return new ArrayList<>(agentStatistics);
    }

    /**
     * Get the total agent tokens used.
     * 
     * @return total agent tokens
     */
    public long getTotalAgentTokens() {
        return totalAgentTokens;
    }

    /**
     * Get the total agent cost.
     * 
     * @return total agent cost
     */
    public double getTotalAgentCost() {
        return totalAgentCost;
    }

    /**
     * Get the optional tracking statistics.
     * 
     * @return tracking statistics or null
     */
    public @Nullable SystemUsageStats getTrackingStats() {
        return trackingStats;
    }

    /**
     * Get the number of registered agents.
     * 
     * @return registered agent count
     */
    public int getRegisteredAgentCount() {
        return registeredAgentCount;
    }

    /**
     * Get the total processing time in nanoseconds.
     * 
     * @return total processing time
     */
    public long getTotalProcessingTime() {
        return totalProcessingTime;
    }

    /**
     * Get the average response time in milliseconds.
     * 
     * @return average response time
     */
    public double getAverageResponseTime() {
        return averageResponseTime;
    }

    // CountsMetrics interface implementation
    @Override
    public long total() {
        return getTotalCount();
    }

    @Override
    public long success() {
        return getSuccessCount();
    }

    @Override
    public long failure() {
        return getFailureCount();
    }

    /**
     * Get the agent success rate.
     * 
     * @return agent success rate as a percentage
     */
    public double getAgentSuccessRate() {
        return total() > 0 ? (double) success() / total() : 0.0;
    }

    /**
     * Get the agent average response time.
     * 
     * @return agent average response time in milliseconds
     */
    public double getAgentAverageResponseTime() {
        return total() > 0 ? (double) getTotalProcessingTime() / total() : 0.0;
    }

    /**
     * Get the total system cost (agent cost + tracking cost).
     * 
     * @return total system cost
     */
    public double getTotalSystemCost() {
        double trackingCost = trackingStats != null ? trackingStats.getTotalCost() : 0.0;
        return totalAgentCost + trackingCost;
    }

    /**
     * Get the total system requests (agent requests + tracking requests).
     * 
     * @return total system requests
     */
    public long getTotalSystemRequests() {
        long trackingRequests = trackingStats != null ? trackingStats.getTotalRequests() : 0;
        return total() + trackingRequests;
    }

    /**
     * Get the system efficiency score.
     * 
     * @return system efficiency score between 0.0 and 1.0
     */
    public double getSystemEfficiency() {
        double successRate = successRate();
        double latencyScore = getAverageResponseTime() < 2000 ? 1.0
                : getAverageResponseTime() < 5000 ? 0.8 : getAverageResponseTime() < 10000 ? 0.6 : 0.4;
        double costEfficiency = totalAgentCost > 0 ? Math.min(1.0, 100.0 / totalAgentCost) : 1.0;
        double agentUtilization = registeredAgentCount > 0
                ? Math.min(1.0, (double) agentStatistics.size() / registeredAgentCount)
                : 0.0;

        return (successRate * 0.4) + (latencyScore * 0.3) + (costEfficiency * 0.2) + (agentUtilization * 0.1);
    }

    /**
     * Check if the system is performing well (high success rate, good efficiency).
     * 
     * @return true if the system is performing well
     */
    public boolean isPerformingWell() {
        return successRate() > 0.85 && getAverageResponseTime() < 5000 && getSystemEfficiency() > 0.7;
    }

    /**
     * Check if there are critical system issues (low success rate or high costs).
     * 
     * @return true if there are critical system issues
     */
    public boolean hasCriticalIssues() {
        return successRate() < 0.7 || getAverageResponseTime() > 15000 || totalAgentCost > 1000.0;
    }

    /**
     * Get the average cost per request.
     * 
     * @return average cost per request
     */
    public double getAverageCostPerRequest() {
        return total() > 0 ? totalAgentCost / total() : 0.0;
    }

    /**
     * Get the average tokens per request.
     * 
     * @return average tokens per request
     */
    public double getAverageTokensPerRequest() {
        return total() > 0 ? (double) totalAgentTokens / total() : 0.0;
    }
}
