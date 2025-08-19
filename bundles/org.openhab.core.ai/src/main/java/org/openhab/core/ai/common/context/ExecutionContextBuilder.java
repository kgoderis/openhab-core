package org.openhab.core.ai.common.context;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.api.ActionKeys;
import org.openhab.core.ai.auth.AuthenticationContext;

/**
 * Builder for creating ExecutionContext instances.
 * 
 * <p>
 * This builder provides a fluent API for creating execution contexts with:
 * - Required protocol and client information
 * - Optional authentication context
 * - Custom metadata and values
 * - Execution timing and correlation
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class ExecutionContextBuilder {

    // Base context fields
    String contextId = UUID.randomUUID().toString();
    String version = "1.0.0";
    final Map<String, Object> values = new HashMap<>();
    final Map<String, Object> metadata = new HashMap<>();
    Instant createdAt = Instant.now();
    Instant lastModifiedAt = Instant.now();

    // Execution-specific fields
    String protocol = "unknown";
    String clientId = "unknown";
    String sessionId = UUID.randomUUID().toString();
    @Nullable
    AuthenticationContext authContext;

    long executionStartTime = System.currentTimeMillis();
    String correlationId = UUID.randomUUID().toString();
    @Nullable
    String priority;

    /**
     * Set the context identifier.
     * 
     * @param contextId the context ID
     * @return this builder
     */
    public ExecutionContextBuilder withContextId(String contextId) {
        this.contextId = Objects.requireNonNull(contextId, "Context ID cannot be null");
        return this;
    }

    /**
     * Set the context version.
     * 
     * @param version the version
     * @return this builder
     */
    public ExecutionContextBuilder withVersion(String version) {
        this.version = Objects.requireNonNull(version, "Version cannot be null");
        return this;
    }

    /**
     * Set the protocol identifier.
     * 
     * @param protocol the protocol (e.g., "mcp", "a2a")
     * @return this builder
     */
    public ExecutionContextBuilder withProtocol(String protocol) {
        this.protocol = Objects.requireNonNull(protocol, "Protocol cannot be null");
        return this;
    }

    /**
     * Set the client identifier.
     * 
     * @param clientId the client ID
     * @return this builder
     */
    public ExecutionContextBuilder withClientId(String clientId) {
        this.clientId = Objects.requireNonNull(clientId, "Client ID cannot be null");
        return this;
    }

    /**
     * Set the session identifier.
     * 
     * @param sessionId the session ID
     * @return this builder
     */
    public ExecutionContextBuilder withSessionId(String sessionId) {
        this.sessionId = Objects.requireNonNull(sessionId, "Session ID cannot be null");
        return this;
    }

    /**
     * Set the authentication context.
     * 
     * @param authContext the authentication context
     * @return this builder
     */
    public ExecutionContextBuilder withAuthContext(@Nullable AuthenticationContext authContext) {
        this.authContext = authContext;
        return this;
    }

    /**
     * Set the execution start time.
     * 
     * @param executionStartTime the start time in milliseconds
     * @return this builder
     */
    public ExecutionContextBuilder withExecutionStartTime(long executionStartTime) {
        this.executionStartTime = executionStartTime;
        return this;
    }

    /**
     * Set the correlation identifier.
     * 
     * @param correlationId the correlation ID
     * @return this builder
     */
    public ExecutionContextBuilder withCorrelationId(String correlationId) {
        this.correlationId = Objects.requireNonNull(correlationId, "Correlation ID cannot be null");
        return this;
    }

    /**
     * Set the execution priority.
     * 
     * @param priority the priority
     * @return this builder
     */
    public ExecutionContextBuilder withPriority(@Nullable String priority) {
        this.priority = priority;
        return this;
    }

    /**
     * Add a context value.
     * 
     * @param key the key
     * @param value the value
     * @return this builder
     */
    public ExecutionContextBuilder withValue(String key, Object value) {
        this.values.put(Objects.requireNonNull(key, "Key cannot be null"), value);
        return this;
    }

    /**
     * Add context values from a map.
     * 
     * @param values the values map
     * @return this builder
     */
    public ExecutionContextBuilder withValues(Map<String, Object> values) {
        if (values != null) {
            this.values.putAll(values);
        }
        return this;
    }

    /**
     * Add protocol-specific context data.
     * 
     * @param protocolContext the protocol context map
     * @return this builder
     */
    public ExecutionContextBuilder withProtocolContext(Map<String, Object> protocolContext) {
        if (protocolContext != null) {
            this.values.putAll(protocolContext);
        }
        return this;
    }

    /**
     * Add a protocol context value using a type-safe key.
     * 
     * @param key the protocol key
     * @param value the value
     * @return this builder
     */
    public ExecutionContextBuilder withProtocolValue(String key, Object value) {
        this.values.put(key, value);
        return this;
    }

    /**
     * Add action name to protocol context.
     * 
     * @param actionName the action name
     * @return this builder
     */
    public ExecutionContextBuilder withActionName(String actionName) {
        return withProtocolValue(ActionKeys.ACTION_NAME.getKey(), actionName);
    }

    /**
     * Add action ID to protocol context.
     * 
     * @param actionId the action ID
     * @return this builder
     */
    public ExecutionContextBuilder withActionId(String actionId) {
        return withProtocolValue(ActionKeys.ACTION_ID.getKey(), actionId);
    }

    /**
     * Add parameters to protocol context.
     * 
     * @param parameters the parameters map
     * @return this builder
     */
    public ExecutionContextBuilder withParameters(Map<String, Object> parameters) {
        return withProtocolValue("parameters", parameters);
    }

    /**
     * Add arguments to protocol context.
     * 
     * @param actionArguments the arguments map
     * @return this builder
     */
    public ExecutionContextBuilder withArguments(Map<String, Object> actionArguments) {
        return withProtocolValue("arguments", actionArguments);
    }

    /**
     * Add metadata.
     * 
     * @param key the metadata key
     * @param value the metadata value
     * @return this builder
     */
    public ExecutionContextBuilder withMetadata(String key, Object value) {
        this.metadata.put(Objects.requireNonNull(key, "Metadata key cannot be null"), value);
        return this;
    }

    /**
     * Add metadata from a map.
     * 
     * @param metadata the metadata map
     * @return this builder
     */
    public ExecutionContextBuilder withMetadata(Map<String, Object> metadata) {
        if (metadata != null) {
            this.metadata.putAll(metadata);
        }
        return this;
    }

    /**
     * Set the creation timestamp.
     * 
     * @param createdAt the creation timestamp
     * @return this builder
     */
    public ExecutionContextBuilder withCreatedAt(Instant createdAt) {
        this.createdAt = Objects.requireNonNull(createdAt, "Creation timestamp cannot be null");
        return this;
    }

    /**
     * Set the last modified timestamp.
     * 
     * @param lastModifiedAt the last modified timestamp
     * @return this builder
     */
    public ExecutionContextBuilder withLastModifiedAt(Instant lastModifiedAt) {
        this.lastModifiedAt = Objects.requireNonNull(lastModifiedAt, "Last modified timestamp cannot be null");
        return this;
    }

    /**
     * Build the ExecutionContext.
     * 
     * @return the new ExecutionContext
     */
    public ExecutionContext build() {
        return new ExecutionContext(this);
    }
}
