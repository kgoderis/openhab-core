package org.openhab.core.ai.action;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Result of an action execution.
 * 
 * This class encapsulates the result of an action execution including
 * success status, data, metadata, and any error information.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ActionResult {

    private final boolean success;
    private final @Nullable Object data;
    private final String message;
    private final @Nullable ActionError error;
    private final long executionTimeMs;
    private final Instant timestamp;
    private final Map<String, Object> metadata;

    private ActionResult(boolean success, @Nullable Object data, String message, @Nullable ActionError error,
            long executionTimeMs, Instant timestamp, Map<String, Object> metadata) {
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
    public static ActionResult success(Object data, long executionTimeMs) {
        return new ActionResult(true, data, "Success", null, executionTimeMs, Instant.now(), Map.of());
    }

    /**
     * Create a successful result with metadata.
     * 
     * @param data the result data
     * @param executionTimeMs the execution time in milliseconds
     * @param metadata additional metadata
     * @return the successful result
     */
    public static ActionResult success(Object data, long executionTimeMs, Map<String, Object> metadata) {
        return new ActionResult(true, data, "Success", null, executionTimeMs, Instant.now(), metadata);
    }

    /**
     * Create an error result.
     * 
     * @param message the error message
     * @param error the error details
     * @param executionTimeMs the execution time in milliseconds
     * @return the error result
     */
    public static ActionResult error(String message, ActionError error, long executionTimeMs) {
        return new ActionResult(false, null, message, error, executionTimeMs, Instant.now(), Map.of());
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
    public static ActionResult error(String message, ActionError error, long executionTimeMs,
            Map<String, Object> metadata) {
        return new ActionResult(false, null, message, error, executionTimeMs, Instant.now(), metadata);
    }

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public @Nullable Object getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public @Nullable ActionError getError() {
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
        return String.format("ActionResult{success=%s, message='%s', executionTime=%dms, timestamp=%s}", success,
                message, executionTimeMs, timestamp);
    }
}
