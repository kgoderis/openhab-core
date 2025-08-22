package org.openhab.core.ai.tool;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.config.ConfigurationException;
import org.openhab.core.ai.config.ConfigurationValidator;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of ToolConfigurationService.
 * 
 * This service manages configuration for the MCP (Model Context Protocol) tool functionality,
 * following the ai.tool.* naming convention and using manual validation.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@Component(service = ToolConfigurationService.class, configurationPid = "org.openhab.ai.tool")
@NonNullByDefault
public class DefaultToolConfigurationService implements ToolConfigurationService {

    private final Logger logger = LoggerFactory.getLogger(DefaultToolConfigurationService.class);
    private final ConfigurationValidator validator = new ConfigurationValidator();

    // Server configuration
    private String serverId = "openhab-tool-server";
    private String serverName = "openHAB Tool Server";
    private String serverVersion = "1.0.0";
    private String serverDescription = "openHAB Tool Server for AI Integration";
    private boolean serverEnabled = true;
    private int serverPort = 8081;

    // Transport configuration
    private String transportType = "STDIO";
    private String transportBaseUrl = "http://localhost:8080";
    private String transportMessageEndpoint = "/tool/message";
    private String transportSseEndpoint = "/tool/events";
    private boolean sseEnabled = true;

    // Feature configuration
    private boolean toolsEnabled = true;
    private boolean resourcesEnabled = true;
    private boolean promptsEnabled = true;
    private boolean loggingEnabled = true;

    // Async configuration
    private boolean asyncServerEnabled = false;
    private boolean asyncToolsEnabled = false;
    private int asyncThreadPoolSize = 10;
    private int asyncQueueCapacity = 1000;
    private boolean asyncCompletionsEnabled = false;

    // Authentication configuration
    private boolean authenticationEnabled = true;
    private boolean requestValidationEnabled = true;
    private String primaryAuthMethod = "oauth2.1";
    private String fallbackAuthMethod = "openhab_users";
    private boolean authFallbackEnabled = true;
    private String authProtocolName = "tool";
    private String authProtocolPermissions = "tool:read,tool:write,tool:execute,tool:admin";
    private int authSessionTimeout = 3600;
    private boolean authSessionRefreshEnabled = true;
    private int authSessionRefreshThreshold = 300;

    // Permission configuration
    private String authReadOperations = "list_tools,list_resources,list_prompts,get_tool,get_resource,get_prompt";
    private String authWriteOperations = "create_tool,create_resource,create_prompt,update_tool,update_resource,update_prompt";
    private String authExecuteOperations = "call_tool,execute_tool,run_tool";
    private String authAdminOperations = "delete_tool,delete_resource,delete_prompt,configure_server,manage_users";

    // Session configuration
    private int maxTools = 100;
    private int maxConcurrentSessions = 10;
    private int sessionCleanupInterval = 300;
    private boolean sessionInvalidationEnabled = true;

    @Activate
    public void activate(Map<String, Object> config) {
        logger.debug("Activating Tool Configuration Service");

        try {
            // Validate configuration before loading
            validator.validateToolConfiguration(config);

            // Load configuration
            loadConfiguration(config);

            logger.info("Tool Configuration Service activated. Server: {}, Port: {}", serverName, serverPort);
        } catch (ConfigurationException e) {
            logger.error("Failed to activate Tool Configuration Service: {}", e.getMessage());
            // Load default configuration as fallback
            loadDefaultConfiguration();
        }
    }

    @Modified
    public void modified(Map<String, Object> config) {
        logger.debug("Modifying Tool Configuration Service");

        try {
            // Validate new configuration
            validator.validateToolConfiguration(config);

            // Reload configuration
            loadConfiguration(config);

            logger.info("Tool Configuration Service modified. Server: {}, Port: {}", serverName, serverPort);
        } catch (ConfigurationException e) {
            logger.error("Failed to modify Tool Configuration Service: {}", e.getMessage());
            // Keep existing configuration
        }
    }

    @Deactivate
    public void deactivate() {
        logger.debug("Deactivating Tool Configuration Service");
        logger.info("Tool Configuration Service deactivated");
    }

    /**
     * Loads configuration from the configuration map.
     * 
     * @param config the configuration map
     */
    private void loadConfiguration(Map<String, Object> config) {
        logger.debug("Loading tool configuration");

        // Server configuration
        serverId = getStringConfig(config, "ai.tool.server.id", "openhab-tool-server");
        serverName = getStringConfig(config, "ai.tool.server.name", "openHAB Tool Server");
        serverVersion = getStringConfig(config, "ai.tool.server.version", "1.0.0");
        serverDescription = getStringConfig(config, "ai.tool.server.description",
                "openHAB Tool Server for AI Integration");
        serverEnabled = getBooleanConfig(config, "ai.tool.server.enabled", true);
        serverPort = getIntConfig(config, "ai.tool.server.port", 8081);

        // Transport configuration
        transportType = getStringConfig(config, "ai.tool.transport.type", "STDIO");
        transportBaseUrl = getStringConfig(config, "ai.tool.transport.base.url", "http://localhost:8080");
        transportMessageEndpoint = getStringConfig(config, "ai.tool.transport.message.endpoint", "/tool/message");
        transportSseEndpoint = getStringConfig(config, "ai.tool.transport.sse.endpoint", "/tool/events");
        sseEnabled = getBooleanConfig(config, "ai.tool.transport.enable.sse", true);

        // Feature configuration
        toolsEnabled = getBooleanConfig(config, "ai.tool.features.enable.tools", true);
        resourcesEnabled = getBooleanConfig(config, "ai.tool.features.enable.resources", true);
        promptsEnabled = getBooleanConfig(config, "ai.tool.features.enable.prompts", true);
        loggingEnabled = getBooleanConfig(config, "ai.tool.features.enable.logging", true);

        // Async configuration
        asyncServerEnabled = getBooleanConfig(config, "ai.tool.async.enable.server", false);
        asyncToolsEnabled = getBooleanConfig(config, "ai.tool.async.enable.tools", false);
        asyncThreadPoolSize = getIntConfig(config, "ai.tool.async.thread.pool.size", 10);
        asyncQueueCapacity = getIntConfig(config, "ai.tool.async.queue.capacity", 1000);
        asyncCompletionsEnabled = getBooleanConfig(config, "ai.tool.async.enable.completions", false);

        // Authentication configuration
        authenticationEnabled = getBooleanConfig(config, "ai.tool.auth.enabled", true);
        requestValidationEnabled = getBooleanConfig(config, "ai.tool.auth.enable.request.validation", true);
        primaryAuthMethod = getStringConfig(config, "ai.tool.auth.primary.method", "oauth2.1");
        fallbackAuthMethod = getStringConfig(config, "ai.tool.auth.fallback.method", "openhab_users");
        authFallbackEnabled = getBooleanConfig(config, "ai.tool.auth.enable.fallback", true);
        authProtocolName = getStringConfig(config, "ai.tool.auth.protocol.name", "tool");
        authProtocolPermissions = getStringConfig(config, "ai.tool.auth.protocol.permissions",
                "tool:read,tool:write,tool:execute,tool:admin");
        authSessionTimeout = getIntConfig(config, "ai.tool.auth.protocol.session.timeout", 3600);
        authSessionRefreshEnabled = getBooleanConfig(config, "ai.tool.auth.protocol.session.refresh.enabled", true);
        authSessionRefreshThreshold = getIntConfig(config, "ai.tool.auth.protocol.session.refresh.threshold", 300);

        // Permission configuration
        authReadOperations = getStringConfig(config, "ai.tool.auth.permissions.read.operations",
                "list_tools,list_resources,list_prompts,get_tool,get_resource,get_prompt");
        authWriteOperations = getStringConfig(config, "ai.tool.auth.permissions.write.operations",
                "create_tool,create_resource,create_prompt,update_tool,update_resource,update_prompt");
        authExecuteOperations = getStringConfig(config, "ai.tool.auth.permissions.execute.operations",
                "call_tool,execute_tool,run_tool");
        authAdminOperations = getStringConfig(config, "ai.tool.auth.permissions.admin.operations",
                "delete_tool,delete_resource,delete_prompt,configure_server,manage_users");

        // Session configuration
        maxTools = getIntConfig(config, "ai.tool.max.tools", 100);
        maxConcurrentSessions = getIntConfig(config, "ai.tool.auth.session.max.concurrent", 10);
        sessionCleanupInterval = getIntConfig(config, "ai.tool.auth.session.cleanup.interval", 300);
        sessionInvalidationEnabled = getBooleanConfig(config, "ai.tool.auth.session.invalidation.enabled", true);

        logger.debug("Tool configuration loaded successfully");
    }

    /**
     * Loads default configuration when validation fails.
     */
    private void loadDefaultConfiguration() {
        logger.info("Loading default tool configuration");

        // Default values are already set in field declarations
        logger.info("Default tool configuration loaded");
    }

    // Helper methods for extracting configuration values

    private String getStringConfig(Map<String, Object> config, String key, String defaultValue) {
        Object value = config.get(key);
        if (value instanceof String) {
            return (String) value;
        }
        return defaultValue;
    }

    private boolean getBooleanConfig(Map<String, Object> config, String key, boolean defaultValue) {
        Object value = config.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return defaultValue;
    }

    private int getIntConfig(Map<String, Object> config, String key, int defaultValue) {
        Object value = config.get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                logger.warn("Invalid integer value for config key {}: {}", key, value);
            }
        }
        return defaultValue;
    }

    // Implementation of ToolConfigurationService interface methods

    @Override
    public @Nullable String getServerId() {
        return serverId;
    }

    @Override
    public @Nullable String getServerName() {
        return serverName;
    }

    @Override
    public @Nullable String getServerVersion() {
        return serverVersion;
    }

    @Override
    public @Nullable String getServerDescription() {
        return serverDescription;
    }

    @Override
    public boolean isServerEnabled() {
        return serverEnabled;
    }

    @Override
    public int getServerPort() {
        return serverPort;
    }

    @Override
    public @Nullable String getTransportType() {
        return transportType;
    }

    @Override
    public @Nullable String getTransportBaseUrl() {
        return transportBaseUrl;
    }

    @Override
    public @Nullable String getTransportMessageEndpoint() {
        return transportMessageEndpoint;
    }

    @Override
    public @Nullable String getTransportSseEndpoint() {
        return transportSseEndpoint;
    }

    @Override
    public boolean isSseEnabled() {
        return sseEnabled;
    }

    @Override
    public boolean isToolsEnabled() {
        return toolsEnabled;
    }

    @Override
    public boolean isResourcesEnabled() {
        return resourcesEnabled;
    }

    @Override
    public boolean isPromptsEnabled() {
        return promptsEnabled;
    }

    @Override
    public boolean isLoggingEnabled() {
        return loggingEnabled;
    }

    @Override
    public boolean isAsyncServerEnabled() {
        return asyncServerEnabled;
    }

    @Override
    public boolean isAsyncToolsEnabled() {
        return asyncToolsEnabled;
    }

    @Override
    public int getAsyncThreadPoolSize() {
        return asyncThreadPoolSize;
    }

    @Override
    public int getAsyncQueueCapacity() {
        return asyncQueueCapacity;
    }

    @Override
    public boolean isAsyncCompletionsEnabled() {
        return asyncCompletionsEnabled;
    }

    @Override
    public boolean isAuthenticationEnabled() {
        return authenticationEnabled;
    }

    @Override
    public boolean isRequestValidationEnabled() {
        return requestValidationEnabled;
    }

    @Override
    public @Nullable String getPrimaryAuthMethod() {
        return primaryAuthMethod;
    }

    @Override
    public @Nullable String getFallbackAuthMethod() {
        return fallbackAuthMethod;
    }

    @Override
    public boolean isAuthFallbackEnabled() {
        return authFallbackEnabled;
    }

    @Override
    public @Nullable String getAuthProtocolName() {
        return authProtocolName;
    }

    @Override
    public @Nullable String getAuthProtocolPermissions() {
        return authProtocolPermissions;
    }

    @Override
    public int getAuthSessionTimeout() {
        return authSessionTimeout;
    }

    @Override
    public boolean isAuthSessionRefreshEnabled() {
        return authSessionRefreshEnabled;
    }

    @Override
    public int getAuthSessionRefreshThreshold() {
        return authSessionRefreshThreshold;
    }

    @Override
    public @Nullable String getAuthReadOperations() {
        return authReadOperations;
    }

    @Override
    public @Nullable String getAuthWriteOperations() {
        return authWriteOperations;
    }

    @Override
    public @Nullable String getAuthExecuteOperations() {
        return authExecuteOperations;
    }

    @Override
    public @Nullable String getAuthAdminOperations() {
        return authAdminOperations;
    }

    @Override
    public int getMaxTools() {
        return maxTools;
    }

    @Override
    public int getMaxConcurrentSessions() {
        return maxConcurrentSessions;
    }

    @Override
    public int getSessionCleanupInterval() {
        return sessionCleanupInterval;
    }

    @Override
    public boolean isSessionInvalidationEnabled() {
        return sessionInvalidationEnabled;
    }
}
