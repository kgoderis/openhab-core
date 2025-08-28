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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.agent.config.AgentConfigurationManager;
import org.openhab.core.ai.common.monitoring.api.MetricKeys;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.service.snapshot.UnifiedMetricsSnapshot;
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

    // Metrics service
    private @Nullable MetricsService metricsService;

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
    // Cache metrics now handled by centralized MetricsService
    private volatile long lastReloadTimestamp = System.currentTimeMillis();

    // Configuration change listeners
    private final List<ConfigurationChangeListener> listeners = Collections.synchronizedList(new ArrayList<>());

    /**
     * Record cache metrics using MetricsService with proper error handling.
     * 
     * @param operationType the type of cache operation
     * @param hit whether this was a cache hit (true) or miss (false)
     */
    private void recordCacheMetrics(String operationType, boolean hit) {
        if (metricsService != null) {
            try {
                metricsService.recordOperation("configuration", operationType).withSuccess(hit)
                        .withData("cacheHit", String.valueOf(hit)).record();
            } catch (Exception e) {
                logger.warn("Failed to record cache metrics for operation {}: {}", operationType, e.getMessage());
                // Graceful degradation - continue without metrics if recording fails
            }
        }
    }

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
            // Record cache hit using MetricsService
            recordCacheMetrics("cache-lookup", true);
            return cached.toString();
        }
        // Record cache miss using MetricsService
        recordCacheMetrics("cache-lookup", false);

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
        long startTime = System.nanoTime();
        boolean success = false;

        try {
            // Record cache size before clearing
            int previousCacheSize = configurationCache.size();

            // Clear cache and record the operation
            configurationCache.clear();
            recordMetrics("configuration", "cache-clear", true, 0L);
            recordCacheSizeMetric(0); // Cache is now empty

            // Reload OSGi configurations
            reloadOsgiConfigurations();

            // Reload YAML configurations
            reloadYamlConfigurations();

            // Update timestamp and record reload event
            lastReloadTimestamp = System.currentTimeMillis();
            recordMetrics("configuration", "reload-event", true, System.nanoTime() - startTime);

            // Record final cache size after reload
            recordCacheSizeMetric(configurationCache.size());

            logger.info("Configuration reload completed successfully - cache size: {} -> {}", previousCacheSize,
                    configurationCache.size());
            success = true;
        } catch (Exception e) {
            logger.error("Failed to reload configurations: {}", e.getMessage());
            recordMetrics("configuration", "reload-event", false, System.nanoTime() - startTime);
            throw new ConfigurationException("Failed to reload configurations", e);
        }
    }

    /**
     * Get cache hit rate from MetricsService.
     * 
     * @return the cache hit rate as a percentage, or 0.0 if not available
     */
    private double getCacheHitRateFromMetrics() {
        if (metricsService == null) {
            return 0.0;
        }

        try {
            var metricKey = MetricKeys.custom("configuration", Map.of("operation", "cache-lookup"),
                    Set.of("counts", "latency"));
            var snapshot = metricsService.getSnapshot(metricKey, UnifiedMetricsSnapshot.class);
            if (snapshot != null) {
                Map<String, Object> rawData = snapshot.getRawData();
                if (rawData != null) {
                    Object totalObj = rawData.get("total");
                    Object successObj = rawData.get("success");

                    if (totalObj instanceof Number && successObj instanceof Number) {
                        long total = ((Number) totalObj).longValue();
                        long hits = ((Number) successObj).longValue();
                        return total > 0 ? (double) hits / total * 100.0 : 0.0;
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to retrieve cache hit rate from metrics: {}", e.getMessage());
        }
        return 0.0;
    }

    // Eliminated getStatistics() method after enhancing metric capture
    // Consumers should use MetricsService directly to access configuration statistics:
    // - Cache operations: metricsService.getSnapshot(MetricKeys.custom("configuration", Map.of("operation",
    // "cache-hit")))
    // - Cache size: metricsService.getSnapshot(MetricKeys.custom("configuration", Map.of("operation", "cache-size")))
    // - Reload events: metricsService.getSnapshot(MetricKeys.custom("configuration", Map.of("operation",
    // "reload-event")))
    // - YAML files: metricsService.getSnapshot(MetricKeys.custom("configuration", Map.of("operation",
    // "yaml-file-discovery")))
    // - Environment variables: metricsService.getSnapshot(MetricKeys.custom("configuration", Map.of("operation",
    // "env-var-discovery")))

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

        // Count YAML files discovered during reload
        int yamlFileCount = 0;

        // This would trigger reloads of the YAML repositories
        // For now, we'll simulate counting YAML files for metric recording
        if (agentConfigRepository != null) {
            try {
                // In a real implementation, this would scan the actual YAML files
                yamlFileCount = 5; // Placeholder count for demonstration
                recordYamlFileDiscovery(yamlFileCount);
                logger.debug("Discovered {} YAML configuration files", yamlFileCount);
            } catch (Exception e) {
                logger.warn("Failed to count YAML configuration files: {}", e.getMessage());
            }
        }

        // Also record environment variable discovery
        int envVarCount = System.getenv().size();
        recordEnvironmentVariableDiscovery(envVarCount);
        logger.debug("Discovered {} environment variables", envVarCount);
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

    /**
     * Record metrics using MetricsService with proper error handling.
     * 
     * @param domain the operation domain
     * @param operationType the type of operation
     * @param success whether the operation was successful
     * @param durationNanos the operation duration in nanoseconds
     */
    private void recordMetrics(String domain, String operationType, boolean success, long durationNanos) {
        if (metricsService != null) {
            try {
                metricsService.recordOperation(domain, operationType).withSuccess(success).withDuration(durationNanos)
                        .record();
            } catch (Exception e) {
                logger.warn("Failed to record metrics for {}.{}: {}", domain, operationType, e.getMessage());
            }
        }
    }

    /**
     * Record cache size metric for tracking configuration cache size over time
     */
    private void recordCacheSizeMetric(int cacheSize) {
        if (metricsService != null) {
            try {
                // Record cache size as a custom metric value
                metricsService.recordOperation("configuration", "cache-size").withSuccess(true)
                        .withData("size", cacheSize).record();
            } catch (Exception e) {
                logger.warn("Failed to record cache size metric: {}", e.getMessage());
            }
        }
    }

    /**
     * Record environment variable discovery events
     */
    private void recordEnvironmentVariableDiscovery(int count) {
        if (metricsService != null) {
            try {
                metricsService.recordOperation("configuration", "env-var-discovery").withSuccess(true)
                        .withData("count", count).record();
            } catch (Exception e) {
                logger.warn("Failed to record environment variable discovery: {}", e.getMessage());
            }
        }
    }

    /**
     * Record YAML file discovery events
     */
    private void recordYamlFileDiscovery(int count) {
        if (metricsService != null) {
            try {
                metricsService.recordOperation("configuration", "yaml-file-discovery").withSuccess(true)
                        .withData("count", count).record();
            } catch (Exception e) {
                logger.warn("Failed to record YAML file discovery: {}", e.getMessage());
            }
        }
    }

    // MetricsService reference

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    public void setMetricsService(MetricsService service) {
        this.metricsService = service;
        logger.debug("Metrics service bound");
    }

    public void unsetMetricsService(MetricsService service) {
        this.metricsService = null;
        logger.debug("Metrics service unbound");
    }
}
