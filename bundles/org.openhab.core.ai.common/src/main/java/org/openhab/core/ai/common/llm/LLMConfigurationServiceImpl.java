package org.openhab.core.ai.common.llm;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.api.llm.LLMConfigurationService;
import org.openhab.core.ai.common.llm.configuration.AnthropicConfiguration;
import org.openhab.core.ai.common.llm.configuration.AzureOpenAIConfiguration;
import org.openhab.core.ai.common.llm.configuration.BaseLLMConfiguration;
import org.openhab.core.ai.common.llm.configuration.GoogleGenAIConfiguration;
import org.openhab.core.ai.common.llm.configuration.LocalAIConfiguration;
import org.openhab.core.ai.common.llm.configuration.OllamaConfiguration;
import org.openhab.core.ai.common.llm.configuration.OpenAIConfiguration;
import org.openhab.core.ai.common.llm.configuration.VLLMConfiguration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the LLM Configuration Service.
 * 
 * This service manages configuration for all LLM providers, loading settings
 * from configuration files and environment variables.
 * 
 * @author openHAB AI Team
 * @since 4.0.0
 */
@Component(service = LLMConfigurationService.class)
@NonNullByDefault
public class LLMConfigurationServiceImpl implements LLMConfigurationService {

    private final Logger logger = LoggerFactory.getLogger(LLMConfigurationServiceImpl.class);

    private final Map<String, BaseLLMConfiguration> providerConfigs = new ConcurrentHashMap<>();

    private String primaryProvider = "ollama";
    private String fallbackProvider = "openai";
    private boolean hybridEnabled = true;
    private boolean loadBalancingEnabled = false;
    private double defaultTemperature = 0.3;
    private int defaultMaxTokens = 1000;
    private int defaultTimeoutMs = 30000;
    private int defaultRetryAttempts = 3;

    @Activate
    public void activate(Map<String, Object> config) {
        logger.debug("Activating LLM Configuration Service");
        loadProviderConfigurations(config);
    }

    @Modified
    public void modified(Map<String, Object> config) {
        logger.debug("Modifying LLM Configuration Service");
        loadProviderConfigurations(config);
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
     * Loads provider configurations from the configuration map.
     * 
     * @param config The configuration map
     */
    private void loadProviderConfigurations(Map<String, Object> config) {
        logger.debug("Loading LLM provider configurations");

        // Load global settings
        primaryProvider = getStringConfig(config, "ai.llm.primary.provider", "ollama");
        fallbackProvider = getStringConfig(config, "ai.llm.fallback.provider", "openai");
        hybridEnabled = getBooleanConfig(config, "ai.llm.hybrid.enabled", true);
        loadBalancingEnabled = getBooleanConfig(config, "ai.llm.load.balancing.enabled", false);
        defaultTemperature = getDoubleConfig(config, "ai.reasoning.temperature", 0.3);
        defaultMaxTokens = getIntConfig(config, "ai.reasoning.maxTokens", 1000);
        defaultTimeoutMs = getIntConfig(config, "ai.reasoning.timeout", 30000);
        defaultRetryAttempts = getIntConfig(config, "ai.reasoning.retryAttempts", 3);

        // Load OpenAI configuration
        providerConfigs.put("openai", buildOpenAIConfig(config));

        // Load Anthropic configuration
        providerConfigs.put("anthropic", buildAnthropicConfig(config));

        // Load Google GenAI configuration
        providerConfigs.put("google", buildGoogleConfig(config));

        // Load Azure OpenAI configuration
        providerConfigs.put("azure", buildAzureConfig(config));

        // Load Ollama configuration
        providerConfigs.put("ollama", buildOllamaConfig(config));

        // Load LocalAI configuration
        providerConfigs.put("localai", buildLocalAIConfig(config));

        // Load vLLM configuration
        providerConfigs.put("vllm", buildVLLMConfig(config));

        // Load LM Studio configuration
        providerConfigs.put("lmstudio", buildLMStudioConfig(config));

        logger.debug("Loaded {} provider configurations", providerConfigs.size());
    }

    /**
     * Builds OpenAI configuration from the configuration map.
     */
    private OpenAIConfiguration buildOpenAIConfig(Map<String, Object> config) {
        boolean enabled = getBooleanConfig(config, "ai.llm.openai.enabled", false);
        String modelName = getStringConfig(config, "ai.llm.openai.model", "gpt-4o-mini");
        double temperature = getDoubleConfig(config, "ai.llm.openai.temperature", 0.3);
        int maxTokens = getIntConfig(config, "ai.llm.openai.maxTokens", 4000);
        int timeoutMs = getIntConfig(config, "ai.llm.openai.timeout", 30000);
        int retryAttempts = getIntConfig(config, "ai.llm.openai.retryAttempts", 3);
        String systemPrompt = getNullableStringConfig(config, "ai.llm.openai.systemPrompt", null);
        String apiKey = getNullableStringConfig(config, "ai.llm.openai.apiKey", null);
        String baseUrl = getStringConfig(config, "ai.llm.openai.baseUrl", "https://api.openai.com/v1");
        double costPer1kTokens = getDoubleConfig(config, "ai.llm.openai.costPer1kTokens", 0.00015);

        return new OpenAIConfiguration(enabled, modelName, temperature, maxTokens, timeoutMs, retryAttempts,
                systemPrompt, apiKey, baseUrl, costPer1kTokens);
    }

    /**
     * Builds Anthropic configuration from the configuration map.
     */
    private AnthropicConfiguration buildAnthropicConfig(Map<String, Object> config) {
        boolean enabled = getBooleanConfig(config, "ai.llm.anthropic.enabled", false);
        String modelName = getStringConfig(config, "ai.llm.anthropic.model", "claude-3-5-sonnet-20241022");
        double temperature = getDoubleConfig(config, "ai.llm.anthropic.temperature", 0.3);
        int maxTokens = getIntConfig(config, "ai.llm.anthropic.maxTokens", 4000);
        int timeoutMs = getIntConfig(config, "ai.llm.anthropic.timeout", 30000);
        int retryAttempts = getIntConfig(config, "ai.llm.anthropic.retryAttempts", 3);
        String systemPrompt = getNullableStringConfig(config, "ai.llm.anthropic.systemPrompt", null);
        String apiKey = getNullableStringConfig(config, "ai.llm.anthropic.apiKey", null);
        String baseUrl = getStringConfig(config, "ai.llm.anthropic.baseUrl", "https://api.anthropic.com");
        double costPer1kTokens = getDoubleConfig(config, "ai.llm.anthropic.costPer1kTokens", 0.00015);

        return new AnthropicConfiguration(enabled, modelName, temperature, maxTokens, timeoutMs, retryAttempts,
                systemPrompt, apiKey, baseUrl, costPer1kTokens);
    }

    /**
     * Builds Google GenAI configuration from the configuration map.
     */
    private GoogleGenAIConfiguration buildGoogleConfig(Map<String, Object> config) {
        boolean enabled = getBooleanConfig(config, "ai.llm.google.enabled", false);
        String modelName = getStringConfig(config, "ai.llm.google.model", "gemini-1.5-pro");
        double temperature = getDoubleConfig(config, "ai.llm.google.temperature", 0.3);
        int maxTokens = getIntConfig(config, "ai.llm.google.maxTokens", 4000);
        int timeoutMs = getIntConfig(config, "ai.llm.google.timeout", 30000);
        int retryAttempts = getIntConfig(config, "ai.llm.google.retryAttempts", 3);
        String systemPrompt = getNullableStringConfig(config, "ai.llm.google.systemPrompt", null);
        String apiKey = getNullableStringConfig(config, "ai.llm.google.apiKey", null);
        String baseUrl = getStringConfig(config, "ai.llm.google.baseUrl", "https://generativelanguage.googleapis.com");
        double costPer1kTokens = getDoubleConfig(config, "ai.llm.google.costPer1kTokens", 0.000125);

        return new GoogleGenAIConfiguration(enabled, modelName, temperature, maxTokens, timeoutMs, retryAttempts,
                systemPrompt, apiKey, baseUrl, costPer1kTokens);
    }

    /**
     * Builds Azure OpenAI configuration from the configuration map.
     */
    private AzureOpenAIConfiguration buildAzureConfig(Map<String, Object> config) {
        boolean enabled = getBooleanConfig(config, "ai.llm.azure.enabled", false);
        String modelName = getStringConfig(config, "ai.llm.azure.model", "gpt-4o-mini");
        double temperature = getDoubleConfig(config, "ai.llm.azure.temperature", 0.3);
        int maxTokens = getIntConfig(config, "ai.llm.azure.maxTokens", 4000);
        int timeoutMs = getIntConfig(config, "ai.llm.azure.timeout", 30000);
        int retryAttempts = getIntConfig(config, "ai.llm.azure.retryAttempts", 3);
        String systemPrompt = getNullableStringConfig(config, "ai.llm.azure.systemPrompt", null);
        String apiKey = getNullableStringConfig(config, "ai.llm.azure.apiKey", null);
        String endpoint = getStringConfig(config, "ai.llm.azure.endpoint", "");
        String deploymentName = getStringConfig(config, "ai.llm.azure.deploymentName", "gpt-4o-mini");

        return new AzureOpenAIConfiguration(enabled, modelName, temperature, maxTokens, timeoutMs, retryAttempts,
                systemPrompt, apiKey, endpoint, deploymentName);
    }

    /**
     * Builds Ollama configuration from the configuration map.
     */
    private OllamaConfiguration buildOllamaConfig(Map<String, Object> config) {
        boolean enabled = getBooleanConfig(config, "ai.llm.ollama.enabled", true);
        String modelName = getStringConfig(config, "ai.llm.ollama.model", "llama3.1:8b");
        double temperature = getDoubleConfig(config, "ai.llm.ollama.temperature", 0.3);
        int maxTokens = getIntConfig(config, "ai.llm.ollama.maxTokens", 4000);
        int timeoutMs = getIntConfig(config, "ai.llm.ollama.timeout", 60000);
        int retryAttempts = getIntConfig(config, "ai.llm.ollama.retryAttempts", 2);
        String systemPrompt = getStringConfig(config, "ai.llm.ollama.systemPrompt", "You are a helpful AI assistant.");
        String baseUrl = getStringConfig(config, "ai.llm.ollama.baseUrl", "http://localhost:11434");
        int concurrentRequests = getIntConfig(config, "ai.llm.ollama.concurrentRequests", 3);

        return new OllamaConfiguration(enabled, modelName, temperature, maxTokens, timeoutMs, retryAttempts,
                systemPrompt, baseUrl, concurrentRequests);
    }

    /**
     * Builds LocalAI configuration from the configuration map.
     */
    private LocalAIConfiguration buildLocalAIConfig(Map<String, Object> config) {
        boolean enabled = getBooleanConfig(config, "ai.llm.localai.enabled", false);
        String modelName = getStringConfig(config, "ai.llm.localai.model", "llama3.1:8b");
        double temperature = getDoubleConfig(config, "ai.llm.localai.temperature", 0.3);
        int maxTokens = getIntConfig(config, "ai.llm.localai.maxTokens", 4000);
        int timeoutMs = getIntConfig(config, "ai.llm.localai.timeout", 60000);
        int retryAttempts = getIntConfig(config, "ai.llm.localai.retryAttempts", 2);
        String systemPrompt = getStringConfig(config, "ai.llm.localai.systemPrompt", "You are a helpful AI assistant.");
        String baseUrl = getStringConfig(config, "ai.llm.localai.baseUrl", "http://localhost:8080");

        return new LocalAIConfiguration(enabled, modelName, temperature, maxTokens, timeoutMs, retryAttempts,
                systemPrompt, baseUrl);
    }

    /**
     * Builds vLLM configuration from the configuration map.
     */
    private VLLMConfiguration buildVLLMConfig(Map<String, Object> config) {
        boolean enabled = getBooleanConfig(config, "ai.llm.vllm.enabled", false);
        String modelName = getStringConfig(config, "ai.llm.vllm.model", "llama3.1:8b");
        double temperature = getDoubleConfig(config, "ai.llm.vllm.temperature", 0.3);
        int maxTokens = getIntConfig(config, "ai.llm.vllm.maxTokens", 4000);
        int timeoutMs = getIntConfig(config, "ai.llm.vllm.timeout", 60000);
        int retryAttempts = getIntConfig(config, "ai.llm.vllm.retryAttempts", 2);
        String systemPrompt = getStringConfig(config, "ai.llm.vllm.systemPrompt", "You are a helpful AI assistant.");
        String baseUrl = getStringConfig(config, "ai.llm.vllm.baseUrl", "http://localhost:8000");

        return new VLLMConfiguration(enabled, modelName, temperature, maxTokens, timeoutMs, retryAttempts, systemPrompt,
                baseUrl);
    }

    /**
     * Builds LM Studio configuration from the configuration map.
     */
    private LMStudioConfiguration buildLMStudioConfig(Map<String, Object> config) {
        boolean enabled = getBooleanConfig(config, "ai.llm.lmstudio.enabled", false);
        String modelName = getStringConfig(config, "ai.llm.lmstudio.model", "llama3.1:8b");
        double temperature = getDoubleConfig(config, "ai.llm.lmstudio.temperature", 0.3);
        int maxTokens = getIntConfig(config, "ai.llm.lmstudio.maxTokens", 4000);
        int timeoutMs = getIntConfig(config, "ai.llm.lmstudio.timeout", 60000);
        int retryAttempts = getIntConfig(config, "ai.llm.lmstudio.retryAttempts", 2);
        String systemPrompt = getStringConfig(config, "ai.llm.lmstudio.systemPrompt",
                "You are a helpful AI assistant.");
        String baseUrl = getStringConfig(config, "ai.llm.lmstudio.baseUrl", "http://localhost:1234");

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
