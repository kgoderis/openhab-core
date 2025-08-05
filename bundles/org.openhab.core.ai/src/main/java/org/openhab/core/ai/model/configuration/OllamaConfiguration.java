package org.openhab.core.ai.model.configuration;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Configuration for Ollama provider.
 * 
 * @author openHAB AI Team
 * @since 4.0.0
 */
@NonNullByDefault
public class OllamaConfiguration extends BaseProviderConfiguration {

    private final String baseUrl;
    private final int concurrentRequests;
    private final boolean autoStartOllama;
    private final boolean autoInstallOllama;

    public OllamaConfiguration(boolean enabled, String modelName, double temperature, int maxTokens, int timeoutMs,
            int retryAttempts, String systemPrompt, String baseUrl, int concurrentRequests, boolean autoStartOllama,
            boolean autoInstallOllama) {
        super(enabled, modelName, temperature, maxTokens, timeoutMs, retryAttempts, systemPrompt);
        this.baseUrl = baseUrl;
        this.concurrentRequests = concurrentRequests;
        this.autoStartOllama = autoStartOllama;
        this.autoInstallOllama = autoInstallOllama;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public int getConcurrentRequests() {
        return concurrentRequests;
    }

    public boolean isAutoStartOllama() {
        return autoStartOllama;
    }

    public boolean isAutoInstallOllama() {
        return autoInstallOllama;
    }
}
