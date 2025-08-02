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
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.link.ItemChannelLink;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action for retrieving channel links in openHAB.
 * 
 * This action provides functionality to get information about
 * links between channels and items.
 * 
 * @author AI Assistant
 * @since 1.0.0
 */
@NonNullByDefault
public class GetChannelLinksAction implements AIAction {

    private static final Logger logger = LoggerFactory.getLogger(GetChannelLinksAction.class);
    private static final String ACTION_ID = "openhab.channels.get-links";
    private static final String ACTION_NAME = "Get Channel Links";
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
        return "Retrieves information about item-channel links including configuration and item details";
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
                "The UID of the channel to get links for (e.g., 'binding:thing:channel')"));
        properties.put("includeItemDetails",
                Map.of("type", "boolean", "description", "Include detailed item information", "default", true));
        properties.put("includeConfiguration",
                Map.of("type", "boolean", "description", "Include link configuration details", "default", true));

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
        properties.put("linkCount", Map.of("type", "integer", "description", "Number of links found"));
        properties.put("links", Map.of("type", "array", "description", "List of channel links"));
        properties.put("summary", Map.of("type", "object", "description", "Summary of link information"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("channel_links", true);
        capabilities.put("item_details", true);
        capabilities.put("link_configuration", true);
        capabilities.put("link_statistics", true);
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
        logger.debug("Executing GetChannelLinksAction with context: {}", context.getProtocol());

        long startTime = System.currentTimeMillis();

        try {
            String channelUID = (String) parameters.get("channelUID");
            boolean includeItemDetails = (Boolean) parameters.getOrDefault("includeItemDetails", true);
            boolean includeConfiguration = (Boolean) parameters.getOrDefault("includeConfiguration", true);

            Map<String, Object> result = getChannelLinks(channelUID, includeItemDetails, includeConfiguration);

            long executionTime = System.currentTimeMillis() - startTime;
            return AIActionResult.success(result, executionTime);

        } catch (Exception e) {
            logger.error("Error executing GetChannelLinksAction: {}", e.getMessage(), e);
            throw new AIActionException(ACTION_ID, "Failed to get channel links: " + e.getMessage(), e);
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
        return AIActionMetadata.builder().author("openHAB")
                .description("Retrieves information about item-channel links").version("1.0.0")
                .tags(List.of("channels", "links", "items")).build();
    }

    @Override
    public void initialize(AIActionContext context) {
        logger.debug("GetChannelLinksAction initialized with context: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("GetChannelLinksAction cleanup completed");
    }

    @Override
    public boolean isReady() {
        return itemChannelLinkRegistry != null && itemRegistry != null;
    }

    private Map<String, Object> getChannelLinks(String channelUID, boolean includeItemDetails,
            boolean includeConfiguration) {

        Map<String, Object> result = new HashMap<>();
        result.put("channelUID", channelUID);
        result.put("found", false);

        try {
            ChannelUID uid = new ChannelUID(channelUID);
            Set<ItemChannelLink> links = itemChannelLinkRegistry.getLinks(uid);

            result.put("found", true);
            result.put("linkCount", links.size());

            List<Map<String, Object>> linkList = new ArrayList<>();
            for (ItemChannelLink link : links) {
                Map<String, Object> linkInfo = new HashMap<>();
                linkInfo.put("itemName", link.getItemName());
                linkInfo.put("channelUID", link.getLinkedUID().toString());

                if (includeConfiguration) {
                    linkInfo.put("configuration", link.getConfiguration().getProperties());
                }

                if (includeItemDetails) {
                    Item item = itemRegistry.get(link.getItemName());
                    if (item != null) {
                        Map<String, Object> itemDetails = new HashMap<>();
                        itemDetails.put("name", item.getName());
                        itemDetails.put("type", item.getType());
                        String itemLabel = "";
                        if (item.getLabel() != null) {
                            itemLabel = item.getLabel();
                        }
                        itemDetails.put("label", itemLabel);
                        String itemCategory = "";
                        if (item.getCategory() != null) {
                            itemCategory = item.getCategory();
                        }
                        itemDetails.put("category", itemCategory);
                        itemDetails.put("tags", item.getTags());
                        itemDetails.put("state", item.getState() != null ? item.getState().toString() : "NULL");
                        linkInfo.put("itemDetails", itemDetails);
                    }
                }

                linkList.add(linkInfo);
            }

            result.put("links", linkList);

            // Summary information
            Map<String, Object> summary = new HashMap<>();
            summary.put("totalLinks", links.size());
            summary.put("linkedItems", links.stream().map(ItemChannelLink::getItemName).distinct().count());
            summary.put("retrievedAt", Instant.now().toString());
            result.put("summary", summary);

        } catch (Exception e) {
            logger.error("Error getting channel links for {}: {}", channelUID, e.getMessage(), e);
            result.put("error", "Error retrieving channel links: " + e.getMessage());
        }

        return result;
    }
}
