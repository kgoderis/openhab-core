package org.openhab.core.ai.a2a.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.action.AIActionContext;
import org.openhab.core.ai.common.action.AIActionRegistry;
import org.openhab.core.ai.common.action.AIActionResult;
import org.openhab.core.ai.common.api.action.AIAction;
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
    private @Nullable A2AProtocolHandler protocolHandler;
    private @Nullable A2ASynchronizationService synchronizationService;
    private @Nullable BundleContext bundleContext;

    // Task execution tracking
    private final Map<String, AtomicBoolean> runningTasks = new ConcurrentHashMap<>();
    private final Map<String, Long> taskStartTimes = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> taskExecutionCounts = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> taskFailureCounts = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> taskRetryCounts = new ConcurrentHashMap<>();

    // Performance monitoring
    private final Map<String, AtomicLong> totalExecutionTime = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> averageExecutionTime = new ConcurrentHashMap<>();

    // Configuration
    private final long defaultTimeoutMs = 30000; // 30 seconds
    private final int maxRetries = 3;
    private final long retryDelayMs = 1000; // 1 second

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
    public void setProtocolHandler(A2AProtocolHandler protocolHandler) {
        this.protocolHandler = protocolHandler;
    }

    @Reference
    public void setSynchronizationService(A2ASynchronizationService synchronizationService) {
        this.synchronizationService = synchronizationService;
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

            // Start task lifecycle management
            startTaskLifecycle(task, eventQueue);

            // Execute task with enhanced features
            executeTaskWithEnhancements(task, authContext, eventQueue);

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
            sendErrorEvent(eventQueue, -32603, "Action registry not available");
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
                handleActionResult(result, eventQueue, task.getId());
            } else {
                logger.warn("No action found for: {}", actionName);
                // Send error event
                sendErrorEvent(eventQueue, -32601, "No action found for: " + actionName);
            }

        } catch (Exception e) {
            logger.error("Error executing task: {}", task.getId(), e);
            sendErrorEvent(eventQueue, -32603, "Task execution failed: " + e.getMessage());
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
    private void handleActionResult(AIActionResult result, EventQueue eventQueue, String taskId) {
        if (result.isSuccess()) {
            // Send success event with result data
            Object data = result.getData();
            Map<String, Object> metadata = result.getMetadata();
            sendSuccessEvent(eventQueue, taskId, data, metadata);
        } else {
            // Send error event
            String errorMessage = result.getMessage();
            if (errorMessage != null) {
                sendErrorEvent(eventQueue, -32603, errorMessage);
            } else {
                sendErrorEvent(eventQueue, -32603, "Action execution failed");
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

    // Helper methods for common event patterns
    @NonNullByDefault
    private void sendErrorEvent(EventQueue eventQueue, int code, String message) {
        eventQueue.enqueueEvent(new JSONRPCError(code, message, null));
    }

    @NonNullByDefault
    private void sendSuccessEvent(EventQueue eventQueue, String taskId, Object data, Map<String, Object> metadata) {
        TaskStatusUpdateEvent successEvent = new TaskStatusUpdateEvent.Builder().taskId(taskId)
                .status(new TaskStatus(TaskState.COMPLETED)).contextId("a2a-context").isFinal(true)
                .metadata(metadata != null ? metadata : new HashMap<>()).build();
        eventQueue.enqueueEvent(successEvent);
    }

    @NonNullByDefault
    private void sendTaskStatusEvent(EventQueue eventQueue, String taskId, TaskState state, boolean isFinal) {
        TaskStatusUpdateEvent statusEvent = new TaskStatusUpdateEvent.Builder().taskId(taskId)
                .status(new TaskStatus(state)).contextId("a2a-context").isFinal(isFinal).metadata(new HashMap<>())
                .build();
        eventQueue.enqueueEvent(statusEvent);
    }

    /**
     * Start task lifecycle management
     */
    @NonNullByDefault
    private void startTaskLifecycle(Task task, EventQueue eventQueue) {
        String taskId = task.getId();

        // Initialize task tracking
        runningTasks.putIfAbsent(taskId, new AtomicBoolean(false));
        taskExecutionCounts.putIfAbsent(taskId, new AtomicLong(0));
        taskFailureCounts.putIfAbsent(taskId, new AtomicLong(0));
        taskRetryCounts.putIfAbsent(taskId, new AtomicLong(0));
        totalExecutionTime.putIfAbsent(taskId, new AtomicLong(0));
        averageExecutionTime.putIfAbsent(taskId, new AtomicLong(0));

        // Mark task as starting
        runningTasks.get(taskId).set(true);
        taskStartTimes.put(taskId, System.currentTimeMillis());
        taskExecutionCounts.get(taskId).incrementAndGet();

        // Send task started event
        sendTaskStatusEvent(eventQueue, taskId, TaskState.WORKING, false);

        logger.debug("Started task lifecycle for task: {}", taskId);
    }

    /**
     * Execute task with enhanced features (timeout, retry, fallback)
     */
    @NonNullByDefault
    private void executeTaskWithEnhancements(Task task, AIAuthenticationContext authContext, EventQueue eventQueue) {
        String taskId = task.getId();

        // Create CompletableFuture for async execution
        CompletableFuture<Void> executionFuture = CompletableFuture.runAsync(() -> {
            try {
                // Execute the actual task
                executeTask(task, authContext, eventQueue);
            } catch (Exception e) {
                logger.error("Task execution failed: {}", taskId, e);
                handleTaskFailure(task, eventQueue, e);
            }
        });

        // Add timeout handling
        executionFuture.orTimeout(defaultTimeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS)
                .exceptionally(throwable -> {
                    if (throwable instanceof java.util.concurrent.TimeoutException) {
                        logger.warn("Task {} timed out after {} ms", taskId, defaultTimeoutMs);
                        handleTaskTimeout(task, eventQueue);
                    } else {
                        logger.error("Task {} failed with exception", taskId, throwable);
                        handleTaskFailure(task, eventQueue, throwable);
                    }
                    return null;
                });
    }

    /**
     * Handle task failure with retry mechanism
     */
    @NonNullByDefault
    private void handleTaskFailure(Task task, EventQueue eventQueue, Throwable error) {
        String taskId = task.getId();
        AtomicLong retryCount = taskRetryCounts.get(taskId);

        if (retryCount != null && retryCount.get() < maxRetries) {
            retryCount.incrementAndGet();
            taskFailureCounts.get(taskId).incrementAndGet();

            logger.debug("Retrying task {} (attempt {}/{})", taskId, retryCount.get(), maxRetries);

            // Schedule retry with delay
            CompletableFuture.delayedExecutor(retryDelayMs, java.util.concurrent.TimeUnit.MILLISECONDS).execute(() -> {
                try {
                    // Re-authenticate for retry
                    AIAuthenticationContext authContext = authenticateRequest(
                            new RequestContext(null, task.getId(), task.getContextId(), task, List.of())); // TODO: Get
                                                                                                           // from
                                                                                                           // context
                    if (authContext != null) {
                        executeTaskWithEnhancements(task, authContext, eventQueue);
                    } else {
                        sendErrorEvent(eventQueue, -32001, "Authentication failed during retry");
                    }
                } catch (Exception e) {
                    logger.error("Retry execution failed for task: {}", taskId, e);
                    sendErrorEvent(eventQueue, -32603, "Retry execution failed: " + e.getMessage());
                }
            });
        } else {
            // Max retries exceeded, mark as failed
            taskFailureCounts.get(taskId).incrementAndGet();
            sendTaskStatusEvent(eventQueue, taskId, TaskState.FAILED, true);
            logger.error("Task {} failed after {} retries", taskId, maxRetries);
        }
    }

    /**
     * Handle task timeout
     */
    @NonNullByDefault
    private void handleTaskTimeout(Task task, EventQueue eventQueue) {
        String taskId = task.getId();

        // Try fallback agents if available
        List<String> fallbackAgents = getFallbackAgents(task);
        if (!fallbackAgents.isEmpty()) {
            logger.debug("Trying fallback agents for timed out task: {}", taskId);
            executeWithFallbackAgents(task, fallbackAgents, eventQueue);
        } else {
            // No fallback available, mark as failed
            taskFailureCounts.get(taskId).incrementAndGet();
            sendTaskStatusEvent(eventQueue, taskId, TaskState.FAILED, true);
            sendErrorEvent(eventQueue, -32002, "Task timed out and no fallback agents available");
        }
    }

    /**
     * Execute task with fallback agents
     */
    @NonNullByDefault
    private void executeWithFallbackAgents(Task task, List<String> fallbackAgents, EventQueue eventQueue) {
        String taskId = task.getId();

        // Try each fallback agent
        for (String fallbackAgent : fallbackAgents) {
            try {
                logger.debug("Trying fallback agent {} for task {}", fallbackAgent, taskId);

                // Create new task for fallback agent (simplified - would need proper Task creation)
                // For now, just retry with original task
                AIAuthenticationContext authContext = authenticateRequest(
                        new RequestContext(null, task.getId(), task.getContextId(), task, List.of())); // TODO: Get from
                                                                                                       // context
                if (authContext != null) {
                    executeTask(task, authContext, eventQueue);
                    return; // Success, exit
                }
            } catch (Exception e) {
                logger.warn("Fallback agent {} failed for task {}: {}", fallbackAgent, taskId, e.getMessage());
            }
        }

        // All fallback agents failed
        taskFailureCounts.get(taskId).incrementAndGet();
        sendTaskStatusEvent(eventQueue, taskId, TaskState.FAILED, true);
        sendErrorEvent(eventQueue, -32003, "All fallback agents failed for task: " + taskId);
    }

    /**
     * Get fallback agents for a task
     */
    @NonNullByDefault
    private List<String> getFallbackAgents(Task task) {
        // Extract fallback agents from task metadata
        @Nullable
        Object fallbackObj = task.getMetadata().get("fallbackAgents");
        if (fallbackObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> fallbackAgents = (List<String>) fallbackObj;
            return fallbackAgents;
        }
        return List.of(); // No fallback agents
    }

    /**
     * Execute task with transaction-like semantics using A2ASynchronizationService
     */
    @NonNullByDefault
    public CompletableFuture<A2ASynchronizationService.TransactionResult> executeTaskWithTransaction(Task task,
            EventQueue eventQueue) {
        if (synchronizationService == null) {
            CompletableFuture<A2ASynchronizationService.TransactionResult> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalStateException("SynchronizationService not available"));
            return future;
        }

        // Create a single-task transaction
        List<Task> tasks = List.of(task);
        return synchronizationService.executeTransaction(tasks);
    }

    /**
     * Execute multiple tasks with dependency resolution
     */
    @NonNullByDefault
    public CompletableFuture<List<io.a2a.spec.TaskStatusUpdateEvent>> executeTasksWithDependencies(List<Task> tasks,
            EventQueue eventQueue) {
        if (synchronizationService == null) {
            CompletableFuture<List<io.a2a.spec.TaskStatusUpdateEvent>> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalStateException("SynchronizationService not available"));
            return future;
        }

        return synchronizationService.executeTasksWithDependencies(tasks);
    }

    /**
     * Acquire resource lock for task execution
     */
    @NonNullByDefault
    public boolean acquireResourceLock(String resourceId, String taskId) {
        if (synchronizationService == null) {
            logger.warn("SynchronizationService not available for resource lock acquisition");
            return false;
        }

        return synchronizationService.acquireLock(resourceId, taskId);
    }

    /**
     * Release resource lock after task execution
     */
    @NonNullByDefault
    public void releaseResourceLock(String resourceId, String taskId) {
        if (synchronizationService != null) {
            synchronizationService.releaseLock(resourceId, taskId);
        }
    }

    /**
     * Get synchronization service statistics
     */
    @NonNullByDefault
    public A2ASynchronizationService.TaskExecutionStats getSynchronizationStats() {
        if (synchronizationService == null) {
            return new A2ASynchronizationService.TaskExecutionStats(0, Map.of(), 0, 0);
        }

        return synchronizationService.getTaskExecutionStats();
    }

    /**
     * Get task execution metrics
     */
    @NonNullByDefault
    public TaskExecutionMetrics getTaskExecutionMetrics(String taskId) {
        AtomicLong executionCount = taskExecutionCounts.get(taskId);
        AtomicLong failureCount = taskFailureCounts.get(taskId);
        AtomicLong retryCount = taskRetryCounts.get(taskId);
        AtomicLong totalTime = totalExecutionTime.get(taskId);
        AtomicLong avgTime = averageExecutionTime.get(taskId);

        return new TaskExecutionMetrics(taskId, executionCount != null ? executionCount.get() : 0,
                failureCount != null ? failureCount.get() : 0, retryCount != null ? retryCount.get() : 0,
                totalTime != null ? totalTime.get() : 0, avgTime != null ? avgTime.get() : 0);
    }

    /**
     * Task execution metrics
     */
    public static class TaskExecutionMetrics {
        private final String taskId;
        private final long executionCount;
        private final long failureCount;
        private final long retryCount;
        private final long totalExecutionTime;
        private final long averageExecutionTime;

        public TaskExecutionMetrics(String taskId, long executionCount, long failureCount, long retryCount,
                long totalExecutionTime, long averageExecutionTime) {
            this.taskId = taskId;
            this.executionCount = executionCount;
            this.failureCount = failureCount;
            this.retryCount = retryCount;
            this.totalExecutionTime = totalExecutionTime;
            this.averageExecutionTime = averageExecutionTime;
        }

        public String getTaskId() {
            return taskId;
        }

        public long getExecutionCount() {
            return executionCount;
        }

        public long getFailureCount() {
            return failureCount;
        }

        public long getRetryCount() {
            return retryCount;
        }

        public long getTotalExecutionTime() {
            return totalExecutionTime;
        }

        public long getAverageExecutionTime() {
            return averageExecutionTime;
        }

        public double getSuccessRate() {
            return executionCount > 0 ? (double) (executionCount - failureCount) / executionCount : 0.0;
        }
    }
}
