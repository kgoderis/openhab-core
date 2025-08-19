package org.openhab.core.ai.common.response;

import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Response implementation for MCP tool responses.
 * 
 * This class represents responses from MCP tool operations,
 * implementing the unified Response interface.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ToolResponse implements Response<Object> {

    private final String id;
    private final @Nullable Object data;
    private final long timestamp;
    private final String toolId;
    private final String operation;
    private final long executionTimeMs;
    private final Map<String, Object> metadata;
    private final @Nullable String errorMessage;

    /**
     * Create a new ToolResponse.
     * 
     * @param id the response ID
     * @param data the response data
     * @param timestamp the response timestamp
     * @param toolId the tool ID
     * @param operation the operation performed
     * @param executionTimeMs the execution time in milliseconds
     * @param metadata additional metadata
     * @param errorMessage the error message if any
     */
    public ToolResponse(String id, @Nullable Object data, long timestamp, String toolId, String operation,
            long executionTimeMs, Map<String, Object> metadata, @Nullable String errorMessage) {
        this.id = Objects.requireNonNull(id, "id");
        this.data = data;
        this.timestamp = timestamp;
        this.toolId = Objects.requireNonNull(toolId, "toolId");
        this.operation = Objects.requireNonNull(operation, "operation");
        this.executionTimeMs = executionTimeMs;
        this.metadata = Objects.requireNonNull(metadata, "metadata");
        this.errorMessage = errorMessage;
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
    public @Nullable Object getData() {
        return data;
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
     * Get the tool ID that generated this response.
     * 
     * @return the tool ID
     */
    public String getToolId() {
        return toolId;
    }

    /**
     * Get the operation that was performed.
     * 
     * @return the operation name
     */
    public String getOperation() {
        return operation;
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
     * Get additional metadata for this response.
     * 
     * @return the metadata map
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * Create a successful response.
     * 
     * @param data the response data
     * @param toolId the tool ID
     * @param operation the operation performed
     * @return a successful ToolResponse
     */
    public static ToolResponse success(Object data, String toolId, String operation) {
        return new ToolResponse(generateId(), data, System.currentTimeMillis(), toolId, operation, 0, Map.of(), null);
    }

    /**
     * Create an error response.
     * 
     * @param errorMessage the error message
     * @param toolId the tool ID
     * @param operation the operation attempted
     * @return an error ToolResponse
     */
    public static ToolResponse error(String errorMessage, String toolId, String operation) {
        return new ToolResponse(generateId(), null, System.currentTimeMillis(), toolId, operation, 0, Map.of(),
                errorMessage);
    }

    /**
     * Create a response with execution time.
     * 
     * @param data the response data
     * @param toolId the tool ID
     * @param operation the operation performed
     * @param executionTimeMs the execution time in milliseconds
     * @return a ToolResponse with execution time
     */
    public static ToolResponse success(Object data, String toolId, String operation, long executionTimeMs) {
        return new ToolResponse(generateId(), data, System.currentTimeMillis(), toolId, operation, executionTimeMs,
                Map.of(), null);
    }

    private static String generateId() {
        return "tool-response-" + System.currentTimeMillis() + "-" + System.nanoTime();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ToolResponse other = (ToolResponse) obj;
        return Objects.equals(id, other.id) && Objects.equals(data, other.data) && timestamp == other.timestamp
                && Objects.equals(toolId, other.toolId) && Objects.equals(operation, other.operation)
                && executionTimeMs == other.executionTimeMs && Objects.equals(metadata, other.metadata)
                && Objects.equals(errorMessage, other.errorMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, data, timestamp, toolId, operation, executionTimeMs, metadata, errorMessage);
    }

    @Override
    public String toString() {
        return "ToolResponse{" + "id='" + id + '\'' + ", data=" + data + ", timestamp=" + timestamp + ", toolId='"
                + toolId + '\'' + ", operation='" + operation + '\'' + ", executionTimeMs=" + executionTimeMs
                + ", metadata=" + metadata + ", errorMessage='" + errorMessage + '\'' + '}';
    }
}
