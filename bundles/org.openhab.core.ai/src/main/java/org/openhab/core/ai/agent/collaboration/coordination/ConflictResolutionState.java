package org.openhab.core.ai.agent.collaboration.coordination;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Conflict resolution state values extracted from AgentCoordinationManager.
 */
@NonNullByDefault
public enum ConflictResolutionState {
    INITIATED,
    IN_PROGRESS,
    RESOLVED,
    FAILED,
    ESCALATED
}


