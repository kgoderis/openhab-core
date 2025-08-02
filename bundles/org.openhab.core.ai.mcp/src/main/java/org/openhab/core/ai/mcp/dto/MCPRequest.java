package org.openhab.core.ai.mcp.dto;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * MCP request DTO.
 * 
 * @author AI Assistant
 * @since 1.0.0
 */
@NonNullByDefault
public class MCPRequest {

    @JsonProperty("jsonrpc")
    private final String jsonrpc = "2.0";

    @JsonProperty("id")
    private final String id;

    @JsonProperty("method")
    private final String method;

    @JsonProperty("params")
    private final Map<String, Object> params;

    /**
     * Create a new MCP request.
     * 
     * @param id Unique identifier for the request
     * @param method The method name to call
     * @param params Method parameters (optional)
     */
    public MCPRequest(String id, String method, Map<String, Object> params) {
        this.id = Objects.requireNonNull(id, "Request ID cannot be null");
        this.method = Objects.requireNonNull(method, "Method cannot be null");
        this.params = params;
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
     * Get the request identifier.
     * 
     * @return Request ID
     */
    public String getId() {
        return id;
    }

    /**
     * Get the method name.
     * 
     * @return Method name
     */
    public String getMethod() {
        return method;
    }

    /**
     * Get the method parameters.
     * 
     * @return Parameters map, or empty if no parameters
     */
    public Optional<Map<String, Object>> getParams() {
        return Optional.ofNullable(params);
    }

    /**
     * Get a specific parameter value.
     * 
     * @param key Parameter key
     * @return Parameter value, or empty if not found
     */
    public Optional<Object> getParam(String key) {
        return Optional.ofNullable(params != null ? params.get(key) : null);
    }

    /**
     * Get a typed parameter value.
     * 
     * @param <T> Parameter type
     * @param key Parameter key
     * @param type Expected type
     * @return Typed parameter value, or empty if not found or wrong type
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getParam(String key, Class<T> type) {
        Object value = params != null ? params.get(key) : null;
        if (value != null && type.isInstance(value)) {
            return Optional.of((T) value);
        }
        return Optional.empty();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        MCPRequest that = (MCPRequest) o;
        return Objects.equals(id, that.id) && Objects.equals(method, that.method)
                && Objects.equals(params, that.params);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, method, params);
    }

    @Override
    public String toString() {
        return "MCPRequest{" + "jsonrpc='" + jsonrpc + '\'' + ", id='" + id + '\'' + ", method='" + method + '\''
                + ", params=" + params + '}';
    }
}
