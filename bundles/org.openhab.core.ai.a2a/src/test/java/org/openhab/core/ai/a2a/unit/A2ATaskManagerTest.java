package org.openhab.core.ai.a2a.unit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.a2a.internal.A2ATaskManager;
import org.openhab.core.ai.a2a.internal.A2ASkillRegistry;
import org.openhab.core.ai.a2a.internal.A2AOpenHABPersistenceManager;
import org.openhab.core.ai.a2a.internal.A2ASynchronizationService;
import org.openhab.core.ai.a2a.internal.A2AAgentRegistry;
import org.openhab.core.ai.common.action.AIActionRegistry;

/**
 * Unit tests for the enhanced A2ATaskManager with deadlock prevention, resource locking,
 * transaction support, and fault tolerance features.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@ExtendWith(MockitoExtension.class)
public class A2ATaskManagerTest {

    private static final String TEST_TASK_ID = "test-task-1";
    private static final String TEST_TASK_ID_2 = "test-task-2";
    private static final String TEST_AGENT_ID = "test-agent-1";
    private static final String TEST_AGENT_ID_2 = "test-agent-2";
    private static final String TEST_TRANSACTION_ID = "test-transaction-1";

    @Mock
    private AIActionRegistry actionRegistry;

    @Mock
    private A2ASkillRegistry skillRegistry;

    @Mock
    private A2AOpenHABPersistenceManager persistenceManager;

    @Mock
    private A2ASynchronizationService synchronizationService;

    @Mock
    private A2AAgentRegistry agentRegistry;

    private A2ATaskManager taskManager;

    @BeforeEach
    public void setUp() {
        taskManager = new A2ATaskManager();
        // Use reflection to set the mocked dependencies
        setField(taskManager, "actionRegistry", actionRegistry);
        setField(taskManager, "skillRegistry", skillRegistry);
        setField(taskManager, "persistenceManager", persistenceManager);
        setField(taskManager, "synchronizationService", synchronizationService);
        setField(taskManager, "agentRegistry", agentRegistry);
    }

    @Test
    public void testTransactionSupport() {
        // Create transaction
        A2ATaskManager.TaskTransaction transaction = taskManager.createTransaction(TEST_TRANSACTION_ID);
        assertNotNull(transaction);
        assertEquals(TEST_TRANSACTION_ID, transaction.getTransactionId());
        assertFalse(transaction.isCommitted());
        assertFalse(transaction.isRolledBack());

        // Test transaction state
        transaction.assignAgent(TEST_TASK_ID, TEST_AGENT_ID);
        assertFalse(transaction.isCommitted());
        assertFalse(transaction.isRolledBack());
    }

    @Test
    public void testTransactionRollback() {
        // Create transaction
        A2ATaskManager.TaskTransaction transaction = taskManager.createTransaction(TEST_TRANSACTION_ID);
        
        // Rollback transaction
        transaction.rollback();
        assertTrue(transaction.isRolledBack());
        assertFalse(transaction.isCommitted());
    }

    @Test
    public void testRetryContext() {
        A2ATaskManager.RetryContext retryContext = new A2ATaskManager.RetryContext(TEST_TASK_ID, 3);
        
        assertEquals(TEST_TASK_ID, retryContext.getTaskId());
        assertEquals(3, retryContext.getMaxRetries());
        assertEquals(0, retryContext.getCurrentAttempts());
        assertTrue(retryContext.canRetry());

        // Test retry attempts
        retryContext.incrementAttempt();
        assertEquals(1, retryContext.getCurrentAttempts());
        assertTrue(retryContext.canRetry());

        retryContext.incrementAttempt();
        retryContext.incrementAttempt();
        assertEquals(3, retryContext.getCurrentAttempts());
        assertFalse(retryContext.canRetry());
    }

    @Test
    public void testFallbackStrategy() {
        A2ATaskManager.FallbackStrategy fallback = new A2ATaskManager.FallbackStrategy(
            TEST_TASK_ID, "ERROR_RESPONSE", taskManager);

        assertEquals(TEST_TASK_ID, fallback.getTaskId());
        assertEquals("ERROR_RESPONSE", fallback.getStrategyType());
    }

    @Test
    public void testTaskMetrics() {
        A2ATaskManager.TaskMetrics metrics = new A2ATaskManager.TaskMetrics(TEST_TASK_ID);
        
        assertEquals(TEST_TASK_ID, metrics.getTaskId());
        assertEquals(0, metrics.getExecutionCount());
        assertEquals(0, metrics.getSuccessCount());
        assertEquals(0, metrics.getErrorCount());
        assertEquals(0, metrics.getCancellationCount());

        // Record some metrics
        metrics.recordExecution(1000L);
        metrics.recordSuccess();
        metrics.recordError(new RuntimeException("Test error"));

        assertEquals(1, metrics.getExecutionCount());
        assertEquals(1, metrics.getSuccessCount());
        assertEquals(1, metrics.getErrorCount());
        assertEquals(1000.0, metrics.getAverageExecutionTime(), 0.01);
    }

    @Test
    public void testTaskOrchestrationState() {
        A2ATaskManager.TaskOrchestrationState state = new A2ATaskManager.TaskOrchestrationState(TEST_TASK_ID);
        
        assertEquals(TEST_TASK_ID, state.getTaskId());
        assertEquals(A2ATaskManager.TaskOrchestrationState.State.PENDING, state.getState());

        // Test state transitions
        state.setState(A2ATaskManager.TaskOrchestrationState.State.RUNNING);
        assertEquals(A2ATaskManager.TaskOrchestrationState.State.RUNNING, state.getState());

        state.setStartTime(System.currentTimeMillis());
        assertTrue(state.getStartTime() > 0);

        state.setState(A2ATaskManager.TaskOrchestrationState.State.COMPLETED);
        state.setEndTime(System.currentTimeMillis());
        assertTrue(state.getEndTime() > 0);
    }

    @Test
    public void testTaskManagerInitialization() {
        assertNotNull(taskManager);
        // Test that the task manager can be activated and deactivated
        assertDoesNotThrow(() -> {
            taskManager.activate();
            taskManager.deactivate();
        });
    }

    // Helper methods
    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }
} 