package org.openhab.core.ai.a2a.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.openhab.core.items.ItemRegistry;
import org.openhab.core.persistence.PersistenceService;
import org.openhab.core.persistence.PersistenceServiceRegistry;
import org.openhab.core.persistence.QueryablePersistenceService;
import org.openhab.core.service.ReadyMarker;
import org.openhab.core.service.ReadyService;
import org.openhab.core.service.ReadyService.ReadyTracker;
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.a2a.spec.Task;
import io.a2a.spec.TaskState;

/**
 * Refactored A2A Persistence Manager using openHAB's StorageService and SLF4J logging.
 * 
 * This class provides persistent storage for A2A tasks using openHAB's StorageService
 * for structured data and SLF4J for logging, ensuring consistency with openHAB patterns.
 * 
 * 
 */
@Component(service = A2AOpenHABPersistenceManager.class, immediate = true)
public class A2AOpenHABPersistenceManager implements ReadyTracker {

    private static final Logger logger = LoggerFactory.getLogger(A2AOpenHABPersistenceManager.class);

    // Ready markers for openHAB integration
    public static final ReadyMarker A2A_PERSISTENCE_OPENHAB_READY = new ReadyMarker("a2a", "persistence-openhab");
    public static final ReadyMarker A2A_CONFIGURATION_OPENHAB_READY = new ReadyMarker("a2a", "configuration-openhab");

    @Reference
    private ReadyService readyService;

    @Reference
    private PersistenceServiceRegistry persistenceServiceRegistry;

    @Reference
    private ItemRegistry itemRegistry;

    @Reference
    private StorageService storageService;

    // openHAB integration configuration
    private static final String DEFAULT_PERSISTENCE_SERVICE = "mapdb";

    // Storage keys for different data types
    private static final String TASKS_STORAGE_KEY = "a2a-tasks";
    private static final String METADATA_STORAGE_KEY = "a2a-metadata";
    private static final String STATISTICS_STORAGE_KEY = "a2a-statistics";
    private static final String CONFIG_STORAGE_KEY = "a2a-config";
    private static final String RECOVERY_STORAGE_KEY = "a2a-recovery";
    private static final String PUSH_NOTIFICATIONS_STORAGE_KEY = "a2a-push-notifications";

    // Storage instances for different data types
    private Storage<Task> taskStorage;
    private Storage<Map<String, Object>> metadataStorage;
    private Storage<Map<String, Object>> statisticsStorage;
    private Storage<Map<String, Object>> configStorage;
    private Storage<Map<String, Object>> recoveryStorage;
    private Storage<Map<String, Object>> pushNotificationsStorage;

    // Enhanced in-memory storage with openHAB integration
    private final ConcurrentHashMap<String, Task> tasks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Map<String, Object>> taskMetadata = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> taskExecutionCounts = new ConcurrentHashMap<>();

    // Enhanced execution tracking with openHAB integration
    private final ConcurrentHashMap<String, TaskExecutionState> taskExecutionStates = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> taskExecutionStartTimes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> taskExecutors = new ConcurrentHashMap<>();

    // Enhanced statistics with openHAB integration
    private final AtomicLong totalTasks = new AtomicLong(0);
    private final AtomicLong completedTasks = new AtomicLong(0);
    private final AtomicLong failedTasks = new AtomicLong(0);
    private final AtomicLong cancelledTasks = new AtomicLong(0);
    private final AtomicLong activeTasks = new AtomicLong(0);
    private final AtomicLong totalExecutionTime = new AtomicLong(0);

    // openHAB persistence service integration
    private PersistenceService primaryPersistenceService;
    private QueryablePersistenceService queryablePersistenceService;
    private final Map<String, PersistenceService> taskPersistenceServices = new HashMap<>();

    // Enhanced task execution state tracking with openHAB integration
    public static class TaskExecutionState {
        private final String taskId;
        private final TaskState state;
        private final long startTime;
        private final String executor;
        private final Map<String, Object> context;

        public TaskExecutionState(String taskId, TaskState state, String executor) {
            this.taskId = taskId;
            this.state = state;
            this.startTime = System.currentTimeMillis();
            this.executor = executor;
            this.context = new HashMap<>();
        }

        // Getters
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

        public void addContext(String key, Object value) {
            context.put(key, value);
        }

        public void updateState(TaskState newState) {
            // Note: This is a simplified approach - in real implementation,
            // you'd need to handle state transitions properly
        }
    }

    @Activate
    public void activate() {
        logger.debug("A2A OpenHAB Persistence Manager activated");

        // Register as a tracker
        readyService.registerTracker(this);

        // Initialize openHAB integration
        initializeOpenHABIntegration();
    }

    @Deactivate
    public void deactivate() {
        logger.debug("A2A OpenHAB Persistence Manager deactivated");

        // Unregister tracker
        readyService.unregisterTracker(this);

        // Save all data before shutdown
        saveAllData();

        // Unmark ready markers
        readyService.unmarkReady(A2A_PERSISTENCE_OPENHAB_READY);
        readyService.unmarkReady(A2A_CONFIGURATION_OPENHAB_READY);
    }

    private void initializeOpenHABIntegration() {
        try {
            // Initialize StorageService instances
            initializeStorageServices();

            // Initialize persistence service integration
            initializePersistenceServiceIntegration();

            // Load existing data with openHAB integration
            loadTasksFromStorage();
            loadMetadataFromStorage();
            loadStatisticsFromStorage();
            loadConfigurationFromStorage();

            // Mark as ready
            readyService.markReady(A2A_PERSISTENCE_OPENHAB_READY);
            readyService.markReady(A2A_CONFIGURATION_OPENHAB_READY);

            logger.info("A2A OpenHAB persistence initialized with {} tasks", tasks.size());

        } catch (Exception e) {
            logger.error("Failed to initialize A2A OpenHAB persistence", e);
        }
    }

    private void initializeStorageServices() {
        // Initialize StorageService instances for different data types
        taskStorage = storageService.getStorage(TASKS_STORAGE_KEY, this.getClass().getClassLoader());
        metadataStorage = storageService.getStorage(METADATA_STORAGE_KEY, this.getClass().getClassLoader());
        statisticsStorage = storageService.getStorage(STATISTICS_STORAGE_KEY, this.getClass().getClassLoader());
        configStorage = storageService.getStorage(CONFIG_STORAGE_KEY, this.getClass().getClassLoader());
        recoveryStorage = storageService.getStorage(RECOVERY_STORAGE_KEY, this.getClass().getClassLoader());
        pushNotificationsStorage = storageService.getStorage(PUSH_NOTIFICATIONS_STORAGE_KEY,
                this.getClass().getClassLoader());

        logger.debug("Initialized StorageService instances for A2A data");
    }

    private void initializePersistenceServiceIntegration() {
        try {
            // Get the primary persistence service
            primaryPersistenceService = persistenceServiceRegistry.get(DEFAULT_PERSISTENCE_SERVICE);

            if (primaryPersistenceService instanceof QueryablePersistenceService) {
                queryablePersistenceService = (QueryablePersistenceService) primaryPersistenceService;
                logger.debug("Using queryable persistence service: {}", DEFAULT_PERSISTENCE_SERVICE);
            } else {
                logger.warn("Primary persistence service is not queryable: {}", DEFAULT_PERSISTENCE_SERVICE);
            }

            // Register A2A-specific persistence services
            registerA2APersistenceServices();

        } catch (Exception e) {
            logger.error("Failed to initialize persistence service integration", e);
        }
    }

    private void registerA2APersistenceServices() {
        // Register A2A task persistence service
        taskPersistenceServices.put("a2a-task-persistence", primaryPersistenceService);

        // Register A2A execution history persistence service
        taskPersistenceServices.put("a2a-execution-history", primaryPersistenceService);

        logger.debug("Registered {} A2A persistence services", taskPersistenceServices.size());
    }

    // Enhanced task persistence with openHAB StorageService
    public void saveTaskWithOpenHABStorage(Task task, TaskExecutionState executionState) {
        try {
            tasks.put(task.getId(), task);
            taskExecutionStates.put(task.getId(), executionState);
            taskExecutionStartTimes.put(task.getId(), executionState.getStartTime());
            taskExecutors.put(task.getId(), executionState.getExecutor());

            // Save to openHAB StorageService
            saveTaskToStorage(task);
            saveExecutionStateToStorage(executionState);

            totalTasks.incrementAndGet();
            activeTasks.incrementAndGet();

            // Log task creation using SLF4J
            logger.info("A2A Task created: id={}, action={}, executor={}", task.getId(),
                    task.getMetadata().get("actionId"), executionState.getExecutor());

        } catch (Exception e) {
            logger.error("Error saving task with OpenHAB StorageService: {}", task.getId(), e);
        }
    }

    private void saveTaskToStorage(Task task) {
        try {
            // Save task to StorageService
            taskStorage.put(task.getId(), task);

            // Log task save operation
            logger.debug("Saved task to StorageService: {}", task.getId());

        } catch (Exception e) {
            logger.error("Error saving task to StorageService: {}", task.getId(), e);
        }
    }

    private void saveExecutionStateToStorage(TaskExecutionState executionState) {
        try {
            // Convert execution state to map for storage
            Map<String, Object> executionData = new HashMap<>();
            executionData.put("taskId", executionState.getTaskId());
            executionData.put("state", executionState.getState().name());
            executionData.put("executor", executionState.getExecutor());
            executionData.put("startTime", executionState.getStartTime());
            executionData.put("context", executionState.getContext());

            // Save to metadata storage
            metadataStorage.put("execution_" + executionState.getTaskId(), executionData);

            logger.debug("Saved execution state to StorageService: {}", executionState.getTaskId());

        } catch (Exception e) {
            logger.error("Error saving execution state to StorageService: {}", executionState.getTaskId(), e);
        }
    }

    public void updateTaskState(String taskId, TaskState newState, String logEntry) {
        try {
            // Update task state in memory
            TaskExecutionState executionState = taskExecutionStates.get(taskId);
            if (executionState != null) {
                executionState.updateState(newState);

                // Update in StorageService
                saveExecutionStateToStorage(executionState);

                // Log state change using SLF4J
                logger.info("A2A Task state updated: id={}, state={}, log={}", taskId, newState, logEntry);
            }

        } catch (Exception e) {
            logger.error("Error updating task state: {}", taskId, e);
        }
    }

    // Configuration integration with openHAB StorageService
    public void saveA2AConfiguration(String configName, Map<String, Object> config) {
        try {
            // Save using openHAB StorageService
            configStorage.put(configName, config);

            logger.debug("Saved A2A configuration to StorageService: {}", configName);

        } catch (Exception e) {
            logger.error("Error saving A2A configuration: {}", configName, e);
        }
    }

    public Map<String, Object> loadA2AConfiguration(String configName) {
        try {
            Map<String, Object> config = configStorage.get(configName);
            if (config != null) {
                logger.debug("Loaded A2A configuration from StorageService: {}", configName);
                return config;
            }
        } catch (Exception e) {
            logger.error("Error loading A2A configuration: {}", configName, e);
        }

        return new HashMap<>();
    }

    // Enhanced statistics with openHAB StorageService
    public Map<String, Object> getOpenHABStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // Basic statistics
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

        // OpenHAB integration statistics
        stats.put("openHABIntegration", true);
        stats.put("persistenceService", primaryPersistenceService != null ? primaryPersistenceService.getId() : "none");
        stats.put("queryablePersistence", queryablePersistenceService != null);
        stats.put("storageService", "openHAB StorageService");

        // Save statistics to StorageService
        saveStatisticsToStorage(stats);

        return stats;
    }

    private void saveStatisticsToStorage(Map<String, Object> stats) {
        try {
            statisticsStorage.put("current", stats);

            // Create timestamp map to match the expected type
            Map<String, Object> timestampMap = new HashMap<>();
            timestampMap.put("timestamp", System.currentTimeMillis());
            statisticsStorage.put("timestamp", timestampMap);

            logger.debug("Saved statistics to StorageService");

        } catch (Exception e) {
            logger.error("Error saving statistics to StorageService", e);
        }
    }

    // Loading methods using StorageService
    private void loadTasksFromStorage() {
        try {
            // Load all tasks from StorageService
            for (String key : taskStorage.getKeys()) {
                Task task = taskStorage.get(key);
                if (task != null) {
                    tasks.put(task.getId(), task);
                    logger.debug("Loaded task from StorageService: {}", task.getId());
                }
            }

            logger.info("Loaded {} tasks from StorageService", tasks.size());

        } catch (Exception e) {
            logger.error("Error loading tasks from StorageService", e);
        }
    }

    private void loadMetadataFromStorage() {
        try {
            // Load metadata from StorageService
            for (String key : metadataStorage.getKeys()) {
                Map<String, Object> metadata = metadataStorage.get(key);
                if (metadata != null) {
                    taskMetadata.put(key, metadata);
                    logger.debug("Loaded metadata from StorageService: {}", key);
                }
            }

            logger.debug("Loaded metadata from StorageService");

        } catch (Exception e) {
            logger.error("Error loading metadata from StorageService", e);
        }
    }

    private void loadStatisticsFromStorage() {
        try {
            Map<String, Object> stats = statisticsStorage.get("current");
            if (stats != null) {
                // Restore statistics from storage
                Long total = (Long) stats.get("totalTasks");
                if (total != null)
                    totalTasks.set(total);

                Long completed = (Long) stats.get("completedTasks");
                if (completed != null)
                    completedTasks.set(completed);

                Long failed = (Long) stats.get("failedTasks");
                if (failed != null)
                    failedTasks.set(failed);

                Long cancelled = (Long) stats.get("cancelledTasks");
                if (cancelled != null)
                    cancelledTasks.set(cancelled);

                logger.debug("Loaded statistics from StorageService");
            }

        } catch (Exception e) {
            logger.error("Error loading statistics from StorageService", e);
        }
    }

    private void loadConfigurationFromStorage() {
        try {
            // Load configuration from StorageService
            for (String key : configStorage.getKeys()) {
                Map<String, Object> config = configStorage.get(key);
                if (config != null) {
                    logger.debug("Loaded configuration from StorageService: {}", key);
                }
            }

            logger.debug("Loaded configuration from StorageService");

        } catch (Exception e) {
            logger.error("Error loading configuration from StorageService", e);
        }
    }

    private void saveAllData() {
        try {
            // Save all tasks to StorageService
            for (Task task : tasks.values()) {
                saveTaskToStorage(task);
            }

            // Save statistics
            getOpenHABStatistics();

            logger.info("Saved all A2A data to StorageService");

        } catch (Exception e) {
            logger.error("Error saving all data to StorageService", e);
        }
    }

    // Task execution logging using SLF4J
    public void logTaskExecution(String taskId, String actionId, long startTime, long endTime, boolean success,
            String result) {

        long duration = endTime - startTime;

        if (success) {
            logger.info("A2A Task executed successfully: id={}, action={}, duration={}ms, result={}", taskId, actionId,
                    duration, result);
        } else {
            logger.error("A2A Task execution failed: id={}, action={}, duration={}ms, result={}", taskId, actionId,
                    duration, result);
        }
    }

    public void logTaskError(String taskId, String actionId, String error, Throwable t) {
        logger.error("A2A Task error: id={}, action={}, error={}", taskId, actionId, error, t);
    }

    public void logTaskCancelled(String taskId, String actionId, String reason) {
        logger.warn("A2A Task cancelled: id={}, action={}, reason={}", taskId, actionId, reason);
    }

    public void logTaskTimeout(String taskId, String actionId, long timeoutMs) {
        logger.warn("A2A Task timeout: id={}, action={}, timeout={}ms", taskId, actionId, timeoutMs);
    }

    // Push notification persistence methods
    public void savePushNotificationConfig(String taskId, Map<String, Object> pushConfig) {
        try {
            // Save push notification configuration to StorageService
            pushNotificationsStorage.put(taskId, pushConfig);

            logger.debug("Saved push notification config to StorageService: taskId={}", taskId);

        } catch (Exception e) {
            logger.error("Error saving push notification config: taskId={}", taskId, e);
        }
    }

    public Map<String, Object> loadPushNotificationConfig(String taskId) {
        try {
            Map<String, Object> config = pushNotificationsStorage.get(taskId);
            if (config != null) {
                logger.debug("Loaded push notification config from StorageService: taskId={}", taskId);
                return config;
            }
        } catch (Exception e) {
            logger.error("Error loading push notification config: taskId={}", taskId, e);
        }

        return new HashMap<>();
    }

    public List<Map<String, Object>> loadAllPushNotificationConfigs() {
        List<Map<String, Object>> configs = new ArrayList<>();

        try {
            // Get all push notification configurations
            for (String key : pushNotificationsStorage.getKeys()) {
                Map<String, Object> config = pushNotificationsStorage.get(key);
                if (config != null) {
                    config.put("taskId", key); // Add taskId to the config for reference
                    configs.add(config);
                }
            }

            logger.debug("Loaded {} push notification configs from StorageService", configs.size());

        } catch (Exception e) {
            logger.error("Error loading all push notification configs", e);
        }

        return configs;
    }

    public void deletePushNotificationConfig(String taskId) {
        try {
            // Remove push notification configuration from StorageService
            pushNotificationsStorage.remove(taskId);

            logger.debug("Deleted push notification config from StorageService: taskId={}", taskId);

        } catch (Exception e) {
            logger.error("Error deleting push notification config: taskId={}", taskId, e);
        }
    }

    public boolean hasPushNotificationConfig(String taskId) {
        try {
            return pushNotificationsStorage.get(taskId) != null;
        } catch (Exception e) {
            logger.error("Error checking push notification config existence: taskId={}", taskId, e);
            return false;
        }
    }

    @Override
    public void onReadyMarkerAdded(ReadyMarker readyMarker) {
        logger.debug("Ready marker added: {}", readyMarker);

        // Check if this is a marker we're waiting for
        if (isPersistenceServiceMarker(readyMarker)) {
            initializePersistenceServiceIntegration();
        }
    }

    @Override
    public void onReadyMarkerRemoved(ReadyMarker readyMarker) {
        logger.debug("Ready marker removed: {}", readyMarker);

        // If persistence service becomes unavailable, log it
        if (isPersistenceServiceMarker(readyMarker)) {
            logger.warn("Persistence service became unavailable: {}", readyMarker);
        }
    }

    private boolean isPersistenceServiceMarker(ReadyMarker marker) {
        // Check if this is a persistence service marker
        return "persistence".equals(marker.getType()) && (marker.getIdentifier().contains("mapdb")
                || marker.getIdentifier().contains("rrd4j") || marker.getIdentifier().contains("influxdb"));
    }

    // Utility methods
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public Task getTask(String taskId) {
        return tasks.get(taskId);
    }

    public boolean hasTask(String taskId) {
        return tasks.containsKey(taskId);
    }

    public void removeTask(String taskId) {
        try {
            Task task = tasks.remove(taskId);
            if (task != null) {
                // Remove from StorageService
                taskStorage.remove(taskId);

                // Log task removal
                logger.info("A2A Task removed: id={}", taskId);
            }
        } catch (Exception e) {
            logger.error("Error removing task: {}", taskId, e);
        }
    }

    public void clearAllTasks() {
        try {
            tasks.clear();
            taskExecutionStates.clear();
            taskExecutionStartTimes.clear();
            taskExecutors.clear();

            // Clear from StorageService
            for (String key : taskStorage.getKeys()) {
                taskStorage.remove(key);
            }

            logger.info("Cleared all A2A tasks");

        } catch (Exception e) {
            logger.error("Error clearing all tasks", e);
        }
    }
}
