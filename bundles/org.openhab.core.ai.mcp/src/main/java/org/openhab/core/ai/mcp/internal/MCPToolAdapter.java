package org.openhab.core.ai.mcp.internal;

import java.util.Map;

import org.openhab.core.ai.common.api.action.AIAction;
import org.openhab.core.ai.common.api.action.AIActionContext;
import org.openhab.core.ai.common.api.action.AIActionResult;
import org.openhab.core.ai.mcp.api.MCPTool;
import org.openhab.core.ai.mcp.api.MCPToolContext;
import org.openhab.core.ai.mcp.api.MCPToolException;
import org.openhab.core.ai.mcp.api.MCPToolMetadata;
import org.openhab.core.ai.mcp.api.MCPToolResult;
import org.openhab.core.ai.mcp.api.MCPToolValidationResult;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapter that converts AIAction to MCPTool for use with the official MCP SDK.
 * 
 * This adapter bridges the gap between openHAB's AIAction system and the MCP
 * protocol, providing seamless integration with the official MCP Java SDK.
 * 
 * 
 */
public class MCPToolAdapter implements MCPTool {

    private static final Logger logger = LoggerFactory.getLogger(MCPToolAdapter.class);

    private final AIAction action;
    private final String toolId;
    private final String toolName;
    private final String description;
    private final Map<String, Object> schema;

    @Reference
    private MCPLoggingManager loggingManager;

    /**
     * Create a new MCP tool adapter from an AIAction.
     * 
     * @param action The AIAction to adapt
     */
    public MCPToolAdapter(AIAction action) {
        this.action = action;
        this.toolId = action.getActionId();
        this.toolName = action.getActionName();
        this.description = action.getDescription();
        this.schema = action.getParameterSchema();
    }

    @Override
    public String getToolId() {
        return toolId;
    }

    @Override
    public String getToolName() {
        return toolName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Map<String, Object> getSchema() {
        return schema;
    }

    @Override
    public MCPToolValidationResult validateParameters(Map<String, Object> parameters) {
        try {
            ValidationResult validation = validateParametersAgainstSchema(parameters, schema);

            if (validation.isValid()) {
                return MCPToolValidationResult.valid();
            } else {
                return MCPToolValidationResult.invalid(String.join("; ", validation.getErrors()));
            }
        } catch (Exception e) {
            logger.error("Parameter validation failed for tool {}: {}", toolId, e.getMessage(), e);
            return MCPToolValidationResult.invalid("Validation error: " + e.getMessage());
        }
    }

    /**
     * Execute the tool with comprehensive validation and error handling.
     * 
     * @param parameters Tool parameters
     * @param context Tool context
     * @return Tool execution result
     * @throws MCPToolException if execution fails
     */
    @Override
    public MCPToolResult execute(Map<String, Object> parameters, MCPToolContext context) throws MCPToolException {
        long startTime = System.currentTimeMillis();
        String toolId = getToolId();

        // Use enhanced logging for tool execution start
        if (loggingManager != null) {
            loggingManager.logMCPEvent("tool", "execution_start", "DEBUG",
                    Map.of("toolId", toolId, "parameters", parameters));
        } else {
            logger.debug("Executing tool: {} with parameters: {}", toolId, parameters);
        }

        try {
            // Pre-execution validation
            ValidationResult validation = validateExecution(parameters);
            if (!validation.isValid()) {
                String errorMsg = "Validation failed: " + String.join(", ", validation.getErrors());
                long executionTime = System.currentTimeMillis() - startTime;

                // Log validation failure with enhanced logging
                if (loggingManager != null) {
                    loggingManager.logToolExecution(toolId, parameters, executionTime, false, errorMsg);
                } else {
                    logger.warn("Tool execution validation failed for {}: {}", toolId, validation.getErrors());
                }

                return MCPToolResult.error(toolId, errorMsg, executionTime);
            }

            // Convert MCP context to AIAction context
            AIActionContext aiContext = convertToAIActionContext(context);

            // Execute the AIAction
            AIActionResult actionResult = action.execute(parameters, aiContext);

            // Post-execution validation
            if (actionResult == null) {
                String errorMsg = "Tool execution returned null result";
                long executionTime = System.currentTimeMillis() - startTime;

                // Log null result with enhanced logging
                if (loggingManager != null) {
                    loggingManager.logToolExecution(toolId, parameters, executionTime, false, errorMsg);
                } else {
                    logger.error("Tool execution returned null result for: {}", toolId);
                }

                return MCPToolResult.error(toolId, errorMsg, executionTime);
            }

            // Convert AIActionResult to MCPToolResult
            MCPToolResult result = convertActionResult(actionResult, toolId, startTime);

            // Log successful execution with enhanced logging
            long executionTime = System.currentTimeMillis() - startTime;
            String resultSummary = actionResult.isSuccess() ? "Success" : "Failed";

            if (loggingManager != null) {
                loggingManager.logToolExecution(toolId, parameters, executionTime, actionResult.isSuccess(),
                        resultSummary);
            } else {
                logger.debug("Tool execution completed successfully: {} in {}ms", toolId, executionTime);
            }

            return result;

        } catch (Exception e) {
            // Handle execution errors with enhanced logging
            long executionTime = System.currentTimeMillis() - startTime;
            String errorMsg = "Tool execution failed: " + e.getMessage();

            if (loggingManager != null) {
                loggingManager.logToolExecution(toolId, parameters, executionTime, false, errorMsg);
            } else {
                logger.error("Tool execution failed for {} after {}ms: {}", toolId, executionTime, e.getMessage(), e);
            }

            return MCPToolResult.error(toolId, errorMsg, executionTime);
        }
    }

    /**
     * Validate tool execution parameters and context.
     * 
     * @param parameters Tool parameters
     * @return Validation result
     */
    private ValidationResult validateExecution(Map<String, Object> parameters) {
        java.util.List<String> errors = new java.util.ArrayList<>();

        // Validate parameters are not null
        if (parameters == null) {
            errors.add("Parameters cannot be null");
            return new ValidationResult(false, errors);
        }

        // Validate action is available
        if (action == null) {
            errors.add("Action is not available");
            return new ValidationResult(false, errors);
        }

        // Validate action ID
        if (action.getActionId() == null || action.getActionId().trim().isEmpty()) {
            errors.add("Action ID is not valid");
            return new ValidationResult(false, errors);
        }

        // Validate tool ID
        if (getToolId() == null || getToolId().trim().isEmpty()) {
            errors.add("Tool ID is not valid");
            return new ValidationResult(false, errors);
        }

        // Validate tool name
        if (getToolName() == null || getToolName().trim().isEmpty()) {
            errors.add("Tool name is not valid");
            return new ValidationResult(false, errors);
        }

        // Validate schema
        Map<String, Object> schema = getSchema();
        if (schema == null || schema.isEmpty()) {
            errors.add("Tool schema is not valid");
            return new ValidationResult(false, errors);
        }

        // Validate parameters against schema
        ValidationResult schemaValidation = validateParametersAgainstSchema(parameters, schema);
        if (!schemaValidation.isValid()) {
            errors.addAll(schemaValidation.getErrors());
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }

    /**
     * Validate parameters against the tool schema.
     * 
     * @param parameters Tool parameters
     * @param schema Tool schema
     * @return Validation result
     */
    private ValidationResult validateParametersAgainstSchema(Map<String, Object> parameters,
            Map<String, Object> schema) {
        java.util.List<String> errors = new java.util.ArrayList<>();

        if (schema == null || schema.isEmpty()) {
            return new ValidationResult(true, errors); // No schema to validate against
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) schema.get("properties");
        if (properties == null) {
            return new ValidationResult(true, errors); // No properties to validate
        }

        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            String paramName = entry.getKey();
            @SuppressWarnings("unchecked")
            Map<String, Object> paramSchema = (Map<String, Object>) entry.getValue();

            // Check if parameter is required
            @SuppressWarnings("unchecked")
            java.util.List<String> required = (java.util.List<String>) schema.get("required");
            boolean isRequired = required != null && required.contains(paramName);

            Object paramValue = parameters.get(paramName);

            // Validate required parameters
            if (isRequired && (paramValue == null
                    || (paramValue instanceof String && ((String) paramValue).trim().isEmpty()))) {
                errors.add("Required parameter missing: " + paramName);
                continue;
            }

            // Skip validation for optional parameters that are not provided
            if (!isRequired && paramValue == null) {
                continue;
            }

            // Validate parameter type
            String expectedType = (String) paramSchema.get("type");
            if (expectedType != null && paramValue != null) {
                if (!isValidType(paramValue, expectedType)) {
                    errors.add("Parameter '" + paramName + "' has invalid type. Expected: " + expectedType + ", Got: "
                            + paramValue.getClass().getSimpleName());
                }
            }
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }

    /**
     * Check if a value matches the expected type.
     * 
     * @param value Parameter value
     * @param expectedType Expected type
     * @return true if type is valid
     */
    private boolean isValidType(Object value, String expectedType) {
        if (value == null) {
            return true; // Null values are handled by required validation
        }

        switch (expectedType.toLowerCase()) {
            case "string":
                return value instanceof String;
            case "number":
                return value instanceof Number;
            case "integer":
                return value instanceof Integer || value instanceof Long;
            case "boolean":
                return value instanceof Boolean;
            case "object":
                return value instanceof Map;
            case "array":
                return value instanceof java.util.List;
            default:
                return true; // Unknown types are accepted
        }
    }

    /**
     * Convert MCP tool context to AIAction context.
     * 
     * @param mcpContext MCP tool context
     * @return AIAction context
     */
    private AIActionContext convertToAIActionContext(MCPToolContext mcpContext) {
        // Create a basic AIAction context using builder pattern
        return AIActionContext.builder().protocol("mcp").clientId("mcp-client")
                .sessionId("mcp-session-" + System.currentTimeMillis())
                .correlationId("mcp-" + System.currentTimeMillis()).priority("normal").build();
    }

    /**
     * Convert AIActionResult to MCPToolResult.
     * 
     * @param actionResult AIAction result
     * @param toolId Tool identifier
     * @param startTime Execution start time
     * @return MCP tool result
     */
    private MCPToolResult convertActionResult(AIActionResult actionResult, String toolId, long startTime) {
        long executionTime = System.currentTimeMillis() - startTime;

        if (actionResult.isSuccess()) {
            return MCPToolResult.successJson(toolId, actionResult.getData(), executionTime);
        } else {
            return MCPToolResult.error(toolId, actionResult.getMessage(), executionTime);
        }
    }

    /**
     * Validation result for tool execution.
     */
    private static class ValidationResult {
        private final boolean valid;
        private final java.util.List<String> errors;

        public ValidationResult(boolean valid, java.util.List<String> errors) {
            this.valid = valid;
            this.errors = errors;
        }

        public boolean isValid() {
            return valid;
        }

        public java.util.List<String> getErrors() {
            return errors;
        }
    }

    @Override
    public MCPToolMetadata getMetadata() {
        return MCPToolMetadata.builder().version("1.0.0").author("openHAB").description(getDescription()).build();
    }

    /**
     * Get the underlying AIAction.
     * 
     * @return AIAction instance
     */
    public AIAction getAction() {
        return action;
    }
}
