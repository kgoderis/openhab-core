package org.openhab.core.ai.agent.infrastructure.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.agent.infrastructure.config.api.ConfigurationPreset;
import org.openhab.core.ai.agent.infrastructure.config.api.ConfigurationTemplate;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.patterns.ConfigurationOperationMetrics;
import org.openhab.core.ai.common.validation.ConfigurationValidationResult;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Manages configuration for agent communication systems
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = AgentCommunicationConfigurationManager.class)
@NonNullByDefault
public class AgentCommunicationConfigurationManager {

    private final Logger logger = LoggerFactory.getLogger(AgentCommunicationConfigurationManager.class);

    // Configuration storage
    private final Map<String, CommunicationConfig> configurations = new ConcurrentHashMap<>();
    private final Map<String, ConfigurationTemplate> templates = new ConcurrentHashMap<>();
    private final Map<String, ConfigurationPreset> presets = new ConcurrentHashMap<>();

    // Configuration versioning
    private final Map<String, ConfigurationVersion> versionHistory = new ConcurrentHashMap<>();

    // Configuration monitoring - now handled by MetricsService
    @Reference
    private @Nullable MetricsService metricsService;

    // Background processors
    private final ScheduledExecutorService configMonitor = Executors.newScheduledThreadPool(1);
    private final ScheduledExecutorService backupProcessor = Executors.newScheduledThreadPool(1);

    // JSON mapper for configuration serialization
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Default configuration paths
    private static final String DEFAULT_CONFIG_PATH = "conf/agent-communication";
    private static final String BACKUP_CONFIG_PATH = "conf/agent-communication/backup";
    private static final String TEMPLATES_CONFIG_PATH = "conf/agent-communication/templates";



    @Activate
    public AgentCommunicationConfigurationManager() {
        initializeDefaultTemplates();
        initializeDefaultPresets();
        startBackgroundProcessors();
        loadExistingConfigurations();
    }

    @Deactivate
    public void deactivate() {
        configMonitor.shutdown();
        backupProcessor.shutdown();
    }

    /**
     * Load configuration from file
     */
    public CompletableFuture<CommunicationConfig> loadConfiguration(String configId, String filePath) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Path path = Paths.get(filePath);
                if (!Files.exists(path)) {
                    throw new ConfigurationException("Configuration file not found: " + filePath);
                }

                String content = Files.readString(path);
                CommunicationConfig config = objectMapper.readValue(content, CommunicationConfig.class);

                // Validate configuration
                validateConfiguration(config);

                // Store configuration
                configurations.put(configId, config);

                // Create version entry
                ConfigurationVersion version = new ConfigurationVersion(configId, config.getVersion(), Instant.now(),
                        "Loaded from file: " + filePath, config);
                versionHistory.put(configId + "_" + config.getVersion(), version);

                logger.debug("Successfully loaded configuration: {} from {}", configId, filePath);
                
                // Replace totalConfigurations.incrementAndGet() and successfulLoads.incrementAndGet()
                recordConfigurationLoad("communication-config", true, Files.size(path), Duration.ZERO);
                
                return config;

            } catch (Exception e) {
                logger.error("Error loading configuration: {} from {}", configId, filePath, e);
                
                // Replace failedLoads.incrementAndGet()
                recordConfigurationLoad("communication-config", false, 0, Duration.ZERO);
                
                throw new ConfigurationException("Failed to load configuration: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Save configuration to file
     */
    public CompletableFuture<Void> saveConfiguration(String configId, String filePath) {
        return CompletableFuture.runAsync(() -> {
            try {
                CommunicationConfig config = configurations.get(configId);
                if (config == null) {
                    throw new ConfigurationException("Configuration not found: " + configId);
                }

                // Create backup before saving
                createBackup(configId, filePath);

                // Serialize and save
                String content = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(config);
                Path path = Paths.get(filePath);
                Files.createDirectories(path.getParent());
                Files.writeString(path, content);

                logger.debug("Successfully saved configuration: {} to {}", configId, filePath);

            } catch (Exception e) {
                logger.error("Error saving configuration: {} to {}", configId, filePath, e);
                throw new ConfigurationException("Failed to save configuration: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Get configuration by ID
     */
    public @Nullable CommunicationConfig getConfiguration(String configId) {
        return configurations.get(configId);
    }

    /**
     * Update configuration
     */
    public CompletableFuture<CommunicationConfig> updateConfiguration(String configId, CommunicationConfig newConfig) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Validate new configuration
                validateConfiguration(newConfig);

                // Create backup of current configuration
                CommunicationConfig currentConfig = configurations.get(configId);
                if (currentConfig != null) {
                    ConfigurationVersion backupVersion = new ConfigurationVersion(configId, currentConfig.getVersion(),
                            Instant.now(), "Backup before update", currentConfig);
                    versionHistory.put(configId + "_" + currentConfig.getVersion(), backupVersion);
                }

                // Update configuration
                configurations.put(configId, newConfig);

                // Create version entry
                ConfigurationVersion version = new ConfigurationVersion(configId, newConfig.getVersion(), Instant.now(),
                        "Configuration updated", newConfig);
                versionHistory.put(configId + "_" + newConfig.getVersion(), version);

                logger.debug("Successfully updated configuration: {}", configId);
                return newConfig;

            } catch (Exception e) {
                logger.error("Error updating configuration: {}", configId, e);
                throw new ConfigurationException("Failed to update configuration: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Validate configuration
     */
    public CompletableFuture<ConfigurationValidationResult> validateConfiguration(CommunicationConfig config) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<String> errors = new ArrayList<>();
                List<String> warnings = new ArrayList<>();

                // Validate required fields
                if (config.getConfigId() == null || config.getConfigId().isEmpty()) {
                    errors.add("Configuration ID is required");
                }

                if (config.getVersion() == null || config.getVersion().isEmpty()) {
                    errors.add("Configuration version is required");
                }

                // Validate messaging settings
                if (config.getMessagingConfig() != null) {
                    MessagingConfig messaging = config.getMessagingConfig();
                    if (messaging.getMaxMessageSize() <= 0) {
                        errors.add("Max message size must be positive");
                    }
                    if (messaging.getTimeoutMs() <= 0) {
                        errors.add("Timeout must be positive");
                    }
                }

                // Validate security settings
                if (config.getSecurityConfig() != null) {
                    SecurityConfig security = config.getSecurityConfig();
                    if (security.isEncryptionEnabled() && security.getEncryptionKey() == null) {
                        errors.add("Encryption key is required when encryption is enabled");
                    }
                }

                // Validate performance settings
                if (config.getPerformanceConfig() != null) {
                    PerformanceConfig performance = config.getPerformanceConfig();
                    if (performance.getMaxConcurrentConnections() <= 0) {
                        errors.add("Max concurrent connections must be positive");
                    }
                    if (performance.getConnectionTimeoutMs() <= 0) {
                        errors.add("Connection timeout must be positive");
                    }
                }

                boolean isValid = errors.isEmpty();
                return new ConfigurationValidationResult(isValid, errors, warnings, Map.of());

            } catch (Exception e) {
                logger.error("Error validating configuration", e);
                return new ConfigurationValidationResult(false, List.of("Validation error: " + e.getMessage()),
                        List.of(), Map.of());
            }
        });
    }

    /**
     * Create configuration from template
     */
    public CompletableFuture<CommunicationConfig> createFromTemplate(String templateId, String configId,
            Map<String, Object> parameters) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ConfigurationTemplate template = templates.get(templateId);
                if (template == null) {
                    throw new ConfigurationException("Template not found: " + templateId);
                }

                // Create configuration from template
                CommunicationConfig config = template.createConfiguration(configId, parameters);

                // Validate the created configuration
                ConfigurationValidationResult validation = validateConfiguration(config).get();
                if (!validation.isValid()) {
                    throw new ConfigurationException(
                            "Invalid configuration created from template: " + validation.getErrors());
                }

                // Store configuration
                configurations.put(configId, config);

                logger.debug("Successfully created configuration: {} from template: {}", configId, templateId);
                return config;

            } catch (Exception e) {
                logger.error("Error creating configuration from template: {}", templateId, e);
                throw new ConfigurationException("Failed to create configuration from template: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Apply configuration preset
     */
    public CompletableFuture<CommunicationConfig> applyPreset(String presetId, String configId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ConfigurationPreset preset = presets.get(presetId);
                if (preset == null) {
                    throw new ConfigurationException("Preset not found: " + presetId);
                }

                // Apply preset to configuration
                CommunicationConfig config = configurations.get(configId);
                if (config == null) {
                    throw new ConfigurationException("Configuration not found: " + configId);
                }

                CommunicationConfig updatedConfig = preset.applyTo(config);

                // Validate the updated configuration
                ConfigurationValidationResult validation = validateConfiguration(updatedConfig).get();
                if (!validation.isValid()) {
                    throw new ConfigurationException(
                            "Invalid configuration after applying preset: " + validation.getErrors());
                }

                // Update configuration
                configurations.put(configId, updatedConfig);

                logger.debug("Successfully applied preset: {} to configuration: {}", presetId, configId);
                return updatedConfig;

            } catch (Exception e) {
                logger.error("Error applying preset: {} to configuration: {}", presetId, configId, e);
                throw new ConfigurationException("Failed to apply preset: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Get configuration version history
     */
    public List<ConfigurationVersion> getVersionHistory(String configId) {
        return versionHistory.values().stream().filter(v -> v.configId().equals(configId))
                .sorted((v1, v2) -> v2.timestamp().compareTo(v1.timestamp())).toList();
    }

    /**
     * Rollback to previous version
     */
    public CompletableFuture<CommunicationConfig> rollbackToVersion(String configId, String version) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ConfigurationVersion targetVersion = versionHistory.get(configId + "_" + version);
                if (targetVersion == null) {
                    throw new ConfigurationException("Version not found: " + version);
                }

                // Rollback to target version
                CommunicationConfig rolledBackConfig = targetVersion.config();
                configurations.put(configId, rolledBackConfig);

                // Create version entry for rollback
                ConfigurationVersion rollbackVersion = new ConfigurationVersion(configId, "rollback_" + version,
                        Instant.now(), "Rollback to version: " + version, rolledBackConfig);
                versionHistory.put(configId + "_rollback_" + version, rollbackVersion);

                logger.debug("Successfully rolled back configuration: {} to version: {}", configId, version);
                return rolledBackConfig;

            } catch (Exception e) {
                logger.error("Error rolling back configuration: {} to version: {}", configId, version, e);
                throw new ConfigurationException("Failed to rollback configuration: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Export configuration
     */
    public CompletableFuture<String> exportConfiguration(String configId, String format) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                CommunicationConfig config = configurations.get(configId);
                if (config == null) {
                    throw new ConfigurationException("Configuration not found: " + configId);
                }

                switch (format.toLowerCase()) {
                    case "json":
                        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(config);
                    case "yaml":
                        // Note: Would need YAML library for full YAML support
                        return objectMapper.writeValueAsString(config);
                    default:
                        throw new ConfigurationException("Unsupported export format: " + format);
                }

            } catch (Exception e) {
                logger.error("Error exporting configuration: {} in format: {}", configId, format, e);
                throw new ConfigurationException("Failed to export configuration: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Import configuration
     */
    public CompletableFuture<CommunicationConfig> importConfiguration(String configId, String content, String format) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                CommunicationConfig config;
                switch (format.toLowerCase()) {
                    case "json":
                        config = objectMapper.readValue(content, CommunicationConfig.class);
                        break;
                    case "yaml":
                        // Note: Would need YAML library for full YAML support
                        config = objectMapper.readValue(content, CommunicationConfig.class);
                        break;
                    default:
                        throw new ConfigurationException("Unsupported import format: " + format);
                }

                // Validate imported configuration
                ConfigurationValidationResult validation = validateConfiguration(config).get();
                if (!validation.isValid()) {
                    throw new ConfigurationException("Invalid imported configuration: " + validation.getErrors());
                }

                // Store configuration
                configurations.put(configId, config);

                logger.debug("Successfully imported configuration: {} in format: {}", configId, format);
                return config;

            } catch (Exception e) {
                logger.error("Error importing configuration: {} in format: {}", configId, format, e);
                throw new ConfigurationException("Failed to import configuration: " + e.getMessage(), e);
            }
        });
    }

    // Background processing methods
    private void monitorConfigurations() {
        // Monitor configuration files for changes and trigger hot-reload
        configurations.forEach((configId, config) -> {
            // Check if configuration file has been modified
            // This is a simplified implementation - in practice, you'd use file watchers
            logger.debug("Monitoring configuration: {}", configId);
        });
    }

    private void createBackups() {
        // Create periodic backups of configurations
        configurations.forEach((configId, config) -> {
            try {
                String backupPath = BACKUP_CONFIG_PATH + "/" + configId + "_" + Instant.now().toEpochMilli() + ".json";
                saveConfiguration(configId, backupPath);
                logger.debug("Created backup for configuration: {}", configId);
            } catch (Exception e) {
                logger.error("Error creating backup for configuration: {}", configId, e);
            }
        });
    }

    private void createBackup(String configId, String originalPath) {
        try {
            Path path = Paths.get(originalPath);
            if (Files.exists(path)) {
                String backupPath = BACKUP_CONFIG_PATH + "/" + configId + "_" + Instant.now().toEpochMilli() + ".json";
                Files.copy(path, Paths.get(backupPath));
            }
        } catch (IOException e) {
            logger.warn("Failed to create backup for configuration: {}", configId, e);
        }
    }

    private void loadExistingConfigurations() {
        try {
            Path configDir = Paths.get(DEFAULT_CONFIG_PATH);
            if (Files.exists(configDir)) {
                Files.list(configDir).filter(path -> path.toString().endsWith(".json")).forEach(path -> {
                    String configId = path.getFileName().toString().replace(".json", "");
                    loadConfiguration(configId, path.toString());
                });
            }
        } catch (IOException e) {
            logger.warn("Error loading existing configurations", e);
        }
    }

    private void startBackgroundProcessors() {
        configMonitor.scheduleAtFixedRate(this::monitorConfigurations, 0, 60000, TimeUnit.MILLISECONDS); // 1 minute
        backupProcessor.scheduleAtFixedRate(this::createBackups, 0, 3600000, TimeUnit.MILLISECONDS); // 1 hour
    }

    private void initializeDefaultTemplates() {
        // Initialize default configuration templates
        templates.put("basic", new BasicConfigurationTemplate());
        templates.put("secure", new SecureConfigurationTemplate());
        templates.put("high-performance", new HighPerformanceConfigurationTemplate());
    }

    private void initializeDefaultPresets() {
        // Initialize default configuration presets
        presets.put("development", new DevelopmentPreset());
        presets.put("production", new ProductionPreset());
        presets.put("testing", new TestingPreset());
    }

    // Data classes extracted to top-level in this package:
    // ConfigurationStatistics, ConfigurationValidationResult, ConfigurationVersion

    // Configuration classes extracted to top-level: CommunicationConfig, MessagingConfig, SecurityConfig,
    // PerformanceConfig

    // MessagingConfig extracted to top-level

    // SecurityConfig extracted to top-level

    // PerformanceConfig extracted to top-level

    // Default template implementations
    /*
     * Extracted: org.openhab.core.ai.agent.infrastructure.config.BasicConfigurationTemplate implements
     * ConfigurationTemplate
     */

    /*
     * Extracted: org.openhab.core.ai.agent.infrastructure.config.SecureConfigurationTemplate implements
     * ConfigurationTemplate
     */

    /*
     * Extracted: org.openhab.core.ai.agent.infrastructure.config.HighPerformanceConfigurationTemplate implements
     * ConfigurationTemplate
     */

    // Default preset implementations
    /* Extracted: org.openhab.core.ai.agent.infrastructure.config.DevelopmentPreset implements ConfigurationPreset */

    /* Extracted: org.openhab.core.ai.agent.infrastructure.config.ProductionPreset implements ConfigurationPreset */

    /* Extracted: org.openhab.core.ai.agent.infrastructure.config.TestingPreset implements ConfigurationPreset */

    // ConfigurationException extracted to top-level
    
    // Metrics recording methods - replacing removed AtomicLong fields using ConfigurationOperationMetrics pattern
    
    /**
     * Record configuration load operation - replaces totalConfigurations.incrementAndGet() and successfulLoads.incrementAndGet()
     */
    private void recordConfigurationLoad(String configType, boolean success, long fileSize, Duration loadTime) {
        try {
            MetricsService metrics = metricsService;
            if (metrics != null) {
                // Use ConfigurationOperationMetrics pattern for configuration loading
                ConfigurationOperationMetrics.recordConfigurationReload(metrics, configType, "config-path", fileSize, loadTime, success, null);
            }
        } catch (Exception e) {
            logger.warn("Failed to record configuration load metric for type {}: {}", configType, e.getMessage());
        }
    }
    
    /**
     * Record configuration change operation - replaces configurationChangePatterns.incrementAndGet()
     */
    private void recordConfigurationChange(String configKey, String oldValue, String newValue, Duration changeTime) {
        try {
            MetricsService metrics = metricsService;
            if (metrics != null) {
                // Use ConfigurationOperationMetrics pattern for configuration changes
                ConfigurationOperationMetrics.recordConfigurationChange(metrics, configKey, oldValue, newValue, changeTime, true, "config-change");
            }
        } catch (Exception e) {
            logger.warn("Failed to record configuration change metric for key {}: {}", configKey, e.getMessage());
        }
    }
    
    /**
     * Record hot reload operation - replaces hotReloads.incrementAndGet()
     */
    private void recordHotReload(String configType, boolean success, Duration reloadTime) {
        try {
            MetricsService metrics = metricsService;
            if (metrics != null) {
                // Use ConfigurationOperationMetrics pattern for hot reloads
                ConfigurationOperationMetrics.recordConfigurationReload(metrics, configType, "hot-reload", 0, reloadTime, success, "hot-reload");
            }
        } catch (Exception e) {
            logger.warn("Failed to record hot reload metric for type {}: {}", configType, e.getMessage());
        }
    }
}
