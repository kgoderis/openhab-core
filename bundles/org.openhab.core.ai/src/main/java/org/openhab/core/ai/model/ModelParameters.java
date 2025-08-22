package org.openhab.core.ai.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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

    /**
     * Create a new ModelParameters.
     * 
     * @param builder the builder containing all parameters
     */
    private ModelParameters(Builder builder) {
        this.temperature = builder.temperature;
        this.maxTokens = builder.maxTokens;
        this.model = builder.model;
        this.additionalParams = Map.copyOf(builder.additionalParams);
        this.stream = builder.stream;
        this.systemPrompt = builder.systemPrompt;
        this.timeoutMs = builder.timeoutMs;
    }

    /**
     * Create a new ModelParametersBuilder instance.
     * 
     * @return a new ModelParametersBuilder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create a builder from this instance for modification.
     * 
     * @return a new builder with current values
     */
    public Builder toBuilder() {
        return new Builder(this);
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

    /**
     * Builder for creating ModelParameters objects.
     * 
     * <p>
     * Provides a fluent API with validation for constructing ModelParameters
     * instances.
     * </p>
     * 
     * @author Karel Goderis - Initial Contribution
     * @since 1.0.0
     */
    public static final class Builder {
        private double temperature = 0.7;
        private int maxTokens = 2048;
        private @Nullable String model;
        private Map<String, Object> additionalParams = Map.of();
        private boolean stream = false;
        private @Nullable String systemPrompt;
        private int timeoutMs = 30000;

        public Builder() {
        }

        public Builder(ModelParameters source) {
            this.temperature = source.temperature;
            this.maxTokens = source.maxTokens;
            this.model = source.model;
            this.additionalParams = new HashMap<>(source.additionalParams);
            this.stream = source.stream;
            this.systemPrompt = source.systemPrompt;
            this.timeoutMs = source.timeoutMs;
        }

        /**
         * Set the temperature.
         * 
         * @param temperature the temperature
         * @return this builder
         */
        public Builder withTemperature(double temperature) {
            this.temperature = temperature;
            return this;
        }

        /**
         * Set the max tokens.
         * 
         * @param maxTokens the max tokens
         * @return this builder
         */
        public Builder withMaxTokens(int maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }

        /**
         * Set the model.
         * 
         * @param model the model
         * @return this builder
         */
        public Builder withModel(@Nullable String model) {
            this.model = model;
            return this;
        }

        /**
         * Set the additional parameters.
         * 
         * @param additionalParams the additional parameters
         * @return this builder
         */
        public Builder withAdditionalParams(Map<String, Object> additionalParams) {
            this.additionalParams = Objects.requireNonNull(additionalParams, "Additional params cannot be null");
            return this;
        }

        /**
         * Set the stream flag.
         * 
         * @param stream the stream flag
         * @return this builder
         */
        public Builder withStream(boolean stream) {
            this.stream = stream;
            return this;
        }

        /**
         * Set the system prompt.
         * 
         * @param systemPrompt the system prompt
         * @return this builder
         */
        public Builder withSystemPrompt(@Nullable String systemPrompt) {
            this.systemPrompt = systemPrompt;
            return this;
        }

        /**
         * Set the timeout in milliseconds.
         * 
         * @param timeoutMs the timeout in milliseconds
         * @return this builder
         */
        public Builder withTimeoutMs(int timeoutMs) {
            this.timeoutMs = timeoutMs;
            return this;
        }

        public ModelParameters build() {
            if (maxTokens <= 0) {
                throw new IllegalArgumentException("Max tokens must be positive");
            }
            if (temperature < 0.0 || temperature > 2.0) {
                throw new IllegalArgumentException("Temperature must be between 0.0 and 2.0");
            }
            if (timeoutMs <= 0) {
                throw new IllegalArgumentException("Timeout must be positive");
            }
            return new ModelParameters(this);
        }
    }
}
