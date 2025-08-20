package org.openhab.core.ai.model;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.response.ModelResponse;

/**
 * Builder for creating ModelResponse objects.
 * 
 * <p>
 * This builder provides a fluent API for constructing ModelResponse instances
 * with all necessary parameters and optional metadata.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ModelResponseBuilder {
    private String content = "";
    private String modelName = "";
    private String providerType = "";
    private int promptTokens = 0;
    private int completionTokens = 0;
    private int totalTokens = 0;
    private double cost = 0.0;
    private long responseTimeMs = 0;
    private @Nullable String finishReason;
    private @Nullable String errorMessage;
    private Map<String, Object> metadata = new HashMap<>();
    private Instant timestamp = Instant.now();

    /**
     * Set the response content.
     * 
     * @param content the response content
     * @return this builder
     */
    public ModelResponseBuilder withContent(String content) {
        this.content = content != null ? content : "";
        return this;
    }

    /**
     * Set the model name.
     * 
     * @param modelName the model name
     * @return this builder
     */
    public ModelResponseBuilder withModelName(String modelName) {
        this.modelName = modelName != null ? modelName : "";
        return this;
    }

    /**
     * Set the provider type.
     * 
     * @param providerType the provider type
     * @return this builder
     */
    public ModelResponseBuilder withProviderType(String providerType) {
        this.providerType = providerType != null ? providerType : "";
        return this;
    }

    /**
     * Set the number of prompt tokens.
     * 
     * @param promptTokens the number of prompt tokens
     * @return this builder
     */
    public ModelResponseBuilder withPromptTokens(int promptTokens) {
        this.promptTokens = promptTokens;
        return this;
    }

    /**
     * Set the number of completion tokens.
     * 
     * @param completionTokens the number of completion tokens
     * @return this builder
     */
    public ModelResponseBuilder withCompletionTokens(int completionTokens) {
        this.completionTokens = completionTokens;
        return this;
    }

    /**
     * Set the total number of tokens.
     * 
     * @param totalTokens the total number of tokens
     * @return this builder
     */
    public ModelResponseBuilder withTotalTokens(int totalTokens) {
        this.totalTokens = totalTokens;
        return this;
    }

    /**
     * Set the cost.
     * 
     * @param cost the cost in the provider's currency
     * @return this builder
     */
    public ModelResponseBuilder withCost(double cost) {
        this.cost = cost;
        return this;
    }

    /**
     * Set the response time in milliseconds.
     * 
     * @param responseTimeMs the response time in milliseconds
     * @return this builder
     */
    public ModelResponseBuilder withResponseTimeMs(long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
        return this;
    }

    /**
     * Set the finish reason.
     * 
     * @param finishReason the finish reason
     * @return this builder
     */
    public ModelResponseBuilder withFinishReason(@Nullable String finishReason) {
        this.finishReason = finishReason;
        return this;
    }

    /**
     * Set the error message.
     * 
     * @param errorMessage the error message
     * @return this builder
     */
    public ModelResponseBuilder withErrorMessage(@Nullable String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    /**
     * Set the metadata.
     * 
     * @param metadata the metadata map
     * @return this builder
     */
    public ModelResponseBuilder withMetadata(Map<String, Object> metadata) {
        this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
        return this;
    }

    /**
     * Add a metadata entry.
     * 
     * @param key the metadata key
     * @param value the metadata value
     * @return this builder
     */
    public ModelResponseBuilder withMetadata(String key, Object value) {
        this.metadata.put(key, value);
        return this;
    }

    /**
     * Set the timestamp.
     * 
     * @param timestamp the timestamp
     * @return this builder
     */
    public ModelResponseBuilder withTimestamp(Instant timestamp) {
        this.timestamp = timestamp != null ? timestamp : Instant.now();
        return this;
    }

    /**
     * Build the ModelResponse.
     * 
     * @return the constructed ModelResponse
     */
    public ModelResponse build() {
        return new ModelResponse(content, modelName, providerType, timestamp.toEpochMilli(), promptTokens,
                completionTokens, totalTokens, cost, responseTimeMs, metadata, finishReason, errorMessage);
    }
}
