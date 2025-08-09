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
import org.openhab.core.items.ItemBuilder;
import org.openhab.core.items.ItemBuilderFactory;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataRegistry;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action for updating items in openHAB.
 * 
 * This action provides functionality to update item
 * configurations and properties.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class UpdateItemAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(UpdateItemAction.class);

    @Reference
    private @Nullable ItemRegistry itemRegistry;

    @Reference
    private @Nullable ItemBuilderFactory itemBuilderFactory;

    @Reference
    private @Nullable MetadataRegistry metadataRegistry;

    @Override
    public String getActionId() {
        return "openhab.items.update";
    }

    @Override
    public String getActionName() {
        return "Update Item";
    }

    @Override
    public String getDescription() {
        return "Update existing openHAB items";
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
                Map.of("type", "string", "description", "The name of the item to update", "required", true));
        properties.put("label",
                Map.of("type", "string", "description", "New human-readable label for the item", "required", false));
        properties.put("category",
                Map.of("type", "string", "description", "New category for the item", "required", false));
        properties.put("groups", Map.of("type", "array", "description", "New list of group names to assign to the item",
                "required", false));
        properties.put("tags",
                Map.of("type", "array", "description", "New list of tags to assign to the item", "required", false));
        properties.put("metadata",
                Map.of("type", "object", "description", "Metadata to update for the item", "required", false));
        properties.put("overwriteGroups", Map.of("type", "boolean", "description",
                "Overwrite existing groups instead of merging", "default", false));
        properties.put("overwriteTags", Map.of("type", "boolean", "description",
                "Overwrite existing tags instead of merging", "default", false));
        properties.put("validateOnly", Map.of("type", "boolean", "description",
                "Only validate parameters without updating the item", "default", false));

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
        properties.put("updated", Map.of("type", "boolean"));
        properties.put("previousInfo", Map.of("type", "object"));
        properties.put("newInfo", Map.of("type", "object"));
        properties.put("changes", Map.of("type", "object"));
        properties.put("metadataUpdated", Map.of("type", "integer"));
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
            String label = (String) parameters.get("label");
            String category = (String) parameters.get("category");
            @SuppressWarnings("unchecked")
            List<String> groups = (List<String>) parameters.get("groups");
            @SuppressWarnings("unchecked")
            List<String> tags = (List<String>) parameters.get("tags");
            @SuppressWarnings("unchecked")
            Map<String, Object> metadata = (Map<String, Object>) parameters.get("metadata");
            Boolean overwriteGroups = (Boolean) parameters.getOrDefault("overwriteGroups", false);
            Boolean overwriteTags = (Boolean) parameters.getOrDefault("overwriteTags", false);
            Boolean validateOnly = (Boolean) parameters.getOrDefault("validateOnly", false);

            logger.debug("Updating item: {} validateOnly: {}", itemName, validateOnly);

            // Get the current item
            Item currentItem = itemRegistry.getItem(itemName);

            Map<String, Object> result = new HashMap<>();
            result.put("itemName", itemName != null ? itemName : "");
            result.put("success", true);
            result.put("timestamp", System.currentTimeMillis());

            // Store previous information
            Map<String, Object> previousInfo = new HashMap<>();
            String currentLabel = "";
            if (currentItem.getLabel() != null) {
                currentLabel = currentItem.getLabel();
            }
            previousInfo.put("label", currentLabel);

            String currentCategory = "";
            if (currentItem.getCategory() != null) {
                currentCategory = currentItem.getCategory();
            }
            previousInfo.put("category", currentCategory);
            previousInfo.put("groups", currentItem.getGroupNames());
            previousInfo.put("tags", currentItem.getTags());
            result.put("previousInfo", previousInfo);

            if (validateOnly) {
                result.put("updated", false);
                result.put("note", "Validation only - no changes made");
                result.put("newInfo", Map.of());
                result.put("changes", Map.of());
                result.put("metadataUpdated", 0);
                return ActionResult.success(result, System.currentTimeMillis() - executionStartTime);
            }

            // Prepare new values
            String newLabel = label != null ? label : currentItem.getLabel();
            String newCategory = category != null ? category : currentItem.getCategory();

            List<String> newGroups;
            if (groups != null) {
                if (overwriteGroups) {
                    newGroups = groups;
                } else {
                    // Merge with existing groups
                    newGroups = new java.util.ArrayList<>(currentItem.getGroupNames());
                    for (String group : groups) {
                        if (!newGroups.contains(group)) {
                            newGroups.add(group);
                        }
                    }
                }
            } else {
                newGroups = currentItem.getGroupNames();
            }

            List<String> newTags;
            if (tags != null) {
                if (overwriteTags) {
                    newTags = tags;
                } else {
                    // Merge with existing tags
                    newTags = new java.util.ArrayList<>(currentItem.getTags());
                    for (String tag : tags) {
                        if (!newTags.contains(tag)) {
                            newTags.add(tag);
                        }
                    }
                }
            } else {
                newTags = new java.util.ArrayList<>(currentItem.getTags());
            }

            // Create updated item using ItemBuilder
            ItemBuilder builder = itemBuilderFactory.newItemBuilder(currentItem.getType(), itemName);
            builder.withLabel(newLabel);
            builder.withCategory(newCategory);
            builder.withGroups(newGroups);
            builder.withTags(new java.util.HashSet<>(newTags));

            Item updatedItem = builder.build();

            // Remove old item and add new one
            itemRegistry.remove(itemName);
            itemRegistry.add(updatedItem);

            // Update metadata if provided
            int metadataUpdated = 0;
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
                        metadataUpdated++;
                    }
                }
            }

            // Create new information
            Map<String, Object> newInfo = new HashMap<>();
            String updatedLabel = "";
            if (updatedItem.getLabel() != null) {
                updatedLabel = updatedItem.getLabel();
            }
            newInfo.put("label", updatedLabel);

            String updatedCategory = "";
            if (updatedItem.getCategory() != null) {
                updatedCategory = updatedItem.getCategory();
            }
            newInfo.put("category", updatedCategory);
            newInfo.put("groups", updatedItem.getGroupNames());
            newInfo.put("tags", updatedItem.getTags());
            result.put("newInfo", newInfo);

            // Track changes
            Map<String, Object> changes = new HashMap<>();
            changes.put("labelChanged", !java.util.Objects.equals(currentItem.getLabel(), newLabel));
            changes.put("categoryChanged", !java.util.Objects.equals(currentItem.getCategory(), newCategory));
            changes.put("groupsChanged", !currentItem.getGroupNames().equals(newGroups));
            changes.put("tagsChanged", !currentItem.getTags().equals(newTags));
            changes.put("metadataChanged", metadataUpdated > 0);
            result.put("changes", changes);

            result.put("updated", true);
            result.put("metadataUpdated", metadataUpdated);

            long executionTime = System.currentTimeMillis() - executionStartTime;
            logger.debug("Successfully updated item: {} in {}ms", itemName, executionTime);

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
            logger.error("Error updating item", e);
            throw new ActionException(getActionId(), "Failed to update item: " + e.getMessage(), e);
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
                .description("Update existing openHAB items with new properties and configurations")
                .tags(List.of("items", "update", "modify", "configuration"))
                .documentation(
                        "Updates existing openHAB items with new labels, categories, groups, tags, and metadata. Supports merging or overwriting existing values.")
                .examples(List.of(
                        "Update label: {\"itemName\": \"LivingRoom_Light\", \"label\": \"Updated Living Room Light\"}",
                        "Update groups: {\"itemName\": \"LivingRoom_Light\", \"groups\": [\"Lights\", \"LivingRoom\"]}",
                        "Merge tags: {\"itemName\": \"LivingRoom_Light\", \"tags\": [\"smart\", \"automated\"]}",
                        "Overwrite groups: {\"itemName\": \"LivingRoom_Light\", \"groups\": [\"NewGroup\"], \"overwriteGroups\": true}",
                        "Update metadata: {\"itemName\": \"LivingRoom_Light\", \"metadata\": {\"semantics\": {\"value\": \"Light\"}}}",
                        "Validate only: {\"itemName\": \"LivingRoom_Light\", \"label\": \"New Label\", \"validateOnly\": true}"))
                .build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("async", true);
        capabilities.put("validation", true);
        capabilities.put("itemUpdate", true);
        capabilities.put("metadataUpdate", true);
        capabilities.put("groupManagement", true);
        capabilities.put("tagManagement", true);
        capabilities.put("mergeMode", true);
        capabilities.put("overwriteMode", true);
        capabilities.put("changeTracking", true);
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
        return itemRegistry != null && itemBuilderFactory != null;
    }
}
