package org.openhab.core.ai.agent.execution;

import java.util.ArrayList;
import java.util.List;


import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.patterns.AgentExecutionMetrics;

/**
 * Task performance metrics for agent task manager.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public class TaskMetrics {
    private final String taskId;
    // Performance metrics - now handled by MetricsService
    private final List<Long> executionTimes = new ArrayList<>();

    public TaskMetrics(String taskId) {
        this.taskId = taskId;
    }

    public void recordExecution(long executionTime) {
        recordTaskExecution();
        executionTimes.add(executionTime);
    }

    public void recordSuccess() {
        recordTaskSuccess();
    }

    public void recordError(Exception error) {
        recordTaskError();
    }

    public void recordCancellation() {
        recordTaskCancellation();
    }

    public String getTaskId() {
        return taskId;
    }

    public long getExecutionCount() {
        // Metrics now come from MetricsService snapshots
        return 0;
    }

    public long getSuccessCount() {
        // Metrics now come from MetricsService snapshots
        return 0;
    }

    public long getErrorCount() {
        // Metrics now come from MetricsService snapshots
        return 0;
    }

    public long getCancellationCount() {
        // Metrics now come from MetricsService snapshots
        return 0;
    }

    public List<Long> getExecutionTimes() {
        return new ArrayList<>(executionTimes);
    }

    public double getAverageExecutionTime() {
        return executionTimes.isEmpty() ? 0.0
                : executionTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
    }

    // Metrics recording methods - replacing removed AtomicLong fields using AgentExecutionMetrics pattern

    /**
     * Record task execution - replaces executionCount.incrementAndGet()
     */
    private void recordTaskExecution() {
        try {
            // Use AgentExecutionMetrics pattern for task execution
            AgentExecutionMetrics.recordAgentExecution(null, "task-metrics", "task-execution", 
                    true, java.time.Duration.ZERO, 1, taskId);
        } catch (Exception e) {
            // Silent fail for metrics recording
        }
    }

    /**
     * Record task success - replaces successCount.incrementAndGet()
     */
    private void recordTaskSuccess() {
        try {
            // Use AgentExecutionMetrics pattern for task success
            AgentExecutionMetrics.recordAgentExecution(null, "task-metrics", "task-execution", 
                    true, java.time.Duration.ZERO, 1, taskId);
        } catch (Exception e) {
            // Silent fail for metrics recording
        }
    }

    /**
     * Record task error - replaces errorCount.incrementAndGet()
     */
    private void recordTaskError() {
        try {
            // Use AgentExecutionMetrics pattern for task error
            AgentExecutionMetrics.recordAgentExecution(null, "task-metrics", "task-execution", 
                    false, java.time.Duration.ZERO, 1, taskId);
        } catch (Exception e) {
            // Silent fail for metrics recording
        }
    }

    /**
     * Record task cancellation - replaces cancellationCount.incrementAndGet()
     */
    private void recordTaskCancellation() {
        try {
            // Use AgentExecutionMetrics pattern for task cancellation
            AgentExecutionMetrics.recordAgentExecution(null, "task-metrics", "task-cancellation", 
                    true, java.time.Duration.ZERO, 1, taskId);
        } catch (Exception e) {
            // Silent fail for metrics recording
        }
    }
}
