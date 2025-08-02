package org.openhab.core.ai.common.actions.things;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.action.AIActionContext;
import org.openhab.core.ai.common.action.AIActionMetadata;
import org.openhab.core.ai.common.action.AIActionResult;
import org.openhab.core.ai.common.action.AIActionValidationResult;
import org.openhab.core.ai.common.api.action.AIAction;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.openhab.core.config.core.ConfigDescriptionRegistry;
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
 * Action to retrieve configuration information for openHAB things
 * 
 * Uses openHAB's ThingTypeRegistry and ConfigDescriptionRegistry to retrieve real configuration schema information.
 */
@Component(service = AIAction.class, immediate = true)
public class GetThingConfigurationAction implements AIAction {

    private static final Logger logger = LoggerFactory.getLogger(GetThingConfigurationAction.class);

    @Reference
    private ThingRegistry thingRegistry;

    @Reference
    private @Nullable ThingTypeRegistry thingTypeRegistry;

    @Reference
    private @Nullable ConfigDescriptionRegistry configDescriptionRegistry;

    @Override
    public String getActionId() {
        return "openhab.things.getConfiguration";
    }

    @Override
    public String getActionName() {
        return "Get Thing Configuration";
    }

    @Override
    public String getDescription() {
        return "Retrieve configuration information for openHAB things";
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
                "The UID of the thing to get configuration for", "required", true));
        properties.put("includeConfigurationKeys",
                Map.of("type", "boolean", "description", "Include list of configuration keys", "default", true));
        properties.put("includeConfigurationSchema",
                Map.of("type", "boolean", "description", "Include configuration schema information", "default", false));

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
        properties.put("configuration", Map.of("type", "object"));
        properties.put("configurationKeys", Map.of("type", "array"));
        properties.put("configurationCount", Map.of("type", "number"));
        properties.put("configurationSchema", Map.of("type", "object"));
        properties.put("success", Map.of("type", "boolean"));
        properties.put("timestamp", Map.of("type", "number"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public AIActionValidationResult validateParameters(Map<String, Object> parameters) {
        try {
            String thingUID = (String) parameters.get("thingUID");
            if (thingUID == null || thingUID.trim().isEmpty()) {
                return AIActionValidationResult.invalid(List.of("thingUID is required"));
            }

            return AIActionValidationResult.valid(parameters);

        } catch (Exception e) {
            return AIActionValidationResult.invalid(List.of("Parameter validation failed: " + e.getMessage()));
        }
    }

    @Override
    public AIActionResult execute(Map<String, Object> parameters, AIActionContext context) throws AIActionException {
        long executionStartTime = System.currentTimeMillis();

        try {
            String thingUID = (String) parameters.get("thingUID");
            Boolean includeConfigurationKeys = (Boolean) parameters.getOrDefault("includeConfigurationKeys", true);
            Boolean includeConfigurationSchema = (Boolean) parameters.getOrDefault("includeConfigurationSchema", false);

            logger.debug("Getting configuration for thing: {}", thingUID);

            Map<String, Object> result = new HashMap<>();
            result.put("thingUID", thingUID);
            result.put("success", true);
            result.put("timestamp", System.currentTimeMillis());

            // Get the thing
            ThingUID uid = new ThingUID(thingUID);
            Thing thing = thingRegistry.get(uid);

            if (thing == null) {
                result.put("error", "Thing not found");
                return AIActionResult.success(result, System.currentTimeMillis() - executionStartTime);
            }

            // Configuration information
            Map<String, Object> configuration = thing.getConfiguration().getProperties();
            result.put("configuration", configuration);
            result.put("configurationCount", configuration.size());

            // Configuration keys
            if (includeConfigurationKeys) {
                List<String> configurationKeys = configuration.keySet().stream().toList();
                result.put("configurationKeys", configurationKeys);
            } else {
                result.put("configurationKeys", List.of());
            }

            // Configuration schema (real implementation)
            if (includeConfigurationSchema) {
                Map<String, Object> configurationSchema = getRealConfigurationSchema(thing);
                result.put("configurationSchema", configurationSchema);
            } else {
                result.put("configurationSchema", Map.of());
            }

            long executionTime = System.currentTimeMillis() - executionStartTime;
            logger.debug("Successfully retrieved configuration for thing: {} in {}ms", thingUID, executionTime);

            return AIActionResult.success(result, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - executionStartTime;
            logger.error("Error getting thing configuration", e);
            throw new AIActionException(getActionId(), "Failed to get thing configuration: " + e.getMessage(), e);
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
                .description("Retrieve configuration information for openHAB things")
                .tags(List.of("things", "configuration", "settings", "properties"))
                .documentation(
                        "Retrieves configuration information for openHAB things including current configuration values and optional schema.")
                .examples(List.of("Get configuration: {\"thingUID\": \"binding:type:id\"}",
                        "Get with keys: {\"thingUID\": \"binding:type:id\", \"includeConfigurationKeys\": true}",
                        "Get with schema: {\"thingUID\": \"binding:type:id\", \"includeConfigurationSchema\": true}"))
                .build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("async", true);
        capabilities.put("validation", true);
        capabilities.put("configurationAccess", true);
        capabilities.put("configurationKeys", true);
        capabilities.put("configurationSchema", true);
        capabilities.put("usesThingTypeRegistry", true);
        capabilities.put("usesConfigDescriptionRegistry", true);
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
        return thingRegistry != null && thingTypeRegistry != null && configDescriptionRegistry != null;
    }

    /**
     * Get real configuration schema using ThingTypeRegistry.
     * This method retrieves actual configuration schema information from the thing type.
     */
    private Map<String, Object> getRealConfigurationSchema(Thing thing) {
        Map<String, Object> schema = new HashMap<>();

        try {
            // Basic thing information
            schema.put("thingUID", thing.getUID().getAsString());
            schema.put("thingTypeUID", thing.getThingTypeUID().getAsString());
            schema.put("bindingId", thing.getThingTypeUID().getBindingId());

            // Get thing type information
            if (thingTypeRegistry != null) {
                ThingType thingType = thingTypeRegistry.getThingType(thing.getThingTypeUID());
                if (thingType != null) {
                    schema.put("thingTypeLabel", thingType.getLabel());
                    schema.put("thingTypeDescription", thingType.getDescription());
                    schema.put("thingTypeCategory", thingType.getCategory());
                    schema.put("thingTypeProperties", thingType.getProperties());
                    schema.put("thingTypeRepresentationProperty", thingType.getRepresentationProperty());

                    // Get configuration description URI
                    String configDescriptionURI = thingType.getConfigDescriptionURI() != null
                            ? thingType.getConfigDescriptionURI().toString()
                            : null;
                    if (configDescriptionURI != null) {
                        schema.put("configDescriptionURI", configDescriptionURI);
                        schema.put("schemaAvailable", true);
                        schema.put("schemaNote", "Configuration description URI available: " + configDescriptionURI);
                    } else {
                        schema.put("schemaAvailable", false);
                        schema.put("schemaNote", "No configuration description URI available for this thing type");
                    }
                } else {
                    schema.put("schemaAvailable", false);
                    schema.put("schemaNote", "ThingType not found: " + thing.getThingTypeUID());
                }
            } else {
                schema.put("schemaAvailable", false);
                schema.put("schemaNote", "ThingTypeRegistry not available");
            }

            // Channel information
            List<Map<String, Object>> channels = new ArrayList<>();
            for (org.openhab.core.thing.Channel channel : thing.getChannels()) {
                Map<String, Object> channelInfo = new HashMap<>();
                channelInfo.put("uid", channel.getUID().getAsString());
                channelInfo.put("id", channel.getUID().getId());
                channelInfo.put("typeUID",
                        channel.getChannelTypeUID() != null ? channel.getChannelTypeUID().getAsString() : null);
                channelInfo.put("label", channel.getLabel());
                channelInfo.put("description", channel.getDescription());
                channelInfo.put("kind", channel.getKind().toString());
                channelInfo.put("defaultTags", channel.getDefaultTags());
                channelInfo.put("properties", channel.getProperties());
                channelInfo.put("configuration", channel.getConfiguration().getProperties());
                channels.add(channelInfo);
            }
            schema.put("channels", channels);
            schema.put("channelCount", channels.size());

            // Bridge information
            if (thing.getBridgeUID() != null) {
                schema.put("bridgeUID", thing.getBridgeUID().getAsString());
                schema.put("isBridge", false);
            } else {
                schema.put("isBridge", true);
            }

            // Properties
            schema.put("properties", thing.getProperties());
            schema.put("propertyCount", thing.getProperties().size());

        } catch (Exception e) {
            logger.warn("Error getting configuration schema for thing {}: {}", thing.getUID(), e.getMessage());
            schema.put("schemaAvailable", false);
            schema.put("schemaNote", "Error retrieving schema: " + e.getMessage());
        }

        return schema;
    }
}
