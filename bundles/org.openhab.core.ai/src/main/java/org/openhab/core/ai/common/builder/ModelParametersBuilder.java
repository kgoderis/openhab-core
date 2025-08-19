package org.openhab.core.ai.common.builder;

import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.model.ModelParameters;

/**
 * Builder for ModelParameters.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class ModelParametersBuilder extends AbstractBuilder<ModelParameters> {

    private double temperature = 0.7;
    private int maxTokens = 2048;
    private @Nullable String model;
    private Map<String, Object> additionalParams = Map.of();
    private boolean stream = false;
    private @Nullable String systemPrompt;
    private int timeoutMs = 30000;

    /**
     * Create a new ModelParametersBuilder.
     */
    public ModelParametersBuilder() {
        super();
    }

    /**
     * Set the temperature.
     * 
     * @param temperature the temperature
     * @return this builder
     */
    public ModelParametersBuilder withTemperature(double temperature) {
        this.temperature = temperature;
        return this;
    }

    /**
     * Set the max tokens.
     * 
     * @param maxTokens the max tokens
     * @return this builder
     */
    public ModelParametersBuilder withMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
        return this;
    }

    /**
     * Set the model.
     * 
     * @param model the model
     * @return this builder
     */
    public ModelParametersBuilder withModel(@Nullable String model) {
        this.model = model;
        return this;
    }

    /**
     * Set the additional parameters.
     * 
     * @param additionalParams the additional parameters
     * @return this builder
     */
    public ModelParametersBuilder withAdditionalParams(Map<String, Object> additionalParams) {
        this.additionalParams = Objects.requireNonNull(additionalParams, "Additional params cannot be null");
        return this;
    }

    /**
     * Set the stream flag.
     * 
     * @param stream the stream flag
     * @return this builder
     */
    public ModelParametersBuilder withStream(boolean stream) {
        this.stream = stream;
        return this;
    }

    /**
     * Set the system prompt.
     * 
     * @param systemPrompt the system prompt
     * @return this builder
     */
    public ModelParametersBuilder withSystemPrompt(@Nullable String systemPrompt) {
        this.systemPrompt = systemPrompt;
        return this;
    }

    /**
     * Set the timeout in milliseconds.
     * 
     * @param timeoutMs the timeout in milliseconds
     * @return this builder
     */
    public ModelParametersBuilder withTimeoutMs(int timeoutMs) {
        this.timeoutMs = timeoutMs;
        return this;
    }

    @Override
    public ModelParameters build() {
        validate();
        return new ModelParameters(this);
    }

    @Override
    protected void validate() {
        if (maxTokens <= 0) {
            addValidationError("Max tokens must be positive");
        }
        if (temperature < 0.0 || temperature > 2.0) {
            addValidationError("Temperature must be between 0.0 and 2.0");
        }
        if (timeoutMs <= 0) {
            addValidationError("Timeout must be positive");
        }
    }

    @Override
    protected void doReset() {
        temperature = 0.7;
        maxTokens = 2048;
        model = null;
        additionalParams = Map.of();
        stream = false;
        systemPrompt = null;
        timeoutMs = 30000;
    }

    // Getter methods for ModelParameters constructor
    public double getTemperature() {
        return temperature;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public @Nullable String getModel() {
        return model;
    }

    public Map<String, Object> getAdditionalParams() {
        return additionalParams;
    }

    public boolean isStream() {
        return stream;
    }

    public @Nullable String getSystemPrompt() {
        return systemPrompt;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }
}
