package org.openhab.core.ai.agent.infrastructure.synchronization;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Aggregated execution statistics for agent synchronization operations.
 *
 * <p>Extracted from AgentSynchronizationService.</p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class SynchronizationTaskExecutionStats {
    private final long totalTasksExecuted;
    private final long successfulTasks;
    private final long failedTasks;
    private final long totalExecutionTimeMs;
    private final double averageExecutionTimeMs;
    private final Map<String, Long> tasksByType;
    private final Map<String, Long> tasksByStatus;
    private final Instant lastExecutionTime;

    public SynchronizationTaskExecutionStats(
            long totalTasksExecuted,
            long successfulTasks,
            long failedTasks,
            long totalExecutionTimeMs,
            double averageExecutionTimeMs,
            Map<String, Long> tasksByType,
            Map<String, Long> tasksByStatus,
            Instant lastExecutionTime) {
        this.totalTasksExecuted = totalTasksExecuted;
        this.successfulTasks = successfulTasks;
        this.failedTasks = failedTasks;
        this.totalExecutionTimeMs = totalExecutionTimeMs;
        this.averageExecutionTimeMs = averageExecutionTimeMs;
        this.tasksByType = tasksByType;
        this.tasksByStatus = tasksByStatus;
        this.lastExecutionTime = lastExecutionTime;
    }

    public long getTotalTasksExecuted() { return totalTasksExecuted; }
    public long getSuccessfulTasks() { return successfulTasks; }
    public long getFailedTasks() { return failedTasks; }
    public long getTotalExecutionTimeMs() { return totalExecutionTimeMs; }
    public double getAverageExecutionTimeMs() { return averageExecutionTimeMs; }
    public Map<String, Long> getTasksByType() { return tasksByType; }
    public Map<String, Long> getTasksByStatus() { return tasksByStatus; }
    public Instant getLastExecutionTime() { return lastExecutionTime; }
}


