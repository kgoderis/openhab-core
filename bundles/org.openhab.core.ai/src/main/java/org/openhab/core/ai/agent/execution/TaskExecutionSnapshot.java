package org.openhab.core.ai.agent.execution;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.service.statistics.StatisticsSnapshot;

/**
 * Immutable snapshot of task execution metrics data.
 * 
 * <p>
 * This class provides a point-in-time view of task execution performance
 * including basic counts, latency metrics, and execution-specific metrics.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record TaskExecutionSnapshot(long total, long success, long failure, long totalDurationNanos, 
        String taskId, long retryCount, long averageExecutionTime, long timestampMs) 
        implements StatisticsSnapshot, CountsMetrics, LatencyMetrics {

    /**
     * Factory method to create a snapshot from metrics data.
     * 
     * @param total total task executions
     * @param success successful executions
     * @param failure failed executions
     * @param totalDurationNanos total duration in nanoseconds
     * @param taskId task identifier
     * @param retryCount number of retries
     * @param averageExecutionTime average execution time in milliseconds
     * @return new TaskExecutionSnapshot instance
     */
    public static TaskExecutionSnapshot of(long total, long success, long failure, long totalDurationNanos,
            String taskId, long retryCount, long averageExecutionTime) {
        return new TaskExecutionSnapshot(total, success, failure, totalDurationNanos, taskId, retryCount,
                averageExecutionTime, System.currentTimeMillis());
    }

    /**
     * Factory method to create an empty snapshot.
     * 
     * @return empty TaskExecutionSnapshot instance
     */
    public static TaskExecutionSnapshot empty() {
        return new TaskExecutionSnapshot(0, 0, 0, 0, "unknown", 0, 0, System.currentTimeMillis());
    }

    /**
     * Get the retry rate as a percentage.
     * 
     * @return retry rate percentage
     */
    public double retryRate() {
        return total > 0 ? (double) retryCount / total * 100.0 : 0.0;
    }

    /**
     * Get the execution efficiency score (0-100).
     * 
     * @return efficiency score
     */
    public double executionEfficiency() {
        double successRate = successRate();
        double retryPenalty = Math.max(0.0, 100.0 - retryRate());
        return (successRate + retryPenalty) / 2.0;
    }

    /**
     * Create a summary string with key metrics.
     * 
     * @return summary string
     */
    public String toSummary() {
        return String.format(
                "TaskExecutionSnapshot{taskId=%s, total=%d, success=%d, failure=%d, avgMs=%.2f, retryRate=%.1f%%, efficiency=%.1f%%}",
                taskId, total, success, failure, LatencyMetrics.super.averageMs(total), retryRate(), executionEfficiency());
    }
}

