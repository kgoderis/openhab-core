package org.openhab.core.ai.common.response;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response implementation for MCP tool responses.
 * 
 * This class represents responses from MCP tool operations,
 * implementing the unified Response interface and supporting
 * both general tool responses and MCP JSON-RPC protocol.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ToolResponse implements Response<Object> {

    @JsonProperty("jsonrpc")
    private final String jsonrpc = "2.0";

    private final String id;
    private final @Nullable Object data;
    private final long timestamp;
    private final String toolId;
    private final String operation;
    private final long executionTimeMs;
    private final Map<String, Object> metadata;
    private final @Nullable String errorMessage;
    private final @Nullable ToolError error;

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
        this.error = null;
    }

    /**
     * Create a new MCP ToolResponse with error.
     * 
     * @param id the response ID
     * @param error the MCP error
     */
    public ToolResponse(String id, ToolError error) {
        this.id = Objects.requireNonNull(id, "id");
        this.data = null;
        this.timestamp = System.currentTimeMillis();
        this.toolId = "unknown";
        this.operation = "unknown";
        this.executionTimeMs = 0;
        this.metadata = Map.of();
        this.errorMessage = error.getMessage();
        this.error = Objects.requireNonNull(error, "error");
    }

    /**
     * Get the JSON-RPC version.
     * 
     * @return Always "2.0"
     */
    public String getJsonrpc() {
        return jsonrpc;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isSuccess() {
        return errorMessage == null && error == null;
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
     * Get the MCP error information.
     * 
     * @return Error information, or empty if this is a success response
     */
    public Optional<ToolError> getError() {
        return Optional.ofNullable(error);
    }

    /**
     * Get the response result (MCP compatibility).
     * 
     * @return Result data, or empty if this is an error response
     */
    public Optional<Object> getResult() {
        return Optional.ofNullable(data);
    }

    /**
     * Get a typed response result (MCP compatibility).
     * 
     * @param <T> Expected result type
     * @param type Expected type class
     * @return Typed result, or empty if not found or wrong type
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getResult(Class<T> type) {
        if (data != null && type.isInstance(data)) {
            @SuppressWarnings("null")
            T typedResult = (T) data;
            return Optional.of(typedResult);
        }
        return Optional.empty();
    }

    /**
     * Check if this response indicates an error (MCP compatibility).
     * 
     * @return true if error, false if successful
     */
    public boolean isError() {
        return error != null || errorMessage != null;
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

    /**
     * Create an MCP error response.
     * 
     * @param id the response ID
     * @param error the MCP error
     * @return an MCP error ToolResponse
     */
    public static ToolResponse mcpError(String id, ToolError error) {
        return new ToolResponse(id, error);
    }

    /**
     * Create an MCP success response.
     * 
     * @param id the response ID
     * @param result the result data
     * @return an MCP success ToolResponse
     */
    public static ToolResponse mcpSuccess(String id, Object result) {
        return new ToolResponse(id, result, System.currentTimeMillis(), "mcp", "response", 0, Map.of(), null);
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
        return Objects.equals(jsonrpc, other.jsonrpc) && Objects.equals(id, other.id)
                && Objects.equals(data, other.data) && timestamp == other.timestamp
                && Objects.equals(toolId, other.toolId) && Objects.equals(operation, other.operation)
                && executionTimeMs == other.executionTimeMs && Objects.equals(metadata, other.metadata)
                && Objects.equals(errorMessage, other.errorMessage) && Objects.equals(error, other.error);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jsonrpc, id, data, timestamp, toolId, operation, executionTimeMs, metadata, errorMessage,
                error);
    }

    @Override
    public String toString() {
        return "ToolResponse{" + "jsonrpc='" + jsonrpc + '\'' + ", id='" + id + '\'' + ", data=" + data + ", timestamp="
                + timestamp + ", toolId='" + toolId + '\'' + ", operation='" + operation + '\'' + ", executionTimeMs="
                + executionTimeMs + ", metadata=" + metadata + ", errorMessage='" + errorMessage + '\'' + ", error="
                + error + '}';
    }
}
