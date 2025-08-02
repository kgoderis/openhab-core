package org.openhab.core.ai.common.llm;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.api.llm.LLMClient;
import org.openhab.core.ai.common.api.llm.LLMConfigurationService;
import org.openhab.core.ai.common.api.llm.LLMProviderType;
import org.openhab.core.ai.common.llm.providers.StubLLMClient;
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

    private final Map<String, LLMClient> providers = new ConcurrentHashMap<>();
    private final Map<LLMProviderType, LLMClient> typedProviders = new ConcurrentHashMap<>();

    @Reference
    private @Nullable LLMConfigurationService configService;

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
    public @Nullable LLMClient getProvider(LLMProviderType providerType) {
        return typedProviders.get(providerType);
    }

    /**
     * Gets an LLM client for the specified provider type string.
     * 
     * @param providerType The provider type string
     * @return The LLM client, or null if not available
     */
    public @Nullable LLMClient getProvider(String providerType) {
        return providers.get(providerType.toLowerCase());
    }

    /**
     * Gets an LLM client for the specified provider type, creating it if necessary.
     * 
     * @param providerType The provider type
     * @return The LLM client
     * @throws IllegalArgumentException if the provider type is not supported
     */
    public LLMClient getOrCreateProvider(LLMProviderType providerType) {
        LLMClient client = typedProviders.get(providerType);
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
    public LLMClient getOrCreateProvider(String providerType) {
        LLMClient client = providers.get(providerType.toLowerCase());
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
    public void registerProvider(LLMProviderType providerType, LLMClient client) {
        typedProviders.put(providerType, client);
        providers.put(providerType.name().toLowerCase(), client);
        logger.debug("Registered LLM provider: {}", providerType);
    }

    /**
     * Unregisters an LLM client for a specific provider type.
     * 
     * @param providerType The provider type
     */
    public void unregisterProvider(LLMProviderType providerType) {
        typedProviders.remove(providerType);
        providers.remove(providerType.name().toLowerCase());
        logger.debug("Unregistered LLM provider: {}", providerType);
    }

    /**
     * Gets all registered providers.
     * 
     * @return Map of provider types to clients
     */
    public Map<LLMProviderType, LLMClient> getAllProviders() {
        return new ConcurrentHashMap<>(typedProviders);
    }

    /**
     * Checks if a provider is available.
     * 
     * @param providerType The provider type
     * @return true if the provider is available
     */
    public boolean isProviderAvailable(LLMProviderType providerType) {
        return typedProviders.containsKey(providerType);
    }

    /**
     * Gets the primary provider based on configuration.
     * 
     * @return The primary provider, or null if not configured
     */
    public @Nullable LLMClient getPrimaryProvider() {
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
    public @Nullable LLMClient getFallbackProvider() {
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
    private LLMClient createProvider(LLMProviderType providerType) {
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
                return createVLLMClient(configService);
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
    private LLMClient createProvider(String providerType) {
        try {
            LLMProviderType type = LLMProviderType.valueOf(providerType.toUpperCase());
            return createProvider(type);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unsupported LLM provider: " + providerType);
        }
    }

    /**
     * Creates an OpenAI client.
     */
    private LLMClient createOpenAIClient(LLMConfigurationService config) {
        logger.debug("Creating OpenAI client (stub implementation)");
        return new StubLLMClient(LLMProviderType.OPENAI, "gpt-4o-mini");
    }

    /**
     * Creates an Anthropic client.
     */
    private LLMClient createAnthropicClient(LLMConfigurationService config) {
        logger.debug("Creating Anthropic client (stub implementation)");
        return new StubLLMClient(LLMProviderType.ANTHROPIC, "claude-3-5-sonnet");
    }

    /**
     * Creates a Google GenAI client.
     */
    private LLMClient createGoogleGenAIClient(LLMConfigurationService config) {
        logger.debug("Creating Google GenAI client (stub implementation)");
        return new StubLLMClient(LLMProviderType.GOOGLE, "gemini-1.5-pro");
    }

    /**
     * Creates an Azure OpenAI client.
     */
    private LLMClient createAzureOpenAIClient(LLMConfigurationService config) {
        logger.debug("Creating Azure OpenAI client (stub implementation)");
        return new StubLLMClient(LLMProviderType.AZURE, "gpt-4o-mini");
    }

    /**
     * Creates an Ollama client.
     */
    private LLMClient createOllamaClient(LLMConfigurationService config) {
        logger.debug("Creating Ollama client (stub implementation)");
        return new StubLLMClient(LLMProviderType.OLLAMA, "llama3.1:8b");
    }

    /**
     * Creates a LocalAI client.
     */
    private LLMClient createLocalAIClient(LLMConfigurationService config) {
        logger.debug("Creating LocalAI client (stub implementation)");
        return new StubLLMClient(LLMProviderType.LOCALAI, "llama3.1:8b");
    }

    /**
     * Creates a vLLM client.
     */
    private LLMClient createVLLMClient(LLMConfigurationService config) {
        logger.debug("Creating vLLM client (stub implementation)");
        return new StubLLMClient(LLMProviderType.VLLM, "llama3.1:8b");
    }

    /**
     * Creates an LM Studio client.
     */
    private LLMClient createLMStudioClient(LLMConfigurationService config) {
        logger.debug("Creating LM Studio client (stub implementation)");
        return new StubLLMClient(LLMProviderType.LMSTUDIO, "llama3.1:8b");
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
