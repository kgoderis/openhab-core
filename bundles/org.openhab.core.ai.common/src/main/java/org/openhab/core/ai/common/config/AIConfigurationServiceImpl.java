package org.openhab.core.ai.common.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.OpenHAB;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete implementation of AIConfigurationService that reads from standard openHAB configuration files.
 * 
 * This service provides configuration management for AI protocol bundles by reading from
 * standard openHAB configuration files (.cfg files) and environment variables.
 * 
 * 
 */
@Component(service = AIConfigurationService.class, immediate = true)
@NonNullByDefault
public class AIConfigurationServiceImpl implements AIConfigurationService {

    private static final Logger logger = LoggerFactory.getLogger(AIConfigurationServiceImpl.class);
    private static final String SERVICE_NAME = "AIConfigurationService";

    // Configuration sources
    private final Map<String, String> configuration = new ConcurrentHashMap<>();
    private final List<AIConfigurationChangeListener> listeners = new CopyOnWriteArrayList<>();
    private final ScheduledExecutorService reloadExecutor = Executors.newSingleThreadScheduledExecutor();

    // Configuration file paths
    private @Nullable Path commonConfigPath;
    private @Nullable Path mcpConfigPath;
    private @Nullable Path a2aConfigPath;

    // Configuration metadata
    private long lastReloadTime = 0;
    private boolean configurationLoaded = false;

    @Activate
    public void activate() {
        logger.info("Activating AI Configuration Service");
        initializeConfigurationPaths();
        loadConfiguration();
        startConfigurationReload();
        logger.info("AI Configuration Service activated successfully");
    }

    @Deactivate
    public void deactivate() {
        logger.info("Deactivating AI Configuration Service");
        stopConfigurationReload();
        configuration.clear();
        listeners.clear();
        logger.info("AI Configuration Service deactivated");
    }

    @Modified
    public void modified() {
        logger.info("AI Configuration Service configuration modified");
        reloadConfiguration();
    }

    @Override
    public Optional<String> getConfigValue(String key) {
        if (key == null || key.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(configuration.get(key));
    }

    @Override
    public String getConfigValue(String key, @Nullable String defaultValue) {
        Optional<String> value = getConfigValue(key);
        if (value.isPresent()) {
            return value.get();
        }
        return defaultValue != null ? defaultValue : "";
    }

    @Override
    public <T> Optional<T> getConfigValue(String key, Class<T> type) {
        Optional<String> value = getConfigValue(key);
        if (value.isEmpty()) {
            return Optional.empty();
        }

        try {
            String configValue = value.get();
            if (configValue != null) {
                return Optional.of(convertValue(configValue, type));
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            logger.warn("Failed to convert configuration value '{}' to type {}", key, type.getSimpleName(), e);
            return Optional.empty();
        }
    }

    @Override
    public <T> T getConfigValue(String key, Class<T> type, T defaultValue) {
        return getConfigValue(key, type).orElse(defaultValue);
    }

    @Override
    public boolean setConfigValue(String key, String value) {
        if (key == null || key.trim().isEmpty()) {
            return false;
        }

        String oldValue = configuration.get(key);
        configuration.put(key, value);

        logger.debug("Configuration value set: {} = {}", key, value);
        notifyConfigurationChanged(key, oldValue, value);

        return true;
    }

    @Override
    public boolean removeConfigValue(String key) {
        if (key == null || key.trim().isEmpty()) {
            return false;
        }

        String oldValue = configuration.remove(key);
        if (oldValue != null) {
            logger.debug("Configuration value removed: {}", key);
            notifyConfigurationChanged(key, oldValue, "");
            return true;
        }

        return false;
    }

    @Override
    public Set<String> getConfigKeys(String prefix) {
        if (prefix == null) {
            return Set.copyOf(configuration.keySet());
        }

        return configuration.keySet().stream().filter(key -> key.startsWith(prefix))
                .collect(java.util.stream.Collectors.toSet());
    }

    @Override
    public Map<String, String> getConfigEntries(String prefix) {
        if (prefix == null) {
            return new HashMap<>(configuration);
        }

        return configuration.entrySet().stream().filter(entry -> entry.getKey().startsWith(prefix))
                .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public boolean hasConfigKey(String key) {
        return key != null && configuration.containsKey(key);
    }

    @Override
    public AIProtocolConfiguration getProtocolConfiguration(String protocol) {
        if (protocol == null || protocol.trim().isEmpty()) {
            return new AIProtocolConfiguration("", false, "", new HashMap<>(), new HashMap<>(), 30, 3);
        }

        String prefix = protocol.toLowerCase() + ".";
        Map<String, String> protocolConfig = getConfigEntries(prefix);

        if (protocolConfig.isEmpty()) {
            return new AIProtocolConfiguration(protocol, false, "", new HashMap<>(), new HashMap<>(), 30, 3);
        }

        // Extract protocol-specific configuration
        boolean enabled = Boolean.parseBoolean(protocolConfig.getOrDefault(prefix + "enabled", "false"));
        String endpoint = protocolConfig.get(prefix + "endpoint");

        // Extract authentication configuration
        Map<String, String> authConfig = new HashMap<>();
        protocolConfig.entrySet().stream().filter(entry -> entry.getKey().startsWith(prefix + "auth."))
                .forEach(entry -> {
                    String authKey = entry.getKey().substring(prefix.length());
                    authConfig.put(authKey, entry.getValue());
                });

        // Extract protocol-specific configuration
        Map<String, Object> protocolSpecificConfig = new HashMap<>();
        protocolConfig.entrySet().stream().filter(entry -> !entry.getKey().startsWith(prefix + "auth."))
                .forEach(entry -> {
                    String configKey = entry.getKey().substring(prefix.length());
                    protocolSpecificConfig.put(configKey, convertValue(entry.getValue(), Object.class));
                });

        int timeoutSeconds = Integer.parseInt(protocolConfig.getOrDefault(prefix + "timeout.seconds", "30"));
        int retryAttempts = Integer.parseInt(protocolConfig.getOrDefault(prefix + "retry.attempts", "3"));

        String endpointValue = endpoint != null ? endpoint : "";
        return new AIProtocolConfiguration(protocol, enabled, endpointValue, authConfig, protocolSpecificConfig,
                timeoutSeconds, retryAttempts);
    }

    @Override
    public boolean updateProtocolConfiguration(String protocol, AIProtocolConfiguration configuration) {
        if (protocol == null || configuration == null) {
            return false;
        }

        String prefix = protocol.toLowerCase() + ".";

        // Update basic configuration
        setConfigValue(prefix + "enabled", String.valueOf(configuration.isEnabled()));
        String endpoint = configuration.getEndpoint();
        if (endpoint != null) {
            setConfigValue(prefix + "endpoint", endpoint);
        }
        setConfigValue(prefix + "timeout.seconds", String.valueOf(configuration.getTimeoutSeconds()));
        setConfigValue(prefix + "retry.attempts", String.valueOf(configuration.getRetryAttempts()));

        // Update authentication configuration
        configuration.getAuthenticationConfig().forEach((key, value) -> {
            setConfigValue(prefix + "auth." + key, value);
        });

        // Update protocol-specific configuration
        configuration.getProtocolSpecificConfig().forEach((key, value) -> {
            setConfigValue(prefix + key, String.valueOf(value));
        });

        logger.info("Updated protocol configuration for: {}", protocol);
        return true;
    }

    @Override
    public boolean reloadConfiguration() {
        logger.info("Reloading AI configuration");

        try {
            // Clear existing configuration
            configuration.clear();

            // Load configuration from all sources
            loadConfigurationFromFiles();
            loadConfigurationFromEnvironment();

            lastReloadTime = System.currentTimeMillis();
            configurationLoaded = true;

            logger.info("Configuration reloaded successfully. Loaded {} configuration entries", configuration.size());
            notifyConfigurationReloaded();

            return true;
        } catch (Exception e) {
            logger.error("Failed to reload configuration", e);
            return false;
        }
    }

    @Override
    public void addConfigurationChangeListener(AIConfigurationChangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
            logger.debug("Added configuration change listener: {}", listener.getListenerName());
        }
    }

    @Override
    public void removeConfigurationChangeListener(AIConfigurationChangeListener listener) {
        if (listener != null) {
            listeners.remove(listener);
            logger.debug("Removed configuration change listener: {}", listener.getListenerName());
        }
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    // Private helper methods

    private void initializeConfigurationPaths() {
        String configDir = OpenHAB.getConfigFolder();
        commonConfigPath = Paths.get(configDir, "ai-common.cfg");
        mcpConfigPath = Paths.get(configDir, "mcp.cfg");
        a2aConfigPath = Paths.get(configDir, "a2a.cfg");

        logger.debug("Configuration paths initialized: common={}, mcp={}, a2a={}", commonConfigPath, mcpConfigPath,
                a2aConfigPath);
    }

    private void loadConfiguration() {
        logger.info("Loading AI configuration from all sources");
        reloadConfiguration();
    }

    private void loadConfigurationFromFiles() {
        List<@Nullable Path> configFiles = new ArrayList<>();
        if (commonConfigPath != null) {
            configFiles.add(commonConfigPath);
        }
        if (mcpConfigPath != null) {
            configFiles.add(mcpConfigPath);
        }
        if (a2aConfigPath != null) {
            configFiles.add(a2aConfigPath);
        }

        for (Path configFile : configFiles) {
            if (configFile != null && Files.exists(configFile)) {
                loadConfigurationFromFile(configFile);
            } else if (configFile != null) {
                logger.debug("Configuration file not found: {}", configFile);
            }
        }
    }

    private void loadConfigurationFromFile(Path configFile) {
        try (InputStream input = Files.newInputStream(configFile)) {
            Properties props = new Properties();
            props.load(input);

            for (String key : props.stringPropertyNames()) {
                String value = props.getProperty(key);
                configuration.put(key, value);
                logger.debug("Loaded configuration: {} = {}", key, value);
            }

            logger.info("Loaded {} configuration entries from {}", props.size(), configFile.getFileName());
        } catch (IOException e) {
            logger.warn("Failed to load configuration from file: {}", configFile, e);
        }
    }

    private void loadConfigurationFromEnvironment() {
        // Load environment variables with AI prefix
        Map<String, String> env = System.getenv();
        env.entrySet().stream().filter(entry -> entry.getKey().startsWith("AI_")).forEach(entry -> {
            String key = entry.getKey().toLowerCase().replace('_', '.');
            configuration.put(key, entry.getValue());
            logger.debug("Loaded environment configuration: {} = {}", key, entry.getValue());
        });
    }

    private void startConfigurationReload() {
        boolean reloadEnabled = Boolean.parseBoolean(getConfigValue("ai.common.config.reload.enabled", "true"));
        if (reloadEnabled) {
            int reloadInterval = Integer.parseInt(getConfigValue("ai.common.config.reload.interval", "60"));
            reloadExecutor.scheduleAtFixedRate(this::reloadConfiguration, reloadInterval, reloadInterval,
                    TimeUnit.SECONDS);
            logger.info("Started configuration reload scheduler with interval: {} seconds", reloadInterval);
        }
    }

    private void stopConfigurationReload() {
        if (!reloadExecutor.isShutdown()) {
            reloadExecutor.shutdown();
            try {
                if (!reloadExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    reloadExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                reloadExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T convertValue(String value, Class<T> type) {
        if (value == null) {
            return null;
        }

        if (type == String.class) {
            return (T) value;
        } else if (type == Integer.class || type == int.class) {
            return (T) Integer.valueOf(value);
        } else if (type == Long.class || type == long.class) {
            return (T) Long.valueOf(value);
        } else if (type == Double.class || type == double.class) {
            return (T) Double.valueOf(value);
        } else if (type == Boolean.class || type == boolean.class) {
            return (T) Boolean.valueOf(value);
        } else if (type == Float.class || type == float.class) {
            return (T) Float.valueOf(value);
        } else if (type == Object.class) {
            // Try to determine the best type
            if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                return (T) Boolean.valueOf(value);
            } else if (value.matches("-?\\d+")) {
                return (T) Long.valueOf(value);
            } else if (value.matches("-?\\d*\\.\\d+")) {
                return (T) Double.valueOf(value);
            } else {
                return (T) value;
            }
        } else {
            throw new IllegalArgumentException("Unsupported type: " + type.getName());
        }
    }

    private void notifyConfigurationChanged(String key, String oldValue, String newValue) {
        for (AIConfigurationChangeListener listener : listeners) {
            try {
                listener.onConfigurationChanged(key, oldValue, newValue);
            } catch (Exception e) {
                logger.warn("Error in configuration change listener: {}", listener.getListenerName(), e);
            }
        }
    }

    private void notifyConfigurationReloaded() {
        for (AIConfigurationChangeListener listener : listeners) {
            try {
                listener.onConfigurationReloaded();
            } catch (Exception e) {
                logger.warn("Error in configuration reload listener: {}", listener.getListenerName(), e);
            }
        }
    }

    // Public utility methods for debugging and monitoring

    public Map<String, String> getAllConfiguration() {
        return new HashMap<>(configuration);
    }

    public long getLastReloadTime() {
        return lastReloadTime;
    }

    public boolean isConfigurationLoaded() {
        return configurationLoaded;
    }

    public int getConfigurationEntryCount() {
        return configuration.size();
    }

    public List<String> getConfigurationSources() {
        List<String> sources = new ArrayList<>();
        if (Files.exists(commonConfigPath)) {
            sources.add("ai-common.cfg");
        }
        if (Files.exists(mcpConfigPath)) {
            sources.add("mcp.cfg");
        }
        if (Files.exists(a2aConfigPath)) {
            sources.add("a2a.cfg");
        }
        sources.add("environment");
        return sources;
    }
}
