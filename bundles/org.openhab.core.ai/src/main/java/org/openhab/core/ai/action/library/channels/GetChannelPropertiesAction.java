package org.openhab.core.ai.action.library.channels;

import java.time.Instant;
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
 * Action for retrieving channel properties in openHAB.
 * 
 * This action provides functionality to get properties
 * of channels including their metadata and configuration.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class GetChannelPropertiesAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(GetChannelPropertiesAction.class);
    private static final String ACTION_ID = "openhab.channels.get-properties";
    private static final String ACTION_NAME = "Get Channel Properties";
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
        logger.debug("Executing GetChannelPropertiesAction with context: {}", context.getProtocol());

        long startTime = System.currentTimeMillis();

        try {
            String channelUID = (String) parameters.get("channelUID");
            boolean includeTypeProperties = (Boolean) parameters.getOrDefault("includeTypeProperties", true);
            boolean includeBindingProperties = (Boolean) parameters.getOrDefault("includeBindingProperties", true);

            Map<String, Object> result = getChannelProperties(channelUID, includeTypeProperties,
                    includeBindingProperties);

            long executionTime = System.currentTimeMillis() - startTime;
            return ActionResult.success(result, executionTime);

        } catch (Exception e) {
            logger.error("Error executing GetChannelPropertiesAction: {}", e.getMessage(), e);
            throw new ActionException(ACTION_ID, "Failed to get channel properties: " + e.getMessage(), e);
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
                .withDescription("Retrieves detailed properties for a specific channel").withVersion("1.0.0")
                .withTags(List.of("channels", "properties", "things")).build();
    }

    @Override
    public void initialize(ExecutionContext context) {
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
            String uidString = "";
            if (channel.getUID() != null && channel.getUID().toString() != null) {
                uidString = channel.getUID().toString();
            }
            basicProperties.put("uid", uidString);

            String idString = "";
            if (channel.getUID() != null && channel.getUID().getId() != null) {
                idString = channel.getUID().getId();
            }
            basicProperties.put("id", idString);
            String labelString = "";
            if (channel.getLabel() != null) {
                labelString = channel.getLabel();
            }
            basicProperties.put("label", labelString);

            String descriptionString = "";
            if (channel.getDescription() != null) {
                descriptionString = channel.getDescription();
            }
            basicProperties.put("description", descriptionString);

            String kindString = "";
            if (channel.getKind() != null) {
                kindString = channel.getKind().toString();
            }
            basicProperties.put("kind", kindString);

            String acceptedItemTypeString = "";
            if (channel.getAcceptedItemType() != null) {
                acceptedItemTypeString = channel.getAcceptedItemType();
            }
            basicProperties.put("acceptedItemType", acceptedItemTypeString);

            String channelTypeUIDString = "";
            if (channel.getChannelTypeUID() != null) {
                channelTypeUIDString = channel.getChannelTypeUID().toString();
            }
            basicProperties.put("channelTypeUID", channelTypeUIDString);
            result.put("basicProperties", basicProperties);

            // Type properties
            if (includeTypeProperties && channel.getChannelTypeUID() != null) {
                ChannelType channelType = channelTypeRegistry.getChannelType(channel.getChannelTypeUID());
                if (channelType != null) {
                    Map<String, Object> typeProperties = new HashMap<>();
                    String typeUidString = "";
                    if (channelType.getUID() != null && channelType.getUID().toString() != null) {
                        typeUidString = channelType.getUID().toString();
                    }
                    typeProperties.put("uid", typeUidString);
                    String typeLabelString = "";
                    if (channelType.getLabel() != null) {
                        typeLabelString = channelType.getLabel();
                    }
                    typeProperties.put("label", typeLabelString);

                    String typeDescriptionString = "";
                    if (channelType.getDescription() != null) {
                        typeDescriptionString = channelType.getDescription();
                    }
                    typeProperties.put("description", typeDescriptionString);

                    String typeCategoryString = "";
                    if (channelType.getCategory() != null) {
                        typeCategoryString = channelType.getCategory();
                    }
                    typeProperties.put("category", typeCategoryString);

                    String typeKindString = "";
                    if (channelType.getKind() != null) {
                        typeKindString = channelType.getKind().toString();
                    }
                    typeProperties.put("kind", typeKindString);

                    String typeItemTypeString = "";
                    if (channelType.getItemType() != null) {
                        typeItemTypeString = channelType.getItemType();
                    }
                    typeProperties.put("itemType", typeItemTypeString);

                    String typeStateString = "";
                    if (channelType.getState() != null) {
                        typeStateString = channelType.getState().toString();
                    }
                    typeProperties.put("stateDescription", typeStateString);

                    String typeAutoUpdatePolicyString = "";
                    if (channelType.getAutoUpdatePolicy() != null) {
                        typeAutoUpdatePolicyString = channelType.getAutoUpdatePolicy().toString();
                    }
                    typeProperties.put("autoUpdatePolicy", typeAutoUpdatePolicyString);
                    result.put("typeProperties", typeProperties);
                }
            }

            // Binding properties
            if (includeBindingProperties) {
                Map<String, Object> bindingProperties = new HashMap<>();
                String bindingIdString = "";
                if (thing.getUID() != null && thing.getUID().getBindingId() != null) {
                    bindingIdString = thing.getUID().getBindingId();
                }
                bindingProperties.put("bindingId", bindingIdString);

                String thingUidString = "";
                if (thing.getUID() != null && thing.getUID().toString() != null) {
                    thingUidString = thing.getUID().toString();
                }
                bindingProperties.put("thingUID", thingUidString);
                String thingLabelString = "";
                if (thing.getLabel() != null) {
                    thingLabelString = thing.getLabel();
                }
                bindingProperties.put("thingLabel", thingLabelString);

                String thingTypeString = "";
                if (thing.getThingTypeUID() != null) {
                    thingTypeString = thing.getThingTypeUID().toString();
                }
                bindingProperties.put("thingType", thingTypeString);

                String thingStatusString = "";
                if (thing.getStatus() != null) {
                    thingStatusString = thing.getStatus().toString();
                }
                bindingProperties.put("thingStatus", thingStatusString);

                String thingLocationString = "";
                if (thing.getLocation() != null) {
                    thingLocationString = thing.getLocation();
                }
                bindingProperties.put("thingLocation", thingLocationString);
                result.put("bindingProperties", bindingProperties);
            }

            // Metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("retrievedAt", Instant.now().toString());
            String metadataThingStatusString = "";
            if (thing.getStatus() != null) {
                metadataThingStatusString = thing.getStatus().toString();
            }
            metadata.put("thingStatus", metadataThingStatusString);
            metadata.put("channelCount", thing.getChannels().size());
            result.put("metadata", metadata);

        } catch (Exception e) {
            logger.error("Error getting channel properties for {}: {}", channelUID, e.getMessage(), e);
            result.put("error", "Error retrieving channel properties: " + e.getMessage());
        }

        return result;
    }
}
