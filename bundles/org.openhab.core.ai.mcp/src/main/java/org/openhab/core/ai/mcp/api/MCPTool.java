package org.openhab.core.ai.mcp.api;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Interface for MCP tools that can be executed by the MCP server.
 * 
 * @author AI Assistant
 * @since 1.0.0
 */
@NonNullByDefault
public interface MCPTool {
    String getToolId();

    String getToolName();

    String getDescription();

    Map<String, Object> getSchema();

    MCPToolValidationResult validateParameters(Map<String, Object> parameters);

    MCPToolResult execute(Map<String, Object> parameters, MCPToolContext context) throws MCPToolException;

    MCPToolMetadata getMetadata();
}
