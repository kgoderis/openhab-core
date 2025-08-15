package org.openhab.core.ai.agent.api;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import io.a2a.spec.Task;
import io.a2a.spec.TaskStatusUpdateEvent;

/**
 * Agent interface for A2A (Agent-to-Agent) communication and task execution.
 * 
 * <p>
 * This interface defines the contract for agents that can:
 * - Execute tasks asynchronously
 * - Handle task status updates
 * - Manage agent lifecycle
 * - Provide agent status and metrics
 * - Support communication protocols
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface Agent {

    // AgentStatus extracted to top-level enum in this package

    /**
     * Backward-compatible nested metrics interface expected by existing usages.
     * Delegates to the top-level {@link org.openhab.core.ai.agent.api.AgentMetrics}.
     */
    interface AgentMetrics extends org.openhab.core.ai.agent.api.AgentMetrics {
    }

    /**
     * Get the unique identifier for this agent
     */
    String getAgentId();

    /**
     * Get the display name for this agent
     */
    String getAgentName();

    /**
     * Get the current status of this agent
     */
    AgentStatus getStatus();

    /**
     * Get the capabilities of this agent
     */
    String[] getCapabilities();

    /**
     * Execute a task asynchronously
     * 
     * @param task the task to execute
     * @return CompletableFuture that completes when the task is finished
     */
    CompletableFuture<Task> executeTask(Task task);

    /**
     * Subscribe to task status updates
     * 
     * @param taskId the ID of the task to monitor
     * @param callback the callback to invoke when status changes
     */
    void subscribeToTaskUpdates(String taskId, Consumer<TaskStatusUpdateEvent> callback);

    /**
     * Unsubscribe from task status updates
     * 
     * @param taskId the ID of the task to stop monitoring
     */
    void unsubscribeFromTaskUpdates(String taskId);

    /**
     * Get agent metrics
     * 
     * @return agent metrics, or null if not available
     */
    @Nullable
    AgentMetrics getMetrics();

    /**
     * Start the agent
     * 
     * @return true if the agent started successfully
     */
    boolean start();

    /**
     * Stop the agent
     * 
     * @return true if the agent stopped successfully
     */
    boolean stop();

    /**
     * Check if the agent is running
     * 
     * @return true if the agent is running
     */
    boolean isRunning();

    /**
     * Get agent configuration
     * 
     * @return agent configuration as a map
     */
    java.util.Map<String, Object> getConfiguration();

    /**
     * Update agent configuration
     * 
     * @param configuration the new configuration
     * @return true if the configuration was updated successfully
     */
    boolean updateConfiguration(java.util.Map<String, Object> configuration);
}
