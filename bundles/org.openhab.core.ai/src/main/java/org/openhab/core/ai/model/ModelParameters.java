package org.openhab.core.ai.model;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.builder.ModelParametersBuilder;

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

    public ModelParameters(ModelParametersBuilder builder) {
        this.temperature = builder.getTemperature();
        this.maxTokens = builder.getMaxTokens();
        this.model = builder.getModel();
        this.additionalParams = builder.getAdditionalParams();
        this.stream = builder.isStream();
        this.systemPrompt = builder.getSystemPrompt();
        this.timeoutMs = builder.getTimeoutMs();
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
