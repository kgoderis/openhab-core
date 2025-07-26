package org.openhab.core.ai.mcp.dto;

import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Transfer Object for MCP (Model Context Protocol) errors.
 * 
 * This class represents error information in MCP protocol responses
 * following the JSON-RPC 2.0 error specification.
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MCPError {

    @JsonProperty("code")
    private final int code;

    @JsonProperty("message")
    private final String message;

    @JsonProperty("data")
    private final Object data;

    /**
     * Create a new MCP error.
     * 
     * @param code Error code (following JSON-RPC 2.0 error codes)
     * @param message Error message
     * @param data Additional error data (optional)
     */
    public MCPError(int code, String message, Object data) {
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
    public MCPError(int code, String message) {
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
            return Optional.of((T) data);
        }
        return Optional.empty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        MCPError mcpError = (MCPError) o;
        return code == mcpError.code && Objects.equals(message, mcpError.message)
                && Objects.equals(data, mcpError.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, message, data);
    }

    @Override
    public String toString() {
        return "MCPError{" + "code=" + code + ", message='" + message + '\'' + ", data=" + data + '}';
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
    public static MCPError parseError(String details) {
        return new MCPError(PARSE_ERROR, "Parse error", details);
    }

    public static MCPError invalidRequest(String details) {
        return new MCPError(INVALID_REQUEST, "Invalid request", details);
    }

    public static MCPError methodNotFound(String method) {
        return new MCPError(METHOD_NOT_FOUND, "Method not found: " + method);
    }

    public static MCPError invalidParams(String details) {
        return new MCPError(INVALID_PARAMS, "Invalid parameters", details);
    }

    public static MCPError internalError(String details) {
        return new MCPError(INTERNAL_ERROR, "Internal error", details);
    }

    public static MCPError toolNotFound(String toolName) {
        return new MCPError(TOOL_NOT_FOUND, "Tool not found: " + toolName);
    }

    public static MCPError toolExecutionError(String toolName, String details) {
        return new MCPError(TOOL_EXECUTION_ERROR, "Tool execution error in " + toolName, details);
    }

    public static MCPError authenticationError(String details) {
        return new MCPError(AUTHENTICATION_ERROR, "Authentication error", details);
    }

    public static MCPError authorizationError(String details) {
        return new MCPError(AUTHORIZATION_ERROR, "Authorization error", details);
    }
}
