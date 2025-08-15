package org.openhab.core.ai.agent.lifecycle;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class SecurityStatistics {
    private final int activeClients;
    private final int failedAttempts;
    private final int blockedClients;
    private final boolean authenticationEnabled;
    private final boolean requestValidationEnabled;
    private final int maxConnections;
    private final int rateLimitPerMinute;
    private final int activeSessions;

    public SecurityStatistics(int activeClients, int failedAttempts, int blockedClients, boolean authenticationEnabled,
            boolean requestValidationEnabled, int maxConnections, int rateLimitPerMinute, int activeSessions) {
        this.activeClients = activeClients;
        this.failedAttempts = failedAttempts;
        this.blockedClients = blockedClients;
        this.authenticationEnabled = authenticationEnabled;
        this.requestValidationEnabled = requestValidationEnabled;
        this.maxConnections = maxConnections;
        this.rateLimitPerMinute = rateLimitPerMinute;
        this.activeSessions = activeSessions;
    }

    public int getActiveClients() {
        return activeClients;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public int getBlockedClients() {
        return blockedClients;
    }

    public boolean isAuthenticationEnabled() {
        return authenticationEnabled;
    }

    public boolean isRequestValidationEnabled() {
        return requestValidationEnabled;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public int getRateLimitPerMinute() {
        return rateLimitPerMinute;
    }

    public int getActiveSessions() {
        return activeSessions;
    }
}
