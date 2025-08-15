package org.openhab.core.ai.agent.execution;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class SchemaCompatibilityResult {
    private final boolean compatible;
    private final String message;

    public SchemaCompatibilityResult(boolean compatible, String message) {
        this.compatible = compatible;
        this.message = message;
    }

    public boolean isCompatible() {
        return compatible;
    }

    public String getMessage() {
        return message;
    }
}
