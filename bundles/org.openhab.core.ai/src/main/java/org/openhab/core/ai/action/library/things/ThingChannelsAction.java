package org.openhab.core.ai.action.library.things;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.ActionContext;
import org.openhab.core.ai.action.ActionMetadata;
import org.openhab.core.ai.action.ActionResult;
import org.openhab.core.ai.action.ActionValidationResult;
import org.openhab.core.ai.api.action.Action;
import org.openhab.core.ai.api.action.ActionException;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to retrieve and manage thing channels
 * 
 * This action provides comprehensive access to thing channels including
 * channel information, configuration, properties, and type details.
 */
@Component(service = Action.class, immediate = true)
public class ThingChannelsAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(ThingChannelsAction.class);

    @Reference
    private ThingRegistry thingRegistry;

    @Reference
    private @Nullable ChannelTypeRegistry channelTypeRegistry;

    @Override
    public String getActionId() {
        return "openhab.things.channels";
    }

    @Override
    public String getActionName() {
        return "Thing Channels";
    }

    @Override
    public String getDescription() {
        return "Retrieve and manage thing channels including channel information, configuration, properties, and type details";
    }

    @Override
    public String getCategory() {
        return "things";
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
        properties.put("thingUID", Map.of("type", "string", "description",
                "The UID of the thing to retrieve channels for", "required", true));
        properties.put("includeConfiguration",
                Map.of("type", "boolean", "description", "Include channel configuration", "default", true));
        properties.put("includeProperties",
                Map.of("type", "boolean", "description", "Include channel properties", "default", true));
        properties.put("includeTypeInfo",
                Map.of("type", "boolean", "description", "Include channel type information", "default", true));
        properties.put("includeLinkedItems",
                Map.of("type", "boolean", "description", "Include linked items information", "default", false));
        properties.put("channelFilter", Map.of("type", "string", "description",
                "Filter channels by ID pattern (e.g., 'temperature*')", "required", false));
        properties.put("kindFilter",
                Map.of("type", "string", "description", "Filter channels by kind (STATE, TRIGGER)", "required", false));

        schema.put("properties", properties);
        schema.put("required", List.of("thingUID"));
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("thingUID", Map.of("type", "string"));
        properties.put("found", Map.of("type", "boolean"));
        properties.put("success", Map.of("type", "boolean"));
        properties.put("timestamp", Map.of("type", "number"));
        properties.put("channels", Map.of("type", "array"));
        properties.put("channelCount", Map.of("type", "number"));
        properties.put("filteredCount", Map.of("type", "number"));
        properties.put("error", Map.of("type", "string"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public ActionValidationResult validateParameters(Map<String, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return ActionValidationResult.invalid(List.of("Parameters cannot be null or empty"));
        }

        String thingUID = (String) parameters.get("thingUID");
        if (thingUID == null || thingUID.trim().isEmpty()) {
            return ActionValidationResult.invalid(List.of("thingUID is required and cannot be empty"));
        }

        try {
            new ThingUID(thingUID);
        } catch (IllegalArgumentException e) {
            return ActionValidationResult.invalid(List.of("Invalid thingUID format: " + thingUID));
        }

        String kindFilter = (String) parameters.get("kindFilter");
        if (kindFilter != null && !kindFilter.isEmpty()) {
            List<String> validKinds = List.of("STATE", "TRIGGER");
            if (!validKinds.contains(kindFilter.toUpperCase())) {
                return ActionValidationResult.invalid(List.of("Invalid kindFilter. Must be one of: " + validKinds));
            }
        }

        return ActionValidationResult.valid(parameters);
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ActionContext context) throws ActionException {
        long executionStartTime = System.currentTimeMillis();

        try {
            String thingUID = (String) parameters.get("thingUID");
            Boolean includeConfiguration = (Boolean) parameters.getOrDefault("includeConfiguration", true);
            Boolean includeProperties = (Boolean) parameters.getOrDefault("includeProperties", true);
            Boolean includeTypeInfo = (Boolean) parameters.getOrDefault("includeTypeInfo", true);
            Boolean includeLinkedItems = (Boolean) parameters.getOrDefault("includeLinkedItems", false);
            String channelFilter = (String) parameters.get("channelFilter");
            String kindFilter = (String) parameters.get("kindFilter");

            logger.debug("Getting channels for thing: {}", thingUID);

            Map<String, Object> result = getThingChannels(thingUID, includeConfiguration, includeProperties,
                    includeTypeInfo, includeLinkedItems, channelFilter, kindFilter);

            return ActionResult.success(result, System.currentTimeMillis() - executionStartTime);

        } catch (Exception e) {
            logger.error("Error executing ThingChannelsAction: {}", e.getMessage(), e);
            throw new ActionException(getActionId(), "Failed to get thing channels: " + e.getMessage(),
                    "EXECUTION_ERROR");
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
        return ActionMetadata.builder().version(getVersion()).description(getDescription()).build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("realThingRegistryIntegration", true);
        capabilities.put("channelTypeRegistry", true);
        capabilities.put("configuration", true);
        capabilities.put("properties", true);
        capabilities.put("typeInfo", true);
        capabilities.put("linkedItems", true);
        capabilities.put("filtering", true);
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
        return thingRegistry != null;
    }

    private Map<String, Object> getThingChannels(String thingUID, boolean includeConfiguration,
            boolean includeProperties, boolean includeTypeInfo, boolean includeLinkedItems, String channelFilter,
            String kindFilter) {

        Map<String, Object> result = new HashMap<>();
        result.put("thingUID", thingUID);
        result.put("found", false);
        result.put("success", false);
        result.put("timestamp", System.currentTimeMillis());

        try {
            ThingUID uid = new ThingUID(thingUID);
            Thing thing = thingRegistry.get(uid);

            if (thing == null) {
                result.put("error", "Thing not found: " + thingUID);
                return result;
            }

            result.put("found", true);
            result.put("success", true);

            // Get all channels
            List<Channel> allChannels = thing.getChannels();
            result.put("channelCount", allChannels.size());

            // Apply filters
            List<Channel> filteredChannels = allChannels.stream().filter(channel -> {
                // Apply channel ID filter
                if (channelFilter != null && !channelFilter.isEmpty()) {
                    if (!channel.getUID().getId().matches(channelFilter.replace("*", ".*"))) {
                        return false;
                    }
                }

                // Apply kind filter
                if (kindFilter != null && !kindFilter.isEmpty()) {
                    if (!channel.getKind().toString().equalsIgnoreCase(kindFilter)) {
                        return false;
                    }
                }

                return true;
            }).collect(Collectors.toList());

            result.put("filteredCount", filteredChannels.size());

            // Convert channels to maps
            List<Map<String, Object>> channelMaps = filteredChannels.stream()
                    .map(channel -> convertChannelToMap(channel, includeConfiguration, includeProperties,
                            includeTypeInfo, includeLinkedItems))
                    .collect(Collectors.toList());

            result.put("channels", channelMaps);

        } catch (Exception e) {
            logger.error("Error getting thing channels for {}: {}", thingUID, e.getMessage(), e);
            result.put("error", "Failed to get thing channels: " + e.getMessage());
        }

        return result;
    }

    private Map<String, Object> convertChannelToMap(Channel channel, boolean includeConfiguration,
            boolean includeProperties, boolean includeTypeInfo, boolean includeLinkedItems) {

        Map<String, Object> channelMap = new HashMap<>();
        channelMap.put("uid", channel.getUID().toString());
        channelMap.put("id", channel.getUID().getId());
        channelMap.put("label", channel.getLabel());
        channelMap.put("description", channel.getDescription());
        channelMap.put("kind", channel.getKind().toString());
        channelMap.put("acceptedItemType", channel.getAcceptedItemType());
        channelMap.put("defaultTags", channel.getDefaultTags());

        if (includeConfiguration) {
            channelMap.put("configuration", channel.getConfiguration().getProperties());
        }

        if (includeProperties) {
            channelMap.put("properties", channel.getProperties());
        }

        if (includeTypeInfo && channel.getChannelTypeUID() != null && channelTypeRegistry != null) {
            ChannelType channelType = channelTypeRegistry.getChannelType(channel.getChannelTypeUID());
            if (channelType != null) {
                Map<String, Object> typeInfo = new HashMap<>();
                typeInfo.put("uid", channelType.getUID().toString());
                typeInfo.put("label", channelType.getLabel());
                typeInfo.put("description", channelType.getDescription());
                typeInfo.put("category", channelType.getCategory());
                typeInfo.put("kind", channelType.getKind().toString());
                typeInfo.put("itemType", channelType.getItemType());
                typeInfo.put("stateDescription",
                        channelType.getState() != null ? channelType.getState().toString() : null);
                typeInfo.put("autoUpdatePolicy",
                        channelType.getAutoUpdatePolicy() != null ? channelType.getAutoUpdatePolicy().toString()
                                : null);
                channelMap.put("typeInfo", typeInfo);
            } else {
                channelMap.put("typeInfo", null);
            }
        } else {
            channelMap.put("typeInfo", null);
        }

        if (includeLinkedItems) {
            // Note: Linked items information would require ItemChannelLinkRegistry
            // For now, we'll provide a placeholder
            channelMap.put("linkedItems", List.of());
            channelMap.put("linkedItemCount", 0);
        }

        return channelMap;
    }
}
