package org.openhab.core.ai.agent.monitoring;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.base.AbstractMetrics;

/**
 * Performance metrics for agent model operations.
 *
 * <p>
 * This class extends AbstractMetrics to provide comprehensive performance metrics for agent model operations
 * including model calls, reasoning, and decision-making processes. It implements CountsMetrics and LatencyMetrics
 * capability interfaces.
 * </p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class AgentModelPerformanceMetrics extends AbstractMetrics implements CountsMetrics, LatencyMetrics {

    private final String modelId;
    private final String operationType;
    private final double minDuration;
    private final double maxDuration;
    private final Map<String, Long> operationsByType;
    private final Map<String, Long> errorsByType;
    private final @Nullable Instant firstOperation;
    private final @Nullable Instant lastOperation;

    /**
     * Create a new AgentModelPerformanceMetrics instance.
     * 
     * @param id unique identifier for this metrics instance
     * @param timestamp timestamp when metrics were collected
     * @param modelId the model identifier
     * @param operationType the operation type
     * @param minDuration minimum duration in milliseconds
     * @param maxDuration maximum duration in milliseconds
     * @param operationsByType operations count by type
     * @param errorsByType errors count by type
     * @param firstOperation timestamp of first operation
     * @param lastOperation timestamp of last operation
     * @param totalOperations total number of operations
     * @param successfulOperations number of successful operations
     * @param failedOperations number of failed operations
     * @param totalProcessingTime total processing time in nanoseconds
     * @param averageResponseTime average response time in milliseconds
     * @param lastOperationTime timestamp of last operation
     * @param data additional monitoring data
     */
    public AgentModelPerformanceMetrics(String id, Instant timestamp, String modelId, String operationType,
            double minDuration, double maxDuration, Map<String, Long> operationsByType, Map<String, Long> errorsByType,
            @Nullable Instant firstOperation, @Nullable Instant lastOperation, long totalOperations,
            long successfulOperations, long failedOperations, long totalProcessingTime, double averageResponseTime,
            @Nullable Instant lastOperationTime, @Nullable Map<String, Object> data) {
        super(id, timestamp, "agent-model-performance", "performance-metrics", "Agent model performance metrics", data,
                totalOperations, successfulOperations, failedOperations, totalProcessingTime, averageResponseTime,
                lastOperationTime);
        this.modelId = modelId;
        this.operationType = operationType;
        this.minDuration = minDuration;
        this.maxDuration = maxDuration;
        this.operationsByType = Map.copyOf(operationsByType);
        this.errorsByType = Map.copyOf(errorsByType);
        this.firstOperation = firstOperation;
        this.lastOperation = lastOperation;
    }

    /**
     * Create a new AgentModelPerformanceMetrics instance with current timestamp.
     * 
     * @param id unique identifier for this metrics instance
     * @param modelId the model identifier
     * @param operationType the operation type
     * @param minDuration minimum duration in milliseconds
     * @param maxDuration maximum duration in milliseconds
     * @param operationsByType operations count by type
     * @param errorsByType errors count by type
     * @param totalOperations total number of operations
     * @param successfulOperations number of successful operations
     * @param failedOperations number of failed operations
     * @param totalProcessingTime total processing time in nanoseconds
     * @param averageResponseTime average response time in milliseconds
     */
    public AgentModelPerformanceMetrics(String id, String modelId, String operationType, double minDuration,
            double maxDuration, Map<String, Long> operationsByType, Map<String, Long> errorsByType,
            long totalOperations, long successfulOperations, long failedOperations, long totalProcessingTime,
            double averageResponseTime) {
        this(id, Instant.now(), modelId, operationType, minDuration, maxDuration, operationsByType, errorsByType,
                Instant.now(), Instant.now(), totalOperations, successfulOperations, failedOperations,
                totalProcessingTime, averageResponseTime, null, null);
    }

    public String getModelId() {
        return modelId;
    }

    public String getOperationType() {
        return operationType;
    }

    public double getMinDuration() {
        return minDuration;
    }

    public double getMaxDuration() {
        return maxDuration;
    }

    public Map<String, Long> getOperationsByType() {
        return operationsByType;
    }

    public Map<String, Long> getErrorsByType() {
        return errorsByType;
    }

    public @Nullable Instant getFirstOperation() {
        return firstOperation;
    }

    public @Nullable Instant getLastOperation() {
        return lastOperation;
    }

    public long getOperationsByType(String type) {
        return operationsByType.getOrDefault(type, 0L);
    }

    public long getErrorsByType(String type) {
        return errorsByType.getOrDefault(type, 0L);
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

    public double getAverageDurationMs() {
        return getAverageResponseTime();
    }

    public double getTotalDurationMs() {
        return getTotalProcessingTime() / 1_000_000.0;
    }

    public double getMinDurationMs() {
        return minDuration;
    }

    public double getMaxDurationMs() {
        return maxDuration;
    }

    public double getSuccessRate() {
        return successRate();
    }

    public double getFailureRate() {
        long total = total();
        return total > 0 ? (double) failure() / total : 0.0;
    }

    /**
     * Get the model performance efficiency score.
     * 
     * @return efficiency score between 0.0 and 1.0
     */
    public double getModelPerformanceEfficiency() {
        double successRate = getSuccessRate();
        double responseTimeScore = getAverageResponseTime() < 1000 ? 1.0
                : getAverageResponseTime() < 5000 ? 0.8 : getAverageResponseTime() < 10000 ? 0.6 : 0.4;
        double consistencyScore = (maxDuration - minDuration) < 1000 ? 1.0
                : (maxDuration - minDuration) < 5000 ? 0.8 : (maxDuration - minDuration) < 10000 ? 0.6 : 0.4;

        return (successRate * 0.5) + (responseTimeScore * 0.3) + (consistencyScore * 0.2);
    }
}
