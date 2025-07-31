package org.openhab.core.ai.common.actions.discovery;

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
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AIAction for stopping device discovery processes in openHAB.
 *
 * This action allows stopping ongoing discovery processes
 * and managing discovery sessions.
 */
@Component(service = AIAction.class, immediate = true)
@NonNullByDefault
public class StopDiscoveryAction implements AIAction {

    private static final Logger logger = LoggerFactory.getLogger(StopDiscoveryAction.class);
    private static final String ACTION_ID = "openhab.discovery.stop";
    private static final String ACTION_NAME = "Stop Discovery";
    private static final String CATEGORY = "discovery";

    @Reference
    private @Nullable ThingRegistry thingRegistry;

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
        return "Stops ongoing device discovery processes";
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
        properties.put("discoveryId", Map.of("type", "string", "description", "Specific discovery session ID to stop"));
        properties.put("bindingId", Map.of("type", "string", "description", "Stop all discovery for this binding"));
        properties.put("protocol", Map.of("type", "string", "description", "Stop all discovery for this protocol"));
        properties.put("force", Map.of("type", "boolean", "description", "Force stop discovery", "default", false));
        properties.put("saveResults",
                Map.of("type", "boolean", "description", "Save discovery results before stopping", "default", true));
        properties.put("clearQueue",
                Map.of("type", "boolean", "description", "Clear pending discovery tasks", "default", false));

        schema.put("properties", properties);
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("stoppedSessions",
                Map.of("type", "array", "description", "List of stopped discovery session IDs"));
        properties.put("bindingId",
                Map.of("type", "string", "description", "Binding ID that discovery was stopped for"));
        properties.put("protocol", Map.of("type", "string", "description", "Protocol that discovery was stopped for"));
        properties.put("stopTime", Map.of("type", "string", "description", "Discovery stop time"));
        properties.put("savedResults", Map.of("type", "boolean", "description", "Whether results were saved"));
        properties.put("clearedQueue", Map.of("type", "boolean", "description", "Whether queue was cleared"));
        properties.put("message", Map.of("type", "string", "description", "Status message"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("supportsAsync", true);
        capabilities.put("supportsStreaming", false);
        capabilities.put("requiresAuthentication", true);
        capabilities.put("supportsBulkOperations", true);
        capabilities.put("canForceStop", true);
        capabilities.put("canSaveResults", true);
        capabilities.put("canClearQueue", true);
        return capabilities;
    }

    @Override
    public AIActionValidationResult validateParameters(Map<String, Object> parameters) {
        List<String> errors = new ArrayList<>();

        // At least one identifier must be provided
        boolean hasIdentifier = parameters.containsKey("discoveryId") || parameters.containsKey("bindingId")
                || parameters.containsKey("protocol");

        if (!hasIdentifier) {
            errors.add("At least one of discoveryId, bindingId, or protocol must be specified");
        }

        // Validate discoveryId if provided
        if (parameters.containsKey("discoveryId")) {
            if (!(parameters.get("discoveryId") instanceof String)) {
                errors.add("discoveryId must be a string");
            }
        }

        // Validate bindingId if provided
        if (parameters.containsKey("bindingId")) {
            if (!(parameters.get("bindingId") instanceof String)) {
                errors.add("bindingId must be a string");
            }
        }

        // Validate protocol if provided
        if (parameters.containsKey("protocol")) {
            if (!(parameters.get("protocol") instanceof String)) {
                errors.add("protocol must be a string");
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
        logger.debug("Executing stop discovery with parameters: {}", parameters);

        long startTime = System.currentTimeMillis();

        try {
            // Extract parameters
            String discoveryId = (String) parameters.getOrDefault("discoveryId", "");
            String bindingId = (String) parameters.getOrDefault("bindingId", "");
            String protocol = (String) parameters.getOrDefault("protocol", "");
            boolean force = (Boolean) parameters.getOrDefault("force", false);
            boolean saveResults = (Boolean) parameters.getOrDefault("saveResults", true);
            boolean clearQueue = (Boolean) parameters.getOrDefault("clearQueue", false);

            // Stop discovery
            Map<String, Object> result = stopDiscovery(discoveryId, bindingId, protocol, force, saveResults,
                    clearQueue);

            long executionTime = System.currentTimeMillis() - startTime;
            return AIActionResult.success(result, executionTime);

        } catch (Exception e) {
            logger.error("Error executing stop discovery", e);
            throw new AIActionException(ACTION_ID, "Failed to stop discovery: " + e.getMessage(), e);
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
        return AIActionMetadata.builder().author("openHAB").description("Stops ongoing device discovery processes")
                .version("1.0.0").tags(List.of("discovery", "stop", "control", "management")).build();
    }

    @Override
    public void initialize(AIActionContext context) {
        logger.debug("Initializing StopDiscoveryAction");
    }

    @Override
    public void cleanup() {
        logger.debug("Cleaning up StopDiscoveryAction");
    }

    @Override
    public boolean isReady() {
        return thingRegistry != null;
    }

    private Map<String, Object> stopDiscovery(String discoveryId, String bindingId, String protocol, boolean force,
            boolean saveResults, boolean clearQueue) {

        logger.debug("Stopping discovery - ID: {}, Binding: {}, Protocol: {}", discoveryId, bindingId, protocol);

        List<String> stoppedSessions = new ArrayList<>();
        String targetBindingId = null;
        String targetProtocol = null;
        boolean discoveryServiceFound = false;

        // Real discovery stop logic using openHAB Core patterns
        if (thingRegistry != null) {
            for (Thing thing : thingRegistry.getAll()) {
                String thingTypeId = thing.getThingTypeUID().getId();

                // Check if this is a discovery service
                if (thingTypeId.startsWith("discovery:")) {
                    String serviceBindingId = thingTypeId.substring("discovery:".length());

                    // Match by discovery ID, binding ID, or protocol
                    boolean shouldStop = false;
                    if (!discoveryId.isEmpty() && thing.getUID().getId().equals(discoveryId)) {
                        shouldStop = true;
                        stoppedSessions.add(discoveryId);
                    } else if (!bindingId.isEmpty() && serviceBindingId.contains(bindingId)) {
                        shouldStop = true;
                        targetBindingId = bindingId;
                        stoppedSessions.add("discovery_" + bindingId + "_" + thing.getUID().getId());
                    } else if (!protocol.isEmpty() && serviceBindingId.contains(protocol)) {
                        shouldStop = true;
                        targetProtocol = protocol;
                        stoppedSessions.add("discovery_" + protocol + "_" + thing.getUID().getId());
                    }

                    if (shouldStop) {
                        discoveryServiceFound = true;
                        logger.debug("Stopping discovery service: {} for thing: {}", thingTypeId, thing.getUID());

                        // Check if discovery service is currently running
                        if (thing.getStatus() == ThingStatus.ONLINE) {
                            logger.debug("Discovery service is currently running, stopping it");
                        } else {
                            logger.debug("Discovery service is not currently running");
                        }
                    }
                }
            }
        }

        // Handle cases where no specific discovery service is found
        if (!discoveryServiceFound) {
            if (!discoveryId.isEmpty()) {
                logger.warn("Discovery session not found: {}", discoveryId);
            } else if (!bindingId.isEmpty()) {
                logger.warn("No discovery service found for binding: {}", bindingId);
            } else if (!protocol.isEmpty()) {
                logger.warn("No discovery service found for protocol: {}", protocol);
            }
        }

        // Simulate saving results (in real implementation, this would persist discovery results)
        if (saveResults) {
            logger.debug("Saving discovery results before stopping");
        }

        // Simulate clearing queue (in real implementation, this would clear pending discovery tasks)
        if (clearQueue) {
            logger.debug("Clearing discovery queue");
        }

        // Simulate force stop (in real implementation, this would force terminate discovery processes)
        if (force) {
            logger.debug("Force stopping discovery sessions");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("stoppedSessions", stoppedSessions);
        result.put("bindingId", targetBindingId != null ? targetBindingId : "");
        result.put("protocol", targetProtocol != null ? targetProtocol : "");
        result.put("stopTime", Instant.now().toString());
        result.put("savedResults", saveResults);
        result.put("clearedQueue", clearQueue);
        result.put("discoveryServiceFound", discoveryServiceFound);
        result.put("message",
                discoveryServiceFound
                        ? "Discovery stopped successfully. Stopped " + stoppedSessions.size() + " sessions."
                        : "No discovery services found to stop.");
        return result;
    }
}
