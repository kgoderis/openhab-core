# Authentication Integration Guide: openHAB MCP Bundle

This document provides a comprehensive analysis of authentication features available from different sources and how they are integrated in the openHAB MCP bundle.

## 🔐 Security Features Analysis

### 1. MCP SDK Security Features (Out of the Box)

**What the MCP SDK Provides:**
- **Transport Security**: Support for STDIO and HTTP-based transports (SSE)
- **Session Management**: Exchange objects for session context (v0.8.0+)
- **Client Capabilities**: Access to client information through exchange objects
- **No Built-in Security**: The SDK itself doesn't provide authentication, authorization, or security features

**OAuth 2.1 Features Missing in MCP SDK:**
- **OAuth 2.1 Authorization Server Discovery** (RFC9728)
  - Protected resource metadata endpoint
  - Client registration endpoint (RFC7591)
  - Authorization server metadata
- **OAuth 2.1 Authorization Flow**
  - Authorization code flow with PKCE
  - Client credentials flow
  - Device authorization flow
  - Token introspection (RFC7662)
- **Token Management**
  - Access token generation and validation
  - Refresh token handling
  - Token revocation (RFC7009)
- **Client Registration**
  - Dynamic client registration (RFC7591)
  - Client metadata management
  - Client authentication

### 2. openHAB Core Security Features

**What openHAB Core Provides:**
- **File-based Authentication**: `users.properties` file with username=password format
- **Basic HTTP Authentication**: Standard HTTP Basic authentication
- **Certificate Authentication**: Keystore-based certificate authentication
- **API Key Support**: Basic API key functionality (mentioned in taskmaster config)
- **User Management**: Basic user creation and management

**Limitations:**
- No OAuth 2.1 support
- No JWT token management
- No role-based access control
- No audit logging
- No session management

### 3. ai.common Bundle Security Features

**What the ai.common Bundle Provides:**
- **JWT Token Management**: Complete JWT token lifecycle management
- **Role-Based Access Control (RBAC)**: Comprehensive permission system
- **Audit Logging**: Detailed security event logging
- **Multiple Authentication Providers**: Pluggable authentication system
- **Session Management**: Session creation, validation, and refresh
- **Authentication Context**: Rich authentication context with permissions

## 🔄 Fallback Authentication System

### Configuration-Based Authentication Selection

The MCP bundle implements a flexible authentication system that allows users to select their preferred authentication method:

```java
MCPServerConfiguration config = new MCPServerConfiguration.Builder()
    .serverId("mcp-server")
    .serverName("MCP Server")
    .serverVersion("1.0.0")
    .transportType(MCPTransportType.STDIO)
    
    // Authentication method selection
    .primaryAuthMethod("oauth2.1")           // Primary method: oauth2.1, openhab_users, api_key, jwt
    .fallbackAuthMethod("openhab_users")     // Fallback method: openhab_users, api_key, jwt, none
    .enableFallbackAuth(true)                // Enable fallback authentication
    
    // OAuth 2.1 configuration
    .oauthIssuerUrl("https://auth.example.com")
    .oauthClientId("mcp-client")
    .oauthClientSecret("client-secret")
    .oauthRedirectUri("http://localhost:8080/callback")
    .oauthPkceEnabled(true)
    
    // openHAB users authentication
    .openhabUsersFile("/path/to/users.properties")
    .openhabUsersEnabled(true)
    
    // API key authentication
    .apiKeyHeader("X-API-Key")
    .apiKeyValue("your-api-key")
    .apiKeyEnabled(true)
    
    // JWT authentication
    .jwtSecret("your-jwt-secret")
    .jwtIssuer("openhab-mcp")
    .jwtExpirationMinutes(60)
    .jwtEnabled(true)
    
    .build();
```

### Authentication Flow

1. **Primary Authentication**: Try the configured primary authentication method
2. **Fallback Authentication**: If primary fails and fallback is enabled, try the fallback method
3. **ai.common Fallback**: If both fail, fall back to the ai.common authentication manager
4. **Authentication Failure**: If all methods fail, block the client

## 🏗️ Implementation Architecture

### Authentication Providers

#### 1. OpenHABUsersAuthenticationProvider

Authenticates users against openHAB's `users.properties` file:

```java
// Configuration
String usersFile = "/path/to/users.properties";
boolean enabled = true;

OpenHABUsersAuthenticationProvider provider = new OpenHABUsersAuthenticationProvider(usersFile, enabled);

// Authentication
Map<String, String> credentials = new HashMap<>();
credentials.put("username", "admin");
credentials.put("password", "password");

Optional<AIAuthenticationContext> context = provider.authenticate(credentials);
```

**Features:**
- Reads from standard openHAB `users.properties` file
- Supports username/password authentication
- Provides basic MCP permissions
- Session expiration (1 hour)

#### 2. APIKeyAuthenticationProvider

Authenticates clients using API keys:

```java
// Configuration
String expectedApiKey = "your-secure-api-key";
String apiKeyHeader = "X-API-Key";
boolean enabled = true;

APIKeyAuthenticationProvider provider = new APIKeyAuthenticationProvider(expectedApiKey, apiKeyHeader, enabled);

// Authentication
Map<String, String> credentials = new HashMap<>();
credentials.put("api_key", "your-secure-api-key");

Optional<AIAuthenticationContext> context = provider.authenticate(credentials);
```

**Features:**
- Multiple API key locations (header, parameter, authorization header)
- Full MCP permissions for API key clients
- Session expiration (2 hours)
- Secure key validation

#### 3. OAuth21AuthenticationProvider

Implements OAuth 2.1 authentication according to MCP specification:

```java
// Configuration
String issuerUrl = "https://auth.example.com";
String clientId = "mcp-client";
String clientSecret = "client-secret";
String redirectUri = "http://localhost:8080/callback";
boolean pkceEnabled = true;
boolean enabled = true;

OAuth21AuthenticationProvider provider = new OAuth21AuthenticationProvider(
    issuerUrl, clientId, clientSecret, redirectUri, pkceEnabled, enabled);

// Authentication
Map<String, String> credentials = new HashMap<>();
credentials.put("access_token", "oauth2.1-access-token");

Optional<AIAuthenticationContext> context = provider.authenticate(credentials);
```

**Features:**
- OAuth 2.1 access token validation
- Scope-based permission mapping
- PKCE support
- Token introspection (planned)
- Client registration (planned)

### Integration with MCPSecurityManager

The `MCPSecurityManager` integrates all authentication providers:

```java
// Initialize security manager with all providers
MCPSecurityManager securityManager = new MCPSecurityManager(
    aiAuthenticationManager,    // ai.common authentication manager
    aiRoleBasedAccessControl,   // ai.common RBAC
    aiAuditLogger,             // ai.common audit logger
    mcpConfiguration           // MCP configuration with auth settings
);

// Authenticate client with fallback support
Optional<AIAuthenticationContext> context = securityManager.authenticateClient(credentials, clientId);
```

## 🔧 Configuration Examples

### Environment Variables

```bash
# Authentication method selection
export MCP_PRIMARY_AUTH_METHOD=oauth2.1
export MCP_FALLBACK_AUTH_METHOD=openhab_users
export MCP_ENABLE_FALLBACK_AUTH=true

# OAuth 2.1 configuration
export MCP_OAUTH_ISSUER_URL=https://auth.example.com
export MCP_OAUTH_CLIENT_ID=mcp-client
export MCP_OAUTH_CLIENT_SECRET=client-secret
export MCP_OAUTH_REDIRECT_URI=http://localhost:8080/callback
export MCP_OAUTH_PKCE_ENABLED=true

# openHAB users authentication
export MCP_OPENHAB_USERS_FILE=/path/to/users.properties
export MCP_OPENHAB_USERS_ENABLED=true

# API key authentication
export MCP_API_KEY_HEADER=X-API-Key
export MCP_API_KEY_VALUE=your-secure-api-key
export MCP_API_KEY_ENABLED=true

# JWT authentication
export MCP_JWT_SECRET=your-jwt-secret
export MCP_JWT_ISSUER=openhab-mcp
export MCP_JWT_EXPIRATION_MINUTES=60
export MCP_JWT_ENABLED=true
```

### Configuration File

```properties
# Authentication method selection
mcp.primary.auth.method=oauth2.1
mcp.fallback.auth.method=openhab_users
mcp.enable.fallback.auth=true

# OAuth 2.1 configuration
mcp.oauth.issuer.url=https://auth.example.com
mcp.oauth.client.id=mcp-client
mcp.oauth.client.secret=client-secret
mcp.oauth.redirect.uri=http://localhost:8080/callback
mcp.oauth.pkce.enabled=true

# openHAB users authentication
mcp.openhab.users.file=/path/to/users.properties
mcp.openhab.users.enabled=true

# API key authentication
mcp.api.key.header=X-API-Key
mcp.api.key.value=your-secure-api-key
mcp.api.key.enabled=true

# JWT authentication
mcp.jwt.secret=your-jwt-secret
mcp.jwt.issuer=openhab-mcp
mcp.jwt.expiration.minutes=60
mcp.jwt.enabled=true
```

## 🚀 Usage Examples

### Basic Setup with openHAB Users

```java
// Simple setup using openHAB users
MCPServerConfiguration config = new MCPServerConfiguration.Builder()
    .primaryAuthMethod("openhab_users")
    .fallbackAuthMethod("none")
    .enableFallbackAuth(false)
    .openhabUsersEnabled(true)
    .build();
```

### OAuth 2.1 with Fallback

```java
// OAuth 2.1 primary with openHAB users fallback
MCPServerConfiguration config = new MCPServerConfiguration.Builder()
    .primaryAuthMethod("oauth2.1")
    .fallbackAuthMethod("openhab_users")
    .enableFallbackAuth(true)
    .oauthIssuerUrl("https://auth.example.com")
    .oauthClientId("mcp-client")
    .oauthClientSecret("client-secret")
    .oauthRedirectUri("http://localhost:8080/callback")
    .oauthPkceEnabled(true)
    .openhabUsersEnabled(true)
    .build();
```

### API Key Authentication

```java
// API key authentication
MCPServerConfiguration config = new MCPServerConfiguration.Builder()
    .primaryAuthMethod("api_key")
    .fallbackAuthMethod("none")
    .enableFallbackAuth(false)
    .apiKeyHeader("X-API-Key")
    .apiKeyValue("your-secure-api-key")
    .apiKeyEnabled(true)
    .build();
```

### JWT Authentication

```java
// JWT authentication
MCPServerConfiguration config = new MCPServerConfiguration.Builder()
    .primaryAuthMethod("jwt")
    .fallbackAuthMethod("openhab_users")
    .enableFallbackAuth(true)
    .jwtSecret("your-jwt-secret")
    .jwtIssuer("openhab-mcp")
    .jwtExpirationMinutes(60)
    .jwtEnabled(true)
    .openhabUsersEnabled(true)
    .build();
```

## 🔒 Security Best Practices

### 1. OAuth 2.1 Best Practices

- **Use PKCE**: Always enable PKCE for public clients
- **Secure Issuer**: Use HTTPS for OAuth 2.1 issuer URLs
- **Token Validation**: Implement proper token validation
- **Scope Limitation**: Limit scopes to minimum required permissions
- **Client Registration**: Use dynamic client registration when possible

### 2. API Key Best Practices

- **Secure Storage**: Store API keys securely (environment variables, secure vaults)
- **Key Rotation**: Implement regular key rotation
- **Access Logging**: Log all API key usage
- **Key Validation**: Use cryptographically secure key validation

### 3. openHAB Users Best Practices

- **Password Security**: Use strong passwords in users.properties
- **File Permissions**: Restrict access to users.properties file
- **Regular Updates**: Regularly update user passwords
- **User Management**: Implement proper user lifecycle management

### 4. JWT Best Practices

- **Secure Secrets**: Use cryptographically secure JWT secrets
- **Token Expiration**: Set appropriate token expiration times
- **Token Validation**: Validate all JWT claims
- **Audience Validation**: Validate JWT audience claims

## 🔍 Monitoring and Auditing

### Health Monitoring

The authentication system provides comprehensive health monitoring:

```json
{
  "security": {
    "enabled": true,
    "primaryMethod": "oauth2.1",
    "fallbackMethod": "openhab_users",
    "fallbackEnabled": true,
    "activeClients": 5,
    "blockedClients": 0,
    "failedAttempts": 2,
    "requestValidation": true
  }
}
```

### Audit Logging

All authentication events are logged through the `AIAuditLogger`:

```java
// Authentication events
auditLogger.logAuthenticationAttempt(clientId, "mcp", Instant.now());
auditLogger.logAuthenticationSuccess(clientId, principalId, "mcp", Instant.now());
auditLogger.logAuthenticationFailure(clientId, "oauth2.1", "Invalid token", Instant.now());

// Permission checks
auditLogger.logPermissionCheck(principalId, "mcp:tools", "mcp", true, Instant.now());

// Security violations
auditLogger.logSecurityViolation(clientId, "RATE_LIMIT", "Rate limit exceeded", "mcp", Instant.now());
```

## 🔮 Future Enhancements

### Planned OAuth 2.1 Features

1. **Token Introspection**: Implement RFC7662 token introspection
2. **Client Registration**: Implement RFC7591 dynamic client registration
3. **Device Authorization**: Implement device authorization flow
4. **Token Revocation**: Implement RFC7009 token revocation
5. **Protected Resource Metadata**: Implement RFC9728 metadata discovery

### Planned Security Enhancements

1. **Multi-Factor Authentication**: Support for MFA
2. **Certificate Authentication**: Client certificate authentication
3. **LDAP Integration**: LDAP authentication provider
4. **OAuth 2.1 Authorization Server**: Built-in OAuth 2.1 server
5. **Advanced RBAC**: Role-based access control with inheritance

---

This authentication integration provides a comprehensive, flexible, and secure authentication system that leverages the strengths of each component while providing fallback mechanisms for reliability. 