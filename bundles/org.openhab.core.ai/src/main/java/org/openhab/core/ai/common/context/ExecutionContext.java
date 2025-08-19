package org.openhab.core.ai.common.context;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
     * Package-private constructor for builder pattern.
     */
    ExecutionContext(ExecutionContextBuilder builder) {
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
        return new ExecutionContextBuilder().withContextId(getContextId()).withProtocol(protocol).withClientId(clientId)
                .withSessionId(sessionId).withAuthContext(authContext).withExecutionStartTime(executionStartTime)
                .withCorrelationId(correlationId).withPriority(priority).withValues(newValues).withMetadata(newMetadata)
                .build();
    }

    /**
     * Get the execution duration in milliseconds.
     * 
     * @return the execution duration
     */
    public long getExecutionDuration() {
        return System.currentTimeMillis() - executionStartTime;
    }

    /**
     * Create a new builder for ExecutionContext.
     * 
     * @return a new ExecutionContextBuilder
     */
    public static ExecutionContextBuilder builder() {
        return new ExecutionContextBuilder();
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
