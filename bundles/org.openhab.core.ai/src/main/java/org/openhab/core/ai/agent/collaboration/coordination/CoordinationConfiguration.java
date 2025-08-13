package org.openhab.core.ai.agent.collaboration.coordination;

import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Coordination configuration extracted from {@link AgentCoordinationManager}.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class CoordinationConfiguration {
    private Duration sessionTimeout = Duration.ofMinutes(30);
    private Duration conflictResolutionTimeout = Duration.ofMinutes(15);
    private Duration contextRetentionPeriod = Duration.ofDays(7);
    private int maxConcurrentSessions = 100;
    private int maxConcurrentConflicts = 50;

    public Duration getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(Duration sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public Duration getConflictResolutionTimeout() {
        return conflictResolutionTimeout;
    }

    public void setConflictResolutionTimeout(Duration conflictResolutionTimeout) {
        this.conflictResolutionTimeout = conflictResolutionTimeout;
    }

    public Duration getContextRetentionPeriod() {
        return contextRetentionPeriod;
    }

    public void setContextRetentionPeriod(Duration contextRetentionPeriod) {
        this.contextRetentionPeriod = contextRetentionPeriod;
    }

    public int getMaxConcurrentSessions() {
        return maxConcurrentSessions;
    }

    public void setMaxConcurrentSessions(int maxConcurrentSessions) {
        this.maxConcurrentSessions = maxConcurrentSessions;
    }

    public int getMaxConcurrentConflicts() {
        return maxConcurrentConflicts;
    }

    public void setMaxConcurrentConflicts(int maxConcurrentConflicts) {
        this.maxConcurrentConflicts = maxConcurrentConflicts;
    }
}


