package org.openhab.core.ai.agent.execution;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Task performance metrics for agent task manager.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public class TaskMetrics {
    private final String taskId;
    private final AtomicLong executionCount = new AtomicLong(0);
    private final AtomicLong successCount = new AtomicLong(0);
    private final AtomicLong errorCount = new AtomicLong(0);
    private final AtomicLong cancellationCount = new AtomicLong(0);
    private final List<Long> executionTimes = new ArrayList<>();

    public TaskMetrics(String taskId) { this.taskId = taskId; }
    public void recordExecution(long executionTime) { executionCount.incrementAndGet(); executionTimes.add(executionTime); }
    public void recordSuccess() { successCount.incrementAndGet(); }
    public void recordError(Exception error) { errorCount.incrementAndGet(); }
    public void recordCancellation() { cancellationCount.incrementAndGet(); }
    public String getTaskId() { return taskId; }
    public long getExecutionCount() { return executionCount.get(); }
    public long getSuccessCount() { return successCount.get(); }
    public long getErrorCount() { return errorCount.get(); }
    public long getCancellationCount() { return cancellationCount.get(); }
    public List<Long> getExecutionTimes() { return new ArrayList<>(executionTimes); }
    public double getAverageExecutionTime() { return executionTimes.isEmpty() ? 0.0 : executionTimes.stream().mapToLong(Long::longValue).average().orElse(0.0); }
}


