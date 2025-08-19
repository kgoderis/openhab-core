package org.openhab.core.ai.common.security;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Agent-specific security statistics implementation.
 * 
 * <p>
 * This class provides security statistics specific to agent operations,
 * including client management, authentication, and session tracking.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class AgentSecurityStatistics extends BaseSecurityStatistics {

    private final int activeClients;
    private final int blockedClients;
    private final boolean authenticationEnabled;
    private final boolean requestValidationEnabled;
    private final int maxConnections;
    private final int rateLimitPerMinute;
    private final int activeSessions;

    /**
     * Constructor for AgentSecurityStatistics.
     * 
     * @param totalOperations Total number of security operations
     * @param successfulOperations Number of successful security operations
     * @param failedOperations Number of failed security operations
     * @param securityViolations Number of security violations
     * @param lastOperationTime Timestamp of last security operation
     * @param activeClients Number of active clients
     * @param blockedClients Number of blocked clients
     * @param authenticationEnabled Whether authentication is enabled
     * @param requestValidationEnabled Whether request validation is enabled
     * @param maxConnections Maximum allowed connections
     * @param rateLimitPerMinute Rate limit per minute
     * @param activeSessions Number of active sessions
     */
    public AgentSecurityStatistics(long totalOperations, long successfulOperations, long failedOperations,
            long securityViolations, @Nullable Instant lastOperationTime, int activeClients, int blockedClients,
            boolean authenticationEnabled, boolean requestValidationEnabled, int maxConnections, int rateLimitPerMinute,
            int activeSessions) {
        super(totalOperations, successfulOperations, failedOperations, securityViolations, lastOperationTime);
        this.activeClients = activeClients;
        this.blockedClients = blockedClients;
        this.authenticationEnabled = authenticationEnabled;
        this.requestValidationEnabled = requestValidationEnabled;
        this.maxConnections = maxConnections;
        this.rateLimitPerMinute = rateLimitPerMinute;
        this.activeSessions = activeSessions;
    }

    /**
     * Get the number of active clients.
     * 
     * @return active clients count
     */
    public int getActiveClients() {
        return activeClients;
    }

    /**
     * Get the number of blocked clients.
     * 
     * @return blocked clients count
     */
    public int getBlockedClients() {
        return blockedClients;
    }

    /**
     * Check if authentication is enabled.
     * 
     * @return true if authentication is enabled
     */
    public boolean isAuthenticationEnabled() {
        return authenticationEnabled;
    }

    /**
     * Check if request validation is enabled.
     * 
     * @return true if request validation is enabled
     */
    public boolean isRequestValidationEnabled() {
        return requestValidationEnabled;
    }

    /**
     * Get the maximum allowed connections.
     * 
     * @return maximum connections
     */
    public int getMaxConnections() {
        return maxConnections;
    }

    /**
     * Get the rate limit per minute.
     * 
     * @return rate limit per minute
     */
    public int getRateLimitPerMinute() {
        return rateLimitPerMinute;
    }

    /**
     * Get the number of active sessions.
     * 
     * @return active sessions count
     */
    public int getActiveSessions() {
        return activeSessions;
    }

    // Backward-compatible aliases for existing code
    /**
     * Get failed attempts (alias for getFailedOperations).
     * 
     * @return failed attempts count
     */
    public int getFailedAttempts() {
        return (int) getFailedOperations();
    }
}
