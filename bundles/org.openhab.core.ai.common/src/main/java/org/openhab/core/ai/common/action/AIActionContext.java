package org.openhab.core.ai.common.action;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.auth.AIAuthenticationContext;

/**
 * Context for AI action execution, providing access to protocol-specific information.
 * 
 * 
 */
@NonNullByDefault
public class AIActionContext {

    // Protocol identification
    private final String protocol; // "mcp" or "a2a"
    private final String clientId;
    private final String sessionId;

    // Authentication and authorization
    private final @Nullable AIAuthenticationContext authContext;

    // Protocol-specific context
    private final Map<String, Object> protocolContext;

    // Execution metadata
    private final long executionStartTime;
    private final String correlationId;
    private final String priority; // For A2A protocol priority levels

    private AIActionContext(Builder builder) {
        this.protocol = builder.protocol;
        this.clientId = builder.clientId;
        this.sessionId = builder.sessionId;
        this.authContext = builder.authContext != null ? builder.authContext
                : new AIAuthenticationContext("default", "none", Map.of(), Set.of(), Instant.now(), null,
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

    public @Nullable AIAuthenticationContext getAuthContext() {
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

    /**
     * Builder for AIActionContext.
     */
    public static class Builder {
        private String protocol = "";
        private String clientId = "";
        private String sessionId = "";
        private @Nullable AIAuthenticationContext authContext;
        private Map<String, Object> protocolContext = Map.of();
        private long executionStartTime = System.currentTimeMillis();
        private String correlationId = "";
        private String priority = "";

        public Builder protocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder authContext(AIAuthenticationContext authContext) {
            this.authContext = authContext;
            return this;
        }

        public Builder protocolContext(Map<String, Object> protocolContext) {
            this.protocolContext = protocolContext != null ? protocolContext : Map.of();
            return this;
        }

        public Builder executionStartTime(long executionStartTime) {
            this.executionStartTime = executionStartTime;
            return this;
        }

        public Builder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public Builder priority(String priority) {
            this.priority = priority;
            return this;
        }

        public AIActionContext build() {
            return new AIActionContext(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
