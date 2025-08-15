package org.openhab.core.ai.tool.adapter;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.tool.api.Tool;
import org.openhab.core.ai.tool.api.ToolContext;
import org.openhab.core.ai.tool.api.ToolException;
import org.openhab.core.ai.tool.validation.api.ToolValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;

/**
 * Adapter for MCP Tools.
 * 
 * This class provides an adapter layer between the Tool interface and the MCP server,
 * handling tool execution and result conversion.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ToolAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ToolAdapter.class);

    private final Tool tool;

    /**
     * Create a new tool converter.
     * 
     * @param tool the tool to convert
     */
    public ToolAdapter(Tool tool) {
        this.tool = tool;
    }

    /**
     * Get the underlying tool.
     * 
     * @return the tool
     */
    public Tool getTool() {
        return tool;
    }

    /**
     * Execute the tool with the given parameters.
     * 
     * @param parameters the tool parameters
     * @return the tool result
     */
    public Map<String, Object> execute(Map<String, Object> parameters) {
        logger.debug("Executing tool: {} with parameters: {}", tool.getId(), parameters);

        try {
            // Create a tool context for execution
            ToolContext context = new ToolContext();
            context.setProperty("requestId", "mcp-request-" + System.currentTimeMillis());
            context.setProperty("principalId", "mcp-client");
            context.setProperty("protocol", "mcp");
            context.setProperty("timestamp", System.currentTimeMillis());

            // Execute the tool
            var result = tool.execute(parameters, context);

            // Convert the result to a Map format suitable for MCP
            Map<String, Object> resultMap = Map.of("success", result.isSuccess(), "toolId", tool.getId(), "content",
                    result.getContent() != null ? result.getContent() : "", "executionTime",
                    result.getExecutionTimeMs());

            logger.debug("Tool execution completed successfully: {}", tool.getId());
            return resultMap;

        } catch (ToolException e) {
            logger.error("Tool execution failed: {}", tool.getId(), e);
            return Map.of("success", false, "toolId", tool.getId(), "error", e.getMessage(), "errorCode",
                    e.getErrorCode().name());
        } catch (Exception e) {
            logger.error("Unexpected error during tool execution: {}", tool.getId(), e);
            return Map.of("success", false, "toolId", tool.getId(), "error", "Unexpected error: " + e.getMessage());
        }
    }

    /**
     * Validate the tool parameters.
     * 
     * @param parameters the parameters to validate
     * @return true if the parameters are valid
     */
    public boolean validateParameters(Map<String, Object> parameters) {
        logger.debug("Validating parameters for tool: {}", tool.getId());

        try {
            ToolValidationResult validationResult = tool.validateParameters(parameters);
            boolean isValid = validationResult.isValid();

            if (!isValid) {
                logger.warn("Parameter validation failed for tool {}: {}", tool.getId(), validationResult.getMessage());
            }

            return isValid;
        } catch (Exception e) {
            logger.error("Error during parameter validation for tool: {}", tool.getId(), e);
            return false;
        }
    }

    /**
     * Get the tool's input schema.
     * 
     * @return the input schema
     */
    public Map<String, Object> getInputSchema() {
        return tool.getInputSchema();
    }

    /**
     * Get the tool's output schema.
     * 
     * @return the output schema
     */
    public Map<String, Object> getOutputSchema() {
        return tool.getOutputSchema();
    }

    /**
     * Get the tool's metadata.
     * 
     * @return the tool metadata
     */
    public org.openhab.core.ai.tool.api.ToolMetadata getMetadata() {
        return tool.getMetadata();
    }

    // ========== MCP Integration Methods ==========

    /**
     * Convert this tool to MCP Tool specification.
     * 
     * @return the MCP tool specification
     */
    public McpSchema.Tool toMcpTool() {
        try {
            // Convert input schema
            McpSchema.JsonSchema inputSchema = convertToJsonSchema(tool.getInputSchema());

            // Create MCP Tool specification
            return McpSchema.Tool.builder().name(tool.getName()).description(tool.getDescription())
                    .inputSchema(inputSchema).build();

        } catch (Exception e) {
            logger.error("Failed to convert tool to MCP specification: {}", tool.getId(), e);
            throw new RuntimeException("Tool conversion failed", e);
        }
    }

    /**
     * Create a sync tool specification for the MCP server.
     * 
     * @return the sync tool specification
     */
    public McpServerFeatures.SyncToolSpecification createSyncToolSpecification() {
        try {
            logger.debug("Creating sync tool specification for tool: {}", tool.getId());

            // Convert input schema
            McpSchema.JsonSchema inputSchema = convertToJsonSchema(tool.getInputSchema());

            // Create MCP Tool definition
            McpSchema.Tool mcpTool = McpSchema.Tool.builder().name(tool.getName()).description(tool.getDescription())
                    .inputSchema(inputSchema).build();

            // Create the call handler
            return McpServerFeatures.SyncToolSpecification.builder().tool(mcpTool).callHandler((exchange, toolReq) -> {
                try {
                    logger.debug("Executing sync tool: {} with arguments: {}", tool.getId(), toolReq.arguments());

                    // Execute the tool using the adapter's execute method
                    Map<String, Object> resultMap = execute(toolReq.arguments());

                    // Convert result to MCP format
                    boolean isError = !(Boolean) resultMap.getOrDefault("success", false);
                    String content = resultMap.containsKey("content") ? resultMap.get("content").toString() : "";
                    if (resultMap.containsKey("error")) {
                        content = "Error: " + resultMap.get("error");
                    }

                    List<McpSchema.Content> contents = List.of(new McpSchema.TextContent(content));
                    return new McpSchema.CallToolResult(contents, isError);

                } catch (Exception e) {
                    logger.error("Unexpected error during sync tool execution: {}", tool.getId(), e);
                    List<McpSchema.Content> contents = List
                            .of(new McpSchema.TextContent("Unexpected error: " + e.getMessage()));
                    return new McpSchema.CallToolResult(contents, true);
                }
            }).build();

        } catch (Exception e) {
            logger.error("Failed to create sync tool specification for tool: {}", tool.getId(), e);
            throw new RuntimeException("Sync tool specification creation failed", e);
        }
    }

    /**
     * Create an async tool specification for the MCP server.
     * 
     * @return the async tool specification
     */
    public McpServerFeatures.AsyncToolSpecification createAsyncToolSpecification() {
        try {
            logger.debug("Creating async tool specification for tool: {}", tool.getId());

            // Convert input schema
            McpSchema.JsonSchema inputSchema = convertToJsonSchema(tool.getInputSchema());

            // Create MCP Tool definition
            McpSchema.Tool mcpTool = McpSchema.Tool.builder().name(tool.getName()).description(tool.getDescription())
                    .inputSchema(inputSchema).build();

            // Create the call handler
            return McpServerFeatures.AsyncToolSpecification.builder().tool(mcpTool).callHandler((exchange, toolReq) -> {
                return reactor.core.publisher.Mono.fromCallable(() -> {
                    try {
                        logger.debug("Executing async tool: {} with arguments: {}", tool.getId(), toolReq.arguments());

                        // Execute the tool using the adapter's execute method
                        Map<String, Object> resultMap = execute(toolReq.arguments());

                        // Convert result to MCP format
                        boolean isError = !(Boolean) resultMap.getOrDefault("success", false);
                        String content = resultMap.containsKey("content") ? resultMap.get("content").toString() : "";
                        if (resultMap.containsKey("error")) {
                            content = "Error: " + resultMap.get("error");
                        }

                        List<McpSchema.Content> contents = List.of(new McpSchema.TextContent(content));
                        return new McpSchema.CallToolResult(contents, isError);

                    } catch (Exception e) {
                        logger.error("Unexpected error during async tool execution: {}", tool.getId(), e);
                        List<McpSchema.Content> contents = List
                                .of(new McpSchema.TextContent("Unexpected error: " + e.getMessage()));
                        return new McpSchema.CallToolResult(contents, true);
                    }
                });
            }).build();

        } catch (Exception e) {
            logger.error("Failed to create async tool specification for tool: {}", tool.getId(), e);
            throw new RuntimeException("Async tool specification creation failed", e);
        }
    }

    /**
     * Convert a Map<String, Object> schema to JsonSchema.
     * 
     * @param schemaMap the schema as a map
     * @return the JsonSchema object
     */
    private McpSchema.JsonSchema convertToJsonSchema(Map<String, Object> schemaMap) {
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
