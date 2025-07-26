package org.openhab.core.ai.mcp.api;

public class MCPToolResult {
    private final String toolId;
    private final boolean success;
    private final Object content;
    private final String error;
    private final long executionTimeMs;

    private MCPToolResult(String toolId, boolean success, Object content, String error, long executionTimeMs) {
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

    public Object getContent() {
        return content;
    }

    public String getError() {
        return error;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }
}
