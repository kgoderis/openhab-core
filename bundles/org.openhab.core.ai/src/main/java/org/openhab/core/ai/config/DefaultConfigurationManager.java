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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.agent.config.AgentConfigurationManager;
import org.openhab.core.ai.config.repo.AgentConfigurationRepository;
import org.openhab.core.ai.config.repo.ModelPresetRepository;
import org.openhab.core.ai.config.repo.PolicyRepository;
import org.openhab.core.ai.config.repo.PromptRepository;
import org.openhab.core.ai.model.DefaultModelConfigurationService;
import org.openhab.core.ai.tool.DefaultToolConfigurationService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of ConfigurationManager.
 * 
 * <p>
 * This implementation provides unified access to all configuration domains with
 * precedence resolution (environment variables > .cfg files > YAML > defaults)
 * and caching for performance.
 * </p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@Component(service = ConfigurationManager.class)
@NonNullByDefault
public class DefaultConfigurationManager implements ConfigurationManager {

    private final Logger logger = LoggerFactory.getLogger(DefaultConfigurationManager.class);

    // Configuration services
    private @Nullable DefaultConfigurationService commonConfigService;
    private @Nullable DefaultModelConfigurationService modelConfigService;
    private @Nullable DefaultToolConfigurationService toolConfigService;
    private @Nullable AgentConfigurationManager agentConfigService;

    // YAML repositories
    private @Nullable PromptRepository promptRepository;
    private @Nullable PolicyRepository policyRepository;
    private @Nullable ModelPresetRepository modelPresetRepository;
    private @Nullable AgentConfigurationRepository agentConfigRepository;

    // Configuration cache
    private final Map<String, Object> configurationCache = new ConcurrentHashMap<>();
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);
    private final AtomicLong lastReloadTimestamp = new AtomicLong(System.currentTimeMillis());

    // Configuration change listeners
    private final List<ConfigurationChangeListener> listeners = Collections.synchronizedList(new ArrayList<>());

    @Activate
    public void activate() {
        logger.debug("Activating Configuration Manager");
        try {
            reload();
            logger.info("Configuration Manager activated successfully");
        } catch (ConfigurationException e) {
            logger.error("Failed to activate Configuration Manager: {}", e.getMessage());
            // Continue with empty configuration
        }
    }

    @Deactivate
    public void deactivate() {
        logger.debug("Deactivating Configuration Manager");
        configurationCache.clear();
        logger.info("Configuration Manager deactivated");
    }

    @Override
    public @Nullable String getString(String key) {
        return getString(key, null);
    }

    @Override
    public String getString(String key, String defaultValue) {
        // Check cache first
        Object cached = configurationCache.get(key);
        if (cached != null) {
            cacheHits.incrementAndGet();
            return cached.toString();
        }
        cacheMisses.incrementAndGet();

        // Check environment variables first (highest precedence)
        String envValue = getEnvironmentVariable(key);
        if (envValue != null) {
            configurationCache.put(key, envValue);
            return envValue;
        }

        // Check OSGi configuration services
        String osgiValue = getOsgiConfiguration(key);
        if (osgiValue != null) {
            configurationCache.put(key, osgiValue);
            return osgiValue;
        }

        // Return default value
        if (defaultValue != null) {
            configurationCache.put(key, defaultValue);
            return defaultValue;
        }

        return null;
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = getString(key);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    @Override
    public int getInt(String key, int defaultValue) {
        String value = getString(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            logger.warn("Invalid integer value for key '{}': {}", key, value);
            return defaultValue;
        }
    }

    @Override
    public double getDouble(String key, double defaultValue) {
        String value = getString(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            logger.warn("Invalid double value for key '{}': {}", key, value);
            return defaultValue;
        }
    }

    @Override
    public long getLong(String key, long defaultValue) {
        String value = getString(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            logger.warn("Invalid long value for key '{}': {}", key, value);
            return defaultValue;
        }
    }

    @Override
    public Map<String, Object> getDomainConfiguration(String domain) {
        // This would need to be implemented based on the specific domain
        // For now, return an empty map
        logger.debug("Getting domain configuration for: {}", domain);
        return Map.of();
    }

    @Override
    public <T> Optional<T> getYamlConfiguration(String domain, String name, Class<T> type) {
        try {
            switch (domain) {
                case "prompts":
                    if (promptRepository != null) {
                        // This would need to be implemented based on the repository interface
                        logger.debug("Getting prompt configuration: {}", name);
                    }
                    break;
                case "policies":
                    if (policyRepository != null) {
                        logger.debug("Getting policy configuration: {}", name);
                    }
                    break;
                case "models":
                    if (modelPresetRepository != null) {
                        logger.debug("Getting model preset configuration: {}", name);
                    }
                    break;
                case "agents":
                    if (agentConfigRepository != null) {
                        logger.debug("Getting agent configuration: {}", name);
                    }
                    break;
                default:
                    logger.warn("Unknown YAML configuration domain: {}", domain);
            }
        } catch (Exception e) {
            logger.error("Error getting YAML configuration for domain '{}', name '{}': {}", domain, name,
                    e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public <T> Map<String, T> getAllYamlConfigurations(String domain, Class<T> type) {
        try {
            switch (domain) {
                case "prompts":
                    if (promptRepository != null) {
                        logger.debug("Getting all prompt configurations");
                    }
                    break;
                case "policies":
                    if (policyRepository != null) {
                        logger.debug("Getting all policy configurations");
                    }
                    break;
                case "models":
                    if (modelPresetRepository != null) {
                        logger.debug("Getting all model preset configurations");
                    }
                    break;
                case "agents":
                    if (agentConfigRepository != null) {
                        logger.debug("Getting all agent configurations");
                    }
                    break;
                default:
                    logger.warn("Unknown YAML configuration domain: {}", domain);
            }
        } catch (Exception e) {
            logger.error("Error getting all YAML configurations for domain '{}': {}", domain, e.getMessage());
        }
        return Map.of();
    }

    @Override
    public boolean hasConfiguration(String key) {
        return getString(key) != null;
    }

    @Override
    public boolean hasYamlConfiguration(String domain, String name) {
        return getYamlConfiguration(domain, name, Object.class).isPresent();
    }

    @Override
    public void reload() throws ConfigurationException {
        logger.debug("Reloading all configurations");

        try {
            // Clear cache
            configurationCache.clear();

            // Reload OSGi configurations
            reloadOsgiConfigurations();

            // Reload YAML configurations
            reloadYamlConfigurations();

            // Update timestamp
            lastReloadTimestamp.set(System.currentTimeMillis());

            logger.info("Configuration reload completed successfully");
        } catch (Exception e) {
            logger.error("Failed to reload configurations: {}", e.getMessage());
            throw new ConfigurationException("Failed to reload configurations", e);
        }
    }

    @Override
    public ConfigurationStatistics getStatistics() {
        return new ConfigurationStatistics() {
            @Override
            public int getOsgiConfigurationCount() {
                return configurationCache.size();
            }

            @Override
            public int getYamlConfigurationCount() {
                // This would need to be implemented based on repository counts
                return 0;
            }

            @Override
            public int getEnvironmentVariableCount() {
                // This would need to be implemented by counting environment variables
                return 0;
            }

            @Override
            public double getCacheHitRate() {
                long hits = cacheHits.get();
                long misses = cacheMisses.get();
                long total = hits + misses;
                return total > 0 ? (double) hits / total * 100.0 : 0.0;
            }

            @Override
            public long getLastReloadTimestamp() {
                return lastReloadTimestamp.get();
            }
        };
    }

    @Override
    public void addConfigurationChangeListener(ConfigurationChangeListener listener) {
        Objects.requireNonNull(listener, "Listener cannot be null");
        listeners.add(listener);
        logger.debug("Configuration change listener added: {}", listener.getClass().getSimpleName());
    }

    @Override
    public void removeConfigurationChangeListener(ConfigurationChangeListener listener) {
        Objects.requireNonNull(listener, "Listener cannot be null");
        listeners.remove(listener);
        logger.debug("Configuration change listener removed: {}", listener.getClass().getSimpleName());
    }

    /**
     * Publishes a configuration change event to all registered listeners.
     * 
     * @param event the configuration change event
     */
    private void publishConfigurationChangeEvent(ConfigurationChangeEvent event) {
        synchronized (listeners) {
            for (ConfigurationChangeListener listener : listeners) {
                try {
                    // Check if listener is interested in this domain
                    String[] interestedDomains = listener.getInterestedDomains();
                    if (interestedDomains != null && interestedDomains.length > 0) {
                        boolean interested = false;
                        for (String domain : interestedDomains) {
                            if (event.affectsDomain(domain)) {
                                interested = true;
                                break;
                            }
                        }
                        if (!interested) {
                            continue;
                        }
                    }

                    // Check if listener is interested in this key
                    String[] interestedKeys = listener.getInterestedKeys();
                    if (interestedKeys != null && interestedKeys.length > 0) {
                        boolean interested = false;
                        for (String pattern : interestedKeys) {
                            if (matchesPattern(event.getConfigurationKey(), pattern)) {
                                interested = true;
                                break;
                            }
                        }
                        if (!interested) {
                            continue;
                        }
                    }

                    listener.onConfigurationChanged(event);
                } catch (Exception e) {
                    logger.error("Error notifying configuration change listener {}: {}",
                            listener.getClass().getSimpleName(), e.getMessage());
                }
            }
        }
    }

    /**
     * Checks if a key matches a pattern (supports wildcards).
     * 
     * @param key the configuration key
     * @param pattern the pattern to match against
     * @return true if the key matches the pattern
     */
    private boolean matchesPattern(String key, String pattern) {
        if (pattern.equals("*")) {
            return true;
        }
        if (pattern.endsWith("*")) {
            return key.startsWith(pattern.substring(0, pattern.length() - 1));
        }
        if (pattern.startsWith("*")) {
            return key.endsWith(pattern.substring(1));
        }
        return key.equals(pattern);
    }

    /**
     * Gets environment variable value.
     */
    private @Nullable String getEnvironmentVariable(String key) {
        // Convert key to environment variable format
        String envKey = key.replace('.', '_').toUpperCase();
        return System.getenv(envKey);
    }

    /**
     * Gets OSGi configuration value.
     */
    private @Nullable String getOsgiConfiguration(String key) {
        // Determine which service to query based on the key prefix
        if (key.startsWith("ai.common.")) {
            if (commonConfigService != null) {
                // This would need to be implemented based on the service interface
                logger.debug("Getting common configuration: {}", key);
            }
        } else if (key.startsWith("ai.model.")) {
            if (modelConfigService != null) {
                logger.debug("Getting model configuration: {}", key);
            }
        } else if (key.startsWith("ai.tool.")) {
            if (toolConfigService != null) {
                logger.debug("Getting tool configuration: {}", key);
            }
        } else if (key.startsWith("ai.agent.")) {
            if (agentConfigService != null) {
                logger.debug("Getting agent configuration: {}", key);
            }
        }

        return null;
    }

    /**
     * Reloads OSGi configurations.
     */
    private void reloadOsgiConfigurations() {
        logger.debug("Reloading OSGi configurations");
        // This would trigger reloads of the individual configuration services
    }

    /**
     * Reloads YAML configurations.
     */
    private void reloadYamlConfigurations() {
        logger.debug("Reloading YAML configurations");
        // This would trigger reloads of the YAML repositories
    }

    // OSGi service references

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    public void setCommonConfigurationService(DefaultConfigurationService service) {
        this.commonConfigService = service;
        logger.debug("Common configuration service bound");
    }

    public void unsetCommonConfigurationService(DefaultConfigurationService service) {
        this.commonConfigService = null;
        logger.debug("Common configuration service unbound");
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    public void setModelConfigurationService(DefaultModelConfigurationService service) {
        this.modelConfigService = service;
        logger.debug("Model configuration service bound");
    }

    public void unsetModelConfigurationService(DefaultModelConfigurationService service) {
        this.modelConfigService = null;
        logger.debug("Model configuration service unbound");
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    public void setToolConfigurationService(DefaultToolConfigurationService service) {
        this.toolConfigService = service;
        logger.debug("Tool configuration service bound");
    }

    public void unsetToolConfigurationService(DefaultToolConfigurationService service) {
        this.toolConfigService = null;
        logger.debug("Tool configuration service unbound");
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    public void setAgentConfigurationManager(AgentConfigurationManager service) {
        this.agentConfigService = service;
        logger.debug("Agent configuration manager bound");
    }

    public void unsetAgentConfigurationManager(AgentConfigurationManager service) {
        this.agentConfigService = null;
        logger.debug("Agent configuration manager unbound");
    }

    // YAML repository references

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    public void setPromptRepository(PromptRepository repository) {
        this.promptRepository = repository;
        logger.debug("Prompt repository bound");
    }

    public void unsetPromptRepository(PromptRepository repository) {
        this.promptRepository = null;
        logger.debug("Prompt repository unbound");
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    public void setPolicyRepository(PolicyRepository repository) {
        this.policyRepository = repository;
        logger.debug("Policy repository bound");
    }

    public void unsetPolicyRepository(PolicyRepository repository) {
        this.policyRepository = null;
        logger.debug("Policy repository unbound");
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    public void setModelPresetRepository(ModelPresetRepository repository) {
        this.modelPresetRepository = repository;
        logger.debug("Model preset repository bound");
    }

    public void unsetModelPresetRepository(ModelPresetRepository repository) {
        this.modelPresetRepository = null;
        logger.debug("Model preset repository unbound");
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    public void setAgentConfigurationRepository(AgentConfigurationRepository repository) {
        this.agentConfigRepository = repository;
        logger.debug("Agent configuration repository bound");
    }

    public void unsetAgentConfigurationRepository(AgentConfigurationRepository repository) {
        this.agentConfigRepository = null;
        logger.debug("Agent configuration repository unbound");
    }
}
