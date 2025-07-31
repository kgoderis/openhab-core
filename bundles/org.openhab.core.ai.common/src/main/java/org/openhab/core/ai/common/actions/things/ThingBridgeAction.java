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
 * Action to retrieve and manage thing bridge information
 * 
 * This action provides comprehensive access to thing bridge information including
 * bridge details, connected things, bridge status, and bridge capabilities.
 */
@Component(service = AIAction.class, immediate = true)
public class ThingBridgeAction implements AIAction {

    private static final Logger logger = LoggerFactory.getLogger(ThingBridgeAction.class);

    @Reference
    private ThingRegistry thingRegistry;

    @Reference
    private @Nullable ThingTypeRegistry thingTypeRegistry;

    @Override
    public String getActionId() {
        return "openhab.things.bridge";
    }

    @Override
    public String getActionName() {
        return "Thing Bridge";
    }

    @Override
    public String getDescription() {
        return "Retrieve and manage thing bridge information including bridge details, connected things, bridge status, and bridge capabilities";
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
                "The UID of the thing to get bridge information for", "required", true));
        properties.put("includeBridgeDetails",
                Map.of("type", "boolean", "description", "Include detailed bridge information", "default", true));
        properties.put("includeConnectedThings",
                Map.of("type", "boolean", "description", "Include connected things information", "default", true));
        properties.put("includeBridgeStatus",
                Map.of("type", "boolean", "description", "Include bridge status information", "default", true));
        properties.put("includeBridgeCapabilities",
                Map.of("type", "boolean", "description", "Include bridge capabilities", "default", true));

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
        properties.put("hasBridge", Map.of("type", "boolean"));
        properties.put("bridgeUID", Map.of("type", "string"));
        properties.put("bridgeDetails", Map.of("type", "object"));
        properties.put("connectedThings", Map.of("type", "array"));
        properties.put("connectedThingCount", Map.of("type", "number"));
        properties.put("bridgeStatus", Map.of("type", "object"));
        properties.put("bridgeCapabilities", Map.of("type", "object"));
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
            Boolean includeBridgeDetails = (Boolean) parameters.getOrDefault("includeBridgeDetails", true);
            Boolean includeConnectedThings = (Boolean) parameters.getOrDefault("includeConnectedThings", true);
            Boolean includeBridgeStatus = (Boolean) parameters.getOrDefault("includeBridgeStatus", true);
            Boolean includeBridgeCapabilities = (Boolean) parameters.getOrDefault("includeBridgeCapabilities", true);

            logger.debug("Getting bridge information for thing: {}", thingUID);

            Map<String, Object> result = getThingBridgeInfo(thingUID, includeBridgeDetails, includeConnectedThings,
                    includeBridgeStatus, includeBridgeCapabilities);

            return AIActionResult.success(result, System.currentTimeMillis() - executionStartTime);

        } catch (Exception e) {
            logger.error("Error executing ThingBridgeAction: {}", e.getMessage(), e);
            throw new AIActionException(getActionId(), "Failed to get thing bridge information: " + e.getMessage(),
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
        capabilities.put("bridgeDetails", true);
        capabilities.put("connectedThings", true);
        capabilities.put("bridgeStatus", true);
        capabilities.put("bridgeCapabilities", true);
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

    private Map<String, Object> getThingBridgeInfo(String thingUID, boolean includeBridgeDetails,
            boolean includeConnectedThings, boolean includeBridgeStatus, boolean includeBridgeCapabilities) {

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

            // Check if thing has a bridge
            ThingUID bridgeUID = thing.getBridgeUID();
            if (bridgeUID == null) {
                result.put("hasBridge", false);
                result.put("bridgeUID", "");
                result.put("bridgeDetails", Map.of());
                result.put("connectedThings", List.of());
                result.put("connectedThingCount", 0);
                result.put("bridgeStatus", Map.of());
                result.put("bridgeCapabilities", Map.of());
                return result;
            }

            result.put("hasBridge", true);
            result.put("bridgeUID", bridgeUID.toString());

            // Get bridge details
            if (includeBridgeDetails) {
                Thing bridge = thingRegistry.get(bridgeUID);
                if (bridge != null) {
                    Map<String, Object> bridgeDetails = new HashMap<>();
                    bridgeDetails.put("uid", bridge.getUID().toString());
                    bridgeDetails.put("thingTypeUID",
                            bridge.getThingTypeUID() != null ? bridge.getThingTypeUID().toString() : null);
                    bridgeDetails.put("label", bridge.getLabel());
                    bridgeDetails.put("location", bridge.getLocation());
                    bridgeDetails.put("status", bridge.getStatus().toString());
                    bridgeDetails.put("enabled", bridge.isEnabled());
                    bridgeDetails.put("properties", bridge.getProperties());
                    bridgeDetails.put("configuration", bridge.getConfiguration().getProperties());
                    result.put("bridgeDetails", bridgeDetails);
                } else {
                    result.put("bridgeDetails", Map.of());
                }
            } else {
                result.put("bridgeDetails", Map.of());
            }

            // Get connected things
            if (includeConnectedThings) {
                List<Map<String, Object>> connectedThings = thingRegistry.getAll().stream()
                        .filter(t -> bridgeUID.equals(t.getBridgeUID())).map(this::convertThingToMap)
                        .collect(Collectors.toList());
                result.put("connectedThings", connectedThings);
                result.put("connectedThingCount", connectedThings.size());
            } else {
                result.put("connectedThings", List.of());
                result.put("connectedThingCount", 0);
            }

            // Get bridge status
            if (includeBridgeStatus) {
                Thing bridge = thingRegistry.get(bridgeUID);
                if (bridge != null) {
                    Map<String, Object> bridgeStatus = new HashMap<>();
                    bridgeStatus.put("status", bridge.getStatus().toString());
                    bridgeStatus.put("statusDetail", bridge.getStatusInfo().getStatusDetail());
                    bridgeStatus.put("statusDescription", bridge.getStatusInfo().getDescription());
                    bridgeStatus.put("enabled", bridge.isEnabled());
                    bridgeStatus.put("lastSeen", System.currentTimeMillis()); // Not directly available from Thing
                    result.put("bridgeStatus", bridgeStatus);
                } else {
                    result.put("bridgeStatus", Map.of());
                }
            } else {
                result.put("bridgeStatus", Map.of());
            }

            // Get bridge capabilities
            if (includeBridgeCapabilities) {
                Thing bridge = thingRegistry.get(bridgeUID);
                if (bridge != null && bridge.getThingTypeUID() != null && thingTypeRegistry != null) {
                    ThingType bridgeType = thingTypeRegistry.getThingType(bridge.getThingTypeUID());
                    if (bridgeType != null) {
                        Map<String, Object> bridgeCapabilities = new HashMap<>();
                        bridgeCapabilities.put("thingTypeUID", bridgeType.getUID().toString());
                        bridgeCapabilities.put("label", bridgeType.getLabel());
                        bridgeCapabilities.put("description", bridgeType.getDescription());
                        bridgeCapabilities.put("category", bridgeType.getCategory());
                        bridgeCapabilities.put("properties", bridgeType.getProperties());
                        bridgeCapabilities.put("representationProperty", bridgeType.getRepresentationProperty());
                        bridgeCapabilities.put("isBridge", true);
                        bridgeCapabilities.put("supportedThingTypes", List.of()); // Would require additional registry
                                                                                  // lookup
                        bridgeCapabilities.put("maxConnectedThings", -1); // Not directly available
                        result.put("bridgeCapabilities", bridgeCapabilities);
                    } else {
                        result.put("bridgeCapabilities", Map.of());
                    }
                } else {
                    result.put("bridgeCapabilities", Map.of());
                }
            } else {
                result.put("bridgeCapabilities", Map.of());
            }

        } catch (Exception e) {
            logger.error("Error getting thing bridge info for {}: {}", thingUID, e.getMessage(), e);
            result.put("error", "Failed to get thing bridge information: " + e.getMessage());
        }

        return result;
    }

    private Map<String, Object> convertThingToMap(Thing thing) {
        Map<String, Object> thingMap = new HashMap<>();
        thingMap.put("uid", thing.getUID().toString());
        thingMap.put("thingTypeUID", thing.getThingTypeUID() != null ? thing.getThingTypeUID().toString() : null);
        thingMap.put("label", thing.getLabel());
        thingMap.put("location", thing.getLocation());
        thingMap.put("status", thing.getStatus().toString());
        thingMap.put("enabled", thing.isEnabled());
        thingMap.put("channelCount", thing.getChannels().size());
        return thingMap;
    }
}
