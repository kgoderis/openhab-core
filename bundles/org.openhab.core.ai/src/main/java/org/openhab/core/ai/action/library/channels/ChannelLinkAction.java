package org.openhab.core.ai.action.library.channels;

import java.time.Instant;
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
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * AI Action for managing openHAB Channel-Item links (link/unlink operations).
 * 
 * 
 */
@Component(service = Action.class, immediate = true)
@NonNullByDefault
public class ChannelLinkAction implements Action {

    private static final String ACTION_ID = "openhab.channels.link";
    private static final String ACTION_NAME = "Channel Link Management";
    private static final String DESCRIPTION = "Manages openHAB Channel-Item links including creating new links, removing existing links, and modifying link configurations";
    private static final String CATEGORY = "channels";
    private static final String VERSION = "1.0.0";

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
        return DESCRIPTION;
    }

    @Override
    public String getCategory() {
        return CATEGORY;
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public Map<String, Object> getParameterSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("operation",
                Map.of("type", "string", "enum", List.of("link", "unlink", "get", "update"), "description",
                        "Operation to perform: link (create), unlink (remove), get (details), update (modify config)"));
        properties.put("channelUID",
                Map.of("type", "string", "description", "Channel UID (e.g., 'hue:color:bridge:bulb1:color')"));
        properties.put("itemName", Map.of("type", "string", "description", "Item name to link/unlink"));
        properties.put("configuration", Map.of("type", "object", "description",
                "Link configuration parameters (for link/update operations)", "additionalProperties", true));
        properties.put("autoUpdate", Map.of("type", "string", "enum", List.of("DEFAULT", "true", "false"),
                "description", "Auto-update policy for the link", "default", "DEFAULT"));

        schema.put("properties", properties);
        schema.put("required", List.of("operation", "channelUID", "itemName"));
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public ActionValidationResult validateParameters(Map<String, Object> parameters) {
        String operation = (String) parameters.get("operation");
        if (operation == null) {
            return ActionValidationResult.invalid(List.of("operation is required"));
        }

        String channelUID = (String) parameters.get("channelUID");
        if (channelUID == null || channelUID.trim().isEmpty()) {
            return ActionValidationResult.invalid(List.of("channelUID is required and cannot be empty"));
        }

        String itemName = (String) parameters.get("itemName");
        if (itemName == null || itemName.trim().isEmpty()) {
            return ActionValidationResult.invalid(List.of("itemName is required and cannot be empty"));
        }

        // Validate channelUID format
        try {
            new ChannelUID(channelUID);
        } catch (Exception e) {
            return ActionValidationResult.invalid(List.of("Invalid channelUID format: " + channelUID));
        }

        return ActionValidationResult.valid(parameters);
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("timestamp", Map.of("type", "string", "description", "Timestamp of the operation"));
        properties.put("operation", Map.of("type", "string", "description", "Operation performed"));
        properties.put("channelUID", Map.of("type", "string", "description", "Channel UID"));
        properties.put("itemName", Map.of("type", "string", "description", "Item name"));
        properties.put("success", Map.of("type", "boolean", "description", "Whether operation was successful"));
        properties.put("message", Map.of("type", "string", "description", "Operation result message"));
        properties.put("action", Map.of("type", "string", "description", "Action taken"));
        properties.put("exists", Map.of("type", "boolean", "description", "Whether link exists (for get operation)"));
        properties.put("configuration", Map.of("type", "object", "description", "Link configuration"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ActionContext context) throws ActionException {
        long startTime = System.currentTimeMillis();

        try {
            String operation = (String) parameters.get("operation");
            String channelUID = (String) parameters.get("channelUID");
            String itemName = (String) parameters.get("itemName");
            @SuppressWarnings("unchecked")
            Map<String, Object> configParams = (Map<String, Object>) parameters.getOrDefault("configuration",
                    new HashMap<>());
            String autoUpdate = (String) parameters.getOrDefault("autoUpdate", "DEFAULT");

            Map<String, Object> result = performLinkOperation(operation, channelUID, itemName, configParams,
                    autoUpdate);

            long executionTime = System.currentTimeMillis() - startTime;
            return ActionResult.success(result, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            throw new ActionException(ACTION_ID, "Failed to manage channel link: " + e.getMessage(), e);
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
        return ActionMetadata.builder().description(DESCRIPTION).version(VERSION).author("openHAB")
                .tags(List.of("channels", "links", "management")).build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("link_creation", true);
        capabilities.put("link_removal", true);
        capabilities.put("link_modification", true);
        capabilities.put("link_inspection", true);
        capabilities.put("configuration_management", true);
        return capabilities;
    }

    @Override
    public void initialize(ActionContext context) {
        // No initialization required
    }

    @Override
    public void cleanup() {
        // No cleanup required
    }

    @Override
    public boolean isReady() {
        return thingRegistry != null && itemRegistry != null && itemChannelLinkRegistry != null;
    }

    private Map<String, Object> performLinkOperation(String operation, String channelUID, String itemName,
            Map<String, Object> configParams, String autoUpdate) throws ActionException {

        Map<String, Object> result = new HashMap<>();
        result.put("timestamp", Instant.now().toString());
        result.put("operation", operation);
        result.put("channelUID", channelUID);
        result.put("itemName", itemName);

        // Validate services are available
        if (thingRegistry == null || itemRegistry == null || itemChannelLinkRegistry == null) {
            throw new ActionException(ACTION_ID, "Required services not available", "SERVICE_UNAVAILABLE");
        }

        ChannelUID channelUIDObj = new ChannelUID(channelUID);

        // Validate channel exists
        @Nullable
        Channel channel = findChannel(channelUIDObj);
        if (channel == null) {
            throw new ActionException(ACTION_ID, "Channel not found: " + channelUID, "RESOURCE_NOT_FOUND");
        }

        // Validate item exists
        Item item = itemRegistry.get(itemName);
        if (item == null) {
            throw new ActionException(ACTION_ID, "Item not found: " + itemName, "RESOURCE_NOT_FOUND");
        }

        switch (operation) {
            case "link" -> {
                return createLink(channelUIDObj, itemName, configParams, autoUpdate, result);
            }
            case "unlink" -> {
                return removeLink(channelUIDObj, itemName, result);
            }
            case "get" -> {
                return getLinkDetails(channelUIDObj, itemName, result);
            }
            case "update" -> {
                return updateLink(channelUIDObj, itemName, configParams, autoUpdate, result);
            }
            default -> {
                throw new ActionException(ACTION_ID, "Unknown operation: " + operation, "INVALID_PARAMETER");
            }
        }
    }

    private @Nullable Channel findChannel(ChannelUID channelUID) {
        if (thingRegistry == null)
            return null;

        Thing thing = thingRegistry.get(channelUID.getThingUID());
        if (thing == null)
            return null;

        return thing.getChannel(channelUID.getId());
    }

    private Map<String, Object> createLink(ChannelUID channelUID, String itemName, Map<String, Object> configParams,
            String autoUpdate, Map<String, Object> result) throws ActionException {

        // Check if link already exists
        Set<ItemChannelLink> existingLinks = itemChannelLinkRegistry.getLinks(channelUID);
        boolean linkExists = existingLinks.stream().anyMatch(link -> link.getItemName().equals(itemName));

        if (linkExists) {
            result.put("success", false);
            result.put("message", "Link already exists between channel and item");
            result.put("action", "no_change");
            return result;
        }

        // Create configuration
        Configuration config = new Configuration(configParams);

        // Create the link
        ItemChannelLink link = new ItemChannelLink(itemName, channelUID, config);

        // Add the link
        itemChannelLinkRegistry.add(link);

        result.put("success", true);
        result.put("message", "Successfully created link between channel and item");
        result.put("action", "created");
        result.put("linkConfiguration", configParams);

        return result;
    }

    private Map<String, Object> removeLink(ChannelUID channelUID, String itemName, Map<String, Object> result)
            throws ActionException {
        // Find existing link
        Set<ItemChannelLink> existingLinks = itemChannelLinkRegistry.getLinks(channelUID);
        ItemChannelLink linkToRemove = existingLinks.stream().filter(link -> link.getItemName().equals(itemName))
                .findFirst().orElse(null);

        if (linkToRemove == null) {
            result.put("success", false);
            result.put("message", "No link exists between channel and item");
            result.put("action", "no_change");
            return result;
        }

        // Remove the link
        itemChannelLinkRegistry.remove(linkToRemove.getUID());

        result.put("success", true);
        result.put("message", "Successfully removed link between channel and item");
        result.put("action", "removed");

        return result;
    }

    private Map<String, Object> getLinkDetails(ChannelUID channelUID, String itemName, Map<String, Object> result) {
        // Find existing link
        Set<ItemChannelLink> existingLinks = itemChannelLinkRegistry.getLinks(channelUID);
        ItemChannelLink link = existingLinks.stream().filter(l -> l.getItemName().equals(itemName)).findFirst()
                .orElse(null);

        if (link == null) {
            result.put("exists", false);
            result.put("message", "No link exists between channel and item");
            return result;
        }

        result.put("exists", true);
        result.put("linkUID", link.getUID());
        result.put("configuration", link.getConfiguration().getProperties());
        result.put("message", "Link details retrieved successfully");

        return result;
    }

    private Map<String, Object> updateLink(ChannelUID channelUID, String itemName, Map<String, Object> configParams,
            String autoUpdate, Map<String, Object> result) throws ActionException {

        // Find existing link
        Set<ItemChannelLink> existingLinks = itemChannelLinkRegistry.getLinks(channelUID);
        ItemChannelLink existingLink = existingLinks.stream().filter(link -> link.getItemName().equals(itemName))
                .findFirst().orElse(null);

        if (existingLink == null) {
            result.put("success", false);
            result.put("message", "No link exists between channel and item to update");
            result.put("action", "no_change");
            return result;
        }

        // Remove old link
        itemChannelLinkRegistry.remove(existingLink.getUID());

        // Create updated configuration
        Configuration newConfig = new Configuration(configParams);

        // Create new link with updated configuration
        ItemChannelLink newLink = new ItemChannelLink(itemName, channelUID, newConfig);

        // Add the updated link
        itemChannelLinkRegistry.add(newLink);

        result.put("success", true);
        result.put("message", "Successfully updated link configuration");
        result.put("action", "updated");
        result.put("oldConfiguration", existingLink.getConfiguration().getProperties());
        result.put("newConfiguration", configParams);

        return result;
    }
}
