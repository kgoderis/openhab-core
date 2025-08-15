package org.openhab.core.ai.reasoning.enums;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class Task {
    private final String taskId;
    private final TaskType taskType;
    private final String description;
    private final Map<String, Object> parameters;
    private final Priority priority;

    public Task(String taskId, TaskType taskType, String description, Map<String, Object> parameters,
            Priority priority) {
        this.taskId = taskId;
        this.taskType = taskType;
        this.description = description;
        this.parameters = new ConcurrentHashMap<>(parameters);
        this.priority = priority;
    }

    public String getTaskId() {
        return taskId;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, Object> getParameters() {
        return new ConcurrentHashMap<>(parameters);
    }

    public Priority getPriority() {
        return priority;
    }
}
