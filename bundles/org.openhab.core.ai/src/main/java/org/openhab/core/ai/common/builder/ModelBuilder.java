package org.openhab.core.ai.common.builder;

import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Unified builder for model-related objects in the openHAB AI system.
 *
 * <p>
 * This class provides a common builder pattern for creating model-related objects
 * such as ModelParameters, ModelMetadata, etc.
 * </p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public abstract class ModelBuilder<T> extends AbstractBuilder<T> {

    protected double temperature = 0.7;
    protected int maxTokens = 1000;
    protected @Nullable String model;
    protected Map<String, Object> additionalParams = Map.of();
    protected boolean stream = false;
    protected @Nullable String systemPrompt;
    protected int timeoutMs = 30000;
    protected String version = "1.0.0";
    protected String author = "Unknown";
    protected String description = "No description provided";

    /**
     * Set the temperature.
     *
     * @param temperature the temperature
     * @return this builder
     */
    public ModelBuilder<T> withTemperature(double temperature) {
        this.temperature = temperature;
        return this;
    }

    /**
     * Set the maximum tokens.
     *
     * @param maxTokens the maximum tokens
     * @return this builder
     */
    public ModelBuilder<T> withMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
        return this;
    }

    /**
     * Set the model.
     *
     * @param model the model
     * @return this builder
     */
    public ModelBuilder<T> withModel(@Nullable String model) {
        this.model = model;
        return this;
    }

    /**
     * Set the additional parameters.
     *
     * @param additionalParams the additional parameters
     * @return this builder
     */
    public ModelBuilder<T> withAdditionalParams(Map<String, Object> additionalParams) {
        this.additionalParams = Objects.requireNonNull(additionalParams, "additionalParams");
        return this;
    }

    /**
     * Set whether to stream.
     *
     * @param stream true if streaming, false otherwise
     * @return this builder
     */
    public ModelBuilder<T> withStream(boolean stream) {
        this.stream = stream;
        return this;
    }

    /**
     * Set the system prompt.
     *
     * @param systemPrompt the system prompt
     * @return this builder
     */
    public ModelBuilder<T> withSystemPrompt(@Nullable String systemPrompt) {
        this.systemPrompt = systemPrompt;
        return this;
    }

    /**
     * Set the timeout in milliseconds.
     *
     * @param timeoutMs the timeout in milliseconds
     * @return this builder
     */
    public ModelBuilder<T> withTimeoutMs(int timeoutMs) {
        this.timeoutMs = timeoutMs;
        return this;
    }

    /**
     * Set the version.
     *
     * @param version the version
     * @return this builder
     */
    public ModelBuilder<T> withVersion(String version) {
        this.version = Objects.requireNonNull(version, "version");
        return this;
    }

    /**
     * Set the author.
     *
     * @param author the author
     * @return this builder
     */
    public ModelBuilder<T> withAuthor(String author) {
        this.author = Objects.requireNonNull(author, "author");
        return this;
    }

    /**
     * Set the description.
     *
     * @param description the description
     * @return this builder
     */
    public ModelBuilder<T> withDescription(String description) {
        this.description = Objects.requireNonNull(description, "description");
        return this;
    }

    @Override
    protected void validate() {
        super.validate();
        // Additional validation specific to ModelBuilder
        if (temperature < 0.0 || temperature > 2.0) {
            throw new IllegalArgumentException("temperature must be between 0.0 and 2.0");
        }
        if (maxTokens <= 0) {
            throw new IllegalArgumentException("maxTokens must be > 0");
        }
        if (timeoutMs <= 0) {
            throw new IllegalArgumentException("timeoutMs must be > 0");
        }
        if (version.isBlank()) {
            throw new IllegalArgumentException("version must not be blank");
        }
        if (author.isBlank()) {
            throw new IllegalArgumentException("author must not be blank");
        }
    }

    @Override
    protected void doReset() {
        super.doReset();
        temperature = 0.7;
        maxTokens = 1000;
        model = null;
        additionalParams = Map.of();
        stream = false;
        systemPrompt = null;
        timeoutMs = 30000;
        version = "1.0.0";
        author = "Unknown";
        description = "No description provided";
    }
}
