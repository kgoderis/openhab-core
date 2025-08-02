package org.openhab.core.ai.common.llm.configuration;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Configuration for Ollama provider.
 * 
 * @author openHAB AI Team
 * @since 4.0.0
 */
@NonNullByDefault
public class OllamaConfiguration extends BaseLLMConfiguration {

    private final String baseUrl;
    private final int concurrentRequests;

    public OllamaConfiguration(boolean enabled, String modelName, double temperature, int maxTokens, int timeoutMs,
            int retryAttempts, String systemPrompt, String baseUrl, int concurrentRequests) {
        super(enabled, modelName, temperature, maxTokens, timeoutMs, retryAttempts, systemPrompt);
        this.baseUrl = baseUrl;
        this.concurrentRequests = concurrentRequests;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public int getConcurrentRequests() {
        return concurrentRequests;
    }
}
