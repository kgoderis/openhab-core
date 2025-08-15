package org.openhab.core.ai.tool.error;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public enum DefaultErrorRecoveryServiceCircuitBreakerState {
    CLOSED,
    OPEN,
    HALF_OPEN
}
