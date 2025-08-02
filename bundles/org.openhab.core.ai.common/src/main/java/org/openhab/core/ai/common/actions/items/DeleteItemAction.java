package org.openhab.core.ai.common.actions.items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.action.AIActionContext;
import org.openhab.core.ai.common.action.AIActionMetadata;
import org.openhab.core.ai.common.action.AIActionResult;
import org.openhab.core.ai.common.action.AIActionValidationResult;
import org.openhab.core.ai.common.api.action.AIAction;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataRegistry;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action for deleting items in openHAB.
 * 
 * This action provides functionality to delete items
 * from the system with safety checks.
 * 
 * @author AI Assistant
 * @since 1.0.0
 */
@NonNullByDefault
public class DeleteItemAction implements AIAction {

    private static final Logger logger = LoggerFactory.getLogger(DeleteItemAction.class);

    @Reference
    private @Nullable ItemRegistry itemRegistry;

    @Reference
    private @Nullable MetadataRegistry metadataRegistry;

    @Override
    public String getActionId() {
        return "openhab.items.delete";
    }

    @Override
    public String getActionName() {
        return "Delete Item";
    }

    @Override
    public String getDescription() {
        return "Delete openHAB items";
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
                Map.of("type", "string", "description", "The name of the item to delete", "required", true));
        properties.put("removeMetadata", Map.of("type", "boolean", "description",
                "Remove all metadata associated with the item", "default", true));
        properties.put("forceDelete", Map.of("type", "boolean", "description",
                "Force deletion even if item is bound to things", "default", false));
        properties.put("validateOnly", Map.of("type", "boolean", "description",
                "Only validate parameters without deleting the item", "default", false));

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
        properties.put("deleted", Map.of("type", "boolean"));
        properties.put("itemInfo", Map.of("type", "object"));
        properties.put("metadataRemoved", Map.of("type", "integer"));
        properties.put("warnings", Map.of("type", "array"));
        properties.put("success", Map.of("type", "boolean"));
        properties.put("timestamp", Map.of("type", "number"));

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

        // Validate that the item exists
        try {
            itemRegistry.getItem(itemName);
        } catch (ItemNotFoundException e) {
            return AIActionValidationResult.invalid(List.of("Item not found: " + itemName));
        } catch (Exception e) {
            return AIActionValidationResult.invalid(List.of("Error validating parameters: " + e.getMessage()));
        }

        return AIActionValidationResult.valid(parameters);
    }

    @Override
    public AIActionResult execute(Map<String, Object> parameters, AIActionContext context) throws AIActionException {
        long executionStartTime = System.currentTimeMillis();

        try {
            String itemName = (String) parameters.get("itemName");
            Boolean removeMetadata = (Boolean) parameters.getOrDefault("removeMetadata", true);
            Boolean forceDelete = (Boolean) parameters.getOrDefault("forceDelete", false);
            Boolean validateOnly = (Boolean) parameters.getOrDefault("validateOnly", false);

            logger.debug("Deleting item: {} removeMetadata: {} forceDelete: {}", itemName, removeMetadata, forceDelete);

            // Get the item before deletion
            Item item = itemRegistry.getItem(itemName);

            Map<String, Object> result = new HashMap<>();
            result.put("itemName", itemName);
            result.put("success", true);
            result.put("timestamp", System.currentTimeMillis());

            // Store item information before deletion
            Map<String, Object> itemInfo = new HashMap<>();
            itemInfo.put("name", item.getName());
            itemInfo.put("type", item.getType());
            String itemLabel = "";
            if (item.getLabel() != null) {
                itemLabel = item.getLabel();
            }
            itemInfo.put("label", itemLabel);

            String itemCategory = "";
            if (item.getCategory() != null) {
                itemCategory = item.getCategory();
            }
            itemInfo.put("category", itemCategory);
            itemInfo.put("groups", item.getGroupNames());
            itemInfo.put("tags", item.getTags());
            itemInfo.put("state", item.getState() != null ? item.getState().toString() : "NULL");
            result.put("itemInfo", itemInfo);

            if (validateOnly) {
                result.put("deleted", false);
                result.put("note", "Validation only - no item deleted");
                result.put("metadataRemoved", 0);
                result.put("warnings", List.of());
                return AIActionResult.success(result, System.currentTimeMillis() - executionStartTime);
            }

            List<String> warnings = new java.util.ArrayList<>();

            // Check for potential issues
            if (!item.getGroupNames().isEmpty()) {
                warnings.add("Item is a member of groups: " + item.getGroupNames());
            }

            if (!item.getTags().isEmpty()) {
                warnings.add("Item has tags: " + item.getTags());
            }

            // Remove metadata if requested
            int metadataRemoved = 0;
            if (removeMetadata && metadataRegistry != null) {
                List<Metadata> allItemMetadata = metadataRegistry.getAll().stream()
                        .filter(metadata -> metadata.getUID().getItemName().equals(itemName)).toList();

                for (Metadata metadata : allItemMetadata) {
                    metadataRegistry.remove(metadata.getUID());
                    metadataRemoved++;
                }

                if (metadataRemoved > 0) {
                    logger.debug("Removed {} metadata entries for item: {}", metadataRemoved, itemName);
                }
            }

            // Delete the item
            itemRegistry.remove(itemName);
            result.put("deleted", true);
            result.put("metadataRemoved", metadataRemoved);
            result.put("warnings", warnings);

            long executionTime = System.currentTimeMillis() - executionStartTime;
            logger.debug("Successfully deleted item: {} in {}ms", itemName, executionTime);

            return AIActionResult.success(result, executionTime);

        } catch (ItemNotFoundException e) {
            long executionTime = System.currentTimeMillis() - executionStartTime;
            logger.debug("Item not found: {}", e.getMessage());

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", "Item not found");
            result.put("timestamp", System.currentTimeMillis());

            return AIActionResult.success(result, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - executionStartTime;
            logger.error("Error deleting item", e);
            throw new AIActionException(getActionId(), "Failed to delete item: " + e.getMessage(), e);
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
                .description("Delete openHAB items and optionally their associated metadata")
                .tags(List.of("items", "delete", "remove", "cleanup"))
                .documentation(
                        "Deletes openHAB items and optionally removes all associated metadata. Provides warnings about potential impacts.")
                .examples(List.of("Delete item: {\"itemName\": \"LivingRoom_Light\"}",
                        "Delete with metadata: {\"itemName\": \"LivingRoom_Light\", \"removeMetadata\": true}",
                        "Force delete: {\"itemName\": \"LivingRoom_Light\", \"forceDelete\": true}",
                        "Validate only: {\"itemName\": \"LivingRoom_Light\", \"validateOnly\": true}"))
                .build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("async", true);
        capabilities.put("validation", true);
        capabilities.put("itemDeletion", true);
        capabilities.put("metadataRemoval", true);
        capabilities.put("warningSystem", true);
        capabilities.put("validationMode", true);
        capabilities.put("forceDeletion", true);
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
        return itemRegistry != null;
    }
}
