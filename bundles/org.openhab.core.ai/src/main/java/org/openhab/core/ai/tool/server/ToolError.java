package org.openhab.core.ai.tool.server;

import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * MCP error DTO.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ToolError {

    @JsonProperty("code")
    private final int code;

    @JsonProperty("message")
    private final String message;

    @JsonProperty("data")
    private final @Nullable Object data;

    /**
     * Create a new MCP error.
     * 
     * @param code Error code (following JSON-RPC 2.0 error codes)
     * @param message Error message
     * @param data Additional error data (optional)
     */
    public ToolError(int code, String message, @Nullable Object data) {
        this.code = code;
        this.message = Objects.requireNonNull(message, "Error message cannot be null");
        this.data = data;
    }

    /**
     * Create a new MCP error without additional data.
     * 
     * @param code Error code
     * @param message Error message
     */
    public ToolError(int code, String message) {
        this(code, message, null);
    }

    /**
     * Get the error code.
     * 
     * @return Error code
     */
    public int getCode() {
        return code;
    }

    /**
     * Get the error message.
     * 
     * @return Error message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get additional error data.
     * 
     * @return Error data, or empty if not provided
     */
    public Optional<Object> getData() {
        return Optional.ofNullable(data);
    }

    /**
     * Get typed error data.
     * 
     * @param <T> Expected data type
     * @param type Expected type class
     * @return Typed error data, or empty if not found or wrong type
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getData(Class<T> type) {
        if (data != null && type.isInstance(data)) {
            @SuppressWarnings("null")
            T typedData = (T) data;
            return Optional.of(typedData);
        }
        return Optional.empty();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ToolError toolError = (ToolError) o;
        return code == toolError.code && Objects.equals(message, toolError.message)
                && Objects.equals(data, toolError.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, message, data);
    }

    @Override
    public String toString() {
        return "ToolError{" + "code=" + code + ", message='" + message + '\'' + ", data=" + data + '}';
    }

    // Standard JSON-RPC 2.0 error codes
    public static final int PARSE_ERROR = -32700;
    public static final int INVALID_REQUEST = -32600;
    public static final int METHOD_NOT_FOUND = -32601;
    public static final int INVALID_PARAMS = -32602;
    public static final int INTERNAL_ERROR = -32603;

    // MCP-specific error codes (custom range)
    public static final int TOOL_NOT_FOUND = -32000;
    public static final int TOOL_EXECUTION_ERROR = -32001;
    public static final int AUTHENTICATION_ERROR = -32002;
    public static final int AUTHORIZATION_ERROR = -32003;
    public static final int RESOURCE_NOT_FOUND = -32004;
    public static final int RESOURCE_ACCESS_ERROR = -32005;

    // Factory methods for common errors
    public static ToolError parseError(String details) {
        return new ToolError(PARSE_ERROR, "Parse error", details);
    }

    public static ToolError invalidRequest(String details) {
        return new ToolError(INVALID_REQUEST, "Invalid request", details);
    }

    public static ToolError methodNotFound(String method) {
        return new ToolError(METHOD_NOT_FOUND, "Method not found: " + method);
    }

    public static ToolError invalidParams(String details) {
        return new ToolError(INVALID_PARAMS, "Invalid parameters", details);
    }

    public static ToolError internalError(String details) {
        return new ToolError(INTERNAL_ERROR, "Internal error", details);
    }

    public static ToolError toolNotFound(String toolName) {
        return new ToolError(TOOL_NOT_FOUND, "Tool not found: " + toolName);
    }

    public static ToolError toolExecutionError(String toolName, String details) {
        return new ToolError(TOOL_EXECUTION_ERROR, "Tool execution error in " + toolName, details);
    }

    public static ToolError authenticationError(String details) {
        return new ToolError(AUTHENTICATION_ERROR, "Authentication error", details);
    }

    public static ToolError authorizationError(String details) {
        return new ToolError(AUTHORIZATION_ERROR, "Authorization error", details);
    }
}
