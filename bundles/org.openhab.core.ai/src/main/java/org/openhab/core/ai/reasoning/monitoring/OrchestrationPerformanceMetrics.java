package org.openhab.core.ai.reasoning.monitoring;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.base.AbstractMetrics;

/**
 * Consolidated orchestration performance metrics that extends the unified monitoring framework.
 * 
 * <p>
 * This class provides comprehensive performance metrics for reasoning orchestration operations including
 * session management, execution counts, success rates, and latency metrics. It implements
 * CountsMetrics and LatencyMetrics capability interfaces.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class OrchestrationPerformanceMetrics extends AbstractMetrics implements CountsMetrics, LatencyMetrics {

    private final int activeSessions;
    private final long totalSessions;

    /**
     * Create a new OrchestrationPerformanceMetrics instance.
     * 
     * @param id unique identifier for this metrics instance
     * @param timestamp timestamp when metrics were collected
     * @param totalOperations total number of orchestration operations
     * @param successfulOperations number of successful operations
     * @param failedOperations number of failed operations
     * @param totalProcessingTime total processing time in nanoseconds
     * @param averageResponseTime average response time in milliseconds
     * @param lastOperationTime timestamp of last operation
     * @param activeSessions number of currently active sessions
     * @param totalSessions total number of sessions created
     * @param data additional monitoring data
     */
    public OrchestrationPerformanceMetrics(String id, Instant timestamp, long totalOperations,
            long successfulOperations, long failedOperations, long totalProcessingTime, double averageResponseTime,
            @Nullable Instant lastOperationTime, int activeSessions, long totalSessions,
            @Nullable Map<String, Object> data) {
        super(id, timestamp, "reasoning", "orchestration-performance", "Orchestration performance metrics", data,
                totalOperations, successfulOperations, failedOperations, totalProcessingTime, averageResponseTime,
                lastOperationTime);
        this.activeSessions = activeSessions;
        this.totalSessions = totalSessions;
    }

    /**
     * Create a new OrchestrationPerformanceMetrics instance with current timestamp.
     * 
     * @param id unique identifier for this metrics instance
     * @param totalOperations total number of orchestration operations
     * @param successfulOperations number of successful operations
     * @param failedOperations number of failed operations
     * @param totalProcessingTime total processing time in nanoseconds
     * @param averageResponseTime average response time in milliseconds
     * @param activeSessions number of currently active sessions
     * @param totalSessions total number of sessions created
     */
    public OrchestrationPerformanceMetrics(String id, long totalOperations, long successfulOperations,
            long failedOperations, long totalProcessingTime, double averageResponseTime, int activeSessions,
            long totalSessions) {
        this(id, Instant.now(), totalOperations, successfulOperations, failedOperations, totalProcessingTime,
                averageResponseTime, null, activeSessions, totalSessions, null);
    }

    /**
     * Get the number of currently active sessions.
     * 
     * @return number of active sessions
     */
    public int getActiveSessions() {
        return activeSessions;
    }

    /**
     * Get the total number of sessions created.
     * 
     * @return total number of sessions
     */
    public long getTotalSessions() {
        return totalSessions;
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
     * Get the orchestration efficiency score.
     * 
     * @return efficiency score between 0.0 and 1.0
     */
    public double getOrchestrationEfficiency() {
        double successRate = successRate();
        double latencyScore = getAverageResponseTime() < 1000 ? 1.0
                : getAverageResponseTime() < 3000 ? 0.8 : getAverageResponseTime() < 5000 ? 0.6 : 0.4;
        double sessionUtilization = totalSessions > 0 ? Math.min(1.0, (double) activeSessions / totalSessions) : 0.0;

        return (successRate * 0.5) + (latencyScore * 0.3) + (sessionUtilization * 0.2);
    }

    /**
     * Get the session utilization rate.
     * 
     * @return session utilization rate as a percentage
     */
    public double getSessionUtilizationRate() {
        return totalSessions > 0 ? (double) activeSessions / totalSessions * 100.0 : 0.0;
    }

    /**
     * Check if the orchestration is performing well (success rate > 80% and good session utilization).
     * 
     * @return true if orchestration is performing well
     */
    public boolean isPerformingWell() {
        return successRate() > 0.8 && getSessionUtilizationRate() > 50.0;
    }
}
