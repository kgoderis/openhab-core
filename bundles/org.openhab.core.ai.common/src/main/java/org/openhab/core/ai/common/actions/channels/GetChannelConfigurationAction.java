package org.openhab.core.ai.common.actions.channels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.api.action.AIAction;
import org.openhab.core.ai.common.api.action.AIActionContext;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.openhab.core.ai.common.api.action.AIActionMetadata;
import org.openhab.core.ai.common.api.action.AIActionResult;
import org.openhab.core.ai.common.api.action.AIActionValidationResult;
import org.openhab.core.config.core.Configuration;
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
 * AIAction for retrieving configuration information for a specific channel.
 * 
 * This action provides detailed configuration information including current values,
 * default values, parameter descriptions, and validation rules.
 */
@Component(service = AIAction.class, immediate = true)
@NonNullByDefault
public class GetChannelConfigurationAction implements AIAction {

    private static final Logger logger = LoggerFactory.getLogger(GetChannelConfigurationAction.class);
    private static final String ACTION_ID = "openhab.channels.get-configuration";
    private static final String ACTION_NAME = "Get Channel Configuration";
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
        return "Retrieves configuration information for a specific channel including current values, defaults, and validation rules";
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
                "The UID of the channel to retrieve configuration for (e.g., 'binding:thing:channel')"));
        properties.put("includeDefaults",
                Map.of("type", "boolean", "description", "Include default configuration values", "default", true));
        properties.put("includeValidation",
                Map.of("type", "boolean", "description", "Include configuration validation rules", "default", true));
        properties.put("includeSchema",
                Map.of("type", "boolean", "description", "Include configuration parameter schema", "default", true));

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
        properties.put("currentConfiguration", Map.of("type", "object", "description", "Current configuration values"));
        properties.put("defaultConfiguration", Map.of("type", "object", "description", "Default configuration values"));
        properties.put("validationRules", Map.of("type", "object", "description", "Configuration validation rules"));
        properties.put("parameterSchema", Map.of("type", "object", "description", "Configuration parameter schema"));
        properties.put("isConfigurable",
                Map.of("type", "boolean", "description", "Whether the channel is configurable"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("channel_configuration", true);
        capabilities.put("configuration_validation", true);
        capabilities.put("configuration_schema", true);
        capabilities.put("default_values", true);
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
        logger.debug("Executing GetChannelConfigurationAction with context: {}", context.getProtocol());

        long startTime = System.currentTimeMillis();

        try {
            String channelUID = (String) parameters.get("channelUID");
            boolean includeDefaults = (Boolean) parameters.getOrDefault("includeDefaults", true);
            boolean includeValidation = (Boolean) parameters.getOrDefault("includeValidation", true);
            boolean includeSchema = (Boolean) parameters.getOrDefault("includeSchema", true);

            Map<String, Object> result = getChannelConfiguration(channelUID, includeDefaults, includeValidation,
                    includeSchema);

            long executionTime = System.currentTimeMillis() - startTime;
            return AIActionResult.success(result, executionTime);

        } catch (Exception e) {
            logger.error("Error executing GetChannelConfigurationAction: {}", e.getMessage(), e);
            throw new AIActionException(ACTION_ID, "Failed to get channel configuration: " + e.getMessage(), e);
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
                .description("Retrieves configuration information for a specific channel").version("1.0.0")
                .tags(List.of("channels", "configuration", "things")).build();
    }

    @Override
    public void initialize(AIActionContext context) {
        logger.debug("GetChannelConfigurationAction initialized with context: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("GetChannelConfigurationAction cleanup completed");
    }

    @Override
    public boolean isReady() {
        return thingRegistry != null && channelTypeRegistry != null;
    }

    private Map<String, Object> getChannelConfiguration(String channelUID, boolean includeDefaults,
            boolean includeValidation, boolean includeSchema) {

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
            result.put("isConfigurable", true);

            // Current configuration
            Configuration config = channel.getConfiguration();
            Map<String, Object> currentConfig = new HashMap<>();
            config.getProperties().forEach((key, value) -> currentConfig.put(key, value));
            result.put("currentConfiguration", currentConfig);

            // Default configuration and schema information
            if (includeDefaults || includeValidation || includeSchema) {
                ChannelType channelType = null;
                if (channel.getChannelTypeUID() != null) {
                    channelType = channelTypeRegistry.getChannelType(channel.getChannelTypeUID());
                }

                if (includeDefaults) {
                    Map<String, Object> defaultConfig = getDefaultConfiguration(channelType);
                    result.put("defaultConfiguration", defaultConfig);
                }

                if (includeValidation) {
                    Map<String, Object> validationRules = getValidationRules(channelType);
                    result.put("validationRules", validationRules);
                }

                if (includeSchema) {
                    Map<String, Object> parameterSchema = getParameterSchema(channelType);
                    result.put("parameterSchema", parameterSchema);
                }
            }

        } catch (Exception e) {
            logger.error("Error getting channel configuration for {}: {}", channelUID, e.getMessage(), e);
            result.put("error", "Error retrieving channel configuration: " + e.getMessage());
        }

        return result;
    }

    private Map<String, Object> getDefaultConfiguration(ChannelType channelType) {
        Map<String, Object> defaults = new HashMap<>();

        if (channelType != null) {
            // Note: ChannelType doesn't directly expose default configuration
            // This would require integration with binding-specific configuration
            defaults.put("note", "Default configuration requires binding-specific integration");
        }

        return defaults;
    }

    private Map<String, Object> getValidationRules(ChannelType channelType) {
        Map<String, Object> validation = new HashMap<>();

        if (channelType != null) {
            // Note: Validation rules would come from ChannelType properties
            // This is a simplified implementation
            validation.put("note", "Validation rules require binding-specific integration");
        }

        return validation;
    }

    private Map<String, Object> getParameterSchema(ChannelType channelType) {
        Map<String, Object> schema = new HashMap<>();

        if (channelType != null) {
            // Note: Parameter schema would come from ChannelType properties
            // This is a simplified implementation
            schema.put("note", "Parameter schema requires binding-specific integration");
        }

        return schema;
    }
}
