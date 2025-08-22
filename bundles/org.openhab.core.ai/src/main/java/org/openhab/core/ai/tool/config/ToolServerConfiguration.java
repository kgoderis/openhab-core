package org.openhab.core.ai.tool.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.transport.TransportType;
import org.openhab.core.ai.config.common.ServerConfiguration;

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
    public ToolServerConfiguration(ServerConfiguration.Builder builder) {
        // Call the protected constructor of ServerConfiguration with default values
        super("tool-server", true, "Tool Server", "1.0.0", Map.of(), "http://localhost:8080", 8080, "/", "/", "/*",
                "/mcp", "/health", "/status", "/metrics", false, "none", "none", false, 100, 1000, true, "", "", "", "",
                false, "", false, "X-API-Key", "", false, "", "", 60, false, true, true, 30, false, false, 30000, 10000,
                true, 30, false, "*", "GET,POST,PUT,DELETE", "*", false, "", "", "", "");

        // Extract tool-specific settings from the builder's settings
        Map<String, Object> settings = new HashMap<>();

        this.serverId = "tool-server";
        this.serverVersion = "1.0.0";
        this.transportType = TransportType.STDIO;
        this.sseEndpoint = "/mcp/events";
        this.enableSse = false;

        this.enableTools = true;
        this.enableResources = true;
        this.enablePrompts = true;
        this.enableLogging = true;

        this.enableAsyncServer = false;
        this.enableAsyncTools = false;
        this.asyncThreadPoolSize = 10;
        this.asyncQueueCapacity = 1000;
        this.enableAsyncCompletions = false;

        this.transportOptions = Map.of();
        this.serverOptions = settings;
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
