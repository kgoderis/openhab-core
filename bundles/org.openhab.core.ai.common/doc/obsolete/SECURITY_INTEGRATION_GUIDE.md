# MCP Security Integration Guide

## Overview

The MCP (Model Context Protocol) bundle integrates with the `ai.common` bundle to provide comprehensive security features including authentication, authorization, rate limiting, and audit logging. This guide explains how the security system works and how to configure it.

## Architecture

### Security Components

```
MCP Security Architecture
├── MCPSecurityManager              # Central security orchestrator
├── AIAuthenticationManager         # ai.common authentication manager
├── AIRoleBasedAccessControl        # ai.common RBAC system
├── AIAuditLogger                   # ai.common audit logging
├── Authentication Providers        # ai.common providers
│   ├── OpenHABUsersAuthenticationProvider
│   ├── APIKeyAuthenticationProvider
│   └── OAuth21AuthenticationProvider
└── MCPServerInstance               # MCP server with security integration
```

### Security Flow

1. **Client Connection**: Client connects to MCP server
2. **Authentication**: `MCPSecurityManager` orchestrates authentication using `ai.common` providers
3. **Authorization**: RBAC system checks permissions for MCP operations
4. **Audit Logging**: All security events are logged via `AIAuditLogger`
5. **Rate Limiting**: Request rate limiting is enforced per client
6. **Tool Access**: Tools are filtered based on security configuration

## Configuration

### Basic Security Configuration

```java
MCPServerConfiguration config = MCPServerConfiguration.builder()
    .enableAuthentication(true)
    .primaryAuthMethod("oauth2.1")
    .fallbackAuthMethod("openhab_users")
    .enableFallbackAuth(true)
    .maxConnections(100)
    .rateLimitPerMinute(1000)
    .enableRequestValidation(true)
    .build();
```

### Authentication Methods

#### OAuth 2.1 Configuration
```java
MCPServerConfiguration config = MCPServerConfiguration.builder()
    .oauthIssuerUrl("https://auth.example.com")
    .oauthClientId("mcp-client")
    .oauthClientSecret("secret")
    .oauthRedirectUri("http://localhost:8080/callback")
    .oauthPkceEnabled(true)
    .build();
```

#### openHAB Users Configuration
```java
MCPServerConfiguration config = MCPServerConfiguration.builder()
    .openhabUsersFile("/path/to/users.properties")
    .openhabUsersEnabled(true)
    .build();
```

#### API Key Configuration
```java
MCPServerConfiguration config = MCPServerConfiguration.builder()
    .apiKeyHeader("X-API-Key")
    .apiKeyValue("your-api-key")
    .apiKeyEnabled(true)
    .build();
```

#### JWT Configuration
```java
MCPServerConfiguration config = MCPServerConfiguration.builder()
    .jwtSecret("your-jwt-secret")
    .jwtIssuer("openhab-mcp")
    .jwtExpirationMinutes(60)
    .jwtEnabled(true)
    .build();
```

## MCP-Specific Permissions

The MCP security system defines the following permissions:

- `mcp:connect` - Permission to connect to the MCP server
- `mcp:tools` - Permission to access MCP tools
- `mcp:read` - Permission to read data via MCP
- `mcp:write` - Permission to write data via MCP
- `mcp:admin` - Administrative permissions

## Usage Examples

### Creating a Secure MCP Server

```java
// Create configuration with security enabled
MCPServerConfiguration config = MCPServerConfiguration.builder()
    .serverId("secure-mcp-server")
    .enableAuthentication(true)
    .primaryAuthMethod("oauth2.1")
    .fallbackAuthMethod("openhab_users")
    .enableFallbackAuth(true)
    .maxConnections(50)
    .rateLimitPerMinute(500)
    .build();

// Create security manager with ai.common components
AIAuthenticationManager authManager = new AIAuthenticationManager(jwtManager, rbac, auditLogger);
MCPSecurityManager securityManager = new MCPSecurityManager(authManager, rbac, auditLogger, config);

// Create server instance
MCPServerInstance server = new MCPServerInstance("server-1", config, toolRegistry);

// Set security manager
server.setSecurityManager(securityManager);

// Start server
server.start();
```

### Authentication Flow

```java
// Client authentication
Map<String, String> credentials = new HashMap<>();
credentials.put("username", "user");
credentials.put("password", "password");

Optional<AIAuthenticationContext> context = securityManager.authenticateClient(credentials, "client-1");
if (context.isPresent()) {
    // Client is authenticated
    AIAuthenticationContext authContext = context.get();
    String principalId = authContext.getPrincipalId();
    
    // Check permissions
    boolean canUseTools = securityManager.hasMCPPermission(authContext, "tools");
    boolean canRead = securityManager.hasMCPPermission(authContext, "read");
    boolean canWrite = securityManager.hasMCPPermission(authContext, "write");
}
```

### JWT Authentication

```java
// JWT authentication
String jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
Optional<AIAuthenticationContext> context = securityManager.authenticateWithJWT(jwtToken, "client-1");
```

### Permission Checking

```java
// Check specific MCP permissions
if (securityManager.hasMCPPermission(context, "tools")) {
    // Allow tool access
}

if (securityManager.hasMCPPermission(context, "read")) {
    // Allow read operations
}

if (securityManager.hasMCPPermission(context, "write")) {
    // Allow write operations
}
```

## Security Statistics

The security system provides comprehensive statistics:

```java
MCPSecurityManager.SecurityStatistics stats = securityManager.getSecurityStatistics();

System.out.println("Active clients: " + stats.getActiveClients());
System.out.println("Failed attempts: " + stats.getFailedAttempts());
System.out.println("Blocked clients: " + stats.getBlockedClients());
System.out.println("Authentication enabled: " + stats.isAuthenticationEnabled());
System.out.println("Request validation enabled: " + stats.isRequestValidationEnabled());
System.out.println("Max connections: " + stats.getMaxConnections());
System.out.println("Rate limit per minute: " + stats.getRateLimitPerMinute());
System.out.println("Active sessions: " + stats.getActiveSessions());
```

## Integration with ai.common Bundle

### Authentication Providers

The MCP security system uses the following `ai.common` authentication providers:

1. **OpenHABUsersAuthenticationProvider**
   - Authenticates against openHAB's `users.properties` file
   - Supports Basic HTTP authentication
   - Automatically loads user permissions

2. **APIKeyAuthenticationProvider**
   - Authenticates using API keys
   - Supports multiple header names
   - Configurable expiration times

3. **OAuth21AuthenticationProvider**
   - Implements OAuth 2.1 authentication
   - Supports authorization code flow with PKCE
   - Token introspection and validation

### RBAC Integration

The MCP security system integrates with the `ai.common` RBAC system:

```java
// Check permissions using ai.common RBAC
boolean hasPermission = authManager.hasPermission(principalId, "mcp:tools", "mcp");

// Get user roles and permissions
Set<String> permissions = rbac.getUserPermissions(principalId);
```

### Audit Logging

All security events are logged through the `ai.common` audit logger:

```java
// Authentication events
auditLogger.logAuthenticationSuccess(clientId, "mcp", principalId, Instant.now());
auditLogger.logAuthenticationFailure(clientId, "mcp", "Invalid credentials", Instant.now());

// Permission checks
auditLogger.logPermissionCheck(principalId, "mcp:tools", "mcp", true, Instant.now());

// Security violations
auditLogger.logSecurityViolation(clientId, "RATE_LIMIT", "Rate limit exceeded", "mcp", Instant.now());
```

## Configuration Files

### Environment Variables

The security system supports configuration via environment variables:

```bash
# Authentication
export MCP_ENABLE_AUTHENTICATION=true
export MCP_PRIMARY_AUTH_METHOD=oauth2.1
export MCP_FALLBACK_AUTH_METHOD=openhab_users

# OAuth 2.1
export MCP_OAUTH_ISSUER_URL=https://auth.example.com
export MCP_OAUTH_CLIENT_ID=mcp-client
export MCP_OAUTH_CLIENT_SECRET=secret

# Rate Limiting
export MCP_MAX_CONNECTIONS=100
export MCP_RATE_LIMIT_PER_MINUTE=1000

# Security
export MCP_ENABLE_REQUEST_VALIDATION=true
export MCP_ENABLE_FALLBACK_AUTH=true
```

### Configuration File (mcp.cfg)

```properties
# Authentication Configuration
authentication.enabled=true
authentication.primary_method=oauth2.1
authentication.fallback_method=openhab_users
authentication.enable_fallback=true

# OAuth 2.1 Configuration
oauth2.1.issuer_url=https://auth.example.com
oauth2.1.client_id=mcp-client
oauth2.1.client_secret=secret
oauth2.1.redirect_uri=http://localhost:8080/callback
oauth2.1.pkce_enabled=true

# openHAB Users Configuration
openhab.users.file=${OPENHAB_CONFIG}/users.properties
openhab.users.enabled=true

# API Key Configuration
api.key.header=X-API-Key
api.key.value=your-api-key
api.key.enabled=false

# JWT Configuration
jwt.secret=your-jwt-secret
jwt.issuer=openhab-mcp
jwt.expiration_minutes=60
jwt.enabled=false

# Rate Limiting
rate.limit.max_connections=100
rate.limit.per_minute=1000

# Security
security.enable_request_validation=true
security.enable_metrics=true
```

## Best Practices

### Security Configuration

1. **Enable Authentication**: Always enable authentication in production
2. **Use Multiple Methods**: Configure primary and fallback authentication methods
3. **Rate Limiting**: Set appropriate rate limits to prevent abuse
4. **Request Validation**: Enable request validation for additional security
5. **Audit Logging**: Ensure audit logging is enabled for compliance

### Authentication Methods

1. **OAuth 2.1**: Use for production environments with proper OAuth providers
2. **openHAB Users**: Use as fallback for existing openHAB installations
3. **API Keys**: Use for service-to-service communication
4. **JWT**: Use for stateless authentication

### Permission Management

1. **Principle of Least Privilege**: Grant minimum required permissions
2. **Role-Based Access**: Use roles to group permissions
3. **Regular Review**: Regularly review and update permissions
4. **Audit Trail**: Maintain audit trail for all permission changes

### Monitoring and Alerting

1. **Security Metrics**: Monitor security statistics
2. **Failed Authentication**: Alert on repeated failed authentication attempts
3. **Rate Limit Violations**: Monitor rate limit violations
4. **Permission Denials**: Track permission denials for potential issues

## Troubleshooting

### Common Issues

1. **Authentication Failures**
   - Check authentication provider configuration
   - Verify credentials and tokens
   - Check audit logs for specific error messages

2. **Permission Denials**
   - Verify user permissions in RBAC system
   - Check MCP-specific permission mappings
   - Review audit logs for permission checks

3. **Rate Limiting Issues**
   - Adjust rate limit configuration
   - Check for client misbehavior
   - Monitor rate limit statistics

4. **Provider Registration Issues**
   - Verify provider configuration
   - Check provider initialization logs
   - Ensure providers are properly registered with authentication manager

### Debugging

Enable debug logging for security components:

```properties
# Security debug logging
logging.level.org.openhab.core.ai.mcp.internal.MCPSecurityManager=DEBUG
logging.level.org.openhab.core.ai.common.auth=DEBUG
```

### Health Checks

Monitor security system health:

```java
// Check security manager health
boolean isHealthy = server.isHealthy();
MCPSecurityManager.SecurityStatistics stats = server.getSecurityStatistics();

// Check for blocked clients
if (stats.getBlockedClients() > 0) {
    logger.warn("{} clients are currently blocked", stats.getBlockedClients());
}
```

## Future Enhancements

### Planned Features

1. **Advanced RBAC**: More granular permission system
2. **Session Management**: Enhanced session tracking and management
3. **Multi-Factor Authentication**: Support for MFA
4. **Certificate-Based Authentication**: X.509 certificate support
5. **OAuth 2.1 Enhancements**: Full OAuth 2.1 implementation
6. **Security Headers**: Additional security headers for web transports

### Integration Opportunities

1. **openHAB Security**: Deeper integration with openHAB security system
2. **External Identity Providers**: Support for external IdPs
3. **Security Information and Event Management (SIEM)**: Integration with SIEM systems
4. **Compliance Reporting**: Enhanced compliance and audit reporting 