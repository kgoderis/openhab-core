package org.openhab.core.ai.agent.lifecycle;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.agent.lifecycle.api.ConfigurationChangeListener;
import org.openhab.core.ai.agent.lifecycle.api.ConfigurationValidator;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A2A Configuration Manager - Handles configuration loading, validation, and runtime updates.
 * 
 * <p>
 * This class is responsible for:
 * - Loading configuration from OSGi ConfigurationAdmin
 * - Validating configuration values
 * - Providing runtime configuration updates
 * - Managing configuration documentation
 * - Supporting configuration testing and validation
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 */
@Component(service = AgentConfigurationManager.class, configurationPid = "org.openhab.core.ai.a2a")
@NonNullByDefault
public class AgentConfigurationManager {

    private static final Logger logger = LoggerFactory.getLogger(AgentConfigurationManager.class);

    @Reference
    private @Nullable ConfigurationAdmin configurationAdmin;

    // Configuration storage
    private final ConcurrentHashMap<String, Object> configuration = new ConcurrentHashMap<>();
    private final AtomicBoolean configurationLoaded = new AtomicBoolean(false);

    // Configuration validation
    private final Map<String, ConfigurationValidator> validators = new HashMap<>();

    // Configuration change listeners
    private final ConcurrentHashMap<String, ConfigurationChangeListener> changeListeners = new ConcurrentHashMap<>();

    // Default configuration values
    private static final Map<String, Object> DEFAULT_CONFIG = createDefaultConfig();

    private static Map<String, Object> createDefaultConfig() {
        Map<String, Object> config = new HashMap<>();

        // Task Ordering
        config.put("task_ordering.enabled", true);
        config.put("task_ordering.max_parallel_tasks", 5);
        config.put("task_ordering.timeout_seconds", 30);

        // Deadlock Prevention
        config.put("deadlock_prevention.enabled", true);
        config.put("deadlock_prevention.max_wait_time", 60);
        config.put("deadlock_prevention.auto_resolve", true);

        // Fault Tolerance
        config.put("fault_tolerance.max_retries", 3);
        config.put("fault_tolerance.retry_delay_ms", 1000);
        config.put("fault_tolerance.fallback_enabled", true);

        // Resource Locking
        config.put("resource_locking.enabled", true);
        config.put("resource_locking.max_wait_time", 30);
        config.put("resource_locking.auto_release", true);

        // Monitoring
        config.put("monitoring.enabled", true);
        config.put("monitoring.check_interval_ms", 10000);
        config.put("monitoring.stuck_task_threshold_ms", 300000);

        // Agent Registry
        config.put("agent_registry.enabled", true);
        config.put("agent_registry.auto_discovery", true);
        config.put("agent_registry.heartbeat_interval_ms", 30000);
        config.put("agent_registry.timeout_ms", 60000);

        // Task Schema
        config.put("schema_generation.enabled", true);
        config.put("schema_generation.cache_ttl_ms", 300000);
        config.put("schema_generation.auto_validate", true);
        config.put("schema_generation.max_versions", 10);

        // Security
        config.put("security.enabled", true);
        config.put("security.require_authentication", true);
        config.put("security.max_concurrent_sessions", 100);
        config.put("security.session_timeout_ms", 3600000);

        // Performance
        config.put("performance.max_thread_pool_size", 20);
        config.put("performance.queue_capacity", 1000);
        config.put("performance.metrics_enabled", true);
        config.put("performance.slow_query_threshold_ms", 5000);

        return config;
    }

    @Activate
    public void activate(Map<String, Object> config) {
        logger.debug("A2A Configuration Manager activating");
        initializeValidators();
        loadConfiguration(config);
        logger.info("A2A Configuration Manager activated with {} configuration items", configuration.size());
    }

    @Modified
    public void modified(Map<String, Object> config) {
        logger.debug("A2A Configuration Manager configuration modified");
        loadConfiguration(config);
        notifyConfigurationChangeListeners();
        logger.info("A2A Configuration Manager configuration updated");
    }

    @Deactivate
    public void deactivate() {
        logger.debug("A2A Configuration Manager deactivating");
        configuration.clear();
        changeListeners.clear();
        validators.clear();
        configurationLoaded.set(false);
        logger.info("A2A Configuration Manager deactivated");
    }

    /**
     * Get a configuration value.
     * 
     * @param key the configuration key
     * @return the configuration value
     */
    public @Nullable Object getConfiguration(String key) {
        return configuration.get(key);
    }

    /**
     * Get a configuration value with a default fallback.
     * 
     * @param key the configuration key
     * @param defaultValue the default value to return if not found
     * @return the configuration value or default
     */
    public Object getConfiguration(String key, Object defaultValue) {
        return configuration.getOrDefault(key, defaultValue);
    }

    /**
     * Get a boolean configuration value.
     * 
     * @param key the configuration key
     * @param defaultValue the default value
     * @return the boolean configuration value
     */
    public boolean getBooleanConfiguration(String key, boolean defaultValue) {
        Object value = configuration.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return defaultValue;
    }

    /**
     * Get an integer configuration value.
     * 
     * @param key the configuration key
     * @param defaultValue the default value
     * @return the integer configuration value
     */
    public int getIntConfiguration(String key, int defaultValue) {
        Object value = configuration.get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Long) {
            return ((Long) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                logger.warn("Invalid integer configuration value for key {}: {}", key, value);
            }
        }
        return defaultValue;
    }

    /**
     * Get a long configuration value.
     * 
     * @param key the configuration key
     * @param defaultValue the default value
     * @return the long configuration value
     */
    public long getLongConfiguration(String key, long defaultValue) {
        Object value = configuration.get(key);
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                logger.warn("Invalid long configuration value for key {}: {}", key, value);
            }
        }
        return defaultValue;
    }

    /**
     * Get a string configuration value.
     * 
     * @param key the configuration key
     * @param defaultValue the default value
     * @return the string configuration value
     */
    public String getStringConfiguration(String key, String defaultValue) {
        Object value = configuration.get(key);
        if (value instanceof String) {
            return (String) value;
        }
        if (value != null) {
            return value.toString();
        }
        return defaultValue;
    }

    /**
     * Update a configuration value at runtime.
     * 
     * @param key the configuration key
     * @param value the new value
     * @return true if the update was successful
     */
    public boolean updateConfiguration(String key, Object value) {
        try {
            // Validate the value
            ConfigurationValidator validator = validators.get(key);
            if (validator != null && !validator.validate(value)) {
                logger.warn("Configuration validation failed for key {}: {}", key, value);
                return false;
            }

            // Update the configuration
            Object oldValue = configuration.put(key, value);
            boolean changed = !value.equals(oldValue);

            if (changed) {
                logger.debug("Configuration updated: {} = {} (was: {})", key, value, oldValue);
                notifyConfigurationChangeListeners(key, oldValue, value);
            }

            return true;
        } catch (Exception e) {
            logger.error("Error updating configuration for key {}: {}", key, e.getMessage());
            return false;
        }
    }

    /**
     * Load configuration from a properties file.
     * 
     * @param filePath the path to the configuration file
     * @return true if loading was successful
     */
    public boolean loadConfigurationFromFile(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                logger.warn("Configuration file not found: {}", filePath);
                return false;
            }

            try (InputStream input = new FileInputStream(file)) {
                Properties props = new Properties();
                props.load(input);

                Map<String, Object> config = new HashMap<>();
                for (String key : props.stringPropertyNames()) {
                    String value = props.getProperty(key);
                    if (value != null) {
                        config.put(key, value);
                    }
                }

                loadConfiguration(config);
                logger.info("Configuration loaded from file: {}", filePath);
                return true;
            }
        } catch (IOException e) {
            logger.error("Error loading configuration from file {}: {}", filePath, e.getMessage());
            return false;
        }
    }

    /**
     * Save current configuration to OSGi ConfigurationAdmin.
     * 
     * @return true if saving was successful
     */
    public boolean saveConfiguration() {
        try {
            ConfigurationAdmin admin = configurationAdmin;
            if (admin == null) {
                logger.warn("ConfigurationAdmin not available");
                return false;
            }

            Configuration config = admin.getConfiguration("org.openhab.core.ai.a2a", null);
            Dictionary<String, Object> properties = new Hashtable<>();

            for (Map.Entry<String, Object> entry : configuration.entrySet()) {
                properties.put(entry.getKey(), entry.getValue());
            }

            config.update(properties);
            logger.info("Configuration saved to OSGi ConfigurationAdmin");
            return true;
        } catch (Exception e) {
            logger.error("Error saving configuration: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get all configuration values.
     * 
     * @return map of all configuration values
     */
    public Map<String, Object> getAllConfiguration() {
        return new HashMap<>(configuration);
    }

    /**
     * Reset configuration to defaults.
     */
    public void resetToDefaults() {
        logger.info("Resetting configuration to defaults");
        configuration.clear();
        configuration.putAll(DEFAULT_CONFIG);
        notifyConfigurationChangeListeners();
    }

    /**
     * Validate current configuration.
     * 
     * @return validation result
     */
    public ConfigurationValidationResult validateConfiguration() {
        ConfigurationValidationResult result = new ConfigurationValidationResult();
        boolean isValid = true;

        for (Map.Entry<String, Object> entry : configuration.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            ConfigurationValidator validator = validators.get(key);
            if (validator != null && !validator.validate(value)) {
                result.addError(key, "Invalid value: " + value);
                isValid = false;
            }
        }

        result.setValid(isValid);
        return result;
    }

    /**
     * Register a configuration change listener.
     * 
     * @param listenerId the listener ID
     * @param listener the change listener
     */
    public void registerConfigurationChangeListener(String listenerId, ConfigurationChangeListener listener) {
        changeListeners.put(listenerId, listener);
        logger.debug("Registered configuration change listener: {}", listenerId);
    }

    /**
     * Unregister a configuration change listener.
     * 
     * @param listenerId the listener ID
     */
    public void unregisterConfigurationChangeListener(String listenerId) {
        changeListeners.remove(listenerId);
        logger.debug("Unregistered configuration change listener: {}", listenerId);
    }

    /**
     * Initialize components with current configuration.
     */
    public void initializeComponents() {
        logger.info("Initializing A2A components with current configuration");
        // This method can be used to initialize components that depend on configuration
        // For now, it's a placeholder for future implementation
    }

    /**
     * Get configuration documentation.
     * 
     * @return configuration documentation
     */
    public String getConfigurationDocumentation() {
        StringBuilder doc = new StringBuilder();
        doc.append("# A2A Configuration Documentation\n\n");

        doc.append("## Task Ordering Configuration\n");
        doc.append("- `task_ordering.enabled`: Enable/disable task ordering (default: true)\n");
        doc.append("- `task_ordering.max_parallel_tasks`: Maximum number of parallel tasks (default: 5)\n");
        doc.append("- `task_ordering.timeout_seconds`: Task timeout in seconds (default: 30)\n\n");

        doc.append("## Deadlock Prevention Configuration\n");
        doc.append("- `deadlock_prevention.enabled`: Enable/disable deadlock prevention (default: true)\n");
        doc.append("- `deadlock_prevention.max_wait_time`: Maximum wait time in seconds (default: 60)\n");
        doc.append("- `deadlock_prevention.auto_resolve`: Auto-resolve deadlocks (default: true)\n\n");

        doc.append("## Fault Tolerance Configuration\n");
        doc.append("- `fault_tolerance.max_retries`: Maximum retry attempts (default: 3)\n");
        doc.append("- `fault_tolerance.retry_delay_ms`: Retry delay in milliseconds (default: 1000)\n");
        doc.append("- `fault_tolerance.fallback_enabled`: Enable fallback mechanisms (default: true)\n\n");

        doc.append("## Resource Locking Configuration\n");
        doc.append("- `resource_locking.enabled`: Enable/disable resource locking (default: true)\n");
        doc.append("- `resource_locking.max_wait_time`: Maximum wait time in seconds (default: 30)\n");
        doc.append("- `resource_locking.auto_release`: Auto-release locks (default: true)\n\n");

        doc.append("## Monitoring Configuration\n");
        doc.append("- `monitoring.enabled`: Enable/disable monitoring (default: true)\n");
        doc.append("- `monitoring.check_interval_ms`: Check interval in milliseconds (default: 10000)\n");
        doc.append(
                "- `monitoring.stuck_task_threshold_ms`: Stuck task threshold in milliseconds (default: 300000)\n\n");

        doc.append("## Agent Registry Configuration\n");
        doc.append("- `agent_registry.enabled`: Enable/disable agent registry (default: true)\n");
        doc.append("- `agent_registry.auto_discovery`: Enable auto-discovery (default: true)\n");
        doc.append("- `agent_registry.heartbeat_interval_ms`: Heartbeat interval in milliseconds (default: 30000)\n");
        doc.append("- `agent_registry.timeout_ms`: Agent timeout in milliseconds (default: 60000)\n\n");

        doc.append("## Task Schema Configuration\n");
        doc.append("- `schema_generation.enabled`: Enable/disable schema generation (default: true)\n");
        doc.append("- `schema_generation.cache_ttl_ms`: Cache TTL in milliseconds (default: 300000)\n");
        doc.append("- `schema_generation.auto_validate`: Auto-validate schemas (default: true)\n");
        doc.append("- `schema_generation.max_versions`: Maximum schema versions (default: 10)\n\n");

        doc.append("## Security Configuration\n");
        doc.append("- `security.enabled`: Enable/disable security (default: true)\n");
        doc.append("- `security.require_authentication`: Require authentication (default: true)\n");
        doc.append("- `security.max_concurrent_sessions`: Maximum concurrent sessions (default: 100)\n");
        doc.append("- `security.session_timeout_ms`: Session timeout in milliseconds (default: 3600000)\n\n");

        doc.append("## Performance Configuration\n");
        doc.append("- `performance.max_thread_pool_size`: Maximum thread pool size (default: 20)\n");
        doc.append("- `performance.queue_capacity`: Queue capacity (default: 1000)\n");
        doc.append("- `performance.metrics_enabled`: Enable metrics (default: true)\n");
        doc.append("- `performance.slow_query_threshold_ms`: Slow query threshold in milliseconds (default: 5000)\n");

        return doc.toString();
    }

    // ============================================================================
    // Private Helper Methods
    // ============================================================================

    private void initializeValidators() {
        // Task ordering validators
        validators.put("task_ordering.max_parallel_tasks", value -> {
            if (value instanceof Number) {
                int val = ((Number) value).intValue();
                return val > 0 && val <= 100;
            }
            return false;
        });

        validators.put("task_ordering.timeout_seconds", value -> {
            if (value instanceof Number) {
                int val = ((Number) value).intValue();
                return val > 0 && val <= 3600;
            }
            return false;
        });

        // Deadlock prevention validators
        validators.put("deadlock_prevention.max_wait_time", value -> {
            if (value instanceof Number) {
                int val = ((Number) value).intValue();
                return val > 0 && val <= 300;
            }
            return false;
        });

        // Fault tolerance validators
        validators.put("fault_tolerance.max_retries", value -> {
            if (value instanceof Number) {
                int val = ((Number) value).intValue();
                return val >= 0 && val <= 10;
            }
            return false;
        });

        validators.put("fault_tolerance.retry_delay_ms", value -> {
            if (value instanceof Number) {
                long val = ((Number) value).longValue();
                return val >= 0 && val <= 60000;
            }
            return false;
        });

        // Resource locking validators
        validators.put("resource_locking.max_wait_time", value -> {
            if (value instanceof Number) {
                int val = ((Number) value).intValue();
                return val > 0 && val <= 300;
            }
            return false;
        });

        // Monitoring validators
        validators.put("monitoring.check_interval_ms", value -> {
            if (value instanceof Number) {
                long val = ((Number) value).longValue();
                return val >= 1000 && val <= 60000;
            }
            return false;
        });

        validators.put("monitoring.stuck_task_threshold_ms", value -> {
            if (value instanceof Number) {
                long val = ((Number) value).longValue();
                return val >= 60000 && val <= 3600000;
            }
            return false;
        });

        // Agent registry validators
        validators.put("agent_registry.heartbeat_interval_ms", value -> {
            if (value instanceof Number) {
                long val = ((Number) value).longValue();
                return val >= 5000 && val <= 300000;
            }
            return false;
        });

        validators.put("agent_registry.timeout_ms", value -> {
            if (value instanceof Number) {
                long val = ((Number) value).longValue();
                return val >= 30000 && val <= 300000;
            }
            return false;
        });

        // Schema generation validators
        validators.put("schema_generation.cache_ttl_ms", value -> {
            if (value instanceof Number) {
                long val = ((Number) value).longValue();
                return val >= 60000 && val <= 3600000;
            }
            return false;
        });

        validators.put("schema_generation.max_versions", value -> {
            if (value instanceof Number) {
                int val = ((Number) value).intValue();
                return val >= 1 && val <= 100;
            }
            return false;
        });

        // Security validators
        validators.put("security.max_concurrent_sessions", value -> {
            if (value instanceof Number) {
                int val = ((Number) value).intValue();
                return val > 0 && val <= 10000;
            }
            return false;
        });

        validators.put("security.session_timeout_ms", value -> {
            if (value instanceof Number) {
                long val = ((Number) value).longValue();
                return val >= 300000 && val <= 86400000;
            }
            return false;
        });

        // Performance validators
        validators.put("performance.max_thread_pool_size", value -> {
            if (value instanceof Number) {
                int val = ((Number) value).intValue();
                return val > 0 && val <= 200;
            }
            return false;
        });

        validators.put("performance.queue_capacity", value -> {
            if (value instanceof Number) {
                int val = ((Number) value).intValue();
                return val > 0 && val <= 100000;
            }
            return false;
        });

        validators.put("performance.slow_query_threshold_ms", value -> {
            if (value instanceof Number) {
                long val = ((Number) value).longValue();
                return val >= 100 && val <= 60000;
            }
            return false;
        });
    }

    private void loadConfiguration(Map<String, Object> config) {
        // Clear existing configuration
        configuration.clear();

        // Load default values first
        configuration.putAll(DEFAULT_CONFIG);

        // Override with provided configuration
        for (Map.Entry<String, Object> entry : config.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // Validate the value
            ConfigurationValidator validator = validators.get(key);
            if (validator != null && !validator.validate(value)) {
                logger.warn("Invalid configuration value for key {}: {}, using default", key, value);
                continue;
            }

            configuration.put(key, value);
        }

        configurationLoaded.set(true);
        logger.debug("Configuration loaded with {} items", configuration.size());
    }

    private void notifyConfigurationChangeListeners() {
        for (ConfigurationChangeListener listener : changeListeners.values()) {
            try {
                listener.onConfigurationChanged();
            } catch (Exception e) {
                logger.error("Error notifying configuration change listener: {}", e.getMessage());
            }
        }
    }

    private void notifyConfigurationChangeListeners(String key, @Nullable Object oldValue, Object newValue) {
        for (ConfigurationChangeListener listener : changeListeners.values()) {
            try {
                listener.onConfigurationChanged(key, oldValue, newValue);
            } catch (Exception e) {
                logger.error("Error notifying configuration change listener: {}", e.getMessage());
            }
        }
    }

    // ConfigurationValidationResult extracted to top-level
    // org.openhab.core.ai.agent.lifecycle.ConfigurationValidationResult
}
