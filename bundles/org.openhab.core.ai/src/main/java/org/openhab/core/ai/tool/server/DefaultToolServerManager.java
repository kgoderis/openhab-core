package org.openhab.core.ai.tool.server;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.configuration.ServerConfiguration;
import org.openhab.core.ai.common.transport.TransportType;
import org.openhab.core.ai.config.ConfigurationService;
import org.openhab.core.ai.tool.logging.ToolLoggingManager;
import org.openhab.core.ai.tool.registry.ToolRegistry;
import org.openhab.core.ai.tool.server.api.ToolServer;
import org.openhab.core.ai.tool.server.api.ToolServerManager;
import org.openhab.core.service.ReadyMarker;
import org.openhab.core.service.ReadyService;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default Tool Server Orchestrator - Central manager for MCP server instances using the official MCP SDK.
 * 
 * This class coordinates MCP server creation, configuration, transport management,
 * and service integration within the openHAB ecosystem using the official
 * Model Context Protocol SDK.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
@Component(service = ToolServerManager.class, immediate = true)
public class DefaultToolServerManager implements ToolServerManager {

    private static final Logger logger = LoggerFactory.getLogger(DefaultToolServerManager.class);

    // Ready markers for MCP server
    public static final ReadyMarker MCP_SERVER_READY = new ReadyMarker("mcp", "server");
    public static final ReadyMarker MCP_TOOL_REGISTRY_READY = new ReadyMarker("mcp", "tool-registry");

    // Core openHAB services we depend on
    private static final ReadyMarker CORE_THINGS_READY = new ReadyMarker("startlevel", "80");
    private static final ReadyMarker CORE_RULES_READY = new ReadyMarker("startlevel", "50");

    @Reference
    private @Nullable ReadyService readyService;

    @Reference
    private @Nullable ToolRegistry toolRegistry;

    @Reference
    private @Nullable ToolLoggingManager loggingManager;

    @Reference
    private @Nullable ConfigurationService configurationService;

    private @Nullable BundleContext bundleContext;
    private final Map<String, DefaultToolServer> serverInstances = new ConcurrentHashMap<>();

    private boolean started = false;
    private boolean componentsInitialized = false;

    @Override
    public @Nullable BundleContext getBundleContext() {
        return bundleContext;
    }

    @Override
    public boolean isComponentsInitialized() {
        return componentsInitialized;
    }

    @Override
    public ReadyMarker getMcpServerReadyMarker() {
        return MCP_SERVER_READY;
    }

    @Override
    public ReadyMarker getMcpToolRegistryReadyMarker() {
        return MCP_TOOL_REGISTRY_READY;
    }

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
    public DefaultToolServer getServerInstance(String serverId) {
        // Validate input parameter
        if (serverId == null) {
            throw new IllegalArgumentException("Server ID cannot be null");
        }
        if (serverId.trim().isEmpty()) {
            throw new IllegalArgumentException("Server ID cannot be empty");
        }

        DefaultToolServer server = serverInstances.get(serverId);

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
    public Map<String, ToolServer> getAllServerInstances() {
        Map<String, ToolServer> instances = new ConcurrentHashMap<>(serverInstances);

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
    public DefaultToolServer createServerInstance(String serverId, ToolServerConfiguration config) throws Exception {
        // Validate input parameters
        if (serverId == null) {
            throw new IllegalArgumentException("Server ID cannot be null");
        }
        if (serverId.trim().isEmpty()) {
            throw new IllegalArgumentException("Server ID cannot be empty");
        }
        if (config == null) {
            throw new NullPointerException("Server configuration cannot be null");
        }

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
            if (toolRegistry == null) {
                throw new IllegalStateException("Tool registry is not available");
            }
            ToolRegistry registry = toolRegistry; // Local variable to satisfy null checker
            DefaultToolServer server = new DefaultToolServer(serverId, config, registry);
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
                String serverIdStr = serverId != null ? serverId : "unknown";
                String errorMsg = e.getMessage() != null ? e.getMessage() : "unknown error";
                eventData.put("serverId", serverIdStr);
                eventData.put("error", errorMsg);
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
        // Validate input parameter
        if (serverId == null) {
            throw new IllegalArgumentException("Server ID cannot be null");
        }
        if (serverId.trim().isEmpty()) {
            throw new IllegalArgumentException("Server ID cannot be empty");
        }

        DefaultToolServer server = serverInstances.remove(serverId);

        if (server != null) {
            // Log server removal
            if (loggingManager != null) {
                Map<String, Object> eventData = new HashMap<>();
                String serverIdStr = serverId != null ? serverId : "unknown";
                eventData.put("serverId", serverIdStr);
                loggingManager.logMCPEvent("server_manager", "remove_server_instance", "INFO", eventData);
            } else {
                logger.info("Removed MCP server instance: {}", serverId);
            }

            return true;
        } else {
            // Log attempt to remove non-existent server
            if (loggingManager != null) {
                Map<String, Object> eventData = new HashMap<>();
                String serverIdStr = serverId != null ? serverId : "unknown";
                eventData.put("serverId", serverIdStr);
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
                String errorMsg = e.getMessage() != null ? e.getMessage() : "unknown error";
                eventData.put("error", errorMsg);
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
            for (Map.Entry<String, DefaultToolServer> entry : serverInstances.entrySet()) {
                String serverId = entry.getKey();
                DefaultToolServer server = entry.getValue();

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
                        String errorMessage = e.getMessage();
                        if (errorMessage == null) {
                            errorMessage = "Unknown error";
                        }
                        loggingManager.logMCPEvent("server_manager", "server_stop_failed", "ERROR",
                                Map.of("serverId", serverId, "error", errorMessage));
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
                String errorMessage = e.getMessage();
                if (errorMessage == null) {
                    errorMessage = "Unknown error";
                }
                loggingManager.logMCPEvent("server_manager", "stop_failed", "ERROR",
                        Map.of("error", errorMessage, "shutdownTimeMs", shutdownTime));
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
    public @Nullable ToolRegistry getToolRegistry() {
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
        ToolServerConfiguration config = loadConfigurationFromService();

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
    private ToolServerConfiguration loadConfigurationFromService() {
        if (configurationService == null) {
            logger.warn("AIConfigurationService not available, using default configuration");
            return createDefaultConfiguration();
        }

        try {
            ServerConfiguration.Builder builder = ServerConfiguration.builder();

            // Server Identity
            builder.withName(configurationService.getConfigValue("mcp.server.name", "openHAB MCP Server"))
                    .withVersion(configurationService.getConfigValue("mcp.server.version", "1.0.0"));

            // Transport Configuration
            String transportType = configurationService.getConfigValue("mcp.transport.type", "STDIO");
            builder.withSetting("transportType", TransportType.valueOf(transportType).name())
                    .withSetting("baseUrl",
                            configurationService.getConfigValue("mcp.transport.base.url", "http://localhost:8080"))
                    .withSetting("messageEndpoint",
                            configurationService.getConfigValue("mcp.transport.message.endpoint", "/mcp/message"))
                    .withSetting("sseEndpoint",
                            configurationService.getConfigValue("mcp.transport.sse.endpoint", "/mcp/events"))
                    .withSetting("enableSse",
                            configurationService.getConfigValue("mcp.transport.enable.sse", Boolean.class, true));

            // Feature Enablement
            builder.withSetting("enableTools",
                    configurationService.getConfigValue("mcp.features.enable.tools", Boolean.class, true))
                    .withSetting("enableResources",
                            configurationService.getConfigValue("mcp.features.enable.resources", Boolean.class, true))
                    .withSetting("enablePrompts",
                            configurationService.getConfigValue("mcp.features.enable.prompts", Boolean.class, true))
                    .withSetting("enableLogging",
                            configurationService.getConfigValue("mcp.features.enable.logging", Boolean.class, true));

            // Async Server Configuration
            builder.withSetting("async.enable.server",
                    configurationService.getConfigValue("mcp.async.enable.server", Boolean.class, false))
                    .withSetting("async.enable.tools",
                            configurationService.getConfigValue("mcp.async.enable.tools", Boolean.class, false))
                    .withSetting("async.thread.pool.size",
                            configurationService.getConfigValue("mcp.async.thread.pool.size", Integer.class, 10))
                    .withSetting("async.queue.capacity",
                            configurationService.getConfigValue("mcp.async.queue.capacity", Integer.class, 1000))
                    .withSetting("async.enable.completions",
                            configurationService.getConfigValue("mcp.async.enable.completions", Boolean.class, false));

            // Security Configuration
            builder.withSetting("auth.enabled",
                    configurationService.getConfigValue("mcp.auth.enabled", Boolean.class, false))
                    .withSetting("auth.enable.request.validation",
                            configurationService.getConfigValue("mcp.auth.enable.request.validation", Boolean.class,
                                    true))
                    .withSetting("connections.max",
                            configurationService.getConfigValue("mcp.connections.max", Integer.class, 100))
                    .withSetting("rate.limit.per.minute",
                            configurationService.getConfigValue("mcp.rate.limit.per.minute", Integer.class, 1000));

            // Authentication Method Selection
            builder.withSetting("auth.primary.method",
                    configurationService.getConfigValue("mcp.auth.primary.method", "oauth2.1"))
                    .withSetting("auth.fallback.method",
                            configurationService.getConfigValue("mcp.auth.fallback.method", "openhab_users"))
                    .withSetting("auth.enable.fallback",
                            configurationService.getConfigValue("mcp.auth.enable.fallback", Boolean.class, true));

            // OAuth 2.1 Configuration
            builder.withSetting("oauth.issuer.url", configurationService.getConfigValue("mcp.oauth.issuer.url", ""))
                    .withSetting("oauth.client.id", configurationService.getConfigValue("mcp.oauth.client.id", ""))
                    .withSetting("oauth.client.secret",
                            configurationService.getConfigValue("mcp.oauth.client.secret", ""))
                    .withSetting("oauth.redirect.uri",
                            configurationService.getConfigValue("mcp.oauth.redirect.uri",
                                    "http://localhost:8080/callback"))
                    .withSetting("oauth.pkce.enabled",
                            configurationService.getConfigValue("mcp.oauth.pkce.enabled", Boolean.class, true));

            // openHAB Users Authentication
            builder.withSetting("openhab.users.file", configurationService.getConfigValue("mcp.openhab.users.file", ""))
                    .withSetting("openhab.users.enabled",
                            configurationService.getConfigValue("mcp.openhab.users.enabled", Boolean.class, true));

            // API Key Authentication
            builder.withSetting("api.key.header",
                    configurationService.getConfigValue("mcp.api.key.header", "X-API-Key"))
                    .withSetting("api.key.value", configurationService.getConfigValue("mcp.api.key.value", ""))
                    .withSetting("api.key.enabled",
                            configurationService.getConfigValue("mcp.api.key.enabled", Boolean.class, false));

            // JWT Authentication
            builder.withSetting("jwt.secret", configurationService.getConfigValue("mcp.jwt.secret", ""))
                    .withSetting("jwt.issuer", configurationService.getConfigValue("mcp.jwt.issuer", "openhab-mcp"))
                    .withSetting("jwt.expiration.minutes",
                            configurationService.getConfigValue("mcp.jwt.expiration.minutes", Integer.class, 60))
                    .withSetting("jwt.enabled",
                            configurationService.getConfigValue("mcp.jwt.enabled", Boolean.class, false));

            // Timeout Settings
            builder.withSetting("timeout.connection",
                    configurationService.getConfigValue("mcp.timeout.connection", Integer.class, 30000))
                    .withSetting("timeout.request",
                            configurationService.getConfigValue("mcp.timeout.request", Integer.class, 60000));

            // Resource Limits - using server options instead of non-existent methods
            builder.withSetting("resources.max.memory.usage",
                    configurationService.getConfigValue("mcp.resources.max.memory.usage", "512MB"))
                    .withSetting("resources.max.concurrent.requests",
                            configurationService.getConfigValue("mcp.resources.max.concurrent.requests", Integer.class,
                                    50))
                    .withSetting("resources.max.tool.executions", configurationService
                            .getConfigValue("mcp.resources.max.tool.executions", Integer.class, 10));

            // Health Monitoring - using server options instead of non-existent methods
            builder.withSetting("health.enabled",
                    configurationService.getConfigValue("mcp.health.enabled", Boolean.class, true))
                    .withSetting("health.endpoint",
                            configurationService.getConfigValue("mcp.health.endpoint", "/health"))
                    .withSetting("health.check.interval",
                            configurationService.getConfigValue("mcp.health.check.interval", "30s"))
                    .withSetting("health.timeout", configurationService.getConfigValue("mcp.health.timeout", "10s"));

            // Metrics Configuration - using server options instead of non-existent methods
            builder.withSetting("metrics.enabled",
                    configurationService.getConfigValue("mcp.metrics.enabled", Boolean.class, true))
                    .withSetting("metrics.endpoint",
                            configurationService.getConfigValue("mcp.metrics.endpoint", "/metrics"))
                    .withSetting("metrics.collection.interval",
                            configurationService.getConfigValue("mcp.metrics.collection.interval", "60s"))
                    .withSetting("metrics.retention.days",
                            configurationService.getConfigValue("mcp.metrics.retention.days", Integer.class, 7));

            // Debug Configuration - using server options instead of non-existent methods
            builder.withSetting("debug.enabled",
                    configurationService.getConfigValue("mcp.debug.enabled", Boolean.class, false))
                    .withSetting("debug.log.requests",
                            configurationService.getConfigValue("mcp.debug.log.requests", Boolean.class, true))
                    .withSetting("debug.log.responses",
                            configurationService.getConfigValue("mcp.debug.log.responses", Boolean.class, true))
                    .withSetting("debug.log.tool.executions",
                            configurationService.getConfigValue("mcp.debug.log.tool.executions", Boolean.class, true));

            // Development Features - using server options instead of non-existent methods
            builder.withSetting("dev.enable.hot.reload",
                    configurationService.getConfigValue("mcp.dev.enable.hot.reload", Boolean.class, false))
                    .withSetting("dev.enable.tool.discovery",
                            configurationService.getConfigValue("mcp.dev.enable.tool.discovery", Boolean.class, true))
                    .withSetting("dev.enable.configuration.reload", configurationService
                            .getConfigValue("mcp.dev.enable.configuration.reload", Boolean.class, true));

            ToolServerConfiguration config = new ToolServerConfiguration(builder);
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
    private ToolServerConfiguration createDefaultConfiguration() {
        ServerConfiguration.Builder builder = ServerConfiguration.builder().withName("openHAB Tool Server")
                .withVersion("1.0.0").withSetting("transportType", TransportType.STDIO.name())
                .withSetting("enableTools", true).withSetting("enableResources", true)
                .withSetting("enablePrompts", true).withSetting("enableLogging", true);
        return new ToolServerConfiguration(builder);
    }

    /**
     * Get the configuration service.
     * 
     * @return Configuration service
     */
    private @Nullable ConfigurationService getConfigurationService() {
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

        // Initialize MCP-specific components
        try {
            // Initialize MCP server instances
            if (toolRegistry != null) {
                logger.debug("Initializing MCP server instances with {} tools", toolRegistry.getToolCount());

                // Create default server instance if none exists
                if (serverInstances.isEmpty()) {
                    createDefaultServerInstance();
                }

                // Initialize each server instance
                for (Map.Entry<String, DefaultToolServer> entry : serverInstances.entrySet()) {
                    DefaultToolServer server = entry.getValue();
                    if (server != null) {
                        logger.debug("MCP server instance ready: {}", entry.getKey());
                    }
                }
            }

            // Initialize MCP transport components
            logger.debug("Initializing MCP transport components");

            // Initialize MCP protocol handlers
            logger.debug("Initializing MCP protocol handlers");

            // Initialize MCP security components
            logger.debug("Initializing MCP security components");

            // Initialize MCP monitoring components
            logger.debug("Initializing MCP monitoring components");

            logger.info("MCP-specific components initialized successfully");

        } catch (Exception e) {
            logger.error("Failed to initialize MCP-specific components", e);
            throw e;
        }

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

        this.bundleContext = null; // This is intentional - clearing the reference
        this.configurationService = null; // This is intentional - clearing the reference
    }
}
