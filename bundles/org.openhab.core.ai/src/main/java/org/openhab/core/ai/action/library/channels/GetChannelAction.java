package org.openhab.core.ai.action.library.channels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.api.Action;
import org.openhab.core.ai.action.api.ActionException;
import org.openhab.core.ai.action.api.ActionMetadata;
import org.openhab.core.ai.action.api.ActionResult;
import org.openhab.core.ai.action.api.ActionValidationResult;
import org.openhab.core.ai.common.context.ExecutionContext;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action for retrieving channel information in openHAB.
 * 
 * This action provides functionality to get detailed information
 * about channels including their configuration and state.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class GetChannelAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(GetChannelAction.class);
    private static final String ACTION_ID = "openhab.channels.get";
    private static final String ACTION_NAME = "Get Channel";
    private static final String CATEGORY = "channels";

    @Reference
    private @Nullable ThingRegistry thingRegistry;

    @Reference
    private @Nullable ChannelTypeRegistry channelTypeRegistry;

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
        return "Retrieves detailed information about a specific channel by its UID";
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
                "The UID of the channel to retrieve (e.g., 'binding:thing:channel')"));
        properties.put("includeConfiguration",
                Map.of("type", "boolean", "description", "Include channel configuration details", "default", true));
        properties.put("includeProperties",
                Map.of("type", "boolean", "description", "Include channel properties", "default", true));
        properties.put("includeTypeInfo",
                Map.of("type", "boolean", "description", "Include channel type information", "default", true));
        properties.put("includeState",
                Map.of("type", "boolean", "description", "Include current channel state", "default", true));

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
        properties.put("thingUID", Map.of("type", "string", "description", "The parent thing UID"));
        properties.put("channelId", Map.of("type", "string", "description", "The channel ID"));
        properties.put("label", Map.of("type", "string", "description", "The channel label"));
        properties.put("description", Map.of("type", "string", "description", "The channel description"));
        properties.put("category", Map.of("type", "string", "description", "The channel category"));
        properties.put("kind", Map.of("type", "string", "description", "The channel kind (STATE/TRIGGER)"));
        properties.put("configuration", Map.of("type", "object", "description", "Channel configuration"));
        properties.put("properties", Map.of("type", "object", "description", "Channel properties"));
        properties.put("typeInfo", Map.of("type", "object", "description", "Channel type information"));
        properties.put("state", Map.of("type", "object", "description", "Current channel state"));
        properties.put("linkedItems", Map.of("type", "array", "description", "Linked items"));
        properties.put("found", Map.of("type", "boolean", "description", "Whether the channel was found"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("channel_info", true);
        capabilities.put("channel_configuration", true);
        capabilities.put("channel_properties", true);
        capabilities.put("channel_type_info", true);
        capabilities.put("channel_state", true);
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

        if (errors.isEmpty()) {
            return ActionValidationResult.valid(parameters);
        } else {
            return ActionValidationResult.invalid(errors);
        }
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ExecutionContext context) throws ActionException {
        logger.debug("Executing GetChannelAction with context: {}", context.getProtocol());

        long startTime = System.currentTimeMillis();

        try {
            String channelUID = (String) parameters.get("channelUID");
            boolean includeConfiguration = (Boolean) parameters.getOrDefault("includeConfiguration", true);
            boolean includeProperties = (Boolean) parameters.getOrDefault("includeProperties", true);
            boolean includeTypeInfo = (Boolean) parameters.getOrDefault("includeTypeInfo", true);
            boolean includeState = (Boolean) parameters.getOrDefault("includeState", true);

            Map<String, Object> result = getChannelInfo(channelUID, includeConfiguration, includeProperties,
                    includeTypeInfo, includeState);

            long executionTime = System.currentTimeMillis() - startTime;
            return ActionResult.success(result, executionTime);

        } catch (Exception e) {
            logger.error("Error executing GetChannelAction: {}", e.getMessage(), e);
            throw new ActionException(ACTION_ID, "Failed to get channel: " + e.getMessage(), e);
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
        return ActionMetadata.builder().withAuthor("openHAB")
                .withDescription("Retrieves detailed information about a specific channel").withVersion("1.0.0")
                .withTags(List.of("channels", "things", "automation")).build();
    }

    @Override
    public void initialize(ExecutionContext context) {
        logger.debug("GetChannelAction initialized with context: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("GetChannelAction cleanup completed");
    }

    @Override
    public boolean isReady() {
        return thingRegistry != null && channelTypeRegistry != null;
    }

    private Map<String, Object> getChannelInfo(String channelUID, boolean includeConfiguration,
            boolean includeProperties, boolean includeTypeInfo, boolean includeState) {

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
            result.put("thingUID", thing.getUID().toString());
            result.put("channelId", channel.getUID().getId());
            String channelLabel = "";
            if (channel.getLabel() != null) {
                channelLabel = channel.getLabel();
            }
            result.put("label", channelLabel);

            String channelDescription = "";
            if (channel.getDescription() != null) {
                channelDescription = channel.getDescription();
            }
            result.put("description", channelDescription);
            // Note: Channel doesn't have a direct getCategory() method
            // Category information comes from ChannelType

            String channelKind = "";
            if (channel.getKind() != null) {
                channelKind = channel.getKind().toString();
            }
            result.put("kind", channelKind);

            if (includeConfiguration) {
                result.put("configuration", channel.getConfiguration().getProperties());
            }

            if (includeProperties) {
                result.put("properties", channel.getProperties());
            }

            if (includeTypeInfo) {
                ChannelType channelType = channelTypeRegistry.getChannelType(channel.getChannelTypeUID());
                if (channelType != null) {
                    Map<String, Object> typeInfo = new HashMap<>();
                    typeInfo.put("uid", channelType.getUID().toString());
                    String typeLabel = "";
                    if (channelType.getLabel() != null) {
                        typeLabel = channelType.getLabel();
                    }
                    typeInfo.put("label", typeLabel);

                    String typeDescription = "";
                    if (channelType.getDescription() != null) {
                        typeDescription = channelType.getDescription();
                    }
                    typeInfo.put("description", typeDescription);

                    String typeCategory = "";
                    if (channelType.getCategory() != null) {
                        typeCategory = channelType.getCategory();
                    }
                    typeInfo.put("category", typeCategory);

                    String typeKind = "";
                    if (channelType.getKind() != null) {
                        typeKind = channelType.getKind().toString();
                    }
                    typeInfo.put("kind", typeKind);

                    String typeItemType = "";
                    if (channelType.getItemType() != null) {
                        typeItemType = channelType.getItemType();
                    }
                    typeInfo.put("itemType", typeItemType);
                    String stateDescription = "";
                    if (channelType.getState() != null) {
                        stateDescription = channelType.getState().toString();
                    }
                    typeInfo.put("stateDescription", stateDescription);
                    // Note: ChannelType doesn't have a direct getCommand() method
                    // Command information would be available through other means

                    String autoUpdatePolicy = "";
                    if (channelType.getAutoUpdatePolicy() != null) {
                        autoUpdatePolicy = channelType.getAutoUpdatePolicy().toString();
                    }
                    typeInfo.put("autoUpdatePolicy", autoUpdatePolicy);
                    result.put("typeInfo", typeInfo);
                }
            }

            if (includeState) {
                // Note: Channel state would require integration with ItemRegistry and persistence
                // For now, we'll provide a placeholder
                Map<String, Object> stateInfo = new HashMap<>();
                stateInfo.put("note", "Channel state requires integration with ItemRegistry and persistence services");
                stateInfo.put("linkedItems", getLinkedItems(channel));
                result.put("state", stateInfo);
            }

        } catch (Exception e) {
            logger.error("Error getting channel info for {}: {}", channelUID, e.getMessage(), e);
            result.put("error", "Error retrieving channel information: " + e.getMessage());
        }

        return result;
    }

    private List<String> getLinkedItems(Channel channel) {
        // This would require integration with ItemChannelLinkRegistry
        // For now, return an empty list
        return new ArrayList<>();
    }
}
