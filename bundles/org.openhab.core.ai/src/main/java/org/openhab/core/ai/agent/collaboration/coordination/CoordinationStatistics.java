package org.openhab.core.ai.agent.collaboration.coordination;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Coordination statistics DTO extracted from {@link AgentCoordinationManager}.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class CoordinationStatistics {
    private final long totalSessions;
    private final long totalConflictResolutions;
    private final long totalContextSharing;
    private final long totalProtocolExecutions;
    private final int activeSessions;
    private final int activeConflictSessions;
    private final int sharedContexts;
    private final int registeredProtocols;

    public CoordinationStatistics(long totalSessions, long totalConflictResolutions, long totalContextSharing,
            long totalProtocolExecutions, int activeSessions, int activeConflictSessions, int sharedContexts,
            int registeredProtocols) {
        this.totalSessions = totalSessions;
        this.totalConflictResolutions = totalConflictResolutions;
        this.totalContextSharing = totalContextSharing;
        this.totalProtocolExecutions = totalProtocolExecutions;
        this.activeSessions = activeSessions;
        this.activeConflictSessions = activeConflictSessions;
        this.sharedContexts = sharedContexts;
        this.registeredProtocols = registeredProtocols;
    }

    public long getTotalSessions() {
        return totalSessions;
    }

    public long getTotalConflictResolutions() {
        return totalConflictResolutions;
    }

    public long getTotalContextSharing() {
        return totalContextSharing;
    }

    public long getTotalProtocolExecutions() {
        return totalProtocolExecutions;
    }

    public int getActiveSessions() {
        return activeSessions;
    }

    public int getActiveConflictSessions() {
        return activeConflictSessions;
    }

    public int getSharedContexts() {
        return sharedContexts;
    }

    public int getRegisteredProtocols() {
        return registeredProtocols;
    }
}


