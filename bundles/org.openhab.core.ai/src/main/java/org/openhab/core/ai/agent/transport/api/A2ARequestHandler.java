package org.openhab.core.ai.agent.transport.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A2A request handler interface for processing Agent-to-Agent protocol requests.
 *
 * <p>Implementations process messages and manage task-related operations.
 * </p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface A2ARequestHandler {
    /**
     * Process an A2A message.
     *
     * @param message the message to process
     * @return processing result
     */
    String processMessage(String message);

    /**
     * Get the status of a task.
     *
     * @param taskId the task ID
     * @return task status
     */
    String getTaskStatus(String taskId);

    /**
     * Cancel a task.
     *
     * @param taskId the task ID
     * @return true if cancellation was successful
     */
    boolean cancelTask(String taskId);
}


