package org.openhab.core.ai.agent.infrastructure.synchronization;

import java.time.Instant;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Transaction result for agent synchronization operations.
 *
 * <p>Extracted from AgentSynchronizationService.</p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class SynchronizationTransactionResult {
    private final String transactionId;
    private final boolean success;
    private final String status;
    private final String message;
    private final List<String> completedTasks;
    private final List<String> failedTasks;
    private final Instant startTime;
    private final Instant endTime;
    private final long durationMs;

    public SynchronizationTransactionResult(
            String transactionId,
            boolean success,
            String status,
            String message,
            List<String> completedTasks,
            List<String> failedTasks,
            Instant startTime,
            Instant endTime,
            long durationMs) {
        this.transactionId = transactionId;
        this.success = success;
        this.status = status;
        this.message = message;
        this.completedTasks = completedTasks;
        this.failedTasks = failedTasks;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationMs = durationMs;
    }

    public String getTransactionId() { return transactionId; }
    public boolean isSuccess() { return success; }
    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public List<String> getCompletedTasks() { return completedTasks; }
    public List<String> getFailedTasks() { return failedTasks; }
    public Instant getStartTime() { return startTime; }
    public Instant getEndTime() { return endTime; }
    public long getDurationMs() { return durationMs; }
}


