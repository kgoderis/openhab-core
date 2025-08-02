package org.openhab.core.ai.a2a.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.api.action.AIAction;
import org.openhab.core.ai.common.api.action.AIActionContext;
import org.openhab.core.ai.common.api.action.AIActionRegistry;
import org.openhab.core.ai.common.api.action.AIActionResult;
import org.openhab.core.ai.common.auth.AIAuthenticationContext;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.a2a.server.agentexecution.AgentExecutor;
import io.a2a.server.agentexecution.RequestContext;
import io.a2a.server.events.EventQueue;
import io.a2a.spec.JSONRPCError;
import io.a2a.spec.Message;
import io.a2a.spec.Task;
import io.a2a.spec.TaskState;
import io.a2a.spec.TaskStatus;
import io.a2a.spec.TaskStatusUpdateEvent;

/**
 * A2A Agent Executor implementation.
 * 
 * @author AI Assistant
 * @since 1.0.0
 */
@Component(service = AgentExecutor.class)
public class A2AAgentExecutor implements AgentExecutor {

    private final Logger logger = LoggerFactory.getLogger(A2AAgentExecutor.class);

    private @Nullable AIActionRegistry actionRegistry;
    private @Nullable A2ASecurityManager securityManager;
    private @Nullable A2AServerManager serverManager;
    private @Nullable BundleContext bundleContext;

    private final Map<String, AtomicBoolean> runningTasks = new ConcurrentHashMap<>();
    private final Map<String, Long> taskStartTimes = new ConcurrentHashMap<>();

    @Activate
    public void activate(BundleContext context) {
        this.bundleContext = context;
        logger.info("A2A Agent Executor activated");
    }

    @Deactivate
    public void deactivate() {
        logger.info("A2A Agent Executor deactivated");
    }

    @Reference
    public void setActionRegistry(AIActionRegistry actionRegistry) {
        this.actionRegistry = actionRegistry;
    }

    @Reference
    public void setSecurityManager(A2ASecurityManager securityManager) {
        this.securityManager = securityManager;
    }

    @Reference
    public void setServerManager(A2AServerManager serverManager) {
        this.serverManager = serverManager;
    }

    @Override
    public void execute(RequestContext requestContext, EventQueue eventQueue) throws JSONRPCError {
        // Implementation without @NonNullByDefault to avoid parameter redefinition
        try {
            logger.debug("Executing A2A task: {}", requestContext.getTaskId());

            // Extract task from request context
            Task task = requestContext.getTask();
            if (task == null) {
                throw new JSONRPCError(-32602, "Invalid task in request context", null);
            }

            // Authenticate and authorize
            AIAuthenticationContext authContext = authenticateRequest(requestContext);
            if (authContext == null) {
                throw new JSONRPCError(-32001, "Authentication failed", null);
            }

            // Check permissions
            if (!authorizeTask(authContext, task)) {
                throw new JSONRPCError(-32003, "Insufficient permissions", null);
            }

            // Mark task as running
            String taskId = requestContext.getTaskId();
            runningTasks.put(taskId, new AtomicBoolean(true));
            taskStartTimes.put(taskId, System.currentTimeMillis());

            // Execute the task
            executeTask(task, authContext, eventQueue);

        } catch (JSONRPCError e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error executing A2A task: {}", requestContext.getTaskId(), e);
            throw new JSONRPCError(-32603, "Internal error: " + e.getMessage(), null);
        } finally {
            // Clean up
            String taskId = requestContext.getTaskId();
            runningTasks.remove(taskId);
            taskStartTimes.remove(taskId);
        }
    }

    @Override
    public void cancel(RequestContext requestContext, EventQueue eventQueue) throws JSONRPCError {
        // Implementation without @NonNullByDefault to avoid parameter redefinition
        try {
            String taskId = requestContext.getTaskId();
            logger.debug("Cancelling A2A task: {}", taskId);

            AtomicBoolean running = runningTasks.get(taskId);
            if (running != null) {
                running.set(false);
                logger.info("Task {} cancelled", taskId);
            } else {
                logger.warn("Task {} not found for cancellation", taskId);
            }

        } catch (Exception e) {
            logger.error("Error cancelling A2A task: {}", requestContext.getTaskId(), e);
            throw new JSONRPCError(-32603, "Internal error: " + e.getMessage(), null);
        }
    }

    @NonNullByDefault
    private @Nullable AIAuthenticationContext authenticateRequest(RequestContext requestContext) {
        A2ASecurityManager security = securityManager;
        if (security == null) {
            logger.error("Security manager not available");
            return null;
        }

        Message message = requestContext.getMessage();
        if (message == null) {
            logger.error("No message in request context");
            return null;
        }

        return security.authenticateA2AMessage(message).orElse(null);
    }

    @NonNullByDefault
    private boolean authorizeTask(AIAuthenticationContext authContext, Task task) {
        A2ASecurityManager security = securityManager;
        if (security == null) {
            logger.error("Security manager not available");
            return false;
        }

        return security.hasA2APermission(authContext, "a2a:execute");
    }

    @NonNullByDefault
    private void executeTask(Task task, AIAuthenticationContext authContext, EventQueue eventQueue) {
        AIActionRegistry registry = actionRegistry;
        if (registry == null) {
            logger.error("Action registry not available");
            return;
        }

        try {
            // Create AI action context
            AIActionContext aiContext = createAIActionContext(task, authContext);

            // Find and execute the appropriate action
            String actionName = extractActionName(task);
            AIAction action = registry.getAction(actionName);

            if (action != null) {
                // Extract parameters from task metadata
                Map<String, Object> parameters = extractParametersFromTask(task);
                AIActionResult result = action.execute(parameters, aiContext);
                handleActionResult(result, eventQueue);
            } else {
                logger.warn("No action found for: {}", actionName);
                // Send error event
                eventQueue.enqueueEvent(new JSONRPCError(-32601, "No action found for: " + actionName, null));
            }

        } catch (Exception e) {
            logger.error("Error executing task: {}", task.getId(), e);
            eventQueue.enqueueEvent(new JSONRPCError(-32603, "Task execution failed: " + e.getMessage(), null));
        }
    }

    @NonNullByDefault
    private AIActionContext createAIActionContext(Task task, AIAuthenticationContext authContext) {
        // Use the builder pattern for AIActionContext
        AIActionContext.Builder builder = AIActionContext.builder().protocol("a2a")
                .clientId(extractClientIdFromTask(task)).sessionId("a2a-session-" + System.currentTimeMillis())
                .correlationId(task.getId()).authContext(authContext);

        return builder.build();
    }

    @NonNullByDefault
    private String extractActionName(Task task) {
        // Extract action name from task metadata
        Map<String, Object> metadata = task.getMetadata();
        if (metadata != null && metadata.containsKey("action")) {
            Object actionObj = metadata.get("action");
            if (actionObj instanceof String) {
                return (String) actionObj;
            }
        }

        // Fallback: try to extract from task history
        List<Message> history = task.getHistory();
        if (history != null && !history.isEmpty()) {
            Message lastMessage = history.get(history.size() - 1);
            // Extract action from message content if available
            String messageContent = extractMessageContent(lastMessage);
            if (messageContent != null && messageContent.startsWith("action:")) {
                return messageContent.substring(7).trim();
            }
        }

        return "default";
    }

    @NonNullByDefault
    private String extractMessageContent(Message message) {
        // Extract content from message - this is a placeholder implementation
        // The actual implementation depends on the Message interface structure
        if (message != null) {
            // Try to get content from message properties or metadata
            // This is a simplified implementation - adjust based on actual Message interface
            return message.toString();
        }
        return null;
    }

    @NonNullByDefault
    private String extractClientIdFromTask(Task task) {
        // Extract client ID from task metadata or use default
        Map<String, Object> metadata = task.getMetadata();
        if (metadata != null && metadata.containsKey("clientId")) {
            Object clientId = metadata.get("clientId");
            if (clientId instanceof String) {
                return (String) clientId;
            }
        }
        return "a2a-client";
    }

    @NonNullByDefault
    private Map<String, Object> extractParametersFromTask(Task task) {
        // Extract parameters from task metadata
        Map<String, Object> metadata = task.getMetadata();
        if (metadata != null && metadata.containsKey("parameters")) {
            Object paramsObj = metadata.get("parameters");
            if (paramsObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> params = (Map<String, Object>) paramsObj;
                return params;
            }
        }
        return new HashMap<>();
    }

    @NonNullByDefault
    private void handleActionResult(AIActionResult result, EventQueue eventQueue) {
        if (result.isSuccess()) {
            // Send success event with result data
            Object data = result.getData();
            Map<String, Object> metadata = result.getMetadata();
            if (data != null) {
                // Create TaskStatusUpdateEvent for success
                TaskStatusUpdateEvent successEvent = new TaskStatusUpdateEvent.Builder().taskId("task-id") // TODO: Get
                                                                                                           // actual
                                                                                                           // task ID
                        .status(new TaskStatus(TaskState.COMPLETED)).contextId("a2a-context").isFinal(true)
                        .metadata(metadata).build();
                eventQueue.enqueueEvent(successEvent);
            } else {
                // Create TaskStatusUpdateEvent for success with empty data
                TaskStatusUpdateEvent successEvent = new TaskStatusUpdateEvent.Builder().taskId("task-id") // TODO: Get
                                                                                                           // actual
                                                                                                           // task ID
                        .status(new TaskStatus(TaskState.COMPLETED)).contextId("a2a-context").isFinal(true)
                        .metadata(new HashMap<>()).build();
                eventQueue.enqueueEvent(successEvent);
            }
        } else {
            // Send error event
            String errorMessage = result.getMessage();
            if (errorMessage != null) {
                eventQueue.enqueueEvent(new JSONRPCError(-32603, errorMessage, null));
            } else {
                eventQueue.enqueueEvent(new JSONRPCError(-32603, "Action execution failed", null));
            }
        }
    }

    @NonNullByDefault
    public boolean isTaskRunning(String taskId) {
        AtomicBoolean running = runningTasks.get(taskId);
        return running != null && running.get();
    }

    @NonNullByDefault
    public long getTaskExecutionTime(String taskId) {
        Long startTime = taskStartTimes.get(taskId);
        if (startTime != null) {
            return System.currentTimeMillis() - startTime;
        }
        return 0;
    }
}
