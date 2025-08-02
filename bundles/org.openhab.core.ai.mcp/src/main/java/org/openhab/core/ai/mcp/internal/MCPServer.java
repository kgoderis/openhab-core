package org.openhab.core.ai.mcp.internal;

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.server.McpAsyncServer;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletSseServerTransportProvider;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpServerTransportProvider;

/**
 * MCP server implementation.
 * 
 * @author AI Assistant
 * @since 1.0.0
 */
@NonNullByDefault
public class MCPServer {

    private static final Logger logger = LoggerFactory.getLogger(MCPServer.class);

    private final String serverId;
    private final MCPServerConfiguration configuration;
    private final ObjectMapper objectMapper;
    private final @Nullable BundleContext bundleContext;
    private final MCPToolRegistry toolRegistry;
    private final AtomicReference<MCPServerState> state = new AtomicReference<>(MCPServerState.STOPPED);

    // MCP server using the official SDK
    private volatile @Nullable McpServerTransportProvider mcpTransport;
    private volatile @Nullable McpSyncServer mcpSyncServer;
    private volatile @Nullable McpAsyncServer mcpAsyncServer;

    // Transport health monitoring
    private volatile @Nullable MCPTransportType currentTransportType;
    private volatile long transportStartTime;
    private volatile boolean transportHealthy = true;
    private volatile @Nullable String lastTransportError;

    // Security and reliability managers
    private volatile @Nullable MCPSecurityManager securityManager;
    private volatile @Nullable MCPErrorRecoveryManager errorRecoveryManager;

    /**
     * Enumeration of server states.
     */
    public enum MCPServerState {
        STOPPED,
        STARTING,
        RUNNING,
        STOPPING,
        ERROR
    }

    /**
     * Create a new MCP server instance using the official SDK.
     * 
     * @param serverId Server identifier
     * @param configuration Server configuration
     * @param toolRegistry Tool registry
     */
    public MCPServer(String serverId, MCPServerConfiguration configuration, MCPToolRegistry toolRegistry) {
        this.serverId = serverId;
        this.configuration = configuration;
        this.toolRegistry = toolRegistry;
        this.objectMapper = new ObjectMapper();
        this.bundleContext = null; // Will be set if needed

        // Initialize security and error recovery managers (will be properly initialized when services are available)
        this.securityManager = null;
        this.errorRecoveryManager = null;
    }

    /**
     * Set the security manager.
     * 
     * @param securityManager Security manager instance
     */
    public void setSecurityManager(MCPSecurityManager securityManager) {
        this.securityManager = securityManager;
        logger.info("Security manager set for MCP server instance: {}", serverId);
    }

    /**
     * Set the error recovery manager.
     * 
     * @param errorRecoveryManager Error recovery manager instance
     */
    public void setErrorRecoveryManager(MCPErrorRecoveryManager errorRecoveryManager) {
        this.errorRecoveryManager = errorRecoveryManager;
        logger.info("Error recovery manager set for MCP server instance: {}", serverId);
    }

    /**
     * Start the MCP server instance.
     * 
     * @throws Exception if startup fails
     */
    public void start() throws Exception {
        if (!state.compareAndSet(MCPServerState.STOPPED, MCPServerState.STARTING)) {
            throw new IllegalStateException("Server instance is not in STOPPED state: " + state.get());
        }

        logger.info("Starting MCP server instance: {}", serverId);

        try {
            // Initialize MCP server based on configuration
            initializeMCPServer();

            // Start the server
            startMCPServer();

            state.set(MCPServerState.RUNNING);
            logger.info("MCP server instance started successfully: {}", serverId);
        } catch (Exception e) {
            state.set(MCPServerState.ERROR);
            logger.error("Failed to start MCP server instance: {}", serverId, e);

            // Handle error through recovery manager if available
            if (errorRecoveryManager != null) {
                String errorMessage = e.getMessage();
                if (errorMessage == null) {
                    errorMessage = "Unknown error";
                }
                MCPErrorRecoveryManager.RecoveryAction action = errorRecoveryManager.handleError("STARTUP_ERROR",
                        errorMessage, "");
                logger.info("Recovery action for startup error: {}", action);
            }

            throw e;
        }
    }

    /**
     * Stop the MCP server instance.
     * 
     * @throws Exception if shutdown fails
     */
    public void stop() throws Exception {
        MCPServerState currentState = state.get();
        if (currentState == MCPServerState.STOPPED) {
            logger.warn("MCP server instance already stopped: {}", serverId);
            return;
        }

        if (!state.compareAndSet(currentState, MCPServerState.STOPPING)) {
            throw new IllegalStateException("Server instance is not in a stoppable state: " + currentState);
        }

        logger.info("Stopping MCP server instance: {}", serverId);

        try {
            stopMCPServer();
            state.set(MCPServerState.STOPPED);
            logger.info("MCP server instance stopped successfully: {}", serverId);
        } catch (Exception e) {
            state.set(MCPServerState.ERROR);
            logger.error("Failed to stop MCP server instance: {}", serverId, e);
            throw e;
        }
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
     * Get the server configuration.
     * 
     * @return Server configuration
     */
    public MCPServerConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Get the current server state.
     * 
     * @return Server state
     */
    public MCPServerState getState() {
        return state.get();
    }

    /**
     * Check if the server is running.
     * 
     * @return true if running
     */
    public boolean isRunning() {
        if (configuration.isEnableAsyncServer()) {
            return state.get() == MCPServerState.RUNNING && mcpAsyncServer != null;
        } else {
            return state.get() == MCPServerState.RUNNING && mcpSyncServer != null;
        }
    }

    /**
     * Get security statistics if security manager is available.
     * 
     * @return Security statistics or null if not available
     */
    public MCPSecurityManager.@Nullable SecurityStatistics getSecurityStatistics() {
        if (securityManager != null) {
            return securityManager.getSecurityStatistics();
        }
        return null;
    }

    /**
     * Get error recovery statistics if error recovery manager is available.
     * 
     * @return Error recovery statistics or null if not available
     */
    public MCPErrorRecoveryManager.@Nullable ErrorRecoveryStatistics getErrorRecoveryStatistics() {
        if (errorRecoveryManager != null) {
            return errorRecoveryManager.getErrorRecoveryStatistics();
        }
        return null;
    }

    /**
     * Get detailed error information if error recovery manager is available.
     * 
     * @return Map of error types to error information or null if not available
     */
    public java.util.@Nullable Map<String, MCPErrorRecoveryManager.ErrorInfo> getErrorDetails() {
        if (errorRecoveryManager != null) {
            return errorRecoveryManager.getErrorDetails();
        }
        return null;
    }

    /**
     * Check if security manager is available.
     * 
     * @return true if security manager is available
     */
    public boolean isSecurityEnabled() {
        return securityManager != null;
    }

    /**
     * Check if error recovery manager is available.
     * 
     * @return true if error recovery manager is available
     */
    public boolean isErrorRecoveryEnabled() {
        return errorRecoveryManager != null;
    }

    /**
     * Enhanced health check including security and reliability status.
     * 
     * @return true if server is healthy
     */
    public boolean isHealthy() {
        MCPServerState currentState = state.get();
        boolean basicHealth = (currentState == MCPServerState.RUNNING || currentState == MCPServerState.STARTING)
                && transportHealthy;

        // Check security health if available
        if (securityManager != null) {
            MCPSecurityManager.SecurityStatistics securityStats = securityManager.getSecurityStatistics();
            if (securityStats != null && securityStats.getBlockedClients() > 10) {
                logger.warn("High number of blocked clients: {}", securityStats.getBlockedClients());
                // Don't fail health check for security issues, just log
            }
        }

        // Check error recovery health if available
        if (errorRecoveryManager != null) {
            MCPErrorRecoveryManager.ErrorRecoveryStatistics errorStats = errorRecoveryManager
                    .getErrorRecoveryStatistics();
            if (errorStats != null && errorStats.getRecoveryRate() < 0.5) {
                logger.warn("Low error recovery rate: {:.2f}%", errorStats.getRecoveryRate() * 100);
                // Don't fail health check for recovery issues, just log
            }
        }

        return basicHealth;
    }

    /**
     * Get transport health information.
     * 
     * @return Transport health status
     */
    public TransportHealthInfo getTransportHealth() {
        return new TransportHealthInfo(currentTransportType, transportHealthy, transportStartTime, lastTransportError,
                System.currentTimeMillis() - transportStartTime);
    }

    /**
     * Transport health information.
     */
    public static class TransportHealthInfo {
        private final @Nullable MCPTransportType transportType;
        private final boolean healthy;
        private final long startTime;
        private final @Nullable String lastError;
        private final long uptime;

        public TransportHealthInfo(@Nullable MCPTransportType transportType, boolean healthy, long startTime,
                @Nullable String lastError, long uptime) {
            this.transportType = transportType;
            this.healthy = healthy;
            this.startTime = startTime;
            this.lastError = lastError;
            this.uptime = uptime;
        }

        public @Nullable MCPTransportType getTransportType() {
            return transportType;
        }

        public boolean isHealthy() {
            return healthy;
        }

        public @Nullable String getLastError() {
            return lastError;
        }

        public long getUptime() {
            return uptime;
        }

        @Override
        public String toString() {
            return String.format("TransportHealthInfo{type=%s, healthy=%s, uptime=%dms, lastError='%s'}", transportType,
                    healthy, uptime, lastError);
        }
    }

    /**
     * Get detailed transport statistics and performance metrics.
     * 
     * @return Transport statistics
     */
    public TransportStatistics getTransportStatistics() {
        return new TransportStatistics(currentTransportType, transportStartTime,
                System.currentTimeMillis() - transportStartTime, transportHealthy, lastTransportError,
                mcpTransport != null ? mcpTransport.getClass().getSimpleName() : "null",
                configuration.getTransportType());
    }

    /**
     * Transport statistics and performance metrics.
     */
    public static class TransportStatistics {
        private final @Nullable MCPTransportType currentType;
        private final long startTime;
        private final long uptime;
        private final boolean healthy;
        private final @Nullable String lastError;
        private final String transportClass;
        private final MCPTransportType configuredType;

        public TransportStatistics(@Nullable MCPTransportType currentType, long startTime, long uptime, boolean healthy,
                @Nullable String lastError, String transportClass, MCPTransportType configuredType) {
            this.currentType = currentType;
            this.startTime = startTime;
            this.uptime = uptime;
            this.healthy = healthy;
            this.lastError = lastError;
            this.transportClass = transportClass;
            this.configuredType = configuredType;
        }

        public @Nullable MCPTransportType getCurrentType() {
            return currentType;
        }

        public long getStartTime() {
            return startTime;
        }

        public long getUptime() {
            return uptime;
        }

        public boolean isHealthy() {
            return healthy;
        }

        public @Nullable String getLastError() {
            return lastError;
        }

        public String getTransportClass() {
            return transportClass;
        }

        public MCPTransportType getConfiguredType() {
            return configuredType;
        }

        public boolean isUsingFallback() {
            return currentType != configuredType;
        }

        @Override
        public String toString() {
            return String.format(
                    "TransportStatistics{current=%s, configured=%s, fallback=%s, healthy=%s, uptime=%dms, class=%s, error='%s'}",
                    currentType, configuredType, isUsingFallback(), healthy, uptime, transportClass, lastError);
        }
    }

    /**
     * Initialize the MCP server using the official SDK.
     * 
     * @throws Exception if initialization fails
     */
    private void initializeMCPServer() throws Exception {
        logger.debug("Initializing MCP server: {}", serverId);

        try {
            // Validate security configuration if security is enabled
            if (configuration.isEnableAuthentication() && securityManager == null) {
                logger.warn("Authentication is enabled but no security manager is set for server: {}", serverId);
            }

            // Create transport using SDK
            mcpTransport = createTransport();

            // Create server based on configuration (sync or async)
            if (configuration.isEnableAsyncServer()) {
                mcpAsyncServer = createAsyncServer();
                registerAsyncTools();
            } else {
                mcpSyncServer = createSyncServer();
                registerSyncTools();
            }

            logger.debug("MCP server initialized: {}", serverId);
        } catch (Exception e) {
            logger.error("Failed to initialize MCP server: {}", serverId, e);
            throw e;
        }
    }

    /**
     * Create transport for the SDK with validation and fallback logic.
     * 
     * @throws Exception if transport creation fails
     */
    private McpServerTransportProvider createTransport() throws Exception {
        MCPTransportType transportType = configuration.getTransportType();
        logger.debug("Creating transport for type: {}", transportType);

        // Validate transport type
        if (transportType == null) {
            logger.warn("Transport type is null, falling back to STDIO");
            transportType = MCPTransportType.STDIO;
        }

        // Validate transport configuration
        validateTransportConfiguration(transportType);

        // Try to create the requested transport
        McpServerTransportProvider transport = null;
        Exception lastException = null;

        try {
            switch (transportType) {
                case STDIO:
                    transport = createStdioTransport();
                    break;
                case SSE:
                    transport = createSseTransport();
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported transport type: " + transportType);
            }

            // Validate the created transport
            if (transport != null) {
                validateTransport(transport, transportType);
            }

            logger.info("Successfully created {} transport", transportType);
            currentTransportType = transportType;
            transportStartTime = System.currentTimeMillis();
            transportHealthy = true;
            lastTransportError = null;
            return transport;

        } catch (Exception e) {
            lastException = e;
            lastTransportError = e.getMessage();
            transportHealthy = false;
            logger.warn("Failed to create {} transport: {}", transportType, e.getMessage());

            // Fallback to STDIO if the requested transport fails
            if (transportType != MCPTransportType.STDIO) {
                logger.info("Falling back to STDIO transport");
                try {
                    transport = createStdioTransport();
                    validateTransport(transport, MCPTransportType.STDIO);
                    logger.info("Successfully created STDIO fallback transport");
                    currentTransportType = MCPTransportType.STDIO;
                    transportStartTime = System.currentTimeMillis();
                    transportHealthy = true;
                    lastTransportError = null;
                    return transport;
                } catch (Exception fallbackException) {
                    lastTransportError = fallbackException.getMessage();
                    transportHealthy = false;
                    logger.error("Failed to create STDIO fallback transport: {}", fallbackException.getMessage());
                    throw new RuntimeException("Failed to create any transport", fallbackException);
                }
            } else {
                // If STDIO itself failed, we can't fallback
                throw new RuntimeException("Failed to create STDIO transport", lastException);
            }
        }
    }

    /**
     * Validate transport configuration before creation.
     * 
     * @param transportType Transport type to validate
     * @throws IllegalArgumentException if configuration is invalid
     */
    private void validateTransportConfiguration(MCPTransportType transportType) {
        switch (transportType) {
            case SSE:
                validateSseConfiguration();
                break;
            case STDIO:
                // STDIO doesn't require special configuration validation
                break;
            default:
                throw new IllegalArgumentException("Unsupported transport type: " + transportType);
        }
    }

    /**
     * Validate SSE transport configuration.
     * 
     * @throws IllegalArgumentException if SSE configuration is invalid
     */
    private void validateSseConfiguration() {
        if (configuration.getBaseUrl() == null || configuration.getBaseUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("Base URL is required for SSE transport");
        }

        if (configuration.getMessageEndpoint() == null || configuration.getMessageEndpoint().trim().isEmpty()) {
            throw new IllegalArgumentException("Message endpoint is required for SSE transport");
        }

        if (configuration.getSseEndpoint() == null || configuration.getSseEndpoint().trim().isEmpty()) {
            throw new IllegalArgumentException("SSE endpoint is required for SSE transport");
        }

        // Validate URL format
        try {
            new java.net.URL(configuration.getBaseUrl());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid base URL format: " + configuration.getBaseUrl());
        }
    }

    /**
     * Validate created transport.
     * 
     * @param transport Transport to validate
     * @param transportType Expected transport type
     * @throws Exception if validation fails
     */
    private void validateTransport(McpServerTransportProvider transport, MCPTransportType transportType) {
        if (transport == null) {
            throw new IllegalStateException("Transport creation returned null");
        }

        // Basic validation - check if transport is of expected type
        String transportClassName = transport.getClass().getSimpleName();
        logger.debug("Created transport: {} for type: {}", transportClassName, transportType);

        // Additional validation could be added here based on transport type
        switch (transportType) {
            case SSE:
                // For SSE, we could validate that the transport can handle HTTP requests
                if (!transportClassName.contains("Http") && !transportClassName.contains("Sse")) {
                    logger.warn("SSE transport may not be properly configured: {}", transportClassName);
                }
                break;
            case STDIO:
                // For STDIO, we could validate that the transport can handle standard I/O
                if (!transportClassName.contains("Stdio")) {
                    logger.warn("STDIO transport may not be properly configured: {}", transportClassName);
                }
                break;
        }
    }

    /**
     * Create STDIO transport using the SDK.
     * 
     * @return STDIO transport
     */
    private McpServerTransportProvider createStdioTransport() {
        logger.debug("Creating STDIO transport with ObjectMapper: {}", objectMapper.getClass().getSimpleName());
        long startTime = System.currentTimeMillis();

        try {
            McpServerTransportProvider transport = new StdioServerTransportProvider(objectMapper);
            long duration = System.currentTimeMillis() - startTime;
            logger.debug("STDIO transport created successfully in {}ms", duration);
            return transport;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("Failed to create STDIO transport after {}ms: {}", duration, e.getMessage());
            throw e;
        }
    }

    /**
     * Create SSE transport using the SDK.
     * 
     * @return SSE transport
     */
    private McpServerTransportProvider createSseTransport() {
        logger.debug("Creating SSE transport for MCP server");
        long startTime = System.currentTimeMillis();

        try {
            // Create SSE transport provider using the SDK
            McpServerTransportProvider transport = HttpServletSseServerTransportProvider.builder()
                    .objectMapper(objectMapper).baseUrl(configuration.getBaseUrl())
                    .messageEndpoint(configuration.getMessageEndpoint()).sseEndpoint(configuration.getSseEndpoint())
                    .build();

            long duration = System.currentTimeMillis() - startTime;
            logger.debug(
                    "SSE transport created successfully in {}ms with baseUrl={}, messageEndpoint={}, sseEndpoint={}",
                    duration, configuration.getBaseUrl(), configuration.getMessageEndpoint(),
                    configuration.getSseEndpoint());
            return transport;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.warn("Failed to create SSE transport after {}ms: {}", duration, e.getMessage());
            return createStdioTransport();
        }
    }

    /**
     * Create sync server using the proper SDK pattern.
     * 
     * @return Sync server
     * @throws Exception if creation fails
     */
    private McpSyncServer createSyncServer() throws Exception {
        logger.debug("Creating MCP sync server with transport: {}", mcpTransport.getClass().getSimpleName());

        // Create server capabilities
        McpSchema.ServerCapabilities capabilities = McpSchema.ServerCapabilities.builder().resources(false, true) // Enable
                                                                                                                  // resource
                                                                                                                  // support
                .tools(true) // Enable tool support
                .prompts(true) // Enable prompt support
                .logging() // Enable logging support
                .completions() // Enable completions support
                .build();

        // Create sync server using the SDK pattern
        return McpServer.sync(mcpTransport).serverInfo(configuration.getServerName(), "1.0.0")
                .capabilities(capabilities).build();
    }

    /**
     * Create async server using the proper SDK pattern.
     * 
     * @return Async server
     * @throws Exception if creation fails
     */
    private McpAsyncServer createAsyncServer() throws Exception {
        logger.debug("Creating MCP async server with transport: {}", mcpTransport.getClass().getSimpleName());

        // Create server capabilities
        McpSchema.ServerCapabilities capabilities = McpSchema.ServerCapabilities.builder().resources(false, true) // Enable
                                                                                                                  // resource
                                                                                                                  // support
                .tools(true) // Enable tool support
                .prompts(true) // Enable prompt support
                .logging() // Enable logging support
                .completions() // Enable completions support
                .build();

        // Create async server using the SDK pattern
        return McpServer.async(mcpTransport).serverInfo(configuration.getServerName(), "1.0.0")
                .capabilities(capabilities).build();
    }

    /**
     * Register tools with the sync server.
     */
    private void registerSyncTools() {
        McpServerFeatures.SyncToolSpecification[] toolSpecs = toolRegistry.getToolSpecifications();

        // Apply security filtering if security is enabled
        if (configuration.isEnableAuthentication() && securityManager != null) {
            logger.debug("Applying security filtering to {} sync tools", toolSpecs.length);
            toolSpecs = filterToolsBySecurity(toolSpecs);
        }

        for (McpServerFeatures.SyncToolSpecification toolSpec : toolSpecs) {
            mcpSyncServer.addTool(toolSpec);
        }
        logger.info("Registered {} sync tools with MCP server", toolSpecs.length);
    }

    /**
     * Register tools with the async server.
     */
    private void registerAsyncTools() {
        McpServerFeatures.AsyncToolSpecification[] toolSpecs = toolRegistry.getAsyncToolSpecifications();

        // Apply security filtering if security is enabled
        if (configuration.isEnableAuthentication() && securityManager != null) {
            logger.debug("Applying security filtering to {} async tools", toolSpecs.length);
            toolSpecs = filterAsyncToolsBySecurity(toolSpecs);
        }

        for (McpServerFeatures.AsyncToolSpecification toolSpec : toolSpecs) {
            mcpAsyncServer.addTool(toolSpec);
        }
        logger.info("Registered {} async tools with MCP server", toolSpecs.length);
    }

    /**
     * Filter tools based on security configuration.
     * 
     * @param toolSpecs Original tool specifications
     * @return Filtered tool specifications
     */
    private McpServerFeatures.SyncToolSpecification[] filterToolsBySecurity(
            McpServerFeatures.SyncToolSpecification[] toolSpecs) {
        // For now, return all tools - security filtering can be implemented here
        // based on the security manager's configuration
        // TODO: Implement security filtering
        logger.debug("Security filtering not yet implemented, returning all {} sync tools", toolSpecs.length);
        return toolSpecs;
    }

    /**
     * Filter async tools based on security configuration.
     * 
     * @param toolSpecs Original async tool specifications
     * @return Filtered async tool specifications
     */
    private McpServerFeatures.AsyncToolSpecification[] filterAsyncToolsBySecurity(
            McpServerFeatures.AsyncToolSpecification[] toolSpecs) {
        // For now, return all tools - security filtering can be implemented here
        // based on the security manager's configuration
        // TODO: Implement security filtering
        logger.debug("Security filtering not yet implemented, returning all {} async tools", toolSpecs.length);
        return toolSpecs;
    }

    /**
     * Start the MCP server.
     * 
     * @throws Exception if startup fails
     */
    private void startMCPServer() throws Exception {
        if (configuration.isEnableAsyncServer()) {
            startAsyncServer();
        } else {
            startSyncServer();
        }
    }

    /**
     * Start the MCP sync server.
     * 
     * @throws Exception if startup fails
     */
    private void startSyncServer() throws Exception {
        logger.debug("Starting MCP sync server: {}", serverId);

        try {
            // Validate security configuration before starting
            if (configuration.isEnableAuthentication() && securityManager == null) {
                logger.warn(
                        "Authentication is enabled but no security manager is set - server will start without security");
            }

            // The sync server is already running when created
            // We just need to ensure it's properly initialized
            if (mcpSyncServer != null) {
                logger.debug("MCP sync server is running: {}", serverId);
            } else {
                throw new IllegalStateException("MCP sync server not initialized");
            }

            logger.info("MCP sync server started successfully: {}", serverId);
        } catch (Exception e) {
            logger.error("Failed to start MCP sync server: {}", serverId, e);
            throw e;
        }
    }

    /**
     * Start the MCP async server.
     * 
     * @throws Exception if startup fails
     */
    private void startAsyncServer() throws Exception {
        logger.debug("Starting MCP async server: {}", serverId);

        try {
            // Validate security configuration before starting
            if (configuration.isEnableAuthentication() && securityManager == null) {
                logger.warn(
                        "Authentication is enabled but no security manager is set - server will start without security");
            }

            // The async server is already running when created
            // We just need to ensure it's properly initialized
            if (mcpAsyncServer != null) {
                logger.debug("MCP async server is running: {}", serverId);
            } else {
                throw new IllegalStateException("MCP async server not initialized");
            }

            logger.info("MCP async server started successfully: {}", serverId);
        } catch (Exception e) {
            logger.error("Failed to start MCP async server: {}", serverId, e);
            throw e;
        }
    }

    /**
     * Stop the MCP server.
     * 
     * @throws Exception if shutdown fails
     */
    private void stopMCPServer() throws Exception {
        if (configuration.isEnableAsyncServer()) {
            stopAsyncServer();
        } else {
            stopSyncServer();
        }
    }

    /**
     * Stop the MCP sync server.
     * 
     * @throws Exception if shutdown fails
     */
    private void stopSyncServer() throws Exception {
        logger.debug("Stopping MCP sync server: {}", serverId);

        try {
            if (mcpSyncServer != null) {
                mcpSyncServer.close();
                mcpSyncServer = null; // This is intentional - clearing the reference
                logger.info("MCP sync server stopped successfully: {}", serverId);
            }
        } catch (Exception e) {
            logger.error("Failed to stop MCP sync server: {}", serverId, e);
            throw e;
        }
    }

    /**
     * Stop the MCP async server.
     * 
     * @throws Exception if shutdown fails
     */
    private void stopAsyncServer() throws Exception {
        logger.debug("Stopping MCP async server: {}", serverId);

        try {
            if (mcpAsyncServer != null) {
                mcpAsyncServer.close();
                mcpAsyncServer = null; // This is intentional - clearing the reference
                logger.info("MCP async server stopped successfully: {}", serverId);
            }
        } catch (Exception e) {
            logger.error("Failed to stop MCP async server: {}", serverId, e);
            throw e;
        }
    }

    @Override
    public String toString() {
        return String.format("MCPServerInstance{serverId='%s', state=%s, configuration=%s}", serverId, state.get(),
                configuration);
    }
}
