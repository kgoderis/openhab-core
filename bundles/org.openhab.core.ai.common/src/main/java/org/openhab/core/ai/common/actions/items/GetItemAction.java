package org.openhab.core.ai.common.actions.items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.api.action.AIAction;
import org.openhab.core.ai.common.api.action.AIActionContext;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.openhab.core.ai.common.api.action.AIActionMetadata;
import org.openhab.core.ai.common.api.action.AIActionResult;
import org.openhab.core.ai.common.api.action.AIActionValidationResult;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to retrieve comprehensive information about a single item
 * 
 * 
 */
@Component(service = AIAction.class, immediate = true)
public class GetItemAction implements AIAction {

    private static final Logger logger = LoggerFactory.getLogger(GetItemAction.class);

    @Reference
    private ItemRegistry itemRegistry;

    @Reference
    private @Nullable MetadataRegistry metadataRegistry;

    @Reference
    private @Nullable ThingRegistry thingRegistry;

    @Override
    public String getActionId() {
        return "openhab.items.get";
    }

    @Override
    public String getActionName() {
        return "Get Item";
    }

    @Override
    public String getDescription() {
        return "Retrieve comprehensive information about a single item";
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
                Map.of("type", "string", "description", "The name of the item to retrieve", "required", true));
        properties.put("includeMetadata",
                Map.of("type", "boolean", "description", "Include metadata information", "default", true));
        properties.put("includeGroups",
                Map.of("type", "boolean", "description", "Include group membership information", "default", true));
        properties.put("includeBinding",
                Map.of("type", "boolean", "description", "Include binding information", "default", true));
        properties.put("includeStateHistory",
                Map.of("type", "boolean", "description", "Include recent state history", "default", false));

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
        properties.put("basicInfo", Map.of("type", "object"));
        properties.put("stateInfo", Map.of("type", "object"));
        properties.put("typeInfo", Map.of("type", "object"));
        properties.put("metadata", Map.of("type", "object"));
        properties.put("groups", Map.of("type", "object"));
        properties.put("binding", Map.of("type", "object"));
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
            Boolean includeMetadata = (Boolean) parameters.getOrDefault("includeMetadata", true);
            Boolean includeGroups = (Boolean) parameters.getOrDefault("includeGroups", true);
            Boolean includeBinding = (Boolean) parameters.getOrDefault("includeBinding", true);
            Boolean includeStateHistory = (Boolean) parameters.getOrDefault("includeStateHistory", false);

            logger.debug("Getting comprehensive information for item: {}", itemName);

            // Get the item
            Item item = itemRegistry.getItem(itemName);

            Map<String, Object> result = new HashMap<>();
            result.put("itemName", itemName);
            result.put("success", true);
            result.put("timestamp", System.currentTimeMillis());

            // Basic item information
            Map<String, Object> basicInfo = new HashMap<>();
            basicInfo.put("name", item.getName());
            basicInfo.put("type", item.getType());
            basicInfo.put("label", item.getLabel());
            basicInfo.put("category", item.getCategory());
            basicInfo.put("isGroup", item instanceof GroupItem);
            result.put("basicInfo", basicInfo);

            // State information
            Map<String, Object> stateInfo = new HashMap<>();
            stateInfo.put("currentState", item.getState() != null ? item.getState().toString() : "NULL");
            stateInfo.put("stateType", item.getState() != null ? item.getState().getClass().getSimpleName() : "NULL");
            stateInfo.put("lastStateChange",
                    item.getLastStateChange() != null ? item.getLastStateChange().toString() : "NULL");
            result.put("stateInfo", stateInfo);

            // Type information
            Map<String, Object> typeInfo = new HashMap<>();
            typeInfo.put("acceptedCommands",
                    item.getAcceptedCommandTypes().stream().map(Class::getSimpleName).collect(Collectors.toList()));
            typeInfo.put("acceptedStates",
                    item.getAcceptedDataTypes().stream().map(Class::getSimpleName).collect(Collectors.toList()));

            if (item instanceof GroupItem groupItem) {
                typeInfo.put("memberCount", groupItem.getMembers().size());
                typeInfo.put("baseItemType",
                        groupItem.getBaseItem() != null ? groupItem.getBaseItem().getType() : null);
                typeInfo.put("function", groupItem.getFunction() != null ? groupItem.getFunction().toString() : null);
            }
            result.put("typeInfo", typeInfo);

            // Metadata information
            if (includeMetadata && metadataRegistry != null) {
                Map<String, Object> metadataInfo = new HashMap<>();
                List<Metadata> allItemMetadata = metadataRegistry.getAll().stream()
                        .filter(metadata -> metadata.getUID().getItemName().equals(itemName))
                        .collect(Collectors.toList());

                Map<String, Object> metadataMap = new HashMap<>();
                for (Metadata metadata : allItemMetadata) {
                    String namespace = metadata.getUID().getNamespace();
                    Map<String, Object> metaInfo = new HashMap<>();
                    metaInfo.put("value", metadata.getValue());
                    metaInfo.put("configuration", metadata.getConfiguration());
                    metadataMap.put(namespace, metaInfo);
                }

                metadataInfo.put("metadata", metadataMap);
                metadataInfo.put("totalMetadataEntries", allItemMetadata.size());
                metadataInfo.put("namespaces", allItemMetadata.stream()
                        .map(metadata -> metadata.getUID().getNamespace()).distinct().collect(Collectors.toList()));
                result.put("metadata", metadataInfo);
            } else {
                result.put("metadata", Map.of());
            }

            // Group membership information
            if (includeGroups) {
                Map<String, Object> groupsInfo = new HashMap<>();
                List<String> groupMemberships = itemRegistry.getAll().stream()
                        .filter(groupItem -> groupItem instanceof GroupItem).map(groupItem -> (GroupItem) groupItem)
                        .filter(groupItem -> groupItem.getMembers().contains(item)).map(GroupItem::getName)
                        .collect(Collectors.toList());

                groupsInfo.put("groupMemberships", groupMemberships);
                groupsInfo.put("totalGroups", groupMemberships.size());
                result.put("groups", groupsInfo);
            } else {
                result.put("groups", List.of());
            }

            // Binding information
            if (includeBinding && thingRegistry != null) {
                Map<String, Object> bindingInfo = new HashMap<>();

                // Find the thing that provides this item
                Thing thing = null;
                Channel channel = null;

                for (Thing t : thingRegistry.getAll()) {
                    for (Channel c : t.getChannels()) {
                        if (itemName.equals(c.getProperties().get("item"))) {
                            thing = t;
                            channel = c;
                            break;
                        }
                    }
                    if (thing != null) {
                        break;
                    }
                }

                if (thing != null && channel != null) {
                    bindingInfo.put("hasBinding", true);
                    bindingInfo.put("bindingId", thing.getThingTypeUID().getBindingId());
                    bindingInfo.put("thingTypeId", thing.getThingTypeUID().getId());
                    bindingInfo.put("thingId", thing.getUID().getId());
                    bindingInfo.put("thingLabel", thing.getLabel());
                    bindingInfo.put("thingStatus", thing.getStatus().toString());
                    bindingInfo.put("channelId", channel.getUID().getId());
                    bindingInfo.put("channelLabel", channel.getLabel());
                    bindingInfo.put("channelDescription", channel.getDescription());
                } else {
                    bindingInfo.put("hasBinding", false);
                }

                result.put("binding", bindingInfo);
            } else {
                result.put("binding", Map.of());
            }

            long executionTime = System.currentTimeMillis() - executionStartTime;
            logger.debug("Successfully retrieved comprehensive information for item: {} in {}ms", itemName,
                    executionTime);

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
            logger.error("Error getting item information", e);
            throw new AIActionException(getActionId(), "Failed to get item information: " + e.getMessage(), e);
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
                .description("Retrieve comprehensive information about a single openHAB item")
                .tags(List.of("items", "information", "details", "comprehensive"))
                .documentation(
                        "Retrieves comprehensive information about a single openHAB item including basic info, state, type, metadata, groups, and binding information.")
                .examples(List.of("Get basic info: {\"itemName\": \"LivingRoom_Light\"}",
                        "Get with metadata: {\"itemName\": \"LivingRoom_Light\", \"includeMetadata\": true}",
                        "Get with groups: {\"itemName\": \"LivingRoom_Light\", \"includeGroups\": true}",
                        "Get with binding: {\"itemName\": \"LivingRoom_Light\", \"includeBinding\": true}",
                        "Get everything: {\"itemName\": \"LivingRoom_Light\", \"includeMetadata\": true, \"includeGroups\": true, \"includeBinding\": true}"))
                .build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("async", true);
        capabilities.put("validation", true);
        capabilities.put("comprehensiveInfo", true);
        capabilities.put("metadataRetrieval", true);
        capabilities.put("groupMembership", true);
        capabilities.put("bindingInfo", true);
        capabilities.put("stateInfo", true);
        capabilities.put("typeInfo", true);
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
