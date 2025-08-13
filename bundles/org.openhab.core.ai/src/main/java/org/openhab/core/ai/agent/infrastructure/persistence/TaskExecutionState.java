package org.openhab.core.ai.agent.infrastructure.persistence;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.persistence.PersistenceService;

import io.a2a.spec.TaskState;

/**
 * Execution state for an A2A task, extracted from AgentOpenHABPersistenceManager.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class TaskExecutionState {
    private final String taskId;
    private final TaskState state;
    private final long startTime;
    private final String executor;
    private final Map<String, Object> context;

    public TaskExecutionState(String taskId, TaskState state, String executor) {
        this.taskId = taskId;
        this.state = state;
        this.startTime = System.currentTimeMillis();
        this.executor = executor;
        this.context = new HashMap<>();
    }

    public String getTaskId() { return taskId; }
    public TaskState getState() { return state; }
    public long getStartTime() { return startTime; }
    public String getExecutor() { return executor; }
    public Map<String, Object> getContext() { return context; }
    public void addContext(String key, Object value) { context.put(key, value); }

    public void updateState(TaskState newState) {
        // TODO: implement state transition rules if needed
    }
}


