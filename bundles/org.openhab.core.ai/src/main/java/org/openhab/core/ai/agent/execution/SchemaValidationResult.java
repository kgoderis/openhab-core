package org.openhab.core.ai.agent.execution;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Schema validation result.
 */
@NonNullByDefault
public class SchemaValidationResult {
    private final boolean valid;
    private final String message;

    public SchemaValidationResult(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }

    public boolean isValid() { return valid; }
    public String getMessage() { return message; }
}


