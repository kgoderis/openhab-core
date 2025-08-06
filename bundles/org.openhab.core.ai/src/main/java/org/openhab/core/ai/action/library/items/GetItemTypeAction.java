package org.openhab.core.ai.action.library.items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.ActionContext;
import org.openhab.core.ai.action.ActionMetadata;
import org.openhab.core.ai.action.ActionResult;
import org.openhab.core.ai.action.ActionValidationResult;
import org.openhab.core.ai.api.action.Action;
import org.openhab.core.ai.api.action.ActionException;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.types.State;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action for getting item type information in openHAB.
 * 
 * This action provides functionality to retrieve
 * type information for items.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class GetItemTypeAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(GetItemTypeAction.class);

    @Reference
    private @Nullable ItemRegistry itemRegistry;

    @Override
    public String getActionId() {
        return "openhab.items.get_type";
    }

    @Override
    public String getActionName() {
        return "Get Item Type";
    }

    @Override
    public String getDescription() {
        return "Retrieve type information for items";
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
                "The name of the item to get type information for", "required", true));
        properties.put("includeAcceptedTypes",
                Map.of("type", "boolean", "description", "Include accepted command and state types", "default", true));
        properties.put("includeTypeDetails",
                Map.of("type", "boolean", "description", "Include detailed type information", "default", false));

        schema.put("properties", properties);
        schema.put("required", List.of("itemName"));
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("itemName", Map.of("type", "string"));
        properties.put("itemType", Map.of("type", "string"));
        properties.put("acceptedCommands", Map.of("type", "array"));
        properties.put("acceptedStates", Map.of("type", "array"));
        properties.put("typeDetails", Map.of("type", "object"));
        properties.put("success", Map.of("type", "boolean"));
        properties.put("timestamp", Map.of("type", "number"));

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

        // Validate that the item exists
        try {
            itemRegistry.getItem(itemName);
        } catch (ItemNotFoundException e) {
            return ActionValidationResult.invalid(List.of("Item not found: " + itemName));
        } catch (Exception e) {
            return ActionValidationResult.invalid(List.of("Error validating parameters: " + e.getMessage()));
        }

        return ActionValidationResult.valid(parameters);
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ActionContext context) throws ActionException {
        long executionStartTime = System.currentTimeMillis();

        try {
            String itemName = (String) parameters.get("itemName");
            Boolean includeAcceptedTypes = (Boolean) parameters.getOrDefault("includeAcceptedTypes", true);
            Boolean includeTypeDetails = (Boolean) parameters.getOrDefault("includeTypeDetails", false);

            logger.debug("Getting type information for item: {} includeAcceptedTypes: {}", itemName,
                    includeAcceptedTypes);

            // Get the item
            Item item = itemRegistry.getItem(itemName);

            Map<String, Object> result = new HashMap<>();
            result.put("itemName", itemName != null ? itemName : "");
            result.put("itemType", item.getType());
            result.put("success", true);
            result.put("timestamp", System.currentTimeMillis());

            if (includeAcceptedTypes) {
                // Get accepted command types
                List<String> acceptedCommands = item.getAcceptedCommandTypes().stream().map(Class::getSimpleName)
                        .toList();
                result.put("acceptedCommands", acceptedCommands);

                // Get accepted state types
                List<String> acceptedStates = item.getAcceptedDataTypes().stream().map(Class::getSimpleName).toList();
                result.put("acceptedStates", acceptedStates);
            }

            if (includeTypeDetails) {
                Map<String, Object> typeDetails = new HashMap<>();

                // Current state type
                State currentState = item.getState();
                if (currentState != null) {
                    typeDetails.put("currentStateType", currentState.getClass().getSimpleName());
                    typeDetails.put("currentStateValue", currentState.toString());
                }

                // Item category and tags
                String category = item.getCategory() != null ? item.getCategory() : "";
                typeDetails.put("category", category);
                String label = item.getLabel() != null ? item.getLabel() : "";
                typeDetails.put("label", label);

                // Group information if applicable
                if (item instanceof org.openhab.core.items.GroupItem groupItem) {
                    typeDetails.put("isGroup", true);
                    String baseItemType = "";
                    if (groupItem.getBaseItem() != null) {
                        baseItemType = groupItem.getBaseItem().getType();
                    }
                    typeDetails.put("baseItemType", baseItemType);

                    String function = "";
                    if (groupItem.getFunction() != null) {
                        function = groupItem.getFunction().toString();
                    }
                    typeDetails.put("function", function);
                } else {
                    typeDetails.put("isGroup", false);
                }

                result.put("typeDetails", typeDetails);
            }

            long executionTime = System.currentTimeMillis() - executionStartTime;
            logger.debug("Successfully retrieved type information for item: {} in {}ms", itemName, executionTime);

            return ActionResult.success(result, executionTime);

        } catch (ItemNotFoundException e) {
            long executionTime = System.currentTimeMillis() - executionStartTime;
            logger.debug("Item not found: {}", e.getMessage());

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", "Item not found");
            result.put("timestamp", System.currentTimeMillis());

            return ActionResult.success(result, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - executionStartTime;
            logger.error("Error getting item type information", e);
            throw new ActionException(getActionId(), "Failed to get item type information: " + e.getMessage(), e);
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
                .description("Retrieve type information for openHAB items")
                .tags(List.of("items", "types", "commands", "states", "validation"))
                .documentation(
                        "Retrieves type information for openHAB items including item type, accepted command and state types, and detailed type information.")
                .examples(List.of("Get basic type info: {\"itemName\": \"LivingRoom_Light\"}",
                        "Get accepted types: {\"itemName\": \"LivingRoom_Light\", \"includeAcceptedTypes\": true}",
                        "Get detailed type info: {\"itemName\": \"LivingRoom_Light\", \"includeTypeDetails\": true}",
                        "Get all type information: {\"itemName\": \"LivingRoom_Light\", \"includeAcceptedTypes\": true, \"includeTypeDetails\": true}"))
                .build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("async", true);
        capabilities.put("validation", true);
        capabilities.put("typeRetrieval", true);
        capabilities.put("acceptedTypes", true);
        capabilities.put("typeDetails", true);
        capabilities.put("commandTypes", true);
        capabilities.put("stateTypes", true);
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
        return itemRegistry != null;
    }
}
