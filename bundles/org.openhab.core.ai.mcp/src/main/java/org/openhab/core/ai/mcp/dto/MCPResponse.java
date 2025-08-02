package org.openhab.core.ai.mcp.dto;

import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * MCP response DTO.
 * 
 * @author AI Assistant
 * @since 1.0.0
 */
@NonNullByDefault
public class MCPResponse {

    @JsonProperty("jsonrpc")
    private final String jsonrpc = "2.0";

    @JsonProperty("id")
    private final String id;

    @JsonProperty("result")
    private final @Nullable Object result;

    @JsonProperty("error")
    private final @Nullable MCPError error;

    /**
     * Create a successful MCP response.
     * 
     * @param id Request identifier this response corresponds to
     * @param result Response result data
     */
    public MCPResponse(String id, Object result) {
        this.id = Objects.requireNonNull(id, "Response ID cannot be null");
        this.result = result;
        this.error = null;
    }

    /**
     * Create an error MCP response.
     * 
     * @param id Request identifier this response corresponds to
     * @param error Error information
     */
    public MCPResponse(String id, MCPError error) {
        this.id = Objects.requireNonNull(id, "Response ID cannot be null");
        this.result = null;
        this.error = Objects.requireNonNull(error, "Error cannot be null");
    }

    /**
     * Get the JSON-RPC version.
     * 
     * @return Always "2.0"
     */
    public String getJsonrpc() {
        return jsonrpc;
    }

    /**
     * Get the response identifier.
     * 
     * @return Response ID (matches request ID)
     */
    public String getId() {
        return id;
    }

    /**
     * Get the response result.
     * 
     * @return Result data, or empty if this is an error response
     */
    public Optional<Object> getResult() {
        return Optional.ofNullable(result);
    }

    /**
     * Get a typed response result.
     * 
     * @param <T> Expected result type
     * @param type Expected type class
     * @return Typed result, or empty if not found or wrong type
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getResult(Class<T> type) {
        if (result != null && type.isInstance(result)) {
            @SuppressWarnings("null")
            T typedResult = (T) result;
            return Optional.of(typedResult);
        }
        return Optional.empty();
    }

    /**
     * Get the error information.
     * 
     * @return Error information, or empty if this is a success response
     */
    public Optional<MCPError> getError() {
        return Optional.ofNullable(error);
    }

    /**
     * Check if this response indicates success.
     * 
     * @return true if successful, false if error
     */
    public boolean isSuccess() {
        return error == null;
    }

    /**
     * Check if this response indicates an error.
     * 
     * @return true if error, false if successful
     */
    public boolean isError() {
        return error != null;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        MCPResponse that = (MCPResponse) o;
        return Objects.equals(id, that.id) && Objects.equals(result, that.result) && Objects.equals(error, that.error);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, result, error);
    }

    @Override
    public String toString() {
        return "MCPResponse{" + "jsonrpc='" + jsonrpc + '\'' + ", id='" + id + '\'' + ", result=" + result + ", error="
                + error + ", success=" + isSuccess() + '}';
    }
}
