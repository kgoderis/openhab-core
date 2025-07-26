package org.openhab.core.ai.common.actions.items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.api.action.AIAction;
import org.openhab.core.ai.common.api.action.AIActionContext;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.openhab.core.ai.common.api.action.AIActionMetadata;
import org.openhab.core.ai.common.api.action.AIActionResult;
import org.openhab.core.ai.common.api.action.AIActionValidationResult;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemBuilder;
import org.openhab.core.items.ItemBuilderFactory;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataRegistry;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to create new openHAB items
 * 
 * 
 */
@Component(service = AIAction.class, immediate = true)
public class CreateItemAction implements AIAction {

    private static final Logger logger = LoggerFactory.getLogger(CreateItemAction.class);

    @Reference
    private ItemRegistry itemRegistry;

    @Reference
    private ItemBuilderFactory itemBuilderFactory;

    @Reference
    private @Nullable MetadataRegistry metadataRegistry;

    @Override
    public String getActionId() {
        return "openhab.items.create";
    }

    @Override
    public String getActionName() {
        return "Create Item";
    }

    @Override
    public String getDescription() {
        return "Create new openHAB items";
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
                Map.of("type", "string", "description", "The name of the item to create", "required", true));
        properties.put("itemType", Map.of("type", "string", "description",
                "The type of item to create (e.g., 'Switch', 'Dimmer', 'String', 'Number')", "required", true));
        properties.put("label",
                Map.of("type", "string", "description", "Human-readable label for the item", "required", false));
        properties.put("category", Map.of("type", "string", "description", "Category for the item", "required", false));
        properties.put("groups",
                Map.of("type", "array", "description", "List of group names to add the item to", "required", false));
        properties.put("tags",
                Map.of("type", "array", "description", "List of tags to assign to the item", "required", false));
        properties.put("metadata",
                Map.of("type", "object", "description", "Metadata to set for the item", "required", false));
        properties.put("validateOnly", Map.of("type", "boolean", "description",
                "Only validate parameters without creating the item", "default", false));

        schema.put("properties", properties);
        schema.put("required", List.of("itemName", "itemType"));
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("itemName", Map.of("type", "string"));
        properties.put("itemType", Map.of("type", "string"));
        properties.put("created", Map.of("type", "boolean"));
        properties.put("itemInfo", Map.of("type", "object"));
        properties.put("metadataSet", Map.of("type", "integer"));
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

        // Validate item name format
        if (!itemName.matches("^[a-zA-Z0-9_]+$")) {
            return AIActionValidationResult
                    .invalid(List.of("Item name must contain only letters, numbers, and underscores"));
        }

        String itemType = (String) parameters.get("itemType");
        if (itemType == null || itemType.trim().isEmpty()) {
            return AIActionValidationResult.invalid(List.of("Item type is required and cannot be empty"));
        }

        // Validate item type
        List<String> validTypes = List.of("Switch", "Dimmer", "Color", "Number", "String", "DateTime", "Contact",
                "Rollershutter", "Player", "Location");
        if (!validTypes.contains(itemType)) {
            return AIActionValidationResult
                    .invalid(List.of("Invalid item type: " + itemType + ". Valid types: " + validTypes));
        }

        // Check if item already exists
        try {
            itemRegistry.getItem(itemName);
            return AIActionValidationResult.invalid(List.of("Item already exists: " + itemName));
        } catch (org.openhab.core.items.ItemNotFoundException e) {
            // Item doesn't exist, which is what we want
        } catch (Exception e) {
            return AIActionValidationResult.invalid(List.of("Error checking item existence: " + e.getMessage()));
        }

        return AIActionValidationResult.valid(parameters);
    }

    @Override
    public AIActionResult execute(Map<String, Object> parameters, AIActionContext context) throws AIActionException {
        long executionStartTime = System.currentTimeMillis();

        try {
            String itemName = (String) parameters.get("itemName");
            String itemType = (String) parameters.get("itemType");
            String label = (String) parameters.get("label");
            String category = (String) parameters.get("category");
            @SuppressWarnings("unchecked")
            List<String> groups = (List<String>) parameters.get("groups");
            @SuppressWarnings("unchecked")
            List<String> tags = (List<String>) parameters.get("tags");
            @SuppressWarnings("unchecked")
            Map<String, Object> metadata = (Map<String, Object>) parameters.get("metadata");
            Boolean validateOnly = (Boolean) parameters.getOrDefault("validateOnly", false);

            logger.debug("Creating item: {} of type: {} validateOnly: {}", itemName, itemType, validateOnly);

            Map<String, Object> result = new HashMap<>();
            result.put("itemName", itemName);
            result.put("itemType", itemType);
            result.put("success", true);
            result.put("timestamp", System.currentTimeMillis());

            if (validateOnly) {
                result.put("created", false);
                result.put("note", "Validation only - no item created");
                result.put("itemInfo", null);
                result.put("metadataSet", 0);
                return AIActionResult.success(result, System.currentTimeMillis() - executionStartTime);
            }

            // Create the item using ItemBuilder
            ItemBuilder builder = itemBuilderFactory.newItemBuilder(itemType, itemName);

            if (label != null && !label.trim().isEmpty()) {
                builder.withLabel(label);
            }

            if (category != null && !category.trim().isEmpty()) {
                builder.withCategory(category);
            }

            if (groups != null && !groups.isEmpty()) {
                builder.withGroups(groups);
            }

            if (tags != null && !tags.isEmpty()) {
                builder.withTags(new java.util.HashSet<>(tags));
            }

            Item item = builder.build();
            itemRegistry.add(item);

            // Set metadata if provided
            int metadataSet = 0;
            if (metadata != null && !metadata.isEmpty() && metadataRegistry != null) {
                for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                    String namespace = entry.getKey();
                    Object value = entry.getValue();

                    String metadataValue;
                    Map<String, Object> configuration = null;

                    if (value instanceof String) {
                        metadataValue = (String) value;
                    } else if (value instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> metaMap = (Map<String, Object>) value;
                        metadataValue = (String) metaMap.get("value");
                        configuration = (Map<String, Object>) metaMap.get("configuration");
                    } else {
                        metadataValue = value.toString();
                    }

                    if (metadataValue != null) {
                        MetadataKey key = new MetadataKey(namespace, itemName);
                        Metadata meta = new Metadata(key, metadataValue, configuration);
                        metadataRegistry.add(meta);
                        metadataSet++;
                    }
                }
            }

            // Create item info
            Map<String, Object> itemInfo = new HashMap<>();
            itemInfo.put("name", item.getName());
            itemInfo.put("type", item.getType());
            itemInfo.put("label", item.getLabel());
            itemInfo.put("category", item.getCategory());
            itemInfo.put("groups", item.getGroupNames());
            itemInfo.put("tags", item.getTags());
            itemInfo.put("state", item.getState() != null ? item.getState().toString() : "NULL");

            result.put("created", true);
            result.put("itemInfo", itemInfo);
            result.put("metadataSet", metadataSet);

            long executionTime = System.currentTimeMillis() - executionStartTime;
            logger.debug("Successfully created item: {} in {}ms", itemName, executionTime);

            return AIActionResult.success(result, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - executionStartTime;
            logger.error("Error creating item", e);
            throw new AIActionException(getActionId(), "Failed to create item: " + e.getMessage(), e);
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
                .description("Create new openHAB items with various types and configurations")
                .tags(List.of("items", "create", "configuration", "setup"))
                .documentation(
                        "Creates new openHAB items with specified type, label, category, groups, tags, and metadata. Supports validation mode.")
                .examples(List.of("Create basic switch: {\"itemName\": \"LivingRoom_Light\", \"itemType\": \"Switch\"}",
                        "Create with label: {\"itemName\": \"LivingRoom_Light\", \"itemType\": \"Switch\", \"label\": \"Living Room Light\"}",
                        "Create with groups: {\"itemName\": \"LivingRoom_Light\", \"itemType\": \"Switch\", \"groups\": [\"Lights\", \"LivingRoom\"]}",
                        "Create with metadata: {\"itemName\": \"LivingRoom_Light\", \"itemType\": \"Switch\", \"metadata\": {\"semantics\": {\"value\": \"Light\"}}}",
                        "Validate only: {\"itemName\": \"LivingRoom_Light\", \"itemType\": \"Switch\", \"validateOnly\": true}"))
                .build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("async", true);
        capabilities.put("validation", true);
        capabilities.put("itemCreation", true);
        capabilities.put("metadataSetting", true);
        capabilities.put("groupAssignment", true);
        capabilities.put("tagAssignment", true);
        capabilities.put("multipleTypes", true);
        capabilities.put("validationMode", true);
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
        return itemRegistry != null && itemBuilderFactory != null;
    }
}
