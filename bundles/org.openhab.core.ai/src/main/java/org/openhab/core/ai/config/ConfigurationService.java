package org.openhab.core.ai.config;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.configuration.ConfigurationChangeListener;

/**
 * Configuration service for AI protocols (MCP and A2A).
 * 
 * This service manages configuration for both Model Context Protocol and
 * Agent-to-Agent protocol implementations, providing a unified interface
 * for configuration management across the AI ecosystem.
 * 
 * 
 */
@NonNullByDefault
public interface ConfigurationService {

    /**
     * Get a configuration value by key.
     * 
     * @param key The configuration key
     * @return The configuration value, or empty if not found
     */
    Optional<String> getConfigValue(String key);

    /**
     * Get a configuration value with a default fallback.
     * 
     * @param key The configuration key
     * @param defaultValue The default value to return if key is not found
     * @return The configuration value or default value
     */
    String getConfigValue(String key, @Nullable String defaultValue);

    /**
     * Get a typed configuration value.
     * 
     * @param <T> The type to convert to
     * @param key The configuration key
     * @param type The target type class
     * @return The typed configuration value, or empty if not found or conversion fails
     */
    <T> Optional<T> getConfigValue(String key, Class<T> type);

    /**
     * Get a typed configuration value with default.
     * 
     * @param <T> The type to convert to
     * @param key The configuration key
     * @param type The target type class
     * @param defaultValue The default value
     * @return The typed configuration value or default value
     */
    <T> T getConfigValue(String key, Class<T> type, T defaultValue);

    /**
     * Set a configuration value.
     * 
     * @param key The configuration key
     * @param value The configuration value
     * @return true if the value was set successfully, false otherwise
     */
    boolean setConfigValue(String key, String value);

    /**
     * Remove a configuration value.
     * 
     * @param key The configuration key to remove
     * @return true if the value was removed, false if it didn't exist
     */
    boolean removeConfigValue(String key);

    /**
     * Get all configuration keys with a specific prefix.
     * 
     * @param prefix The key prefix to filter by
     * @return Set of keys that start with the prefix
     */
    Set<String> getConfigKeys(String prefix);

    /**
     * Get all configuration entries with a specific prefix.
     * 
     * @param prefix The key prefix to filter by
     * @return Map of key-value pairs that start with the prefix
     */
    Map<String, String> getConfigEntries(String prefix);

    /**
     * Check if a configuration key exists.
     * 
     * @param key The configuration key
     * @return true if the key exists, false otherwise
     */
    boolean hasConfigKey(String key);

    /**
     * Get the configuration for a specific AI protocol.
     * 
     * @param protocol The protocol name ("mcp" or "a2a")
     * @return Protocol-specific configuration
     */
    ProtocolConfiguration getProtocolConfiguration(String protocol);

    /**
     * Update the configuration for a specific AI protocol.
     * 
     * @param protocol The protocol name ("mcp" or "a2a")
     * @param configuration The new configuration
     * @return true if updated successfully, false otherwise
     */
    boolean updateProtocolConfiguration(String protocol, ProtocolConfiguration configuration);

    /**
     * Reload configuration from the underlying source.
     * 
     * @return true if reload was successful, false otherwise
     */
    boolean reloadConfiguration();

    /**
     * Add a configuration change listener.
     * 
     * @param listener The listener to add
     */
    void addConfigurationChangeListener(ConfigurationChangeListener listener);

    /**
     * Remove a configuration change listener.
     * 
     * @param listener The listener to remove
     */
    void removeConfigurationChangeListener(ConfigurationChangeListener listener);

    /**
     * Get the name of the configuration service.
     * 
     * @return Service name
     */
    String getServiceName();
}
