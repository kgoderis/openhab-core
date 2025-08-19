package org.openhab.core.ai.action.library.items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.api.Action;
import org.openhab.core.ai.action.api.ActionException;
import org.openhab.core.ai.action.api.ActionMetadata;
import org.openhab.core.ai.action.api.ActionResult;
import org.openhab.core.ai.action.api.ActionValidationResult;
import org.openhab.core.ai.common.context.ExecutionContext;
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
@Component(service = Action.class, immediate = true)
@NonNullByDefault
public class GetItemAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(GetItemAction.class);

    @Reference
    private @Nullable ItemRegistry itemRegistry;

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
    public ActionResult execute(Map<String, Object> parameters, ExecutionContext context) throws ActionException {
        long executionStartTime = System.currentTimeMillis();

        if (itemRegistry == null) {
            throw new ActionException(getActionId(), "ItemRegistry service not available");
        }

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
            result.put("itemName", itemName != null ? itemName : "");
            result.put("success", true);
            result.put("timestamp", System.currentTimeMillis());

            // Basic item information
            Map<String, Object> basicInfo = new HashMap<>();
            basicInfo.put("name", item.getName() != null ? item.getName() : "");
            basicInfo.put("type", item.getType() != null ? item.getType() : "");
            String label = item.getLabel() != null ? item.getLabel() : "";
            basicInfo.put("label", label);
            String category = item.getCategory() != null ? item.getCategory() : "";
            basicInfo.put("category", category);
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
                String baseItemType = "";
                if (groupItem.getBaseItem() != null) {
                    baseItemType = groupItem.getBaseItem().getType();
                }
                typeInfo.put("baseItemType", baseItemType);

                String function = "";
                if (groupItem.getFunction() != null) {
                    function = groupItem.getFunction().toString();
                }
                typeInfo.put("function", function);
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
                    String bindingId = "";
                    if (thing.getThingTypeUID() != null && thing.getThingTypeUID().getBindingId() != null) {
                        bindingId = thing.getThingTypeUID().getBindingId();
                    }
                    bindingInfo.put("bindingId", bindingId);

                    String thingTypeId = "";
                    if (thing.getThingTypeUID() != null && thing.getThingTypeUID().getId() != null) {
                        thingTypeId = thing.getThingTypeUID().getId();
                    }
                    bindingInfo.put("thingTypeId", thingTypeId);

                    String thingId = "";
                    if (thing.getUID() != null && thing.getUID().getId() != null) {
                        thingId = thing.getUID().getId();
                    }
                    bindingInfo.put("thingId", thingId);

                    String thingLabel = "";
                    if (thing.getLabel() != null) {
                        thingLabel = thing.getLabel();
                    }
                    bindingInfo.put("thingLabel", thingLabel);

                    String thingStatus = "";
                    if (thing.getStatus() != null) {
                        thingStatus = thing.getStatus().toString();
                    }
                    bindingInfo.put("thingStatus", thingStatus);

                    String channelId = "";
                    if (channel.getUID() != null && channel.getUID().getId() != null) {
                        channelId = channel.getUID().getId();
                    }
                    bindingInfo.put("channelId", channelId);

                    String channelLabel = "";
                    if (channel.getLabel() != null) {
                        channelLabel = channel.getLabel();
                    }
                    bindingInfo.put("channelLabel", channelLabel);

                    String channelDescription = "";
                    if (channel.getDescription() != null) {
                        channelDescription = channel.getDescription();
                    }
                    bindingInfo.put("channelDescription", channelDescription);
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
            logger.error("Error getting item information", e);
            throw new ActionException(getActionId(), "Failed to get item information: " + e.getMessage(), e);
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
                .withDescription("Retrieve comprehensive information about a single openHAB item")
                .withTags(List.of("items", "information", "details", "comprehensive"))
                .withDocumentation(
                        "Retrieves comprehensive information about a single openHAB item including basic info, state, type, metadata, groups, and binding information.")
                .withExamples(List.of("Get basic info: {\"itemName\": \"LivingRoom_Light\"}",
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
