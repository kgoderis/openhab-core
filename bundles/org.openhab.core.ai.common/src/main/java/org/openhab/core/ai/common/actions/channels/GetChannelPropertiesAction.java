package org.openhab.core.ai.common.actions.channels;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.api.action.AIAction;
import org.openhab.core.ai.common.api.action.AIActionContext;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.openhab.core.ai.common.api.action.AIActionMetadata;
import org.openhab.core.ai.common.api.action.AIActionResult;
import org.openhab.core.ai.common.api.action.AIActionValidationResult;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AIAction for retrieving properties for a specific channel.
 * 
 * This action provides detailed property information about a channel including
 * metadata, capabilities, and binding-specific properties.
 */
@Component(service = AIAction.class, immediate = true)
@NonNullByDefault
public class GetChannelPropertiesAction implements AIAction {

    private static final Logger logger = LoggerFactory.getLogger(GetChannelPropertiesAction.class);
    private static final String ACTION_ID = "openhab.channels.get-properties";
    private static final String ACTION_NAME = "Get Channel Properties";
    private static final String CATEGORY = "channels";

    @Reference
    private ThingRegistry thingRegistry;

    @Reference
    private ChannelTypeRegistry channelTypeRegistry;

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
        return "Retrieves detailed properties for a specific channel including metadata and capabilities";
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
                "The UID of the channel to retrieve properties for (e.g., 'binding:thing:channel')"));
        properties.put("includeTypeProperties",
                Map.of("type", "boolean", "description", "Include channel type properties", "default", true));
        properties.put("includeBindingProperties",
                Map.of("type", "boolean", "description", "Include binding-specific properties", "default", true));

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
        properties.put("basicProperties", Map.of("type", "object", "description", "Basic channel properties"));
        properties.put("typeProperties", Map.of("type", "object", "description", "Channel type properties"));
        properties.put("bindingProperties", Map.of("type", "object", "description", "Binding-specific properties"));
        properties.put("metadata", Map.of("type", "object", "description", "Channel metadata"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("channel_properties", true);
        capabilities.put("type_properties", true);
        capabilities.put("binding_properties", true);
        capabilities.put("metadata_access", true);
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
        logger.debug("Executing GetChannelPropertiesAction with context: {}", context.getProtocol());

        long startTime = System.currentTimeMillis();

        try {
            String channelUID = (String) parameters.get("channelUID");
            boolean includeTypeProperties = (Boolean) parameters.getOrDefault("includeTypeProperties", true);
            boolean includeBindingProperties = (Boolean) parameters.getOrDefault("includeBindingProperties", true);

            Map<String, Object> result = getChannelProperties(channelUID, includeTypeProperties,
                    includeBindingProperties);

            long executionTime = System.currentTimeMillis() - startTime;
            return AIActionResult.success(result, executionTime);

        } catch (Exception e) {
            logger.error("Error executing GetChannelPropertiesAction: {}", e.getMessage(), e);
            throw new AIActionException(ACTION_ID, "Failed to get channel properties: " + e.getMessage(), e);
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
                .description("Retrieves detailed properties for a specific channel").version("1.0.0")
                .tags(List.of("channels", "properties", "things")).build();
    }

    @Override
    public void initialize(AIActionContext context) {
        logger.debug("GetChannelPropertiesAction initialized with context: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("GetChannelPropertiesAction cleanup completed");
    }

    @Override
    public boolean isReady() {
        return thingRegistry != null && channelTypeRegistry != null;
    }

    private Map<String, Object> getChannelProperties(String channelUID, boolean includeTypeProperties,
            boolean includeBindingProperties) {

        Map<String, Object> result = new HashMap<>();
        result.put("channelUID", channelUID);
        result.put("found", false);

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

            // Basic properties
            Map<String, Object> basicProperties = new HashMap<>();
            basicProperties.put("uid", channel.getUID().toString());
            basicProperties.put("id", channel.getUID().getId());
            basicProperties.put("label", channel.getLabel());
            basicProperties.put("description", channel.getDescription());
            basicProperties.put("kind", channel.getKind().toString());
            basicProperties.put("acceptedItemType", channel.getAcceptedItemType());
            basicProperties.put("channelTypeUID",
                    channel.getChannelTypeUID() != null ? channel.getChannelTypeUID().toString() : null);
            result.put("basicProperties", basicProperties);

            // Type properties
            if (includeTypeProperties && channel.getChannelTypeUID() != null) {
                ChannelType channelType = channelTypeRegistry.getChannelType(channel.getChannelTypeUID());
                if (channelType != null) {
                    Map<String, Object> typeProperties = new HashMap<>();
                    typeProperties.put("uid", channelType.getUID().toString());
                    typeProperties.put("label", channelType.getLabel());
                    typeProperties.put("description", channelType.getDescription());
                    typeProperties.put("category", channelType.getCategory());
                    typeProperties.put("kind", channelType.getKind().toString());
                    typeProperties.put("itemType", channelType.getItemType());
                    typeProperties.put("stateDescription",
                            channelType.getState() != null ? channelType.getState().toString() : null);
                    typeProperties.put("autoUpdatePolicy",
                            channelType.getAutoUpdatePolicy() != null ? channelType.getAutoUpdatePolicy().toString()
                                    : null);
                    result.put("typeProperties", typeProperties);
                }
            }

            // Binding properties
            if (includeBindingProperties) {
                Map<String, Object> bindingProperties = new HashMap<>();
                bindingProperties.put("bindingId", thing.getUID().getBindingId());
                bindingProperties.put("thingUID", thing.getUID().toString());
                bindingProperties.put("thingLabel", thing.getLabel());
                bindingProperties.put("thingType",
                        thing.getThingTypeUID() != null ? thing.getThingTypeUID().toString() : null);
                bindingProperties.put("thingStatus", thing.getStatus().toString());
                bindingProperties.put("thingLocation", thing.getLocation());
                result.put("bindingProperties", bindingProperties);
            }

            // Metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("retrievedAt", Instant.now().toString());
            metadata.put("thingStatus", thing.getStatus().toString());
            metadata.put("channelCount", thing.getChannels().size());
            result.put("metadata", metadata);

        } catch (Exception e) {
            logger.error("Error getting channel properties for {}: {}", channelUID, e.getMessage(), e);
            result.put("error", "Error retrieving channel properties: " + e.getMessage());
        }

        return result;
    }
}
