/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.core.ai.config;

import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Unified configuration manager for the openHAB AI bundle.
 * 
 * <p>
 * This interface provides a centralized access point for all configuration domains,
 * implementing configuration precedence (environment variables > .cfg files > YAML > defaults)
 * and providing caching for performance.
 * </p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public interface ConfigurationManager {

    /**
     * Gets a configuration value with precedence resolution.
     * 
     * @param key the configuration key (e.g., "ai.model.primary.provider")
     * @return the configuration value, or null if not found
     */
    @Nullable
    String getString(String key);

    /**
     * Gets a configuration value with precedence resolution and default.
     * 
     * @param key the configuration key
     * @param defaultValue the default value if not found
     * @return the configuration value or default
     */
    String getString(String key, String defaultValue);

    /**
     * Gets a boolean configuration value.
     * 
     * @param key the configuration key
     * @param defaultValue the default value if not found
     * @return the boolean configuration value
     */
    boolean getBoolean(String key, boolean defaultValue);

    /**
     * Gets an integer configuration value.
     * 
     * @param key the configuration key
     * @param defaultValue the default value if not found
     * @return the integer configuration value
     */
    int getInt(String key, int defaultValue);

    /**
     * Gets a double configuration value.
     * 
     * @param key the configuration key
     * @param defaultValue the default value if not found
     * @return the double configuration value
     */
    double getDouble(String key, double defaultValue);

    /**
     * Gets a long configuration value.
     * 
     * @param key the configuration key
     * @param defaultValue the default value if not found
     * @return the long configuration value
     */
    long getLong(String key, long defaultValue);

    /**
     * Gets all configuration values for a specific domain.
     * 
     * @param domain the configuration domain (e.g., "ai.model", "ai.agent")
     * @return map of configuration key-value pairs for the domain
     */
    Map<String, Object> getDomainConfiguration(String domain);

    /**
     * Gets a YAML configuration object.
     * 
     * @param <T> the type of configuration object
     * @param domain the configuration domain
     * @param name the configuration name
     * @param type the configuration object type
     * @return optional configuration object
     */
    <T> Optional<T> getYamlConfiguration(String domain, String name, Class<T> type);

    /**
     * Gets all YAML configurations for a domain.
     * 
     * @param <T> the type of configuration objects
     * @param domain the configuration domain
     * @param type the configuration object type
     * @return map of name to configuration objects
     */
    <T> Map<String, T> getAllYamlConfigurations(String domain, Class<T> type);

    /**
     * Checks if a configuration key exists.
     * 
     * @param key the configuration key
     * @return true if the configuration exists
     */
    boolean hasConfiguration(String key);

    /**
     * Checks if a YAML configuration exists.
     * 
     * @param domain the configuration domain
     * @param name the configuration name
     * @return true if the YAML configuration exists
     */
    boolean hasYamlConfiguration(String domain, String name);

    /**
     * Reloads all configurations.
     * 
     * @throws ConfigurationException if reloading fails
     */
    void reload() throws ConfigurationException;

    /**
     * Gets configuration statistics.
     * 
     * @return configuration statistics
     */
    // Eliminated getStatistics() method - consumers should access statistics directly via MetricsService
    // Use: metricsService.getSnapshot(MetricKeys.custom("configuration", Map.of("operation", "cache-hit")))

    /**
     * Registers a configuration change listener.
     * 
     * @param listener the listener to register
     */
    void addConfigurationChangeListener(ConfigurationChangeListener listener);

    /**
     * Unregisters a configuration change listener.
     * 
     * @param listener the listener to unregister
     */
    void removeConfigurationChangeListener(ConfigurationChangeListener listener);

    /**
     * Configuration statistics.
     */
    interface ConfigurationStatistics {
        /**
         * Gets the number of OSGi configuration values.
         * 
         * @return the count
         */
        int getOsgiConfigurationCount();

        /**
         * Gets the number of YAML configuration files.
         * 
         * @return the count
         */
        int getYamlConfigurationCount();

        /**
         * Gets the number of environment variable overrides.
         * 
         * @return the count
         */
        int getEnvironmentVariableCount();

        /**
         * Gets the cache hit rate.
         * 
         * @return the hit rate as a percentage
         */
        double getCacheHitRate();

        /**
         * Gets the last reload timestamp.
         * 
         * @return the timestamp in milliseconds
         */
        long getLastReloadTimestamp();
    }
}
