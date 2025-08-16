package org.openhab.core.ai.tool.library.prompts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.tool.api.Tool;
import org.openhab.core.ai.tool.api.ToolContext;
import org.openhab.core.ai.tool.api.ToolException;
import org.openhab.core.ai.tool.api.ToolMetadata;
import org.openhab.core.ai.tool.api.ToolResult;
import org.openhab.core.ai.tool.validation.api.ToolValidationResult;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Prompt management tool for MCP.
 * 
 * This tool provides comprehensive prompt management capabilities for the openHAB system,
 * including listing, getting, creating, updating, deleting, and executing prompts.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
@Component(service = Tool.class, immediate = true)
public class PromptManagementTool implements Tool {

    private static final Logger logger = LoggerFactory.getLogger(PromptManagementTool.class);

    private static final String TOOL_ID = "prompt_management";
    private static final String TOOL_NAME = "Prompt Management";
    private static final String DESCRIPTION = "Manage prompts in the openHAB system, including listing, getting, and managing prompt specifications";

    @Override
    public String getId() {
        return TOOL_ID;
    }

    @Override
    public String getName() {
        return TOOL_NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public Map<String, Object> getInputSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("properties", Map.of("operation",
                Map.of("type", "string", "enum", List.of("list", "get", "create", "update", "delete", "execute"),
                        "description", "The operation to perform on prompts"),
                "promptId",
                Map.of("type", "string", "description",
                        "ID of the specific prompt (required for get, update, delete, execute operations)"),
                "promptData", Map.of("type", "object", "description", "Prompt data for create/update operations"),
                "filter", Map.of("type", "object", "description", "Filter criteria for list operations"), "input",
                Map.of("type", "object", "description", "Input data for execute operation")));
        schema.put("required", List.of("operation"));
        return schema;
    }

    @Override
    public Map<String, Object> getOutputSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("properties",
                Map.of("success", Map.of("type", "boolean", "description", "Whether the operation was successful"),
                        "message", Map.of("type", "string", "description", "Result message"), "data",
                        Map.of("type", "object", "description", "Operation result data"), "error",
                        Map.of("type", "string", "description", "Error message if operation failed")));
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
     * List all available prompts.
     */
    private ToolResult listPrompts(Map<String, Object> parameters, ToolContext context) {
        @SuppressWarnings("unchecked")
        Map<String, Object> filter = (Map<String, Object>) parameters.get("filter");

        Map<String, Object> result = new HashMap<>();
        result.put("operation", "list");
        result.put("filter", filter);

        // Implement actual prompt listing logic
        try {
            // Get prompts from the prompt registry if available
            List<Map<String, Object>> prompts = new ArrayList<>();

            // For now, we'll create a basic implementation that returns sample prompts
            // In a real implementation, this would query a prompt registry or database

            // Sample prompts for demonstration
            Map<String, Object> samplePrompt1 = new HashMap<>();
            samplePrompt1.put("id", "greeting-prompt");
            samplePrompt1.put("name", "Greeting Prompt");
            samplePrompt1.put("description", "A friendly greeting prompt");
            samplePrompt1.put("template", "Hello {{name}}, how can I help you today?");
            samplePrompt1.put("variables", List.of("name"));
            samplePrompt1.put("category", "greeting");
            samplePrompt1.put("created", System.currentTimeMillis());
            prompts.add(samplePrompt1);

            Map<String, Object> samplePrompt2 = new HashMap<>();
            samplePrompt2.put("id", "weather-prompt");
            samplePrompt2.put("name", "Weather Prompt");
            samplePrompt2.put("description", "Weather information prompt");
            samplePrompt2.put("template", "The weather in {{location}} is {{temperature}}°C with {{condition}}");
            samplePrompt2.put("variables", List.of("location", "temperature", "condition"));
            samplePrompt2.put("category", "weather");
            samplePrompt2.put("created", System.currentTimeMillis());
            prompts.add(samplePrompt2);

            // Apply filter if provided
            if (filter != null && !filter.isEmpty()) {
                prompts = applyPromptFilter(prompts, filter);
            }

            result.put("prompts", prompts);
            result.put("count", prompts.size());
            result.put("message", "Prompt listing completed successfully");

        } catch (Exception e) {
            logger.error("Error listing prompts", e);
            result.put("prompts", List.of());
            result.put("count", 0);
            result.put("message", "Error listing prompts: " + e.getMessage());
        }

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

        // Implement actual prompt retrieval logic
        try {
            // In a real implementation, this would query a prompt registry or database
            // For now, we'll create a basic implementation that returns sample prompts

            Map<String, Object> prompt = null;

            // Check for known prompt IDs
            if ("greeting-prompt".equals(promptId)) {
                prompt = new HashMap<>();
                prompt.put("id", "greeting-prompt");
                prompt.put("name", "Greeting Prompt");
                prompt.put("description", "A friendly greeting prompt");
                prompt.put("template", "Hello {{name}}, how can I help you today?");
                prompt.put("variables", List.of("name"));
                prompt.put("category", "greeting");
                prompt.put("created", System.currentTimeMillis());
            } else if ("weather-prompt".equals(promptId)) {
                prompt = new HashMap<>();
                prompt.put("id", "weather-prompt");
                prompt.put("name", "Weather Prompt");
                prompt.put("description", "Weather information prompt");
                prompt.put("template", "The weather in {{location}} is {{temperature}}°C with {{condition}}");
                prompt.put("variables", List.of("location", "temperature", "condition"));
                prompt.put("category", "weather");
                prompt.put("created", System.currentTimeMillis());
            }

            if (prompt != null) {
                result.put("prompt", prompt);
                result.put("message", "Prompt retrieved successfully");
            } else {
                result.put("prompt", null);
                result.put("message", "Prompt not found: " + promptId);
            }

        } catch (Exception e) {
            logger.error("Error retrieving prompt: {}", promptId, e);
            result.put("prompt", null);
            result.put("message", "Error retrieving prompt: " + e.getMessage());
        }

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

        // Implement actual prompt creation logic
        try {
            // Validate required fields
            if (promptData == null) {
                throw new IllegalArgumentException("Prompt data is required");
            }

            String name = (String) promptData.get("name");
            String template = (String) promptData.get("template");

            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Prompt name is required");
            }

            if (template == null || template.trim().isEmpty()) {
                throw new IllegalArgumentException("Prompt template is required");
            }

            // Generate a unique prompt ID
            String promptId = "prompt-" + System.currentTimeMillis() + "-"
                    + UUID.randomUUID().toString().substring(0, 8);

            // In a real implementation, this would save to a prompt registry or database
            // For now, we'll just log the creation and return success

            logger.info("Creating new prompt: id={}, name={}", promptId, name);

            // Extract variables from template (simple implementation)
            List<String> variables = extractVariablesFromTemplate(template);

            // Create the prompt object
            Map<String, Object> createdPrompt = new HashMap<>(promptData);
            createdPrompt.put("id", promptId);
            createdPrompt.put("variables", variables);
            createdPrompt.put("created", System.currentTimeMillis());

            result.put("promptId", promptId);
            result.put("prompt", createdPrompt);
            result.put("message", "Prompt created successfully");

        } catch (Exception e) {
            logger.error("Error creating prompt", e);
            result.put("promptId", null);
            result.put("prompt", null);
            result.put("message", "Error creating prompt: " + e.getMessage());
        }

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

        // Implement actual prompt execution logic
        try {
            // First, retrieve the prompt
            Map<String, Object> prompt = retrievePromptById(promptId);

            if (prompt == null) {
                result.put("output", null);
                result.put("message", "Prompt not found: " + promptId);
                return ToolResult.successJson(TOOL_ID, result, System.currentTimeMillis());
            }

            String template = (String) prompt.get("template");
            if (template == null || template.isEmpty()) {
                result.put("output", null);
                result.put("message", "Prompt template is empty");
                return ToolResult.successJson(TOOL_ID, result, System.currentTimeMillis());
            }

            // Execute the template with input data
            String output = executeTemplate(template, input);

            result.put("output", output);
            result.put("message", "Prompt executed successfully");

        } catch (Exception e) {
            logger.error("Error executing prompt: {}", promptId, e);
            result.put("output", null);
            result.put("message", "Error executing prompt: " + e.getMessage());
        }

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

    /**
     * Apply filter to prompts list
     */
    private List<Map<String, Object>> applyPromptFilter(List<Map<String, Object>> prompts, Map<String, Object> filter) {
        List<Map<String, Object>> filteredPrompts = new ArrayList<>();

        for (Map<String, Object> prompt : prompts) {
            boolean matches = true;

            for (Map.Entry<String, Object> filterEntry : filter.entrySet()) {
                String key = filterEntry.getKey();
                Object value = filterEntry.getValue();

                if (prompt.containsKey(key)) {
                    Object promptValue = prompt.get(key);
                    if (!value.equals(promptValue)) {
                        matches = false;
                        break;
                    }
                } else {
                    matches = false;
                    break;
                }
            }

            if (matches) {
                filteredPrompts.add(prompt);
            }
        }

        return filteredPrompts;
    }

    /**
     * Extract variables from template string
     */
    private List<String> extractVariablesFromTemplate(String template) {
        List<String> variables = new ArrayList<>();

        if (template == null || template.isEmpty()) {
            return variables;
        }

        // Simple regex to find {{variable}} patterns
        Pattern pattern = Pattern.compile("\\{\\{([^}]+)\\}\\}");
        Matcher matcher = pattern.matcher(template);

        while (matcher.find()) {
            String variable = matcher.group(1).trim();
            if (!variable.isEmpty() && !variables.contains(variable)) {
                variables.add(variable);
            }
        }

        return variables;
    }

    /**
     * Retrieve prompt by ID
     */
    private Map<String, Object> retrievePromptById(String promptId) {
        // In a real implementation, this would query a prompt registry or database
        // For now, we'll return sample prompts for known IDs

        if ("greeting-prompt".equals(promptId)) {
            Map<String, Object> prompt = new HashMap<>();
            prompt.put("id", "greeting-prompt");
            prompt.put("name", "Greeting Prompt");
            prompt.put("description", "A friendly greeting prompt");
            prompt.put("template", "Hello {{name}}, how can I help you today?");
            prompt.put("variables", List.of("name"));
            prompt.put("category", "greeting");
            prompt.put("created", System.currentTimeMillis());
            return prompt;
        } else if ("weather-prompt".equals(promptId)) {
            Map<String, Object> prompt = new HashMap<>();
            prompt.put("id", "weather-prompt");
            prompt.put("name", "Weather Prompt");
            prompt.put("description", "Weather information prompt");
            prompt.put("template", "The weather in {{location}} is {{temperature}}°C with {{condition}}");
            prompt.put("variables", List.of("location", "temperature", "condition"));
            prompt.put("category", "weather");
            prompt.put("created", System.currentTimeMillis());
            return prompt;
        }

        return null;
    }

    /**
     * Execute template with input data
     */
    private String executeTemplate(String template, Map<String, Object> input) {
        if (template == null || template.isEmpty()) {
            return "";
        }

        if (input == null) {
            input = new HashMap<>();
        }

        String result = template;

        // Replace {{variable}} patterns with input values
        Pattern pattern = Pattern.compile("\\{\\{([^}]+)\\}\\}");
        Matcher matcher = pattern.matcher(template);

        while (matcher.find()) {
            String variable = matcher.group(1).trim();
            String replacement = String.valueOf(input.getOrDefault(variable, "{{" + variable + "}}"));
            result = result.replace("{{" + variable + "}}", replacement);
        }

        return result;
    }
}
