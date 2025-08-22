package org.openhab.core.ai.tool.server;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.transport.TransportType;

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
    private ServerConfiguration(Builder builder) {
        this.serverId = builder.serverId;
        this.serverName = builder.serverName;
        this.serverVersion = builder.serverVersion;
        this.transportType = builder.transportType;
        this.baseUrl = builder.baseUrl;
        this.messageEndpoint = builder.messageEndpoint;
        this.sseEndpoint = builder.sseEndpoint;
        this.enableSse = builder.enableSse;
        this.enableTools = builder.enableTools;
        this.enableResources = builder.enableResources;
        this.enablePrompts = builder.enablePrompts;
        this.enableLogging = builder.enableLogging;
        this.enableAsyncServer = builder.enableAsyncServer;
        this.enableAsyncTools = builder.enableAsyncTools;
        this.asyncThreadPoolSize = builder.asyncThreadPoolSize;
        this.asyncQueueCapacity = builder.asyncQueueCapacity;
        this.enableAsyncCompletions = builder.enableAsyncCompletions;
        this.enableAuthentication = builder.enableAuthentication;
        this.authToken = builder.authToken;
        this.maxConnections = builder.maxConnections;
        this.rateLimitPerMinute = builder.rateLimitPerMinute;
        this.enableRequestValidation = builder.enableRequestValidation;
        this.primaryAuthMethod = builder.primaryAuthMethod;
        this.fallbackAuthMethod = builder.fallbackAuthMethod;
        this.enableFallbackAuth = builder.enableFallbackAuth;
        this.oauthIssuerUrl = builder.oauthIssuerUrl;
        this.oauthClientId = builder.oauthClientId;
        this.oauthClientSecret = builder.oauthClientSecret;
        this.oauthRedirectUri = builder.oauthRedirectUri;
        this.oauthPkceEnabled = builder.oauthPkceEnabled;
        this.openhabUsersFile = builder.openhabUsersFile;
        this.openhabUsersEnabled = builder.openhabUsersEnabled;
        this.apiKeyHeader = builder.apiKeyHeader;
        this.apiKeyValue = builder.apiKeyValue;
        this.apiKeyEnabled = builder.apiKeyEnabled;
        this.jwtSecret = builder.jwtSecret;
        this.jwtIssuer = builder.jwtIssuer;
        this.jwtExpirationMinutes = builder.jwtExpirationMinutes;
        this.jwtEnabled = builder.jwtEnabled;
        this.enableMetrics = builder.enableMetrics;
        this.enableHealthChecks = builder.enableHealthChecks;
        this.healthCheckInterval = builder.healthCheckInterval;
        this.enablePerformanceMonitoring = builder.enablePerformanceMonitoring;
        this.productionMode = builder.productionMode;
        this.requestTimeout = builder.requestTimeout;
        this.connectionTimeout = builder.connectionTimeout;
        this.enableGracefulShutdown = builder.enableGracefulShutdown;
        this.shutdownTimeout = builder.shutdownTimeout;
        this.transportOptions = Map.copyOf(builder.transportOptions);
        this.serverOptions = Map.copyOf(builder.serverOptions);
    }

    /**
     * Create a new builder for server configuration.
     * 
     * @return New builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create a new builder from this configuration.
     * 
     * @return a new builder with current values
     */
    public Builder toBuilder() {
        return new Builder(this);
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

    /**
     * Builder for ServerConfiguration.
     * 
     * This builder provides a fluent API for creating ServerConfiguration instances.
     * The builder is not thread-safe and should be used for single-threaded construction.
     * 
     * @author Karel Goderis - Initial Contribution
     * @since 1.0.0
     */
    public static final class Builder {
        private String serverId = "default-server";
        private String serverName = "Default Server";
        private String serverVersion = "1.0.0";
        private TransportType transportType = TransportType.HTTP;
        private String baseUrl = "http://localhost:8080";
        private String messageEndpoint = "/mcp";
        private String sseEndpoint = "/sse";
        private boolean enableSse = false;
        private boolean enableTools = true;
        private boolean enableResources = true;
        private boolean enablePrompts = true;
        private boolean enableLogging = true;
        private boolean enableAsyncServer = false;
        private boolean enableAsyncTools = false;
        private int asyncThreadPoolSize = 10;
        private int asyncQueueCapacity = 100;
        private boolean enableAsyncCompletions = false;
        private boolean enableAuthentication = false;
        private @Nullable String authToken;
        private int maxConnections = 100;
        private int rateLimitPerMinute = 60;
        private boolean enableRequestValidation = true;
        private String primaryAuthMethod = "none";
        private String fallbackAuthMethod = "none";
        private boolean enableFallbackAuth = false;
        private @Nullable String oauthIssuerUrl;
        private @Nullable String oauthClientId;
        private @Nullable String oauthClientSecret;
        private @Nullable String oauthRedirectUri;
        private boolean oauthPkceEnabled = false;
        private @Nullable String openhabUsersFile;
        private boolean openhabUsersEnabled = false;
        private String apiKeyHeader = "X-API-Key";
        private @Nullable String apiKeyValue;
        private boolean apiKeyEnabled = false;
        private @Nullable String jwtSecret;
        private @Nullable String jwtIssuer;
        private int jwtExpirationMinutes = 60;
        private boolean jwtEnabled = false;
        private boolean enableMetrics = true;
        private boolean enableHealthChecks = true;
        private int healthCheckInterval = 30;
        private boolean enablePerformanceMonitoring = false;
        private boolean productionMode = false;
        private int requestTimeout = 30000;
        private int connectionTimeout = 10000;
        private boolean enableGracefulShutdown = true;
        private int shutdownTimeout = 5000;
        private Map<String, Object> transportOptions = new HashMap<>();
        private Map<String, Object> serverOptions = new HashMap<>();

        /**
         * Create a new builder with default values.
         */
        public Builder() {
        }

        /**
         * Create a new builder from an existing ServerConfiguration.
         * 
         * @param source the source configuration to copy from
         */
        public Builder(ServerConfiguration source) {
            this.serverId = source.serverId;
            this.serverName = source.serverName;
            this.serverVersion = source.serverVersion;
            this.transportType = source.transportType;
            this.baseUrl = source.baseUrl;
            this.messageEndpoint = source.messageEndpoint;
            this.sseEndpoint = source.sseEndpoint;
            this.enableSse = source.enableSse;
            this.enableTools = source.enableTools;
            this.enableResources = source.enableResources;
            this.enablePrompts = source.enablePrompts;
            this.enableLogging = source.enableLogging;
            this.enableAsyncServer = source.enableAsyncServer;
            this.enableAsyncTools = source.enableAsyncTools;
            this.asyncThreadPoolSize = source.asyncThreadPoolSize;
            this.asyncQueueCapacity = source.asyncQueueCapacity;
            this.enableAsyncCompletions = source.enableAsyncCompletions;
            this.enableAuthentication = source.enableAuthentication;
            this.authToken = source.authToken;
            this.maxConnections = source.maxConnections;
            this.rateLimitPerMinute = source.rateLimitPerMinute;
            this.enableRequestValidation = source.enableRequestValidation;
            this.primaryAuthMethod = source.primaryAuthMethod;
            this.fallbackAuthMethod = source.fallbackAuthMethod;
            this.enableFallbackAuth = source.enableFallbackAuth;
            this.oauthIssuerUrl = source.oauthIssuerUrl;
            this.oauthClientId = source.oauthClientId;
            this.oauthClientSecret = source.oauthClientSecret;
            this.oauthRedirectUri = source.oauthRedirectUri;
            this.oauthPkceEnabled = source.oauthPkceEnabled;
            this.openhabUsersFile = source.openhabUsersFile;
            this.openhabUsersEnabled = source.openhabUsersEnabled;
            this.apiKeyHeader = source.apiKeyHeader;
            this.apiKeyValue = source.apiKeyValue;
            this.apiKeyEnabled = source.apiKeyEnabled;
            this.jwtSecret = source.jwtSecret;
            this.jwtIssuer = source.jwtIssuer;
            this.jwtExpirationMinutes = source.jwtExpirationMinutes;
            this.jwtEnabled = source.jwtEnabled;
            this.enableMetrics = source.enableMetrics;
            this.enableHealthChecks = source.enableHealthChecks;
            this.healthCheckInterval = source.healthCheckInterval;
            this.enablePerformanceMonitoring = source.enablePerformanceMonitoring;
            this.productionMode = source.productionMode;
            this.requestTimeout = source.requestTimeout;
            this.connectionTimeout = source.connectionTimeout;
            this.enableGracefulShutdown = source.enableGracefulShutdown;
            this.shutdownTimeout = source.shutdownTimeout;
            this.transportOptions = new HashMap<>(source.transportOptions);
            this.serverOptions = new HashMap<>(source.serverOptions);
        }

        /**
         * Set the server ID.
         * 
         * @param serverId the server ID (cannot be null or blank)
         * @return this builder
         */
        public Builder withServerId(String serverId) {
            this.serverId = Objects.requireNonNull(serverId, "serverId");
            return this;
        }

        /**
         * Set the server name.
         * 
         * @param serverName the server name (cannot be null or blank)
         * @return this builder
         */
        public Builder withServerName(String serverName) {
            this.serverName = Objects.requireNonNull(serverName, "serverName");
            return this;
        }

        /**
         * Set the server version.
         * 
         * @param serverVersion the server version (cannot be null or blank)
         * @return this builder
         */
        public Builder withServerVersion(String serverVersion) {
            this.serverVersion = Objects.requireNonNull(serverVersion, "serverVersion");
            return this;
        }

        /**
         * Set the transport type.
         * 
         * @param transportType the transport type (cannot be null)
         * @return this builder
         */
        public Builder withTransportType(TransportType transportType) {
            this.transportType = Objects.requireNonNull(transportType, "transportType");
            return this;
        }

        /**
         * Set the base URL.
         * 
         * @param baseUrl the base URL (cannot be null or blank)
         * @return this builder
         */
        public Builder withBaseUrl(String baseUrl) {
            this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl");
            return this;
        }

        /**
         * Set the message endpoint.
         * 
         * @param messageEndpoint the message endpoint (cannot be null or blank)
         * @return this builder
         */
        public Builder withMessageEndpoint(String messageEndpoint) {
            this.messageEndpoint = Objects.requireNonNull(messageEndpoint, "messageEndpoint");
            return this;
        }

        /**
         * Set the SSE endpoint.
         * 
         * @param sseEndpoint the SSE endpoint (cannot be null or blank)
         * @return this builder
         */
        public Builder withSseEndpoint(String sseEndpoint) {
            this.sseEndpoint = Objects.requireNonNull(sseEndpoint, "sseEndpoint");
            return this;
        }

        /**
         * Set whether SSE is enabled.
         * 
         * @param enableSse whether SSE is enabled
         * @return this builder
         */
        public Builder withEnableSse(boolean enableSse) {
            this.enableSse = enableSse;
            return this;
        }

        /**
         * Set whether tools are enabled.
         * 
         * @param enableTools whether tools are enabled
         * @return this builder
         */
        public Builder withEnableTools(boolean enableTools) {
            this.enableTools = enableTools;
            return this;
        }

        /**
         * Set whether resources are enabled.
         * 
         * @param enableResources whether resources are enabled
         * @return this builder
         */
        public Builder withEnableResources(boolean enableResources) {
            this.enableResources = enableResources;
            return this;
        }

        /**
         * Set whether prompts are enabled.
         * 
         * @param enablePrompts whether prompts are enabled
         * @return this builder
         */
        public Builder withEnablePrompts(boolean enablePrompts) {
            this.enablePrompts = enablePrompts;
            return this;
        }

        /**
         * Set whether logging is enabled.
         * 
         * @param enableLogging whether logging is enabled
         * @return this builder
         */
        public Builder withEnableLogging(boolean enableLogging) {
            this.enableLogging = enableLogging;
            return this;
        }

        /**
         * Set whether async server is enabled.
         * 
         * @param enableAsyncServer whether async server is enabled
         * @return this builder
         */
        public Builder withEnableAsyncServer(boolean enableAsyncServer) {
            this.enableAsyncServer = enableAsyncServer;
            return this;
        }

        /**
         * Set whether async tools are enabled.
         * 
         * @param enableAsyncTools whether async tools are enabled
         * @return this builder
         */
        public Builder withEnableAsyncTools(boolean enableAsyncTools) {
            this.enableAsyncTools = enableAsyncTools;
            return this;
        }

        /**
         * Set the async thread pool size.
         * 
         * @param asyncThreadPoolSize the async thread pool size (must be positive)
         * @return this builder
         */
        public Builder withAsyncThreadPoolSize(int asyncThreadPoolSize) {
            this.asyncThreadPoolSize = asyncThreadPoolSize;
            return this;
        }

        /**
         * Set the async queue capacity.
         * 
         * @param asyncQueueCapacity the async queue capacity (must be positive)
         * @return this builder
         */
        public Builder withAsyncQueueCapacity(int asyncQueueCapacity) {
            this.asyncQueueCapacity = asyncQueueCapacity;
            return this;
        }

        /**
         * Set whether async completions are enabled.
         * 
         * @param enableAsyncCompletions whether async completions are enabled
         * @return this builder
         */
        public Builder withEnableAsyncCompletions(boolean enableAsyncCompletions) {
            this.enableAsyncCompletions = enableAsyncCompletions;
            return this;
        }

        /**
         * Set whether authentication is enabled.
         * 
         * @param enableAuthentication whether authentication is enabled
         * @return this builder
         */
        public Builder withEnableAuthentication(boolean enableAuthentication) {
            this.enableAuthentication = enableAuthentication;
            return this;
        }

        /**
         * Set the auth token.
         * 
         * @param authToken the auth token (can be null)
         * @return this builder
         */
        public Builder withAuthToken(@Nullable String authToken) {
            this.authToken = authToken;
            return this;
        }

        /**
         * Set the maximum connections.
         * 
         * @param maxConnections the maximum connections (must be positive)
         * @return this builder
         */
        public Builder withMaxConnections(int maxConnections) {
            this.maxConnections = maxConnections;
            return this;
        }

        /**
         * Set the rate limit per minute.
         * 
         * @param rateLimitPerMinute the rate limit per minute (must be positive)
         * @return this builder
         */
        public Builder withRateLimitPerMinute(int rateLimitPerMinute) {
            this.rateLimitPerMinute = rateLimitPerMinute;
            return this;
        }

        /**
         * Set whether request validation is enabled.
         * 
         * @param enableRequestValidation whether request validation is enabled
         * @return this builder
         */
        public Builder withEnableRequestValidation(boolean enableRequestValidation) {
            this.enableRequestValidation = enableRequestValidation;
            return this;
        }

        /**
         * Set the primary authentication method.
         * 
         * @param primaryAuthMethod the primary authentication method (cannot be null or blank)
         * @return this builder
         */
        public Builder withPrimaryAuthMethod(String primaryAuthMethod) {
            this.primaryAuthMethod = Objects.requireNonNull(primaryAuthMethod, "primaryAuthMethod");
            return this;
        }

        /**
         * Set the fallback authentication method.
         * 
         * @param fallbackAuthMethod the fallback authentication method (cannot be null or blank)
         * @return this builder
         */
        public Builder withFallbackAuthMethod(String fallbackAuthMethod) {
            this.fallbackAuthMethod = Objects.requireNonNull(fallbackAuthMethod, "fallbackAuthMethod");
            return this;
        }

        /**
         * Set whether fallback authentication is enabled.
         * 
         * @param enableFallbackAuth whether fallback authentication is enabled
         * @return this builder
         */
        public Builder withEnableFallbackAuth(boolean enableFallbackAuth) {
            this.enableFallbackAuth = enableFallbackAuth;
            return this;
        }

        /**
         * Set the OAuth issuer URL.
         * 
         * @param oauthIssuerUrl the OAuth issuer URL (can be null)
         * @return this builder
         */
        public Builder withOauthIssuerUrl(@Nullable String oauthIssuerUrl) {
            this.oauthIssuerUrl = oauthIssuerUrl;
            return this;
        }

        /**
         * Set the OAuth client ID.
         * 
         * @param oauthClientId the OAuth client ID (can be null)
         * @return this builder
         */
        public Builder withOauthClientId(@Nullable String oauthClientId) {
            this.oauthClientId = oauthClientId;
            return this;
        }

        /**
         * Set the OAuth client secret.
         * 
         * @param oauthClientSecret the OAuth client secret (can be null)
         * @return this builder
         */
        public Builder withOauthClientSecret(@Nullable String oauthClientSecret) {
            this.oauthClientSecret = oauthClientSecret;
            return this;
        }

        /**
         * Set the OAuth redirect URI.
         * 
         * @param oauthRedirectUri the OAuth redirect URI (can be null)
         * @return this builder
         */
        public Builder withOauthRedirectUri(@Nullable String oauthRedirectUri) {
            this.oauthRedirectUri = oauthRedirectUri;
            return this;
        }

        /**
         * Set whether OAuth PKCE is enabled.
         * 
         * @param oauthPkceEnabled whether OAuth PKCE is enabled
         * @return this builder
         */
        public Builder withOauthPkceEnabled(boolean oauthPkceEnabled) {
            this.oauthPkceEnabled = oauthPkceEnabled;
            return this;
        }

        /**
         * Set the openHAB users file.
         * 
         * @param openhabUsersFile the openHAB users file (can be null)
         * @return this builder
         */
        public Builder withOpenhabUsersFile(@Nullable String openhabUsersFile) {
            this.openhabUsersFile = openhabUsersFile;
            return this;
        }

        /**
         * Set whether openHAB users authentication is enabled.
         * 
         * @param openhabUsersEnabled whether openHAB users authentication is enabled
         * @return this builder
         */
        public Builder withOpenhabUsersEnabled(boolean openhabUsersEnabled) {
            this.openhabUsersEnabled = openhabUsersEnabled;
            return this;
        }

        /**
         * Set the API key header.
         * 
         * @param apiKeyHeader the API key header (cannot be null or blank)
         * @return this builder
         */
        public Builder withApiKeyHeader(String apiKeyHeader) {
            this.apiKeyHeader = Objects.requireNonNull(apiKeyHeader, "apiKeyHeader");
            return this;
        }

        /**
         * Set the API key value.
         * 
         * @param apiKeyValue the API key value (can be null)
         * @return this builder
         */
        public Builder withApiKeyValue(@Nullable String apiKeyValue) {
            this.apiKeyValue = apiKeyValue;
            return this;
        }

        /**
         * Set whether API key authentication is enabled.
         * 
         * @param apiKeyEnabled whether API key authentication is enabled
         * @return this builder
         */
        public Builder withApiKeyEnabled(boolean apiKeyEnabled) {
            this.apiKeyEnabled = apiKeyEnabled;
            return this;
        }

        /**
         * Set the JWT secret.
         * 
         * @param jwtSecret the JWT secret (can be null)
         * @return this builder
         */
        public Builder withJwtSecret(@Nullable String jwtSecret) {
            this.jwtSecret = jwtSecret;
            return this;
        }

        /**
         * Set the JWT issuer.
         * 
         * @param jwtIssuer the JWT issuer (can be null)
         * @return this builder
         */
        public Builder withJwtIssuer(@Nullable String jwtIssuer) {
            this.jwtIssuer = jwtIssuer;
            return this;
        }

        /**
         * Set the JWT expiration minutes.
         * 
         * @param jwtExpirationMinutes the JWT expiration minutes (must be positive)
         * @return this builder
         */
        public Builder withJwtExpirationMinutes(int jwtExpirationMinutes) {
            this.jwtExpirationMinutes = jwtExpirationMinutes;
            return this;
        }

        /**
         * Set whether JWT authentication is enabled.
         * 
         * @param jwtEnabled whether JWT authentication is enabled
         * @return this builder
         */
        public Builder withJwtEnabled(boolean jwtEnabled) {
            this.jwtEnabled = jwtEnabled;
            return this;
        }

        /**
         * Set whether metrics are enabled.
         * 
         * @param enableMetrics whether metrics are enabled
         * @return this builder
         */
        public Builder withEnableMetrics(boolean enableMetrics) {
            this.enableMetrics = enableMetrics;
            return this;
        }

        /**
         * Set whether health checks are enabled.
         * 
         * @param enableHealthChecks whether health checks are enabled
         * @return this builder
         */
        public Builder withEnableHealthChecks(boolean enableHealthChecks) {
            this.enableHealthChecks = enableHealthChecks;
            return this;
        }

        /**
         * Set the health check interval.
         * 
         * @param healthCheckInterval the health check interval (must be positive)
         * @return this builder
         */
        public Builder withHealthCheckInterval(int healthCheckInterval) {
            this.healthCheckInterval = healthCheckInterval;
            return this;
        }

        /**
         * Set whether performance monitoring is enabled.
         * 
         * @param enablePerformanceMonitoring whether performance monitoring is enabled
         * @return this builder
         */
        public Builder withEnablePerformanceMonitoring(boolean enablePerformanceMonitoring) {
            this.enablePerformanceMonitoring = enablePerformanceMonitoring;
            return this;
        }

        /**
         * Set whether production mode is enabled.
         * 
         * @param productionMode whether production mode is enabled
         * @return this builder
         */
        public Builder withProductionMode(boolean productionMode) {
            this.productionMode = productionMode;
            return this;
        }

        /**
         * Set the request timeout.
         * 
         * @param requestTimeout the request timeout in milliseconds (must be positive)
         * @return this builder
         */
        public Builder withRequestTimeout(int requestTimeout) {
            this.requestTimeout = requestTimeout;
            return this;
        }

        /**
         * Set the connection timeout.
         * 
         * @param connectionTimeout the connection timeout in milliseconds (must be positive)
         * @return this builder
         */
        public Builder withConnectionTimeout(int connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
            return this;
        }

        /**
         * Set whether graceful shutdown is enabled.
         * 
         * @param enableGracefulShutdown whether graceful shutdown is enabled
         * @return this builder
         */
        public Builder withEnableGracefulShutdown(boolean enableGracefulShutdown) {
            this.enableGracefulShutdown = enableGracefulShutdown;
            return this;
        }

        /**
         * Set the shutdown timeout.
         * 
         * @param shutdownTimeout the shutdown timeout in milliseconds (must be positive)
         * @return this builder
         */
        public Builder withShutdownTimeout(int shutdownTimeout) {
            this.shutdownTimeout = shutdownTimeout;
            return this;
        }

        /**
         * Add a transport option.
         * 
         * @param key the option key (cannot be null)
         * @param value the option value (cannot be null)
         * @return this builder
         */
        public Builder withTransportOption(String key, Object value) {
            this.transportOptions.put(Objects.requireNonNull(key, "key"), Objects.requireNonNull(value, "value"));
            return this;
        }

        /**
         * Set transport options.
         * 
         * @param transportOptions the transport options map (cannot be null)
         * @return this builder
         */
        public Builder withTransportOptions(Map<String, Object> transportOptions) {
            this.transportOptions.clear();
            this.transportOptions.putAll(Objects.requireNonNull(transportOptions, "transportOptions"));
            return this;
        }

        /**
         * Add a server option.
         * 
         * @param key the option key (cannot be null)
         * @param value the option value (cannot be null)
         * @return this builder
         */
        public Builder withServerOption(String key, Object value) {
            this.serverOptions.put(Objects.requireNonNull(key, "key"), Objects.requireNonNull(value, "value"));
            return this;
        }

        /**
         * Set server options.
         * 
         * @param serverOptions the server options map (cannot be null)
         * @return this builder
         */
        public Builder withServerOptions(Map<String, Object> serverOptions) {
            this.serverOptions.clear();
            this.serverOptions.putAll(Objects.requireNonNull(serverOptions, "serverOptions"));
            return this;
        }

        /**
         * Build the ServerConfiguration instance.
         * 
         * @return the configured ServerConfiguration
         * @throws IllegalArgumentException if validation fails
         */
        public ServerConfiguration build() {
            if (serverId.isBlank()) {
                throw new IllegalArgumentException("serverId must not be blank");
            }
            if (serverName.isBlank()) {
                throw new IllegalArgumentException("serverName must not be blank");
            }
            if (serverVersion.isBlank()) {
                throw new IllegalArgumentException("serverVersion must not be blank");
            }
            if (baseUrl.isBlank()) {
                throw new IllegalArgumentException("baseUrl must not be blank");
            }
            if (messageEndpoint.isBlank()) {
                throw new IllegalArgumentException("messageEndpoint must not be blank");
            }
            if (sseEndpoint.isBlank()) {
                throw new IllegalArgumentException("sseEndpoint must not be blank");
            }
            if (maxConnections <= 0) {
                throw new IllegalArgumentException("maxConnections must be positive");
            }
            if (rateLimitPerMinute <= 0) {
                throw new IllegalArgumentException("rateLimitPerMinute must be positive");
            }
            if (primaryAuthMethod.isBlank()) {
                throw new IllegalArgumentException("primaryAuthMethod must not be blank");
            }
            if (fallbackAuthMethod.isBlank()) {
                throw new IllegalArgumentException("fallbackAuthMethod must not be blank");
            }
            if (apiKeyHeader.isBlank()) {
                throw new IllegalArgumentException("apiKeyHeader must not be blank");
            }
            if (jwtExpirationMinutes <= 0) {
                throw new IllegalArgumentException("jwtExpirationMinutes must be positive");
            }
            if (healthCheckInterval <= 0) {
                throw new IllegalArgumentException("healthCheckInterval must be positive");
            }
            if (requestTimeout <= 0) {
                throw new IllegalArgumentException("requestTimeout must be positive");
            }
            if (connectionTimeout <= 0) {
                throw new IllegalArgumentException("connectionTimeout must be positive");
            }
            if (shutdownTimeout <= 0) {
                throw new IllegalArgumentException("shutdownTimeout must be positive");
            }
            if (asyncThreadPoolSize <= 0) {
                throw new IllegalArgumentException("asyncThreadPoolSize must be positive");
            }
            if (asyncQueueCapacity <= 0) {
                throw new IllegalArgumentException("asyncQueueCapacity must be positive");
            }
            return new ServerConfiguration(this);
        }
    }
}
