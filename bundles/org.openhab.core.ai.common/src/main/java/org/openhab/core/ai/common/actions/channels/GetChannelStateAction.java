package org.openhab.core.ai.common.actions.channels;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.api.action.AIAction;
import org.openhab.core.ai.common.api.action.AIActionContext;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.openhab.core.ai.common.api.action.AIActionMetadata;
import org.openhab.core.ai.common.api.action.AIActionResult;
import org.openhab.core.ai.common.api.action.AIActionValidationResult;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.link.ItemChannelLink;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.types.State;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action for retrieving channel state in openHAB.
 * 
 * This action provides functionality to get the current state
 * of channels including their values and status.
 * 
 * @author AI Assistant
 * @since 1.0.0
 */
@NonNullByDefault
public class GetChannelStateAction implements AIAction {

    private static final Logger logger = LoggerFactory.getLogger(GetChannelStateAction.class);
    private static final String ACTION_ID = "openhab.channels.get-state";
    private static final String ACTION_NAME = "Get Channel State";
    private static final String CATEGORY = "channels";

    @Reference
    private @Nullable ThingRegistry thingRegistry;

    @Reference
    private @Nullable ItemRegistry itemRegistry;

    @Reference
    private @Nullable ItemChannelLinkRegistry itemChannelLinkRegistry;

    @Override
    public String getActionId() {
        return ACTION_ID;
    }

    @Override
    public String getActionName() {
        return ACTION_NAME;
    }

    @Override
    public String getDescription() {
        return "Retrieves the current state of a channel including linked items and their states";
    }

    @Override
    public String getCategory() {
        return CATEGORY;
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
        properties.put("channelUID", Map.of("type", "string", "description",
                "The UID of the channel to get state for (e.g., 'binding:thing:channel')"));
        properties.put("includeLinkedItems",
                Map.of("type", "boolean", "description", "Include information about linked items", "default", true));
        properties.put("includeItemStates",
                Map.of("type", "boolean", "description", "Include current states of linked items", "default", true));

        schema.put("properties", properties);
        schema.put("required", List.of("channelUID"));
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("channelUID", Map.of("type", "string", "description", "The channel UID"));
        properties.put("found", Map.of("type", "boolean", "description", "Whether the channel was found"));
        properties.put("isLinked",
                Map.of("type", "boolean", "description", "Whether the channel is linked to any items"));
        properties.put("linkedItems", Map.of("type", "array", "description", "List of linked items"));
        properties.put("channelState", Map.of("type", "object", "description", "Current channel state information"));
        properties.put("lastUpdate", Map.of("type", "string", "description", "Last update timestamp"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("channel_state", true);
        capabilities.put("linked_items", true);
        capabilities.put("item_states", true);
        capabilities.put("state_history", false); // Would require persistence integration
        return capabilities;
    }

    @Override
    public AIActionValidationResult validateParameters(Map<String, Object> parameters) {
        List<String> errors = new ArrayList<>();

        if (!parameters.containsKey("channelUID")) {
            errors.add("channelUID is required");
        } else {
            Object channelUID = parameters.get("channelUID");
            if (!(channelUID instanceof String) || ((String) channelUID).trim().isEmpty()) {
                errors.add("channelUID must be a non-empty string");
            }
        }

        if (errors.isEmpty()) {
            return AIActionValidationResult.valid(parameters);
        } else {
            return AIActionValidationResult.invalid(errors);
        }
    }

    @Override
    public AIActionResult execute(Map<String, Object> parameters, AIActionContext context) throws AIActionException {
        logger.debug("Executing GetChannelStateAction with context: {}", context.getProtocol());

        long startTime = System.currentTimeMillis();

        try {
            String channelUID = (String) parameters.get("channelUID");
            boolean includeLinkedItems = (Boolean) parameters.getOrDefault("includeLinkedItems", true);
            boolean includeItemStates = (Boolean) parameters.getOrDefault("includeItemStates", true);

            Map<String, Object> result = getChannelState(channelUID, includeLinkedItems, includeItemStates);

            long executionTime = System.currentTimeMillis() - startTime;
            return AIActionResult.success(result, executionTime);

        } catch (Exception e) {
            logger.error("Error executing GetChannelStateAction: {}", e.getMessage(), e);
            throw new AIActionException(ACTION_ID, "Failed to get channel state: " + e.getMessage(), e);
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
        return AIActionMetadata.builder().author("openHAB").description("Retrieves the current state of a channel")
                .version("1.0.0").tags(List.of("channels", "state", "items")).build();
    }

    @Override
    public void initialize(AIActionContext context) {
        logger.debug("GetChannelStateAction initialized with context: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("GetChannelStateAction cleanup completed");
    }

    @Override
    public boolean isReady() {
        return thingRegistry != null && itemRegistry != null && itemChannelLinkRegistry != null;
    }

    private Map<String, Object> getChannelState(String channelUID, boolean includeLinkedItems,
            boolean includeItemStates) {

        Map<String, Object> result = new HashMap<>();
        result.put("channelUID", channelUID);
        result.put("found", false);
        result.put("lastUpdate", Instant.now().toString());

        try {
            ChannelUID uid = new ChannelUID(channelUID);
            Thing thing = thingRegistry.get(uid.getThingUID());

            if (thing == null) {
                result.put("error", "Thing not found: " + uid.getThingUID());
                return result;
            }

            Channel channel = thing.getChannel(uid.getId());
            if (channel == null) {
                result.put("error", "Channel not found: " + channelUID);
                return result;
            }

            result.put("found", true);

            // Channel state information
            Map<String, Object> channelState = new HashMap<>();
            channelState.put("kind", channel.getKind().toString());
            String acceptedItemType = channel.getAcceptedItemType();
            channelState.put("acceptedItemType", acceptedItemType != null ? acceptedItemType : "");
            String channelLabel = channel.getLabel();
            if (channelLabel == null) {
                channelLabel = "";
            }
            channelState.put("label", channelLabel);
            String channelDescription = channel.getDescription();
            if (channelDescription == null) {
                channelDescription = "";
            }
            channelState.put("description", channelDescription);
            result.put("channelState", channelState);

            // Linked items information
            if (includeLinkedItems) {
                List<Map<String, Object>> linkedItems = getLinkedItems(uid, includeItemStates);
                result.put("linkedItems", linkedItems);
                result.put("isLinked", !linkedItems.isEmpty());
            } else {
                boolean isLinked = !itemChannelLinkRegistry.getLinks(uid).isEmpty();
                result.put("isLinked", isLinked);
            }

        } catch (Exception e) {
            logger.error("Error getting channel state for {}: {}", channelUID, e.getMessage(), e);
            result.put("error", "Error retrieving channel state: " + e.getMessage());
        }

        return result;
    }

    private List<Map<String, Object>> getLinkedItems(ChannelUID channelUID, boolean includeItemStates) {
        List<Map<String, Object>> linkedItems = new ArrayList<>();

        try {
            Set<ItemChannelLink> links = itemChannelLinkRegistry.getLinks(channelUID);

            for (ItemChannelLink link : links) {
                Map<String, Object> itemInfo = new HashMap<>();
                itemInfo.put("itemName", link.getItemName());
                itemInfo.put("configuration", link.getConfiguration().getProperties());

                if (includeItemStates) {
                    Item item = itemRegistry.get(link.getItemName());
                    if (item != null) {
                        State state = item.getState();
                        itemInfo.put("state", state != null ? state.toString() : "NULL");
                        itemInfo.put("stateType", state != null ? state.getClass().getSimpleName() : "NULL");
                        itemInfo.put("itemType", item.getType());
                        String itemLabel = item.getLabel();
                        if (itemLabel == null) {
                            itemLabel = "";
                        }
                        itemInfo.put("itemLabel", itemLabel);
                    }
                }

                linkedItems.add(itemInfo);
            }

        } catch (Exception e) {
            logger.error("Error getting linked items for channel {}: {}", channelUID, e.getMessage(), e);
        }

        return linkedItems;
    }
}
