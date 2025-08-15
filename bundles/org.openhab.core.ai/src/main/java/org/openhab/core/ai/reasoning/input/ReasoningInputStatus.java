package org.openhab.core.ai.reasoning.input;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public enum ReasoningInputStatus {
    PENDING,
    PROCESSING,
    ROUTED,
    FAILED,
    COMPLETED
}
