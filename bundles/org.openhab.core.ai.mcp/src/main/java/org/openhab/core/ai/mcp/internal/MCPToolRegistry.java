package org.openhab.core.ai.mcp.internal;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.action.AIActionRegistry;
import org.openhab.core.ai.common.api.action.AIAction;
import org.openhab.core.ai.mcp.api.MCPTool;
import org.openhab.core.service.ReadyMarker;
import org.openhab.core.service.ReadyService;
import org.openhab.core.service.ReadyService.ReadyTracker;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.modelcontextprotocol.server.McpServerFeatures;

/**
 * Registry for MCP tools.
 * 
 * @author AI Assistant
 * @since 1.0.0
 */
@NonNullByDefault
public class MCPToolRegistry implements ReadyTracker {

    private static final Logger logger = LoggerFactory.getLogger(MCPToolRegistry.class);

    // Ready markers for MCP tool registry
    public static final ReadyMarker MCP_TOOLS_READY = new ReadyMarker("mcp", "tools");

    @Reference
    private @Nullable ReadyService readyService;

    private @Nullable BundleContext bundleContext;
    private @Nullable AIActionRegistry aiActionRegistry;
    private final Map<String, MCPToolAdapter> toolAdapters = new ConcurrentHashMap<>();
    private final Map<String, MCPTool> tools = new ConcurrentHashMap<>();
    private boolean toolsPopulated = false;

    /**
     * Activate the component.
     * 
     * @param bundleContext the bundle context
     */
    @Activate
    public void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        logger.info("MCP Tool Registry activated");

        // Register as a tracker if readyService is available
        if (readyService != null) {
            readyService.registerTracker(this);
        }

        discoverTools();
        createToolsFromActions();

        // Mark tools as ready even if no AIActions are available yet
        // This allows the server to start with discovered tools
        markToolsReady();

        logger.info("MCP Tool Registry started with {} tools", tools.size());
    }

    /**
     * Deactivate the component.
     */
    @Deactivate
    public void deactivate() {
        logger.info("MCP Tool Registry deactivated");

        // Unregister tracker and unmark ready if readyService is available
        if (readyService != null) {
            readyService.unregisterTracker(this);
            readyService.unmarkReady(MCP_TOOLS_READY);
        }

        toolAdapters.clear();
        tools.clear();
        this.bundleContext = null; // This is intentional - clearing the reference
        this.aiActionRegistry = null; // This is intentional - clearing the reference
    }

    /**
     * Bind the AIAction registry.
     * 
     * @param aiActionRegistry the AIAction registry
     */
    @Reference
    public void bindAIActionRegistry(AIActionRegistry aiActionRegistry) {
        this.aiActionRegistry = aiActionRegistry;
        logger.info("AIAction Registry bound to MCP Tool Registry");
        // Create tools from existing actions
        if (this.bundleContext != null) {
            createToolsFromActions();
            markToolsReady();
        }
    }

    /**
     * Unbind the AIAction registry.
     * 
     * @param aiActionRegistry the AIAction registry
     */
    public void unbindAIActionRegistry(AIActionRegistry aiActionRegistry) {
        this.aiActionRegistry = null;
        logger.info("AIAction Registry unbound from MCP Tool Registry");
    }

    /**
     * Create MCP tools from AIActions in the AIAction registry.
     */
    private void createToolsFromActions() {
        if (aiActionRegistry == null) {
            logger.debug("AIAction Registry not yet available, skipping tool creation");
            return;
        }

        Map<String, AIAction> actions = aiActionRegistry.getAllActions();
        for (AIAction action : actions.values()) {
            createToolFromAction(action);
        }

        // Mark tools as ready after creating tools from actions
        markToolsReady();
    }

    /**
     * Stop the tool registry.
     */
    public void stop() {
        logger.info("Stopping MCP Tool Registry");
        toolAdapters.clear();
        tools.clear();
        logger.info("MCP Tool Registry stopped");
    }

    /**
     * Discover and register available MCP tools.
     */
    private void discoverTools() {
        try {
            // Find all MCPTool services
            Collection<ServiceReference<MCPTool>> refs = bundleContext.getServiceReferences(MCPTool.class, null);
            if (refs != null) {
                for (ServiceReference<MCPTool> ref : refs) {
                    MCPTool tool = bundleContext.getService(ref);
                    if (tool != null) {
                        registerTool(tool);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error discovering MCP tools", e);
        }
    }

    /**
     * Register a tool with the registry.
     * 
     * @param tool The tool to register
     */
    public void registerTool(MCPTool tool) {
        // Validate input parameter
        if (tool == null) {
            return; // Cannot register null tool
        }

        String toolId = tool.getToolId();
        tools.put(toolId, tool);

        // Create adapter for AIAction-based tools
        if (tool instanceof MCPToolAdapter) {
            toolAdapters.put(toolId, (MCPToolAdapter) tool);
        }

        logger.info("Registered MCP tool: {} ({})", toolId, tool.getToolName());

        // Mark tools as ready after registering a tool
        markToolsReady();
    }

    /**
     * Create an MCP tool from an AIAction.
     * 
     * @param action the AIAction to convert
     */
    public void createToolFromAction(AIAction action) {
        // Validate input parameter
        if (action == null) {
            return; // Cannot create tool from null action
        }

        MCPToolAdapter adapter = new MCPToolAdapter(action);
        String toolId = adapter.getToolId();

        tools.put(toolId, adapter);
        toolAdapters.put(toolId, adapter);

        logger.info("Created MCP tool from AIAction: {} ({})", toolId, action.getActionName());

        // Mark tools as ready after creating a tool from action
        markToolsReady();
    }

    /**
     * Unregister a tool from the registry.
     * 
     * @param toolId The tool ID to unregister
     */
    public void unregisterTool(String toolId) {
        // Validate input parameter
        if (toolId == null) {
            return; // Cannot unregister tool with null ID
        }

        tools.remove(toolId);
        toolAdapters.remove(toolId);
        logger.info("Unregistered MCP tool: {}", toolId);
    }

    /**
     * Get a tool by ID.
     * 
     * @param toolId Tool identifier
     * @return Tool or null if not found
     */
    public @Nullable MCPTool getTool(String toolId) {
        // Validate input parameter
        if (toolId == null) {
            return null; // Cannot get tool with null ID
        }

        return tools.get(toolId);
    }

    /**
     * Get a tool adapter by ID.
     * 
     * @param toolId Tool identifier
     * @return Tool adapter or null if not found
     */
    public @Nullable MCPToolAdapter getToolAdapter(String toolId) {
        // Validate input parameter
        if (toolId == null) {
            return null; // Cannot get tool adapter with null ID
        }

        return toolAdapters.get(toolId);
    }

    /**
     * Get all registered tools.
     * 
     * @return Map of all tools
     */
    public Map<String, MCPTool> getAllTools() {
        return new ConcurrentHashMap<>(tools);
    }

    /**
     * Get all tool adapters.
     * 
     * @return Map of all tool adapters
     */
    public Map<String, MCPToolAdapter> getAllToolAdapters() {
        return new ConcurrentHashMap<>(toolAdapters);
    }

    /**
     * Get the number of registered tools.
     * 
     * @return Number of tools
     */
    public int getToolCount() {
        return tools.size();
    }

    /**
     * Check if a tool is registered.
     * 
     * @param toolId Tool identifier
     * @return true if registered
     */
    public boolean isToolRegistered(String toolId) {
        // Validate input parameter
        if (toolId == null) {
            return false; // Cannot check if null tool ID is registered
        }

        return tools.containsKey(toolId);
    }

    /**
     * Get tool specifications for the SDK.
     * 
     * @return Array of tool specifications
     */
    public McpServerFeatures.SyncToolSpecification[] getToolSpecifications() {
        logger.debug("Creating tool specifications for {} tools", tools.size());

        // For now, return empty array until we implement proper tool specification creation
        // The tools are registered via the adapter pattern, but the SDK integration
        // requires proper tool specifications that we'll implement in a future iteration
        // TODO : Implement proper tool specification creation
        logger.warn("Tool specification creation not yet implemented - returning empty array");
        return new McpServerFeatures.SyncToolSpecification[0];
    }

    /**
     * Get async tool specifications for the SDK.
     * 
     * @return Array of async tool specifications
     */
    public McpServerFeatures.AsyncToolSpecification[] getAsyncToolSpecifications() {
        logger.debug("Creating async tool specifications for {} tools", tools.size());

        // For now, return empty array until we implement proper tool specification creation
        // The tools are registered via the adapter pattern, but the SDK integration
        // requires proper tool specifications that we'll implement in a future iteration
        // TODO : Implement proper async tool specification creation
        logger.warn("Async tool specification creation not yet implemented - returning empty array");
        return new McpServerFeatures.AsyncToolSpecification[0];
    }

    /**
     * Create a sync tool specification from an MCP tool.
     * 
     * @param tool the MCP tool
     * @return the tool specification or null if creation fails
     */
    private McpServerFeatures.@Nullable SyncToolSpecification createToolSpecification(MCPTool tool) {
        try {
            String toolId = tool.getToolId();
            String description = tool.getDescription();
            Map<String, Object> schema = tool.getSchema();

            logger.debug("Creating sync tool specification for tool: {}", toolId);

            // TODO: Implement proper tool specification creation
            // This requires understanding the MCP SDK's tool specification API
            // For now, return null to indicate that specification creation is not implemented
            logger.warn("Tool specification creation not yet implemented for tool: {}", toolId);
            return null;

        } catch (Exception e) {
            logger.error("Failed to create tool specification for tool: {}", tool.getToolId(), e);
            return null;
        }
    }

    /**
     * Create an async tool specification from an MCP tool.
     * 
     * @param tool the MCP tool
     * @return the async tool specification or null if creation fails
     */
    private McpServerFeatures.@Nullable AsyncToolSpecification createAsyncToolSpecification(MCPTool tool) {
        try {
            String toolId = tool.getToolId();
            String description = tool.getDescription();
            Map<String, Object> schema = tool.getSchema();

            logger.debug("Creating async tool specification for tool: {}", toolId);

            // TODO: Implement proper async tool specification creation
            // This requires understanding the MCP SDK's async tool specification API
            // For now, return null to indicate that specification creation is not implemented
            logger.warn("Async tool specification creation not yet implemented for tool: {}", toolId);
            return null;

        } catch (Exception e) {
            logger.error("Failed to create async tool specification for tool: {}", tool.getToolId(), e);
            return null;
        }
    }

    /**
     * Convert a Map-based schema to JSON Schema format.
     * 
     * @param schema the input schema map
     * @return the JSON Schema object
     */
    private Object convertToJsonSchema(Map<String, Object> schema) {
        if (schema == null || schema.isEmpty()) {
            // Return a basic JSON schema for empty schemas
            return Map.of("type", "object", "properties", Map.of(), "required", java.util.List.of());
        }

        // For now, return the schema as-is since it should already be in JSON Schema format
        // In the future, we might need to do more sophisticated conversion
        return schema;
    }

    // ===== ReadyTracker Implementation =====

    @Override
    public void onReadyMarkerAdded(ReadyMarker readyMarker) {
        logger.debug("Ready marker added: {}", readyMarker);
    }

    @Override
    public void onReadyMarkerRemoved(ReadyMarker readyMarker) {
        logger.debug("Ready marker removed: {}", readyMarker);
    }

    /**
     * Mark tools as ready when they have been populated.
     */
    private void markToolsReady() {
        if (!toolsPopulated) {
            toolsPopulated = true;
            if (readyService != null) {
                readyService.markReady(MCP_TOOLS_READY);
            }
            logger.info("MCP tools ready - {} tools available", tools.size());
        }
    }
}
