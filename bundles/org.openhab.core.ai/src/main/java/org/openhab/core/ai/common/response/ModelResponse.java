package org.openhab.core.ai.common.response;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Response implementation for LLM model responses.
 * 
 * This class represents responses from language model providers,
 * implementing the unified Response interface.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ModelResponse implements Response<String> {

    private final String id;
    private final String content;
    private final String modelName;
    private final String providerType;
    private final long timestamp;
    private final Instant timestampInstant;
    private final int promptTokens;
    private final int completionTokens;
    private final int totalTokens;
    private final double cost;
    private final long responseTimeMs;
    private final Map<String, Object> metadata;
    private final @Nullable String finishReason;
    private final @Nullable String errorMessage;

    /**
     * Create a new ModelResponse.
     * 
     * @param builder the builder containing all parameters
     */
    private ModelResponse(Builder builder) {
        this.id = builder.id != null ? builder.id : generateId();
        this.content = Objects.requireNonNull(builder.content, "content");
        this.modelName = Objects.requireNonNull(builder.modelName, "modelName");
        this.providerType = Objects.requireNonNull(builder.providerType, "providerType");
        this.timestamp = builder.timestamp;
        this.timestampInstant = Instant.ofEpochMilli(timestamp);
        this.promptTokens = builder.promptTokens;
        this.completionTokens = builder.completionTokens;
        this.totalTokens = builder.totalTokens;
        this.cost = builder.cost;
        this.responseTimeMs = builder.responseTimeMs;
        this.metadata = Map.copyOf(builder.metadata);
        this.finishReason = builder.finishReason;
        this.errorMessage = builder.errorMessage;
    }

    /**
     * Create a new ModelResponseBuilder instance.
     * 
     * @return a new ModelResponseBuilder
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

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isSuccess() {
        return errorMessage == null;
    }

    @Override
    public String getData() {
        return content;
    }

    /**
     * Get the response content (alias for getData() for backward compatibility).
     * 
     * @return the response content
     */
    public String getContent() {
        return content;
    }

    @Override
    public @Nullable String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Get the model name that generated this response.
     * 
     * @return the model name
     */
    public String getModelName() {
        return modelName;
    }

    /**
     * Get the provider type that generated this response.
     * 
     * @return the provider type
     */
    public String getProviderType() {
        return providerType;
    }

    /**
     * Get the number of prompt tokens used.
     * 
     * @return the number of prompt tokens
     */
    public int getPromptTokens() {
        return promptTokens;
    }

    /**
     * Get the number of completion tokens generated.
     * 
     * @return the number of completion tokens
     */
    public int getCompletionTokens() {
        return completionTokens;
    }

    /**
     * Get the total number of tokens used.
     * 
     * @return the total number of tokens
     */
    public int getTotalTokens() {
        return totalTokens;
    }

    /**
     * Get the cost of this response.
     * 
     * @return the response cost
     */
    public double getCost() {
        return cost;
    }

    /**
     * Get the response time in milliseconds.
     * 
     * @return the response time
     */
    public long getResponseTimeMs() {
        return responseTimeMs;
    }

    /**
     * Get the timestamp as Instant.
     * 
     * @return the timestamp as Instant
     */
    public Instant getTimestampInstant() {
        return timestampInstant;
    }

    /**
     * Get additional metadata for this response.
     * 
     * @return the metadata map
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * Get the finish reason for this response.
     * 
     * @return the finish reason, or null if not available
     */
    public @Nullable String getFinishReason() {
        return finishReason;
    }

    /**
     * Create a successful response.
     * 
     * @param content the response content
     * @param modelName the model name
     * @param providerType the provider type
     * @return a successful ModelResponse
     */
    public static ModelResponse success(String content, String modelName, String providerType) {
        return builder().withContent(content).withModelName(modelName).withProviderType(providerType).build();
    }

    /**
     * Create an error response.
     * 
     * @param errorMessage the error message
     * @param modelName the model name
     * @param providerType the provider type
     * @return an error ModelResponse
     */
    public static ModelResponse error(String errorMessage, String modelName, String providerType) {
        return builder().withContent("").withModelName(modelName).withProviderType(providerType)
                .withErrorMessage(errorMessage).build();
    }

    private static String generateId() {
        return "model-response-" + System.currentTimeMillis() + "-" + System.nanoTime();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ModelResponse other = (ModelResponse) obj;
        return Objects.equals(id, other.id) && Objects.equals(content, other.content)
                && Objects.equals(modelName, other.modelName) && Objects.equals(providerType, other.providerType)
                && timestamp == other.timestamp && Objects.equals(timestampInstant, other.timestampInstant)
                && promptTokens == other.promptTokens && completionTokens == other.completionTokens
                && totalTokens == other.totalTokens && Double.compare(cost, other.cost) == 0
                && responseTimeMs == other.responseTimeMs && Objects.equals(metadata, other.metadata)
                && Objects.equals(finishReason, other.finishReason) && Objects.equals(errorMessage, other.errorMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, content, modelName, providerType, timestamp, timestampInstant, promptTokens,
                completionTokens, totalTokens, cost, responseTimeMs, metadata, finishReason, errorMessage);
    }

    @Override
    public String toString() {
        return "ModelResponse{" + "id='" + id + '\'' + ", content='" + content + '\'' + ", modelName='" + modelName
                + '\'' + ", providerType='" + providerType + '\'' + ", timestamp=" + timestamp + ", timestampInstant="
                + timestampInstant + ", promptTokens=" + promptTokens + ", completionTokens=" + completionTokens
                + ", totalTokens=" + totalTokens + ", cost=" + cost + ", responseTimeMs=" + responseTimeMs
                + ", metadata=" + metadata + ", finishReason='" + finishReason + '\'' + ", errorMessage='"
                + errorMessage + '\'' + '}';
    }

    /**
     * Builder for creating ModelResponse objects.
     * 
     * <p>
     * Provides a fluent API with validation for constructing ModelResponse
     * instances.
     * </p>
     * 
     * @author Karel Goderis - Initial Contribution
     * @since 1.0.0
     */
    public static final class Builder {
        private @Nullable String id;
        private String content = "";
        private String modelName = "";
        private String providerType = "";
        private long timestamp = System.currentTimeMillis();
        private int promptTokens = 0;
        private int completionTokens = 0;
        private int totalTokens = 0;
        private double cost = 0.0;
        private long responseTimeMs = 0L;
        private Map<String, Object> metadata = Map.of();
        private @Nullable String finishReason;
        private @Nullable String errorMessage;

        public Builder() {
        }

        public Builder(ModelResponse source) {
            this.id = source.id;
            this.content = source.content;
            this.modelName = source.modelName;
            this.providerType = source.providerType;
            this.timestamp = source.timestamp;
            this.promptTokens = source.promptTokens;
            this.completionTokens = source.completionTokens;
            this.totalTokens = source.totalTokens;
            this.cost = source.cost;
            this.responseTimeMs = source.responseTimeMs;
            this.metadata = new HashMap<>(source.metadata);
            this.finishReason = source.finishReason;
            this.errorMessage = source.errorMessage;
        }

        public Builder withId(String id) {
            this.id = Objects.requireNonNull(id, "id");
            return this;
        }

        public Builder withContent(String content) {
            this.content = Objects.requireNonNull(content, "content");
            return this;
        }

        public Builder withModelName(String modelName) {
            this.modelName = Objects.requireNonNull(modelName, "modelName");
            return this;
        }

        public Builder withProviderType(String providerType) {
            this.providerType = Objects.requireNonNull(providerType, "providerType");
            return this;
        }

        public Builder withTimestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder withPromptTokens(int promptTokens) {
            this.promptTokens = promptTokens;
            return this;
        }

        public Builder withCompletionTokens(int completionTokens) {
            this.completionTokens = completionTokens;
            return this;
        }

        public Builder withTotalTokens(int totalTokens) {
            this.totalTokens = totalTokens;
            return this;
        }

        public Builder withCost(double cost) {
            this.cost = cost;
            return this;
        }

        public Builder withResponseTimeMs(long responseTimeMs) {
            this.responseTimeMs = responseTimeMs;
            return this;
        }

        public Builder withMetadata(Map<String, Object> metadata) {
            this.metadata = Objects.requireNonNull(metadata, "metadata");
            return this;
        }

        public Builder withFinishReason(@Nullable String finishReason) {
            this.finishReason = finishReason;
            return this;
        }

        public Builder withErrorMessage(@Nullable String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public ModelResponse build() {
            if (content.isBlank()) {
                throw new IllegalArgumentException("content must not be blank");
            }
            if (modelName.isBlank()) {
                throw new IllegalArgumentException("modelName must not be blank");
            }
            if (providerType.isBlank()) {
                throw new IllegalArgumentException("providerType must not be blank");
            }
            if (promptTokens < 0) {
                throw new IllegalArgumentException("promptTokens must be >= 0");
            }
            if (completionTokens < 0) {
                throw new IllegalArgumentException("completionTokens must be >= 0");
            }
            if (totalTokens < 0) {
                throw new IllegalArgumentException("totalTokens must be >= 0");
            }
            if (responseTimeMs < 0) {
                throw new IllegalArgumentException("responseTimeMs must be >= 0");
            }
            if (cost < 0) {
                throw new IllegalArgumentException("cost must be >= 0");
            }
            return new ModelResponse(this);
        }
    }
}
