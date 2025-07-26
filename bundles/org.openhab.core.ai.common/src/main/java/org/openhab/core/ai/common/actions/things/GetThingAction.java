package org.openhab.core.ai.common.actions.things;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.api.action.AIAction;
import org.openhab.core.ai.common.api.action.AIActionContext;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.openhab.core.ai.common.api.action.AIActionMetadata;
import org.openhab.core.ai.common.api.action.AIActionResult;
import org.openhab.core.ai.common.api.action.AIActionValidationResult;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.type.ThingType;
import org.openhab.core.thing.type.ThingTypeRegistry;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to retrieve comprehensive information about a single thing
 * 
 * 
 */
@Component(service = AIAction.class, immediate = true)
public class GetThingAction implements AIAction {

    private static final Logger logger = LoggerFactory.getLogger(GetThingAction.class);

    @Reference
    private ThingRegistry thingRegistry;

    @Reference
    private @Nullable ThingTypeRegistry thingTypeRegistry;

    @Override
    public String getActionId() {
        return "openhab.things.get";
    }

    @Override
    public String getActionName() {
        return "Get Thing";
    }

    @Override
    public String getDescription() {
        return "Retrieve comprehensive information about a single thing";
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
        properties.put("thingUID",
                Map.of("type", "string", "description", "The UID of the thing to retrieve", "required", true));
        properties.put("includeChannels",
                Map.of("type", "boolean", "description", "Include channel information", "default", true));
        properties.put("includeConfiguration",
                Map.of("type", "boolean", "description", "Include configuration information", "default", true));
        properties.put("includeProperties",
                Map.of("type", "boolean", "description", "Include thing properties", "default", true));
        properties.put("includeHandler",
                Map.of("type", "boolean", "description", "Include thing handler information", "default", false));

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
        properties.put("basicInfo", Map.of("type", "object"));
        properties.put("statusInfo", Map.of("type", "object"));
        properties.put("typeInfo", Map.of("type", "object"));
        properties.put("configuration", Map.of("type", "object"));
        properties.put("properties", Map.of("type", "object"));
        properties.put("channels", Map.of("type", "array"));
        properties.put("handler", Map.of("type", "object"));
        properties.put("success", Map.of("type", "boolean"));
        properties.put("timestamp", Map.of("type", "number"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public AIActionValidationResult validateParameters(Map<String, Object> parameters) {
        if (parameters == null) {
            return AIActionValidationResult.invalid(List.of("Parameters cannot be null"));
        }

        String thingUID = (String) parameters.get("thingUID");
        if (thingUID == null || thingUID.trim().isEmpty()) {
            return AIActionValidationResult.invalid(List.of("Thing UID is required and cannot be empty"));
        }

        // Validate that the thing exists
        try {
            ThingUID uid = new ThingUID(thingUID);
            thingRegistry.get(uid);
        } catch (IllegalArgumentException e) {
            return AIActionValidationResult.invalid(List.of("Invalid thing UID format: " + thingUID));
        } catch (Exception e) {
            return AIActionValidationResult.invalid(List.of("Error validating parameters: " + e.getMessage()));
        }

        return AIActionValidationResult.valid(parameters);
    }

    @Override
    public AIActionResult execute(Map<String, Object> parameters, AIActionContext context) throws AIActionException {
        long executionStartTime = System.currentTimeMillis();

        try {
            String thingUID = (String) parameters.get("thingUID");
            Boolean includeChannels = (Boolean) parameters.getOrDefault("includeChannels", true);
            Boolean includeConfiguration = (Boolean) parameters.getOrDefault("includeConfiguration", true);
            Boolean includeProperties = (Boolean) parameters.getOrDefault("includeProperties", true);
            Boolean includeHandler = (Boolean) parameters.getOrDefault("includeHandler", false);

            logger.debug("Getting comprehensive information for thing: {}", thingUID);

            // Get the thing
            ThingUID uid = new ThingUID(thingUID);
            Thing thing = thingRegistry.get(uid);

            if (thing == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("error", "Thing not found");
                result.put("timestamp", System.currentTimeMillis());
                return AIActionResult.success(result, System.currentTimeMillis() - executionStartTime);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("thingUID", thingUID);
            result.put("success", true);
            result.put("timestamp", System.currentTimeMillis());

            // Basic thing information
            Map<String, Object> basicInfo = new HashMap<>();
            basicInfo.put("uid", thing.getUID().getAsString());
            basicInfo.put("thingTypeUID", thing.getThingTypeUID().getAsString());
            basicInfo.put("bindingId", thing.getThingTypeUID().getBindingId());
            basicInfo.put("label", thing.getLabel());
            basicInfo.put("location", thing.getLocation());
            basicInfo.put("bridgeUID", thing.getBridgeUID() != null ? thing.getBridgeUID().getAsString() : null);
            result.put("basicInfo", basicInfo);

            // Status information
            Map<String, Object> statusInfo = new HashMap<>();
            ThingStatus status = thing.getStatus();
            statusInfo.put("status", status.toString());
            statusInfo.put("statusDetail", thing.getStatusInfo().getStatusDetail());
            statusInfo.put("statusDescription", thing.getStatusInfo().getDescription());
            result.put("statusInfo", statusInfo);

            // Type information
            Map<String, Object> typeInfo = new HashMap<>();
            if (thingTypeRegistry != null) {
                ThingType thingType = thingTypeRegistry.getThingType(thing.getThingTypeUID());
                if (thingType != null) {
                    typeInfo.put("label", thingType.getLabel());
                    typeInfo.put("description", thingType.getDescription());
                    typeInfo.put("category", thingType.getCategory());
                    typeInfo.put("properties", thingType.getProperties());
                    typeInfo.put("representationProperty", thingType.getRepresentationProperty());
                    typeInfo.put("isBridge", thingType.getUID().getBindingId().contains("bridge"));
                    typeInfo.put("isListed", true); // Default assumption
                }
            }
            result.put("typeInfo", typeInfo);

            // Configuration information
            if (includeConfiguration) {
                Map<String, Object> configInfo = new HashMap<>();
                configInfo.put("configuration", thing.getConfiguration().getProperties());
                configInfo.put("configurationKeys",
                        thing.getConfiguration().getProperties().keySet().stream().collect(Collectors.toList()));
                result.put("configuration", configInfo);
            } else {
                result.put("configuration", null);
            }

            // Properties information
            if (includeProperties) {
                Map<String, Object> propsInfo = new HashMap<>();
                propsInfo.put("properties", thing.getProperties());
                propsInfo.put("propertyKeys", thing.getProperties().keySet().stream().collect(Collectors.toList()));
                result.put("properties", propsInfo);
            } else {
                result.put("properties", null);
            }

            // Channels information
            if (includeChannels) {
                List<Map<String, Object>> channelsInfo = thing.getChannels().stream().map(this::convertChannelToMap)
                        .collect(Collectors.toList());
                result.put("channels", channelsInfo);
                result.put("channelCount", channelsInfo.size());
            } else {
                result.put("channels", null);
                result.put("channelCount", 0);
            }

            // Handler information
            if (includeHandler) {
                Map<String, Object> handlerInfo = new HashMap<>();
                ThingHandler handler = thing.getHandler();
                if (handler != null) {
                    handlerInfo.put("hasHandler", true);
                    handlerInfo.put("handlerClass", handler.getClass().getSimpleName());
                    handlerInfo.put("handlerClassName", handler.getClass().getName());
                } else {
                    handlerInfo.put("hasHandler", false);
                }
                result.put("handler", handlerInfo);
            } else {
                result.put("handler", null);
            }

            long executionTime = System.currentTimeMillis() - executionStartTime;
            logger.debug("Successfully retrieved comprehensive information for thing: {} in {}ms", thingUID,
                    executionTime);

            return AIActionResult.success(result, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - executionStartTime;
            logger.error("Error getting thing information", e);
            throw new AIActionException(getActionId(), "Failed to get thing information: " + e.getMessage(), e);
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
        return AIActionMetadata.builder().version(getVersion()).author("openHAB")
                .description("Retrieve comprehensive information about a single openHAB thing")
                .tags(List.of("things", "information", "details", "comprehensive"))
                .documentation(
                        "Retrieves comprehensive information about a single openHAB thing including basic info, status, type, configuration, properties, channels, and handler information.")
                .examples(List.of("Get basic info: {\"thingUID\": \"binding:type:thing\"}",
                        "Get with channels: {\"thingUID\": \"binding:type:thing\", \"includeChannels\": true}",
                        "Get with configuration: {\"thingUID\": \"binding:type:thing\", \"includeConfiguration\": true}",
                        "Get with properties: {\"thingUID\": \"binding:type:thing\", \"includeProperties\": true}",
                        "Get with handler: {\"thingUID\": \"binding:type:thing\", \"includeHandler\": true}",
                        "Get everything: {\"thingUID\": \"binding:type:thing\", \"includeChannels\": true, \"includeConfiguration\": true, \"includeProperties\": true, \"includeHandler\": true}"))
                .build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("async", true);
        capabilities.put("validation", true);
        capabilities.put("comprehensiveInfo", true);
        capabilities.put("channelInfo", true);
        capabilities.put("configurationInfo", true);
        capabilities.put("propertiesInfo", true);
        capabilities.put("handlerInfo", true);
        capabilities.put("statusInfo", true);
        capabilities.put("typeInfo", true);
        return capabilities;
    }

    @Override
    public void initialize(AIActionContext context) {
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

    /**
     * Convert a channel to a map representation
     */
    private Map<String, Object> convertChannelToMap(Channel channel) {
        Map<String, Object> channelMap = new HashMap<>();
        channelMap.put("uid", channel.getUID().getAsString());
        channelMap.put("id", channel.getUID().getId());
        channelMap.put("label", channel.getLabel());
        channelMap.put("description", channel.getDescription());
        channelMap.put("typeUID",
                channel.getChannelTypeUID() != null ? channel.getChannelTypeUID().getAsString() : null);
        channelMap.put("kind", channel.getKind().toString());
        channelMap.put("properties", channel.getProperties());
        channelMap.put("configuration", channel.getConfiguration().getProperties());
        channelMap.put("defaultTags", channel.getDefaultTags());
        return channelMap;
    }
}
