package org.openhab.core.ai.tool;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Service interface for tool configuration management.
 * 
 * This service provides configuration management for the MCP (Model Context Protocol)
 * tool functionality, following the ai.tool.* naming convention.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public interface ToolConfigurationService {

    /**
     * Gets the tool server ID.
     * 
     * @return the server ID
     */
    @Nullable
    String getServerId();

    /**
     * Gets the tool server name.
     * 
     * @return the server name
     */
    @Nullable
    String getServerName();

    /**
     * Gets the tool server version.
     * 
     * @return the server version
     */
    @Nullable
    String getServerVersion();

    /**
     * Gets the tool server description.
     * 
     * @return the server description
     */
    @Nullable
    String getServerDescription();

    /**
     * Checks if the tool server is enabled.
     * 
     * @return true if enabled, false otherwise
     */
    boolean isServerEnabled();

    /**
     * Gets the tool server port.
     * 
     * @return the server port
     */
    int getServerPort();

    /**
     * Gets the transport type.
     * 
     * @return the transport type
     */
    @Nullable
    String getTransportType();

    /**
     * Gets the base URL for transport.
     * 
     * @return the base URL
     */
    @Nullable
    String getTransportBaseUrl();

    /**
     * Gets the message endpoint.
     * 
     * @return the message endpoint
     */
    @Nullable
    String getTransportMessageEndpoint();

    /**
     * Gets the SSE endpoint.
     * 
     * @return the SSE endpoint
     */
    @Nullable
    String getTransportSseEndpoint();

    /**
     * Checks if SSE is enabled.
     * 
     * @return true if SSE is enabled, false otherwise
     */
    boolean isSseEnabled();

    /**
     * Checks if tools feature is enabled.
     * 
     * @return true if tools feature is enabled, false otherwise
     */
    boolean isToolsEnabled();

    /**
     * Checks if resources feature is enabled.
     * 
     * @return true if resources feature is enabled, false otherwise
     */
    boolean isResourcesEnabled();

    /**
     * Checks if prompts feature is enabled.
     * 
     * @return true if prompts feature is enabled, false otherwise
     */
    boolean isPromptsEnabled();

    /**
     * Checks if logging feature is enabled.
     * 
     * @return true if logging feature is enabled, false otherwise
     */
    boolean isLoggingEnabled();

    /**
     * Checks if async server is enabled.
     * 
     * @return true if async server is enabled, false otherwise
     */
    boolean isAsyncServerEnabled();

    /**
     * Checks if async tools are enabled.
     * 
     * @return true if async tools are enabled, false otherwise
     */
    boolean isAsyncToolsEnabled();

    /**
     * Gets the async thread pool size.
     * 
     * @return the thread pool size
     */
    int getAsyncThreadPoolSize();

    /**
     * Gets the async queue capacity.
     * 
     * @return the queue capacity
     */
    int getAsyncQueueCapacity();

    /**
     * Checks if async completions are enabled.
     * 
     * @return true if async completions are enabled, false otherwise
     */
    boolean isAsyncCompletionsEnabled();

    /**
     * Checks if authentication is enabled.
     * 
     * @return true if authentication is enabled, false otherwise
     */
    boolean isAuthenticationEnabled();

    /**
     * Checks if request validation is enabled.
     * 
     * @return true if request validation is enabled, false otherwise
     */
    boolean isRequestValidationEnabled();

    /**
     * Gets the primary authentication method.
     * 
     * @return the primary authentication method
     */
    @Nullable
    String getPrimaryAuthMethod();

    /**
     * Gets the fallback authentication method.
     * 
     * @return the fallback authentication method
     */
    @Nullable
    String getFallbackAuthMethod();

    /**
     * Checks if authentication fallback is enabled.
     * 
     * @return true if authentication fallback is enabled, false otherwise
     */
    boolean isAuthFallbackEnabled();

    /**
     * Gets the protocol name for authentication.
     * 
     * @return the protocol name
     */
    @Nullable
    String getAuthProtocolName();

    /**
     * Gets the protocol permissions.
     * 
     * @return the protocol permissions
     */
    @Nullable
    String getAuthProtocolPermissions();

    /**
     * Gets the session timeout in seconds.
     * 
     * @return the session timeout
     */
    int getAuthSessionTimeout();

    /**
     * Checks if session refresh is enabled.
     * 
     * @return true if session refresh is enabled, false otherwise
     */
    boolean isAuthSessionRefreshEnabled();

    /**
     * Gets the session refresh threshold.
     * 
     * @return the session refresh threshold
     */
    int getAuthSessionRefreshThreshold();

    /**
     * Gets the read operations permissions.
     * 
     * @return the read operations permissions
     */
    @Nullable
    String getAuthReadOperations();

    /**
     * Gets the write operations permissions.
     * 
     * @return the write operations permissions
     */
    @Nullable
    String getAuthWriteOperations();

    /**
     * Gets the execute operations permissions.
     * 
     * @return the execute operations permissions
     */
    @Nullable
    String getAuthExecuteOperations();

    /**
     * Gets the admin operations permissions.
     * 
     * @return the admin operations permissions
     */
    @Nullable
    String getAuthAdminOperations();

    /**
     * Gets the maximum number of tools.
     * 
     * @return the maximum number of tools
     */
    int getMaxTools();

    /**
     * Gets the maximum concurrent sessions.
     * 
     * @return the maximum concurrent sessions
     */
    int getMaxConcurrentSessions();

    /**
     * Gets the session cleanup interval.
     * 
     * @return the session cleanup interval
     */
    int getSessionCleanupInterval();

    /**
     * Checks if session invalidation is enabled.
     * 
     * @return true if session invalidation is enabled, false otherwise
     */
    boolean isSessionInvalidationEnabled();
}
