package org.openhab.core.ai.tool.validation.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Result of MCP tool parameter validation.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ToolValidationResult {
    private final boolean valid;
    private final @Nullable String message;

    private ToolValidationResult(boolean valid, @Nullable String message) {
        this.valid = valid;
        this.message = message;
    }

    public static ToolValidationResult valid() {
        return new ToolValidationResult(true, null);
    }

    public static ToolValidationResult invalid(String message) {
        return new ToolValidationResult(false, message);
    }

    public boolean isValid() {
        return valid;
    }

    public @Nullable String getMessage() {
        return message;
    }
}
