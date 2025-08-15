package org.openhab.core.ai.tool.server;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Builder for {@link ServerConfiguration}.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class ServerConfigurationBuilder {
    String serverId = "default-server-id";
    String serverName = "openHAB MCP Server";
    String serverVersion = "1.0.0";
    org.openhab.core.ai.tool.server.api.TransportType transportType = org.openhab.core.ai.tool.server.api.TransportType.STDIO;
    String baseUrl = "http://localhost:8080";
    String messageEndpoint = "/mcp/message";
    String sseEndpoint = "/mcp/events";
    boolean enableSse = true;

    boolean enableTools = true;
    boolean enableResources = true;
    boolean enablePrompts = true;
    boolean enableLogging = true;

    boolean enableAsyncServer = false;
    boolean enableAsyncTools = false;
    int asyncThreadPoolSize = 10;
    int asyncQueueCapacity = 1000;
    boolean enableAsyncCompletions = false;

    boolean enableAuthentication = false;
    String authToken = "";
    int maxConnections = 100;
    int rateLimitPerMinute = 1000;
    boolean enableRequestValidation = true;

    String primaryAuthMethod = "oauth2.1";
    String fallbackAuthMethod = "openhab_users";
    boolean enableFallbackAuth = true;

    String oauthIssuerUrl = "";
    String oauthClientId = "";
    String oauthClientSecret = "";
    String oauthRedirectUri = "";
    boolean oauthPkceEnabled = true;

    String openhabUsersFile = "";
    boolean openhabUsersEnabled = true;

    String apiKeyHeader = "X-API-Key";
    String apiKeyValue = "";
    boolean apiKeyEnabled = false;

    String jwtSecret = "";
    String jwtIssuer = "openhab-mcp";
    int jwtExpirationMinutes = 60;
    boolean jwtEnabled = false;

    boolean enableMetrics = true;
    boolean enableHealthChecks = true;
    int healthCheckInterval = 30000;
    boolean enablePerformanceMonitoring = true;

    boolean productionMode = false;
    int requestTimeout = 30000;
    int connectionTimeout = 10000;
    boolean enableGracefulShutdown = true;
    int shutdownTimeout = 30000;

    Map<String, Object> transportOptions = new HashMap<>();
    Map<String, Object> serverOptions = new HashMap<>();

    public ServerConfigurationBuilder serverId(String serverId) {
        this.serverId = serverId;
        return this;
    }

    public ServerConfigurationBuilder serverName(String serverName) {
        this.serverName = serverName;
        return this;
    }

    public ServerConfigurationBuilder serverVersion(String serverVersion) {
        this.serverVersion = serverVersion;
        return this;
    }

    public ServerConfigurationBuilder transportType(org.openhab.core.ai.tool.server.api.TransportType transportType) {
        this.transportType = transportType;
        return this;
    }

    public ServerConfigurationBuilder baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public ServerConfigurationBuilder messageEndpoint(String messageEndpoint) {
        this.messageEndpoint = messageEndpoint;
        return this;
    }

    public ServerConfigurationBuilder sseEndpoint(String sseEndpoint) {
        this.sseEndpoint = sseEndpoint;
        return this;
    }

    public ServerConfigurationBuilder enableSse(boolean enableSse) {
        this.enableSse = enableSse;
        return this;
    }

    public ServerConfigurationBuilder enableTools(boolean enableTools) {
        this.enableTools = enableTools;
        return this;
    }

    public ServerConfigurationBuilder enableResources(boolean enableResources) {
        this.enableResources = enableResources;
        return this;
    }

    public ServerConfigurationBuilder enablePrompts(boolean enablePrompts) {
        this.enablePrompts = enablePrompts;
        return this;
    }

    public ServerConfigurationBuilder enableLogging(boolean enableLogging) {
        this.enableLogging = enableLogging;
        return this;
    }

    public ServerConfigurationBuilder enableAsyncServer(boolean enableAsyncServer) {
        this.enableAsyncServer = enableAsyncServer;
        return this;
    }

    public ServerConfigurationBuilder enableAsyncTools(boolean enableAsyncTools) {
        this.enableAsyncTools = enableAsyncTools;
        return this;
    }

    public ServerConfigurationBuilder asyncThreadPoolSize(int asyncThreadPoolSize) {
        this.asyncThreadPoolSize = asyncThreadPoolSize;
        return this;
    }

    public ServerConfigurationBuilder asyncQueueCapacity(int asyncQueueCapacity) {
        this.asyncQueueCapacity = asyncQueueCapacity;
        return this;
    }

    public ServerConfigurationBuilder enableAsyncCompletions(boolean enableAsyncCompletions) {
        this.enableAsyncCompletions = enableAsyncCompletions;
        return this;
    }

    public ServerConfigurationBuilder transportOption(String key, Object value) {
        this.transportOptions.put(key, value);
        return this;
    }

    public ServerConfigurationBuilder transportOptions(Map<String, Object> transportOptions) {
        this.transportOptions = new HashMap<>(transportOptions);
        return this;
    }

    public ServerConfigurationBuilder serverOption(String key, Object value) {
        this.serverOptions.put(key, value);
        return this;
    }

    public ServerConfigurationBuilder serverOptions(Map<String, Object> serverOptions) {
        this.serverOptions = new HashMap<>(serverOptions);
        return this;
    }

    public ServerConfigurationBuilder enableAuthentication(boolean enableAuthentication) {
        this.enableAuthentication = enableAuthentication;
        return this;
    }

    public ServerConfigurationBuilder authToken(String authToken) {
        this.authToken = authToken;
        return this;
    }

    public ServerConfigurationBuilder maxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
        return this;
    }

    public ServerConfigurationBuilder rateLimitPerMinute(int rateLimitPerMinute) {
        this.rateLimitPerMinute = rateLimitPerMinute;
        return this;
    }

    public ServerConfigurationBuilder enableRequestValidation(boolean enableRequestValidation) {
        this.enableRequestValidation = enableRequestValidation;
        return this;
    }

    public ServerConfigurationBuilder primaryAuthMethod(String primaryAuthMethod) {
        this.primaryAuthMethod = primaryAuthMethod;
        return this;
    }

    public ServerConfigurationBuilder fallbackAuthMethod(String fallbackAuthMethod) {
        this.fallbackAuthMethod = fallbackAuthMethod;
        return this;
    }

    public ServerConfigurationBuilder enableFallbackAuth(boolean enableFallbackAuth) {
        this.enableFallbackAuth = enableFallbackAuth;
        return this;
    }

    public ServerConfigurationBuilder oauthIssuerUrl(String oauthIssuerUrl) {
        this.oauthIssuerUrl = oauthIssuerUrl;
        return this;
    }

    public ServerConfigurationBuilder oauthClientId(String oauthClientId) {
        this.oauthClientId = oauthClientId;
        return this;
    }

    public ServerConfigurationBuilder oauthClientSecret(String oauthClientSecret) {
        this.oauthClientSecret = oauthClientSecret;
        return this;
    }

    public ServerConfigurationBuilder oauthRedirectUri(String oauthRedirectUri) {
        this.oauthRedirectUri = oauthRedirectUri;
        return this;
    }

    public ServerConfigurationBuilder oauthPkceEnabled(boolean oauthPkceEnabled) {
        this.oauthPkceEnabled = oauthPkceEnabled;
        return this;
    }

    public ServerConfigurationBuilder openhabUsersFile(String openhabUsersFile) {
        this.openhabUsersFile = openhabUsersFile;
        return this;
    }

    public ServerConfigurationBuilder openhabUsersEnabled(boolean openhabUsersEnabled) {
        this.openhabUsersEnabled = openhabUsersEnabled;
        return this;
    }

    public ServerConfigurationBuilder apiKeyHeader(String apiKeyHeader) {
        this.apiKeyHeader = apiKeyHeader;
        return this;
    }

    public ServerConfigurationBuilder apiKeyValue(String apiKeyValue) {
        this.apiKeyValue = apiKeyValue;
        return this;
    }

    public ServerConfigurationBuilder apiKeyEnabled(boolean apiKeyEnabled) {
        this.apiKeyEnabled = apiKeyEnabled;
        return this;
    }

    public ServerConfigurationBuilder jwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
        return this;
    }

    public ServerConfigurationBuilder jwtIssuer(String jwtIssuer) {
        this.jwtIssuer = jwtIssuer;
        return this;
    }

    public ServerConfigurationBuilder jwtExpirationMinutes(int jwtExpirationMinutes) {
        this.jwtExpirationMinutes = jwtExpirationMinutes;
        return this;
    }

    public ServerConfigurationBuilder jwtEnabled(boolean jwtEnabled) {
        this.jwtEnabled = jwtEnabled;
        return this;
    }

    public ServerConfigurationBuilder enableMetrics(boolean enableMetrics) {
        this.enableMetrics = enableMetrics;
        return this;
    }

    public ServerConfigurationBuilder enableHealthChecks(boolean enableHealthChecks) {
        this.enableHealthChecks = enableHealthChecks;
        return this;
    }

    public ServerConfigurationBuilder healthCheckInterval(int healthCheckInterval) {
        this.healthCheckInterval = healthCheckInterval;
        return this;
    }

    public ServerConfigurationBuilder enablePerformanceMonitoring(boolean enablePerformanceMonitoring) {
        this.enablePerformanceMonitoring = enablePerformanceMonitoring;
        return this;
    }

    public ServerConfigurationBuilder productionMode(boolean productionMode) {
        this.productionMode = productionMode;
        return this;
    }

    public ServerConfigurationBuilder requestTimeout(int requestTimeout) {
        this.requestTimeout = requestTimeout;
        return this;
    }

    public ServerConfigurationBuilder connectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    public ServerConfigurationBuilder enableGracefulShutdown(boolean enableGracefulShutdown) {
        this.enableGracefulShutdown = enableGracefulShutdown;
        return this;
    }

    public ServerConfigurationBuilder shutdownTimeout(int shutdownTimeout) {
        this.shutdownTimeout = shutdownTimeout;
        return this;
    }

    public ServerConfiguration build() {
        if (serverId == null || serverId.trim().isEmpty()) {
            throw new IllegalArgumentException("Server ID is required");
        }
        if (serverName == null || serverName.trim().isEmpty()) {
            throw new IllegalArgumentException("Server name is required");
        }
        if (serverVersion == null || serverVersion.trim().isEmpty()) {
            throw new IllegalArgumentException("Server version is required");
        }
        if (transportType == null) {
            throw new IllegalArgumentException("Transport type is required");
        }
        return new ServerConfiguration(this);
    }
}
