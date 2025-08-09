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
import org.openhab.core.ai.action.api.Action;
import org.openhab.core.ai.action.api.ActionException;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.items.events.ItemCommandEvent;
import org.openhab.core.items.events.ItemStateEvent;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.TypeParser;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action for bulk item operations in openHAB.
 * 
 * This action provides functionality to perform
 * operations on multiple items at once.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class BulkItemOperationsAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(BulkItemOperationsAction.class);

    @Reference
    private @Nullable ItemRegistry itemRegistry;

    @Reference
    private @Nullable MetadataRegistry metadataRegistry;

    @Reference
    private org.openhab.core.events.@Nullable EventPublisher eventPublisher;

    @Override
    public String getActionId() {
        return "openhab.items.bulk_operations";
    }

    @Override
    public String getActionName() {
        return "Bulk Item Operations";
    }

    @Override
    public String getDescription() {
        return "Perform bulk operations on multiple items";
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
        properties.put("operation", Map.of("type", "string", "description",
                "Type of operation: 'setState', 'sendCommand', 'setMetadata', 'setTags'", "required", true));
        properties.put("itemNames",
                Map.of("type", "array", "description", "List of item names to operate on", "required", true));
        properties.put("value",
                Map.of("type", "string", "description", "Value to set (for setState, sendCommand)", "required", false));
        properties.put("metadata", Map.of("type", "object", "description",
                "Metadata to set (for setMetadata operation)", "required", false));
        properties.put("tags",
                Map.of("type", "object", "description", "Tags to set (for setTags operation)", "required", false));
        properties.put("namespace",
                Map.of("type", "string", "description", "Namespace for metadata operations", "required", false));
        properties.put("continueOnError", Map.of("type", "boolean", "description",
                "Continue processing if individual operations fail", "default", true));
        properties.put("validateOnly", Map.of("type", "boolean", "description",
                "Only validate parameters without making changes", "default", false));

        schema.put("properties", properties);
        schema.put("required", List.of("operation", "itemNames"));
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("operation", Map.of("type", "string"));
        properties.put("totalItems", Map.of("type", "integer"));
        properties.put("successfulOperations", Map.of("type", "integer"));
        properties.put("failedOperations", Map.of("type", "integer"));
        properties.put("results", Map.of("type", "object"));
        properties.put("errors", Map.of("type", "array"));
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

        String operation = (String) parameters.get("operation");
        if (operation == null || operation.trim().isEmpty()) {
            return ActionValidationResult.invalid(List.of("Operation is required"));
        }

        List<String> validOperations = List.of("setState", "sendCommand", "setMetadata", "setTags");
        if (!validOperations.contains(operation)) {
            return ActionValidationResult
                    .invalid(List.of("Invalid operation: " + operation + ". Valid operations: " + validOperations));
        }

        Object itemNamesObj = parameters.get("itemNames");
        if (itemNamesObj == null || !(itemNamesObj instanceof List)) {
            return ActionValidationResult.invalid(List.of("itemNames must be an array"));
        }

        @SuppressWarnings("unchecked")
        List<String> itemNames = (List<String>) itemNamesObj;
        if (itemNames.isEmpty()) {
            return ActionValidationResult.invalid(List.of("itemNames cannot be empty"));
        }

        if (itemNames.size() > 100) {
            return ActionValidationResult.invalid(List.of("Cannot operate on more than 100 items at once"));
        }

        // Validate operation-specific parameters
        switch (operation) {
            case "setState":
            case "sendCommand":
                String value = (String) parameters.get("value");
                if (value == null || value.trim().isEmpty()) {
                    return ActionValidationResult.invalid(List.of("Value is required for " + operation + " operation"));
                }
                break;
            case "setMetadata":
                String namespace = (String) parameters.get("namespace");
                if (namespace == null || namespace.trim().isEmpty()) {
                    return ActionValidationResult.invalid(List.of("Namespace is required for setMetadata operation"));
                }
                Object metadataObj = parameters.get("metadata");
                if (metadataObj == null || !(metadataObj instanceof Map)) {
                    return ActionValidationResult
                            .invalid(List.of("Metadata must be an object for setMetadata operation"));
                }
                break;
            case "setTags":
                Object tagsObj = parameters.get("tags");
                if (tagsObj == null || !(tagsObj instanceof Map)) {
                    return ActionValidationResult.invalid(List.of("Tags must be an object for setTags operation"));
                }
                break;
        }

        return ActionValidationResult.valid(parameters);
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ActionContext context) throws ActionException {
        long executionStartTime = System.currentTimeMillis();

        try {
            String operation = (String) parameters.get("operation");
            @SuppressWarnings("unchecked")
            List<String> itemNames = (List<String>) parameters.get("itemNames");
            String value = (String) parameters.get("value");
            @SuppressWarnings("unchecked")
            Map<String, Object> metadata = (Map<String, Object>) parameters.get("metadata");
            @SuppressWarnings("unchecked")
            Map<String, Object> tags = (Map<String, Object>) parameters.get("tags");
            String namespace = (String) parameters.get("namespace");
            Boolean continueOnError = (Boolean) parameters.getOrDefault("continueOnError", true);
            Boolean validateOnly = (Boolean) parameters.getOrDefault("validateOnly", false);

            logger.debug("Performing bulk operation: {} on {} items", operation, itemNames.size());

            Map<String, Object> result = new HashMap<>();
            result.put("operation", operation);
            result.put("totalItems", itemNames.size());
            result.put("success", true);
            result.put("timestamp", System.currentTimeMillis());

            if (validateOnly) {
                result.put("note", "Validation only - no changes made");
                result.put("successfulOperations", 0);
                result.put("failedOperations", 0);
                result.put("results", new HashMap<>());
                result.put("errors", List.of());
                return ActionResult.success(result, System.currentTimeMillis() - executionStartTime);
            }

            Map<String, Object> results = new HashMap<>();
            List<String> errors = new java.util.ArrayList<>();
            int successfulOperations = 0;
            int failedOperations = 0;

            for (String itemName : itemNames) {
                try {
                    Map<String, Object> itemResult = new HashMap<>();

                    switch (operation) {
                        case "setState":
                            itemResult = performSetState(itemName, value);
                            break;
                        case "sendCommand":
                            itemResult = performSendCommand(itemName, value);
                            break;
                        case "setMetadata":
                            itemResult = performSetMetadata(itemName, namespace, metadata);
                            break;
                        case "setTags":
                            itemResult = performSetTags(itemName, tags);
                            break;
                    }

                    results.put(itemName, itemResult);
                    if ((Boolean) itemResult.get("success")) {
                        successfulOperations++;
                    } else {
                        failedOperations++;
                        errors.add(itemName + ": " + itemResult.get("error"));
                    }

                } catch (Exception e) {
                    failedOperations++;
                    String error = itemName + ": " + e.getMessage();
                    errors.add(error);
                    logger.debug("Error processing item {}: {}", itemName, e.getMessage());

                    if (!continueOnError) {
                        throw new ActionException(getActionId(),
                                "Bulk operation failed on item " + itemName + ": " + e.getMessage(), e);
                    }
                }
            }

            result.put("successfulOperations", successfulOperations);
            result.put("failedOperations", failedOperations);
            result.put("results", results);
            result.put("errors", errors);

            long executionTime = System.currentTimeMillis() - executionStartTime;
            logger.debug("Bulk operation completed: {} successful, {} failed in {}ms", successfulOperations,
                    failedOperations, executionTime);

            return ActionResult.success(result, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - executionStartTime;
            logger.error("Error performing bulk operations", e);
            throw new ActionException(getActionId(), "Failed to perform bulk operations: " + e.getMessage(), e);
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
                .description("Perform bulk operations on multiple openHAB items")
                .tags(List.of("items", "bulk", "operations", "batch", "efficiency"))
                .documentation(
                        "Performs bulk operations on multiple openHAB items including state updates, commands, metadata, and tag operations. Supports error handling and validation.")
                .examples(List.of(
                        "Bulk state update: {\"operation\": \"setState\", \"itemNames\": [\"Light1\", \"Light2\"], \"value\": \"ON\"}",
                        "Bulk command: {\"operation\": \"sendCommand\", \"itemNames\": [\"Switch1\", \"Switch2\"], \"value\": \"OFF\"}",
                        "Bulk metadata: {\"operation\": \"setMetadata\", \"itemNames\": [\"Item1\"], \"namespace\": \"semantics\", \"metadata\": {\"value\": \"Light\"}}",
                        "Bulk tags: {\"operation\": \"setTags\", \"itemNames\": [\"Item1\", \"Item2\"], \"tags\": {\"location\": \"LivingRoom\"}}"))
                .build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("async", true);
        capabilities.put("validation", true);
        capabilities.put("bulkOperations", true);
        capabilities.put("stateUpdates", true);
        capabilities.put("commands", true);
        capabilities.put("metadata", true);
        capabilities.put("tags", true);
        capabilities.put("errorHandling", true);
        capabilities.put("batchProcessing", true);
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

    /**
     * Perform set state operation
     */
    private Map<String, Object> performSetState(String itemName, String value) {
        Map<String, Object> result = new HashMap<>();

        try {
            Item item = itemRegistry.getItem(itemName);
            State newState = TypeParser.parseState(item.getAcceptedDataTypes(), value);

            if (newState != null) {
                ItemStateEvent stateEvent = (ItemStateEvent) org.openhab.core.items.events.ItemEventFactory
                        .createStateEvent(itemName, newState);
                eventPublisher.post(stateEvent);
                result.put("success", true);
                result.put("previousState", item.getState() != null ? item.getState().toString() : "NULL");
                result.put("newState", newState.toString());
            } else {
                result.put("success", false);
                String errorValue = value != null ? value : "null";
                result.put("error", "Failed to parse state value: " + errorValue);
            }
        } catch (Exception e) {
            result.put("success", false);
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error";
            result.put("error", errorMessage);
        }

        return result;
    }

    /**
     * Perform send command operation
     */
    private Map<String, Object> performSendCommand(String itemName, String value) {
        Map<String, Object> result = new HashMap<>();

        try {
            Item item = itemRegistry.getItem(itemName);
            Command newCommand = TypeParser.parseCommand(item.getAcceptedCommandTypes(), value);

            if (newCommand != null) {
                ItemCommandEvent commandEvent = (ItemCommandEvent) org.openhab.core.items.events.ItemEventFactory
                        .createCommandEvent(itemName, newCommand);
                eventPublisher.post(commandEvent);
                result.put("success", true);
                result.put("command", newCommand.toString());
            } else {
                result.put("success", false);
                String errorValue = value != null ? value : "null";
                result.put("error", "Failed to parse command value: " + errorValue);
            }
        } catch (Exception e) {
            result.put("success", false);
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error";
            result.put("error", errorMessage);
        }

        return result;
    }

    /**
     * Perform set metadata operation
     */
    private Map<String, Object> performSetMetadata(String itemName, String namespace, Map<String, Object> metadata) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (metadataRegistry == null) {
                result.put("success", false);
                result.put("error", "Metadata registry not available");
                return result;
            }

            String metadataValue = (String) metadata.get("value");
            @SuppressWarnings("unchecked")
            Map<String, Object> configuration = (Map<String, Object>) metadata.get("configuration");

            MetadataKey key = new MetadataKey(namespace, itemName);
            Metadata newMetadata = new Metadata(key, metadataValue, configuration);
            metadataRegistry.add(newMetadata);

            result.put("success", true);
            result.put("namespace", namespace);
            String safeMetadataValue = metadataValue != null ? metadataValue : "";
            result.put("value", safeMetadataValue);
        } catch (Exception e) {
            result.put("success", false);
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error";
            result.put("error", errorMessage);
        }

        return result;
    }

    /**
     * Perform set tags operation
     */
    private Map<String, Object> performSetTags(String itemName, Map<String, Object> tags) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (metadataRegistry == null) {
                result.put("success", false);
                result.put("error", "Metadata registry not available");
                return result;
            }

            int tagsSet = 0;
            for (Map.Entry<String, Object> tagEntry : tags.entrySet()) {
                String tagName = tagEntry.getKey();
                Object tagValue = tagEntry.getValue();

                String value;
                Map<String, Object> configuration = null;

                if (tagValue instanceof String) {
                    value = (String) tagValue;
                } else if (tagValue instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> tagMap = (Map<String, Object>) tagValue;
                    value = (String) tagMap.get("value");
                    configuration = (Map<String, Object>) tagMap.get("configuration");
                } else {
                    value = tagValue.toString();
                }

                MetadataKey key = new MetadataKey(tagName, itemName);
                Metadata newMetadata = new Metadata(key, value, configuration);
                metadataRegistry.add(newMetadata);
                tagsSet++;
            }

            result.put("success", true);
            result.put("tagsSet", tagsSet);
        } catch (Exception e) {
            result.put("success", false);
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error";
            result.put("error", errorMessage);
        }

        return result;
    }
}
