package org.openhab.core.ai.tool.server;

import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.builder.ServerConfigurationBuilder;
import org.openhab.core.ai.common.configuration.ServerConfiguration;
import org.openhab.core.ai.common.transport.TransportType;

/**
 * Configuration for MCP tool server.
 * 
 * Extends the unified ServerConfiguration to provide tool-specific configuration
 * while inheriting common server settings.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ToolServerConfiguration extends ServerConfiguration {

    // Tool-specific fields
    private final String serverId;
    private final String serverVersion;
    private final TransportType transportType;
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

    private final Map<String, Object> transportOptions;
    private final Map<String, Object> serverOptions;

    /**
     * Private constructor for builder pattern.
     */
    public ToolServerConfiguration(ServerConfigurationBuilder builder) {
        // Call the protected constructor of ServerConfiguration with default values for missing fields
        super(builder.getName() != null ? builder.getName() : "default-server", builder.isEnabled(),
                builder.getName() != null ? builder.getName() : "Default Server",
                builder.getVersion() != null ? builder.getVersion() : "1.0.0", builder.getSettings(),
                "http://" + builder.getHost() + ":" + builder.getPort(), // baseUrl
                builder.getPort(), // port
                "/", // contextPath
                "/", // servletPath
                "/*", // servletPattern
                "/mcp", // messageEndpoint
                "/health", // healthEndpoint
                "/status", // statusEndpoint
                "/metrics", // metricsEndpoint
                false, // enableAuthentication
                "none", // primaryAuthMethod
                "none", // fallbackAuthMethod
                false, // enableFallbackAuth
                100, // maxConnections
                60, // rateLimitPerMinute
                true, // enableRequestValidation
                null, // oauthIssuerUrl
                null, // oauthClientId
                null, // oauthClientSecret
                null, // oauthRedirectUri
                false, // oauthPkceEnabled
                null, // openhabUsersFile
                false, // openhabUsersEnabled
                "X-API-Key", // apiKeyHeader
                null, // apiKeyValue
                false, // apiKeyEnabled
                null, // jwtSecret
                null, // jwtIssuer
                60, // jwtExpirationMinutes
                false, // jwtEnabled
                true, // enableMetrics
                true, // enableHealthChecks
                30, // healthCheckInterval
                false, // enablePerformanceMonitoring
                false, // productionMode
                30000, // requestTimeout
                10000, // connectionTimeout
                true, // enableGracefulShutdown
                5000, // shutdownTimeout
                false, // enableCors
                "*", // corsAllowedOrigins
                "GET,POST,PUT,DELETE,OPTIONS", // corsAllowedMethods
                "Content-Type,Authorization", // corsAllowedHeaders
                false, // enableSsl
                null, // sslKeyStore
                null, // sslKeyStorePassword
                null, // sslTrustStore
                null); // sslTrustStorePassword

        // Tool-specific initialization
        this.serverId = builder.getName() != null ? builder.getName() : "default-server";
        this.serverVersion = "1.0.0"; // Default value since unified builder doesn't have version
        this.transportType = TransportType.HTTP; // Default value since unified builder doesn't have transport type

        // Default values for tool-specific fields
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
        this.transportOptions = builder.getSettings() != null ? Map.copyOf(builder.getSettings()) : Map.of();
        this.serverOptions = Map.of();
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
        String baseUrl = getBaseUrl();
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

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o)
            return true;
        if (!super.equals(o) || getClass() != o.getClass())
            return false;
        ToolServerConfiguration that = (ToolServerConfiguration) o;
        return Objects.equals(serverId, that.serverId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), serverId);
    }

    @Override
    public String toString() {
        return "ToolServerConfiguration{" + "serverId='" + serverId + '\'' + ", serverName='" + getName() + '\''
                + ", serverVersion='" + serverVersion + '\'' + ", transportType=" + transportType + ", baseUrl='"
                + getBaseUrl() + '\'' + ", messageEndpoint='" + getMessageEndpoint() + '\'' + ", sseEndpoint='"
                + sseEndpoint + '\'' + ", enableSse=" + enableSse + ", enableTools=" + enableTools
                + ", enableResources=" + enableResources + ", enablePrompts=" + enablePrompts + ", enableLogging="
                + enableLogging + '}';
    }
}
