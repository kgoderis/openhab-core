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
import org.openhab.core.ai.model.api.ModelConfigurationService;
import org.openhab.core.ai.model.configuration.AnthropicConfiguration;
import org.openhab.core.ai.model.configuration.AzureOpenAIConfiguration;
import org.openhab.core.ai.model.configuration.BaseProviderConfiguration;
import org.openhab.core.ai.model.configuration.GoogleGenAIConfiguration;
import org.openhab.core.ai.model.configuration.LMStudioConfiguration;
import org.openhab.core.ai.model.configuration.LocalAIConfiguration;
import org.openhab.core.ai.model.configuration.OllamaConfiguration;
import org.openhab.core.ai.model.configuration.OpenAIConfiguration;
import org.openhab.core.ai.model.configuration.VLLMConfiguration;
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
@Component(service = ModelConfigurationService.class, configurationPid = "org.openhab.ai.llm")
@NonNullByDefault
public class DefaultModelConfigurationService implements ModelConfigurationService, WatchService.WatchEventListener {

    private final Logger logger = LoggerFactory.getLogger(DefaultModelConfigurationService.class);

    private final Map<String, BaseProviderConfiguration> providerConfigs = new ConcurrentHashMap<>();
    private final ScheduledExecutorService reloadExecutor = Executors.newSingleThreadScheduledExecutor();

    // Configuration file paths
    private static final String CONF_DIR = "conf";
    private static final String AI_DIR = "ai";
    private static final String LLM_CONFIG_FILE = "llm.cfg";

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
        logger.debug("Activating LLM Configuration Service");

        // Load initial configuration
        loadProviderConfigurations(config);

        // Initialize configuration file and start watching
        initializeConfigurationFile();

        logger.info("LLM Configuration Service activated. Primary provider: {}, Fallback: {}", primaryProvider,
                fallbackProvider);
    }

    @Modified
    public void modified(Map<String, Object> config) {
        logger.debug("Modifying LLM Configuration Service");
        loadProviderConfigurations(config);
        logger.info("LLM Configuration Service modified. Primary provider: {}, Fallback: {}", primaryProvider,
                fallbackProvider);
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
        if (fullPath.endsWith(LLM_CONFIG_FILE)) {
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
            configFilePath = confDir.resolve(LLM_CONFIG_FILE);

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
        logger.debug("Loading LLM provider configurations");

        // Load global settings
        primaryProvider = getStringConfig(config, "primary.provider", "ollama");
        fallbackProvider = getStringConfig(config, "fallback.provider", "openai");
        hybridEnabled = getBooleanConfig(config, "hybrid.enabled", true);
        loadBalancingEnabled = getBooleanConfig(config, "load.balancing.enabled", false);
        defaultTemperature = getDoubleConfig(config, "default.temperature", 0.3);
        defaultMaxTokens = getIntConfig(config, "default.maxTokens", 1000);
        defaultTimeoutMs = getIntConfig(config, "default.timeoutMs", 30000);
        defaultRetryAttempts = getIntConfig(config, "default.retryAttempts", 3);

        // Load provider-specific configurations
        providerConfigs.clear();

        if (getBooleanConfig(config, "openai.enabled", false)) {
            providerConfigs.put("openai", buildOpenAIConfig(config));
        }

        if (getBooleanConfig(config, "anthropic.enabled", false)) {
            providerConfigs.put("anthropic", buildAnthropicConfig(config));
        }

        if (getBooleanConfig(config, "google.enabled", false)) {
            providerConfigs.put("google", buildGoogleConfig(config));
        }

        if (getBooleanConfig(config, "azure.enabled", false)) {
            providerConfigs.put("azure", buildAzureConfig(config));
        }

        if (getBooleanConfig(config, "ollama.enabled", true)) {
            providerConfigs.put("ollama", buildOllamaConfig(config));
        }

        if (getBooleanConfig(config, "localai.enabled", false)) {
            providerConfigs.put("localai", buildLocalAIConfig(config));
        }

        if (getBooleanConfig(config, "vllm.enabled", false)) {
            providerConfigs.put("vllm", buildVLLMConfig(config));
        }

        if (getBooleanConfig(config, "lmstudio.enabled", false)) {
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
        defaultConfig.put("primary.provider", "ollama");
        defaultConfig.put("fallback.provider", "openai");
        defaultConfig.put("hybrid.enabled", "true");
        defaultConfig.put("load.balancing.enabled", "false");
        defaultConfig.put("ollama.enabled", "true");
        defaultConfig.put("openai.enabled", "false");

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
                # OpenHAB AI - LLM Configuration
                # This file configures the LLM providers for the AI system.

                # Global settings
                primary.provider=ollama
                fallback.provider=openai
                hybrid.enabled=true
                load.balancing.enabled=false
                default.temperature=0.3
                default.maxTokens=1000
                default.timeoutMs=30000
                default.retryAttempts=3

                # Ollama (local) - enabled by default
                ollama.enabled=true
                ollama.baseUrl=http://localhost:11434
                ollama.defaultModel=llama3.1:8b
                ollama.timeoutMs=60000
                ollama.retryAttempts=2

                # OpenAI - disabled by default
                openai.enabled=false
                openai.apiKey=your_openai_api_key_here
                openai.defaultModel=gpt-4o-mini
                openai.timeoutMs=30000
                openai.retryAttempts=3

                # Other providers disabled by default
                anthropic.enabled=false
                google.enabled=false
                azure.enabled=false
                localai.enabled=false
                vllm.enabled=false
                lmstudio.enabled=false
                """;

        Files.writeString(configFile, defaultConfig);
    }

    /**
     * Builds OpenAI configuration from the configuration map.
     */
    private OpenAIConfiguration buildOpenAIConfig(Map<String, Object> config) {
        boolean enabled = getBooleanConfig(config, "openai.enabled", false);
        String modelName = getStringConfig(config, "openai.defaultModel", "gpt-4o-mini");
        double temperature = getDoubleConfig(config, "openai.temperature", 0.3);
        int maxTokens = getIntConfig(config, "openai.maxTokens", 4000);
        int timeoutMs = getIntConfig(config, "openai.timeoutMs", 30000);
        int retryAttempts = getIntConfig(config, "openai.retryAttempts", 3);
        String systemPrompt = getNullableStringConfig(config, "openai.systemPrompt", null);
        String apiKey = getNullableStringConfig(config, "openai.apiKey", null);
        String baseUrl = getStringConfig(config, "openai.baseUrl", "https://api.openai.com/v1");
        double costPer1kTokens = getDoubleConfig(config, "openai.costPer1kTokens", 0.00015);

        return new OpenAIConfiguration(enabled, modelName, temperature, maxTokens, timeoutMs, retryAttempts,
                systemPrompt, apiKey, baseUrl, costPer1kTokens);
    }

    /**
     * Builds Anthropic configuration from the configuration map.
     */
    private AnthropicConfiguration buildAnthropicConfig(Map<String, Object> config) {
        boolean enabled = getBooleanConfig(config, "anthropic.enabled", false);
        String modelName = getStringConfig(config, "anthropic.defaultModel", "claude-3-5-sonnet-20241022");
        double temperature = getDoubleConfig(config, "anthropic.temperature", 0.3);
        int maxTokens = getIntConfig(config, "anthropic.maxTokens", 4000);
        int timeoutMs = getIntConfig(config, "anthropic.timeoutMs", 30000);
        int retryAttempts = getIntConfig(config, "anthropic.retryAttempts", 3);
        String systemPrompt = getNullableStringConfig(config, "anthropic.systemPrompt", null);
        String apiKey = getNullableStringConfig(config, "anthropic.apiKey", null);
        String baseUrl = getStringConfig(config, "anthropic.baseUrl", "https://api.anthropic.com");
        double costPer1kTokens = getDoubleConfig(config, "anthropic.costPer1kTokens", 0.00015);

        return new AnthropicConfiguration(enabled, modelName, temperature, maxTokens, timeoutMs, retryAttempts,
                systemPrompt, apiKey, baseUrl, costPer1kTokens);
    }

    /**
     * Builds Google GenAI configuration from the configuration map.
     */
    private GoogleGenAIConfiguration buildGoogleConfig(Map<String, Object> config) {
        boolean enabled = getBooleanConfig(config, "google.enabled", false);
        String modelName = getStringConfig(config, "google.defaultModel", "gemini-1.5-pro");
        double temperature = getDoubleConfig(config, "google.temperature", 0.3);
        int maxTokens = getIntConfig(config, "google.maxTokens", 4000);
        int timeoutMs = getIntConfig(config, "google.timeoutMs", 30000);
        int retryAttempts = getIntConfig(config, "google.retryAttempts", 3);
        String systemPrompt = getNullableStringConfig(config, "google.systemPrompt", null);
        String apiKey = getNullableStringConfig(config, "google.apiKey", null);
        String baseUrl = getStringConfig(config, "google.baseUrl", "https://generativelanguage.googleapis.com");
        double costPer1kTokens = getDoubleConfig(config, "google.costPer1kTokens", 0.000125);

        return new GoogleGenAIConfiguration(enabled, modelName, temperature, maxTokens, timeoutMs, retryAttempts,
                systemPrompt, apiKey, baseUrl, costPer1kTokens);
    }

    /**
     * Builds Azure OpenAI configuration from the configuration map.
     */
    private AzureOpenAIConfiguration buildAzureConfig(Map<String, Object> config) {
        boolean enabled = getBooleanConfig(config, "azure.enabled", false);
        String modelName = getStringConfig(config, "azure.defaultModel", "gpt-4o-mini");
        double temperature = getDoubleConfig(config, "azure.temperature", 0.3);
        int maxTokens = getIntConfig(config, "azure.maxTokens", 4000);
        int timeoutMs = getIntConfig(config, "azure.timeoutMs", 30000);
        int retryAttempts = getIntConfig(config, "azure.retryAttempts", 3);
        String systemPrompt = getNullableStringConfig(config, "azure.systemPrompt", null);
        String apiKey = getNullableStringConfig(config, "azure.apiKey", null);
        String endpoint = getStringConfig(config, "azure.endpoint", "");
        String deploymentName = getStringConfig(config, "azure.deploymentName", "gpt-4o-mini");

        return new AzureOpenAIConfiguration(enabled, modelName, temperature, maxTokens, timeoutMs, retryAttempts,
                systemPrompt, apiKey, endpoint, deploymentName);
    }

    /**
     * Builds Ollama configuration from the configuration map.
     */
    private OllamaConfiguration buildOllamaConfig(Map<String, Object> config) {
        boolean enabled = getBooleanConfig(config, "ollama.enabled", true);
        String modelName = getStringConfig(config, "ollama.defaultModel", "llama3.1:8b");
        double temperature = getDoubleConfig(config, "ollama.temperature", 0.3);
        int maxTokens = getIntConfig(config, "ollama.maxTokens", 4000);
        int timeoutMs = getIntConfig(config, "ollama.timeoutMs", 60000);
        int retryAttempts = getIntConfig(config, "ollama.retryAttempts", 2);
        String systemPrompt = getStringConfig(config, "ollama.systemPrompt", "You are a helpful AI assistant.");
        String baseUrl = getStringConfig(config, "ollama.baseUrl", "http://localhost:11434");
        int concurrentRequests = getIntConfig(config, "ollama.concurrentRequests", 3);
        boolean autoStartOllama = getBooleanConfig(config, "ollama.autoStart", true);
        boolean autoInstallOllama = getBooleanConfig(config, "ollama.autoInstall", false);

        return new OllamaConfiguration(enabled, modelName, temperature, maxTokens, timeoutMs, retryAttempts,
                systemPrompt, baseUrl, concurrentRequests, autoStartOllama, autoInstallOllama);
    }

    /**
     * Builds LocalAI configuration from the configuration map.
     */
    private LocalAIConfiguration buildLocalAIConfig(Map<String, Object> config) {
        boolean enabled = getBooleanConfig(config, "localai.enabled", false);
        String modelName = getStringConfig(config, "localai.defaultModel", "llama3.1:8b");
        double temperature = getDoubleConfig(config, "localai.temperature", 0.3);
        int maxTokens = getIntConfig(config, "localai.maxTokens", 4000);
        int timeoutMs = getIntConfig(config, "localai.timeoutMs", 60000);
        int retryAttempts = getIntConfig(config, "localai.retryAttempts", 2);
        String systemPrompt = getStringConfig(config, "localai.systemPrompt", "You are a helpful AI assistant.");
        String baseUrl = getStringConfig(config, "localai.baseUrl", "http://localhost:8080");

        return new LocalAIConfiguration(enabled, modelName, temperature, maxTokens, timeoutMs, retryAttempts,
                systemPrompt, baseUrl);
    }

    /**
     * Builds vLLM configuration from the configuration map.
     */
    private VLLMConfiguration buildVLLMConfig(Map<String, Object> config) {
        boolean enabled = getBooleanConfig(config, "vllm.enabled", false);
        String modelName = getStringConfig(config, "vllm.defaultModel", "llama3.1:8b");
        double temperature = getDoubleConfig(config, "vllm.temperature", 0.3);
        int maxTokens = getIntConfig(config, "vllm.maxTokens", 4000);
        int timeoutMs = getIntConfig(config, "vllm.timeoutMs", 60000);
        int retryAttempts = getIntConfig(config, "vllm.retryAttempts", 2);
        String systemPrompt = getStringConfig(config, "vllm.systemPrompt", "You are a helpful AI assistant.");
        String baseUrl = getStringConfig(config, "vllm.baseUrl", "http://localhost:8000");

        return new VLLMConfiguration(enabled, modelName, temperature, maxTokens, timeoutMs, retryAttempts, systemPrompt,
                baseUrl);
    }

    /**
     * Builds LM Studio configuration from the configuration map.
     */
    private LMStudioConfiguration buildLMStudioConfig(Map<String, Object> config) {
        boolean enabled = getBooleanConfig(config, "lmstudio.enabled", false);
        String modelName = getStringConfig(config, "lmstudio.defaultModel", "llama3.1:8b");
        double temperature = getDoubleConfig(config, "lmstudio.temperature", 0.3);
        int maxTokens = getIntConfig(config, "lmstudio.maxTokens", 4000);
        int timeoutMs = getIntConfig(config, "lmstudio.timeoutMs", 60000);
        int retryAttempts = getIntConfig(config, "lmstudio.retryAttempts", 2);
        String systemPrompt = getStringConfig(config, "lmstudio.systemPrompt", "You are a helpful AI assistant.");
        String baseUrl = getStringConfig(config, "lmstudio.baseUrl", "http://localhost:1234");

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
