package org.openhab.core.ai.agent.execution;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public enum TaskOrchestrationStateState {
    PENDING,
    RUNNING,
    PAUSED,
    COMPLETED,
    ERROR,
    CANCELLED
}
