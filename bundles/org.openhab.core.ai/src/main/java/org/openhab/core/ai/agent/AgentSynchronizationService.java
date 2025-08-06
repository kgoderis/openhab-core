/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.core.ai.agent;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.a2a.spec.Task;
import io.a2a.spec.TaskState;
import io.a2a.spec.TaskStatus;
import io.a2a.spec.TaskStatusUpdateEvent;

/**
 * A2A Synchronization Service for managing multi-agent task execution with dependency resolution,
 * resource locking, deadlock detection, and monitoring capabilities.
 *
 * @author Karel Goderis - Initial Contribution
 */
@Component(service = AgentSynchronizationService.class)
@NonNullByDefault
public class AgentSynchronizationService {

    private final Logger logger = LoggerFactory.getLogger(AgentSynchronizationService.class);

    // Resource locking for concurrent access
    private final Map<String, ReentrantLock> resourceLocks = new ConcurrentHashMap<>();
    private final Map<String, String> lockOwners = new ConcurrentHashMap<>();

    // Task dependency management
    private final Map<String, Set<String>> dependencyGraph = new ConcurrentHashMap<>();
    private final Map<String, TaskState> taskStates = new ConcurrentHashMap<>();

    // Monitoring and observability
    private final ScheduledExecutorService monitoringExecutor = Executors.newSingleThreadScheduledExecutor();
    private final Map<String, Long> taskStartTimes = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> taskRetryCounts = new ConcurrentHashMap<>();
    private final Map<String, List<String>> taskExecutionHistory = new ConcurrentHashMap<>();

    // Configuration
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final long defaultTimeoutMs = 30000; // 30 seconds
    private final int maxRetries = 3;
    private final long retryDelayMs = 1000; // 1 second

    @Activate
    public void activate() {
        logger.debug("Activating A2A Synchronization Service");
        isRunning.set(true);
        startMonitoring();
    }

    @Deactivate
    public void deactivate() {
        logger.debug("Deactivating A2A Synchronization Service");
        isRunning.set(false);
        stopMonitoring();
        releaseAllLocks();
    }

    /**
     * Execute tasks with dependency resolution
     */
    public CompletableFuture<List<TaskStatusUpdateEvent>> executeTasksWithDependencies(List<Task> tasks) {
        logger.debug("Executing {} tasks with dependency resolution", tasks.size());

        // Build dependency graph
        Map<String, Set<String>> graph = buildDependencyGraph(tasks);

        // Check for circular dependencies
        if (hasCircularDependency(graph)) {
            CompletableFuture<List<TaskStatusUpdateEvent>> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalStateException("Circular dependency detected in task graph"));
            return future;
        }

        // Execute tasks in dependency order
        return executeTasksInOrder(tasks, graph);
    }

    /**
     * Build dependency graph from tasks
     */
    private Map<String, Set<String>> buildDependencyGraph(List<Task> tasks) {
        Map<String, Set<String>> graph = new HashMap<>();

        for (Task task : tasks) {
            String taskId = task.getId();
            graph.putIfAbsent(taskId, new HashSet<>());

            // Add dependencies from task metadata
            @Nullable
            Object depsObj = task.getMetadata().get("dependencies");
            if (depsObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> dependencies = (List<String>) depsObj;
                for (String dependency : dependencies) {
                    graph.computeIfAbsent(dependency, k -> new HashSet<>()).add(taskId);
                }
            }
        }

        return graph;
    }

    /**
     * Execute tasks in dependency order
     */
    private CompletableFuture<List<TaskStatusUpdateEvent>> executeTasksInOrder(List<Task> tasks,
            Map<String, Set<String>> dependencyGraph) {

        List<TaskStatusUpdateEvent> responses = Collections.synchronizedList(new ArrayList<>());
        Map<String, CompletableFuture<TaskStatusUpdateEvent>> taskFutures = new ConcurrentHashMap<>();

        // Create futures for all tasks
        for (Task task : tasks) {
            taskFutures.put(task.getId(), new CompletableFuture<>());
        }

        // Execute tasks when dependencies are satisfied
        for (Task task : tasks) {
            executeTaskWhenReady(task, dependencyGraph, taskFutures, responses);
        }

        // Wait for all tasks to complete
        CompletableFuture<Void> allTasks = CompletableFuture
                .allOf(taskFutures.values().toArray(new CompletableFuture[0]));

        return allTasks.thenApply(v -> responses);
    }

    /**
     * Execute a task when its dependencies are satisfied
     */
    private void executeTaskWhenReady(Task task, Map<String, Set<String>> dependencyGraph,
            Map<String, CompletableFuture<TaskStatusUpdateEvent>> taskFutures, List<TaskStatusUpdateEvent> responses) {

        String taskId = task.getId();
        CompletableFuture<TaskStatusUpdateEvent> taskFuture = taskFutures.get(taskId);

        // Check if all dependencies are completed
        List<CompletableFuture<TaskStatusUpdateEvent>> dependencies = new ArrayList<>();
        @Nullable
        Object depsObj = task.getMetadata().get("dependencies");
        if (depsObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> taskDependencies = (List<String>) depsObj;
            for (String dependency : taskDependencies) {
                CompletableFuture<TaskStatusUpdateEvent> depFuture = taskFutures.get(dependency);
                if (depFuture != null) {
                    dependencies.add(depFuture);
                }
            }
        }

        if (dependencies.isEmpty()) {
            // No dependencies, execute immediately
            executeTask(task).thenAccept(response -> {
                taskFuture.complete(response);
                responses.add(response);
            }).exceptionally(throwable -> {
                taskFuture.completeExceptionally(throwable);
                return null;
            });
        } else {
            // Wait for dependencies to complete
            CompletableFuture.allOf(dependencies.toArray(new CompletableFuture[0])).thenRun(() -> {
                // Check if any dependency failed
                for (CompletableFuture<TaskStatusUpdateEvent> dep : dependencies) {
                    if (dep.isCompletedExceptionally()) {
                        taskFuture.completeExceptionally(new RuntimeException("Dependency failed for task: " + taskId));
                        return;
                    }
                }

                // Execute task
                executeTask(task).thenAccept(response -> {
                    taskFuture.complete(response);
                    responses.add(response);
                }).exceptionally(throwable -> {
                    taskFuture.completeExceptionally(throwable);
                    return null;
                });
            });
        }
    }

    /**
     * Execute a single task with timeout and retry
     */
    private CompletableFuture<TaskStatusUpdateEvent> executeTask(Task task) {
        String taskId = task.getId();
        taskStartTimes.put(taskId, System.currentTimeMillis());
        taskStates.put(taskId, TaskState.WORKING);

        logger.debug("Executing task: {}", taskId);

        return executeWithTimeout(task, Duration.ofMillis(defaultTimeoutMs)).thenCompose(response -> {
            if (response.getStatus().state() == TaskState.COMPLETED) {
                taskStates.put(taskId, TaskState.COMPLETED);
                return CompletableFuture.completedFuture(response);
            } else {
                return executeWithRetry(task, maxRetries);
            }
        }).exceptionally(throwable -> {
            taskStates.put(taskId, TaskState.FAILED);
            logger.error("Task execution failed: {}", taskId, throwable);
            return new TaskStatusUpdateEvent(taskId, new TaskStatus(TaskState.FAILED), task.getContextId(), true, null);
        });
    }

    /**
     * Acquire a resource lock
     */
    public boolean acquireLock(String resourceId, String agentId) {
        ReentrantLock lock = resourceLocks.computeIfAbsent(resourceId, k -> new ReentrantLock());

        if (lock.tryLock()) {
            lockOwners.put(resourceId, agentId);
            logger.debug("Agent {} acquired lock for resource {}", agentId, resourceId);
            return true;
        } else {
            String currentOwner = lockOwners.get(resourceId);
            logger.debug("Agent {} failed to acquire lock for resource {} (owned by {})", agentId, resourceId,
                    currentOwner);
            return false;
        }
    }

    /**
     * Release a resource lock
     */
    public void releaseLock(String resourceId, String agentId) {
        ReentrantLock lock = resourceLocks.get(resourceId);
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
            lockOwners.remove(resourceId);
            logger.debug("Agent {} released lock for resource {}", agentId, resourceId);
        }
    }

    /**
     * Release all locks (for cleanup)
     */
    private void releaseAllLocks() {
        for (Map.Entry<String, ReentrantLock> entry : resourceLocks.entrySet()) {
            ReentrantLock lock = entry.getValue();
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        lockOwners.clear();
        resourceLocks.clear();
    }

    /**
     * Check for circular dependencies in the dependency graph
     */
    public boolean hasCircularDependency(Map<String, Set<String>> dependencyGraph) {
        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();

        for (String taskId : dependencyGraph.keySet()) {
            if (!visited.contains(taskId)) {
                if (hasCycle(taskId, dependencyGraph, visited, recursionStack)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check for cycles in the dependency graph using DFS
     */
    private boolean hasCycle(String taskId, Map<String, Set<String>> graph, Set<String> visited,
            Set<String> recursionStack) {

        visited.add(taskId);
        recursionStack.add(taskId);

        Set<String> dependencies = graph.get(taskId);
        if (dependencies != null) {
            for (String dependency : dependencies) {
                if (!visited.contains(dependency)) {
                    if (hasCycle(dependency, graph, visited, recursionStack)) {
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
     * Execute a transaction with multiple tasks
     */
    public CompletableFuture<TransactionResult> executeTransaction(List<Task> tasks) {
        logger.debug("Executing transaction with {} tasks", tasks.size());

        return executeTasksWithDependencies(tasks).thenApply(responses -> {
            boolean allSuccess = responses.stream()
                    .allMatch(response -> response.getStatus().state() == TaskState.COMPLETED);
            String message = allSuccess ? "Transaction completed successfully" : "Transaction failed";
            return new TransactionResult(allSuccess, message, responses);
        });
    }

    /**
     * Execute a task with timeout
     */
    public CompletableFuture<TaskStatusUpdateEvent> executeWithTimeout(Task task, Duration timeout) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Simulate task execution - replace with actual task execution logic
                Thread.sleep(100); // Simulate work
                return new TaskStatusUpdateEvent(task.getId(), new TaskStatus(TaskState.COMPLETED), task.getContextId(),
                        true, null);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Task execution interrupted", e);
            }
        }).orTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS).exceptionally(throwable -> {
            if (throwable instanceof java.util.concurrent.TimeoutException) {
                return new TaskStatusUpdateEvent(task.getId(), new TaskStatus(TaskState.FAILED), task.getContextId(),
                        true, null);
            } else {
                return new TaskStatusUpdateEvent(task.getId(), new TaskStatus(TaskState.FAILED), task.getContextId(),
                        true, null);
            }
        });
    }

    /**
     * Execute a task with retry mechanism
     */
    public CompletableFuture<TaskStatusUpdateEvent> executeWithRetry(Task task, int maxRetries) {
        AtomicInteger retryCount = taskRetryCounts.computeIfAbsent(task.getId(), k -> new AtomicInteger(0));

        return executeWithTimeout(task, Duration.ofMillis(defaultTimeoutMs)).thenCompose(response -> {
            if (response.getStatus().state() == TaskState.COMPLETED) {
                return CompletableFuture.completedFuture(response);
            } else if (retryCount.get() < maxRetries) {
                retryCount.incrementAndGet();
                logger.debug("Retrying task {} (attempt {}/{})", task.getId(), retryCount.get(), maxRetries);

                return CompletableFuture.supplyAsync(() -> {
                    try {
                        Thread.sleep(retryDelayMs);
                        return executeWithRetry(task, maxRetries);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", e);
                    }
                }).thenCompose(future -> future);
            } else {
                return CompletableFuture.completedFuture(response);
            }
        });
    }

    /**
     * Execute a task with fallback agents
     */
    public CompletableFuture<TaskStatusUpdateEvent> executeWithFallback(Task task, List<String> fallbackAgents) {
        if (fallbackAgents.isEmpty()) {
            return executeWithTimeout(task, Duration.ofMillis(defaultTimeoutMs));
        }

        return executeWithTimeout(task, Duration.ofMillis(defaultTimeoutMs)).thenCompose(response -> {
            if (response.getStatus().state() == TaskState.COMPLETED) {
                return CompletableFuture.completedFuture(response);
            } else {
                // Try fallback agents
                return tryFallbackAgents(task, fallbackAgents, 0);
            }
        });
    }

    /**
     * Try fallback agents recursively
     */
    private CompletableFuture<TaskStatusUpdateEvent> tryFallbackAgents(Task task, List<String> fallbackAgents,
            int currentIndex) {

        if (currentIndex >= fallbackAgents.size()) {
            return CompletableFuture.completedFuture(new TaskStatusUpdateEvent(task.getId(),
                    new TaskStatus(TaskState.FAILED), task.getContextId(), true, null));
        }

        String fallbackAgent = fallbackAgents.get(currentIndex);
        logger.debug("Trying fallback agent {} for task {}", fallbackAgent, task.getId());

        // Create a new task for the fallback agent (simplified - in real implementation would need proper Task
        // creation)
        // For now, just try the original task again
        return executeWithTimeout(task, Duration.ofMillis(defaultTimeoutMs)).thenCompose(response -> {
            if (response.getStatus().state() == TaskState.COMPLETED) {
                return CompletableFuture.completedFuture(response);
            } else {
                return tryFallbackAgents(task, fallbackAgents, currentIndex + 1);
            }
        });
    }

    /**
     * Start monitoring task execution
     */
    private void startMonitoring() {
        monitoringExecutor.scheduleAtFixedRate(this::monitorTaskExecution, 10, 10, TimeUnit.SECONDS);
    }

    /**
     * Stop monitoring task execution
     */
    private void stopMonitoring() {
        monitoringExecutor.shutdown();
        try {
            if (!monitoringExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                monitoringExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            monitoringExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Monitor task execution for stuck tasks and deadlocks
     */
    public void monitorTaskExecution() {
        if (!isRunning.get()) {
            return;
        }

        long currentTime = System.currentTimeMillis();

        // Check for stuck tasks
        for (Map.Entry<String, Long> entry : taskStartTimes.entrySet()) {
            String taskId = entry.getKey();
            Long startTime = entry.getValue();
            TaskState state = taskStates.get(taskId);

            if (state == TaskState.WORKING && (currentTime - startTime) > defaultTimeoutMs) {
                logger.warn("Task {} appears to be stuck (running for {} ms)", taskId, currentTime - startTime);
            }
        }

        // Check for deadlocks in resource locks
        checkForDeadlocks();

        // Clean up completed tasks
        cleanupCompletedTasks();
    }

    /**
     * Check for deadlocks in resource locks
     */
    private void checkForDeadlocks() {
        // Simple deadlock detection - check for long-held locks
        long currentTime = System.currentTimeMillis();
        for (Map.Entry<String, ReentrantLock> entry : resourceLocks.entrySet()) {
            String resourceId = entry.getKey();
            ReentrantLock lock = entry.getValue();

            if (lock.isLocked() && lock.getQueueLength() > 0) {
                logger.warn("Resource {} has waiting threads: {}", resourceId, lock.getQueueLength());
            }
        }
    }

    /**
     * Clean up completed tasks
     */
    private void cleanupCompletedTasks() {
        taskStates.entrySet()
                .removeIf(entry -> entry.getValue() == TaskState.COMPLETED || entry.getValue() == TaskState.FAILED);

        taskStartTimes.entrySet().removeIf(entry -> !taskStates.containsKey(entry.getKey()));
    }

    /**
     * Get task execution statistics
     */
    public TaskExecutionStats getTaskExecutionStats() {
        Map<TaskState, Integer> stateCounts = new HashMap<>();
        for (TaskState state : taskStates.values()) {
            stateCounts.merge(state, 1, Integer::sum);
        }

        return new TaskExecutionStats(taskStates.size(), stateCounts, resourceLocks.size(), lockOwners.size());
    }

    /**
     * Agent task representation
     */
    public static class AgentTask {
        private final String taskId;
        private final String agentId;
        private final List<String> dependencies;
        private final Map<String, Object> parameters;

        public AgentTask(String taskId, String agentId, List<String> dependencies, Map<String, Object> parameters) {
            this.taskId = taskId;
            this.agentId = agentId;
            this.dependencies = new ArrayList<>(dependencies);
            this.parameters = new HashMap<>(parameters);
        }

        public String getTaskId() {
            return taskId;
        }

        public String getAgentId() {
            return agentId;
        }

        public List<String> getDependencies() {
            return Collections.unmodifiableList(dependencies);
        }

        public Map<String, Object> getParameters() {
            return Collections.unmodifiableMap(parameters);
        }
    }

    /**
     * Agent response representation
     */
    public static class AgentResponse {
        private final String taskId;
        private final boolean success;
        private final String message;
        private final @Nullable Object data;

        public AgentResponse(String taskId, boolean success, String message, @Nullable Object data) {
            this.taskId = taskId;
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public String getTaskId() {
            return taskId;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public @Nullable Object getData() {
            return data;
        }
    }

    /**
     * Transaction result representation
     */
    public static class TransactionResult {
        private final boolean success;
        private final String message;
        private final List<TaskStatusUpdateEvent> responses;

        public TransactionResult(boolean success, String message, List<TaskStatusUpdateEvent> responses) {
            this.success = success;
            this.message = message;
            this.responses = new ArrayList<>(responses);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public List<TaskStatusUpdateEvent> getResponses() {
            return Collections.unmodifiableList(responses);
        }
    }

    /**
     * Task execution statistics
     */
    public static class TaskExecutionStats {
        private final int totalTasks;
        private final Map<TaskState, Integer> stateCounts;
        private final int activeLocks;
        private final int lockedResources;

        public TaskExecutionStats(int totalTasks, Map<TaskState, Integer> stateCounts, int activeLocks,
                int lockedResources) {
            this.totalTasks = totalTasks;
            this.stateCounts = new HashMap<>(stateCounts);
            this.activeLocks = activeLocks;
            this.lockedResources = lockedResources;
        }

        public int getTotalTasks() {
            return totalTasks;
        }

        public Map<TaskState, Integer> getStateCounts() {
            return Collections.unmodifiableMap(stateCounts);
        }

        public int getActiveLocks() {
            return activeLocks;
        }

        public int getLockedResources() {
            return lockedResources;
        }
    }
}
