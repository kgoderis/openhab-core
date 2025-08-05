package org.openhab.core.ai.api.tool;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception thrown by MCP tools during execution.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ToolException extends Exception {
    private static final long serialVersionUID = 1L;

    private final String toolId;
    private final ToolErrorCode errorCode;

    public enum ToolErrorCode {
        INVALID_PARAMETER,
        SERVICE_UNAVAILABLE,
        RESOURCE_NOT_FOUND,
        ACCESS_DENIED,
        EXECUTION_ERROR,
        TIMEOUT
    }

    public ToolException(String toolId, String message, ToolErrorCode errorCode) {
        super(message);
        this.toolId = toolId;
        this.errorCode = errorCode;
    }

    public ToolException(String toolId, String message, Throwable cause, ToolErrorCode errorCode) {
        super(message, cause);
        this.toolId = toolId;
        this.errorCode = errorCode;
    }

    public String getToolId() {
        return toolId;
    }

    public ToolErrorCode getErrorCode() {
        return errorCode;
    }
}
