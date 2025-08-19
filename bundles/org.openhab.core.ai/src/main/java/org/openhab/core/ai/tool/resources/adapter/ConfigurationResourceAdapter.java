package org.openhab.core.ai.tool.resources.adapter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.adapter.ResourceAdapter;
import org.openhab.core.ai.tool.resources.api.ResourceContext;
import org.openhab.core.ai.tool.resources.api.ResourceResult;
import org.openhab.core.ai.tool.resources.api.dto.Resource;
import org.openhab.core.config.core.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resource Adapter for openHAB configurations using the unified adapter hierarchy.
 * 
 * This adapter provides MCP resource access to openHAB configurations with caching and lifecycle management.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ConfigurationResourceAdapter extends ResourceAdapter<Resource, ResourceContext, ResourceResult> {

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
    public @Nullable ResourceResult adapt(Resource source, ResourceContext context) {
        // For resource adapters, we typically don't adapt existing resources
        // but rather create new ones or execute operations
        return null;
    }

    @Override
    public boolean canAdapt(Resource source) {
        // Check if this adapter can handle the given resource
        return source != null && "openhab-configuration".equals(source.getMetadata().get("type"));
    }

    @Override
    public Class<Resource> getSourceType() {
        return Resource.class;
    }

    @Override
    public Class<ResourceResult> getResultType() {
        return ResourceResult.class;
    }

    @Override
    protected void doRefresh(String identifier, ResourceContext context) {
        refresh(identifier, context);
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
            LOGGER.debug("Loading configuration: {}", identifier);

            // Parse the configuration identifier to determine the type
            String[] parts = identifier.split(":");
            if (parts.length < 2) {
                LOGGER.warn("Invalid configuration identifier format: {}", identifier);
                return null;
            }

            String configType = parts[0];
            String configId = parts[1];

            switch (configType) {
                case "thing":
                    return loadThingConfiguration(configId);
                case "binding":
                    return loadBindingConfiguration(configId);
                case "service":
                    return loadServiceConfiguration(configId);
                case "system":
                    return loadSystemConfiguration(configId);
                default:
                    LOGGER.warn("Unknown configuration type: {}", configType);
                    return null;
            }
        } catch (Exception e) {
            LOGGER.error("Error loading configuration: {}", identifier, e);
            return null;
        }
    }

    /**
     * Load thing configuration.
     * 
     * @param thingId the thing ID
     * @return the configuration or null if not found
     */
    private @Nullable Configuration loadThingConfiguration(String thingId) {
        try {
            // In a real implementation, this would load from ThingRegistry
            Configuration config = new Configuration();
            config.put("id", thingId);
            config.put("type", "thing");
            config.put("enabled", true);
            config.put("label", "Thing " + thingId);

            // Add common thing configuration properties
            config.put("location", "");
            config.put("bridgeUID", "");

            LOGGER.debug("Loaded thing configuration: {}", thingId);
            return config;
        } catch (Exception e) {
            LOGGER.error("Error loading thing configuration: {}", thingId, e);
            return null;
        }
    }

    /**
     * Load binding configuration.
     * 
     * @param bindingId the binding ID
     * @return the configuration or null if not found
     */
    private @Nullable Configuration loadBindingConfiguration(String bindingId) {
        try {
            // In a real implementation, this would load from BindingRegistry
            Configuration config = new Configuration();
            config.put("id", bindingId);
            config.put("type", "binding");
            config.put("enabled", true);
            config.put("name", "Binding " + bindingId);

            LOGGER.debug("Loaded binding configuration: {}", bindingId);
            return config;
        } catch (Exception e) {
            LOGGER.error("Error loading binding configuration: {}", bindingId, e);
            return null;
        }
    }

    /**
     * Load service configuration.
     * 
     * @param serviceId the service ID
     * @return the configuration or null if not found
     */
    private @Nullable Configuration loadServiceConfiguration(String serviceId) {
        try {
            // In a real implementation, this would load from ServiceRegistry
            Configuration config = new Configuration();
            config.put("id", serviceId);
            config.put("type", "service");
            config.put("enabled", true);
            config.put("name", "Service " + serviceId);

            LOGGER.debug("Loaded service configuration: {}", serviceId);
            return config;
        } catch (Exception e) {
            LOGGER.error("Error loading service configuration: {}", serviceId, e);
            return null;
        }
    }

    /**
     * Load system configuration.
     * 
     * @param systemId the system ID
     * @return the configuration or null if not found
     */
    private @Nullable Configuration loadSystemConfiguration(String systemId) {
        try {
            // In a real implementation, this would load from SystemConfiguration
            Configuration config = new Configuration();
            config.put("id", systemId);
            config.put("type", "system");
            config.put("enabled", true);
            config.put("name", "System " + systemId);

            LOGGER.debug("Loaded system configuration: {}", systemId);
            return config;
        } catch (Exception e) {
            LOGGER.error("Error loading system configuration: {}", systemId, e);
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
            LOGGER.debug("Setting property {}={} for configuration: {}", property, value, identifier);

            // Parse the configuration identifier to determine the type
            String[] parts = identifier.split(":");
            if (parts.length < 2) {
                LOGGER.warn("Invalid configuration identifier format: {}", identifier);
                return false;
            }

            String configType = parts[0];

            // Get the current configuration
            CachedConfigurationData cachedData = getOrCreateCachedData(identifier);
            if (cachedData == null || cachedData.getConfiguration() == null) {
                LOGGER.warn("Configuration not found: {}", identifier);
                return false;
            }

            Configuration config = cachedData.getConfiguration();

            // Validate the property based on configuration type
            if (!isValidPropertyForConfigType(configType, property, value)) {
                LOGGER.warn("Invalid property {}={} for configuration type: {}", property, value, configType);
                return false;
            }

            // Update the configuration property
            config.put(property, value);

            // In a real implementation, this would persist the configuration
            LOGGER.info("Configuration property updated: {} {}={}", identifier, property, value);

            // Update cached content
            cachedData.updateRefreshTime();

            return true;
        } catch (Exception e) {
            LOGGER.error("Error setting property {}={} for configuration: {}", property, value, identifier, e);
            return false;
        }
    }

    /**
     * Validate if a property is valid for a given configuration type.
     * 
     * @param configType the configuration type
     * @param property the property name
     * @param value the property value
     * @return true if valid
     */
    private boolean isValidPropertyForConfigType(String configType, String property, Object value) {
        try {
            switch (configType) {
                case "thing":
                    return isValidThingProperty(property, value);
                case "binding":
                    return isValidBindingProperty(property, value);
                case "service":
                    return isValidServiceProperty(property, value);
                case "system":
                    return isValidSystemProperty(property, value);
                default:
                    LOGGER.warn("Unknown configuration type for validation: {}", configType);
                    return false;
            }
        } catch (Exception e) {
            LOGGER.error("Error validating property {}={} for config type: {}", property, value, configType, e);
            return false;
        }
    }

    /**
     * Validate thing configuration property.
     * 
     * @param property the property name
     * @param value the property value
     * @return true if valid
     */
    private boolean isValidThingProperty(String property, Object value) {
        switch (property) {
            case "enabled":
                return value instanceof Boolean;
            case "label":
            case "location":
            case "bridgeUID":
                return value instanceof String;
            default:
                // Allow custom properties
                return true;
        }
    }

    /**
     * Validate binding configuration property.
     * 
     * @param property the property name
     * @param value the property value
     * @return true if valid
     */
    private boolean isValidBindingProperty(String property, Object value) {
        switch (property) {
            case "enabled":
                return value instanceof Boolean;
            case "name":
            case "version":
                return value instanceof String;
            default:
                // Allow custom properties
                return true;
        }
    }

    /**
     * Validate service configuration property.
     * 
     * @param property the property name
     * @param value the property value
     * @return true if valid
     */
    private boolean isValidServiceProperty(String property, Object value) {
        switch (property) {
            case "enabled":
                return value instanceof Boolean;
            case "name":
            case "url":
                return value instanceof String;
            default:
                // Allow custom properties
                return true;
        }
    }

    /**
     * Validate system configuration property.
     * 
     * @param property the property name
     * @param value the property value
     * @return true if valid
     */
    private boolean isValidSystemProperty(String property, Object value) {
        switch (property) {
            case "enabled":
                return value instanceof Boolean;
            case "name":
            case "version":
            case "timezone":
                return value instanceof String;
            default:
                // Allow custom properties
                return true;
        }
    }

    /**
     * Cached configuration data for performance optimization.
     */
    // CachedConfigurationData extracted to org.openhab.core.ai.tool.resources.adapter.CachedConfigurationData
    // CachedConfigurationData extracted to org.openhab.core.ai.tool.resources.adapter.CachedConfigurationData
}
