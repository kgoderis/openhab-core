# MCP Client Automation Testing Guide

## Overview

This guide provides comprehensive strategies for automating tests with real MCP clients, including Claude, GPT-4, and other AI assistants that support the Model Context Protocol.

## 1. MCP Client Types and Testing Strategies

### 1.1 STDIO-Based MCP Clients

**Examples**: Claude Desktop, Local MCP servers
**Testing Approach**: Process-based automation

```java
package org.openhab.core.ai.mcp.integration.client;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openhab.core.test.java.JavaOSGiTest;
import org.osgi.test.common.annotation.InjectService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@ExtendWith(JavaOSGiTest.class)
class MCPStdioClientTest {

    private Process mcpClientProcess;
    private BufferedWriter writer;
    private BufferedReader reader;
    private ObjectMapper objectMapper;

    @InjectService
    private MCPServerManager serverManager;

    @BeforeEach
    void setUp() throws IOException {
        objectMapper = new ObjectMapper();
        
        // Start MCP client process (example with Claude Desktop)
        ProcessBuilder processBuilder = new ProcessBuilder(
            "claude", "--mcp-server", "openhab-mcp-server"
        );
        processBuilder.redirectErrorStream(true);
        
        mcpClientProcess = processBuilder.start();
        writer = new BufferedWriter(new OutputStreamWriter(mcpClientProcess.getOutputStream()));
        reader = new BufferedReader(new InputStreamReader(mcpClientProcess.getInputStream()));
        
        // Wait for client to be ready
        waitForClientReady();
    }

    @Test
    void testMCPClientConnection() throws Exception {
        // Send initialization request
        ObjectNode initRequest = objectMapper.createObjectNode();
        initRequest.put("jsonrpc", "2.0");
        initRequest.put("id", 1);
        initRequest.put("method", "initialize");
        
        ObjectNode params = objectMapper.createObjectNode();
        params.put("protocolVersion", "2024-11-05");
        params.put("capabilities", objectMapper.createObjectNode());
        params.put("clientInfo", objectMapper.createObjectNode()
            .put("name", "test-client")
            .put("version", "1.0.0"));
        
        initRequest.set("params", params);
        
        sendRequest(initRequest);
        
        // Verify initialization response
        ObjectNode response = readResponse();
        assertEquals("2.0", response.get("jsonrpc").asText());
        assertEquals(1, response.get("id").asInt());
        assertNotNull(response.get("result"));
    }

    @Test
    void testMCPToolListing() throws Exception {
        // Send tools/list request
        ObjectNode listRequest = objectMapper.createObjectNode();
        listRequest.put("jsonrpc", "2.0");
        listRequest.put("id", 2);
        listRequest.put("method", "tools/list");
        listRequest.set("params", objectMapper.createObjectNode());
        
        sendRequest(listRequest);
        
        // Verify tools list response
        ObjectNode response = readResponse();
        assertEquals(2, response.get("id").asInt());
        
        ObjectNode result = (ObjectNode) response.get("result");
        assertNotNull(result.get("tools"));
        
        // Verify specific tools are available
        var tools = result.get("tools");
        boolean hasItemsTool = false;
        boolean hasPersistenceTool = false;
        
        for (var tool : tools) {
            String name = tool.get("name").asText();
            if ("openhab.items.list".equals(name)) hasItemsTool = true;
            if ("openhab.persistence.manage".equals(name)) hasPersistenceTool = true;
        }
        
        assertTrue(hasItemsTool, "Items list tool should be available");
        assertTrue(hasPersistenceTool, "Persistence tool should be available");
    }

    @Test
    void testMCPToolExecution() throws Exception {
        // Send tools/call request
        ObjectNode callRequest = objectMapper.createObjectNode();
        callRequest.put("jsonrpc", "2.0");
        callRequest.put("id", 3);
        callRequest.put("method", "tools/call");
        
        ObjectNode params = objectMapper.createObjectNode();
        params.put("name", "openhab.items.list");
        
        ObjectNode arguments = objectMapper.createObjectNode();
        arguments.put("filter", "all");
        params.set("arguments", arguments);
        
        callRequest.set("params", params);
        
        sendRequest(callRequest);
        
        // Verify tool execution response
        ObjectNode response = readResponse();
        assertEquals(3, response.get("id").asInt());
        
        ObjectNode result = (ObjectNode) response.get("result");
        assertNotNull(result.get("content"));
        
        // Verify content contains items data
        var content = result.get("content");
        assertTrue(content.toString().contains("items"), "Response should contain items data");
    }

    @Test
    void testMCPErrorHandling() throws Exception {
        // Send invalid tool call
        ObjectNode invalidRequest = objectMapper.createObjectNode();
        invalidRequest.put("jsonrpc", "2.0");
        invalidRequest.put("id", 4);
        invalidRequest.put("method", "tools/call");
        
        ObjectNode params = objectMapper.createObjectNode();
        params.put("name", "invalid.tool");
        params.set("arguments", objectMapper.createObjectNode());
        
        invalidRequest.set("params", params);
        
        sendRequest(invalidRequest);
        
        // Verify error response
        ObjectNode response = readResponse();
        assertEquals(4, response.get("id").asInt());
        assertNotNull(response.get("error"));
        
        ObjectNode error = (ObjectNode) response.get("error");
        assertTrue(error.get("code").asInt() > 0, "Should have error code");
        assertNotNull(error.get("message"), "Should have error message");
    }

    @Test
    void testMCPConcurrentRequests() throws Exception {
        // Send multiple concurrent requests
        CompletableFuture<ObjectNode> future1 = CompletableFuture.supplyAsync(() -> {
            try {
                sendRequest(createToolCallRequest(5, "openhab.items.list", Map.of("filter", "all")));
                return readResponse();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        
        CompletableFuture<ObjectNode> future2 = CompletableFuture.supplyAsync(() -> {
            try {
                sendRequest(createToolCallRequest(6, "openhab.persistence.manage", Map.of("action", "status")));
                return readResponse();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        
        // Wait for both responses
        ObjectNode response1 = future1.get(30, TimeUnit.SECONDS);
        ObjectNode response2 = future2.get(30, TimeUnit.SECONDS);
        
        assertEquals(5, response1.get("id").asInt());
        assertEquals(6, response2.get("id").asInt());
        assertNotNull(response1.get("result"));
        assertNotNull(response2.get("result"));
    }

    private void sendRequest(ObjectNode request) throws IOException {
        String json = objectMapper.writeValueAsString(request);
        writer.write(json + "\n");
        writer.flush();
    }

    private ObjectNode readResponse() throws IOException {
        String line = reader.readLine();
        if (line == null) {
            throw new IOException("No response from MCP client");
        }
        return objectMapper.readValue(line, ObjectNode.class);
    }

    private void waitForClientReady() throws IOException {
        // Wait for client to send initial message or timeout
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < 10000) {
            if (reader.ready()) {
                String line = reader.readLine();
                if (line != null && line.contains("jsonrpc")) {
                    return; // Client is ready
                }
            }
            Thread.sleep(100);
        }
        throw new IOException("MCP client did not become ready within timeout");
    }

    private ObjectNode createToolCallRequest(int id, String toolName, Map<String, Object> arguments) {
        ObjectNode request = objectMapper.createObjectNode();
        request.put("jsonrpc", "2.0");
        request.put("id", id);
        request.put("method", "tools/call");
        
        ObjectNode params = objectMapper.createObjectNode();
        params.put("name", toolName);
        params.set("arguments", objectMapper.valueToTree(arguments));
        
        request.set("params", params);
        return request;
    }
}
```

### 1.2 HTTP-Based MCP Clients

**Examples**: Web-based MCP clients, REST APIs
**Testing Approach**: HTTP client automation

```java
package org.openhab.core.ai.mcp.integration.http;

import static org.junit.jupiter.api.Assertions.*;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openhab.core.test.java.JavaOSGiTest;
import org.osgi.test.common.annotation.InjectService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@ExtendWith(JavaOSGiTest.class)
class MCPHttpClientTest {

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
        
        assertTrue(serverManager.isRunning(), "MCP server should be running");
    }

    @Test
    void testMCPHttpInitialization() throws Exception {
        // Test HTTP-based MCP initialization
        ObjectNode initRequest = objectMapper.createObjectNode();
        initRequest.put("jsonrpc", "2.0");
        initRequest.put("id", 1);
        initRequest.put("method", "initialize");
        
        ObjectNode params = objectMapper.createObjectNode();
        params.put("protocolVersion", "2024-11-05");
        params.put("capabilities", objectMapper.createObjectNode());
        params.put("clientInfo", objectMapper.createObjectNode()
            .put("name", "http-test-client")
            .put("version", "1.0.0"));
        
        initRequest.set("params", params);
        
        String jsonBody = objectMapper.writeValueAsString(initRequest);
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/jsonrpc"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();

        HttpResponse<String> response = httpClient.send(request, 
            HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        
        ObjectNode responseJson = objectMapper.readValue(response.body(), ObjectNode.class);
        assertEquals("2.0", responseJson.get("jsonrpc").asText());
        assertEquals(1, responseJson.get("id").asInt());
        assertNotNull(responseJson.get("result"));
    }

    @Test
    void testMCPHttpToolExecution() throws Exception {
        // Test HTTP-based tool execution
        ObjectNode callRequest = objectMapper.createObjectNode();
        callRequest.put("jsonrpc", "2.0");
        callRequest.put("id", 2);
        callRequest.put("method", "tools/call");
        
        ObjectNode params = objectMapper.createObjectNode();
        params.put("name", "openhab.items.list");
        
        ObjectNode arguments = objectMapper.createObjectNode();
        arguments.put("filter", "all");
        params.set("arguments", arguments);
        
        callRequest.set("params", params);
        
        String jsonBody = objectMapper.writeValueAsString(callRequest);
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/jsonrpc"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();

        HttpResponse<String> response = httpClient.send(request, 
            HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        
        ObjectNode responseJson = objectMapper.readValue(response.body(), ObjectNode.class);
        assertEquals(2, responseJson.get("id").asInt());
        assertNotNull(responseJson.get("result"));
        
        ObjectNode result = (ObjectNode) responseJson.get("result");
        assertNotNull(result.get("content"));
    }

    @Test
    void testMCPHttpStreaming() throws Exception {
        // Test HTTP streaming for long-running operations
        ObjectNode callRequest = objectMapper.createObjectNode();
        callRequest.put("jsonrpc", "2.0");
        callRequest.put("id", 3);
        callRequest.put("method", "tools/call");
        
        ObjectNode params = objectMapper.createObjectNode();
        params.put("name", "openhab.persistence.manage");
        params.put("stream", true); // Enable streaming
        
        ObjectNode arguments = objectMapper.createObjectNode();
        arguments.put("action", "backup");
        params.set("arguments", arguments);
        
        callRequest.set("params", params);
        
        String jsonBody = objectMapper.writeValueAsString(callRequest);
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/jsonrpc/stream"))
            .header("Content-Type", "application/json")
            .header("Accept", "text/event-stream")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();

        HttpResponse<String> response = httpClient.send(request, 
            HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        
        // Parse Server-Sent Events
        String[] events = response.body().split("\n");
        boolean hasData = false;
        boolean hasDone = false;
        
        for (String event : events) {
            if (event.startsWith("data: ")) {
                String data = event.substring(6);
                if (data.contains("content")) {
                    hasData = true;
                }
                if (data.contains("\"isError\":false")) {
                    hasDone = true;
                }
            }
        }
        
        assertTrue(hasData, "Should have received data events");
        assertTrue(hasDone, "Should have received completion event");
    }
}
```

### 1.3 WebSocket-Based MCP Clients

**Examples**: Real-time MCP clients, browser-based clients
**Testing Approach**: WebSocket client automation

```java
package org.openhab.core.ai.mcp.integration.websocket;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openhab.core.test.java.JavaOSGiTest;
import org.osgi.test.common.annotation.InjectService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@ExtendWith(JavaOSGiTest.class)
class MCPWebSocketClientTest {

    private ObjectMapper objectMapper;
    private String wsUrl = "ws://localhost:8080/mcp/ws";

    @InjectService
    private MCPServerManager serverManager;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        assertTrue(serverManager.isRunning(), "MCP server should be running");
    }

    @Test
    void testMCPWebSocketConnection() throws Exception {
        CountDownLatch connectionLatch = new CountDownLatch(1);
        CountDownLatch messageLatch = new CountDownLatch(1);
        
        CompletableFuture<String> messageFuture = new CompletableFuture<>();
        
        WebSocketClient client = new WebSocketClient(new URI(wsUrl)) {
            @Override
            public void onOpen(ServerHandshake handshake) {
                connectionLatch.countDown();
            }
            
            @Override
            public void onMessage(String message) {
                messageFuture.complete(message);
                messageLatch.countDown();
            }
            
            @Override
            public void onClose(int code, String reason, boolean remote) {
                // Handle close
            }
            
            @Override
            public void onError(Exception ex) {
                messageFuture.completeExceptionally(ex);
            }
        };
        
        client.connect();
        
        // Wait for connection
        assertTrue(connectionLatch.await(10, TimeUnit.SECONDS), "Should connect within timeout");
        
        // Send initialization request
        ObjectNode initRequest = objectMapper.createObjectNode();
        initRequest.put("jsonrpc", "2.0");
        initRequest.put("id", 1);
        initRequest.put("method", "initialize");
        
        ObjectNode params = objectMapper.createObjectNode();
        params.put("protocolVersion", "2024-11-05");
        params.put("capabilities", objectMapper.createObjectNode());
        params.put("clientInfo", objectMapper.createObjectNode()
            .put("name", "ws-test-client")
            .put("version", "1.0.0"));
        
        initRequest.set("params", params);
        
        String jsonBody = objectMapper.writeValueAsString(initRequest);
        client.send(jsonBody);
        
        // Wait for response
        assertTrue(messageLatch.await(10, TimeUnit.SECONDS), "Should receive response within timeout");
        
        String response = messageFuture.get(10, TimeUnit.SECONDS);
        ObjectNode responseJson = objectMapper.readValue(response, ObjectNode.class);
        
        assertEquals("2.0", responseJson.get("jsonrpc").asText());
        assertEquals(1, responseJson.get("id").asInt());
        assertNotNull(responseJson.get("result"));
        
        client.close();
    }

    @Test
    void testMCPWebSocketToolExecution() throws Exception {
        CountDownLatch connectionLatch = new CountDownLatch(1);
        CountDownLatch responseLatch = new CountDownLatch(1);
        
        CompletableFuture<String> responseFuture = new CompletableFuture<>();
        
        WebSocketClient client = new WebSocketClient(new URI(wsUrl)) {
            @Override
            public void onOpen(ServerHandshake handshake) {
                connectionLatch.countDown();
            }
            
            @Override
            public void onMessage(String message) {
                responseFuture.complete(message);
                responseLatch.countDown();
            }
            
            @Override
            public void onClose(int code, String reason, boolean remote) {
                // Handle close
            }
            
            @Override
            public void onError(Exception ex) {
                responseFuture.completeExceptionally(ex);
            }
        };
        
        client.connect();
        assertTrue(connectionLatch.await(10, TimeUnit.SECONDS));
        
        // Send tool execution request
        ObjectNode callRequest = objectMapper.createObjectNode();
        callRequest.put("jsonrpc", "2.0");
        callRequest.put("id", 2);
        callRequest.put("method", "tools/call");
        
        ObjectNode params = objectMapper.createObjectNode();
        params.put("name", "openhab.items.list");
        
        ObjectNode arguments = objectMapper.createObjectNode();
        arguments.put("filter", "all");
        params.set("arguments", arguments);
        
        callRequest.set("params", params);
        
        String jsonBody = objectMapper.writeValueAsString(callRequest);
        client.send(jsonBody);
        
        // Wait for response
        assertTrue(responseLatch.await(10, TimeUnit.SECONDS));
        
        String response = responseFuture.get(10, TimeUnit.SECONDS);
        ObjectNode responseJson = objectMapper.readValue(response, ObjectNode.class);
        
        assertEquals(2, responseJson.get("id").asInt());
        assertNotNull(responseJson.get("result"));
        
        client.close();
    }
}
```

## 2. MCP Client Test Automation Framework

### 2.1 Abstract MCP Client Test Base

```java
package org.openhab.core.ai.mcp.integration.framework;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openhab.core.test.java.JavaOSGiTest;
import org.osgi.test.common.annotation.InjectService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@ExtendWith(JavaOSGiTest.class)
abstract class AbstractMCPClientTest {

    protected ObjectMapper objectMapper;
    protected MCPClientAdapter clientAdapter;

    @InjectService
    protected MCPServerManager serverManager;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        clientAdapter = createClientAdapter();
        clientAdapter.connect();
        
        assertTrue(serverManager.isRunning(), "MCP server should be running");
    }

    protected abstract MCPClientAdapter createClientAdapter() throws Exception;

    @Test
    void testClientInitialization() throws Exception {
        ObjectNode response = clientAdapter.initialize();
        
        assertEquals("2.0", response.get("jsonrpc").asText());
        assertNotNull(response.get("result"));
        
        ObjectNode result = (ObjectNode) response.get("result");
        assertNotNull(result.get("serverInfo"));
        assertNotNull(result.get("capabilities"));
    }

    @Test
    void testToolListing() throws Exception {
        ObjectNode response = clientAdapter.listTools();
        
        assertEquals("2.0", response.get("jsonrpc").asText());
        
        ObjectNode result = (ObjectNode) response.get("result");
        assertNotNull(result.get("tools"));
        
        var tools = result.get("tools");
        assertTrue(tools.size() > 0, "Should have available tools");
        
        // Verify specific tools
        boolean hasItemsTool = false;
        boolean hasPersistenceTool = false;
        
        for (var tool : tools) {
            String name = tool.get("name").asText();
            if ("openhab.items.list".equals(name)) hasItemsTool = true;
            if ("openhab.persistence.manage".equals(name)) hasPersistenceTool = true;
        }
        
        assertTrue(hasItemsTool, "Items list tool should be available");
        assertTrue(hasPersistenceTool, "Persistence tool should be available");
    }

    @Test
    void testToolExecution() throws Exception {
        Map<String, Object> arguments = Map.of("filter", "all");
        ObjectNode response = clientAdapter.callTool("openhab.items.list", arguments);
        
        assertEquals("2.0", response.get("jsonrpc").asText());
        assertNotNull(response.get("result"));
        
        ObjectNode result = (ObjectNode) response.get("result");
        assertNotNull(result.get("content"));
        
        // Verify content structure
        var content = result.get("content");
        assertTrue(content.toString().contains("items"), "Response should contain items data");
    }

    @Test
    void testErrorHandling() throws Exception {
        Map<String, Object> arguments = Map.of("invalid", "parameter");
        ObjectNode response = clientAdapter.callTool("openhab.items.list", arguments);
        
        assertEquals("2.0", response.get("jsonrpc").asText());
        assertNotNull(response.get("error"));
        
        ObjectNode error = (ObjectNode) response.get("error");
        assertTrue(error.get("code").asInt() > 0, "Should have error code");
        assertNotNull(error.get("message"), "Should have error message");
    }

    @Test
    void testConcurrentExecution() throws Exception {
        CompletableFuture<ObjectNode> future1 = CompletableFuture.supplyAsync(() -> {
            try {
                return clientAdapter.callTool("openhab.items.list", Map.of("filter", "all"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        
        CompletableFuture<ObjectNode> future2 = CompletableFuture.supplyAsync(() -> {
            try {
                return clientAdapter.callTool("openhab.persistence.manage", Map.of("action", "status"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        
        ObjectNode response1 = future1.get(30, TimeUnit.SECONDS);
        ObjectNode response2 = future2.get(30, TimeUnit.SECONDS);
        
        assertNotNull(response1.get("result"));
        assertNotNull(response2.get("result"));
    }

    @Test
    void testStreamingExecution() throws Exception {
        Map<String, Object> arguments = Map.of("action", "backup");
        var streamResponse = clientAdapter.callToolStreaming("openhab.persistence.manage", arguments);
        
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
}
```

### 2.2 MCP Client Adapter Interface

```java
package org.openhab.core.ai.mcp.integration.framework;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.node.ObjectNode;

public interface MCPClientAdapter {
    
    void connect() throws Exception;
    
    void disconnect() throws Exception;
    
    ObjectNode initialize() throws Exception;
    
    ObjectNode listTools() throws Exception;
    
    ObjectNode callTool(String toolName, Map<String, Object> arguments) throws Exception;
    
    List<ObjectNode> callToolStreaming(String toolName, Map<String, Object> arguments) throws Exception;
    
    boolean isConnected();
}
```

### 2.3 STDIO Client Adapter Implementation

```java
package org.openhab.core.ai.mcp.integration.framework;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class StdioMCPClientAdapter implements MCPClientAdapter {
    
    private Process process;
    private BufferedWriter writer;
    private BufferedReader reader;
    private ObjectMapper objectMapper;
    private int requestId = 1;
    
    public StdioMCPClientAdapter(String command, String... args) {
        this.objectMapper = new ObjectMapper();
        
        ProcessBuilder processBuilder = new ProcessBuilder(command, args);
        processBuilder.redirectErrorStream(true);
        
        try {
            this.process = processBuilder.start();
            this.writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            this.reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to start MCP client process", e);
        }
    }
    
    @Override
    public void connect() throws Exception {
        // Wait for client to be ready
        waitForClientReady();
    }
    
    @Override
    public void disconnect() throws Exception {
        if (process != null) {
            process.destroy();
        }
    }
    
    @Override
    public ObjectNode initialize() throws Exception {
        ObjectNode request = objectMapper.createObjectNode();
        request.put("jsonrpc", "2.0");
        request.put("id", requestId++);
        request.put("method", "initialize");
        
        ObjectNode params = objectMapper.createObjectNode();
        params.put("protocolVersion", "2024-11-05");
        params.put("capabilities", objectMapper.createObjectNode());
        params.put("clientInfo", objectMapper.createObjectNode()
            .put("name", "test-client")
            .put("version", "1.0.0"));
        
        request.set("params", params);
        
        return sendRequest(request);
    }
    
    @Override
    public ObjectNode listTools() throws Exception {
        ObjectNode request = objectMapper.createObjectNode();
        request.put("jsonrpc", "2.0");
        request.put("id", requestId++);
        request.put("method", "tools/list");
        request.set("params", objectMapper.createObjectNode());
        
        return sendRequest(request);
    }
    
    @Override
    public ObjectNode callTool(String toolName, Map<String, Object> arguments) throws Exception {
        ObjectNode request = objectMapper.createObjectNode();
        request.put("jsonrpc", "2.0");
        request.put("id", requestId++);
        request.put("method", "tools/call");
        
        ObjectNode params = objectMapper.createObjectNode();
        params.put("name", toolName);
        params.set("arguments", objectMapper.valueToTree(arguments));
        
        request.set("params", params);
        
        return sendRequest(request);
    }
    
    @Override
    public List<ObjectNode> callToolStreaming(String toolName, Map<String, Object> arguments) throws Exception {
        ObjectNode request = objectMapper.createObjectNode();
        request.put("jsonrpc", "2.0");
        request.put("id", requestId++);
        request.put("method", "tools/call");
        
        ObjectNode params = objectMapper.createObjectNode();
        params.put("name", toolName);
        params.put("stream", true);
        params.set("arguments", objectMapper.valueToTree(arguments));
        
        request.set("params", params);
        
        return sendStreamingRequest(request);
    }
    
    @Override
    public boolean isConnected() {
        return process != null && process.isAlive();
    }
    
    private ObjectNode sendRequest(ObjectNode request) throws Exception {
        String json = objectMapper.writeValueAsString(request);
        writer.write(json + "\n");
        writer.flush();
        
        return readResponse();
    }
    
    private List<ObjectNode> sendStreamingRequest(ObjectNode request) throws Exception {
        String json = objectMapper.writeValueAsString(request);
        writer.write(json + "\n");
        writer.flush();
        
        return readStreamingResponse();
    }
    
    private ObjectNode readResponse() throws Exception {
        String line = reader.readLine();
        if (line == null) {
            throw new IOException("No response from MCP client");
        }
        return objectMapper.readValue(line, ObjectNode.class);
    }
    
    private List<ObjectNode> readStreamingResponse() throws Exception {
        List<ObjectNode> events = new ArrayList<>();
        
        while (true) {
            String line = reader.readLine();
            if (line == null) break;
            
            ObjectNode event = objectMapper.readValue(line, ObjectNode.class);
            events.add(event);
            
            // Check if this is the final event
            if (event.has("isError") && !event.get("isError").asBoolean()) {
                break;
            }
        }
        
        return events;
    }
    
    private void waitForClientReady() throws Exception {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < 10000) {
            if (reader.ready()) {
                String line = reader.readLine();
                if (line != null && line.contains("jsonrpc")) {
                    return; // Client is ready
                }
            }
            Thread.sleep(100);
        }
        throw new IOException("MCP client did not become ready within timeout");
    }
}
```

## 3. CI/CD Integration

### 3.1 GitHub Actions Workflow

```yaml
name: MCP Client Integration Tests

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  mcp-client-tests:
    runs-on: ubuntu-latest
    
    strategy:
      matrix:
        client: [claude, gpt4, local]
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Set up MCP Client
      run: |
        case ${{ matrix.client }} in
          claude)
            # Install Claude Desktop or CLI
            curl -L https://github.com/anthropics/anthropic-sdk-java/releases/latest/download/claude-cli.jar -o claude-cli.jar
            ;;
          gpt4)
            # Install OpenAI CLI or similar
            curl -L https://github.com/openai/openai-cli/releases/latest/download/openai-cli -o openai-cli
            chmod +x openai-cli
            ;;
          local)
            # Build local MCP server
            mvn clean install -DskipTests
            ;;
        esac
    
    - name: Start MCP Server
      run: |
        # Start the openHAB MCP server
        java -jar target/org.openhab.core.ai.mcp-*.jar &
        sleep 10
    
    - name: Run MCP Client Tests
      run: |
        mvn test -Dtest=MCPClientIntegrationTest -Dmcp.client=${{ matrix.client }}
    
    - name: Upload Test Results
      uses: actions/upload-artifact@v3
      if: always()
      with:
        name: mcp-test-results-${{ matrix.client }}
        path: target/surefire-reports/
```

### 3.2 Docker-based Testing

```dockerfile
# Dockerfile for MCP Client Testing
FROM openjdk:17-jdk-slim

# Install MCP clients
RUN apt-get update && apt-get install -y \
    curl \
    wget \
    && rm -rf /var/lib/apt/lists/*

# Install Claude CLI
RUN curl -L https://github.com/anthropics/anthropic-sdk-java/releases/latest/download/claude-cli.jar -o /usr/local/bin/claude-cli.jar

# Install OpenAI CLI
RUN curl -L https://github.com/openai/openai-cli/releases/latest/download/openai-cli -o /usr/local/bin/openai-cli \
    && chmod +x /usr/local/bin/openai-cli

# Copy test application
COPY target/org.openhab.core.ai.mcp-*.jar /app/mcp-server.jar
COPY src/test/resources/mcp-test-config.properties /app/config.properties

# Expose MCP server port
EXPOSE 8080

# Start MCP server and run tests
CMD ["java", "-jar", "/app/mcp-server.jar", "--config", "/app/config.properties"]
```

## 4. Test Configuration Management

### 4.1 MCP Client Configuration

```properties
# mcp-test-config.properties

# MCP Server Configuration
mcp.server.host=localhost
mcp.server.port=8080
mcp.server.transport.type=STDIO

# Client Configuration
mcp.client.type=claude
mcp.client.command=claude
mcp.client.args=--mcp-server,openhab-mcp-server

# Test Configuration
mcp.test.timeout=30000
mcp.test.retry.count=3
mcp.test.concurrent.requests=5

# Authentication (if required)
mcp.client.api.key=${ANTHROPIC_API_KEY}
mcp.client.api.base.url=https://api.anthropic.com
```

### 4.2 Environment-specific Configuration

```java
package org.openhab.core.ai.mcp.integration.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class MCPTestConfiguration {
    
    private static Properties config;
    
    static {
        config = new Properties();
        try (InputStream input = MCPTestConfiguration.class.getClassLoader()
                .getResourceAsStream("mcp-test-config.properties")) {
            if (input != null) {
                config.load(input);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load MCP test configuration", e);
        }
    }
    
    public static String get(String key) {
        return config.getProperty(key);
    }
    
    public static String get(String key, String defaultValue) {
        return config.getProperty(key, defaultValue);
    }
    
    public static int getInt(String key, int defaultValue) {
        String value = config.getProperty(key);
        return value != null ? Integer.parseInt(value) : defaultValue;
    }
    
    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = config.getProperty(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }
}
```

## 5. Best Practices for MCP Client Automation

### 5.1 Test Reliability
- **Use timeouts**: Always set appropriate timeouts for client operations
- **Retry logic**: Implement retry mechanisms for flaky operations
- **Cleanup**: Ensure proper cleanup of client connections and resources
- **Isolation**: Each test should be independent and not affect others

### 5.2 Performance Considerations
- **Connection pooling**: Reuse client connections when possible
- **Concurrent testing**: Test multiple concurrent client connections
- **Resource monitoring**: Monitor memory and CPU usage during tests
- **Load testing**: Test with realistic load scenarios

### 5.3 Error Handling
- **Graceful degradation**: Handle client failures gracefully
- **Error logging**: Log detailed error information for debugging
- **Fallback mechanisms**: Implement fallback strategies for failed clients
- **Health checks**: Regular health checks for client connections

### 5.4 Security
- **API key management**: Secure handling of API keys and credentials
- **Network security**: Use secure connections (HTTPS, WSS) when possible
- **Input validation**: Validate all inputs to prevent injection attacks
- **Rate limiting**: Respect API rate limits and implement backoff strategies

This comprehensive guide provides practical approaches for automating tests with real MCP clients, ensuring reliable and maintainable test suites that can validate the openHAB MCP server against actual client implementations. 