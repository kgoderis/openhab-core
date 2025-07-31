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
 * Action to retrieve metadata information for items
 * 
 * 
 */
@Component(service = AIAction.class, immediate = true)
public class GetItemMetadataAction implements AIAction {

    private static final Logger logger = LoggerFactory.getLogger(GetItemMetadataAction.class);

    @Reference
    private ItemRegistry itemRegistry;

    @Reference
    private @Nullable MetadataRegistry metadataRegistry;

    @Override
    public String getActionId() {
        return "openhab.items.get_metadata";
    }

    @Override
    public String getActionName() {
        return "Get Item Metadata";
    }

    @Override
    public String getDescription() {
        return "Retrieve metadata information for items";
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
                Map.of("type", "string", "description", "The name of the item to get metadata for", "required", true));
        properties.put("namespace", Map.of("type", "string", "description",
                "Specific namespace to retrieve metadata from (optional)", "required", false));
        properties.put("includeAllNamespaces",
                Map.of("type", "boolean", "description", "Include metadata from all namespaces", "default", true));
        properties.put("includeValue",
                Map.of("type", "boolean", "description", "Include the metadata value", "default", true));
        properties.put("includeConfiguration",
                Map.of("type", "boolean", "description", "Include metadata configuration", "default", true));

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
        properties.put("namespace", Map.of("type", "string"));
        properties.put("metadata", Map.of("type", "object"));
        properties.put("allNamespaces", Map.of("type", "object"));
        properties.put("totalMetadataEntries", Map.of("type", "integer"));
        properties.put("availableNamespaces", Map.of("type", "array"));
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
            String namespace = (String) parameters.get("namespace");
            Boolean includeAllNamespaces = (Boolean) parameters.getOrDefault("includeAllNamespaces", true);
            Boolean includeValue = (Boolean) parameters.getOrDefault("includeValue", true);
            Boolean includeConfiguration = (Boolean) parameters.getOrDefault("includeConfiguration", true);

            logger.debug("Getting metadata for item: {} namespace: {}", itemName, namespace);

            // Get the item
            Item item = itemRegistry.getItem(itemName);

            Map<String, Object> result = new HashMap<>();
            result.put("itemName", itemName);
            result.put("success", true);
            result.put("timestamp", System.currentTimeMillis());

            // Check if metadata registry is available
            if (metadataRegistry != null) {
                if (namespace != null && !namespace.trim().isEmpty()) {
                    // Get metadata for specific namespace
                    MetadataKey key = new MetadataKey(namespace, itemName);
                    Metadata metadata = metadataRegistry.get(key);

                    if (metadata != null) {
                        Map<String, Object> metadataInfo = new HashMap<>();
                        metadataInfo.put("namespace", namespace);

                        if (includeValue) {
                            metadataInfo.put("value", metadata.getValue());
                        }

                        if (includeConfiguration) {
                            metadataInfo.put("configuration", metadata.getConfiguration());
                        }

                        result.put("metadata", metadataInfo);
                        result.put("totalMetadataEntries", 1);
                    } else {
                        result.put("metadata", Map.of());
                        result.put("totalMetadataEntries", 0);
                        result.put("note", "No metadata found for namespace: " + namespace);
                    }
                } else if (includeAllNamespaces) {
                    // Get metadata for all namespaces
                    Map<String, Object> allMetadata = new HashMap<>();
                    List<String> availableNamespaces = List.of();

                    // Get all metadata for this item
                    List<Metadata> allItemMetadata = metadataRegistry.getAll().stream()
                            .filter(metadata -> metadata.getUID().getItemName().equals(itemName)).toList();

                    for (Metadata metadata : allItemMetadata) {
                        String metaNamespace = metadata.getUID().getNamespace();
                        Map<String, Object> metadataInfo = new HashMap<>();

                        if (includeValue) {
                            metadataInfo.put("value", metadata.getValue());
                        }

                        if (includeConfiguration) {
                            metadataInfo.put("configuration", metadata.getConfiguration());
                        }

                        allMetadata.put(metaNamespace, metadataInfo);
                    }

                    availableNamespaces = allItemMetadata.stream().map(metadata -> metadata.getUID().getNamespace())
                            .distinct().toList();

                    result.put("allNamespaces", allMetadata);
                    result.put("totalMetadataEntries", allItemMetadata.size());
                    result.put("availableNamespaces", availableNamespaces);

                    if (allItemMetadata.isEmpty()) {
                        result.put("note", "No metadata found for item: " + itemName);
                    }
                }
            } else {
                // Fallback when metadata registry is not available
                result.put("metadata", Map.of());
                result.put("allNamespaces", List.of());
                result.put("totalMetadataEntries", 0);
                result.put("availableNamespaces", List.of());
                result.put("note", "Metadata registry not available");
            }

            long executionTime = System.currentTimeMillis() - executionStartTime;
            logger.debug("Successfully retrieved metadata for item: {} in {}ms", itemName, executionTime);

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
            logger.error("Error getting item metadata", e);
            throw new AIActionException(getActionId(), "Failed to get item metadata: " + e.getMessage(), e);
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
                .description("Retrieve metadata information for openHAB items")
                .tags(List.of("items", "metadata", "configuration", "properties"))
                .documentation(
                        "Retrieves metadata information for openHAB items including namespace-specific metadata, values, and configuration properties.")
                .examples(List.of("Get all metadata: {\"itemName\": \"LivingRoom_Light\"}",
                        "Get specific namespace: {\"itemName\": \"LivingRoom_Light\", \"namespace\": \"semantics\"}",
                        "Get metadata without values: {\"itemName\": \"LivingRoom_Light\", \"includeValue\": false}",
                        "Get only configuration: {\"itemName\": \"LivingRoom_Light\", \"includeValue\": false, \"includeConfiguration\": true}"))
                .build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("async", true);
        capabilities.put("validation", true);
        capabilities.put("namespaceFiltering", true);
        capabilities.put("valueRetrieval", true);
        capabilities.put("configurationRetrieval", true);
        capabilities.put("allNamespaces", true);
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
