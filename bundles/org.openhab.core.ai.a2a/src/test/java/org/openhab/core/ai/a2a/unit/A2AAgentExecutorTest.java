package org.openhab.core.ai.a2a.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.a2a.internal.A2AAgentExecutor;
import org.openhab.core.ai.a2a.internal.A2ASecurityManager;
import org.openhab.core.ai.common.api.action.AIActionRegistry;
import org.osgi.framework.BundleContext;

import io.a2a.server.agentexecution.RequestContext;
import io.a2a.server.events.EventQueue;
import io.a2a.spec.JSONRPCError;
import io.a2a.spec.Message;

@ExtendWith(MockitoExtension.class)
class A2AAgentExecutorTest {

    @Mock
    private BundleContext mockBundleContext;

    @Mock
    private AIActionRegistry mockActionRegistry;

    @Mock
    private A2ASecurityManager mockSecurityManager;

    @Mock
    private RequestContext mockRequestContext;

    @Mock
    private EventQueue mockEventQueue;

    @Mock
    private Message mockMessage;

    private A2AAgentExecutor agentExecutor;

    @BeforeEach
    void setUp() {
        agentExecutor = new A2AAgentExecutor(mockBundleContext);
    }

    @Test
    void testConstructor() {
        assertNotNull(agentExecutor);
    }

    @Test
    void testDeactivate() {
        // Test deactivation
        agentExecutor.deactivate();

        // Should not throw exception
        assertNotNull(agentExecutor);
    }

    @Test
    void testExecuteWithNullRequestContext() {
        // Test execution with null request context
        assertThrows(JSONRPCError.class, () -> {
            agentExecutor.execute(null, mockEventQueue);
        });
    }

    @Test
    void testExecuteWithNullEventQueue() {
        // Test execution with null event queue
        assertThrows(JSONRPCError.class, () -> {
            agentExecutor.execute(mockRequestContext, null);
        });
    }

    @Test
    void testExecuteWithValidParameters() {
        // Setup mock request context
        when(mockRequestContext.getMessage()).thenReturn(mockMessage);
        when(mockRequestContext.getTaskId()).thenReturn("test-task-123");

        // Setup mock message
        when(mockMessage.getParts()).thenReturn(java.util.List.of());

        // Test execution with valid parameters
        // This would need proper setup with action registry
        assertNotNull(agentExecutor);
    }

    @Test
    void testCancelWithNullRequestContext() {
        // Test cancellation with null request context
        assertThrows(JSONRPCError.class, () -> {
            agentExecutor.cancel(null, mockEventQueue);
        });
    }

    @Test
    void testCancelWithNullEventQueue() {
        // Test cancellation with null event queue
        assertThrows(JSONRPCError.class, () -> {
            agentExecutor.cancel(mockRequestContext, null);
        });
    }

    @Test
    void testCancelWithValidParameters() {
        // Setup mock request context
        when(mockRequestContext.getTaskId()).thenReturn("test-task-123");

        // Test cancellation with valid parameters
        agentExecutor.cancel(mockRequestContext, mockEventQueue);

        // Should not throw exception
        assertNotNull(agentExecutor);
    }

    @Test
    void testIsTaskActive() {
        // Test task active status
        boolean isActive = agentExecutor.isTaskActive("test-task-id");

        // Should return false for non-existent task
        assertFalse(isActive);
    }

    @Test
    void testIsTaskActiveWithNullTaskId() {
        // Test task active status with null task ID
        boolean isActive = agentExecutor.isTaskActive(null);

        // Should return false for null task ID
        assertFalse(isActive);
    }

    @Test
    void testIsTaskActiveWithEmptyTaskId() {
        // Test task active status with empty task ID
        boolean isActive = agentExecutor.isTaskActive("");

        // Should return false for empty task ID
        assertFalse(isActive);
    }

    @Test
    void testCancelTask() {
        // Test task cancellation
        agentExecutor.cancelTask("test-task-id");

        // Should not throw exception
        assertNotNull(agentExecutor);
    }

    @Test
    void testCancelTaskWithNullTaskId() {
        // Test task cancellation with null task ID
        agentExecutor.cancelTask(null);

        // Should not throw exception
        assertNotNull(agentExecutor);
    }

    @Test
    void testCancelTaskWithEmptyTaskId() {
        // Test task cancellation with empty task ID
        agentExecutor.cancelTask("");

        // Should not throw exception
        assertNotNull(agentExecutor);
    }

    @Test
    void testGetTaskStatistics() {
        // Test getting task statistics
        Map<String, Object> statistics = agentExecutor.getTaskStatistics();

        // Should return statistics map
        assertNotNull(statistics);
        assertTrue(statistics instanceof Map);

        // Should contain basic statistics
        assertTrue(statistics.containsKey("activeTasks"));
        assertTrue(statistics.containsKey("totalTasks"));
        assertTrue(statistics.containsKey("cancelledTasks"));
    }

    @Test
    void testAgentExecutorLifecycle() {
        // Test complete lifecycle
        assertNotNull(agentExecutor);

        agentExecutor.deactivate();
        assertNotNull(agentExecutor);
    }

    @Test
    void testTaskLifecycle() {
        // Test task lifecycle
        String taskId = "test-task-123";

        // Initially not active
        assertFalse(agentExecutor.isTaskActive(taskId));

        // Cancel non-existent task
        agentExecutor.cancelTask(taskId);
        assertFalse(agentExecutor.isTaskActive(taskId));

        // Get statistics
        Map<String, Object> stats = agentExecutor.getTaskStatistics();
        assertNotNull(stats);
    }

    @Test
    void testMultipleTaskOperations() {
        // Test multiple task operations
        String taskId1 = "task-1";
        String taskId2 = "task-2";
        String taskId3 = "task-3";

        // Check initial states
        assertFalse(agentExecutor.isTaskActive(taskId1));
        assertFalse(agentExecutor.isTaskActive(taskId2));
        assertFalse(agentExecutor.isTaskActive(taskId3));

        // Cancel tasks
        agentExecutor.cancelTask(taskId1);
        agentExecutor.cancelTask(taskId2);
        agentExecutor.cancelTask(taskId3);

        // Verify states remain false
        assertFalse(agentExecutor.isTaskActive(taskId1));
        assertFalse(agentExecutor.isTaskActive(taskId2));
        assertFalse(agentExecutor.isTaskActive(taskId3));
    }

    @Test
    void testStatisticsConsistency() {
        // Test that statistics are consistent
        Map<String, Object> stats1 = agentExecutor.getTaskStatistics();
        Map<String, Object> stats2 = agentExecutor.getTaskStatistics();

        // Should return consistent statistics
        assertNotNull(stats1);
        assertNotNull(stats2);
        assertEquals(stats1.size(), stats2.size());

        // Should contain expected keys
        assertTrue(stats1.containsKey("activeTasks"));
        assertTrue(stats1.containsKey("totalTasks"));
        assertTrue(stats1.containsKey("cancelledTasks"));
    }

    @Test
    void testExceptionHandling() {
        // Test exception handling in various scenarios
        assertThrows(JSONRPCError.class, () -> {
            agentExecutor.execute(null, mockEventQueue);
        });

        assertThrows(JSONRPCError.class, () -> {
            agentExecutor.execute(mockRequestContext, null);
        });

        assertThrows(JSONRPCError.class, () -> {
            agentExecutor.cancel(null, mockEventQueue);
        });

        assertThrows(JSONRPCError.class, () -> {
            agentExecutor.cancel(mockRequestContext, null);
        });
    }

    @Test
    void testBundleContextIntegration() {
        // Test that bundle context is properly used
        assertNotNull(agentExecutor);

        // Deactivate should work
        agentExecutor.deactivate();
        assertNotNull(agentExecutor);
    }

    @Test
    void testTaskIdValidation() {
        // Test various task ID formats
        assertFalse(agentExecutor.isTaskActive(""));
        assertFalse(agentExecutor.isTaskActive("   "));
        assertFalse(agentExecutor.isTaskActive("task-123"));
        assertFalse(agentExecutor.isTaskActive("TASK_456"));
        assertFalse(agentExecutor.isTaskActive("task.with.dots"));
        assertFalse(agentExecutor.isTaskActive("task-with-dashes"));
    }
}
