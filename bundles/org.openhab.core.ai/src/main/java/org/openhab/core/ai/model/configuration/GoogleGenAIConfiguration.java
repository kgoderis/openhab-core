package org.openhab.core.ai.model.configuration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Configuration for Google GenAI provider.
 * 
 * @author openHAB AI Team
 * @since 4.0.0
 */
@NonNullByDefault
public class GoogleGenAIConfiguration extends BaseProviderConfiguration {

    private final @Nullable String apiKey;
    private final String baseUrl;
    private final double costPer1kTokens;

    public GoogleGenAIConfiguration(boolean enabled, String modelName, double temperature, int maxTokens, int timeoutMs,
            int retryAttempts, @Nullable String systemPrompt, @Nullable String apiKey, String baseUrl,
            double costPer1kTokens) {
        super(enabled, modelName, temperature, maxTokens, timeoutMs, retryAttempts, systemPrompt);
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.costPer1kTokens = costPer1kTokens;
    }

    public @Nullable String getApiKey() {
        return apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public double getCostPer1kTokens() {
        return costPer1kTokens;
    }
}
