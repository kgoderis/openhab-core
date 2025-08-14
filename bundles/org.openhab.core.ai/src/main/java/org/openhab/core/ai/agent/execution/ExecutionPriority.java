package org.openhab.core.ai.agent.execution;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public enum ExecutionPriority {
    LOW(1),
    MEDIUM(2),
    HIGH(3),
    CRITICAL(4);

    private final int value;

    ExecutionPriority(int value) {
        this.value = value;
    }

    public int getValue() { return value; }
}


