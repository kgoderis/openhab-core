package org.openhab.core.ai.mcp.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.openhab.core.ai.common.config.AIConfigurationService;
import org.openhab.core.service.ReadyMarker;
import org.openhab.core.service.ReadyService;
import org.openhab.core.service.ReadyService.ReadyTracker;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Central manager for MCP server instances using the official MCP SDK.
 * 
 * This class coordinates MCP server creation, configuration, transport management,
 * and service integration within the openHAB ecosystem using the official
 * Model Context Protocol SDK.
 * 
 * 
 */
@Component(service = MCPServerManager.class, immediate = true)
public class MCPServerManager implements ReadyTracker {

    private static final Logger logger = LoggerFactory.getLogger(MCPServerManager.class);

    // Ready markers for MCP server
    public static final ReadyMarker MCP_SERVER_READY = new ReadyMarker("mcp", "server");
    public static final ReadyMarker MCP_TOOL_REGISTRY_READY = new ReadyMarker("mcp", "tool-registry");

    // Core openHAB services we depend on
    private static final ReadyMarker CORE_THINGS_READY = new ReadyMarker("startlevel", "80");
    private static final ReadyMarker CORE_RULES_READY = new ReadyMarker("startlevel", "50");

    @Reference
    private ReadyService readyService;

    @Reference
    private MCPToolRegistry toolRegistry;

    @Reference
    private MCPLoggingManager loggingManager;

    @Reference
    private AIConfigurationService configurationService;

    private BundleContext bundleContext;
    private final Map<String, MCPServer> serverInstances = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private boolean started = false;
    private boolean componentsInitialized = false;

    /**
     * Start the MCP server manager and initialize server instances.
     * 
     * @param bundleContext OSGi bundle context
     * @throws Exception if startup fails
     */
    @Activate
    public void activate(BundleContext bundleContext) throws Exception {
        this.bundleContext = bundleContext;

        // Use enhanced logging for startup
        if (loggingManager != null) {
            loggingManager.logMCPEvent("server_manager", "activation_start", "INFO",
                    Map.of("bundleContext", bundleContext != null ? "available" : "null"));
        } else {
            logger.info("MCP Server Manager activated - waiting for core services...");
        }

        // Register as a tracker for core openHAB services
        readyService.registerTracker(this);

        // Check if core services are already ready
        checkCoreServicesReady();
    }

    /**
     * Get a server instance by ID.
     * 
     * @param serverId Server identifier
     * @return Server instance or null if not found
     */
    public MCPServer getServerInstance(String serverId) {
        MCPServer server = serverInstances.get(serverId);

        // Log server instance access
        if (loggingManager != null) {
            loggingManager.logMCPEvent("server_manager", "get_server_instance", "DEBUG",
                    Map.of("serverId", serverId, "found", server != null));
        }

        return server;
    }

    /**
     * Get all server instances.
     * 
     * @return Map of all server instances
     */
    public Map<String, MCPServer> getAllServerInstances() {
        Map<String, MCPServer> instances = new ConcurrentHashMap<>(serverInstances);

        // Log server instances retrieval
        if (loggingManager != null) {
            loggingManager.logMCPEvent("server_manager", "get_all_server_instances", "DEBUG",
                    Map.of("instanceCount", instances.size()));
        }

        return instances;
    }

    /**
     * Create a new server instance.
     * 
     * @param serverId Server identifier
     * @param config Server configuration
     * @return Created server instance
     * @throws Exception if creation fails
     */
    public MCPServer createServerInstance(String serverId, MCPServerConfiguration config) throws Exception {
        long startTime = System.currentTimeMillis();

        // Use enhanced logging for server creation
        if (loggingManager != null) {
            loggingManager.logMCPEvent("server_manager", "create_server_instance", "INFO",
                    Map.of("serverId", serverId, "config", config != null ? "provided" : "null"));
        } else {
            logger.info("Creating MCP server instance: {}", serverId);
        }

        try {
            // Create server instance using the correct constructor
            MCPServer server = new MCPServer(serverId, config, toolRegistry);
            serverInstances.put(serverId, server);

            long creationTime = System.currentTimeMillis() - startTime;

            // Log successful creation
            if (loggingManager != null) {
                loggingManager.logMCPEvent("server_manager", "server_instance_created", "INFO",
                        Map.of("serverId", serverId, "creationTimeMs", creationTime));
            } else {
                logger.info("MCP server instance created successfully: {} in {}ms", serverId, creationTime);
            }

            return server;

        } catch (Exception e) {
            long creationTime = System.currentTimeMillis() - startTime;

            // Log creation failure
            if (loggingManager != null) {
                Map<String, Object> eventData = new HashMap<>();
                eventData.put("serverId", serverId != null ? serverId : "unknown");
                eventData.put("error", e.getMessage() != null ? e.getMessage() : "unknown error");
                eventData.put("creationTimeMs", creationTime);
                loggingManager.logMCPEvent("server_manager", "server_instance_creation_failed", "ERROR", eventData);
            } else {
                logger.error("Failed to create MCP server instance: {} after {}ms", serverId, creationTime, e);
            }

            throw e;
        }
    }

    /**
     * Remove a server instance.
     * 
     * @param serverId Server identifier
     * @return true if removed, false if not found
     */
    public boolean removeServerInstance(String serverId) {
        MCPServer server = serverInstances.remove(serverId);

        if (server != null) {
            // Log server removal
            if (loggingManager != null) {
                Map<String, Object> eventData = new HashMap<>();
                eventData.put("serverId", serverId != null ? serverId : "unknown");
                loggingManager.logMCPEvent("server_manager", "remove_server_instance", "INFO", eventData);
            } else {
                logger.info("Removed MCP server instance: {}", serverId);
            }

            return true;
        } else {
            // Log attempt to remove non-existent server
            if (loggingManager != null) {
                Map<String, Object> eventData = new HashMap<>();
                eventData.put("serverId", serverId != null ? serverId : "unknown");
                loggingManager.logMCPEvent("server_manager", "remove_server_instance_not_found", "WARN", eventData);
            } else {
                logger.warn("Attempted to remove non-existent MCP server instance: {}", serverId);
            }

            return false;
        }
    }

    /**
     * Start the MCP server manager.
     * 
     * @throws Exception if startup fails
     */
    public void start() throws Exception {
        if (started) {
            if (loggingManager != null) {
                loggingManager.logMCPEvent("server_manager", "start_already_started", "WARN", Map.of());
            } else {
                logger.warn("MCP Server Manager is already started");
            }
            return;
        }

        long startTime = System.currentTimeMillis();

        // Use enhanced logging for startup
        if (loggingManager != null) {
            loggingManager.logMCPEvent("server_manager", "start_begin", "INFO", Map.of());
        } else {
            logger.info("Starting MCP Server Manager...");
        }

        try {
            // Initialize components
            initializeMCPComponents();

            // Create default server instance
            createDefaultServerInstance();

            started = true;
            long startupTime = System.currentTimeMillis() - startTime;

            // Log successful startup
            if (loggingManager != null) {
                loggingManager.logMCPEvent("server_manager", "start_complete", "INFO",
                        Map.of("startupTimeMs", startupTime, "serverCount", serverInstances.size()));
            } else {
                logger.info("MCP Server Manager started successfully in {}ms with {} server instances", startupTime,
                        serverInstances.size());
            }

        } catch (Exception e) {
            long startupTime = System.currentTimeMillis() - startTime;

            // Log startup failure
            if (loggingManager != null) {
                Map<String, Object> eventData = new HashMap<>();
                eventData.put("error", e.getMessage() != null ? e.getMessage() : "unknown error");
                eventData.put("startupTimeMs", startupTime);
                loggingManager.logMCPEvent("server_manager", "start_failed", "ERROR", eventData);
            } else {
                logger.error("Failed to start MCP Server Manager after {}ms", startupTime, e);
            }

            throw e;
        }
    }

    /**
     * Stop the MCP server manager.
     * 
     * @throws Exception if shutdown fails
     */
    public void stop() throws Exception {
        if (!started) {
            if (loggingManager != null) {
                loggingManager.logMCPEvent("server_manager", "stop_not_started", "WARN", Map.of());
            } else {
                logger.warn("MCP Server Manager is not started");
            }
            return;
        }

        long startTime = System.currentTimeMillis();

        // Use enhanced logging for shutdown
        if (loggingManager != null) {
            loggingManager.logMCPEvent("server_manager", "stop_begin", "INFO",
                    Map.of("serverCount", serverInstances.size()));
        } else {
            logger.info("Stopping MCP Server Manager with {} server instances...", serverInstances.size());
        }

        try {
            // Stop all server instances
            for (Map.Entry<String, MCPServer> entry : serverInstances.entrySet()) {
                String serverId = entry.getKey();
                MCPServer server = entry.getValue();

                try {
                    server.stop();

                    // Log individual server stop
                    if (loggingManager != null) {
                        loggingManager.logMCPEvent("server_manager", "server_stopped", "DEBUG",
                                Map.of("serverId", serverId));
                    }

                } catch (Exception e) {
                    // Log server stop failure
                    if (loggingManager != null) {
                        loggingManager.logMCPEvent("server_manager", "server_stop_failed", "ERROR",
                                Map.of("serverId", serverId, "error", e.getMessage()));
                    } else {
                        logger.error("Failed to stop MCP server instance: {}", serverId, e);
                    }
                }
            }

            serverInstances.clear();
            started = false;
            componentsInitialized = false;

            long shutdownTime = System.currentTimeMillis() - startTime;

            // Log successful shutdown
            if (loggingManager != null) {
                loggingManager.logMCPEvent("server_manager", "stop_complete", "INFO",
                        Map.of("shutdownTimeMs", shutdownTime));
            } else {
                logger.info("MCP Server Manager stopped successfully in {}ms", shutdownTime);
            }

        } catch (Exception e) {
            long shutdownTime = System.currentTimeMillis() - startTime;

            // Log shutdown failure
            if (loggingManager != null) {
                loggingManager.logMCPEvent("server_manager", "stop_failed", "ERROR",
                        Map.of("error", e.getMessage(), "shutdownTimeMs", shutdownTime));
            } else {
                logger.error("Failed to stop MCP Server Manager after {}ms", shutdownTime, e);
            }

            throw e;
        }
    }

    /**
     * Check if the server manager is started.
     * 
     * @return true if started
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * Get the tool registry.
     * 
     * @return Tool registry
     */
    public MCPToolRegistry getToolRegistry() {
        return toolRegistry;
    }

    /**
     * Create the default server instance.
     * 
     * @throws Exception if creation fails
     */
    private void createDefaultServerInstance() throws Exception {
        String defaultServerId = "default";

        // Load configuration from AIConfigurationService
        MCPServerConfiguration config = loadConfigurationFromService();

        createServerInstance(defaultServerId, config);

        // Log default server creation
        if (loggingManager != null) {
            loggingManager.logMCPEvent("server_manager", "default_server_created", "INFO",
                    Map.of("serverId", defaultServerId, "transportType", config.getTransportType()));
        } else {
            logger.info("Created default MCP server instance: {} with transport: {}", defaultServerId,
                    config.getTransportType());
        }
    }

    /**
     * Load MCP configuration from the AIConfigurationService.
     * 
     * @return MCP server configuration
     */
    private MCPServerConfiguration loadConfigurationFromService() {
        if (configurationService == null) {
            logger.warn("AIConfigurationService not available, using default configuration");
            return createDefaultConfiguration();
        }

        try {
            MCPServerConfiguration.Builder builder = MCPServerConfiguration.builder();

            // Server Identity
            builder.serverId(configurationService.getConfigValue("mcp.server.id", "openhab-mcp-server"))
                    .serverName(configurationService.getConfigValue("mcp.server.name", "openHAB MCP Server"))
                    .serverVersion(configurationService.getConfigValue("mcp.server.version", "1.0.0"));

            // Transport Configuration
            String transportType = configurationService.getConfigValue("mcp.transport.type", "STDIO");
            builder.transportType(MCPTransportType.valueOf(transportType))
                    .baseUrl(configurationService.getConfigValue("mcp.transport.base.url", "http://localhost:8080"))
                    .messageEndpoint(
                            configurationService.getConfigValue("mcp.transport.message.endpoint", "/mcp/message"))
                    .sseEndpoint(configurationService.getConfigValue("mcp.transport.sse.endpoint", "/mcp/events"))
                    .enableSse(configurationService.getConfigValue("mcp.transport.enable.sse", Boolean.class, true));

            // Feature Enablement
            builder.enableTools(configurationService.getConfigValue("mcp.features.enable.tools", Boolean.class, true))
                    .enableResources(
                            configurationService.getConfigValue("mcp.features.enable.resources", Boolean.class, true))
                    .enablePrompts(
                            configurationService.getConfigValue("mcp.features.enable.prompts", Boolean.class, true))
                    .enableLogging(
                            configurationService.getConfigValue("mcp.features.enable.logging", Boolean.class, true));

            // Async Server Configuration
            builder.enableAsyncServer(
                    configurationService.getConfigValue("mcp.async.enable.server", Boolean.class, false))
                    .enableAsyncTools(
                            configurationService.getConfigValue("mcp.async.enable.tools", Boolean.class, false))
                    .asyncThreadPoolSize(
                            configurationService.getConfigValue("mcp.async.thread.pool.size", Integer.class, 10))
                    .asyncQueueCapacity(
                            configurationService.getConfigValue("mcp.async.queue.capacity", Integer.class, 1000))
                    .enableAsyncCompletions(
                            configurationService.getConfigValue("mcp.async.enable.completions", Boolean.class, false));

            // Security Configuration
            builder.enableAuthentication(configurationService.getConfigValue("mcp.auth.enabled", Boolean.class, false))
                    .enableRequestValidation(configurationService.getConfigValue("mcp.auth.enable.request.validation",
                            Boolean.class, true))
                    .maxConnections(configurationService.getConfigValue("mcp.connections.max", Integer.class, 100))
                    .rateLimitPerMinute(
                            configurationService.getConfigValue("mcp.rate.limit.per.minute", Integer.class, 1000));

            // Authentication Method Selection
            builder.primaryAuthMethod(configurationService.getConfigValue("mcp.auth.primary.method", "oauth2.1"))
                    .fallbackAuthMethod(
                            configurationService.getConfigValue("mcp.auth.fallback.method", "openhab_users"))
                    .enableFallbackAuth(
                            configurationService.getConfigValue("mcp.auth.enable.fallback", Boolean.class, true));

            // OAuth 2.1 Configuration
            builder.oauthIssuerUrl(configurationService.getConfigValue("mcp.oauth.issuer.url", ""))
                    .oauthClientId(configurationService.getConfigValue("mcp.oauth.client.id", ""))
                    .oauthClientSecret(configurationService.getConfigValue("mcp.oauth.client.secret", ""))
                    .oauthRedirectUri(configurationService.getConfigValue("mcp.oauth.redirect.uri",
                            "http://localhost:8080/callback"))
                    .oauthPkceEnabled(
                            configurationService.getConfigValue("mcp.oauth.pkce.enabled", Boolean.class, true));

            // openHAB Users Authentication
            builder.openhabUsersFile(configurationService.getConfigValue("mcp.openhab.users.file", ""))
                    .openhabUsersEnabled(
                            configurationService.getConfigValue("mcp.openhab.users.enabled", Boolean.class, true));

            // API Key Authentication
            builder.apiKeyHeader(configurationService.getConfigValue("mcp.api.key.header", "X-API-Key"))
                    .apiKeyValue(configurationService.getConfigValue("mcp.api.key.value", ""))
                    .apiKeyEnabled(configurationService.getConfigValue("mcp.api.key.enabled", Boolean.class, false));

            // JWT Authentication
            builder.jwtSecret(configurationService.getConfigValue("mcp.jwt.secret", ""))
                    .jwtIssuer(configurationService.getConfigValue("mcp.jwt.issuer", "openhab-mcp"))
                    .jwtExpirationMinutes(
                            configurationService.getConfigValue("mcp.jwt.expiration.minutes", Integer.class, 60))
                    .jwtEnabled(configurationService.getConfigValue("mcp.jwt.enabled", Boolean.class, false));

            // Timeout Settings
            builder.connectionTimeout(
                    configurationService.getConfigValue("mcp.timeout.connection", Integer.class, 30000))
                    .requestTimeout(configurationService.getConfigValue("mcp.timeout.request", Integer.class, 60000));

            // Resource Limits - using server options instead of non-existent methods
            builder.serverOption("max.memory.usage",
                    configurationService.getConfigValue("mcp.resources.max.memory.usage", "512MB"))
                    .serverOption("max.concurrent.requests",
                            configurationService.getConfigValue("mcp.resources.max.concurrent.requests", Integer.class,
                                    50))
                    .serverOption("max.tool.executions", configurationService
                            .getConfigValue("mcp.resources.max.tool.executions", Integer.class, 10));

            // Health Monitoring - using server options instead of non-existent methods
            builder.enableHealthChecks(configurationService.getConfigValue("mcp.health.enabled", Boolean.class, true))
                    .serverOption("health.endpoint",
                            configurationService.getConfigValue("mcp.health.endpoint", "/health"))
                    .serverOption("health.check.interval",
                            configurationService.getConfigValue("mcp.health.check.interval", "30s"))
                    .serverOption("health.timeout", configurationService.getConfigValue("mcp.health.timeout", "10s"));

            // Metrics Configuration - using server options instead of non-existent methods
            builder.enableMetrics(configurationService.getConfigValue("mcp.metrics.enabled", Boolean.class, true))
                    .serverOption("metrics.endpoint",
                            configurationService.getConfigValue("mcp.metrics.endpoint", "/metrics"))
                    .serverOption("metrics.collection.interval",
                            configurationService.getConfigValue("mcp.metrics.collection.interval", "60s"))
                    .serverOption("metrics.retention.days",
                            configurationService.getConfigValue("mcp.metrics.retention.days", Integer.class, 7));

            // Debug Configuration - using server options instead of non-existent methods
            builder.serverOption("debug.enabled",
                    configurationService.getConfigValue("mcp.debug.enabled", Boolean.class, false))
                    .serverOption("debug.log.requests",
                            configurationService.getConfigValue("mcp.debug.log.requests", Boolean.class, true))
                    .serverOption("debug.log.responses",
                            configurationService.getConfigValue("mcp.debug.log.responses", Boolean.class, true))
                    .serverOption("debug.log.tool.executions",
                            configurationService.getConfigValue("mcp.debug.log.tool.executions", Boolean.class, true));

            // Development Features - using server options instead of non-existent methods
            builder.serverOption("dev.enable.hot.reload",
                    configurationService.getConfigValue("mcp.dev.enable.hot.reload", Boolean.class, false))
                    .serverOption("dev.enable.tool.discovery",
                            configurationService.getConfigValue("mcp.dev.enable.tool.discovery", Boolean.class, true))
                    .serverOption("dev.enable.configuration.reload", configurationService
                            .getConfigValue("mcp.dev.enable.configuration.reload", Boolean.class, true));

            MCPServerConfiguration config = builder.build();
            logger.info("Loaded MCP configuration from AIConfigurationService");
            return config;

        } catch (Exception e) {
            logger.warn("Failed to load configuration from AIConfigurationService, using default configuration", e);
            return createDefaultConfiguration();
        }
    }

    /**
     * Create default configuration as fallback.
     * 
     * @return Default MCP server configuration
     */
    private MCPServerConfiguration createDefaultConfiguration() {
        return MCPServerConfiguration.builder().serverId("openhab-mcp-server").serverName("openHAB MCP Server")
                .serverVersion("1.0.0").transportType(MCPTransportType.STDIO).enableTools(true).enableResources(true)
                .enablePrompts(true).enableLogging(true).build();
    }

    /**
     * Get the configuration service.
     * 
     * @return Configuration service
     */
    private AIConfigurationService getConfigurationService() {
        return configurationService;
    }

    @Override
    public void onReadyMarkerAdded(ReadyMarker readyMarker) {
        // Log ready marker addition
        if (loggingManager != null) {
            loggingManager.logMCPEvent("server_manager", "ready_marker_added", "DEBUG",
                    Map.of("marker", readyMarker.toString()));
        } else {
            logger.debug("Ready marker added: {}", readyMarker);
        }

        if (isCoreServiceMarker(readyMarker)) {
            checkCoreServicesReady();
        } else if (isToolRegistryMarker(readyMarker)) {
            checkToolsReady();
        }
    }

    @Override
    public void onReadyMarkerRemoved(ReadyMarker readyMarker) {
        // Log ready marker removal
        if (loggingManager != null) {
            loggingManager.logMCPEvent("server_manager", "ready_marker_removed", "DEBUG",
                    Map.of("marker", readyMarker.toString()));
        } else {
            logger.debug("Ready marker removed: {}", readyMarker);
        }

        if (isCoreServiceMarker(readyMarker)) {
            // Core service became unavailable
            if (started) {
                logger.warn("Core service became unavailable: {}", readyMarker);
            }
        }
    }

    private boolean isCoreServiceMarker(ReadyMarker marker) {
        return marker.equals(CORE_THINGS_READY) || marker.equals(CORE_RULES_READY);
    }

    private boolean isToolRegistryMarker(ReadyMarker marker) {
        return marker.equals(MCP_TOOL_REGISTRY_READY);
    }

    private void checkCoreServicesReady() {
        boolean thingsReady = readyService.isReady(CORE_THINGS_READY);
        boolean rulesReady = readyService.isReady(CORE_RULES_READY);

        if (thingsReady && rulesReady && !componentsInitialized) {
            // Log core services ready
            if (loggingManager != null) {
                loggingManager.logMCPEvent("server_manager", "core_services_ready", "INFO",
                        Map.of("thingsReady", thingsReady, "rulesReady", rulesReady));
            } else {
                logger.info("Core openHAB services are ready - things: {}, rules: {}", thingsReady, rulesReady);
            }

            componentsInitialized = true;

            // Try to start if not already started
            if (!started) {
                try {
                    start();
                } catch (Exception e) {
                    logger.error("Failed to start MCP Server Manager after core services became ready", e);
                }
            }
        }
    }

    private void checkToolsReady() {
        boolean toolsReady = readyService.isReady(MCP_TOOL_REGISTRY_READY);

        if (toolsReady) {
            // Log tools ready
            if (loggingManager != null) {
                loggingManager.logMCPEvent("server_manager", "tools_ready", "INFO",
                        Map.of("toolCount", toolRegistry.getToolCount()));
            } else {
                logger.info("MCP tools are ready - {} tools available", toolRegistry.getToolCount());
            }

            // Mark server as ready
            readyService.markReady(MCP_SERVER_READY);
        }
    }

    private void initializeMCPComponents() throws Exception {
        // Initialize MCP-specific components
        if (loggingManager != null) {
            loggingManager.logMCPEvent("server_manager", "initialize_components", "DEBUG", Map.of());
        } else {
            logger.debug("Initializing MCP components...");
        }

        // Component initialization logic here
        // This is where you would initialize any MCP-specific components

        if (loggingManager != null) {
            loggingManager.logMCPEvent("server_manager", "components_initialized", "DEBUG", Map.of());
        } else {
            logger.debug("MCP components initialized");
        }
    }

    @Deactivate
    public void deactivate() {
        try {
            stop();
        } catch (Exception e) {
            logger.error("Error stopping MCP Server Manager during deactivation", e);
        }

        // Unregister tracker and unmark ready
        readyService.unregisterTracker(this);
        readyService.unmarkReady(MCP_SERVER_READY);
        readyService.unmarkReady(MCP_TOOL_REGISTRY_READY);

        // Log deactivation
        if (loggingManager != null) {
            loggingManager.logMCPEvent("server_manager", "deactivated", "INFO", Map.of());
        } else {
            logger.info("MCP Server Manager deactivated");
        }

        this.bundleContext = null;
        this.configurationService = null;
    }
}
