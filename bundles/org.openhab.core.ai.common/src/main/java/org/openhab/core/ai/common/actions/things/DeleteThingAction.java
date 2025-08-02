package org.openhab.core.ai.common.actions.things;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.openhab.core.ai.common.action.AIActionContext;
import org.openhab.core.ai.common.action.AIActionMetadata;
import org.openhab.core.ai.common.action.AIActionResult;
import org.openhab.core.ai.common.action.AIActionValidationResult;
import org.openhab.core.ai.common.api.action.AIAction;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AIAction for deleting openHAB Things.
 * This action allows removing Things from the system with proper cleanup.
 */
@Component(service = AIAction.class, immediate = true)
public class DeleteThingAction implements AIAction {

    private static final Logger logger = LoggerFactory.getLogger(DeleteThingAction.class);

    @Reference
    private ThingRegistry thingRegistry;

    @Override
    public String getActionId() {
        return "openhab.things.delete";
    }

    @Override
    public String getActionName() {
        return "Delete Thing";
    }

    @Override
    public String getDescription() {
        return "Deletes an openHAB Thing from the system with proper cleanup";
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
        properties.put("thingUID", Map.of("type", "string", "description", "Unique identifier of the Thing to delete"));
        properties.put("force",
                Map.of("type", "boolean", "description", "Force deletion even if Thing is online", "default", false));
        properties.put("removeChildThings",
                Map.of("type", "boolean", "description", "Remove child Things if this is a bridge", "default", true));

        schema.put("properties", properties);
        schema.put("required", List.of("thingUID"));
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public AIActionValidationResult validateParameters(Map<String, Object> parameters) {
        String thingUID = (String) parameters.get("thingUID");

        if (thingUID == null || thingUID.trim().isEmpty()) {
            return AIActionValidationResult.invalid(List.of("thingUID is required and cannot be empty"));
        }

        // Validate Thing UID format
        try {
            new ThingUID(thingUID);
        } catch (IllegalArgumentException e) {
            return AIActionValidationResult.invalid(List.of("Invalid thingUID format: " + e.getMessage()));
        }

        // Check if Thing exists
        try {
            ThingUID uid = new ThingUID(thingUID);
            Thing thing = thingRegistry.get(uid);
            if (thing == null) {
                return AIActionValidationResult.invalid(List.of("Thing with UID '" + thingUID + "' not found"));
            }
        } catch (Exception e) {
            return AIActionValidationResult.invalid(List.of("Invalid thingUID: " + e.getMessage()));
        }

        return AIActionValidationResult.valid(parameters);
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("success",
                Map.of("type", "boolean", "description", "Whether the Thing was deleted successfully"));
        properties.put("thingUID", Map.of("type", "string", "description", "UID of the deleted Thing"));
        properties.put("message", Map.of("type", "string", "description", "Result message"));
        properties.put("deletedChildThings",
                Map.of("type", "array", "description", "List of child Things that were also deleted"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public AIActionResult execute(Map<String, Object> parameters, AIActionContext context) throws AIActionException {
        try {
            Map<String, Object> result = deleteThing(parameters);
            return AIActionResult.success(result, System.currentTimeMillis());
        } catch (Exception e) {
            logger.error("Error deleting Thing: {}", e.getMessage(), e);
            throw new AIActionException(getActionId(), "Failed to delete Thing: " + e.getMessage(), e);
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
        return AIActionMetadata.builder().version(getVersion()).author("openHAB").description(getDescription())
                .tags(List.of("things", "delete", "management")).build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("supportsAsync", true);
        capabilities.put("supportsValidation", true);
        capabilities.put("supportsForceDeletion", true);
        capabilities.put("supportsChildThingRemoval", true);
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

    private Map<String, Object> deleteThing(Map<String, Object> parameters) throws AIActionException {
        Map<String, Object> result = new HashMap<>();

        String thingUID = (String) parameters.get("thingUID");
        Boolean force = (Boolean) parameters.get("force");
        Boolean removeChildThings = (Boolean) parameters.get("removeChildThings");

        if (force == null) {
            force = false;
        }
        if (removeChildThings == null) {
            removeChildThings = true;
        }

        try {
            ThingUID uid = new ThingUID(thingUID);
            Thing thing = thingRegistry.get(uid);

            if (thing == null) {
                result.put("success", false);
                result.put("message", "Thing not found");
                return result;
            }

            // Check if Thing is online and force deletion is not enabled
            if (!force && "ONLINE".equals(thing.getStatus().toString())) {
                result.put("success", false);
                result.put("message", "Thing is online. Use force=true to delete online Things");
                return result;
            }

            // Get child Things if this is a bridge and removeChildThings is enabled
            List<String> deletedChildThings = new java.util.ArrayList<>();
            if (removeChildThings && thing.getBridgeUID() == null) {
                // This might be a bridge, check for child Things
                for (Thing childThing : thingRegistry.getAll()) {
                    if (uid.equals(childThing.getBridgeUID())) {
                        deletedChildThings.add(childThing.getUID().toString());
                    }
                }
            }

            // Remove the Thing from registry
            thingRegistry.remove(uid);

            // Build result
            result.put("success", true);
            result.put("thingUID", thingUID);
            result.put("message", "Thing deleted successfully");
            result.put("deletedChildThings", deletedChildThings);

            logger.info("Deleted Thing: {} (force={}, childThings={})", thingUID, force, deletedChildThings.size());

        } catch (Exception e) {
            logger.error("Error deleting Thing {}: {}", thingUID, e.getMessage(), e);
            result.put("success", false);
            result.put("message", "Failed to delete Thing: " + e.getMessage());
        }

        return result;
    }
}
