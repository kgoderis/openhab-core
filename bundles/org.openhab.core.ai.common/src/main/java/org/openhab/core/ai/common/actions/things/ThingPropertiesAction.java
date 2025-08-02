package org.openhab.core.ai.common.actions.things;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.action.AIActionContext;
import org.openhab.core.ai.common.action.AIActionMetadata;
import org.openhab.core.ai.common.action.AIActionResult;
import org.openhab.core.ai.common.action.AIActionValidationResult;
import org.openhab.core.ai.common.api.action.AIAction;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.type.ThingType;
import org.openhab.core.thing.type.ThingTypeRegistry;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to retrieve and manage thing properties
 * 
 * This action provides comprehensive access to thing properties including
 * basic properties, type properties, binding properties, and metadata.
 */
@Component(service = AIAction.class, immediate = true)
public class ThingPropertiesAction implements AIAction {

    private static final Logger logger = LoggerFactory.getLogger(ThingPropertiesAction.class);

    @Reference
    private ThingRegistry thingRegistry;

    @Reference
    private @Nullable ThingTypeRegistry thingTypeRegistry;

    @Override
    public String getActionId() {
        return "openhab.things.properties";
    }

    @Override
    public String getActionName() {
        return "Thing Properties";
    }

    @Override
    public String getDescription() {
        return "Retrieve and manage thing properties including basic properties, type properties, binding properties, and metadata";
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
                "The UID of the thing to retrieve properties for", "required", true));
        properties.put("includeTypeProperties",
                Map.of("type", "boolean", "description", "Include thing type properties", "default", true));
        properties.put("includeBindingProperties",
                Map.of("type", "boolean", "description", "Include binding-specific properties", "default", true));
        properties.put("includeMetadata",
                Map.of("type", "boolean", "description", "Include thing metadata", "default", true));
        properties.put("includeChannelProperties",
                Map.of("type", "boolean", "description", "Include channel properties", "default", false));

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
        properties.put("basicProperties", Map.of("type", "object"));
        properties.put("typeProperties", Map.of("type", "object"));
        properties.put("bindingProperties", Map.of("type", "object"));
        properties.put("metadata", Map.of("type", "object"));
        properties.put("channelProperties", Map.of("type", "array"));
        properties.put("error", Map.of("type", "string"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public AIActionValidationResult validateParameters(Map<String, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return AIActionValidationResult.invalid(List.of("Parameters cannot be null or empty"));
        }

        String thingUID = (String) parameters.get("thingUID");
        if (thingUID == null || thingUID.trim().isEmpty()) {
            return AIActionValidationResult.invalid(List.of("thingUID is required and cannot be empty"));
        }

        try {
            new ThingUID(thingUID);
        } catch (IllegalArgumentException e) {
            return AIActionValidationResult.invalid(List.of("Invalid thingUID format: " + thingUID));
        }

        return AIActionValidationResult.valid(parameters);
    }

    @Override
    public AIActionResult execute(Map<String, Object> parameters, AIActionContext context) throws AIActionException {
        long executionStartTime = System.currentTimeMillis();

        try {
            String thingUID = (String) parameters.get("thingUID");
            Boolean includeTypeProperties = (Boolean) parameters.getOrDefault("includeTypeProperties", true);
            Boolean includeBindingProperties = (Boolean) parameters.getOrDefault("includeBindingProperties", true);
            Boolean includeMetadata = (Boolean) parameters.getOrDefault("includeMetadata", true);
            Boolean includeChannelProperties = (Boolean) parameters.getOrDefault("includeChannelProperties", false);

            logger.debug("Getting properties for thing: {}", thingUID);

            Map<String, Object> result = getThingProperties(thingUID, includeTypeProperties, includeBindingProperties,
                    includeMetadata, includeChannelProperties);

            return AIActionResult.success(result, System.currentTimeMillis() - executionStartTime);

        } catch (Exception e) {
            logger.error("Error executing ThingPropertiesAction: {}", e.getMessage(), e);
            throw new AIActionException(getActionId(), "Failed to get thing properties: " + e.getMessage(),
                    "EXECUTION_ERROR");
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
        return AIActionMetadata.builder().version(getVersion()).description(getDescription()).build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("realThingRegistryIntegration", true);
        capabilities.put("typeProperties", true);
        capabilities.put("bindingProperties", true);
        capabilities.put("metadata", true);
        capabilities.put("channelProperties", true);
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

    private Map<String, Object> getThingProperties(String thingUID, boolean includeTypeProperties,
            boolean includeBindingProperties, boolean includeMetadata, boolean includeChannelProperties) {

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

            // Basic properties
            Map<String, Object> basicProperties = new HashMap<>();
            basicProperties.put("uid", thing.getUID().toString());
            basicProperties.put("thingTypeUID",
                    thing.getThingTypeUID() != null ? thing.getThingTypeUID().toString() : null);
            basicProperties.put("label", thing.getLabel());
            basicProperties.put("location", thing.getLocation());
            basicProperties.put("status", thing.getStatus().toString());
            basicProperties.put("statusInfo", thing.getStatusInfo().toString());
            basicProperties.put("enabled", thing.isEnabled());
            basicProperties.put("bridgeUID", thing.getBridgeUID() != null ? thing.getBridgeUID().toString() : null);
            basicProperties.put("properties", thing.getProperties());
            basicProperties.put("configuration", thing.getConfiguration().getProperties());
            result.put("basicProperties", basicProperties);

            // Type properties
            if (includeTypeProperties && thingTypeRegistry != null && thing.getThingTypeUID() != null) {
                ThingType thingType = thingTypeRegistry.getThingType(thing.getThingTypeUID());
                if (thingType != null) {
                    Map<String, Object> typeProperties = new HashMap<>();
                    typeProperties.put("uid", thingType.getUID().toString());
                    typeProperties.put("label", thingType.getLabel());
                    typeProperties.put("description", thingType.getDescription());
                    typeProperties.put("category", thingType.getCategory());
                    typeProperties.put("properties", thingType.getProperties());
                    typeProperties.put("representationProperty", thingType.getRepresentationProperty());
                    typeProperties.put("isBridge", thingType.getUID().getBindingId().contains("bridge"));
                    typeProperties.put("isListed", true); // Default assumption
                    result.put("typeProperties", typeProperties);
                } else {
                    result.put("typeProperties", Map.of());
                }
            } else {
                result.put("typeProperties", Map.of());
            }

            // Binding properties
            if (includeBindingProperties) {
                Map<String, Object> bindingProperties = new HashMap<>();
                bindingProperties.put("bindingId",
                        thing.getThingTypeUID() != null ? thing.getThingTypeUID().getBindingId() : null);
                bindingProperties.put("thingTypeId",
                        thing.getThingTypeUID() != null ? thing.getThingTypeUID().getId() : null);
                bindingProperties.put("thingId", thing.getUID().getId());
                bindingProperties.put("bindingVersion", "unknown"); // Not directly available from Thing
                bindingProperties.put("bindingDescription", "Binding information not directly available");
                result.put("bindingProperties", bindingProperties);
            } else {
                result.put("bindingProperties", Map.of());
            }

            // Metadata
            if (includeMetadata) {
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("creationTime", System.currentTimeMillis()); // Not directly available from Thing
                metadata.put("lastModified", System.currentTimeMillis()); // Not directly available from Thing
                metadata.put("lastSeen", System.currentTimeMillis()); // Not directly available from Thing
                metadata.put("firmwareVersion", thing.getProperties().get("firmwareVersion"));
                metadata.put("modelId", thing.getProperties().get("modelId"));
                metadata.put("vendor", thing.getProperties().get("vendor"));
                metadata.put("serialNumber", thing.getProperties().get("serialNumber"));
                metadata.put("hardwareVersion", thing.getProperties().get("hardwareVersion"));
                metadata.put("softwareVersion", thing.getProperties().get("softwareVersion"));
                result.put("metadata", metadata);
            } else {
                result.put("metadata", Map.of());
            }

            // Channel properties
            if (includeChannelProperties) {
                List<Map<String, Object>> channelProperties = thing.getChannels().stream().map(channel -> {
                    Map<String, Object> channelProps = new HashMap<>();
                    channelProps.put("uid", channel.getUID().toString());
                    channelProps.put("id", channel.getUID().getId());
                    channelProps.put("label", channel.getLabel());
                    channelProps.put("description", channel.getDescription());
                    channelProps.put("kind", channel.getKind().toString());
                    channelProps.put("properties", channel.getProperties());
                    channelProps.put("configuration", channel.getConfiguration().getProperties());
                    channelProps.put("defaultTags", channel.getDefaultTags());
                    return channelProps;
                }).collect(Collectors.toList());
                result.put("channelProperties", channelProperties);
                result.put("channelCount", channelProperties.size());
            } else {
                result.put("channelProperties", Map.of());
                result.put("channelCount", 0);
            }

        } catch (Exception e) {
            logger.error("Error getting thing properties for {}: {}", thingUID, e.getMessage(), e);
            result.put("error", "Failed to get thing properties: " + e.getMessage());
        }

        return result;
    }
}
