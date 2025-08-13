package org.openhab.core.ai.agent.infrastructure.synchronization.api;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.agent.infrastructure.synchronization.SynchronizationTaskExecutionStats;
import org.openhab.core.ai.agent.infrastructure.synchronization.SynchronizationTransactionResult;

import io.a2a.spec.Task;
import io.a2a.spec.TaskStatusUpdateEvent;

/**
 * Agent Synchronization Manager Interface
 * 
 * <p>
 * This interface defines the contract for agent synchronization management implementations that provide:
 * - Task dependency resolution and execution
 * - Resource locking and deadlock detection
 * - Transaction management
 * - Timeout and retry mechanisms
 * - Fallback execution strategies
 * - Task execution monitoring
 * - Performance statistics and reporting
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface AgentSynchronizationManager {

    /**
     * Execute tasks with dependency resolution
     * 
     * @param tasks List of tasks to execute
     * @return List of task status update events
     */
    CompletableFuture<List<TaskStatusUpdateEvent>> executeTasksWithDependencies(List<Task> tasks);

    /**
     * Execute transaction with multiple tasks
     * 
     * @param tasks List of tasks to execute in transaction
     * @return Transaction result
     */
    CompletableFuture<SynchronizationTransactionResult> executeTransaction(List<Task> tasks);

    /**
     * Execute task with timeout
     * 
     * @param task Task to execute
     * @param timeout Timeout duration
     * @return Task status update event
     */
    CompletableFuture<TaskStatusUpdateEvent> executeWithTimeout(Task task, Duration timeout);

    /**
     * Execute task with retry mechanism
     * 
     * @param task Task to execute
     * @param maxRetries Maximum number of retries
     * @return Task status update event
     */
    CompletableFuture<TaskStatusUpdateEvent> executeWithRetry(Task task, int maxRetries);

    /**
     * Execute task with fallback agents
     * 
     * @param task Task to execute
     * @param fallbackAgents List of fallback agent IDs
     * @return Task status update event
     */
    CompletableFuture<TaskStatusUpdateEvent> executeWithFallback(Task task, List<String> fallbackAgents);

    /**
     * Acquire lock for resource
     * 
     * @param resourceId Resource ID
     * @param agentId Agent ID
     * @return true if lock acquired successfully
     */
    boolean acquireLock(String resourceId, String agentId);

    /**
     * Release lock for resource
     * 
     * @param resourceId Resource ID
     * @param agentId Agent ID
     */
    void releaseLock(String resourceId, String agentId);

    /**
     * Check for circular dependencies in dependency graph
     * 
     * @param dependencyGraph Dependency graph
     * @return true if circular dependency detected
     */
    boolean hasCircularDependency(Map<String, Set<String>> dependencyGraph);

    /**
     * Monitor task execution
     */
    void monitorTaskExecution();

    /**
     * Get task execution statistics
     * 
     * @return Task execution statistics
     */
    SynchronizationTaskExecutionStats getTaskExecutionStats();

    /**
     * Get dependency graph
     * 
     * @return Dependency graph
     */
    Map<String, Set<String>> getDependencyGraph();

    /**
     * Get task states
     * 
     * @return Map of task states
     */
    Map<String, io.a2a.spec.TaskState> getTaskStates();

    /**
     * Get resource locks
     * 
     * @return Map of resource locks
     */
    Map<String, java.util.concurrent.locks.ReentrantLock> getResourceLocks();

    /**
     * Get lock owners
     * 
     * @return Map of lock owners
     */
    Map<String, String> getLockOwners();

    /**
     * Check if service is running
     * 
     * @return true if service is running
     */
    boolean isRunning();

    /**
     * Get default timeout in milliseconds
     * 
     * @return Default timeout in milliseconds
     */
    long getDefaultTimeoutMs();

    /**
     * Get maximum retries
     * 
     * @return Maximum retries
     */
    int getMaxRetries();

    /**
     * Get retry delay in milliseconds
     * 
     * @return Retry delay in milliseconds
     */
    long getRetryDelayMs();

    /**
     * Set default timeout in milliseconds
     * 
     * @param timeoutMs Timeout in milliseconds
     */
    void setDefaultTimeoutMs(long timeoutMs);

    /**
     * Set maximum retries
     * 
     * @param maxRetries Maximum retries
     */
    void setMaxRetries(int maxRetries);

    /**
     * Set retry delay in milliseconds
     * 
     * @param retryDelayMs Retry delay in milliseconds
     */
    void setRetryDelayMs(long retryDelayMs);

    /**
     * Release all locks
     */
    void releaseAllLocks();

    /**
     * Get task start times
     * 
     * @return Map of task start times
     */
    Map<String, Long> getTaskStartTimes();

    /**
     * Get task retry counts
     * 
     * @return Map of task retry counts
     */
    Map<String, java.util.concurrent.atomic.AtomicInteger> getTaskRetryCounts();

    /**
     * Get task execution history
     * 
     * @return Map of task execution history
     */
    Map<String, List<String>> getTaskExecutionHistory();
}
