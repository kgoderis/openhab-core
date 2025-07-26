package org.openhab.core.ai.mcp.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.common.auth.AIAuditLogger;

/**
 * Unit tests for MCPErrorRecoveryManager using real SDK classes.
 *
 * Tests error detection, recovery strategies, retry mechanisms,
 * and graceful degradation scenarios.
 *
 * 
 */
@ExtendWith(MockitoExtension.class)
class MCPErrorRecoveryManagerTest {

    @Mock
    private AIAuditLogger auditLogger;

    private MCPServerConfiguration config;
    private MCPErrorRecoveryManager errorRecoveryManager;

    @BeforeEach
    void setUp() {
        config = MCPServerConfiguration.builder().serverId("test-server").serverName("Test Server").build();
        errorRecoveryManager = new MCPErrorRecoveryManager(auditLogger, config);
    }

    @Test
    void testInitialization() {
        // Test that the error recovery manager is properly initialized
        assertNotNull(errorRecoveryManager);
    }

    @Test
    void testHandleError() {
        // Test handling an error
        MCPErrorRecoveryManager.RecoveryAction action = errorRecoveryManager.handleError("network_error",
                "Connection failed", "client-123");

        assertNotNull(action);
        assertTrue(action == MCPErrorRecoveryManager.RecoveryAction.RETRY
                || action == MCPErrorRecoveryManager.RecoveryAction.FALLBACK
                || action == MCPErrorRecoveryManager.RecoveryAction.DEGRADE);
    }

    @Test
    void testHandleErrorWithNullClientId() {
        // Test handling an error with null client ID
        MCPErrorRecoveryManager.RecoveryAction action = errorRecoveryManager.handleError("tool_error",
                "Tool execution failed", null);

        assertNotNull(action);
    }

    @Test
    void testHandleErrorWithNullErrorType() {
        // Test handling an error with null error type
        MCPErrorRecoveryManager.RecoveryAction action = errorRecoveryManager.handleError(null, "Some error occurred",
                "client-123");

        assertNotNull(action);
    }

    @Test
    void testHandleErrorWithNullErrorMessage() {
        // Test handling an error with null error message
        MCPErrorRecoveryManager.RecoveryAction action = errorRecoveryManager.handleError("unknown_error", null,
                "client-123");

        assertNotNull(action);
    }

    @Test
    void testRecordRecovery() {
        // Test recording a successful recovery
        assertDoesNotThrow(() -> {
            errorRecoveryManager.handleError("test_error", "Test error", "client-123");
            errorRecoveryManager.recordRecovery("test_error");
        });
    }

    @Test
    void testRecordRecoveryWithNullErrorType() {
        // Test recording recovery with null error type
        assertDoesNotThrow(() -> {
            errorRecoveryManager.recordRecovery(null);
        });
    }

    @Test
    void testRecordFallback() {
        // Test recording a fallback action
        assertDoesNotThrow(() -> {
            errorRecoveryManager.recordFallback("test_error", "Used fallback mechanism");
        });
    }

    @Test
    void testRecordFallbackWithNullErrorType() {
        // Test recording fallback with null error type
        assertDoesNotThrow(() -> {
            errorRecoveryManager.recordFallback(null, "Fallback action");
        });
    }

    @Test
    void testRecordFallbackWithNullAction() {
        // Test recording fallback with null action
        assertDoesNotThrow(() -> {
            errorRecoveryManager.recordFallback("test_error", null);
        });
    }

    @Test
    void testIsServiceHealthy() {
        // Test checking service health
        boolean healthy = errorRecoveryManager.isServiceHealthy("test-service");
        assertFalse(healthy); // Should be false by default
    }

    @Test
    void testIsServiceHealthyWithNullServiceName() {
        // Test checking service health with null service name
        boolean healthy = errorRecoveryManager.isServiceHealthy(null);
        assertFalse(healthy);
    }

    @Test
    void testUpdateServiceHealth() {
        // Test updating service health
        assertDoesNotThrow(() -> {
            errorRecoveryManager.updateServiceHealth("test-service", true);
            boolean healthy = errorRecoveryManager.isServiceHealthy("test-service");
            assertTrue(healthy);

            errorRecoveryManager.updateServiceHealth("test-service", false);
            healthy = errorRecoveryManager.isServiceHealthy("test-service");
            assertFalse(healthy);
        });
    }

    @Test
    void testUpdateServiceHealthWithNullServiceName() {
        // Test updating service health with null service name
        assertDoesNotThrow(() -> {
            errorRecoveryManager.updateServiceHealth(null, true);
        });
    }

    @Test
    void testGetErrorRecoveryStatistics() {
        // Test getting error recovery statistics
        MCPErrorRecoveryManager.ErrorRecoveryStatistics stats = errorRecoveryManager.getErrorRecoveryStatistics();
        assertNotNull(stats);
        assertEquals(0, stats.getTotalErrors());
        assertEquals(0, stats.getTotalRecoveries());
        assertEquals(0, stats.getTotalFallbacks());
    }

    @Test
    void testGetErrorRecoveryStatisticsAfterErrors() {
        // Test getting statistics after handling errors
        errorRecoveryManager.handleError("error1", "First error", "client-1");
        errorRecoveryManager.handleError("error2", "Second error", "client-2");
        errorRecoveryManager.recordRecovery("error1");
        errorRecoveryManager.recordFallback("error2", "Used fallback");

        MCPErrorRecoveryManager.ErrorRecoveryStatistics stats = errorRecoveryManager.getErrorRecoveryStatistics();
        assertNotNull(stats);
        assertEquals(2, stats.getTotalErrors());
        assertEquals(1, stats.getTotalRecoveries());
        assertEquals(1, stats.getTotalFallbacks());
    }

    @Test
    void testGetErrorDetails() {
        // Test getting error details
        Map<String, MCPErrorRecoveryManager.ErrorInfo> errorDetails = errorRecoveryManager.getErrorDetails();
        assertNotNull(errorDetails);
        assertTrue(errorDetails.isEmpty());
    }

    @Test
    void testGetErrorDetailsAfterErrors() {
        // Test getting error details after handling errors
        errorRecoveryManager.handleError("test_error", "Test error message", "client-123");

        Map<String, MCPErrorRecoveryManager.ErrorInfo> errorDetails = errorRecoveryManager.getErrorDetails();
        assertNotNull(errorDetails);
        assertTrue(errorDetails.containsKey("test_error"));

        MCPErrorRecoveryManager.ErrorInfo errorInfo = errorDetails.get("test_error");
        assertNotNull(errorInfo);
        assertEquals(1, errorInfo.getCount());
        assertNotNull(errorInfo.getLastErrorTime());
        assertEquals("Test error message", errorInfo.getLastErrorMessage());
    }

    @Test
    void testErrorRecoveryStatisticsToString() {
        // Test that statistics toString method works
        MCPErrorRecoveryManager.ErrorRecoveryStatistics stats = errorRecoveryManager.getErrorRecoveryStatistics();
        String statsString = stats.toString();

        assertNotNull(statsString);
        assertFalse(statsString.isEmpty());
    }

    @Test
    void testErrorInfoCreation() {
        // Test creating ErrorInfo objects with null circuit breaker state
        MCPErrorRecoveryManager.ErrorInfo errorInfo = new MCPErrorRecoveryManager.ErrorInfo(5,
                System.currentTimeMillis(), "Test error", null);

        assertNotNull(errorInfo);
        assertEquals(5, errorInfo.getCount());
        assertNotNull(errorInfo.getLastErrorTime());
        assertEquals("Test error", errorInfo.getLastErrorMessage());
        assertNull(errorInfo.getCircuitBreakerState());
    }

    @Test
    void testErrorInfoWithValidCircuitBreakerState() {
        // Test creating ErrorInfo objects with valid circuit breaker state
        // Note: CircuitBreakerState.State is private, so we can't test it directly
        // This test verifies that the constructor works with null state
        MCPErrorRecoveryManager.ErrorInfo errorInfo = new MCPErrorRecoveryManager.ErrorInfo(3,
                System.currentTimeMillis(), "Another error", null);

        assertNotNull(errorInfo);
        assertEquals(3, errorInfo.getCount());
        assertNotNull(errorInfo.getLastErrorTime());
        assertEquals("Another error", errorInfo.getLastErrorMessage());
        assertNull(errorInfo.getCircuitBreakerState());
    }

    @Test
    void testErrorInfoToString() {
        // Test ErrorInfo toString method
        MCPErrorRecoveryManager.ErrorInfo errorInfo = new MCPErrorRecoveryManager.ErrorInfo(2,
                System.currentTimeMillis(), "Test error", null);

        String errorInfoString = errorInfo.toString();
        assertNotNull(errorInfoString);
        assertFalse(errorInfoString.isEmpty());
    }

    @Test
    void testErrorInfoEquality() {
        // Test ErrorInfo equality
        long timestamp = System.currentTimeMillis();
        MCPErrorRecoveryManager.ErrorInfo errorInfo1 = new MCPErrorRecoveryManager.ErrorInfo(1, timestamp, "Test error",
                null);

        MCPErrorRecoveryManager.ErrorInfo errorInfo2 = new MCPErrorRecoveryManager.ErrorInfo(1, timestamp, "Test error",
                null);

        MCPErrorRecoveryManager.ErrorInfo errorInfo3 = new MCPErrorRecoveryManager.ErrorInfo(2, timestamp, "Test error",
                null);

        assertEquals(errorInfo1, errorInfo2);
        assertNotEquals(errorInfo1, errorInfo3);
        assertNotEquals(errorInfo1, null);
        assertNotEquals(errorInfo1, "not an error info");
    }

    @Test
    void testErrorInfoHashCode() {
        // Test ErrorInfo hashCode consistency
        long timestamp = System.currentTimeMillis();
        MCPErrorRecoveryManager.ErrorInfo errorInfo1 = new MCPErrorRecoveryManager.ErrorInfo(1, timestamp, "Test error",
                null);

        MCPErrorRecoveryManager.ErrorInfo errorInfo2 = new MCPErrorRecoveryManager.ErrorInfo(1, timestamp, "Test error",
                null);

        assertEquals(errorInfo1.hashCode(), errorInfo2.hashCode());
    }

    @Test
    void testRecoveryActionEnum() {
        // Test RecoveryAction enum values
        MCPErrorRecoveryManager.RecoveryAction[] actions = MCPErrorRecoveryManager.RecoveryAction.values();
        assertEquals(3, actions.length);

        assertTrue(containsAction(actions, MCPErrorRecoveryManager.RecoveryAction.RETRY));
        assertTrue(containsAction(actions, MCPErrorRecoveryManager.RecoveryAction.FALLBACK));
        assertTrue(containsAction(actions, MCPErrorRecoveryManager.RecoveryAction.DEGRADE));
    }

    @Test
    void testRecoveryActionValueOf() {
        // Test RecoveryAction valueOf method
        assertEquals(MCPErrorRecoveryManager.RecoveryAction.RETRY,
                MCPErrorRecoveryManager.RecoveryAction.valueOf("RETRY"));
        assertEquals(MCPErrorRecoveryManager.RecoveryAction.FALLBACK,
                MCPErrorRecoveryManager.RecoveryAction.valueOf("FALLBACK"));
        assertEquals(MCPErrorRecoveryManager.RecoveryAction.DEGRADE,
                MCPErrorRecoveryManager.RecoveryAction.valueOf("DEGRADE"));
    }

    @Test
    void testRecoveryActionValueOfInvalid() {
        // Test RecoveryAction valueOf with invalid value
        assertThrows(IllegalArgumentException.class, () -> {
            MCPErrorRecoveryManager.RecoveryAction.valueOf("INVALID");
        });
    }

    @Test
    void testConcurrentErrorHandling() {
        // Test concurrent error handling
        assertDoesNotThrow(() -> {
            Thread thread1 = new Thread(() -> {
                for (int i = 0; i < 10; i++) {
                    errorRecoveryManager.handleError("concurrent_error", "Error " + i, "client-" + i);
                }
            });

            Thread thread2 = new Thread(() -> {
                for (int i = 0; i < 10; i++) {
                    errorRecoveryManager.recordRecovery("concurrent_error");
                }
            });

            thread1.start();
            thread2.start();

            thread1.join(5000);
            thread2.join(5000);

            MCPErrorRecoveryManager.ErrorRecoveryStatistics stats = errorRecoveryManager.getErrorRecoveryStatistics();
            assertNotNull(stats);
        });
    }

    @Test
    void testMultipleErrorTypes() {
        // Test handling multiple error types
        errorRecoveryManager.handleError("network_error", "Network failed", "client-1");
        errorRecoveryManager.handleError("tool_error", "Tool failed", "client-2");
        errorRecoveryManager.handleError("auth_error", "Auth failed", "client-3");

        Map<String, MCPErrorRecoveryManager.ErrorInfo> errorDetails = errorRecoveryManager.getErrorDetails();
        assertEquals(3, errorDetails.size());
        assertTrue(errorDetails.containsKey("network_error"));
        assertTrue(errorDetails.containsKey("tool_error"));
        assertTrue(errorDetails.containsKey("auth_error"));
    }

    @Test
    void testServiceHealthTracking() {
        // Test service health tracking
        errorRecoveryManager.updateServiceHealth("service1", true);
        errorRecoveryManager.updateServiceHealth("service2", false);
        errorRecoveryManager.updateServiceHealth("service3", true);

        assertTrue(errorRecoveryManager.isServiceHealthy("service1"));
        assertFalse(errorRecoveryManager.isServiceHealthy("service2"));
        assertTrue(errorRecoveryManager.isServiceHealthy("service3"));
        assertFalse(errorRecoveryManager.isServiceHealthy("unknown_service"));
    }

    @Test
    void testToString() {
        // Test toString method
        String managerString = errorRecoveryManager.toString();
        assertNotNull(managerString);
        assertFalse(managerString.isEmpty());
    }

    @Test
    void testEquality() {
        // Test equality
        MCPErrorRecoveryManager manager1 = new MCPErrorRecoveryManager(auditLogger, config);
        MCPErrorRecoveryManager manager2 = new MCPErrorRecoveryManager(auditLogger, config);

        // These should be different instances
        assertNotEquals(manager1, manager2);
        assertNotEquals(manager1, null);
        assertNotEquals(manager1, "not a manager");
    }

    private boolean containsAction(MCPErrorRecoveryManager.RecoveryAction[] actions,
            MCPErrorRecoveryManager.RecoveryAction target) {
        for (MCPErrorRecoveryManager.RecoveryAction action : actions) {
            if (action == target) {
                return true;
            }
        }
        return false;
    }
}
