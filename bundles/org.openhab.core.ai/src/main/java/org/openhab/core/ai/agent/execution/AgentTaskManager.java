package org.openhab.core.ai.agent.execution;

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
import org.openhab.core.ai.action.ActionRegistry;
import org.openhab.core.ai.action.api.Action;
import org.openhab.core.ai.action.api.ActionContext;
import org.openhab.core.ai.action.api.ActionError;
import org.openhab.core.ai.action.api.ActionResult;
import org.openhab.core.ai.agent.core.MessageType;
import org.openhab.core.ai.agent.infrastructure.persistence.AgentPersistenceManager;
import org.openhab.core.ai.agent.infrastructure.synchronization.ConcurrentAgentSynchronizationManager;
import org.openhab.core.ai.agent.lifecycle.api.AgentRegistry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.a2a.server.tasks.TaskStore;
import io.a2a.spec.EventKind;
import io.a2a.spec.JSONRPCError;
import io.a2a.spec.Message;
import io.a2a.spec.MessageSendParams;
import io.a2a.spec.Part;
import io.a2a.spec.Task;
import io.a2a.spec.TaskQueryParams;
import io.a2a.spec.TaskState;
import io.a2a.spec.TaskStatus;
import io.a2a.spec.TaskStatusUpdateEvent;
import io.a2a.spec.TextPart;

/**
 * Task Orchestration and Lifecycle Management.
 * 
 * <p>
 * <strong>Primary Responsibility:</strong> Task Orchestration and Lifecycle Management
 * </p>
 * 
 * <p>
 * <strong>Concerns:</strong>
 * <ul>
 * <li><strong>Task Creation:</strong> Creates tasks from A2A messages</li>
 * <li><strong>Task Lifecycle:</strong> Manages task states (pending, running, completed, failed)</li>
 * <li><strong>Task Routing:</strong> Routes tasks to appropriate executors</li>
 * <li><strong>Dependency Management:</strong> Manages task dependencies</li>
 * <li><strong>Task Persistence:</strong> Stores and retrieves task information</li>
 * <li><strong>Task Metrics:</strong> Tracks task execution metrics</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>What this class DOES:</strong>
 * <ul>
 * <li>Creates tasks from A2A messages via {@link #handleMessageSend(MessageSendParams)}</li>
 * <li>Manages task lifecycle and state transitions</li>
 * <li>Routes tasks to {@link AgentTaskExecutor} for execution</li>
 * <li>Manages task dependencies and execution order</li>
 * <li>Stores and retrieves task information</li>
 * <li>Tracks task execution metrics and statistics</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>What this class DOES NOT do:</strong>
 * <ul>
 * <li>❌ Execute tasks directly (delegates to AgentTaskExecutor)</li>
 * <li>❌ Handle protocol communication (delegates to AgentProtocolHandler)</li>
 * <li>❌ Manage skill registration (delegates to AgentSkillRegistry)</li>
 * <li>❌ Handle authentication (delegates to security manager)</li>
 * <li>❌ Execute business logic (delegates to execution layers)</li>
 * <li>❌ Handle A2A SDK compliance (delegates to AgentTaskExecutor)</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>Boundary Conditions:</strong>
 * <ul>
 * <li>Only manages task lifecycle and orchestration</li>
 * <li>Delegates execution to AgentTaskExecutor</li>
 * <li>Does not contain business logic</li>
 * <li>Does not handle protocol communication</li>
 * <li>Does not manage skill registration</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>Dependencies:</strong>
 * <ul>
 * <li>{@link AgentTaskExecutor}: For task execution</li>
 * <li>{@link AgentSkillManager}: For skill execution</li>
 * <li>Task storage/retrieval mechanisms</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>Architecture Layer:</strong> Task Management Layer
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
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
    private @Nullable ConcurrentAgentSynchronizationManager synchronizationService;

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
                return tasks.get(taskId);
            }

            @Override
            public void delete(@Nullable String taskId) {
                logger.debug("Deleting task: {}", taskId);
                tasks.remove(taskId);
            }
        };
    }

    // ============================================================================
    // Public API Methods - Single Task Execution (from original A2ATaskManager)
    // ============================================================================

    public Task getTask(String taskId) throws JSONRPCError {
        logger.debug("Getting task: {}", taskId);

        if (taskId == null || taskId.trim().isEmpty()) {
            throw new JSONRPCError(-32602, "Task ID cannot be null or empty", null);
        }

        TaskStore store = taskStore;
        if (store != null) {
            Task task = store.get(taskId);
            if (task != null) {
                return task;
            }
        }

        throw new JSONRPCError(-32601, "Task not found: " + taskId, null);
    }

    public Task cancelTask(String taskId) throws JSONRPCError {
        logger.debug("Cancelling task: {}", taskId);

        if (taskId == null || taskId.trim().isEmpty()) {
            throw new JSONRPCError(-32602, "Task ID cannot be null or empty", null);
        }

        TaskOrchestrationState state = taskStates.get(taskId);
        if (state == null) {
            throw new JSONRPCError(-32601, "Task not found: " + taskId, null);
        }

        // Update task state
        state.setState(TaskOrchestrationStateState.CANCELLED);
        state.setEndTime(System.currentTimeMillis());

        // Record cancellation in metrics
        TaskMetrics metrics = taskMetrics.get(taskId);
        if (metrics != null) {
            metrics.recordCancellation();
        }

        // Get the task from store
        TaskStore store = taskStore;
        if (store != null) {
            Task task = store.get(taskId);
            if (task != null) {
                // Update task status to cancelled (use available constructor)
                TaskStatus cancelledStatus = new TaskStatus(TaskState.CANCELED);
                // Preserve existing fields we have access to; use empty lists for attachments/messages
                Task cancelledTask = new Task(task.getId(), task.getContextId(), cancelledStatus, new ArrayList<>(),
                        new ArrayList<>(), task.getMetadata(), "task");

                // Save updated task
                store.save(cancelledTask);

                logger.info("Task cancelled successfully: {}", taskId);
                return cancelledTask;
            }
        }

        throw new JSONRPCError(-32601, "Task not found in store: " + taskId, null);
    }

    /**
     * List tasks with optional filtering and pagination.
     * 
     * @param params the query parameters for task listing
     * @return list of tasks matching the criteria
     * @throws JSONRPCError if there's an error listing tasks
     */
    public List<Task> listTasks(@Nullable TaskQueryParams params) throws JSONRPCError {
        // TODO(openHAB AI): Implement server-side filtering and pagination for tasks/list
        // - The A2A SDK's TaskQueryParams currently only offers historyLength for single-task queries
        // - For compliance, keep tasks/list unfiltered; layering options:
        // 1) Introduce an internal DTO (e.g., org.openhab...ListTasksParams) consumed by a non-SDK endpoint
        // 2) Apply filtering in AgentProtocolHandler before returning to clients
        // 3) Revisit when the SDK exposes rich list query params and wire those getters here
        // Tracked in doc/BRAIN_PLAN.md under "Implement server-side filtering for tasks/list (AgentTaskManager)"
        logger.debug("Listing tasks with params: {}", params);

        try {
            TaskStore store = taskStore;
            if (store == null) {
                throw new JSONRPCError(-32603, "Internal error: TaskStore not available", null);
            }

            // Get all tasks from the store
            List<Task> allTasks = getAllTasksFromStore(store);

            logger.debug("Returning {} tasks", allTasks.size());
            return allTasks;

        } catch (Exception e) {
            logger.error("Error listing tasks", e);
            throw new JSONRPCError(-32603, "Internal error listing tasks: " + e.getMessage(), null);
        }
    }

    /**
     * List tasks with server-side filtering and pagination using internal params.
     * This does not alter SDK surface; callers within openHAB can use this for UI/API needs.
     */
    public List<Task> listTasksFiltered(ListTasksParams filter) throws JSONRPCError {
        try {
            TaskStore store = taskStore;
            if (store == null) {
                throw new JSONRPCError(-32603, "Internal error: TaskStore not available", null);
            }

            List<Task> all = getAllTasksFromStore(store);

            // Apply filters
            List<Task> filtered = new ArrayList<>();
            for (Task t : all) {
                if (!matchesFilters(t, filter)) {
                    continue;
                }
                filtered.add(t);
            }

            // Pagination
            int fromIndex = Math.min(filter.offset(), filtered.size());
            int toIndex = Math.min(fromIndex + filter.limit(), filtered.size());
            return filtered.subList(fromIndex, toIndex);
        } catch (Exception e) {
            logger.error("Error listing tasks (filtered)", e);
            throw new JSONRPCError(-32603, "Internal error listing tasks: " + e.getMessage(), null);
        }
    }

    private boolean matchesFilters(Task task, ListTasksParams filter) {
        // Status
        if (filter.status() != null && task.getStatus() != null && task.getStatus().state() != filter.status()) {
            return false;
        }

        // Agent assignment
        if (filter.agentId() != null && !filter.agentId().isEmpty()) {
            String assigned = taskAgentAssignments.get(task.getId());
            if (assigned == null || !assigned.equals(filter.agentId())) {
                return false;
            }
        }

        // Skill
        if (filter.skillId() != null && !filter.skillId().isEmpty()) {
            String skill = extractSkillId(task);
            if (skill == null || !filter.skillId().equals(skill)) {
                return false;
            }
        }

        // Created time window from metadata
        Long created = null;
        Map<String, Object> md = task.getMetadata();
        if (md != null && md.get("created") instanceof Number) {
            created = ((Number) md.get("created")).longValue();
        }
        if (filter.createdAfter() != null && created != null && created <= filter.createdAfter()) {
            return false;
        }
        if (filter.createdBefore() != null && created != null && created >= filter.createdBefore()) {
            return false;
        }

        return true;
    }

    /**
     * Get all tasks from the task store.
     * 
     * @param store the task store
     * @return list of all tasks
     */
    private List<Task> getAllTasksFromStore(TaskStore store) {
        // Since TaskStore doesn't have a list method, we need to implement this
        // For now, we'll return tasks from our in-memory state
        List<Task> tasks = new ArrayList<>();

        for (String taskId : taskStates.keySet()) {
            Task task = store.get(taskId);
            if (task != null) {
                tasks.add(task);
            }
        }

        return tasks;
    }

    /**
     * Extract skill ID from task metadata.
     * 
     * @param task the task
     * @return the skill ID or null if not found
     */
    private String extractSkillId(Task task) {
        if (task.getMetadata() != null && task.getMetadata().containsKey("skillId")) {
            Object skillIdObj = task.getMetadata().get("skillId");
            return skillIdObj != null ? skillIdObj.toString() : null;
        }
        return null;
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
                state.setState(TaskOrchestrationStateState.RUNNING);
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
            if (state != null && state.getState() == TaskOrchestrationStateState.RUNNING) {
                state.setState(TaskOrchestrationStateState.PAUSED);
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
            if (state != null && state.getState() == TaskOrchestrationStateState.PAUSED) {
                state.setState(TaskOrchestrationStateState.RUNNING);
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
                state.setState(TaskOrchestrationStateState.CANCELLED);

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
                state.setState(TaskOrchestrationStateState.ERROR);
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
            if (state != null && state.getState() == TaskOrchestrationStateState.ERROR) {
                // TODO: Implement recovery logic
                // This could involve retrying with different parameters, using fallback agents, etc.

                state.setState(TaskOrchestrationStateState.RUNNING);
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

    private String extractSkillIdFromMessage(Message message) {
        String content = extractTextContent(message);
        if (content != null && content.contains(" ")) {
            return content.split(" ")[0];
        }
        return "default";
    }

    private String extractTextContent(Message message) {
        if (message.getParts() != null) {
            StringBuilder textBuilder = new StringBuilder();
            for (Part<?> part : message.getParts()) {
                if (part instanceof TextPart textPart) {
                    textBuilder.append(textPart.getText());
                }
            }
            return textBuilder.toString();
        }
        return "";
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

    TaskStatusUpdateEvent executeTaskWithOrchestration(Task task) {
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
            ConcurrentAgentSynchronizationManager syncService = synchronizationService;
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

    TaskStatusUpdateEvent createErrorResponse(Task task, String errorMessage) {
        // TODO: Create proper error response using A2A SDK
        return new TaskStatusUpdateEvent(task.getId(), new TaskStatus(TaskState.FAILED), task.getContextId(), true,
                null);
    }

    String getRequiredCapability(Task task) {
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
    List<String> getAgentsWithCapability(String capability) {
        AgentRegistry registry = agentRegistry;
        if (registry != null) {
            // Use a default user ID for capability lookup
            return registry.findAgentsWithCapability(capability, "system");
        }
        return List.of();
    }

    // =========================================================================
    // Package-private accessors for extracted helpers
    // =========================================================================

    Logger getLogger() {
        return logger;
    }

    void assignAgentToTask(String taskId, String agentId) {
        taskAgentAssignments.put(taskId, agentId);
    }

    void removeAgentAssignment(String taskId) {
        taskAgentAssignments.remove(taskId);
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

            if (state.getState() == TaskOrchestrationStateState.RUNNING) {
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

            if (state.getState() == TaskOrchestrationStateState.RUNNING && state.getStartTime() > latestStartTime) {
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
    // (removed: extracted to top-level TaskOrchestrationState)

    /**
     * Task performance metrics.
     */
    // (removed: extracted to top-level TaskMetrics)

    /**
     * Task Transaction for multi-agent operations
     */
    // (removed: extracted to top-level TaskTransaction)

    /**
     * Retry Context for fault tolerance
     */
    // (removed: extracted to top-level RetryContext)

    /**
     * Fallback Strategy for failed tasks
     */
    // (removed: extracted to top-level FallbackStrategy)

    // ============================================================================
    // Message Type Detection and Handling
    // ============================================================================

    /**
     * Detect the type of message based on content and metadata.
     * 
     * @param message the A2A message to analyze
     * @return the detected message type
     */
    private MessageType detectMessageType(Message message) {
        String content = extractTextContent(message);
        Map<String, Object> metadata = message.getMetadata();

        // Check metadata for explicit type
        if (metadata != null && metadata.containsKey("messageType")) {
            try {
                return MessageType.valueOf(metadata.get("messageType").toString().toUpperCase());
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid messageType in metadata: {}", metadata.get("messageType"));
            }
        }

        // Infer from content
        if (content == null || content.trim().isEmpty()) {
            return MessageType.QUERY; // Default to query for empty messages
        }

        String lowerContent = content.toLowerCase().trim();

        // Discovery patterns
        if (lowerContent.startsWith("discover_") || lowerContent.startsWith("get_agent_capabilities")
                || lowerContent.startsWith("find_agents") || lowerContent.contains("capabilities")) {
            return MessageType.DISCOVERY;
        }

        // Query patterns
        if (lowerContent.startsWith("get_") || lowerContent.startsWith("list_") || lowerContent.startsWith("ping")
                || lowerContent.startsWith("status") || lowerContent.startsWith("info")
                || lowerContent.startsWith("query")) {
            return MessageType.QUERY;
        }

        // Control patterns
        if (lowerContent.startsWith("cancel_") || lowerContent.startsWith("pause_")
                || lowerContent.startsWith("resume_") || lowerContent.startsWith("stop_")
                || lowerContent.startsWith("abort_")) {
            return MessageType.CONTROL;
        }

        // Execution patterns
        if (lowerContent.startsWith("execute_") || lowerContent.startsWith("perform_")
                || lowerContent.startsWith("run_") || lowerContent.startsWith("start_")
                || lowerContent.startsWith("trigger_") || lowerContent.startsWith("action:")) {
            return MessageType.EXECUTION;
        }

        // Notification patterns
        if (lowerContent.startsWith("notify_") || lowerContent.startsWith("alert_")
                || lowerContent.startsWith("event_")) {
            return MessageType.NOTIFICATION;
        }

        // Default to query for safety
        logger.debug("Could not determine message type for content: '{}', defaulting to QUERY", content);
        return MessageType.QUERY;
    }

    /**
     * Handle discovery messages without creating execution tasks.
     * 
     * @param task the A2A SDK Task
     * @return the event kind response
     */
    private EventKind handleDiscoveryMessage(Task task) {
        Map<String, Object> metadata = task.getMetadata();
        String content = "";
        if (metadata != null && metadata.containsKey("rawContent")) {
            content = metadata.get("rawContent").toString();
        }
        logger.debug("Handling discovery message for task {}: {}", task.getId(), content);

        // Handle different discovery requests
        if (content.toLowerCase().contains("capabilities")) {
            return new EventKind() {
                @Override
                public String getKind() {
                    return "capabilities.discovered";
                }
            };
        } else if (content.toLowerCase().contains("agents")) {
            return new EventKind() {
                @Override
                public String getKind() {
                    return "agents.discovered";
                }
            };
        }

        return new EventKind() {
            @Override
            public String getKind() {
                return "discovery.completed";
            }
        };
    }

    /**
     * Handle query messages without creating execution tasks.
     * 
     * @param task the A2A SDK Task
     * @return the event kind response
     */
    private EventKind handleQueryMessage(Task task) {
        Map<String, Object> metadata = task.getMetadata();
        String content = "";
        if (metadata != null && metadata.containsKey("rawContent")) {
            content = metadata.get("rawContent").toString();
        }
        logger.debug("Handling query message for task {}: {}", task.getId(), content);

        String lowerContent = content.toLowerCase();

        // Handle different query types
        if (lowerContent.startsWith("get_task_status")) {
            return new EventKind() {
                @Override
                public String getKind() {
                    return "task.status.returned";
                }
            };
        } else if (lowerContent.startsWith("list_available_skills")) {
            return new EventKind() {
                @Override
                public String getKind() {
                    return "skills.listed";
                }
            };
        } else if (lowerContent.startsWith("ping")) {
            return new EventKind() {
                @Override
                public String getKind() {
                    return "pong";
                }
            };
        } else if (lowerContent.startsWith("get_system_info")) {
            return new EventKind() {
                @Override
                public String getKind() {
                    return "system.info.returned";
                }
            };
        }

        return new EventKind() {
            @Override
            public String getKind() {
                return "query.processed";
            }
        };
    }

    /**
     * Handle control messages for task and system control.
     * 
     * @param task the A2A SDK Task
     * @return the event kind response
     */
    private EventKind handleControlMessage(Task task) {
        Map<String, Object> metadata = task.getMetadata();
        String content = "";
        if (metadata != null && metadata.containsKey("rawContent")) {
            content = metadata.get("rawContent").toString();
        }
        logger.debug("Handling control message for task {}: {}", task.getId(), content);

        String lowerContent = content.toLowerCase();

        // Extract task ID from control message
        String taskId = extractTaskIdFromControlMessage(content);

        if (lowerContent.startsWith("cancel_task")) {
            if (taskId != null) {
                try {
                    cancelTask(taskId);
                    return new EventKind() {
                        @Override
                        public String getKind() {
                            return "task.cancelled";
                        }
                    };
                } catch (JSONRPCError e) {
                    logger.error("Failed to cancel task: {}", taskId, e);
                    return new EventKind() {
                        @Override
                        public String getKind() {
                            return "task.cancel.failed";
                        }
                    };
                }
            }
        } else if (lowerContent.startsWith("pause_task")) {
            if (taskId != null) {
                pauseTask(taskId);
                return new EventKind() {
                    @Override
                    public String getKind() {
                        return "task.paused";
                    }
                };
            }
        } else if (lowerContent.startsWith("resume_task")) {
            if (taskId != null) {
                resumeTask(taskId);
                return new EventKind() {
                    @Override
                    public String getKind() {
                        return "task.resumed";
                    }
                };
            }
        }

        return new EventKind() {
            @Override
            public String getKind() {
                return "control.processed";
            }
        };
    }

    /**
     * Handle notification messages for event notifications.
     * 
     * @param task the A2A SDK Task
     * @return the event kind response
     */
    private EventKind handleNotificationMessage(Task task) {
        Map<String, Object> metadata = task.getMetadata();
        String content = "";
        if (metadata != null && metadata.containsKey("rawContent")) {
            content = metadata.get("rawContent").toString();
        }
        logger.debug("Handling notification message for task {}: {}", task.getId(), content);

        return new EventKind() {
            @Override
            public String getKind() {
                return "notification.received";
            }
        };
    }

    /**
     * Handle execution messages by executing the provided task.
     * 
     * @param task the A2A SDK Task to execute
     * @return the event kind response
     */
    private EventKind handleExecutionMessage(Task task) {
        logger.debug("Handling execution message for task: {}", task.getId());

        try {
            // Save the task if we have a task store
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

                    // Extract action information from task metadata
                    Map<String, Object> metadata = task.getMetadata();
                    String actionId = "";
                    Map<String, Object> parameters = new HashMap<>();

                    if (metadata != null) {
                        if (metadata.containsKey("skillId")) {
                            actionId = metadata.get("skillId").toString();
                        }
                        if (metadata.containsKey("parameters")) {
                            Object paramsObj = metadata.get("parameters");
                            if (paramsObj instanceof Map) {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> paramsMap = (Map<String, Object>) paramsObj;
                                parameters.putAll(paramsMap);
                            }
                        }
                    }

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
            logger.error("Error handling execution message", e);
            throw new JSONRPCError(-32603, "Internal error: " + e.getMessage(), null);
        }
    }

    /**
     * Extract task ID from control message content.
     * 
     * @param content the message content
     * @return the task ID or null if not found
     */
    private @Nullable String extractTaskIdFromControlMessage(String content) {
        if (content == null || content.trim().isEmpty()) {
            return null;
        }

        // Look for task ID patterns in control messages
        String[] parts = content.split("\\s+");
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].startsWith("task") || parts[i].matches("^[a-zA-Z0-9_-]+$")) {
                return parts[i];
            }
        }

        return null;
    }

    // ============================================================================
    // Protocol Conversion Methods
    // ============================================================================

    /**
     * Convert A2A Message to A2A SDK Task.
     * 
     * <p>
     * This method extracts all necessary information from an A2A Message
     * and creates an A2A SDK Task that can be used throughout
     * the execution chain. The A2A SDK Task is already protocol-agnostic
     * and serves as the standard data structure for task information.
     * </p>
     * 
     * @param message the A2A message to convert
     * @return the A2A SDK Task
     */
    private Task convertMessageToTask(Message message) {
        // Generate unique task ID and context ID
        String taskId = "task_" + System.currentTimeMillis() + "_" + System.nanoTime();
        String contextId = "context_" + System.currentTimeMillis();

        // Extract skill ID from message content or metadata
        String skillId = extractSkillId(message);

        // Extract parameters from message parts and metadata
        Map<String, Object> parameters = extractParameters(message);

        // Create metadata for the task
        Map<String, Object> metadata = new HashMap<>();
        if (message.getMetadata() != null) {
            metadata.putAll(message.getMetadata());
        }
        metadata.put("skillId", skillId);
        metadata.put("parameters", parameters);
        metadata.put("originalMessageHash", String.valueOf(message.hashCode()));
        metadata.put("conversionTimestamp", System.currentTimeMillis());

        // Detect message type and store it
        MessageType messageType = detectMessageType(message);
        metadata.put("messageType", messageType.name());

        // Create initial task status
        TaskStatus initialStatus = new TaskStatus(TaskState.SUBMITTED);

        logger.debug("Converting A2A message to A2A SDK Task: id={}, skillId={}, type={}", taskId, skillId,
                messageType);

        // Create and return A2A SDK Task
        return new Task(taskId, contextId, initialStatus, new ArrayList<>(), List.of(message), metadata, "task");
    }

    /**
     * Extract skill ID from A2A message.
     * 
     * @param message the A2A message
     * @return the skill ID
     */
    private String extractSkillId(Message message) {
        // First check metadata for explicit skill ID
        if (message.getMetadata() != null && message.getMetadata().containsKey("skillId")) {
            Object skillIdObj = message.getMetadata().get("skillId");
            if (skillIdObj instanceof String) {
                return (String) skillIdObj;
            }
        }

        // Extract from message content
        String content = extractTextContent(message);
        if (content != null && !content.trim().isEmpty()) {
            // Try to extract skill ID from content (e.g., "execute_skill:turn_on_light")
            String[] parts = content.split(":", 2);
            if (parts.length > 1 && parts[0].trim().equalsIgnoreCase("execute_skill")) {
                return parts[1].trim();
            }
        }

        // Default to a generic skill ID
        return "default_skill";
    }

    /**
     * Extract parameters from A2A message.
     * 
     * @param message the A2A message
     * @return the parameters map
     */
    private Map<String, Object> extractParameters(Message message) {
        Map<String, Object> parameters = new HashMap<>();

        // Extract from message parts
        if (message.getParts() != null) {
            for (Part<?> part : message.getParts()) {
                if (part instanceof TextPart) {
                    String text = ((TextPart) part).getText();
                    if (text != null && !text.trim().isEmpty()) {
                        // Try to parse as JSON or key-value pairs
                        try {
                            // Simple key-value parsing for now
                            String[] lines = text.split("\n");
                            for (String line : lines) {
                                String[] kv = line.split("=", 2);
                                if (kv.length == 2) {
                                    parameters.put(kv[0].trim(), kv[1].trim());
                                }
                            }
                        } catch (Exception e) {
                            // If parsing fails, store as raw text
                            parameters.put("rawContent", text);
                        }
                    }
                }
            }
        }

        // Extract from metadata (excluding protocol-specific fields)
        if (message.getMetadata() != null) {
            for (Map.Entry<String, Object> entry : message.getMetadata().entrySet()) {
                String key = entry.getKey();
                if (!key.equals("skillId") && !key.equals("messageType")) {
                    parameters.put(key, entry.getValue());
                }
            }
        }

        return parameters;
    }

    // ============================================================================
    // Updated Message Handler
    // ============================================================================

    public EventKind handleMessageSend(MessageSendParams params) throws JSONRPCError {
        logger.debug("Handling message send request: {}", params);

        if (params == null) {
            throw new JSONRPCError(-32602, "MessageSendParams cannot be null", null);
        }

        try {
            // Convert A2A message to A2A SDK Task early
            Task task = convertMessageToTask(params.message());
            logger.debug("Converted A2A message to A2A SDK Task: {}", task.getId());

            // Get message type from task metadata
            Map<String, Object> metadata = task.getMetadata();
            String messageTypeStr = "";
            if (metadata != null && metadata.containsKey("messageType")) {
                messageTypeStr = metadata.get("messageType").toString();
            }

            // Handle message based on type using A2A SDK Task
            switch (messageTypeStr) {
                case "DISCOVERY":
                    return handleDiscoveryMessage(task);

                case "QUERY":
                    return handleQueryMessage(task);

                case "EXECUTION":
                    return handleExecutionMessage(task);

                case "CONTROL":
                    return handleControlMessage(task);

                case "NOTIFICATION":
                    return handleNotificationMessage(task);

                default:
                    logger.warn("Unknown message type: {}, defaulting to QUERY", messageTypeStr);
                    return handleQueryMessage(task);
            }
        } catch (Exception e) {
            logger.error("Error processing message send request", e);
            throw new JSONRPCError(-32603, "Internal error: " + e.getMessage(), null);
        }
    }
}
