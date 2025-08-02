package org.openhab.core.ai.a2a.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.a2a.internal.A2AOpenHABPersistenceManager;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.persistence.PersistenceService;
import org.openhab.core.persistence.PersistenceServiceRegistry;
import org.openhab.core.persistence.QueryablePersistenceService;
import org.openhab.core.service.ReadyService;
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;

import io.a2a.spec.Task;
import io.a2a.spec.TaskState;

@ExtendWith(MockitoExtension.class)
class A2AOpenHABPersistenceManagerTest {

    @Mock
    private ReadyService mockReadyService;

    @Mock
    private PersistenceServiceRegistry mockPersistenceServiceRegistry;

    @Mock
    private ItemRegistry mockItemRegistry;

    @Mock
    private StorageService mockStorageService;

    @Mock
    private PersistenceService mockPersistenceService;

    @Mock
    private QueryablePersistenceService mockQueryablePersistenceService;

    @Mock
    private Storage<Task> mockTaskStorage;

    @Mock
    private Storage<Map<String, Object>> mockMetadataStorage;

    @Mock
    private Storage<Map<String, Object>> mockStatisticsStorage;

    @Mock
    private Storage<Map<String, Object>> mockConfigStorage;

    @Mock
    private Storage<Map<String, Object>> mockRecoveryStorage;

    @Mock
    private Storage<Map<String, Object>> mockPushNotificationsStorage;

    @Mock
    private Task mockTask;

    private A2AOpenHABPersistenceManager persistenceManager;

    @BeforeEach
    void setUp() {
        persistenceManager = new A2AOpenHABPersistenceManager();

        // Setup storage mocks with proper type casting
        when(mockStorageService.getStorage(eq("a2a-tasks"), any())).thenReturn((Storage) mockTaskStorage);
        when(mockStorageService.getStorage(eq("a2a-metadata"), any())).thenReturn((Storage) mockMetadataStorage);
        when(mockStorageService.getStorage(eq("a2a-statistics"), any())).thenReturn((Storage) mockStatisticsStorage);
        when(mockStorageService.getStorage(eq("a2a-config"), any())).thenReturn((Storage) mockConfigStorage);
        when(mockStorageService.getStorage(eq("a2a-recovery"), any())).thenReturn((Storage) mockRecoveryStorage);
        when(mockStorageService.getStorage(eq("a2a-push-notifications"), any()))
                .thenReturn((Storage) mockPushNotificationsStorage);

        // Setup persistence service mocks
        when(mockPersistenceServiceRegistry.get("mapdb")).thenReturn(mockPersistenceService);
        when(mockPersistenceService.getId()).thenReturn("mapdb");

        // Setup mockTask
        when(mockTask.getId()).thenReturn("test-task-1");
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("actionId", "test-action");
        when(mockTask.getMetadata()).thenReturn(metadata);
    }

    @Test
    void testActivate() {
        // Test activation
        persistenceManager.activate();

        // Verify ready service registration
        verify(mockReadyService, times(1)).registerTracker(persistenceManager);
    }

    @Test
    void testDeactivate() {
        // Activate first
        persistenceManager.activate();

        // Test deactivation
        persistenceManager.deactivate();

        // Verify ready service unregistration
        verify(mockReadyService, times(1)).unregisterTracker(persistenceManager);
        verify(mockReadyService, times(1)).unmarkReady(A2AOpenHABPersistenceManager.A2A_PERSISTENCE_OPENHAB_READY);
        verify(mockReadyService, times(1)).unmarkReady(A2AOpenHABPersistenceManager.A2A_CONFIGURATION_OPENHAB_READY);
    }

    @Test
    void testSaveTaskWithOpenHABStorage() {
        // Setup
        Task task = createTestTask("test-task-1", "test-action");
        A2AOpenHABPersistenceManager.TaskExecutionState executionState = new A2AOpenHABPersistenceManager.TaskExecutionState(
                "test-task-1", TaskState.SUBMITTED, "test-executor");

        // Execute
        persistenceManager.saveTaskWithOpenHABStorage(task, executionState);

        // Verify task storage
        verify(mockTaskStorage, times(1)).put("test-task-1", task);
        verify(mockMetadataStorage, times(1)).put(eq("execution_test-task-1"), any(Map.class));
    }

    @Test
    void testUpdateTaskState() {
        // Setup
        A2AOpenHABPersistenceManager.TaskExecutionState executionState = new A2AOpenHABPersistenceManager.TaskExecutionState(
                "test-task-1", TaskState.SUBMITTED, "test-executor");
        persistenceManager.saveTaskWithOpenHABStorage(mockTask, executionState);

        // Execute
        persistenceManager.updateTaskState("test-task-1", TaskState.WORKING, "Task started");

        // Verify state update
        verify(mockMetadataStorage, times(2)).put(eq("execution_test-task-1"), any(Map.class));
    }

    @Test
    void testSaveA2AConfiguration() {
        // Setup
        Map<String, Object> config = new HashMap<>();
        config.put("key1", "value1");
        config.put("key2", "value2");

        // Execute
        persistenceManager.saveA2AConfiguration("test-config", config);

        // Verify configuration storage
        verify(mockConfigStorage, times(1)).put("test-config", config);
    }

    @Test
    void testLoadA2AConfiguration() {
        // Setup
        Map<String, Object> expectedConfig = new HashMap<>();
        expectedConfig.put("key1", "value1");
        when(mockConfigStorage.get("test-config")).thenReturn(expectedConfig);

        // Execute
        Map<String, Object> result = persistenceManager.loadA2AConfiguration("test-config");

        // Verify
        assertEquals(expectedConfig, result);
        verify(mockConfigStorage, times(1)).get("test-config");
    }

    @Test
    void testLoadA2AConfigurationNotFound() {
        // Setup
        when(mockConfigStorage.get("nonexistent-config")).thenReturn(null);

        // Execute
        Map<String, Object> result = persistenceManager.loadA2AConfiguration("nonexistent-config");

        // Verify
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetOpenHABStatistics() {
        // Setup
        A2AOpenHABPersistenceManager.TaskExecutionState executionState = new A2AOpenHABPersistenceManager.TaskExecutionState(
                "test-task-1", TaskState.SUBMITTED, "test-executor");
        persistenceManager.saveTaskWithOpenHABStorage(mockTask, executionState);

        // Execute
        Map<String, Object> stats = persistenceManager.getOpenHABStatistics();

        // Verify basic statistics
        assertNotNull(stats);
        assertEquals(1L, stats.get("totalTasks"));
        assertEquals(1L, stats.get("activeTasks"));
        assertEquals(0.0, stats.get("successRate"));

        // Verify openHAB integration statistics
        assertTrue((Boolean) stats.get("openHABIntegration"));
        assertEquals("mapdb", stats.get("persistenceService"));
        assertTrue((Boolean) stats.get("queryablePersistence"));
        assertEquals("openHAB StorageService", stats.get("storageService"));

        // Verify statistics storage
        verify(mockStatisticsStorage, times(1)).put("current", stats);
        verify(mockStatisticsStorage, times(1)).put("timestamp", any(Map.class));
    }

    @Test
    void testLogTaskExecution() {
        // Execute
        persistenceManager.logTaskExecution("test-task-1", "test-action", 1000L, 2000L, true, "Success");

        // Verify - this method only logs, so we just verify it doesn't throw exceptions
        assertDoesNotThrow(() -> {
            persistenceManager.logTaskExecution("test-task-1", "test-action", 1000L, 2000L, false, "Error");
        });
    }

    @Test
    void testLogTaskError() {
        // Execute
        Exception testException = new RuntimeException("Test error");
        persistenceManager.logTaskError("test-task-1", "test-action", "Test error message", testException);

        // Verify - this method only logs, so we just verify it doesn't throw exceptions
        assertDoesNotThrow(() -> {
            persistenceManager.logTaskError("test-task-1", "test-action", "Another error", testException);
        });
    }

    @Test
    void testLogTaskCancelled() {
        // Execute
        persistenceManager.logTaskCancelled("test-task-1", "test-action", "User cancelled");

        // Verify - this method only logs, so we just verify it doesn't throw exceptions
        assertDoesNotThrow(() -> {
            persistenceManager.logTaskCancelled("test-task-1", "test-action", "Timeout");
        });
    }

    @Test
    void testLogTaskTimeout() {
        // Execute
        persistenceManager.logTaskTimeout("test-task-1", "test-action", 5000L);

        // Verify - this method only logs, so we just verify it doesn't throw exceptions
        assertDoesNotThrow(() -> {
            persistenceManager.logTaskTimeout("test-task-1", "test-action", 10000L);
        });
    }

    @Test
    void testSavePushNotificationConfig() {
        // Setup
        Map<String, Object> pushConfig = new HashMap<>();
        pushConfig.put("enabled", true);
        pushConfig.put("url", "https://example.com/webhook");

        // Execute
        persistenceManager.savePushNotificationConfig("test-task-1", pushConfig);

        // Verify
        verify(mockPushNotificationsStorage, times(1)).put("test-task-1", pushConfig);
    }

    @Test
    void testLoadPushNotificationConfig() {
        // Setup
        Map<String, Object> expectedConfig = new HashMap<>();
        expectedConfig.put("enabled", true);
        expectedConfig.put("url", "https://example.com/webhook");
        when(mockPushNotificationsStorage.get("test-task-1")).thenReturn(expectedConfig);

        // Execute
        Map<String, Object> result = persistenceManager.loadPushNotificationConfig("test-task-1");

        // Verify
        assertEquals(expectedConfig, result);
        verify(mockPushNotificationsStorage, times(1)).get("test-task-1");
    }

    @Test
    void testLoadPushNotificationConfigNotFound() {
        // Setup
        when(mockPushNotificationsStorage.get("nonexistent-task")).thenReturn(null);

        // Execute
        Map<String, Object> result = persistenceManager.loadPushNotificationConfig("nonexistent-task");

        // Verify
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testLoadAllPushNotificationConfigs() {
        // Setup
        Map<String, Object> config1 = new HashMap<>();
        config1.put("enabled", true);
        Map<String, Object> config2 = new HashMap<>();
        config2.put("enabled", false);

        when(mockPushNotificationsStorage.getKeys()).thenReturn(List.of("task1", "task2"));
        when(mockPushNotificationsStorage.get("task1")).thenReturn(config1);
        when(mockPushNotificationsStorage.get("task2")).thenReturn(config2);

        // Execute
        List<Map<String, Object>> result = persistenceManager.loadAllPushNotificationConfigs();

        // Verify
        assertEquals(2, result.size());
        assertTrue(result.get(0).containsKey("taskId"));
        assertTrue(result.get(1).containsKey("taskId"));
    }

    @Test
    void testDeletePushNotificationConfig() {
        // Execute
        persistenceManager.deletePushNotificationConfig("test-task-1");

        // Verify
        verify(mockPushNotificationsStorage, times(1)).remove("test-task-1");
    }

    @Test
    void testHasPushNotificationConfig() {
        // Setup
        when(mockPushNotificationsStorage.get("test-task-1")).thenReturn(new HashMap<>());
        when(mockPushNotificationsStorage.get("nonexistent-task")).thenReturn(null);

        // Execute and verify
        assertTrue(persistenceManager.hasPushNotificationConfig("test-task-1"));
        assertFalse(persistenceManager.hasPushNotificationConfig("nonexistent-task"));
    }

    @Test
    void testOnReadyMarkerAdded() {
        // Execute
        persistenceManager.onReadyMarkerAdded(new org.openhab.core.service.ReadyMarker("persistence", "mapdb"));

        // Verify - this method only logs, so we just verify it doesn't throw exceptions
        assertDoesNotThrow(() -> {
            persistenceManager.onReadyMarkerAdded(new org.openhab.core.service.ReadyMarker("other", "service"));
        });
    }

    @Test
    void testOnReadyMarkerRemoved() {
        // Execute
        persistenceManager.onReadyMarkerRemoved(new org.openhab.core.service.ReadyMarker("persistence", "mapdb"));

        // Verify - this method only logs, so we just verify it doesn't throw exceptions
        assertDoesNotThrow(() -> {
            persistenceManager.onReadyMarkerRemoved(new org.openhab.core.service.ReadyMarker("other", "service"));
        });
    }

    @Test
    void testGetAllTasks() {
        // Setup
        A2AOpenHABPersistenceManager.TaskExecutionState executionState = new A2AOpenHABPersistenceManager.TaskExecutionState(
                "test-task-1", TaskState.SUBMITTED, "test-executor");
        persistenceManager.saveTaskWithOpenHABStorage(mockTask, executionState);

        // Execute
        List<Task> result = persistenceManager.getAllTasks();

        // Verify
        assertEquals(1, result.size());
        assertEquals("test-task-1", result.get(0).getId());
    }

    @Test
    void testGetTask() {
        // Setup
        A2AOpenHABPersistenceManager.TaskExecutionState executionState = new A2AOpenHABPersistenceManager.TaskExecutionState(
                "test-task-1", TaskState.SUBMITTED, "test-executor");
        persistenceManager.saveTaskWithOpenHABStorage(mockTask, executionState);

        // Execute
        Task result = persistenceManager.getTask("test-task-1");
        Task nonexistent = persistenceManager.getTask("nonexistent-task");

        // Verify
        assertEquals(mockTask, result);
        assertNull(nonexistent);
    }

    @Test
    void testHasTask() {
        // Setup
        A2AOpenHABPersistenceManager.TaskExecutionState executionState = new A2AOpenHABPersistenceManager.TaskExecutionState(
                "test-task-1", TaskState.SUBMITTED, "test-executor");
        persistenceManager.saveTaskWithOpenHABStorage(mockTask, executionState);

        // Execute and verify
        assertTrue(persistenceManager.hasTask("test-task-1"));
        assertFalse(persistenceManager.hasTask("nonexistent-task"));
    }

    @Test
    void testRemoveTask() {
        // Setup
        A2AOpenHABPersistenceManager.TaskExecutionState executionState = new A2AOpenHABPersistenceManager.TaskExecutionState(
                "test-task-1", TaskState.SUBMITTED, "test-executor");
        persistenceManager.saveTaskWithOpenHABStorage(mockTask, executionState);

        // Execute
        persistenceManager.removeTask("test-task-1");

        // Verify
        verify(mockTaskStorage, times(1)).remove("test-task-1");
        assertFalse(persistenceManager.hasTask("test-task-1"));
    }

    @Test
    void testClearAllTasks() {
        // Setup
        A2AOpenHABPersistenceManager.TaskExecutionState executionState = new A2AOpenHABPersistenceManager.TaskExecutionState(
                "test-task-1", TaskState.SUBMITTED, "test-executor");
        persistenceManager.saveTaskWithOpenHABStorage(mockTask, executionState);

        when(mockTaskStorage.getKeys()).thenReturn(List.of("test-task-1"));

        // Execute
        persistenceManager.clearAllTasks();

        // Verify
        verify(mockTaskStorage, times(1)).remove("test-task-1");
        assertTrue(persistenceManager.getAllTasks().isEmpty());
    }

    @Test
    void testTaskExecutionState() {
        // Test TaskExecutionState inner class
        A2AOpenHABPersistenceManager.TaskExecutionState state = new A2AOpenHABPersistenceManager.TaskExecutionState(
                "test-task-1", TaskState.SUBMITTED, "test-executor");

        // Verify initial state
        assertEquals("test-task-1", state.getTaskId());
        assertEquals(TaskState.SUBMITTED, state.getState());
        assertEquals("test-executor", state.getExecutor());
        assertNotNull(state.getContext());
        assertTrue(state.getStartTime() > 0);

        // Test context operations
        state.addContext("key1", "value1");
        assertEquals("value1", state.getContext().get("key1"));

        // Test state update (this is a simplified implementation)
        assertDoesNotThrow(() -> state.updateState(TaskState.WORKING));
    }

    @Test
    void testInitializeWithNullPersistenceService() {
        // Setup
        when(mockPersistenceServiceRegistry.get("mapdb")).thenReturn(null);

        // Execute - should not throw exception
        assertDoesNotThrow(() -> {
            persistenceManager.activate();
        });
    }

    @Test
    void testInitializeWithNonQueryablePersistenceService() {
        // Setup
        when(mockPersistenceServiceRegistry.get("mapdb")).thenReturn(mockPersistenceService);
        when(mockPersistenceService.getId()).thenReturn("mapdb");

        // Execute - should not throw exception
        assertDoesNotThrow(() -> {
            persistenceManager.activate();
        });
    }

    // Helper method to create a test Task
    private Task createTestTask(String taskId, String actionId) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("actionId", actionId);

        Task mockTask = mock(Task.class);
        when(mockTask.getId()).thenReturn(taskId);
        when(mockTask.getMetadata()).thenReturn(metadata);

        return mockTask;
    }
}
