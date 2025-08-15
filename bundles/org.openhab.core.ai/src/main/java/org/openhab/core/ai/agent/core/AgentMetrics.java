package org.openhab.core.ai.agent.core;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.api.ActionMetric;

/**
 * Metrics for autonomous agent performance and behavior
 * 
 * <p>
 * This class provides comprehensive metrics for autonomous agents:
 * - Action execution statistics (success/failure rates)
 * - Performance timing and processing metrics
 * - Historical metrics tracking
 * - Real-time performance monitoring
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class AgentMetrics {

    private final String agentId;
    private final AgentState state;
    private final long totalActionsExecuted;
    private final long totalActionsSucceeded;
    private final long totalActionsFailed;
    private final long totalProcessingTime;
    private final List<ActionMetric> recentMetrics;
    private final Instant timestamp;

    /**
     * Create agent metrics
     * 
     * @param agentId the agent identifier
     * @param state the current agent state
     * @param totalActionsExecuted the total number of actions executed
     * @param totalActionsSucceeded the total number of successful actions
     * @param totalActionsFailed the total number of failed actions
     * @param totalProcessingTime the total processing time in milliseconds
     * @param recentMetrics the list of recent metrics
     * @param timestamp the timestamp when metrics were collected
     */
    public AgentMetrics(String agentId, AgentState state, long totalActionsExecuted, long totalActionsSucceeded,
            long totalActionsFailed, long totalProcessingTime, List<ActionMetric> recentMetrics, Instant timestamp) {
        this.agentId = agentId;
        this.state = state;
        this.totalActionsExecuted = totalActionsExecuted;
        this.totalActionsSucceeded = totalActionsSucceeded;
        this.totalActionsFailed = totalActionsFailed;
        this.totalProcessingTime = totalProcessingTime;
        this.recentMetrics = new ArrayList<>(recentMetrics);
        this.timestamp = timestamp;
    }

    /**
     * Get the agent identifier
     * 
     * @return the agent identifier
     */
    public String getAgentId() {
        return agentId;
    }

    /**
     * Get the current agent state
     * 
     * @return the agent state
     */
    public AgentState getState() {
        return state;
    }

    /**
     * Get the total number of actions executed
     * 
     * @return the total actions executed
     */
    public long getTotalActionsExecuted() {
        return totalActionsExecuted;
    }

    /**
     * Get the total number of successful actions
     * 
     * @return the total successful actions
     */
    public long getTotalActionsSucceeded() {
        return totalActionsSucceeded;
    }

    /**
     * Get the total number of failed actions
     * 
     * @return the total failed actions
     */
    public long getTotalActionsFailed() {
        return totalActionsFailed;
    }

    /**
     * Get the total processing time in milliseconds
     * 
     * @return the total processing time
     */
    public long getTotalProcessingTime() {
        return totalProcessingTime;
    }

    /**
     * Get the list of recent metrics
     * 
     * @return the recent metrics
     */
    public List<ActionMetric> getRecentMetrics() {
        return recentMetrics;
    }

    /**
     * Get the timestamp when metrics were collected
     * 
     * @return the timestamp
     */
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Calculate the success rate as a percentage
     * 
     * @return the success rate (0.0 to 1.0)
     */
    public double getSuccessRate() {
        return totalActionsExecuted > 0 ? (double) totalActionsSucceeded / totalActionsExecuted : 0.0;
    }

    /**
     * Calculate the failure rate as a percentage
     * 
     * @return the failure rate (0.0 to 1.0)
     */
    public double getFailureRate() {
        return totalActionsExecuted > 0 ? (double) totalActionsFailed / totalActionsExecuted : 0.0;
    }

    /**
     * Calculate the average processing time per action
     * 
     * @return the average processing time in milliseconds
     */
    public double getAverageProcessingTime() {
        return totalActionsExecuted > 0 ? (double) totalProcessingTime / totalActionsExecuted : 0.0;
    }

    /**
     * Get the average processing time as a Duration
     * 
     * @return the average processing time as Duration
     */
    public Duration getAverageProcessingDuration() {
        return Duration.ofMillis((long) getAverageProcessingTime());
    }

    /**
     * Get the total processing time as a Duration
     * 
     * @return the total processing time as Duration
     */
    public Duration getTotalProcessingDuration() {
        return Duration.ofMillis(totalProcessingTime);
    }

    /**
     * Check if the agent is performing well (success rate > 80%)
     * 
     * @return true if the agent is performing well
     */
    public boolean isPerformingWell() {
        return getSuccessRate() > 0.8;
    }

    /**
     * Check if the agent needs attention (success rate < 50%)
     * 
     * @return true if the agent needs attention
     */
    public boolean needsAttention() {
        return getSuccessRate() < 0.5;
    }

    /**
     * Get metrics for a specific action
     * 
     * @param actionName the action name
     * @return the metrics for the action, or null if not found
     */
    public @Nullable ActionMetric getActionMetrics(String actionName) {
        return recentMetrics.stream().filter(metric -> metric.getActionName().equals(actionName)).findFirst()
                .orElse(null);
    }

    /**
     * Get the number of recent metrics
     * 
     * @return the number of recent metrics
     */
    public int getRecentMetricsCount() {
        return recentMetrics.size();
    }

    /**
     * Check if metrics are recent (within the last hour)
     * 
     * @return true if metrics are recent
     */
    public boolean isRecent() {
        return Duration.between(timestamp, Instant.now()).toHours() < 1;
    }

    @Override
    public String toString() {
        return "AgentMetrics{" + "agentId='" + agentId + '\'' + ", state=" + state + ", totalActionsExecuted="
                + totalActionsExecuted + ", totalActionsSucceeded=" + totalActionsSucceeded + ", totalActionsFailed="
                + totalActionsFailed + ", successRate=" + String.format("%.2f%%", getSuccessRate() * 100)
                + ", averageProcessingTime=" + getAverageProcessingDuration() + ", recentMetricsCount="
                + recentMetrics.size() + ", timestamp=" + timestamp + '}';
    }
}
