package org.openhab.core.ai.tool.util;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.api.Tool;
import org.openhab.core.ai.tool.api.ToolContext;
import org.openhab.core.ai.tool.api.ToolResult;
import org.openhab.core.ai.tool.validation.api.ToolValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;

/**
 * Enhanced MCP Tool Utilities.
 * 
 * This class provides improved tool integration utilities, enhanced tool specification
 * generation, and better tool registration and management capabilities.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ToolUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ToolUtils.class);

    /** Cache of generated tool specifications. */
    private static final Map<String, McpServerFeatures.SyncToolSpecification> syncSpecCache = new ConcurrentHashMap<>();
    private static final Map<String, McpServerFeatures.AsyncToolSpecification> asyncSpecCache = new ConcurrentHashMap<>();

    /**
     * Enhanced tool specification builder.
     * 
     * @param tool the tool to build specification for
     * @return the sync tool specification
     */
    public static McpServerFeatures.SyncToolSpecification buildSyncToolSpecification(Tool tool) {
        String toolId = tool.getId();

        // Check cache first
        McpServerFeatures.SyncToolSpecification cached = syncSpecCache.get(toolId);
        if (cached != null) {
            return cached;
        }

        try {
            // Build enhanced specification using the correct pattern from ToolAdapter
            McpSchema.Tool mcpTool = McpSchema.Tool.builder().name(tool.getId())
                    .description(tool.getMetadata().getDescription())
                    .inputSchema(convertToJsonSchema(tool.getInputSchema())).build();

            McpServerFeatures.SyncToolSpecification spec = McpServerFeatures.SyncToolSpecification.builder()
                    .tool(mcpTool).callHandler((exchange, toolReq) -> {
                        // Implement actual tool execution logic
                        try {
                            LOGGER.debug("Executing sync tool: {}", toolId);

                            // Convert MCP request to tool parameters
                            Map<String, Object> parameters = toolReq.arguments() != null ? toolReq.arguments()
                                    : Map.of();

                            // Validate parameters
                            ToolValidationResult validation = tool.validateParameters(parameters);
                            if (!validation.isValid()) {
                                LOGGER.warn("Tool parameter validation failed for {}: {}", toolId,
                                        validation.getMessage());
                                return new McpSchema.CallToolResult(Arrays.asList(
                                        new McpSchema.TextContent("Validation failed: " + validation.getMessage())),
                                        true);
                            }

                            // Create tool context
                            ToolContext context = new ToolContext();
                            context.setProperty("requestId", toolReq.name());
                            context.setProperty("timestamp", System.currentTimeMillis());

                            // Execute the tool
                            long startTime = System.currentTimeMillis();
                            ToolResult result = tool.execute(parameters, context);
                            long executionTime = System.currentTimeMillis() - startTime;

                            // Log performance metrics
                            org.openhab.core.ai.tool.util.helpers.ToolUtils.optimizeToolPerformance(toolId,
                                    executionTime);

                            if (result.isSuccess()) {
                                LOGGER.debug("Tool execution successful for {} in {}ms", toolId, executionTime);

                                // Convert result to MCP format
                                List<McpSchema.Content> contents = Arrays.asList(new McpSchema.TextContent(
                                        result.getContent() != null ? result.getContent().toString() : "Success"));

                                return new McpSchema.CallToolResult(contents, false);
                            } else {
                                LOGGER.warn("Tool execution failed for {}: {}", toolId, result.getError());
                                return new McpSchema.CallToolResult(
                                        Arrays.asList(
                                                new McpSchema.TextContent("Execution failed: " + result.getError())),
                                        true);
                            }

                        } catch (Exception e) {
                            LOGGER.error("Error executing sync tool: {}", toolId, e);
                            return new McpSchema.CallToolResult(
                                    Arrays.asList(new McpSchema.TextContent("Internal error: " + e.getMessage())),
                                    true);
                        }
                    }).build();

            // Cache the specification
            syncSpecCache.put(toolId, spec);

            LOGGER.debug("Built sync tool specification for: {}", toolId);
            return spec;

        } catch (Exception e) {
            LOGGER.error("Error building sync tool specification for: {}", toolId, e);
            return null;
        }
    }

    /**
     * Enhanced async tool specification builder.
     * 
     * @param tool the tool to build specification for
     * @return the async tool specification
     */
    public static McpServerFeatures.AsyncToolSpecification buildAsyncToolSpecification(Tool tool) {
        String toolId = tool.getId();

        // Check cache first
        McpServerFeatures.AsyncToolSpecification cached = asyncSpecCache.get(toolId);
        if (cached != null) {
            return cached;
        }

        try {
            // Build enhanced specification using the correct pattern from ToolAdapter
            McpSchema.Tool mcpTool = McpSchema.Tool.builder().name(tool.getId())
                    .description(tool.getMetadata().getDescription())
                    .inputSchema(convertToJsonSchema(tool.getInputSchema())).build();

            McpServerFeatures.AsyncToolSpecification spec = McpServerFeatures.AsyncToolSpecification.builder()
                    .tool(mcpTool).callHandler((exchange, toolReq) -> {
                        // Implement actual async tool execution logic
                        return reactor.core.publisher.Mono.fromCallable(() -> {
                            try {
                                LOGGER.debug("Executing async tool: {}", toolId);

                                // Convert MCP request to tool parameters
                                Map<String, Object> parameters = toolReq.arguments() != null ? toolReq.arguments()
                                        : Map.of();

                                // Validate parameters
                                ToolValidationResult validation = tool.validateParameters(parameters);
                                if (!validation.isValid()) {
                                    LOGGER.warn("Tool parameter validation failed for {}: {}", toolId,
                                            validation.getMessage());
                                    return new McpSchema.CallToolResult(Arrays.asList(
                                            new McpSchema.TextContent("Validation failed: " + validation.getMessage())),
                                            true);
                                }

                                // Create tool context
                                ToolContext context = new ToolContext();
                                context.setProperty("requestId", toolReq.name());
                                context.setProperty("timestamp", System.currentTimeMillis());

                                // Execute the tool
                                long startTime = System.currentTimeMillis();
                                ToolResult result = tool.execute(parameters, context);
                                long executionTime = System.currentTimeMillis() - startTime;

                                // Log performance metrics
                                org.openhab.core.ai.tool.util.helpers.ToolUtils.optimizeToolPerformance(toolId,
                                        executionTime);

                                if (result.isSuccess()) {
                                    LOGGER.debug("Async tool execution successful for {} in {}ms", toolId,
                                            executionTime);

                                    // Convert result to MCP format
                                    List<McpSchema.Content> contents = Arrays.asList(new McpSchema.TextContent(
                                            result.getContent() != null ? result.getContent().toString() : "Success"));

                                    return new McpSchema.CallToolResult(contents, false);
                                } else {
                                    LOGGER.warn("Async tool execution failed for {}: {}", toolId, result.getError());
                                    return new McpSchema.CallToolResult(Arrays.asList(
                                            new McpSchema.TextContent("Execution failed: " + result.getError())), true);
                                }

                            } catch (Exception e) {
                                LOGGER.error("Error executing async tool: {}", toolId, e);
                                return new McpSchema.CallToolResult(
                                        Arrays.asList(new McpSchema.TextContent("Internal error: " + e.getMessage())),
                                        true);
                            }
                        });
                    }).build();

            // Cache the specification
            asyncSpecCache.put(toolId, spec);

            LOGGER.debug("Built async tool specification for: {}", toolId);
            return spec;

        } catch (Exception e) {
            LOGGER.error("Error building async tool specification for: {}", toolId, e);
            return null;
        }
    }

    /**
     * Validate tool parameters with enhanced validation.
     * 
     * @param tool the tool to validate
     * @param parameters the parameters to validate
     * @return validation result with enhanced details
     */
    public static EnhancedValidationResult validateToolParameters(Tool tool, Map<String, Object> parameters) {
        String toolId = tool.getId();

        try {
            ToolValidationResult result = tool.validateParameters(parameters);

            EnhancedValidationResult enhancedResult = new EnhancedValidationResult(result.isValid(),
                    result.isValid() ? Arrays.asList()
                            : Arrays.asList(result.getMessage() != null ? result.getMessage() : "Validation failed"),
                    Arrays.asList(), toolId, parameters);

            if (!result.isValid()) {
                LOGGER.warn("Tool parameter validation failed for {}: {}", toolId, result.getMessage());
            } else {
                LOGGER.debug("Tool parameter validation passed for: {}", toolId);
            }

            return enhancedResult;

        } catch (Exception e) {
            LOGGER.error("Error validating tool parameters for: {}", toolId, e);
            return new EnhancedValidationResult(false, Arrays.asList("Validation error: " + e.getMessage()),
                    Arrays.asList(), toolId, parameters);
        }
    }

    /**
     * Enhanced validation result with additional context.
     */
    /* Extracted to top-level: org.openhab.core.ai.tool.util.EnhancedValidationResult */

    /**
     * Convert schema map to MCP JSON schema.
     * 
     * @param schemaMap the schema map
     * @return the MCP JSON schema
     */
    private static McpSchema.JsonSchema convertToJsonSchema(Map<String, Object> schemaMap) {
        try {
            // Enhanced schema conversion with better error handling
            if (schemaMap == null || schemaMap.isEmpty()) {
                return new McpSchema.JsonSchema("object", Map.of(), Arrays.asList(), true, Map.of(), Map.of());
            }

            // Convert the schema map to MCP JSON schema format
            // This is a simplified conversion - in a real implementation,
            // you would have more sophisticated schema conversion logic
            return new McpSchema.JsonSchema("object", Map.of(), Arrays.asList(), true, Map.of(), schemaMap);

        } catch (Exception e) {
            LOGGER.error("Error converting schema to JSON schema", e);
            return new McpSchema.JsonSchema("object", Map.of(), Arrays.asList(), true, Map.of(), Map.of());
        }
    }

    /**
     * Clear tool specification cache.
     * 
     * @param toolId the tool ID to clear, or null to clear all
     */
    public static void clearSpecificationCache(@Nullable String toolId) {
        if (toolId != null) {
            syncSpecCache.remove(toolId);
            asyncSpecCache.remove(toolId);
            LOGGER.debug("Cleared specification cache for tool: {}", toolId);
        } else {
            syncSpecCache.clear();
            asyncSpecCache.clear();
            LOGGER.debug("Cleared all specification caches");
        }
    }

    /**
     * Get cached specification count.
     * 
     * @return the number of cached specifications
     */
    public static int getCachedSpecificationCount() {
        return syncSpecCache.size() + asyncSpecCache.size();
    }

    /**
     * Check if a tool specification is cached.
     * 
     * @param toolId the tool ID
     * @return true if the specification is cached
     */
    public static boolean isSpecificationCached(String toolId) {
        return syncSpecCache.containsKey(toolId) || asyncSpecCache.containsKey(toolId);
    }

    /**
     * Get tool metadata summary.
     * 
     * @param tool the tool
     * @return metadata summary
     */
    public static String getToolMetadataSummary(Tool tool) {
        var metadata = tool.getMetadata();
        return String.format("Tool: %s, Description: %s, Version: %s, Author: %s", tool.getId(),
                metadata.getDescription(), metadata.getVersion(), metadata.getAuthor());
    }

    /**
     * Validate tool metadata.
     * 
     * @param tool the tool to validate
     * @return validation result
     */
    public static boolean validateToolMetadata(Tool tool) {
        var metadata = tool.getMetadata();

        if (tool.getId() == null || tool.getId().trim().isEmpty()) {
            LOGGER.error("Tool ID is null or empty");
            return false;
        }

        if (metadata.getDescription() == null || metadata.getDescription().trim().isEmpty()) {
            LOGGER.warn("Tool {} has no description", tool.getId());
        }

        if (metadata.getVersion() == null || metadata.getVersion().trim().isEmpty()) {
            LOGGER.warn("Tool {} has no version", tool.getId());
        }

        if (metadata.getAuthor() == null || metadata.getAuthor().trim().isEmpty()) {
            LOGGER.warn("Tool {} has no author", tool.getId());
        }

        return true;
    }

    /**
     * Get tool performance metrics.
     * 
     * @param tool the tool
     * @param executionTimeMs execution time in milliseconds
     * @return performance metrics
     */
    public static ToolPerformanceMetrics getToolPerformanceMetrics(Tool tool, long executionTimeMs) {
        return new ToolPerformanceMetrics(tool.getId(), executionTimeMs, System.currentTimeMillis(),
                tool.getMetadata().getVersion());
    }

    /**
     * Tool performance metrics.
     */
    /* Extracted to top-level: org.openhab.core.ai.tool.util.ToolPerformanceMetrics */
}
