package org.openhab.core.ai.model;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.response.ModelResponseBuilder;
import org.openhab.core.ai.common.response.Response;

/**
 * Represents a response from an LLM provider.
 * 
 * @author openHAB AI Team
 * @since 4.0.0
 */
@NonNullByDefault
public class ModelResponse implements Response<String> {

    private final String content;
    private final String modelName;
    private final String providerType;
    private final Instant timestamp;
    private final int promptTokens;
    private final int completionTokens;
    private final int totalTokens;
    private final double cost;
    private final long responseTimeMs;
    private final Map<String, Object> metadata;
    private final @Nullable String finishReason;
    private final @Nullable String errorMessage;

    // Constructor using inner Builder removed; use ModelResponseBuilder instead

    public ModelResponse(String content, String modelName, String providerType, long timestamp, int promptTokens,
            int completionTokens, int totalTokens, double cost, long responseTimeMs, Map<String, Object> metadata,
            @Nullable String finishReason, @Nullable String errorMessage) {
        this.content = content;
        this.modelName = modelName;
        this.providerType = providerType;
        this.timestamp = Instant.ofEpochMilli(timestamp);
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.totalTokens = totalTokens;
        this.cost = cost;
        this.responseTimeMs = responseTimeMs;
        this.metadata = metadata;
        this.finishReason = finishReason;
        this.errorMessage = errorMessage;
    }

    /**
     * Gets the generated content/text from the LLM.
     * 
     * @return The response content
     */
    public String getContent() {
        return content;
    }

    /**
     * Gets the model name that generated this response.
     * 
     * @return The model name
     */
    public String getModelName() {
        return modelName;
    }

    /**
     * Gets the provider type that generated this response.
     * 
     * @return The provider type
     */
    public String getProviderType() {
        return providerType;
    }

    /**
     * Gets the timestamp when this response was generated.
     * 
     * @return The timestamp as Instant
     */
    public Instant getTimestampInstant() {
        return timestamp;
    }

    // Response interface implementation
    @Override
    public String getId() {
        return UUID.randomUUID().toString();
    }

    @Override
    public boolean isSuccess() {
        return errorMessage == null;
    }

    @Override
    public String getData() {
        return content;
    }

    @Override
    public long getTimestamp() {
        return timestamp.toEpochMilli();
    }

    /**
     * Gets the number of tokens in the prompt.
     * 
     * @return Number of prompt tokens
     */
    public int getPromptTokens() {
        return promptTokens;
    }

    /**
     * Gets the number of tokens in the completion.
     * 
     * @return Number of completion tokens
     */
    public int getCompletionTokens() {
        return completionTokens;
    }

    /**
     * Gets the total number of tokens used.
     * 
     * @return Total number of tokens
     */
    public int getTotalTokens() {
        return totalTokens;
    }

    /**
     * Gets the cost of this request.
     * 
     * @return The cost in the provider's currency
     */
    public double getCost() {
        return cost;
    }

    /**
     * Gets the response time in milliseconds.
     * 
     * @return Response time in milliseconds
     */
    public long getResponseTimeMs() {
        return responseTimeMs;
    }

    /**
     * Gets additional metadata about the response.
     * 
     * @return Metadata map
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * Gets the finish reason for the completion.
     * 
     * @return The finish reason, or null if not available
     */
    public @Nullable String getFinishReason() {
        return finishReason;
    }

    /**
     * Gets any error message associated with this response.
     * 
     * @return Error message, or null if no error
     */
    public @Nullable String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Checks if this response contains an error.
     * 
     * @return true if an error occurred
     */
    public boolean hasError() {
        return errorMessage != null;
    }

    // Builder extracted to top-level: see ModelResponseBuilder

    public static ModelResponseBuilder builder() {
        return new ModelResponseBuilder();
    }
}
