package org.openhab.core.ai.agent.monitoring;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.base.AbstractMetrics;

/**
 * Consolidated collaboration performance metrics that extends the unified monitoring framework.
 * 
 * <p>
 * This class provides comprehensive performance metrics for agent collaboration operations including
 * coordination counts, success rates, and latency metrics. It implements both
 * CountsMetrics and LatencyMetrics capability interfaces.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class CollaborationPerformanceMetrics extends AbstractMetrics implements CountsMetrics, LatencyMetrics {

    private final String agentId;

    /**
     * Create a new CollaborationPerformanceMetrics instance.
     * 
     * @param id unique identifier for this metrics instance
     * @param timestamp timestamp when metrics were collected
     * @param agentId the agent identifier
     * @param totalOperations total number of collaboration operations
     * @param successfulOperations number of successful operations
     * @param failedOperations number of failed operations
     * @param totalProcessingTime total processing time in nanoseconds
     * @param averageResponseTime average response time in milliseconds
     * @param lastOperationTime timestamp of last operation
     * @param data additional monitoring data
     */
    public CollaborationPerformanceMetrics(String id, Instant timestamp, String agentId, long totalOperations,
            long successfulOperations, long failedOperations, long totalProcessingTime, double averageResponseTime,
            @Nullable Instant lastOperationTime, @Nullable Map<String, Object> data) {
        super(id, timestamp, "agent", "collaboration-performance", "Collaboration performance metrics", data,
                totalOperations, successfulOperations, failedOperations, totalProcessingTime, averageResponseTime,
                lastOperationTime);
        this.agentId = agentId;
    }

    /**
     * Create a new CollaborationPerformanceMetrics instance with current timestamp.
     * 
     * @param id unique identifier for this metrics instance
     * @param agentId the agent identifier
     * @param totalOperations total number of collaboration operations
     * @param successfulOperations number of successful operations
     * @param failedOperations number of failed operations
     * @param totalProcessingTime total processing time in nanoseconds
     * @param averageResponseTime average response time in milliseconds
     */
    public CollaborationPerformanceMetrics(String id, String agentId, long totalOperations, long successfulOperations,
            long failedOperations, long totalProcessingTime, double averageResponseTime) {
        this(id, Instant.now(), agentId, totalOperations, successfulOperations, failedOperations, totalProcessingTime,
                averageResponseTime, null, null);
    }

    /**
     * Get the agent identifier.
     * 
     * @return the agent identifier
     */
    public String getAgentId() {
        return agentId;
    }

    // CountsMetrics interface implementation
    @Override
    public long total() {
        return getTotalOperations();
    }

    @Override
    public long success() {
        return getSuccessfulOperations();
    }

    @Override
    public long failure() {
        return getFailedOperations();
    }

    // LatencyMetrics interface implementation
    @Override
    public long totalDurationNanos() {
        return getTotalProcessingTime();
    }

    /**
     * Get the collaboration success rate.
     * 
     * @return collaboration success rate as a percentage
     */
    public double getCollaborationSuccessRate() {
        return successRate() * 100.0;
    }

    /**
     * Get the collaboration efficiency score.
     * 
     * @return collaboration efficiency score between 0.0 and 1.0
     */
    public double getCollaborationEfficiency() {
        double successRate = successRate();
        double latencyScore = getAverageResponseTime() < 2000 ? 1.0
                : getAverageResponseTime() < 5000 ? 0.8 : getAverageResponseTime() < 10000 ? 0.6 : 0.4;

        return (successRate * 0.6) + (latencyScore * 0.4);
    }

    /**
     * Get the coordination effectiveness.
     * 
     * @return coordination effectiveness score between 0.0 and 1.0
     */
    public double getCoordinationEffectiveness() {
        return getCollaborationEfficiency();
    }
}
