package org.openhab.core.ai.model.configuration;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Configuration for LocalAI provider.
 * 
 * @author openHAB AI Team
 * @since 4.0.0
 */
@NonNullByDefault
public class LocalAIConfiguration extends BaseProviderConfiguration {

    private final String baseUrl;

    public LocalAIConfiguration(boolean enabled, String modelName, double temperature, int maxTokens, int timeoutMs,
            int retryAttempts, String systemPrompt, String baseUrl) {
        super(enabled, modelName, temperature, maxTokens, timeoutMs, retryAttempts, systemPrompt);
        this.baseUrl = baseUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}
