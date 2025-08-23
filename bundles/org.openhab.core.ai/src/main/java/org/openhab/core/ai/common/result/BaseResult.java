package org.openhab.core.ai.common.result;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Abstract base class for result implementations.
 * 
 * <p>
 * This class provides common functionality for all result implementations:
 * - Immutable result data with defensive copying
 * - Common validation and utility methods
 * - Standard equals/hashCode/toString implementations
 * - Success/failure status tracking
 * - Execution timing and metadata
 * - Static factory methods for common result types
 * - Builder pattern for complex construction
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public abstract class BaseResult {

    private final String resultId;
    private final String resultType;
    private final boolean success;
    private final String message;
    private final @Nullable String errorMessage;
    private final long executionTimeMs;
    private final Instant timestamp;
    private final Map<String, Object> metadata;

    /**
     * Create a new base result.
     * 
     * @param resultId the unique result identifier
     * @param resultType the result type/category
     * @param success whether the operation was successful
     * @param message the result message
     * @param errorMessage the error message (if any)
     * @param executionTimeMs the execution time in milliseconds
     * @param timestamp the result timestamp
     * @param metadata additional metadata (will be copied)
     */
    protected BaseResult(String resultId, String resultType, boolean success, String message,
            @Nullable String errorMessage, long executionTimeMs, Instant timestamp,
            @Nullable Map<String, Object> metadata) {
        this.resultId = Objects.requireNonNull(resultId, "Result ID cannot be null");
        this.resultType = Objects.requireNonNull(resultType, "Result type cannot be null");
        this.success = success;
        this.message = Objects.requireNonNull(message, "Message cannot be null");
        this.errorMessage = errorMessage;
        this.executionTimeMs = executionTimeMs;
        this.timestamp = Objects.requireNonNull(timestamp, "Timestamp cannot be null");
        this.metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
    }

    // ===== STATIC FACTORY METHODS =====

    /**
     * Create a successful result with default values.
     * 
     * @param resultType the result type
     * @param message the success message
     * @return a builder for the successful result
     */
    public static Builder success(String resultType, String message) {
        return new Builder().resultId(generateResultId()).resultType(resultType).success(true).message(message)
                .executionTimeMs(0).timestamp(Instant.now());
    }

    /**
     * Create an error result with default values.
     * 
     * @param resultType the result type
     * @param message the error message
     * @return a builder for the error result
     */
    public static Builder error(String resultType, String message) {
        return new Builder().resultId(generateResultId()).resultType(resultType).success(false).message(message)
                .errorMessage(message).executionTimeMs(0).timestamp(Instant.now());
    }

    /**
     * Create a successful result with execution time.
     * 
     * @param resultType the result type
     * @param message the success message
     * @param executionTimeMs the execution time in milliseconds
     * @return a builder for the successful result
     */
    public static Builder success(String resultType, String message, long executionTimeMs) {
        return success(resultType, message).executionTimeMs(executionTimeMs);
    }

    /**
     * Create an error result with execution time.
     * 
     * @param resultType the result type
     * @param message the error message
     * @param executionTimeMs the execution time in milliseconds
     * @return a builder for the error result
     */
    public static Builder error(String resultType, String message, long executionTimeMs) {
        return error(resultType, message).executionTimeMs(executionTimeMs);
    }

    /**
     * Generate a unique result ID.
     * 
     * @return a unique result ID
     */
    protected static String generateResultId() {
        return "result-" + UUID.randomUUID().toString();
    }

    // ===== CORE GETTERS =====

    /**
     * Get the result ID.
     * 
     * @return the result ID
     */
    public String getResultId() {
        return resultId;
    }

    /**
     * Get the result type.
     * 
     * @return the result type
     */
    public String getResultType() {
        return resultType;
    }

    /**
     * Check if the operation was successful.
     * 
     * @return true if successful, false otherwise
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Get the result message.
     * 
     * @return the result message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get the error message.
     * 
     * @return the error message or null if no error
     */
    public @Nullable String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Get the execution time in milliseconds.
     * 
     * @return the execution time
     */
    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    /**
     * Get the result timestamp.
     * 
     * @return the timestamp
     */
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Get the result metadata.
     * 
     * @return the metadata map
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    // ===== HELPER METHODS =====

    /**
     * Check if there is an error message.
     * 
     * @return true if there is an error message, false otherwise
     */
    public boolean hasError() {
        return errorMessage != null && !errorMessage.isEmpty();
    }

    /**
     * Get a specific metadata value.
     * 
     * @param key the metadata key
     * @return the metadata value or null if not found
     */
    public @Nullable Object getMetadata(String key) {
        return metadata.get(key);
    }

    /**
     * Check if metadata contains a specific key.
     * 
     * @param key the metadata key
     * @return true if the key exists, false otherwise
     */
    public boolean hasMetadata(String key) {
        return metadata.containsKey(key);
    }

    /**
     * Get the metadata size.
     * 
     * @return the number of metadata entries
     */
    public int getMetadataSize() {
        return metadata.size();
    }

    /**
     * Check if metadata is empty.
     * 
     * @return true if metadata is empty, false otherwise
     */
    public boolean isMetadataEmpty() {
        return metadata.isEmpty();
    }

    /**
     * Get a summary of the result.
     * 
     * @return a summary string
     */
    public String getSummary() {
        if (success) {
            return String.format("Success: %s (execution: %dms)", message, executionTimeMs);
        } else {
            return String.format("Error: %s (execution: %dms)", errorMessage != null ? errorMessage : message,
                    executionTimeMs);
        }
    }

    // ===== EQUALS/HASHCODE/TOSTRING =====

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        BaseResult other = (BaseResult) obj;
        return Objects.equals(resultId, other.resultId) && Objects.equals(resultType, other.resultType)
                && success == other.success && Objects.equals(message, other.message)
                && Objects.equals(errorMessage, other.errorMessage) && executionTimeMs == other.executionTimeMs
                && Objects.equals(timestamp, other.timestamp) && Objects.equals(metadata, other.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resultId, resultType, success, message, errorMessage, executionTimeMs, timestamp, metadata);
    }

    @Override
    public String toString() {
        return String.format(
                "BaseResult{id='%s', type='%s', success=%s, message='%s', executionTime=%dms, timestamp=%s, metadataSize=%d}",
                resultId, resultType, success, message, executionTimeMs, timestamp, metadata.size());
    }

    // ===== BUILDER CLASS =====

    /**
     * Builder for creating BaseResult instances.
     * 
     * @author Karel Goderis - Initial Contribution
     * @since 1.0.0
     */
    public static final class Builder {
        private String resultId = generateResultId();
        private String resultType = "base";
        private boolean success = true;
        private String message = "Success";
        private @Nullable String errorMessage = null;
        private long executionTimeMs = 0;
        private Instant timestamp = Instant.now();
        private Map<String, Object> metadata = Map.of();

        /**
         * Set the result ID.
         * 
         * @param resultId the result ID
         * @return this builder
         */
        public Builder resultId(String resultId) {
            this.resultId = Objects.requireNonNull(resultId, "Result ID cannot be null");
            return this;
        }

        /**
         * Set the result type.
         * 
         * @param resultType the result type
         * @return this builder
         */
        public Builder resultType(String resultType) {
            this.resultType = Objects.requireNonNull(resultType, "Result type cannot be null");
            return this;
        }

        /**
         * Set the success status.
         * 
         * @param success the success status
         * @return this builder
         */
        public Builder success(boolean success) {
            this.success = success;
            return this;
        }

        /**
         * Set the result message.
         * 
         * @param message the result message
         * @return this builder
         */
        public Builder message(String message) {
            this.message = Objects.requireNonNull(message, "Message cannot be null");
            return this;
        }

        /**
         * Set the error message.
         * 
         * @param errorMessage the error message
         * @return this builder
         */
        public Builder errorMessage(@Nullable String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        /**
         * Set the execution time.
         * 
         * @param executionTimeMs the execution time in milliseconds
         * @return this builder
         */
        public Builder executionTimeMs(long executionTimeMs) {
            this.executionTimeMs = executionTimeMs;
            return this;
        }

        /**
         * Set the timestamp.
         * 
         * @param timestamp the timestamp
         * @return this builder
         */
        public Builder timestamp(Instant timestamp) {
            this.timestamp = Objects.requireNonNull(timestamp, "Timestamp cannot be null");
            return this;
        }

        /**
         * Set the metadata.
         * 
         * @param metadata the metadata map
         * @return this builder
         */
        public Builder metadata(@Nullable Map<String, Object> metadata) {
            this.metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
            return this;
        }

        /**
         * Add a metadata entry.
         * 
         * @param key the metadata key
         * @param value the metadata value
         * @return this builder
         */
        public Builder addMetadata(String key, Object value) {
            Objects.requireNonNull(key, "Metadata key cannot be null");
            Map<String, Object> newMetadata = new java.util.HashMap<>(this.metadata);
            newMetadata.put(key, value);
            this.metadata = Map.copyOf(newMetadata);
            return this;
        }

        /**
         * Build a BaseResult instance.
         * Note: This method should be overridden by subclasses to return the specific result type.
         * 
         * @return a BaseResult instance
         * @throws UnsupportedOperationException if called on BaseResult.Builder
         */
        public BaseResult build() {
            throw new UnsupportedOperationException(
                    "BaseResult.Builder.build() should not be called directly. Use a specific result builder instead.");
        }
    }
}
