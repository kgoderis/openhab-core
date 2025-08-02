package org.openhab.core.ai.common.llm.configuration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Configuration for Azure OpenAI provider.
 * 
 * @author openHAB AI Team
 * @since 4.0.0
 */
@NonNullByDefault
public class AzureOpenAIConfiguration extends BaseLLMConfiguration {

    private final @Nullable String apiKey;
    private final String endpoint;
    private final String deploymentName;

    public AzureOpenAIConfiguration(boolean enabled, String modelName, double temperature, int maxTokens, int timeoutMs,
            int retryAttempts, @Nullable String systemPrompt, @Nullable String apiKey, String endpoint,
            String deploymentName) {
        super(enabled, modelName, temperature, maxTokens, timeoutMs, retryAttempts, systemPrompt);
        this.apiKey = apiKey;
        this.endpoint = endpoint;
        this.deploymentName = deploymentName;
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
}
