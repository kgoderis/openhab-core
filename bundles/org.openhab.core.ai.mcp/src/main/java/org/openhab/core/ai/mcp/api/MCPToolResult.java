package org.openhab.core.ai.mcp.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Result of MCP tool execution.
 * 
 * @author AI Assistant
 * @since 1.0.0
 */
@NonNullByDefault
public class MCPToolResult {
    private final String toolId;
    private final boolean success;
    private final @Nullable Object content;
    private final @Nullable String error;
    private final long executionTimeMs;

    private MCPToolResult(String toolId, boolean success, @Nullable Object content, @Nullable String error,
            long executionTimeMs) {
        this.toolId = toolId;
        this.success = success;
        this.content = content;
        this.error = error;
        this.executionTimeMs = executionTimeMs;
    }

    public static MCPToolResult successJson(String toolId, Object content, long executionTimeMs) {
        return new MCPToolResult(toolId, true, content, null, executionTimeMs);
    }

    public static MCPToolResult error(String toolId, String error, long executionTimeMs) {
        return new MCPToolResult(toolId, false, null, error, executionTimeMs);
    }

    public String getToolId() {
        return toolId;
    }

    public boolean isSuccess() {
        return success;
    }

    public @Nullable Object getContent() {
        return content;
    }

    public @Nullable String getError() {
        return error;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }
}
