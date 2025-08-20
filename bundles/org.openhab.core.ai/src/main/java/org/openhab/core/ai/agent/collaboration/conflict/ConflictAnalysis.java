package org.openhab.core.ai.agent.collaboration.conflict;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.agent.collaboration.ConflictType;

@NonNullByDefault
public class ConflictAnalysis {
    private final boolean conflictDetected;
    private final ConflictType conflictType;
    private final ConflictPriority priority;
    private final int severity;
    private final String description;

    public ConflictAnalysis(boolean conflictDetected, ConflictType conflictType, ConflictPriority priority,
            int severity, String description) {
        this.conflictDetected = conflictDetected;
        this.conflictType = conflictType;
        this.priority = priority;
        this.severity = severity;
        this.description = description;
    }

    public boolean isConflictDetected() {
        return conflictDetected;
    }

    public ConflictType getConflictType() {
        return conflictType;
    }

    public ConflictPriority getPriority() {
        return priority;
    }

    public int getSeverity() {
        return severity;
    }

    public String getDescription() {
        return description;
    }
}
