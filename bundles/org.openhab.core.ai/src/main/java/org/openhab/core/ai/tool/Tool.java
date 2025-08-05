package org.openhab.core.ai.tool.internal;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Interface for MCP Tools.
 * 
 * This interface defines the contract for MCP tools that can be registered
 * with the ToolRegistry and used by the MCP server.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface Tool {

    /**
     * Get the tool ID.
     * 
     * @return the tool ID
     */
    String getId();

    /**
     * Get the tool name.
     * 
     * @return the tool name
     */
    String getName();

    /**
     * Get the tool description.
     * 
     * @return the tool description
     */
    String getDescription();

    /**
     * Get the input schema.
     * 
     * @return the input schema
     */
    Map<String, Object> getInputSchema();

    /**
     * Get the output schema.
     * 
     * @return the output schema
     */
    Map<String, Object> getOutputSchema();
}
