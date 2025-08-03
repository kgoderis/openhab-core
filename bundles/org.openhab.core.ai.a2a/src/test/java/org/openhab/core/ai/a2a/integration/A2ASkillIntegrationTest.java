package org.openhab.core.ai.a2a.integration;

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
import org.openhab.core.ai.a2a.internal.A2AAgentExecutor;
import org.openhab.core.ai.a2a.internal.A2AProtocolHandler;
import org.openhab.core.ai.a2a.internal.A2ARestEndpoint;
import org.openhab.core.ai.a2a.internal.A2ASecurityManager;
import org.openhab.core.ai.a2a.internal.A2ASkillRegistry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * A2A Skill Integration Tests for openHAB AI A2A bundle.
 * 
 * Tests A2A skill execution and skill registry integration
 * as outlined in the TEST_PLAN.md integration testing strategy.
 */
@ExtendWith(MockitoExtension.class)
class A2ASkillIntegrationTest {

    private ObjectMapper objectMapper;
    private A2ATestClient testClient;

    @Mock
    private A2AProtocolHandler protocolHandler;

    @Mock
    private A2ASkillRegistry skillRegistry;

    @Mock
    private A2ASecurityManager securityManager;

    @Mock
    private A2AAgentExecutor agentExecutor;

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
     * Test skill listing functionality.
     */
    @Test
    void testSkillListing() throws Exception {
        ObjectNode listResponse = testClient.listSkills();

        assertEquals("2.0", listResponse.get("jsonrpc").asText());

        ObjectNode result = (ObjectNode) listResponse.get("result");
        assertNotNull(result.get("skills"));

        var skills = result.get("skills");
        assertTrue(skills.size() > 0, "Should have available skills");

        // Verify skill structure
        for (var skill : skills) {
            assertTrue(skill.has("name"), "Skill should have name");
            assertTrue(skill.has("description"), "Skill should have description");
            assertTrue(skill.has("parameters"), "Skill should have parameters");
        }
    }

    /**
     * Test items list skill execution.
     */
    @Test
    void testItemsListSkill() throws Exception {
        Map<String, Object> arguments = Map.of("filter", "all");
        ObjectNode response = testClient.executeSkill("openhab.items.list", arguments);

        assertEquals("2.0", response.get("jsonrpc").asText());
        assertNotNull(response.get("result"));

        ObjectNode result = (ObjectNode) response.get("result");
        assertNotNull(result.get("content"));

        // Verify content contains items data
        var content = result.get("content");
        assertTrue(content.toString().contains("items"), "Response should contain items data");
    }

    /**
     * Test persistence management skill execution.
     */
    @Test
    void testPersistenceManagementSkill() throws Exception {
        Map<String, Object> arguments = Map.of("action", "status");
        ObjectNode response = testClient.executeSkill("openhab.persistence.manage", arguments);

        assertEquals("2.0", response.get("jsonrpc").asText());
        assertNotNull(response.get("result"));

        ObjectNode result = (ObjectNode) response.get("result");
        assertNotNull(result.get("content"));

        // Verify content contains persistence data
        var content = result.get("content");
        assertTrue(content.toString().contains("persistence"), "Response should contain persistence data");
    }

    /**
     * Test things list skill execution.
     */
    @Test
    void testThingsListSkill() throws Exception {
        Map<String, Object> arguments = Map.of("filter", "all");
        ObjectNode response = testClient.executeSkill("openhab.things.list", arguments);

        assertEquals("2.0", response.get("jsonrpc").asText());
        assertNotNull(response.get("result"));

        ObjectNode result = (ObjectNode) response.get("result");
        assertNotNull(result.get("content"));

        // Verify content contains things data
        var content = result.get("content");
        assertTrue(content.toString().contains("things"), "Response should contain things data");
    }

    /**
     * Test skill execution with different parameters.
     */
    @Test
    void testSkillExecutionWithParameters() throws Exception {
        // Test with different filter parameters
        Map<String, Object> allFilter = Map.of("filter", "all");
        ObjectNode allResponse = testClient.executeSkill("openhab.items.list", allFilter);
        assertNotNull(allResponse.get("result"));

        Map<String, Object> activeFilter = Map.of("filter", "active");
        ObjectNode activeResponse = testClient.executeSkill("openhab.items.list", activeFilter);
        assertNotNull(activeResponse.get("result"));

        Map<String, Object> inactiveFilter = Map.of("filter", "inactive");
        ObjectNode inactiveResponse = testClient.executeSkill("openhab.items.list", inactiveFilter);
        assertNotNull(inactiveResponse.get("result"));
    }

    /**
     * Test skill execution error handling.
     */
    @Test
    void testSkillExecutionErrorHandling() throws Exception {
        // Test invalid skill name
        ObjectNode notFoundResponse = testClient.executeSkill("non.existent.skill", Map.of());

        assertEquals("2.0", notFoundResponse.get("jsonrpc").asText());
        assertNotNull(notFoundResponse.get("error"));

        ObjectNode error = (ObjectNode) notFoundResponse.get("error");
        assertEquals(-32601, error.get("code").asInt(), "Should have method not found error");

        // Test invalid parameters
        Map<String, Object> invalidArgs = Map.of("invalid", "parameter");
        ObjectNode invalidResponse = testClient.executeSkill("openhab.items.list", invalidArgs);

        assertEquals("2.0", invalidResponse.get("jsonrpc").asText());
        assertNotNull(invalidResponse.get("error"));

        ObjectNode invalidError = (ObjectNode) invalidResponse.get("error");
        assertTrue(invalidError.get("code").asInt() > 0, "Should have error code");
    }

    /**
     * Test skill streaming execution.
     */
    @Test
    void testSkillStreamingExecution() throws Exception {
        // Test streaming execution for long-running operations
        Map<String, Object> arguments = Map.of("action", "backup");
        var streamResponse = testClient.executeSkillStreaming("openhab.persistence.manage", arguments);

        assertNotNull(streamResponse);
        assertTrue(streamResponse.size() > 0, "Should have received streaming events");

        boolean hasData = false;
        boolean hasDone = false;

        for (ObjectNode event : streamResponse) {
            if (event.has("content")) {
                hasData = true;
            }
            if (event.has("isError") && !event.get("isError").asBoolean()) {
                hasDone = true;
            }
        }

        assertTrue(hasData, "Should have received data events");
        assertTrue(hasDone, "Should have received completion event");
    }

    /**
     * Test concurrent skill execution.
     */
    @Test
    void testConcurrentSkillExecution() throws Exception {
        // Send multiple concurrent skill requests
        CompletableFuture<ObjectNode> future1 = CompletableFuture.supplyAsync(() -> {
            try {
                return testClient.executeSkill("openhab.items.list", Map.of("filter", "all"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        CompletableFuture<ObjectNode> future2 = CompletableFuture.supplyAsync(() -> {
            try {
                return testClient.executeSkill("openhab.persistence.manage", Map.of("action", "status"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        CompletableFuture<ObjectNode> future3 = CompletableFuture.supplyAsync(() -> {
            try {
                return testClient.executeSkill("openhab.things.list", Map.of("filter", "all"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // Wait for all responses
        ObjectNode response1 = future1.get(30, TimeUnit.SECONDS);
        ObjectNode response2 = future2.get(30, TimeUnit.SECONDS);
        ObjectNode response3 = future3.get(30, TimeUnit.SECONDS);

        assertNotNull(response1.get("result"));
        assertNotNull(response2.get("result"));
        assertNotNull(response3.get("result"));
    }

    /**
     * Test skill execution performance.
     */
    @Test
    void testSkillExecutionPerformance() throws Exception {
        long startTime = System.currentTimeMillis();

        // Execute multiple skill requests in sequence
        for (int i = 0; i < 10; i++) {
            ObjectNode response = testClient.executeSkill("openhab.items.list", Map.of("filter", "all"));
            assertNotNull(response.get("result"));
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Performance assertion: 10 skill executions should complete within 10 seconds
        assertTrue(duration < 10000, "Skill execution performance test took too long: " + duration + "ms");
    }

    /**
     * Test skill parameter validation.
     */
    @Test
    void testSkillParameterValidation() throws Exception {
        // Test with missing required parameters
        ObjectNode missingParamsResponse = testClient.executeSkill("openhab.items.list", Map.of());

        // Should either succeed (if no required params) or fail with validation error
        if (missingParamsResponse.has("error")) {
            ObjectNode error = (ObjectNode) missingParamsResponse.get("error");
            int errorCode = error.get("code").asInt();
            assertTrue(errorCode == -32602 || errorCode == -32603, "Should have invalid params or internal error code");
        }

        // Test with correct parameters
        Map<String, Object> correctParams = Map.of("filter", "all");
        ObjectNode correctResponse = testClient.executeSkill("openhab.items.list", correctParams);
        assertNotNull(correctResponse.get("result"));
    }

    /**
     * Test skill result format.
     */
    @Test
    void testSkillResultFormat() throws Exception {
        Map<String, Object> arguments = Map.of("filter", "all");
        ObjectNode response = testClient.executeSkill("openhab.items.list", arguments);

        // Verify response structure
        assertEquals("2.0", response.get("jsonrpc").asText());
        assertTrue(response.has("id"));
        assertNotNull(response.get("result"));

        ObjectNode result = (ObjectNode) response.get("result");
        assertNotNull(result.get("content"), "Result should have content");
        assertTrue(result.has("timestamp"), "Result should have timestamp");
        assertTrue(result.has("success"), "Result should have success flag");
    }
}
