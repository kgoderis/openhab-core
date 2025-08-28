package org.openhab.core.ai.common.monitoring.service.snapshot;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;

/**
 * Snapshot record for task lifecycle metrics.
 * 
 * <p>
 * This record provides task lifecycle-specific metrics including execution counts,
 * failure rates, retry counts, and timing information. It implements
 * CountsMetrics and LatencyMetrics capability interfaces.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record TaskSnapshot(String taskId, String taskType, String taskState, long totalExecutions,
        long successfulExecutions, long failedExecutions, long retryCount, long totalExecutionTimeNanos,
        long averageExecutionTimeNanos, long minExecutionTimeNanos, long maxExecutionTimeNanos,
        long timestampMs) implements MetricsSnapshot, CountsMetrics, LatencyMetrics {

    // CountsMetrics implementation
    @Override
    public long total() {
        return totalExecutions;
    }

    @Override
    public long success() {
        return successfulExecutions;
    }

    @Override
    public long failure() {
        return failedExecutions;
    }

    // LatencyMetrics implementation
    @Override
    public long totalDurationNanos() {
        return totalExecutionTimeNanos;
    }

    /**
     * Calculate success rate as a percentage.
     * 
     * @return success rate between 0.0 and 1.0
     */
    public double successRate() {
        return totalExecutions > 0 ? (double) successfulExecutions / totalExecutions : 0.0;
    }

    /**
     * Calculate failure rate as a percentage.
     * 
     * @return failure rate between 0.0 and 1.0
     */
    public double failureRate() {
        return totalExecutions > 0 ? (double) failedExecutions / totalExecutions : 0.0;
    }

    /**
     * Calculate retry rate as a percentage.
     * 
     * @return retry rate between 0.0 and 1.0
     */
    public double retryRate() {
        return totalExecutions > 0 ? (double) retryCount / totalExecutions : 0.0;
    }

    /**
     * Get average execution time in milliseconds.
     * 
     * @return average execution time in ms
     */
    public double averageExecutionTimeMs() {
        return averageExecutionTimeNanos / 1_000_000.0;
    }

    /**
     * Get minimum execution time in milliseconds.
     * 
     * @return minimum execution time in ms
     */
    public double minExecutionTimeMs() {
        return minExecutionTimeNanos / 1_000_000.0;
    }

    /**
     * Get maximum execution time in milliseconds.
     * 
     * @return maximum execution time in ms
     */
    public double maxExecutionTimeMs() {
        return maxExecutionTimeNanos / 1_000_000.0;
    }

    /**
     * Check if task is currently active (not in final state).
     * 
     * @return true if task is active
     */
    public boolean isActive() {
        return !"COMPLETED".equals(taskState) && !"FAILED".equals(taskState) && !"CANCELLED".equals(taskState);
    }

    /**
     * Check if task has completed successfully.
     * 
     * @return true if task is completed
     */
    public boolean isCompleted() {
        return "COMPLETED".equals(taskState);
    }

    /**
     * Check if task has failed.
     * 
     * @return true if task has failed
     */
    public boolean isFailed() {
        return "FAILED".equals(taskState);
    }

    /**
     * Check if task performance is considered healthy.
     * 
     * @return true if success rate > 80% and average execution time is reasonable
     */
    public boolean isHealthy() {
        return successRate() > 0.8 && averageExecutionTimeMs() < 5000.0; // 5 seconds threshold
    }

    /**
     * Get task execution efficiency score.
     * 
     * @return efficiency score between 0.0 and 1.0
     */
    public double efficiencyScore() {
        double successWeight = successRate();
        double timeWeight = Math.max(0.0, 1.0 - (averageExecutionTimeMs() / 10000.0)); // 10 second baseline
        double retryWeight = Math.max(0.0, 1.0 - retryRate());

        return (successWeight * 0.5) + (timeWeight * 0.3) + (retryWeight * 0.2);
    }
}
