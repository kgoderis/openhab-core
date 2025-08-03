package org.openhab.core.ai.a2a.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

import io.a2a.server.tasks.TaskStore;
import io.a2a.spec.EventKind;
import io.a2a.spec.JSONRPCError;
import io.a2a.spec.MessageSendParams;
import io.a2a.spec.Task;
import io.a2a.spec.TaskState;
import io.a2a.spec.TaskStatus;
import io.a2a.spec.TaskStatusUpdateEvent;

/**
 * A2A Task Manager - Handles task lifecycle management, execution, and storage.
 * 
 * <p>
 * This class is responsible for:
 * - Task creation and storage
 * - Task execution and monitoring
 * - AI action execution
 * - Skill execution
 * - Task status management
 * </p>
 * 
 * @author AI Assistant
 * @since 1.0.0
 */
@Component(service = A2ATaskManager.class)
public class A2ATaskManager {

    private static final Logger logger = LoggerFactory.getLogger(A2ATaskManager.class);

    @Reference
    private @Nullable AIActionRegistry actionRegistry;

    @Reference
    private @Nullable A2ASkillRegistry skillRegistry;

    @Reference
    private @Nullable A2AOpenHABPersistenceManager persistenceManager;

    // Task storage and execution
    private @Nullable TaskStore taskStore;
    private @Nullable ExecutorService asyncExecutor;

    @Activate
    public void activate() {
        logger.debug("A2A Task Manager activated");
        initializeComponents();
    }

    @Deactivate
    public void deactivate() {
        logger.debug("A2A Task Manager deactivated");

        // Shutdown executor
        if (asyncExecutor != null) {
            asyncExecutor.shutdown();
        }
    }

    private void initializeComponents() {
        logger.debug("Initializing A2A Task Manager components");

        // Create async executor
        asyncExecutor = Executors.newCachedThreadPool();

        // Create task store
        taskStore = createTaskStore();

        logger.debug("A2A Task Manager components initialized");
    }

    private TaskStore createTaskStore() {
        logger.debug("Creating A2A task store");

        return new TaskStore() {
            private final Map<String, Task> tasks = new HashMap<>();

            @Override
            public void save(@Nullable Task task) {
                logger.debug("Saving task: {}", task != null ? task.getId() : "null");
                if (task != null) {
                    tasks.put(task.getId(), task);
                }
            }

            @Override
            public @Nullable Task get(@Nullable String taskId) {
                logger.debug("Getting task: {}", taskId);
                if (taskId == null) {
                    return null;
                }
                return tasks.get(taskId);
            }

            @Override
            public void delete(@Nullable String taskId) {
                logger.debug("Deleting task: {}", taskId);
                if (taskId != null) {
                    tasks.remove(taskId);
                }
            }
        };
    }

    // ============================================================================
    // Public API Methods
    // ============================================================================

    public EventKind handleMessageSend(MessageSendParams params) throws JSONRPCError {
        logger.debug("Handling message send request: {}", params);

        try {
            // Create a task for this message
            Task task = createTaskFromMessage(params);
            if (taskStore != null) {
                taskStore.save(task);
            }

            // Execute the task asynchronously
            ExecutorService executor = asyncExecutor;
            if (executor == null) {
                logger.error("Async executor not available");
                throw new JSONRPCError(-32603, "Internal error: executor not available", null);
            }

            CompletableFuture.runAsync(() -> {
                try {
                    logger.debug("Executing task: {}", task.getId());

                    // Publish task status update
                    publishTaskStatusUpdate(task.getId(), TaskState.WORKING, "Task started");

                    // Extract action information from the message
                    String actionId = extractActionIdFromMessage(params.message());
                    Map<String, Object> parameters = extractParametersFromMessage(params.message());

                    // Execute the actual AI action
                    AIActionResult result = executeAIAction(actionId, parameters, task.getId());

                    if (result.isSuccess()) {
                        // Publish success status
                        publishTaskStatusUpdate(task.getId(), TaskState.COMPLETED, "Task completed successfully");

                        // Update task with result
                        updateTaskWithResult(task.getId(), result);
                    } else {
                        // Publish failure status
                        String errorMessage = result.getMessage() != null ? result.getMessage()
                                : "Task execution failed";
                        publishTaskStatusUpdate(task.getId(), TaskState.FAILED, errorMessage);
                    }

                } catch (Exception e) {
                    logger.error("Error executing task: {}", task.getId(), e);
                    publishTaskStatusUpdate(task.getId(), TaskState.FAILED, "Task failed: " + e.getMessage());
                }
            }, executor);

            // Return SDK event kind
            return new EventKind() {
                @Override
                public String getKind() {
                    return "task.created";
                }
            };
        } catch (Exception e) {
            logger.error("Error handling message send request", e);
            throw new JSONRPCError(-32603, "Internal error: " + e.getMessage(), null);
        }
    }

    public Task getTask(String taskId) throws JSONRPCError {
        logger.debug("Getting task: {}", taskId);

        if (taskStore == null) {
            throw new JSONRPCError(-32603, "Internal error: TaskStore not available", null);
        }

        Task task = taskStore.get(taskId);
        if (task == null) {
            throw new JSONRPCError(-32001, "Task not found: " + taskId, null);
        }
        return task;
    }

    public Task cancelTask(String taskId) throws JSONRPCError {
        logger.debug("Cancelling task: {}", taskId);

        if (taskStore == null) {
            throw new JSONRPCError(-32603, "Internal error: TaskStore not available", null);
        }

        Task task = taskStore.get(taskId);
        if (task == null) {
            throw new JSONRPCError(-32001, "Task not found: " + taskId, null);
        }

        try {
            // Cancel the task
            logger.debug("Task cancellation requested for: {}", taskId);

            // Publish cancellation status
            publishTaskStatusUpdate(taskId, TaskState.CANCELED, "Task cancelled");

            taskStore.delete(taskId);
            return task;
        } catch (Exception e) {
            logger.error("Error cancelling task: {}", taskId, e);
            throw new JSONRPCError(-32603, "Internal error: " + e.getMessage(), null);
        }
    }

    public Object executeSkill(io.a2a.spec.Message message) {
        logger.debug("Executing skill for message: {}", message);

        try {
            String skillId = extractSkillIdFromMessage(message);
            if (skillId != null && skillRegistry != null && skillRegistry.hasSkill(skillId)) {
                return skillRegistry.executeSkill(skillId, message);
            } else {
                logger.warn("Skill not found: {}", skillId);
                return Map.of("error", "Skill not found: " + skillId);
            }
        } catch (Exception e) {
            logger.error("Error executing skill", e);
            return Map.of("error", "Error executing skill: " + e.getMessage());
        }
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

    private void publishTaskStatusUpdate(String taskId, TaskState state, String message) {
        try {
            TaskStatus status = new TaskStatus(state);
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("message", message);
            metadata.put("timestamp", System.currentTimeMillis());

            TaskStatusUpdateEvent event = new TaskStatusUpdateEvent(taskId, status, taskId, false, metadata);
            logger.debug("Published task status update: {} -> {}", taskId, state);
        } catch (Exception e) {
            logger.error("Error publishing task status update for task: {}", taskId, e);
        }
    }

    private String extractSkillIdFromMessage(io.a2a.spec.Message message) {
        String content = extractTextContent(message);
        if (content != null && content.contains(" ")) {
            return content.split(" ")[0];
        }
        return "default";
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

    private AIActionResult executeAIAction(String actionId, Map<String, Object> parameters, String taskId) {
        try {
            logger.debug("Executing AI action: {} for task: {}", actionId, taskId);

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

            // Execute the action
            AIActionResult result = action.execute(parameters, context);

            logger.debug("AI action execution completed for task: {} - success: {}", taskId, result.isSuccess());
            return result;

        } catch (Exception e) {
            logger.error("Error executing AI action: {} for task: {}", actionId, taskId, e);
            return AIActionResult.error("Execution error: " + e.getMessage(),
                    new AIActionError("EXECUTION_ERROR", "Execution error: " + e.getMessage(), "EXCEPTION", e),
                    System.currentTimeMillis());
        }
    }

    private void updateTaskWithResult(String taskId, AIActionResult result) {
        try {
            // Get the current task
            if (taskStore == null) {
                return;
            }

            Task task = taskStore.get(taskId);
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

    public @Nullable TaskStore getTaskStore() {
        return taskStore;
    }

    public @Nullable ExecutorService getAsyncExecutor() {
        return asyncExecutor;
    }
}
