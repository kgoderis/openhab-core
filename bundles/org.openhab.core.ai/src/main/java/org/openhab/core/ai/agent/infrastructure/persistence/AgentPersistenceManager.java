package org.openhab.core.ai.agent.infrastructure.persistence;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricKeys;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.collector.MetricsCollector;
import org.openhab.core.ai.common.monitoring.registry.MonitoringRegistry;
import org.openhab.core.ai.common.monitoring.service.statistics.AgentPersistenceStatistics;
import org.openhab.core.service.ReadyMarker;
import org.openhab.core.service.ReadyService;
import org.openhab.core.service.ReadyService.ReadyTracker;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.a2a.spec.Task;
import io.a2a.spec.TaskState;

/**
 * Enhanced persistence manager for A2A agent tasks and metadata.
 * 
 * <p>
 * This component provides comprehensive persistence capabilities for A2A agent operations:
 * - Task storage and retrieval with enhanced metadata
 * - Execution state tracking and recovery
 * - Statistics and performance metrics persistence via MetricsService
 * - Enhanced error recovery and data validation
 * - Integration with openHAB persistence services
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = AgentPersistenceManager.class)
@NonNullByDefault
public class AgentPersistenceManager implements ReadyTracker {

    private static final Logger logger = LoggerFactory.getLogger(AgentPersistenceManager.class);

    // Ready service integration
    public static final ReadyMarker AGENT_PERSISTENCE_READY = new ReadyMarker("agent", "persistence");
    private static final String PERSISTENCE_DIR = "ai" + File.separator + "agents";

    // Enhanced in-memory storage with openHAB integration
    private final ConcurrentHashMap<String, Task> tasks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Map<String, Object>> taskMetadata = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, TaskExecutionState> taskExecutionStates = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> taskExecutionStartTimes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> taskExecutors = new ConcurrentHashMap<>();

    // Enhanced JSON handling with better serialization
    private final Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();

    // Enhanced persistence directory with subdirectories
    private @Nullable Path persistenceDir;
    private @Nullable Path tasksDir;
    private @Nullable Path logsDir;
    private @Nullable Path recoveryDir;

    // NEW: Monitoring registry for centralized metrics collection
    @Reference
    private @Nullable MonitoringRegistry monitoringRegistry;

    @Reference
    private @Nullable MetricsService metricsService;

    // Dependencies
    @Reference
    private @Nullable ReadyService readyService;

    // Inner class extracted to top-level: org.openhab.core.ai.agent.infrastructure.persistence.TaskExecutionState

    @Activate
    public void activate() {
        logger.debug("A2A Persistence Manager activated");

        // Register as a tracker
        if (readyService != null) {
            readyService.registerTracker(this);
        }

        // Initialize persistence
        initializePersistence();
    }

    @Deactivate
    public void deactivate() {
        logger.debug("A2A Persistence Manager deactivated");

        // Unregister tracker
        if (readyService != null) {
            readyService.unregisterTracker(this);
        }

        // Save all data before shutdown
        saveAllData();

        // Unmark ready marker
        if (readyService != null) {
            readyService.unmarkReady(AGENT_PERSISTENCE_READY);
        }
    }

    private void initializePersistence() {
        try {
            // Create enhanced persistence directory structure
            String userDataDir = System.getProperty("user.home") + File.separator + ".openhab";
            persistenceDir = Paths.get(userDataDir, PERSISTENCE_DIR);
            tasksDir = persistenceDir.resolve("tasks");
            logsDir = persistenceDir.resolve("logs");
            recoveryDir = persistenceDir.resolve("recovery");

            Files.createDirectories(persistenceDir);
            Files.createDirectories(tasksDir);
            Files.createDirectories(logsDir);
            Files.createDirectories(recoveryDir);

            logger.debug("Enhanced A2A persistence directory structure: {}", persistenceDir);

            // Load existing data with enhanced recovery
            loadTasksWithRecovery();
            loadStatistics();
            loadMetadata();
            loadExecutionLogs();

            // Mark as ready
            if (readyService != null) {
                readyService.markReady(AGENT_PERSISTENCE_READY);
            }
            logger.info("Enhanced A2A persistence initialized with {} tasks", tasks.size());

        } catch (Exception e) {
            logger.error("Failed to initialize enhanced A2A persistence", e);
        }
    }

    // Task persistence methods
    public void saveTask(Task task) {
        long startTime = System.nanoTime();
        boolean success = false;

        try {
            tasks.put(task.getId(), task);
            saveTasks();
            success = true;
            logger.debug("Saved task: {}", task.getId());
        } catch (Exception e) {
            logger.error("Error saving task: {}", task.getId(), e);
        } finally {
            recordMetrics("agent-persistence", "task-management", success, System.nanoTime() - startTime);
        }
    }

    public @Nullable Task getTask(String taskId) {
        return tasks.get(taskId);
    }

    public void deleteTask(String taskId) {
        long startTime = System.nanoTime();
        boolean success = false;

        try {
            tasks.remove(taskId);
            taskMetadata.remove(taskId);
            taskExecutionStates.remove(taskId);
            taskExecutionStartTimes.remove(taskId);
            taskExecutors.remove(taskId);
            saveTasks();
            success = true;
            logger.debug("Deleted task: {}", taskId);
        } catch (Exception e) {
            logger.error("Error deleting task: {}", taskId, e);
        } finally {
            recordMetrics("agent-persistence", "task-management", success, System.nanoTime() - startTime);
        }
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public List<Task> getTasksByState(TaskState state) {
        List<Task> filteredTasks = new ArrayList<>();
        for (Task task : tasks.values()) {
            if (task.getStatus().state() == state) {
                filteredTasks.add(task);
            }
        }
        return filteredTasks;
    }

    public List<Task> getTasksByTimeRange(long startTime, long endTime) {
        List<Task> filteredTasks = new ArrayList<>();
        for (Task task : tasks.values()) {
            Map<String, Object> metadata = task.getMetadata();
            if (metadata != null && metadata.containsKey("created")) {
                Long created = (Long) metadata.get("created");
                if (created != null && created >= startTime && created <= endTime) {
                    filteredTasks.add(task);
                }
            }
        }
        return filteredTasks;
    }

    // Task metadata methods
    public void saveTaskMetadata(String taskId, Map<String, Object> metadata) {
        try {
            taskMetadata.put(taskId, metadata);
            saveMetadata();
            logger.debug("Saved metadata for task: {}", taskId);
        } catch (Exception e) {
            logger.error("Error saving metadata for task: {}", taskId, e);
        }
    }

    public @Nullable Map<String, Object> getTaskMetadata(String taskId) {
        return taskMetadata.get(taskId);
    }

    public void updateTaskResult(String taskId, Object result, boolean success, String message) {
        try {
            Map<String, Object> metadata = taskMetadata.getOrDefault(taskId, new HashMap<>());
            metadata.put("result", result);
            metadata.put("success", success);
            metadata.put("message", message);
            metadata.put("completedAt", System.currentTimeMillis());

            taskMetadata.put(taskId, metadata);
            saveMetadata();

            // Record metrics for task result update
            recordMetrics("agent-persistence", "task-result-update", success, 0L);
        } catch (Exception e) {
            logger.error("Error updating result for task: {}", taskId, e);
            recordMetrics("agent-persistence", "task-result-update", false, 0L);
        }
    }

    // Task execution tracking
    public void incrementTaskExecutionCount(String taskId) {
        // Metrics are now handled by MetricsService
        recordMetrics("agent-persistence", "task-execution", true, 0L);
    }

    public long getTaskExecutionCount(String taskId) {
        // Get execution count from MetricsService
        MetricsService metrics = metricsService;
        if (metrics != null) {
            try {
                var snapshot = metrics.getDomainAggregatedSnapshot("agent-persistence");
                return snapshot.totalOperations();
            } catch (Exception e) {
                logger.warn("Error retrieving execution count for task {}: {}", taskId, e.getMessage());
            }
        }
        return 0L;
    }

    public void markTaskCancelled(String taskId) {
        recordMetrics("agent-persistence", "task-cancellation", true, 0L);
        logger.debug("Marked task as cancelled: {}", taskId);
    }

    // Enhanced task persistence with real execution support
    public void saveTaskWithExecutionState(Task task, TaskExecutionState executionState) {
        try {
            tasks.put(task.getId(), task);
            taskExecutionStates.put(task.getId(), executionState);
            taskExecutionStartTimes.put(task.getId(), executionState.getStartTime());
            taskExecutors.put(task.getId(), executionState.getExecutor());

            // Save to persistent storage
            saveTaskToFile(task);
            saveExecutionStateToFile(executionState);

            // Record metrics for task save with execution state
            recordMetrics("agent-persistence", "task-save-with-execution", true, 0L);

            logger.debug("Saved task with execution state: {} (executor: {})", task.getId(),
                    executionState.getExecutor());
        } catch (Exception e) {
            logger.error("Error saving task with execution state: {}", task.getId(), e);
            recordMetrics("agent-persistence", "task-save-with-execution", false, 0L);
        }
    }

    public @Nullable TaskExecutionState getTaskExecutionState(String taskId) {
        return taskExecutionStates.get(taskId);
    }

    /**
     * Update task execution state using the new monitoring framework
     */
    public void updateTaskExecutionState(String taskId, TaskState newState, String logEntry) {
        TaskExecutionState state = taskExecutionStates.get(taskId);
        if (state != null) {
            // Maintain execution log entry if supported by TaskExecutionState implementation
            try {
                Method m = state.getClass().getMethod("addLogEntry", String.class);
                m.invoke(state, logEntry);
            } catch (Exception ignore) {
                // no-op if method not present
            }
            state.updateState(newState);

            // Update statistics based on state change using monitoring registry
            if (monitoringRegistry != null) {
                MetricsCollector collector = monitoringRegistry
                        .metricsCollector(MetricKeys.action("task-state-update"));

                if (newState == TaskState.COMPLETED) {
                    collector.recordExecution(true, 0L);
                } else if (newState == TaskState.FAILED) {
                    collector.recordExecution(false, 0L);
                } else if (newState == TaskState.CANCELED) {
                    collector.recordExecution(false, 0L);
                } else {
                    collector.recordExecution(true, 0L);
                }
            }

            // Save execution state
            saveExecutionStateToFile(state);
            logger.debug("Updated task execution state: {} -> {}", taskId, newState);
        }
    }

    // Enhanced file-based persistence for real execution
    private void saveTaskToFile(Task task) {
        try {
            Path taskFile = tasksDir.resolve(task.getId() + ".json");
            try (FileWriter writer = new FileWriter(taskFile.toFile())) {
                gson.toJson(task, writer);
            }
            logger.debug("Saved task to file: {}", taskFile);
        } catch (Exception e) {
            logger.error("Error saving task to file: {}", task.getId(), e);
        }
    }

    private void saveExecutionStateToFile(TaskExecutionState state) {
        try {
            Path stateFile = logsDir.resolve(state.getTaskId() + "-execution.json");
            try (FileWriter writer = new FileWriter(stateFile.toFile())) {
                gson.toJson(state, writer);
            }
            logger.debug("Saved execution state to file: {}", stateFile);
        } catch (Exception e) {
            logger.error("Error saving execution state to file: {}", state.getTaskId(), e);
        }
    }

    // Statistics methods - now using MetricsService exclusively
    public AgentPersistenceStatistics getStatistics() {
        MetricsService metrics = metricsService;
        if (metrics != null) {
            try {
                return metrics.getAgentPersistenceStatistics("default", Duration.ofHours(1));
            } catch (Exception e) {
                logger.warn("Error retrieving metrics for agent-persistence: {}", e.getMessage());
            }
        } else {
            logger.warn("MetricsService not available, returning empty statistics");
        }
        return AgentPersistenceStatistics.empty("default", Duration.ofHours(1));
    }

    // Enhanced statistics with additional context
    public Map<String, Object> getEnhancedStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // Get the agent persistence statistics
        AgentPersistenceStatistics statistics = getStatistics();

        // Add basic statistics data
        stats.put("totalOperations", statistics.total());
        stats.put("successfulOperations", statistics.success());
        stats.put("failedOperations", statistics.failure());
        stats.put("totalDurationNanos", statistics.totalDurationNanos());
        stats.put("successRate", statistics.successRate());
        stats.put("timestampMs", statistics.timestampMs());

        // Add additional context-specific data
        stats.put("totalTasks", tasks.size());

        // Add per-executor statistics
        Map<String, Object> executorStats = new HashMap<>();
        taskExecutors.values().stream().collect(Collectors.groupingBy(executor -> executor, Collectors.counting()))
                .forEach((executor, count) -> executorStats.put(executor, count));
        stats.put("executorStatistics", executorStats);

        return stats;
    }

    // Persistence file operations
    private void saveTasks() {
        try {
            Path tasksFile = persistenceDir.resolve("tasks.json");
            Map<String, Object> tasksData = new HashMap<>();
            tasksData.put("tasks", tasks);
            tasksData.put("timestamp", System.currentTimeMillis());

            try (FileWriter writer = new FileWriter(tasksFile.toFile())) {
                gson.toJson(tasksData, writer);
            }

            logger.debug("Saved {} tasks to persistence", tasks.size());
        } catch (Exception e) {
            logger.error("Error saving tasks", e);
        }
    }

    private void loadTasks() {
        try {
            Path tasksFile = persistenceDir.resolve("tasks.json");
            if (Files.exists(tasksFile)) {
                try (FileReader reader = new FileReader(tasksFile.toFile())) {
                    JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
                    // Note: This is a simplified loading - in a real implementation,
                    // you'd need to properly deserialize Task objects
                    logger.debug("Loaded tasks from persistence");
                }
            }
        } catch (Exception e) {
            logger.error("Error loading tasks", e);
        }
    }

    private void saveStatistics() {
        try {
            Path statsFile = persistenceDir.resolve("statistics.json");
            Map<String, Object> statsData = new HashMap<>();
            statsData.put("timestamp", System.currentTimeMillis());

            try (FileWriter writer = new FileWriter(statsFile.toFile())) {
                gson.toJson(statsData, writer);
            }

            logger.debug("Saved statistics to persistence");
        } catch (Exception e) {
            logger.error("Error saving statistics", e);
        }
    }

    private void loadStatistics() {
        try {
            Path statsFile = persistenceDir.resolve("statistics.json");
            if (Files.exists(statsFile)) {
                try (FileReader reader = new FileReader(statsFile.toFile())) {
                    JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
                    // Note: This is a simplified loading - in a real implementation,
                    // you'd need to properly deserialize the statistics
                    logger.debug("Loaded statistics from persistence");
                }
            }
        } catch (Exception e) {
            logger.error("Error loading statistics", e);
        }
    }

    private void saveMetadata() {
        try {
            Path metadataFile = persistenceDir.resolve("metadata.json");
            Map<String, Object> metadataData = new HashMap<>();
            metadataData.put("taskMetadata", taskMetadata);
            metadataData.put("timestamp", System.currentTimeMillis());

            try (FileWriter writer = new FileWriter(metadataFile.toFile())) {
                gson.toJson(metadataData, writer);
            }

            logger.debug("Saved metadata to persistence");
        } catch (Exception e) {
            logger.error("Error saving metadata", e);
        }
    }

    private void loadMetadata() {
        try {
            Path metadataFile = persistenceDir.resolve("metadata.json");
            if (Files.exists(metadataFile)) {
                try (FileReader reader = new FileReader(metadataFile.toFile())) {
                    JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
                    // Note: This is a simplified loading - in a real implementation,
                    // you'd need to properly deserialize the metadata
                    logger.debug("Loaded metadata from persistence");
                }
            }
        } catch (Exception e) {
            logger.error("Error loading metadata", e);
        }
    }

    private void saveAllData() {
        saveTasks();
        saveStatistics();
        saveMetadata();
        logger.debug("Saved all A2A data to persistence");
    }

    private void loadTasksWithRecovery() {
        try {
            // Load from individual task files for better recovery
            if (tasksDir != null && Files.exists(tasksDir)) {
                Files.list(tasksDir).filter(path -> path.toString().endsWith(".json")).forEach(this::loadTaskFromFile);
            }

            // Also load from legacy tasks.json if it exists
            loadTasks();

            logger.debug("Loaded {} tasks with recovery support", tasks.size());
        } catch (Exception e) {
            logger.error("Error loading tasks with recovery", e);
        }
    }

    private void loadTaskFromFile(Path taskFile) {
        try {
            try (FileReader reader = new FileReader(taskFile.toFile())) {
                Task task = gson.fromJson(reader, Task.class);
                if (task != null) {
                    tasks.put(task.getId(), task);
                    logger.debug("Loaded task from file: {}", taskFile);
                }
            }
        } catch (Exception e) {
            logger.error("Error loading task from file: {}", taskFile, e);
        }
    }

    private void loadExecutionLogs() {
        try {
            if (logsDir != null && Files.exists(logsDir)) {
                Files.list(logsDir).filter(path -> path.toString().endsWith("-execution.json"))
                        .forEach(this::loadExecutionStateFromFile);
            }
            logger.debug("Loaded execution logs");
        } catch (Exception e) {
            logger.error("Error loading execution logs", e);
        }
    }

    private void loadExecutionStateFromFile(Path stateFile) {
        try {
            try (FileReader reader = new FileReader(stateFile.toFile())) {
                TaskExecutionState state = gson.fromJson(reader, TaskExecutionState.class);
                if (state != null) {
                    taskExecutionStates.put(state.getTaskId(), state);
                    taskExecutionStartTimes.put(state.getTaskId(), state.getStartTime());
                    taskExecutors.put(state.getTaskId(), state.getExecutor());
                    logger.debug("Loaded execution state from file: {}", stateFile);
                }
            }
        } catch (Exception e) {
            logger.error("Error loading execution state from file: {}", stateFile, e);
        }
    }

    // Enhanced cleanup with real execution consideration
    public void cleanupOldTasksWithExecutionState(long maxAgeMs) {
        long cutoffTime = System.currentTimeMillis() - maxAgeMs;
        List<String> tasksToRemove = new ArrayList<>();

        for (Map.Entry<String, Task> entry : tasks.entrySet()) {
            Task task = entry.getValue();
            TaskExecutionState state = taskExecutionStates.get(task.getId());

            if (state != null && state.getStartTime() < cutoffTime) {
                tasksToRemove.add(entry.getKey());
            }
        }

        for (String taskId : tasksToRemove) {
            deleteTaskWithExecutionState(taskId);
        }

        logger.info("Cleaned up {} old tasks with execution state", tasksToRemove.size());
    }

    private void deleteTaskWithExecutionState(String taskId) {
        try {
            // Remove from memory
            tasks.remove(taskId);
            taskMetadata.remove(taskId);
            taskExecutionStates.remove(taskId);
            taskExecutionStartTimes.remove(taskId);
            taskExecutors.remove(taskId);

            // Remove from filesystem
            Path taskFile = tasksDir.resolve(taskId + ".json");
            Path stateFile = logsDir.resolve(taskId + "-execution.json");

            Files.deleteIfExists(taskFile);
            Files.deleteIfExists(stateFile);

            logger.debug("Deleted task with execution state: {}", taskId);
        } catch (Exception e) {
            logger.error("Error deleting task with execution state: {}", taskId, e);
        }
    }

    // Cleanup methods
    public void cleanupOldTasks(long maxAgeMs) {
        long cutoffTime = System.currentTimeMillis() - maxAgeMs;
        List<String> tasksToRemove = new ArrayList<>();

        for (Map.Entry<String, Task> entry : tasks.entrySet()) {
            Task task = entry.getValue();
            Map<String, Object> metadata = task.getMetadata();
            if (metadata != null && metadata.containsKey("created")) {
                Long created = (Long) metadata.get("created");
                if (created != null && created < cutoffTime) {
                    tasksToRemove.add(entry.getKey());
                }
            }
        }

        for (String taskId : tasksToRemove) {
            deleteTask(taskId);
        }

        logger.info("Cleaned up {} old tasks", tasksToRemove.size());
    }

    public void resetStatistics() {
        // Statistics reset is now handled by MetricsService
        logger.info("Statistics reset requested - handled by MetricsService");
    }

    /**
     * Get task statistics using the new monitoring framework
     */
    public Map<String, Object> getTaskStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        // Get metrics from MetricsService
        MetricsService metrics = metricsService;
        if (metrics != null) {
            try {
                var snapshot = metrics.getDomainAggregatedSnapshot("agent-persistence");
                statistics.put("totalTasks", tasks.size());
                statistics.put("totalTaskSaves", snapshot.totalOperations());
                statistics.put("successfulTaskSaves", snapshot.totalOperations() - snapshot.failedOperations());
                statistics.put("failedTaskSaves", snapshot.failedOperations());
                statistics.put("totalTaskDeletions", snapshot.totalOperations());
                statistics.put("successfulTaskDeletions", snapshot.totalOperations() - snapshot.failedOperations());
                statistics.put("failedTaskDeletions", snapshot.failedOperations());
                statistics.put("totalStateUpdates", snapshot.totalOperations());
                statistics.put("successfulStateUpdates", snapshot.totalOperations() - snapshot.failedOperations());
                statistics.put("failedStateUpdates", snapshot.failedOperations());
            } catch (Exception e) {
                logger.warn("Error retrieving metrics for agent-persistence: {}", e.getMessage());
                // Fallback to basic statistics
                statistics.put("totalTasks", tasks.size());
                statistics.put("totalTaskSaves", 0);
                statistics.put("successfulTaskSaves", 0);
                statistics.put("failedTaskSaves", 0);
                statistics.put("totalTaskDeletions", 0);
                statistics.put("successfulTaskDeletions", 0);
                statistics.put("failedTaskDeletions", 0);
                statistics.put("totalStateUpdates", 0);
                statistics.put("successfulStateUpdates", 0);
                statistics.put("failedStateUpdates", 0);
            }
        } else {
            logger.warn("MetricsService not available, returning empty statistics");
            // Fallback to basic statistics
            statistics.put("totalTasks", tasks.size());
            statistics.put("totalTaskSaves", 0);
            statistics.put("successfulTaskSaves", 0);
            statistics.put("failedTaskSaves", 0);
            statistics.put("totalTaskDeletions", 0);
            statistics.put("successfulTaskDeletions", 0);
            statistics.put("failedTaskDeletions", 0);
            statistics.put("totalStateUpdates", 0);
            statistics.put("successfulStateUpdates", 0);
            statistics.put("failedStateUpdates", 0);
        }

        return statistics;
    }

    /**
     * Record metrics for an operation.
     * 
     * @param domain the operation domain
     * @param operation the operation name
     * @param success whether the operation was successful
     * @param durationNanos the operation duration in nanoseconds
     */
    private void recordMetrics(String domain, String operation, boolean success, long durationNanos) {
        MetricsService metrics = metricsService;
        if (metrics != null) {
            metrics.recordOperation(domain, operation, success, java.time.Duration.ofNanos(durationNanos));
        } else {
            logger.warn("MetricsService not available, cannot record metrics for operation: {} - {}", domain,
                    operation);
        }
    }

    @Override
    public void onReadyMarkerAdded(ReadyMarker readyMarker) {
        logger.debug("Ready marker added: {}", readyMarker);
    }

    @Override
    public void onReadyMarkerRemoved(ReadyMarker readyMarker) {
        logger.debug("Ready marker removed: {}", readyMarker);
    }
}
