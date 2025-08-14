package org.openhab.core.ai.model;

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
public class ModelParameters {

    private final double temperature;
    private final int maxTokens;
    private final @Nullable String model;
    private final Map<String, Object> additionalParams;
    private final boolean stream;
    private final @Nullable String systemPrompt;
    private final int timeoutMs;

    /* package */ ModelParameters(ModelParametersBuilder builder) {
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

    public static ModelParametersBuilder builder() {
        return new ModelParametersBuilder();
    }
}
