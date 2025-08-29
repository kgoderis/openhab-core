package org.openhab.core.ai.tool.prompts.library;

import java.util.HashMap;
import java.util.Map;


import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.patterns.SystemPerformanceMetrics;
import org.openhab.core.ai.tool.registry.PromptExecutionResult;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Item Control Prompt Implementation for openHAB.
 * 
 * This class provides parameterized prompt templates with argument validation
 * for openHAB item operations, as specified in section 16.2.14.2.2 of BRAIN_PLAN.md.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
@Component(service = ItemControlPrompt.class)
public class ItemControlPrompt {

    private static final Logger logger = LoggerFactory.getLogger(ItemControlPrompt.class);

    public static final String PROMPT_NAME = "item_control";
    public static final String PROMPT_DESCRIPTION = "Control openHAB items with parameterized commands";

    private final ItemRegistry itemRegistry;

    // Performance monitoring - now handled by MetricsService
    
    // Metrics service
    @Reference
    private @Nullable MetricsService metricsService;

    public ItemControlPrompt(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    /**
     * Execute the item control prompt with the given arguments.
     *
     * @param arguments the prompt arguments
     * @return the execution result
     */
    public PromptExecutionResult execute(Map<String, Object> arguments) {
        long startTime = System.currentTimeMillis();
        boolean success = false;
        
        try {
            logger.debug("Executing item control prompt with arguments: {}", arguments);

            // Validate arguments
            String itemName = validateAndGetString(arguments, "itemName");
            String action = validateAndGetString(arguments, "action");
            String value = getOptionalString(arguments, "value");

            // Validate item exists
            Item item = itemRegistry.get(itemName);
            if (item == null) {
                String errorMessage = "Item not found: " + itemName;
                logger.warn(errorMessage);
                return new PromptExecutionResult(false, errorMessage, null);
            }

            // Validate action
            if (!isValidAction(action)) {
                String errorMessage = "Invalid action: " + action
                        + ". Valid actions are: ON, OFF, TOGGLE, INCREASE, DECREASE";
                logger.warn(errorMessage);
                return new PromptExecutionResult(false, errorMessage, null);
            }

            // Execute the action
            String result = executeItemAction(item, action, value);

            success = true;
            logger.debug("Item control prompt executed successfully: {} {} {}", itemName, action, value);

            return new PromptExecutionResult(true, null, result);

        } catch (Exception e) {
            String errorMessage = "Error executing item control prompt: " + e.getMessage();
            logger.error(errorMessage, e);
            return new PromptExecutionResult(false, errorMessage, null);
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            recordItemControlOperation("execute", success, executionTime);
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
        return "ON".equals(action) || "OFF".equals(action) || "TOGGLE".equals(action) || "INCREASE".equals(action)
                || "DECREASE".equals(action);
    }

    private String executeItemAction(Item item, String action, @Nullable String value) {
        // TODO: Implement actual item command execution
        // This would typically involve sending commands to the item through the event bus
        // For now, return a simulation result

        String result = String.format("Command '%s' would be sent to item '%s'", action, item.getName());
        if (value != null) {
            result += String.format(" with value '%s'", value);
        }

        logger.info("Simulated item action: {} {} {}", item.getName(), action, value);
        return result;
    }

    /**
     * Get performance metrics for this prompt.
     *
     * @return performance metrics as a map
     */
    public Map<String, Object> getPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        // Metrics now handled by MetricsService - return 0 for removed AtomicLong fields
        metrics.put("totalExecutions", 0);
        metrics.put("successfulExecutions", 0);
        metrics.put("failedExecutions", 0);
        metrics.put("totalExecutionTimeMs", 0);
        metrics.put("averageExecutionTimeMs", 0);
        metrics.put("successRate", 0.0);
        return metrics;
    }

    /**
     * Get the prompt template with placeholders.
     *
     * @return the prompt template
     */
    public String getPromptTemplate() {
        return "Control the openHAB item '{itemName}' with action '{action}'"
                + ". This will execute the specified command on the item.";
    }

    /**
     * Get usage examples for this prompt.
     *
     * @return usage examples
     */
    public String[] getUsageExamples() {
        return new String[] { "Turn on the living room light: itemName='LivingRoom_Light', action='ON'",
                "Turn off the kitchen light: itemName='Kitchen_Light', action='OFF'",
                "Toggle the bedroom light: itemName='Bedroom_Light', action='TOGGLE'",
                "Set dimmer to 50%: itemName='DiningRoom_Dimmer', action='ON', value='50'",
                "Increase brightness: itemName='Office_Light', action='INCREASE'" };
    }

    /**
     * Get the prompt argument schema.
     *
     * @return the argument schema
     */
    public Map<String, Object> getArgumentSchema() {
        Map<String, Object> schema = new HashMap<>();

        // itemName argument
        Map<String, Object> itemNameSchema = new HashMap<>();
        itemNameSchema.put("type", "string");
        itemNameSchema.put("description", "Name of the openHAB item to control");
        itemNameSchema.put("required", true);
        schema.put("itemName", itemNameSchema);

        // action argument
        Map<String, Object> actionSchema = new HashMap<>();
        actionSchema.put("type", "string");
        actionSchema.put("description", "Action to perform (ON, OFF, TOGGLE, INCREASE, DECREASE)");
        actionSchema.put("required", true);
        actionSchema.put("enum", new String[] { "ON", "OFF", "TOGGLE", "INCREASE", "DECREASE" });
        schema.put("action", actionSchema);

        // value argument
        Map<String, Object> valueSchema = new HashMap<>();
        valueSchema.put("type", "string");
        valueSchema.put("description", "Optional value for the action (e.g., percentage for dimmers)");
        valueSchema.put("required", false);
        schema.put("value", valueSchema);

        return schema;
    }

    // Metrics recording methods - replacing removed AtomicLong fields using SystemPerformanceMetrics pattern

    /**
     * Record item control operation - replaces totalExecutions.incrementAndGet(), successfulExecutions.incrementAndGet(), 
     * failedExecutions.incrementAndGet(), and totalExecutionTimeMs.addAndGet()
     * ONE-FOR-ONE REPLACEMENT: Single MetricsService call handles all AtomicLong operations automatically
     */
    private void recordItemControlOperation(String operation, boolean success, long durationMs) {
        try {
            MetricsService metrics = metricsService;
            if (metrics != null) {
                // ONE-FOR-ONE REPLACEMENT: 
                // - totalExecutions.incrementAndGet() -> automatically handled by recordOperation()
                // - successfulExecutions.incrementAndGet() -> automatically handled by recordOperation() 
                // - failedExecutions.incrementAndGet() -> automatically handled by recordOperation()
                // - totalExecutionTimeMs.addAndGet(duration) -> handled by withDuration()
                SystemPerformanceMetrics.recordMessageLatency(metrics, "item-control-prompt", operation, 
                        durationMs, success);
            }
        } catch (Exception e) {
            logger.warn("Failed to record item control operation metric for {}: {}", operation, e.getMessage());
        }
    }

    // PromptExecutionResult unified to org.openhab.core.ai.tool.registry.PromptExecutionResult
}
