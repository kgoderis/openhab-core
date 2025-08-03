package org.openhab.core.ai.common.api.llm;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Represents a response from an LLM provider.
 * 
 * @author openHAB AI Team
 * @since 4.0.0
 */
@NonNullByDefault
public class LLMResponse {

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

    private LLMResponse(Builder builder) {
        this.content = builder.content;
        this.modelName = builder.modelName;
        this.providerType = builder.providerType;
        this.timestamp = builder.timestamp;
        this.promptTokens = builder.promptTokens;
        this.completionTokens = builder.completionTokens;
        this.totalTokens = builder.totalTokens;
        this.cost = builder.cost;
        this.responseTimeMs = builder.responseTimeMs;
        this.metadata = builder.metadata;
        this.finishReason = builder.finishReason;
        this.errorMessage = builder.errorMessage;
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
     * @return The timestamp
     */
    public Instant getTimestamp() {
        return timestamp;
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

    public static class Builder {
        private String content = "";
        private String modelName = "";
        private String providerType = "";
        private Instant timestamp = Instant.now();
        private int promptTokens = 0;
        private int completionTokens = 0;
        private int totalTokens = 0;
        private double cost = 0.0;
        private long responseTimeMs = 0;
        private Map<String, Object> metadata = Map.of();
        private @Nullable String finishReason;
        private @Nullable String errorMessage;

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder modelName(String modelName) {
            this.modelName = modelName;
            return this;
        }

        public Builder providerType(String providerType) {
            this.providerType = providerType;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder promptTokens(int promptTokens) {
            this.promptTokens = promptTokens;
            return this;
        }

        public Builder completionTokens(int completionTokens) {
            this.completionTokens = completionTokens;
            return this;
        }

        public Builder totalTokens(int totalTokens) {
            this.totalTokens = totalTokens;
            return this;
        }

        public Builder cost(double cost) {
            this.cost = cost;
            return this;
        }

        public Builder responseTimeMs(long responseTimeMs) {
            this.responseTimeMs = responseTimeMs;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder finishReason(@Nullable String finishReason) {
            this.finishReason = finishReason;
            return this;
        }

        public Builder errorMessage(@Nullable String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public LLMResponse build() {
            return new LLMResponse(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
