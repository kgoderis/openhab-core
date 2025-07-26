# Integration Testing Examples for MCP and A2A Bundles

## Overview

This document provides concrete examples of how to implement integration tests for the MCP and A2A bundles, including OSGi container testing and end-to-end scenarios.

## 1. MCP Integration Test Example

### 1.1 OSGi Container Integration Test

```java
package org.openhab.core.ai.mcp.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openhab.core.ai.common.api.action.AIAction;
import org.openhab.core.ai.common.api.action.AIActionContext;
import org.openhab.core.ai.common.api.action.AIActionResult;
import org.openhab.core.ai.mcp.internal.MCPServerManager;
import org.openhab.core.ai.mcp.internal.MCPToolRegistry;
import org.openhab.core.test.java.JavaOSGiTest;
import org.osgi.service.component.runtime.ServiceComponentRuntime;
import org.osgi.test.common.annotation.InjectService;

@ExtendWith(JavaOSGiTest.class)
class MCPIntegrationTest {

    @InjectService
    private MCPServerManager serverManager;

    @InjectService
    private MCPToolRegistry toolRegistry;

    @InjectService
    private ServiceComponentRuntime scr;

    @BeforeEach
    void setUp() {
        // Wait for services to be ready
        assertNotNull(serverManager, "MCPServerManager should be available");
        assertNotNull(toolRegistry, "MCPToolRegistry should be available");
    }

    @Test
    void testMCPToolRegistration() {
        // Verify that AIActions are registered as MCP tools
        Map<String, Object> tools = toolRegistry.getAvailableTools();
        assertNotNull(tools);
        assertFalse(tools.isEmpty(), "Should have registered tools");
        
        // Verify specific tool registration
        assertTrue(tools.containsKey("openhab.items.list"), "Items list tool should be registered");
        assertTrue(tools.containsKey("openhab.persistence.manage"), "Persistence tool should be registered");
    }

    @Test
    void testMCPToolExecution() throws Exception {
        // Test tool execution through MCP protocol
        Map<String, Object> parameters = Map.of("filter", "all");
        
        AIActionResult result = toolRegistry.executeTool("openhab.items.list", parameters);
        
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
    }

    @Test
    void testMCPServerLifecycle() {
        // Test server startup and shutdown
        assertTrue(serverManager.isRunning(), "Server should be running");
        
        // Test server configuration
        var config = serverManager.getConfiguration();
        assertNotNull(config);
        assertNotNull(config.getTransportType());
    }

    @Test
    void testMCPTransportIntegration() throws Exception {
        // Test transport layer integration
        var transport = serverManager.getCurrentTransport();
        assertNotNull(transport);
        
        // Test transport health
        var health = transport.getHealth();
        assertNotNull(health);
        assertTrue(health.isHealthy(), "Transport should be healthy");
    }

    @Test
    void testMCPErrorHandling() {
        // Test error handling for invalid tool calls
        Map<String, Object> invalidParameters = Map.of("invalid", "parameter");
        
        AIActionResult result = toolRegistry.executeTool("openhab.items.list", invalidParameters);
        
        assertNotNull(result);
        assertFalse(result.isSuccess(), "Should fail with invalid parameters");
        assertNotNull(result.getMessage(), "Should have error message");
    }

    @Test
    void testMCPConcurrentExecution() throws Exception {
        // Test concurrent tool execution
        CompletableFuture<AIActionResult> future1 = CompletableFuture.supplyAsync(() -> {
            return toolRegistry.executeTool("openhab.items.list", Map.of("filter", "all"));
        });
        
        CompletableFuture<AIActionResult> future2 = CompletableFuture.supplyAsync(() -> {
            return toolRegistry.executeTool("openhab.persistence.manage", Map.of("action", "status"));
        });
        
        // Wait for both to complete
        AIActionResult result1 = future1.get(10, TimeUnit.SECONDS);
        AIActionResult result2 = future2.get(10, TimeUnit.SECONDS);
        
        assertTrue(result1.isSuccess());
        assertTrue(result2.isSuccess());
    }
}
```

### 1.2 MCP Client Integration Test

```java
package org.openhab.core.ai.mcp.integration.client;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openhab.core.test.java.JavaOSGiTest;
import org.osgi.test.common.annotation.InjectService;

import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(JavaOSGiTest.class)
class MCPClientIntegrationTest {

    private HttpClient httpClient;
    private ObjectMapper objectMapper;
    private String baseUrl = "http://localhost:8080/mcp";

    @InjectService
    private MCPServerManager serverManager;

    @BeforeEach
    void setUp() {
        httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        objectMapper = new ObjectMapper();
        
        // Ensure server is running
        assertTrue(serverManager.isRunning(), "MCP server should be running");
    }

    @Test
    void testMCPClientConnection() throws Exception {
        // Test basic connectivity
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/health"))
            .GET()
            .build();

        HttpResponse<String> response = httpClient.send(request, 
            HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        
        Map<String, Object> healthData = objectMapper.readValue(response.body(), Map.class);
        assertEquals("healthy", healthData.get("status"));
    }

    @Test
    void testMCPToolListing() throws Exception {
        // Test tool listing endpoint
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/tools"))
            .GET()
            .build();

        HttpResponse<String> response = httpClient.send(request, 
            HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        
        Map<String, Object> toolsData = objectMapper.readValue(response.body(), Map.class);
        assertNotNull(toolsData.get("tools"));
        assertTrue(((List<?>) toolsData.get("tools")).size() > 0);
    }

    @Test
    void testMCPToolExecution() throws Exception {
        // Test tool execution via HTTP
        Map<String, Object> requestBody = Map.of(
            "tool", "openhab.items.list",
            "parameters", Map.of("filter", "all")
        );

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/execute"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();

        HttpResponse<String> response = httpClient.send(request, 
            HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        
        Map<String, Object> result = objectMapper.readValue(response.body(), Map.class);
        assertTrue((Boolean) result.get("success"));
        assertNotNull(result.get("data"));
    }

    @Test
    void testMCPAuthentication() throws Exception {
        // Test authentication requirements
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/admin"))
            .GET()
            .build();

        HttpResponse<String> response = httpClient.send(request, 
            HttpResponse.BodyHandlers.ofString());

        assertEquals(401, response.statusCode(), "Should require authentication");
    }
}
```

## 2. A2A Integration Test Example

### 2.1 A2A Server Integration Test

```java
package org.openhab.core.ai.a2a.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openhab.core.ai.a2a.internal.A2AServerManager;
import org.openhab.core.ai.a2a.internal.A2ASkillRegistry;
import org.openhab.core.ai.a2a.api.A2ASkillResult;
import org.openhab.core.test.java.JavaOSGiTest;
import org.osgi.test.common.annotation.InjectService;

import io.a2a.spec.Message;
import io.a2a.spec.Part;

@ExtendWith(JavaOSGiTest.class)
class A2AIntegrationTest {

    @InjectService
    private A2AServerManager serverManager;

    @InjectService
    private A2ASkillRegistry skillRegistry;

    @BeforeEach
    void setUp() {
        assertNotNull(serverManager, "A2AServerManager should be available");
        assertNotNull(skillRegistry, "A2ASkillRegistry should be available");
    }

    @Test
    void testA2ASkillRegistration() {
        // Verify that AIActions are registered as A2A skills
        var skills = skillRegistry.getSkillDefinitions();
        assertNotNull(skills);
        assertFalse(skills.isEmpty(), "Should have registered skills");
        
        // Verify specific skill registration
        assertTrue(skills.stream()
            .anyMatch(skill -> skill.getSkillId().equals("openhab.items.list")), 
            "Items list skill should be registered");
    }

    @Test
    void testA2ASkillExecution() throws Exception {
        // Test skill execution through A2A protocol
        Message message = createTestMessage("openhab.items.list", "filter=all");
        
        A2ASkillResult result = skillRegistry.executeSkill("openhab.items.list", message);
        
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
    }

    @Test
    void testA2AServerLifecycle() {
        // Test server startup and shutdown
        assertTrue(serverManager.isRunning(), "Server should be running");
        
        // Test server components
        assertNotNull(serverManager.getAgentCard(), "Agent card should be available");
        assertNotNull(serverManager.getRequestHandler(), "Request handler should be available");
    }

    @Test
    void testA2ATaskManagement() throws Exception {
        // Test task creation and execution
        Message message = createTestMessage("openhab.persistence.manage", "action=status");
        
        String taskId = serverManager.executeSkill("openhab.persistence.manage", message);
        assertNotNull(taskId);
        
        // Wait for task completion
        Thread.sleep(1000);
        
        var task = serverManager.getTaskStore().getTask(taskId);
        assertNotNull(task);
        assertEquals("COMPLETED", task.getState().toString());
    }

    @Test
    void testA2AExternalAgentCommunication() throws Exception {
        // Test communication with external agents
        var agentCard = serverManager.getAgentCard();
        assertNotNull(agentCard);
        
        var capabilities = agentCard.getCapabilities();
        assertNotNull(capabilities);
        assertTrue(capabilities.getSkills().size() > 0, "Should have skills");
    }

    @Test
    void testA2AErrorHandling() {
        // Test error handling for invalid skill calls
        Message invalidMessage = createTestMessage("invalid.skill", "param=value");
        
        A2ASkillResult result = skillRegistry.executeSkill("invalid.skill", invalidMessage);
        
        assertNotNull(result);
        assertFalse(result.isSuccess(), "Should fail with invalid skill");
        assertNotNull(result.getErrorMessage(), "Should have error message");
    }

    @Test
    void testA2AConcurrentExecution() throws Exception {
        // Test concurrent skill execution
        Message message1 = createTestMessage("openhab.items.list", "filter=all");
        Message message2 = createTestMessage("openhab.persistence.manage", "action=status");
        
        CompletableFuture<A2ASkillResult> future1 = CompletableFuture.supplyAsync(() -> {
            return skillRegistry.executeSkill("openhab.items.list", message1);
        });
        
        CompletableFuture<A2ASkillResult> future2 = CompletableFuture.supplyAsync(() -> {
            return skillRegistry.executeSkill("openhab.persistence.manage", message2);
        });
        
        // Wait for both to complete
        A2ASkillResult result1 = future1.get(10, TimeUnit.SECONDS);
        A2ASkillResult result2 = future2.get(10, TimeUnit.SECONDS);
        
        assertTrue(result1.isSuccess());
        assertTrue(result2.isSuccess());
    }

    private Message createTestMessage(String skillId, String parameters) {
        // Create a test A2A message
        Part part = new Part() {
            @Override
            public String getText() {
                return skillId + " " + parameters;
            }
        };
        
        return new Message() {
            @Override
            public List<Part> getParts() {
                return List.of(part);
            }
        };
    }
}
```

### 2.2 A2A REST API Integration Test

```java
package org.openhab.core.ai.a2a.integration.rest;

import static org.junit.jupiter.api.Assertions.*;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openhab.core.test.java.JavaOSGiTest;
import org.osgi.test.common.annotation.InjectService;

import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(JavaOSGiTest.class)
class A2ARestIntegrationTest {

    private HttpClient httpClient;
    private ObjectMapper objectMapper;
    private String baseUrl = "http://localhost:8080/a2a";

    @InjectService
    private A2AServerManager serverManager;

    @BeforeEach
    void setUp() {
        httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        objectMapper = new ObjectMapper();
        
        assertTrue(serverManager.isRunning(), "A2A server should be running");
    }

    @Test
    void testA2AHealthCheck() throws Exception {
        // Test health check endpoint
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/health"))
            .GET()
            .build();

        HttpResponse<String> response = httpClient.send(request, 
            HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        
        Map<String, Object> healthData = objectMapper.readValue(response.body(), Map.class);
        assertEquals("healthy", healthData.get("status"));
    }

    @Test
    void testA2AAgentCard() throws Exception {
        // Test agent card endpoint
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/agent"))
            .GET()
            .build();

        HttpResponse<String> response = httpClient.send(request, 
            HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        
        Map<String, Object> agentData = objectMapper.readValue(response.body(), Map.class);
        assertNotNull(agentData.get("name"));
        assertNotNull(agentData.get("capabilities"));
    }

    @Test
    void testA2ASkillExecution() throws Exception {
        // Test skill execution via REST
        Map<String, Object> requestBody = Map.of(
            "skillId", "openhab.items.list",
            "message", Map.of(
                "parts", List.of(Map.of("text", "openhab.items.list filter=all"))
            )
        );

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/skills/execute"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();

        HttpResponse<String> response = httpClient.send(request, 
            HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        
        Map<String, Object> result = objectMapper.readValue(response.body(), Map.class);
        assertTrue((Boolean) result.get("success"));
        assertNotNull(result.get("data"));
    }

    @Test
    void testA2ATaskRetrieval() throws Exception {
        // Test task retrieval endpoint
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/tasks"))
            .GET()
            .build();

        HttpResponse<String> response = httpClient.send(request, 
            HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        
        Map<String, Object> tasksData = objectMapper.readValue(response.body(), Map.class);
        assertNotNull(tasksData.get("tasks"));
    }
}
```

## 3. End-to-End Integration Test Example

### 3.1 Complete Workflow Test

```java
package org.openhab.core.ai.integration.workflow;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openhab.core.ai.common.api.action.AIAction;
import org.openhab.core.ai.common.api.action.AIActionContext;
import org.openhab.core.ai.common.api.action.AIActionResult;
import org.openhab.core.ai.mcp.internal.MCPServerManager;
import org.openhab.core.ai.a2a.internal.A2AServerManager;
import org.openhab.core.test.java.JavaOSGiTest;
import org.osgi.test.common.annotation.InjectService;

@ExtendWith(JavaOSGiTest.class)
class EndToEndIntegrationTest {

    @InjectService
    private MCPServerManager mcpServerManager;

    @InjectService
    private A2AServerManager a2aServerManager;

    @InjectService
    private AIActionContext actionContext;

    @BeforeEach
    void setUp() {
        assertNotNull(mcpServerManager, "MCP server should be available");
        assertNotNull(a2aServerManager, "A2A server should be available");
        assertNotNull(actionContext, "Action context should be available");
    }

    @Test
    void testCompleteWorkflow() throws Exception {
        // Test a complete workflow involving multiple actions
        
        // Step 1: List items via MCP
        Map<String, Object> mcpResult = mcpServerManager.getToolRegistry()
            .executeTool("openhab.items.list", Map.of("filter", "all"))
            .getData();
        
        assertNotNull(mcpResult);
        assertTrue(((List<?>) mcpResult.get("items")).size() > 0);
        
        // Step 2: Check persistence status via A2A
        var a2aResult = a2aServerManager.getSkillRegistry()
            .executeSkill("openhab.persistence.manage", 
                createMessage("openhab.persistence.manage action=status"));
        
        assertTrue(a2aResult.isSuccess());
        assertNotNull(a2aResult.getData());
        
        // Step 3: Verify both protocols work with same underlying actions
        assertTrue(mcpResult.containsKey("items"));
        assertTrue(a2aResult.getData().containsKey("persistenceAvailable"));
    }

    @Test
    void testCrossProtocolConsistency() throws Exception {
        // Test that MCP and A2A provide consistent results for the same action
        
        // Execute same action via both protocols
        var mcpResult = mcpServerManager.getToolRegistry()
            .executeTool("openhab.persistence.manage", Map.of("action", "status"));
        
        var a2aResult = a2aServerManager.getSkillRegistry()
            .executeSkill("openhab.persistence.manage", 
                createMessage("openhab.persistence.manage action=status"));
        
        // Verify consistent results
        assertTrue(mcpResult.isSuccess());
        assertTrue(a2aResult.isSuccess());
        
        // Compare key data points
        var mcpData = mcpResult.getData();
        var a2aData = a2aResult.getData();
        
        assertEquals(mcpData.get("action"), a2aData.get("action"));
        assertEquals(mcpData.get("persistenceAvailable"), a2aData.get("persistenceAvailable"));
    }

    @Test
    void testConcurrentProtocolExecution() throws Exception {
        // Test concurrent execution across both protocols
        
        CompletableFuture<AIActionResult> mcpFuture = CompletableFuture.supplyAsync(() -> {
            return mcpServerManager.getToolRegistry()
                .executeTool("openhab.items.list", Map.of("filter", "all"));
        });
        
        CompletableFuture<A2ASkillResult> a2aFuture = CompletableFuture.supplyAsync(() -> {
            return a2aServerManager.getSkillRegistry()
                .executeSkill("openhab.persistence.manage", 
                    createMessage("openhab.persistence.manage action=status"));
        });
        
        // Wait for both to complete
        var mcpResult = mcpFuture.get(10, TimeUnit.SECONDS);
        var a2aResult = a2aFuture.get(10, TimeUnit.SECONDS);
        
        assertTrue(mcpResult.isSuccess());
        assertTrue(a2aResult.isSuccess());
    }

    private Message createMessage(String text) {
        return new Message() {
            @Override
            public List<Part> getParts() {
                return List.of(new Part() {
                    @Override
                    public String getText() {
                        return text;
                    }
                });
            }
        };
    }
}
```

## 4. Integration Test Configuration

### 4.1 Test Dependencies

```xml
<dependencies>
    <!-- OSGi Testing -->
    <dependency>
        <groupId>org.osgi</groupId>
        <artifactId>org.osgi.test.junit5</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- openHAB Testing -->
    <dependency>
        <groupId>org.openhab.core.bundles</groupId>
        <artifactId>org.openhab.core.test</artifactId>
        <version>${project.version}</version>
        <scope>test</scope>
    </dependency>
    
    <!-- HTTP Client for REST testing -->
    <dependency>
        <groupId>org.apache.httpcomponents.client5</groupId>
        <artifactId>httpclient5</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- JSON Processing -->
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### 4.2 Test Configuration

```properties
# Integration test configuration
org.openhab.core.ai.mcp.transport.type=STDIO
org.openhab.core.ai.mcp.transport.base.url=http://localhost:8080
org.openhab.core.ai.a2a.server.port=8080
org.openhab.core.ai.a2a.server.host=localhost
```

## 5. Best Practices for Integration Tests

### 5.1 Test Organization
- Separate unit tests from integration tests
- Use descriptive test names that explain the scenario
- Group related tests in test classes
- Use test categories for different execution speeds

### 5.2 Test Data Management
- Use realistic test data that mirrors production
- Clean up test data after tests
- Use test data factories for consistent data creation
- Isolate test data between tests

### 5.3 Error Handling
- Test both success and failure scenarios
- Verify error messages and error codes
- Test timeout scenarios
- Test resource cleanup

### 5.4 Performance Considerations
- Monitor test execution times
- Use appropriate timeouts
- Test concurrent execution scenarios
- Optimize slow tests

### 5.5 Reliability
- Make tests deterministic
- Avoid flaky tests
- Use proper synchronization for concurrent tests
- Handle OSGi service lifecycle properly

This comprehensive integration testing approach ensures that both MCP and A2A bundles work correctly in a real OSGi environment and can handle end-to-end scenarios effectively. 