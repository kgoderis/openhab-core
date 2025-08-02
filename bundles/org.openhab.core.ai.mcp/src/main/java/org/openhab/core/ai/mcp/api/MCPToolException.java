package org.openhab.core.ai.mcp.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception thrown by MCP tools during execution.
 * 
 * @author AI Assistant
 * @since 1.0.0
 */
@NonNullByDefault
public class MCPToolException extends Exception {
    private static final long serialVersionUID = 1L;

    private final String toolId;
    private final MCPToolErrorCode errorCode;

    public enum MCPToolErrorCode {
        INVALID_PARAMETER,
        SERVICE_UNAVAILABLE,
        RESOURCE_NOT_FOUND,
        ACCESS_DENIED,
        EXECUTION_ERROR,
        TIMEOUT
    }

    public MCPToolException(String toolId, String message, MCPToolErrorCode errorCode) {
        super(message);
        this.toolId = toolId;
        this.errorCode = errorCode;
    }

    public MCPToolException(String toolId, String message, Throwable cause, MCPToolErrorCode errorCode) {
        super(message, cause);
        this.toolId = toolId;
        this.errorCode = errorCode;
    }

    public String getToolId() {
        return toolId;
    }

    public MCPToolErrorCode getErrorCode() {
        return errorCode;
    }
}
