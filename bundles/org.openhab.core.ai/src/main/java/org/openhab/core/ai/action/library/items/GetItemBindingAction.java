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
import org.openhab.core.ai.api.action.Action;
import org.openhab.core.ai.api.action.ActionException;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action for getting item binding information in openHAB.
 * 
 * This action provides functionality to retrieve
 * binding information for items.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class GetItemBindingAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(GetItemBindingAction.class);

    @Reference
    private @Nullable ItemRegistry itemRegistry;

    @Reference
    private @Nullable ThingRegistry thingRegistry;

    @Reference
    private @Nullable ChannelTypeRegistry channelTypeRegistry;

    @Override
    public String getActionId() {
        return "openhab.items.get_binding";
    }

    @Override
    public String getActionName() {
        return "Get Item Binding";
    }

    @Override
    public String getDescription() {
        return "Retrieve binding information for items";
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
        properties.put("itemName", Map.of("type", "string", "description",
                "The name of the item to get binding information for", "required", true));
        properties.put("includeThingDetails",
                Map.of("type", "boolean", "description", "Include detailed thing information", "default", true));
        properties.put("includeChannelDetails",
                Map.of("type", "boolean", "description", "Include detailed channel information", "default", false));
        properties.put("includeBindingStatus",
                Map.of("type", "boolean", "description", "Include binding status information", "default", true));

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
        properties.put("hasBinding", Map.of("type", "boolean"));
        properties.put("bindingInfo", Map.of("type", "object"));
        properties.put("thingInfo", Map.of("type", "object"));
        properties.put("channelInfo", Map.of("type", "object"));
        properties.put("bindingStatus", Map.of("type", "string"));
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
            Boolean includeThingDetails = (Boolean) parameters.getOrDefault("includeThingDetails", true);
            Boolean includeChannelDetails = (Boolean) parameters.getOrDefault("includeChannelDetails", false);
            Boolean includeBindingStatus = (Boolean) parameters.getOrDefault("includeBindingStatus", true);

            logger.debug("Getting binding information for item: {} includeThingDetails: {}", itemName,
                    includeThingDetails);

            // Get the item
            Item item = itemRegistry.getItem(itemName);

            Map<String, Object> result = new HashMap<>();
            result.put("itemName", itemName);
            result.put("success", true);
            result.put("timestamp", System.currentTimeMillis());

            // Check if thing registry is available
            if (thingRegistry == null) {
                result.put("hasBinding", false);
                result.put("bindingInfo", Map.of());
                result.put("thingInfo", Map.of());
                result.put("channelInfo", Map.of());
                result.put("bindingStatus", "UNKNOWN");
                result.put("note", "Thing registry not available");
                return ActionResult.success(result, System.currentTimeMillis() - executionStartTime);
            }

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
                result.put("hasBinding", true);

                // Basic binding information
                Map<String, Object> bindingInfo = new HashMap<>();
                bindingInfo.put("bindingId", thing.getThingTypeUID().getBindingId());
                bindingInfo.put("thingTypeId", thing.getThingTypeUID().getId());
                bindingInfo.put("thingId", thing.getUID().getId());
                bindingInfo.put("channelId", channel.getUID().getId());
                result.put("bindingInfo", bindingInfo);

                if (includeThingDetails) {
                    Map<String, Object> thingInfo = new HashMap<>();
                    thingInfo.put("thingUID", thing.getUID().toString());
                    String thingLabel = "";
                    if (thing.getLabel() != null) {
                        thingLabel = thing.getLabel();
                    }
                    thingInfo.put("label", thingLabel);

                    String thingLocation = "";
                    if (thing.getLocation() != null) {
                        thingLocation = thing.getLocation();
                    }
                    thingInfo.put("location", thingLocation);

                    String thingStatus = "";
                    if (thing.getStatus() != null) {
                        thingStatus = thing.getStatus().toString();
                    }
                    thingInfo.put("status", thingStatus);

                    String thingStatusInfo = "";
                    if (thing.getStatusInfo() != null) {
                        thingStatusInfo = thing.getStatusInfo().toString();
                    }
                    thingInfo.put("statusInfo", thingStatusInfo);
                    thingInfo.put("properties", thing.getProperties());
                    thingInfo.put("configuration", thing.getConfiguration().getProperties());
                    result.put("thingInfo", thingInfo);
                }

                if (includeChannelDetails) {
                    Map<String, Object> channelInfo = new HashMap<>();
                    channelInfo.put("channelUID", channel.getUID().toString());
                    String channelLabel = "";
                    if (channel.getLabel() != null) {
                        channelLabel = channel.getLabel();
                    }
                    channelInfo.put("label", channelLabel);

                    String channelDescription = "";
                    if (channel.getDescription() != null) {
                        channelDescription = channel.getDescription();
                    }
                    channelInfo.put("description", channelDescription);
                    channelInfo.put("properties", channel.getProperties());
                    channelInfo.put("configuration", channel.getConfiguration().getProperties());

                    // Get channel type information if available
                    if (channelTypeRegistry != null) {
                        ChannelType channelType = channelTypeRegistry.getChannelType(channel.getChannelTypeUID());
                        if (channelType != null) {
                            String channelTypeLabel = "";
                            if (channelType.getLabel() != null) {
                                channelTypeLabel = channelType.getLabel();
                            }
                            channelInfo.put("channelType", channelTypeLabel);

                            String channelTypeDescription = "";
                            if (channelType.getDescription() != null) {
                                channelTypeDescription = channelType.getDescription();
                            }
                            channelInfo.put("channelDescription", channelTypeDescription);

                            String channelTypeCategory = "";
                            if (channelType.getCategory() != null) {
                                channelTypeCategory = channelType.getCategory();
                            }
                            channelInfo.put("category", channelTypeCategory);

                            String channelTypeItemType = "";
                            if (channelType.getItemType() != null) {
                                channelTypeItemType = channelType.getItemType();
                            }
                            channelInfo.put("itemType", channelTypeItemType);
                        }
                    }

                    result.put("channelInfo", channelInfo);
                }

                if (includeBindingStatus) {
                    String status = thing.getStatus().toString();
                    result.put("bindingStatus", status);

                    // Get handler status if available
                    ThingHandler handler = thing.getHandler();
                    if (handler != null) {
                        result.put("handlerStatus", handler.toString());
                    }
                }
            } else {
                result.put("hasBinding", false);
                result.put("bindingInfo", Map.of());
                result.put("thingInfo", Map.of());
                result.put("channelInfo", Map.of());
                result.put("bindingStatus", "UNBOUND");
                result.put("note", "Item is not bound to any thing");
            }

            long executionTime = System.currentTimeMillis() - executionStartTime;
            logger.debug("Successfully retrieved binding information for item: {} in {}ms", itemName, executionTime);

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
            logger.error("Error getting item binding information", e);
            throw new ActionException(getActionId(), "Failed to get item binding information: " + e.getMessage(), e);
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
                .description("Retrieve binding information for openHAB items")
                .tags(List.of("items", "bindings", "things", "channels", "status"))
                .documentation(
                        "Retrieves binding information for openHAB items including thing details, channel information, and binding status.")
                .examples(List.of("Get basic binding info: {\"itemName\": \"LivingRoom_Light\"}",
                        "Get thing details: {\"itemName\": \"LivingRoom_Light\", \"includeThingDetails\": true}",
                        "Get channel details: {\"itemName\": \"LivingRoom_Light\", \"includeChannelDetails\": true}",
                        "Get binding status: {\"itemName\": \"LivingRoom_Light\", \"includeBindingStatus\": true}"))
                .build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("async", true);
        capabilities.put("validation", true);
        capabilities.put("bindingRetrieval", true);
        capabilities.put("thingDetails", true);
        capabilities.put("channelDetails", true);
        capabilities.put("bindingStatus", true);
        capabilities.put("thingRegistry", true);
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
        return itemRegistry != null;
    }
}
