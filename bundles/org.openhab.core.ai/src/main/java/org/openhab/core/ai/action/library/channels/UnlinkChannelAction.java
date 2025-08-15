package org.openhab.core.ai.action.library.channels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.api.Action;
import org.openhab.core.ai.action.api.ActionContext;
import org.openhab.core.ai.action.api.ActionException;
import org.openhab.core.ai.action.api.ActionMetadata;
import org.openhab.core.ai.action.api.ActionResult;
import org.openhab.core.ai.action.api.ActionValidationResult;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.link.ItemChannelLink;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action for unlinking channels from items in openHAB.
 * 
 * This action provides functionality to remove links
 * between channels and items.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class UnlinkChannelAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(UnlinkChannelAction.class);
    private static final String ACTION_ID = "openhab.channels.unlink";
    private static final String ACTION_NAME = "Unlink Channel";
    private static final String CATEGORY = "channels";

    @Reference
    private @Nullable ItemChannelLinkRegistry itemChannelLinkRegistry;

    @Reference
    private @Nullable ItemRegistry itemRegistry;

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
        return "Unlinks a channel from an item";
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
                "The UID of the channel to unlink (e.g., 'binding:thing:channel')"));
        properties.put("itemName", Map.of("type", "string", "description", "The name of the item to unlink from"));
        properties.put("force",
                Map.of("type", "boolean", "description", "Force unlink even if validation fails", "default", false));

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
        properties.put("success", Map.of("type", "boolean", "description", "Whether the unlink was successful"));
        properties.put("linkExisted",
                Map.of("type", "boolean", "description", "Whether the link existed before unlinking"));
        properties.put("message", Map.of("type", "string", "description", "Result message"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("channel_unlinking", true);
        capabilities.put("link_removal", true);
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
        logger.debug("Executing UnlinkChannelAction with context: {}", context.getProtocol());

        long startTime = System.currentTimeMillis();

        try {
            String channelUID = (String) parameters.get("channelUID");
            String itemName = (String) parameters.get("itemName");
            boolean force = (Boolean) parameters.getOrDefault("force", false);

            Map<String, Object> result = unlinkChannel(channelUID, itemName, force);

            long executionTime = System.currentTimeMillis() - startTime;
            return ActionResult.success(result, executionTime);

        } catch (Exception e) {
            logger.error("Error executing UnlinkChannelAction: {}", e.getMessage(), e);
            throw new ActionException(ACTION_ID, "Failed to unlink channel: " + e.getMessage(), e);
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
        return ActionMetadata.builder().author("openHAB").description("Unlinks a channel from an item").version("1.0.0")
                .tags(List.of("channels", "links", "items")).build();
    }

    @Override
    public void initialize(ActionContext context) {
        logger.debug("UnlinkChannelAction initialized with context: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("UnlinkChannelAction cleanup completed");
    }

    @Override
    public boolean isReady() {
        return itemChannelLinkRegistry != null;
    }

    private Map<String, Object> unlinkChannel(String channelUID, String itemName, boolean force) {

        Map<String, Object> result = new HashMap<>();
        result.put("channelUID", channelUID);
        result.put("itemName", itemName);
        result.put("success", false);

        try {
            ChannelUID uid = new ChannelUID(channelUID);

            // Check if link exists
            Set<ItemChannelLink> existingLinks = itemChannelLinkRegistry.getLinks(uid);
            boolean linkExists = existingLinks.stream().anyMatch(link -> link.getItemName().equals(itemName));

            result.put("linkExisted", linkExists);

            if (!linkExists) {
                result.put("success", true);
                result.put("message", "No link exists between channel " + channelUID + " and item " + itemName);
                return result;
            }

            // Use real ItemChannelLinkRegistry to remove the link
            ItemChannelLink linkToRemove = new ItemChannelLink(itemName, uid);
            itemChannelLinkRegistry.remove(linkToRemove.getUID());

            result.put("success", true);
            result.put("message", "Link removed successfully between channel " + channelUID + " and item " + itemName);
            logger.info("Removed link between channel {} and item {}", channelUID, itemName);

        } catch (Exception e) {
            logger.error("Error unlinking channel {} from item {}: {}", channelUID, itemName, e.getMessage(), e);
            result.put("error", "Error removing link: " + e.getMessage());
        }

        return result;
    }
}
