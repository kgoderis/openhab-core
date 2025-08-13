package org.openhab.core.ai.tool.prompts.library;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Automation Prompt Implementation for openHAB.
 * 
 * This class provides parameterized prompt templates with argument validation
 * for openHAB automation operations, as specified in section 16.2.14.2.3 of BRAIN_PLAN.md.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class AutomationPrompt {

    private static final Logger logger = LoggerFactory.getLogger(AutomationPrompt.class);

    public static final String PROMPT_NAME = "automation_control";
    public static final String PROMPT_DESCRIPTION = "Control openHAB automation rules and workflows";

    // Performance monitoring
    private final AtomicLong totalExecutions = new AtomicLong(0);
    private final AtomicLong successfulExecutions = new AtomicLong(0);
    private final AtomicLong failedExecutions = new AtomicLong(0);
    private final AtomicLong totalExecutionTimeMs = new AtomicLong(0);

    public AutomationPrompt() {
        // Constructor for automation prompt
    }

    /**
     * Execute the automation control prompt with the given arguments.
     *
     * @param arguments the prompt arguments
     * @return the execution result
     */
    public org.openhab.core.ai.tool.registry.PromptExecutionResult execute(Map<String, Object> arguments) {
        totalExecutions.incrementAndGet();
        long startTime = System.currentTimeMillis();

        try {
            logger.debug("Executing automation control prompt with arguments: {}", arguments);

            // Validate arguments
            String ruleUID = validateAndGetString(arguments, "ruleUID");
            String action = validateAndGetString(arguments, "action");
            String parameters = getOptionalString(arguments, "parameters");

            // Validate action
            if (!isValidAction(action)) {
                String errorMessage = "Invalid action: " + action
                        + ". Valid actions are: ENABLE, DISABLE, EXECUTE, GET_STATUS";
                logger.warn(errorMessage);
                failedExecutions.incrementAndGet();
                return new org.openhab.core.ai.tool.registry.PromptExecutionResult(false, errorMessage, null);
            }

            // Execute the automation action
            String result = executeAutomationAction(ruleUID, action, parameters);

            successfulExecutions.incrementAndGet();
            logger.debug("Automation control prompt executed successfully: {} {} {}", ruleUID, action, parameters);

            return new org.openhab.core.ai.tool.registry.PromptExecutionResult(true, null, result);

        } catch (Exception e) {
            String errorMessage = "Error executing automation control prompt: " + e.getMessage();
            logger.error(errorMessage, e);
            failedExecutions.incrementAndGet();
            return new org.openhab.core.ai.tool.registry.PromptExecutionResult(false, errorMessage, null);
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            totalExecutionTimeMs.addAndGet(executionTime);
        }
    }

    private String validateAndGetString(Map<String, Object> arguments, String key) {
        Object value = arguments.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Required argument missing: " + key);
        }
        if (!(value instanceof String)) {
            throw new IllegalArgumentException("Argument must be a string: " + key);
        }
        return (String) value;
    }

    private @Nullable String getOptionalString(Map<String, Object> arguments, String key) {
        Object value = arguments.get(key);
        return value instanceof String ? (String) value : null;
    }

    private boolean isValidAction(String action) {
        return "ENABLE".equals(action) || "DISABLE".equals(action) || "EXECUTE".equals(action)
                || "GET_STATUS".equals(action);
    }

    private String executeAutomationAction(String ruleUID, String action, @Nullable String parameters) {
        // TODO: Implement actual automation rule execution
        // This would typically involve interacting with the rule engine
        // For now, return a simulation result

        String result = String.format("Automation action '%s' would be performed on rule '%s'", action, ruleUID);
        if (parameters != null) {
            result += String.format(" with parameters '%s'", parameters);
        }

        logger.info("Simulated automation action: {} {} {}", ruleUID, action, parameters);
        return result;
    }

    /**
     * Get performance metrics for this prompt.
     *
     * @return performance metrics as a map
     */
    public Map<String, Object> getPerformanceMetrics() {
        Map<String, Object> metrics = new java.util.HashMap<>();
        metrics.put("totalExecutions", totalExecutions.get());
        metrics.put("successfulExecutions", successfulExecutions.get());
        metrics.put("failedExecutions", failedExecutions.get());
        metrics.put("totalExecutionTimeMs", totalExecutionTimeMs.get());
        metrics.put("averageExecutionTimeMs",
                totalExecutions.get() > 0 ? totalExecutionTimeMs.get() / totalExecutions.get() : 0);
        metrics.put("successRate",
                totalExecutions.get() > 0 ? (double) successfulExecutions.get() / totalExecutions.get() : 0.0);
        return metrics;
    }

    /**
     * Get the prompt template with placeholders.
     *
     * @return the prompt template
     */
    public String getPromptTemplate() {
        return "Control the openHAB automation rule '{ruleUID}' with action '{action}'"
                + ". This will perform the specified operation on the automation rule.";
    }

    /**
     * Get usage examples for this prompt.
     *
     * @return usage examples
     */
    public String[] getUsageExamples() {
        return new String[] { "Enable a rule: ruleUID='MorningRoutine', action='ENABLE'",
                "Disable a rule: ruleUID='NightMode', action='DISABLE'",
                "Execute a rule: ruleUID='EmergencyAlert', action='EXECUTE'",
                "Get rule status: ruleUID='TemperatureControl', action='GET_STATUS'",
                "Execute with parameters: ruleUID='CustomRule', action='EXECUTE', parameters='param1=value1'" };
    }

    /**
     * Get the prompt argument schema.
     *
     * @return the argument schema
     */
    public Map<String, Object> getArgumentSchema() {
        Map<String, Object> schema = new java.util.HashMap<>();

        // ruleUID argument
        Map<String, Object> ruleUIDSchema = new java.util.HashMap<>();
        ruleUIDSchema.put("type", "string");
        ruleUIDSchema.put("description", "UID of the automation rule");
        ruleUIDSchema.put("required", true);
        schema.put("ruleUID", ruleUIDSchema);

        // action argument
        Map<String, Object> actionSchema = new java.util.HashMap<>();
        actionSchema.put("type", "string");
        actionSchema.put("description", "Action to perform on the rule (ENABLE, DISABLE, EXECUTE, GET_STATUS)");
        actionSchema.put("required", true);
        actionSchema.put("enum", new String[] { "ENABLE", "DISABLE", "EXECUTE", "GET_STATUS" });
        schema.put("action", actionSchema);

        // parameters argument
        Map<String, Object> parametersSchema = new java.util.HashMap<>();
        parametersSchema.put("type", "string");
        parametersSchema.put("description", "Optional parameters for the action");
        parametersSchema.put("required", false);
        schema.put("parameters", parametersSchema);

        return schema;
    }

    // PromptExecutionResult unified to org.openhab.core.ai.tool.registry.PromptExecutionResult
}
