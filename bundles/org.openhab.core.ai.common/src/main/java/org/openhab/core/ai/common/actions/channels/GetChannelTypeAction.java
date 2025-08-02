package org.openhab.core.ai.common.actions.channels;

import java.time.Instant;
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
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action for retrieving channel type information in openHAB.
 * 
 * This action provides functionality to get detailed information
 * about channel types including their properties and capabilities.
 * 
 * @author AI Assistant
 * @since 1.0.0
 */
@NonNullByDefault
public class GetChannelTypeAction implements AIAction {

    private static final Logger logger = LoggerFactory.getLogger(GetChannelTypeAction.class);
    private static final String ACTION_ID = "openhab.channels.get-type";
    private static final String ACTION_NAME = "Get Channel Type";
    private static final String CATEGORY = "channels";

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
        return "Retrieves detailed information about a channel type including properties and capabilities";
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
        properties.put("channelTypeUID", Map.of("type", "string", "description",
                "The UID of the channel type to retrieve (e.g., 'binding:channelType')"));
        properties.put("includeProperties",
                Map.of("type", "boolean", "description", "Include channel type properties", "default", true));
        properties.put("includeStateDescription",
                Map.of("type", "boolean", "description", "Include state description details", "default", true));

        schema.put("properties", properties);
        schema.put("required", List.of("channelTypeUID"));
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("channelTypeUID", Map.of("type", "string", "description", "The channel type UID"));
        properties.put("found", Map.of("type", "boolean", "description", "Whether the channel type was found"));
        properties.put("basicInfo", Map.of("type", "object", "description", "Basic channel type information"));
        properties.put("properties", Map.of("type", "object", "description", "Channel type properties"));
        properties.put("stateDescription", Map.of("type", "object", "description", "State description details"));
        properties.put("metadata", Map.of("type", "object", "description", "Channel type metadata"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("channel_type_info", true);
        capabilities.put("type_properties", true);
        capabilities.put("state_description", true);
        capabilities.put("metadata_access", true);
        return capabilities;
    }

    @Override
    public AIActionValidationResult validateParameters(Map<String, Object> parameters) {
        List<String> errors = new ArrayList<>();

        if (!parameters.containsKey("channelTypeUID")) {
            errors.add("channelTypeUID is required");
        } else {
            Object channelTypeUID = parameters.get("channelTypeUID");
            if (!(channelTypeUID instanceof String) || ((String) channelTypeUID).trim().isEmpty()) {
                errors.add("channelTypeUID must be a non-empty string");
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
        logger.debug("Executing GetChannelTypeAction with context: {}", context.getProtocol());

        long startTime = System.currentTimeMillis();

        try {
            String channelTypeUID = (String) parameters.get("channelTypeUID");
            boolean includeProperties = (Boolean) parameters.getOrDefault("includeProperties", true);
            boolean includeStateDescription = (Boolean) parameters.getOrDefault("includeStateDescription", true);

            Map<String, Object> result = getChannelTypeInfo(channelTypeUID, includeProperties, includeStateDescription);

            long executionTime = System.currentTimeMillis() - startTime;
            return AIActionResult.success(result, executionTime);

        } catch (Exception e) {
            logger.error("Error executing GetChannelTypeAction: {}", e.getMessage(), e);
            throw new AIActionException(ACTION_ID, "Failed to get channel type: " + e.getMessage(), e);
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
                .description("Retrieves detailed information about a channel type").version("1.0.0")
                .tags(List.of("channels", "types", "things")).build();
    }

    @Override
    public void initialize(AIActionContext context) {
        logger.debug("GetChannelTypeAction initialized with context: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("GetChannelTypeAction cleanup completed");
    }

    @Override
    public boolean isReady() {
        return channelTypeRegistry != null;
    }

    private Map<String, Object> getChannelTypeInfo(String channelTypeUID, boolean includeProperties,
            boolean includeStateDescription) {

        Map<String, Object> result = new HashMap<>();
        result.put("channelTypeUID", channelTypeUID);
        result.put("found", false);

        try {
            ChannelTypeUID uid = new ChannelTypeUID(channelTypeUID);
            ChannelType channelType = channelTypeRegistry.getChannelType(uid);

            if (channelType == null) {
                result.put("error", "Channel type not found: " + channelTypeUID);
                return result;
            }

            result.put("found", true);

            // Basic information
            Map<String, Object> basicInfo = new HashMap<>();
            basicInfo.put("uid", channelType.getUID().toString());
            String channelTypeLabel = "";
            if (channelType.getLabel() != null) {
                channelTypeLabel = channelType.getLabel();
            }
            basicInfo.put("label", channelTypeLabel);
            String channelTypeDescription = "";
            if (channelType.getDescription() != null) {
                channelTypeDescription = channelType.getDescription();
            }
            basicInfo.put("description", channelTypeDescription);
            String channelTypeCategory = "";
            if (channelType.getCategory() != null) {
                channelTypeCategory = channelType.getCategory();
            }
            basicInfo.put("category", channelTypeCategory);
            basicInfo.put("kind", channelType.getKind().toString());
            String itemType = "";
            if (channelType.getItemType() != null) {
                itemType = channelType.getItemType();
            }
            basicInfo.put("itemType", itemType);
            String autoUpdatePolicy = "";
            if (channelType.getAutoUpdatePolicy() != null) {
                autoUpdatePolicy = channelType.getAutoUpdatePolicy().toString();
            }
            basicInfo.put("autoUpdatePolicy", autoUpdatePolicy);
            result.put("basicInfo", basicInfo);

            // Properties
            if (includeProperties) {
                Map<String, Object> properties = new HashMap<>();
                // Note: ChannelType doesn't have a direct getProperties() method
                // Properties would be available through other means
                properties.put("note", "Properties require binding-specific integration");
                result.put("properties", properties);
            }

            // State description
            if (includeStateDescription && channelType.getState() != null) {
                Map<String, Object> stateDescription = new HashMap<>();
                String pattern = "";
                if (channelType.getState().getPattern() != null) {
                    pattern = channelType.getState().getPattern();
                }
                stateDescription.put("pattern", pattern);
                stateDescription.put("readOnly", channelType.getState().isReadOnly());
                stateDescription.put("options", channelType.getState().getOptions());
                result.put("stateDescription", stateDescription);
            }

            // Metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("retrievedAt", Instant.now().toString());
            metadata.put("bindingId", uid.getBindingId());
            result.put("metadata", metadata);

        } catch (Exception e) {
            logger.error("Error getting channel type info for {}: {}", channelTypeUID, e.getMessage(), e);
            result.put("error", "Error retrieving channel type information: " + e.getMessage());
        }

        return result;
    }
}
