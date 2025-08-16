package org.openhab.core.ai.action.library.items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.api.Action;
import org.openhab.core.ai.action.api.ActionContext;
import org.openhab.core.ai.action.api.ActionException;
import org.openhab.core.ai.action.api.ActionMetadata;
import org.openhab.core.ai.action.api.ActionResult;
import org.openhab.core.ai.action.api.ActionValidationResult;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.events.ItemCommandEvent;
import org.openhab.core.items.events.ItemEventFactory;
import org.openhab.core.items.events.ItemStateEvent;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.TypeParser;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to set the state of a specific item
 * 
 * @author Kai Kreuzer - Initial contribution
 */
@Component(service = Action.class, immediate = true)
@NonNullByDefault
public class SetItemStateAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(SetItemStateAction.class);

    @Reference
    private @Nullable ItemRegistry itemRegistry;

    @Reference
    private @Nullable EventPublisher eventPublisher;

    @Override
    public String getActionId() {
        return "openhab.items.set_state";
    }

    @Override
    public String getActionName() {
        return "Set Item State";
    }

    @Override
    public String getDescription() {
        return "Set the state of a specific item";
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
        properties.put("itemName",
                Map.of("type", "string", "description", "The name of the item to set the state for", "required", true));
        properties.put("state",
                Map.of("type", "string", "description", "The new state value to set", "required", true));
        properties.put("validateState", Map.of("type", "boolean", "description",
                "Validate that the state is valid for the item type", "default", true));
        properties.put("useCommand", Map.of("type", "boolean", "description",
                "Use sendCommand instead of postUpdate (for items that need commands)", "default", false));

        schema.put("properties", properties);
        schema.put("required", List.of("itemName", "state"));
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("itemName", Map.of("type", "string"));
        properties.put("previousState", Map.of("type", "string"));
        properties.put("newState", Map.of("type", "string"));
        properties.put("success", Map.of("type", "boolean"));
        properties.put("timestamp", Map.of("type", "number"));
        properties.put("method", Map.of("type", "string"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public ActionValidationResult validateParameters(Map<String, Object> parameters) {
        if (parameters == null) {
            return ActionValidationResult.invalid(List.of("Parameters cannot be null"));
        }

        String itemName = (String) parameters.get("itemName");
        if (itemName == null || itemName.trim().isEmpty()) {
            return ActionValidationResult.invalid(List.of("Item name is required and cannot be empty"));
        }

        String state = (String) parameters.get("state");
        if (state == null || state.trim().isEmpty()) {
            return ActionValidationResult.invalid(List.of("State is required and cannot be empty"));
        }

        // Validate that the item exists
        try {
            Item item = itemRegistry.getItem(itemName);

            // Validate state if requested
            Boolean validateState = (Boolean) parameters.getOrDefault("validateState", true);
            if (validateState) {
                State parsedState = TypeParser.parseState(item.getAcceptedDataTypes(), state);
                if (parsedState == null) {
                    return ActionValidationResult
                            .invalid(List.of("Invalid state '" + state + "' for item type '" + item.getType() + "'"));
                }
            }
        } catch (ItemNotFoundException e) {
            return ActionValidationResult.invalid(List.of("Item not found: " + itemName));
        } catch (Exception e) {
            return ActionValidationResult.invalid(List.of("Error validating parameters: " + e.getMessage()));
        }

        return ActionValidationResult.valid(parameters);
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ActionContext context) throws ActionException {
        long startTime = System.currentTimeMillis();

        try {
            String itemName = (String) parameters.get("itemName");
            String stateValue = (String) parameters.get("state");
            boolean validateState = (Boolean) parameters.getOrDefault("validateState", true);
            boolean useCommand = (Boolean) parameters.getOrDefault("useCommand", false);

            logger.debug("Setting state for item: {} to: {} (useCommand: {})", itemName, stateValue, useCommand);

            if (eventPublisher == null) {
                throw new ActionException(getActionId(), "Event publisher not available");
            }

            Item item = itemRegistry.getItem(itemName);
            State previousState = item.getState();

            final State newState;
            final Command newCommand;
            final String method;

            if (validateState) {
                if (useCommand) {
                    Command tempCommand = TypeParser.parseCommand(item.getAcceptedCommandTypes(), stateValue);
                    if (tempCommand == null) {
                        throw new ActionException(getActionId(),
                                "Invalid command '" + stateValue + "' for item type '" + item.getType() + "'");
                    }
                    newCommand = tempCommand;
                    newState = null;
                    method = "sendCommand";
                } else {
                    State tempState = TypeParser.parseState(item.getAcceptedDataTypes(), stateValue);
                    if (tempState == null) {
                        throw new ActionException(getActionId(),
                                "Invalid state '" + stateValue + "' for item type '" + item.getType() + "'");
                    }
                    newState = tempState;
                    newCommand = null;
                    method = "postUpdate";
                }
            } else {
                if (useCommand) {
                    Command tempCommand = TypeParser.parseCommand(item.getAcceptedCommandTypes(), stateValue);
                    if (tempCommand == null) {
                        // If parsing fails, create a string command
                        tempCommand = StringType.valueOf(stateValue);
                    }
                    newCommand = tempCommand;
                    newState = null;
                    method = "sendCommand";
                } else {
                    // Try to parse without validation
                    State tempState = TypeParser.parseState(item.getAcceptedDataTypes(), stateValue);
                    if (tempState == null) {
                        // If parsing fails, create a string state
                        tempState = StringType.valueOf(stateValue);
                    }
                    newState = tempState;
                    newCommand = null;
                    method = "postUpdate";
                }
            }

            // Update the item state using the proper openHAB event system
            if (useCommand && newCommand != null) {
                // Create and post ItemCommandEvent using ItemEventFactory
                ItemCommandEvent commandEvent = (ItemCommandEvent) ItemEventFactory.createCommandEvent(itemName,
                        newCommand);
                eventPublisher.post(commandEvent);
                logger.debug("Posted command event to item: {} command: {}", itemName, newCommand);
            } else if (newState != null) {
                // Create and post ItemStateEvent using ItemEventFactory
                ItemStateEvent stateEvent = (ItemStateEvent) ItemEventFactory.createStateEvent(itemName, newState);
                eventPublisher.post(stateEvent);
                logger.debug("Posted state event to item: {} state: {}", itemName, newState);
            } else {
                throw new ActionException(getActionId(), "Failed to parse state or command: " + stateValue);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("itemName", itemName);
            result.put("previousState", previousState != null ? previousState.toString() : "NULL");
            result.put("newState", stateValue);
            result.put("success", true);
            result.put("timestamp", System.currentTimeMillis());
            result.put("method", method);

            long executionTime = System.currentTimeMillis() - startTime;
            logger.debug("Successfully set state for item: {} to: {} in {}ms", itemName, stateValue, executionTime);

            return ActionResult.success(result, executionTime);

        } catch (ItemNotFoundException e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.debug("Item not found: {}", e.getMessage());

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", "Item not found");
            result.put("timestamp", System.currentTimeMillis());

            return ActionResult.success(result, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Error setting item state", e);
            throw new ActionException(getActionId(), "Failed to set item state: " + e.getMessage(), e);
        }
    }

    @Override
    public CompletableFuture<ActionResult> executeAsync(Map<String, Object> parameters, ActionContext context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return execute(parameters, context);
            } catch (ActionException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public ActionMetadata getMetadata() {
        return ActionMetadata.builder().version(getVersion()).author("openHAB")
                .description("Set the state of a specific item").tags(List.of("items", "state", "set"))
                .documentation(
                        "Sets the state of a specific openHAB item with optional state validation. Uses the proper openHAB event system to update item states.")
                .examples(List.of("Set switch to ON: {\"itemName\": \"LivingRoom_Light\", \"state\": \"ON\"}",
                        "Set dimmer to 50%: {\"itemName\": \"LivingRoom_Dimmer\", \"state\": \"50\"}",
                        "Set string item: {\"itemName\": \"LivingRoom_Message\", \"state\": \"Hello World\"}",
                        "Set without validation: {\"itemName\": \"LivingRoom_Light\", \"state\": \"ON\", \"validateState\": false}",
                        "Send command instead of update: {\"itemName\": \"LivingRoom_Light\", \"state\": \"ON\", \"useCommand\": true}"))
                .build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("async", true);
        capabilities.put("validation", true);
        capabilities.put("commands", true);
        capabilities.put("updates", true);
        return capabilities;
    }

    @Override
    public void initialize(ActionContext context) {
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
