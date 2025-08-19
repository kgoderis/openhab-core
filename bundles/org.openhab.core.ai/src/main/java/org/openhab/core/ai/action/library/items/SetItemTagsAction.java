package org.openhab.core.ai.action.library.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.api.Action;
import org.openhab.core.ai.action.api.ActionException;
import org.openhab.core.ai.action.api.ActionMetadata;
import org.openhab.core.ai.action.api.ActionResult;
import org.openhab.core.ai.action.api.ActionValidationResult;
import org.openhab.core.ai.common.context.ExecutionContext;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataRegistry;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action for setting item tags in openHAB.
 * 
 * This action provides functionality to set tags
 * for items for organization and categorization.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class SetItemTagsAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(SetItemTagsAction.class);

    @Reference
    private @Nullable ItemRegistry itemRegistry;

    @Reference
    private @Nullable MetadataRegistry metadataRegistry;

    @Override
    public String getActionId() {
        return "openhab.items.set_tags";
    }

    @Override
    public String getActionName() {
        return "Set Item Tags";
    }

    @Override
    public String getDescription() {
        return "Set or update tag information for items";
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
                Map.of("type", "string", "description", "The name of the item to set tags for", "required", true));
        properties.put("tags", Map.of("type", "object", "description",
                "Map of tag names to tag values and configuration", "required", true));
        properties.put("overwrite",
                Map.of("type", "boolean", "description", "Whether to overwrite existing tags", "default", true));
        properties.put("removeExisting", Map.of("type", "boolean", "description",
                "Remove existing tags not in the provided list", "default", false));
        properties.put("validateOnly", Map.of("type", "boolean", "description",
                "Only validate parameters without making changes", "default", false));

        schema.put("properties", properties);
        schema.put("required", List.of("itemName", "tags"));
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("itemName", Map.of("type", "string"));
        properties.put("tagsSet", Map.of("type", "object"));
        properties.put("tagsRemoved", Map.of("type", "array"));
        properties.put("overwritten", Map.of("type", "array"));
        properties.put("totalTagsSet", Map.of("type", "integer"));
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

        Object tagsObj = parameters.get("tags");
        if (tagsObj == null || !(tagsObj instanceof Map)) {
            return ActionValidationResult.invalid(List.of("Tags must be an object"));
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> tags = (Map<String, Object>) tagsObj;
        if (tags.isEmpty()) {
            return ActionValidationResult.invalid(List.of("Tags object cannot be empty"));
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
    public ActionResult execute(Map<String, Object> parameters, ExecutionContext context) throws ActionException {
        long executionStartTime = System.currentTimeMillis();

        try {
            String itemName = (String) parameters.get("itemName");
            @SuppressWarnings("unchecked")
            Map<String, Object> tags = (Map<String, Object>) parameters.get("tags");
            Boolean overwrite = (Boolean) parameters.getOrDefault("overwrite", true);
            Boolean removeExisting = (Boolean) parameters.getOrDefault("removeExisting", false);
            Boolean validateOnly = (Boolean) parameters.getOrDefault("validateOnly", false);

            logger.debug("Setting tags for item: {} tags: {}", itemName, tags.keySet());

            // Get the item
            Item item = itemRegistry.getItem(itemName);

            Map<String, Object> result = new HashMap<>();
            result.put("itemName", itemName);
            result.put("success", true);
            result.put("timestamp", System.currentTimeMillis());

            // Check if metadata registry is available
            if (metadataRegistry == null) {
                result.put("success", false);
                result.put("error", "Metadata registry not available");
                result.put("timestamp", System.currentTimeMillis());
                return ActionResult.success(result, System.currentTimeMillis() - executionStartTime);
            }

            // Check if this is just validation
            if (validateOnly) {
                result.put("note", "Validation only - no changes made");
                result.put("timestamp", System.currentTimeMillis());
                return ActionResult.success(result, System.currentTimeMillis() - executionStartTime);
            }

            List<String> tagsSet = new ArrayList<>();
            List<String> tagsRemoved = new ArrayList<>();
            List<String> overwritten = new ArrayList<>();
            Map<String, Object> tagsSetDetails = new HashMap<>();

            // Get existing tags for this item
            List<Metadata> existingMetadata = metadataRegistry.getAll().stream()
                    .filter(metadata -> metadata.getUID().getItemName().equals(itemName)).toList();

            List<String> existingTags = existingMetadata.stream().map(metadata -> metadata.getUID().getNamespace())
                    .toList();

            // Remove existing tags if requested
            if (removeExisting) {
                for (String existingTag : existingTags) {
                    if (!tags.containsKey(existingTag)) {
                        MetadataKey key = new MetadataKey(existingTag, itemName);
                        metadataRegistry.remove(key);
                        tagsRemoved.add(existingTag);
                        logger.debug("Removed tag: {} from item: {}", existingTag, itemName);
                    }
                }
            }

            // Set new tags
            for (Map.Entry<String, Object> tagEntry : tags.entrySet()) {
                String tagName = tagEntry.getKey();
                Object tagValue = tagEntry.getValue();

                MetadataKey key = new MetadataKey(tagName, itemName);
                Metadata existingMetadataEntry = metadataRegistry.get(key);

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

                if (existingMetadataEntry != null) {
                    if (!overwrite) {
                        logger.debug("Tag {} already exists and overwrite is disabled", tagName);
                        continue;
                    }
                    overwritten.add(tagName);
                }

                // Create new metadata
                Metadata newMetadata = new Metadata(key, value, configuration);
                metadataRegistry.add(newMetadata);

                tagsSet.add(tagName);
                tagsSetDetails.put(tagName, Map.of("value", value, "configuration", configuration));

                logger.debug("Set tag: {} = {} for item: {}", tagName, value, itemName);
            }

            result.put("tagsSet", tagsSetDetails);
            result.put("tagsRemoved", tagsRemoved);
            result.put("overwritten", overwritten);
            result.put("totalTagsSet", tagsSet.size());

            long executionTime = System.currentTimeMillis() - executionStartTime;
            logger.debug("Successfully set {} tags for item: {} in {}ms", tagsSet.size(), itemName, executionTime);

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
            logger.error("Error setting item tags", e);
            throw new ActionException(getActionId(), "Failed to set item tags: " + e.getMessage(), e);
        }
    }

    @Override
    public CompletableFuture<ActionResult> executeAsync(Map<String, Object> parameters, ExecutionContext context) {
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
        return ActionMetadata.builder().withVersion(getVersion()).withAuthor("openHAB")
                .withDescription("Set or update tag information for openHAB items")
                .withTags(List.of("items", "tags", "metadata", "write"))
                .withDocumentation(
                        "Sets or updates tag information for openHAB items by managing metadata namespaces. Supports bulk tag operations and removal of existing tags.")
                .withExamples(List.of(
                        "Set basic tags: {\"itemName\": \"LivingRoom_Light\", \"tags\": {\"semantics\": \"Light\", \"location\": \"LivingRoom\"}}",
                        "Set tags with configuration: {\"itemName\": \"LivingRoom_Light\", \"tags\": {\"autoupdate\": {\"value\": \"true\", \"configuration\": {\"strategy\": \"everyChange\"}}}}",
                        "Replace all tags: {\"itemName\": \"LivingRoom_Light\", \"tags\": {\"semantics\": \"Light\"}, \"removeExisting\": true}",
                        "Validate only: {\"itemName\": \"LivingRoom_Light\", \"tags\": {\"semantics\": \"Light\"}, \"validateOnly\": true}"))
                .build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("async", true);
        capabilities.put("validation", true);
        capabilities.put("write", true);
        capabilities.put("bulkOperations", true);
        capabilities.put("tagRemoval", true);
        capabilities.put("overwrite", true);
        capabilities.put("validateOnly", true);
        capabilities.put("metadataRegistry", true);
        return capabilities;
    }

    @Override
    public void initialize(ExecutionContext context) {
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
