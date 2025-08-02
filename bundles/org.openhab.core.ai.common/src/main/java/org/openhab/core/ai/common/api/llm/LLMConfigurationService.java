package org.openhab.core.ai.common.api.llm;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.llm.LMStudioConfiguration;
import org.openhab.core.ai.common.llm.configuration.AnthropicConfiguration;
import org.openhab.core.ai.common.llm.configuration.AzureOpenAIConfiguration;
import org.openhab.core.ai.common.llm.configuration.GoogleGenAIConfiguration;
import org.openhab.core.ai.common.llm.configuration.LocalAIConfiguration;
import org.openhab.core.ai.common.llm.configuration.OllamaConfiguration;
import org.openhab.core.ai.common.llm.configuration.OpenAIConfiguration;
import org.openhab.core.ai.common.llm.configuration.VLLMConfiguration;

/**
 * Service for managing LLM configuration.
 * 
 * @author openHAB AI Team
 * @since 4.0.0
 */
@NonNullByDefault
public interface LLMConfigurationService {

    /**
     * Gets the primary LLM provider.
     * 
     * @return The primary provider name, or null if not configured
     */
    @Nullable
    String getPrimaryProvider();

    /**
     * Gets the fallback LLM provider.
     * 
     * @return The fallback provider name, or null if not configured
     */
    @Nullable
    String getFallbackProvider();

    /**
     * Gets the OpenAI configuration.
     * 
     * @return OpenAI configuration, or null if not configured
     */
    @Nullable
    OpenAIConfiguration getOpenAIConfig();

    /**
     * Gets the Anthropic configuration.
     * 
     * @return Anthropic configuration, or null if not configured
     */
    @Nullable
    AnthropicConfiguration getAnthropicConfig();

    /**
     * Gets the Google GenAI configuration.
     * 
     * @return Google GenAI configuration, or null if not configured
     */
    @Nullable
    GoogleGenAIConfiguration getGoogleConfig();

    /**
     * Gets the Azure OpenAI configuration.
     * 
     * @return Azure OpenAI configuration, or null if not configured
     */
    @Nullable
    AzureOpenAIConfiguration getAzureConfig();

    /**
     * Gets the Ollama configuration.
     * 
     * @return Ollama configuration, or null if not configured
     */
    @Nullable
    OllamaConfiguration getOllamaConfig();

    /**
     * Gets the LocalAI configuration.
     * 
     * @return LocalAI configuration, or null if not configured
     */
    @Nullable
    LocalAIConfiguration getLocalAIConfig();

    /**
     * Gets the vLLM configuration.
     * 
     * @return vLLM configuration, or null if not configured
     */
    @Nullable
    VLLMConfiguration getVLLMConfig();

    /**
     * Gets the LM Studio configuration.
     * 
     * @return LM Studio configuration, or null if not configured
     */
    @Nullable
    LMStudioConfiguration getLMStudioConfig();

    /**
     * Checks if hybrid mode is enabled.
     * 
     * @return true if hybrid mode is enabled
     */
    boolean isHybridEnabled();

    /**
     * Checks if load balancing is enabled.
     * 
     * @return true if load balancing is enabled
     */
    boolean isLoadBalancingEnabled();

    /**
     * Gets the default temperature for reasoning.
     * 
     * @return Default temperature value
     */
    double getDefaultTemperature();

    /**
     * Gets the default maximum tokens for reasoning.
     * 
     * @return Default maximum tokens
     */
    int getDefaultMaxTokens();

    /**
     * Gets the default timeout for LLM requests.
     * 
     * @return Default timeout in milliseconds
     */
    int getDefaultTimeoutMs();

    /**
     * Gets the default number of retry attempts.
     * 
     * @return Default retry attempts
     */
    int getDefaultRetryAttempts();
}
