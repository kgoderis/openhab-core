package org.openhab.core.ai.agent.monitoring;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.agent.core.AgentState;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.base.AbstractStatistics;

/**
 * Consolidated agent statistics that extends the unified monitoring framework.
 * 
 * <p>
 * This class provides comprehensive statistics for agent operations including
 * execution counts, success rates, and performance metrics. It implements
 * the CountsMetrics and LatencyMetrics capability interfaces.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class AgentStatistics extends AbstractStatistics implements CountsMetrics, LatencyMetrics {

    private final String agentId;
    private final AgentState agentState;
    private final long totalDurationNanos;

    /**
     * Create a new AgentStatistics instance.
     * 
     * @param id unique identifier for this statistics instance
     * @param timestamp timestamp when statistics were collected
     * @param agentId the agent identifier
     * @param agentState the current agent state
     * @param totalCount total number of agent operations
     * @param successCount number of successful operations
     * @param failureCount number of failed operations
     * @param totalDurationNanos total execution duration in nanoseconds
     * @param collectionStartTime when collection started
     * @param collectionEndTime when collection ended
     * @param additionalMeasures additional statistical measures
     * @param data additional monitoring data
     */
    public AgentStatistics(String id, Instant timestamp, String agentId, AgentState agentState, long totalCount,
            long successCount, long failureCount, long totalDurationNanos, @Nullable Instant collectionStartTime,
            @Nullable Instant collectionEndTime, @Nullable Map<String, Double> additionalMeasures,
            @Nullable Map<String, Object> data) {
        super(id, timestamp, "agent", "agent-statistics", "Agent execution statistics", data, totalCount, successCount,
                failureCount, collectionStartTime, collectionEndTime, additionalMeasures);
        this.agentId = agentId;
        this.agentState = agentState;
        this.totalDurationNanos = totalDurationNanos;
    }

    /**
     * Create a new AgentStatistics instance with current timestamp.
     * 
     * @param id unique identifier for this statistics instance
     * @param agentId the agent identifier
     * @param agentState the current agent state
     * @param totalCount total number of agent operations
     * @param successCount number of successful operations
     * @param failureCount number of failed operations
     * @param totalDurationNanos total execution duration in nanoseconds
     */
    public AgentStatistics(String id, String agentId, AgentState agentState, long totalCount, long successCount,
            long failureCount, long totalDurationNanos) {
        this(id, Instant.now(), agentId, agentState, totalCount, successCount, failureCount, totalDurationNanos, null,
                null, null, null);
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
     * Get the current agent state.
     * 
     * @return the agent state
     */
    public AgentState getAgentState() {
        return agentState;
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

    // LatencyMetrics interface implementation
    @Override
    public long totalDurationNanos() {
        return totalDurationNanos;
    }

    /**
     * Get the average execution time from additional measures.
     * 
     * @return average execution time in milliseconds, or 0.0 if not available
     */
    public double getAverageExecutionTime() {
        Map<String, Double> measures = getAdditionalMeasures();
        return measures != null ? measures.getOrDefault("averageExecutionTime", 0.0) : 0.0;
    }

    /**
     * Get the agent performance score.
     * 
     * @return performance score between 0.0 and 1.0
     */
    public double getPerformanceScore() {
        double successRate = successRate();
        double avgExecutionTime = getAverageExecutionTime();

        // Simple performance score calculation: success rate weighted more heavily
        // than execution time performance
        double executionTimeScore = avgExecutionTime < 1000 ? 1.0
                : avgExecutionTime < 5000 ? 0.8 : avgExecutionTime < 10000 ? 0.6 : 0.4;

        return (successRate * 0.7) + (executionTimeScore * 0.3);
    }

    /**
     * Check if the agent is performing well (success rate > 80%).
     * 
     * @return true if the agent is performing well
     */
    public boolean isPerformingWell() {
        return successRate() > 0.8;
    }

    /**
     * Check if the agent needs attention (success rate < 50%).
     * 
     * @return true if the agent needs attention
     */
    public boolean needsAttention() {
        return successRate() < 0.5;
    }
}
