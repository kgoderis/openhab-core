package org.openhab.core.ai.agent.monitoring;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.ThroughputMetrics;
import org.openhab.core.ai.common.monitoring.base.AbstractMetrics;

/**
 * Consolidated communication performance metrics that extends the unified monitoring framework.
 * 
 * <p>
 * This class provides comprehensive performance metrics for agent communication operations including
 * message counts, success rates, latency metrics, and throughput. It implements
 * CountsMetrics, LatencyMetrics, and ThroughputMetrics capability interfaces.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class CommunicationPerformanceMetrics extends AbstractMetrics
        implements CountsMetrics, LatencyMetrics, ThroughputMetrics {

    private final String agentId;
    private final double operationsPerSecond;
    private final double itemsPerSecond;
    private final long peakThroughput;

    /**
     * Create a new CommunicationPerformanceMetrics instance.
     * 
     * @param id unique identifier for this metrics instance
     * @param timestamp timestamp when metrics were collected
     * @param agentId the agent identifier
     * @param totalOperations total number of communication operations
     * @param successfulOperations number of successful operations
     * @param failedOperations number of failed operations
     * @param totalProcessingTime total processing time in nanoseconds
     * @param averageResponseTime average response time in milliseconds
     * @param lastOperationTime timestamp of last operation
     * @param operationsPerSecond current operations per second
     * @param itemsPerSecond current items per second
     * @param peakThroughput peak throughput achieved
     * @param data additional monitoring data
     */
    public CommunicationPerformanceMetrics(String id, Instant timestamp, String agentId, long totalOperations,
            long successfulOperations, long failedOperations, long totalProcessingTime, double averageResponseTime,
            @Nullable Instant lastOperationTime, double operationsPerSecond, double itemsPerSecond, long peakThroughput,
            @Nullable Map<String, Object> data) {
        super(id, timestamp, "agent", "communication-performance", "Communication performance metrics", data,
                totalOperations, successfulOperations, failedOperations, totalProcessingTime, averageResponseTime,
                lastOperationTime);
        this.agentId = agentId;
        this.operationsPerSecond = operationsPerSecond;
        this.itemsPerSecond = itemsPerSecond;
        this.peakThroughput = peakThroughput;
    }

    /**
     * Create a new CommunicationPerformanceMetrics instance with current timestamp.
     * 
     * @param id unique identifier for this metrics instance
     * @param agentId the agent identifier
     * @param totalOperations total number of communication operations
     * @param successfulOperations number of successful operations
     * @param failedOperations number of failed operations
     * @param totalProcessingTime total processing time in nanoseconds
     * @param averageResponseTime average response time in milliseconds
     * @param operationsPerSecond current operations per second
     * @param itemsPerSecond current items per second
     * @param peakThroughput peak throughput achieved
     */
    public CommunicationPerformanceMetrics(String id, String agentId, long totalOperations, long successfulOperations,
            long failedOperations, long totalProcessingTime, double averageResponseTime, double operationsPerSecond,
            double itemsPerSecond, long peakThroughput) {
        this(id, Instant.now(), agentId, totalOperations, successfulOperations, failedOperations, totalProcessingTime,
                averageResponseTime, null, operationsPerSecond, itemsPerSecond, peakThroughput, null);
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

    // ThroughputMetrics interface implementation
    @Override
    public double operationsPerSecond() {
        return operationsPerSecond;
    }

    @Override
    public double itemsPerSecond() {
        return itemsPerSecond;
    }

    @Override
    public long peakThroughput() {
        return peakThroughput;
    }

    /**
     * Get the communication efficiency score.
     * 
     * @return efficiency score between 0.0 and 1.0
     */
    public double getCommunicationEfficiency() {
        double successRate = successRate();
        double throughputScore = operationsPerSecond > 0 ? Math.min(1.0, operationsPerSecond / 100.0) : 0.0;
        double latencyScore = getAverageResponseTime() < 500 ? 1.0
                : getAverageResponseTime() < 1000 ? 0.8 : getAverageResponseTime() < 2000 ? 0.6 : 0.4;

        return (successRate * 0.4) + (throughputScore * 0.3) + (latencyScore * 0.3);
    }

    /**
     * Get the message delivery rate.
     * 
     * @return message delivery rate as a percentage
     */
    public double getMessageDeliveryRate() {
        return successRate() * 100.0;
    }

    /**
     * Get the average message latency.
     * 
     * @return average message latency in milliseconds
     */
    public double getAverageMessageLatency() {
        return getAverageResponseTime();
    }
}
