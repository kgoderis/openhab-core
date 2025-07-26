package org.openhab.core.ai.common.api.action;

import java.time.Instant;
import java.util.Map;

/**
 * Result of AI action execution.
 * 
 * 
 */
public class AIActionResult {

    private final boolean success;
    private final Object data;
    private final String message;
    private final AIActionError error;
    private final long executionTimeMs;
    private final Instant timestamp;
    private final Map<String, Object> metadata;

    private AIActionResult(boolean success, Object data, String message, AIActionError error, long executionTimeMs,
            Instant timestamp, Map<String, Object> metadata) {
        this.success = success;
        this.data = data;
        this.message = message;
        this.error = error;
        this.executionTimeMs = executionTimeMs;
        this.timestamp = timestamp;
        this.metadata = metadata != null ? metadata : Map.of();
    }

    /**
     * Create a successful result.
     * 
     * @param data the result data
     * @param executionTimeMs the execution time in milliseconds
     * @return the successful result
     */
    public static AIActionResult success(Object data, long executionTimeMs) {
        return new AIActionResult(true, data, "Success", null, executionTimeMs, Instant.now(), Map.of());
    }

    /**
     * Create a successful result with metadata.
     * 
     * @param data the result data
     * @param executionTimeMs the execution time in milliseconds
     * @param metadata additional metadata
     * @return the successful result
     */
    public static AIActionResult success(Object data, long executionTimeMs, Map<String, Object> metadata) {
        return new AIActionResult(true, data, "Success", null, executionTimeMs, Instant.now(), metadata);
    }

    /**
     * Create an error result.
     * 
     * @param message the error message
     * @param error the error details
     * @param executionTimeMs the execution time in milliseconds
     * @return the error result
     */
    public static AIActionResult error(String message, AIActionError error, long executionTimeMs) {
        return new AIActionResult(false, null, message, error, executionTimeMs, Instant.now(), Map.of());
    }

    /**
     * Create an error result with metadata.
     * 
     * @param message the error message
     * @param error the error details
     * @param executionTimeMs the execution time in milliseconds
     * @param metadata additional metadata
     * @return the error result
     */
    public static AIActionResult error(String message, AIActionError error, long executionTimeMs,
            Map<String, Object> metadata) {
        return new AIActionResult(false, null, message, error, executionTimeMs, Instant.now(), metadata);
    }

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public Object getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public AIActionError getError() {
        return error;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    @Override
    public String toString() {
        return String.format("AIActionResult{success=%s, message='%s', executionTime=%dms, timestamp=%s}", success,
                message, executionTimeMs, timestamp);
    }
}
