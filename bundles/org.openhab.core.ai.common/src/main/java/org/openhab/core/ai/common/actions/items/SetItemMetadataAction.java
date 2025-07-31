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
 * Action to set or update metadata information for items
 * 
 * 
 */
@Component(service = AIAction.class, immediate = true)
public class SetItemMetadataAction implements AIAction {

    private static final Logger logger = LoggerFactory.getLogger(SetItemMetadataAction.class);

    @Reference
    private ItemRegistry itemRegistry;

    @Reference
    private @Nullable MetadataRegistry metadataRegistry;

    @Override
    public String getActionId() {
        return "openhab.items.set_metadata";
    }

    @Override
    public String getActionName() {
        return "Set Item Metadata";
    }

    @Override
    public String getDescription() {
        return "Set or update metadata information for items";
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
                Map.of("type", "string", "description", "The name of the item to set metadata for", "required", true));
        properties.put("namespace",
                Map.of("type", "string", "description", "The namespace for the metadata", "required", true));
        properties.put("value", Map.of("type", "string", "description", "The metadata value to set", "required", true));
        properties.put("configuration", Map.of("type", "object", "description",
                "Configuration properties for the metadata (optional)", "required", false));
        properties.put("overwrite",
                Map.of("type", "boolean", "description", "Whether to overwrite existing metadata", "default", true));
        properties.put("validateOnly", Map.of("type", "boolean", "description",
                "Only validate parameters without making changes", "default", false));

        schema.put("properties", properties);
        schema.put("required", List.of("itemName", "namespace", "value"));
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("itemName", Map.of("type", "string"));
        properties.put("namespace", Map.of("type", "string"));
        properties.put("value", Map.of("type", "string"));
        properties.put("configuration", Map.of("type", "object"));
        properties.put("overwritten", Map.of("type", "boolean"));
        properties.put("previousValue", Map.of("type", "string"));
        properties.put("previousConfiguration", Map.of("type", "object"));
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

        String namespace = (String) parameters.get("namespace");
        if (namespace == null || namespace.trim().isEmpty()) {
            return AIActionValidationResult.invalid(List.of("Namespace is required and cannot be empty"));
        }

        String value = (String) parameters.get("value");
        if (value == null) {
            return AIActionValidationResult.invalid(List.of("Value is required"));
        }

        // Validate that the item exists
        try {
            itemRegistry.getItem(itemName);
        } catch (ItemNotFoundException e) {
            return AIActionValidationResult.invalid(List.of("Item not found: " + itemName));
        } catch (Exception e) {
            return AIActionValidationResult.invalid(List.of("Error validating parameters: " + e.getMessage()));
        }

        // Validate configuration if provided
        Object configuration = parameters.get("configuration");
        if (configuration != null && !(configuration instanceof Map)) {
            return AIActionValidationResult.invalid(List.of("Configuration must be an object"));
        }

        return AIActionValidationResult.valid(parameters);
    }

    @Override
    public AIActionResult execute(Map<String, Object> parameters, AIActionContext context) throws AIActionException {
        long executionStartTime = System.currentTimeMillis();

        try {
            String itemName = (String) parameters.get("itemName");
            String namespace = (String) parameters.get("namespace");
            String value = (String) parameters.get("value");
            @SuppressWarnings("unchecked")
            Map<String, Object> configuration = (Map<String, Object>) parameters.get("configuration");
            Boolean overwrite = (Boolean) parameters.getOrDefault("overwrite", true);
            Boolean validateOnly = (Boolean) parameters.getOrDefault("validateOnly", false);

            logger.debug("Setting metadata for item: {} namespace: {} value: {}", itemName, namespace, value);

            // Get the item
            Item item = itemRegistry.getItem(itemName);

            Map<String, Object> result = new HashMap<>();
            result.put("itemName", itemName);
            result.put("namespace", namespace);
            result.put("value", value);
            result.put("configuration", configuration);
            result.put("success", true);
            result.put("timestamp", System.currentTimeMillis());

            // Check if metadata registry is available
            if (metadataRegistry == null) {
                result.put("success", false);
                result.put("error", "Metadata registry not available");
                result.put("timestamp", System.currentTimeMillis());
                return AIActionResult.success(result, System.currentTimeMillis() - executionStartTime);
            }

            // Check if this is just validation
            if (validateOnly) {
                result.put("note", "Validation only - no changes made");
                result.put("timestamp", System.currentTimeMillis());
                return AIActionResult.success(result, System.currentTimeMillis() - executionStartTime);
            }

            // Check for existing metadata
            MetadataKey key = new MetadataKey(namespace, itemName);
            Metadata existingMetadata = metadataRegistry.get(key);

            if (existingMetadata != null) {
                if (!overwrite) {
                    result.put("success", false);
                    result.put("error", "Metadata already exists and overwrite is disabled");
                    result.put("timestamp", System.currentTimeMillis());
                    return AIActionResult.success(result, System.currentTimeMillis() - executionStartTime);
                }

                // Store previous values
                result.put("overwritten", true);
                result.put("previousValue", existingMetadata.getValue());
                result.put("previousConfiguration", existingMetadata.getConfiguration());
            } else {
                result.put("overwritten", false);
                result.put("previousValue", "");
                result.put("previousConfiguration", Map.of());
            }

            // Create new metadata
            Metadata newMetadata = new Metadata(key, value, configuration);

            // Add or update the metadata
            metadataRegistry.add(newMetadata);

            logger.debug("Successfully set metadata for item: {} namespace: {} value: {}", itemName, namespace, value);

            long executionTime = System.currentTimeMillis() - executionStartTime;
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
            logger.error("Error setting item metadata", e);
            throw new AIActionException(getActionId(), "Failed to set item metadata: " + e.getMessage(), e);
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
                .description("Set or update metadata information for openHAB items")
                .tags(List.of("items", "metadata", "configuration", "properties", "write"))
                .documentation(
                        "Sets or updates metadata information for openHAB items including values and configuration properties. Supports overwriting existing metadata.")
                .examples(List.of(
                        "Set basic metadata: {\"itemName\": \"LivingRoom_Light\", \"namespace\": \"semantics\", \"value\": \"Light\"}",
                        "Set metadata with configuration: {\"itemName\": \"LivingRoom_Light\", \"namespace\": \"autoupdate\", \"value\": \"true\", \"configuration\": {\"strategy\": \"everyChange\"}}",
                        "Set metadata without overwriting: {\"itemName\": \"LivingRoom_Light\", \"namespace\": \"semantics\", \"value\": \"Light\", \"overwrite\": false}",
                        "Validate only: {\"itemName\": \"LivingRoom_Light\", \"namespace\": \"semantics\", \"value\": \"Light\", \"validateOnly\": true}"))
                .build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("async", true);
        capabilities.put("validation", true);
        capabilities.put("write", true);
        capabilities.put("overwrite", true);
        capabilities.put("configuration", true);
        capabilities.put("validateOnly", true);
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
