# MCP Bundle Usage Examples

This document provides practical examples of how to use the openHAB MCP bundle in various scenarios.

## Basic Server Setup

### Simple STDIO Server

```java
import org.openhab.core.ai.mcp.internal.*;

// Create basic configuration
MCPServerConfiguration config = MCPServerConfiguration.builder()
    .serverId("simple-mcp-server")
    .serverName("Simple MCP Server")
    .serverVersion("1.0.0")
    .transportType(MCPTransportType.STDIO)
    .enableTools(true)
    .build();

// Create tool registry
MCPToolRegistry toolRegistry = new MCPToolRegistry();

// Register basic tools
toolRegistry.registerTool(new ListItemsTool());
toolRegistry.registerTool(new ListThingsTool());

// Create and start server
MCPServerInstance server = new MCPServerInstance("server-1", config, toolRegistry);
server.start();

// Check server status
System.out.println("Server running: " + server.isRunning());
System.out.println("Server healthy: " + server.isHealthy());

// Stop server when done
server.stop();
```

### SSE Server with Custom Configuration

```java
// Create SSE configuration
MCPServerConfiguration config = MCPServerConfiguration.builder()
    .serverId("sse-mcp-server")
    .serverName("SSE MCP Server")
    .serverVersion("1.0.0")
    .transportType(MCPTransportType.SSE)
    .baseUrl("http://localhost:8080")
    .messageEndpoint("/mcp/message")
    .sseEndpoint("/mcp/events")
    .enableSse(true)
    .enableTools(true)
    .enableResources(true)
    .enablePrompts(true)
    .enableLogging(true)
    .transportOption("timeout", 30000)
    .transportOption("maxConnections", 10)
    .serverOption("debug", true)
    .build();

// Create comprehensive tool registry
MCPToolRegistry toolRegistry = new MCPToolRegistry();

// Register all available tools
toolRegistry.registerTool(new ListItemsTool());
toolRegistry.registerTool(new GetThingTool());
toolRegistry.registerTool(new ListThingsTool());
toolRegistry.registerTool(new ListChannelsTool());
toolRegistry.registerTool(new ChannelLinkTool());
toolRegistry.registerTool(new ListRulesTool());
toolRegistry.registerTool(new ConfigurationBackupTool());
toolRegistry.registerTool(new ConfigurationExportTool());
toolRegistry.registerTool(new ConfigurationGetTool());
toolRegistry.registerTool(new ConfigurationImportTool());
toolRegistry.registerTool(new ConfigurationListTool());
toolRegistry.registerTool(new ConfigurationSetTool());
toolRegistry.registerTool(new ConfigurationValidationTool());
toolRegistry.registerTool(new HealthCheckTool());
toolRegistry.registerTool(new SystemDiagnosticsTool());
toolRegistry.registerTool(new MemoryManagementTool());
toolRegistry.registerTool(new SecurityManagementTool());
toolRegistry.registerTool(new FileSystemManagementTool());
toolRegistry.registerTool(new ScriptExecutionTool());
toolRegistry.registerTool(new ScriptLibraryTool());
toolRegistry.registerTool(new ListScriptsTool());
toolRegistry.registerTool(new PersistenceTool());
toolRegistry.registerTool(new EventManagementTool());
toolRegistry.registerTool(new ListBindingsTool());
toolRegistry.registerTool(new BindingConfigurationTool());
toolRegistry.registerTool(new DiscoveryTool());
toolRegistry.registerTool(new LoggingMonitoringTool());
toolRegistry.registerTool(new DataAnalysisTool());
toolRegistry.registerTool(new AdvancedAutomationTool());

// Create and start server
MCPServerInstance server = new MCPServerInstance("sse-server", config, toolRegistry);
server.start();
```

## Health Monitoring Examples

### Basic Health Check

```java
// Check server health
boolean isHealthy = server.isHealthy();
System.out.println("Server healthy: " + isHealthy);

// Get transport health information
TransportHealthInfo health = server.getTransportHealth();
System.out.println("Transport type: " + health.getTransportType());
System.out.println("Transport healthy: " + health.isHealthy());
System.out.println("Transport uptime: " + health.getUptime() + "ms");
System.out.println("Last error: " + health.getLastError());
```

### Detailed Transport Statistics

```java
// Get comprehensive transport statistics
TransportStatistics stats = server.getTransportStatistics();
System.out.println("Current transport: " + stats.getCurrentType());
System.out.println("Configured transport: " + stats.getConfiguredType());
System.out.println("Using fallback: " + stats.isUsingFallback());
System.out.println("Transport class: " + stats.getTransportClass());
System.out.println("Transport healthy: " + stats.isHealthy());
System.out.println("Transport uptime: " + stats.getUptime() + "ms");
System.out.println("Last error: " + stats.getLastError());

// Format uptime for display
long uptimeSeconds = stats.getUptime() / 1000;
long hours = uptimeSeconds / 3600;
long minutes = (uptimeSeconds % 3600) / 60;
long seconds = uptimeSeconds % 60;
System.out.printf("Uptime: %02d:%02d:%02d%n", hours, minutes, seconds);
```

### Continuous Health Monitoring

```java
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// Create scheduled executor for health monitoring
ScheduledExecutorService healthMonitor = Executors.newScheduledThreadPool(1);

// Schedule health check every 30 seconds
healthMonitor.scheduleAtFixedRate(() -> {
    try {
        TransportHealthInfo health = server.getTransportHealth();
        
        if (!health.isHealthy()) {
            System.err.println("WARNING: Transport unhealthy - " + health.getLastError());
        }
        
        // Log health status every 5 minutes
        if (System.currentTimeMillis() % 300000 < 30000) {
            System.out.println("Health check: " + health);
        }
        
    } catch (Exception e) {
        System.err.println("Health check failed: " + e.getMessage());
    }
}, 0, 30, TimeUnit.SECONDS);

// Shutdown monitor when done
healthMonitor.shutdown();
```

## Error Handling Examples

### Graceful Error Handling

```java
try {
    // Create server with potentially problematic configuration
    MCPServerConfiguration config = MCPServerConfiguration.builder()
        .serverId("error-test-server")
        .transportType(MCPTransportType.SSE)
        .baseUrl("http://invalid-url:9999")  // Invalid URL
        .build();
    
    MCPToolRegistry toolRegistry = new MCPToolRegistry();
    MCPServerInstance server = new MCPServerInstance("error-test", config, toolRegistry);
    
    // Start server - should fallback to STDIO
    server.start();
    
    // Check if fallback occurred
    TransportStatistics stats = server.getTransportStatistics();
    if (stats.isUsingFallback()) {
        System.out.println("Fallback occurred: " + stats.getLastError());
        System.out.println("Now using: " + stats.getCurrentType());
    }
    
} catch (Exception e) {
    System.err.println("Server creation failed: " + e.getMessage());
}
```

### Transport Failure Recovery

```java
// Monitor for transport failures and recovery
ScheduledExecutorService recoveryMonitor = Executors.newScheduledThreadPool(1);

recoveryMonitor.scheduleAtFixedRate(() -> {
    try {
        TransportHealthInfo health = server.getTransportHealth();
        
        if (!health.isHealthy()) {
            System.err.println("Transport failure detected: " + health.getLastError());
            
            // Attempt recovery by restarting server
            System.out.println("Attempting server restart...");
            server.stop();
            Thread.sleep(1000); // Wait 1 second
            server.start();
            
            // Check if recovery was successful
            TransportHealthInfo newHealth = server.getTransportHealth();
            if (newHealth.isHealthy()) {
                System.out.println("Recovery successful!");
            } else {
                System.err.println("Recovery failed: " + newHealth.getLastError());
            }
        }
        
    } catch (Exception e) {
        System.err.println("Recovery attempt failed: " + e.getMessage());
    }
}, 0, 60, TimeUnit.SECONDS); // Check every minute
```

## Configuration Examples

### Environment-Based Configuration

```java
// Load configuration from environment variables
MCPServerConfiguration config = MCPServerConfiguration.builder()
    .serverId(System.getenv().getOrDefault("MCP_SERVER_ID", "default-server"))
    .serverName(System.getenv().getOrDefault("MCP_SERVER_NAME", "openHAB MCP Server"))
    .serverVersion(System.getenv().getOrDefault("MCP_SERVER_VERSION", "1.0.0"))
    .transportType(MCPTransportType.valueOf(
        System.getenv().getOrDefault("MCP_TRANSPORT_TYPE", "STDIO")))
    .baseUrl(System.getenv().getOrDefault("MCP_BASE_URL", "http://localhost:8080"))
    .messageEndpoint(System.getenv().getOrDefault("MCP_MESSAGE_ENDPOINT", "/mcp/message"))
    .sseEndpoint(System.getenv().getOrDefault("MCP_SSE_ENDPOINT", "/mcp/events"))
    .enableSse(Boolean.parseBoolean(System.getenv().getOrDefault("MCP_ENABLE_SSE", "true")))
    .enableTools(Boolean.parseBoolean(System.getenv().getOrDefault("MCP_ENABLE_TOOLS", "true")))
    .enableResources(Boolean.parseBoolean(System.getenv().getOrDefault("MCP_ENABLE_RESOURCES", "true")))
    .enablePrompts(Boolean.parseBoolean(System.getenv().getOrDefault("MCP_ENABLE_PROMPTS", "true")))
    .enableLogging(Boolean.parseBoolean(System.getenv().getOrDefault("MCP_ENABLE_LOGGING", "true")))
    .build();
```

### Production Configuration

```java
// Production-ready configuration with comprehensive settings
MCPServerConfiguration config = MCPServerConfiguration.builder()
    .serverId("openhab-mcp-production")
    .serverName("openHAB MCP Production Server")
    .serverVersion("1.0.0")
    .transportType(MCPTransportType.SSE)
    .baseUrl("https://openhab.example.com")
    .messageEndpoint("/api/mcp/message")
    .sseEndpoint("/api/mcp/events")
    .enableSse(true)
    .enableTools(true)
    .enableResources(true)
    .enablePrompts(true)
    .enableLogging(true)
    .transportOption("timeout", 60000)
    .transportOption("maxConnections", 100)
    .transportOption("connectionTimeout", 30000)
    .transportOption("readTimeout", 30000)
    .transportOption("writeTimeout", 30000)
    .serverOption("debug", false)
    .serverOption("production", true)
    .serverOption("metrics", true)
    .serverOption("healthCheck", true)
    .build();
```

## Tool Usage Examples

### Custom Tool Registration

```java
// Create custom tool
public class CustomItemTool implements MCPTool {
    @Override
    public String getName() {
        return "custom_item_tool";
    }
    
    @Override
    public String getDescription() {
        return "Custom tool for item operations";
    }
    
    @Override
    public McpSchema.ToolSpecification getSpecification() {
        return McpSchema.ToolSpecification.builder()
            .name(getName())
            .withDescription(getDescription())
            .inputSchema(McpSchema.JsonSchema.builder()
                .type("object")
                .properties(Map.of(
                    "itemName", McpSchema.JsonSchema.builder().type("string").build()
                ))
                .required(List.of("itemName"))
                .build())
            .build();
    }
    
    @Override
    public McpSchema.ToolResult execute(McpSchema.ToolCall call) {
        // Implementation here
        return McpSchema.ToolResult.builder()
            .content(List.of(McpSchema.TextContent.builder()
                .text("Custom tool executed successfully")
                .build()))
            .build();
    }
}

// Register custom tool
MCPToolRegistry toolRegistry = new MCPToolRegistry();
toolRegistry.registerTool(new CustomItemTool());
```

### Tool Registry Management

```java
// Create tool registry with specific tools
MCPToolRegistry toolRegistry = new MCPToolRegistry();

// Register tools by category
// Item management
toolRegistry.registerTool(new ListItemsTool());
toolRegistry.registerTool(new GetThingTool());

// Thing management
toolRegistry.registerTool(new ListThingsTool());
toolRegistry.registerTool(new ThingConfigurationTool());

// System management
toolRegistry.registerTool(new HealthCheckTool());
toolRegistry.registerTool(new SystemDiagnosticsTool());
toolRegistry.registerTool(new MemoryManagementTool());

// Configuration management
toolRegistry.registerTool(new ConfigurationBackupTool());
toolRegistry.registerTool(new ConfigurationExportTool());
toolRegistry.registerTool(new ConfigurationGetTool());
toolRegistry.registerTool(new ConfigurationImportTool());
toolRegistry.registerTool(new ConfigurationListTool());
toolRegistry.registerTool(new ConfigurationSetTool());
toolRegistry.registerTool(new ConfigurationValidationTool());

// Get tool specifications
McpServerFeatures.SyncToolSpecification[] specs = toolRegistry.getToolSpecifications();
System.out.println("Registered " + specs.length + " tools:");

for (McpServerFeatures.SyncToolSpecification spec : specs) {
    System.out.println("  - " + spec.name() + ": " + spec.withDescription());
}
```

## Integration Examples

### OSGi Bundle Integration

```java
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleException;

public class MCPBundleActivator implements BundleActivator {
    private MCPServerInstance server;
    private MCPToolRegistry toolRegistry;
    
    @Override
    public void start(BundleContext context) throws Exception {
        // Create configuration
        MCPServerConfiguration config = MCPServerConfiguration.builder()
            .serverId("osgi-mcp-server")
            .transportType(MCPTransportType.STDIO)
            .enableTools(true)
            .build();
        
        // Create tool registry
        toolRegistry = new MCPToolRegistry();
        
        // Register tools
        toolRegistry.registerTool(new ListItemsTool());
        toolRegistry.registerTool(new ListThingsTool());
        
        // Create and start server
        server = new MCPServerInstance("osgi-server", config, toolRegistry);
        server.start();
        
        System.out.println("MCP Bundle started successfully");
    }
    
    @Override
    public void stop(BundleContext context) throws Exception {
        if (server != null) {
            server.stop();
            System.out.println("MCP Bundle stopped successfully");
        }
    }
}
```

### Spring Integration

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MCPConfiguration {
    
    @Bean
    public MCPServerConfiguration mcpServerConfiguration() {
        return MCPServerConfiguration.builder()
            .serverId("spring-mcp-server")
            .transportType(MCPTransportType.SSE)
            .baseUrl("http://localhost:8080")
            .enableTools(true)
            .build();
    }
    
    @Bean
    public MCPToolRegistry mcpToolRegistry() {
        MCPToolRegistry registry = new MCPToolRegistry();
        registry.registerTool(new ListItemsTool());
        registry.registerTool(new ListThingsTool());
        return registry;
    }
    
    @Bean
    public MCPServerInstance mcpServerInstance(MCPServerConfiguration config, MCPToolRegistry registry) {
        MCPServerInstance server = new MCPServerInstance("spring-server", config, registry);
        try {
            server.start();
        } catch (Exception e) {
            throw new RuntimeException("Failed to start MCP server", e);
        }
        return server;
    }
}
```

## Performance Monitoring Examples

### Performance Metrics Collection

```java
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class MCPPerformanceMonitor {
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong successfulRequests = new AtomicLong(0);
    private final AtomicLong failedRequests = new AtomicLong(0);
    private final Map<String, AtomicLong> toolUsage = new ConcurrentHashMap<>();
    private final long startTime = System.currentTimeMillis();
    
    public void recordToolUsage(String toolName) {
        toolUsage.computeIfAbsent(toolName, k -> new AtomicLong(0)).incrementAndGet();
        totalRequests.incrementAndGet();
    }
    
    public void recordSuccess() {
        successfulRequests.incrementAndGet();
    }
    
    public void recordFailure() {
        failedRequests.incrementAndGet();
    }
    
    public void printMetrics() {
        long uptime = System.currentTimeMillis() - startTime;
        long total = totalRequests.get();
        long successful = successfulRequests.get();
        long failed = failedRequests.get();
        
        System.out.println("=== MCP Performance Metrics ===");
        System.out.println("Uptime: " + (uptime / 1000) + " seconds");
        System.out.println("Total requests: " + total);
        System.out.println("Successful: " + successful);
        System.out.println("Failed: " + failed);
        System.out.println("Success rate: " + (total > 0 ? (successful * 100.0 / total) : 0) + "%");
        System.out.println("Requests per second: " + (uptime > 0 ? (total * 1000.0 / uptime) : 0));
        
        System.out.println("\nTool usage:");
        toolUsage.forEach((tool, count) -> 
            System.out.println("  " + tool + ": " + count.get() + " calls"));
    }
}
```

## Troubleshooting Examples

### Debug Configuration

```java
// Enable debug mode for troubleshooting
MCPServerConfiguration debugConfig = MCPServerConfiguration.builder()
    .serverId("debug-mcp-server")
    .transportType(MCPTransportType.STDIO)
    .enableTools(true)
    .enableLogging(true)
    .serverOption("debug", true)
    .serverOption("verbose", true)
    .transportOption("debug", true)
    .build();

// Create server with debug logging
MCPToolRegistry toolRegistry = new MCPToolRegistry();
MCPServerInstance server = new MCPServerInstance("debug-server", debugConfig, toolRegistry);

try {
    server.start();
    
    // Monitor transport health
    TransportHealthInfo health = server.getTransportHealth();
    System.out.println("Debug - Transport health: " + health);
    
    // Get detailed statistics
    TransportStatistics stats = server.getTransportStatistics();
    System.out.println("Debug - Transport stats: " + stats);
    
} catch (Exception e) {
    System.err.println("Debug - Server startup failed: " + e.getMessage());
    e.printStackTrace();
}
```

### Error Recovery

```java
// Implement error recovery with retry logic
public class MCPErrorRecovery {
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 5000;
    
    public static MCPServerInstance createServerWithRetry(MCPServerConfiguration config, 
                                                         MCPToolRegistry registry) {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                System.out.println("Attempt " + attempt + " to create MCP server...");
                
                MCPServerInstance server = new MCPServerInstance("retry-server", config, registry);
                server.start();
                
                System.out.println("MCP server created successfully on attempt " + attempt);
                return server;
                
            } catch (Exception e) {
                lastException = e;
                System.err.println("Attempt " + attempt + " failed: " + e.getMessage());
                
                if (attempt < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", ie);
                    }
                }
            }
        }
        
        throw new RuntimeException("Failed to create MCP server after " + MAX_RETRIES + " attempts", lastException);
    }
}
```

These examples demonstrate the comprehensive capabilities of the MCP bundle and provide practical guidance for various use cases.