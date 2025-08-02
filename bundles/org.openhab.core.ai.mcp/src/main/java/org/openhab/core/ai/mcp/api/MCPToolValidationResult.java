package org.openhab.core.ai.mcp.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Result of MCP tool parameter validation.
 * 
 * @author AI Assistant
 * @since 1.0.0
 */
@NonNullByDefault
public class MCPToolValidationResult {
    private final boolean valid;
    private final @Nullable String message;

    private MCPToolValidationResult(boolean valid, @Nullable String message) {
        this.valid = valid;
        this.message = message;
    }

    public static MCPToolValidationResult valid() {
        return new MCPToolValidationResult(true, null);
    }

    public static MCPToolValidationResult invalid(String message) {
        return new MCPToolValidationResult(false, message);
    }

    public boolean isValid() {
        return valid;
    }

    public @Nullable String getMessage() {
        return message;
    }
}
