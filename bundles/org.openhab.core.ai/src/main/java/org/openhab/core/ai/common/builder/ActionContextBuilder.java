package org.openhab.core.ai.common.builder;

import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.auth.AuthenticationContext;
import org.openhab.core.ai.common.context.ExecutionContext;
import org.openhab.core.ai.common.context.ExecutionContextBuilder;

/**
 * Unified builder for ExecutionContext objects.
 *
 * <p>
 * This builder provides a standardized way to create ExecutionContext objects
 * with proper validation and consistent API.
 * </p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class ActionContextBuilder extends ActionBuilder<ExecutionContext> {

    private String protocol = "";
    private String clientId = "";
    private String sessionId = "";
    private @Nullable AuthenticationContext authContext;
    private Map<String, Object> protocolContext = Map.of();
    private long executionStartTime = System.currentTimeMillis();
    private String correlationId = "";
    private String priority = "";

    /**
     * Set the protocol.
     *
     * @param protocol the protocol
     * @return this builder
     */
    public ActionContextBuilder withProtocol(String protocol) {
        this.protocol = Objects.requireNonNull(protocol, "protocol");
        return this;
    }

    /**
     * Set the client ID.
     *
     * @param clientId the client ID
     * @return this builder
     */
    public ActionContextBuilder withClientId(String clientId) {
        this.clientId = Objects.requireNonNull(clientId, "clientId");
        return this;
    }

    /**
     * Set the session ID.
     *
     * @param sessionId the session ID
     * @return this builder
     */
    public ActionContextBuilder withSessionId(String sessionId) {
        this.sessionId = Objects.requireNonNull(sessionId, "sessionId");
        return this;
    }

    /**
     * Set the authentication context.
     *
     * @param authContext the authentication context
     * @return this builder
     */
    public ActionContextBuilder withAuthContext(@Nullable AuthenticationContext authContext) {
        this.authContext = authContext;
        return this;
    }

    /**
     * Set the protocol context.
     *
     * @param protocolContext the protocol context
     * @return this builder
     */
    public ActionContextBuilder withProtocolContext(Map<String, Object> protocolContext) {
        this.protocolContext = protocolContext != null ? protocolContext : Map.of();
        return this;
    }

    /**
     * Set the execution start time.
     *
     * @param executionStartTime the execution start time
     * @return this builder
     */
    public ActionContextBuilder withExecutionStartTime(long executionStartTime) {
        this.executionStartTime = executionStartTime;
        return this;
    }

    /**
     * Set the correlation ID.
     *
     * @param correlationId the correlation ID
     * @return this builder
     */
    public ActionContextBuilder withCorrelationId(String correlationId) {
        this.correlationId = Objects.requireNonNull(correlationId, "correlationId");
        return this;
    }

    /**
     * Set the priority.
     *
     * @param priority the priority
     * @return this builder
     */
    public ActionContextBuilder withPriority(String priority) {
        this.priority = Objects.requireNonNull(priority, "priority");
        return this;
    }

    @Override
    public ExecutionContext build() {
        if (!isValid()) {
            throw new IllegalArgumentException("Invalid ActionContextBuilder state: " + getValidationErrors());
        }
        ExecutionContextBuilder builder = ExecutionContext.builder().withContextId(correlationId).withProtocol(protocol)
                .withClientId(clientId).withSessionId(sessionId).withAuthContext(authContext)
                .withExecutionStartTime(executionStartTime).withCorrelationId(correlationId)
                .withValues(protocolContext);
        if (priority != null && !priority.isEmpty()) {
            builder.withPriority(priority);
        }
        return builder.build();
    }

    @Override
    protected void validate() {
        super.validate();
        // Additional validation specific to ExecutionContext
        if (protocol.isBlank()) {
            throw new IllegalArgumentException("protocol must not be blank");
        }
        if (clientId.isBlank()) {
            throw new IllegalArgumentException("clientId must not be blank");
        }
        if (correlationId.isBlank()) {
            throw new IllegalArgumentException("correlationId must not be blank");
        }
    }

    @Override
    protected void doReset() {
        super.doReset();
        protocol = "";
        clientId = "";
        sessionId = "";
        authContext = null;
        protocolContext = Map.of();
        executionStartTime = System.currentTimeMillis();
        correlationId = "";
        priority = "";
    }

    /**
     * Create a new ActionContextBuilder instance.
     *
     * @return a new builder instance
     */
    public static ActionContextBuilder builder() {
        return new ActionContextBuilder();
    }

    /**
     * Create a builder from an existing ExecutionContext.
     *
     * @param context the existing context
     * @return a builder with values from the existing context
     */
    public static ActionContextBuilder builder(ExecutionContext context) {
        Objects.requireNonNull(context, "context");
        ActionContextBuilder builder = builder().withProtocol(context.getProtocol()).withClientId(context.getClientId())
                .withSessionId(context.getSessionId()).withAuthContext(context.getAuthContext())
                .withExecutionStartTime(context.getExecutionStartTime()).withCorrelationId(context.getCorrelationId());
        context.getPriority().ifPresent(builder::withPriority);
        return builder;
    }

    // Getters for the ExecutionContext constructor
    public String getProtocol() {
        return protocol;
    }

    public String getClientId() {
        return clientId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public @Nullable AuthenticationContext getAuthContext() {
        return authContext;
    }

    public Map<String, Object> getProtocolContext() {
        return protocolContext;
    }

    public long getExecutionStartTime() {
        return executionStartTime;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getPriority() {
        return priority;
    }
}
