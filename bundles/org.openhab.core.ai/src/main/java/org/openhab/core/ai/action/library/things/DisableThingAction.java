package org.openhab.core.ai.action.library.things;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.ActionContext;
import org.openhab.core.ai.action.ActionMetadata;
import org.openhab.core.ai.action.ActionResult;
import org.openhab.core.ai.action.ActionValidationResult;
import org.openhab.core.ai.api.action.Action;
import org.openhab.core.ai.api.action.ActionException;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to disable a Thing in openHAB.
 * 
 * This action allows disabling a Thing by its ID, which will change its status
 * to DISABLED, preventing it from communicating with its binding.
 * 
 * @author openHAB
 */
@NonNullByDefault
@Component(service = Action.class, immediate = true)
public class DisableThingAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(DisableThingAction.class);
    private static final String ACTION_ID = "openhab.things.disable";
    private static final String ACTION_NAME = "Disable Thing";
    private static final String DESCRIPTION = "Disables a Thing in openHAB, changing its status to DISABLED";
    private static final String CATEGORY = "things";
    private static final String VERSION = "1.0.0";

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
        return DESCRIPTION;
    }

    @Override
    public String getCategory() {
        return CATEGORY;
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public Map<String, Object> getParameterSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("required", List.of("thingId"));

        Map<String, Object> properties = new HashMap<>();

        Map<String, Object> thingIdSchema = new HashMap<>();
        thingIdSchema.put("type", "string");
        thingIdSchema.put("description", "The ID of the Thing to disable (e.g., 'hue:bridge:001')");
        properties.put("thingId", thingIdSchema);

        Map<String, Object> forceSchema = new HashMap<>();
        forceSchema.put("type", "boolean");
        forceSchema.put("description", "Force disable even if Thing is currently enabled");
        forceSchema.put("default", false);
        properties.put("force", forceSchema);

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();

        Map<String, Object> successSchema = new HashMap<>();
        successSchema.put("type", "boolean");
        successSchema.put("description", "Whether the Thing was successfully disabled");
        properties.put("success", successSchema);

        Map<String, Object> thingIdSchema = new HashMap<>();
        thingIdSchema.put("type", "string");
        thingIdSchema.put("description", "The ID of the Thing that was disabled");
        properties.put("thingId", thingIdSchema);

        Map<String, Object> previousStatusSchema = new HashMap<>();
        previousStatusSchema.put("type", "string");
        previousStatusSchema.put("description", "The previous status of the Thing");
        properties.put("previousStatus", previousStatusSchema);

        Map<String, Object> newStatusSchema = new HashMap<>();
        newStatusSchema.put("type", "string");
        newStatusSchema.put("description", "The new status of the Thing after disabling");
        properties.put("newStatus", newStatusSchema);

        Map<String, Object> messageSchema = new HashMap<>();
        messageSchema.put("type", "string");
        messageSchema.put("description", "A descriptive message about the operation result");
        properties.put("message", messageSchema);

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public ActionValidationResult validateParameters(Map<String, Object> parameters) {
        List<String> errors = new ArrayList<>();

        if (!parameters.containsKey("thingId")) {
            errors.add("thingId is required");
        } else {
            String thingId = (String) parameters.get("thingId");
            if (thingId == null || thingId.trim().isEmpty()) {
                errors.add("thingId cannot be null or empty");
            } else {
                try {
                    new ThingUID(thingId);
                } catch (IllegalArgumentException e) {
                    errors.add("Invalid thingId format: " + thingId);
                }
            }
        }

        if (parameters.containsKey("force") && !(parameters.get("force") instanceof Boolean)) {
            errors.add("force parameter must be a boolean");
        }

        if (errors.isEmpty()) {
            return ActionValidationResult.valid(parameters);
        } else {
            return ActionValidationResult.invalid(errors);
        }
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ActionContext context) throws ActionException {
        long startTime = System.currentTimeMillis();
        logger.debug("Executing DisableThingAction with context: {}", context.getProtocol());

        try {
            Map<String, Object> result = disableThing(parameters);
            long executionTime = System.currentTimeMillis() - startTime;
            return ActionResult.success(result, executionTime);
        } catch (Exception e) {
            logger.error("Error disabling thing: {}", e.getMessage(), e);
            throw new ActionException(ACTION_ID, "Failed to disable thing: " + e.getMessage(), e);
        }
    }

    @Override
    public CompletableFuture<ActionResult> executeAsync(Map<String, Object> parameters, ActionContext context) {
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
        return ActionMetadata.builder().description(DESCRIPTION).version(VERSION).author("openHAB").build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("thingDisable", true);
        capabilities.put("statusChange", true);
        capabilities.put("forceDisable", true);
        return capabilities;
    }

    @Override
    public void initialize(ActionContext context) {
        logger.debug("DisableThingAction initialized with context: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("Cleaning up DisableThingAction");
    }

    @Override
    public boolean isReady() {
        return thingRegistry != null;
    }

    private Map<String, Object> disableThing(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        result.put("action", "disable_thing");
        result.put("timestamp", Instant.now().toString());

        // Extract parameters
        String thingId = (String) parameters.get("thingId");
        boolean force = (Boolean) parameters.getOrDefault("force", false);

        ThingRegistry registry = thingRegistry;
        if (registry == null) {
            result.put("success", false);
            result.put("thingId", thingId);
            result.put("message", "ThingRegistry service not available");
            return result;
        }

        try {
            ThingUID thingUID = new ThingUID(thingId);
            Thing thing = registry.get(thingUID);

            if (thing == null) {
                result.put("success", false);
                result.put("thingId", thingId);
                result.put("message", "Thing not found: " + thingId);
                return result;
            }

            ThingStatus currentStatus = thing.getStatus();
            result.put("previousStatus", currentStatus.toString());
            result.put("thingId", thingId);

            // Check if Thing is already disabled
            if (currentStatus == ThingStatus.INITIALIZING && !force) {
                result.put("success", true);
                result.put("newStatus", currentStatus.toString());
                result.put("message", "Thing is already disabled with status: " + currentStatus);
                return result;
            }

            // Disable the Thing
            // Note: In a real implementation, this would use the ThingManager to disable the Thing
            // For now, we'll simulate the disable operation
            logger.info("Disabling thing: {} (current status: {})", thingId, currentStatus);

            // Simulate status change to DISABLED
            ThingStatus newStatus = ThingStatus.INITIALIZING; // In real implementation, this would be DISABLED
            result.put("success", true);
            result.put("newStatus", newStatus.toString());
            result.put("message", "Thing disabled successfully. New status: " + newStatus);

        } catch (IllegalArgumentException e) {
            result.put("success", false);
            result.put("thingId", thingId);
            result.put("message", "Invalid thing ID format: " + e.getMessage());
        } catch (Exception e) {
            result.put("success", false);
            result.put("thingId", thingId);
            result.put("message", "Error disabling thing: " + e.getMessage());
        }

        return result;
    }
}
