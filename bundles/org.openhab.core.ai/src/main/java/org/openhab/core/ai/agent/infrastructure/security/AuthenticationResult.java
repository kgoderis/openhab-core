package org.openhab.core.ai.agent.infrastructure.security;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Authentication result for agent authentication
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class AuthenticationResult {
    private final String agentId;
    private final String token;
    private final Instant timestamp;
    private final boolean success;
    private final String errorMessage;

    private AuthenticationResult(String agentId, String token, Instant timestamp) {
        this.agentId = agentId;
        this.token = token;
        this.timestamp = timestamp;
        this.success = true;
        this.errorMessage = null;
    }

    private AuthenticationResult(String errorMessage) {
        this.agentId = null;
        this.token = null;
        this.timestamp = null;
        this.success = false;
        this.errorMessage = errorMessage;
    }

    // Getters
    public String getAgentId() {
        return agentId;
    }

    public String getToken() {
        return token;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public static AuthenticationResult success(String agentId, String token, Instant timestamp) {
        return new AuthenticationResult(agentId, token, timestamp);
    }

    public static AuthenticationResult failure(String errorMessage) {
        return new AuthenticationResult(errorMessage);
    }
}
