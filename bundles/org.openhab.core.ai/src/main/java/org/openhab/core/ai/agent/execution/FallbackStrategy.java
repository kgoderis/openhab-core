package org.openhab.core.ai.agent.execution;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

import io.a2a.spec.Task;
import io.a2a.spec.TaskStatusUpdateEvent;

/**
 * Fallback strategy applied when task execution fails.
 *
 * <p>Supports multiple strategies like retrying with a different agent or simplified execution.</p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class FallbackStrategy {
    private final String taskId;
    private final String strategyType;
    private final AgentTaskManager taskManager;

    public FallbackStrategy(String taskId, String strategyType, AgentTaskManager taskManager) {
        this.taskId = taskId;
        this.strategyType = strategyType;
        this.taskManager = taskManager;
    }

    public TaskStatusUpdateEvent execute(Task task) {
        switch (strategyType) {
            case "RETRY_WITH_DIFFERENT_AGENT":
                return retryWithDifferentAgent(task);
            case "SIMPLIFIED_EXECUTION":
                return simplifiedExecution(task);
            case "ERROR_RESPONSE":
            default:
                return createErrorResponse(task, "Task failed and fallback strategy executed");
        }
    }

    private TaskStatusUpdateEvent retryWithDifferentAgent(Task task) {
        String capability = taskManager.getRequiredCapability(task);
        List<String> agents = taskManager.getAgentsWithCapability(capability);
        if (agents.size() > 1) {
            String alternativeAgentId = agents.get(1);
            taskManager.assignAgentToTask(task.getId(), alternativeAgentId);
            return taskManager.executeTaskWithOrchestration(task);
        } else {
            return createErrorResponse(task, "No alternative agent available for fallback");
        }
    }

    private TaskStatusUpdateEvent simplifiedExecution(Task task) {
        taskManager.getLogger().info("Executing simplified version of task: {}", task.getId());
        return createErrorResponse(task, "Simplified execution completed");
    }

    private TaskStatusUpdateEvent createErrorResponse(Task task, String message) {
        return taskManager.createErrorResponse(task, message);
    }

    public String getTaskId() { return taskId; }
    public String getStrategyType() { return strategyType; }
}


