package org.openhab.core.ai.reasoning.metrics;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Performance metrics for the reasoning orchestration service.
 *
 * Provides a lightweight snapshot of active and total sessions to help
 * monitoring and optimization components.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public class OrchestrationMetrics {
    private final int activeSessions;
    private final long totalSessions;

    public OrchestrationMetrics(int activeSessions, long totalSessions) {
        this.activeSessions = activeSessions;
        this.totalSessions = totalSessions;
    }

    public int getActiveSessions() {
        return activeSessions;
    }

    public long getTotalSessions() {
        return totalSessions;
    }
}
