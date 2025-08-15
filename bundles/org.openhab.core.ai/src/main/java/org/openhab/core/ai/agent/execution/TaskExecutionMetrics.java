package org.openhab.core.ai.agent.execution;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Task execution metrics for agent task execution.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public class TaskExecutionMetrics {
    private final String taskId;
    private final long executionCount;
    private final long failureCount;
    private final long retryCount;
    private final long totalExecutionTime;
    private final long averageExecutionTime;

    public TaskExecutionMetrics(String taskId, long executionCount, long failureCount, long retryCount,
            long totalExecutionTime, long averageExecutionTime) {
        this.taskId = taskId;
        this.executionCount = executionCount;
        this.failureCount = failureCount;
        this.retryCount = retryCount;
        this.totalExecutionTime = totalExecutionTime;
        this.averageExecutionTime = averageExecutionTime;
    }

    public String getTaskId() {
        return taskId;
    }

    public long getExecutionCount() {
        return executionCount;
    }

    public long getFailureCount() {
        return failureCount;
    }

    public long getRetryCount() {
        return retryCount;
    }

    public long getTotalExecutionTime() {
        return totalExecutionTime;
    }

    public long getAverageExecutionTime() {
        return averageExecutionTime;
    }

    public double getSuccessRate() {
        return executionCount > 0 ? (double) (executionCount - failureCount) / executionCount : 0.0;
    }
}
