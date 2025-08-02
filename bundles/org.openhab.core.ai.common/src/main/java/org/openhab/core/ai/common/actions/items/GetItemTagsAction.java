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
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataRegistry;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to retrieve tag information for items
 * 
 * 
 */
@Component(service = AIAction.class, immediate = true)
@NonNullByDefault
public class GetItemTagsAction implements AIAction {

    private static final Logger logger = LoggerFactory.getLogger(GetItemTagsAction.class);

    @Reference
    private @Nullable ItemRegistry itemRegistry;

    @Reference
    private @Nullable MetadataRegistry metadataRegistry;

    @Override
    public String getActionId() {
        return "openhab.items.get_tags";
    }

    @Override
    public String getActionName() {
        return "Get Item Tags";
    }

    @Override
    public String getDescription() {
        return "Retrieve tag information for items";
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
                Map.of("type", "string", "description", "The name of the item to get tags for", "required", true));
        properties.put("includeTagDetails", Map.of("type", "boolean", "description",
                "Include detailed information about each tag", "default", true));
        properties.put("includeTagProperties",
                Map.of("type", "boolean", "description", "Include tag properties and configuration", "default", false));
        properties.put("filterByNamespace", Map.of("type", "string", "description",
                "Filter tags by specific namespace (optional)", "required", false));

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
        properties.put("tags", Map.of("type", "array"));
        properties.put("tagDetails", Map.of("type", "object"));
        properties.put("totalTags", Map.of("type", "integer"));
        properties.put("namespaces", Map.of("type", "array"));
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
            Boolean includeTagDetails = (Boolean) parameters.getOrDefault("includeTagDetails", true);
            Boolean includeTagProperties = (Boolean) parameters.getOrDefault("includeTagProperties", false);
            String filterByNamespace = (String) parameters.get("filterByNamespace");

            logger.debug("Getting tags for item: {} includeDetails: {}", itemName, includeTagDetails);

            // Get the item
            Item item = itemRegistry.getItem(itemName);

            Map<String, Object> result = new HashMap<>();
            result.put("itemName", itemName);
            result.put("success", true);
            result.put("timestamp", System.currentTimeMillis());

            // Check if metadata registry is available
            if (metadataRegistry != null) {
                // Get all metadata for this item
                List<Metadata> allItemMetadata = metadataRegistry.getAll().stream()
                        .filter(metadata -> metadata.getUID().getItemName().equals(itemName)).toList();

                // Extract tags from metadata
                List<String> tags = allItemMetadata.stream().map(metadata -> metadata.getUID().getNamespace())
                        .distinct().toList();

                // Apply namespace filter if specified
                List<String> filteredTags = tags;
                if (filterByNamespace != null && !filterByNamespace.trim().isEmpty()) {
                    filteredTags = tags.stream().filter(tag -> tag.equals(filterByNamespace)).toList();
                }

                result.put("tags", filteredTags);
                result.put("totalTags", filteredTags.size());
                result.put("namespaces", filteredTags);

                if (includeTagDetails && !filteredTags.isEmpty()) {
                    Map<String, Object> tagDetails = new HashMap<>();

                    for (String tag : filteredTags) {
                        Map<String, Object> tagInfo = new HashMap<>();

                        // Get metadata for this tag
                        MetadataKey key = new MetadataKey(tag, itemName);
                        Metadata metadata = metadataRegistry.get(key);

                        if (metadata != null) {
                            tagInfo.put("value", metadata.getValue());

                            if (includeTagProperties) {
                                tagInfo.put("configuration", metadata.getConfiguration());
                            }
                        }

                        tagDetails.put(tag, tagInfo);
                    }

                    result.put("tagDetails", tagDetails);
                }

                if (filteredTags.isEmpty()) {
                    result.put("note", "No tags found for item: " + itemName);
                }
            } else {
                // Fallback when metadata registry is not available
                result.put("tags", List.of());
                result.put("tagDetails", new HashMap<>());
                result.put("totalTags", 0);
                result.put("namespaces", List.of());
                result.put("note", "Metadata registry not available");
            }

            long executionTime = System.currentTimeMillis() - executionStartTime;
            logger.debug("Successfully retrieved tags for item: {} in {}ms", itemName, executionTime);

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
            logger.error("Error getting item tags", e);
            throw new AIActionException(getActionId(), "Failed to get item tags: " + e.getMessage(), e);
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
                .description("Retrieve tag information for openHAB items")
                .tags(List.of("items", "tags", "metadata", "properties"))
                .documentation(
                        "Retrieves tag information for openHAB items including tag names, values, and properties. Tags are derived from metadata namespaces.")
                .examples(List.of("Get all tags: {\"itemName\": \"LivingRoom_Light\"}",
                        "Get tags with details: {\"itemName\": \"LivingRoom_Light\", \"includeTagDetails\": true}",
                        "Get tags with properties: {\"itemName\": \"LivingRoom_Light\", \"includeTagProperties\": true}",
                        "Filter by namespace: {\"itemName\": \"LivingRoom_Light\", \"filterByNamespace\": \"semantics\"}"))
                .build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("async", true);
        capabilities.put("validation", true);
        capabilities.put("tagRetrieval", true);
        capabilities.put("tagDetails", true);
        capabilities.put("tagProperties", true);
        capabilities.put("namespaceFiltering", true);
        capabilities.put("metadataRegistry", true);
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
