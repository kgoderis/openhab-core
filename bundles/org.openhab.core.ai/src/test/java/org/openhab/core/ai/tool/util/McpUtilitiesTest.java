package org.openhab.core.ai.tool.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.ai.tool.api.Tool;
import org.openhab.core.ai.tool.api.ToolContext;
import org.openhab.core.ai.tool.api.ToolException;
import org.openhab.core.ai.tool.api.ToolMetadata;
import org.openhab.core.ai.tool.api.ToolResult;
import org.openhab.core.ai.tool.api.ToolValidationResult;
import org.openhab.core.ai.tool.notification.NotificationManager;
import org.openhab.core.ai.tool.progress.ToolProgressTracker;

/**
 * Test class for enhanced MCP utilities.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
class McpUtilitiesTest {

    private McpUtilitiesManager utilitiesManager;
    private NotificationManager notificationManager;
    private ToolProgressTracker progressTracker;

    @BeforeEach
    void setUp() {
        utilitiesManager = McpUtilitiesManager.getInstance();
        notificationManager = utilitiesManager.getNotificationManager();
        progressTracker = utilitiesManager.getProgressTracker();
    }

    @Test
    void testNotificationManager() {
        // Test notification subscription
        String subscriptionId = notificationManager.createSubscription("test-client", "test.notification",
                Map.of("test", "data"));
        assertNotNull(subscriptionId);
        assertEquals(1, notificationManager.getSubscriptionCount());

        // Test notification sending
        notificationManager.sendNotification("test.notification", "test-source", Map.of("message", "test message"),
                "info", "test-correlation");

        List<NotificationManager.NotificationSubscription> subscriptions = notificationManager
                .getSubscriptionsForType("test.notification");
        assertEquals(1, subscriptions.size());
        assertEquals("test-client", subscriptions.get(0).getClientId());

        // Test notification removal
        boolean removed = notificationManager.removeSubscription(subscriptionId);
        assertTrue(removed);
        assertEquals(0, notificationManager.getSubscriptionCount());
    }

    @Test
    void testProgressTracker() {
        // Test progress session creation
        String operationId = progressTracker.createProgressSession("test_operation", Map.of("test", "data"));
        assertNotNull(operationId);
        assertEquals(1, progressTracker.getProgressSessionCount());

        // Test progress tracking
        progressTracker.startProgress(operationId, 5, "Starting test operation");

        ToolProgressTracker.ProgressSession session = progressTracker.getProgressSession(operationId);
        assertNotNull(session);
        assertEquals(ToolProgressTracker.ProgressState.RUNNING, session.getState());
        assertEquals(5, session.getTotalSteps());
        assertEquals(0, session.getCurrentStep());

        // Test progress updates
        progressTracker.updateProgress(operationId, 2, "Processing step 2");
        session = progressTracker.getProgressSession(operationId);
        assertEquals(2, session.getCurrentStep());
        assertEquals(40.0, session.getProgressPercentage(), 0.1);

        // Test progress completion
        progressTracker.completeProgress(operationId, "Operation completed successfully");
        session = progressTracker.getProgressSession(operationId);
        assertEquals(ToolProgressTracker.ProgressState.COMPLETED, session.getState());
        assertEquals(5, session.getCurrentStep());
        assertEquals(100.0, session.getProgressPercentage(), 0.1);
    }

    @Test
    void testToolUtils() {
        // Create a test tool
        TestTool testTool = new TestTool();

        // Test tool metadata validation
        boolean isValid = McpToolUtils.validateToolMetadata(testTool);
        assertTrue(isValid);

        // Test tool metadata summary
        String summary = McpToolUtils.getToolMetadataSummary(testTool);
        assertTrue(summary.contains("test-tool"));
        assertTrue(summary.contains("Test tool description"));

        // Test parameter validation
        Map<String, Object> validParameters = Map.of("param1", "value1");
        var validationResult = McpToolUtils.validateToolParameters(testTool, validParameters);
        assertTrue(validationResult.isValid());
        assertFalse(validationResult.hasErrors());

        // Test performance metrics
        var performanceMetrics = McpToolUtils.getToolPerformanceMetrics(testTool, 150);
        assertEquals("test-tool", performanceMetrics.getToolId());
        assertEquals(150, performanceMetrics.getExecutionTimeMs());
        assertTrue(performanceMetrics.isSlowExecution(100));
        assertFalse(performanceMetrics.isSlowExecution(200));
    }

    @Test
    void testUtilitiesManager() {
        // Test tool registration
        TestTool testTool = new TestTool();
        utilitiesManager.registerTool(testTool);

        Map<String, Object> stats = utilitiesManager.getStatistics();
        assertEquals(1, stats.get("registeredTools"));
        assertTrue(stats.get("notificationSubscriptions") instanceof Integer);
        assertTrue(stats.get("activeProgressSessions") instanceof Integer);

        // Test tool execution with progress
        Map<String, Object> parameters = Map.of("param1", "value1");
        Map<String, Object> result = utilitiesManager.executeToolWithProgress("test-tool", parameters);

        assertTrue((Boolean) result.get("success"));
        assertTrue(result.containsKey("content"));
        assertTrue(result.containsKey("executionTime"));

        // Test health status
        Map<String, Object> healthStatus = utilitiesManager.getHealthStatus();
        assertTrue(healthStatus.containsKey("notificationManager"));
        assertTrue(healthStatus.containsKey("progressTracker"));
        assertTrue(healthStatus.containsKey("toolUtils"));

        // Test tool unregistration
        utilitiesManager.unregisterTool("test-tool");
        assertNull(utilitiesManager.getTool("test-tool"));
    }

    @Test
    void testNotificationFilters() {
        // Test notification filter
        String subscriptionId = notificationManager.createSubscription("test-client", "filtered.notification",
                Map.of());

        notificationManager.addFilter(subscriptionId, (subscription, notification) -> {
            // Only deliver notifications with severity "high"
            return "high".equals(notification.getSeverity());
        });

        // Send low priority notification (should be filtered out)
        notificationManager.sendNotification("filtered.notification", "test-source", Map.of("message", "low priority"),
                "low", null);

        // Send high priority notification (should be delivered)
        notificationManager.sendNotification("filtered.notification", "test-source", Map.of("message", "high priority"),
                "high", null);

        // Clean up
        notificationManager.removeSubscription(subscriptionId);
    }

    @Test
    void testProgressListeners() {
        // Test progress listener
        String operationId = progressTracker.createProgressSession("listener_test", Map.of());

        TestProgressListener listener = new TestProgressListener();
        progressTracker.addProgressListener(operationId, listener);

        // Start and update progress
        progressTracker.startProgress(operationId, 3, "Starting listener test");
        progressTracker.updateProgress(operationId, 1, "Step 1");
        progressTracker.updateProgress(operationId, 2, "Step 2");
        progressTracker.completeProgress(operationId, "Listener test completed");

        // Verify listener was called
        assertTrue(listener.isUpdateCalled());
        assertTrue(listener.isCompletedCalled());
        assertFalse(listener.isFailedCalled());

        // Clean up
        progressTracker.removeProgressListener(operationId, listener);
    }

    @Test
    void testMaintenance() {
        // Create some test data
        TestTool testTool = new TestTool();
        utilitiesManager.registerTool(testTool);

        String operationId = progressTracker.createProgressSession("maintenance_test", Map.of());
        progressTracker.startProgress(operationId, 1, "Test");
        progressTracker.completeProgress(operationId, "Completed");

        // Perform maintenance
        utilitiesManager.performMaintenance();

        // Verify maintenance cleaned up completed sessions
        List<ToolProgressTracker.ProgressSession> completedSessions = progressTracker
                .getProgressSessionsByState(ToolProgressTracker.ProgressState.COMPLETED);
        assertEquals(0, completedSessions.size());
    }

    /**
     * Test tool implementation for testing.
     */
    private static class TestTool implements Tool {
        @Override
        public String getId() {
            return "test-tool";
        }

        @Override
        public String getName() {
            return "Test Tool";
        }

        @Override
        public String getDescription() {
            return "Test tool description";
        }

        @Override
        public ToolMetadata getMetadata() {
            return ToolMetadata.builder().version("1.0.0").author("Test Author").description("Test tool description")
                    .build();
        }

        @Override
        public Map<String, Object> getInputSchema() {
            return Map.of("param1", Map.of("type", "string", "description", "Test parameter"));
        }

        @Override
        public Map<String, Object> getOutputSchema() {
            return Map.of("result", Map.of("type", "string", "description", "Test result"));
        }

        @Override
        public ToolResult execute(Map<String, Object> parameters, ToolContext context) throws ToolException {
            return ToolResult.successJson("test-tool", "Test execution completed successfully", 0);
        }

        @Override
        public ToolValidationResult validateParameters(Map<String, Object> parameters) {
            if (parameters.containsKey("param1")) {
                return ToolValidationResult.valid();
            }
            return ToolValidationResult.invalid("Missing required parameter: param1");
        }
    }

    /**
     * Test progress listener implementation.
     */
    private static class TestProgressListener implements ToolProgressTracker.ProgressListener {
        private boolean updateCalled = false;
        private boolean completedCalled = false;
        private boolean failedCalled = false;

        @Override
        public void onProgressUpdate(ToolProgressTracker.ProgressSession session) {
            updateCalled = true;
        }

        @Override
        public void onProgressCompleted(ToolProgressTracker.ProgressSession session) {
            completedCalled = true;
        }

        @Override
        public void onProgressFailed(ToolProgressTracker.ProgressSession session, String error) {
            failedCalled = true;
        }

        public boolean isUpdateCalled() {
            return updateCalled;
        }

        public boolean isCompletedCalled() {
            return completedCalled;
        }

        public boolean isFailedCalled() {
            return failedCalled;
        }
    }
}
