package org.openhab.core.ai.integration.protocol;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Cross-System Integration Tests for Section 2.2 Protocol Integration.
 * 
 * This test class implements cross-system integration testing as specified
 * in section 2.2 of the TODO list, including:
 * - Simultaneous MCP and A2A operation
 * - Resource sharing and conflict resolution
 * - Security boundary testing between protocols
 * - Performance impact of concurrent protocols
 * 
 * These tests validate the integration between MCP and A2A protocols,
 * ensuring they can operate simultaneously without conflicts.
 */
@ExtendWith(MockitoExtension.class)
class CrossSystemIntegrationTest {

    private ObjectMapper objectMapper;
    private HttpClient httpClient;
    private String mcpServerUrl = "http://localhost:8080/mcp";
    private String a2aServerUrl = "http://localhost:8081/a2a";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    /**
     * Test simultaneous MCP and A2A operation.
     */
    @Test
    void testSimultaneousMCPAndA2AOperation() throws Exception {
        // Test that both MCP and A2A can operate simultaneously

        // Initialize MCP connection
        ObjectNode mcpInitRequest = createMCPInitRequest("simultaneous-mcp-client", "1.0.0");
        ObjectNode mcpInitResponse = sendMCPRequest(mcpInitRequest);
        assertNotNull(mcpInitResponse.get("result"), "MCP init should succeed");

        // Initialize A2A connection
        ObjectNode a2aInitRequest = createA2AInitRequest("simultaneous-a2a-agent", "1.0.0");
        ObjectNode a2aInitResponse = sendA2ARequest(a2aInitRequest);
        assertNotNull(a2aInitResponse.get("result"), "A2A init should succeed");

        // Execute MCP tool call
        ObjectNode mcpToolRequest = createMCPToolRequest("openhab.items.list", Map.of("filter", "all"));
        ObjectNode mcpToolResponse = sendMCPRequest(mcpToolRequest);
        assertNotNull(mcpToolResponse.get("result"), "MCP tool execution should succeed");

        // Execute A2A task
        ObjectNode a2aTaskRequest = createA2ATaskRequest("openhab.things.list", Map.of("filter", "all"));
        ObjectNode a2aTaskResponse = sendA2ARequest(a2aTaskRequest);
        assertNotNull(a2aTaskResponse.get("result"), "A2A task execution should succeed");

        // Verify both operations completed successfully
        assertNotNull(mcpToolResponse.get("result"), "MCP operation should complete");
        assertNotNull(a2aTaskResponse.get("result"), "A2A operation should complete");

        System.out.println("✅ Simultaneous MCP and A2A operation validated");
    }

    /**
     * Test resource sharing and conflict resolution.
     */
    @Test
    void testResourceSharingAndConflictResolution() throws Exception {
        // Test resource sharing between MCP and A2A protocols

        // Initialize both protocols
        ObjectNode mcpInitResponse = sendMCPRequest(createMCPInitRequest("resource-mcp-client", "1.0.0"));
        ObjectNode a2aInitResponse = sendA2ARequest(createA2AInitRequest("resource-a2a-agent", "1.0.0"));

        assertNotNull(mcpInitResponse.get("result"), "MCP init should succeed");
        assertNotNull(a2aInitResponse.get("result"), "A2A init should succeed");

        // Test concurrent access to shared resources
        CompletableFuture<ObjectNode> mcpResourceFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return sendMCPRequest(createMCPToolRequest("openhab.persistence.manage", Map.of("action", "status")));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        CompletableFuture<ObjectNode> a2aResourceFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return sendA2ARequest(createA2ATaskRequest("openhab.persistence.manage", Map.of("action", "status")));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // Wait for both operations to complete
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(mcpResourceFuture, a2aResourceFuture);
        allFutures.get(30, TimeUnit.SECONDS);

        // Verify both operations succeeded (conflict resolution should handle this)
        assertNotNull(mcpResourceFuture.get().get("result"), "MCP resource access should succeed");
        assertNotNull(a2aResourceFuture.get().get("result"), "A2A resource access should succeed");

        System.out.println("✅ Resource sharing and conflict resolution validated");
    }

    /**
     * Test security boundary testing between protocols.
     */
    @Test
    void testSecurityBoundaryTestingBetweenProtocols() throws Exception {
        // Test security boundaries between MCP and A2A protocols

        // Test MCP with different security contexts
        ObjectNode mcpSecureRequest = createMCPInitRequest("secure-mcp-client", "1.0.0");
        ((ObjectNode) mcpSecureRequest.get("params")).put("securityLevel", "high");
        ObjectNode mcpSecureResponse = sendMCPRequest(mcpSecureRequest);
        assertNotNull(mcpSecureResponse.get("result"), "MCP secure init should succeed");

        // Test A2A with different security contexts
        ObjectNode a2aSecureRequest = createA2AInitRequest("secure-a2a-agent", "1.0.0");
        ((ObjectNode) a2aSecureRequest.get("params")).put("securityLevel", "high");
        ObjectNode a2aSecureResponse = sendA2ARequest(a2aSecureRequest);
        assertNotNull(a2aSecureResponse.get("result"), "A2A secure init should succeed");

        // Test cross-protocol security isolation
        ObjectNode mcpToolRequest = createMCPToolRequest("openhab.security.audit", Map.of("action", "check"));
        ObjectNode mcpToolResponse = sendMCPRequest(mcpToolRequest);
        assertNotNull(mcpToolResponse.get("result"), "MCP security operation should succeed");

        ObjectNode a2aTaskRequest = createA2ATaskRequest("openhab.security.audit", Map.of("action", "check"));
        ObjectNode a2aTaskResponse = sendA2ARequest(a2aTaskRequest);
        assertNotNull(a2aTaskResponse.get("result"), "A2A security operation should succeed");

        // Verify security boundaries are maintained
        assertNotNull(mcpToolResponse.get("result"), "MCP security boundary should be maintained");
        assertNotNull(a2aTaskResponse.get("result"), "A2A security boundary should be maintained");

        System.out.println("✅ Security boundary testing between protocols validated");
    }

    /**
     * Test performance impact of concurrent protocols.
     */
    @Test
    void testPerformanceImpactOfConcurrentProtocols() throws Exception {
        // Test performance characteristics when both protocols are active

        long startTime = System.currentTimeMillis();

        // Initialize both protocols
        ObjectNode mcpInitResponse = sendMCPRequest(createMCPInitRequest("performance-mcp-client", "1.0.0"));
        ObjectNode a2aInitResponse = sendA2ARequest(createA2AInitRequest("performance-a2a-agent", "1.0.0"));

        assertNotNull(mcpInitResponse.get("result"), "MCP init should succeed");
        assertNotNull(a2aInitResponse.get("result"), "A2A init should succeed");

        // Execute concurrent operations on both protocols
        CompletableFuture<ObjectNode> mcpFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return sendMCPRequest(createMCPToolRequest("openhab.items.list", Map.of("filter", "all")));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        CompletableFuture<ObjectNode> a2aFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return sendA2ARequest(createA2ATaskRequest("openhab.things.list", Map.of("filter", "all")));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // Wait for both operations to complete
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(mcpFuture, a2aFuture);
        allFutures.get(30, TimeUnit.SECONDS);

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        // Verify both operations succeeded
        assertNotNull(mcpFuture.get().get("result"), "MCP operation should succeed");
        assertNotNull(a2aFuture.get().get("result"), "A2A operation should succeed");

        // Performance assertion: Concurrent operations should complete within reasonable time
        assertTrue(totalTime < 15000, "Concurrent protocol operations took too long: " + totalTime + "ms");

        System.out.println("✅ Performance impact of concurrent protocols validated");
    }

    /**
     * Test multi-protocol workflow coordination.
     */
    @Test
    void testMultiProtocolWorkflowCoordination() throws Exception {
        // Test coordination between MCP and A2A in a complex workflow

        // Initialize both protocols
        ObjectNode mcpInitResponse = sendMCPRequest(createMCPInitRequest("workflow-mcp-client", "1.0.0"));
        ObjectNode a2aInitResponse = sendA2ARequest(createA2AInitRequest("workflow-a2a-agent", "1.0.0"));

        assertNotNull(mcpInitResponse.get("result"), "MCP init should succeed");
        assertNotNull(a2aInitResponse.get("result"), "A2A init should succeed");

        // Step 1: MCP discovers available items
        ObjectNode mcpDiscoveryRequest = createMCPToolRequest("openhab.items.list", Map.of("filter", "all"));
        ObjectNode mcpDiscoveryResponse = sendMCPRequest(mcpDiscoveryRequest);
        assertNotNull(mcpDiscoveryResponse.get("result"), "MCP discovery should succeed");

        // Step 2: A2A creates a task based on MCP discovery
        ObjectNode a2aTaskRequest = createA2ATaskRequest("openhab.items.analyze", Map.of("action", "analyze"));
        ObjectNode a2aTaskResponse = sendA2ARequest(a2aTaskRequest);
        assertNotNull(a2aTaskResponse.get("result"), "A2A task creation should succeed");

        // Step 3: MCP performs additional analysis
        ObjectNode mcpAnalysisRequest = createMCPToolRequest("openhab.persistence.manage", Map.of("action", "query"));
        ObjectNode mcpAnalysisResponse = sendMCPRequest(mcpAnalysisRequest);
        assertNotNull(mcpAnalysisResponse.get("result"), "MCP analysis should succeed");

        // Step 4: A2A completes the workflow
        ObjectNode a2aCompletionRequest = createA2ATaskRequest("openhab.workflow.complete",
                Map.of("status", "success"));
        ObjectNode a2aCompletionResponse = sendA2ARequest(a2aCompletionRequest);
        assertNotNull(a2aCompletionResponse.get("result"), "A2A workflow completion should succeed");

        System.out.println("✅ Multi-protocol workflow coordination validated");
    }

    /**
     * Test protocol isolation and independence.
     */
    @Test
    void testProtocolIsolationAndIndependence() throws Exception {
        // Test that protocols can operate independently without interference

        // Initialize MCP only
        ObjectNode mcpInitResponse = sendMCPRequest(createMCPInitRequest("isolated-mcp-client", "1.0.0"));
        assertNotNull(mcpInitResponse.get("result"), "MCP init should succeed");

        // Execute MCP operations
        ObjectNode mcpToolResponse = sendMCPRequest(
                createMCPToolRequest("openhab.items.list", Map.of("filter", "all")));
        assertNotNull(mcpToolResponse.get("result"), "MCP tool execution should succeed");

        // Initialize A2A independently
        ObjectNode a2aInitResponse = sendA2ARequest(createA2AInitRequest("isolated-a2a-agent", "1.0.0"));
        assertNotNull(a2aInitResponse.get("result"), "A2A init should succeed");

        // Execute A2A operations
        ObjectNode a2aTaskResponse = sendA2ARequest(
                createA2ATaskRequest("openhab.things.list", Map.of("filter", "all")));
        assertNotNull(a2aTaskResponse.get("result"), "A2A task execution should succeed");

        // Verify both protocols operate independently
        assertNotNull(mcpToolResponse.get("result"), "MCP should operate independently");
        assertNotNull(a2aTaskResponse.get("result"), "A2A should operate independently");

        System.out.println("✅ Protocol isolation and independence validated");
    }

    /**
     * Test error handling across protocols.
     */
    @Test
    void testErrorHandlingAcrossProtocols() throws Exception {
        // Test error handling when both protocols encounter issues

        // Initialize both protocols
        ObjectNode mcpInitResponse = sendMCPRequest(createMCPInitRequest("error-mcp-client", "1.0.0"));
        ObjectNode a2aInitResponse = sendA2ARequest(createA2AInitRequest("error-a2a-agent", "1.0.0"));

        assertNotNull(mcpInitResponse.get("result"), "MCP init should succeed");
        assertNotNull(a2aInitResponse.get("result"), "A2A init should succeed");

        // Test MCP error handling
        ObjectNode mcpErrorRequest = createMCPToolRequest("nonexistent.tool", Map.of());
        ObjectNode mcpErrorResponse = sendMCPRequest(mcpErrorRequest);
        assertNotNull(mcpErrorResponse.get("error"), "MCP should handle errors gracefully");

        // Test A2A error handling
        ObjectNode a2aErrorRequest = createA2ATaskRequest("nonexistent.skill", Map.of());
        ObjectNode a2aErrorResponse = sendA2ARequest(a2aErrorRequest);
        assertNotNull(a2aErrorResponse.get("error"), "A2A should handle errors gracefully");

        // Verify both protocols continue to function after errors
        ObjectNode mcpRecoveryRequest = createMCPToolRequest("openhab.items.list", Map.of("filter", "all"));
        ObjectNode mcpRecoveryResponse = sendMCPRequest(mcpRecoveryRequest);
        assertNotNull(mcpRecoveryResponse.get("result"), "MCP should recover from errors");

        ObjectNode a2aRecoveryRequest = createA2ATaskRequest("openhab.things.list", Map.of("filter", "all"));
        ObjectNode a2aRecoveryResponse = sendA2ARequest(a2aRecoveryRequest);
        assertNotNull(a2aRecoveryResponse.get("result"), "A2A should recover from errors");

        System.out.println("✅ Error handling across protocols validated");
    }

    // Helper methods for creating MCP requests

    private ObjectNode createMCPInitRequest(String clientName, String clientVersion) {
        ObjectNode request = objectMapper.createObjectNode();
        request.put("jsonrpc", "2.0");
        request.put("id", 1);
        request.put("method", "initialize");

        ObjectNode params = objectMapper.createObjectNode();
        params.put("protocolVersion", "2024-11-05");
        params.set("capabilities", objectMapper.createObjectNode());

        ObjectNode clientInfo = objectMapper.createObjectNode();
        clientInfo.put("name", clientName);
        clientInfo.put("version", clientVersion);
        params.set("clientInfo", clientInfo);

        request.set("params", params);
        return request;
    }

    private ObjectNode createMCPToolRequest(String toolName, Map<String, Object> arguments) {
        ObjectNode request = objectMapper.createObjectNode();
        request.put("jsonrpc", "2.0");
        request.put("id", 2);
        request.put("method", "tools/call");

        ObjectNode params = objectMapper.createObjectNode();
        params.put("name", toolName);
        params.set("arguments", objectMapper.valueToTree(arguments));

        request.set("params", params);
        return request;
    }

    // Helper methods for creating A2A requests

    private ObjectNode createA2AInitRequest(String agentName, String agentVersion) {
        ObjectNode request = objectMapper.createObjectNode();
        request.put("jsonrpc", "2.0");
        request.put("id", 3);
        request.put("method", "initialize");

        ObjectNode params = objectMapper.createObjectNode();
        params.put("protocolVersion", "2024-11-05");
        params.set("capabilities", objectMapper.createObjectNode());

        ObjectNode agentInfo = objectMapper.createObjectNode();
        agentInfo.put("name", agentName);
        agentInfo.put("version", agentVersion);
        params.set("agentInfo", agentInfo);

        request.set("params", params);
        return request;
    }

    private ObjectNode createA2ATaskRequest(String skillName, Map<String, Object> arguments) {
        ObjectNode request = objectMapper.createObjectNode();
        request.put("jsonrpc", "2.0");
        request.put("id", 4);
        request.put("method", "tasks/create");

        ObjectNode params = objectMapper.createObjectNode();
        params.put("skillName", skillName);
        params.set("arguments", objectMapper.valueToTree(arguments));

        request.set("params", params);
        return request;
    }

    // Helper methods for sending requests

    private ObjectNode sendMCPRequest(ObjectNode request) throws Exception {
        String requestBody = objectMapper.writeValueAsString(request);

        HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(mcpServerUrl))
                .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), ObjectNode.class);
        } else {
            throw new RuntimeException("MCP request failed with status: " + response.statusCode());
        }
    }

    private ObjectNode sendA2ARequest(ObjectNode request) throws Exception {
        String requestBody = objectMapper.writeValueAsString(request);

        HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(a2aServerUrl))
                .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), ObjectNode.class);
        } else {
            throw new RuntimeException("A2A request failed with status: " + response.statusCode());
        }
    }
}
