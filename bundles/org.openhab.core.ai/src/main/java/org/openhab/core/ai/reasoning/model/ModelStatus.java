package org.openhab.core.ai.reasoning.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public enum ModelStatus {
    AVAILABLE,
    DEGRADED,
    MAINTENANCE,
    UNAVAILABLE
}
