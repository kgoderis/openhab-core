# HTTP Server Integration for MCP and A2A Protocols

## Overview

This document describes the HTTP server integration for MCP (Model Context Protocol) and A2A (Agent-to-Agent) protocols within openHAB's HTTP server infrastructure. The integration uses the OSGi HTTP Whiteboard pattern to register servlets and filters with openHAB's shared HTTP server.

## Architecture

### OpenHAB HTTP Whiteboard Pattern

The integration leverages openHAB's OSGi HTTP Whiteboard pattern, which provides:

- **Shared HTTP Server**: Both MCP and A2A protocols use openHAB's existing HTTP server on port 8080
- **Servlet Registration**: Automatic servlet registration and deregistration through OSGi services
- **Filter Integration**: Security filters applied to protocol-specific paths
- **Lifecycle Management**: Proper servlet lifecycle management with openHAB's HTTP infrastructure

### Component Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    openHAB HTTP Server                      │
│                     (Port 8080)                             │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                ProtocolSecurityFilter                       │
│              (Applied to /mcp/* and /a2a/*)                │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐ │
│  │   McpServlet    │  │   A2AServlet    │  │ Other openHAB│ │
│  │   (/mcp/*)      │  │   (/a2a/*)      │  │   Servlets   │ │
│  └─────────────────┘  └─────────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│              ServletLifecycleManager                        │
│           (Centralized Management)                          │
└─────────────────────────────────────────────────────────────┘
```

## Components

### 1. McpServlet

**Location**: `src/main/java/org/openhab/core/ai/servlet/McpServlet.java`

**Purpose**: Handles MCP protocol HTTP requests using Server-Sent Events (SSE) transport.

**Features**:
- Extends `HttpServletSseServerTransportProvider` from MCP SDK
- OSGi HTTP Whiteboard registration with `/mcp/*` pattern
- Automatic servlet lifecycle management
- Integration with `ToolRegistry` for tools, resources, and prompts
- Health monitoring and statistics

**Endpoints**:
- `GET /mcp/message` - MCP message endpoint
- `GET /mcp/sse` - Server-Sent Events endpoint
- `POST /mcp/message` - MCP message processing

**Configuration**:
```java
@Component(service = Servlet.class, immediate = true)
@HttpWhiteboardServletName("mcp-servlet")
@HttpWhiteboardServletPattern("/mcp/*")
```

### 2. A2AServlet

**Location**: `src/main/java/org/openhab/core/ai/servlet/A2AServlet.java`

**Purpose**: Handles A2A protocol HTTP requests using JSON-RPC over HTTP.

**Features**:
- Extends `jakarta.servlet.http.HttpServlet`
- OSGi HTTP Whiteboard registration with `/a2a/*` pattern
- JSON-RPC message processing
- Health and status endpoints
- CORS support

**Endpoints**:
- `POST /a2a/message/send` - Send A2A messages
- `POST /a2a/task/get` - Get A2A tasks
- `POST /a2a/task/cancel` - Cancel A2A tasks
- `GET /a2a/health` - Health check
- `GET /a2a/status` - Status information

**Configuration**:
```java
@Component(service = Servlet.class, immediate = true)
@HttpWhiteboardServletName("a2a-servlet")
@HttpWhiteboardServletPattern("/a2a/*")
```

### 3. ProtocolSecurityFilter

**Location**: `src/main/java/org/openhab/core/ai/tool/filter/ProtocolSecurityFilter.java`

**Purpose**: Provides unified security for both MCP and A2A protocols.

**Features**:
- Implements `jakarta.servlet.Filter` interface
- OSGi HTTP Whiteboard registration with `/mcp/*` and `/a2a/*` patterns
- Rate limiting (1000 requests per minute)
- Protocol-specific security validation
- Audit logging for all requests
- Client IP address detection

**Security Features**:
- Rate limiting and abuse prevention
- Protocol-specific validation
- Request audit logging
- Error tracking and statistics

**Configuration**:
```java
@Component(service = Filter.class, immediate = true)
@HttpWhiteboardFilterName("protocol-security-filter")
@HttpWhiteboardFilterPattern({ "/mcp/*", "/a2a/*" })
```

### 4. ServletLifecycleManager

**Location**: `src/main/java/org/openhab/core/ai/tool/manager/ServletLifecycleManager.java`

**Purpose**: Centralized management of servlet lifecycle and monitoring.

**Features**:
- Servlet registration and deregistration tracking
- Health monitoring for all servlets
- Request and error statistics
- Performance metrics
- Unified health status reporting

**Management Features**:
- Servlet lifecycle tracking
- Health status monitoring
- Request/error statistics
- Performance metrics
- Protocol-specific filtering

### 5. Configuration Classes

#### HttpServerConfiguration

**Location**: `src/main/java/org/openhab/core/ai/tool/HttpServerConfiguration.java`

**Purpose**: Unified configuration for HTTP server settings.

**Configuration Areas**:
- Server settings (port, base URL, context path)
- Servlet paths and patterns
- Security configuration (authentication, rate limiting)
- OAuth 2.1, API key, JWT authentication
- CORS configuration
- SSL/TLS settings
- Monitoring and metrics

#### A2AServerConfiguration

**Location**: `src/main/java/org/openhab/core/ai/tool/A2AServerConfiguration.java`

**Purpose**: A2A-specific server configuration.

**Configuration Areas**:
- A2A protocol endpoints
- Task management settings
- Message routing configuration
- A2A-specific security settings

## Configuration

### Basic Configuration

The HTTP server integration uses openHAB's configuration system. Configuration files are located in:

- `conf/ai/mcp.cfg` - MCP protocol configuration
- `conf/ai/a2a.cfg` - A2A protocol configuration

### Example Configuration

#### MCP Configuration (mcp.cfg)
```properties
# MCP Server Configuration
mcp.server.id=openhab-mcp-server
mcp.server.name=openHAB MCP Server
mcp.server.version=1.0.0

# HTTP Server Integration
mcp.http.baseUrl=http://localhost:8080
mcp.http.servletPath=/mcp
mcp.http.servletPattern=/mcp/*

# Security
mcp.security.enableAuthentication=false
mcp.security.rateLimitPerMinute=1000
mcp.security.enableRequestValidation=true

# Monitoring
mcp.monitoring.enableMetrics=true
mcp.monitoring.enableHealthChecks=true
```

#### A2A Configuration (a2a.cfg)
```properties
# A2A Server Configuration
a2a.server.id=openhab-a2a-server
a2a.server.name=openHAB A2A Server
a2a.server.version=1.0.0

# HTTP Server Integration
a2a.http.baseUrl=http://localhost:8080
a2a.http.servletPath=/a2a
a2a.http.servletPattern=/a2a/*

# Protocol Endpoints
a2a.endpoints.messageSend=/a2a/message/send
a2a.endpoints.taskGet=/a2a/task/get
a2a.endpoints.taskCancel=/a2a/task/cancel
a2a.endpoints.health=/a2a/health
a2a.endpoints.status=/a2a/status

# Security
a2a.security.enableAuthentication=false
a2a.security.rateLimitPerMinute=1000
a2a.security.enableRequestValidation=true

# Task Management
a2a.task.enableTaskManagement=true
a2a.task.maxTaskQueueSize=1000
a2a.task.taskTimeoutSeconds=300
```

## Usage

### Accessing MCP Protocol

The MCP protocol is accessible at:
- **Base URL**: `http://localhost:8080/mcp`
- **Message Endpoint**: `http://localhost:8080/mcp/message`
- **SSE Endpoint**: `http://localhost:8080/mcp/sse`

### Accessing A2A Protocol

The A2A protocol is accessible at:
- **Base URL**: `http://localhost:8080/a2a`
- **Message Send**: `http://localhost:8080/a2a/message/send`
- **Task Get**: `http://localhost:8080/a2a/task/get`
- **Task Cancel**: `http://localhost:8080/a2a/task/cancel`
- **Health Check**: `http://localhost:8080/a2a/health`
- **Status**: `http://localhost:8080/a2a/status`

### Health Monitoring

Both protocols provide health monitoring endpoints:

```bash
# MCP Health Check
curl http://localhost:8080/mcp/health

# A2A Health Check
curl http://localhost:8080/a2a/health

# A2A Status
curl http://localhost:8080/a2a/status
```

## Security

### Authentication Methods

The HTTP server integration supports multiple authentication methods:

1. **OAuth 2.1** - Primary authentication method
2. **openHAB Users** - Integration with openHAB's user system
3. **API Key** - Simple API key authentication
4. **JWT** - JSON Web Token authentication

### Rate Limiting

- **Default Rate Limit**: 1000 requests per minute per client
- **Configurable**: Can be adjusted per protocol
- **Client Identification**: Uses X-Forwarded-For, X-Real-IP, or remote address

### CORS Support

CORS is enabled by default with the following settings:
- **Allowed Origins**: `*` (configurable)
- **Allowed Methods**: `GET, POST, PUT, DELETE, OPTIONS`
- **Allowed Headers**: `Content-Type, Authorization, X-API-Key`

## Monitoring and Metrics

### Servlet Lifecycle Manager

The `ServletLifecycleManager` provides comprehensive monitoring:

```java
// Get overall statistics
ServletLifecycleManager.LifecycleStatistics stats = lifecycleManager.getStatistics();

// Check health status
boolean isHealthy = lifecycleManager.isHealthy();

// Get servlet-specific information
ServletLifecycleManager.ServletInfo mcpInfo = lifecycleManager.getServletInfo("mcp-servlet");
```

### Metrics Available

- **Request Count**: Total requests per servlet
- **Error Count**: Total errors per servlet
- **Error Rate**: Percentage of requests that resulted in errors
- **Requests Per Minute**: Throughput metrics
- **Uptime**: Service uptime in milliseconds
- **Health Status**: Individual servlet health status

## Testing

### Unit Tests

Comprehensive unit tests are available for all components:

- `McpServletTest` - Tests for MCP servlet functionality
- `A2AServletTest` - Tests for A2A servlet functionality
- `ProtocolSecurityFilterTest` - Tests for security filter
- `ServletLifecycleManagerTest` - Tests for lifecycle management

### Integration Tests

Integration tests verify the complete HTTP server integration:

- End-to-end MCP protocol testing
- End-to-end A2A protocol testing
- HTTP server integration testing
- Security integration testing

### Manual Testing

Test the integration manually:

```bash
# Test MCP health
curl -X GET http://localhost:8080/mcp/health

# Test A2A health
curl -X GET http://localhost:8080/a2a/health

# Test A2A status
curl -X GET http://localhost:8080/a2a/status

# Test rate limiting
for i in {1..1001}; do curl -X GET http://localhost:8080/a2a/health; done
```

## Troubleshooting

### Common Issues

1. **Servlet Not Registered**
   - Check OSGi bundle status
   - Verify HTTP Whiteboard annotations
   - Check for configuration errors

2. **Security Filter Blocking Requests**
   - Check rate limiting settings
   - Verify client IP address detection
   - Review audit logs

3. **Authentication Failures**
   - Verify authentication configuration
   - Check API keys and tokens
   - Review authentication method settings

### Logging

Enable debug logging for troubleshooting:

```properties
# Enable debug logging for HTTP server integration
log:set DEBUG org.openhab.core.ai.servlet
log:set DEBUG org.openhab.core.ai.tool.filter
log:set DEBUG org.openhab.core.ai.tool.manager
```

### Health Checks

Use health check endpoints to verify service status:

```bash
# Check overall health
curl http://localhost:8080/a2a/health

# Check detailed status
curl http://localhost:8080/a2a/status
```

## Best Practices

### Security

1. **Enable Authentication**: Always enable authentication in production
2. **Use HTTPS**: Configure SSL/TLS for production deployments
3. **Rate Limiting**: Adjust rate limits based on expected load
4. **Audit Logging**: Monitor audit logs for security events

### Performance

1. **Connection Pooling**: Configure appropriate connection limits
2. **Timeout Settings**: Set appropriate request and connection timeouts
3. **Monitoring**: Enable performance monitoring and metrics
4. **Load Testing**: Test with expected load patterns

### Configuration

1. **Environment-Specific**: Use different configurations for development, testing, and production
2. **Validation**: Validate configuration on startup
3. **Documentation**: Document all configuration changes
4. **Backup**: Keep configuration backups

## Future Enhancements

### Planned Features

1. **Enhanced Authentication**: Integration with more authentication providers
2. **Advanced Monitoring**: Real-time monitoring dashboard
3. **Load Balancing**: Support for multiple server instances
4. **API Versioning**: Support for protocol versioning
5. **WebSocket Support**: WebSocket transport for real-time communication

### Extensibility

The architecture is designed for extensibility:

- **New Protocols**: Easy addition of new protocol servlets
- **Custom Filters**: Support for custom security filters
- **Configuration**: Extensible configuration system
- **Monitoring**: Pluggable monitoring and metrics

## Conclusion

The HTTP server integration provides a robust, secure, and scalable foundation for MCP and A2A protocols within openHAB. By leveraging the OSGi HTTP Whiteboard pattern, the integration seamlessly fits into openHAB's existing HTTP infrastructure while providing comprehensive security, monitoring, and management capabilities.

The modular architecture ensures that each component can be developed, tested, and deployed independently, while the unified configuration and management systems provide a consistent experience across both protocols. 