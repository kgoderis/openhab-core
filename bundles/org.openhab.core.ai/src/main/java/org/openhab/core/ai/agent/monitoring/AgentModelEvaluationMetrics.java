package org.openhab.core.ai.agent.monitoring;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.agent.model.AgentModelEvaluationStatus;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.base.AbstractMetrics;

/**
 * Metrics for agent model evaluations.
 * 
 * <p>
 * This class extends AbstractMetrics to provide comprehensive metrics about model evaluations,
 * including status counts, average scores, and evaluation timing. It implements
 * CountsMetrics and LatencyMetrics capability interfaces.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public class AgentModelEvaluationMetrics extends AbstractMetrics implements CountsMetrics, LatencyMetrics {

    private final int totalEvaluations;
    private final Map<AgentModelEvaluationStatus, Long> statusCounts;
    private final double averageScore;
    private final @Nullable Instant lastEvaluationTime;

    /**
     * Create a new AgentModelEvaluationMetrics instance.
     * 
     * @param id unique identifier for this metrics instance
     * @param timestamp timestamp when metrics were collected
     * @param totalEvaluations total number of evaluations performed
     * @param statusCounts counts by evaluation status
     * @param averageScore average evaluation score
     * @param lastEvaluationTime timestamp of last evaluation
     * @param totalOperations total number of operations
     * @param successfulOperations number of successful operations
     * @param failedOperations number of failed operations
     * @param totalProcessingTime total processing time in nanoseconds
     * @param averageResponseTime average response time in milliseconds
     * @param lastOperationTime timestamp of last operation
     * @param data additional monitoring data
     */
    public AgentModelEvaluationMetrics(String id, Instant timestamp, int totalEvaluations,
            Map<AgentModelEvaluationStatus, Long> statusCounts, double averageScore,
            @Nullable Instant lastEvaluationTime, long totalOperations, long successfulOperations,
            long failedOperations, long totalProcessingTime, double averageResponseTime,
            @Nullable Instant lastOperationTime, @Nullable Map<String, Object> data) {
        super(id, timestamp, "agent-model-evaluation", "evaluation-metrics", "Agent model evaluation metrics", data,
                totalOperations, successfulOperations, failedOperations, totalProcessingTime, averageResponseTime,
                lastOperationTime);
        this.totalEvaluations = totalEvaluations;
        this.statusCounts = Map.copyOf(statusCounts);
        this.averageScore = averageScore;
        this.lastEvaluationTime = lastEvaluationTime;
    }

    /**
     * Create a new AgentModelEvaluationMetrics instance with current timestamp.
     * 
     * @param id unique identifier for this metrics instance
     * @param totalEvaluations total number of evaluations performed
     * @param statusCounts counts by evaluation status
     * @param averageScore average evaluation score
     * @param totalOperations total number of operations
     * @param successfulOperations number of successful operations
     * @param failedOperations number of failed operations
     * @param totalProcessingTime total processing time in nanoseconds
     * @param averageResponseTime average response time in milliseconds
     */
    public AgentModelEvaluationMetrics(String id, int totalEvaluations,
            Map<AgentModelEvaluationStatus, Long> statusCounts, double averageScore, long totalOperations,
            long successfulOperations, long failedOperations, long totalProcessingTime, double averageResponseTime) {
        this(id, Instant.now(), totalEvaluations, statusCounts, averageScore, Instant.now(), totalOperations,
                successfulOperations, failedOperations, totalProcessingTime, averageResponseTime, null, null);
    }

    public int getTotalEvaluations() {
        return totalEvaluations;
    }

    public Map<AgentModelEvaluationStatus, Long> getStatusCounts() {
        return statusCounts;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public @Nullable Instant getLastEvaluationTime() {
        return lastEvaluationTime;
    }

    public long getStatusCount(AgentModelEvaluationStatus status) {
        return statusCounts.getOrDefault(status, 0L);
    }

    public long getExcellentCount() {
        return getStatusCount(AgentModelEvaluationStatus.EXCELLENT);
    }

    public long getGoodCount() {
        return getStatusCount(AgentModelEvaluationStatus.GOOD);
    }

    public long getFairCount() {
        return getStatusCount(AgentModelEvaluationStatus.FAIR);
    }

    public long getPoorCount() {
        return getStatusCount(AgentModelEvaluationStatus.POOR);
    }

    public long getErrorCount() {
        return getStatusCount(AgentModelEvaluationStatus.ERROR) + getStatusCount(AgentModelEvaluationStatus.NOT_FOUND);
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

    public double getSuccessRate() {
        if (totalEvaluations == 0) {
            return 0.0;
        }
        long successfulEvaluations = getExcellentCount() + getGoodCount() + getFairCount() + getPoorCount();
        return (double) successfulEvaluations / totalEvaluations;
    }

    public boolean hasEvaluations() {
        return totalEvaluations > 0;
    }

    /**
     * Get the evaluation efficiency score.
     * 
     * @return efficiency score between 0.0 and 1.0
     */
    public double getEvaluationEfficiency() {
        double successRate = getSuccessRate();
        double scoreQuality = averageScore;
        double responseTimeScore = getAverageResponseTime() < 1000 ? 1.0
                : getAverageResponseTime() < 5000 ? 0.8 : getAverageResponseTime() < 10000 ? 0.6 : 0.4;

        return (successRate * 0.4) + (scoreQuality * 0.4) + (responseTimeScore * 0.2);
    }
}
