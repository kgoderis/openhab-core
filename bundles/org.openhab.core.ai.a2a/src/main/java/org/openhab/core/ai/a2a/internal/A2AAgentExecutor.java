package org.openhab.core.ai.a2a.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

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
import io.a2a.spec.Artifact;
import io.a2a.spec.JSONRPCError;
import io.a2a.spec.Message;
import io.a2a.spec.TaskArtifactUpdateEvent;
import io.a2a.spec.TaskState;
import io.a2a.spec.TaskStatus;
import io.a2a.spec.TaskStatusUpdateEvent;
import io.a2a.spec.TextPart;

/**
 * A2A Agent Executor implementation for OpenHAB using SDK patterns.
 * 
 * This class implements the A2A SDK's AgentExecutor interface to handle
 * agent requests and execute AI actions through the ai.common bundle.
 * Enhanced with SDK utilities and patterns for better integration.
 * 
 * 
 */
@Component(service = AgentExecutor.class, immediate = true)
public class A2AAgentExecutor implements AgentExecutor {

    private static final Logger logger = LoggerFactory.getLogger(A2AAgentExecutor.class);

    // Core dependencies
    @Reference
    private AIActionRegistry actionRegistry;

    @Reference
    private A2ASecurityManager securityManager;

    private final BundleContext bundleContext;

    // Track active tasks for cancellation using SDK patterns
    private final ConcurrentHashMap<String, AtomicBoolean> activeTasks = new ConcurrentHashMap<>();

    // Enhanced task execution tracking
    private final ConcurrentHashMap<String, Long> taskStartTimes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> taskExecutors = new ConcurrentHashMap<>();

    /**
     * Create a new A2A agent executor.
     * 
     * @param bundleContext OSGi bundle context
     */
    @Activate
    public A2AAgentExecutor(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        logger.debug("A2A Agent Executor initialized with SDK patterns");
    }

    /**
     * Cleanup on deactivation.
     */
    @Deactivate
    public void deactivate() {
        logger.debug("A2A Agent Executor deactivated");

        // Clean up active tasks
        activeTasks.clear();
        taskStartTimes.clear();
        taskExecutors.clear();
    }

    @Override
    public void execute(RequestContext requestContext, EventQueue eventQueue) throws JSONRPCError {
        Message message = requestContext.getMessage();
        String taskId = requestContext.getTaskId();

        logger.debug("Executing A2A request for task: {} using SDK patterns", taskId);

        // Track task execution start time
        taskStartTimes.put(taskId, System.currentTimeMillis());

        // Register task as active for cancellation tracking
        AtomicBoolean cancelled = new AtomicBoolean(false);
        activeTasks.put(taskId, cancelled);

        try {
            // Step 1: Authenticate the A2A message using security manager
            Optional<AIAuthenticationContext> authContext = securityManager.authenticateA2AMessage(message);

            if (authContext.isEmpty()) {
                logger.warn("Authentication failed for A2A request: {}", taskId);
                throw new JSONRPCError(-32001, "Authentication failed", null);
            }

            // Step 2: Validate request and check rate limiting
            String clientId = extractClientIdFromMessage(message);
            if (!securityManager.validateA2ARequest(clientId, "execute")) {
                logger.warn("Request validation failed for A2A request: {}", taskId);
                throw new JSONRPCError(-32002, "Request validation failed", null);
            }

            // Step 3: Check A2A-specific permissions
            if (!securityManager.hasA2APermission(authContext.get(), A2ASecurityManager.A2APermission.EXECUTE)) {
                logger.warn("Insufficient permissions for A2A request: {}", taskId);
                throw new JSONRPCError(-32003, "Insufficient permissions", null);
            }

            // Step 4: Create AIAction context with authenticated information
            AIActionContext aiContext = createAIActionContext(requestContext, authContext.get());

            // Step 5: Extract action information from message using SDK patterns
            String actionId = extractActionIdFromMessage(message);
            Map<String, Object> parameters = extractParametersFromMessage(message);

            // Step 6: Find and execute the action
            AIAction action = actionRegistry.getAction(actionId);
            if (action == null) {
                logger.warn("Action not found: {}", actionId);
                throw new JSONRPCError(-32004, "Action not found: " + actionId, null);
            }

            // Track executor information
            taskExecutors.put(taskId, actionId);

            // Check if task was cancelled before execution
            if (cancelled.get()) {
                logger.debug("Task was cancelled before execution: {}", taskId);
                sendTaskStatusUpdate(eventQueue, taskId, TaskState.CANCELED, "Task cancelled before execution");
                return;
            }

            // Step 7: Execute the action using SDK patterns
            AIActionResult result = action.execute(parameters, aiContext);

            // Check if task was cancelled during execution
            if (cancelled.get()) {
                logger.debug("Task was cancelled during execution: {}", taskId);
                sendTaskStatusUpdate(eventQueue, taskId, TaskState.CANCELED, "Task cancelled during execution");
                return;
            }

            // Step 8: Handle execution result using SDK patterns
            handleExecutionResult(result, eventQueue, taskId);

        } catch (JSONRPCError e) {
            // Re-throw JSON-RPC errors
            throw e;
        } catch (Exception e) {
            logger.error("Error executing A2A request: {}", taskId, e);
            handleExecutionError(e, eventQueue, taskId);
        } finally {
            // Clean up task tracking
            activeTasks.remove(taskId);
            taskStartTimes.remove(taskId);
            taskExecutors.remove(taskId);
        }
    }

    @Override
    public void cancel(RequestContext requestContext, EventQueue eventQueue) throws JSONRPCError {
        String taskId = requestContext.getTaskId();
        logger.debug("Cancelling A2A task: {} using SDK patterns", taskId);

        // Mark task as cancelled
        AtomicBoolean cancelled = activeTasks.get(taskId);
        if (cancelled != null) {
            cancelled.set(true);
            logger.debug("Task marked for cancellation: {}", taskId);
        } else {
            logger.warn("Task not found for cancellation: {}", taskId);
        }

        // Send cancellation status update
        sendTaskStatusUpdate(eventQueue, taskId, TaskState.CANCELED, "Task cancelled by user");
    }

    private AIActionContext createAIActionContext(RequestContext requestContext, AIAuthenticationContext authContext) {
        // Use the builder pattern for AIActionContext
        AIActionContext.Builder builder = AIActionContext.builder().protocol("a2a")
                .clientId(extractClientIdFromMessage(requestContext.getMessage()))
                .sessionId("a2a-session-" + System.currentTimeMillis()).correlationId(requestContext.getTaskId())
                .priority("normal").authContext(authContext);

        // Add SDK context information
        Map<String, Object> protocolContext = new HashMap<>();
        protocolContext.put("taskId", requestContext.getTaskId());
        protocolContext.put("executionTime", System.currentTimeMillis());

        // Add authentication context information
        if (authContext != null) {
            protocolContext.put("authenticated", true);
            protocolContext.put("permissions", authContext.getPermissions());
        } else {
            protocolContext.put("authenticated", false);
        }

        return builder.protocolContext(protocolContext).build();
    }

    private String extractClientIdFromMessage(Message message) {
        // Extract client ID from message metadata or content
        Map<String, Object> metadata = message.getMetadata();
        if (metadata != null && metadata.containsKey("clientId")) {
            return metadata.get("clientId").toString();
        }

        // Fallback: extract from message content
        String content = extractTextContent(message);
        if (content != null && content.startsWith("client:")) {
            return content.substring(7).split(" ")[0];
        }

        return "unknown-client";
    }

    private String extractActionIdFromMessage(Message message) {
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

    private Map<String, Object> extractParametersFromMessage(Message message) {
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

    private String extractTextContent(Message message) {
        if (message.getParts() != null) {
            StringBuilder textBuilder = new StringBuilder();
            for (io.a2a.spec.Part part : message.getParts()) {
                if (part instanceof TextPart textPart) {
                    textBuilder.append(textPart.getText());
                }
            }
            return textBuilder.toString();
        }
        return null;
    }

    private void handleExecutionResult(AIActionResult result, EventQueue eventQueue, String taskId) {
        try {
            if (result.isSuccess()) {
                // Send completion status using SDK patterns
                sendTaskStatusUpdate(eventQueue, taskId, TaskState.COMPLETED, "Task completed successfully");

                // Send artifact update if result contains data
                if (result.getData() != null) {
                    sendTaskArtifactUpdate(eventQueue, taskId, result.getData());
                }

                logger.debug("Task execution completed successfully: {}", taskId);
            } else {
                // Send failure status using SDK patterns
                String errorMessage = result.getMessage() != null ? result.getMessage() : "Task execution failed";
                sendTaskStatusUpdate(eventQueue, taskId, TaskState.FAILED, errorMessage);
                logger.warn("Task execution failed: {} - {}", taskId, errorMessage);
            }
        } catch (Exception e) {
            logger.error("Error handling execution result for task: {}", taskId, e);
            sendTaskStatusUpdate(eventQueue, taskId, TaskState.FAILED, "Error handling result: " + e.getMessage());
        }
    }

    private void handleExecutionError(Exception error, EventQueue eventQueue, String taskId) {
        try {
            String errorMessage = error.getMessage() != null ? error.getMessage() : "Unknown execution error";
            sendTaskStatusUpdate(eventQueue, taskId, TaskState.FAILED, "Execution error: " + errorMessage);
            logger.error("Task execution error: {} - {}", taskId, errorMessage);
        } catch (Exception e) {
            logger.error("Error handling execution error for task: {}", taskId, e);
        }
    }

    private void sendTaskStatusUpdate(EventQueue eventQueue, String taskId, TaskState status, String message) {
        try {
            TaskStatus taskStatus = new TaskStatus(status);
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("message", message);
            metadata.put("timestamp", System.currentTimeMillis());
            metadata.put("executionTime", getTaskExecutionTime(taskId));

            TaskStatusUpdateEvent event = new TaskStatusUpdateEvent(taskId, taskStatus, taskId, status.isFinal(),
                    metadata);
            eventQueue.enqueueEvent(event);

            logger.debug("Published task status update: {} -> {}", taskId, status);
        } catch (Exception e) {
            logger.error("Error publishing task status update for task: {}", taskId, e);
        }
    }

    private void sendTaskArtifactUpdate(EventQueue eventQueue, String taskId, Object data) {
        try {
            // Create artifact from result data using the correct constructor
            String resultText = data.toString();
            TextPart textPart = new TextPart(resultText);
            List<io.a2a.spec.Part<?>> parts = List.of(textPart);
            Map<String, Object> artifactMetadata = new HashMap<>();
            artifactMetadata.put("mimeType", "application/json");

            Artifact artifact = new Artifact("result", "Task Result", "Result data from AI action execution", parts,
                    artifactMetadata);

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("timestamp", System.currentTimeMillis());
            metadata.put("executor", taskExecutors.get(taskId));

            TaskArtifactUpdateEvent event = new TaskArtifactUpdateEvent(taskId, artifact, taskId, false, true,
                    metadata);
            eventQueue.enqueueEvent(event);

            logger.debug("Published task artifact update: {}", taskId);
        } catch (Exception e) {
            logger.error("Error publishing task artifact update for task: {}", taskId, e);
        }
    }

    private long getTaskExecutionTime(String taskId) {
        Long startTime = taskStartTimes.get(taskId);
        if (startTime != null) {
            return System.currentTimeMillis() - startTime;
        }
        return 0;
    }

    // Enhanced utility methods for SDK integration
    public boolean isTaskActive(String taskId) {
        return activeTasks.containsKey(taskId);
    }

    public void cancelTask(String taskId) {
        AtomicBoolean cancelled = activeTasks.get(taskId);
        if (cancelled != null) {
            cancelled.set(true);
            logger.debug("Task cancellation requested: {}", taskId);
        }
    }

    public Map<String, Object> getTaskStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("activeTasks", activeTasks.size());
        stats.put("totalStartTimes", taskStartTimes.size());
        stats.put("totalExecutors", taskExecutors.size());
        return stats;
    }
}
