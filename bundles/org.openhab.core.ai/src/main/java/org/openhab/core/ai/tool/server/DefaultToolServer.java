package org.openhab.core.ai.tool.server;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.service.statistics.ErrorRecoveryStatistics;
import org.openhab.core.ai.common.security.ToolSecurityStatistics;
import org.openhab.core.ai.common.transport.TransportType;
import org.openhab.core.ai.tool.config.ToolServerConfiguration;
import org.openhab.core.ai.tool.error.DefaultErrorRecoveryService;
import org.openhab.core.ai.tool.error.ErrorInfo;
import org.openhab.core.ai.tool.registry.ToolRegistry;
import org.openhab.core.ai.tool.security.DefaultToolSecurityService;
import org.openhab.core.ai.tool.server.api.ToolServer;
import org.openhab.core.ai.tool.server.api.ToolServerState;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.server.McpAsyncServer;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletSseServerTransportProvider;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import io.modelcontextprotocol.spec.McpServerTransportProvider;

/**
 * Default MCP Tool server implementation.
 * 
 * This class provides the core server functionality for the MCP protocol,
 * managing tool registration, transport configuration, and server lifecycle.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class DefaultToolServer implements ToolServer {

    private static final Logger logger = LoggerFactory.getLogger(DefaultToolServer.class);

    private final String serverId;
    private final ToolServerConfiguration configuration;
    private final ObjectMapper objectMapper;
    private final @Nullable BundleContext bundleContext;
    private final ToolRegistry toolRegistry;
    private final AtomicReference<ToolServerState> state = new AtomicReference<>(ToolServerState.STOPPED);

    // MCP server using the official SDK
    private volatile @Nullable McpServerTransportProvider mcpTransport;
    private volatile @Nullable McpAsyncServer mcpAsyncServer;
    private volatile @Nullable McpSyncServer mcpSyncServer;

    // Transport health monitoring
    private volatile @Nullable TransportType currentTransportType;
    private volatile long transportStartTime;
    private volatile boolean transportHealthy = true;
    private volatile @Nullable String lastTransportError;

    // Security and reliability managers
    private volatile @Nullable DefaultToolSecurityService securityManager;
    private volatile @Nullable DefaultErrorRecoveryService errorRecoveryManager;

    // Metrics service for centralized metrics collection
    @Reference(cardinality = ReferenceCardinality.OPTIONAL)
    private @Nullable MetricsService metricsService;

    /**
     * Enumeration of server states.
     */
    // Inner enum removed; using org.openhab.core.ai.tool.server.api.ToolServerState

    /**
     * Create a new MCP Tool server instance using the official SDK.
     * 
     * @param serverId Server identifier
     * @param configuration Server configuration
     * @param toolRegistry Tool registry
     */
    public DefaultToolServer(String serverId, ToolServerConfiguration configuration, ToolRegistry toolRegistry) {
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
    public void setSecurityManager(DefaultToolSecurityService securityManager) {
        this.securityManager = securityManager;
        logger.info("Security manager set for MCP Tool server instance: {}", serverId);
    }

    /**
     * Set the error recovery manager.
     * 
     * @param errorRecoveryManager Error recovery manager instance
     */
    public void setErrorRecoveryManager(DefaultErrorRecoveryService errorRecoveryManager) {
        this.errorRecoveryManager = errorRecoveryManager;
        logger.info("Error recovery manager set for MCP Tool server instance: {}", serverId);
    }

    /**
     * Start the MCP Tool server.
     * 
     * @throws Exception if the server fails to start
     */
    public void start() throws Exception {
        logger.info("Starting MCP Tool server: {}", serverId);

        if (!state.compareAndSet(ToolServerState.STOPPED, ToolServerState.STARTING)) {
            throw new IllegalStateException("Server is not in STOPPED state: " + state.get());
        }

        try {
            // Initialize the MCP server
            initializeMCPServer();

            // Start the server
            startMCPServer();

            // Update state
            state.set(ToolServerState.RUNNING);
            logger.info("MCP Tool server started successfully: {}", serverId);

        } catch (Exception e) {
            state.set(ToolServerState.ERROR);
            logger.error("Failed to start MCP Tool server: {}", serverId, e);
            throw e;
        }
    }

    /**
     * Stop the MCP Tool server.
     * 
     * @throws Exception if the server fails to stop
     */
    public void stop() throws Exception {
        logger.info("Stopping MCP Tool server: {}", serverId);

        if (!state.compareAndSet(ToolServerState.RUNNING, ToolServerState.STOPPING)) {
            logger.warn("Server is not in RUNNING state: {}", state.get());
            return;
        }

        try {
            // Stop the server
            stopMCPServer();

            // Update state
            state.set(ToolServerState.STOPPED);
            logger.info("MCP Tool server stopped successfully: {}", serverId);

        } catch (Exception e) {
            state.set(ToolServerState.ERROR);
            logger.error("Failed to stop MCP Tool server: {}", serverId, e);
            throw e;
        }
    }

    /**
     * Get the server ID.
     * 
     * @return the server ID
     */
    public String getServerId() {
        return serverId;
    }

    /**
     * Get the server configuration.
     * 
     * @return the server configuration
     */
    public ToolServerConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Get the current server state.
     * 
     * @return the current server state
     */
    @Override
    public ToolServerState getState() {
        return state.get();
    }

    /**
     * Check if the server is running.
     * 
     * @return true if the server is running
     */
    public boolean isRunning() {
        return state.get() == ToolServerState.RUNNING;
    }

    /**
     * Get security statistics.
     * 
     * @return security statistics or null if security manager is not available
     */
    public @Nullable ToolSecurityStatistics getSecurityStatistics() {
        DefaultToolSecurityService manager = securityManager;
        return manager != null ? manager.getSecurityStatistics() : null;
    }

    /**
     * Get error recovery statistics.
     * 
     * @return error recovery statistics or null if error recovery manager is not available
     */
    public @Nullable ErrorRecoveryStatistics getErrorRecoveryStatistics() {
        DefaultErrorRecoveryService manager = errorRecoveryManager;
        return manager != null ? manager.getErrorRecoveryStatistics() : null;
    }

    /**
     * Get detailed error information.
     * 
     * @return detailed error information or null if error recovery manager is not available
     */
    public @Nullable Map<String, ErrorInfo> getErrorDetails() {
        DefaultErrorRecoveryService manager = errorRecoveryManager;
        return manager != null ? manager.getErrorDetails() : null;
    }

    /**
     * Check if security is enabled.
     * 
     * @return true if security is enabled
     */
    public boolean isSecurityEnabled() {
        DefaultToolSecurityService manager = securityManager;
        return manager != null && manager.isSecurityEnabled();
    }

    /**
     * Check if error recovery is enabled.
     * 
     * @return true if error recovery is enabled
     */
    public boolean isErrorRecoveryEnabled() {
        DefaultErrorRecoveryService manager = errorRecoveryManager;
        return manager != null && manager.isErrorRecoveryEnabled();
    }

    /**
     * Check if the server is healthy.
     * 
     * @return true if the server is healthy
     */
    public boolean isHealthy() {
        ToolServerState currentState = state.get();

        if (currentState != ToolServerState.RUNNING) {
            return false;
        }

        // Check transport health
        if (!transportHealthy) {
            return false;
        }

        // Check security manager health if enabled
        if (isSecurityEnabled()) {
            DefaultToolSecurityService manager = securityManager;
            if (manager != null && !manager.isHealthy()) {
                return false;
            }
        }

        // Check error recovery manager health if enabled
        if (isErrorRecoveryEnabled()) {
            DefaultErrorRecoveryService manager = errorRecoveryManager;
            if (manager != null && !manager.isHealthy()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Get transport health information.
     * 
     * @return transport health information
     */
    @Override
    public TransportHealthInfo getTransportHealth() {
        long uptime = System.currentTimeMillis() - transportStartTime;
        return new TransportHealthInfo(currentTransportType, transportHealthy, transportStartTime, lastTransportError,
                uptime);
    }

    /**
     * Transport health information.
     */
    // Inner class extracted to top-level: org.openhab.core.ai.tool.server.TransportHealthInfo

    /**
     * Get transport statistics.
     * 
     * @return transport statistics
     */
    public TransportStatistics getTransportStatistics() {
        long uptime = System.currentTimeMillis() - transportStartTime;
        String transportClass = mcpTransport != null ? mcpTransport.getClass().getSimpleName() : "None";
        return new TransportStatistics(currentTransportType, transportStartTime, uptime, transportHealthy,
                lastTransportError, transportClass, configuration.getTransportType());
    }

    /**
     * Transport statistics.
     */
    // Inner class extracted to top-level: org.openhab.core.ai.tool.server.TransportStatistics

    // Private implementation methods...

    private void initializeMCPServer() throws Exception {
        logger.debug("Initializing MCP Tool server: {}", serverId);

        // Validate transport configuration first
        TransportType transportType = configuration.getTransportType();
        validateTransportConfiguration(transportType);

        // Create transport first
        mcpTransport = createTransport();

        // Create MCP servers based on configuration
        if (configuration.isAsyncEnabled()) {
            mcpAsyncServer = createAsyncServer();
        } else {
            mcpSyncServer = createSyncServer();
        }

        // Validate transport
        McpServerTransportProvider transport = mcpTransport;
        if (transport == null) {
            throw new IllegalStateException("Transport is null after creation for type: " + transportType);
        }
        validateTransport(transport, transportType);

        // Set transport health info
        currentTransportType = transportType;
        transportStartTime = System.currentTimeMillis();
        transportHealthy = true;
        lastTransportError = null;

        logger.debug("MCP Tool server initialized: {}", serverId);
    }

    private McpServerTransportProvider createTransport() throws Exception {
        TransportType transportType = configuration.getTransportType();
        logger.debug("Creating transport for type: {}", transportType);

        switch (transportType) {
            case STDIO:
                return createStdioTransport();
            case SSE:
                return createSseTransport();
            default:
                throw new IllegalArgumentException("Unsupported transport type: " + transportType);
        }
    }

    private void validateTransportConfiguration(TransportType transportType) {
        logger.debug("Validating transport configuration for type: {}", transportType);

        switch (transportType) {
            case STDIO:
                // STDIO doesn't require additional validation
                break;
            case SSE:
                validateSseConfiguration();
                break;
            default:
                throw new IllegalArgumentException("Unsupported transport type: " + transportType);
        }
    }

    private void validateSseConfiguration() {
        // Validate SSE-specific configuration
        if (configuration.getSsePath() == null || configuration.getSsePath().trim().isEmpty()) {
            throw new IllegalArgumentException("SSE path must be configured for SSE transport");
        }

        if (configuration.getSsePort() <= 0 || configuration.getSsePort() > 65535) {
            throw new IllegalArgumentException("SSE port must be between 1 and 65535");
        }

        logger.debug("SSE configuration validated: path={}, port={}", configuration.getSsePath(),
                configuration.getSsePort());
    }

    private void validateTransport(McpServerTransportProvider transport, TransportType transportType) {
        if (transport == null) {
            throw new IllegalStateException("Transport is null for type: " + transportType);
        }

        logger.debug("Transport validated for type: {}", transportType);
    }

    private McpServerTransportProvider createStdioTransport() {
        logger.debug("Creating STDIO transport");
        return new StdioServerTransportProvider();
    }

    private McpServerTransportProvider createSseTransport() {
        logger.debug("Creating SSE transport: path={}, port={}", configuration.getSsePath(),
                configuration.getSsePort());

        // Create SSE transport with configuration using the builder pattern
        String messageEndpoint = configuration.getSsePath() != null ? configuration.getSsePath() : "/mcp";
        String sseEndpoint = configuration.getSsePath() != null ? configuration.getSsePath() + "/sse" : "/mcp/sse";

        return HttpServletSseServerTransportProvider.builder().objectMapper(objectMapper)
                .messageEndpoint(messageEndpoint).sseEndpoint(sseEndpoint).build();
    }

    private McpSyncServer createSyncServer() throws Exception {
        logger.debug("Creating MCP sync server");

        // Get tool specifications from registry
        McpServerFeatures.SyncToolSpecification[] toolSpecs = toolRegistry.getSyncToolSpecifications();

        // Filter tools by security if enabled
        if (isSecurityEnabled()) {
            toolSpecs = filterToolsBySecurity(toolSpecs);
        }

        // Create the sync server using McpServer.sync
        McpSyncServer syncServer = McpServer.sync(mcpTransport).serverInfo(serverId, "1.00")
                .capabilities(ServerCapabilities.builder().resources(false, true) // Resource support with list changes
                                                                                  // notifications
                        .tools(true) // Tool support with list changes notifications
                        .prompts(true) // Prompt support with list changes notifications
                        .logging() // Enable logging support (enabled by default with logging level INFO)
                        .completions() // Enable completions support
                        .build())
                .build();

        // Register tools with the sync server
        for (McpServerFeatures.SyncToolSpecification toolSpec : toolSpecs) {
            syncServer.addTool(toolSpec);
        }

        // Register resources with the sync server
        McpServerFeatures.SyncResourceSpecification[] resourceSpecs = toolRegistry.getSyncResourceSpecifications();
        for (McpServerFeatures.SyncResourceSpecification resourceSpec : resourceSpecs) {
            syncServer.addResource(resourceSpec);
        }

        // Register prompts with the sync server
        McpServerFeatures.SyncPromptSpecification[] promptSpecs = toolRegistry.getSyncPromptSpecifications();
        for (McpServerFeatures.SyncPromptSpecification promptSpec : promptSpecs) {
            syncServer.addPrompt(promptSpec);
        }

        // TODO: IMPLEMENT WHEN MCP SDK IS COMPLIANT
        // Reference: https://modelcontextprotocol.io/sdk/java/mcp-server#completion-specification
        // Action Point: Uncomment this code when MCP Java SDK includes completion registration
        //
        // The official MCP specification shows that completions should be registered with the server:
        // Register completions with the sync server
        // McpServerFeatures.SyncCompletionSpecification[] completionSpecs =
        // toolRegistry.getSyncCompletionSpecifications();
        // for (McpServerFeatures.SyncCompletionSpecification completionSpec : completionSpecs) {
        // syncServer.addCompletion(completionSpec);
        // }

        logger.debug("MCP sync server created successfully with {} tools, {} resources, {} prompts", toolSpecs.length,
                resourceSpecs.length, promptSpecs.length);
        return syncServer;
    }

    private McpAsyncServer createAsyncServer() throws Exception {
        logger.debug("Creating MCP async server");

        // Get tool specifications from registry
        McpServerFeatures.AsyncToolSpecification[] toolSpecs = toolRegistry.getAsyncToolSpecifications();

        // Filter tools by security if enabled
        if (isSecurityEnabled()) {
            toolSpecs = filterAsyncToolsBySecurity(toolSpecs);
        }

        // Create the async server using McpServer.async
        McpAsyncServer asyncServer = McpServer.async(mcpTransport).serverInfo(serverId, "1.00")
                .capabilities(ServerCapabilities.builder().resources(false, true) // Resource support with list changes
                                                                                  // notifications
                        .tools(true) // Tool support with list changes notifications
                        .prompts(true) // Prompt support with list changes notifications
                        .logging() // Enable logging support (enabled by default with logging level INFO)
                        .completions() // Enable completions support
                        .build())
                .build();

        // Register tools with the async server using reactive patterns
        for (McpServerFeatures.AsyncToolSpecification toolSpec : toolSpecs) {
            asyncServer.addTool(toolSpec)
                    .doOnSuccess(v -> logger.info("Async tool registered: {}", toolSpec.tool().name())).subscribe();
        }

        // Register resources with the async server using reactive patterns
        McpServerFeatures.AsyncResourceSpecification[] resourceSpecs = toolRegistry.getAsyncResourceSpecifications();
        for (McpServerFeatures.AsyncResourceSpecification resourceSpec : resourceSpecs) {
            asyncServer.addResource(resourceSpec)
                    .doOnSuccess(v -> logger.info("Async resource registered: {}", resourceSpec.resource().name()))
                    .subscribe();
        }

        // Register prompts with the async server using reactive patterns
        McpServerFeatures.AsyncPromptSpecification[] promptSpecs = toolRegistry.getAsyncPromptSpecifications();
        for (McpServerFeatures.AsyncPromptSpecification promptSpec : promptSpecs) {
            asyncServer.addPrompt(promptSpec)
                    .doOnSuccess(v -> logger.info("Async prompt registered: {}", promptSpec.prompt().name()))
                    .subscribe();
        }

        // TODO: IMPLEMENT WHEN MCP SDK IS COMPLIANT
        // Reference: https://modelcontextprotocol.io/sdk/java/mcp-server#completion-specification
        // Action Point: Uncomment this code when MCP Java SDK includes async completion registration
        //
        // The official MCP specification shows that completions should be registered with the server:
        // var mcpServer = McpServer.async(mcpServerTransportProvider)
        // .capabilities(ServerCapabilities.builder()
        // .completions() // enable completions support
        // .build())
        // .completions(new McpServerFeatures.AsyncCompletionSpecification( // register completion specification
        // new McpSchema.PromptReference("code_review"), asyncCompletionSpecification))
        // .build();
        //
        // Register completions with the async server using reactive patterns
        // McpServerFeatures.AsyncCompletionSpecification[] completionSpecs =
        // toolRegistry.getAsyncCompletionSpecifications();
        // for (McpServerFeatures.AsyncCompletionSpecification completionSpec : completionSpecs) {
        // asyncServer.addCompletion(completionSpec)
        // .doOnSuccess(v -> logger.info("Async completion registered: {}", completionSpec.promptReference().name()))
        // .subscribe();
        // }

        logger.debug("MCP async server created successfully with {} tools, {} resources, {} prompts", toolSpecs.length,
                resourceSpecs.length, promptSpecs.length);
        return asyncServer;
    }

    private McpServerFeatures.SyncToolSpecification[] filterToolsBySecurity(
            McpServerFeatures.SyncToolSpecification[] toolSpecs) {
        DefaultToolSecurityService manager = securityManager;
        if (manager != null) {
            return manager.filterSyncTools(toolSpecs);
        }
        return toolSpecs;
    }

    private McpServerFeatures.AsyncToolSpecification[] filterAsyncToolsBySecurity(
            McpServerFeatures.AsyncToolSpecification[] toolSpecs) {
        DefaultToolSecurityService manager = securityManager;
        if (manager != null) {
            return manager.filterAsyncTools(toolSpecs);
        }
        return toolSpecs;
    }

    private void startMCPServer() throws Exception {
        logger.debug("Starting MCP Tool server");

        if (configuration.isAsyncEnabled()) {
            startAsyncServer();
        } else {
            startSyncServer();
        }
    }

    private void startSyncServer() throws Exception {
        logger.debug("Starting MCP sync server");

        if (mcpSyncServer == null) {
            mcpSyncServer = createSyncServer();
        }

        // Start the sync server
        if (mcpSyncServer != null) {
            // Sync servers are created and ready to use - no explicit start needed
            // The server runs until close() is called
            logger.debug("MCP sync server started successfully");
        } else {
            logger.warn("MCP sync server is null - using placeholder implementation");
        }
    }

    private void startAsyncServer() throws Exception {
        logger.debug("Starting MCP async server");

        if (mcpAsyncServer == null) {
            mcpAsyncServer = createAsyncServer();
        }

        // Start the async server
        if (mcpAsyncServer != null) {
            // Async servers are created and ready to use - no explicit start needed
            // Tools are registered during creation with reactive patterns
            logger.debug("MCP async server started successfully");
        } else {
            logger.warn("MCP async server is null - using placeholder implementation");
        }
    }

    private void stopMCPServer() throws Exception {
        logger.debug("Stopping MCP Tool server");

        if (configuration.isAsyncEnabled()) {
            stopAsyncServer();
        } else {
            stopSyncServer();
        }
    }

    private void stopSyncServer() throws Exception {
        logger.debug("Stopping MCP sync server");

        if (mcpSyncServer != null) {
            // Close the sync server
            mcpSyncServer.close();
            mcpSyncServer = null;
            logger.debug("MCP sync server stopped");
        } else {
            logger.debug("MCP sync server was already null");
        }
    }

    private void stopAsyncServer() throws Exception {
        logger.debug("Stopping MCP async server");

        if (mcpAsyncServer != null) {
            // Close the async server
            mcpAsyncServer.close();
            mcpAsyncServer = null;
            logger.debug("MCP async server stopped");
        } else {
            logger.debug("MCP async server was already null");
        }
    }

    @Override
    public String toString() {
        return String.format("ToolServer{serverId='%s', state=%s, transportType=%s, healthy=%s}", serverId, state.get(),
                currentTransportType, transportHealthy);
    }

    protected void setMetricsService(MetricsService metricsService) {
        this.metricsService = metricsService;
        logger.debug("MetricsService set for DefaultToolServer");
    }

    protected void unsetMetricsService(MetricsService metricsService) {
        this.metricsService = null;
        logger.debug("MetricsService unset for DefaultToolServer");
    }

    /**
     * Record metrics for transport operations
     */
    private void recordMetrics(String operation, boolean success, long durationNanos) {
        MetricsService metrics = metricsService;
        if (metrics != null) {
            try {
                metrics.recordOperation("tool-server-transport", operation, success,
                        java.time.Duration.ofNanos(durationNanos));
            } catch (Exception e) {
                logger.debug("Failed to record metrics for {}.{}: {}", "tool-server-transport", operation,
                        e.getMessage());
            }
        } else {
            logger.debug("MetricsService not available, cannot record metrics for operation: {}", operation);
        }
    }
}
