package org.openhab.core.ai.agent.collaboration.conflict;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Conflict resolution statistics snapshot.
 *
 * Provides counts and totals for conflict processing.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ConflictResolutionStatistics {
    private final long totalConflictsDetected;
    private final long totalConflictsResolved;
    private final long totalConflictsEscalated;
    private final long totalConflictsPrevented;
    private final long totalResolutionTime;
    private final int activeConflicts;
    private final int conflictHistorySize;
    private final int resolutionStrategies;
    private final int mediators;

    public ConflictResolutionStatistics(long totalConflictsDetected, long totalConflictsResolved,
            long totalConflictsEscalated, long totalConflictsPrevented, long totalResolutionTime, int activeConflicts,
            int conflictHistorySize, int resolutionStrategies, int mediators) {
        this.totalConflictsDetected = totalConflictsDetected;
        this.totalConflictsResolved = totalConflictsResolved;
        this.totalConflictsEscalated = totalConflictsEscalated;
        this.totalConflictsPrevented = totalConflictsPrevented;
        this.totalResolutionTime = totalResolutionTime;
        this.activeConflicts = activeConflicts;
        this.conflictHistorySize = conflictHistorySize;
        this.resolutionStrategies = resolutionStrategies;
        this.mediators = mediators;
    }

    public long getTotalConflictsDetected() {
        return totalConflictsDetected;
    }

    public long getTotalConflictsResolved() {
        return totalConflictsResolved;
    }

    public long getTotalConflictsEscalated() {
        return totalConflictsEscalated;
    }

    public long getTotalConflictsPrevented() {
        return totalConflictsPrevented;
    }

    public long getTotalResolutionTime() {
        return totalResolutionTime;
    }

    public int getActiveConflicts() {
        return activeConflicts;
    }

    public int getConflictHistorySize() {
        return conflictHistorySize;
    }

    public int getResolutionStrategies() {
        return resolutionStrategies;
    }

    public int getMediators() {
        return mediators;
    }
}
