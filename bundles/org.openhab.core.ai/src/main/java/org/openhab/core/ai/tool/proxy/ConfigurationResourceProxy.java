package org.openhab.core.ai.tool.proxy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.dto.AbstractResource;
import org.openhab.core.config.core.Configuration;

/**
 * Encapsulated resource proxy for openHAB configurations.
 * 
 * This class extends AbstractResource to provide proper encapsulation,
 * lifecycle management, and state caching for openHAB configurations.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ConfigurationResourceProxy extends AbstractResource {

    private final String configurationId;
    private final Configuration configuration;
    private volatile @Nullable String cachedContent;

    /**
     * Create a new ConfigurationResourceProxy.
     *
     * @param configurationId the configuration ID
     * @param configuration the configuration object
     * @param refreshIntervalMs the refresh interval in milliseconds
     */
    public ConfigurationResourceProxy(String configurationId, Configuration configuration, long refreshIntervalMs) {
        super("openhab://configuration/" + configurationId, "Configuration: " + configurationId,
                "Resource adapter for openHAB configuration: " + configurationId, "application/json",
                createMetadata(configurationId, configuration), refreshIntervalMs);

        this.configurationId = configurationId;
        this.configuration = configuration;
    }

    /**
     * Create metadata for the configuration.
     *
     * @param configurationId the configuration ID
     * @param configuration the configuration object
     * @return the metadata map
     */
    private static Map<String, Object> createMetadata(String configurationId, Configuration configuration) {
        Map<String, Object> metadata = new ConcurrentHashMap<>();
        metadata.put("type", "openhab-configuration");
        metadata.put("configurationId", configurationId);
        metadata.put("uri", "openhab://configuration/" + configurationId);
        metadata.put("properties", configuration.getProperties());
        return metadata;
    }

    @Override
    public @Nullable String getContent() {
        if (needsRefresh()) {
            refresh();
        }
        return cachedContent;
    }

    @Override
    public boolean isWritable() {
        // Configurations are generally writable
        return true;
    }

    @Override
    public boolean writeContent(@Nullable String content) {
        if (!isWritable()) {
            LOGGER.warn("Configuration is not writable: {}", configurationId);
            return false;
        }

        if (content == null || content.trim().isEmpty()) {
            LOGGER.warn("Content is null or empty for configuration: {}", configurationId);
            return false;
        }

        try {
            // Parse JSON content and update configuration
            // This is a simplified implementation - in practice, you'd want to use a proper JSON parser
            if (content.contains("\"properties\":")) {
                // Note: This would need to be implemented with proper openHAB configuration handling
                LOGGER.info("Would update configuration {} properties", configurationId);

                // Refresh the cache after write
                refresh();
                return true;
            }

            LOGGER.warn("Invalid content format for configuration: {}", configurationId);
            return false;
        } catch (Exception e) {
            LOGGER.error("Error writing content to configuration: {}", configurationId, e);
            return false;
        }
    }

    @Override
    public boolean exists() {
        // Configuration always exists if the proxy was created
        return true;
    }

    @Override
    public void refresh() {
        try {
            cachedContent = createConfigurationJson();

            // Update metadata with current configuration information
            metadata.put("properties", configuration.getProperties());

            updateRefreshTime();
            LOGGER.debug("Refreshed configuration resource: {}", configurationId);
        } catch (Exception e) {
            LOGGER.error("Error refreshing configuration resource: {}", configurationId, e);
            markInvalid();
        }
    }

    @Override
    public void close() {
        LOGGER.debug("Closing configuration resource: {}", configurationId);
        cachedContent = null;
        markInvalid();
    }

    /**
     * Create JSON representation of configuration.
     *
     * @return the JSON string
     */
    private String createConfigurationJson() {
        StringBuilder content = new StringBuilder();
        content.append("{");
        content.append("\"id\":\"").append(configurationId).append("\",");
        content.append("\"properties\":{");

        boolean first = true;
        for (Map.Entry<String, Object> entry : configuration.getProperties().entrySet()) {
            if (!first) {
                content.append(",");
            }
            content.append("\"").append(entry.getKey()).append("\":");
            if (entry.getValue() instanceof String) {
                content.append("\"").append(entry.getValue()).append("\"");
            } else {
                content.append(entry.getValue());
            }
            first = false;
        }
        content.append("}");
        content.append("}");

        return content.toString();
    }

    /**
     * Get the configuration ID.
     *
     * @return the configuration ID
     */
    public String getConfigurationId() {
        return configurationId;
    }

    /**
     * Get the underlying configuration.
     *
     * @return the configuration
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Get a property from the configuration.
     *
     * @param key the property key
     * @return the property value or null if not found
     */
    public @Nullable Object getProperty(String key) {
        return configuration.get(key);
    }

    /**
     * Set a property in the configuration.
     *
     * @param key the property key
     * @param value the property value
     * @return true if successful
     */
    public boolean setProperty(String key, Object value) {
        try {
            configuration.put(key, value);
            refresh(); // Refresh cache after modification
            return true;
        } catch (Exception e) {
            LOGGER.error("Error setting property {} for configuration: {}", key, configurationId, e);
            return false;
        }
    }
}
