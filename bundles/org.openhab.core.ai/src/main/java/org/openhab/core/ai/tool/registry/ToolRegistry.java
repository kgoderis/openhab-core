package org.openhab.core.ai.tool.registry;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.api.tool.CompletionRegistry;
import org.openhab.core.ai.api.tool.PromptRegistry;
import org.openhab.core.ai.api.tool.ResourceRegistry;
import org.openhab.core.ai.api.tool.Tool;
import org.openhab.core.ai.tool.adapter.ToolAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;

/**
 * Registry for MCP Tools, Resources, Prompts, and Completions.
 *
 * This class manages the registration and discovery of MCP tools, resources, prompts, and completions,
 * providing access to specifications for the MCP server.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ToolRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ToolRegistry.class);

    /** Map of tool adapters by tool ID. */
    private final Map<String, ToolAdapter> toolAdapters = new ConcurrentHashMap<>();
    /** Map of tools by tool ID. */
    private final Map<String, Tool> tools = new ConcurrentHashMap<>();

    /** Resource registry. */
    private final ResourceRegistry resourceRegistry;
    /** Prompt registry. */
    private final PromptRegistry promptRegistry;
    /** Completion registry. */
    private final CompletionRegistry completionRegistry;

    /**
     * Create a new ToolRegistry with all specification registries.
     */
    public ToolRegistry() {
        this.resourceRegistry = new org.openhab.core.ai.tool.registry.ResourceRegistryImpl();
        this.promptRegistry = new org.openhab.core.ai.tool.registry.PromptRegistryImpl();
        this.completionRegistry = new org.openhab.core.ai.tool.registry.CompletionRegistryImpl();
    }

    /**
     * Get the resource registry.
     *
     * @return the resource registry
     */
    public ResourceRegistry getResourceRegistry() {
        return resourceRegistry;
    }

    /**
     * Get the prompt registry.
     *
     * @return the prompt registry
     */
    public PromptRegistry getPromptRegistry() {
        return promptRegistry;
    }

    /**
     * Get the completion registry.
     *
     * @return the completion registry
     */
    public CompletionRegistry getCompletionRegistry() {
        return completionRegistry;
    }

    /**
     * Register a tool.
     *
     * @param tool the tool to register
     */
    public void registerTool(final Tool tool) {
        String toolId = tool.getId();
        tools.put(toolId, tool);

        // Create and register tool adapter
        ToolAdapter adapter = new ToolAdapter(tool);
        toolAdapters.put(toolId, adapter);

        LOGGER.debug("Registered tool: {}", toolId);
    }

    /**
     * Unregister a tool.
     *
     * @param toolId the tool ID to unregister
     */
    public void unregisterTool(final String toolId) {
        tools.remove(toolId);
        toolAdapters.remove(toolId);
        LOGGER.debug("Unregistered tool: {}", toolId);
    }

    /**
     * Get a tool by ID.
     *
     * @param toolId the tool ID
     * @return the tool or null if not found
     */
    public @Nullable Tool getTool(final String toolId) {
        return tools.get(toolId);
    }

    /**
     * Get a tool adapter by ID.
     *
     * @param toolId the tool ID
     * @return the tool adapter or null if not found
     */
    public @Nullable ToolAdapter getToolAdapter(final String toolId) {
        return toolAdapters.get(toolId);
    }

    /**
     * Get all tools.
     *
     * @return all registered tools
     */
    public Map<String, Tool> getAllTools() {
        return new ConcurrentHashMap<>(tools);
    }

    /**
     * Get all tool adapters.
     *
     * @return all registered tool adapters
     */
    public Map<String, ToolAdapter> getAllToolAdapters() {
        return new ConcurrentHashMap<>(toolAdapters);
    }

    /**
     * Get the number of registered tools.
     *
     * @return the number of tools
     */
    public int getToolCount() {
        return tools.size();
    }

    /**
     * Check if a tool is registered.
     *
     * @param toolId the tool ID
     * @return true if the tool is registered
     */
    public boolean isToolRegistered(final String toolId) {
        return tools.containsKey(toolId);
    }

    /**
     * Get sync tool specifications.
     *
     * @return sync tool specifications
     */
    public McpServerFeatures.SyncToolSpecification[] getSyncToolSpecifications() {
        return tools.values().stream().map(this::createSyncToolSpecification).filter(spec -> spec != null)
                .toArray(McpServerFeatures.SyncToolSpecification[]::new);
    }

    /**
     * Get async tool specifications.
     *
     * @return async tool specifications
     */
    public McpServerFeatures.AsyncToolSpecification[] getAsyncToolSpecifications() {
        return tools.values().stream().map(this::createAsyncToolSpecification).filter(spec -> spec != null)
                .toArray(McpServerFeatures.AsyncToolSpecification[]::new);
    }

    /**
     * Get sync resource specifications.
     *
     * @return sync resource specifications
     */
    public McpServerFeatures.SyncResourceSpecification[] getSyncResourceSpecifications() {
        return resourceRegistry.getSyncResourceSpecifications();
    }

    /**
     * Get async resource specifications.
     *
     * @return async resource specifications
     */
    public McpServerFeatures.AsyncResourceSpecification[] getAsyncResourceSpecifications() {
        return resourceRegistry.getAsyncResourceSpecifications();
    }

    /**
     * Get sync prompt specifications.
     *
     * @return sync prompt specifications
     */
    public McpServerFeatures.SyncPromptSpecification[] getSyncPromptSpecifications() {
        return promptRegistry.getSyncPromptSpecifications();
    }

    /**
     * Get async prompt specifications.
     *
     * @return async prompt specifications
     */
    public McpServerFeatures.AsyncPromptSpecification[] getAsyncPromptSpecifications() {
        return promptRegistry.getAsyncPromptSpecifications();
    }

    /**
     * Get sync completion specifications.
     *
     * @return sync completion specifications
     */
    public McpServerFeatures.SyncCompletionSpecification[] getSyncCompletionSpecifications() {
        return completionRegistry.getSyncCompletionSpecifications();
    }

    /**
     * Get async completion specifications.
     *
     * @return async completion specifications
     */
    public McpServerFeatures.AsyncCompletionSpecification[] getAsyncCompletionSpecifications() {
        return completionRegistry.getAsyncCompletionSpecifications();
    }

    /**
     * Create a sync tool specification from a tool.
     *
     * @param tool the tool
     * @return the sync tool specification or null if creation fails
     */
    private McpServerFeatures.@Nullable SyncToolSpecification createSyncToolSpecification(final Tool tool) {
        try {
            LOGGER.debug("Creating sync tool specification for tool: {}", tool.getId());

            // Convert Map<String, Object> schema to JsonSchema
            final McpSchema.JsonSchema inputSchema = convertToJsonSchema(tool.getInputSchema());

            // Create the MCP Tool definition using builder pattern
            final McpSchema.Tool mcpTool = McpSchema.Tool.builder().name(tool.getName())
                    .description(tool.getDescription()).inputSchema(inputSchema).build();

            // Create the call handler that delegates to the ToolAdapter
            final ToolAdapter adapter = toolAdapters.get(tool.getId());
            if (adapter == null) {
                LOGGER.warn("No adapter found for tool: {}", tool.getId());
                return null;
            }

            return McpServerFeatures.SyncToolSpecification.builder().tool(mcpTool).callHandler((exchange, toolReq) -> {
                try {
                    // Execute the tool through the adapter
                    final Map<String, Object> result = adapter.execute(toolReq.arguments());

                    // Convert the result to MCP format
                    final McpSchema.CallToolResult callResult = new McpSchema.CallToolResult(
                            List.of(new McpSchema.TextContent(result.toString())), false);

                    return callResult;
                } catch (Exception e) {
                    LOGGER.error("Error executing tool: {}", tool.getId(), e);
                    return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("Error: " + e.getMessage())),
                            true);
                }
            }).build();

        } catch (Exception e) {
            LOGGER.warn("Failed to create sync tool specification for tool: {}", tool.getId(), e);
            return null;
        }
    }

    /**
     * Create an async tool specification from a tool.
     *
     * @param tool the tool
     * @return the async tool specification or null if creation fails
     */
    private McpServerFeatures.@Nullable AsyncToolSpecification createAsyncToolSpecification(final Tool tool) {
        try {
            LOGGER.debug("Creating async tool specification for tool: {}", tool.getId());

            // Convert Map<String, Object> schema to JsonSchema
            final McpSchema.JsonSchema inputSchema = convertToJsonSchema(tool.getInputSchema());

            // Create the MCP Tool definition using builder pattern
            final McpSchema.Tool mcpTool = McpSchema.Tool.builder().name(tool.getName())
                    .description(tool.getDescription()).inputSchema(inputSchema).build();

            // Create the call handler that delegates to the ToolAdapter
            final ToolAdapter adapter = toolAdapters.get(tool.getId());
            if (adapter == null) {
                LOGGER.warn("No adapter found for tool: {}", tool.getId());
                return null;
            }

            return McpServerFeatures.AsyncToolSpecification.builder().tool(mcpTool).callHandler((exchange, toolReq) -> {
                return reactor.core.publisher.Mono.fromCallable(() -> {
                    try {
                        // Execute the tool through the adapter
                        final Map<String, Object> result = adapter.execute(toolReq.arguments());

                        // Convert the result to MCP format
                        final McpSchema.CallToolResult callResult = new McpSchema.CallToolResult(
                                List.of(new McpSchema.TextContent(result.toString())), false);

                        return callResult;
                    } catch (Exception e) {
                        LOGGER.error("Error executing tool: {}", tool.getId(), e);
                        return new McpSchema.CallToolResult(
                                List.of(new McpSchema.TextContent("Error: " + e.getMessage())), true);
                    }
                });
            }).build();

        } catch (Exception e) {
            LOGGER.warn("Failed to create async tool specification for tool: {}", tool.getId(), e);
            return null;
        }
    }

    /**
     * Convert a Map<String, Object> schema to JsonSchema.
     *
     * @param schemaMap the schema as a map
     * @return the JsonSchema object
     */
    private McpSchema.JsonSchema convertToJsonSchema(final Map<String, Object> schemaMap) {
        if (schemaMap == null || schemaMap.isEmpty()) {
            // Return a default schema for objects
            return new McpSchema.JsonSchema("object", Map.of(), List.of(), true, Map.of(), Map.of());
        }

        final String type = (String) schemaMap.getOrDefault("type", "object");
        @SuppressWarnings("unchecked")
        final Map<String, Object> properties = (Map<String, Object>) schemaMap.getOrDefault("properties", Map.of());
        @SuppressWarnings("unchecked")
        final List<String> required = (List<String>) schemaMap.getOrDefault("required", List.of());
        final Boolean additionalProperties = (Boolean) schemaMap.getOrDefault("additionalProperties", true);
        @SuppressWarnings("unchecked")
        final Map<String, Object> defs = (Map<String, Object>) schemaMap.getOrDefault("$defs", Map.of());
        @SuppressWarnings("unchecked")
        final Map<String, Object> definitions = (Map<String, Object>) schemaMap.getOrDefault("definitions", Map.of());

        return new McpSchema.JsonSchema(type, properties, required, additionalProperties, defs, definitions);
    }
}
