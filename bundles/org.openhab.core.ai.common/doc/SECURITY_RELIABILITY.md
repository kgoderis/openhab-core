# Security & Reliability Guide: openHAB MCP Bundle

This document describes the security and reliability features implemented in the openHAB MCP bundle, including authentication, authorization, error recovery, and graceful degradation.

## Overview

The MCP bundle integrates with the `org.openhab.core.ai.common` bundle to provide comprehensive security and reliability features:

- **Authentication & Authorization**: JWT-based authentication with role-based access control
- **Rate Limiting**: Per-client request rate limiting with configurable thresholds
- **Request Validation**: Input validation and sanitization
- **Error Recovery**: Circuit breaker pattern with automatic fallback mechanisms
- **Graceful Degradation**: Service degradation when dependencies are unavailable
- **Audit Logging**: Comprehensive security event logging

## Security Features

### Authentication

The MCP bundle supports multiple authentication methods through the `AIAuthenticationManager`:

#### JWT Authentication
```java
// Authenticate with JWT token
Optional<AIAuthenticationContext> context = securityManager.authenticateWithJWT(jwtToken, clientId);
if (context.isPresent()) {
    // Authentication successful
    String principalId = context.get().getPrincipalId();
    Set<String> permissions = context.get().getPermissions();
}
```

#### Credential Authentication
```java
// Authenticate with credentials
Map<String, String> credentials = new HashMap<>();
credentials.put("username", "user");
credentials.put("password", "pass");

Optional<AIAuthenticationContext> context = securityManager.authenticateClient(credentials, clientId);
```

### Authorization

Role-based access control is implemented through the `AIRoleBasedAccessControl` interface:

#### MCP-Specific Permissions
- `mcp:connect` - Permission to connect to MCP server
- `mcp:tools` - Permission to use MCP tools
- `mcp:read` - Permission to read data
- `mcp:write` - Permission to write data
- `mcp:admin` - Administrative permissions

#### Permission Checking
```java
// Check if client has permission
boolean hasPermission = securityManager.hasPermission(context, "mcp:tools");
if (hasPermission) {
    // Execute tool operation
} else {
    // Return permission denied error
}
```

### Rate Limiting

Per-client rate limiting prevents abuse:

```java
// Validate request with rate limiting
boolean allowed = securityManager.validateRequest(clientId, "tool_execution");
if (!allowed) {
    // Return rate limit exceeded error
}
```

**Configuration:**
- `maxConnections`: Maximum concurrent connections (default: 100)
- `rateLimitPerMinute`: Requests per minute per client (default: 1000)

### Request Validation

Input validation and sanitization:

```java
// Validate request format
if (config.isEnableRequestValidation()) {
    boolean valid = validateRequestFormat(requestType);
    if (!valid) {
        // Return validation error
    }
}
```

## Reliability Features

### Error Recovery

The `MCPErrorRecoveryManager` implements the circuit breaker pattern:

#### Circuit Breaker States
- **CLOSED**: Normal operation
- **OPEN**: Circuit is open (failing)
- **HALF_OPEN**: Testing if service is recovered

#### Recovery Actions
- **RETRY**: Retry the operation
- **FALLBACK**: Use fallback mechanism
- **DEGRADE**: Degrade functionality

#### Error Handling
```java
// Handle error and determine recovery action
RecoveryAction action = errorRecoveryManager.handleError("NETWORK_ERROR", "Connection timeout", clientId);
switch (action) {
    case RETRY:
        // Retry operation
        break;
    case FALLBACK:
        // Use fallback mechanism
        errorRecoveryManager.recordFallback("NETWORK_ERROR", "Using cached data");
        break;
    case DEGRADE:
        // Degrade functionality
        break;
}
```

### Graceful Degradation

Service degradation when dependencies are unavailable:

```java
// Check service health
boolean healthy = errorRecoveryManager.isServiceHealthy("database");
if (!healthy) {
    // Use degraded mode
    logger.warn("Database service unhealthy, using degraded mode");
}
```

### Error Statistics

Monitor error recovery performance:

```java
// Get error recovery statistics
ErrorRecoveryStatistics stats = errorRecoveryManager.getErrorRecoveryStatistics();
logger.info("Recovery rate: {:.2f}%", stats.getRecoveryRate() * 100);
logger.info("Total errors: {}, Recoveries: {}, Fallbacks: {}", 
    stats.getTotalErrors(), stats.getTotalRecoveries(), stats.getTotalFallbacks());
```

## Integration with ai.common Bundle

### Authentication Manager Integration

The MCP bundle integrates with the `AIAuthenticationManager` from the ai.common bundle:

```java
// Initialize security manager with ai.common components
MCPSecurityManager securityManager = new MCPSecurityManager(
    aiAuthenticationManager,    // From ai.common bundle
    aiRoleBasedAccessControl,   // From ai.common bundle
    aiAuditLogger,             // From ai.common bundle
    mcpConfiguration
);
```

### Audit Logging

All security events are logged through the `AIAuditLogger`:

```java
// Authentication events
auditLogger.logAuthenticationAttempt(clientId, "mcp", Instant.now());
auditLogger.logAuthenticationSuccess(clientId, principalId, "mcp", Instant.now());
auditLogger.logAuthenticationFailure(clientId, "provider", "Invalid credentials", Instant.now());

// Permission checks
auditLogger.logPermissionCheck(principalId, "mcp:tools", "mcp", true, Instant.now());

// Security violations
auditLogger.logSecurityViolation(clientId, "RATE_LIMIT", "Rate limit exceeded", "mcp", Instant.now());
```

## Configuration

### Security Configuration

```java
MCPServerConfiguration config = new MCPServerConfiguration.Builder()
    .serverId("mcp-server")
    .serverName("MCP Server")
    .serverVersion("1.0.0")
    .transportType(MCPTransportType.STDIO)
    
    // Security settings
    .enableAuthentication(true)
    .authToken("your-auth-token")
    .maxConnections(100)
    .rateLimitPerMinute(1000)
    .enableRequestValidation(true)
    
    // Monitoring settings
    .enableMetrics(true)
    .enableHealthChecks(true)
    .healthCheckInterval(30000)
    .enablePerformanceMonitoring(true)
    
    // Production settings
    .productionMode(true)
    .requestTimeout(30000)
    .connectionTimeout(10000)
    .enableGracefulShutdown(true)
    .shutdownTimeout(30000)
    
    .build();
```

### Environment Variables

```bash
# Security
export MCP_ENABLE_AUTHENTICATION=true
export MCP_AUTH_TOKEN=your-secure-token
export MCP_MAX_CONNECTIONS=100
export MCP_RATE_LIMIT_PER_MINUTE=1000
export MCP_ENABLE_REQUEST_VALIDATION=true

# Monitoring
export MCP_ENABLE_METRICS=true
export MCP_ENABLE_HEALTH_CHECKS=true
export MCP_HEALTH_CHECK_INTERVAL=30000
export MCP_ENABLE_PERFORMANCE_MONITORING=true

# Production
export MCP_PRODUCTION_MODE=true
export MCP_REQUEST_TIMEOUT=30000
export MCP_CONNECTION_TIMEOUT=10000
export MCP_ENABLE_GRACEFUL_SHUTDOWN=true
export MCP_SHUTDOWN_TIMEOUT=30000
```

## Health Monitoring

### Enhanced Health Endpoint

The `/health` endpoint includes security and reliability information:

```json
{
  "status": "UP",
  "transport": {
    "type": "STDIO",
    "healthy": true,
    "uptime": 123456,
    "lastError": null
  },
  "uptime": 1234567,
  "version": "1.0.0",
  "security": {
    "enabled": true,
    "activeClients": 5,
    "blockedClients": 0,
    "failedAttempts": 2,
    "requestValidation": true
  },
  "errorRecovery": {
    "totalErrors": 10,
    "totalRecoveries": 8,
    "totalFallbacks": 2,
    "recoveryRate": 0.80,
    "activeErrorTypes": 2,
    "activeCircuitBreakers": 1
  },
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### Enhanced Metrics Endpoint

The `/metrics` endpoint includes security and reliability metrics:

```
# Security metrics
mcp_security_active_clients 5
mcp_security_blocked_clients 0
mcp_security_failed_attempts 2
mcp_security_enabled 1

# Error recovery metrics
mcp_errors_total_count 10
mcp_recoveries_total 8
mcp_fallbacks_total 2
mcp_recovery_rate 0.80
mcp_active_error_types 2
mcp_active_circuit_breakers 1
```

## Best Practices

### Security Best Practices

1. **Enable Authentication**: Always enable authentication in production
2. **Use Strong Tokens**: Use cryptographically secure tokens for `MCP_AUTH_TOKEN`
3. **Rate Limiting**: Configure appropriate rate limits based on expected load
4. **Request Validation**: Enable request validation to prevent malicious input
5. **Audit Logging**: Monitor audit logs for security events

### Reliability Best Practices

1. **Circuit Breakers**: Monitor circuit breaker states and adjust thresholds
2. **Fallback Mechanisms**: Implement appropriate fallback mechanisms for critical services
3. **Error Monitoring**: Monitor error recovery rates and adjust strategies
4. **Graceful Degradation**: Design systems to function with reduced capabilities
5. **Health Checks**: Regular health checks to detect issues early

### Monitoring Best Practices

1. **Metrics Collection**: Collect and analyze security and reliability metrics
2. **Alerting**: Set up alerts for security violations and low recovery rates
3. **Log Analysis**: Regular analysis of audit logs and error logs
4. **Performance Monitoring**: Monitor performance impact of security measures
5. **Capacity Planning**: Plan capacity based on security and reliability requirements

## Troubleshooting

### Common Security Issues

1. **Authentication Failures**: Check JWT token validity and provider configuration
2. **Rate Limit Exceeded**: Adjust rate limits or investigate client behavior
3. **Permission Denied**: Verify role assignments and permission configurations
4. **Blocked Clients**: Check failed attempt thresholds and blocking duration

### Common Reliability Issues

1. **Circuit Breaker Open**: Investigate underlying service issues
2. **Low Recovery Rate**: Review error handling and fallback mechanisms
3. **Service Unhealthy**: Check service dependencies and health check configuration
4. **High Error Count**: Investigate error sources and improve error handling

## API Reference

### MCPSecurityManager

- `authenticateClient(credentials, clientId)`: Authenticate with credentials
- `authenticateWithJWT(jwtToken, clientId)`: Authenticate with JWT
- `hasPermission(context, permission)`: Check permission
- `validateRequest(clientId, requestType)`: Validate and rate limit request
- `isClientBlocked(clientId)`: Check if client is blocked
- `getSecurityStatistics()`: Get security statistics

### MCPErrorRecoveryManager

- `handleError(errorType, errorMessage, clientId)`: Handle error and determine action
- `recordRecovery(errorType)`: Record successful recovery
- `recordFallback(errorType, fallbackAction)`: Record fallback action
- `isServiceHealthy(serviceName)`: Check service health
- `updateServiceHealth(serviceName, healthy)`: Update service health
- `getErrorRecoveryStatistics()`: Get error recovery statistics
- `getErrorDetails()`: Get detailed error information

---

For more information, see the main README and USAGE_EXAMPLES documentation. 