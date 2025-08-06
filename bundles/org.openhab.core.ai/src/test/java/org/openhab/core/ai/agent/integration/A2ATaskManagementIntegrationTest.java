package org.openhab.core.ai.common.agent.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.agent.AgentProtocolHandler;
import org.openhab.core.ai.agent.AgentSecurityManager;
import org.openhab.core.ai.agent.AgentSkillRegistry;
import org.openhab.core.ai.agent.internal.A2ARestEndpoint;
import org.openhab.core.ai.agent.internal.AgentAgentExecutor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * A2A Task Management Integration Tests for openHAB AI A2A bundle.
 * 
 * Tests A2A task management functionality including task creation, execution,
 * monitoring, cancellation, and cleanup as outlined in the TEST_PLAN.md.
 */
@ExtendWith(MockitoExtension.class)
class A2ATaskManagementIntegrationTest {

    private ObjectMapper objectMapper;
    private A2ATestClient testClient;

    @Mock
    private AgentProtocolHandler protocolHandler;

    @Mock
    private AgentSkillRegistry skillRegistry;

    @Mock
    private AgentSecurityManager securityManager;

    @Mock
    private AgentAgentExecutor agentExecutor;

    @Mock
    private A2ARestEndpoint restEndpoint;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        testClient = new A2ATestClient();

        // Mock server manager behavior
        when(serverManager.isRunning()).thenReturn(true);

        // Initialize test client
        testClient.initialize();
        testClient.initializeA2A();
    }

    /**
     * Test task creation and submission.
     */
    @Test
    void testTaskCreationAndSubmission() throws Exception {
        // Create a new task
        Map<String, Object> taskDefinition = Map.of("name", "test-task", "description",
                "Test task for integration testing", "skill", "openhab.items.list", "parameters",
                Map.of("filter", "all"), "priority", "normal", "timeout", 30000);

        ObjectNode createResponse = testClient.createTask(taskDefinition);

        assertEquals("2.0", createResponse.get("jsonrpc").asText());
        assertNotNull(createResponse.get("result"));

        ObjectNode result = (ObjectNode) createResponse.get("result");
        assertNotNull(result.get("taskId"));
        assertEquals("submitted", result.get("status").asText());

        String taskId = result.get("taskId").asText();
        assertNotNull(taskId);
        assertFalse(taskId.isEmpty());
    }

    /**
     * Test task execution and monitoring.
     */
    @Test
    void testTaskExecutionAndMonitoring() throws Exception {
        // Create and submit a task
        Map<String, Object> taskDefinition = Map.of("name", "monitoring-test-task", "skill",
                "openhab.persistence.manage", "parameters", Map.of("action", "status"));

        ObjectNode createResponse = testClient.createTask(taskDefinition);
        String taskId = createResponse.get("result").get("taskId").asText();

        // Monitor task execution
        ObjectNode statusResponse = testClient.getTaskStatus(taskId);
        assertNotNull(statusResponse.get("result"));

        ObjectNode statusResult = (ObjectNode) statusResponse.get("result");
        assertNotNull(statusResult.get("taskId"));
        assertEquals(taskId, statusResult.get("taskId").asText());
        assertNotNull(statusResult.get("status"));

        // Wait for task completion
        int maxAttempts = 10;
        int attempts = 0;
        String status = "submitted";

        while (!"completed".equals(status) && !"failed".equals(status) && attempts < maxAttempts) {
            Thread.sleep(1000); // Wait 1 second between checks
            statusResponse = testClient.getTaskStatus(taskId);
            statusResult = (ObjectNode) statusResponse.get("result");
            status = statusResult.get("status").asText();
            attempts++;
        }

        assertTrue("completed".equals(status) || "failed".equals(status),
                "Task should complete or fail within timeout");
    }

    /**
     * Test task cancellation.
     */
    @Test
    void testTaskCancellation() throws Exception {
        // Create a long-running task
        Map<String, Object> taskDefinition = Map.of("name", "cancellation-test-task", "skill",
                "openhab.persistence.manage", "parameters", Map.of("action", "backup"), "timeout", 60000);

        ObjectNode createResponse = testClient.createTask(taskDefinition);
        String taskId = createResponse.get("result").get("taskId").asText();

        // Wait a bit for task to start
        Thread.sleep(1000);

        // Cancel the task
        ObjectNode cancelResponse = testClient.cancelTask(taskId);
        assertNotNull(cancelResponse.get("result"));

        ObjectNode cancelResult = (ObjectNode) cancelResponse.get("result");
        assertEquals("cancelled", cancelResult.get("status").asText());

        // Verify task status is cancelled
        ObjectNode statusResponse = testClient.getTaskStatus(taskId);
        ObjectNode statusResult = (ObjectNode) statusResponse.get("result");
        assertEquals("cancelled", statusResult.get("status").asText());
    }

    /**
     * Test task cleanup and removal.
     */
    @Test
    void testTaskCleanupAndRemoval() throws Exception {
        // Create a task
        Map<String, Object> taskDefinition = Map.of("name", "cleanup-test-task", "skill", "openhab.items.list",
                "parameters", Map.of("filter", "all"));

        ObjectNode createResponse = testClient.createTask(taskDefinition);
        String taskId = createResponse.get("result").get("taskId").asText();

        // Wait for task completion
        Thread.sleep(2000);

        // Remove the task
        ObjectNode removeResponse = testClient.removeTask(taskId);
        assertNotNull(removeResponse.get("result"));

        ObjectNode removeResult = (ObjectNode) removeResponse.get("result");
        assertEquals("removed", removeResult.get("status").asText());

        // Verify task is no longer accessible
        ObjectNode statusResponse = testClient.getTaskStatus(taskId);
        assertNotNull(statusResponse.get("error"));

        ObjectNode error = (ObjectNode) statusResponse.get("error");
        assertEquals(-32601, error.get("code").asInt(), "Should have method not found error");
    }

    /**
     * Test task state management.
     */
    @Test
    void testTaskStateManagement() throws Exception {
        // Create a task
        Map<String, Object> taskDefinition = Map.of("name", "state-test-task", "skill", "openhab.things.list",
                "parameters", Map.of("filter", "all"));

        ObjectNode createResponse = testClient.createTask(taskDefinition);
        String taskId = createResponse.get("result").get("taskId").asText();

        // Test state transitions
        String[] expectedStates = { "submitted", "working", "completed" };
        int stateIndex = 0;

        for (int i = 0; i < 10 && stateIndex < expectedStates.length; i++) {
            ObjectNode statusResponse = testClient.getTaskStatus(taskId);
            ObjectNode statusResult = (ObjectNode) statusResponse.get("result");
            String currentState = statusResult.get("status").asText();

            if (currentState.equals(expectedStates[stateIndex])) {
                stateIndex++;
            }

            Thread.sleep(1000);
        }

        assertTrue(stateIndex > 0, "Task should progress through states");
    }

    /**
     * Test task persistence and recovery.
     */
    @Test
    void testTaskPersistenceAndRecovery() throws Exception {
        // Create a task
        Map<String, Object> taskDefinition = Map.of("name", "persistence-test-task", "skill", "openhab.items.list",
                "parameters", Map.of("filter", "all"), "persistent", true);

        ObjectNode createResponse = testClient.createTask(taskDefinition);
        String taskId = createResponse.get("result").get("taskId").asText();

        // Simulate system restart by reinitializing client
        testClient.close();
        testClient.initialize();
        testClient.initializeA2A();

        // Verify task is still accessible after restart
        ObjectNode statusResponse = testClient.getTaskStatus(taskId);
        assertNotNull(statusResponse.get("result"));

        ObjectNode statusResult = (ObjectNode) statusResponse.get("result");
        assertEquals(taskId, statusResult.get("taskId").asText());
        assertNotNull(statusResult.get("status"));
    }

    /**
     * Test concurrent task management.
     */
    @Test
    void testConcurrentTaskManagement() throws Exception {
        // Create multiple tasks concurrently
        CompletableFuture<ObjectNode>[] futures = new CompletableFuture[5];

        for (int i = 0; i < 5; i++) {
            final int taskIndex = i;
            futures[i] = CompletableFuture.supplyAsync(() -> {
                try {
                    Map<String, Object> taskDefinition = Map.of("name", "concurrent-task-" + taskIndex, "skill",
                            "openhab.items.list", "parameters", Map.of("filter", "all"));
                    return testClient.createTask(taskDefinition);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }

        // Wait for all tasks to be created
        ObjectNode[] responses = new ObjectNode[5];
        for (int i = 0; i < 5; i++) {
            responses[i] = futures[i].get(30, TimeUnit.SECONDS);
            assertNotNull(responses[i].get("result"));
        }

        // Verify all tasks have unique IDs
        String[] taskIds = new String[5];
        for (int i = 0; i < 5; i++) {
            taskIds[i] = responses[i].get("result").get("taskId").asText();
        }

        for (int i = 0; i < 5; i++) {
            for (int j = i + 1; j < 5; j++) {
                assertNotEquals(taskIds[i], taskIds[j], "Task IDs should be unique");
            }
        }
    }

    /**
     * Test task error handling and recovery.
     */
    @Test
    void testTaskErrorHandlingAndRecovery() throws Exception {
        // Create a task that will fail
        Map<String, Object> taskDefinition = Map.of("name", "error-test-task", "skill", "non.existent.skill",
                "parameters", Map.of("invalid", "parameter"));

        ObjectNode createResponse = testClient.createTask(taskDefinition);
        String taskId = createResponse.get("result").get("taskId").asText();

        // Wait for task to fail
        Thread.sleep(3000);

        // Check task status
        ObjectNode statusResponse = testClient.getTaskStatus(taskId);
        ObjectNode statusResult = (ObjectNode) statusResponse.get("result");
        String status = statusResult.get("status").asText();

        assertTrue("failed".equals(status) || "error".equals(status), "Task should fail due to invalid skill");

        // Verify error details are available
        if (statusResult.has("error")) {
            ObjectNode error = (ObjectNode) statusResult.get("error");
            assertNotNull(error.get("message"));
            assertTrue(error.get("code").asInt() > 0);
        }
    }

    /**
     * Test task timeout handling.
     */
    @Test
    void testTaskTimeoutHandling() throws Exception {
        // Create a task with short timeout
        Map<String, Object> taskDefinition = Map.of("name", "timeout-test-task", "skill", "openhab.persistence.manage",
                "parameters", Map.of("action", "backup"), "timeout", 1000 // 1 second timeout
        );

        ObjectNode createResponse = testClient.createTask(taskDefinition);
        String taskId = createResponse.get("result").get("taskId").asText();

        // Wait for timeout
        Thread.sleep(3000);

        // Check task status
        ObjectNode statusResponse = testClient.getTaskStatus(taskId);
        ObjectNode statusResult = (ObjectNode) statusResponse.get("result");
        String status = statusResult.get("status").asText();

        assertTrue("timeout".equals(status) || "failed".equals(status), "Task should timeout or fail");
    }

    /**
     * Test task result retrieval.
     */
    @Test
    void testTaskResultRetrieval() throws Exception {
        // Create a task
        Map<String, Object> taskDefinition = Map.of("name", "result-test-task", "skill", "openhab.items.list",
                "parameters", Map.of("filter", "all"));

        ObjectNode createResponse = testClient.createTask(taskDefinition);
        String taskId = createResponse.get("result").get("taskId").asText();

        // Wait for task completion
        Thread.sleep(3000);

        // Get task result
        ObjectNode resultResponse = testClient.getTaskResult(taskId);
        assertNotNull(resultResponse.get("result"));

        ObjectNode result = (ObjectNode) resultResponse.get("result");
        assertNotNull(result.get("taskId"));
        assertEquals(taskId, result.get("taskId").asText());

        if (result.has("content")) {
            assertNotNull(result.get("content"));
        }
    }
}
