package org.openhab.core.ai.action.api;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.auth.AuthenticationContext;

/**
 * Context for action execution, providing access to protocol-specific information.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ActionContext {

    // Protocol identification
    private final String protocol; // "mcp" or "a2a"
    private final String clientId;
    private final String sessionId;

    // Authentication and authorization
    private final @Nullable AuthenticationContext authContext;

    // Protocol-specific context
    private final Map<String, Object> protocolContext;

    // Execution metadata
    private final long executionStartTime;
    private final String correlationId;
    private final String priority; // For A2A protocol priority levels

    /* package */ ActionContext(ActionContextBuilder builder) {
        this.protocol = builder.protocol;
        this.clientId = builder.clientId;
        this.sessionId = builder.sessionId;
        this.authContext = builder.authContext != null ? builder.authContext
                : new AuthenticationContext("default", "none", Map.of(), Set.of(), Instant.now(), null,
                        "default-session");
        this.protocolContext = builder.protocolContext;
        this.executionStartTime = builder.executionStartTime != 0 ? builder.executionStartTime
                : System.currentTimeMillis();
        this.correlationId = builder.correlationId;
        this.priority = builder.priority;
    }

    // Getters
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

    public Optional<String> getPriority() {
        return Optional.ofNullable(priority);
    }

    public static ActionContextBuilder builder() {
        return new ActionContextBuilder();
    }
}
