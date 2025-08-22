package org.openhab.core.ai.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.config.ConfigurationException;
import org.openhab.core.ai.config.ConfigurationValidator;
import org.openhab.core.ai.model.api.ModelConfigurationService;
import org.openhab.core.ai.model.config.AnthropicConfiguration;
import org.openhab.core.ai.model.config.AzureOpenAIConfiguration;
import org.openhab.core.ai.model.config.BaseProviderConfiguration;
import org.openhab.core.ai.model.config.GoogleGenAIConfiguration;
import org.openhab.core.ai.model.config.LMStudioConfiguration;
import org.openhab.core.ai.model.config.LocalAIConfiguration;
import org.openhab.core.ai.model.config.OllamaConfiguration;
import org.openhab.core.ai.model.config.OpenAIConfiguration;
import org.openhab.core.ai.model.config.VLLMConfiguration;
import org.openhab.core.service.WatchService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the LLM Configuration Service.
 * 
 * This service manages configuration for all LLM providers, loading settings
 * from configuration files and environment variables. It integrates with
 * openHAB's file-based configuration system by using the standard WatchService
 * to monitor the conf/ai directory for changes to llm.cfg.
 * 
 * @author openHAB AI Team
 * @since 4.0.0
 */
@Component(service = ModelConfigurationService.class, configurationPid = "org.openhab.ai.model")
@NonNullByDefault
public class DefaultModelConfigurationService implements ModelConfigurationService, WatchService.WatchEventListener {

    private final Logger logger = LoggerFactory.getLogger(DefaultModelConfigurationService.class);
    private final ConfigurationValidator validator = new ConfigurationValidator();

    private final Map<String, BaseProviderConfiguration> providerConfigs = new ConcurrentHashMap<>();
    private final ScheduledExecutorService reloadExecutor = Executors.newSingleThreadScheduledExecutor();

    // Configuration file paths
    private static final String CONF_DIR = "conf";
    private static final String AI_DIR = "ai";
    private static final String MODEL_CONFIG_FILE = "model.cfg";

    // Global settings
    private String primaryProvider = "ollama";
    private String fallbackProvider = "openai";
    private boolean hybridEnabled = true;
    private boolean loadBalancingEnabled = false;
    private double defaultTemperature = 0.3;
    private int defaultMaxTokens = 1000;
    private int defaultTimeoutMs = 30000;
    private int defaultRetryAttempts = 3;

    private @Nullable WatchService watchService;
    private @Nullable Path configFilePath;

    @Activate
    public void activate(Map<String, Object> config) {
        logger.debug("Activating Model Configuration Service");

        try {
            // Validate configuration before loading
            validator.validateModelConfiguration(config);

            // Load initial configuration
            loadProviderConfigurations(config);

            // Initialize configuration file and start watching
            initializeConfigurationFile();

            logger.info("Model Configuration Service activated. Primary provider: {}, Fallback: {}", primaryProvider,
                    fallbackProvider);
        } catch (ConfigurationException e) {
            logger.error("Failed to activate Model Configuration Service: {}", e.getMessage());
            // Load default configuration as fallback
            loadDefaultConfiguration();
        }
    }

    @Modified
    public void modified(Map<String, Object> config) {
        logger.debug("Modifying Model Configuration Service");

        try {
            // Validate new configuration
            validator.validateModelConfiguration(config);

            // Reload configuration
            loadProviderConfigurations(config);

            logger.info("Model Configuration Service modified. Primary provider: {}, Fallback: {}", primaryProvider,
                    fallbackProvider);
        } catch (ConfigurationException e) {
            logger.error("Failed to modify Model Configuration Service: {}", e.getMessage());
            // Keep existing configuration
        }
    }

    @Deactivate
    public void deactivate() {
        logger.debug("Deactivating LLM Configuration Service");

        // Stop file watching
        if (watchService != null) {
            watchService.unregisterListener(this);
        }

        // Shutdown executor
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

        logger.info("LLM Configuration Service deactivated");
    }

    @Reference(target = WatchService.CONFIG_WATCHER_FILTER)
    protected void setWatchService(WatchService watchService) {
        this.watchService = watchService;
        if (configFilePath != null) {
            // Register for watching the ai directory
            Path aiDir = Paths.get(AI_DIR);
            watchService.registerListener(this, aiDir, false);
            logger.debug("Registered with WatchService for directory: {}", aiDir);
        }
    }

    protected void unsetWatchService(WatchService watchService) {
        if (this.watchService == watchService) {
            this.watchService = null;
        }
    }

    @Override
    public void processWatchEvent(WatchService.Kind kind, Path fullPath) {
        // Only process events for our configuration file
        if (fullPath.endsWith(MODEL_CONFIG_FILE)) {
            logger.debug("Configuration file change detected: {} - {}", kind, fullPath);

            switch (kind) {
                case MODIFY:
                    logger.info("Configuration file modified, reloading...");
                    // Use a small delay to ensure file is fully written
                    reloadExecutor.schedule(this::reloadConfigurationFromFile, 500, TimeUnit.MILLISECONDS);
                    break;
                case CREATE:
                    logger.info("Configuration file created, loading...");
                    reloadExecutor.schedule(this::reloadConfigurationFromFile, 100, TimeUnit.MILLISECONDS);
                    break;
                case DELETE:
                    logger.warn("Configuration file deleted, using default configuration");
                    loadDefaultConfiguration();
                    break;
                default:
                    // Ignore other events
                    break;
            }
        }
    }

    @Override
    public @Nullable String getPrimaryProvider() {
        return primaryProvider;
    }

    @Override
    public @Nullable String getFallbackProvider() {
        return fallbackProvider;
    }

    @Override
    public @Nullable OpenAIConfiguration getOpenAIConfig() {
        return (OpenAIConfiguration) providerConfigs.get("openai");
    }

    @Override
    public @Nullable AnthropicConfiguration getAnthropicConfig() {
        return (AnthropicConfiguration) providerConfigs.get("anthropic");
    }

    @Override
    public @Nullable GoogleGenAIConfiguration getGoogleConfig() {
        return (GoogleGenAIConfiguration) providerConfigs.get("google");
    }

    @Override
    public @Nullable AzureOpenAIConfiguration getAzureConfig() {
        return (AzureOpenAIConfiguration) providerConfigs.get("azure");
    }

    @Override
    public @Nullable OllamaConfiguration getOllamaConfig() {
        return (OllamaConfiguration) providerConfigs.get("ollama");
    }

    @Override
    public @Nullable LocalAIConfiguration getLocalAIConfig() {
        return (LocalAIConfiguration) providerConfigs.get("localai");
    }

    @Override
    public @Nullable VLLMConfiguration getVLLMConfig() {
        return (VLLMConfiguration) providerConfigs.get("vllm");
    }

    @Override
    public @Nullable LMStudioConfiguration getLMStudioConfig() {
        return (LMStudioConfiguration) providerConfigs.get("lmstudio");
    }

    @Override
    public boolean isHybridEnabled() {
        return hybridEnabled;
    }

    @Override
    public boolean isLoadBalancingEnabled() {
        return loadBalancingEnabled;
    }

    @Override
    public double getDefaultTemperature() {
        return defaultTemperature;
    }

    @Override
    public int getDefaultMaxTokens() {
        return defaultMaxTokens;
    }

    @Override
    public int getDefaultTimeoutMs() {
        return defaultTimeoutMs;
    }

    @Override
    public int getDefaultRetryAttempts() {
        return defaultRetryAttempts;
    }

    /**
     * Initialize the configuration file and directory structure.
     */
    private void initializeConfigurationFile() {
        try {
            Path confDir = Paths.get(CONF_DIR, AI_DIR);
            configFilePath = confDir.resolve(MODEL_CONFIG_FILE);

            // Create conf/ai directory if it doesn't exist
            if (!Files.exists(confDir)) {
                Files.createDirectories(confDir);
                logger.info("Created configuration directory: {}", confDir);
            }

            // Create default config file if it doesn't exist
            if (configFilePath != null && !Files.exists(configFilePath)) {
                createDefaultConfigFile(configFilePath);
                logger.info("Created default configuration file: {}", configFilePath);
            }

            // Register with WatchService if available
            if (watchService != null) {
                Path aiDir = Paths.get(AI_DIR);
                watchService.registerListener(this, aiDir, false);
                logger.info("Registered with WatchService for directory: {}", aiDir);
            }

        } catch (IOException e) {
            logger.warn("Failed to initialize configuration file: {}", e.getMessage());
        }
    }

    /**
     * Loads provider configurations from the configuration map.
     * This method is called both from OSGi configuration and file-based configuration.
     */
    private void loadProviderConfigurations(Map<String, Object> config) {
        logger.debug("Loading model provider configurations");

        // Load global settings with ai.model.* prefix
        primaryProvider = getStringConfig(config, "ai.model.primary.provider", "ollama");
        fallbackProvider = getStringConfig(config, "ai.model.fallback.provider", "openai");
        hybridEnabled = getBooleanConfig(config, "ai.model.hybrid.enabled", true);
        loadBalancingEnabled = getBooleanConfig(config, "ai.model.load.balancing.enabled", false);
        defaultTemperature = getDoubleConfig(config, "ai.model.default.temperature", 0.3);
        defaultMaxTokens = getIntConfig(config, "ai.model.default.maxTokens", 1000);
        defaultTimeoutMs = getIntConfig(config, "ai.model.default.timeoutMs", 30000);
        defaultRetryAttempts = getIntConfig(config, "ai.model.default.retryAttempts", 3);

        // Load provider-specific configurations
        providerConfigs.clear();

        if (getBooleanConfig(config, "ai.model.openai.enabled", false)) {
            providerConfigs.put("openai", buildOpenAIConfig(config));
        }

        if (getBooleanConfig(config, "ai.model.anthropic.enabled", false)) {
            providerConfigs.put("anthropic", buildAnthropicConfig(config));
        }

        if (getBooleanConfig(config, "ai.model.google.enabled", false)) {
            providerConfigs.put("google", buildGoogleConfig(config));
        }

        if (getBooleanConfig(config, "ai.model.azure.enabled", false)) {
            providerConfigs.put("azure", buildAzureConfig(config));
        }

        if (getBooleanConfig(config, "ai.model.ollama.enabled", true)) {
            providerConfigs.put("ollama", buildOllamaConfig(config));
        }

        if (getBooleanConfig(config, "ai.model.localai.enabled", false)) {
            providerConfigs.put("localai", buildLocalAIConfig(config));
        }

        if (getBooleanConfig(config, "ai.model.vllm.enabled", false)) {
            providerConfigs.put("vllm", buildVLLMConfig(config));
        }

        if (getBooleanConfig(config, "ai.model.lmstudio.enabled", false)) {
            providerConfigs.put("lmstudio", buildLMStudioConfig(config));
        }

        logger.debug("Loaded {} provider configurations", providerConfigs.size());
    }

    /**
     * Reloads configuration from the conf/ai/llm.cfg file.
     */
    private void reloadConfigurationFromFile() {
        if (configFilePath == null || !Files.exists(configFilePath)) {
            logger.warn("Configuration file does not exist: {}", configFilePath);
            return;
        }

        try {
            Properties props = new Properties();
            props.load(Files.newInputStream(configFilePath));

            // Convert Properties to Map<String, Object> for loadProviderConfigurations
            Map<String, Object> config = new ConcurrentHashMap<>();
            props.forEach((key, value) -> config.put(key.toString(), value));

            // Reload configuration
            loadProviderConfigurations(config);

            logger.info("Configuration reloaded from file: {}", configFilePath);

        } catch (IOException e) {
            logger.warn("Failed to reload configuration from file: {}", e.getMessage());
        }
    }

    /**
     * Loads default configuration when the config file is deleted.
     */
    private void loadDefaultConfiguration() {
        Map<String, Object> defaultConfig = new ConcurrentHashMap<>();
        defaultConfig.put("ai.model.primary.provider", "ollama");
        defaultConfig.put("ai.model.fallback.provider", "openai");
        defaultConfig.put("ai.model.hybrid.enabled", "true");
        defaultConfig.put("ai.model.load.balancing.enabled", "false");
        defaultConfig.put("ai.model.ollama.enabled", "true");
        defaultConfig.put("ai.model.openai.enabled", "false");

        loadProviderConfigurations(defaultConfig);
        logger.info("Loaded default configuration");
    }

    /**
     * Creates a default configuration file with sample settings.
     */
    private void createDefaultConfigFile(@Nullable Path configFile) throws IOException {
        if (configFile == null) {
            throw new IllegalArgumentException("Config file path cannot be null");
        }
        String defaultConfig = """
                # OpenHAB AI - Model Configuration
                # This file configures the AI model providers for the AI system.

                # Global settings
                ai.model.primary.provider=ollama
                ai.model.fallback.provider=openai
                ai.model.hybrid.enabled=true
                ai.model.load.balancing.enabled=false
                ai.model.default.temperature=0.3
                ai.model.default.maxTokens=1000
                ai.model.default.timeoutMs=30000
                ai.model.default.retryAttempts=3

                # Ollama (local) - enabled by default
                ai.model.ollama.enabled=true
                ai.model.ollama.baseUrl=http://localhost:11434
                ai.model.ollama.defaultModel=llama3.1:8b
                ai.model.ollama.timeoutMs=60000
                ai.model.ollama.retryAttempts=2

                # OpenAI - disabled by default
                ai.model.openai.enabled=false
                ai.model.openai.apiKey=your_openai_api_key_here
                ai.model.openai.defaultModel=gpt-4o-mini
                ai.model.openai.timeoutMs=30000
                ai.model.openai.retryAttempts=3

                # Other providers disabled by default
                ai.model.anthropic.enabled=false
                ai.model.google.enabled=false
                ai.model.azure.enabled=false
                ai.model.localai.enabled=false
                ai.model.vllm.enabled=false
                ai.model.lmstudio.enabled=false
                """;

        Files.writeString(configFile, defaultConfig);
    }

    /**
     * Builds OpenAI configuration from the configuration map.
     */
    private OpenAIConfiguration buildOpenAIConfig(Map<String, Object> config) {
        boolean enabled = getBooleanConfig(config, "ai.model.openai.enabled", false);
        String modelName = getStringConfig(config, "ai.model.openai.defaultModel", "gpt-4o-mini");
        double temperature = getDoubleConfig(config, "ai.model.openai.temperature", 0.3);
        int maxTokens = getIntConfig(config, "ai.model.openai.maxTokens", 4000);
        int timeoutMs = getIntConfig(config, "ai.model.openai.timeoutMs", 30000);
        int retryAttempts = getIntConfig(config, "ai.model.openai.retryAttempts", 3);
        String systemPrompt = getNullableStringConfig(config, "ai.model.openai.systemPrompt", null);
        String apiKey = getNullableStringConfig(config, "ai.model.openai.apiKey", null);
        String baseUrl = getStringConfig(config, "ai.model.openai.baseUrl", "https://api.openai.com/v1");
        double costPer1kTokens = getDoubleConfig(config, "ai.model.openai.costPer1kTokens", 0.00015);

        return new OpenAIConfiguration(enabled, modelName, temperature, maxTokens, timeoutMs, retryAttempts,
                systemPrompt, apiKey, baseUrl, costPer1kTokens);
    }

    /**
     * Builds Anthropic configuration from the configuration map.
     */
    private AnthropicConfiguration buildAnthropicConfig(Map<String, Object> config) {
        boolean enabled = getBooleanConfig(config, "ai.model.anthropic.enabled", false);
        String modelName = getStringConfig(config, "ai.model.anthropic.defaultModel", "claude-3-5-sonnet-20241022");
        double temperature = getDoubleConfig(config, "ai.model.anthropic.temperature", 0.3);
        int maxTokens = getIntConfig(config, "ai.model.anthropic.maxTokens", 4000);
        int timeoutMs = getIntConfig(config, "ai.model.anthropic.timeoutMs", 30000);
        int retryAttempts = getIntConfig(config, "ai.model.anthropic.retryAttempts", 3);
        String systemPrompt = getNullableStringConfig(config, "ai.model.anthropic.systemPrompt", null);
        String apiKey = getNullableStringConfig(config, "ai.model.anthropic.apiKey", null);
        String baseUrl = getStringConfig(config, "ai.model.anthropic.baseUrl", "https://api.anthropic.com");
        double costPer1kTokens = getDoubleConfig(config, "ai.model.anthropic.costPer1kTokens", 0.00015);

        return new AnthropicConfiguration(enabled, modelName, temperature, maxTokens, timeoutMs, retryAttempts,
                systemPrompt, apiKey, baseUrl, costPer1kTokens);
    }

    /**
     * Builds Google GenAI configuration from the configuration map.
     */
    private GoogleGenAIConfiguration buildGoogleConfig(Map<String, Object> config) {
        boolean enabled = getBooleanConfig(config, "ai.model.google.enabled", false);
        String modelName = getStringConfig(config, "ai.model.google.defaultModel", "gemini-1.5-pro");
        double temperature = getDoubleConfig(config, "ai.model.google.temperature", 0.3);
        int maxTokens = getIntConfig(config, "ai.model.google.maxTokens", 4000);
        int timeoutMs = getIntConfig(config, "ai.model.google.timeoutMs", 30000);
        int retryAttempts = getIntConfig(config, "ai.model.google.retryAttempts", 3);
        String systemPrompt = getNullableStringConfig(config, "ai.model.google.systemPrompt", null);
        String apiKey = getNullableStringConfig(config, "ai.model.google.apiKey", null);
        String baseUrl = getStringConfig(config, "ai.model.google.baseUrl",
                "https://generativelanguage.googleapis.com");
        double costPer1kTokens = getDoubleConfig(config, "ai.model.google.costPer1kTokens", 0.000125);

        return new GoogleGenAIConfiguration(enabled, modelName, temperature, maxTokens, timeoutMs, retryAttempts,
                systemPrompt, apiKey, baseUrl, costPer1kTokens);
    }

    /**
     * Builds Azure OpenAI configuration from the configuration map.
     */
    private AzureOpenAIConfiguration buildAzureConfig(Map<String, Object> config) {
        boolean enabled = getBooleanConfig(config, "ai.model.azure.enabled", false);
        String modelName = getStringConfig(config, "ai.model.azure.defaultModel", "gpt-4o-mini");
        double temperature = getDoubleConfig(config, "ai.model.azure.temperature", 0.3);
        int maxTokens = getIntConfig(config, "ai.model.azure.maxTokens", 4000);
        int timeoutMs = getIntConfig(config, "ai.model.azure.timeoutMs", 30000);
        int retryAttempts = getIntConfig(config, "ai.model.azure.retryAttempts", 3);
        String systemPrompt = getNullableStringConfig(config, "ai.model.azure.systemPrompt", null);
        String apiKey = getNullableStringConfig(config, "ai.model.azure.apiKey", null);
        String endpoint = getStringConfig(config, "ai.model.azure.endpoint", "");
        String deploymentName = getStringConfig(config, "ai.model.azure.deploymentName", "gpt-4o-mini");
        double costPer1kTokens = getDoubleConfig(config, "ai.model.azure.costPer1kTokens", 0.03);

        return new AzureOpenAIConfiguration(enabled, modelName, temperature, maxTokens, timeoutMs, retryAttempts,
                systemPrompt, apiKey, endpoint, deploymentName, costPer1kTokens);
    }

    /**
     * Builds Ollama configuration from the configuration map.
     */
    private OllamaConfiguration buildOllamaConfig(Map<String, Object> config) {
        boolean enabled = getBooleanConfig(config, "ai.model.ollama.enabled", true);
        String modelName = getStringConfig(config, "ai.model.ollama.defaultModel", "llama3.1:8b");
        double temperature = getDoubleConfig(config, "ai.model.ollama.temperature", 0.3);
        int maxTokens = getIntConfig(config, "ai.model.ollama.maxTokens", 4000);
        int timeoutMs = getIntConfig(config, "ai.model.ollama.timeoutMs", 60000);
        int retryAttempts = getIntConfig(config, "ai.model.ollama.retryAttempts", 2);
        String systemPrompt = getStringConfig(config, "ai.model.ollama.systemPrompt",
                "You are a helpful AI assistant.");
        String baseUrl = getStringConfig(config, "ai.model.ollama.baseUrl", "http://localhost:11434");
        int concurrentRequests = getIntConfig(config, "ai.model.ollama.concurrentRequests", 3);
        boolean autoStartOllama = getBooleanConfig(config, "ai.model.ollama.autoStart", true);
        boolean autoInstallOllama = getBooleanConfig(config, "ai.model.ollama.autoInstall", false);

        return new OllamaConfiguration(enabled, modelName, temperature, maxTokens, timeoutMs, retryAttempts,
                systemPrompt, baseUrl, concurrentRequests, autoStartOllama, autoInstallOllama);
    }

    /**
     * Builds LocalAI configuration from the configuration map.
     */
    private LocalAIConfiguration buildLocalAIConfig(Map<String, Object> config) {
        boolean enabled = getBooleanConfig(config, "ai.model.localai.enabled", false);
        String modelName = getStringConfig(config, "ai.model.localai.defaultModel", "llama3.1:8b");
        double temperature = getDoubleConfig(config, "ai.model.localai.temperature", 0.3);
        int maxTokens = getIntConfig(config, "ai.model.localai.maxTokens", 4000);
        int timeoutMs = getIntConfig(config, "ai.model.localai.timeoutMs", 60000);
        int retryAttempts = getIntConfig(config, "ai.model.localai.retryAttempts", 2);
        String systemPrompt = getStringConfig(config, "ai.model.localai.systemPrompt",
                "You are a helpful AI assistant.");
        String baseUrl = getStringConfig(config, "ai.model.localai.baseUrl", "http://localhost:8080");

        return new LocalAIConfiguration(enabled, modelName, temperature, maxTokens, timeoutMs, retryAttempts,
                systemPrompt, baseUrl);
    }

    /**
     * Builds vLLM configuration from the configuration map.
     */
    private VLLMConfiguration buildVLLMConfig(Map<String, Object> config) {
        boolean enabled = getBooleanConfig(config, "ai.model.vllm.enabled", false);
        String modelName = getStringConfig(config, "ai.model.vllm.defaultModel", "llama3.1:8b");
        double temperature = getDoubleConfig(config, "ai.model.vllm.temperature", 0.3);
        int maxTokens = getIntConfig(config, "ai.model.vllm.maxTokens", 4000);
        int timeoutMs = getIntConfig(config, "ai.model.vllm.timeoutMs", 60000);
        int retryAttempts = getIntConfig(config, "ai.model.vllm.retryAttempts", 2);
        String systemPrompt = getStringConfig(config, "ai.model.vllm.systemPrompt", "You are a helpful AI assistant.");
        String baseUrl = getStringConfig(config, "ai.model.vllm.baseUrl", "http://localhost:8000");

        return new VLLMConfiguration(enabled, modelName, temperature, maxTokens, timeoutMs, retryAttempts, systemPrompt,
                baseUrl);
    }

    /**
     * Builds LM Studio configuration from the configuration map.
     */
    private LMStudioConfiguration buildLMStudioConfig(Map<String, Object> config) {
        boolean enabled = getBooleanConfig(config, "ai.model.lmstudio.enabled", false);
        String modelName = getStringConfig(config, "ai.model.lmstudio.defaultModel", "llama3.1:8b");
        double temperature = getDoubleConfig(config, "ai.model.lmstudio.temperature", 0.3);
        int maxTokens = getIntConfig(config, "ai.model.lmstudio.maxTokens", 4000);
        int timeoutMs = getIntConfig(config, "ai.model.lmstudio.timeoutMs", 60000);
        int retryAttempts = getIntConfig(config, "ai.model.lmstudio.retryAttempts", 2);
        String systemPrompt = getStringConfig(config, "ai.model.lmstudio.systemPrompt",
                "You are a helpful AI assistant.");
        String baseUrl = getStringConfig(config, "ai.model.lmstudio.baseUrl", "http://localhost:1234");

        return new LMStudioConfiguration(enabled, modelName, temperature, maxTokens, timeoutMs, retryAttempts,
                systemPrompt, baseUrl);
    }

    /**
     * Helper method to get a string configuration value.
     */
    private String getStringConfig(Map<String, Object> config, String key, String defaultValue) {
        Object value = config.get(key);
        if (value instanceof String) {
            return (String) value;
        }
        return defaultValue;
    }

    /**
     * Helper method to get a string configuration value that can be null.
     */
    private @Nullable String getNullableStringConfig(Map<String, Object> config, String key,
            @Nullable String defaultValue) {
        Object value = config.get(key);
        if (value instanceof String) {
            return (String) value;
        }
        return defaultValue;
    }

    /**
     * Helper method to get a boolean configuration value.
     */
    private boolean getBooleanConfig(Map<String, Object> config, String key, boolean defaultValue) {
        Object value = config.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return defaultValue;
    }

    /**
     * Helper method to get an integer configuration value.
     */
    private int getIntConfig(Map<String, Object> config, String key, int defaultValue) {
        Object value = config.get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                logger.warn("Invalid integer value for config key {}: {}", key, value);
            }
        }
        return defaultValue;
    }

    /**
     * Helper method to get a double configuration value.
     */
    private double getDoubleConfig(Map<String, Object> config, String key, double defaultValue) {
        Object value = config.get(key);
        if (value instanceof Double) {
            return (Double) value;
        }
        if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                logger.warn("Invalid double value for config key {}: {}", key, value);
            }
        }
        return defaultValue;
    }
}
