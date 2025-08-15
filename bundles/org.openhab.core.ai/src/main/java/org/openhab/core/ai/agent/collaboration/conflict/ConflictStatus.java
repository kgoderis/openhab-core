package org.openhab.core.ai.agent.collaboration.conflict;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public enum ConflictStatus {
    DETECTED,
    RESOLVING,
    RESOLVED,
    ESCALATED,
    FAILED
}
