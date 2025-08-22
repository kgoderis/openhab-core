package org.openhab.core.ai.model.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Base configuration class for LLM providers.
 * 
 * @author openHAB AI Team
 * @since 4.0.0
 */
@NonNullByDefault
public abstract class BaseProviderConfiguration {

    protected final boolean enabled;
    protected final String modelName;
    protected final double temperature;
    protected final int maxTokens;
    protected final int timeoutMs;
    protected final int retryAttempts;
    protected final @Nullable String systemPrompt;

    protected BaseProviderConfiguration(boolean enabled, String modelName, double temperature, int maxTokens,
            int timeoutMs, int retryAttempts, @Nullable String systemPrompt) {
        this.enabled = enabled;
        this.modelName = modelName;
        this.temperature = temperature;
        this.maxTokens = maxTokens;
        this.timeoutMs = timeoutMs;
        this.retryAttempts = retryAttempts;
        this.systemPrompt = systemPrompt;
    }

    /**
     * Checks if this provider is enabled.
     * 
     * @return true if enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Gets the model name.
     * 
     * @return The model name
     */
    public String getModelName() {
        return modelName;
    }

    /**
     * Gets the temperature parameter.
     * 
     * @return Temperature value
     */
    public double getTemperature() {
        return temperature;
    }

    /**
     * Gets the maximum tokens.
     * 
     * @return Maximum tokens
     */
    public int getMaxTokens() {
        return maxTokens;
    }

    /**
     * Gets the timeout in milliseconds.
     * 
     * @return Timeout in ms
     */
    public int getTimeoutMs() {
        return timeoutMs;
    }

    /**
     * Gets the number of retry attempts.
     * 
     * @return Retry attempts
     */
    public int getRetryAttempts() {
        return retryAttempts;
    }

    /**
     * Gets the system prompt.
     * 
     * @return System prompt, or null if not specified
     */
    public @Nullable String getSystemPrompt() {
        return systemPrompt;
    }
}
