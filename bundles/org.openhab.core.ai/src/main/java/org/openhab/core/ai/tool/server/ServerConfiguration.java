package org.openhab.core.ai.tool.server;

import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.builder.ServerConfigurationBuilder;
import org.openhab.core.ai.tool.server.api.TransportType;

/**
 * Configuration for MCP server.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ServerConfiguration {

    // TODO : Check if all these fields are effectively used in the code

    private final String serverId;
    private final String serverName;
    private final String serverVersion;
    private final TransportType transportType;
    private final String baseUrl;
    private final String messageEndpoint;
    private final String sseEndpoint;
    private final boolean enableSse;

    private final boolean enableTools;
    private final boolean enableResources;
    private final boolean enablePrompts;
    private final boolean enableLogging;

    // Async server configuration
    private final boolean enableAsyncServer;
    private final boolean enableAsyncTools;
    private final int asyncThreadPoolSize;
    private final int asyncQueueCapacity;
    private final boolean enableAsyncCompletions;

    // Security configuration
    private final boolean enableAuthentication;
    private final String authToken;
    private final int maxConnections;
    private final int rateLimitPerMinute;
    private final boolean enableRequestValidation;

    // Authentication method selection
    private final String primaryAuthMethod; // oauth2.1, openhab_users, api_key, jwt
    private final String fallbackAuthMethod; // openhab_users, api_key, jwt, none
    private final boolean enableFallbackAuth;

    // OAuth 2.1 configuration
    private final String oauthIssuerUrl;
    private final String oauthClientId;
    private final String oauthClientSecret;
    private final String oauthRedirectUri;
    private final boolean oauthPkceEnabled;

    // openHAB users authentication
    private final String openhabUsersFile; // Default: OpenHAB.getConfigFolder() + "/users.properties"
    private final boolean openhabUsersEnabled;

    // API key authentication
    private final String apiKeyHeader;
    private final String apiKeyValue;
    private final boolean apiKeyEnabled;

    // JWT authentication
    private final String jwtSecret;
    private final String jwtIssuer;
    private final int jwtExpirationMinutes;
    private final boolean jwtEnabled;

    // Monitoring and metrics
    private final boolean enableMetrics;
    private final boolean enableHealthChecks;
    private final int healthCheckInterval;
    private final boolean enablePerformanceMonitoring;

    // Production settings
    private final boolean productionMode;
    private final int requestTimeout;
    private final int connectionTimeout;
    private final boolean enableGracefulShutdown;
    private final int shutdownTimeout;

    private final Map<String, Object> transportOptions;
    private final Map<String, Object> serverOptions;

    /**
     * Private constructor for builder pattern.
     */
    public ServerConfiguration(ServerConfigurationBuilder builder) {
        this.serverId = builder.getName() != null ? builder.getName() : "default-server";
        this.serverName = builder.getName() != null ? builder.getName() : "Default Server";
        this.serverVersion = "1.0.0"; // Default value since unified builder doesn't have version
        this.transportType = TransportType.HTTP; // Default value since unified builder doesn't have transport type
        this.baseUrl = "http://" + builder.getHost() + ":" + builder.getPort();

        // Default values for missing fields
        this.messageEndpoint = "/mcp";
        this.sseEndpoint = "/sse";
        this.enableSse = false;
        this.enableTools = true;
        this.enableResources = true;
        this.enablePrompts = true;
        this.enableLogging = true;
        this.enableAsyncServer = false;
        this.enableAsyncTools = false;
        this.asyncThreadPoolSize = 10;
        this.asyncQueueCapacity = 100;
        this.enableAsyncCompletions = false;
        this.enableAuthentication = false;
        this.authToken = null;
        this.maxConnections = 100;
        this.rateLimitPerMinute = 60;
        this.enableRequestValidation = true;
        this.primaryAuthMethod = "none";
        this.fallbackAuthMethod = "none";
        this.enableFallbackAuth = false;
        this.oauthIssuerUrl = null;
        this.oauthClientId = null;
        this.oauthClientSecret = null;
        this.oauthRedirectUri = null;
        this.oauthPkceEnabled = false;
        this.openhabUsersFile = null;
        this.openhabUsersEnabled = false;
        this.apiKeyHeader = "X-API-Key";
        this.apiKeyValue = null;
        this.apiKeyEnabled = false;
        this.jwtSecret = null;
        this.jwtIssuer = null;
        this.jwtExpirationMinutes = 60;
        this.jwtEnabled = false;
        this.enableMetrics = true;
        this.enableHealthChecks = true;
        this.healthCheckInterval = 30;
        this.enablePerformanceMonitoring = false;
        this.productionMode = false;
        this.requestTimeout = 30000;
        this.connectionTimeout = 10000;
        this.enableGracefulShutdown = true;
        this.shutdownTimeout = 5000;
        this.transportOptions = builder.getSettings() != null ? Map.copyOf(builder.getSettings()) : Map.of();
        this.serverOptions = Map.of();
    }

    /**
     * Create a new builder for server configuration.
     * 
     * @return New builder instance
     */
    public static ServerConfigurationBuilder builder() {
        return new ServerConfigurationBuilder();
    }

    /**
     * Get the server ID.
     * 
     * @return Server identifier
     */
    public String getServerId() {
        return serverId;
    }

    /**
     * Get the server name.
     * 
     * @return Server name
     */
    public String getServerName() {
        return serverName;
    }

    /**
     * Get the server version.
     * 
     * @return Server version
     */
    public String getServerVersion() {
        return serverVersion;
    }

    /**
     * Get the transport type.
     * 
     * @return Transport type
     */
    public TransportType getTransportType() {
        return transportType;
    }

    /**
     * Get the base URL for SSE transport.
     * 
     * @return Base URL
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Get the message endpoint for SSE transport.
     * 
     * @return Message endpoint
     */
    public String getMessageEndpoint() {
        return messageEndpoint;
    }

    /**
     * Get the SSE endpoint for SSE transport.
     * 
     * @return SSE endpoint
     */
    public String getSseEndpoint() {
        return sseEndpoint;
    }

    /**
     * Get the SSE path for SSE transport.
     * 
     * @return SSE path
     */
    public String getSsePath() {
        return sseEndpoint;
    }

    /**
     * Get the SSE port for SSE transport.
     * 
     * @return SSE port
     */
    public int getSsePort() {
        // Extract port from baseUrl or use default
        if (baseUrl != null && baseUrl.contains(":")) {
            try {
                String portPart = baseUrl.substring(baseUrl.lastIndexOf(":") + 1);
                if (portPart.contains("/")) {
                    portPart = portPart.substring(0, portPart.indexOf("/"));
                }
                return Integer.parseInt(portPart);
            } catch (NumberFormatException e) {
                // Fall back to default
            }
        }
        return 8080; // Default port
    }

    /**
     * Check if SSE transport is enabled.
     * 
     * @return true if SSE is enabled
     */
    public boolean isEnableSse() {
        return enableSse;
    }

    /**
     * Check if tools are enabled.
     * 
     * @return true if tools are enabled
     */
    public boolean isEnableTools() {
        return enableTools;
    }

    /**
     * Check if resources are enabled.
     * 
     * @return true if resources are enabled
     */
    public boolean isEnableResources() {
        return enableResources;
    }

    /**
     * Check if prompts are enabled.
     * 
     * @return true if prompts are enabled
     */
    public boolean isEnablePrompts() {
        return enablePrompts;
    }

    /**
     * Check if logging is enabled.
     * 
     * @return true if logging is enabled
     */
    public boolean isEnableLogging() {
        return enableLogging;
    }

    public boolean isEnableAsyncServer() {
        return enableAsyncServer;
    }

    public boolean isEnableAsyncTools() {
        return enableAsyncTools;
    }

    public int getAsyncThreadPoolSize() {
        return asyncThreadPoolSize;
    }

    public int getAsyncQueueCapacity() {
        return asyncQueueCapacity;
    }

    public boolean isEnableAsyncCompletions() {
        return enableAsyncCompletions;
    }

    /**
     * Check if async server is enabled.
     * 
     * @return true if async server is enabled
     */
    public boolean isAsyncEnabled() {
        return enableAsyncServer;
    }

    /**
     * Get transport-specific options.
     * 
     * @return Transport options map
     */
    public Map<String, Object> getTransportOptions() {
        return transportOptions;
    }

    /**
     * Get server-specific options.
     * 
     * @return Server options map
     */
    public Map<String, Object> getServerOptions() {
        return serverOptions;
    }

    // Security configuration getters

    /**
     * Check if authentication is enabled.
     * 
     * @return true if authentication is enabled
     */
    public boolean isEnableAuthentication() {
        return enableAuthentication;
    }

    /**
     * Get the authentication token.
     * 
     * @return Authentication token
     */
    public String getAuthToken() {
        return authToken;
    }

    /**
     * Get the maximum number of connections.
     * 
     * @return Maximum connections
     */
    public int getMaxConnections() {
        return maxConnections;
    }

    /**
     * Get the rate limit per minute.
     * 
     * @return Rate limit per minute
     */
    public int getRateLimitPerMinute() {
        return rateLimitPerMinute;
    }

    /**
     * Check if request validation is enabled.
     * 
     * @return true if request validation is enabled
     */
    public boolean isEnableRequestValidation() {
        return enableRequestValidation;
    }

    // Authentication method selection getters

    /**
     * Get the primary authentication method.
     * 
     * @return Primary authentication method
     */
    public String getPrimaryAuthMethod() {
        return primaryAuthMethod;
    }

    /**
     * Get the fallback authentication method.
     * 
     * @return Fallback authentication method
     */
    public String getFallbackAuthMethod() {
        return fallbackAuthMethod;
    }

    /**
     * Check if fallback authentication is enabled.
     * 
     * @return true if fallback authentication is enabled
     */
    public boolean isEnableFallbackAuth() {
        return enableFallbackAuth;
    }

    // OAuth 2.1 configuration getters

    /**
     * Get the OAuth 2.1 issuer URL.
     * 
     * @return OAuth 2.1 issuer URL
     */
    public String getOauthIssuerUrl() {
        return oauthIssuerUrl;
    }

    /**
     * Get the OAuth 2.1 client ID.
     * 
     * @return OAuth 2.1 client ID
     */
    public String getOauthClientId() {
        return oauthClientId;
    }

    /**
     * Get the OAuth 2.1 client secret.
     * 
     * @return OAuth 2.1 client secret
     */
    public String getOauthClientSecret() {
        return oauthClientSecret;
    }

    /**
     * Get the OAuth 2.1 redirect URI.
     * 
     * @return OAuth 2.1 redirect URI
     */
    public String getOauthRedirectUri() {
        return oauthRedirectUri;
    }

    /**
     * Check if PKCE is enabled for OAuth 2.1.
     * 
     * @return true if PKCE is enabled
     */
    public boolean isOauthPkceEnabled() {
        return oauthPkceEnabled;
    }

    // openHAB users authentication getters

    /**
     * Get the path to the OpenHAB users file.
     * 
     * @return Path to OpenHAB users file
     */
    public String getOpenhabUsersFile() {
        return openhabUsersFile;
    }

    /**
     * Check if OpenHAB users authentication is enabled.
     * 
     * @return true if OpenHAB users authentication is enabled
     */
    public boolean isOpenhabUsersEnabled() {
        return openhabUsersEnabled;
    }

    // API key authentication getters

    /**
     * Get the header name for API key authentication.
     * 
     * @return Header name for API key
     */
    public String getApiKeyHeader() {
        return apiKeyHeader;
    }

    /**
     * Get the API key value.
     * 
     * @return API key value
     */
    public String getApiKeyValue() {
        return apiKeyValue;
    }

    /**
     * Check if API key authentication is enabled.
     * 
     * @return true if API key authentication is enabled
     */
    public boolean isApiKeyEnabled() {
        return apiKeyEnabled;
    }

    // JWT authentication getters

    /**
     * Get the JWT secret.
     * 
     * @return JWT secret
     */
    public String getJwtSecret() {
        return jwtSecret;
    }

    /**
     * Get the JWT issuer.
     * 
     * @return JWT issuer
     */
    public String getJwtIssuer() {
        return jwtIssuer;
    }

    /**
     * Get the JWT expiration in minutes.
     * 
     * @return JWT expiration in minutes
     */
    public int getJwtExpirationMinutes() {
        return jwtExpirationMinutes;
    }

    /**
     * Check if JWT authentication is enabled.
     * 
     * @return true if JWT authentication is enabled
     */
    public boolean isJwtEnabled() {
        return jwtEnabled;
    }

    // Monitoring and metrics getters

    /**
     * Check if metrics are enabled.
     * 
     * @return true if metrics are enabled
     */
    public boolean isEnableMetrics() {
        return enableMetrics;
    }

    /**
     * Check if health checks are enabled.
     * 
     * @return true if health checks are enabled
     */
    public boolean isEnableHealthChecks() {
        return enableHealthChecks;
    }

    /**
     * Get the health check interval in milliseconds.
     * 
     * @return Health check interval
     */
    public int getHealthCheckInterval() {
        return healthCheckInterval;
    }

    /**
     * Check if performance monitoring is enabled.
     * 
     * @return true if performance monitoring is enabled
     */
    public boolean isEnablePerformanceMonitoring() {
        return enablePerformanceMonitoring;
    }

    // Production settings getters

    /**
     * Check if production mode is enabled.
     * 
     * @return true if production mode is enabled
     */
    public boolean isProductionMode() {
        return productionMode;
    }

    /**
     * Get the request timeout in milliseconds.
     * 
     * @return Request timeout
     */
    public int getRequestTimeout() {
        return requestTimeout;
    }

    /**
     * Get the connection timeout in milliseconds.
     * 
     * @return Connection timeout
     */
    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * Check if graceful shutdown is enabled.
     * 
     * @return true if graceful shutdown is enabled
     */
    public boolean isEnableGracefulShutdown() {
        return enableGracefulShutdown;
    }

    /**
     * Get the shutdown timeout in milliseconds.
     * 
     * @return Shutdown timeout
     */
    public int getShutdownTimeout() {
        return shutdownTimeout;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ServerConfiguration that = (ServerConfiguration) o;
        return Objects.equals(serverId, that.serverId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverId);
    }

    @Override
    public String toString() {
        return "MCPServerConfiguration{" + "serverId='" + serverId + '\'' + ", serverName='" + serverName + '\''
                + ", serverVersion='" + serverVersion + '\'' + ", transportType=" + transportType + ", baseUrl='"
                + baseUrl + '\'' + ", messageEndpoint='" + messageEndpoint + '\'' + ", sseEndpoint='" + sseEndpoint
                + '\'' + ", enableSse=" + enableSse

                + ", enableTools=" + enableTools + ", enableResources=" + enableResources + ", enablePrompts="
                + enablePrompts + ", enableLogging=" + enableLogging + '}';
    }
}
