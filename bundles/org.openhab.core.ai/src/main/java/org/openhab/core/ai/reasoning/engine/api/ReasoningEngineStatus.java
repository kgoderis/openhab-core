package org.openhab.core.ai.reasoning.engine.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public enum ReasoningEngineStatus {
    ACTIVE,
    DEGRADED,
    MAINTENANCE,
    SHUTDOWN,
    ERROR
}
