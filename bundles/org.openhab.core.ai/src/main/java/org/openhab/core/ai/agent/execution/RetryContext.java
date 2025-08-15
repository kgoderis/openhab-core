package org.openhab.core.ai.agent.execution;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Retry context tracking attempts and thresholds for a task.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class RetryContext {
    private final String taskId;
    private final int maxRetries;
    private int currentAttempts;

    public RetryContext(String taskId, int maxRetries) {
        this.taskId = taskId;
        this.maxRetries = maxRetries;
        this.currentAttempts = 0;
    }

    public boolean canRetry() {
        return currentAttempts < maxRetries;
    }

    public void incrementAttempt() {
        currentAttempts++;
    }

    public int getCurrentAttempts() {
        return currentAttempts;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public String getTaskId() {
        return taskId;
    }
}
