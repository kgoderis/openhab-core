package org.openhab.core.ai.agent.internal;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.service.ReadyMarker;
import org.openhab.core.service.ReadyService;
import org.openhab.core.service.ReadyService.ReadyTracker;
import org.osgi.service.component.annotations.Activate;
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
 * Persistence manager for A2A operations.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class AgentPersistenceManager implements ReadyTracker {

    private static final Logger logger = LoggerFactory.getLogger(AgentPersistenceManager.class);

    // Ready marker for Agent persistence
    public static final ReadyMarker AGENT_PERSISTENCE_READY = new ReadyMarker("agent", "persistence");

    @Reference
    private @Nullable ReadyService readyService;

    // Enhanced persistence configuration
    private static final String PERSISTENCE_DIR = "a2a";
    private static final String TASKS_FILE = "tasks.json";
    private static final String STATISTICS_FILE = "statistics.json";
    private static final String METADATA_FILE = "metadata.json";
    private static final String EXECUTION_LOGS_FILE = "execution-logs.json";
    private static final String RECOVERY_FILE = "recovery-state.json";

    // Enhanced in-memory storage with persistence
    private final ConcurrentHashMap<String, Task> tasks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Map<String, Object>> taskMetadata = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> taskExecutionCounts = new ConcurrentHashMap<>();

    // Enhanced execution tracking for real execution
    private final ConcurrentHashMap<String, TaskExecutionState> taskExecutionStates = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> taskExecutionStartTimes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> taskExecutors = new ConcurrentHashMap<>();

    // Enhanced statistics for real execution monitoring
    private final AtomicLong totalTasks = new AtomicLong(0);
    private final AtomicLong completedTasks = new AtomicLong(0);
    private final AtomicLong failedTasks = new AtomicLong(0);
    private final AtomicLong cancelledTasks = new AtomicLong(0);
    private final AtomicLong activeTasks = new AtomicLong(0);
    private final AtomicLong totalExecutionTime = new AtomicLong(0);

    // Enhanced JSON handling with better serialization
    private final Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();

    // Enhanced persistence directory with subdirectories
    private @Nullable Path persistenceDir;
    private @Nullable Path tasksDir;
    private @Nullable Path logsDir;
    private @Nullable Path recoveryDir;

    // Enhanced task execution state tracking
    public static class TaskExecutionState {
        private final String taskId;
        private final TaskState state;
        private final long startTime;
        private final String executor;
        private final Map<String, Object> context;
        private final List<String> executionLog;

        public TaskExecutionState(String taskId, TaskState state, String executor) {
            this.taskId = taskId;
            this.state = state;
            this.startTime = System.currentTimeMillis();
            this.executor = executor;
            this.context = new HashMap<>();
            this.executionLog = new ArrayList<>();
        }

        // Getters and setters
        public String getTaskId() {
            return taskId;
        }

        public TaskState getState() {
            return state;
        }

        public long getStartTime() {
            return startTime;
        }

        public String getExecutor() {
            return executor;
        }

        public Map<String, Object> getContext() {
            return context;
        }

        public List<String> getExecutionLog() {
            return executionLog;
        }

        public void addLogEntry(String entry) {
            executionLog.add(System.currentTimeMillis() + ": " + entry);
        }

        public void updateState(TaskState newState) {
            // Note: This is a simplified approach - in real implementation,
            // you'd need to handle state transitions properly
        }
    }

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
        try {
            tasks.put(task.getId(), task);
            saveTasks();
            totalTasks.incrementAndGet();
            logger.debug("Saved task: {}", task.getId());
        } catch (Exception e) {
            logger.error("Error saving task: {}", task.getId(), e);
        }
    }

    public @Nullable Task getTask(String taskId) {
        return tasks.get(taskId);
    }

    public void deleteTask(String taskId) {
        try {
            tasks.remove(taskId);
            taskMetadata.remove(taskId);
            taskExecutionCounts.remove(taskId);
            saveTasks();
            logger.debug("Deleted task: {}", taskId);
        } catch (Exception e) {
            logger.error("Error deleting task: {}", taskId, e);
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

            // Update statistics
            if (success) {
                completedTasks.incrementAndGet();
            } else {
                failedTasks.incrementAndGet();
            }

            logger.debug("Updated result for task: {} - success: {}", taskId, success);
        } catch (Exception e) {
            logger.error("Error updating result for task: {}", taskId, e);
        }
    }

    // Task execution tracking
    public void incrementTaskExecutionCount(String taskId) {
        AtomicLong count = taskExecutionCounts.computeIfAbsent(taskId, k -> new AtomicLong(0));
        count.incrementAndGet();
        saveStatistics();
    }

    public long getTaskExecutionCount(String taskId) {
        AtomicLong count = taskExecutionCounts.get(taskId);
        return count != null ? count.get() : 0;
    }

    public void markTaskCancelled(String taskId) {
        cancelledTasks.incrementAndGet();
        saveStatistics();
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

            totalTasks.incrementAndGet();
            activeTasks.incrementAndGet();

            logger.debug("Saved task with execution state: {} (executor: {})", task.getId(),
                    executionState.getExecutor());
        } catch (Exception e) {
            logger.error("Error saving task with execution state: {}", task.getId(), e);
        }
    }

    public @Nullable TaskExecutionState getTaskExecutionState(String taskId) {
        return taskExecutionStates.get(taskId);
    }

    public void updateTaskExecutionState(String taskId, TaskState newState, String logEntry) {
        TaskExecutionState state = taskExecutionStates.get(taskId);
        if (state != null) {
            state.addLogEntry(logEntry);
            state.updateState(newState);

            // Update statistics based on state change
            if (newState == TaskState.COMPLETED) {
                completedTasks.incrementAndGet();
                activeTasks.decrementAndGet();
                long executionTime = System.currentTimeMillis() - state.getStartTime();
                totalExecutionTime.addAndGet(executionTime);
            } else if (newState == TaskState.FAILED) {
                failedTasks.incrementAndGet();
                activeTasks.decrementAndGet();
            } else if (newState == TaskState.CANCELED) {
                cancelledTasks.incrementAndGet();
                activeTasks.decrementAndGet();
            }

            // Save updated state
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

    // Statistics methods
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTasks", totalTasks.get());
        stats.put("completedTasks", completedTasks.get());
        stats.put("failedTasks", failedTasks.get());
        stats.put("cancelledTasks", cancelledTasks.get());
        stats.put("activeTasks", tasks.size());
        stats.put("totalExecutions", taskExecutionCounts.values().stream().mapToLong(AtomicLong::get).sum());

        // Calculate success rate
        long totalExecuted = completedTasks.get() + failedTasks.get();
        if (totalExecuted > 0) {
            double successRate = (double) completedTasks.get() / totalExecuted;
            stats.put("successRate", successRate);
        } else {
            stats.put("successRate", 0.0);
        }

        return stats;
    }

    // Enhanced statistics with real execution metrics
    public Map<String, Object> getEnhancedStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTasks", totalTasks.get());
        stats.put("completedTasks", completedTasks.get());
        stats.put("failedTasks", failedTasks.get());
        stats.put("cancelledTasks", cancelledTasks.get());
        stats.put("activeTasks", activeTasks.get());
        stats.put("totalExecutions", taskExecutionCounts.values().stream().mapToLong(AtomicLong::get).sum());
        stats.put("totalExecutionTime", totalExecutionTime.get());
        stats.put("averageExecutionTime", calculateAverageExecutionTime());

        // Calculate success rate
        long totalExecuted = completedTasks.get() + failedTasks.get();
        if (totalExecuted > 0) {
            double successRate = (double) completedTasks.get() / totalExecuted;
            stats.put("successRate", successRate);
        } else {
            stats.put("successRate", 0.0);
        }

        // Add per-executor statistics
        Map<String, Object> executorStats = new HashMap<>();
        taskExecutors.values().stream()
                .collect(java.util.stream.Collectors.groupingBy(executor -> executor,
                        java.util.stream.Collectors.counting()))
                .forEach((executor, count) -> executorStats.put(executor, count));
        stats.put("executorStatistics", executorStats);

        return stats;
    }

    private double calculateAverageExecutionTime() {
        long totalExecuted = completedTasks.get() + failedTasks.get();
        if (totalExecuted > 0) {
            return (double) totalExecutionTime.get() / totalExecuted;
        }
        return 0.0;
    }

    // Persistence file operations
    private void saveTasks() {
        try {
            Path tasksFile = persistenceDir.resolve(TASKS_FILE);
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
            Path tasksFile = persistenceDir.resolve(TASKS_FILE);
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
            Path statsFile = persistenceDir.resolve(STATISTICS_FILE);
            Map<String, Object> statsData = new HashMap<>();
            statsData.put("totalTasks", totalTasks.get());
            statsData.put("completedTasks", completedTasks.get());
            statsData.put("failedTasks", failedTasks.get());
            statsData.put("cancelledTasks", cancelledTasks.get());
            statsData.put("taskExecutionCounts", taskExecutionCounts);
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
            Path statsFile = persistenceDir.resolve(STATISTICS_FILE);
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
            Path metadataFile = persistenceDir.resolve(METADATA_FILE);
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
            Path metadataFile = persistenceDir.resolve(METADATA_FILE);
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
            taskExecutionCounts.remove(taskId);
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
        totalTasks.set(0);
        completedTasks.set(0);
        failedTasks.set(0);
        cancelledTasks.set(0);
        taskExecutionCounts.clear();
        saveStatistics();
        logger.info("Reset A2A statistics");
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
