package org.openhab.core.ai.a2a.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SubmissionPublisher;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.action.AIActionContext;
import org.openhab.core.ai.common.action.AIActionError;
import org.openhab.core.ai.common.action.AIActionRegistry;
import org.openhab.core.ai.common.action.AIActionResult;
import org.openhab.core.ai.common.api.action.AIAction;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.a2a.spec.JSONRPCError;
import io.a2a.spec.MessageSendParams;
import io.a2a.spec.StreamingEventKind;
import io.a2a.spec.Task;
import io.a2a.spec.TaskState;
import io.a2a.spec.TaskStatus;
import io.a2a.spec.TaskStatusUpdateEvent;

/**
 * A2A Streaming Manager - Handles streaming event management and real-time task updates.
 * 
 * <p>
 * This class is responsible for:
 * - Streaming message handling
 * - Real-time task status updates
 * - Event publishing and subscription management
 * - Streaming task execution
 * </p>
 * 
 * @author AI Assistant
 * @since 1.0.0
 */
@Component(service = A2AStreamingManager.class)
public class A2AStreamingManager {

    private static final Logger logger = LoggerFactory.getLogger(A2AStreamingManager.class);

    @Reference
    private @Nullable AIActionRegistry actionRegistry;

    @Reference
    private @Nullable A2ATaskManager taskManager;

    // Streaming event management
    private final Map<String, SubmissionPublisher<StreamingEventKind>> streamingPublishers = new HashMap<>();
    private @Nullable ExecutorService asyncExecutor;

    @Activate
    public void activate() {
        logger.debug("A2A Streaming Manager activated");
        initializeComponents();
    }

    @Deactivate
    public void deactivate() {
        logger.debug("A2A Streaming Manager deactivated");

        // Close all streaming publishers
        streamingPublishers.values().forEach(SubmissionPublisher::close);
        streamingPublishers.clear();

        // Shutdown executor
        if (asyncExecutor != null) {
            asyncExecutor.shutdown();
        }
    }

    private void initializeComponents() {
        logger.debug("Initializing A2A Streaming Manager components");

        // Get executor from task manager
        if (taskManager != null) {
            asyncExecutor = taskManager.getAsyncExecutor();
        }

        logger.debug("A2A Streaming Manager components initialized");
    }

    // ============================================================================
    // Public API Methods
    // ============================================================================

    public java.util.concurrent.Flow.Publisher<StreamingEventKind> handleStreamingMessageSend(MessageSendParams params)
            throws JSONRPCError {
        logger.debug("Processing streaming message send request: {}", params);

        // Create streaming publisher
        String streamId = "stream-" + System.currentTimeMillis();
        SubmissionPublisher<StreamingEventKind> publisher = new SubmissionPublisher<>();
        streamingPublishers.put(streamId, publisher);

        // Create a task for this message
        Task task = createTaskFromMessage(params);
        if (taskManager != null && taskManager.getTaskStore() != null) {
            taskManager.getTaskStore().save(task);
        }

        // Execute the task asynchronously and stream updates
        ExecutorService executor = asyncExecutor;
        if (executor == null) {
            logger.error("Async executor not available for streaming");
            // Send error status and close publisher instead of throwing
            publishStreamingTaskStatus(publisher, task.getId(), TaskState.FAILED,
                    "Internal error: executor not available");
            publisher.close();
            streamingPublishers.remove(streamId);
            // Return the publisher instead of throwing - it will emit the error status
            return publisher;
        }

        CompletableFuture.runAsync(() -> {
            try {
                logger.debug("Executing streaming task: {}", task.getId());

                // Send initial status update
                publishStreamingTaskStatus(publisher, task.getId(), TaskState.WORKING, "Task started");

                // Extract action information from the message
                String actionId = extractActionIdFromMessage(params.message());
                Map<String, Object> parameters = extractParametersFromMessage(params.message());

                // Execute the actual AI action with streaming updates
                AIActionResult result = executeAIActionWithStreaming(actionId, parameters, task.getId(), publisher);

                if (result.isSuccess()) {
                    // Send completion status
                    publishStreamingTaskStatus(publisher, task.getId(), TaskState.COMPLETED,
                            "Task completed successfully");

                    // Update task with result
                    updateTaskWithResult(task.getId(), result);
                } else {
                    // Send error status
                    String errorMessage = result.getMessage() != null ? result.getMessage() : "Task execution failed";
                    publishStreamingTaskStatus(publisher, task.getId(), TaskState.FAILED, errorMessage);
                }

                // Close the publisher
                publisher.close();
                streamingPublishers.remove(streamId);

            } catch (Exception e) {
                logger.error("Error in streaming task execution: {}", task.getId(), e);

                // Send error status
                publishStreamingTaskStatus(publisher, task.getId(), TaskState.FAILED, "Task failed: " + e.getMessage());
                publisher.close();
                streamingPublishers.remove(streamId);
            }
        }, executor);

        // Always return the publisher - it will handle errors by emitting error events
        return publisher;
    }

    public java.util.concurrent.Flow.Publisher<StreamingEventKind> resubscribeToTask(String taskId)
            throws JSONRPCError {
        logger.debug("Resubscribing to task: {}", taskId);

        // Create streaming publisher for resubscription
        SubmissionPublisher<StreamingEventKind> publisher = new SubmissionPublisher<>();
        streamingPublishers.put(taskId, publisher);

        // Check if task exists
        if (taskManager != null && taskManager.getTaskStore() != null) {
            Task task = taskManager.getTaskStore().get(taskId);
            if (task != null) {
                // Send current task status
                publishStreamingTaskStatus(publisher, taskId, task.getStatus().state(), "Task resubscription");
            } else {
                // Task not found, send error status
                publishStreamingTaskStatus(publisher, taskId, TaskState.FAILED, "Task not found: " + taskId);
                logger.warn("Task not found for resubscription: {}", taskId);
            }
        } else {
            // TaskStore not available, send error status
            publishStreamingTaskStatus(publisher, taskId, TaskState.FAILED, "TaskStore not available");
            logger.error("TaskStore not available for resubscription");
        }

        // Always return the publisher - it will handle errors by emitting error events
        return publisher;
    }

    // ============================================================================
    // Private Helper Methods
    // ============================================================================

    private Task createTaskFromMessage(MessageSendParams params) {
        String taskId = "task-" + System.currentTimeMillis();
        String content = extractTextContent(params.message());

        // Create task using SDK patterns
        TaskStatus initialStatus = new TaskStatus(TaskState.SUBMITTED);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("content", content);
        metadata.put("created", System.currentTimeMillis());

        return new Task(taskId, "OpenHAB A2A Task", initialStatus, new ArrayList<>(), List.of(params.message()),
                metadata, "task");
    }

    private void publishStreamingTaskStatus(SubmissionPublisher<StreamingEventKind> publisher, String taskId,
            TaskState state, String message) {
        try {
            TaskStatus status = new TaskStatus(state);
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("message", message);
            metadata.put("timestamp", System.currentTimeMillis());

            TaskStatusUpdateEvent event = new TaskStatusUpdateEvent(taskId, status, taskId, false, metadata);
            publisher.submit(event);
            logger.debug("Published streaming task status: {} -> {}", taskId, state);
        } catch (Exception e) {
            logger.error("Error publishing streaming task status for task: {}", taskId, e);
        }
    }

    private String extractTextContent(io.a2a.spec.Message message) {
        if (message.getParts() != null) {
            StringBuilder textBuilder = new StringBuilder();
            for (io.a2a.spec.Part part : message.getParts()) {
                if (part instanceof io.a2a.spec.TextPart textPart) {
                    textBuilder.append(textPart.getText());
                }
            }
            return textBuilder.toString();
        }
        return "";
    }

    private String extractActionIdFromMessage(io.a2a.spec.Message message) {
        // Extract action ID from message metadata
        Map<String, Object> metadata = message.getMetadata();
        if (metadata != null && metadata.containsKey("actionId")) {
            return metadata.get("actionId").toString();
        }

        // Fallback: extract from message content
        String content = extractTextContent(message);
        if (content != null && content.contains(" ")) {
            String[] parts = content.split(" ");
            if (parts.length > 0) {
                return parts[0];
            }
        }

        return "system.info";
    }

    private Map<String, Object> extractParametersFromMessage(io.a2a.spec.Message message) {
        Map<String, Object> parameters = new HashMap<>();

        // Extract parameters from message metadata
        Map<String, Object> metadata = message.getMetadata();
        if (metadata != null) {
            // Copy relevant parameters from metadata
            for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                String key = entry.getKey();
                if (key.startsWith("param.")) {
                    String paramName = key.substring(6); // Remove "param." prefix
                    parameters.put(paramName, entry.getValue());
                }
            }
        }

        // Extract parameters from message content
        String content = extractTextContent(message);
        if (content != null) {
            // Parse content for parameters (format: actionId param1=value1 param2=value2)
            String[] parts = content.split(" ");
            for (int i = 1; i < parts.length; i++) {
                String part = parts[i];
                if (part.contains("=")) {
                    String[] keyValue = part.split("=", 2);
                    if (keyValue.length == 2) {
                        parameters.put(keyValue[0], keyValue[1]);
                    }
                }
            }
        }

        return parameters;
    }

    private AIActionResult executeAIActionWithStreaming(String actionId, Map<String, Object> parameters, String taskId,
            SubmissionPublisher<StreamingEventKind> publisher) {
        try {
            logger.debug("Executing AI action with streaming: {} for task: {}", actionId, taskId);

            // Get the AI action from registry
            if (actionRegistry == null) {
                return AIActionResult.error("ActionRegistry not available",
                        new AIActionError("REGISTRY_NOT_AVAILABLE", "ActionRegistry not available"),
                        System.currentTimeMillis());
            }

            AIAction action = actionRegistry.getAction(actionId);
            if (action == null) {
                logger.warn("AI action not found: {}", actionId);
                return AIActionResult.error("Action not found: " + actionId,
                        new AIActionError("ACTION_NOT_FOUND", "Action not found: " + actionId),
                        System.currentTimeMillis());
            }

            // Create AI action context
            AIActionContext context = AIActionContext.builder().protocol("a2a").clientId("a2a-client")
                    .sessionId("a2a-session-" + taskId).correlationId(taskId).priority("normal").build();

            // Send progress update
            publishStreamingTaskStatus(publisher, taskId, TaskState.WORKING, "Executing action: " + actionId);

            // Execute the action
            AIActionResult result = action.execute(parameters, context);

            // Send final status update
            if (result.isSuccess()) {
                publishStreamingTaskStatus(publisher, taskId, TaskState.WORKING, "Action completed successfully");
            } else {
                publishStreamingTaskStatus(publisher, taskId, TaskState.WORKING, "Action completed with errors");
            }

            logger.debug("AI action streaming execution completed for task: {} - success: {}", taskId,
                    result.isSuccess());
            return result;

        } catch (Exception e) {
            logger.error("Error executing AI action with streaming: {} for task: {}", actionId, taskId, e);
            return AIActionResult.error("Execution error: " + e.getMessage(),
                    new AIActionError("EXECUTION_ERROR", "Execution error: " + e.getMessage(), "EXCEPTION", e),
                    System.currentTimeMillis());
        }
    }

    private void updateTaskWithResult(String taskId, AIActionResult result) {
        try {
            // Get the current task
            if (taskManager == null || taskManager.getTaskStore() == null) {
                return;
            }

            Task task = taskManager.getTaskStore().get(taskId);
            if (task != null) {
                // Update task metadata with result
                Map<String, Object> metadata = new HashMap<>(task.getMetadata());
                metadata.put("result", result.getData());
                metadata.put("success", result.isSuccess());
                metadata.put("message", result.getMessage());
                metadata.put("completedAt", System.currentTimeMillis());

                logger.debug("Updated task {} with result - success: {}", taskId, result.isSuccess());
            }
        } catch (Exception e) {
            logger.error("Error updating task with result: {}", taskId, e);
        }
    }

    // ============================================================================
    // Getters for other components
    // ============================================================================

    public Map<String, SubmissionPublisher<StreamingEventKind>> getStreamingPublishers() {
        return new HashMap<>(streamingPublishers);
    }
}
