package org.openhab.core.ai.tool.resources.adapter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.adapter.BaseAdapter;
import org.openhab.core.ai.tool.api.Adapter;
import org.openhab.core.ai.tool.api.ResourceContext;
import org.openhab.core.ai.tool.api.ResourceResult;
import org.openhab.core.ai.tool.resources.dto.Resource;
import org.openhab.core.config.core.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Consolidated Resource Adapter for openHAB configurations.
 * 
 * This adapter combines the functionality of both the old Adapter and Proxy classes,
 * providing MCP resource access to openHAB configurations with caching and lifecycle management.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ConfigurationResourceAdapter extends BaseAdapter
        implements Adapter<Resource, ResourceContext, ResourceResult> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationResourceAdapter.class);

    private static final String ADAPTER_TYPE = "configurations";
    private static final String URI_PATTERN = "openhab://configurations/{configId}";
    private static final long DEFAULT_REFRESH_INTERVAL_MS = 5 * 60 * 1000; // 5 minutes

    private final Map<String, CachedConfigurationData> configCache = new ConcurrentHashMap<>();

    /**
     * Create a new ConfigurationResourceAdapter.
     */
    public ConfigurationResourceAdapter() {
        super(DEFAULT_REFRESH_INTERVAL_MS);
    }

    @Override
    public @Nullable Resource createEntity(String identifier, ResourceContext context) {
        try {
            // For configurations, we'll create a resource based on the identifier
            String uri = "openhab://configurations/" + identifier;
            String name = "Configuration: " + identifier;
            String description = "Resource adapter for openHAB configuration: " + identifier;
            String mimeType = "application/json";

            // Create metadata with configuration information
            Map<String, Object> metadata = new ConcurrentHashMap<>();
            metadata.put("type", "openhab-configuration");
            metadata.put("configId", identifier);
            metadata.put("uri", uri);

            return new Resource(uri, name, description, mimeType, metadata);
        } catch (Exception e) {
            LOGGER.error("Error creating resource for configuration: {}", identifier, e);
            return null;
        }
    }

    @Override
    public @Nullable String getContent(String identifier, ResourceContext context) {
        CachedConfigurationData cachedData = getOrCreateCachedData(identifier);
        if (cachedData != null && cachedData.needsRefresh()) {
            refresh(identifier, context);
        }
        return cachedData != null ? cachedData.getContent() : null;
    }

    @Override
    public boolean isWritable(String identifier, ResourceContext context) {
        return true; // Configurations are generally writable
    }

    @Override
    public boolean writeContent(String identifier, @Nullable String content, ResourceContext context) {
        try {
            if (content == null || content.isEmpty()) {
                LOGGER.warn("Attempted to write null or empty content to configuration: {}", identifier);
                return false;
            }

            // TODO: Implement actual configuration writing logic
            // This would involve updating the configuration via ConfigurationRegistry
            LOGGER.debug("Writing content to configuration: {} - {}", identifier, content);

            // Update cached content
            CachedConfigurationData cachedData = getOrCreateCachedData(identifier);
            if (cachedData != null) {
                cachedData.setContent(content);
                cachedData.updateRefreshTime();
            }

            return true;
        } catch (Exception e) {
            LOGGER.error("Error writing content to configuration: {}", identifier, e);
            return false;
        }
    }

    @Override
    public boolean exists(String identifier, ResourceContext context) {
        CachedConfigurationData cachedData = getOrCreateCachedData(identifier);
        return cachedData != null && cachedData.getConfiguration() != null;
    }

    @Override
    public ResourceResult execute(String identifier, String operation, Map<String, Object> parameters,
            ResourceContext context) {
        long startTime = System.currentTimeMillis();

        try {
            LOGGER.debug("Executing configuration operation: {} for configuration: {} with parameters: {}", operation,
                    identifier, parameters);

            CachedConfigurationData cachedData = getOrCreateCachedData(identifier);
            if (cachedData == null || cachedData.getConfiguration() == null) {
                return ResourceResult.failure("Configuration not found: " + identifier,
                        System.currentTimeMillis() - startTime);
            }

            switch (operation) {
                case "get":
                    Configuration config = cachedData.getConfiguration();
                    Map<String, Object> result = new ConcurrentHashMap<>();
                    result.put("success", true);
                    result.put("configId", identifier);
                    result.put("properties", config.getProperties());

                    long getExecutionTime = System.currentTimeMillis() - startTime;
                    return ResourceResult.success(result, getExecutionTime);

                case "set":
                    Object value = parameters.get("value");
                    String property = (String) parameters.get("property");
                    if (value != null && property != null) {
                        boolean success = setConfigurationProperty(identifier, property, value);
                        Map<String, Object> setResult = new ConcurrentHashMap<>();
                        setResult.put("success", success);
                        setResult.put("configId", identifier);
                        setResult.put("property", property);
                        setResult.put("value", value);

                        long setExecutionTime = System.currentTimeMillis() - startTime;
                        return ResourceResult.success(setResult, setExecutionTime);
                    } else {
                        return ResourceResult.failure("Property and value parameters are required for set operation",
                                System.currentTimeMillis() - startTime);
                    }

                case "update":
                    // Similar to set but with different semantics if needed
                    return execute(identifier, "set", parameters, context);

                default:
                    return ResourceResult.failure("Unknown operation: " + operation,
                            System.currentTimeMillis() - startTime);
            }

        } catch (Exception e) {
            LOGGER.error("Error executing configuration operation: {} for configuration: {}", operation, identifier, e);
            return ResourceResult.failure("Execution error: " + e.getMessage(), System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public void refresh(String identifier, ResourceContext context) {
        try {
            // TODO: Implement actual configuration loading logic
            // This would involve loading the configuration from ConfigurationRegistry
            Configuration config = loadConfiguration(identifier);
            CachedConfigurationData cachedData = getOrCreateCachedData(identifier);

            if (config != null) {
                // Create JSON representation of configuration
                StringBuilder content = new StringBuilder();
                content.append("{\n");
                content.append("  \"id\": \"").append(identifier).append("\",\n");
                content.append("  \"properties\": {\n");

                Map<String, Object> properties = config.getProperties();
                if (properties != null && !properties.isEmpty()) {
                    boolean first = true;
                    for (Map.Entry<String, Object> entry : properties.entrySet()) {
                        if (!first) {
                            content.append(",\n");
                        }
                        content.append("    \"").append(entry.getKey()).append("\": \"")
                                .append(entry.getValue() != null ? entry.getValue().toString() : "").append("\"");
                        first = false;
                    }
                }
                content.append("\n  }\n");
                content.append("}");

                cachedData.setConfiguration(config);
                cachedData.setContent(content.toString());
            } else {
                cachedData.setConfiguration(null);
                cachedData.setContent("{}");
            }

            cachedData.updateRefreshTime();
            updateRefreshTime();
            LOGGER.debug("Refreshed configuration data: {}", identifier);
        } catch (Exception e) {
            LOGGER.error("Error refreshing configuration data: {}", identifier, e);
        }
    }

    @Override
    public void cleanup() {
        LOGGER.debug("Cleaning up ConfigurationResourceAdapter resources");
        configCache.clear();
        markInvalid();
    }

    @Override
    public String getAdapterType() {
        return ADAPTER_TYPE;
    }

    @Override
    public String getUriPattern() {
        return URI_PATTERN;
    }

    @Override
    public void close() {
        cleanup();
    }

    /**
     * Get or create cached data for a configuration.
     * 
     * @param identifier the configuration identifier
     * @return the cached data or null if configuration doesn't exist
     */
    private @Nullable CachedConfigurationData getOrCreateCachedData(String identifier) {
        return configCache.computeIfAbsent(identifier, key -> {
            Configuration config = loadConfiguration(key);
            return config != null ? new CachedConfigurationData(config) : null;
        });
    }

    /**
     * Load a configuration by identifier.
     * 
     * @param identifier the configuration identifier
     * @return the configuration or null if not found
     */
    private @Nullable Configuration loadConfiguration(String identifier) {
        try {
            // TODO: Implement actual configuration loading logic
            // This would involve loading from ConfigurationRegistry
            LOGGER.debug("Loading configuration: {}", identifier);

            // For now, create a mock configuration
            Configuration config = new Configuration();
            config.put("id", identifier);
            config.put("name", "Configuration " + identifier);
            config.put("enabled", true);

            return config;
        } catch (Exception e) {
            LOGGER.error("Error loading configuration: {}", identifier, e);
            return null;
        }
    }

    /**
     * Set a configuration property.
     * 
     * @param identifier the configuration identifier
     * @param property the property name
     * @param value the property value
     * @return true if successful
     */
    private boolean setConfigurationProperty(String identifier, String property, Object value) {
        try {
            // TODO: Implement actual configuration property setting logic
            // This would involve updating the configuration via ConfigurationRegistry
            LOGGER.debug("Setting property {}={} for configuration: {}", property, value, identifier);
            return true;
        } catch (Exception e) {
            LOGGER.error("Error setting property {}={} for configuration: {}", property, value, identifier, e);
            return false;
        }
    }

    /**
     * Cached configuration data for performance optimization.
     */
    private static class CachedConfigurationData {
        private volatile @Nullable Configuration configuration;
        private volatile @Nullable String content;
        private volatile long lastRefreshTime = 0;
        private final long refreshIntervalMs = 5 * 60 * 1000; // 5 minutes

        public CachedConfigurationData(Configuration configuration) {
            this.configuration = configuration;
        }

        public @Nullable Configuration getConfiguration() {
            return configuration;
        }

        public void setConfiguration(@Nullable Configuration configuration) {
            this.configuration = configuration;
        }

        public @Nullable String getContent() {
            return content;
        }

        public void setContent(@Nullable String content) {
            this.content = content;
        }

        public boolean needsRefresh() {
            return System.currentTimeMillis() - lastRefreshTime > refreshIntervalMs;
        }

        public void updateRefreshTime() {
            lastRefreshTime = System.currentTimeMillis();
        }
    }
}
