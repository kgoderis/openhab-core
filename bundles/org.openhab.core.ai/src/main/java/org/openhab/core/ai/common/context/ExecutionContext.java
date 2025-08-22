package org.openhab.core.ai.common.context;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.api.ActionKeys;
import org.openhab.core.ai.auth.AuthenticationContext;

/**
 * Unified execution context for AI operations.
 * 
 * <p>
 * This class consolidates execution context functionality from multiple sources:
 * - Protocol identification and metadata
 * - Authentication and authorization context
 * - Execution timing and correlation
 * - Protocol-specific context data
 * - Priority and scheduling information
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ExecutionContext extends BaseContext {

    // Protocol identification
    private final String protocol;
    private final String clientId;
    private final String sessionId;

    // Authentication and authorization
    private final @Nullable AuthenticationContext authContext;

    // Execution metadata
    private final long executionStartTime;
    private final String correlationId;
    private final @Nullable String priority;

    /**
     * Private constructor for builder pattern.
     */
    private ExecutionContext(Builder builder) {
        super(builder.contextId, "execution", builder.version, builder.values, builder.metadata, builder.createdAt,
                builder.lastModifiedAt);
        this.protocol = builder.protocol;
        this.clientId = builder.clientId;
        this.sessionId = builder.sessionId;
        this.authContext = builder.authContext;

        this.executionStartTime = builder.executionStartTime;
        this.correlationId = builder.correlationId;
        this.priority = builder.priority;
    }

    /**
     * Create a new builder for ExecutionContext.
     * 
     * @return a new Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create a builder from this ExecutionContext for modification.
     * 
     * @return a new Builder with current values
     */
    public Builder toBuilder() {
        return new Builder(this);
    }

    /**
     * Get the protocol identifier (e.g., "mcp", "a2a").
     * 
     * @return the protocol identifier
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * Get the client identifier.
     * 
     * @return the client ID
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Get the session identifier.
     * 
     * @return the session ID
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Get the authentication context.
     * 
     * @return the authentication context, or null if not available
     */
    public @Nullable AuthenticationContext getAuthContext() {
        return authContext;
    }

    /**
     * Get the execution start time in milliseconds.
     * 
     * @return the execution start time
     */
    public long getExecutionStartTime() {
        return executionStartTime;
    }

    /**
     * Get the correlation identifier for tracking.
     * 
     * @return the correlation ID
     */
    public String getCorrelationId() {
        return correlationId;
    }

    /**
     * Get the execution priority.
     * 
     * @return the priority, or empty if not set
     */
    public Optional<String> getPriority() {
        return Optional.ofNullable(priority);
    }

    @Override
    protected BaseContext createCopy(Map<String, Object> newValues, Map<String, Object> newMetadata) {
        return builder().withContextId(getContextId()).withProtocol(protocol).withClientId(clientId)
                .withSessionId(sessionId).withAuthContext(authContext).withExecutionStartTime(executionStartTime)
                .withCorrelationId(correlationId).withPriority(priority).withValues(newValues).withMetadata(newMetadata)
                .build();
    }

    /**
     * Builder for creating ExecutionContext instances.
     * 
     * @author Karel Goderis - Initial Contribution
     * @since 1.0.0
     */
    public static final class Builder {

        // Base context fields
        private String contextId = UUID.randomUUID().toString();
        private String version = "1.0.0";
        private final Map<String, Object> values = new HashMap<>();
        private final Map<String, Object> metadata = new HashMap<>();
        private Instant createdAt = Instant.now();
        private Instant lastModifiedAt = Instant.now();

        // Execution-specific fields
        private String protocol = "unknown";
        private String clientId = "unknown";
        private String sessionId = UUID.randomUUID().toString();
        private @Nullable AuthenticationContext authContext;

        private long executionStartTime = System.currentTimeMillis();
        private String correlationId = UUID.randomUUID().toString();
        private @Nullable String priority;

        /**
         * Default constructor.
         */
        public Builder() {
        }

        /**
         * Copy constructor.
         * 
         * @param source the source ExecutionContext
         */
        public Builder(ExecutionContext source) {
            this.contextId = source.getContextId();
            this.version = source.getVersion();
            this.values.putAll(source.getAllValues());
            this.metadata.putAll(source.getMetadata());
            this.createdAt = source.getCreatedAt();
            this.lastModifiedAt = source.getLastModifiedAt();
            this.protocol = source.protocol;
            this.clientId = source.clientId;
            this.sessionId = source.sessionId;
            this.authContext = source.authContext;
            this.executionStartTime = source.executionStartTime;
            this.correlationId = source.correlationId;
            this.priority = source.priority;
        }

        /**
         * Set the context identifier.
         * 
         * @param contextId the context ID
         * @return this builder
         */
        public Builder withContextId(String contextId) {
            this.contextId = Objects.requireNonNull(contextId, "Context ID cannot be null");
            return this;
        }

        /**
         * Set the context version.
         * 
         * @param version the version
         * @return this builder
         */
        public Builder withVersion(String version) {
            this.version = Objects.requireNonNull(version, "Version cannot be null");
            return this;
        }

        /**
         * Set the protocol identifier.
         * 
         * @param protocol the protocol (e.g., "mcp", "a2a")
         * @return this builder
         */
        public Builder withProtocol(String protocol) {
            this.protocol = Objects.requireNonNull(protocol, "Protocol cannot be null");
            return this;
        }

        /**
         * Set the client identifier.
         * 
         * @param clientId the client ID
         * @return this builder
         */
        public Builder withClientId(String clientId) {
            this.clientId = Objects.requireNonNull(clientId, "Client ID cannot be null");
            return this;
        }

        /**
         * Set the session identifier.
         * 
         * @param sessionId the session ID
         * @return this builder
         */
        public Builder withSessionId(String sessionId) {
            this.sessionId = Objects.requireNonNull(sessionId, "Session ID cannot be null");
            return this;
        }

        /**
         * Set the authentication context.
         * 
         * @param authContext the authentication context
         * @return this builder
         */
        public Builder withAuthContext(@Nullable AuthenticationContext authContext) {
            this.authContext = authContext;
            return this;
        }

        /**
         * Set the execution start time.
         * 
         * @param executionStartTime the execution start time
         * @return this builder
         */
        public Builder withExecutionStartTime(long executionStartTime) {
            this.executionStartTime = executionStartTime;
            return this;
        }

        /**
         * Set the correlation identifier.
         * 
         * @param correlationId the correlation ID
         * @return this builder
         */
        public Builder withCorrelationId(String correlationId) {
            this.correlationId = Objects.requireNonNull(correlationId, "Correlation ID cannot be null");
            return this;
        }

        /**
         * Set the execution priority.
         * 
         * @param priority the priority
         * @return this builder
         */
        public Builder withPriority(@Nullable String priority) {
            this.priority = priority;
            return this;
        }

        /**
         * Add a value to the context.
         * 
         * @param key the key
         * @param value the value
         * @return this builder
         */
        public Builder withValue(String key, Object value) {
            this.values.put(Objects.requireNonNull(key, "Value key cannot be null"), value);
            return this;
        }

        /**
         * Add values from a map.
         * 
         * @param values the values map
         * @return this builder
         */
        public Builder withValues(Map<String, Object> values) {
            if (values != null) {
                this.values.putAll(values);
            }
            return this;
        }

        /**
         * Add a protocol-specific value.
         * 
         * @param key the key
         * @param value the value
         * @return this builder
         */
        public Builder withProtocolValue(String key, Object value) {
            this.values.put("protocol." + Objects.requireNonNull(key, "Protocol key cannot be null"), value);
            return this;
        }

        /**
         * Add action name to protocol context.
         * 
         * @param actionName the action name
         * @return this builder
         */
        public Builder withActionName(String actionName) {
            return withProtocolValue(ActionKeys.ACTION_NAME.getKey(), actionName);
        }

        /**
         * Add action ID to protocol context.
         * 
         * @param actionId the action ID
         * @return this builder
         */
        public Builder withActionId(String actionId) {
            return withProtocolValue(ActionKeys.ACTION_ID.getKey(), actionId);
        }

        /**
         * Add parameters to protocol context.
         * 
         * @param parameters the parameters map
         * @return this builder
         */
        public Builder withParameters(Map<String, Object> parameters) {
            return withProtocolValue("parameters", parameters);
        }

        /**
         * Add arguments to protocol context.
         * 
         * @param actionArguments the arguments map
         * @return this builder
         */
        public Builder withArguments(Map<String, Object> actionArguments) {
            return withProtocolValue("arguments", actionArguments);
        }

        /**
         * Add metadata.
         * 
         * @param key the metadata key
         * @param value the metadata value
         * @return this builder
         */
        public Builder withMetadata(String key, Object value) {
            this.metadata.put(Objects.requireNonNull(key, "Metadata key cannot be null"), value);
            return this;
        }

        /**
         * Add metadata from a map.
         * 
         * @param metadata the metadata map
         * @return this builder
         */
        public Builder withMetadata(Map<String, Object> metadata) {
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
        public Builder withCreatedAt(Instant createdAt) {
            this.createdAt = Objects.requireNonNull(createdAt, "Creation timestamp cannot be null");
            return this;
        }

        /**
         * Set the last modified timestamp.
         * 
         * @param lastModifiedAt the last modified timestamp
         * @return this builder
         */
        public Builder withLastModifiedAt(Instant lastModifiedAt) {
            this.lastModifiedAt = Objects.requireNonNull(lastModifiedAt, "Last modified timestamp cannot be null");
            return this;
        }

        /**
         * Build the ExecutionContext.
         * 
         * @return the new ExecutionContext
         */
        public ExecutionContext build() {
            if (contextId.isBlank()) {
                throw new IllegalArgumentException("contextId must not be blank");
            }
            if (protocol.isBlank()) {
                throw new IllegalArgumentException("protocol must not be blank");
            }
            if (clientId.isBlank()) {
                throw new IllegalArgumentException("clientId must not be blank");
            }
            if (correlationId.isBlank()) {
                throw new IllegalArgumentException("correlationId must not be blank");
            }
            return new ExecutionContext(this);
        }
    }

    /**
     * Get the execution duration in milliseconds.
     * 
     * @return the execution duration
     */
    public long getExecutionDuration() {
        return System.currentTimeMillis() - executionStartTime;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj) || getClass() != obj.getClass()) {
            return false;
        }
        ExecutionContext other = (ExecutionContext) obj;
        return Objects.equals(protocol, other.protocol) && Objects.equals(clientId, other.clientId)
                && Objects.equals(sessionId, other.sessionId) && Objects.equals(authContext, other.authContext)
                && executionStartTime == other.executionStartTime && Objects.equals(correlationId, other.correlationId)
                && Objects.equals(priority, other.priority);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), protocol, clientId, sessionId, authContext, executionStartTime,
                correlationId, priority);
    }

    @Override
    public String toString() {
        return String.format(
                "ExecutionContext{id='%s', protocol='%s', clientId='%s', sessionId='%s', correlationId='%s'}",
                getContextId(), protocol, clientId, sessionId, correlationId);
    }
}
