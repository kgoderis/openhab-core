package org.openhab.core.ai.mcp.api;

import java.util.Map;

public interface MCPTool {
    String getToolId();

    String getToolName();

    String getDescription();

    Map<String, Object> getSchema();

    MCPToolValidationResult validateParameters(Map<String, Object> parameters);

    MCPToolResult execute(Map<String, Object> parameters, MCPToolContext context) throws MCPToolException;

    MCPToolMetadata getMetadata();
}
