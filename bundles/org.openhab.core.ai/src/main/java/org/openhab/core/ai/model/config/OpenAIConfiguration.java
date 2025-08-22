package org.openhab.core.ai.model.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Configuration for OpenAI provider.
 * 
 * @author openHAB AI Team
 * @since 4.0.0
 */
@NonNullByDefault
public class OpenAIConfiguration extends BaseProviderConfiguration {

    private final @Nullable String apiKey;
    private final String baseUrl;
    private final double costPer1kTokens;

    public OpenAIConfiguration(boolean enabled, String modelName, double temperature, int maxTokens, int timeoutMs,
            int retryAttempts, @Nullable String systemPrompt, @Nullable String apiKey, String baseUrl,
            double costPer1kTokens) {
        super(enabled, modelName, temperature, maxTokens, timeoutMs, retryAttempts, systemPrompt);
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.costPer1kTokens = costPer1kTokens;
    }

    /**
     * Gets the API key.
     * 
     * @return The API key, or null if not configured
     */
    public @Nullable String getApiKey() {
        return apiKey;
    }

    /**
     * Gets the base URL.
     * 
     * @return The base URL
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Gets the cost per 1K tokens.
     * 
     * @return Cost per 1K tokens
     */
    public double getCostPer1kTokens() {
        return costPer1kTokens;
    }
}
