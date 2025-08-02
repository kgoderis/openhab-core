package org.openhab.core.ai.common.llm;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.llm.configuration.BaseLLMConfiguration;

/**
 * Configuration for LM Studio provider.
 * 
 * @author openHAB AI Team
 * @since 4.0.0
 */
@NonNullByDefault
public class LMStudioConfiguration extends BaseLLMConfiguration {

    private final String baseUrl;

    public LMStudioConfiguration(boolean enabled, String modelName, double temperature, int maxTokens, int timeoutMs,
            int retryAttempts, String systemPrompt, String baseUrl) {
        super(enabled, modelName, temperature, maxTokens, timeoutMs, retryAttempts, systemPrompt);
        this.baseUrl = baseUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}
