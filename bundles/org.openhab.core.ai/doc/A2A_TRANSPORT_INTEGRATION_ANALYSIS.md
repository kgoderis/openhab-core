# A2A Transport Integration Analysis

## Overview

This document analyzes how the missing gRPC and REST transports can be integrated into the existing openHAB A2A classes to achieve 100% A2A protocol specification compliance.

## Current Architecture Analysis

### **Existing Transport Implementation**

The current implementation uses a **single-transport architecture** with JSON-RPC 2.0:

```java
// Current AgentProtocolHandler - Single Transport
@Component(service = AgentProtocolHandler.class)
public class AgentProtocolHandler implements RequestHandler {
    // Direct JSON-RPC 2.0 implementation via A2A SDK
    // No transport abstraction layer
}
```

### **Current AgentCard Configuration**

```java
// Current AgentCardBuilder - Single Transport Declaration
public class AgentCardBuilder {
    public AgentCard buildAgentCard() {
        return AgentCard.builder()
            .name("openHAB AI Agent")
            .description("openHAB AI Agent with A2A protocol support")
            .transport("http")  // Only HTTP transport declared
            .endpoint("http://localhost:8080/a2a")
            .build();
    }
}
```

## Integration Strategy

### **1. Transport Factory Pattern**

**Proposed Architecture:**
```java
// Transport Abstraction Interface
public interface A2ATransport {
    void start();
    void stop();
    boolean isRunning();
    String getTransportType();
    AgentCard getTransportCapabilities();
}

// Transport Factory
@Component(service = A2ATransportFactory.class)
public class A2ATransportFactory {
    private final Map<String, A2ATransport> transports = new ConcurrentHashMap<>();
    
    public A2ATransport getTransport(String type) {
        return transports.get(type);
    }
    
    public void registerTransport(String type, A2ATransport transport) {
        transports.put(type, transport);
    }
}
```

### **2. Enhanced AgentProtocolHandler**

**Updated Architecture:**
```java
@Component(service = AgentProtocolHandler.class)
public class AgentProtocolHandler implements RequestHandler {
    
    @Reference
    private A2ATransportFactory transportFactory;
    
    private A2ATransport activeTransport;
    
    @Activate
    public void activate() {
        // Default to JSON-RPC 2.0, but can switch dynamically
        this.activeTransport = transportFactory.getTransport("json-rpc");
    }
    
    // All existing methods remain unchanged
    // Transport abstraction is transparent to protocol logic
}
```

### **3. Multi-Transport AgentCard**

**Enhanced AgentCard:**
```java
public class AgentCardBuilder {
    public AgentCard buildAgentCard() {
        return AgentCard.builder()
            .name("openHAB AI Agent")
            .description("openHAB AI Agent with multi-transport A2A support")
            .transports(Arrays.asList(
                TransportCapability.builder()
                    .type("json-rpc")
                    .endpoint("http://localhost:8080/a2a/json-rpc")
                    .priority(1)
                    .build(),
                TransportCapability.builder()
                    .type("grpc")
                    .endpoint("http://localhost:8081/a2a/grpc")
                    .priority(2)
                    .build(),
                TransportCapability.builder()
                    .type("rest")
                    .endpoint("http://localhost:8082/a2a/rest")
                    .priority(3)
                    .build()
            ))
            .build();
    }
}
```

## **Potential Interference Analysis**

### **1. MCP Transport Interference**

**Current MCP Transport Configuration:**
- **Port 8080**: MCP SSE transport (`/mcp/message`, `/mcp/events`)
- **STDIO**: MCP default transport (no port conflict)
- **Base URL**: `http://localhost:8080`

**Potential Conflicts:**
- **Port 8080**: MCP already uses this for SSE transport
- **HTTP Endpoints**: MCP uses `/mcp/*` paths, A2A would use `/a2a/*` paths

**Resolution Strategy:**
```java
// A2A Transport Configuration - Avoid Port Conflicts
@Component(service = A2ATransportConfiguration.class)
public class A2ATransportConfiguration {
    
    // Use different ports for A2A transports
    private static final int A2A_JSON_RPC_PORT = 8080;  // Share with MCP (different paths)
    private static final int A2A_GRPC_PORT = 8081;      // Dedicated port (stub framework uses this)
    private static final int A2A_REST_PORT = 8082;      // Dedicated port
    
    // Use different URL paths to avoid conflicts
    private static final String A2A_BASE_PATH = "/a2a";
    private static final String MCP_BASE_PATH = "/mcp";  // Existing MCP paths
}
```

**No Interference Expected:**
- ✅ **Different URL Paths**: MCP uses `/mcp/*`, A2A uses `/a2a/*`
- ✅ **Different Protocols**: MCP uses SSE, A2A uses JSON-RPC/gRPC/REST
- ✅ **Port Sharing**: A2A JSON-RPC can share port 8080 with MCP (different paths)
- ✅ **Independent Lifecycles**: MCP and A2A can start/stop independently

### **2. openHAB Service Interference**

**Current openHAB Port Usage (from configuration files):**
- **Port 8080**: openHAB main web interface + MCP SSE transport
- **Port 8081**: Stub framework WebSocket (ai-common.cfg)
- **Port 1883**: Stub framework MQTT (ai-common.cfg)
- **Port 5683**: Stub framework CoAP (ai-common.cfg)

**Port Conflict Analysis:**
- ❌ **Port 8081**: CONFLICT - Stub framework uses this for WebSocket
- ✅ **Port 8082**: AVAILABLE - No conflicts detected
- ✅ **Port 8083+**: AVAILABLE - Safe range for A2A transports

### **3. openHAB REST API Integration**

**openHAB REST API Analysis:**
Based on standard openHAB REST API patterns, openHAB typically uses:
- **Base Path**: `/rest/` for REST API endpoints
- **Common Endpoints**: `/rest/items`, `/rest/things`, `/rest/rules`, etc.
- **Authentication**: Standard openHAB authentication mechanisms
- **Content-Type**: `application/json` for requests/responses

**A2A REST Integration Strategy:**
```java
// A2A REST Transport with openHAB REST Integration
@Component(service = AgentRestTransport.class)
public class AgentRestTransport implements AgentTransport {
    
    private final io.a2a.spec.RequestHandler requestHandler; // A2A SDK
    private final HttpServer restServer;
    
    // openHAB REST API integration
    @Reference
    private ItemRegistry itemRegistry;
    
    @Reference
    private ThingRegistry thingRegistry;
    
    @Reference
    private RuleRegistry ruleRegistry;
    
    @Override
    public void start() {
        // Start REST server with A2A endpoints
        restServer = HttpServer.create(new InetSocketAddress(8082), 0);
        
        // A2A-specific endpoints (separate from openHAB REST)
        registerA2AEndpoints(restServer);
        
        // Integration with openHAB REST (optional)
        registerOpenHABIntegrationEndpoints(restServer);
        
        restServer.start();
    }
    
    private void registerA2AEndpoints(HttpServer server) {
        // A2A protocol endpoints (separate from openHAB REST)
        server.createContext("/a2a/v1/message:send", this::handleMessageSend);
        server.createContext("/a2a/v1/tasks/", this::handleTaskOperations);
        server.createContext("/a2a/v1/card", this::handleAgentCard);
        server.createContext("/a2a/v1/events/", this::handleStreamingEvents);
    }
    
    private void registerOpenHABIntegrationEndpoints(HttpServer server) {
        // Optional: A2A endpoints that integrate with openHAB REST
        server.createContext("/a2a/v1/openhab/items", this::handleOpenHABItems);
        server.createContext("/a2a/v1/openhab/things", this::handleOpenHABThings);
        server.createContext("/a2a/v1/openhab/rules", this::handleOpenHABRules);
    }
    
    private void handleOpenHABItems(HttpExchange exchange) throws IOException {
        // Integrate with openHAB REST API patterns
        if ("GET".equals(exchange.getRequestMethod())) {
            // Use openHAB ItemRegistry directly
            List<Item> items = itemRegistry.getAll();
            String response = convertToA2AFormat(items);
            sendResponse(exchange, 200, response);
        }
    }
}
```

**REST Integration Benefits:**
- ✅ **Path Separation**: A2A uses `/a2a/*`, openHAB uses `/rest/*`
- ✅ **Direct Integration**: A2A can access openHAB services directly
- ✅ **Consistent Patterns**: Follow openHAB REST API patterns
- ✅ **Authentication**: Use openHAB authentication mechanisms
- ✅ **No Conflicts**: Separate endpoints, no interference

**Resolution Strategy:**
```java
// A2A Transport Port Configuration
@Component(service = AgentTransportPortManager.class)
public class AgentTransportPortManager {
    
    @Reference
    private ConfigurationService configService;
    
    public int getAvailablePort(String transportType) {
        // Check openHAB configuration for port conflicts
        int openhabPort = configService.getProperty("org.openhab.http.port", 8080);
        int stubWebSocketPort = configService.getProperty("ai.common.stub.ports.websocket", 8081);
        
        switch (transportType) {
            case "json-rpc":
                return openhabPort; // Share port with openHAB/MCP (different paths)
            case "grpc":
                return findAvailablePort(8083, 8090); // Avoid stub framework port 8081
            case "rest":
                return findAvailablePort(8082, 8090); // Use available port 8082
            default:
                throw new IllegalArgumentException("Unknown transport: " + transportType);
        }
    }
}
```

**Updated Port Assignment:**
- ✅ **A2A JSON-RPC**: Port 8080 (shared with MCP/openHAB, different paths)
- ✅ **A2A gRPC**: Port 8083 (avoiding stub framework port 8081)
- ✅ **A2A REST**: Port 8082 (available port, integrates with openHAB REST)
- ✅ **Path Separation**: A2A uses `/a2a/*` paths, openHAB uses `/rest/*`
- ✅ **Service Independence**: A2A transports are independent OSGi services

## **SDK Classes Reuse Analysis**

### **1. A2A SDK Classes - Origin and Availability**

**A2A SDK Classes Analysis:**
Based on the existing implementation and documentation, the A2A classes mentioned in the analysis are **NOT from the A2A SDK** but are **custom implementations** in the openHAB AI bundle:

**❌ NOT from A2A SDK (Custom Implementations):**
```java
// These are custom openHAB implementations, NOT from A2A SDK
import org.openhab.core.ai.agent.communication.protocol.AgentProtocolHandler;
import org.openhab.core.ai.agent.delegation.AgentCardBuilder;
import org.openhab.core.ai.agent.execution.AgentTaskManager;
import org.openhab.core.ai.agent.communication.AgentStreamingManager;
```

**✅ ACTUAL A2A SDK Classes (Available for Reuse):**
```java
// These are the real A2A SDK classes that can be reused
import io.a2a.spec.RequestHandler;           // Main protocol interface
import io.a2a.spec.Task;                     // Task data structure
import io.a2a.spec.TaskStatus;               // Task status enumeration
import io.a2a.spec.AgentCard;                // Agent capability description
import io.a2a.spec.JSONRPCError;             // Error handling
import io.a2a.spec.MessageSendParams;        // Message parameters
import io.a2a.spec.TaskQueryParams;          // Task query parameters
import io.a2a.spec.TaskIdParams;             // Task ID parameters
import io.a2a.spec.StreamingEventKind;       // Streaming events
import io.a2a.spec.TaskPushNotificationConfig; // Push notifications
```

**Transport-Specific SDK Classes:**
```java
// These may not exist in A2A SDK - need verification
import io.a2a.transport.TransportProvider;   // Transport abstraction (if exists)
import io.a2a.transport.TransportFactory;    // Transport factory (if exists)
import io.a2a.transport.TransportConfig;     // Transport configuration (if exists)
```

### **2. MCP SDK Classes Available for Reuse**

**MCP Transport Classes:**
```java
// ✅ REUSABLE: MCP Transport Infrastructure
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.server.transport.HttpServletSseServerTransportProvider;
import io.modelcontextprotocol.server.transport.McpServerTransportProvider;
import io.modelcontextprotocol.server.transport.McpTransport;
```

**MCP Configuration Classes:**
```java
// ✅ REUSABLE: MCP Configuration Patterns
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
```

### **3. MCP Pattern Consistency for A2A Implementation**

**MCP Transport Pattern Analysis:**
The MCP bundle uses a **consistent transport pattern** that can be applied to A2A:

**MCP Transport Pattern:**
```java
// MCP Transport Provider Pattern
public interface McpServerTransportProvider {
    McpTransport createTransport(McpServer server, Map<String, Object> options);
    String getTransportType();
    boolean isSupported();
}

// MCP Transport Implementation
public interface McpTransport {
    void start();
    void stop();
    boolean isRunning();
    void close();
}
```

**A2A Transport Pattern (Following MCP Consistency):**
```java
// A2A Transport Provider Pattern (Following MCP Pattern)
public interface A2ATransportProvider {
    A2ATransport createTransport(A2AServer server, Map<String, Object> options);
    String getTransportType();
    boolean isSupported();
}

// A2A Transport Implementation (Following MCP Pattern)
public interface A2ATransport {
    void start();
    void stop();
    boolean isRunning();
    void close();
    AgentCard getTransportCapabilities();
}
```

**Consistency Benefits:**
- ✅ **Same Interface Pattern**: Both MCP and A2A use provider/transport pattern
- ✅ **Same Lifecycle Methods**: `start()`, `stop()`, `isRunning()`, `close()`
- ✅ **Same Configuration Approach**: Map-based options
- ✅ **Same Factory Pattern**: Provider creates transport instances
- ✅ **Same Error Handling**: Consistent exception patterns

### **3. Reuse Strategy for Transport Implementation**

#### **A. Transport Provider Pattern Reuse**

```java
// Reuse MCP transport provider pattern for A2A
public interface A2ATransportProvider {
    A2ATransport createTransport(TransportConfig config);
    String getTransportType();
    boolean isSupported();
}

// Implement using MCP patterns
@Component(service = A2AJsonRpcTransportProvider.class)
public class A2AJsonRpcTransportProvider implements A2ATransportProvider {
    
    // Reuse MCP transport patterns
    private final HttpServletSseServerTransportProvider mcpTransportProvider;
    
    @Override
    public A2ATransport createTransport(TransportConfig config) {
        // Adapt MCP transport for A2A use
        return new A2AJsonRpcTransport(mcpTransportProvider, config);
    }
}
```

#### **B. Configuration Pattern Reuse**

```java
// Reuse MCP configuration patterns
@Component(service = A2ATransportConfiguration.class)
public class A2ATransportConfiguration {
    
    // Reuse MCP configuration structure
    private final MCPServerConfiguration mcpConfig;
    
    public A2ATransportConfig createA2AConfig(String transportType) {
        return A2ATransportConfig.builder()
            .transportType(transportType)
            .baseUrl(mcpConfig.getBaseUrl())
            .port(getA2APort(transportType))
            .path("/a2a/" + transportType)
            .build();
    }
}
```

#### **C. Server Lifecycle Pattern Reuse**

```java
// Reuse MCP server lifecycle patterns
@Component(service = A2ATransportManager.class)
public class A2ATransportManager {
    
    private final Map<String, A2ATransport> transports = new ConcurrentHashMap<>();
    
    // Reuse MCP server lifecycle methods
    public void startTransport(String type) {
        A2ATransport transport = transports.get(type);
        if (transport != null) {
            transport.start();
        }
    }
    
    public void stopTransport(String type) {
        A2ATransport transport = transports.get(type);
        if (transport != null) {
            transport.stop();
        }
    }
}
```

### **4. Implementation Classes to Create**

#### **A. New A2A Transport Classes (Using A2A SDK + MCP Pattern)**

```java
// A2A Transport using A2A SDK classes + MCP pattern consistency
public class A2AGrpcTransport implements A2ATransport {
    private final io.a2a.spec.RequestHandler requestHandler; // A2A SDK
    private final Server grpcServer; // gRPC server
    
    public A2AGrpcTransport(io.a2a.spec.RequestHandler handler) {
        this.requestHandler = handler; // Use A2A SDK RequestHandler
    }
    
    @Override
    public void start() {
        // Start gRPC server using A2A SDK patterns
    }
    
    @Override
    public void stop() {
        // Stop gRPC server
    }
    
    @Override
    public boolean isRunning() {
        return grpcServer != null && !grpcServer.isShutdown();
    }
    
    @Override
    public void close() {
        stop();
    }
    
    @Override
    public io.a2a.spec.AgentCard getTransportCapabilities() {
        // Return A2A SDK AgentCard with gRPC capabilities
        return io.a2a.spec.AgentCard.builder()
            .transport("grpc")
            .endpoint("grpc://localhost:8083/a2a")
            .build();
    }
}

public class A2ARestTransport implements A2ATransport {
    private final io.a2a.spec.RequestHandler requestHandler; // A2A SDK
    private final HttpServer restServer; // REST server
    
    public A2ARestTransport(io.a2a.spec.RequestHandler handler) {
        this.requestHandler = handler; // Use A2A SDK RequestHandler
    }
    
    // Similar implementation following MCP pattern
}

public class A2ATransportFactory {
    private final Map<String, A2ATransportProvider> providers = new ConcurrentHashMap<>();
    
    public A2ATransport createTransport(String type, io.a2a.spec.RequestHandler handler) {
        A2ATransportProvider provider = providers.get(type);
        if (provider != null) {
            return provider.createTransport(handler, Map.of());
        }
        throw new IllegalArgumentException("Unknown transport type: " + type);
    }
}
```

#### **B. A2A SDK Integration with MCP Pattern**

```java
// A2A Transport Provider using A2A SDK classes
@Component(service = A2AGrpcTransportProvider.class)
public class A2AGrpcTransportProvider implements A2ATransportProvider {
    
    @Override
    public A2ATransport createTransport(io.a2a.spec.RequestHandler handler, Map<String, Object> options) {
        return new A2AGrpcTransport(handler); // Pass A2A SDK RequestHandler
    }
    
    @Override
    public String getTransportType() {
        return "grpc";
    }
    
    @Override
    public boolean isSupported() {
        return true; // gRPC is supported
    }
}

// A2A Server using A2A SDK classes
public class A2AServer {
    private final io.a2a.spec.RequestHandler requestHandler; // A2A SDK
    private final Map<String, A2ATransport> transports = new ConcurrentHashMap<>();
    
    public A2AServer(io.a2a.spec.RequestHandler handler) {
        this.requestHandler = handler; // Use A2A SDK RequestHandler
    }
    
    public void addTransport(String type, A2ATransport transport) {
        transports.put(type, transport);
    }
    
    public io.a2a.spec.AgentCard getAgentCard() {
        // Return A2A SDK AgentCard with all transport capabilities
        return io.a2a.spec.AgentCard.builder()
            .name("openHAB AI Agent")
            .description("Multi-transport A2A agent")
            .transports(transports.values().stream()
                .map(A2ATransport::getTransportCapabilities)
                .collect(Collectors.toList()))
            .build();
    }
}
```

#### **B. Enhanced AgentCard Classes**

```java
// Enhanced AgentCard for multi-transport support
public class MultiTransportAgentCard extends AgentCard {
    private final List<TransportCapability> transports;
    
    public MultiTransportAgentCard(String name, String description, 
                                  List<TransportCapability> transports) {
        super(name, description);
        this.transports = transports;
    }
}

public class TransportCapability {
    private final String type;
    private final String endpoint;
    private final int priority;
    private final Map<String, Object> capabilities;
}
```

## **SSE Conflict Resolution**

### **SSE Transport Conflict Analysis**

**Current SSE Usage:**
- **MCP SSE**: Uses port 8080 with paths `/mcp/message` and `/mcp/events`
- **A2A Streaming**: Needs SSE for real-time task updates and streaming events

**Conflict Resolution Strategy:**

#### **A. Shared SSE Infrastructure**
```java
// Shared SSE infrastructure for both MCP and A2A
@Component(service = SharedSseManager.class)
public class SharedSseManager {
    
    private final Map<String, SseEmitter> mcpEmitters = new ConcurrentHashMap<>();
    private final Map<String, SseEmitter> a2aEmitters = new ConcurrentHashMap<>();
    
    // MCP SSE endpoints
    @GetMapping("/mcp/events/{subscriptionId}")
    public SseEmitter getMcpEvents(@PathVariable String subscriptionId) {
        SseEmitter emitter = new SseEmitter();
        mcpEmitters.put(subscriptionId, emitter);
        return emitter;
    }
    
    // A2A SSE endpoints
    @GetMapping("/a2a/events/{taskId}")
    public SseEmitter getA2AEvents(@PathVariable String taskId) {
        SseEmitter emitter = new SseEmitter();
        a2aEmitters.put(taskId, emitter);
        return emitter;
    }
    
    // Send events to appropriate clients
    public void sendMcpEvent(String subscriptionId, Object event) {
        SseEmitter emitter = mcpEmitters.get(subscriptionId);
        if (emitter != null) {
            try {
                emitter.send(event);
            } catch (IOException e) {
                mcpEmitters.remove(subscriptionId);
            }
        }
    }
    
    public void sendA2AEvent(String taskId, io.a2a.spec.StreamingEventKind event) {
        SseEmitter emitter = a2aEmitters.get(taskId);
        if (emitter != null) {
            try {
                emitter.send(event);
            } catch (IOException e) {
                a2aEmitters.remove(taskId);
            }
        }
    }
}
```

#### **B. A2A Streaming Integration**
```java
// A2A Streaming using shared SSE infrastructure
@Component(service = A2AStreamingManager.class)
public class A2AStreamingManager {
    
    @Reference
    private SharedSseManager sseManager;
    
    public void publishTaskUpdate(String taskId, io.a2a.spec.TaskStatus status) {
        // Create A2A SDK streaming event
        io.a2a.spec.StreamingEventKind event = new io.a2a.spec.StreamingEventKind(
            taskId, status, System.currentTimeMillis()
        );
        
        // Send via shared SSE infrastructure
        sseManager.sendA2AEvent(taskId, event);
    }
    
    public void publishTaskArtifact(String taskId, Object artifact) {
        // Create A2A SDK artifact event
        io.a2a.spec.StreamingEventKind event = new io.a2a.spec.StreamingEventKind(
            taskId, artifact, System.currentTimeMillis()
        );
        
        // Send via shared SSE infrastructure
        sseManager.sendA2AEvent(taskId, event);
    }
}
```

**SSE Conflict Resolution Benefits:**
- ✅ **Shared Infrastructure**: Both MCP and A2A use same SSE server
- ✅ **Path Separation**: `/mcp/events/*` vs `/a2a/events/*`
- ✅ **Resource Efficiency**: Single SSE server instance
- ✅ **Consistent Patterns**: Same SSE implementation for both protocols
- ✅ **No Port Conflicts**: Both use port 8080 with different paths

## **Integration Benefits**

### **1. Zero Interference with Existing Systems**
- ✅ **MCP Compatibility**: No conflicts with existing MCP transport
- ✅ **openHAB Compatibility**: No conflicts with openHAB services
- ✅ **Port Management**: Automatic port conflict detection and resolution
- ✅ **Path Separation**: Clear separation of URL paths
- ✅ **SSE Sharing**: Shared SSE infrastructure eliminates conflicts

### **2. Maximum SDK Reuse**
- ✅ **A2A SDK**: Reuse all core protocol classes and interfaces
- ✅ **MCP SDK**: Reuse transport patterns and configuration structures
- ✅ **openHAB SDK**: Reuse service management patterns

### **3. Clean Architecture**
- ✅ **Single Responsibility**: Each transport handles its own protocol
- ✅ **Open/Closed Principle**: Easy to add new transports without modifying existing code
- ✅ **Dependency Inversion**: Transports depend on abstractions, not concretions

## **Implementation Timeline**

### **Phase 1: Transport Abstraction Layer (Week 1)**
- [ ] Create `A2ATransport` interface
- [ ] Create `A2ATransportFactory` class
- [ ] Refactor `AgentProtocolHandler` to use transport abstraction
- [ ] Update `AgentCardBuilder` for multi-transport support

### **Phase 2: gRPC Transport Implementation (Week 2)**
- [ ] Create `A2AGrpcTransport` class
- [ ] Implement Protocol Buffers definitions
- [ ] Add gRPC-specific error handling
- [ ] Implement bidirectional streaming

### **Phase 3: REST Transport Implementation (Week 3)**
- [ ] Create `A2ARestTransport` class
- [ ] Implement HTTP+JSON endpoints
- [ ] Add Server-Sent Events support
- [ ] Implement REST-specific error handling

### **Phase 4: Integration and Testing (Week 4)**
- [ ] Implement transport selection and fallback
- [ ] Add comprehensive testing suite
- [ ] Performance optimization
- [ ] Documentation and examples

## **Conclusion**

The transport abstraction layer will **NOT interfere** with MCP or openHAB systems due to:
- **Path separation** (`/mcp/*` vs `/a2a/*`)
- **Port management** (dedicated ports for new transports)
- **Protocol independence** (different transport protocols)

**SDK Reuse Opportunities:**
- **A2A SDK**: 100% reuse of core protocol classes
- **MCP SDK**: Reuse transport patterns and configuration
- **openHAB SDK**: Reuse service management patterns

The implementation maintains the **Single Responsibility Principle** while adding **multi-transport capabilities** through a well-defined abstraction layer.
