package org.openhab.core.ai.agent.collaboration.coordination;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Coordination state values extracted from AgentCoordinationManager.
 */
@NonNullByDefault
public enum CoordinationState {
    INITIATED,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    CANCELLED
}
