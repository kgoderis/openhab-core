package org.openhab.core.ai.tool.services;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.context.ToolContext;
import org.openhab.core.ai.tool.api.Tool;
import org.openhab.core.ai.tool.api.ToolResult;

/**
 * Service interface for tool operations.
 * 
 * Provides high-level tool management and execution capabilities.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public interface ToolService {

    /**
     * Register a tool.
     * 
     * @param tool the tool to register
     * @return true if registration was successful
     */
    boolean registerTool(Tool tool);

    /**
     * Unregister a tool.
     * 
     * @param toolId the tool ID
     * @return true if unregistration was successful
     */
    boolean unregisterTool(String toolId);

    /**
     * Get a tool by ID.
     * 
     * @param toolId the tool ID
     * @return the tool or null if not found
     */
    @Nullable
    Tool getTool(String toolId);

    /**
     * Get all registered tools.
     * 
     * @return map of tool ID to tool
     */
    Map<String, Tool> getAllTools();

    /**
     * Execute a tool.
     * 
     * @param toolId the tool ID
     * @param context the tool context
     * @return the execution result
     */
    ToolResult executeTool(String toolId, ToolContext context);

    /**
     * Validate a tool.
     * 
     * @param toolId the tool ID
     * @return true if the tool is valid
     */
    boolean validateTool(String toolId);

    /**
     * Get tool statistics.
     * 
     * @return map of tool statistics
     */
    Map<String, Object> getToolStatistics();

    /**
     * Get service health status.
     * 
     * @return true if the service is healthy
     */
    boolean isHealthy();

    /**
     * Get service version.
     * 
     * @return the service version
     */
    String getVersion();
}
