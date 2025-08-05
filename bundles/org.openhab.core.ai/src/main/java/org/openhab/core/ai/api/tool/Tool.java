package org.openhab.core.ai.api.tool;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Interface for MCP tools that can be executed by the MCP server.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface Tool {
    String getToolId();

    String getToolName();

    String getDescription();

    Map<String, Object> getSchema();

    ToolValidationResult validateParameters(Map<String, Object> parameters);

    ToolResult execute(Map<String, Object> parameters, ToolContext context) throws ToolException;

    ToolMetadata getMetadata();
}
