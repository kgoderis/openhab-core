package org.openhab.core.ai.common.response;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

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
     * @param id the response ID
     * @param content the response content
     * @param modelName the model name
     * @param providerType the provider type
     * @param timestamp the response timestamp
     * @param promptTokens the number of prompt tokens
     * @param completionTokens the number of completion tokens
     * @param totalTokens the total number of tokens
     * @param cost the response cost
     * @param responseTimeMs the response time in milliseconds
     * @param metadata additional metadata
     * @param finishReason the finish reason
     * @param errorMessage the error message if any
     */
    public ModelResponse(String id, String content, String modelName, String providerType, long timestamp,
            int promptTokens, int completionTokens, int totalTokens, double cost, long responseTimeMs,
            Map<String, Object> metadata, @Nullable String finishReason, @Nullable String errorMessage) {
        this.id = Objects.requireNonNull(id, "id");
        this.content = Objects.requireNonNull(content, "content");
        this.modelName = Objects.requireNonNull(modelName, "modelName");
        this.providerType = Objects.requireNonNull(providerType, "providerType");
        this.timestamp = timestamp;
        this.timestampInstant = Instant.ofEpochMilli(timestamp);
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.totalTokens = totalTokens;
        this.cost = cost;
        this.responseTimeMs = responseTimeMs;
        this.metadata = Objects.requireNonNull(metadata, "metadata");
        this.finishReason = finishReason;
        this.errorMessage = errorMessage;
    }

    /**
     * Create a new ModelResponse with Instant timestamp.
     * 
     * @param content the response content
     * @param modelName the model name
     * @param providerType the provider type
     * @param timestamp the response timestamp
     * @param promptTokens the number of prompt tokens
     * @param completionTokens the number of completion tokens
     * @param totalTokens the total number of tokens
     * @param cost the response cost
     * @param responseTimeMs the response time in milliseconds
     * @param metadata additional metadata
     * @param finishReason the finish reason
     * @param errorMessage the error message if any
     */
    public ModelResponse(String content, String modelName, String providerType, long timestamp, int promptTokens,
            int completionTokens, int totalTokens, double cost, long responseTimeMs, Map<String, Object> metadata,
            @Nullable String finishReason, @Nullable String errorMessage) {
        this(UUID.randomUUID().toString(), content, modelName, providerType, timestamp, promptTokens, completionTokens,
                totalTokens, cost, responseTimeMs, metadata, finishReason, errorMessage);
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
        return new ModelResponse(generateId(), content, modelName, providerType, System.currentTimeMillis(), 0, 0, 0,
                0.0, 0, Map.of(), null, null);
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
        return new ModelResponse(generateId(), "", modelName, providerType, System.currentTimeMillis(), 0, 0, 0, 0.0, 0,
                Map.of(), null, errorMessage);
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
}
