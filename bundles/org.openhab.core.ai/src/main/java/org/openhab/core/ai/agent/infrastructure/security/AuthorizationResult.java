package org.openhab.core.ai.agent.infrastructure.security;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Authorization result for agent action authorization
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class AuthorizationResult {
    private final String agentId;
    private final String action;
    private final String resource;
    private final Instant timestamp;
    private final boolean granted;
    private final String reason;

    private AuthorizationResult(String agentId, String action, String resource, Instant timestamp, boolean granted,
            String reason) {
        this.agentId = agentId;
        this.action = action;
        this.resource = resource;
        this.timestamp = timestamp;
        this.granted = granted;
        this.reason = reason;
    }

    // Getters
    public String getAgentId() {
        return agentId;
    }

    public String getAction() {
        return action;
    }

    public String getResource() {
        return resource;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public boolean isGranted() {
        return granted;
    }

    public String getReason() {
        return reason;
    }

    public static AuthorizationResult granted(String agentId, String action, String resource, Instant timestamp) {
        return new AuthorizationResult(agentId, action, resource, timestamp, true, "Action authorized");
    }

    public static AuthorizationResult denied(String reason) {
        return new AuthorizationResult(null, null, null, null, false, reason);
    }
}
