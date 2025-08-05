package org.openhab.core.ai.model.clients;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.ActionRegistry;
import org.openhab.core.ai.api.model.ModelClient;
import org.openhab.core.ai.api.model.ModelConfigurationService;
import org.openhab.core.ai.api.model.ModelProviderType;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating and managing LLM provider clients.
 * 
 * This factory provides a centralized way to create and access LLM clients
 * for different providers (OpenAI, Anthropic, Google, Ollama, etc.).
 * 
 * @author openHAB AI Team
 * @since 4.0.0
 */
@Component(service = LLMProviderFactory.class)
@NonNullByDefault
public class LLMProviderFactory {

    private final Logger logger = LoggerFactory.getLogger(LLMProviderFactory.class);

    private final Map<String, ModelClient> providers = new ConcurrentHashMap<>();
    private final Map<ModelProviderType, ModelClient> typedProviders = new ConcurrentHashMap<>();

    @Reference
    private @Nullable ModelConfigurationService configService;

    @Reference
    private @Nullable ActionRegistry actionRegistry;

    @Activate
    public void activate(Map<String, Object> config) {
        logger.debug("Activating LLM Provider Factory");
        initializeProviders(config);
    }

    @Modified
    public void modified(Map<String, Object> config) {
        logger.debug("Modifying LLM Provider Factory configuration");
        clearProviders();
        initializeProviders(config);
    }

    @Deactivate
    public void deactivate() {
        logger.debug("Deactivating LLM Provider Factory");
        clearProviders();
    }

    /**
     * Gets an LLM client for the specified provider type.
     * 
     * @param providerType The provider type
     * @return The LLM client, or null if not available
     */
    public @Nullable ModelClient getProvider(ModelProviderType providerType) {
        return typedProviders.get(providerType);
    }

    /**
     * Gets an LLM client for the specified provider type string.
     * 
     * @param providerType The provider type string
     * @return The LLM client, or null if not available
     */
    public @Nullable ModelClient getProvider(String providerType) {
        return providers.get(providerType.toLowerCase());
    }

    /**
     * Gets an LLM client for the specified provider type, creating it if necessary.
     * 
     * @param providerType The provider type
     * @return The LLM client
     * @throws IllegalArgumentException if the provider type is not supported
     */
    public ModelClient getOrCreateProvider(ModelProviderType providerType) {
        ModelClient client = typedProviders.get(providerType);
        if (client == null) {
            client = createProvider(providerType);
            typedProviders.put(providerType, client);
        }
        return client;
    }

    /**
     * Gets an LLM client for the specified provider type string, creating it if necessary.
     * 
     * @param providerType The provider type string
     * @return The LLM client
     * @throws IllegalArgumentException if the provider type is not supported
     */
    public ModelClient getOrCreateProvider(String providerType) {
        ModelClient client = providers.get(providerType.toLowerCase());
        if (client == null) {
            client = createProvider(providerType);
            providers.put(providerType.toLowerCase(), client);
        }
        return client;
    }

    /**
     * Registers an LLM client for a specific provider type.
     * 
     * @param providerType The provider type
     * @param client The LLM client to register
     */
    public void registerProvider(ModelProviderType providerType, ModelClient client) {
        typedProviders.put(providerType, client);
        providers.put(providerType.name().toLowerCase(), client);
        logger.debug("Registered LLM provider: {}", providerType);
    }

    /**
     * Unregisters an LLM client for a specific provider type.
     * 
     * @param providerType The provider type
     */
    public void unregisterProvider(ModelProviderType providerType) {
        typedProviders.remove(providerType);
        providers.remove(providerType.name().toLowerCase());
        logger.debug("Unregistered LLM provider: {}", providerType);
    }

    /**
     * Gets all registered providers.
     * 
     * @return Map of provider types to clients
     */
    public Map<ModelProviderType, ModelClient> getAllProviders() {
        return new ConcurrentHashMap<>(typedProviders);
    }

    /**
     * Checks if a provider is available.
     * 
     * @param providerType The provider type
     * @return true if the provider is available
     */
    public boolean isProviderAvailable(ModelProviderType providerType) {
        return typedProviders.containsKey(providerType);
    }

    /**
     * Gets the primary provider based on configuration.
     * 
     * @return The primary provider, or null if not configured
     */
    public @Nullable ModelClient getPrimaryProvider() {
        if (configService == null) {
            return null;
        }

        String primaryProvider = configService.getPrimaryProvider();
        if (primaryProvider != null) {
            return getOrCreateProvider(primaryProvider);
        }

        return null;
    }

    /**
     * Gets the fallback provider based on configuration.
     * 
     * @return The fallback provider, or null if not configured
     */
    public @Nullable ModelClient getFallbackProvider() {
        if (configService == null) {
            return null;
        }

        String fallbackProvider = configService.getFallbackProvider();
        if (fallbackProvider != null) {
            return getOrCreateProvider(fallbackProvider);
        }

        return null;
    }

    /**
     * Creates an LLM client for the specified provider type.
     * 
     * @param providerType The provider type
     * @return The created LLM client
     * @throws IllegalArgumentException if the provider type is not supported
     */
    private ModelClient createProvider(ModelProviderType providerType) {
        if (configService == null) {
            throw new IllegalStateException("Configuration service not available");
        }

        switch (providerType) {
            case OPENAI:
                return createOpenAIClient(configService);
            case ANTHROPIC:
                return createAnthropicClient(configService);
            case GOOGLE:
                return createGoogleGenAIClient(configService);
            case AZURE:
                return createAzureOpenAIClient(configService);
            case OLLAMA:
                return createOllamaClient(configService);
            case LOCALAI:
                return createLocalAIClient(configService);
            case VLLM:
                return createVModelClient(configService);
            case LMSTUDIO:
                return createLMStudioClient(configService);
            default:
                throw new IllegalArgumentException("Unsupported LLM provider: " + providerType);
        }
    }

    /**
     * Creates an LLM client for the specified provider type string.
     * 
     * @param providerType The provider type string
     * @return The created LLM client
     * @throws IllegalArgumentException if the provider type is not supported
     */
    private ModelClient createProvider(String providerType) {
        try {
            ModelProviderType type = ModelProviderType.valueOf(providerType.toUpperCase());
            return createProvider(type);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unsupported LLM provider: " + providerType);
        }
    }

    /**
     * Creates an OpenAI client.
     */
    private ModelClient createOpenAIClient(@Nullable ModelConfigurationService config) {
        if (config != null) {
            var openAIConfig = config.getOpenAIConfig();
            if (openAIConfig != null && openAIConfig.isEnabled()) {
                logger.debug("Creating OpenAI client with configuration");
                return new OpenAIClientImpl(openAIConfig, actionRegistry);
            }
        }
        logger.debug("Creating OpenAI client (stub implementation)");
        return new StubModelClient(ModelProviderType.OPENAI, "gpt-4o-mini");
    }

    /**
     * Creates an Anthropic client.
     */
    private ModelClient createAnthropicClient(@Nullable ModelConfigurationService config) {
        if (config != null) {
            var anthropicConfig = config.getAnthropicConfig();
            if (anthropicConfig != null && anthropicConfig.isEnabled()) {
                logger.debug("Creating Anthropic client with configuration");
                return new AnthropicClientImpl(anthropicConfig, actionRegistry);
            }
        }
        logger.debug("Creating Anthropic client (stub implementation)");
        return new StubModelClient(ModelProviderType.ANTHROPIC, "claude-3-5-sonnet");
    }

    /**
     * Creates a Google GenAI client.
     */
    private ModelClient createGoogleGenAIClient(@Nullable ModelConfigurationService config) {
        if (config != null) {
            var googleConfig = config.getGoogleConfig();
            if (googleConfig != null && googleConfig.isEnabled()) {
                logger.debug("Creating Google GenAI client with configuration");
                return new GoogleGenAIClientImpl(googleConfig, actionRegistry);
            }
        }
        logger.debug("Creating Google GenAI client (stub implementation)");
        return new StubModelClient(ModelProviderType.GOOGLE, "gemini-1.5-pro");
    }

    /**
     * Creates an Azure OpenAI client.
     */
    private ModelClient createAzureOpenAIClient(@Nullable ModelConfigurationService config) {
        if (config != null) {
            var azureConfig = config.getAzureConfig();
            if (azureConfig != null && azureConfig.isEnabled()) {
                logger.debug("Creating Azure OpenAI client with configuration");
                return new AzureOpenAIClientImpl(azureConfig, actionRegistry);
            }
        }
        logger.debug("Creating Azure OpenAI client (stub implementation)");
        return new StubModelClient(ModelProviderType.AZURE, "gpt-4o-mini");
    }

    /**
     * Creates an Ollama client.
     */
    private ModelClient createOllamaClient(@Nullable ModelConfigurationService config) {
        if (config != null) {
            var ollamaConfig = config.getOllamaConfig();
            if (ollamaConfig != null && ollamaConfig.isEnabled()) {
                logger.debug("Creating Ollama client with configuration");
                return new OllamaClientImpl(ollamaConfig, actionRegistry);
            }
        }
        logger.debug("Creating Ollama client (stub implementation)");
        return new StubModelClient(ModelProviderType.OLLAMA, "llama3.1:8b");
    }

    /**
     * Creates a LocalAI client.
     */
    private ModelClient createLocalAIClient(@Nullable ModelConfigurationService config) {
        if (config != null) {
            var localAIConfig = config.getLocalAIConfig();
            if (localAIConfig != null && localAIConfig.isEnabled()) {
                logger.debug("Creating LocalAI client with configuration");
                return new LocalAIClientImpl(localAIConfig, actionRegistry);
            }
        }
        logger.debug("Creating LocalAI client (stub implementation)");
        return new StubModelClient(ModelProviderType.LOCALAI, "llama3.1:8b");
    }

    /**
     * Creates a vLLM client.
     */
    private ModelClient createVModelClient(@Nullable ModelConfigurationService config) {
        if (config != null) {
            var vllmConfig = config.getVLLMConfig();
            if (vllmConfig != null && vllmConfig.isEnabled()) {
                logger.debug("Creating vLLM client with configuration");
                return new VModelClientImpl(vllmConfig, actionRegistry);
            }
        }
        logger.debug("Creating vLLM client (stub implementation)");
        return new StubModelClient(ModelProviderType.VLLM, "llama3.1:8b");
    }

    /**
     * Creates an LM Studio client.
     */
    private ModelClient createLMStudioClient(@Nullable ModelConfigurationService config) {
        if (config != null) {
            var lmStudioConfig = config.getLMStudioConfig();
            if (lmStudioConfig != null && lmStudioConfig.isEnabled()) {
                logger.debug("Creating LM Studio client with configuration");
                return new LMStudioClientImpl(lmStudioConfig, actionRegistry);
            }
        }
        logger.debug("Creating LM Studio client (stub implementation)");
        return new StubModelClient(ModelProviderType.LMSTUDIO, "llama3.1:8b");
    }

    /**
     * Initializes providers based on configuration.
     */
    private void initializeProviders(Map<String, Object> config) {
        logger.debug("Initializing LLM providers from configuration");
        // TODO: Initialize providers based on configuration
    }

    /**
     * Clears all registered providers.
     */
    private void clearProviders() {
        providers.clear();
        typedProviders.clear();
        logger.debug("Cleared all LLM providers");
    }
}
