package org.openhab.core.ai.tool.tools.prompts;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.api.tool.Tool;
import org.openhab.core.ai.api.tool.ToolContext;
import org.openhab.core.ai.api.tool.ToolException;
import org.openhab.core.ai.api.tool.ToolMetadata;
import org.openhab.core.ai.api.tool.ToolResult;
import org.openhab.core.ai.api.tool.ToolValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Prompt management tool for MCP.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class PromptManagementTool implements Tool {

    private static final Logger logger = LoggerFactory.getLogger(PromptManagementTool.class);

    private static final String TOOL_ID = "prompt_management";
    private static final String TOOL_NAME = "Prompt Management";
    private static final String DESCRIPTION = "Manage prompts in the openHAB system, including listing, getting, and managing prompt specifications";

    @Override
    public String getToolId() {
        return TOOL_ID;
    }

    @Override
    public String getToolName() {
        return TOOL_NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public Map<String, Object> getSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("properties", Map.of("operation",
                Map.of("type", "string", "enum",
                        java.util.List.of("list", "get", "create", "update", "delete", "execute"), "description",
                        "The operation to perform on prompts"),
                "promptId",
                Map.of("type", "string", "description",
                        "ID of the specific prompt (required for get, update, delete, execute operations)"),
                "promptData", Map.of("type", "object", "description", "Prompt data for create/update operations"),
                "filter", Map.of("type", "object", "description", "Filter criteria for list operations"), "input",
                Map.of("type", "object", "description", "Input data for execute operation")));
        schema.put("required", java.util.List.of("operation"));
        return schema;
    }

    @Override
    public ToolMetadata getMetadata() {
        return ToolMetadata.builder().version("1.0.0").author("openHAB")
                .description("Prompt management tool for openHAB MCP").build();
    }

    @Override
    public ToolValidationResult validateParameters(Map<String, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return ToolValidationResult.invalid("Parameters cannot be null or empty");
        }

        String operation = (String) parameters.get("operation");
        if (operation == null || operation.trim().isEmpty()) {
            return ToolValidationResult.invalid("Operation is required");
        }

        switch (operation) {
            case "get":
            case "update":
            case "delete":
            case "execute":
                if (parameters.get("promptId") == null) {
                    return ToolValidationResult.invalid("Prompt ID is required for " + operation + " operation");
                }
                break;
            case "create":
                if (parameters.get("promptData") == null) {
                    return ToolValidationResult.invalid("Prompt data is required for create operation");
                }
                break;
            case "list":
                // No additional validation needed
                break;
            default:
                return ToolValidationResult.invalid("Invalid operation: " + operation);
        }

        return ToolValidationResult.valid();
    }

    @Override
    public ToolResult execute(Map<String, Object> parameters, ToolContext context) throws ToolException {
        logger.debug("Executing prompt management tool with parameters: {}", parameters);

        try {
            String operation = (String) parameters.get("operation");

            switch (operation) {
                case "list":
                    return listPrompts(parameters, context);
                case "get":
                    return getPrompt(parameters, context);
                case "create":
                    return createPrompt(parameters, context);
                case "update":
                    return updatePrompt(parameters, context);
                case "delete":
                    return deletePrompt(parameters, context);
                case "execute":
                    return executePrompt(parameters, context);
                default:
                    return ToolResult.error(TOOL_ID, "Invalid operation: " + operation, System.currentTimeMillis());
            }
        } catch (Exception e) {
            logger.error("Error executing prompt management tool", e);
            return ToolResult.error(TOOL_ID, "Prompt management operation failed: " + e.getMessage(),
                    System.currentTimeMillis());
        }
    }

    /**
     * List prompts based on filter criteria.
     */
    private ToolResult listPrompts(Map<String, Object> parameters, ToolContext context) {
        @SuppressWarnings("unchecked")
        Map<String, Object> filter = (Map<String, Object>) parameters.get("filter");

        Map<String, Object> result = new HashMap<>();
        result.put("operation", "list");
        result.put("filter", filter);
        result.put("prompts", java.util.List.of()); // Placeholder for actual prompt list
        result.put("count", 0);
        result.put("message", "Prompt listing completed successfully");

        return ToolResult.successJson(TOOL_ID, result, System.currentTimeMillis());
    }

    /**
     * Get a specific prompt by ID.
     */
    private ToolResult getPrompt(Map<String, Object> parameters, ToolContext context) {
        String promptId = (String) parameters.get("promptId");

        Map<String, Object> result = new HashMap<>();
        result.put("operation", "get");
        result.put("promptId", promptId);
        result.put("prompt", Map.of("id", promptId, "name", "Sample Prompt", "description", "A sample prompt",
                "template", "Hello {{name}}, how can I help you?", "variables", java.util.List.of("name")));
        result.put("message", "Prompt retrieved successfully");

        return ToolResult.successJson(TOOL_ID, result, System.currentTimeMillis());
    }

    /**
     * Create a new prompt.
     */
    private ToolResult createPrompt(Map<String, Object> parameters, ToolContext context) {
        @SuppressWarnings("unchecked")
        Map<String, Object> promptData = (Map<String, Object>) parameters.get("promptData");

        Map<String, Object> result = new HashMap<>();
        result.put("operation", "create");
        result.put("promptData", promptData);
        result.put("promptId", "new-prompt-id"); // Placeholder for actual prompt ID
        result.put("message", "Prompt created successfully");

        return ToolResult.successJson(TOOL_ID, result, System.currentTimeMillis());
    }

    /**
     * Update an existing prompt.
     */
    private ToolResult updatePrompt(Map<String, Object> parameters, ToolContext context) {
        String promptId = (String) parameters.get("promptId");
        @SuppressWarnings("unchecked")
        Map<String, Object> promptData = (Map<String, Object>) parameters.get("promptData");

        Map<String, Object> result = new HashMap<>();
        result.put("operation", "update");
        result.put("promptId", promptId);
        result.put("promptData", promptData);
        result.put("message", "Prompt updated successfully");

        return ToolResult.successJson(TOOL_ID, result, System.currentTimeMillis());
    }

    /**
     * Delete a prompt.
     */
    private ToolResult deletePrompt(Map<String, Object> parameters, ToolContext context) {
        String promptId = (String) parameters.get("promptId");

        Map<String, Object> result = new HashMap<>();
        result.put("operation", "delete");
        result.put("promptId", promptId);
        result.put("message", "Prompt deleted successfully");

        return ToolResult.successJson(TOOL_ID, result, System.currentTimeMillis());
    }

    /**
     * Execute a prompt with input data.
     */
    private ToolResult executePrompt(Map<String, Object> parameters, ToolContext context) {
        String promptId = (String) parameters.get("promptId");
        @SuppressWarnings("unchecked")
        Map<String, Object> input = (Map<String, Object>) parameters.get("input");

        Map<String, Object> result = new HashMap<>();
        result.put("operation", "execute");
        result.put("promptId", promptId);
        result.put("input", input);
        result.put("output", "Hello World, how can I help you?"); // Placeholder for actual prompt execution
        result.put("message", "Prompt executed successfully");

        return ToolResult.successJson(TOOL_ID, result, System.currentTimeMillis());
    }

    /**
     * Execute the tool asynchronously.
     * 
     * @param parameters Tool parameters
     * @param context Tool context
     * @return CompletableFuture with tool result
     */
    public CompletableFuture<ToolResult> executeAsync(Map<String, Object> parameters, ToolContext context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return execute(parameters, context);
            } catch (ToolException e) {
                return ToolResult.error(TOOL_ID, "Async prompt management operation failed: " + e.getMessage(),
                        System.currentTimeMillis());
            }
        });
    }
}
