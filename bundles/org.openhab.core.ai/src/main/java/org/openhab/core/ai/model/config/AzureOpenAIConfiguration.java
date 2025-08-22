package org.openhab.core.ai.model.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Configuration for Azure OpenAI provider.
 * 
 * @author openHAB AI Team
 * @since 4.0.0
 */
@NonNullByDefault
public class AzureOpenAIConfiguration extends BaseProviderConfiguration {

    private final @Nullable String apiKey;
    private final String endpoint;
    private final String deploymentName;
    private final double costPer1kTokens;

    public AzureOpenAIConfiguration(boolean enabled, String modelName, double temperature, int maxTokens, int timeoutMs,
            int retryAttempts, @Nullable String systemPrompt, @Nullable String apiKey, String endpoint,
            String deploymentName, double costPer1kTokens) {
        super(enabled, modelName, temperature, maxTokens, timeoutMs, retryAttempts, systemPrompt);
        this.apiKey = apiKey;
        this.endpoint = endpoint;
        this.deploymentName = deploymentName;
        this.costPer1kTokens = costPer1kTokens;
    }

    public @Nullable String getApiKey() {
        return apiKey;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getDeploymentName() {
        return deploymentName;
    }

    public double getCostPer1kTokens() {
        return costPer1kTokens;
    }
}
