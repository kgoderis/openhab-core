package org.openhab.core.ai.tool.adapter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.dto.AbstractResource;
import org.openhab.core.ai.tool.dto.Resource;
import org.openhab.core.ai.tool.factory.ResourceFactory;
import org.openhab.core.ai.tool.proxy.ConfigurationResourceProxy;
import org.openhab.core.config.core.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resource adapter for openHAB configuration.
 * 
 * This adapter provides MCP resource access to openHAB configuration,
 * allowing reading and writing of configuration settings.
 * 
 * Updated to use encapsulated architecture internally while maintaining
 * the same public interface for backward compatibility.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ConfigurationResourceAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationResourceAdapter.class);

    private final String configurationId;
    private final Configuration configuration;
    private final ResourceFactory resourceFactory;

    /**
     * Create a new ConfigurationResourceAdapter.
     *
     * @param configurationId the configuration ID
     * @param configuration the configuration object
     */
    public ConfigurationResourceAdapter(String configurationId, Configuration configuration) {
        this.configurationId = configurationId;
        this.configuration = configuration;
        this.resourceFactory = new ResourceFactory();
    }

    /**
     * Create a resource for an openHAB configuration.
     *
     * @return the resource
     */
    public Resource createConfigurationResource() {
        String uri = "openhab://configuration/" + configurationId;
        String name = "Configuration: " + configurationId;
        String description = "Resource adapter for openHAB configuration: " + configurationId;
        String mimeType = "application/json";

        // Create metadata with configuration information
        Map<String, Object> metadata = new ConcurrentHashMap<>();
        metadata.put("type", "openhab-configuration");
        metadata.put("configurationId", configurationId);
        metadata.put("uri", uri);
        metadata.put("properties", configuration.getProperties());

        return new Resource(uri, name, description, mimeType, metadata);
    }

    /**
     * Get resource content for a configuration.
     *
     * @return the configuration content as a string
     */
    public String getConfigurationContent() {
        AbstractResource resource = getOrCreateEncapsulatedResource();
        return resource != null ? resource.getContent() : "{}";
    }

    /**
     * Check if a configuration is writable.
     *
     * @return true if the configuration is writable
     */
    public boolean isConfigurationWritable() {
        AbstractResource resource = getOrCreateEncapsulatedResource();
        return resource != null && resource.isWritable();
    }

    /**
     * Write content to a configuration.
     *
     * @param content the content to write
     * @return true if successful
     */
    public boolean writeConfigurationContent(@Nullable String content) {
        AbstractResource resource = getOrCreateEncapsulatedResource();
        return resource != null && resource.writeContent(content);
    }

    /**
     * Get a property from the configuration.
     *
     * @param key the property key
     * @return the property value or null if not found
     */
    public @Nullable Object getProperty(String key) {
        ConfigurationResourceProxy proxy = getConfigurationResourceProxy();
        return proxy != null ? proxy.getProperty(key) : configuration.get(key);
    }

    /**
     * Set a property in the configuration.
     *
     * @param key the property key
     * @param value the property value
     * @return true if successful
     */
    public boolean setProperty(String key, Object value) {
        ConfigurationResourceProxy proxy = getConfigurationResourceProxy();
        if (proxy != null) {
            return proxy.setProperty(key, value);
        } else {
            try {
                configuration.put(key, value);
                return true;
            } catch (Exception e) {
                LOGGER.error("Error setting property {} for configuration: {}", key, configurationId, e);
                return false;
            }
        }
    }

    /**
     * Get or create an encapsulated resource for the configuration.
     *
     * @return the encapsulated resource
     */
    private @Nullable AbstractResource getOrCreateEncapsulatedResource() {
        String uri = "openhab://configuration/" + configurationId;

        return resourceFactory.createResource(uri, (resourceUri, refreshIntervalMs) -> {
            try {
                return new ConfigurationResourceProxy(configurationId, configuration, refreshIntervalMs);
            } catch (Exception e) {
                LOGGER.error("Error creating resource for configuration: {}", configurationId, e);
                return null;
            }
        });
    }

    /**
     * Get the underlying ConfigurationResourceProxy.
     *
     * @return the ConfigurationResourceProxy or null if not found
     */
    private @Nullable ConfigurationResourceProxy getConfigurationResourceProxy() {
        AbstractResource resource = getOrCreateEncapsulatedResource();
        if (resource instanceof ConfigurationResourceProxy) {
            return (ConfigurationResourceProxy) resource;
        }
        return null;
    }

    /**
     * Clean up resources managed by this adapter.
     */
    public void cleanup() {
        LOGGER.debug("Cleaning up ConfigurationResourceAdapter resources");
        resourceFactory.cleanup();
    }
}
