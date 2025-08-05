package org.openhab.core.ai.actions.channels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.ActionContext;
import org.openhab.core.ai.action.ActionMetadata;
import org.openhab.core.ai.action.ActionResult;
import org.openhab.core.ai.action.ActionValidationResult;
import org.openhab.core.ai.api.action.Action;
import org.openhab.core.ai.api.action.ActionException;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.link.ItemChannelLink;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action for linking channels to items in openHAB.
 * 
 * This action provides functionality to create links
 * between channels and items with configuration options.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class LinkChannelAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(LinkChannelAction.class);
    private static final String ACTION_ID = "openhab.channels.link";
    private static final String ACTION_NAME = "Link Channel";
    private static final String CATEGORY = "channels";

    @Reference
    private @Nullable ItemChannelLinkRegistry itemChannelLinkRegistry;

    @Reference
    private @Nullable ItemRegistry itemRegistry;

    @Reference
    private @Nullable ThingRegistry thingRegistry;

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
        return "Links a channel to an item with optional configuration parameters";
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
                "The UID of the channel to link (e.g., 'binding:thing:channel')"));
        properties.put("itemName", Map.of("type", "string", "description", "The name of the item to link to"));
        properties.put("configuration", Map.of("type", "object", "description",
                "Optional configuration parameters for the link", "additionalProperties", true));
        properties.put("force", Map.of("type", "boolean", "description", "Force link creation even if validation fails",
                "default", false));

        schema.put("properties", properties);
        schema.put("required", List.of("channelUID", "itemName"));
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("channelUID", Map.of("type", "string", "description", "The channel UID"));
        properties.put("itemName", Map.of("type", "string", "description", "The item name"));
        properties.put("success",
                Map.of("type", "boolean", "description", "Whether the link was created successfully"));
        properties.put("linkExists", Map.of("type", "boolean", "description", "Whether the link already existed"));
        properties.put("configuration", Map.of("type", "object", "description", "Link configuration"));
        properties.put("validationErrors", Map.of("type", "array", "description", "Validation errors if any"));
        properties.put("message", Map.of("type", "string", "description", "Result message"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("channel_linking", true);
        capabilities.put("link_configuration", true);
        capabilities.put("link_validation", true);
        capabilities.put("link_management", true);
        return capabilities;
    }

    @Override
    public ActionValidationResult validateParameters(Map<String, Object> parameters) {
        List<String> errors = new ArrayList<>();

        if (!parameters.containsKey("channelUID")) {
            errors.add("channelUID is required");
        } else {
            Object channelUID = parameters.get("channelUID");
            if (!(channelUID instanceof String) || ((String) channelUID).trim().isEmpty()) {
                errors.add("channelUID must be a non-empty string");
            }
        }

        if (!parameters.containsKey("itemName")) {
            errors.add("itemName is required");
        } else {
            Object itemName = parameters.get("itemName");
            if (!(itemName instanceof String) || ((String) itemName).trim().isEmpty()) {
                errors.add("itemName must be a non-empty string");
            }
        }

        if (errors.isEmpty()) {
            return ActionValidationResult.valid(parameters);
        } else {
            return ActionValidationResult.invalid(errors);
        }
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ActionContext context) throws ActionException {
        logger.debug("Executing LinkChannelAction with context: {}", context.getProtocol());

        long startTime = System.currentTimeMillis();

        try {
            String channelUID = (String) parameters.get("channelUID");
            String itemName = (String) parameters.get("itemName");
            @SuppressWarnings("unchecked")
            Map<String, Object> configuration = (Map<String, Object>) parameters.getOrDefault("configuration",
                    new HashMap<>());
            boolean force = (Boolean) parameters.getOrDefault("force", false);

            Map<String, Object> result = linkChannel(channelUID, itemName, configuration, force);

            long executionTime = System.currentTimeMillis() - startTime;
            return ActionResult.success(result, executionTime);

        } catch (Exception e) {
            logger.error("Error executing LinkChannelAction: {}", e.getMessage(), e);
            throw new ActionException(ACTION_ID, "Failed to link channel: " + e.getMessage(), e);
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
        return ActionMetadata.builder().author("openHAB").description("Links a channel to an item").version("1.0.0")
                .tags(List.of("channels", "links", "items")).build();
    }

    @Override
    public void initialize(ActionContext context) {
        logger.debug("LinkChannelAction initialized with context: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("LinkChannelAction cleanup completed");
    }

    @Override
    public boolean isReady() {
        return itemChannelLinkRegistry != null && itemRegistry != null && thingRegistry != null;
    }

    private Map<String, Object> linkChannel(String channelUID, String itemName, Map<String, Object> configuration,
            boolean force) {

        Map<String, Object> result = new HashMap<>();
        result.put("channelUID", channelUID);
        result.put("itemName", itemName);
        result.put("success", false);

        try {
            // Validate channel exists
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

            // Validate item exists
            Item item = itemRegistry.get(itemName);
            if (item == null) {
                result.put("error", "Item not found: " + itemName);
                return result;
            }

            // Check if link already exists
            Set<ItemChannelLink> existingLinks = itemChannelLinkRegistry.getLinks(uid);
            boolean linkExists = existingLinks.stream().anyMatch(link -> link.getItemName().equals(itemName));
            if (linkExists) {
                result.put("linkExists", true);
                result.put("success", true);
                result.put("message", "Link already exists between channel " + channelUID + " and item " + itemName);
                result.put("configuration", new HashMap<>());
                return result;
            }

            // Validate compatibility
            List<String> validationErrors = validateLink(channel, item);
            result.put("validationErrors", validationErrors);

            if (!validationErrors.isEmpty() && !force) {
                result.put("message", "Link validation failed");
                return result;
            }

            // Create link with real ItemChannelLinkRegistry integration
            Configuration linkConfig = new Configuration();
            configuration.forEach((key, value) -> linkConfig.put(key, value));

            ItemChannelLink link = new ItemChannelLink(itemName, uid, linkConfig);

            // Use real ItemChannelLinkRegistry to add the link
            itemChannelLinkRegistry.add(link);

            result.put("success", true);
            result.put("linkExists", false);
            result.put("configuration", configuration);
            result.put("message", "Link created successfully between channel " + channelUID + " and item " + itemName);
            logger.info("Created link between channel {} and item {} with configuration: {}", channelUID, itemName,
                    configuration);

        } catch (Exception e) {
            logger.error("Error linking channel {} to item {}: {}", channelUID, itemName, e.getMessage(), e);
            result.put("error", "Error creating link: " + e.getMessage());
        }

        return result;
    }

    private List<String> validateLink(Channel channel, Item item) {
        List<String> errors = new ArrayList<>();

        // Check item type compatibility
        String acceptedItemType = channel.getAcceptedItemType();
        if (acceptedItemType != null && !acceptedItemType.equals(item.getType())) {
            errors.add("Item type '" + item.getType() + "' is not compatible with channel's accepted type '"
                    + acceptedItemType + "'");
        }

        // Additional validation could include:
        // - Check if item is already linked to another channel of the same type
        // - Validate configuration parameters
        // - Check binding-specific constraints

        return errors;
    }
}
