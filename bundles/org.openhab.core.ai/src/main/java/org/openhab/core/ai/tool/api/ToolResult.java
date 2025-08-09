package org.openhab.core.ai.tool.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Result of MCP tool execution.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ToolResult {
    private final String toolId;
    private final boolean success;
    private final @Nullable Object content;
    private final @Nullable String error;
    private final long executionTimeMs;

    private ToolResult(String toolId, boolean success, @Nullable Object content, @Nullable String error,
            long executionTimeMs) {
        this.toolId = toolId;
        this.success = success;
        this.content = content;
        this.error = error;
        this.executionTimeMs = executionTimeMs;
    }

    public static ToolResult successJson(String toolId, Object content, long executionTimeMs) {
        return new ToolResult(toolId, true, content, null, executionTimeMs);
    }

    public static ToolResult error(String toolId, String error, long executionTimeMs) {
        return new ToolResult(toolId, false, null, error, executionTimeMs);
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
