package org.openhab.core.ai.agent.execution;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Task orchestration state tracking.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public class TaskOrchestrationState {
    public enum State { PENDING, RUNNING, PAUSED, COMPLETED, ERROR, CANCELLED }

    private final String taskId;
    private State state;
    private long startTime;
    private long endTime;
    private @Nullable Exception lastError;

    public TaskOrchestrationState(String taskId) {
        this.taskId = taskId;
        this.state = State.PENDING;
    }

    public String getTaskId() { return taskId; }
    public State getState() { return state; }
    public void setState(State state) { this.state = state; }
    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }
    public long getEndTime() { return endTime; }
    public void setEndTime(long endTime) { this.endTime = endTime; }
    public @Nullable Exception getLastError() { return lastError; }
    public void setLastError(Exception lastError) { this.lastError = lastError; }
}


