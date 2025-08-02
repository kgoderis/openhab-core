package org.openhab.core.ai.common.llm;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Configuration parameters for LLM requests.
 * 
 * @author openHAB AI Team
 * @since 4.0.0
 */
@NonNullByDefault
public class LLMParameters {

    private final double temperature;
    private final int maxTokens;
    private final @Nullable String model;
    private final Map<String, Object> additionalParams;
    private final boolean stream;
    private final @Nullable String systemPrompt;
    private final int timeoutMs;

    private LLMParameters(Builder builder) {
        this.temperature = builder.temperature;
        this.maxTokens = builder.maxTokens;
        this.model = builder.model;
        this.additionalParams = builder.additionalParams;
        this.stream = builder.stream;
        this.systemPrompt = builder.systemPrompt;
        this.timeoutMs = builder.timeoutMs;
    }

    /**
     * Gets the temperature parameter for controlling randomness.
     * 
     * @return Temperature value (0.0 to 2.0)
     */
    public double getTemperature() {
        return temperature;
    }

    /**
     * Gets the maximum number of tokens to generate.
     * 
     * @return Maximum tokens
     */
    public int getMaxTokens() {
        return maxTokens;
    }

    /**
     * Gets the model to use for the request.
     * 
     * @return Model name, or null to use default
     */
    public @Nullable String getModel() {
        return model;
    }

    /**
     * Gets additional provider-specific parameters.
     * 
     * @return Additional parameters map
     */
    public Map<String, Object> getAdditionalParams() {
        return additionalParams;
    }

    /**
     * Checks if streaming is enabled.
     * 
     * @return true if streaming is enabled
     */
    public boolean isStream() {
        return stream;
    }

    /**
     * Gets the system prompt to use.
     * 
     * @return System prompt, or null if not specified
     */
    public @Nullable String getSystemPrompt() {
        return systemPrompt;
    }

    /**
     * Gets the timeout for the request in milliseconds.
     * 
     * @return Timeout in milliseconds
     */
    public int getTimeoutMs() {
        return timeoutMs;
    }

    /**
     * Gets a specific additional parameter by key.
     * 
     * @param key The parameter key
     * @return The parameter value, or null if not found
     */
    @SuppressWarnings("unchecked")
    public @Nullable <T> T getAdditionalParam(String key) {
        return (T) additionalParams.get(key);
    }

    public static class Builder {
        private double temperature = 0.7;
        private int maxTokens = 1000;
        private @Nullable String model;
        private Map<String, Object> additionalParams = Map.of();
        private boolean stream = false;
        private @Nullable String systemPrompt;
        private int timeoutMs = 30000;

        public Builder temperature(double temperature) {
            this.temperature = temperature;
            return this;
        }

        public Builder maxTokens(int maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }

        public Builder model(@Nullable String model) {
            this.model = model;
            return this;
        }

        public Builder additionalParams(Map<String, Object> additionalParams) {
            this.additionalParams = additionalParams;
            return this;
        }

        public Builder stream(boolean stream) {
            this.stream = stream;
            return this;
        }

        public Builder systemPrompt(@Nullable String systemPrompt) {
            this.systemPrompt = systemPrompt;
            return this;
        }

        public Builder timeoutMs(int timeoutMs) {
            this.timeoutMs = timeoutMs;
            return this;
        }

        public LLMParameters build() {
            return new LLMParameters(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
