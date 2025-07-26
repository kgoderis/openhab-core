package org.openhab.core.ai.a2a.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.common.api.action.AIActionRegistry;
import org.openhab.core.service.ReadyService;
import org.osgi.framework.BundleContext;

import io.a2a.spec.AgentCard;
import io.a2a.spec.SendMessageRequest;
import io.a2a.spec.Task;

@ExtendWith(MockitoExtension.class)
class A2ARestEndpointTest {

    @Mock
    private AIActionRegistry mockActionRegistry;

    @Mock
    private A2ASkillRegistry mockSkillRegistry;

    @Mock
    private BundleContext mockBundleContext;

    @Mock
    private ReadyService mockReadyService;

    @Mock
    private A2AServerManager mockServerManager;

    @Mock
    private AgentCard mockAgentCard;

    @Mock
    private Task mockTask;

    private A2ARestEndpoint restEndpoint;

    @BeforeEach
    void setUp() {
        restEndpoint = new A2ARestEndpoint();
    }

    @Test
    void testConstructor() {
        assertNotNull(restEndpoint);
    }

    @Test
    void testInitialize() {
        // Test initialization
        restEndpoint.initialize();

        // Should not throw exception
        assertNotNull(restEndpoint);
    }

    @Test
    void testGetAgentCard() {
        // Test agent card endpoint
        AgentCard result = restEndpoint.getAgentCard();

        // Should return agent card
        assertNotNull(result);
    }

    @Test
    void testSendMessage() {
        // Test send message endpoint
        SendMessageRequest request = mock(SendMessageRequest.class);
        Map<String, Object> result = restEndpoint.sendMessage(request);

        // Should return response map
        assertNotNull(result);
        assertTrue(result instanceof Map);
    }

    @Test
    void testSendMessageWithNullRequest() {
        // Test send message with null request
        Map<String, Object> result = restEndpoint.sendMessage(null);

        // Should return error response
        assertNotNull(result);
        assertTrue(result.containsKey("error"));
    }

    @Test
    void testGetTask() {
        // Test get task endpoint
        String taskId = "test-task-123";
        Task result = restEndpoint.getTask(taskId);

        // Should return task
        assertNotNull(result);
    }

    @Test
    void testGetTaskWithNullTaskId() {
        // Test get task with null task ID
        Task result = restEndpoint.getTask(null);

        // Should return null or default task
        assertNull(result);
    }

    @Test
    void testGetTaskWithEmptyTaskId() {
        // Test get task with empty task ID
        Task result = restEndpoint.getTask("");

        // Should return null or default task
        assertNull(result);
    }

    @Test
    void testCancelTask() {
        // Test cancel task endpoint
        Map<String, String> request = Map.of("taskId", "test-task-123");
        Map<String, Object> result = restEndpoint.cancelTask(request);

        // Should return response map
        assertNotNull(result);
        assertTrue(result instanceof Map);
    }

    @Test
    void testCancelTaskWithNullRequest() {
        // Test cancel task with null request
        Map<String, Object> result = restEndpoint.cancelTask(null);

        // Should return error response
        assertNotNull(result);
        assertTrue(result.containsKey("error"));
    }

    @Test
    void testCancelTaskWithEmptyRequest() {
        // Test cancel task with empty request
        Map<String, String> request = Map.of();
        Map<String, Object> result = restEndpoint.cancelTask(request);

        // Should return error response
        assertNotNull(result);
        assertTrue(result.containsKey("error"));
    }

    @Test
    void testHealthCheck() {
        // Test health check endpoint
        Map<String, Object> result = restEndpoint.healthCheck();

        // Should return health data
        assertNotNull(result);
        assertTrue(result instanceof Map);

        // Should contain health information
        assertTrue(result.containsKey("status"));
        assertTrue(result.containsKey("timestamp"));
    }

    @Test
    void testRestEndpointLifecycle() {
        // Test complete lifecycle
        assertNotNull(restEndpoint);

        restEndpoint.initialize();
        assertNotNull(restEndpoint);
    }

    @Test
    void testHealthCheckConsistency() {
        // Test that health check returns consistent data
        Map<String, Object> result1 = restEndpoint.healthCheck();
        Map<String, Object> result2 = restEndpoint.healthCheck();

        // Both should return health data
        assertNotNull(result1);
        assertNotNull(result2);
        assertTrue(result1 instanceof Map);
        assertTrue(result2 instanceof Map);

        // Should contain expected fields
        assertTrue(result1.containsKey("status"));
        assertTrue(result1.containsKey("timestamp"));
        assertTrue(result2.containsKey("status"));
        assertTrue(result2.containsKey("timestamp"));
    }

    @Test
    void testSendMessageRequestValidation() {
        // Test send message request validation
        SendMessageRequest validRequest = mock(SendMessageRequest.class);
        when(validRequest.getParams()).thenReturn(mock(io.a2a.spec.MessageSendParams.class));

        Map<String, Object> result = restEndpoint.sendMessage(validRequest);

        // Should return response
        assertNotNull(result);
        assertTrue(result instanceof Map);
    }

    @Test
    void testTaskRetrievalWithValidId() {
        // Test task retrieval with valid ID
        String taskId = "valid-task-id";
        Task result = restEndpoint.getTask(taskId);

        // Should return task
        assertNotNull(result);
    }

    @Test
    void testTaskRetrievalWithInvalidId() {
        // Test task retrieval with invalid ID
        String taskId = "invalid-task-id";
        Task result = restEndpoint.getTask(taskId);

        // Should return null or default task
        assertNull(result);
    }

    @Test
    void testCancelTaskWithValidRequest() {
        // Test cancel task with valid request
        Map<String, String> request = Map.of("taskId", "valid-task-id");
        Map<String, Object> result = restEndpoint.cancelTask(request);

        // Should return response
        assertNotNull(result);
        assertTrue(result instanceof Map);
    }

    @Test
    void testCancelTaskWithInvalidRequest() {
        // Test cancel task with invalid request
        Map<String, String> request = Map.of("invalidField", "value");
        Map<String, Object> result = restEndpoint.cancelTask(request);

        // Should return error response
        assertNotNull(result);
        assertTrue(result.containsKey("error"));
    }

    @Test
    void testAgentCardRetrieval() {
        // Test agent card retrieval
        AgentCard result = restEndpoint.getAgentCard();

        // Should return agent card
        assertNotNull(result);

        // Should contain expected fields
        assertNotNull(result.name());
        assertNotNull(result.description());
        assertNotNull(result.capabilities());
        assertNotNull(result.skills());
    }

    @Test
    void testErrorHandling() {
        // Test error handling for various scenarios

        // Null send message request
        Map<String, Object> nullMessageResult = restEndpoint.sendMessage(null);
        assertNotNull(nullMessageResult);
        assertTrue(nullMessageResult.containsKey("error"));

        // Null cancel task request
        Map<String, Object> nullCancelResult = restEndpoint.cancelTask(null);
        assertNotNull(nullCancelResult);
        assertTrue(nullCancelResult.containsKey("error"));

        // Empty cancel task request
        Map<String, Object> emptyCancelResult = restEndpoint.cancelTask(Map.of());
        assertNotNull(emptyCancelResult);
        assertTrue(emptyCancelResult.containsKey("error"));
    }

    @Test
    void testResponseDataStructures() {
        // Test that responses have correct data structures

        // Health check response
        Map<String, Object> healthResult = restEndpoint.healthCheck();
        assertTrue(healthResult.containsKey("status"));
        assertTrue(healthResult.containsKey("timestamp"));
        assertTrue(healthResult.get("status") instanceof String);
        assertTrue(healthResult.get("timestamp") instanceof String);

        // Send message response
        SendMessageRequest request = mock(SendMessageRequest.class);
        Map<String, Object> messageResult = restEndpoint.sendMessage(request);
        assertNotNull(messageResult);
        assertTrue(messageResult instanceof Map);

        // Cancel task response
        Map<String, String> cancelRequest = Map.of("taskId", "test-task");
        Map<String, Object> cancelResult = restEndpoint.cancelTask(cancelRequest);
        assertNotNull(cancelResult);
        assertTrue(cancelResult instanceof Map);
    }

    @Test
    void testEndpointAvailability() {
        // Test that all endpoints are available

        // Agent card endpoint
        AgentCard agentCard = restEndpoint.getAgentCard();
        assertNotNull(agentCard);

        // Health check endpoint
        Map<String, Object> health = restEndpoint.healthCheck();
        assertNotNull(health);

        // Task endpoint
        Task task = restEndpoint.getTask("test-task");
        assertNotNull(task);
    }
}
