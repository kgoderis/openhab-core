package org.openhab.core.ai.a2a.api;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import io.a2a.spec.Task;
import io.a2a.spec.TaskStatusUpdateEvent;

/**
 * Interface for A2A agents that can execute tasks and participate in multi-agent coordination.
 * 
 * This interface defines the contract for agents that can be registered with the A2A system
 * and participate in task execution, coordination, and communication with other agents.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public interface A2AAgent {

    /**
     * Get the unique identifier for this agent.
     * 
     * @return the agent ID
     */
    String getAgentId();

    /**
     * Get the display name for this agent.
     * 
     * @return the agent name
     */
    String getAgentName();

    /**
     * Get the version of this agent.
     * 
     * @return the agent version
     */
    String getAgentVersion();

    /**
     * Get the capabilities of this agent.
     * 
     * @return array of capability strings
     */
    String[] getCapabilities();

    /**
     * Check if this agent supports a specific capability.
     * 
     * @param capability the capability to check
     * @return true if the agent supports the capability
     */
    boolean hasCapability(String capability);

    /**
     * Execute a task synchronously - agent waits for completion.
     * 
     * @param task the task to execute
     * @return CompletableFuture containing the task execution result
     */
    CompletableFuture<TaskStatusUpdateEvent> executeTaskSync(Task task);

    /**
     * Execute a task asynchronously - agent returns immediately.
     * 
     * @param task the task to execute
     * @return CompletableFuture containing the task execution result
     */
    CompletableFuture<TaskStatusUpdateEvent> executeTaskAsync(Task task);

    /**
     * Execute a task with streaming progress updates.
     * 
     * @param task the task to execute
     * @param progressCallback callback for progress updates
     * @return CompletableFuture containing the final task execution result
     */
    CompletableFuture<TaskStatusUpdateEvent> executeTaskStream(Task task,
            @Nullable Consumer<TaskStatusUpdateEvent> progressCallback);

    /**
     * Cancel a running task.
     * 
     * @param taskId the ID of the task to cancel
     * @return CompletableFuture indicating cancellation success
     */
    CompletableFuture<Boolean> cancelTask(String taskId);

    /**
     * Get the current status of this agent.
     * 
     * @return the agent status
     */
    AgentStatus getStatus();

    /**
     * Start this agent.
     * 
     * @return CompletableFuture indicating start success
     */
    CompletableFuture<Boolean> start();

    /**
     * Stop this agent.
     * 
     * @return CompletableFuture indicating stop success
     */
    CompletableFuture<Boolean> stop();

    /**
     * Get performance metrics for this agent.
     * 
     * @return agent metrics
     */
    AgentMetrics getMetrics();

    /**
     * Check if this agent is healthy and ready to execute tasks.
     * 
     * @return true if the agent is healthy
     */
    boolean isHealthy();

    /**
     * Get the maximum number of concurrent tasks this agent can handle.
     * 
     * @return maximum concurrent tasks
     */
    int getMaxConcurrentTasks();

    /**
     * Get the current number of active tasks being executed by this agent.
     * 
     * @return current active task count
     */
    int getActiveTaskCount();

    /**
     * Agent status enumeration.
     */
    enum AgentStatus {
        RUNNING,
        STOPPED,
        UNKNOWN
    }

    /**
     * Agent performance metrics.
     */
    interface AgentMetrics {
        /**
         * Get the list of execution times for completed tasks.
         * 
         * @return array of execution times in milliseconds
         */
        long[] getExecutionTimes();

        /**
         * Get the total number of successful task executions.
         * 
         * @return success count
         */
        long getSuccessCount();

        /**
         * Get the total number of failed task executions.
         * 
         * @return failure count
         */
        long getFailureCount();

        /**
         * Get the average execution time in milliseconds.
         * 
         * @return average execution time
         */
        double getAverageExecutionTime();

        /**
         * Get the total number of task executions.
         * 
         * @return total executions
         */
        long getTotalExecutions();
    }
}
