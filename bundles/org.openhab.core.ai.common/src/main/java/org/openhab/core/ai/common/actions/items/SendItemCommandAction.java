package org.openhab.core.ai.common.actions.items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.api.action.AIAction;
import org.openhab.core.ai.common.api.action.AIActionContext;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.openhab.core.ai.common.api.action.AIActionMetadata;
import org.openhab.core.ai.common.api.action.AIActionResult;
import org.openhab.core.ai.common.api.action.AIActionValidationResult;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.events.ItemCommandEvent;
import org.openhab.core.items.events.ItemEventFactory;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.TypeParser;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action for sending item commands in openHAB.
 * 
 * This action provides functionality to send commands
 * to items for control and automation.
 * 
 * @author AI Assistant
 * @since 1.0.0
 */
@NonNullByDefault
public class SendItemCommandAction implements AIAction {

    private static final Logger logger = LoggerFactory.getLogger(SendItemCommandAction.class);

    @Reference
    private @Nullable ItemRegistry itemRegistry;

    @Reference
    private @Nullable EventPublisher eventPublisher;

    @Override
    public String getActionId() {
        return "openhab.items.send_command";
    }

    @Override
    public String getActionName() {
        return "Send Item Command";
    }

    @Override
    public String getDescription() {
        return "Send a command to a specific item";
    }

    @Override
    public String getCategory() {
        return "items";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public Map<String, Object> getParameterSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("itemName", Map.of("type", "string", "description",
                "The name of the item to send the command to", "required", true));
        properties.put("command",
                Map.of("type", "string", "description", "The command to send to the item", "required", true));
        properties.put("validateCommand", Map.of("type", "boolean", "description",
                "Validate that the command is valid for the item type", "default", true));
        properties.put("forceCommand", Map.of("type", "boolean", "description",
                "Force sending the command even if validation fails (creates string command)", "default", false));

        schema.put("properties", properties);
        schema.put("required", List.of("itemName", "command"));
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("itemName", Map.of("type", "string"));
        properties.put("command", Map.of("type", "string"));
        properties.put("success", Map.of("type", "boolean"));
        properties.put("timestamp", Map.of("type", "number"));
        properties.put("validationPassed", Map.of("type", "boolean"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public AIActionValidationResult validateParameters(Map<String, Object> parameters) {
        if (parameters == null) {
            return AIActionValidationResult.invalid(List.of("Parameters cannot be null"));
        }

        String itemName = (String) parameters.get("itemName");
        if (itemName == null || itemName.trim().isEmpty()) {
            return AIActionValidationResult.invalid(List.of("Item name is required and cannot be empty"));
        }

        String command = (String) parameters.get("command");
        if (command == null || command.trim().isEmpty()) {
            return AIActionValidationResult.invalid(List.of("Command is required and cannot be empty"));
        }

        // Validate that the item exists
        try {
            Item item = itemRegistry.getItem(itemName);

            // Validate command if requested
            Boolean validateCommand = (Boolean) parameters.getOrDefault("validateCommand", true);
            if (validateCommand) {
                Command parsedCommand = TypeParser.parseCommand(item.getAcceptedCommandTypes(), command);
                if (parsedCommand == null) {
                    return AIActionValidationResult.invalid(
                            List.of("Invalid command '" + command + "' for item type '" + item.getType() + "'"));
                }
            }
        } catch (ItemNotFoundException e) {
            return AIActionValidationResult.invalid(List.of("Item not found: " + itemName));
        } catch (Exception e) {
            return AIActionValidationResult.invalid(List.of("Error validating parameters: " + e.getMessage()));
        }

        return AIActionValidationResult.valid(parameters);
    }

    @Override
    public AIActionResult execute(Map<String, Object> parameters, AIActionContext context) throws AIActionException {
        long startTime = System.currentTimeMillis();

        try {
            String itemName = (String) parameters.get("itemName");
            String commandValue = (String) parameters.get("command");
            boolean validateCommand = (Boolean) parameters.getOrDefault("validateCommand", true);
            boolean forceCommand = (Boolean) parameters.getOrDefault("forceCommand", false);

            logger.debug("Sending command to item: {} command: {} (validate: {}, force: {})", itemName, commandValue,
                    validateCommand, forceCommand);

            if (eventPublisher == null) {
                throw new AIActionException(getActionId(), "Event publisher not available");
            }

            Item item = itemRegistry.getItem(itemName);
            State currentState = item.getState();

            final Command newCommand;
            final boolean validationPassed;

            if (validateCommand) {
                Command tempCommand = TypeParser.parseCommand(item.getAcceptedCommandTypes(), commandValue);
                if (tempCommand == null) {
                    if (forceCommand) {
                        // If parsing fails but force is enabled, create a string command
                        newCommand = org.openhab.core.library.types.StringType.valueOf(commandValue);
                        validationPassed = false;
                        logger.debug("Command validation failed, but forcing string command: {}", commandValue);
                    } else {
                        throw new AIActionException(getActionId(),
                                "Invalid command '" + commandValue + "' for item type '" + item.getType() + "'");
                    }
                } else {
                    newCommand = tempCommand;
                    validationPassed = true;
                }
            } else {
                // Try to parse without validation
                Command tempCommand = TypeParser.parseCommand(item.getAcceptedCommandTypes(), commandValue);
                if (tempCommand == null) {
                    // If parsing fails, create a string command
                    tempCommand = org.openhab.core.library.types.StringType.valueOf(commandValue);
                }
                newCommand = tempCommand;
                validationPassed = false; // Since we didn't validate
            }

            // Send the command using the proper openHAB event system
            ItemCommandEvent commandEvent = (ItemCommandEvent) ItemEventFactory.createCommandEvent(itemName,
                    newCommand);
            eventPublisher.post(commandEvent);
            logger.debug("Posted command event to item: {} command: {}", itemName, newCommand);

            Map<String, Object> result = new HashMap<>();
            result.put("itemName", itemName);
            result.put("command", commandValue);
            result.put("success", true);
            result.put("timestamp", System.currentTimeMillis());
            result.put("validationPassed", validationPassed);
            result.put("currentState", currentState != null ? currentState.toString() : "NULL");

            long executionTime = System.currentTimeMillis() - startTime;
            logger.debug("Successfully sent command to item: {} in {}ms", itemName, executionTime);

            return AIActionResult.success(result, executionTime);

        } catch (ItemNotFoundException e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.debug("Item not found: {}", e.getMessage());

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", "Item not found");
            result.put("timestamp", System.currentTimeMillis());

            return AIActionResult.success(result, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Error sending item command", e);
            throw new AIActionException(getActionId(), "Failed to send item command: " + e.getMessage(), e);
        }
    }

    @Override
    public CompletableFuture<AIActionResult> executeAsync(Map<String, Object> parameters, AIActionContext context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return execute(parameters, context);
            } catch (AIActionException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public AIActionMetadata getMetadata() {
        return AIActionMetadata.builder().version(getVersion()).author("openHAB")
                .description("Send a command to a specific openHAB item").tags(List.of("items", "command", "send"))
                .documentation(
                        "Sends a command to a specific openHAB item using the proper event system. Supports command validation and forced execution.")
                .examples(List.of("Send ON command: {\"itemName\": \"LivingRoom_Light\", \"command\": \"ON\"}",
                        "Send dimmer command: {\"itemName\": \"LivingRoom_Dimmer\", \"command\": \"50\"}",
                        "Send without validation: {\"itemName\": \"LivingRoom_Light\", \"command\": \"ON\", \"validateCommand\": false}",
                        "Force command: {\"itemName\": \"LivingRoom_Light\", \"command\": \"CUSTOM_COMMAND\", \"forceCommand\": true}"))
                .build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("async", true);
        capabilities.put("validation", true);
        capabilities.put("forceExecution", true);
        capabilities.put("commandTypes", List.of("ON", "OFF", "UP", "DOWN", "PLAY", "PAUSE", "STOP", "REWIND",
                "FASTFORWARD", "NEXT", "PREVIOUS", "REFRESH", "MOVE", "INCREASE", "DECREASE", "STRING"));
        return capabilities;
    }

    @Override
    public void initialize(AIActionContext context) {
        // No initialization needed
    }

    @Override
    public void cleanup() {
        // No cleanup needed
    }

    @Override
    public boolean isReady() {
        return itemRegistry != null && eventPublisher != null;
    }
}
