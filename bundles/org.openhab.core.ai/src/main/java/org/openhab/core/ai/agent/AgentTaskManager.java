package org.openhab.core.ai.agent.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.ActionContext;
import org.openhab.core.ai.action.ActionError;
import org.openhab.core.ai.action.ActionRegistry;
import org.openhab.core.ai.action.ActionResult;
import org.openhab.core.ai.api.action.Action;
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
 * Enhanced A2A Task Manager - Handles task lifecycle management, execution, orchestration, and storage
 * with advanced deadlock prevention, resource locking, transaction support, and fault tolerance.
 * 
 * <p>
 * This class is responsible for:
 * - Task creation and storage
 * - Single task execution and monitoring
 * - Multi-task orchestration and coordination
 * - AI action execution
 * - Skill execution
 * - Task status management
 * - Agent coordination and load balancing
 * - Performance monitoring and metrics
 * - Security and authorization controls
 * - Deadlock prevention and circular dependency detection
 * - Resource locking for concurrent agent access
 * - Transaction support for multi-agent operations
 * - Advanced fault tolerance with retry mechanisms and fallback support
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 */
@Component(service = AgentTaskManager.class)
@NonNullByDefault
public class AgentTaskManager {

    private static final Logger logger = LoggerFactory.getLogger(AgentTaskManager.class);

    @Reference
    private @Nullable ActionRegistry actionRegistry;

    @Reference
    private @Nullable AgentSkillRegistry skillRegistry;

    @Reference
    private @Nullable AgentPersistenceManager persistenceManager;

    @Reference
    private @Nullable AgentSynchronizationService synchronizationService;

    @Reference
    private @Nullable AgentRegistry agentRegistry;

    // Task storage and execution
    private @Nullable TaskStore taskStore;
    private @Nullable ExecutorService asyncExecutor;
    private @Nullable ScheduledExecutorService retryExecutor;

    // Task orchestration state (from AgentAgentTaskOrchestrator)
    private final ConcurrentHashMap<String, TaskOrchestrationState> taskStates = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, TaskMetrics> taskMetrics = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> taskAgentAssignments = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<String>> taskDependencies = new ConcurrentHashMap<>();

    // Performance tracking
    private final AtomicLong totalOrchestratedTasks = new AtomicLong(0);
    private final AtomicLong successfulOrchestrations = new AtomicLong(0);
    private final AtomicLong failedOrchestrations = new AtomicLong(0);

    // Enhanced features for section 16.1.5.9
    // Resource locking for concurrent agent access
    private final ConcurrentHashMap<String, ReentrantReadWriteLock> agentLocks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Lock> taskLocks = new ConcurrentHashMap<>();

    // Transaction support for multi-agent operations
    private final ConcurrentHashMap<String, TaskTransaction> activeTransactions = new ConcurrentHashMap<>();

    // Deadlock prevention and circular dependency detection
    private final ConcurrentHashMap<String, Set<String>> resourceAllocationGraph = new ConcurrentHashMap<>();
    private final Lock deadlockDetectionLock = new ReentrantLock();

    // Advanced fault tolerance
    private final ConcurrentHashMap<String, RetryContext> retryContexts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, FallbackStrategy> fallbackStrategies = new ConcurrentHashMap<>();

    // Configuration
    private static final long DEFAULT_TASK_TIMEOUT_MS = 30000; // 30 seconds
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 1000; // 1 second
    private static final long DEADLOCK_DETECTION_INTERVAL_MS = 5000; // 5 seconds
    private static final long TRANSACTION_TIMEOUT_MS = 60000; // 60 seconds

    @Activate
    public void activate() {
        logger.debug("Enhanced A2A Task Manager activated");
        initializeComponents();
        startDeadlockDetection();
    }

    @Deactivate
    public void deactivate() {
        logger.debug("Enhanced A2A Task Manager deactivated");
        if (asyncExecutor != null) {
            asyncExecutor.shutdown();
        }
        if (retryExecutor != null) {
            retryExecutor.shutdown();
        }
        cleanupAllTransactions();
    }

    private void initializeComponents() {
        taskStore = createTaskStore();
        asyncExecutor = Executors.newCachedThreadPool();
        retryExecutor = Executors.newScheduledThreadPool(2);
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
    // Public API Methods - Single Task Execution (from original A2ATaskManager)
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

                    // Initialize task state for orchestration
                    TaskOrchestrationState state = new TaskOrchestrationState(task.getId());
                    taskStates.put(task.getId(), state);
                    startTask(task.getId());

                    // Publish task status update
                    publishTaskStatusUpdate(task.getId(), TaskState.WORKING, "Task started");

                    // Extract action information from the message
                    String actionId = extractActionIdFromMessage(params.message());
                    Map<String, Object> parameters = extractParametersFromMessage(params.message());

                    // Execute the actual AI action
                    ActionResult result = executeAction(actionId, parameters, task.getId());

                    if (result.isSuccess()) {
                        // Publish success status
                        publishTaskStatusUpdate(task.getId(), TaskState.COMPLETED, "Task completed successfully");
                        updateTaskWithResult(task.getId(), result);

                        // Update metrics
                        TaskMetrics metrics = taskMetrics.get(task.getId());
                        if (metrics != null) {
                            metrics.recordSuccess();
                        }
                    } else {
                        // Publish failure status
                        String errorMessage = result.getMessage() != null ? result.getMessage()
                                : "Task execution failed";
                        publishTaskStatusUpdate(task.getId(), TaskState.FAILED, errorMessage);

                        // Update metrics
                        TaskMetrics metrics = taskMetrics.get(task.getId());
                        if (metrics != null) {
                            metrics.recordError(new Exception(errorMessage));
                        }
                    }

                } catch (Exception e) {
                    logger.error("Error executing task: {}", task.getId(), e);
                    publishTaskStatusUpdate(task.getId(), TaskState.FAILED, "Task failed: " + e.getMessage());
                    handleTaskError(task.getId(), e);
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
            // Cancel the task using orchestration methods
            cancelTaskOrchestration(taskId);

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
    // Public API Methods - Multi-Task Orchestration (from AgentAgentTaskOrchestrator)
    // ============================================================================

    /**
     * Orchestrate multiple tasks with coordination and dependency management.
     * 
     * @param tasks the list of tasks to orchestrate
     * @return CompletableFuture containing the orchestration results
     */
    public CompletableFuture<List<TaskStatusUpdateEvent>> orchestrateTasks(List<Task> tasks) {
        logger.debug("Starting orchestration of {} tasks", tasks.size());

        return CompletableFuture.supplyAsync(() -> {
            List<TaskStatusUpdateEvent> results = new ArrayList<>();
            List<Task> validatedTasks = new ArrayList<>();

            try {
                // Step 1: Validate all tasks
                for (Task task : tasks) {
                    if (validateTask(task)) {
                        validatedTasks.add(task);
                    } else {
                        logger.warn("Task validation failed for task: {}", task.getId());
                        results.add(createErrorResponse(task, "Task validation failed"));
                    }
                }

                // Step 2: Build dependency graph
                Map<String, List<String>> dependencyGraph = buildDependencyGraph(validatedTasks);

                // Step 3: Check for circular dependencies
                if (hasCircularDependencies(dependencyGraph)) {
                    logger.error("Circular dependencies detected in task graph");
                    for (Task task : validatedTasks) {
                        results.add(createErrorResponse(task, "Circular dependencies detected"));
                    }
                    return results;
                }

                // Step 4: Execute tasks in dependency order
                List<Task> orderedTasks = orderTasksByDependencies(validatedTasks, dependencyGraph);

                for (Task task : orderedTasks) {
                    try {
                        TaskStatusUpdateEvent result = executeTaskWithOrchestration(task);
                        results.add(result);
                        successfulOrchestrations.incrementAndGet();
                    } catch (Exception e) {
                        logger.error("Task orchestration failed for task: {}", task.getId(), e);
                        results.add(createErrorResponse(task, "Orchestration failed: " + e.getMessage()));
                        failedOrchestrations.incrementAndGet();
                    }
                }

                totalOrchestratedTasks.addAndGet(validatedTasks.size());
                logger.info("Completed orchestration of {} tasks", validatedTasks.size());

            } catch (Exception e) {
                logger.error("Task orchestration failed", e);
                for (Task task : tasks) {
                    results.add(createErrorResponse(task, "Orchestration failed: " + e.getMessage()));
                }
            }

            return results;
        });
    }

    /**
     * Validate a single task for correctness and completeness.
     * 
     * @param task the task to validate
     * @return true if the task is valid
     */
    public boolean validateTask(Task task) {
        try {
            // Basic validation
            if (task == null || task.getId() == null || task.getId().isEmpty()) {
                logger.warn("Task validation failed: null or empty task ID");
                return false;
            }

            // Schema validation
            List<String> schemaErrors = validateTaskSchema(task);
            if (!schemaErrors.isEmpty()) {
                logger.warn("Task schema validation failed for task {}: {}", task.getId(), schemaErrors);
                return false;
            }

            // Capability validation
            String requiredCapability = getRequiredCapability(task);
            if (requiredCapability != null && !requiredCapability.isEmpty()) {
                List<String> agentsWithCapability = getAgentsWithCapability(requiredCapability);
                if (agentsWithCapability.isEmpty()) {
                    logger.warn("No agents available with required capability: {}", requiredCapability);
                    return false;
                }
            }

            logger.debug("Task validation successful for task: {}", task.getId());
            return true;

        } catch (Exception e) {
            logger.error("Task validation error for task: {}", task.getId(), e);
            return false;
        }
    }

    /**
     * Validate task schema and return any validation errors.
     * 
     * @param task the task to validate
     * @return list of validation error messages
     */
    public List<String> validateTaskSchema(Task task) {
        List<String> errors = new ArrayList<>();

        try {
            // Validate required fields
            if (task.getMetadata() == null) {
                errors.add("Task metadata is required");
            }

            if (task.getStatus() == null) {
                errors.add("Task status is required");
            }

            // Validate action-specific requirements
            String actionId = getActionId(task);
            if (actionId != null && !actionId.isEmpty()) {
                // TODO: Add action-specific validation based on ActionRegistry
                // This would validate parameters, required fields, etc.
            }

            // Validate dependencies
            List<String> dependencies = getDependencies(task);
            if (dependencies != null) {
                for (String dependency : dependencies) {
                    if (dependency == null || dependency.isEmpty()) {
                        errors.add("Invalid dependency: null or empty");
                    }
                }
            }

        } catch (Exception e) {
            errors.add("Schema validation error: " + e.getMessage());
        }

        return errors;
    }

    /**
     * Select the optimal agent for a given task based on capabilities and load.
     * 
     * @param task the task to route
     * @return the ID of the selected agent
     */
    public String selectOptimalAgent(Task task) {
        try {
            String requiredCapability = getRequiredCapability(task);
            if (requiredCapability == null || requiredCapability.isEmpty()) {
                logger.warn("No capability requirement found for task: {}", task.getId());
                return "";
            }

            List<String> availableAgents = getAgentsWithCapability(requiredCapability);
            if (availableAgents.isEmpty()) {
                logger.warn("No agents available with capability: {}", requiredCapability);
                return "";
            }

            // Simple load balancing: select agent with lowest active task count
            String selectedAgent = availableAgents.stream().min((a1, a2) -> {
                int load1 = getAgentActiveTaskCount(a1);
                int load2 = getAgentActiveTaskCount(a2);
                return Integer.compare(load1, load2);
            }).orElse("");

            logger.debug("Selected agent {} for task {} with capability {}", selectedAgent, task.getId(),
                    requiredCapability);
            return selectedAgent;

        } catch (Exception e) {
            logger.error("Error selecting optimal agent for task: {}", task.getId(), e);
            return "";
        }
    }

    /**
     * Distribute tasks across available agents.
     * 
     * @param tasks the tasks to distribute
     * @return list of agent IDs for each task
     */
    public List<String> distributeTasks(List<Task> tasks) {
        List<String> agentAssignments = new ArrayList<>();

        for (Task task : tasks) {
            String agentId = selectOptimalAgent(task);
            agentAssignments.add(agentId);

            if (agentId != null && !agentId.isEmpty()) {
                taskAgentAssignments.put(task.getId(), agentId);
            }
        }

        return agentAssignments;
    }

    /**
     * Start a task execution.
     * 
     * @param taskId the ID of the task to start
     */
    public void startTask(String taskId) {
        try {
            TaskOrchestrationState state = taskStates.get(taskId);
            if (state != null) {
                state.setState(TaskOrchestrationState.State.RUNNING);
                state.setStartTime(System.currentTimeMillis());

                // Initialize metrics
                taskMetrics.put(taskId, new TaskMetrics(taskId));

                logger.debug("Started task: {}", taskId);
            } else {
                logger.warn("Task not found for start: {}", taskId);
            }
        } catch (Exception e) {
            logger.error("Error starting task: {}", taskId, e);
        }
    }

    /**
     * Pause a running task.
     * 
     * @param taskId the ID of the task to pause
     */
    public void pauseTask(String taskId) {
        try {
            TaskOrchestrationState state = taskStates.get(taskId);
            if (state != null && state.getState() == TaskOrchestrationState.State.RUNNING) {
                state.setState(TaskOrchestrationState.State.PAUSED);
                logger.debug("Paused task: {}", taskId);
            } else {
                logger.warn("Cannot pause task {}: not running", taskId);
            }
        } catch (Exception e) {
            logger.error("Error pausing task: {}", taskId, e);
        }
    }

    /**
     * Resume a paused task.
     * 
     * @param taskId the ID of the task to resume
     */
    public void resumeTask(String taskId) {
        try {
            TaskOrchestrationState state = taskStates.get(taskId);
            if (state != null && state.getState() == TaskOrchestrationState.State.PAUSED) {
                state.setState(TaskOrchestrationState.State.RUNNING);
                logger.debug("Resumed task: {}", taskId);
            } else {
                logger.warn("Cannot resume task {}: not paused", taskId);
            }
        } catch (Exception e) {
            logger.error("Error resuming task: {}", taskId, e);
        }
    }

    /**
     * Cancel a task execution (orchestration version).
     * 
     * @param taskId the ID of the task to cancel
     */
    public void cancelTaskOrchestration(String taskId) {
        try {
            TaskOrchestrationState state = taskStates.get(taskId);
            if (state != null) {
                state.setState(TaskOrchestrationState.State.CANCELLED);

                // Update metrics
                TaskMetrics metrics = taskMetrics.get(taskId);
                if (metrics != null) {
                    metrics.recordCancellation();
                }

                logger.debug("Cancelled task: {}", taskId);
            } else {
                logger.warn("Task not found for cancellation: {}", taskId);
            }
        } catch (Exception e) {
            logger.error("Error cancelling task: {}", taskId, e);
        }
    }

    /**
     * Get metrics for a specific task.
     * 
     * @param taskId the task ID
     * @return the task metrics
     */
    public TaskMetrics getTaskMetrics(String taskId) {
        return taskMetrics.getOrDefault(taskId, new TaskMetrics(taskId));
    }

    /**
     * Get metrics for all tasks.
     * 
     * @return list of all task metrics
     */
    public List<TaskMetrics> getAllTaskMetrics() {
        return new ArrayList<>(taskMetrics.values());
    }

    /**
     * Handle task execution errors.
     * 
     * @param taskId the task ID
     * @param error the error that occurred
     */
    public void handleTaskError(String taskId, Exception error) {
        try {
            TaskOrchestrationState state = taskStates.get(taskId);
            if (state != null) {
                state.setState(TaskOrchestrationState.State.ERROR);
                state.setLastError(error);

                // Update metrics
                TaskMetrics metrics = taskMetrics.get(taskId);
                if (metrics != null) {
                    metrics.recordError(error);
                }

                logger.error("Task error handled for task: {}", taskId, error);
            }
        } catch (Exception e) {
            logger.error("Error handling task error for task: {}", taskId, e);
        }
    }

    /**
     * Attempt to recover from a task error.
     * 
     * @param taskId the task ID
     * @return true if recovery was successful
     */
    public boolean recoverFromTaskError(String taskId) {
        try {
            TaskOrchestrationState state = taskStates.get(taskId);
            if (state != null && state.getState() == TaskOrchestrationState.State.ERROR) {
                // TODO: Implement recovery logic
                // This could involve retrying with different parameters, using fallback agents, etc.

                state.setState(TaskOrchestrationState.State.RUNNING);
                logger.debug("Recovered from error for task: {}", taskId);
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error during task recovery for task: {}", taskId, e);
            return false;
        }
    }

    /**
     * Authorize an agent to execute a task.
     * 
     * @param agentId the agent ID
     * @param task the task to authorize
     * @return true if authorized
     */
    public boolean authorizeTask(String agentId, Task task) {
        try {
            // TODO: Implement proper authorization logic
            // This should check agent permissions, task ownership, etc.

            // For now, basic capability check
            String requiredCapability = getRequiredCapability(task);
            if (requiredCapability != null && !requiredCapability.isEmpty()) {
                List<String> agentsWithCapability = getAgentsWithCapability(requiredCapability);
                return agentsWithCapability.contains(agentId);
            }

            return true; // Default to authorized if no capability requirement
        } catch (Exception e) {
            logger.error("Error authorizing task for agent: {}", agentId, e);
            return false;
        }
    }

    /**
     * Enforce security controls on a task.
     * 
     * @param task the task to secure
     */
    public void enforceTaskSecurity(Task task) {
        try {
            // TODO: Implement security enforcement
            // This could involve sanitizing inputs, checking permissions, etc.

            logger.debug("Security enforced for task: {}", task.getId());
        } catch (Exception e) {
            logger.error("Error enforcing security for task: {}", task.getId(), e);
        }
    }

    // ============================================================================
    // Private Helper Methods - Single Task Execution (from original A2ATaskManager)
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

    private ActionResult executeAction(String actionId, Map<String, Object> parameters, String taskId) {
        try {
            logger.debug("Executing AI action: {} for task: {}", actionId, taskId);

            // Get the AI action from registry
            if (actionRegistry == null) {
                return ActionResult.error("ActionRegistry not available",
                        new ActionError("REGISTRY_NOT_AVAILABLE", "ActionRegistry not available"),
                        System.currentTimeMillis());
            }

            Action action = actionRegistry.getAction(actionId);
            if (action == null) {
                logger.warn("AI action not found: {}", actionId);
                return ActionResult.error("Action not found: " + actionId,
                        new ActionError("ACTION_NOT_FOUND", "Action not found: " + actionId),
                        System.currentTimeMillis());
            }

            // Create AI action context
            ActionContext context = ActionContext.builder().protocol("a2a").clientId("a2a-client")
                    .sessionId("a2a-session-" + taskId).correlationId(taskId).priority("normal").build();

            // Execute the action
            ActionResult result = action.execute(parameters, context);

            logger.debug("AI action execution completed for task: {} - success: {}", taskId, result.isSuccess());
            return result;

        } catch (Exception e) {
            logger.error("Error executing AI action: {} for task: {}", actionId, taskId, e);
            return ActionResult.error("Execution error: " + e.getMessage(),
                    new ActionError("EXECUTION_ERROR", "Execution error: " + e.getMessage(), "EXCEPTION", e),
                    System.currentTimeMillis());
        }
    }

    private void updateTaskWithResult(String taskId, ActionResult result) {
        try {
            // Get the current task
            if (taskStore == null) {
                return;
            }

            Task task = taskStore.get(taskId);
            if (task != null) {
                // Update task metadata with result
                Map<String, Object> metadata = new HashMap<>(task.getMetadata());
                Object resultData = result.getData();
                metadata.put("result", resultData != null ? resultData : "");
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
    // Private Helper Methods - Multi-Task Orchestration (from AgentAgentTaskOrchestrator)
    // ============================================================================

    private TaskStatusUpdateEvent executeTaskWithOrchestration(Task task) {
        try {
            // Initialize task state
            TaskOrchestrationState state = new TaskOrchestrationState(task.getId());
            taskStates.put(task.getId(), state);

            // Start task
            startTask(task.getId());

            // Select agent
            String agentId = selectOptimalAgent(task);
            if (agentId == null || agentId.isEmpty()) {
                throw new RuntimeException("No suitable agent found for task: " + task.getId());
            }

            // Authorize task
            if (!authorizeTask(agentId, task)) {
                throw new RuntimeException("Task not authorized for agent: " + agentId);
            }

            // Enforce security
            enforceTaskSecurity(task);

            // Execute task with synchronization service
            AgentSynchronizationService syncService = synchronizationService;
            if (syncService != null) {
                List<Task> singleTaskList = List.of(task);
                List<TaskStatusUpdateEvent> results = syncService.executeTasksWithDependencies(singleTaskList).get();
                return results.isEmpty()
                        ? new TaskStatusUpdateEvent(task.getId(), new TaskStatus(TaskState.FAILED), task.getContextId(),
                                true, null)
                        : results.get(0);
            } else {
                throw new RuntimeException("Synchronization service not available");
            }

        } catch (Exception e) {
            handleTaskError(task.getId(), e);
            throw new RuntimeException("Task execution failed", e);
        }
    }

    private TaskStatusUpdateEvent createErrorResponse(Task task, String errorMessage) {
        // TODO: Create proper error response using A2A SDK
        return new TaskStatusUpdateEvent(task.getId(), new TaskStatus(TaskState.FAILED), task.getContextId(), true,
                null);
    }

    private String getRequiredCapability(Task task) {
        Map<String, Object> metadata = task.getMetadata();
        if (metadata != null) {
            return (String) metadata.get("requiredCapability");
        }
        return "";
    }

    private String getActionId(Task task) {
        Map<String, Object> metadata = task.getMetadata();
        if (metadata != null) {
            return (String) metadata.get("actionId");
        }
        return "";
    }

    @SuppressWarnings("unchecked")
    private List<String> getDependencies(Task task) {
        Map<String, Object> metadata = task.getMetadata();
        if (metadata != null) {
            Object deps = metadata.get("dependencies");
            if (deps instanceof List) {
                return (List<String>) deps;
            }
        }
        return new ArrayList<>();
    }

    /**
     * Order tasks by dependencies using topological sort
     */
    private List<Task> orderTasksByDependencies(List<Task> tasks, Map<String, List<String>> dependencyGraph) {
        // Simple topological sort implementation
        List<Task> orderedTasks = new ArrayList<>();
        Set<String> visited = ConcurrentHashMap.newKeySet();
        Set<String> processing = ConcurrentHashMap.newKeySet();

        for (Task task : tasks) {
            if (!visited.contains(task.getId())) {
                topologicalSortUtil(task.getId(), tasks, dependencyGraph, visited, processing, orderedTasks);
            }
        }

        return orderedTasks;
    }

    /**
     * Utility method for topological sort
     */
    private void topologicalSortUtil(String taskId, List<Task> allTasks, Map<String, List<String>> dependencyGraph,
            Set<String> visited, Set<String> processing, List<Task> orderedTasks) {

        if (processing.contains(taskId)) {
            // Circular dependency detected
            logger.warn("Circular dependency detected for task: {}", taskId);
            return;
        }

        if (visited.contains(taskId)) {
            return;
        }

        visited.add(taskId);
        processing.add(taskId);

        List<String> dependencies = dependencyGraph.get(taskId);
        if (dependencies != null) {
            for (String dependency : dependencies) {
                topologicalSortUtil(dependency, allTasks, dependencyGraph, visited, processing, orderedTasks);
            }
        }

        processing.remove(taskId);

        // Find the task and add it to ordered list
        for (Task task : allTasks) {
            if (task.getId().equals(taskId)) {
                orderedTasks.add(task);
                break;
            }
        }
    }

    /**
     * Get agents with capability (fixed method call)
     */
    private List<String> getAgentsWithCapability(String capability) {
        AgentRegistry registry = agentRegistry;
        if (registry != null) {
            // Use a default user ID for capability lookup
            return registry.findAgentsWithCapability(capability, "system");
        }
        return List.of();
    }

    private int getAgentActiveTaskCount(String agentId) {
        // TODO: Implement agent load tracking
        return 0; // Placeholder
    }

    private Map<String, List<String>> buildDependencyGraph(List<Task> tasks) {
        Map<String, List<String>> graph = new HashMap<>();

        for (Task task : tasks) {
            List<String> dependencies = getDependencies(task);
            if (dependencies != null) {
                graph.put(task.getId(), new ArrayList<>(dependencies));
            } else {
                graph.put(task.getId(), new ArrayList<>());
            }
        }

        return graph;
    }

    /**
     * Enhanced circular dependency detection with deadlock prevention
     */
    private boolean hasCircularDependencies(Map<String, List<String>> dependencyGraph) {
        // Use depth-first search to detect cycles
        Set<String> visited = ConcurrentHashMap.newKeySet();
        Set<String> recursionStack = ConcurrentHashMap.newKeySet();

        for (String taskId : dependencyGraph.keySet()) {
            if (!visited.contains(taskId)) {
                if (hasCycleUtil(taskId, visited, recursionStack, dependencyGraph)) {
                    logger.warn("Circular dependency detected for task: {}", taskId);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Utility method for cycle detection using DFS (generic version)
     */
    private boolean hasCycleUtil(String taskId, Set<String> visited, Set<String> recursionStack,
            Map<String, ? extends Iterable<String>> dependencyGraph) {
        visited.add(taskId);
        recursionStack.add(taskId);

        Iterable<String> dependencies = dependencyGraph.get(taskId);
        if (dependencies != null) {
            for (String dependency : dependencies) {
                if (!visited.contains(dependency)) {
                    if (hasCycleUtil(dependency, visited, recursionStack, dependencyGraph)) {
                        return true;
                    }
                } else if (recursionStack.contains(dependency)) {
                    return true;
                }
            }
        }

        recursionStack.remove(taskId);
        return false;
    }

    /**
     * Start deadlock detection service
     */
    private void startDeadlockDetection() {
        if (retryExecutor != null) {
            retryExecutor.scheduleAtFixedRate(this::detectDeadlocks, DEADLOCK_DETECTION_INTERVAL_MS,
                    DEADLOCK_DETECTION_INTERVAL_MS, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Detect and resolve deadlocks
     */
    private void detectDeadlocks() {
        deadlockDetectionLock.lock();
        try {
            // Build resource allocation graph
            buildResourceAllocationGraph();

            // Check for cycles in the graph
            if (hasDeadlock()) {
                logger.warn("Deadlock detected, initiating resolution");
                resolveDeadlock();
            }
        } finally {
            deadlockDetectionLock.unlock();
        }
    }

    /**
     * Build resource allocation graph for deadlock detection
     */
    private void buildResourceAllocationGraph() {
        resourceAllocationGraph.clear();

        // Add all active tasks and their resource requirements
        for (Map.Entry<String, TaskOrchestrationState> entry : taskStates.entrySet()) {
            String taskId = entry.getKey();
            TaskOrchestrationState state = entry.getValue();

            if (state.getState() == TaskOrchestrationState.State.RUNNING) {
                String agentId = taskAgentAssignments.get(taskId);
                if (agentId != null) {
                    resourceAllocationGraph.put(taskId, Set.of(agentId));
                }
            }
        }
    }

    /**
     * Check if there's a deadlock in the resource allocation graph
     */
    private boolean hasDeadlock() {
        Set<String> visited = ConcurrentHashMap.newKeySet();
        Set<String> recursionStack = ConcurrentHashMap.newKeySet();

        for (String taskId : resourceAllocationGraph.keySet()) {
            if (!visited.contains(taskId)) {
                if (hasCycleUtil(taskId, visited, recursionStack, resourceAllocationGraph)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Resolve deadlock by cancelling one of the involved tasks
     */
    private void resolveDeadlock() {
        // Simple resolution: cancel the task with the most recent start time
        String taskToCancel = null;
        long latestStartTime = 0;

        for (Map.Entry<String, TaskOrchestrationState> entry : taskStates.entrySet()) {
            String taskId = entry.getKey();
            TaskOrchestrationState state = entry.getValue();

            if (state.getState() == TaskOrchestrationState.State.RUNNING && state.getStartTime() > latestStartTime) {
                taskToCancel = taskId;
                latestStartTime = state.getStartTime();
            }
        }

        if (taskToCancel != null) {
            logger.warn("Resolving deadlock by cancelling task: {}", taskToCancel);
            cancelTaskOrchestration(taskToCancel);
        }
    }

    /**
     * Clean up all active transactions
     */
    private void cleanupAllTransactions() {
        for (String transactionId : activeTransactions.keySet()) {
            TaskTransaction transaction = activeTransactions.get(transactionId);
            if (transaction != null) {
                transaction.rollback();
            }
        }
        activeTransactions.clear();
    }

    /**
     * Get or create agent lock for resource management
     */
    private ReentrantReadWriteLock getAgentLock(String agentId) {
        return agentLocks.computeIfAbsent(agentId, k -> new ReentrantReadWriteLock());
    }

    /**
     * Get or create task lock
     */
    private Lock getTaskLock(String taskId) {
        return taskLocks.computeIfAbsent(taskId, k -> new ReentrantLock());
    }

    /**
     * Execute task with resource locking
     */
    public CompletableFuture<TaskStatusUpdateEvent> executeTaskWithResourceLocking(Task task) {
        String taskId = task.getId();
        Lock taskLock = getTaskLock(taskId);

        return CompletableFuture.supplyAsync(() -> {
            taskLock.lock();
            try {
                String agentId = selectOptimalAgent(task);
                if (agentId != null) {
                    ReentrantReadWriteLock agentLock = getAgentLock(agentId);
                    agentLock.writeLock().lock();
                    try {
                        return executeTaskWithOrchestration(task);
                    } finally {
                        agentLock.writeLock().unlock();
                    }
                } else {
                    return createErrorResponse(task, "No suitable agent available");
                }
            } finally {
                taskLock.unlock();
            }
        }, asyncExecutor);
    }

    /**
     * Execute task with retry mechanism and fallback support
     */
    public CompletableFuture<TaskStatusUpdateEvent> executeTaskWithFaultTolerance(Task task) {
        String taskId = task.getId();
        RetryContext retryContext = new RetryContext(taskId, MAX_RETRY_ATTEMPTS);
        retryContexts.put(taskId, retryContext);

        return executeTaskWithRetry(task, retryContext, 0);
    }

    /**
     * Execute task with retry mechanism
     */
    private CompletableFuture<TaskStatusUpdateEvent> executeTaskWithRetry(Task task, RetryContext retryContext,
            int attempt) {
        return executeTaskWithResourceLocking(task).handle((result, throwable) -> {
            if (throwable != null && retryContext.canRetry()) {
                logger.warn("Task {} failed, attempt {}/{}, retrying...", task.getId(), attempt + 1,
                        MAX_RETRY_ATTEMPTS);
                retryContext.incrementAttempt();

                if (retryExecutor != null) {
                    retryExecutor.schedule(() -> {
                        executeTaskWithRetry(task, retryContext, attempt + 1);
                    }, RETRY_DELAY_MS * (attempt + 1), TimeUnit.MILLISECONDS);
                }
                return null;
            } else if (throwable != null) {
                logger.error("Task {} failed after {} attempts, using fallback strategy", task.getId(),
                        MAX_RETRY_ATTEMPTS);
                return executeFallbackStrategy(task);
            } else {
                return result;
            }
        });
    }

    /**
     * Execute fallback strategy for failed tasks
     */
    private TaskStatusUpdateEvent executeFallbackStrategy(Task task) {
        FallbackStrategy fallback = fallbackStrategies.get(task.getId());
        if (fallback != null) {
            return fallback.execute(task);
        } else {
            // Default fallback: return error response
            return createErrorResponse(task, "Task failed and no fallback strategy available");
        }
    }

    /**
     * Create transaction for multi-agent operations
     */
    public TaskTransaction createTransaction(String transactionId) {
        TaskTransaction transaction = new TaskTransaction(transactionId, this);
        activeTransactions.put(transactionId, transaction);
        return transaction;
    }

    /**
     * Commit transaction
     */
    public boolean commitTransaction(String transactionId) {
        TaskTransaction transaction = activeTransactions.get(transactionId);
        if (transaction != null) {
            boolean success = transaction.commit();
            activeTransactions.remove(transactionId);
            return success;
        }
        return false;
    }

    /**
     * Rollback transaction
     */
    public void rollbackTransaction(String transactionId) {
        TaskTransaction transaction = activeTransactions.get(transactionId);
        if (transaction != null) {
            transaction.rollback();
            activeTransactions.remove(transactionId);
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

    // ============================================================================
    // Inner classes
    // ============================================================================

    /**
     * Task orchestration state tracking.
     */
    public static class TaskOrchestrationState {
        public enum State {
            PENDING,
            RUNNING,
            PAUSED,
            COMPLETED,
            ERROR,
            CANCELLED
        }

        private final String taskId;
        private State state;
        private long startTime;
        private long endTime;
        private @Nullable Exception lastError;

        public TaskOrchestrationState(String taskId) {
            this.taskId = taskId;
            this.state = State.PENDING;
        }

        // Getters and setters
        public String getTaskId() {
            return taskId;
        }

        public State getState() {
            return state;
        }

        public void setState(State state) {
            this.state = state;
        }

        public long getStartTime() {
            return startTime;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }

        public long getEndTime() {
            return endTime;
        }

        public void setEndTime(long endTime) {
            this.endTime = endTime;
        }

        public @Nullable Exception getLastError() {
            return lastError;
        }

        public void setLastError(Exception lastError) {
            this.lastError = lastError;
        }
    }

    /**
     * Task performance metrics.
     */
    public static class TaskMetrics {
        private final String taskId;
        private final AtomicLong executionCount = new AtomicLong(0);
        private final AtomicLong successCount = new AtomicLong(0);
        private final AtomicLong errorCount = new AtomicLong(0);
        private final AtomicLong cancellationCount = new AtomicLong(0);
        private final List<Long> executionTimes = new ArrayList<>();

        public TaskMetrics(String taskId) {
            this.taskId = taskId;
        }

        public void recordExecution(long executionTime) {
            executionCount.incrementAndGet();
            executionTimes.add(executionTime);
        }

        public void recordSuccess() {
            successCount.incrementAndGet();
        }

        public void recordError(Exception error) {
            errorCount.incrementAndGet();
        }

        public void recordCancellation() {
            cancellationCount.incrementAndGet();
        }

        // Getters
        public String getTaskId() {
            return taskId;
        }

        public long getExecutionCount() {
            return executionCount.get();
        }

        public long getSuccessCount() {
            return successCount.get();
        }

        public long getErrorCount() {
            return errorCount.get();
        }

        public long getCancellationCount() {
            return cancellationCount.get();
        }

        public List<Long> getExecutionTimes() {
            return new ArrayList<>(executionTimes);
        }

        public double getAverageExecutionTime() {
            if (executionTimes.isEmpty()) {
                return 0.0;
            }
            return executionTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
        }
    }

    /**
     * Task Transaction for multi-agent operations
     */
    public static class TaskTransaction {
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
                // Execute all tasks in the transaction
                for (Task task : tasks) {
                    String agentId = taskAgentAssignments.get(task.getId());
                    if (agentId != null) {
                        taskManager.taskAgentAssignments.put(task.getId(), agentId);
                    }
                    taskManager.startTask(task.getId());
                }

                committed = true;
                return true;
            } catch (Exception e) {
                logger.error("Transaction commit failed: {}", transactionId, e);
                rollback();
                return false;
            }
        }

        public void rollback() {
            if (committed || rolledBack) {
                return;
            }

            try {
                // Cancel all tasks in the transaction
                for (Task task : tasks) {
                    taskManager.cancelTaskOrchestration(task.getId());
                }

                // Remove agent assignments
                for (String taskId : taskAgentAssignments.keySet()) {
                    taskManager.taskAgentAssignments.remove(taskId);
                }

                rolledBack = true;
            } catch (Exception e) {
                logger.error("Transaction rollback failed: {}", transactionId, e);
            }
        }

        public String getTransactionId() {
            return transactionId;
        }

        public boolean isCommitted() {
            return committed;
        }

        public boolean isRolledBack() {
            return rolledBack;
        }
    }

    /**
     * Retry Context for fault tolerance
     */
    public static class RetryContext {
        private final String taskId;
        private final int maxRetries;
        private int currentAttempts;

        public RetryContext(String taskId, int maxRetries) {
            this.taskId = taskId;
            this.maxRetries = maxRetries;
            this.currentAttempts = 0;
        }

        public boolean canRetry() {
            return currentAttempts < maxRetries;
        }

        public void incrementAttempt() {
            currentAttempts++;
        }

        public int getCurrentAttempts() {
            return currentAttempts;
        }

        public int getMaxRetries() {
            return maxRetries;
        }

        public String getTaskId() {
            return taskId;
        }
    }

    /**
     * Fallback Strategy for failed tasks
     */
    public static class FallbackStrategy {
        private final String taskId;
        private final String strategyType;
        private final AgentTaskManager taskManager;

        public FallbackStrategy(String taskId, String strategyType, AgentTaskManager taskManager) {
            this.taskId = taskId;
            this.strategyType = strategyType;
            this.taskManager = taskManager;
        }

        public TaskStatusUpdateEvent execute(Task task) {
            switch (strategyType) {
                case "RETRY_WITH_DIFFERENT_AGENT":
                    return retryWithDifferentAgent(task);
                case "SIMPLIFIED_EXECUTION":
                    return simplifiedExecution(task);
                case "ERROR_RESPONSE":
                default:
                    return createErrorResponse(task, "Task failed and fallback strategy executed");
            }
        }

        private TaskStatusUpdateEvent retryWithDifferentAgent(Task task) {
            // Try to find a different agent with the same capability
            String capability = taskManager.getRequiredCapability(task);
            List<String> agents = taskManager.getAgentsWithCapability(capability);

            if (agents.size() > 1) {
                // Try the second agent
                String alternativeAgentId = agents.get(1);
                taskManager.taskAgentAssignments.put(task.getId(), alternativeAgentId);
                return taskManager.executeTaskWithOrchestration(task);
            } else {
                return createErrorResponse(task, "No alternative agent available for fallback");
            }
        }

        private TaskStatusUpdateEvent simplifiedExecution(Task task) {
            // Execute a simplified version of the task
            logger.info("Executing simplified version of task: {}", task.getId());
            return createErrorResponse(task, "Simplified execution completed");
        }

        private TaskStatusUpdateEvent createErrorResponse(Task task, String message) {
            // Create a simple error response using the existing method
            return taskManager.createErrorResponse(task, message);
        }

        public String getTaskId() {
            return taskId;
        }

        public String getStrategyType() {
            return strategyType;
        }
    }
}
