package org.openhab.core.ai.agent.execution;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.ActionContext;
import org.openhab.core.ai.action.ActionRegistry;
import org.openhab.core.ai.action.ActionResult;
import org.openhab.core.ai.action.api.Action;
import org.openhab.core.ai.agent.api.AgentSkillManager;
import org.openhab.core.ai.agent.api.AgentSkillResult;
import org.openhab.core.ai.agent.communication.protocol.AgentProtocolHandler;
import org.openhab.core.ai.agent.infrastructure.synchronization.ConcurrentAgentSynchronizationManager;
import org.openhab.core.ai.agent.lifecycle.AgentSecurityManager;
import org.openhab.core.ai.auth.AuthenticationContext;
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
import io.a2a.server.tasks.TaskUpdater;
import io.a2a.spec.JSONRPCError;
import io.a2a.spec.Message;
import io.a2a.spec.Part;
import io.a2a.spec.Task;
import io.a2a.spec.TaskNotCancelableError;
import io.a2a.spec.TaskState;
import io.a2a.spec.TextPart;

/**
 * A2A SDK Compliant Task Execution.
 * 
 * <p>
 * <strong>Primary Responsibility:</strong> A2A SDK Compliant Task Execution
 * </p>
 * 
 * <p>
 * <strong>Concerns:</strong>
 * <ul>
 * <li><strong>A2A SDK Compliance:</strong> Implements AgentExecutor interface</li>
 * <li><strong>Task Execution:</strong> Executes tasks according to A2A specifications</li>
 * <li><strong>Authentication:</strong> Authenticates task execution requests</li>
 * <li><strong>Authorization:</strong> Authorizes task execution permissions</li>
 * <li><strong>Timeout Management:</strong> Handles task execution timeouts</li>
 * <li><strong>Retry Logic:</strong> Implements retry mechanisms for failed tasks</li>
 * <li><strong>Fallback Handling:</strong> Manages fallback agent execution</li>
 * <li><strong>Error Reporting:</strong> Reports execution errors via A2A events</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>What this class DOES:</strong>
 * <ul>
 * <li>Implements A2A SDK {@link AgentExecutor} interface</li>
 * <li>Executes tasks via {@link #execute(RequestContext, EventQueue)}</li>
 * <li>Handles task cancellation via {@link #cancel(RequestContext, EventQueue)}</li>
 * <li>Authenticates and authorizes task execution requests</li>
 * <li>Manages task execution timeouts and retry logic</li>
 * <li>Handles fallback agent execution for failed tasks</li>
 * <li>Reports execution results via A2A EventQueue</li>
 * <li>Delegates skill execution to {@link AgentSkillManager}</li>
 * <li>Delegates action execution to {@link ActionRegistry}</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>What this class DOES NOT do:</strong>
 * <ul>
 * <li>❌ Manage task lifecycle (delegates to AgentTaskManager)</li>
 * <li>❌ Handle protocol communication (delegates to AgentProtocolHandler)</li>
 * <li>❌ Register or manage skills (delegates to AgentSkillRegistry)</li>
 * <li>❌ Contain business logic (delegates to Action.executeAsync)</li>
 * <li>❌ Manage skill registry (delegates to AgentSkillRegistry)</li>
 * <li>❌ Handle parameter conversion (delegates to AgentSkillManagerImpl)</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>Boundary Conditions:</strong>
 * <ul>
 * <li>Only executes tasks according to A2A specifications</li>
 * <li>Delegates skill execution to AgentSkillManager</li>
 * <li>Delegates action execution to ActionRegistry</li>
 * <li>Reports results via A2A EventQueue</li>
 * <li>Does not manage task lifecycle</li>
 * <li>Does not handle protocol communication</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>Dependencies:</strong>
 * <ul>
 * <li>{@link AgentSkillManager}: For skill execution</li>
 * <li>{@link ActionRegistry}: For action execution</li>
 * <li>{@link AgentSecurityManager}: For authentication/authorization</li>
 * <li>A2A SDK classes: For protocol compliance</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>Architecture Layer:</strong> A2A Protocol Layer
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = AgentExecutor.class)
@NonNullByDefault
public class AgentTaskExecutor implements AgentExecutor {

    private final Logger logger = LoggerFactory.getLogger(AgentTaskExecutor.class);

    @Reference
    private @Nullable ActionRegistry actionRegistry;

    @Reference
    private @Nullable AgentSecurityManager securityManager;

    @Reference
    private @Nullable AgentProtocolHandler protocolHandler;

    @Reference
    private @Nullable ConcurrentAgentSynchronizationManager synchronizationService;

    @Reference
    private @Nullable AgentSkillManager agentSkillManager;

    private @Nullable BundleContext bundleContext;

    // Task execution tracking
    private final Map<String, AtomicBoolean> runningTasks = new ConcurrentHashMap<>();
    private final Map<String, Long> taskStartTimes = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> taskExecutionCounts = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> taskFailureCounts = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> taskRetryCounts = new ConcurrentHashMap<>();

    // Performance metrics
    private final Map<String, AtomicLong> totalExecutionTime = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> averageExecutionTime = new ConcurrentHashMap<>();

    // Configuration
    private final long defaultTimeoutMs = 30000; // 30 seconds
    private final int maxRetries = 3;
    private final long retryDelayMs = 1000; // 1 second

    @Activate
    public void activate(BundleContext context) {
        this.bundleContext = context;
        logger.debug("Agent Task Executor activated");
    }

    @Deactivate
    public void deactivate() {
        logger.debug("Agent Task Executor deactivated");
        // Clean up running tasks
        runningTasks.clear();
        taskStartTimes.clear();
    }

    @Reference
    public void setActionRegistry(ActionRegistry actionRegistry) {
        this.actionRegistry = actionRegistry;
    }

    @Reference
    public void setSecurityManager(AgentSecurityManager securityManager) {
        this.securityManager = securityManager;
    }

    @Reference
    public void setProtocolHandler(AgentProtocolHandler protocolHandler) {
        this.protocolHandler = protocolHandler;
    }

    @Reference
    public void setSynchronizationService(ConcurrentAgentSynchronizationManager synchronizationService) {
        this.synchronizationService = synchronizationService;
    }

    @Reference
    public void setAgentSkillManager(AgentSkillManager agentSkillManager) {
        this.agentSkillManager = agentSkillManager;
    }

    @Override
    public void execute(RequestContext requestContext, EventQueue eventQueue) throws JSONRPCError {
        TaskUpdater updater = new TaskUpdater(requestContext, eventQueue);
        String taskId = requestContext.getTaskId();

        try {
            logger.debug("Executing A2A task: {}", taskId);

            // 1. Task lifecycle management - follow A2A SDK pattern
            if (requestContext.getTask() == null) {
                updater.submit();
            }
            updater.startWork();

            // Track task execution for metrics (not lifecycle - that's handled by TaskUpdater)
            runningTasks.put(taskId, new AtomicBoolean(true));
            taskStartTimes.put(taskId, System.currentTimeMillis());
            taskExecutionCounts.computeIfAbsent(taskId, k -> new AtomicLong(0)).incrementAndGet();

            // 2. Authentication and authorization
            AuthenticationContext authContext = authenticateRequest(requestContext);
            if (authContext == null) {
                updater.fail(newAgentMessage("Authentication failed", Map.of("error", "AUTH_FAILED")));
                return;
            }

            Task task = requestContext.getTask();
            if (task == null) {
                updater.fail(newAgentMessage("Invalid task in request context", Map.of("error", "INVALID_TASK")));
                return;
            }

            if (!authorizeTask(authContext, task)) {
                updater.fail(newAgentMessage("Insufficient permissions", Map.of("error", "INSUFFICIENT_PERMISSIONS")));
                return;
            }

            // 3. Execute task with enhanced features (timeout, retry, fallback)
            executeTaskWithEnhancements(task, authContext, eventQueue);

        } catch (JSONRPCError e) {
            // Re-throw JSONRPC errors as-is
            throw e;
        } catch (Exception e) {
            logger.error("Error executing A2A task: {}", taskId, e);

            // Track failure for metrics
            taskFailureCounts.computeIfAbsent(taskId, k -> new AtomicLong(0)).incrementAndGet();

            // Fail task with error message
            updater.fail(newAgentMessage("Task execution failed: " + e.getMessage(),
                    Map.of("error", "EXECUTION_ERROR", "details", e.getMessage())));
        } finally {
            // Clean up tracking
            runningTasks.remove(taskId);
            taskStartTimes.remove(taskId);
        }
    }

    @Override
    public void cancel(RequestContext requestContext, EventQueue eventQueue) throws JSONRPCError {
        Task task = requestContext.getTask();
        String taskId = requestContext.getTaskId();

        logger.debug("Cancelling A2A task: {}", taskId);

        // Check if task can be cancelled - follow A2A SDK pattern
        if (task == null) {
            throw new JSONRPCError(-32602, "Task not found", null);
        }

        if (task.getStatus().state() == TaskState.CANCELED) {
            throw new TaskNotCancelableError();
        }

        if (task.getStatus().state() == TaskState.COMPLETED) {
            throw new TaskNotCancelableError();
        }

        // Cancel the task using TaskUpdater
        TaskUpdater updater = new TaskUpdater(requestContext, eventQueue);
        updater.cancel();

        // Clean up tracking
        AtomicBoolean running = runningTasks.get(taskId);
        if (running != null) {
            running.set(false);
            logger.info("Task {} cancelled", taskId);
        }
    }

    @NonNullByDefault
    private void executeTaskWithEnhancements(Task task, AuthenticationContext authContext, EventQueue eventQueue) {
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
                    return (Void) null;
                });
    }

    @NonNullByDefault
    private void executeTask(Task task, AuthenticationContext authContext, EventQueue eventQueue) {
        try {
            // Extract action name from task
            String actionName = extractActionName(task);

            // Check if this is a skill execution task
            if (isSkillExecution(actionName)) {
                executeSkillTask(task, authContext, eventQueue);
            } else {
                executeActionTask(task, authContext, eventQueue);
            }
        } catch (Exception e) {
            logger.error("Error executing task: {}", task.getId(), e);
            handleTaskFailure(task, eventQueue, e);
        }
    }

    @NonNullByDefault
    private void executeSkillTask(Task task, AuthenticationContext authContext, EventQueue eventQueue) {
        AgentSkillManager skillManager = agentSkillManager;
        if (skillManager == null) {
            logger.error("AgentSkillManager not available");
            handleTaskFailure(task, eventQueue, new IllegalStateException("AgentSkillManager not available"));
            return;
        }

        try {
            String skillId = extractSkillId(task);
            Map<String, Object> parameters = extractParametersFromTask(task);

            logger.debug("Executing skill task: {} with skill: {}", task.getId(), skillId);

            // Execute skill via AgentSkillManager
            AgentSkillResult skillResult = skillManager.executeSkill(skillId, parameters);
            handleSkillResult(skillResult, eventQueue, task.getId());

        } catch (Exception e) {
            logger.error("Error executing skill task: {}", task.getId(), e);
            handleTaskFailure(task, eventQueue, e);
        }
    }

    @NonNullByDefault
    private void executeActionTask(Task task, AuthenticationContext authContext, EventQueue eventQueue) {
        ActionRegistry registry = actionRegistry;
        if (registry == null) {
            logger.error("Action registry not available");
            handleTaskFailure(task, eventQueue, new IllegalStateException("Action registry not available"));
            return;
        }

        try {
            // Create AI action context
            ActionContext aiContext = createActionContext(task, authContext);

            // Find and execute the appropriate action
            String actionName = extractActionName(task);
            Action action = registry.getAction(actionName);

            if (action != null) {
                // Extract parameters from task metadata
                Map<String, Object> parameters = extractParametersFromTask(task);
                ActionResult result = action.execute(parameters, aiContext);
                handleActionResult(result, eventQueue, task.getId());
            } else {
                logger.warn("No action found for: {}", actionName);
                handleTaskFailure(task, eventQueue, new IllegalArgumentException("No action found for: " + actionName));
            }

        } catch (Exception e) {
            logger.error("Error executing action task: {}", task.getId(), e);
            handleTaskFailure(task, eventQueue, e);
        }
    }

    @NonNullByDefault
    private void handleTaskFailure(Task task, EventQueue eventQueue, Throwable error) {
        String taskId = task.getId();
        AtomicLong retryCount = taskRetryCounts.computeIfAbsent(taskId, k -> new AtomicLong(0));

        if (retryCount.get() < maxRetries) {
            retryCount.incrementAndGet();
            taskFailureCounts.computeIfAbsent(taskId, k -> new AtomicLong(0)).incrementAndGet();

            logger.debug("Retrying task {} (attempt {}/{})", taskId, retryCount.get(), maxRetries);

            // Schedule retry with delay
            CompletableFuture.delayedExecutor(retryDelayMs, java.util.concurrent.TimeUnit.MILLISECONDS).execute(() -> {
                try {
                    // Re-authenticate for retry
                    AuthenticationContext authContext = authenticateRequest(
                            new RequestContext(null, task.getId(), task.getContextId(), task, List.of()));
                    if (authContext != null) {
                        executeTaskWithEnhancements(task, authContext, eventQueue);
                    } else {
                        // Create TaskUpdater for error reporting
                        RequestContext retryContext = new RequestContext(null, task.getId(), task.getContextId(), task,
                                List.of());
                        TaskUpdater updater = new TaskUpdater(retryContext, eventQueue);
                        updater.fail(newAgentMessage("Authentication failed during retry",
                                Map.of("error", "AUTH_FAILED_RETRY")));
                    }
                } catch (Exception e) {
                    logger.error("Retry execution failed for task: {}", taskId, e);
                    // Create TaskUpdater for error reporting
                    RequestContext retryContext = new RequestContext(null, task.getId(), task.getContextId(), task,
                            List.of());
                    TaskUpdater updater = new TaskUpdater(retryContext, eventQueue);
                    updater.fail(newAgentMessage("Retry execution failed: " + e.getMessage(),
                            Map.of("error", "RETRY_FAILED")));
                }
            });
        } else {
            // Max retries exceeded, mark as failed
            taskFailureCounts.computeIfAbsent(taskId, k -> new AtomicLong(0)).incrementAndGet();

            // Create TaskUpdater for final failure reporting
            RequestContext failureContext = new RequestContext(null, task.getId(), task.getContextId(), task,
                    List.of());
            TaskUpdater updater = new TaskUpdater(failureContext, eventQueue);
            updater.fail(newAgentMessage("Task failed after " + maxRetries + " retries",
                    Map.of("error", "MAX_RETRIES_EXCEEDED", "retryCount", maxRetries)));

            logger.error("Task {} failed after {} retries", taskId, maxRetries);
        }
    }

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
            taskFailureCounts.computeIfAbsent(taskId, k -> new AtomicLong(0)).incrementAndGet();

            // Create TaskUpdater for timeout failure reporting
            RequestContext timeoutContext = new RequestContext(null, task.getId(), task.getContextId(), task,
                    List.of());
            TaskUpdater updater = new TaskUpdater(timeoutContext, eventQueue);
            updater.fail(newAgentMessage("Task timed out and no fallback agents available",
                    Map.of("error", "TIMEOUT_NO_FALLBACK", "timeoutMs", defaultTimeoutMs)));
        }
    }

    @NonNullByDefault
    private void executeWithFallbackAgents(Task task, List<String> fallbackAgents, EventQueue eventQueue) {
        String taskId = task.getId();

        // Try each fallback agent
        for (String fallbackAgent : fallbackAgents) {
            try {
                logger.debug("Trying fallback agent {} for task {}", fallbackAgent, taskId);

                // Create new task for fallback agent (simplified - would need proper Task creation)
                // For now, just retry with original task
                AuthenticationContext authContext = authenticateRequest(
                        new RequestContext(null, task.getId(), task.getContextId(), task, List.of()));
                if (authContext != null) {
                    executeTask(task, authContext, eventQueue);
                    return; // Success, exit
                }
            } catch (Exception e) {
                logger.warn("Fallback agent {} failed for task {}: {}", fallbackAgent, taskId, e.getMessage());
            }
        }

        // All fallback agents failed
        taskFailureCounts.computeIfAbsent(taskId, k -> new AtomicLong(0)).incrementAndGet();

        // Create TaskUpdater for fallback failure reporting
        RequestContext fallbackContext = new RequestContext(null, task.getId(), task.getContextId(), task, List.of());
        TaskUpdater updater = new TaskUpdater(fallbackContext, eventQueue);
        updater.fail(newAgentMessage("All fallback agents failed for task: " + taskId,
                Map.of("error", "ALL_FALLBACKS_FAILED", "fallbackAgents", fallbackAgents)));
    }

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

    @NonNullByDefault
    private void handleActionResult(ActionResult result, EventQueue eventQueue, String taskId) {
        // Create TaskUpdater for result reporting
        RequestContext resultContext = new RequestContext(null, taskId, "a2a-context", null, List.of());
        TaskUpdater updater = new TaskUpdater(resultContext, eventQueue);

        if (result.isSuccess()) {
            // Send success event with result data
            Object data = result.getData();
            Map<String, Object> metadata = result.getMetadata();

            List<Part<?>> responseParts = List.of(new TextPart(data != null ? data.toString() : "Success", null));
            updater.addArtifact(responseParts, null, null, metadata != null ? metadata : Map.of());
            updater.complete();
        } else {
            // Send error event
            String errorMessage = result.getMessage();
            updater.fail(newAgentMessage(errorMessage != null ? errorMessage : "Action execution failed",
                    Map.of("error", "ACTION_EXECUTION_FAILED")));
        }
    }

    @NonNullByDefault
    private void handleSkillResult(AgentSkillResult skillResult, EventQueue eventQueue, String taskId) {
        // Create TaskUpdater for result reporting
        RequestContext resultContext = new RequestContext(null, taskId, "a2a-context", null, List.of());
        TaskUpdater updater = new TaskUpdater(resultContext, eventQueue);

        if (skillResult.isSuccess()) {
            // Send success event with result data
            Object data = skillResult.getData();
            Map<String, Object> metadata = Map.of("executionTime", skillResult.getExecutionTime(), "errorCode",
                    skillResult.getErrorCode() != null ? skillResult.getErrorCode() : "SUCCESS");

            List<Part<?>> responseParts = List.of(new TextPart(data != null ? data.toString() : "Success", null));
            updater.addArtifact(responseParts, null, null, metadata);
            updater.complete();
        } else {
            // Send error event
            String errorMessage = skillResult.getErrorMessage();
            updater.fail(newAgentMessage(errorMessage != null ? errorMessage : "Skill execution failed",
                    Map.of("error", "SKILL_EXECUTION_FAILED")));
        }
    }

    @NonNullByDefault
    private Message newAgentMessage(String text, Map<String, Object> metadata) {
        List<Part<?>> parts = List.of(new TextPart(text, null));
        return new Message(Message.Role.AGENT, parts, null, null, null, null, metadata);
    }

    @NonNullByDefault
    private @Nullable AuthenticationContext authenticateRequest(RequestContext requestContext) {
        AgentSecurityManager security = securityManager;
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
    private boolean authorizeTask(AuthenticationContext authContext, Task task) {
        AgentSecurityManager security = securityManager;
        if (security == null) {
            logger.error("Security manager not available");
            return false;
        }

        return security.hasA2APermission(authContext, "a2a:execute");
    }

    @NonNullByDefault
    private ActionContext createActionContext(Task task, AuthenticationContext authContext) {
        return ActionContext.builder().protocol("a2a").clientId(extractClientIdFromTask(task))
                .sessionId("a2a-session-" + System.currentTimeMillis()).correlationId(task.getId())
                .authContext(authContext).build();
    }

    @NonNullByDefault
    private String extractActionName(Task task) {
        // Extract action name from task metadata or message content
        Map<String, Object> metadata = task.getMetadata();
        if (metadata != null && metadata.containsKey("actionName")) {
            Object actionNameObj = metadata.get("actionName");
            if (actionNameObj instanceof String) {
                return (String) actionNameObj;
            }
        }

        // Fallback: extract from message content
        List<Message> history = task.getHistory();
        if (history != null && !history.isEmpty()) {
            Message lastMessage = history.get(history.size() - 1);
            String content = extractMessageContent(lastMessage);
            if (content != null && content.contains(" ")) {
                return content.split(" ")[0];
            }
        }

        return "system.info";
    }

    @NonNullByDefault
    private String extractMessageContent(Message message) {
        if (message.getParts() != null) {
            StringBuilder textBuilder = new StringBuilder();
            for (Part<?> part : message.getParts()) {
                if (part instanceof TextPart textPart) {
                    textBuilder.append(textPart.getText());
                }
            }
            return textBuilder.toString();
        }
        return null;
    }

    @NonNullByDefault
    private String extractClientIdFromTask(Task task) {
        Map<String, Object> metadata = task.getMetadata();
        if (metadata != null && metadata.containsKey("clientId")) {
            Object clientIdObj = metadata.get("clientId");
            if (clientIdObj instanceof String) {
                return (String) clientIdObj;
            }
        }
        return "unknown";
    }

    @NonNullByDefault
    private Map<String, Object> extractParametersFromTask(Task task) {
        Map<String, Object> parameters = new ConcurrentHashMap<>();

        // Extract from task metadata
        Map<String, Object> metadata = task.getMetadata();
        if (metadata != null) {
            for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                String key = entry.getKey();
                if (key.startsWith("param.")) {
                    String paramName = key.substring(6); // Remove "param." prefix
                    parameters.put(paramName, entry.getValue());
                }
            }
        }

        // Extract from message content
        List<Message> history = task.getHistory();
        if (history != null && !history.isEmpty()) {
            Message lastMessage = history.get(history.size() - 1);
            String content = extractMessageContent(lastMessage);
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
        }

        return parameters;
    }

    @NonNullByDefault
    private boolean isSkillExecution(String actionName) {
        // Check if the action name indicates a skill execution
        return actionName != null && (actionName.startsWith("skill.") || actionName.startsWith("agent.skill.")
                || actionName.equals("executeSkill") || actionName.equals("skill"));
    }

    @NonNullByDefault
    private String extractSkillId(Task task) {
        // Extract skill ID from task metadata
        Map<String, Object> metadata = task.getMetadata();
        if (metadata != null && metadata.containsKey("skillId")) {
            Object skillIdObj = metadata.get("skillId");
            if (skillIdObj instanceof String) {
                return (String) skillIdObj;
            }
        }

        // Fallback: try to extract from action name
        String actionName = extractActionName(task);
        if (actionName != null && actionName.startsWith("skill.")) {
            return actionName.substring(6); // Remove "skill." prefix
        }

        // Default skill ID
        return "default";
    }

    // Additional utility methods for task management
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

    // Task execution metrics
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

    // Inner class extracted to top-level: org.openhab.core.ai.agent.execution.TaskExecutionMetrics
}
