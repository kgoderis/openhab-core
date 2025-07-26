package org.openhab.core.ai.mcp.api;

public class MCPToolValidationResult {
    private final boolean valid;
    private final String message;

    private MCPToolValidationResult(boolean valid, String message) {
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

    public String getMessage() {
        return message;
    }
}
