# openHAB MCP Bundle

## Overview

The `org.openhab.core.ai.mcp` bundle provides a complete implementation of the Model Context Protocol (MCP) for openHAB, enabling AI assistants to interact with openHAB systems through a standardized protocol.

## Features

### ✅ **Complete SDK Integration**
- **Real MCP Java SDK**: Full integration with the official [Model Context Protocol Java SDK](https://github.com/modelcontextprotocol/java-sdk)
- **Sync Server Pattern**: Uses `McpServer.sync()` for reliable server implementation
- **Proper Lifecycle Management**: Start/stop/close functionality with state tracking
- **Tool Integration**: Real tool registration using `McpToolUtils` and proper specifications

### ✅ **Transport Implementation**
- **STDIO Transport**: Standard input/output for process-based communication
- **SSE Transport**: Server-Sent Events over HTTP for web-based clients
- **Transport Selection**: Configurable transport type with automatic fallback
- **Health Monitoring**: Real-time transport health and performance metrics

### ✅ **Comprehensive Tool Suite**
- **Item Management**: List, get, and manage openHAB items
- **Thing Management**: Discover, configure, and manage things
- **Channel Management**: Link and manage channels
- **Rule Management**: List and manage automation rules
- **Configuration Management**: Backup, restore, and validate configurations
- **System Management**: Health checks, diagnostics, and monitoring
- **Security Management**: Authentication and access control
- **File System Management**: File operations and management
- **Script Management**: Script execution and library management
- **Persistence Management**: Data persistence operations
- **Event Management**: Event monitoring and management
- **Binding Management**: Binding discovery and configuration
- **Analytics Tools**: Data analysis and reporting
- **Advanced Automation**: Complex automation workflows

### ✅ **Production-Ready Features**
- **Configuration Management**: Comprehensive configuration options
- **Error Handling**: Robust error handling with fallback mechanisms
- **Logging**: Detailed logging for debugging and monitoring
- **Health Monitoring**: Transport and server health tracking
- **Performance Metrics**: Transport performance and statistics
- **State Management**: Proper server state tracking

## Architecture

### Core Components

```
org.openhab.core.ai.mcp/
├── api/                    # Public API interfaces
│   └── tool/              # Tool-related interfaces
├── internal/              # Internal implementation
│   ├── MCPServerInstance.java      # Main server instance
│   ├── MCPServerConfiguration.java # Configuration management
│   ├── MCPTransportType.java       # Transport type enumeration
│   ├── MCPToolRegistry.java        # Tool registration
│   └── MCPToolAdapter.java         # Tool adapter implementation
└── tools/                 # MCP tool implementations
    ├── items/             # Item management tools
    ├── things/            # Thing management tools
    ├── channels/          # Channel management tools
    ├── rules/             # Rule management tools
    ├── config/            # Configuration tools
    ├── system/            # System management tools
    ├── security/          # Security tools
    ├── filesystem/        # File system tools
    ├── scripts/           # Script management tools
    ├── persistence/       # Persistence tools
    ├── events/            # Event management tools
    ├── bindings/          # Binding management tools
    ├── analytics/         # Analytics tools
    └── automation/        # Advanced automation tools
```

### Transport Architecture

The bundle supports multiple transport types with automatic fallback:

1. **STDIO Transport** (Default)
   - Process-based communication
   - Reliable and simple
   - Used as fallback for other transports

2. **SSE Transport** (Server-Sent Events)
   - HTTP-based communication
   - Web client support
   - Real-time event streaming

### Health Monitoring

The implementation includes comprehensive health monitoring:

- **Transport Health**: Real-time transport status and metrics
- **Performance Tracking**: Transport creation and operation timing
- **Error Tracking**: Detailed error logging and recovery
- **Fallback Detection**: Automatic detection of transport fallbacks

## Configuration

### Basic Configuration

```java
MCPServerConfiguration config = MCPServerConfiguration.builder()
    .serverId("openhab-mcp-server")
    .serverName("openHAB MCP Server")
    .serverVersion("1.0.0")
    .transportType(MCPTransportType.STDIO)  // or MCPTransportType.SSE
    .enableTools(true)
    .enableResources(true)
    .enablePrompts(true)
    .enableLogging(true)
    .build();
```

### SSE Transport Configuration

```java
MCPServerConfiguration config = MCPServerConfiguration.builder()
    .transportType(MCPTransportType.SSE)
    .baseUrl("http://localhost:8080")
    .messageEndpoint("/mcp/message")
    .sseEndpoint("/mcp/events")
    .enableSse(true)
    .build();
```

### Transport Options

```java
MCPServerConfiguration config = MCPServerConfiguration.builder()
    .transportOption("timeout", 30000)
    .transportOption("maxConnections", 10)
    .serverOption("debug", true)
    .build();
```

## Usage

### Creating a Server Instance

```java
// Create configuration
MCPServerConfiguration config = MCPServerConfiguration.builder()
    .serverId("my-mcp-server")
    .transportType(MCPTransportType.STDIO)
    .build();

// Create tool registry
MCPToolRegistry toolRegistry = new MCPToolRegistry();

// Create server instance
MCPServerInstance server = new MCPServerInstance("server-1", config, toolRegistry);

// Start the server
server.start();
```

### Health Monitoring

```java
// Check server health
boolean isHealthy = server.isHealthy();

// Get transport health information
TransportHealthInfo health = server.getTransportHealth();
System.out.println("Transport: " + health.getTransportType());
System.out.println("Healthy: " + health.isHealthy());
System.out.println("Uptime: " + health.getUptime() + "ms");

// Get detailed statistics
TransportStatistics stats = server.getTransportStatistics();
System.out.println("Using fallback: " + stats.isUsingFallback());
System.out.println("Transport class: " + stats.getTransportClass());
```

### Tool Registration

```java
// Register tools with the registry
toolRegistry.registerTool(new ListItemsTool());
toolRegistry.registerTool(new GetThingTool());
toolRegistry.registerTool(new ListRulesTool());

// Tools are automatically available to MCP clients
```

## Transport Features

### Automatic Fallback

The implementation includes intelligent fallback logic:

1. **Primary Transport**: Attempts to create the configured transport
2. **Fallback Logic**: If primary fails, automatically falls back to STDIO
3. **Error Handling**: Comprehensive error tracking and reporting
4. **Health Monitoring**: Real-time health status tracking

### Performance Monitoring

- **Creation Timing**: Tracks transport creation performance
- **Uptime Tracking**: Monitors transport uptime and stability
- **Error Tracking**: Detailed error logging and analysis
- **Fallback Detection**: Automatic detection of transport fallbacks

### Configuration Validation

- **Transport Type Validation**: Ensures valid transport types
- **Configuration Validation**: Validates all configuration parameters
- **Default Values**: Provides sensible defaults for all options
- **Error Recovery**: Graceful handling of configuration errors

## Development Status

### ✅ **Completed Features**
- Full SDK integration with official MCP Java SDK
- STDIO and SSE transport implementations
- Comprehensive tool suite (40+ tools)
- Transport health monitoring and fallback logic
- Production-ready configuration management
- Detailed logging and error handling

### 🔄 **In Progress**
- Documentation and usage examples
- Performance optimization
- Advanced configuration options

### 📋 **Planned Features**
- WebSocket transport (removed from current scope)
- Async server support
- Resource management
- Prompt management
- Completion support

## Building

```bash
# Build the MCP bundle
cd bundles/org.openhab.core.ai.mcp
mvn clean install

# Build with tests
mvn clean verify

# Build with integration tests
mvn clean verify -P integration-test
```

## Testing

The bundle includes comprehensive testing infrastructure:

- **Unit Tests**: Individual component testing
- **Integration Tests**: End-to-end testing with real MCP clients
- **Transport Tests**: Transport-specific testing
- **Tool Tests**: Individual tool testing

```bash
# Run unit tests
mvn test

# Run integration tests
mvn verify -P integration-test

# Run with coverage
mvn clean verify jacoco:report
```

## Dependencies

### Core Dependencies
- **MCP Java SDK**: Official Model Context Protocol SDK
- **Jackson**: JSON processing and serialization
- **SLF4J**: Logging framework
- **OSGi Framework**: Bundle management

### Transport Dependencies
- **Jakarta Servlet API**: For SSE transport
- **Reactor**: For reactive programming support

### Testing Dependencies
- **JUnit 5**: Unit testing framework
- **Mockito**: Mocking framework
- **TestContainers**: Integration testing

## Contributing

This bundle follows openHAB development guidelines:

1. **Code Style**: Follow openHAB coding standards
2. **Testing**: Include comprehensive tests for new features
3. **Documentation**: Update documentation for changes
4. **Review Process**: Submit pull requests for review

## License

This project is licensed under the Eclipse Public License 2.0 - see the openHAB project for details.

## Support

For issues and questions:
- **GitHub Issues**: Report bugs and feature requests
- **openHAB Community**: Community support and discussion
- **Documentation**: Check this README and inline documentation