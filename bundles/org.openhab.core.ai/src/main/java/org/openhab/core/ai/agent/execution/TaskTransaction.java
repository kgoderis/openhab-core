package org.openhab.core.ai.agent.execution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import io.a2a.spec.Task;
import io.a2a.spec.TaskStatusUpdateEvent;

/**
 * Transaction for orchestrating multiple tasks atomically.
 *
 * <p>Allows assigning agents to tasks, committing to start all tasks, or rolling back to cancel and clear assignments.</p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class TaskTransaction {
    private final String transactionId;
    private final AgentTaskManager taskManager;
    private final List<Task> tasks;
    private final Map<String, String> taskAgentAssignments;
    private boolean committed = false;
    private boolean rolledBack = false;

    public TaskTransaction(String transactionId, AgentTaskManager taskManager) {
        this.transactionId = transactionId;
        this.taskManager = taskManager;
        this.tasks = new ArrayList<>();
        this.taskAgentAssignments = new HashMap<>();
    }

    public void addTask(Task task) {
        if (!committed && !rolledBack) {
            tasks.add(task);
        }
    }

    public void assignAgent(String taskId, String agentId) {
        if (!committed && !rolledBack) {
            taskAgentAssignments.put(taskId, agentId);
        }
    }

    public boolean commit() {
        if (committed || rolledBack) {
            return false;
        }
        try {
            for (Task task : tasks) {
                @Nullable String agentId = taskAgentAssignments.get(task.getId());
                if (agentId != null) {
                    taskManager.assignAgentToTask(task.getId(), agentId);
                }
                taskManager.startTask(task.getId());
            }
            committed = true;
            return true;
        } catch (Exception e) {
            taskManager.getLogger().error("Transaction commit failed: {}", transactionId, e);
            rollback();
            return false;
        }
    }

    public void rollback() {
        if (committed || rolledBack) {
            return;
        }
        try {
            for (Task task : tasks) {
                taskManager.cancelTaskOrchestration(task.getId());
            }
            for (String taskId : taskAgentAssignments.keySet()) {
                taskManager.removeAgentAssignment(taskId);
            }
            rolledBack = true;
        } catch (Exception e) {
            taskManager.getLogger().error("Transaction rollback failed: {}", transactionId, e);
        }
    }

    public String getTransactionId() { return transactionId; }
    public boolean isCommitted() { return committed; }
    public boolean isRolledBack() { return rolledBack; }
}


